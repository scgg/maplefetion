/*
s * Licensed to the Apache Software Foundation (ASF) under one or more
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
 * Package  : net.solosky.net.maplefetion.client.dialog
 * File     : DialogFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-10
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferFactory;
import net.solosky.maplefetion.sipc.SipcNotify;

import org.apache.log4j.Logger;

/**
 * 
 * 对话工厂，建立三种类型的对话
 * 
 * @author solosky <solosky772@qq.com>
 */
public class DialogFactory
{
	/**
	 * 客户端对象
	 */
	private FetionContext context;

	/**
	 * 服务器对话
	 */
	private ServerDialog serverDialog;

	/**
	 * 聊天对话列表
	 */
	private ArrayList<ChatDialog> chatDialogList;

	/**
	 * 群对话框列表
	 */
	private ArrayList<GroupDialog> groupDialogList;

	/**
	 * 定时检查闲置的对话框任务
	 */
	private TimerTask idleTimeCheckTask;

	/**
	 * LOGGER
	 */
	private static Logger logger = Logger.getLogger(DialogFactory.class);

	/**
	 * 默认构造函数
	 * 
	 * @param client
	 */
	public DialogFactory(FetionContext client)
	{
		this.context = client;
		this.chatDialogList = new ArrayList<ChatDialog>();
		this.groupDialogList = new ArrayList<GroupDialog>();
		this.idleTimeCheckTask = new IdleTimeCheckTask();

		this.context.getGlobalTimer().schedule(
		                this.idleTimeCheckTask, 0,
		                FetionConfig.getInteger("fetion.dialog.check-idle-interval") * 1000);
	}

	/**
	 * 创建服务器对话，只能创建一次
	 * 
	 * @param user
	 *            登录用户对象
	 */
	public synchronized ServerDialog createServerDialog()
	{
		this.serverDialog = new ServerDialog(context);
		return this.serverDialog;
	}

	/**
	 * 创建聊天对话
	 * 
	 * @param buddy
	 *            好友对象
	 * @return
	 */
	public synchronized ChatDialog createChatDialog(Buddy buddy)
	        throws DialogException
	{
		ChatDialog dialog = this.findChatDialog(buddy);
		if (dialog != null && dialog.getState()!=DialogState.CLOSED && dialog.getState()!=DialogState.FAILED)
			return dialog;

		int presence = buddy.getPresence().getValue();
		// 如果用户手机在线 或者电脑离线，将建立手机聊天对话框
		if (presence == Presence.OFFLINE) {
			dialog = new BasicChatDialog(this.context, buddy);
		} else if (presence == Presence.ONLINE || presence == Presence.AWAY
		        || presence == Presence.BUSY || presence == Presence.HIDEN) { // 如果用户电脑在线，建立在线聊天对话框
			TransferFactory factory = this.context.getTransferFactory();
			if (factory.isMutiConnectionSupported()) {
				dialog = new LiveV2ChatDialog(buddy, this.context);
			} else {
				dialog = new LiveV1ChatDialog(buddy, this.context);
			}
		} else {
			throw new DialogException("Illegal buddy presence - presence="
			        + Integer.toString(presence));
		}

		// 添加对话框至列表并返回
		this.chatDialogList.add(dialog);
		return dialog;
	}

	/**
	 * 以一个邀请通知创建会话
	 * 
	 * @param inviteNotify
	 *            邀请的通知
	 * @return
	 */
	public synchronized ChatDialog createChatDialog(SipcNotify inviteNotify) throws DialogException
	{
		// 当收到会话邀请时，发起邀请的好友一定是在线，手机在线的好友是不会发起会话邀请的，所以这里无需判断好友状态
		ChatDialog dialog = null;

		if (this.context.getTransferFactory().isMutiConnectionSupported()) {
			dialog = new LiveV2ChatDialog(inviteNotify, context);
		} else {
			dialog = new LiveV1ChatDialog(inviteNotify, context);
		}
		this.chatDialogList.add(dialog);

		return dialog;
	}

