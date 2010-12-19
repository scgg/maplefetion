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
 * Package  : net.solosky.maplefetion.net.mina
 * File     : MinaTransferFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import java.net.InetSocketAddress;

import net.solosky.maplefetion.net.ITransfer;
import net.solosky.maplefetion.net.ITransferFactory;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 *
 *	Mina异步库连接工厂
 *
 * @author solosky <solosky772@qq.com>
 */
public class MinaTransferFactory implements ITransferFactory
{

	/**
	 * 连接器
	 * Mina框架提供，负责建立连接和回调数据处理
	 */
	private NioSocketConnector connector;
	
	/**
	 * 事件回调函数
	 */
	private IoHandler  ioHandler;
	
	
	/**
	 * 默认的构造函数
	 */
	public MinaTransferFactory()
	{
		connector = new NioSocketConnector();
		ioHandler = new SIPIoHandler();
		connector.getFilterChain()
		.addFirst("SIPCodec",
				new ProtocolCodecFilter(
						new SIPProtocolCodecFactory()));
		connector.setHandler(ioHandler);
	}
	
	/**
	 * 以连接对象和消息处理器构造
	 * @param connector
	 * @param handler
	 */
	public MinaTransferFactory(NioSocketConnector connector, IoHandler handler)
	{
		this.connector = connector;
		this.ioHandler = handler;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransferFactory#createTransfer(java.lang.String, int)
     */
    @Override
    public ITransfer createTransfer(String host, int port) throws Exception
    {
    	 ConnectFuture cf = connector.connect(new InetSocketAddress(host, port));
    	 cf.await();
    	 return new MinaTransfer(cf.getSession());
    }
    
    /**
     * 返回异步连接对象
     * @return
     */
    public NioSocketConnector getNioSocketConnector()
    {
    	return this.connector;
    }
    
    /**
     * 返回事件处理接口
     * @return
     */
    public IoHandler getIoHandler()
    {
    	return this.ioHandler;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransferFactory#closeFactory()
     */
    @Override
    public void closeFactory()
    {
	    this.connector.dispose();
    }

}
