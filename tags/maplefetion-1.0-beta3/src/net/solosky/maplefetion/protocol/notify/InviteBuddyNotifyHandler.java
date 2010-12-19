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
 * File     : InvateBuddyNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol.notify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPReceipt;

/**
 *
 *	被邀请加入会会话处理器
 *
 * @author solosky <solosky772@qq.com> 
 */
public class InviteBuddyNotifyHandler extends AbstractSIPNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPNotifyHandler#handle(net.solosky.maplefetion.sip.SIPNotify)
     */
    @Override
    public void handle(SIPNotify notify) throws Exception
    {
    	String from = notify.getFrom();
    	FetionBuddy buddy = this.client.getFetionStore().getBuddy(from);
    	if(buddy!=null) {
    		String auth = notify.getHeader(SIPHeader.FIELD_AUTHORIZATION).getValue();
    		Pattern pt = Pattern.compile("address=\"(.*?):(.*?);.*?=\"(.*?)\"");
    		Matcher mc = pt.matcher(auth);
    		if(mc.find()) {
        		this.client.getChatDialogFactory().createChatDialog( mc.group(1), Integer.parseInt(mc.group(2)), mc.group(3));

        		SIPReceipt receipt = this.dialog.getMessageFactory().createChatMessageReceipt(
        				notify.getFrom() ,Integer.toString(notify.getCallID()), notify.getSequence());
        		this.dialog.getTransferService().sendSIPMessage(receipt);
    		}else {
    			logger.warn("cannot parse auth string:"+auth);
    		}
    	}else {
    		logger.warn("Cannot find buddy:[uri="+from+"]");
    		return;
    	}
    }

}
