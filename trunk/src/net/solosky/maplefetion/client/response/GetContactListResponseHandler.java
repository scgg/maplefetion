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
 * File     : GetContactListResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.response;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.MobileBuddy;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.BeanHelper;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class GetContactListResponseHandler extends AbstractResponseHandler
{

	/**
     * @param client
     * @param dialog
     * @param listener
     */
    public GetContactListResponseHandler(FetionContext client, Dialog dialog,
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
    	FetionStore store = context.getFetionStore();
    	Element result = XMLHelper.build(response.getBody().toSendString());
    	Element contacts = result.getChild("contacts");
    	
    	//分组列表
    	List list = contacts.getChild("buddy-lists").getChildren();
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		store.addCord(new Cord(Integer.parseInt(e.getAttributeValue("id")), e.getAttributeValue("name")));
    	}
    	
    	//飞信好友列表
    	list = contacts.getChild("buddies").getChildren();
    	it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		Buddy b = new FetionBuddy();
    		BeanHelper.toBean(FetionBuddy.class, b, e);
    		b.getRelation().setValue(Integer.parseInt(e.getAttributeValue("relation-status")));
    		store.addBuddy(b);
    	}
    	
    	// 飞信手机好友列表
    	list = contacts.getChild("mobile-buddies").getChildren();
    	it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		Buddy b = new MobileBuddy();
    		BeanHelper.toBean(MobileBuddy.class, b, e);
    		b.getRelation().setValue(Integer.parseInt(e.getAttributeValue("relation-status")));
    		store.addBuddy(b);
    	}
    	
    	//TODO 处理allowList...
    }
    
    

}
