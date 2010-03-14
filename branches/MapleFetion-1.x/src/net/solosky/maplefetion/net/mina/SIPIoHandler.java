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
 * File     : SIPIoHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import net.solosky.maplefetion.sip.SIPMessage;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 *
 * Mina异步消息处理器 继承自IoHandlerAdapter
 * 完成的消息的回调
 *
 * @author solosky <solosky772@qq.com>
 */
public class SIPIoHandler extends IoHandlerAdapter
{
	private static Logger logger = Logger.getLogger(SIPIoHandler.class);
	
	/**
	 * 传输过程中发生异常
	 */
    @Override
    public void exceptionCaught(IoSession session, Throwable exception)
            throws Exception
    {
    	MinaTransfer transfer = (MinaTransfer) session.getAttribute("TRANSFER"); 
    	if(transfer!=null&&transfer.getSIPMessageListener()!=null) {
    		transfer.getSIPMessageListener().ExceptionCaught(exception);
    	}
    }

    /**
     * 收到SIP消息 
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception
    {
		if(message instanceof SIPMessage) {
			
			//查找和这个Session相关的传输对应的Transfer对象
			MinaTransfer transfer = (MinaTransfer) session.getAttribute("TRANSFER"); 
			SIPMessage tmpMessage = (SIPMessage) message;
	    	
			if( transfer!=null) {
	    		if(tmpMessage instanceof SIPNotify) {				//收到了异步通知
	    			transfer.minaNotifyReceived((SIPNotify) tmpMessage);
	    		}else if(tmpMessage instanceof SIPResponse) {		//收到了回复
	    			//通知监听器，收到了消息
	    			transfer.minaResponseReceived((SIPResponse) tmpMessage);
	    		}else {												//其他未知回复。。不会发生的情况
	    			logger.warn("Unknown SIPMessage:"+message);
	    		}
	    	}else {
	    		throw new IllegalStateException("Cannot find Transfer in IoSession:"+session);
	    	}
		}else {
			logger.warn("Cannot decode bytes to SIPMessage:"+message);
		}
    }

	/**
	 * 发送了一个SIP消息
	 */
    @Override
    public void messageSent(IoSession session, Object message) throws Exception
    {
    	//Do nothing...
    }

    
    
}
