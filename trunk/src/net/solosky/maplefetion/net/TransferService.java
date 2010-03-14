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
 * Package  : net.solosky.net.maplefetion.net
 * File     : TransferService.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-6
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.chain.AbstractProcessor;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;

import org.apache.log4j.Logger;

/**
 * 
 * 传输服务 提供发出包和接收包的管理，并处理超时异常
 * 
 * @author solosky <solosky772@qq.com>
 */
public class TransferService extends AbstractProcessor
{

	/**
	 * 已发送请求队列
	 */
	private Queue<SipcRequest> requestQueue;

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
	public TransferService()
	{
		this.requestQueue = new LinkedList<SipcRequest>();
		this.timeOutCheckTask = new TimeOutCheckTask();
	}

	/**
	 * 处理接受的包 在已发送队列中查找相对应的发出包，然后放入接受包中
	 */
	@Override
	protected boolean doProcessIncoming(Object o) throws FetionException
	{
		if (o instanceof SipcResponse) {
			// 如果是回复的话，查找对应的请求，并通知回复等待对象
			SipcResponse response = (SipcResponse) o;
			SipcRequest request = this.findRequest(response);
			response.setRequest(request);
			
			//找到了对应的请求，然后判断是否回复已经到了指定的回复次数，如果到了就从队列中移除，如果没有，就不移除
			if(request!=null) {
				request.incReplyTimes();
				if(request.getNeedReplyTimes()==request.getReplyTimes()) {
					requestQueue.remove(request);
				}
			}
		}

		return true;
	}

	/**
	 * 处理发送的包 如果这个包需要回复，就把这个包加入到已发送的队列中，然后隔一段时间后检查是否超时
	 */
	@Override
	protected boolean doProcessOutcoming(Object o) throws FetionException
	{
		if (o instanceof SipcRequest) {
			SipcRequest request = (SipcRequest) o;
			//判断需要回复次数大于0才放人队列中
			if(request.getNeedReplyTimes()>0)
				this.requestQueue.add(request);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.solosky.net.maplefetion.chain.Processor#getProcessorName()
	 */
	@Override
	public String getProcessorName()
	{
		return TransferService.class.getName();
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.chain.AbstractProcessor#stopProcessor()
     */
    @Override
    public void stopProcessor() throws FetionException
    {
    	//停止超时检查定时任务
    	this.timeOutCheckTask.cancel();
    	//通知当前发送队列中的请求都超时
    	Iterator<SipcRequest> it = this.requestQueue.iterator();
    	while(it.hasNext()) {
    		SipcRequest request = it.next();
    		if(request.getResponseHandler()!=null) {
    			request.getResponseHandler().timeout(request);
    		}
    	}
    }

	/**
	 * 返回超时检查工作
	 * 
	 * @return
	 */
	public TimerTask getTimeOutCheckTask()
	{
		return this.timeOutCheckTask;
	}

	/**
	 * 
	 * 内部类，实现了超时检查任务 简单的委托给队列管理器处理
	 * 
	 * @author solosky <solosky772@qq.com>
	 */
	public class TimeOutCheckTask extends TimerTask
	{
		@Override
		public void run()
		{
			try {
				checkTimeOutRequest();
			} catch (FetionException e) {
				raiseException(e);
			}
		}
	}

	/**
	 * 在已发送队列中查找对应发送信令
	 * 
	 * @param response
	 * @return
	 */
	public synchronized SipcRequest findRequest(SipcResponse response)
	{
		Iterator<SipcRequest> it = this.requestQueue.iterator();
		SipcRequest request = null;
		String resCallID = response.getHeader(SipcHeader.CALLID).getValue();
		String resSequence = response.getHeader(SipcHeader.SEQUENCE).getValue();
		String reqCallID = null;
		String reqSequence = null;
		while (it.hasNext()) {
			request = it.next();
			reqCallID = request.getHeader(SipcHeader.CALLID).getValue();
			reqSequence = request.getHeader(SipcHeader.SEQUENCE).getValue();
			//如果callID和sequence都相等的话就找到了指定的请求
			if (resCallID.equals(reqCallID) && resSequence.equals(reqSequence)) {
				return request;
			}
		}
		return null;
	}

	/**
	 * 检查超时的包，如果没有超过指定的重发次数，就重发 如果只要有一个包超出了重发的次数，就抛出超时异常
	 * 
	 * @throws FetionException
	 */
	private synchronized void checkTimeOutRequest() throws FetionException
	{
		SipcRequest out = null;
		int curtime = (int) System.currentTimeMillis() / 1000;
		int maxTryTimes = FetionConfig
		        .getInteger("fetion.sip.default-retry-times");
		int aliveTimes = FetionConfig
		        .getInteger("fetion.sip.default-alive-time");
		// 如果队列为空就不需要查找了
		if (this.requestQueue.size() == 0)
			return;

		// 从队列的头部开始查找，如果查到一个包存活时间还没有超过当前时间，就不在查找，因为队列中包的时间是按时间先后顺序存放的
		while (true) {
			out = this.requestQueue.peek();
			if (out.getAliveTime() > curtime) {
				logger.debug("Request was not responsed but still in alive time. " +
						"Request="+out+", AliveTime="+out.getAliveTime()+", RetryTimes="+out.getReplyTimes());
				return; // 当前包还处于存活期内，退出查找
			} else {
				// 当前这个包是超时的包
				if (out.getRetryTimes() < maxTryTimes) {
					// 如果小于重发次数，就重发这个包
					logger.debug("Request was timeout, now sending it again... Request="+out);
					this.requestQueue.poll();
					out.incRetryTimes();
					out.setAliveTime(((int) System.currentTimeMillis() / 1000) + aliveTimes);
					// 重新发送这个包
					this.processOutcoming(out);
				} else { // 这个包已经超过重发次数，通知对话对象，发生了超时异常
					logger.warn("A OutMessage is resend three times, handle this timeout exception...");
					this.handleRequestTimeout(out);
				}
			}
		}
	}

	/**
	 * 处理包超时异常
	 * 
	 * @param timeoutMessage
	 * @throws FetionException 
	 */
	private void handleRequestTimeout(SipcRequest request) throws FetionException
	{
		//发出包设置了超时处理器，就调用超时处理器，否则抛出超时异常，结束整个程序
		if (request.getResponseHandler() != null) {
			request.getResponseHandler().timeout(request);
		} else {
			this.handleTimeOutException(request);
		}
	}

	/**
	 * 超时退出程序
	 */
	private synchronized void handleTimeOutException(SipcRequest request)
	{
		// 通知对话对象，发生了超时异常
		this.raiseException(new RequestTimeoutException(request));
	}

}
