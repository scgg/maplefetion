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
 * File     : BugReporter.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-1
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcInMessage;
import net.solosky.maplefetion.sipc.SipcMessage;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcResponse;

/**
 *
 * 错误报告工具
 * 如果客户端发生错误了，可以使用这个工具把报告发送到指定的接口
 * （接口还未实现）
 *
 * @author solosky <solosky772@qq.com>
 */
public class CrushBuilder
{
	/**
	 * 字符缓冲对象
	 */
	private StringBuffer buffer;
	
	/**
	 * LOGGER
	 */
	private static Logger logger = Logger.getLogger(CrushBuilder.class);
	
	/**
	 * 默认构造函数
	 */
	public CrushBuilder()
	{
		buffer = new StringBuffer();
	}
	
	/**
	 * 构建异常报告
	 * @param exception
	 * @return
	 */
	public void dumpException(Throwable exception)
	{
		buffer.append("------------Exception-----------\n");
		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		buffer.append(writer.toString());
	}
	
	/**
	 * 构建消息报告
	 * @param message
	 */
	public void dumpSipcMessage(SipcMessage message)
	{
		buffer.append("----------SipcMessage-----------\n");
		buffer.append("Class:"+message.getClass().getSimpleName()+"\n");
		if(message instanceof SipcOutMessage) {
			SipcOutMessage out = (SipcOutMessage) message;
			buffer.append(out.toSendString());
		}else {
			SipcInMessage in = (SipcInMessage) message;
			if(in instanceof SipcResponse) {
				SipcResponse res = (SipcResponse) in;
				buffer.append(SipcMessage.SIP_VERSION+" "+res.getStatusCode()+" "+res.getStatusMessage()+"\r\n");
			}else {
				SipcNotify no = (SipcNotify) in;
				buffer.append(no.getMethod()+" "+no.getSid()+" "+SipcMessage.SIP_VERSION+"\r\n");
			}
			Iterator<SipcHeader> it = in.getHeaders().iterator();
			while(it.hasNext()) {
				buffer.append(it.next().toSendString());
			}
			buffer.append("\r\n");
			if(in.getBody()!=null)
				buffer.append(in.getBody().toSendString());
		}
	}
	
	/**
	 * 构建报告的头部
	 */
	public void buildHeader()
	{
		buffer.append("==========MapleFetion CrushReport=============\n");
		buffer.append("if you saw this report, FetionClient maybe crushed by some exceptions.\n");
		buffer.append("\n");
	}
	
	/**
	 * 打印版本信息
	 */
	public void dumpVersion()
	{
		buffer.append("------------Version------------\n");
		buffer.append("ClientVersion   : "+FetionClient.CLIENT_VERSION+"\n");
		buffer.append("ProtocolVersion : "+FetionClient.PROTOCOL_VERSION+"\n");
		buffer.append("\n");
	}
	
	/**
	 * 打印配置
	 */
	public void dumpConfig()
	{
		buffer.append("------------Config-------------\n");
		
		Properties prop = FetionConfig.getProperties();
		Iterator<Object> it = prop.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)  it.next();
			buffer.append(key+" = " + prop.getProperty(key)+"\n");
		}
		
		buffer.append("\n");
	}
	
	/**
	 * 返回建立的错误报告
	 */
	public String toString()
	{
		return this.buffer.toString();
	}

	/**
	 * 建立并保存报告
	 * @param t
	 * @param f
	 * @throws IOException 
	 */
	private static void buildAndSaveCrushReport(Object ...args)
	{
		DateFormat df = new SimpleDateFormat("y.M.d.H.m.s");
		String name = "MapleFetion-CrushReport-["+df.format(new Date())+"].txt";
		try {
			FileWriter writer = new FileWriter(new File(name));
            writer.append(CrushBuilder.buildCrushReport(args));
            writer.close();
        } catch (IOException e) {
        	logger.warn("Save crush report failed.");
        }
	}
	
	/**
	 * 把错误报告发送到指定的URL
	 * @param args
	 */
	private static void buildAndSendCrushReport(Object ...args)
	{
		String url    = FetionConfig.getString("crush.send.url");
		String report = buildCrushReport(args);
		if(url!=null) {
			url = url.replace("{report}", report);
			try {
	            URL trueURL = new URL(url);
	            logger.info("Sending crush report...");
	            HttpURLConnection conn = (HttpURLConnection) trueURL.openConnection();
	            if(conn.getResponseCode()==200) {
	            	logger.info("Send crush report OK.");
	            }else {
	            	logger.info("Send crush report Failed. status="+conn.getResponseCode()+" "+conn.getResponseMessage());
	            }
            } catch (MalformedURLException e) {
            	logger.info("send crush report Failed",e);
            } catch (IOException e) {
            	logger.info("send crush report Failed",e);
            }
		}
	}
	
	/**
	 * 建立错误报告
	 * @param args 变长参数，类型可以是Throwable和SipcMessage
	 * @return
	 */
	private static String buildCrushReport(Object ...args)
	{
		CrushBuilder cb = new CrushBuilder();
		cb.buildHeader();
		cb.dumpVersion();
		for(Object o:args) {
			if(o instanceof SipcMessage) {
				cb.dumpSipcMessage((SipcMessage) o);
			}else if(o instanceof Throwable) {
				cb.dumpException((Throwable) o);
			}else {
				logger.warn("Incorrect crush object.."+o.getClass().getSimpleName());
			}
		}
		return cb.toString();
	}
	
	/**
	 * 处理错误报告
	 * @param args
	 */
	public static void handleCrushReport(Object ...args)
	{
		if(FetionConfig.getBoolean("crush.send.enable")) {
			buildAndSendCrushReport(args);
		}else if(FetionConfig.getBoolean("crush.build.enable")) {
			buildAndSaveCrushReport(args);
		}else {
			//do nothing...
		}
	}

}
