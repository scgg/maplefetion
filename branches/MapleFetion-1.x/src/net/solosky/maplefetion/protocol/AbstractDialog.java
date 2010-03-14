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
 * File     : AbstractDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import java.net.InetSocketAddress;

import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.net.ISIPMessageListener;
import net.solosky.maplefetion.net.TransferService;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;

import org.apache.log4j.Logger;

/**
 *
 *	抽象的一个对话
 *  
 *  对话指和服务器或者和在线好友交流的一个会话
 *  在飞信中，存在一个服务器对话和多个好友对话，
 *  和电脑在线的好友发送或者接受信息必须通过聊天服务器中转，和手机在线的好友发送信息是通过服务器对话完成的
 *  通常一个对话对应了一个连接
 *
 * @author solosky <solosky772@qq.com> 
 */
public abstract class AbstractDialog
{
	/**
	 * 飞信客户端
	 */
	protected IFetionClient client;
	
	/**
	 * 传输服务
	 */
	protected TransferService transferService;
	
	/**
	 * 信令分发器
	 */
	private SIPMessageDispatcher dispatcher;
	
	/**
	 * 对话会话
	 */
	protected DialogSession  dialogSession;
	
	/**
	 * 信令工厂
	 */
	protected SIPMessageFactory messageFactory;
	
	/**
	 * 信令监听器
	 */
	protected DialogSIPMessageListener messageListener;
	
	/**
	 * 是否是阻塞模式
	 */
	protected boolean isBlockMode;
	
	/**
	 * 连接的地址
	 */
	protected InetSocketAddress address;
	
	
	/**
	 * 日志记录
	 */
	protected static Logger logger = Logger.getLogger(AbstractDialog.class);
	
	/**
	 * 构造函数
	 * @param client
	 */
	public AbstractDialog(IFetionClient client) throws Exception
	{
		this.client = client;
		this.dialogSession = new DialogSession();
		this.dispatcher = new SIPMessageDispatcher(client, this);
		this.messageFactory = new SIPMessageFactory(client);
		this.messageListener = new DialogSIPMessageListener();
		this.isBlockMode = true;
	}
	
	/**
	 * 返回对话会话
	 * @return
	 */
	public DialogSession getDialogSession()
	{
		return this.dialogSession;
	}
	
	/**
	 * 返回传输服务
	 * @return
	 */
	public TransferService getTransferService()
	{
		return this.transferService;
	}
	
	/**
	 * 返回消息工厂
	 */
	public SIPMessageFactory getMessageFactory()
	{
		return this.messageFactory;
	}
	
	/**
	 * 打开对话
	 * @return  成功返回true  失败返回false
	 * @throws  Exception
	 */
	public abstract boolean openDialog() throws Exception;
	
	/**
	 * 关闭对话
	 * @throws Exception
	 */
	public abstract void closeDialog() throws Exception;
	
	/**
	 * 发生了异常，需要处理
	 * @throws Exception
	 */
	public abstract void excetionCaught(Throwable e);
	
	/**
	 * 返回这个对话名字
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * 返回这个对话的地址
	 * @return
	 */
	public InetSocketAddress getAddress()
	{
		return this.address;
	}
	
	/**
	 * 内部类
	 */
	
	public class DialogSIPMessageListener implements ISIPMessageListener
	{
		/* (non-Javadoc)
	     * @see net.solosky.maplefetion.net.SIPMessageListener#ExceptionCaught(java.lang.Exception)
	     */
	    @Override
	    public void ExceptionCaught(Throwable exception)
	    {
	    	logger.warn("Transfer Exception:"+exception);
	    	exception.printStackTrace();
	    	excetionCaught(exception);
	    }

		/* (non-Javadoc)
	     * @see net.solosky.maplefetion.net.ISIPMessageListener#SIPNotifyRecived(net.solosky.maplefetion.sip.SIPNotify)
	     */
	    @Override
	    public void SIPNotifyReceived(SIPNotify notify)
	    {
	    	dispatcher.dispatchSIPNotify(notify);
	    }

		/* (non-Javadoc)
	     * @see net.solosky.maplefetion.net.ISIPMessageListener#SIPResponseRecived(net.solosky.maplefetion.sip.SIPResponse, net.solosky.maplefetion.sip.SIPRequest)
	     */
	    @Override
	    public void SIPResponseReceived(SIPResponse response, SIPRequest request)
	    {
	    	dispatcher.dispatchSIPResponse(response, request);
	    }
	}
}
