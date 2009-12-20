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
 * Package  : net.solosky.maplefetion.net.mina
 * File     : MinaTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import net.solosky.maplefetion.net.ISIPMessageListener;
import net.solosky.maplefetion.net.ITransfer;
import net.solosky.maplefetion.net.QueueManager;
import net.solosky.maplefetion.sip.SIPOutMessage;

/**
 *
 *	Mina传输对象
 *
 * @author solosky <solosky772@qq.com>
 */
public class MinaTransfer implements ITransfer
{

	/**
	 * 队列刮管理对象
	 */
	private QueueManager queueManager;
	
	/**
	 * 会话对象，一个对象代表了一个连接
	 */
	private IoSession session;
	
	/**
	 * SIP信令监听器
	 */
	private ISIPMessageListener listener;
	
	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(MinaTransfer.class);
	
	
	/**
	 * 构造函数
	 * @param session Connector建立连接的会话对象
	 */
	public MinaTransfer(IoSession session)
	{
		this.session = session;
		this.queueManager = new QueueManager(this);
		this.session.setAttribute("TRANSFER", this);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#getQueueManager()
     */
    @Override
    public QueueManager getQueueManager()
    {
    	return queueManager;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#getSIPMessageListener()
     */
    @Override
    public ISIPMessageListener getSIPMessageListener()
    {
	    return this.listener;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#sendSIPMessage(net.solosky.maplefetion.sip.SIPOutMessage)
     */
    @Override
    public void sendSIPMessage(SIPOutMessage outMessage) throws IOException
    {
	   this.session.write(outMessage);
	   if(outMessage.isNeedAck()) {
		   this.queueManager.sendedSIPMessage(outMessage);
	   }
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#setSIPMessageListener(net.solosky.maplefetion.net.ISIPMessageListener)
     */
    @Override
    public void setSIPMessageListener(ISIPMessageListener listener)
    {
    	this.listener = listener;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#startTransfer()
     */
    @Override
    public void startTransfer()
    {
    	logger.debug("MinaTransfer started:"+this.session);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#stopTransfer()
     */
    @Override
    public void stopTransfer()
    {
    	this.session.close(false);
    	logger.debug("MinaTransfer stoped:"+this.session);
    }

}
