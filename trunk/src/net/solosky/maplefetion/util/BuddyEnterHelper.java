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
 * File     : BuddyEnterFuture.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-1
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import net.solosky.maplefetion.bean.Buddy;

/**
 *
 *  等待好友进入对话框
 *
 * @author solosky <solosky772@qq.com>
 */
public class BuddyEnterHelper
{
	/**
	 * 锁，利用这个对象来实现等待
	 */
	private Object lock;
	
	/**
	 * 当前的好友
	 */
	private Buddy curBuddy;
	
	/**
	 * 构造函数
	 */
	public BuddyEnterHelper()
	{
		this.lock = new Object();
	}
	
	/**
	 * 等待好友进入对话框
	 * @param buddy
	 * @throws InterruptedException 
	 */
	public void waitBuddyEnter(Buddy buddy) throws InterruptedException
	{
		synchronized (lock) {
        	while(this.curBuddy==null || !this.curBuddy.getUri().equals(buddy.getUri())) {
        		lock.wait();
        	}
        	return;
        }
	}
	
	
	/**
	 * 通知等待线程，好友已经进入了   
	 * @param buddy
	 */
	public void buddyEntered(Buddy buddy)
	{
		synchronized (lock) {
			this.curBuddy = buddy;
			lock.notifyAll();
        }
	}
}
