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
 * File     : AddBuddyResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-11
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import org.jdom.Element;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 * 添加好友回复处理器
 *
 * @author solosky <solosky772@qq.com>
 */
public class AddBuddyResponseHandler extends AbstractResponseHandler
{

	/**
     * @param client
     * @param dialog
     * @param listener
     */
    public AddBuddyResponseHandler(FetionContext client, Dialog dialog, ActionListener listener)
    {
	    super(client, dialog, listener);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.response.AbstractResponseHandler#doHandle(net.solosky.maplefetion.sipc.SipcResponse)
     */
    @Override
    protected void doHandle(SipcResponse response) throws FetionException
    {
    	if(response.getStatusCode()==SipcStatus.NO_SUBSCRIPTION) {	//如果返回的是522，表明用户没开通飞信，那就添加手机好友
			//client.getDialogFactory().getServerDialog().addMobileBuddy(uri, cordId, desc);
			return;
		}else if(response.getStatusCode()==SipcStatus.ACTION_OK){		
    		//用户已经开通飞信,返回了用户的真实的uri,建立一个好友对象，并加入到好友列表中
    		FetionBuddy buddy = new FetionBuddy();
    		Element root = XMLHelper.build(response.getBody().toSendString());
    		Element element = XMLHelper.find(root, "/results/contacts/buddies/buddy");
    		BeanHelper.toBean(FetionBuddy.class, buddy, element);
    		buddy.getRelation().setValue(Integer.parseInt(element.getAttributeValue("relation-status")));
    		
    		this.context.getFetionStore().addBuddy(buddy);
		}else{
			logger.warn("Error ocurred when adding Buddy:[response:"+response);
		}
    }
    
    

}
