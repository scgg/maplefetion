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
 * Project  : maplefetion-2.5
 * Package  : net.solosky.maplefetion.client.notify
 * File     : DialogInfoNotifyHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2011-3-23
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.notify;

import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.client.dialog.ChatDialogProxy;
import net.solosky.maplefetion.event.notify.ChatInputEvent;
import net.solosky.maplefetion.event.notify.ChatNudgeEvent;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 * 在线聊天对话信息处理，包括输入状态，震屏消息
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public class DialogInfoNotifyHandler extends AbstractNotifyHandler{

	@Override
	public void handle(SipcNotify notify) throws FetionException {
		Element root = XMLHelper.build(notify.getBody().toSendString());
		Element node = XMLHelper.find(root, "/is-composing/state");
		String  state = node.getText();
		Buddy   from  = context.getFetionStore().getBuddyByUri(notify.getFrom());
		ChatDialogProxy chatDialogProxy = this.context.getChatDialogProxyFactoy().create(from);
		if("nudge".equals(state)){
			this.tryFireNotifyEvent(new ChatNudgeEvent(chatDialogProxy));
		}else if("input".equals(state)){
			this.tryFireNotifyEvent(new ChatInputEvent(chatDialogProxy));
		}else{
			logger.warn("unknown dialog info notify.\n\n"+notify.toSendString());
		}
	}

}
