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
 * Package  : net.solosky.maplefetion.net
 * File     : AbstractTransfer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-21
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net;

import java.io.IOException;

import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPResponse;

/**
 *
 * 抽象的传输对象
 * 实现了传输对象的基本功能，其他传输对象可以继承自抽象传输对象
 *
 * @author solosky <solosky772@qq.com>
 */
public abstract class AbstractTransfer implements ITransfer
{	
	/**
	 * 监听器
	 */
	protected ISIPMessageListener listener;
	
	
	public AbstractTransfer()
	{
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
     * @see net.solosky.maplefetion.net.ITransfer#setSIPMessageListener(net.solosky.maplefetion.net.ISIPMessageListener)
     */
    @Override
    public void setSIPMessageListener(ISIPMessageListener listener)
    {
    	this.listener = listener;
    }
    

    
    /**
     * 收到了回复
     * 这个方法供子类调用
     * @param response
     * @throws IOException
     */
    protected void responseReceived(SIPResponse response) throws IOException
    {
    	this.listener.SIPResponseReceived(response, null);
    }
    
    /**
     * 收到了通知
     * 这个方法供子类调用
     * @param notify
     * @throws IOException
     */
    protected void notifyReceived(SIPNotify notify) throws IOException
    {
    	this.listener.SIPNotifyReceived(notify);
    }
    
    /**
     * 发生了异常
     * 这个方法供子类调用
     * @param exception
     */
    protected void exceptionCaught(Throwable exception)
    {
    	this.listener.ExceptionCaught(exception);
    }
 
	
}
