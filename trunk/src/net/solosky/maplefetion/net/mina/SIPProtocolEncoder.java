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
 * File     : SIPProtocolEncoder.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import net.solosky.maplefetion.sip.SIPMessage;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.util.ConvertHelper;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 *
 *	从飞信信令对象编码到字节流
 *
 * @author solosky <solosky772@qq.com>
 */
public class SIPProtocolEncoder implements ProtocolEncoder
{
	
	/**
	 * 解码结束后清理资源
	 */
    @Override
    public void dispose(IoSession arg0) throws Exception
    {
    	//这个函数是在每次解码结束后清理资源，这里不做任何处理
	    
    }

	/**
	 * 把SIPMessage编码为字节数组，便于发送
	 */
    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput output)
            throws Exception
    {
    	if(message instanceof SIPMessage) {
    		SIPMessage m = (SIPMessage) message;
    		if(m instanceof SIPOutMessage) {
    			SIPOutMessage out = (SIPOutMessage) m;
    			output.write(IoBuffer.wrap(ConvertHelper.string2Byte(out.toSendString())));
    			output.flush();
    		}
    	}
    }
	
}
