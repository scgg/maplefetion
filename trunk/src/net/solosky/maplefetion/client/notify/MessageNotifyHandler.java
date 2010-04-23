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
package net.solosky.maplefetion.client.notify;

import java.io.IOException;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.MobileBuddy;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.client.response.GetContactInfoResponseHandler;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcReceipt;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.ParseException;
import net.solosky.maplefetion.util.UriHelper;

/**
 *
 *	收到服务消息回复
 *
 * @author solosky <solosky772@qq.com> 
 */
public class MessageNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
    	SipcHeader event = notify.getHeader(SipcHeader.EVENT);
    	String from = notify.getFrom();
    	if(event!=null && event.getValue().equals("system-message")) {
    		this.systemMessageReceived(notify);
    	}else if(UriHelper.isGroup(from)) {
    		this.groupMessageReceived(notify);
    	}else {
    		this.buddyMessageRecived(notify);
    	}
    }
    
    /**
     * 好友消息
     * @throws IOException 
     */
    private void buddyMessageRecived(SipcNotify notify) throws FetionException
    {
    	//发送信息收到回复
	    SipcReceipt receipt = this.dialog.getMessageFactory()
	    .createChatMessageReceipt(notify.getFrom(), Integer.toString(notify.getCallID()), notify.getSequence());
	    this.dialog.process(receipt);
	    
    	//查找消息是哪个好友发送的
        FetionStore store = this.context.getFetionStore();
	    Buddy from   = store.getBuddyByUri(notify.getFrom());
	    String body  = notify.getBody()!=null?notify.getBody().toSendString():"";	//防止产生NULL错误
	    Message msg  = null;
	    SipcHeader contentHeader = notify.getHeader(SipcHeader.CONTENT_TYPE);
	    if(contentHeader!=null && "text/plain".equals(contentHeader.getValue())) {
	    	msg = Message.wrap(body);
	    }else {
	    	try {
	            msg = Message.parse(body);
            } catch (ParseException e) {
            	msg = Message.wrap(body);
            }
	    }
	    
	    //如果好友没有找到，可能是陌生人发送的信息，
	    if(from==null) {
	    	//这里新建一个好友对象，并设置关系为陌生人
	    	if(UriHelper.isMobile(notify.getFrom()))
	    		from = new MobileBuddy();
	    	else
	    		from = new FetionBuddy();
	    	
	    	from.setUri(notify.getFrom());
	    	from.getRelation().setValue(Relation.RELATION_STRANGER);
	    	//添加至列表中
	    	this.context.getFetionStore().addBuddy(from);
	    	
	    	//如果是飞信好友，还需要获取这个陌生人的信息
	    	if(from instanceof FetionBuddy) {
    	    	SipcRequest request = this.dialog.getMessageFactory().createGetContactDetailRequest(notify.getFrom());
    	    	request.setResponseHandler(new GetContactInfoResponseHandler((FetionBuddy) from));
    	    	this.dialog.process(request);
	    	}
	    }
	   
	    //通知消息监听器
	    ChatDialog chatDialog = this.context.getDialogFactory().findChatDialog(from);
	    if(chatDialog==null) {
	    	final ChatDialog dialog = this.context.getDialogFactory().createChatDialog(from);
	    	final Buddy ffrom = from;
	    	final Message fmsg = msg;
	    	dialog.openDialog(new ActionListener() {
	    		public void actionFinished(int status) {
	    			if(status==ActionStatus.ACTION_OK) {
    	    			 dialog.updateActiveTime();
    	    			 if(context.getNotifyListener()!=null)
    	    				 context.getNotifyListener().buddyMessageRecived(ffrom, fmsg, dialog);
    	    		}
	    		}
	    	});
	    }else {
	    	chatDialog.updateActiveTime();
	    	if(this.context.getNotifyListener()!=null)
	    		this.context.getNotifyListener().buddyMessageRecived(from, Message.parse(body), chatDialog);
	    }
	    logger.debug("RecivedMessage:[from="+notify.getFrom()+", message="+body+"]");
    }
    
    /**
     * 系统消息
     */
    private void systemMessageReceived(SipcNotify notify)
    {
    	logger.debug("Recived a system message:"+notify.getBody().toSendString());
    	if(this.context.getNotifyListener()!=null)
    		this.context.getNotifyListener().systemMessageRecived(notify.getBody().toSendString());
    }
    
    /**
     * 群消息
     */
    private void groupMessageReceived(SipcNotify notify) throws FetionException
    {
    	//发送信息收到回复
	    SipcReceipt receipt = this.dialog.getMessageFactory()
	    .createChatMessageReceipt(notify.getFrom(), Integer.toString(notify.getCallID()), notify.getSequence());
	    this.dialog.process(receipt);
	    
	    Group  group  = this.context.getFetionStore().getGroup(notify.getFrom());
	    
	    Member member = this.context.getFetionStore().getGroupMember(group, notify.getHeader("SO").getValue());
	    String body   = notify.getBody()!=null?notify.getBody().toSendString():"";	//防止产生NULL错误
	    GroupDialog groupDialog = this.context.getDialogFactory().findGroupDialog(group);
	    
	    if(group!=null && member!=null && groupDialog!=null&&this.context.getNotifyListener()!=null) {
	    	this.context.getNotifyListener().groupMessageRecived(group, member, Message.parse(body), groupDialog);
	    	logger.debug("Received a group message:[ Group="+group.getName()+", from="+member.getDisplayName()+", msg="+body );
	    }
    }
}
