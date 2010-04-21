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
 * Package  : net.solosky.net.maplefetion.net.tcp
 * File     : TcpTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-6
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.solosky.maplefetion.net.AbstractTransfer;
import net.solosky.maplefetion.net.Port;
import net.solosky.maplefetion.net.TransferException;

import org.apache.log4j.Logger;


/**
 * 
 * TCP方式消息传输
 * 
 * @author solosky <solosky772@qq.com>
 */
public class TcpTransfer extends AbstractTransfer
{

	/**
	 * 内部线程，用于读取数据
	 */
	private Thread readThread;

	/**
	 * SOCKET
	 */
	private Socket socket;
	/**
	 * 读取对象
	 */
	private InputStream reader;

	/**
	 * 发送对象
	 */
	private OutputStream writer;

	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(TcpTransfer.class);

	/**
	 * 关闭标志
	 */
	private volatile boolean closeFlag;

	/**
	 * 构造函数
	 * 
	 */
	public TcpTransfer(Port port) throws TransferException
	{
		try {
			logger.debug("Connecting to "+port.toString());
			socket = new Socket();
			socket.connect(new InetSocketAddress(port.getAddress(), port.getPort()));
			logger.debug("Connected to "+port.toString());
			reader = socket.getInputStream();
	        writer = socket.getOutputStream();
        } catch (IOException e) {
        	logger.warn("Cannot connect to "+port.toString());
        	throw new TransferException(e);
        }
		closeFlag = false;
	}

	@Override
	public void startTransfer() throws TransferException
	{
		Runnable readRunner = new Runnable()
		{
			public void run()
			{
				try {
					logger.debug("Socket ready for read: "+ socket);
					logger.debug("Local port:"+socket.getLocalSocketAddress().toString());
					byte[] buff = new byte[1024];
					int len   = 0; 
					while(!closeFlag) {
						len = reader.read(buff, 0, buff.length);
						bytesRecived(buff, 0, len);
					}
				} catch (IOException e) {
					if(!closeFlag) {
						raiseException(new TransferException(e));
					}else {
						logger.debug("Connection closed by user:"+socket);
					}
					
					return;
				}
				
				
				//程序执行到这里，表明流已经读取完毕，可能是服务器主动关闭了连接，
				//在关闭连接之前，服务器会发送一个Registration通知
				//所以在这里，也把流关闭，并设置关闭标志
				try {
	                stopTransfer();
	                logger.debug("Connection closed by server:"+socket);
                } catch (TransferException e) {
                	logger.warn("Close connection error.", e);
                }
                
                raiseException(new TransferException("Connection Closed by Server."));
			}
		};

		readThread = new Thread(readRunner);
		readThread.setName(getTransferName());

		readThread.start();

	}

	@Override
	public void stopTransfer() throws TransferException
	{
		if(!this.closeFlag) {
    		this.closeFlag = true;
    		try {
    			this.reader.close();
    			this.writer.close();
            } catch (IOException e) {
            	throw new TransferException(e);
            }
		}
	}

	@Override
	public String getTransferName()
	{
		return "TCPTransfer-" + socket.getInetAddress().getHostAddress();
	}
	
	

	/**
     * @return the socket
     */
    public Socket getSocket()
    {
    	return socket;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.AbstractTransfer#sendBytes(byte[], int, int)
     */
    @Override
    protected void sendBytes(byte[] buff, int offset, int len)
            throws TransferException
    {
    	try {
	        this.writer.write(buff, offset, len);
	        this.writer.flush();
        } catch (IOException e) {
	       throw new TransferException(e);
        }
    	
    }
}