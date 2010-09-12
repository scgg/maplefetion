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
 * File     : TransferSerivce.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-4
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPInMessage;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.SIPMessageLogger;

import org.apache.log4j.Logger;

/**
 *
 * 传输服务
 * 提供发出包和接收包的管理，日志记录，并处理超时异常
 *
 * @author solosky <solosky772@qq.com>
 */
public class TransferService implements ISIPMessageListener
{
	/**
	 * 传输对象
	 */
	private ITransfer transfer;
	
	/**
	 * 监听器
	 */
	protected ISIPMessageListener listener;
	
	/**
	 * 已发送队列
	 */
	private Queue<SIPOutMessage> sendQueue;
	
	/**
	 * 信令记录器
	 */
	protected SIPMessageLogger messageLogger;
	
	/**
	 * 定时检查超时任务，
	 */
	private TimerTask timeOutCheckTask;
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(TransferService.class);
	
	
	/**
	 * 默认构造函数
	 */
	public TransferService(ITransfer transfer, ISIPMessageListener listener)
	{
		this.transfer = transfer;
		this.listener = listener;
		this.sendQueue =  new LinkedList<SIPOutMessage>();
		this.messageLogger = SIPMessageLogger.create(transfer.getName());
		this.timeOutCheckTask = new TimeOutCheckTask();
		
		this.transfer.setSIPMessageListener(this);
		
	}
	
	/**
	 * 发送SIP信令
	 * @param outMessage
	 * @throws IOException
	 */
	public void sendSIPMessage(SIPOutMessage outMessage) throws IOException
    {
    	//交给传输对象发送这个消息
    	this.transfer.sendSIPMessage(outMessage);
    	//如果需要回复才放入发送队列
    	if(outMessage.isNeedAck()) {
    		this.sendQueue.add(outMessage);
    	}
    	messageLogger.logSIPMessage(outMessage);
    }
	
	/**
	 * 在已发送队列中查找对应发送信令
	 * @param in
	 * @return
	 */
	public synchronized SIPOutMessage findSIPMessage(SIPInMessage in)
	{
		Iterator<SIPOutMessage> it = this.sendQueue.iterator();
    	SIPOutMessage out = null;
    	String inCallID = in.getHeader(SIPHeader.FIELD_CALLID).getValue();
    	String inSequence = in.getHeader(SIPHeader.FIELD_SEQUENCE).getValue();
    	String outCallID = null;
    	String outSequence = null;
    	while(it.hasNext()) {
    		out = it.next();
    		outCallID = out.getHeader(SIPHeader.FIELD_CALLID).getValue();
    		outSequence = out.getHeader(SIPHeader.FIELD_SEQUENCE).getValue();
    		if(inCallID.equals(outCallID) && inSequence.equals(outSequence) ){
    			it.remove();
				return out;
			}
    	}
    	return null;
	}
	
