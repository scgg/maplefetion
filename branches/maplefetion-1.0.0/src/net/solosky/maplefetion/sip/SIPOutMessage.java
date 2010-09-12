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
 * Package  : net.solosky.maplefetion.sip
 * File     : InMessage.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sip;

import net.solosky.maplefetion.protocol.ISIPTimeoutHandler;


/**
 *
 * 可以发出去的消息
 * 
 * 子类有SIPRequest和SIPReceipt
 *
 * @author solosky <solosky772@qq.com> 
 */
public abstract class SIPOutMessage extends SIPMessage
{
	/**
	 * 是否需要回复
	 */
	protected boolean needAck;
	
	/**
	 * 有效时间，超过这个时间没有收到回复就重发
	 */
	protected int  aliveTime;
	
	/**
	 * 重发次数
	 */
	protected int retryTimes;
	
	/**
	 * 超时处理器
	 */
	protected ISIPTimeoutHandler timeoutHandler;
	
	
	/**
	 * 默认的构造函数
	 */
	public SIPOutMessage()
	{
		this.needAck = true;		//默认需要回复
		this.aliveTime = ((int) System.currentTimeMillis()/1000)+60;	//存活时间为发出去后的60秒
		this.retryTimes = 0;		//重发次数重置为0,最多只能重发3次。。。
	}
	
	
	/**
	 * 设置请求者
	 * @param from		请求者
	 */
	public void setFrom(String from)
	{
		this.getHeader(SIPHeader.FIELD_FROM).setValue(from);
	}
	
	/**
	 * 设置全局请求序号
	 * @param callid	全局请求序号
	 */
	public void setCallID(int callid)
	{
		this.getHeader(SIPHeader.FIELD_CALLID).setValue(Integer.toString(callid));
	}
	
	/**
	 * 设置请求序号
	 * @param sequence	请求序号
	 */
	public void setSequence(String sequence)
	{
		this.getHeader(SIPHeader.FIELD_SEQUENCE).setValue(sequence);
	}
	
	/**
	 * 设置请求长度
	 * @param length	请求长度
	 */
	public void setLength(int length)
	{
		this.getHeader(SIPHeader.FIELD_LENGTH).setValue(Integer.toString(length));
	}
	
	/**
	 * 转化为可以发送的字符串序列
	 * @return			可发送的字符串序列
	 */
	public abstract String toSendString();

	
	/**
	 * 当前信令是否需要回复
	 * @return
	 */
	public boolean isNeedAck()
    {
    	return needAck;
    }

	/**
	 * 设置是否需要回复
	 * @param needAck
	 */
	public void setNeedAck(boolean needAck)
    {
    	this.needAck = needAck;
    }
	
	/**
	 * 设置存活时间
	 * @param aliveTime
	 */
	public void setAliveTime(int aliveTime)
	{
		this.aliveTime = aliveTime;
	}
	
	/**
	 * 返回存活时间
	 * @return
	 */
	public int getAliveTime()
    {
    	return aliveTime;
    }

	/**
     *返回重发次数
     */
    public int getRetryTimes()
    {
    	return retryTimes;
    }

	/**
     * 递增重发次数
     */
    public void incRetryTimes()
    {
    	this.retryTimes++;
    }


	/**
	 * 返回超时处理器
     * @return the timeoutHandler
     */
    public ISIPTimeoutHandler getTimeoutHandler()
    {
    	return timeoutHandler;
    }


	/**
	 * 设置超时处理器
     * @param timeoutHandler the timeoutHandler to set
     */
    public void setTimeoutHandler(ISIPTimeoutHandler timeoutHandler)
    {
    	this.timeoutHandler = timeoutHandler;
    }	
	
	
}
