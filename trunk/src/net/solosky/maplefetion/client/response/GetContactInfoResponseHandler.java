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
 * Package  : net.solosky.maplefetion.protocol.response
 * File     : GetContectInfoResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-30
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.client.ResponseHandler;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 * 
 * 获取一位好友详细信息的回调函数
 * 
 * @author solosky <solosky772@qq.com>
 */
public class GetContactInfoResponseHandler implements ResponseHandler
{
	/**
	 * 好友对象
	 */
	private FetionBuddy buddy;

	/**
	 * 默认构造函数
	 * 
	 * @param buddy
	 */
	public GetContactInfoResponseHandler(FetionBuddy buddy)
	{
		this.buddy = buddy;
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
		Element root = XMLHelper.build(response.getBody().toSendString());
		Element contact = XMLHelper.find(root, "/results/contacts/contact");
		if (contact != null
		        && contact.getAttributeValue("status-code").equals("200")) {
			String uri = contact.getAttributeValue("uri");
			if (!uri.equals(buddy.getUri()))
				return; // 判断获取结果的uri和需要更新的好友的uri是否相同，如果不同直接返回
			Element personal = contact.getChild("personal");
			if (personal != null) {
				BeanHelper.toBean(FetionBuddy.class, this.buddy, personal);
			}
		}
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.ResponseHandler#timeout(net.solosky.maplefetion.sipc.SipcRequest)
     */
    @Override
    public void timeout(SipcRequest request)
    {
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.ResponseHandler#error(net.solosky.maplefetion.sipc.SipcRequest)
     */
    @Override
    public void ioerror(SipcRequest request)
    {
	    
    }
}
