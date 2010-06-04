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
import net.solosky.maplefetion.client.SystemException;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.event.action.FailureEvent;
import net.solosky.maplefetion.event.action.FailureType;
import net.solosky.maplefetion.event.action.SuccessEvent;
import net.solosky.maplefetion.event.action.SystemErrorEvent;
import net.solosky.maplefetion.event.action.TimeoutEvent;
import net.solosky.maplefetion.event.action.TransferErrorEvent;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;

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
	protected ActionEventListener listener;
	//logger
	protected static Logger logger = Logger.getLogger(ResponseHandler.class);
	
	/**
	 * 构造函数
	 * @param client
	 * @param dialog
	 * @param listener
	 */
	public AbstractResponseHandler(FetionContext context, Dialog dialog, ActionEventListener listener)
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
    	try {
    		//交给子类处理这个回复，并捕获所有异常
    		this.fireEvent(this.doHandle(response));
		} catch (FetionException e) {
			this.fireEvent(new SystemErrorEvent(e));
			throw e;							//重新抛出已知异常
		} catch(Throwable t){
			this.fireEvent(new SystemErrorEvent(t));
			throw new SystemException(t);		//包装下未知异常为系统异常，重新抛出
		}
    }

    
    /**
     * 超时错误
     */
    public void timeout(SipcRequest request)
    {
		this.fireEvent(new TimeoutEvent());
    }
    
    
    /**
     * 发生了网络错误
     */
    public void ioerror(SipcRequest request)
    {
    	this.fireEvent(new TransferErrorEvent());
    }
    
    
    /**
     * 触发操作事件
     */
    private void fireEvent(ActionEvent event)
    {
    	if(this.listener!=null)
    		this.listener.fireEevent(event);
    }
    /**
     * 处理这个回复，根据不同的状态回调不同的方法，子类可以重载这个
     * @param response	回复对象
     * @return 处理后会产生一个结果事件
     * @throws FetionException
     */
    protected ActionEvent doHandle(SipcResponse response) throws FetionException
    {
    	switch(response.getStatusCode()){
    		case SipcStatus.TRYING:			 return this.doTrying(response);
    		case SipcStatus.ACTION_OK:       return this.doActionOK(response);
    		case SipcStatus.SEND_SMS_OK:     return this.doSendSMSOK(response);
    		case SipcStatus.NOT_AUTHORIZED:  return this.doNotAuthorized(response);
    		case SipcStatus.NOT_FOUND:       return this.doNotFound(response);
    		case SipcStatus.TA_EXIST:        return this.doTaExsit(response);
    		case SipcStatus.NO_SUBSCRIPTION: return this.doNoSubscription(response);
    		default:	
				logger.warn("Unhandled sipc response status, default make action fail. status="
						+response.getStatusCode()+", response="+response);
				return new FailureEvent(FailureType.SIPC_FAIL);
    	}
    }
    
    //100
    protected ActionEvent doTrying(SipcResponse response) throws FetionException {
    	return new SuccessEvent();
    }
    
    //200
    protected ActionEvent doActionOK(SipcResponse response) throws FetionException{
    	return new SuccessEvent();
    }
    
    //208
    protected ActionEvent doSendSMSOK(SipcResponse response) throws FetionException{
    	return new SuccessEvent();
    }
    
    //401
    protected ActionEvent doNotAuthorized(SipcResponse response) throws FetionException{
    	return new FailureEvent(FailureType.SIPC_FAIL);
    }
    //404
    protected ActionEvent doNotFound(SipcResponse response) throws FetionException{
    	return new FailureEvent(FailureType.SIPC_FAIL);
    }
    
    //521
    protected ActionEvent doTaExsit(SipcResponse response) throws FetionException{
    	return new FailureEvent(FailureType.SIPC_FAIL);
    }
    
    //522
    protected ActionEvent doNoSubscription(SipcResponse response) throws FetionException{
    	return new FailureEvent(FailureType.SIPC_FAIL);
    }
}
