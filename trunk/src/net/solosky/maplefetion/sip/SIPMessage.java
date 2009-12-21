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
 * File     : SIPMessage.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sip;

import java.util.ArrayList;
import java.util.Iterator;


/**
 *
 * SIP消息
 * 这是一个抽象类，SIP发送和回复都是其子类
 * 所有的请求和回复都是异步的
 *
 * @author solosky <solosky772@qq.com> 
 */
public abstract class SIPMessage
{
	
	/**
	 * SIP版本字符串
	 */
	public static String SIP_VERSION = "SIP-C/2.0";
	
	/**
	 * 消息头列表
	 */
	protected ArrayList<SIPHeader> headers;
	
	/**
	 * 消息正文
	 */
	protected SIPBody body;
	
	
	/**
	 * 默认构造函数
	 */
	public SIPMessage()
	{	
		headers = new ArrayList<SIPHeader>();
	}
	
	/**
	 * 返回消息头
	 * @param name	消息头值
	 * @return		消息头
	 */
	public SIPHeader getHeader(String name)
	{
		Iterator<SIPHeader> it = headers.iterator();
		while(it.hasNext()) {
			SIPHeader header = it.next();
			if(header.getName()!=null&&header.getName().equals(name))
				return header;
		}
		return null;
	}
	
	/**
	 * 检查是否有给定名字的消息头
	 * @param name	消息头名
	 * @return		存在返回true不存在返回false
	 */
	public boolean hasHeader(String name)
	{
		Iterator<SIPHeader> it = headers.iterator();
		while(it.hasNext()) {
			SIPHeader header = it.next();
			if(header.getName().equals(name))
				return true;
		}
		return false;
	}
	
	/**
	 * 添加头部
	 * @param header	请求头
	 */
	public void addHeader(SIPHeader header)
	{
		this.headers.add(header);
	}
	
	/**
	 * 添加头部
	 * @param name		请求头名
	 * @param value		请求头值
	 */
	public void addHeader(String name, String value)
	{
		this.addHeader(new SIPHeader(name, value));
	}
	
	
	/**
	 * 返回所有的消息头集合
	 * @return		消息头集合
	 */
	public ArrayList<SIPHeader> getHeaders()
	{
		return headers;
	}
	
	/**
	 * 设置消息正文
	 * @param body	消息正文
	 */
	public void setBody(SIPBody body)
	{
		this.body = body;
	}
	
	/**
	 * 返回消息正文
	 * @return body		消息正文
	 */
	public SIPBody getBody()
	{
		return body;
	}	
	
}
