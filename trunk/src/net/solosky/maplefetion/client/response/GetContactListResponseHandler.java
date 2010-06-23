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
 * Project  : MapleFetion2
 * Package  : net.solosky.maplefetion.client.response
 * File     : GetContactListResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import java.util.Iterator;
import java.util.List;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.MobileBuddy;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class GetContactListResponseHandler extends AbstractResponseHandler
{

	/**
     * @param client
     * @param dialog
     * @param listener
     */
    public GetContactListResponseHandler(FetionContext client, Dialog dialog,
            ActionEventListener listener)
    {
	    super(client, dialog, listener);
    }


	/* (non-Javadoc)
	 * @see net.solosky.maplefetion.client.response.AbstractResponseHandler#doActionOK(net.solosky.maplefetion.sipc.SipcResponse)
	 */
	@Override
	protected ActionEvent doActionOK(SipcResponse response)
			throws FetionException
	{
		FetionStore store = context.getFetionStore();
    	Element result = XMLHelper.build(response.getBody().toSendString());
    	Element contacts = result.getChild("contacts");
    	
    	//分组列表
    	Element buddyLists = contacts.getChild("buddy-lists");
    	if(buddyLists!=null) {
        	List list = contacts.getChild("buddy-lists").getChildren();
        	Iterator it = list.iterator();
        	while(it.hasNext()) {
        		Element e = (Element) it.next();
        		store.addCord(new Cord(Integer.parseInt(e.getAttributeValue("id")), e.getAttributeValue("name")));
        	}
    	}else {
    		logger.debug("No buddy-lists defined in the contact list.");
    	}
    	
    	
    	//飞信好友列表
    	Element buddies = contacts.getChild("buddies");
    	if(buddies!=null) {
    		List list = buddies.getChildren();
    		Iterator it = list.iterator();
        	while(it.hasNext()) {
        		Element e = (Element) it.next();
        		Buddy b = new FetionBuddy();
        		BeanHelper.toBean(FetionBuddy.class, b, e);
        		store.addBuddy(b);
        	}
    	}else {
    		logger.debug("No fetion buddies defined in the contact list..");
    	}
    	
    	// 飞信手机好友列表
    	Element mobileBuddies = contacts.getChild("mobile-buddies");
    	if(mobileBuddies!=null) {
        	List list = mobileBuddies.getChildren();
        	Iterator it = list.iterator();
        	while(it.hasNext()) {
        		Element e = (Element) it.next();
        		Buddy b = new MobileBuddy();
        		BeanHelper.toBean(MobileBuddy.class, b, e);
        		store.addBuddy(b);
        	}
    	}else {
    		logger.debug("No mobile buddies defined in the contact list..");
    	}
    	
    	//处理 chat-friend..
    	//这个chat-friend具体是什么含义我也没搞得太清楚，目前猜测里面的名单可能和用户是陌生人关系
    	Element chatFriends = contacts.getChild("chat-friends");
    	if(chatFriends!=null){
    		List list = chatFriends.getChildren();
    		Iterator it = list.iterator();
    		while(it.hasNext()){
    			Element e = (Element) it.next();
    			Buddy b = new FetionBuddy();
        		BeanHelper.toBean(FetionBuddy.class, b, e);
        		BeanHelper.setValue(b, "relation", Relation.STRANGER);
        		store.addBuddy(b);
    		}
    	}
    	
    	//TODO 处理allowList...
		return super.doActionOK(response);
	}
    
    

}