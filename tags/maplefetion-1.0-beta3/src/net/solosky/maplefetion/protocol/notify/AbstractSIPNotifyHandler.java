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
 * Package  : net.solosky.maplefetion.protocol.notify
 * File     : AbstractSIPNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol.notify;

import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.protocol.AbstractDialog;
import net.solosky.maplefetion.protocol.ISIPNotifyHandler;

import org.apache.log4j.Logger;

/**
 *
 * 所有服务器发回的异步通知处理器的基类
 *
 * @author solosky <solosky772@qq.com> 
 */
public abstract class AbstractSIPNotifyHandler implements ISIPNotifyHandler
{
	/**
	 * 飞信客户端对象
	 */
	protected IFetionClient client; 
	
	/**
	 * 飞信传输服务
	 */
	protected AbstractDialog dialog;
	
	/**
	 * Logger
	 */
	protected Logger logger = Logger.getLogger(AbstractSIPNotifyHandler.class);
	
	/**
	 * 设置飞信客户端对象
	 * 这个函数仅在第一实例化对象时候调用，注入飞信客户端对象
	 * @param client	客户端对象
	 */
	public void setFetionClient(IFetionClient client)
	{
		this.client = client;
	}
	
	/**
     * 设置对话对象
	 * @param dailog			当前对话对象
	 */
	public void setDailog(AbstractDialog dialog)
	{
		this.dialog = dialog;
	}
}
