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
 * Package  : net.solosky.maplefetion.protocol.notify
 * File     : PresenceChangedSIPNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol.notify;

import org.jdom.Element;

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 * 好友状态改变
 *
 * @author solosky <solosky772@qq.com> 
 */
public class PresenceSIPNotifyHandler extends AbstractSIPNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SIPNotify notify) throws Exception
    {
	    Element root = XMLHelper.build(notify.getBody().toSendString());
	    Element presence = XMLHelper.find(root, "/events/event/presence");
	    Element basic = XMLHelper.find(root ,"/events/event/presence/basic");
	    Element personal = XMLHelper.find(root, "/events/event/presence/personal");
	    
	    String uri = presence.getAttributeValue("uri");
	    FetionBuddy buddy = client.getFetionStore().getBuddy(uri);
	    
	    //状态改变
	    if(basic!=null&&buddy!=null) {
    	    int oldpresense = buddy.getPresence(); 
    	    int curpresense = Integer.parseInt(basic.getAttributeValue("value"));    
    	    if(oldpresense!=curpresense) {
    	    	buddy.setPresence(curpresense);
    	    	client.getNotifyListener().presenceChanged(buddy);
    	    }
	    }
	    //好友信息改变
	    if(personal!=null) {
	    	ParseHelper.parseBuddyPersonalBasic(buddy, personal);
	    }
	    
	    logger.debug("PresenceChanged:"+buddy.getDisplayName()+" [presence="+buddy.getPresence()+"]");
	    //TODO ..这里只处理了好友状态改变，本来还应该处理其他信息改变，如好友个性签名和昵称的改变，以后添加。。
    }

}
