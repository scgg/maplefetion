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
 * File     : DeclineApplicationResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-7-19
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import org.jdom.Element;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.bean.StoreVersion;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.XMLHelper;

/**
 * 
 * 拒绝添加好友请求的回复处理，完成从好友列表中删除好友的任务
 * @author solosky < solosky772@qq.com >
 *
 */
public class DeclineApplicationResponseHandler extends AbstractResponseHandler{

	public DeclineApplicationResponseHandler(FetionContext context,
			Dialog dialog, ActionEventListener listener) {
		super(context, dialog, listener);
	}

	@Override
	protected ActionEvent doActionOK(SipcResponse response)
			throws FetionException {
		Element root = XMLHelper.build(response.getBody().toSendString());
		Element element = XMLHelper.find(root, "/results/contacts/buddies/buddy");
		FetionStore store = this.context.getFetionStore();
		if(element!=null && element.getAttributeValue("uri")!=null) {
			Buddy buddy = store.getBuddyByUri(element.getAttributeValue("uri"));
			if(buddy!=null)
				store.deleteBuddy(buddy);
		}
		
		Element contacts = XMLHelper.find(root, "/results/contacts");
		if(contacts!=null && contacts.getAttributeValue("version")!=null){
			StoreVersion storeVersion = store.getStoreVersion();
			storeVersion.setContactVersion(Integer.parseInt(contacts.getAttributeValue("version")));
		}
		
		return super.doActionOK(response);
	}

	
	
}
