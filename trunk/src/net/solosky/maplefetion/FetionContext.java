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
 * Package  : net.solosky.maplefetion
 * File     : FetionContext.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.client.dialog.DialogFactory;
import net.solosky.maplefetion.net.TransferFactory;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.FetionExecutor;
import net.solosky.maplefetion.util.FetionTimer;
import net.solosky.maplefetion.util.ObjectWaiter;

/**
 *
 * 飞信运行上下文 相当于一个导航树，便于子模块可以获得飞信运行环境时的资源
 *
 * @author solosky <solosky772@qq.com>
 */
public interface FetionContext
{

	/**
	 * 返回对话框工厂
	 * @return
	 */
	public abstract DialogFactory getDialogFactory();

	/**
	 * 返回传输工厂
	 * @return
	 */
	public abstract TransferFactory getTransferFactory();

	/**
	 * 返回单线程执行器
	 * @return
	 */
	public abstract FetionExecutor getFetionExecutor();

	/**
	 * 返回全局的定时器
	 * @return
	 */
	public abstract FetionTimer getFetionTimer();

	/**
	 * 返回飞信用户
	 * @return
	 */
	public abstract User getFetionUser();

	/**
	 * 返回存储对象
	 */
	public abstract FetionStore getFetionStore();

	/**
	 * 返回登录监听器
	 * @return the loginListener
	 */
	public abstract LoginListener getLoginListener();

	/**
	 * 返回通知监听器
	 * @return the notifyListener
	 */
	public abstract NotifyListener getNotifyListener();

	/**
	 * 设置客户端状态
	 */
	public abstract void updateState(ClientState state);

	/**
	 * 返回客户端状态
	 */
	public abstract ClientState getState();
	
	/**
	 * 返回登录等待对象,  用于同步登录
	 */
	public abstract ObjectWaiter<LoginState> getLoginWaiter();

	/**
	 * 处理不可恢复的异常的回调方法
	 * 通常这个方法是为Client处理异常准备的
	 * @param exception
	 */
	public abstract void handleException(FetionException exception);

}