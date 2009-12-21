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
 * Package  : net.solosky.maplefetion.net
 * File     : AbstractTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-21
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import java.io.IOException;

import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.SIPMessageLogger;

/**
 *
 * 抽象的传输对象
 * 实现了传输对象的基本功能，其他传输对象可以继承自抽象传输对象
 *
 * @author solosky <solosky772@qq.com>
 */
public abstract class AbstractTransfer implements ITransfer
{
	
	/**
	 * 队列管理器
	 */
	protected QueueManager queueManager;
	
	/**
	 * 监听器
	 */
	protected ISIPMessageListener listener;
	
	/**
	 * 信令记录器
	 */
	protected SIPMessageLogger messageLogger;
	
	
	public AbstractTransfer()
	{
		queueManager = new QueueManager(this);
		messageLogger = new SIPMessageLogger();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#getQueueManager()
     */
    @Override
    public QueueManager getQueueManager()
    {
    	return this.queueManager;
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
     * @see net.solosky.maplefetion.net.ITransfer#setSIPMessageListener(net.solosky.maplefetion.net.ISIPMessageListener)
     */
    @Override
    public void setSIPMessageListener(ISIPMessageListener listener)
    {
    	this.listener = listener;
    }
    

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#sendSIPMessage(net.solosky.maplefetion.sip.SIPOutMessage)
     */
    @Override
    public void sendSIPMessage(SIPOutMessage outMessage) throws IOException
    {
    	//交给子类发送这个消息
    	this.doSendSIPMessage(outMessage);
    	//如果需要回复才放入发送队列
    	if(outMessage.isNeedAck()) {
    		queueManager.sendedSIPMessage(outMessage);
    	}
    	messageLogger.logSIPMessage(outMessage);
    }

	/**
	 * 启动传输，这里只是简单的调用子类的启动函数
	 * @throws Exception 
	 */
    @Override
    public void startTransfer() throws Exception
    {
    	this.doStartTransfer();
    }

	/**
	 * 停止传输，调用子类停止传输后，关闭日志记录
	 * @throws IOException 
	 */
    @Override
    public void stopTransfer() throws Exception
    {
    	this.doStopTransfer();
    	this.messageLogger.close();
        queueManager.getTimeOutCheckTask().cancel();	//停止已发送队列超时检查任务
    }
    
    /**
     * 收到了回复
     * 这个方法供子类调用
     * @param response
     * @throws IOException
     */
    protected void responseReceived(SIPResponse response) throws IOException
    {
    	SIPRequest  request = (SIPRequest) this.queueManager.findSIPMessage(response);
    	this.listener.SIPResponseReceived(response, request);
    	this.messageLogger.logSIPMessage(response);
    }
    
    /**
     * 收到了通知
     * 这个方法供子类调用
     * @param notify
     * @throws IOException
     */
    protected void notifyReceived(SIPNotify notify) throws IOException
    {
    	this.listener.SIPNotifyReceived(notify);
    	this.messageLogger.logSIPMessage(notify);
    }
    
    /**
     * 发生了异常
     * 这个方法供子类调用
     * @param exception
     */
    protected void exceptionCaught(Throwable exception)
    {
    	this.listener.ExceptionCaught(exception);
    }
    
    /**
     * 发送消息
     * 子类在这个函数里完成消息发送工作
     * @param outMessage
     * @throws IOException
     */
    protected abstract void doSendSIPMessage(SIPOutMessage outMessage) throws IOException;
    
    /**
     * 启动传输
     * 子类重载
     * @throws Exception
     */
    protected abstract void doStartTransfer() throws Exception;
    
    /**
     * 停止传输
     * 子类重载
     * @throws Exception
     */
    protected abstract void doStopTransfer() throws Exception;
	
}
