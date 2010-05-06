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
 * File     : LiveV1ChatDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-14
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.client.response.DefaultResponseHandler;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.util.CallHelper;
import net.solosky.maplefetion.util.ObjectWaiter;
import net.solosky.maplefetion.util.ResponseFuture;

import org.apache.log4j.Logger;

/**
 * 
 * 第一版和在线好友聊天对话框 这个对话框是以前飞信版本采用的和在线好友聊天的协议，是通过服务器建立会话的
 * 飞信在2008版本升级为第二版，采用了另外一个聊天服务器建立会话， 现在这种对话主要用在HTTP通信模式下和在线好友建立会话
 * 
 * @author solosky <solosky772@qq.com>
 */
public class LiveV1ChatDialog extends ChatDialog
{

	/**
	 * callHelper
	 */
	private CallHelper callHelper;

	/**
	 * 邀请通知
	 */
	private SipcNotify inviteNotify;
	
	/**
	 * 等待对方确认的等待对象
	 */
	private ObjectWaiter<Boolean> ackWaiter;
	
	/**
	 * LOGGER
	 */
	private static Logger logger = Logger.getLogger(LiveV1ChatDialog.class);

	/**
	 * 主动建立会话
	 * 
	 * @param mainBuddy
	 * @param client
	 */
	public LiveV1ChatDialog(Buddy mainBuddy, FetionContext client)
	{
		super(mainBuddy, client);
		this.callHelper = new CallHelper();
		this.ackWaiter  = new ObjectWaiter<Boolean>();
	}

	/**
	 * 被邀请建立会话
	 * 
	 * @param inviteNotify
	 * @param client
	 */
	public LiveV1ChatDialog(SipcNotify inviteNotify, FetionContext client)
	{
		super(client);
		this.inviteNotify = inviteNotify;
		this.ackWaiter    = new ObjectWaiter<Boolean>();
		this.callHelper   = new CallHelper(inviteNotify.getCallID());
		this.mainBuddy    = client.getFetionStore().getBuddyByUri(
		        inviteNotify.getFrom());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.solosky.maplefetion.client.dialog.ChatDialog#isMutipartySupported()
	 */
	@Override
	public boolean isMutipartySupported()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.solosky.maplefetion.client.dialog.ChatDialog#sendChatMessage(java
	 * .lang.String, net.solosky.maplefetion.client.dialog.ActionListener)
	 */
	@Override
	public void sendChatMessage(Message message, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest req = this.getMessageFactory().createSendChatMessageRequest(this.mainBuddy.getUri(), message);
		req.setResponseHandler(new DefaultResponseHandler(listener));
		
		this.process(req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.solosky.maplefetion.client.dialog.Dialog#closeDialog()
	 */
	@Override
	public void closeDialog()
	{
		if(!this.context
				.getDialogFactory()
				.getServerDialog()
				.getProcessorChain()
				.isChainClosed()) {
			try {
	            this.bye();
            } catch (Exception e) {
            	logger.warn("Close ChatDialog failed. dialog = "+this.toString()+" ", e);
            }
		}
		this.setState(DialogState.CLOSED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.solosky.maplefetion.client.dialog.Dialog#openDialog()
	 */
	@Override
	public void openDialog() throws TransferException, DialogException
	{
		try {
			this.setState(DialogState.OPENNING);
			//只有主动打开对话才会发出邀请请求，如果是被动邀请，什么也不需要做
			if (!this.isBeenInvited()) {
				this.invite();
				this.ack();
			}else {
				this.ackWaiter.waitObject(60*1000);	//等待60秒，如果没有回复就抛出异常
			}
			
			this.setState(DialogState.OPENED);
		} catch (RequestTimeoutException e) {
			this.setState(DialogState.FAILED);
			throw new DialogException(e);
		} catch (InterruptedException e) {
			this.setState(DialogState.FAILED);
			throw new DialogException(e);
		} catch (Exception e) {
			this.setState(DialogState.FAILED);
			throw new DialogException(e);
        }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.solosky.maplefetion.client.dialog.Dialog#process(net.solosky.maplefetion
	 * .sipc.SipcOutMessage)
	 */
	@Override
	public void process(SipcOutMessage out)
	{
		this.context.getDialogFactory().getServerDialog().process(out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.solosky.maplefetion.client.dialog.Dialog#getMessageFactory()
	 */
	@Override
	public MessageFactory getMessageFactory()
	{
		return this.context.getDialogFactory().getServerDialog()
		        .getMessageFactory();
	}

	/**
	 * 邀请对方加入会话
	 * 
	 * @throws TransferException
	 * @throws RequestTimeoutException
	 * @throws InterruptedException
	 * @throws IllegalResponseException
	 */
	private void invite() throws TransferException, RequestTimeoutException,
	        InterruptedException, IllegalResponseException
	{
		SipcRequest request = this.getMessageFactory()
		        .createInviteRequest(
		                this.mainBuddy.getUri(),
		                this.context.getTransferFactory()
		                        .getDefaultTransferLocalPort());
		this.callHelper.set(request);
		ResponseFuture future = ResponseFuture.wrap(request);
		this.process(request);

		int status = future.waitResponse().getStatusCode();
		if (status == SipcStatus.TRYING) {
			assertStatus(future.waitResponse().getStatusCode(),
			        SipcStatus.ACTION_OK);
		} else if (status == SipcStatus.ACTION_OK) {
		} else {
			throw new IllegalResponseException("Unexpected response status");
		}
	}

	/**
	 * 确认会话已经建立
	 * 
	 * @throws TransferException
	 */
	private void ack() throws TransferException
	{
		// 这个请求不需要回复
		SipcRequest request = this.getMessageFactory().createAckRequest(
		        this.mainBuddy.getUri());
		this.callHelper.set(request);
		this.process(request);
	}

	/**
	 * 离开对话
	 * 
	 * @throws TransferException
	 */
	private void bye() throws TransferException
	{
		SipcRequest request = this.getMessageFactory().createLogoutRequest(
		        this.mainBuddy.getUri());
		this.process(request);
	}

	/**
	 * 用户是否被邀请进入这个对话框的
	 * 
	 * @return
	 */
	public boolean isBeenInvited()
	{
		return this.inviteNotify != null;
	}
	
	public String toString()
    {
    	return "[LiveV1ChatDialog - " +
    			"MainBuddy= "+mainBuddy.getDisplayName()+", "+mainBuddy.getUri()+" ]";
    }
	
	/**
	 * 当服务器返回会话确认的时候回调
	 * 只有当邀请加入会话的时候才会回调
	 */
    public void dialogOpened()
    {
    	this.setState(DialogState.OPENED);
    	this.ackWaiter.objectArrive(true);
    }

}
