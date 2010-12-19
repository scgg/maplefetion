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
 * File     : GetContactDetailResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-3-14
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.BuddyExtend;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class GetContactDetailResponseHandler extends AbstractResponseHandler
{

	/**
     * @param context
     * @param dialog
     * @param listener
     */
    public GetContactDetailResponseHandler(FetionContext context,
            Dialog dialog, ActionEventListener listener)
    {
	    super(context, dialog, listener);
	    // TODO Auto-generated constructor stub
    }

	/* (non-Javadoc)
	 * @see net.solosky.maplefetion.client.response.AbstractResponseHandler#doActionOK(net.solosky.maplefetion.sipc.SipcResponse)
	 */
	@Override
	protected ActionEvent doActionOK(SipcResponse response)
			throws FetionException
	{
		Element root = XMLHelper.build(response.getBody().toSendString());
		Element contact = XMLHelper.find(root, "/results/contacts/contact");
		Element personal = XMLHelper.find(root, "/results/contacts/contact/personal");
		FetionBuddy buddy = (FetionBuddy) this.context.getFetionStore().getBuddyByUri(contact.getAttributeValue("uri"));
		if(personal!=null && buddy!=null ) {
			BeanHelper.toBean(FetionBuddy.class, buddy, personal);
			BuddyExtend extend = buddy.getExtend();
			if(extend==null) {
				extend = new BuddyExtend();
				buddy.setExtend(extend);
			}
			BeanHelper.toBean(BuddyExtend.class, extend, personal);
			context.getFetionStore().flushBuddy(buddy);
		}
		return super.doActionOK(response);
	}

    
}
