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
 * Package  : net.solosky.maplefetion.protocol
 * File     : ChatDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import java.io.IOException;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.IMessageCallback;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.net.TransferService;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.ObjectWaiter;

import org.apache.log4j.Logger;

/**
 *  在线好友对话
 *  
 *  飞信和在线好友发送消息不一样，是通过聊天服务器（暂且就这样叫吧）中转的
 *  每一个对话应该可以有多人参与,(这里一个对话仅能参与一人，以后完善吧)
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ChatDialog extends AbstractDialog
{

	/**
	 * 这个对话对应的好友列表
	 */
	private FetionBuddy buddy;
	
	/**
	 * 等待用户加入的同步对象
	 */
	private ObjectWaiter<String> buddyEnterWaiter;
	
	/**
	 * 上一次收发消息时间,如果在指定的时间内没有消息发送或者接受，就需要关闭这个对话框，这个字段记录了上一次收发消息的时间
	 */
	private long lastActiveTime;
	
	/**
	 * 日志记录
	 */
	protected static Logger logger = Logger.getLogger(ChatDialog.class);
	/**
     * @param client
     * @param host
     * @param port
     * @throws Exception
     */
    public ChatDialog(IFetionClient client,String host, int port, String ticket) throws Exception
    {
	    super(client);
	    this.transferService = new TransferService(
	    		client.getTransferFactory().createTransfer(host, port),
	    		this.messageListener);
	    this.dialogSession.setAttribute("TICKET", ticket);
	    this.buddyEnterWaiter = new ObjectWaiter<String>();
	    this.lastActiveTime = System.currentTimeMillis();
	    
	    this.client.getGlobalTimer().schedule(
	    		this.transferService.getTimeOutCheckTask(), 0, 
	    		FetionConfig.getInteger("fetion.sip.check-alive-interval")*1000);
    }

	/**
	 * 关闭这个对话
	 * 关闭连接，释放资源
	 */
    @Override
    public void closeDialog() throws Exception
    {
    	SIPRequest req =this.messageFactory.createLogoutRequest(this.client.getFetionUser().getUri());
		this.transferService.sendSIPMessage(req);
		this.client.getGlobalTimer().purge();
		this.transferService.stopService();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.AbstractDialog#getName()
     */
    @Override
    public String getName()
    {
    	return address.getHostName()+":"+address.getPort();
    }

    /**
     * 打开这个对话
     */
    @Override
    public boolean openDialog() throws Exception
    {
    	logger.debug("Register to chatServer..");
    	this.transferService.startService();
    	SIPRequest request = this.messageFactory.createRegisterChatRequest(
    			(String)this.dialogSession.getAttribute("TICKET"));
    	this.transferService.sendSIPMessage(request);
    	SIPResponse response = request.getResponseWaiter().waitResponse();
    	if(response.getStatusCode()==200) {
    		logger.debug("Register successfull, processed...");
    		//this.sendFetionShow();
    		return true;
    	}else {
    		logger.debug("Register failed, quit...");
    		return false;
    	}
    }
    
	/**
     * 发送聊天消息
     * @param uri
     * @param content
     * @throws IOException 
     */
    public boolean sendChatMessage(String uri, String content) throws Exception
    {
    	this.updateActiveTime();
    	SIPRequest request  = this.messageFactory.createChatMessageRequest(uri, content);
    	this.transferService.sendSIPMessage(request);
    	SIPResponse response = request.getResponseWaiter().waitResponse();
    	return response.getStatusCode()==200 || response.getStatusCode()==280;
    }
	
	/**
	 * 异步发送聊天信息
	 * @param uri
	 * @param content
	 * @param callback
	 * @throws Exception 
	 */
	public void sendChatMessageEx(final String uri, final String content, final IMessageCallback callback) throws Exception
	{
		SIPRequest request  = this.messageFactory.createChatMessageRequest(uri, content);
		ISIPResponseHandler handler = new ISIPResponseHandler()
		{
			public void handle(SIPResponse response) throws Exception
			{
				callback.messageSended(uri, content, response.getStatusCode()==200);
			}
		};
		request.setResponseHandler(handler);
		this.transferService.sendSIPMessage(request);
	}

   /**
    *  判断好友是否在这个会话中
    * @param uri
    * @return
    */
    public boolean isInDialog(String uri)
    {
    	return this.buddy!=null && this.buddy.getUri().equals(uri);
    }
    
    /**
     * 发送飞信秀请求
     * (飞信秀是啥东西，暂时还不知道。。先完整的发出去再说)
     * @throws IOException 
     */
    public void sendFetionShow() throws IOException
    {
    	SIPRequest request = this.messageFactory.createFetionShowRequest();
    	this.transferService.sendSIPMessage(request);
    }
    
    /**
     * 邀请好友加入会话
     * @param uri
     * @throws Exception 
     */
    public boolean invateBuddy(String uri) throws Exception
    {
    	SIPRequest request = this.messageFactory.createInvateBuddyRequest(uri);
    	this.transferService.sendSIPMessage(request);
    	SIPResponse response = request.getResponseWaiter().waitResponse();
    	return response.getStatusCode()==200;
    }
    
	/**
	 * 用户进入了会话
	 * @param uri 		用户uri
	 */
    public void buddyEntered(String uri)
    {
    	this.buddy = this.client.getFetionStore().getBuddy(uri);
    	this.buddyEnterWaiter.objectArrive(uri);
    }
    
    /**
     * 用户离开了回话
     * @param uri 		用户uri
     * @throws Exception 
     */
    public void buddyLeft(String uri) throws Exception
    {
    	//本来一个会话可能会有多人参与，但由于这里只实现了两个人参与，如果对方离开，这个对话就应该关闭了
    	this.client.getChatDialogFactory().closeChatDialog(this);
    	
    }
    
    
    /**
     * 等待用户进入会话
     * @param uri 等待的URI		
     */
    public void waitBuddyEnter()
    {
		String curi = this.buddyEnterWaiter.waitObject();
		logger.debug("buddy is entered this dialog:"+curi);
		return;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.AbstractDialog#excetionCaught(java.lang.Exception)
     */
    @Override
    public void excetionCaught(Throwable exception)
    {
    	//因为聊天对话读线程发生了异常，但对服务器对话没有任何影响，下一次开始聊天的时候可以重新建立一个对话
    	//这里只需从对话框工厂中把这个对话框删除就行了
    	try {
    		logger.debug("ChatDialog excetion caught, now close this ChatDialog...");
	        client.getChatDialogFactory().closeChatDialog(this);
        } catch (Exception e1) {
	       //这里不能做任何事情，只能记录
        	logger.warn("Handle Chat Dialog exception:"+e1);
        }
    }
    
    /**
     * 返回上一次收到或者发送消息时间
     * @return
     */
    public long getLastActiveTime()
    {
    	return this.lastActiveTime;
    }
    
    /**
     * 更新最近一次活动时间
     */
    public void updateActiveTime()
    {
    	this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 返回在这个会话中的好友
     * @return
     */
    public FetionBuddy getBuddy()
    {
    	return this.buddy;
    }
}
