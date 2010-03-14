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
 * File     : PresenceChangedSIPNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import java.util.Iterator;
import java.util.List;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.MobileChatDialog;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 * 好友状态改变
 *
 * @author solosky <solosky772@qq.com> 
 */
public class BuddyPresenceNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
	    Element root = XMLHelper.build(notify.getBody().toSendString());
	    List list = XMLHelper.findAll(root, "/events/event/*presence");
	    if(list==null)	return;
	    Iterator it = list.iterator();
	    while(it.hasNext()) {
	    	Element presence = (Element) it.next();
	    	Element basic = presence.getChild("basic");
	 	    Element personal = presence.getChild("personal");
	 	    List extendz = presence.getChildren("extended");
    	    String uri = presence.getAttributeValue("uri");
    	    FetionBuddy buddy = (FetionBuddy) context.getFetionStore().getBuddy(uri);	//这里一定是FetionBuddy
    	    if(buddy==null && context.getFetionUser().getUri().equals(uri)) {
    	    	buddy = context.getFetionUser();			//这里可能是用户自己
    	    }
    	    
    	    if(buddy!=null) {
				//好友信息改变
        	    if(personal!=null) {
        	    	//ParseHelper.parseBuddyPersonalBasic(buddy, personal);
        	    	BeanHelper.toBean(FetionBuddy.class, buddy, personal);
        	    }

				//状态改变
        	    if(basic!=null) {
            	    int oldpresense = buddy.getPresence().getValue(); 
            	    int curpresense = Integer.parseInt(basic.getAttributeValue("value"));    
            	    if(oldpresense!=curpresense) {
            	    	//更新状态
            	    	buddy.getPresence().setValue(curpresense);
            	    	
            	    	//注意，如果好友上线了，并且当前打开了手机聊天对话框，需要关闭这个手机聊天对话框
            	    	if(curpresense == Presence.AWAY || curpresense==Presence.BUSY || curpresense==Presence.ONLINE ) {
            	    		ChatDialog chatDialog = this.context.getDialogFactory().findChatDialog(buddy);
            	    		if(chatDialog!=null && chatDialog instanceof MobileChatDialog) {
            	    			this.context.getDialogFactory().closeDialog(chatDialog);
            	    		}
            	    	}
            	    	
            	    	//通知监听器，好友状态已经改变
            	    	this.context.getNotifyListener().presenceChanged(buddy);
            	    }
        	    }
        	    
        	    if(extendz!=null) {
        	    	Iterator eit = extendz.iterator();
        	    	while(eit.hasNext()) {
        	    		Element extend = (Element) eit.next();
        	    		String type = extend.getAttributeValue("type");
        	    		if(type.equals("sms")) {
        	    			buddy.getSMSPolicy().parse(extend.getText());
        	    		}
        	    	}
        	    }
        	    
        	    logger.debug("PresenceChanged:"+buddy.getDisplayName()+" [presence="+buddy.getPresence().getValue()+"]");
        	    //TODO ..这里只处理了好友状态改变，本来还应该处理其他信息改变，如好友个性签名和昵称的改变，以后添加。。
    	    }else {
    	    	logger.warn("Unknown Buddy in PresenceChanged notify:"+uri);
    	    }
	    }
    }
}
