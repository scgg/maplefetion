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
 * File     : FetionConfig.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-23
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 *	飞信配置
 *
 * @author solosky <solosky772@qq.com> 
 */
public class FetionConfig
{
	public static String NAV_SYSTEM_CONFIG = "http://nav.fetion.com.cn/nav/getsystemconfig.aspx";
	public static String SSI_APP_SIGN_IN = "https://uid.fetion.com.cn/ssiportal/SSIAppSignIn.aspx";
	public static String SSI_APP_SIGN_OUT = "http://ssi.fetion.com.cn/ssiportal/SSIAppSignOut.aspx";
	public static String SIPC_PROXY = "221.130.46.141:8080";
	public static boolean DEBUG_MODE = false;		//是否开启调试模式
	public static boolean LOG_ENABLE = false;		//是否开启日志记录
	public static String  LOG_DIR = "./";			//日志记录目录
	public static String  LOG_FILE = "maplefetion.log";			//日志记录文件名
	public static boolean SIP_MESSAGE_LOG_ENABLE = false;		//是否开启记录SIP信令
	public static String  SIP_MESSAGE_LOG_DIR = ".";			//记录SIP信令目录
	
	//动态读取当前目录下的配置文件 maplefetion.properties
	static {
		File file = new File("maplefetion.properties");
		if(file.exists()&&file.canRead()) {
			try {
				Properties prop = new Properties();
	            prop.load(new FileReader(file));
	            
	            
	            String value = null; 
	            value = prop.getProperty("DEBUG_MODE");
	            if(value!=null) 	DEBUG_MODE = value.equals("true")?true:false;
	            
	            
	            value = prop.getProperty("LOG_ENABLE");
	            if(value!=null) 	LOG_ENABLE = value.equals("true")?true:false;
	            
	            value = prop.getProperty("LOG_SIP_MESSAGE_ENABLE");
	            if(value!=null) 	SIP_MESSAGE_LOG_ENABLE = value.equals("true")?true:false;
	            
	            value = prop.getProperty("LOG_DIR");
	            if(value!=null)		LOG_DIR = value;
	            
	            value = prop.getProperty("LOG_SIP_MESSAGE_DIR");
	            if(value!=null)		SIP_MESSAGE_LOG_DIR = value;
	            
            } catch (FileNotFoundException e) {
            	//do nothing
            } catch (IOException e) {
            	//do nothing
            }
            
            //动态的配置日志记录器
            Logger root = Logger.getRootLogger();
            Appender consoleAppender = root.getAppender("console");
            Appender fileAppender    = root.getAppender("file");
            
            //设置文件日志记录的路径
            FileAppender fileAppender2 = (FileAppender) fileAppender;
            fileAppender2.setFile(LOG_DIR+LOG_FILE);
            //如果处在调试模式，日志级别为最低，写入控制台
            if(DEBUG_MODE) {
            	root.removeAppender("file");
            	root.setLevel(Level.ALL);
            }else if(!DEBUG_MODE && LOG_ENABLE){
            	root.removeAppender("console");
            	root.setLevel(Level.ALL);
            }else {
            	root.removeAppender("file");
            	root.setLevel(Level.OFF);
            }
		}
	}
}
