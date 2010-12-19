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
 * File     : ByeNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-5-6
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.DialogState;
import net.solosky.maplefetion.sipc.SipcNotify;

/**
 * 
 * 好友离开对话的通知
 *
 * @author solosky <solosky772@qq.com>
 */
public class ByeNotifyHandler extends AbstractNotifyHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.NotifyHandler#handle(net.solosky.maplefetion.sipc.SipcNotify)
     */
    @Override
    public void handle(SipcNotify notify) throws FetionException
    {
    	Buddy buddy = this.context.getFetionStore().getBuddyByUri(notify.getFrom());
    	if(buddy!=null) {
    		ChatDialog dialog = this.context.getDialogFactory().findChatDialog(buddy);
    		if(dialog!=null && dialog.getState()!=DialogState.CLOSED) {
    			dialog.closeDialog();
    		}
    	}
    }

}
