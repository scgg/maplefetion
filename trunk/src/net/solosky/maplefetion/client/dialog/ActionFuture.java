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
 * File     : ActionFuture.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-11
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;


/**
 *
 * 异步等待操作结果
 * 可以使用这个类来实现同步操作
 *
 * @author solosky <solosky772@qq.com>
 */
public class ActionFuture
{
	//锁
	private Object lock;
	//结果
	private int status;
	//是否已经通知
	private boolean isNotifyed;
	//是否超时
	private boolean isTimeout;
	//是否有传输异常
	private boolean isIoError;
	
	/**
	 * 构造函数
	 */
	public ActionFuture()
	{
		this.lock = new Object();
		this.clear();
	}
	
	/**
	 * 等待操作结果
	 * 事实上这个操作不会一直等待，因为传输层如果在请求发出的后的指定时间内没有收到回复，
	 * 并且重发过一定次数后仍没有收到回复，就会抛出请求超时异常
	 * 
	 * 建议使用这个方法等待操作结果
	 * 
	 * @throws InterruptedException 如果等待过程被中断就抛出中断异常
	 * @throws ReqeustTimeoutExcetion 如果请求超时会跑出请求超时异常
	 */
	public int waitStatus() throws RequestTimeoutException, InterruptedException
	{
		synchronized (lock) {
	        //判断是否已经提前通知过了
			if(this.isNotifyed)
				return this.status;
			
			//尝试等待
	        lock.wait();
             //判断是否超时
             if(this.isTimeout)
            	 throw new RequestTimeoutException();
             else
            	 return this.status;
        }
	}
	/**
	 * 在指定的时间内等待操作结果，
	 * 如果在指定的时间操作仍没有完成 或者操作已经发生了超时操作，就抛出RequestTimeoutException
	 * 
	 * @throws InterruptedException 如果等待过程被中断就抛出中断异常
	 * @throws TransferException    如果等待过程中出现网络异常就抛出
	 * @throws ReqeustTimeoutExcetion 如果请求超时会跑出请求超时异常
	 */
	public int waitStatus(long timeout) throws RequestTimeoutException, InterruptedException, TransferException
	{
		synchronized (lock) {
	        //判断是否已经提前通知过了
			if(this.isNotifyed)
				return this.status;
			
			//尝试等待
	        lock.wait(timeout);
	        
             //如果超时或者在等待的时间没有通知，就跑出请求超时异常
             if(this.isTimeout || !this.isNotifyed) {
            	 throw new RequestTimeoutException();
             }else if(this.isIoError) {
            	 throw new TransferException();
             }else {}
            return this.status;
        }
	}
	
	
	/**
	 * 设置操作结果
	 * @param status
	 */
	public void setStatus(int status)
	{
		synchronized (lock) {
	        this.isNotifyed = true;
	        this.status = status;
	        this.lock.notifyAll();
        }
	}
	
	/**
	 * 设置请求超时
	 */
	public void setTimeout()
	{
		synchronized (lock) {
	        this.isNotifyed = true;
	        this.isTimeout = true;
	        this.lock.notifyAll();
        }
	}
	
	/**
	 * 设置传输异常
	 */
	public void setIoError()
	{
		synchronized (lock) {
	        this.isNotifyed = true;
	        this.isIoError = true;
	        this.lock.notifyAll();
        }
	}
	/**
	 * 清除等待结果，便于下一次等待
	 */
	public void clear()
	{
		synchronized (lock) {
			this.status = 0;
	        this.isNotifyed = false;
	        this.isTimeout  = false;
	        this.isIoError = false;
        }
	}
	
	
}
