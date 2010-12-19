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
 * Package  : net.solosky.maplefetion.util
 * File     : ObjectWaiter.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import net.solosky.maplefetion.net.MessageTimeoutException;
import net.solosky.maplefetion.sip.SIPResponse;

import org.apache.log4j.Logger;


/**
 *
 *  对象等待
 *  这个是个同步工具，可以让一个线程等待另一个线程的结果
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ResponseWaiter
{
	/**
	 * 等待对象
	 */
	private SIPResponse response;
	
	/**
	 * 同步锁
	 */
	private Object lock;
	
	/**
	 * 是否有人再等等
	 */
	private boolean isWaiting;
	
	/**
	 * 是否超时
	 */
	private boolean isTimeout;
	
	/**
	 * 默认构造函数
	 */
	public ResponseWaiter()
	{
		this.response = null;
		this.isWaiting = false;
		this.isTimeout = false;
		this.lock  = new Object();
	}
	
	/**
	 * 等待回复
	 * 如果结果没有返回，就在此等待
	 * @return  等待结果对象
	 */
	public SIPResponse waitResponse() throws MessageTimeoutException
	{
		synchronized (lock) {
			this.isWaiting = true;
			this.response   = null;
	        try {
	            lock.wait();
	            if(this.isTimeout) {
	            	throw new MessageTimeoutException();
	            }
            } catch (InterruptedException e) {
	           Logger.getLogger(ResponseWaiter.class).warn("ResponseWaiter Interrupted..");
            }
	        this.isWaiting = false;
	        return this.response;
        }
	}
	
	/**
	 * 等待对象收到
	 * @param target 等待对象
	 */
	public void responseRecived(SIPResponse response)
	{
		synchronized (lock) {
	        this.response = response;
	        lock.notifyAll();
        }
	}
	
	/**
	 * 请求超时
	 * @param exception
	 */
	public void setResponseTimeout()
	{
		synchronized (lock) {
			this.isTimeout = true;
	        lock.notifyAll();
        }
	}
	
	/**
	 * 判断是否有人在等待
	 * 这个函数并不阻塞
	 * @return
	 */
	public boolean isWaiting()
	{
		return this.isWaiting;
	}
}