	/**
	 * 创建群组对话
	 * 
	 * @param group
	 *            群对象
	 * @return
	 */
	public synchronized GroupDialog createGroupDialog(Group group)
	{
		GroupDialog dialog = new GroupDialog(this.context, group);
		this.groupDialogList.add(dialog);
		return dialog;
	}

	/**
	 * 查找聊天对话框 只是查找对话框的主要参与者
	 * 
	 * @param buddy
	 *            飞信对象
	 * @return
	 */
	public synchronized ChatDialog findChatDialog(Buddy buddy)
	{
		Iterator<ChatDialog> it = this.chatDialogList.iterator();
		ChatDialog dialog = null;
		while (it.hasNext()) {
			dialog = it.next();
			if (dialog.getMainBuddy().equals(buddy)) {
				return dialog;
			}
		}
		return null;
	}
	
	
	/**
	 * 返回一个聊天对话框，首先查找当前活动的聊天对话，如果找到并且没有关闭就返回这个对话
	 * 如果不存在或者会话已经关闭就新建立一个对话并返回
	 * @param buddy		好友对象
	 * @return			聊天对话
	 * @throws DialogException
	 */
	public ChatDialog getChatDialog(Buddy buddy) throws DialogException
	{
		ChatDialog dialog = this.findChatDialog(buddy);
		if(dialog!=null && dialog.getState()!=DialogState.CLOSED) {
			return dialog;
		}else {
			return this.createChatDialog(buddy);
		}
		
	}

	/**
	 * 查找群对话框
	 * 
	 * @param group
	 *            群对象
	 * @return
	 */
	public synchronized GroupDialog findGroupDialog(Group group)
	{
		Iterator<GroupDialog> it = this.groupDialogList.iterator();
		GroupDialog dialog = null;
		while (it.hasNext()) {
			dialog = it.next();
			if (dialog.getGroup().equals(group)) {
				break;
			}
		}

		return dialog;
	}

	/**
	 * 返回服务器对话框
	 * 
	 * @return
	 */
	public synchronized ServerDialog getServerDialog()
	{
		return this.serverDialog;
	}

	/**
	 * 关闭聊天对话框
	 * 
	 * @throws DialogException
	 * @throws TransferException
	 */
	public synchronized void closeDialog(Dialog dialog)
	        throws TransferException, DialogException
	{
		dialog.closeDialog();
		if (dialog instanceof ChatDialog) {
			this.chatDialogList.remove(dialog);
		}
		logger.debug("Dialog is closed by client."+dialog.toString());
	}
	
	/**
	 * 关闭所有的对话框
	 * @throws TransferException
	 * @throws DialogException
	 */
	public synchronized void closeAllDialog() throws TransferException, DialogException
	{
		Iterator<GroupDialog> git = this.groupDialogList.iterator();
		while(git.hasNext()) {
			git.next().closeDialog();
		}
		
		Iterator<ChatDialog> cit = this.chatDialogList.iterator();
		while(cit.hasNext()) {
			cit.next().closeDialog();
		}
		
		this.serverDialog.closeDialog();
	}

	/**
	 * 为了减少资源占用率，如果用户没有手动关闭对话框，就需要一个计划任务定时检查空闲的对话框， 
	 * 如果对话框在指定的时间没有收到消息，就关闭这个对话框
	 * 
	 * 
	 * @author solosky <solosky772@qq.com>
	 */
	private class IdleTimeCheckTask extends TimerTask
	{
		@Override
		public void run()
		{
			int maxIdleTime = FetionConfig
			        .getInteger("fetion.dialog.max-idle-time"); // 最大空闲时间,单位秒，用户可以设置
			Iterator<ChatDialog> it = chatDialogList.iterator();
			while (it.hasNext()) {
				ChatDialog dialog = it.next();
				if (dialog.getState()==DialogState.CLOSED)
					it.remove();
				else if (dialog.getActiveTime() + maxIdleTime < (int) (System.currentTimeMillis() / 1000)) {
					dialog.closeDialog();
                    it.remove();
				} else {
				}
			}
		}

	}
}
