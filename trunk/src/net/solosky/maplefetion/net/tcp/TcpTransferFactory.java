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
 * Package  : net.solosky.maplefetion.net.tcp
 * File     : TcpTransferFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.tcp;

import java.net.UnknownHostException;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.net.Port;
import net.solosky.maplefetion.net.Transfer;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferFactory;

/**
 *
 *	TCP连接工厂
 *
 * @author solosky <solosky772@qq.com>
 */
public class TcpTransferFactory implements TransferFactory
{

	private Port localPort;
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#closeFactory()
     */
    @Override
    public void closeFactory()
    {
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#createDefaultTransfer()
     */
    @Override
    public Transfer createDefaultTransfer() throws TransferException
    {
    	String proxy = FetionConfig.getString("server.sipc-proxy");
	    try {
	    	TcpTransfer transfer = (TcpTransfer) this.createTransfer(new Port(proxy));
	    	this.localPort = new Port(transfer.getSocket().getLocalAddress(), transfer.getSocket().getLocalPort());
	        return transfer;
        } catch (UnknownHostException e) {
	        throw new TransferException("Unkown host - proxy="+proxy);
        }
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#createTransfer(java.lang.String, int)
     */
    @Override
    public Transfer createTransfer(Port port) throws TransferException
    {
	   return new TcpTransfer(port.getAddress(), port.getPort());
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#isMutiConnectionSupported()
     */
    @Override
    public boolean isMutiConnectionSupported()
    {
	    return true;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#openFactory()
     */
    @Override
    public void openFactory()
    {
	    // TODO Auto-generated method stub
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#getDefaultTransferLocalPort()
     */
    @Override
    public Port getDefaultTransferLocalPort()
    {
	    return this.localPort;
    }

}
