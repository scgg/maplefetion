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
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.net.Port;
import net.solosky.maplefetion.net.Transfer;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferFactory;

import org.apache.log4j.Logger;

/**
 *
 *	TCP连接工厂
 *
 * @author solosky <solosky772@qq.com>
 */
public class TcpTransferFactory implements TransferFactory
{

	private Port localPort;
	private static Logger logger = Logger.getLogger(TcpTransferFactory.class);
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
    	//尝试建立Sipc-proxy连接
    	TcpTransfer transfer = this.tryCreateTransfer(FetionConfig.getString("server.sipc-proxy"));
    	//尝试建立sipc-proxy-ssl连接
    	if(transfer==null) 
    		transfer = this.tryCreateTransfer(FetionConfig.getString("server.sipc-ssl-proxy"));
        
    	if(transfer==null) {
    		//仍然建立失败，抛出异常
    		throw new TransferException("Cannot create Default transfer..");
    	}else {
    		//建立成功
    		this.localPort = new Port(transfer.getSocket().getLocalAddress(), transfer.getSocket().getLocalPort());
    		return transfer;
    	}
    }

    
    /**
     * 尝试建立传输对象，建立失败不抛出异常
     * @param port
     * @return
     */
    private TcpTransfer tryCreateTransfer(String portstr)
    {
    	try {
	        return (TcpTransfer) this.createTransfer(new Port(portstr));
        } catch (TransferException e) {
        	logger.warn("Connect to "+portstr+" failed..", e);
        } catch (UnknownHostException e) {
        	logger.warn("Connect to "+portstr+" failed..", e);
        }
        return null;
    }
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#createTransfer(java.lang.String, int)
     */
    @Override
    public Transfer createTransfer(Port port) throws TransferException
    {
	   return new TcpTransfer(port);
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

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.TransferFactory#setFetionContext(net.solosky.maplefetion.FetionContext)
     */
    @Override
    public void setFetionContext(FetionContext context)
    {
    }

}
