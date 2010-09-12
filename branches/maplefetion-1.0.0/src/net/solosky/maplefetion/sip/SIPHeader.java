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
 * File     : SIPHeader.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sip;

/**
 *
 * SIP消息头
 * 
 * 一个消息头格式: name: value
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SIPHeader
{
	/**
	 * 消息头名
	 */
	private String name;
	
	/**
	 * 消息值
	 */
	private String value;
	
	
	public final static String FIELD_CALLID   = "I";
	public final static String FIELD_SEQUENCE = "Q";
	public final static String FIELD_FROM     = "F";
	public final static String FIELD_LENGTH   = "L";
	public final static String FIELD_EVENT    = "N";
	public final static String FIELD_TO       = "T";
	public final static String FIELD_SUPPORTED       = "K";
	public final static String FIELD_CONTENT_TYPE    = "C";
	public final static String FIELD_AUTHORIZATION   = "A";
	public final static String FIELD_WWWAUTHENTICATE = "W";
	
	/**
	 * 使用给定的头和值构造消息头
	 * @param name		消息头名
	 * @param value		消息头值
	 */
	public SIPHeader(String name, String value)
	{
		this.name  = name;
		this.value = value;
	}
	
	/**
	 * 默认的构造函数
	 */
	public SIPHeader()
	{
	}
	
	/**
	 * 以一个消息头字符串构造
	 * @param header	返回的消息头 如:  I:22
	 */
	public SIPHeader(String header)
	{
		int i = header.indexOf(':');
		if(i!=-1) {
			this.name = header.substring(0,i).trim();
			this.value = header.substring(i+1).trim();
		}else {}
	}
	
	/**
	 * 设置消息头名
	 * @param name		消息头名
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * 设置消息头值
	 * @param value		消息头值
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	/**
	 * 返回消息头名
	 * @return			消息头名
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * 返回消息头值
	 * @return		消息头值
	 */
	public String getValue()
	{
		return this.value;
	}
	
	/**
	 * ToString
	 * @return 		消息完整字符串
	 */
	public String toSendString()
	{
		return this.name+": "+value+"\r\n";
	}
}
