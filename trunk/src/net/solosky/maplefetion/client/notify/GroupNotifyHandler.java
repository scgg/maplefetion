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
 * Package  : net.solosky.maplefetion.client.notify
 * File     : GroupNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-9
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import java.util.Iterator;
import java.util.List;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;


/**
 *
 * 群成员状态改变
 *
 * @author solosky <solosky772@qq.com>
 */ 
public class GroupNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.NotifyHandler#handle(net.solosky.maplefetion.sipc.SipcNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
    	Element root = XMLHelper.build(notify.getBody().toSendString());
    	Element event = XMLHelper.find(root, "/events/event");
    	String type = event.getAttributeValue("type");
    	if(type.equals("PresenceChanged")) {
    		this.presenceChanged(event);
    	}else {
    		logger.warn(" GroupNotifyHandler: Unknown event type - "+type);
    	}
    }
    
    
    /**
     * 处理状态改变
     */
    private void presenceChanged(Element event)
    {
    	List groups = event.getChildren("group");
    	Iterator it = groups.iterator();
    	while(it.hasNext()) {
    		Element el = (Element) it.next();
    		Group group = this.context.getFetionStore().getGroup(el.getAttributeValue("uri"));
    		if(group!=null) {
    			group.setName(el.getAttributeValue("name"));
    			group.setBulletin(el.getAttributeValue("bulletin"));
    			group.setIntro(el.getAttributeValue("introduce"));
    			
    			//成员
    			List members = el.getChildren("member");
    			Iterator mit = members.iterator();
    			while(mit.hasNext()) {
    				Element ell = (Element) mit.next();
    				Member member = this.context.getFetionStore().getGroupMember(group, ell.getAttributeValue("uri"));
    				//member.setIdentity(Integer.parseInt(ell.getAttributeValue("identity")));
    				member.setNickName(ell.getAttributeValue("nickname"));
    				member.getPresence().setValue(Integer.parseInt(ell.getAttributeValue("state")));
    				member.getPresence().setClientType(ell.getAttributeValue("client-type"));
    			}
    		}
    	}
    }

}
