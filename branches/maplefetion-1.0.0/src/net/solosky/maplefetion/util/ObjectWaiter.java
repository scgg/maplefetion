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

import org.apache.log4j.Logger;


/**
 *
 *  对象等待
 *  这个是个同步工具，可以让一个线程等待另一个线程的结果
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ObjectWaiter<T>
{
	/**
	 * 等待对象
	 */
	private T target;
	
	/**
	 * 异常，如果发生的话
	 */
	private Exception exception;
	
	/**
	 * 同步锁
	 */
	private Object lock;
	
	/**
	 * 是否有人再等等
	 */
	private boolean isWaiting;
	
	/**
	 * 默认构造函数
	 */
	public ObjectWaiter()
	{
		this.target = null;
		this.exception = null;
		this.isWaiting = false;
		this.lock  = new Object();
	}
	
	/**
	 * 等待对象
	 * 如果结果没有返回，就在此等待
	 * @return  等待结果对象
	 */
	public T waitObject()
	{
		synchronized (lock) {
			this.isWaiting = true;
			this.target   = null;
			this.exception = null;
	        try {
	            lock.wait();
            } catch (InterruptedException e) {
	           Logger.getLogger(ObjectWaiter.class).warn("ActionWaiter Interrupted..");
            }
	        this.isWaiting = false;
	        return this.target;
        }
	}
	
	/**
	 * 等待对象收到
	 * @param target 等待对象
	 */
	public void objectArrive(T target)
	{
		synchronized (lock) {
	        this.target = target;
	        lock.notifyAll();
        }
	}
	
	/**
	 * 事件发生了异常
	 * @param exception
	 */
	public void objectException(Exception exception)
	{
		synchronized (lock) {
			this.target = null;
	        this.exception = exception;
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
	
	/**
	 * 判断是否有异常发生
	 * @return
	 */
	public boolean hasException()
	{
		return this.exception!=null;
	}
	
	/**
	 * 返回异常
	 * @return
	 */
	public Exception getException()
	{
		return this.exception;
	}
}
