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
 * File     : FindBuddyByMobileResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-4-15
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import org.jdom.Element;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.client.dialog.SessionKey;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class FindBuddyByMobileResponseHandler extends AbstractResponseHandler
{

	private int statusCode;
	/**
     * @param context
     * @param dialog
     * @param listener
     */
    public FindBuddyByMobileResponseHandler(FetionContext context,
            Dialog dialog, ActionListener listener)
    {
	    super(context, dialog, listener);
	    this.statusCode = SipcStatus.ACTION_OK;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.response.AbstractResponseHandler#doHandle(net.solosky.maplefetion.sipc.SipcResponse)
     */
    @Override
    protected void doHandle(SipcResponse response) throws FetionException
    {
    	if(response.getStatusCode()==SipcStatus.ACTION_OK) {
    		Element root = XMLHelper.build(response.getBody().toSendString());
			Element contact = XMLHelper.find(root, "/results/contacts/contact");
			int status = Integer.parseInt(contact.getAttributeValue("status-code"));
			if(status==SipcStatus.ACTION_OK) {
    			Element personal = XMLHelper.find(root, "/results/contacts/contact/personal");
    			if(personal!=null) {
    				int userId = Integer.parseInt(personal.getAttributeValue("user-id"));
    				Buddy buddy = this.context.getFetionStore().getBuddyByUserId(userId);
    				if(buddy!=null) {
        				this.dialog.getSession()
        					.setAttribute(SessionKey.FIND_BUDDY_BY_MOBILE_RESULT,
        							this.context.getFetionStore().getBuddyByUserId(userId));
        				this.statusCode = ActionStatus.ACTION_OK;		//找到该用户并且是好友，操作正确完成
    				}else {
    					this.statusCode = ActionStatus.INVALD_BUDDY;	//找到该用户但不是好友
    				}
    			}
			}else if(status==SipcStatus.NOT_FOUND) {
				this.statusCode = ActionStatus.NOT_FOUND;				//该用户找不到
			}else {
				this.statusCode = ActionStatus.OTHER_ERROR;				//其他未知错误
			}
    	}else {
    		this.statusCode = response.getStatusCode();
    	}
    }
    
    //重载callback 调用自定义的回调状态
    protected void callback(SipcResponse response)
    {
    	if(this.listener!=null)
    		this.listener.actionFinished(this.statusCode);
    }

}
