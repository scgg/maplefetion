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
 * File     : SIPProtocolDecoder.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import java.io.IOException;

import net.solosky.maplefetion.sip.SIPBody;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPInMessage;
import net.solosky.maplefetion.sip.SIPMessage;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.util.ByteArrayBuffer;
import net.solosky.maplefetion.util.ConvertHelper;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 *
 * 从字节流解码到飞信信令对象
 *
 * @author solosky <solosky772@qq.com>
 */
public class SIPProtocolDecoder implements ProtocolDecoder
{
	/**
	 * 字节缓冲区
	 */
	private ByteArrayBuffer headBuffer;
	
	private SIPInMessage tmpMessage;
		
	private int contentBytesLeft;
	
	/**
	 * 默认构造函数
	 */
	public SIPProtocolDecoder()
	{
		this.headBuffer = new ByteArrayBuffer(20480);
		this.contentBytesLeft = 0;
	}
	
	/**
	 * 从buffer里解码ＳＩＰ信令对象
	 */
    @Override
    public void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output)
            throws Exception
    {
    	while(buffer.hasRemaining()) {
    		if(contentBytesLeft>0) {
    			this.readSIPBody(buffer);
    			if(contentBytesLeft>0) {	//如果仍还有部分正文字节没有读完，等待下一个缓冲对象继续读取
    				return;		
    			}else {						//如果已经读完，返回这个信令对象
    				this.tmpMessage.setBody(new SIPBody( ConvertHelper.byte2String(headBuffer.toByteArray())));
    				output.write(tmpMessage);
    	    	 	tmpMessage = null;
    	    	 	continue;				//继续下一次读取
    			}
    		}else {
        	 	String headline = this.readLine(buffer);
        	 	if(headline.startsWith(SIPMessage.SIP_VERSION)) {
        	 		this.readResponse(buffer, headline);	//SIP-C/2.0 200 OK
        	 	}else {
        	 		
        	 		this.readNotify(buffer, headline);		//BN 685592830 SIP-C/2.0
        	 	}
        	 	if(contentBytesLeft==0) {
            	 	output.write(tmpMessage);
            	 	tmpMessage = null;
        	 	}
    		}
    	}
    }

	/* (non-Javadoc)
     * @see org.apache.mina.filter.codec.ProtocolDecoder#dispose(org.apache.mina.core.session.IoSession)
     */
    @Override
    public void dispose(IoSession session) throws Exception
    {
    	//这个函数是在每次解码结束后清理资源，这里不做任何处理
    }

	/* (non-Javadoc)
     * @see org.apache.mina.filter.codec.ProtocolDecoder#finishDecode(org.apache.mina.core.session.IoSession, org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    @Override
    public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
            throws Exception
    {
	    // TODO 当这个IoSession关闭时候调用
	    
    }
    
    
    /**
     * 读取一行字符
     * @param buffer	缓存对象
     * @return			字符串不包含\r\n
     */
    public String readLine(IoBuffer buffer)
    {
    	headBuffer.clear();
    	int cur  = 0x7FFFFFFF;
    	int last = 0x7FFFFFFF;
    	while(buffer.hasRemaining()) {
    		cur = buffer.get();
    		//0x0D 0x0A为行结束符
    		if(last==0x0D && cur==0x0A) {
    			break;
    		}else if(last==0x7FFFFFFF) {
    			last = cur;
    		}else {
    			headBuffer.append(last);
    			last = cur;
    		}
    	}  	
    	return new String(headBuffer.toByteArray());
    }
    
    
    /**
     * 读取一行消息头
     * @param buffer		缓存对象
     * @return				消息头
     */
    private SIPHeader readSIPHeader(IoBuffer buffer)
    {
    	SIPHeader header = null;
    	String headline = this.readLine(buffer);
    	if(headline.length()>0)
    		header = new SIPHeader(headline);
    	
    	return header;
    }
    
    /**
     * 读取消息体
     * @param buffer		缓存对象
     * @param length		消息正文长度
     * @return				消息正文对象
     */
    private void readSIPBody(IoBuffer buffer)
    {
    	for(; buffer.hasRemaining() && contentBytesLeft>0; contentBytesLeft--)
    		this.headBuffer.append(buffer.get());
    }
    
    
    /**
     * 读取一个服务器回复
     * @param buffer 	缓存对象
     * @param head		首行信息
     */
    private void readResponse(IoBuffer buffer,String headline)
    {
		this.tmpMessage = new SIPResponse(headline);

		//读取消息头
		SIPHeader header = null;
		while((header=this.readSIPHeader(buffer))!=null)
				this.tmpMessage.addHeader(header);
		
		//读取消息正文
		if(this.tmpMessage.getLength()>0) {
			this.contentBytesLeft = this.tmpMessage.getLength();
			this.headBuffer.clear();
			this.readSIPBody(buffer);
			if(this.contentBytesLeft==0) {
				this.tmpMessage.setBody(new SIPBody( ConvertHelper.byte2String(headBuffer.toByteArray())));
			}
		}
    }
    
    /**
     * 读取服务器发送的通知，如BN，M
     * @param head		首行信息
     */
    private void readNotify(IoBuffer buffer, String headline) throws IOException
    {
    	//BN 685592830 SIP-C/2.0
    	this.tmpMessage = new SIPNotify(headline);
    	
    	//读取消息头
		SIPHeader header = null;
		while((header=this.readSIPHeader(buffer))!=null)
			this.tmpMessage.addHeader(header);
		
		//读取消息正文
		if(this.tmpMessage.getLength()>0) {
			this.contentBytesLeft = this.tmpMessage.getLength();
			this.headBuffer.clear();
			this.readSIPBody(buffer);
			if(this.contentBytesLeft==0) {
				this.tmpMessage.setBody(new SIPBody( ConvertHelper.byte2String(headBuffer.toByteArray())));
			}
		}
    }

}
