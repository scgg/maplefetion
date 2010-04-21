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
 * File     : DeleteCordResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-4-15
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import org.jdom.Element;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class DeleteCordResponseHandler extends AbstractResponseHandler
{

	/**
     * @param context
     * @param dialog
     * @param listener
     */
    public DeleteCordResponseHandler(FetionContext context, Dialog dialog,
            ActionListener listener)
    {
	    super(context, dialog, listener);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.response.AbstractResponseHandler#doHandle(net.solosky.maplefetion.sipc.SipcResponse)
     */
    @Override
    protected void doHandle(SipcResponse response) throws FetionException
    {
    	if(response.getStatusCode()==SipcStatus.ACTION_OK) {
    		Element root = XMLHelper.build(response.getBody().toSendString());
    		Element node = XMLHelper.find(root, "/results/contacts/buddy-lists/buddy-list");
    		if(node!=null) {
    			Cord cord = this.context.getFetionStore().getCord(Integer.parseInt(node.getAttributeValue("id")));
    			if(cord!=null)
    				this.context.getFetionStore().deleteCord(cord);
    		}
    		
    		node = XMLHelper.find(root, "/results/contacts");
    		if(node!=null) {
    			int version = Integer.parseInt(node.getAttributeValue("version"));
    			this.context.getFetionStore().getStoreVersion().setContactVersion(version);
    			this.context.getFetionUser().getStoreVersion().setContactVersion(version);
    		}
    		this.context.getFetionStore().flush();
    	}
    }

}