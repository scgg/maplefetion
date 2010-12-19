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
 * File     : SIPProtocolCodecFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 *	SIP协议工厂函数
 *  建立从字节流解析到SIP信令的解码对象或者从SIP信令到字节流的编码对象
 *
 * @author solosky <solosky772@qq.com>
 */
public class SIPProtocolCodecFactory implements ProtocolCodecFactory 
{
	
	/**
	 * 返回关于这个会话的协议编码器
	 */
    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception
    {
	    ProtocolDecoder decoder = (ProtocolDecoder) session.getAttribute("DECODER");
	    if(decoder==null) {
	    	decoder = new SIPProtocolDecoder();
	    	session.setAttribute("DECODER",decoder);
	    }
	    return decoder;
    }

	/**
	 * 返回关于这个会话的协议编码器
	 */
    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception
    {
    	ProtocolEncoder encoder = (ProtocolEncoder) session.getAttribute("ENCODER");
    	if(encoder==null) {
    		encoder = new SIPProtocolEncoder();
    		session.setAttribute("ENCODER", encoder);
    	}
    	return encoder;
    }
}
