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
 * Package  : net.solosky.net.maplefetion.sipc
 * File     : SipcResponse.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-5
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sipc;




/**
*
* SIP回复类
*
* @author solosky <solosky772@qq.com> 
*/
public class SipcResponse extends SipcInMessage
{
	/**
	 * 回复状态代码
	 */
	private int status;
	
	/**
	 * 回复状态说明
	 */
	private String statusMessage;
	
	/**
	 * 回复等待对象
	 */
	private SipcRequest request;
	
	
	
	/**
	 * 构造函数
	 * @param statusCode		回复状态代码
	 * @param statusMessage		回复状态说明
	 */
	public SipcResponse(int statusCode, String statusMessage)
	{
		this.status   =  statusCode;
		this.statusMessage = statusMessage;
	}
	
	/**
	 * 以响应头构造通知
	 * @param headline		响应头
	 */
	public SipcResponse(String headline)
	{
		int start = SipcMessage.SIP_VERSION.length();
		this.status    = Integer.parseInt((headline.substring(start+1,start+4)));		//响应状态代码
		this.statusMessage = headline.substring(start+5);								//响应状态说明
	}
	/**
	 * 返回请求状态代码
	 * @return		请求状态代码
	 */
	public int getStatusCode()
	{
		return status;
	}
	
	/**
	 * 返回回复状态说明
	 * @return		回复状态说明
	 */
	public String getStatusMessage()
	{
		return this.statusMessage;
	}
	
	

	/**
     * @return the request
     */
    public SipcRequest getRequest()
    {
    	return request;
    }

	/**
     * @param request the request to set
     */
    public void setRequest(SipcRequest request)
    {
    	this.request = request;
    }

	public String toString()
	{
		return "[SipcResponse: status="+this.getStatusCode()+"; I:"+this.getCallID()+"; Q:"+this.getSequence()+"; L:"+this.getLength()+"]";
	}

	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.sipc.SipcMessage#toHeadLine()
     */
    @Override
    protected String toHeadLine()
    {
	    return SipcMessage.SIP_VERSION+" "+Integer.toString(this.status)+' '+this.statusMessage;
    }
}