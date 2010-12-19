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
 * File     : BuddyMessageRecivedNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-25
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol.notify;

import java.io.IOException;

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.protocol.ChatDialog;
import net.solosky.maplefetion.protocol.response.GetContactInfoResponseHandler;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPReceipt;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.store.IFetionStore;

/**
 *
 *	收到服务消息回复
 *
 * @author solosky <solosky772@qq.com> 
 */
public class MessageNotifyHandler extends AbstractSIPNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SIPNotify notify) throws Exception
    {
    	SIPHeader event = notify.getHeader(SIPHeader.FIELD_EVENT);
    	if(event!=null && event.getValue().equals("system-message")) {
    		this.systemMessageRecived(notify);
    	}else {
    		this.buddyMessageRecived(notify);
    	}
    }
    
    /**
     * 好友消息
     * @throws IOException 
     */
    private void buddyMessageRecived(SIPNotify notify) throws IOException
    {
    	//发送信息收到回复
	    SIPReceipt receipt = this.dialog.getMessageFactory()
	    .createChatMessageReceipt(notify.getFrom(), Integer.toString(notify.getCallID()), notify.getSequence());
	    this.dialog.getTransferService().sendSIPMessage(receipt);
	    //如果是聊天对话的话，更新活动时间
	    if(dialog instanceof ChatDialog) {
	    	ChatDialog cd = (ChatDialog) dialog;
	    	cd.updateActiveTime();
	    }
	    
    	//查找消息是哪个好友发送的
        IFetionStore store = this.client.getFetionStore();
	    FetionBuddy from  = store.getBuddy(notify.getFrom());
	    String      body  = notify.getBody()!=null?notify.getBody().toSendString():"";	//防止产生NULL错误
	    
	    //如果好友没有找到，可能是陌生人发送的信息，
	    if(from==null) {
	    	//这里新建一个好友对象，并设置关系为陌生人
	    	from = new FetionBuddy();
	    	from.setUri(notify.getFrom());
	    	from.setRelationStatus(FetionBuddy.RELATION_STATUS_STRANGER);
	    	this.client.getFetionStore().addBuddy(from);
	    	//还需要获取这个陌生人的信息
	    	SIPRequest request = this.client.getServerDialog()
	    		.getMessageFactory()
	    		.createGetContactDetailRequest(notify.getFrom());
	    	request.setResponseHandler(new GetContactInfoResponseHandler(from));
	    	this.client.getServerDialog().getTransferService().sendSIPMessage(request);
	    }
	   
	    //通知消息监听器
	    this.client.getNotifyListener().buddyMessageRecived(from, body);
	    
	    logger.debug("RecivedMessage:[from="+notify.getFrom()+", message="+body+"]");
    }
    
    /**
     * 系统消息
     */
    private void systemMessageRecived(SIPNotify notify)
    {
    	logger.debug("Recived a system message:"+notify.getBody().toSendString());
	    this.client.getNotifyListener().systemMessageRecived(notify.getBody().toSendString());
    }

}
