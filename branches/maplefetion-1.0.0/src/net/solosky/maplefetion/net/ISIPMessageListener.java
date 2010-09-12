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
 * File     : SIPMessageListener.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;

/**
 *
 *	SIP信令监听器
 * 
 *  这个是在SIPMessageTransfer收到SIP信令时回调SIPMessageRecived方法，完成SIP信令的处理
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface ISIPMessageListener
{
	/**
	 * 接受到了SIP回复的回调函数
	 * @param response	SIP回复
	 * @param reqeust   对应的请求
	 */
	public void SIPResponseReceived(SIPResponse response, SIPRequest request);
	
	/**
	 * 接受到了SIP通知的回调函数
	 * @param notify	SIP通知
	 */
	public void SIPNotifyReceived(SIPNotify notify);
	
	/**
	 * 发生了异常回调函数
	 * @param e		异常
	 */
	public void ExceptionCaught(Throwable e);
}
