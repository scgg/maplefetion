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
 * File     : MinaTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.mina;

import java.io.IOException;

import net.solosky.maplefetion.net.AbstractTransfer;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPOutMessage;
import net.solosky.maplefetion.sip.SIPResponse;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

/**
 *
 *	Mina传输对象
 *
 * @author solosky <solosky772@qq.com>
 */
public class MinaTransfer extends AbstractTransfer
{	
	/**
	 * 会话对象，一个对象代表了一个连接
	 */
	private IoSession session;
	
	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(MinaTransfer.class);
	
	
	/**
	 * 构造函数
	 * @param session Connector建立连接的会话对象
	 */
	public MinaTransfer(IoSession session)
	{
		super();
		this.session = session;
		this.session.setAttribute("TRANSFER", this);
	}
	
    @Override
    public void sendSIPMessage(SIPOutMessage outMessage)
            throws IOException
    {
    	 this.session.write(outMessage);
    }

    @Override
    public void startTransfer() throws Exception
    {
    	logger.debug("MinaTransfer started:"+this.session);
    }

    @Override
    public void stopTransfer() throws Exception
    {
    	this.session.close(false);
    	logger.debug("MinaTransfer stoped:"+this.session);
    }

    /**
     * 因为MinaTransfer是由IoHandler调用的，但recieved的两个方法都是保护的，外部能访问，所以这里需要使用两个适配器转发
     * @param notify
     * @throws IOException
     */
    public void minaNotifyReceived(SIPNotify notify) throws IOException
    {
	    super.notifyReceived(notify);
    }

    /**
     * 转发至父类的方法
     * @param response
     * @throws IOException
     */
    public void minaResponseReceived(SIPResponse response) throws IOException
    {
	    super.responseReceived(response);
    }

    @Override
    public String getName()
    {
    	String addr = this.session.getRemoteAddress().toString();
	    return "MinaTransfer-"+addr.substring(1, addr.lastIndexOf(":"));
    }
    
    
    

}
