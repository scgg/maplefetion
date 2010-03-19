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
 * Package  : net.solosky.net.maplefetion.util
 * File     : MessageLogger.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-8
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.chain.AbstractProcessor;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcInMessage;
import net.solosky.maplefetion.sipc.SipcMessage;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcResponse;

import org.apache.log4j.Logger;

/**
*
*	SIP信令记录器
*
* @author solosky <solosky772@qq.com> 
*/
public class MessageLogger extends AbstractProcessor
{
	private String name;
	private BufferedWriter writer;
	private boolean enableLogging;
	private boolean isClosed;
	private static Logger logger = Logger.getLogger(MessageLogger.class);
	/**
	 * 构造函数
	 * @param fileName
	 */
	public MessageLogger(String name)
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
	public void logMessage(SipcInMessage in) throws IOException
	{
		if(!enableLogging || writer==null)
			return;
		
		writer.append("接受信令:<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\r\n");
		if(in instanceof SipcResponse) {
			SipcResponse res = (SipcResponse) in;
			writer.append(SipcMessage.SIP_VERSION+" "+res.getStatusCode()+" "+res.getStatusMessage()+"\r\n");
		}else {
			SipcNotify no = (SipcNotify) in;
			writer.append(no.getMethod()+" "+no.getSid()+" "+SipcMessage.SIP_VERSION+"\r\n");
		}
		Iterator<SipcHeader> it = in.getHeaders().iterator();
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
	public void logMessage(SipcOutMessage out) throws IOException
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
	public static MessageLogger create(String name)
	{
		return new MessageLogger(name);
	}
	/* (non-Javadoc)
     * @see net.solosky.net.maplefetion.chain.Processor#getProcessorName()
     */
    @Override
    public String getProcessorName()
    {
	    return "MessageLogger";
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.chain.AbstractProcessor#doProcessIncoming(java.lang.Object)
     */
    @Override
    protected boolean doProcessIncoming(Object o) throws FetionException
    {
	    try {
	        this.logMessage((SipcInMessage) o);
        } catch (IOException e) {
	      throw new FetionException(e);
        }
        return true;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.chain.AbstractProcessor#doProcessOutcoming(java.lang.Object)
     */
    @Override
    protected boolean doProcessOutcoming(Object o) throws FetionException
    {
    	 try {
 	        this.logMessage((SipcOutMessage) o);
         } catch (IOException e) {
        	 throw new FetionException(e);
         }
         return true;
    }

}