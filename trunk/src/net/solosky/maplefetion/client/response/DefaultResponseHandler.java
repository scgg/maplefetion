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
 * File     : DefaultResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-15
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.client.ResponseHandler;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;

/**
 * 
 * 默认回复处理
 * 
 * @author solosky <solosky772@qq.com>
 */
public class DefaultResponseHandler implements ResponseHandler
{
	private ActionListener actionListener;

	public DefaultResponseHandler(ActionListener actionListener)
	{
		if(actionListener!=null)
			this.actionListener = actionListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.solosky.maplefetion.client.ResponseHandler#handle(net.solosky.maplefetion
	 * .sipc.SipcResponse)
	 */
	@Override
	public void handle(SipcResponse response) throws FetionException
	{
		if(actionListener!=null)
			this.actionListener.actionFinished(response.getStatusCode());
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.ResponseHandler#timeout(net.solosky.maplefetion.sipc.SipcRequest)
     */
    @Override
    public void timeout(SipcRequest request)
    {
	    if(this.actionListener!=null)
	    	this.actionListener.actionFinished(ActionStatus.TIME_OUT);
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.ResponseHandler#error(net.solosky.maplefetion.sipc.SipcRequest)
     */
    @Override
    public void ioerror(SipcRequest request)
    {
    	  if(this.actionListener!=null)
  	    	this.actionListener.actionFinished(ActionStatus.IO_ERROR);
    }
    
    
    
}
