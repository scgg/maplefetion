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
 * Package  : net.solosky.maplefetion.client.response
 * File     : AbstractResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.client.ResponseHandler;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;

import org.apache.log4j.Logger;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public abstract class AbstractResponseHandler implements ResponseHandler
{
	//客户端
	protected FetionContext context;
	//对话框
	protected Dialog dialog;
	//监听器
	protected ActionListener listener;
	//logger
	protected static Logger logger = Logger.getLogger(ResponseHandler.class);
	
	/**
	 * 构造函数
	 * @param client
	 * @param dialog
	 * @param listener
	 */
	public AbstractResponseHandler(FetionContext context, Dialog dialog, ActionListener listener)
	{
		this.context = context;
		this.dialog = dialog;
		this.listener = listener;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.ResponseHandler#handle(net.solosky.maplefetion.sipc.SipcResponse)
     */
    @Override
    public void handle(SipcResponse response) throws FetionException
    {
    	this.doHandle(response);
    	this.callback(response);
    }

    protected abstract void doHandle(SipcResponse response) throws FetionException;
    
    public void timeout(SipcRequest request)
    {
    	if(this.listener!=null)
    		this.listener.actionFinished(ActionStatus.TIME_OUT);
    }
    
    public void ioerror(SipcRequest request)
    {
    	if(this.listener!=null)
    		this.listener.actionFinished(ActionStatus.IO_ERROR);
    }
    
    protected void callback(SipcResponse response)
    {
    	if(this.listener!=null)
    		this.listener.actionFinished(response.getStatusCode());
    }
    
}
