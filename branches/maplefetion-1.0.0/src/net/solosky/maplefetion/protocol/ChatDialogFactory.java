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
 * Package  : net.solosky.maplefetion.protocol
 * File     : ChatDialogFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-27
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.IFetionClient;

/**
 *
 *	构造聊天对话工厂
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ChatDialogFactory
{
	/**
	 * 所有聊天会话
	 */
	private ArrayList<ChatDialog> chatDialogList;
	
	/**
	 * 飞信客户端
	 */
	private IFetionClient  client;
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(ChatDialogFactory.class);
	
	/**
	 * 默认构造函数
	 */
	public ChatDialogFactory(IFetionClient client)
	{
		this.client = client;
		this.chatDialogList = new ArrayList<ChatDialog>();
		this.client.getGlobalTimer().schedule(
				new ActiveTimeChecker(),
				3*60*1000, 
				FetionConfig.getInteger("fetion.dialog.check-idle-interval")*1000);
	}
	
	/**
	 * 建立聊天对话
	 * @throws Exception 
	 */
	public synchronized ChatDialog createChatDialog(String host, int port, String ticket) throws Exception
	{
		ChatDialog cd = new ChatDialog(client, host, port, ticket);
		cd.openDialog();
		chatDialogList.add(cd);
		return cd;
	}
	
	/**
	 * 返回当前所有活动的对话
	 * @return
	 */
	public List<ChatDialog> getChatDialogList()
	{
		return this.chatDialogList;
	}
	
	/**
	 * 查找好友所在的聊天对话
	 * @param uri
	 * @return
	 */
	public synchronized ChatDialog findBuddyChatDialog(String uri)
	{
		Iterator<ChatDialog> it = this.chatDialogList.iterator();
		while(it.hasNext()) {
			ChatDialog cd = it.next();
			if(cd.isInDialog(uri))
				return cd;
		}
		return null;
	}
	
	/**
	 * 关闭对话
	 * @param cd
	 * @throws Exception 
	 */
	public void closeChatDialog(ChatDialog cd) throws Exception
	{
		cd.closeDialog();
		chatDialogList.remove(cd);
	}
	
	/**
	 * 
	 * 关闭所有对话
	 * @throws Exception
	 */
	public void closeAllChatDialog() throws Exception
	{
		Iterator<ChatDialog> it = this.chatDialogList.iterator();
		while(it.hasNext()) {
			ChatDialog cd = it.next();
			cd.closeDialog();
		}
	}
	
	/**
	 * 内部类，交个全局定时器每隔指定的时间运行，
	 * 检查当前建立连接的对话，如果在指定的时间内没有任何活动的话，关闭这个对话，释放资源
	 */
	public class ActiveTimeChecker extends TimerTask{
		/* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
        	logger.debug("ActiveTimeChecker is checking idle ChatDialog...");
        	Iterator<ChatDialog> it = chatDialogList.iterator();
        	long curtime = System.currentTimeMillis();
        	long interval = FetionConfig.getInteger("fetion.dialog.max-idle-time");
        	while(it.hasNext()) {
        		ChatDialog cd = it.next();
        		if((cd.getLastActiveTime()+interval)<curtime) {
        			//如果在5分钟内没有消息发送或者接受，关闭这个对话
        			try {
        				int min = (int) ((curtime-cd.getLastActiveTime())/(1000*60));
        				logger.debug("ChatDialog is idle "+Integer.toString(min)+" minutes, now close it ..");
        				cd.closeDialog();
	                    it.remove();
                    } catch (Exception e) {
	                   logger.warn("Exception caught when closing idle ChatDialog:"+e);
                    }
        		}
        	}
        }
		
	}
}
