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
 * Package  : net.solosky.maplefetion.client
 * File     : UpdateBuddyListWork.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-6-29
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client;


import java.util.ArrayList;
import java.util.Iterator;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.ActionEventType;
import net.solosky.maplefetion.event.action.ActionEventFuture;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.event.action.SuccessEvent;

/**
 *
 * 更新好友的操作
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public class UpdateBuddyListWork implements Runnable
{
	/**
	 * 飞信上下文对象
	 */
	private FetionContext context;
	
	/**
	 * 结果回调
	 */
	private ActionEventListener listener;
	
	/**
	 * 构造函数
	 * @param context
	 */
	public UpdateBuddyListWork(FetionContext context, ActionEventListener listener)
	{
		this.context  = context;
		this.listener = listener;
	}
	
	/**
	 * 获取好友列表
	 * @return
	 */
	private ActionEvent updateBuddyList()
	{
		ActionEventFuture future = new ActionEventFuture();
		this.context.getDialogFactory().getServerDialog().getContactList(future);
		return future.waitActionEventWithoutException();
	}
	
	/**
	 * 获取联系人详细信息， 这里只有飞信好友才能获取消息信息
	 * @return
	 */
	private ActionEvent updateBuddiesInfo()
	{
		ActionEventFuture future = new ActionEventFuture();
        ArrayList<FetionBuddy> list = new ArrayList<FetionBuddy>();
        Iterator<Buddy> it = this.context.getFetionStore().getBuddyList().iterator();
        while(it.hasNext()) {
        	Buddy b = it.next();
        	if(b instanceof FetionBuddy) {
        		list.add((FetionBuddy)b);
        	}
        }
        if(list.size()>0){
			this.context.getDialogFactory().getServerDialog().getContactsInfo(list, future);
			return future.waitActionEventWithoutException();
        }else{
        	return new SuccessEvent();
        }
	}
	
	/**
	 * 执行更新好友信息
	 */
	@Override
	public void run()
	{
		ActionEvent event = this.updateBuddyList();
		if(event.getEventType()==ActionEventType.SUCCESS){
			this.listener.fireEevent(this.updateBuddiesInfo());
		}else{
			this.listener.fireEevent(event);
		}
	}

}
