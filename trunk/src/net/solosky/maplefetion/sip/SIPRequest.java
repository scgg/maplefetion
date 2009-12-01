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
 * File     : SIPRequest.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sip;

import java.util.Iterator;

import net.solosky.maplefetion.protocol.ISIPResponseHandler;

import org.apache.log4j.Logger;

/**
 *
 * SIP请求
 * 这也是一个抽象类，所有的子类都代表了一个具体的请求，比如登录验证，发送短信等
 * 应使用SIPRequestFactory来创建相应的具体请求
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SIPRequest extends SIPOutMessage
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
	 * 相应的回复
	 */
	private SIPResponse response;
	
	/**
	 * 回复的监听器
	 */
	private ISIPResponseHandler handler;
	
	/**
	 * 默认构造函数
	 */
	public SIPRequest(String method, String domain)
	{
		this.method = method;
		this.domain = domain;
	}
	

	/**
	 * 转化为可以发送的字符串序列
	 * @return			可发送的字符串序列
	 */
	public String toSendString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.method+' '+this.domain+' '+SIPMessage.SIP_VERSION+"\r\n");
		Iterator<SIPHeader> it = this.getHeaders().iterator();
		while(it.hasNext()) {
			buffer.append(it.next().toSendString());
		}
		if(this.body!=null) {
			int len = this.body.toSendString().getBytes().length;
			if(len>0)
				buffer.append("L: "+len+"\r\n");
		}
		buffer.append("\r\n");
		if(this.body!=null)
			buffer.append(body.toSendString());
		
		return buffer.toString();
	}
	
	
	public String toString()
	{
		return "[SIPRequest: method="+this.method+"; L:"+(getBody()!=null?getBody().getLength():"0")+"]";
	}
	
	/**
	 * 设置回复对象
	 * @param response
	 */
	public void setResponse(SIPResponse response)
	{
		synchronized (this) {
	        this.response = response;
	        this.notifyAll();
        }
	}
	
	/**
	 * 等待回复收到,如果没有收到回复就在此等待
	 * @return  回复对象
	 */
	public SIPResponse waitRepsonse()
	{
		synchronized (this) {
            try {
            	 while(this.response==null)
            		 this.wait();
            	 return this.response;
            } catch (InterruptedException e) {
            	Logger.getLogger(SIPRequest.class).warn("Wait response failed:"+e);
            }
	       return null;
        }
	}
	
	/**
	 * 设置回复监听器
	 * @param listener
	 */
	public void setResponseHandler(ISIPResponseHandler handler)
	{
		this.handler = handler;
	}
	
	/**
	 * 返回回复监听器
	 * @return
	 */
	public ISIPResponseHandler getResponseHandler()
	{
		return this.handler;
	}

}
