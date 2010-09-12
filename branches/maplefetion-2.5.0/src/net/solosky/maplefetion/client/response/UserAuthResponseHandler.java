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
 * File     : UserAuthResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-3-14
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
import net.solosky.maplefetion.bean.StoreVersion;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.event.action.FailureEvent;
import net.solosky.maplefetion.event.action.FailureType;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.UriHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class UserAuthResponseHandler extends AbstractResponseHandler
{

	/**
     * @param client
     * @param dialog
     * @param listener
     */
    public UserAuthResponseHandler(FetionContext client, Dialog dialog,
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
	
		Element root = XMLHelper.build(response.getBody().toSendString());
		
		FetionStore store = this.context.getFetionStore();
		
		//解析个人信息，飞信真有意思，这里却不简写，map.xml里面全是简写的，所以这里只能手动注入了。
		Element personal = XMLHelper.find(root, "/results/user-info/personal");
		User user = this.context.getFetionUser();
		user.setImpresa(personal.getAttributeValue("impresa"));
		user.setTrueName(personal.getAttributeValue("name"));
		user.setNickName(personal.getAttributeValue("nickname"));	
		user.setEmail(personal.getAttributeValue("register-email"));
		user.setUri(personal.getAttributeValue("uri"));
		user.setFetionId(Integer.parseInt(personal.getAttributeValue("sid")));
		user.setMobile(Long.parseLong(personal.getAttributeValue("mobile-no")));
		user.setUserId(Integer.parseInt(personal.getAttributeValue("user-id")));
		
		Element contactList = XMLHelper.find(root, "/results/user-info/contact-list");
		
		//一定要对飞信列表加锁，防止其他飞信操作获取到空的数据
		synchronized (store) {
			
			//先清除飞信存储对象的所有数据
    		store.clearBuddyList();
    		store.clearCordList();
    		
			//解析分组列表
			List list = XMLHelper.findAll(root, "/results/user-info/contact-list/buddy-lists/*buddy-list");
			Iterator it = list.iterator();
			while(it.hasNext()) {
				Element e = (Element) it.next();
				store.addCord(new Cord(Integer.parseInt(e.getAttributeValue("id")), e.getAttributeValue("name")));
			}
			
			//解析好友列表
			list = XMLHelper.findAll(root, "/results/user-info/contact-list/buddies/*b");
			it = list.iterator();
			while(it.hasNext()) {
				Element e = (Element) it.next();
				String uri = e.getAttributeValue("u");
				
				Buddy b = UriHelper.createBuddy(uri);
				b.setUserId(Integer.parseInt(e.getAttributeValue("i")));
				b.setLocalName(e.getAttributeValue("n"));
				b.setUri(e.getAttributeValue("u"));
				b.setCordId(e.getAttributeValue("l"));
				
				store.addBuddy(b);
			}
			
			//处理 chat-friend..
	    	//这个chat-friend具体是什么含义我也没搞得太清楚，目前猜测里面的名单可能和用户是陌生人关系
			list = XMLHelper.findAll(root, "/results/user-info/contact-list/chat-friends/*c");
			it = list.iterator();
    		while(it.hasNext()){
    			Element e = (Element) it.next();
    			Buddy b = UriHelper.createBuddy(e.getAttributeValue("u"));
				b.setUserId(Integer.parseInt(e.getAttributeValue("i")));
				b.setUri(e.getAttributeValue("u"));
        		b.setRelation(Relation.STRANGER);
        		store.addBuddy(b);
    		}
    		
    		//处理Blacklist
    		list = XMLHelper.findAll(root, "/results/user-info/contact-list/blacklist/*k");
			it = list.iterator();
    		while(it.hasNext()){
    			Element e = (Element) it.next();
    			String uri = e.getAttributeValue("u");
    			Buddy b = store.getBuddyByUri(uri);
    			if(b!=null) {
    				b.setRelation(Relation.BANNED);
    			}
    		}
        }
		
		return super.doActionOK(response);
	}
	

	@Override
	protected ActionEvent doRequestFailure(SipcResponse response)
			throws FetionException {
		return new FailureEvent(FailureType.REGISTER_FORBIDDEN);
	}

	@Override
	protected ActionEvent doNotAuthorized(SipcResponse response)
			throws FetionException
	{
		return new FailureEvent(FailureType.AUTHORIZATION_FAIL);
	}
    
    
}
