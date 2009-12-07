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
 * Package  : net.solosky.maplefetion
 * File     : FetionClient.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import java.util.Timer;

import net.solosky.maplefetion.bean.FetionUser;
import net.solosky.maplefetion.net.ITransferFactory;
import net.solosky.maplefetion.protocol.ChatDialogFactory;
import net.solosky.maplefetion.protocol.ServerDialog;
import net.solosky.maplefetion.store.IFetionStore;

/**
 *
 *	飞信客户端接口
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface IFetionClient
{
	/**
	 * 返回会话对象
	 * @return		会话对象
	 */
	public FetionSession getFetionSession();
	
	/**
	 * 返回存储对象
	 * @return		存储对象
	 */
	public IFetionStore   getFetionStore();
	
	/**
	 * 返回连接工厂对象
	 * @return		传输对象
	 */
	public ITransferFactory   getTransferFactory();
	
	/**
	 * 返回用户对象
	 */
	public FetionUser getFetionUser();
	
	/**
	 * 返回用户设置的通知监听器
	 * @return		通知监听器
	 */
	public INotifyListener getNotifyListener();
	
	/**
	 * 返回用户登录监听器
	 * @return		登陆监听器
	 */
	public ILoginListener getLoginListener();
	
	/**
	 * 返回全局的定时器
	 * @return		定时器
	 */
	public Timer getGlobalTimer();
	
	
	/**
	 * 返回对话工厂
	 * @return		对话工厂
	 */
	public ChatDialogFactory getChatDialogFactory();
	
	/**
	 * 返回服务器对话
	 * @return		服务器对话
	 */
	public ServerDialog getServerDialog();
	
	/**
	 * 发生了致命的异常
	 * @param e		异常
	 */
	public void exceptionCaught(Throwable exception);
	
	
}
