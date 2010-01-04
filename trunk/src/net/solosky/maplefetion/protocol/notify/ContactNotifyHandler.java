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
package net.solosky.maplefetion.protocol.notify;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.protocol.ISIPResponseHandler;
import net.solosky.maplefetion.protocol.response.GetContactInfoResponseHandler;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ContactNotifyHandler extends AbstractSIPNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SIPNotify notify) throws Exception
    {
    	Element root = XMLHelper.build(notify.getBody().toSendString());
    	Element event = root.getChild("event");
    	 String eventType = event.getAttributeValue("type");
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
     */
    private void serviceResult(Element event)
    {
    	List list = XMLHelper.findAll(event, "/event/results/contacts/*contact");
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e =  (Element) it.next();
    		String uri = e.getAttributeValue("uri");
    		String status = e.getAttributeValue("status-code");
    		FetionBuddy buddy = client.getFetionStore().getBuddy(uri);
    		if(status!=null && status.equals("200") && buddy!=null) {
    			//个人信息
    			Element p = e.getChild("personal");
    			if(p!=null) {
        			ParseHelper.parseBuddyPersonalBasic(buddy, p);
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
    						buddy.setLevel(Integer.parseInt(s.getAttributeValue("value")));
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
    private void buddyApplication(Element event) throws IOException
    {
    	Element app = event.getChild("application");
    	String uri  = app.getAttributeValue("uri");
    	final String desc = app.getAttributeValue("desc");
    	//建立一个新好友，并把关系设置为陌生人
    	FetionBuddy buddy = new FetionBuddy();
    	buddy.setUri(uri);
    	buddy.setRelationStatus(FetionBuddy.RELATION_STATUS_STRANGER);
    	client.getFetionStore().addBuddy(buddy);
    	//获取陌生人的信息
    	SIPRequest request = this.dialog.getMessageFactory().createGetContactDetailRequest(uri);
    	request.setResponseHandler(new GetContactInfoResponseHandler(buddy));
    	this.dialog.getTransferService().sendSIPMessage(request);

    	//通知监听器
		client.getNotifyListener().buddyApplication(buddy, desc);
		logger.debug("Recived a buddy application:"+desc);
    }
    
    
    /**
     * 手机好友同意或者拒绝添加手机好友
     * @param event
     */
    private void updateMobileBuddy(Element event)
    {
    	List list = XMLHelper.findAll(event, "/event/contacts/mobile-buddies/*mobile-buddy");
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		String uri = e.getAttributeValue("uri");
    		FetionBuddy buddy = client.getFetionStore().getBuddy(uri);
    		if(buddy!=null) {
    			//检查用户关系的变化
    			int relationStatus = Integer.parseInt(e.getAttributeValue("relation-status"));
    			//如果当前好友关系是没有确认，而返回的好友是确认了，表明好友同意了你添加好友的请求
    			if(relationStatus==FetionBuddy.RELATION_STATUS_AGREED 
    					&& buddy.getRelationStatus()!=FetionBuddy.RELATION_STATUS_AGREED) {
    				
    				//因为这里是手机好友，没有详细信息，故不再获取详细信息
    				logger.debug("Mobile buddy agreed your buddy request:"+buddy.getDisplayName());
    				client.getNotifyListener().buddyConfirmed( buddy, true);		//通知监听器
    				
    			}else if(relationStatus==FetionBuddy.RELATION_STATUS_DECLINED) {	//对方拒绝了请求
    				
    				logger.debug("buddy declined your buddy request:"+buddy.getDisplayName());
    				client.getNotifyListener().buddyConfirmed(buddy, false);		//通知监听器
    				
    			}else {}

        		buddy.setUid(Integer.parseInt(e.getAttributeValue("user-id")));
    			buddy.setRelationStatus(relationStatus);
			}
    	}
    }
    
    /**
     * 好友同意或者拒绝加入好友
     * @throws IOException 
     */
    private void updateBuddy(Element event) throws IOException
    {
    	List list = XMLHelper.findAll(event, "/event/contacts/buddies/*buddy");
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e =  (Element) it.next();
    		String uri = e.getAttributeValue("uri");
    		final FetionBuddy buddy = client.getFetionStore().getBuddy(uri);
    		if(buddy!=null) {
    			//检查用户关系的变化
    			int relationStatus = Integer.parseInt(e.getAttributeValue("relation-status"));
    			//如果当前好友关系是没有确认，而返回的好友是确认了，表明好友同意了你添加好友的请求
    			if(relationStatus==FetionBuddy.RELATION_STATUS_AGREED 
    					&& buddy.getRelationStatus()!=FetionBuddy.RELATION_STATUS_AGREED) {
    				
    				//这里还需要获取好友的详细信息
    				SIPRequest request = dialog.getMessageFactory().createGetContactDetailRequest(buddy.getUri());
    				
    				//这里需要动态的创建一个hanler, 当前操作在读线程上，还不能等待
    				ISIPResponseHandler handler = new ISIPResponseHandler()
    					{
    						public void handle(SIPResponse response) throws Exception
    						{
    							Element root = XMLHelper.build(response.getBody().toSendString());
    		    				Element contact = root.getChild("contacts").getChild("contact");
    		    				if(contact!=null) {
    		        				Element p = contact.getChild("personal");
    		        			    if(p!=null) {	
    		        			    	ParseHelper.parseBuddyPersonalBasic(buddy, p);
    		        			    }
    		    				}
    		    				
    		    				logger.debug("buddy agreed your buddy request:"+buddy.getDisplayName());
    		    				client.getNotifyListener().buddyConfirmed( buddy, true);		//通知监听器
    						}
    					};
    					//消息回复收到后就会自动调用这个处理器
    					request.setResponseHandler(handler);
    					//发出这个消息
    					dialog.getTransferService().sendSIPMessage(request);
    			}else if(relationStatus==FetionBuddy.RELATION_STATUS_DECLINED) {	//对方拒绝了请求
    				logger.debug("buddy declined your buddy request:"+buddy.getDisplayName());
    				client.getNotifyListener().buddyConfirmed(buddy, false);		//通知监听器
    			}else {}

    			buddy.setUid(Integer.parseInt(e.getAttributeValue("user-id")));
    			buddy.setRelationStatus(relationStatus);
    		}
    	}
    }

}
