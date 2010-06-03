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
 * File     : UserEnteredConversationNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.client.dialog.MutipartyDialog;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *	用户进入对话后的通知
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ConversationNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
    	Element root   = XMLHelper.build(notify.getBody().toSendString());
    	Element event  = XMLHelper.find(root, "/events/event");
    	String type = event.getAttributeValue("type");
    	if(type.equals("UserEntered")) {    	//这里只是处理了好友进入会话请求
    		this.userEntered(event);
    	}else if(type.equals("UserLeft")){
    		this.userLeft(event);
    	}else {
    		logger.warn("Unknown converstion event type:"+type);
    	}    	
    }
    
    
    /**
     * 用户进入了会话
     */
    private void userEntered(Element event)
    {
    	if(this.dialog instanceof MutipartyDialog) {
    		MutipartyDialog cd = (MutipartyDialog) this.dialog;
    		Element member = event.getChild("member");
    		String uri = member.getAttributeValue("uri");
    		Buddy buddy = this.context.getFetionStore().getBuddyByUri(uri);
    		cd.buddyEntered(buddy);
    		
    		logger.debug("Buddy entered this dialog:"+uri);
    	}
    }
    
    
    /**
     * 用户离开了回话
     * @throws Exception 
     */
    private void userLeft(Element event)
    {
    	if(this.dialog instanceof MutipartyDialog) {
    		MutipartyDialog cd = (MutipartyDialog) this.dialog;
    		Element member = event.getChild("member");
    		String uri = member.getAttributeValue("uri");
    		Buddy buddy = this.context.getFetionStore().getBuddyByUri(uri);
    		cd.buddyLeft(buddy);
    		
    		logger.debug("Buddy left this dialog:"+uri);
    	}
    	
    }
    
    
}
