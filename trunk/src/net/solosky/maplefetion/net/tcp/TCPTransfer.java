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
 * Package  : net.solosky.maplefetion.net.tcp
 * File     : TCPSIPMessageTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.solosky.maplefetion.net.ISIPMessageListener;
import net.solosky.maplefetion.net.ITransfer;
import net.solosky.maplefetion.net.QueueManager;
import net.solosky.maplefetion.sip.SIPBody;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPMessage;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.ByteArrayBuffer;
import net.solosky.maplefetion.util.SIPMessageLogger;

import org.apache.log4j.Logger;

/**
 *
 *  TCP方式消息传输
 *
 * @author solosky <solosky772@qq.com> 
 */
public class TCPTransfer implements ITransfer
{

	/**
	 * 内部线程，用于读取数据
	 */
	private Thread readThread;
	
	/**
	 * 队列管理器
	 */
	private QueueManager queueManager;
	
	/**
	 * 监听器
	 */
	private ISIPMessageListener listener;
	
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
	 * 字节缓冲
	 */
	private ByteArrayBuffer buffer;
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(TCPTransfer.class);
	
	/**
	 * 信令记录器
	 */
	private SIPMessageLogger messageLogger;
	
	/**
	 * 关闭标志
	 */
	private volatile boolean closeFlag;
	
	/**
	 * 构造函数
	 * @param host		主机名
	 * @param port		端口
	 * @throws IOException 
	 */
	public TCPTransfer(String host, int port) throws IOException
	{
		socket = new Socket(host, port);
	    reader = socket.getInputStream();
	    writer = socket.getOutputStream();
	    buffer = new ByteArrayBuffer(20480);
	    queueManager = new QueueManager(this); 
	    closeFlag = false;
		messageLogger = new SIPMessageLogger(host+".log");
	}
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.SIPMessageTransfer#sendSIPMessage(net.solosky.maplefetion.sip.SIPOutMessage)
     */
    @Override
    public void sendSIPMessage(SIPOutMessage outMessage) throws IOException
    {
    	writer.write(outMessage.toSendString().getBytes());
    	writer.flush();
    	//如果需要回复才放入发送队列
    	if(outMessage.isNeedAck()) {
    		queueManager.sendedSIPMessage(outMessage);
    	}
    	messageLogger.logSIPMessage(outMessage);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.SIPMessageTransfer#setSIPMessageListener(net.solosky.maplefetion.net.SIPMessageListener)
     */
    @Override
    public void setSIPMessageListener(ISIPMessageListener listener)
    {
    	this.listener = listener;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.SIPMessageTransfer#startTransfer()
     */
    @Override
    public void startTransfer()
    {
    	Runnable readRunner = new Runnable()
	    {
            public void run()
            {
            	try {
            		logger.debug("The read thread of transfer started:"+socket.getInetAddress());
	                loopReadSIPMessage();
                } catch (Throwable e) {
                	if(!closeFlag) {
                    	listener.ExceptionCaught(e);
                	}else {
                		logger.debug("connection closed by user:"+socket.getInetAddress());
                	}
                }
            }
	    };
	    
	    readThread = new Thread(readRunner);
	    readThread.setName("Transfer:"+socket.getInetAddress());
	    
	    readThread.start();
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.SIPMessageTransfer#stopTransfer()
     */
    @Override
    public void stopTransfer()
    {
    	try {
    		//关闭流
    		closeFlag = true;
	        reader.close();
	        writer.close();
	        messageLogger.close();
	        
	        //停止已发送队列超时检查任务
	        queueManager.getTimeOutCheckTask().cancel();
	        
	        //中断线程
	        readThread.interrupt();
        } catch (IOException e) {
	       logger.warn("IOException occured when closing stream..");
        }
    	
    }
    
    /**
     * 读取一行字符
     * @param buffer	缓存对象
     * @return			字符串不包含\r\n
     * @throws IOException 
     */
    public String readLine() throws IOException
    {
    	buffer.clear();
    	int cur  = 0x7FFFFFFF;
    	int last = 0x7FFFFFFF;
    	while(true) {
    		cur = this.reader.read();
    		//0x0D 0x0A为行结束符
    		if(last==0x0D && cur==0x0A) {
    			break;
    		}else if(last==0x7FFFFFFF) {
    			last = cur;
    		}else {
    			buffer.append(last);
    			last = cur;
    		}
    	}  	
    	return new String(buffer.toByteArray());
    }
    
    
    /**
     * 读取SIP信令
     * @throws IOException 
     */
    public void loopReadSIPMessage() throws IOException
    {
    	while(true) {
    		//首先读取第一行
    		String head = this.readLine(); 
 			//读取一个回复
    		if(head.startsWith(SIPMessage.SIP_VERSION)) {
        		//如果是SIP-C/2.0 xxx msg...，表明是一个回复
    			SIPResponse response = this.readResponse(head);
    			SIPRequest  request = (SIPRequest) this.queueManager.findSIPMessage(response);
    			this.messageLogger.logSIPMessage(response);
    			this.listener.SIPResponseRecived(response, request);
    		}else {	//表明是服务器发回的通知
    			SIPNotify notify = this.readNotify(head);
    			this.messageLogger.logSIPMessage(notify);
    			this.listener.SIPNotifyRecived(notify);
    		}
    	}
    }

    /**
     * 读取一个服务器回复
     * @param head		首行信息
     * @return			回复对象
     * @throws IOException
     */
    private SIPResponse readResponse(String head) throws IOException
    {
    	int start = SIPMessage.SIP_VERSION.length();
    	
		int statusCode = Integer.parseInt(head.substring(start+1,start+4));		//响应状态代码
		String statusMessage = head.substring(start+7);							//响应状态说明

		SIPResponse response = new SIPResponse(statusCode, statusMessage);
		
		//读取消息头
		SIPHeader header = null;
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
    private SIPNotify readNotify(String head) throws IOException
    {
    	//BN 685592830 SIP-C/2.0
    	SIPNotify notify = new SIPNotify(head);
    	
    	//读取消息头
		SIPHeader header = null;
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
    private SIPHeader readHeader() throws IOException
    {
			String headline = this.readLine();
			SIPHeader header = null;
			//判断这一行是否为结束行,\r\n会被去掉，所以某一行长度为0时表示头部信息读取完毕了
			if(headline.length()>0) {
				header = new SIPHeader(headline);
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
    private SIPBody readBody(int length) throws IOException
    {
    	SIPBody body = null;
    	if(length>0) {
			buffer.clear(); 
			//一个字符一个字符的读取
			for(int i=0;i<length; i++)
				buffer.append((byte) reader.read());
			//转化为SIPBody对象
			body = new SIPBody(new String(buffer.toByteArray(),"utf8"));
    	}else {
    		body = null;
    	}
    	return body;
    }
    
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#getSIPMessageListener()
     */
    @Override
    public ISIPMessageListener getSIPMessageListener()
    {
    	return this.listener;
    }
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransfer#getQueueManager()
     */
    @Override
    public QueueManager getQueueManager()
    {
	   return this.queueManager;
    }

}
