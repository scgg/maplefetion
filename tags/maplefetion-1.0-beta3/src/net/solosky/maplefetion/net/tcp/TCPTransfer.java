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

import net.solosky.maplefetion.net.AbstractTransfer;
import net.solosky.maplefetion.sip.SIPBody;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPMessage;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.ByteArrayBuffer;
import net.solosky.maplefetion.util.ConvertHelper;

import org.apache.log4j.Logger;

/**
 *
 *  TCP方式消息传输
 *
 * @author solosky <solosky772@qq.com> 
 */
public class TCPTransfer extends AbstractTransfer
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
	 * 字节缓冲
	 */
	private ByteArrayBuffer buffer;
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(TCPTransfer.class);
	
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
		super();
		socket = new Socket(host, port);
	    reader = socket.getInputStream();
	    writer = socket.getOutputStream();
	    buffer = new ByteArrayBuffer(20480);
	    closeFlag = false;
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
    			this.responseReceived(response);
    		}else {	//表明是服务器发回的通知
    			SIPNotify notify = this.readNotify(head);
    			this.notifyReceived(notify);
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
    
    @Override
    public void sendSIPMessage(SIPOutMessage outMessage)
            throws IOException
    {
    	writer.write(ConvertHelper.string2Byte(outMessage.toSendString()));
    	writer.flush();
	    
    }
    
    @Override
    public void startTransfer() throws Exception
    {
    	Runnable readRunner = new Runnable()
	    {
            public void run()
            {
            	try {
            		logger.debug("The read thread of transfer started:"+getName());
	                loopReadSIPMessage();
                } catch (Throwable e) {
                	if(!closeFlag) {
                    	exceptionCaught(e);
                	}else {
                		logger.debug("connection closed by user:"+getName());
                	}
                }
            }
	    };
	    
	    readThread = new Thread(readRunner);
	    readThread.setName(getName());
	    
	    readThread.start();
	    
    }
    
    @Override
    public void stopTransfer() throws Exception
    {
    		//关闭流
    		closeFlag = true;
	        reader.close();
	        writer.close();
	        //中断线程
	        readThread.interrupt();
    }

    @Override
    public String getName()
    {
	    return "TCPTransfer-"+socket.getInetAddress().getHostAddress();
    }

}
