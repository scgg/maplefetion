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
 * Package  : net.solosky.maplefetion.event.notify
 * File     : ChatNudgeEvent.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2011-3-23
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.event.notify;

import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.client.dialog.ChatDialogProxy;
import net.solosky.maplefetion.event.NotifyEvent;
import net.solosky.maplefetion.event.NotifyEventType;

/**
 *
 * 震屏消息
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public class ChatNudgeEvent extends NotifyEvent {

private ChatDialogProxy proxy;
	
	public ChatNudgeEvent(ChatDialogProxy proxy) {
		this.proxy = proxy;
	}

	public ChatDialogProxy getChatDialogProxy() {
		return proxy;
	}
	
	public Buddy getBuddy()	{
		return proxy.getMainBuddy();
	}
	
	@Override
	public NotifyEventType getEventType() {
		return NotifyEventType.CHAT_NUDGE;
	}

	@Override
	public String toString() {
		return "ChatNudgeEvent [proxy=" + proxy + "]";
	}
}
