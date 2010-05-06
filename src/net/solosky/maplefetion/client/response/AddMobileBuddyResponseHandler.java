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
 * File     : AddMobieBuddyResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-11
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.MobileBuddy;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 * 添加手机好友的回调函数
 *
 * @author solosky <solosky772@qq.com>
 */
public class AddMobileBuddyResponseHandler extends AbstractResponseHandler
{

	/**
     * @param client
     * @param dialog
     * @param listener
     */
    public AddMobileBuddyResponseHandler(FetionContext client, Dialog dialog,
            ActionListener listener)
    {
	    super(client, dialog, listener);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.response.AbstractResponseHandler#doHandle(net.solosky.maplefetion.sipc.SipcResponse)
     */
    @Override
    protected void doHandle(SipcResponse response) throws FetionException
    {
    	if(response.getStatusCode()==SipcStatus.ACTION_OK) {
        	Buddy buddy = new MobileBuddy();
    		Element root = XMLHelper.build(response.getBody().toSendString());
    		Element element = XMLHelper.find(root, "/results/contacts/mobile-buddies/mobile-buddy");
    		
    		BeanHelper.toBean(MobileBuddy.class, buddy, element);
    		buddy.getRelation().setValue(Integer.parseInt(element.getAttributeValue("relation-status")));
    		
    		this.context.getFetionStore().addBuddy(buddy);
    	}
    }

}