	/**
	 * 检查超时的包，如果没有超过指定的重发次数，就重发
	 * 如果只要有一个包超出了重发的次数，就抛出超时异常
	 * @throws IOException 
	 */
	private synchronized void checkTimeOutMessage() throws IOException
	{	
		SIPOutMessage out = null;
		int curtime = (int) System.currentTimeMillis()/1000;
		int maxTryTimes = FetionConfig.getInteger("fetion.sip.default-retry-times");
		int aliveTimes = FetionConfig.getInteger("fetion.sip.default-alive-time");
		//如果队列为空就不需要查找了
		if(this.sendQueue.size()==0)
			return;
		
		//从队列的头部开始查找，如果查到一个包存活时间还没有超过当前时间，就不在查找，因为队列中包的时间是按时间先后顺序存放的
		while(true) {
			out = this.sendQueue.peek();
			if(out.getAliveTime()>curtime) {
				return;		//当前包还处于存活期内，退出查找
			}else {
				//当前这个包是超时的包
				if(out.getRetryTimes()<maxTryTimes) {
					//如果小于重发次数，就重发这个包
					logger.debug("A OutMessage:"+out+" timeout, now resend it...");
					this.sendQueue.poll();
					out.incRetryTimes();
					out.setAliveTime(((int)System.currentTimeMillis()/1000)+aliveTimes);
					this.transfer.sendSIPMessage(out);
				}else {		//这个包已经超过重发次数，通知对话对象，发生了超时异常
					logger.warn("A OutMessage is resend three times, handle this timeout exception...");
					this.handlePacketTimeout(out);
				}
			}
		}
	}
	
	
	/**
	 * 处理包超时异常
	 * @param timeoutMessage
	 */
	private void handlePacketTimeout(SIPOutMessage timeoutMessage)
	{
		//如果发出包设置了超时处理器，就调用超时处理器，否则抛出超时异常，结束整个程序
		if(timeoutMessage.getTimeoutHandler()!=null) {
			timeoutMessage.getTimeoutHandler().handleTimeout(timeoutMessage);
		}else {
			this.handleTimeOutException(timeoutMessage);
		}
	}
	
	/**
	 * 超时退出程序
	 */
	private synchronized void handleTimeOutException(SIPOutMessage timeoutMessage)
	{
		//遍历所有发出队列里面的包，如果有是请求包并在等待回复，则通知回复
		Iterator<SIPOutMessage> it = this.sendQueue.iterator();
    	SIPOutMessage out = null;
    	while(it.hasNext()) {
    		out = it.next();
    		if(out.isNeedAck()) {
    			//判断是否是请求包，如果是强制转换，因为只有请求包才会需要等待回复
    			if(out instanceof SIPRequest) {
    				SIPRequest req = (SIPRequest) out;
    				req.getResponseWaiter().setResponseTimeout();		//设置该请求超时
    			}
    		}
    	}
    	//通知对话对象，发生了超时异常
    	this.listener.ExceptionCaught(new MessageTimeoutException(timeoutMessage));
	}

	
	/**
	 * 返回超时检查工作
	 * @return
	 */
	public TimerTask getTimeOutCheckTask()
	{
		return this.timeOutCheckTask;
	}
	
	/**
	 * 启动服务
	 * @throws Exception
	 */
	public void startService() throws Exception
	{
		this.transfer.startTransfer();
	}
	
	/**
	 * 停止服务
	 * @throws Exception
	 */
	public void stopService() throws Exception
	{
		this.transfer.stopTransfer();
	}
	
	
	
	/**
	 * 
	 *	内部类，实现了超时检查任务
	 *  简单的委托给队列管理器处理
	 *
	 * @author solosky <solosky772@qq.com>
	 */
	public class TimeOutCheckTask extends TimerTask
	{
        @Override
        public void run()
        {
        	try {
        		logger.debug("TimeOutCheckTask is checking sended queue..[QueueSize:"+sendQueue.size()+"]");
	            checkTimeOutMessage();
            } catch (Exception e) {
            	logger.warn("Exception caught when run TimeOutCheckTask..");
            	transfer.getSIPMessageListener().ExceptionCaught(e);
            }
        }
		
	}

    @Override
    public void ExceptionCaught(Throwable e)
    {
    	listener.ExceptionCaught(e);
    }
    @Override
    public void SIPNotifyReceived(SIPNotify notify)
    {
	    listener.SIPNotifyReceived(notify);
	    try {
	        this.messageLogger.logSIPMessage(notify);
        } catch (IOException e) {
	       logger.warn("Cannot log sip notify:"+e);
        }
    }
    @Override
    public void SIPResponseReceived(SIPResponse response, SIPRequest request)
    {
    	request = (SIPRequest) this.findSIPMessage(response);
    	this.listener.SIPResponseReceived(response, request);
    	try {
	        this.messageLogger.logSIPMessage(response);
        } catch (IOException e) {
	       logger.warn("Cannot log sip response:"+e);
        }
    }
}
