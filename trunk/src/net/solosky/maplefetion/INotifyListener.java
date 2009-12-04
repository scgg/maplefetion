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
 * Package  : net.solosky.maplefetion
 * File     : NotifyListener.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-23
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import net.solosky.maplefetion.bean.FetionBuddy;

/**
 *
 *	通知监听接口
 *
 *	收到服务器发回的相关通知便会调用
 *
 *  警告：
 *  这里的所有函数如果要进行有关客户端同步的操作，（如接受到好友请求马上回复同意）必须在另外一个线程调用
 *  如果在收到通知后马上调用有关客户端的同步方法就会会造成死锁，因为回调函数还在读数据线程的调用栈上，等待回复永远不可能成功
 *  可以调用异步的方法，如异步发送消息等。。
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface INotifyListener
{
	/**
	 * 收到用户消息
	 * @param from 		来自好友
	 * @param message	用户消息字符串
	 */
	public void buddyMessageRecived(FetionBuddy from, String message);
	
	/**
	 * 收到系统消息
	 * @param m			系统消息字符串
	 */
	public void systemMessageRecived(String m);
	
	/**
	 * 收到添加好友请求
	 * @param b			待添加的好友
	 * @param desc		请求者
	 */
	public void buddyApplication(FetionBuddy buddy, String desc);
	
	/**
	 * 对方同意了添加对方为好友
	 * @param buddy		好友对象
	 * @param isAgreed 	对方是否同意添加
	 */
	public void buddyConfirmed(FetionBuddy buddy, boolean isAgreed);
	
	/**
	 *  用户状态改变了
	 * @param 状态改变的好友
	 */
	public void presenceChanged(FetionBuddy buddy);
	
	/**
	 * 客户端发生了错误
	 * 这个错误是致命的，无法修复，调用者应该结束自己的程序或者重新登录
	 * @param exception
	 */
	public void exceptionCaught(Throwable exception);
}
