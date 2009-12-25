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
 * File     : QueueManager.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-7
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPInMessage;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPRequest;

/**
 *
 * 发送队列管理
 * 处理查找对应的发出信令对象和定时检查超时信令并重发
 * 如果超时指定的次数，就向对话对象抛出超时异常，
 * 如果是普通的聊天对话，就简单的关闭这个聊天对话，如果是服务器对话就结束整个客户端
 *
 * @author solosky <solosky772@qq.com>
 */
public class QueueManager 
{
	/**
	 * 已发送队列
	 */
	private Queue<SIPOutMessage> sendQueue;
	
	/**
	 * 传输对象
	 */
	private ITransfer transfer;
	
	/**
	 * 定时检查超时任务，
	 */
	private TimerTask timeOutCheckTask;
	
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(QueueManager.class);
	
	
	/**
	 * 构造函数
	 * @param sendQueue		发送队列
	 * @param transfer		传输对象
	 */
	public QueueManager(Queue<SIPOutMessage> sendQueue, ITransfer transfer)
	{
		this.sendQueue = sendQueue;
		this.transfer  = transfer;
		this.timeOutCheckTask = new TimeOutCheckTask();
	}
	
	/**
	 * 构造函数
	 * @param transfer		传输对象
	 */
	public QueueManager(ITransfer transfer)
	{
		this( new LinkedList<SIPOutMessage>(), transfer);
	}
	
	/**
	 * 发送了一个SIP信令
	 * @param out
	 */
	public synchronized void sendedSIPMessage(SIPOutMessage out)
	{
		this.sendQueue.add(out);
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
					this.handleTimeOutException();
				}
			}
		}
	}
	
	/**
	 * 处理包超时异常
	 */
	private synchronized void handleTimeOutException()
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
    				req.setResponse(null);		//设置一个空的回复，可能调用者会出现Null异常。。。
    			}
    		}
    	}
    	//通知对话对象，发生了超时异常
    	this.transfer.getSIPMessageListener().ExceptionCaught(new IllegalStateException("A SIPMessage has sended 3 times without any response."));
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
	 * 
	 *	内部类，实现了超时检查任务
	 *  简单的委托给队列管理器处理
	 *
	 * @author solosky <solosky772@qq.com>
	 */
	public class TimeOutCheckTask extends TimerTask
	{

		/* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
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
}
