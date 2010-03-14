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
 * Package  : net.solosky.net.maplefetion.sipc
 * File     : SipcRequest.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-5
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sipc;

import java.util.Iterator;

import net.solosky.maplefetion.client.ResponseHandler;
import net.solosky.maplefetion.util.ConvertHelper;

/**
*
* SIP请求
* 这也是一个抽象类，所有的子类都代表了一个具体的请求，比如登录验证，发送短信等
* 应使用SIPRequestFactory来创建相应的具体请求
*
* @author solosky <solosky772@qq.com> 
*/
public class SipcRequest extends SipcOutMessage
{
	/**
	 * 请求方法
	 */
	private String method;
	
	/**
	 * 请求域
	 */
	private String domain;
	
	/**
	 * 有效时间，超过这个时间没有收到回复就重发
	 */
	protected int  aliveTime;
	
	/**
	 * 重发次数
	 */
	protected int retryTimes;
	
	/**
	 * 需要回复的次数，有些请求会回复两次，如群的I请求，有些请求则不需要回复如群的A请求 (飞信协议真乱。。)
	 */
	protected int needReplyTimes;
	
	/**
	 * 当前回复次数
	 */
	protected int replyTimes;
	
	/**
	 * 回复处理器
	 */
	private ResponseHandler responseHandler;

	/**
	 * 默认构造函数
	 */
	public SipcRequest(String method, String domain)
	{
		this.method = method;
		this.domain = domain;
		this.aliveTime = ((int) System.currentTimeMillis()/1000)+60;	//存活时间为发出去后的60秒
		this.retryTimes = 0;											//重发次数重置为0,最多只能重发3次。。。
		this.needReplyTimes = 1 ;
		this.replyTimes =0;
	}	
	
	/**
     * @return the responseHandler
     */
    public ResponseHandler getResponseHandler()
    {
    	return responseHandler;
    }


	/**
     * @param responseHandler the responseHandler to set
     */
    public void setResponseHandler(ResponseHandler responseHandler)
    {
    	this.responseHandler = responseHandler;
    }
    
    


	/**
	 * 转化为可以发送的字符串序列
	 * @return			可发送的字符串序列
	 */
	public String toSendString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.method+' '+this.domain+' '+SipcMessage.SIP_VERSION+"\r\n");
		Iterator<SipcHeader> it = this.getHeaders().iterator();
		while(it.hasNext()) {
			buffer.append(it.next().toSendString());
		}
		if(this.body!=null) {
			int len =ConvertHelper.string2Byte(body.toSendString()).length;
			if(len>0)
				buffer.append("L: "+len+"\r\n");
		}
		buffer.append("\r\n");
		if(this.body!=null)
			buffer.append(body.toSendString());
		
		return buffer.toString();
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
     * @return the needReplyTimes
     */
    public int getNeedReplyTimes()
    {
    	return needReplyTimes;
    }

	/**
     * @param needReplyTimes the needReplyTimes to set
     */
    public void setNeedReplyTimes(int needReplyTimes)
    {
    	this.needReplyTimes = needReplyTimes;
    }

	/**
     * @return the replyTimes
     */
    public int getReplyTimes()
    {
    	return replyTimes;
    }

	/**
     * 递增回复次数
     */
    public void incReplyTimes()
    {
    	this.replyTimes++;
    }

	/**
     * @return the method
     */
    public String getMethod()
    {
    	return method;
    }


	public String toString()
	{
		return "[SipcRequest: M="+this.method+", I="+this.getHeader(SipcHeader.CALLID).getValue()+
				", Q="+this.getHeader(SipcHeader.SEQUENCE).getValue()+
				", L="+(this.getBody()!=null? this.getBody().getLength():0)+"]";
	}

	
}