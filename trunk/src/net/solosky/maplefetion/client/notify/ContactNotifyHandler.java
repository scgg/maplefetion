 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /**
 * Project  : MapleFetion
 * Package  : net.solosky.maplefetion.protocol.notify
 * File     : ContactsInfoSIPNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.client.ResponseHandler;
import net.solosky.maplefetion.client.response.GetContactInfoResponseHandler;
import net.solosky.maplefetion.event.notify.BuddyApplicationEvent;
import net.solosky.maplefetion.event.notify.BuddyConfirmedEvent;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.UriHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ContactNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
    	if(notify.getBody()==null)	return;
    	Element root  = XMLHelper.build(notify.getBody().toSendString());
    	Element event = XMLHelper.find(root, "/results/event"); 
    	if(event==null)		return;
    	String eventType = event.getAttributeValue("type");
    	if(eventType==null)	return;
    	if(eventType.equals("ServiceResult")) {
    		this.serviceResult(event);
    	}else if(eventType.equals("UpdateBuddy")) {
    		this.updateBuddy(event);
    	}else if(eventType.equals("UpdateMobileBuddy")) {
    		this.updateMobileBuddy(event);
    	}else if(eventType.equals("AddBuddyApplication")){
    		this.buddyApplication(event);
    	}else{
    		logger.warn("Unknown cantact event type:"+eventType);
    	}
    	
    }
    
    
    /**
     * 好友信息更新
     * @throws FetionException 
     */
    private void serviceResult(Element event) throws FetionException
    {
    	List list = XMLHelper.findAll(event, "/event/results/contacts/*contact");
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e =  (Element) it.next();
    		String uri = e.getAttributeValue("uri");
    		String status = e.getAttributeValue("status-code");
    		FetionBuddy buddy = (FetionBuddy) context.getFetionStore().getBuddyByUri(uri);
    		if(status!=null && status.equals("200") && buddy!=null) {
    			//个人信息
    			Element p = e.getChild("personal");
    			if(p!=null) {
        			BeanHelper.toBean(FetionBuddy.class, buddy, p);
    			}
    			//扩展信息
    			List extendz = e.getChildren("extended");
    			if(extendz!=null && extendz.size()>0) {
    				it = extendz.iterator();
    				while(it.hasNext()) {
    					Element m = (Element) it.next();
    					//这里可能的值有 score sms feike show rtm, 这里只处理score TODO ..处理其他的。。
    					if(m.getAttribute("type")!=null&&m.getAttributeValue("type").equals("score")) {
    						Element s = m.getChild("score");
    						BeanHelper.setValue(buddy, "level", Integer.parseInt(s.getAttributeValue("value")));
    					}
    				}
    			}
    		}else {
    			//这里是判断失败，说明结果有误，忽略掉，也不记录到日志
    		}
    	}//end contact iterator
    }
    
    /**
     * 陌生人添加用户为好友的请求
     * @throws IOException 
     */
    private void buddyApplication(Element event) throws FetionException
    {
    	Element app = event.getChild("application");
    	String uri  = app.getAttributeValue("uri");
    	final String desc = app.getAttributeValue("desc");
    	//建立一个新好友，并把关系设置为陌生人
    	Buddy buddy = UriHelper.createBuddy(uri);
    	buddy.setUri(uri);
    	BeanHelper.setValue(buddy, "relation", Relation.STRANGER);
    	context.getFetionStore().addBuddy(buddy);
    	//如果是飞信好友，获取陌生人的信息
    	if(buddy instanceof FetionBuddy) {
        	SipcRequest request = this.dialog.getMessageFactory().createGetContactDetailRequest(uri);
        	request.setResponseHandler(new GetContactInfoResponseHandler((FetionBuddy)buddy));
        	dialog.process(request);
    	}
    	//通知监听器
    	if(this.context.getNotifyEventListener()!=null)
    		context.getNotifyEventListener().fireEvent(new BuddyApplicationEvent( buddy, desc));
		logger.debug("Recived a buddy application:"+desc);
    }
    
    
    /**
     * 手机好友同意或者拒绝添加手机好友
     * @param event
     * @throws FetionException 
     */
    private void updateMobileBuddy(Element event) throws FetionException
    {
    	List list = XMLHelper.findAll(event, "/event/contacts/mobile-buddies/*mobile-buddy");
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		String uri = e.getAttributeValue("uri");
    		Buddy buddy = context.getFetionStore().getBuddyByUri(uri);
    		if(buddy!=null) {
    			//检查用户关系的变化
    			Relation relation = ParseHelper.parseRelation(e.getAttributeValue("relation-status"));
    			//如果当前好友关系是没有确认，而返回的好友是确认了，表明好友同意了你添加好友的请求
    			if(relation==Relation.BUDDY && buddy.getRelation()!=Relation.BUDDY) {
    				
    				//因为这里是手机好友，没有详细信息，故不再获取详细信息
    				logger.debug("Mobile buddy agreed your buddy request:"+buddy.getFetionId());
    				if(this.context.getNotifyEventListener()!=null)
    					context.getNotifyEventListener()
    					.fireEvent(new BuddyConfirmedEvent(buddy, true));		//通知监听器
    				
    			}else if(relation==Relation.DECLINED) {	//对方拒绝了请求
    				
    				logger.debug("buddy declined your buddy request:"+buddy.getDisplayName());
    				if(this.context.getNotifyEventListener()!=null)
    					context.getNotifyEventListener()
    					.fireEvent(new BuddyConfirmedEvent(buddy, false));	//通知监听器
    				
    			}else {}

        		//buddy.setUserId(Integer.parseInt(e.getAttributeValue("user-id")));
    			BeanHelper.setValue(buddy, "relation", relation);
			}
    	}
    }
    
    /**
     * 好友同意或者拒绝加入好友
     * @throws IOException 
     */
    private void updateBuddy(Element event) throws FetionException
    {
    	List list = XMLHelper.findAll(event, "/event/contacts/buddies/*buddy");
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e =  (Element) it.next();
    		String uri = e.getAttributeValue("uri");
    		final Buddy buddy = context.getFetionStore().getBuddyByUri(uri);
    		if(buddy!=null) {
    			//检查用户关系的变化
    			Relation relation = ParseHelper.parseRelation(e.getAttributeValue("relation-status"));
    			//如果当前好友关系是没有确认，而返回的好友是确认了，表明好友同意了你添加好友的请求
    			if(relation==Relation.BUDDY && buddy.getRelation()!=Relation.BUDDY) {
    				
    				//这里还需要获取好友的详细信息
    				SipcRequest request = dialog.getMessageFactory().createGetContactDetailRequest(buddy.getUri());
    				
    				//这里需要动态的创建一个hanler, 当前操作在读线程上，还不能等待
    				ResponseHandler handler = new ResponseHandler()
    					{
    						public void handle(SipcResponse response) throws FetionException
    						{
    							Element root = XMLHelper.build(response.getBody().toSendString());
    		    				Element contact = root.getChild("contacts").getChild("contact");
    		    				if(contact!=null) {
    		        				Element p = contact.getChild("personal");
    		        			    if(p!=null) {	
    		        			    	BeanHelper.toBean(FetionBuddy.class, buddy, p);
    		        			    }
    		    				}
    		    				
    		    				logger.debug("buddy agreed your buddy request:"+buddy.getDisplayName());
    		    				if(context.getNotifyEventListener()!=null)
    		    					context.getNotifyEventListener()
    		    					.fireEvent(new BuddyConfirmedEvent(buddy, true));			//通知监听器
    						}

							@Override
                            public void timeout(SipcRequest request)
                            {
								logger.warn("ContactDetailRequest timeout ...");
                            }

							@Override
                            public void ioerror(SipcRequest request)
                            {
								logger.warn("ContactDetailRequest error ...");
                            }
    					};
    					//消息回复收到后就会自动调用这个处理器
    					request.setResponseHandler(handler);
    					//发出这个消息
    					dialog.process(request);
    			}else if(relation==Relation.DECLINED) {	//对方拒绝了请求
    				logger.debug("buddy declined your buddy request:"+buddy.getDisplayName());
    				if(this.context.getNotifyEventListener()!=null)
    					context.getNotifyEventListener()
    					.fireEvent(new BuddyConfirmedEvent( buddy, false));	//通知监听器
    			}else {}

    			//buddy.setUserId(Integer.parseInt(e.getAttributeValue("user-id")));
    			BeanHelper.setValue(buddy, "relation", relation);
    		}
    	}
    }

}
