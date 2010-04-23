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
import java.util.Iterator;
import java.util.Properties;

import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.FetionConfig;

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
		buffer.append("==========Exception=============\n");
		buffer.append(exception.toString()+"\n");
		StackTraceElement[] ts =  exception.getStackTrace();
		for(int i=0; i<ts.length; i++) {
			buffer.append("\tat "+ts[i]);
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
		buffer.append("===========Version=============\n");
		buffer.append("ClientVersion   : "+FetionClient.CLIENT_VERSION+"\n");
		buffer.append("ProtocolVersion : "+FetionClient.PROTOCOL_VERSION+"\n");
		buffer.append("\n");
	}
	
	/**
	 * 打印配置
	 */
	public void dumpConfig()
	{
		buffer.append("==========Config=============\n");
		
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
	public static void buildAndSaveCrushReport(Throwable t, File f) throws IOException
	{
		CrushBuilder cb = new CrushBuilder();
		cb.buildHeader();
		cb.dumpVersion();
		cb.dumpException(t);
		
		FileWriter writer = new FileWriter(f);
		writer.append(cb.toString());
		writer.close();
	}

}
