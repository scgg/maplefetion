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
	
	/**
	 * 当前解码的消息
	 */
	private SIPInMessage curMessage;
		
	/**
	 * 消息体还需要读取的长度
	 */
	private int contentLeft;
	
	/**
	 * 读取一行时上一个字符
	 */
	private int lineLast;
	
	/**
	 * 上一次操作
	 */
	private int lastAction;
	
	/**
	 * 有效字符掩码
	 */
	private static final int BYTE_MASK = 0x7FFFFFFF;
	
	/**
	 * 操作常量
	 */
	private static final int ACTION_NONE 		= 0x00;
	private static final int ACTION_READ_RETURN 	= 0x01;
	private static final int ACTION_READ_HEADER 	= 0x02;
	private static final int ACTION_READ_CONTENT = 0x03;
	
	
	
	/**
	 * 默认构造函数
	 */
	public SIPProtocolDecoder()
	{
		this.headBuffer = new ByteArrayBuffer(20480);
		this.contentLeft = 0;
		this.lastAction = ACTION_NONE;
		this.lineLast = BYTE_MASK;
	}
	
	/**
	 * 从buffer里解码ＳＩＰ信令对象
	 */
    @Override
    public void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output)
            throws Exception
    {
    	while(buffer.hasRemaining()) {
    		switch(lastAction)
    		{
    		case ACTION_READ_RETURN:
    		case ACTION_NONE:
    			String headline = this.readLine(buffer);
        	 	if(headline==null) {
        	 		lastAction = ACTION_READ_RETURN;
        	 		break;
        	 	}
        	 	if(this.readSIPMessage(buffer, headline)) {
        	 		this.flushSIPMessage(output);
        	 	}
        	 	break;
        	 	
    		case ACTION_READ_HEADER:
    			if(this.readSIPHeaders(buffer)&&this.readSIPBody(buffer)) {
    				this.flushSIPMessage(output);
    			}
    			break;
    		
    		case ACTION_READ_CONTENT:
    			if(this.readSIPBody(buffer)) {
    				this.flushSIPMessage(output);
    			}
    			break;
    		default:
    			break;
    		}
    	}
    }

	/**
	 * 这个函数是在每次解码结束后清理资源，这里不做任何处理
	 */
    @Override
    public void dispose(IoSession session) throws Exception
    {
    	//Do nothing.... 
    }

	/**
	 * 当这个IoSession关闭时候调用
	 */
    @Override
    public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
            throws Exception
    {
	    // Do nothing.... 
    }
    
    /**
     * 把当前对象刷新至下一个处理器
     */
    private void flushSIPMessage(ProtocolDecoderOutput output)
    {
    	output.write(this.curMessage);
	 	this.curMessage = null;
	 	this.lastAction = ACTION_NONE;
	 	this.lineLast = BYTE_MASK;
    }
    
    /**
     * 读取SIP信令
     * @param buffer 	缓存对象
     * @param head		首行信息
     */
    private boolean readSIPMessage(IoBuffer buffer,String headline)
    {
    	if(headline.startsWith(SIPMessage.SIP_VERSION)) {
    		this.curMessage = new SIPResponse(headline);	//SIP-C/2.0 200 OK
	 	}else {
	 		this.curMessage = new SIPNotify(headline);		//BN 685592830 SIP-C/2.0
	 	}

		if(this.readSIPHeaders(buffer)) {
			return this.readSIPBody(buffer);
		}else {
			return false;
		}
    }
    
    
    /**
     * 读取一行字符
     * @param buffer	缓存对象
     * @return			字符串不包含\r\n
     */
    public String readLine(IoBuffer buffer)
    {
    	int cur  = BYTE_MASK;
    	int last = BYTE_MASK;
    	boolean end = false;
    	if(lineLast==BYTE_MASK) {
    		headBuffer.clear();
        }else {
        	last = lineLast;
        }
    	
    	while(buffer.hasRemaining()) {
    		cur = buffer.get();
    		//0x0D 0x0A为行结束符
    		if(last==0x0D && cur==0x0A) {
    			end = true;
    			break;
    		}else if(last==BYTE_MASK) {
    			last = cur;
    		}else {
    			headBuffer.append(last);
    			last = cur;
    		}
    	} 
    	if(end) {
    		lineLast = BYTE_MASK;
    		return ConvertHelper.byte2String(headBuffer.toByteArray());
    	}else {
    		lineLast = last;
    		return null;
    	}
    	
    }
    
    /**
     * 读取所有消息头
     * @param buffer		缓存对象
     * @return				如果读取完毕返回true否则返回false
     */
    private boolean readSIPHeaders(IoBuffer buffer)
    {
    	while(true) {
        	String headline = this.readLine(buffer);
        	if(headline==null) {
        		lastAction = ACTION_READ_HEADER;
        		return false;
        	}else if(headline.length()==0) {
        		lastAction = ACTION_NONE;
        		return true;
        	}else {
        		curMessage.addHeader(new SIPHeader(headline));
        	}
    	}
    }
    
    
    /**
     * 读取消息体
     * @param buffer		缓存对象
     * @return		如果读取完毕返回true否则返回false
     */
    private boolean readSIPBody(IoBuffer buffer)
    {
    	if(this.lastAction==ACTION_NONE) {
    		if(this.curMessage.getLength()>0) {
    			this.contentLeft = this.curMessage.getLength();
    			this.headBuffer.clear();
    		}else {
    			lastAction = ACTION_NONE;
    			return true;
    		}
    	}
    	
    	for(; buffer.hasRemaining() && this.contentLeft>0; this.contentLeft--)
    		this.headBuffer.append(buffer.get());
    	if(this.contentLeft==0) {
    		this.curMessage.setBody(new SIPBody( ConvertHelper.byte2String(this.headBuffer.toByteArray())));
    		this.lastAction = ACTION_NONE;
    		return true;
    	}else {
    		this.lastAction = ACTION_READ_CONTENT;
    		return false;
    	}
    }
    
}
