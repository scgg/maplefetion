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
 * Package  : net.solosky.maplefetion.client.dialog
 * File     : BasicChatDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-14
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.sipc.SipcOutMessage;

/**
 * 
 * 和手机在线和离线的好友聊天
 * 聊天消息是通过SIPC服务器中转的
 * 
 * @author solosky <solosky772@qq.com>
 */
public class BasicChatDialog extends ChatDialog
{
	/**
	 * @param mainBuddy
	 * @param client
	 */
	public BasicChatDialog(FetionContext client, Buddy mainBuddy)
	{
		super(mainBuddy, client);
	}

	/**
	 * 发送信令
	 * 
	 * @param out
	 *            发出信令
	 * @throws TransferException
	 */
	@Override
	public synchronized void process(SipcOutMessage out) throws TransferException
	{
		this.context.getDialogFactory().getServerDialog().process(out);
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.ChatDialog#isMutipartySupported()
     */
    @Override
    public boolean isMutipartySupported()
    {
	    return false;
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.ChatDialog#sendChatMessage(java.lang.String, net.solosky.maplefetion.client.dialog.ActionListener)
     */
    @Override
    public synchronized void sendChatMessage(Message message, ActionListener listener)
    {
	    //给手机在线的人发送消息是通过服务器对话框发送的
    	this.ensureOpened();
    	this.context.getDialogFactory().getServerDialog().sendChatMessage(this.mainBuddy, message, listener);
    	this.updateActiveTime();
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#closeDialog()
     */
    @Override
    public synchronized void closeDialog()
    {
    	//不做任何事情
    	this.setState(DialogState.CLOSED);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#openDialog()
     */
    @Override
    public synchronized void openDialog() throws TransferException, DialogException
    {
	    //不做任何事情
    	this.setState(DialogState.OPENED);
	    
    }
    
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#getMessageFactory()
     */
    @Override
    public MessageFactory getMessageFactory()
    {
    	return this.context.getDialogFactory().getServerDialog().getMessageFactory();
    }
    
    public String toString()
    {
    	return "[BasicChatDialog - " +
		"MainBuddy= "+ mainBuddy.getDisplayName()+", "+mainBuddy.getUri()+" ]";
    }

}
