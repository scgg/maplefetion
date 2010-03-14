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
 * Package  : net.solosky.net.maplefetion.sipc
 * File     : SipcInMessage.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-5
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sipc;

/**
*
* 可以接受的消息
* 
* 子类有SipcResponse和SipcNotify
*
* @author solosky <solosky772@qq.com> 
*/
public abstract class SipcInMessage extends SipcMessage
{	
	/**
	 * 返回请求者
	 * @return		请求者
	 */
	public String getFrom()
	{
		return this.getHeader(SipcHeader.FROM).getValue();
	}
	
	/**
	 * 返回全局请求编号
	 * @return		全局请求编号
	 */
	public int getCallID()
	{
		SipcHeader header = this.getHeader(SipcHeader.CALLID);
		return header==null?-1:Integer.parseInt(header.getValue());
	}
	
	/**
	 * 返回请求序号
	 * @return		请求序号
	 */
	public String getSequence() 
	{
		SipcHeader header = this.getHeader(SipcHeader.SEQUENCE);
		return header==null?null:header.getValue();
	}
	
	/**
	 * 返回正文长度
	 * @return		正文长度，如果没有消息头返回-1
	 */
	public int getLength()
	{
		//长度有点奇怪，只在有回复正文时候才会有长度回复头
		SipcHeader header = this.getHeader(SipcHeader.LENGTH);
		return header==null ? -1 : Integer.parseInt(header.getValue());
	}
}