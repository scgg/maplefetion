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
 * Package  : net.solosky.maplefetion.sip
 * File     : SIPReceipt.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.sip;

import java.util.Iterator;

/**
 *
 *	SIP收据
 *
 *  这个是对Notify的回复
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SIPReceipt extends SIPOutMessage
{
	/**
	 * 回复状态代码
	 */
	private int statusCode;
	
	/**
	 * 回复状态说明
	 */
	private String statusMessage;
	
	/**
	 * SIP收据
	 * @param statusCode		回复状态代码
	 * @param statusMessage		回复状态说明
	 */
	public SIPReceipt(int statusCode, String statusMessage)
	{
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.setNeedAck(false);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.sip.SIPOutMessage#toSendString()
     */
    @Override
    public String toSendString()
    {
    	StringBuffer buffer = new StringBuffer();
		buffer.append(SIPMessage.SIP_VERSION+" "+Integer.toString(this.statusCode)+' '+this.statusMessage+"\r\n");
		Iterator<SIPHeader> it = this.getHeaders().iterator();
		while(it.hasNext()) {
			buffer.append(it.next().toSendString());
		}
		buffer.append("\r\n");
		if(this.body!=null)
			buffer.append(body.toSendString());
		
		return buffer.toString();
    }

}
