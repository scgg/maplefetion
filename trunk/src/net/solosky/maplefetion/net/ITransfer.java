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
 * Package  : net.solosky.maplefetion.net
 * File     : SIPMessageTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import java.io.IOException;

import net.solosky.maplefetion.sip.SIPOutMessage;

/**
 *
 *  SIP信令传输对象
 *  
 *  这个对象完成SIP信令的接受和发送
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface ITransfer
{
	/**
	 * 发送SIP信令
	 * @param outMessage		需发送的信令
	 * @throws IOException		如发送失败抛出IOException
	 */
	public void sendSIPMessage(SIPOutMessage outMessage) throws IOException;
	
	/**
	 * 设置SIP信令监听器
	 * @param listener				SIP信令监听器
	 */
	public void setSIPMessageListener(ISIPMessageListener listener);
	
	/**
	 * 返回SIP信令监听器
	 * @return						SIP信令监听器
	 */
	public ISIPMessageListener getSIPMessageListener();
	
	/**
	 * 启动传输
	 */
	public void startTransfer() throws Exception;
	
	/**
	 * 停止传输
	 * @throws Exception 
	 */
	public void stopTransfer() throws Exception;
	
	/**
	 * 返回这个传输对象的名字
	 * @return
	 */
	public String getName();
}
