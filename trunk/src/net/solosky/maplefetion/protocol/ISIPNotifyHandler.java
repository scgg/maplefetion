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
 * Package  : net.solosky.maplefetion.protocol
 * File     : NotifyProcessor.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.sip.SIPNotify;

/**
 *  处理服务器发回的通知
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface ISIPNotifyHandler
{
	/**
	 * 设置飞信客户端对象
	 * 利用这个对象可以访问任何一个客户端对象
	 * @param client		飞信客户端
	 */
	public void setFetionClient(IFetionClient client);
	
	/**
     * 设置对话对象
	 * @param dailog			当前对话对象
	 */
	public void setDailog(AbstractDialog dialog);
	/**
	 * 处理通知
	 * @param request		通知请求对象
	 * @throws Exception 
	 */
	public void handle(SIPNotify notify) throws Exception;
}
