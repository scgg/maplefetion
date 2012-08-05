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
 * Project  : MapleFetion2.5
 * Package  : net.solosky.maplefetion.client.notify
 * File     : SyncUserInfoNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-9-12
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.client.response.GetContactInfoResponseHandler;
import net.solosky.maplefetion.event.notify.BuddyConfirmedEvent;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.ParseException;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class SyncUserInfoNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.NotifyHandler#handle(net.solosky.maplefetion.sipc.SipcNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
    	Element root = XMLHelper.build(notify.getBody().toSendString());
 	    Element userInfo = XMLHelper.find(root, "/events/event/user-info");
	    List<Element> nodes = userInfo.getChildren();
	    for(Element node: nodes){
	    	if("score".equals(node.getName())){
	    		//ingore
	    	}else if("contact-list".equals(node.getName())){
	    		handleContactList(node);
	    	}
	    }
    }

	private void handleContactList(Element node) throws FetionException 
	{
		List<Element> list = XMLHelper.findAll(node, "/contact-list/buddies/*buddy");
		for(Element e: list){
			String action = e.getAttributeValue("action");
			if("remove".equals(action)){
				deleteBuddy(e);
			}else if("add".equals(action)){
				addBuddy(e);
			}else if("update".equals(action)){
				updateBuddy(e);
			}
		}
		
		//更新联系人版本信息
    	String sContactVersion = node.getAttributeValue("version");
    	FetionStore store = context.getFetionStore();
    	if(sContactVersion!=null){
    		int iContactVersion = Integer.parseInt(sContactVersion);
    		store.getStoreVersion().setContactVersion(iContactVersion);
    	}
    	store.flushStoreVersion(store.getStoreVersion());
	}
	
	
    /**
     * 好友同意或者拒绝加入好友
     * @throws IOException 
     */
    private void updateBuddy(Element event) throws FetionException
    {
		String sUserId = event.getAttributeValue("user-id");
		final Buddy buddy = context.getFetionStore().getBuddyByUserId(Integer.parseInt(sUserId));
		if(buddy!=null) {
			//检查用户关系的变化
			Relation relation = ParseHelper.parseRelation(event.getAttributeValue("relation-status"));
			//如果当前好友关系是没有确认，而返回的好友是确认了，表明好友同意了你添加好友的请求
			if(relation==Relation.BUDDY ) {
				//这里还需要获取好友的详细信息
				SipcRequest request = dialog.getMessageFactory().createGetContactInfoRequest(buddy.getUri());
				request.setResponseHandler(new GetContactInfoResponseHandler(context, dialog, ((Buddy) buddy),null));
				dialog.process(request);
				
				this.tryFireNotifyEvent(new BuddyConfirmedEvent( buddy, true));
			}else if(relation==Relation.DECLINED) {	//对方拒绝了请求
				logger.debug("buddy declined your buddy request:"+buddy.getDisplayName());
				this.tryFireNotifyEvent(new BuddyConfirmedEvent( buddy, false));	//通知监听器
			}else {}

			//buddy.setUserId(Integer.parseInt(e.getAttributeValue("user-id")));
			buddy.setRelation(relation);
			
			context.getFetionStore().flushBuddy(buddy);
		}
    }
    
    /**
     * 服务器发回删除好友的请求，这个一般是客户端设置为全部同意对方邀请时服务器首先发挥删除好友请求，然后发回添加好友请求
     * @param event
     */
    private void deleteBuddy(Element event)
    {
    	FetionStore store = context.getFetionStore();
    	Buddy buddy = store.getBuddyByUserId(Integer.parseInt(event.getAttributeValue("user-id")));
    	if(buddy!=null){
    		store.deleteBuddy(buddy);
    	}
    }
    
    
    /**
     * 服务器发回的添加好友请求，注释同上
     * @throws ParseException 
     */
    private void addBuddy(Element event) throws ParseException
    {
		FetionStore store = this.context.getFetionStore();
		if(event.getAttributeValue("user-id")!=null){
			int userId = Integer.parseInt(event.getAttributeValue("user-id"));
			Buddy buddy = store.getBuddyByUserId(userId);
			if(buddy==null){
				buddy = new Buddy();
			}
			BeanHelper.toBean(Buddy.class, buddy, event);
			
			Relation relation = ParseHelper.parseRelation(event.getAttributeValue("relation-status"));
			buddy.setRelation(relation);
			store.addBuddy(buddy);
			
			logger.info("Added Buddy : "+buddy);
		}
    	
    }


}
