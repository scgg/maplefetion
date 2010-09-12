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
 * File     : SIPMessageLogger.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPInMessage;
import net.solosky.maplefetion.sip.SIPMessage;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPResponse;

/**
 *
 *	SIP信令记录器
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SIPMessageLogger
{
	private String name;
	private BufferedWriter writer;
	private boolean enableLogging;
	private boolean isClosed;
	private static Logger logger = Logger.getLogger(SIPMessageLogger.class);
	/**
	 * 构造函数
	 * @param fileName
	 */
	public SIPMessageLogger(String name)
	{
		this.name = name;
		enableLogging = FetionConfig.getBoolean("log.sip.enable");
		if(!enableLogging)
			return;
		String fileName = FetionConfig.getString("log.sip.dir")+name+".log";
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
		}catch (IOException e) {
			logger.warn("Cannot create SIPMessage log file:"+fileName);
		}
		isClosed = false;
	}
	
	/**
	 * 记录收到的信令
	 * @param in
	 * @throws IOException 
	 */
	public void logSIPMessage(SIPInMessage in) throws IOException
	{
		if(!enableLogging || writer==null)
			return;
		
		writer.append("接受信令:<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\r\n");
		if(in instanceof SIPResponse) {
			SIPResponse res = (SIPResponse) in;
			writer.append(SIPMessage.SIP_VERSION+" "+res.getStatusCode()+" "+res.getStatusMessage()+"\r\n");
		}else {
			SIPNotify no = (SIPNotify) in;
			writer.append(no.getMethod()+" "+no.getSid()+" "+SIPMessage.SIP_VERSION+"\r\n");
		}
		Iterator<SIPHeader> it = in.getHeaders().iterator();
		while(it.hasNext()) {
			writer.append(it.next().toSendString());
		}
		writer.append("\r\n");
		if(in.getBody()!=null)
			writer.append(in.getBody().toSendString());
		writer.append("\r\n-------------------------------------------\r\n");
		writer.flush();
	}
	
	/**
	 * 记录发出包
	 * @param out
	 * @throws IOException 
	 */
	public void logSIPMessage(SIPOutMessage out) throws IOException
	{
		if(!enableLogging || writer==null)
			return;
		
		writer.append("发送信令:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\r\n");
		writer.append(out.toSendString());
		writer.append("\r\n--------------------------------------------\r\n");
		writer.flush();
	}
	
	/**
	 * 关闭记录器
	 * @throws IOException 
	 */
	public void close() throws IOException
	{
		if(!enableLogging || !isClosed || writer==null)
			return;
		else {
			writer.close();
		}
	}
	
	/**
	 * 返回记录器的明智
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * 工厂方法
	 * @param name
	 * @return
	 */
	public static SIPMessageLogger create(String name)
	{
		return new SIPMessageLogger(name);
	}
	
}
