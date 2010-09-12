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
 * File     : ISIPMessageTransferFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

/**
 *
 *  传输连接工厂接口
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface ITransferFactory
{
	/**
	 * 建立传输对象
	 * @param host		主机地址
	 * @param port		主机端口
	 * @return
	 * @throws Exception
	 */
	public ITransfer createTransfer(String host, int port) throws Exception;
	
	/**
	 * 关闭这个连接工厂
	 */
	public void closeFactory();
}
