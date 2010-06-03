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
 * Package  : net.solosky.net.maplefetion.client
 * File     : Dialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-10
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.ActionEventType;
import net.solosky.maplefetion.event.action.SuccessEvent;
import net.solosky.maplefetion.event.action.SystemErrorEvent;
import net.solosky.maplefetion.event.action.TimeoutEvent;
import net.solosky.maplefetion.event.action.TransferErrorEvent;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.sipc.SipcOutMessage;

/**
 *
 * 飞信对话
 * 一个对话可以是一些列操作的分类
 * ServerDialog : 和主服务器通信，处理登录，添加好友等操作，有且仅有一个，在登录时创建
 * ChatDialog   : 聊天对话，和用户交谈时创建，处理聊天信息，不同的登录模式聊天对话框处理方式不同
 *		BasicChatDialog  : 和短信在线好友聊天的对话框
 *		LiveV2ChatDialog  : 和在线好友聊天时候就是用在线聊天对话
 *		LiveV1ChatDialog  : 使用旧版本协议和在线好友聊天时的对话框
 * GroupDialog  : 群对话，和群里的消息和处理群的登录和退出，在登录的时候自动创建
 *
 * @author solosky <solosky772@qq.com>
 */
public abstract class Dialog
{
	
	/**
	 * 飞信客户端
	 */
	protected FetionContext context;
	
	/**
	 *  对话框会话
	 */
	protected DialogSession session;
	
	/**
	 * 对话框状态
	 */
	private DialogState state;
	
	/**
	 * 对话框监听器
	 */
	protected DialogListener listener;
	/**
	 * 默认的构造函数
	 */
	public Dialog(FetionContext context)
	{
		this.context = context;
		this.state  = DialogState.CREATED;
		this.session = new DialogSession();
	}
	
	/**
	 * 启动这个对话框
	 */
	public abstract void openDialog() throws TransferException, RequestTimeoutException, DialogException;
	
	/**
	 * 关闭这个对话框
	 */
	public abstract void closeDialog();
	
	/**
	 * 发送信令
	 * @param out		发出信令
	 * @throws FetionException
	 */
	public abstract void process(SipcOutMessage out) throws TransferException;
	
	/**
	 * 返回会话对象
	 * @return
	 */
	public DialogSession getSession()
	{
		return this.session;
	}
	
	/**
	 * 断言回复状态，如果状态不同就抛出无效回复异常
	 * @throws IllegalResponseException 
	 */
	public static void assertStatus(int currentStatus, int expectedStatus) throws IllegalResponseException
	{
		if(currentStatus!=expectedStatus) {
			throw new IllegalResponseException("Unexpected response status - " +
					"current = "+Integer.toString(currentStatus)+
					", expected = "+Integer.toString(expectedStatus));
		}
	}
	
	/**
	 * 断言回复事件，如果事件不是指定的事件就抛出无效回复异常
	 * @param event
	 * @param type
	 * @throws IllegalResponseException
	 */
	public static void assertActionEvent(ActionEvent event, ActionEventType type) throws IllegalResponseException
	{
		if(event.getEventType()!=type) {
			throw new IllegalResponseException("Unexpected response event - " +
					"current = "+event+
					", expected = "+type.name());
		}
	}
	
	/**
	 * 确保对话框是打开状态的，否则就跑出IllegalStateException
	 * @throws IllegalStateException 如果对话框没有打开或者已经关闭，则抛出
	 */
	protected synchronized void ensureOpened()
	{
		if(this.state == DialogState.CLOSED) {
			throw new IllegalStateException("Dialog is closed.");
		}else if(this.state == DialogState.OPENNING) {
			throw new IllegalStateException("Dialog is openning.");
		}else if(this.state == DialogState.CREATED){
			throw new IllegalStateException("Dialog just created.");
		}else if(this.state == DialogState.FAILED) {
			throw new IllegalStateException("Dialog is failed to open.");
		}
	}
	
	/**
	 * 异步模式打开对话框
	 * @param listener
	 */
	public void openDialog(final ActionEventListener listener)
	{
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					  openDialog();
		              listener.fireEevent(new SuccessEvent());
	                } catch (TransferException e) {
	                	 listener.fireEevent(new TransferErrorEvent());
	                } catch (RequestTimeoutException e) {
	                	listener.fireEevent(new TimeoutEvent());
	                } catch (DialogException e) {
	                	listener.fireEevent(new SystemErrorEvent(e));
	                }
			}
		};
		this.context.getFetionExecutor().submitTask(r);
	}
	
	
	/**
	 * 异步模式关闭对话框
	 */
	public void closeDialog(final ActionEventListener listener)
	{
		Runnable r = new Runnable() {
			public void run() {
				closeDialog();
				listener.fireEevent(new SuccessEvent());
			}
		};
		this.context.getFetionExecutor().submitTask(r);
	}

	/**
	 * 返回消息工厂
     * @return
     */
    public abstract MessageFactory getMessageFactory();
    
    
    /**
     * 返回对话框状态
     * @return
     */
    public DialogState getState()
    {
    	return this.state;
    }
    
    /**
     * 设置对话框状态
     * @param state
     */
    protected synchronized void setState(DialogState state)
    {
    	this.state = state;
    	if(this.listener!=null)
    		this.listener.dialogStateChanged(state);
    }
    
    /**
     * 设置对话框监听器
     * @param listener
     */
    public void setDialogListener(DialogListener listener)
    {
    	this.listener = listener;
    }
}