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
 * File     : IMessageCallback.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-1
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

/**
 *
 *	消息完成后的回调函数
 *	用于异步发送消息
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface IMessageCallback
{
	/**
	 * 消息已经发送
	 * @param uri			发送的飞信地址
	 * @param content		消息内容
	 * @param isSuccess		是否发送成功
	 */
	public void messageSended(String uri, String content, boolean isSuccess);
}
