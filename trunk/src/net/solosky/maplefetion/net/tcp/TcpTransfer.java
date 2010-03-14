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
import java.net.InetAddress;
import java.net.Socket;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.net.AbstractTransfer;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.buffer.ByteArrayWriter;
import net.solosky.maplefetion.sipc.SipcBody;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcInMessage;
import net.solosky.maplefetion.sipc.SipcMessage;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.util.ConvertHelper;

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
	 * 缓冲区，读取数据就存放到这个缓冲区
	 */
	private ByteArrayWriter buffer;

	/**
	 * 构造函数
	 * 
	 * @param host
	 *            主机名
	 * @param port
	 *            端口
	 * @throws TransferException 
	 */
	public TcpTransfer(InetAddress host, int port) throws TransferException
	{
		try {
			socket = new Socket(host, port);
			reader = socket.getInputStream();
	        writer = socket.getOutputStream();
        } catch (IOException e) {
        	throw new TransferException(e);
        }
		closeFlag = false;
		buffer = new ByteArrayWriter();
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.AbstractTransfer#sendSipcMessage(net.solosky.maplefetion.sipc.SipcOutMessage)
     */
    @Override
    protected void sendSipcMessage(SipcOutMessage out) throws TransferException
    {
    	try {
	        writer.write(ConvertHelper.string2Byte(out.toSendString()));
	        writer.flush();
        } catch (IOException e) {
	        throw new TransferException(e);
        }
    }
    
	/**
	 * 读取SIP信令
	 * 
	 * @throws IOException
	 */
	private void readNextSipcMessage(String head) throws IOException
	{
		//读取回复或者通知
		SipcInMessage in = null;
		if(head.startsWith(SipcMessage.SIP_VERSION)) {
    		//如果是SIP-C/2.0 xxx msg...，表明是一个回复
			in = this.readResponse(head);
		}else {	//表明是服务器发回的通知
			in = this.readNotify(head);
		}
		
		//通知接受到回复了
		try {
			this.sipcMessageRecived(in);
		}catch (Throwable e) {
			this.raiseException(new FetionException(e));
		}
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
					
					String head = null;
					while((head=readLine())!=null) {
						readNextSipcMessage(head);
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

	/**
     * 读取一行字符
     * @param buffer	缓存对象
     * @return			字符串不包含\r\n
     * @throws IOException 
     */
    private String readLine() throws IOException
    {
    	buffer.clear();
    	int cur  = 0x7FFFFFFF;
    	int last = 0x7FFFFFFF;
    	while(true) {
    		cur = this.reader.read();
    		if(cur==-1) {	//读取到流的结尾，可能是服务器关闭了连接
    			return null;
    		}
    		//0x0D 0x0A为行结束符
    		if(last==0x0D && cur==0x0A) {
    			break;
    		}else if(last==0x7FFFFFFF) {
    			last = cur;
    		}else {
    			buffer.writeByte(last);
    			last = cur;
    		}
    	}  	
    	return new String(buffer.toByteArray());
    }
    
    
    /**
     * 读取一个服务器回复
     * @param head		首行信息
     * @return			回复对象
     * @throws IOException
     */
    private SipcResponse readResponse(String head) throws IOException
    {
		SipcResponse response = new SipcResponse(head);
		
		//读取消息头
		SipcHeader header = null;
		while((header=this.readHeader())!=null)
				response.addHeader(header);
		
		//读取消息正文
		response.setBody(this.readBody(response.getLength()));
		
		return response;
    }
    
    /**
     * 读取服务器发送的通知，如BN，M
     * @param head		首行信息
     * @return			通知对象
     * @throws IOException 
     */
    private SipcNotify readNotify(String head) throws IOException
    {
    	//BN 685592830 SIP-C/2.0
    	SipcNotify notify = new SipcNotify(head);
    	
    	//读取消息头
		SipcHeader header = null;
		while((header=this.readHeader())!=null)
				notify.addHeader(header);
		
		//读取消息正文
		notify.setBody(this.readBody(notify.getLength()));
		
		return notify;
    }
    
    /**
     * 读取回复或者通知的头部
     * @return 			读取消息头的数量
     * @throws IOException 
     */
    private SipcHeader readHeader() throws IOException
    {
			String headline = this.readLine();
			SipcHeader header = null;
			//判断这一行是否为结束行,\r\n会被去掉，所以某一行长度为0时表示头部信息读取完毕了
			if(headline.length()>0) {
				header = new SipcHeader(headline);
			}else {
				header = null;
			}
    	return header;
    }
    
    /**
     * 读取消息体
     * @param length	消息体长度
     * @return			消息体对象
     * @throws IOException
     */
    private SipcBody readBody(int length) throws IOException
    {
    	SipcBody body = null;
    	byte tmpByte = 0;
    	if(length>0) {
			buffer.clear(); 
			//一个字符一个字符的读取
			for(int i=0;i<length; i++) {
				tmpByte = (byte) reader.read();
				if(tmpByte==-1) {
					return null;
				}
				buffer.writeByte(tmpByte);
			}
			//转化为SIPBody对象
			body = new SipcBody(new String(buffer.toByteArray(),"utf8"));
    	}else {
    		body = null;
    	}
    	return body;
    }
}