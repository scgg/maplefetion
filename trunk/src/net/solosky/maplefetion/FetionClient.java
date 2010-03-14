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
 * File     : FetionClient.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-10
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import java.util.Collection;
import java.util.Timer;

import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.bean.VerifyImage;
import net.solosky.maplefetion.client.LoginWork;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.DialogFactory;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferFactory;
import net.solosky.maplefetion.sipc.SipcMessage;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.SingleExecutor;
import net.solosky.maplefetion.util.VerifyImageFetcher;

import org.apache.log4j.Logger;

/**
 *
 * 飞信主客户端
 *
 * @author solosky <solosky772@qq.com>
 */
public class FetionClient implements FetionContext
{
	/**
	 * MapleFetion版本
	 */
	public static final String CLIENT_VERSION = "MapleFetion 2.0 Beta1";
	
	/**
	 * 协议版本
	 */
	public static final String PROTOCOL_VERSION = "2009 3.5.1170";
	
	/**
	 * SIPC版本
	 */
	public static final String SIPC_VERSION = SipcMessage.SIP_VERSION;
	/**
	 * 传输工厂
	 */
	private TransferFactory transferFactory;
	
	/**
	 * 对话框工厂
	 */
	private DialogFactory dialogFactory;
	
	/**
	 * 单线程执行器
	 */
	private SingleExecutor singleExecutor;
	
	/**
	 * 飞信用户
	 */
	private User user;
	
	/**
	 * 飞信存储对象
	 */
	private FetionStore store;
	
	/**
	 * 全局定时器
	 */
	private Timer globalTimer;
	
	/**
	 * 登录过程
	 */
	private LoginWork loginWork;
	
	/**
	 * 登录监听器
	 */
	private LoginListener loginListener;
	
	/**
	 * 通知监听器
	 */
	private NotifyListener notifyListener;
	
	/**
	 * 客户端状态
	 */
	private int state;
	
	
	 /////////////客户端状态常量///////////////////
	/**
	 * 退出状态
	 */
	public static final int STATE_LOGOUT = 0x01;
	/**
	 * 正在登陆
	 */
	public static final int STATE_LOGGING = 0x02;
	/**
	 * 登陆成功，在线状态
	 */
	public static final int STATE_ONLINE = 0x03;
	/**
	 * 网络连接异常
	 */
	public static final int STATE_IO_ERROR = 0x14;
	/**
	 * 服务器关闭了连接
	 */
	public static final int STATE_DISCONNECTED = 0x15;
	/**
	 * 其他客户端登录
	 */
	public static final int STATE_OTHER_LOGIN = 0x16;
	/**
	 * 未知错误
	 */
	public static final int STATE_UNKOWN_ERROR = 0x17;
	

	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(FetionClient.class);
	
	
	/**
	 * 详细的构造函数
	 * @param mobileNo			手机号
	 * @param pass				用户密码
	 * @param transferFactory	传输工厂
	 * @param fetionStore		分析存储对象
	 * @param notifyListener	通知监听器
	 * @param loginListener		登录监听器
	 */
	public FetionClient(long mobileNo,
							String pass, 
							TransferFactory transferFactory,
							FetionStore fetionStore,
							NotifyListener notifyListener,
							LoginListener loginListener)
	{
		this(new User(mobileNo, pass, "fetion.com.cn"), transferFactory, fetionStore, notifyListener, loginListener);
	}
	
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getDialogFactory()
     */
	public DialogFactory getDialogFactory()
	{
		return this.dialogFactory;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getTransferFactory()
     */
	public TransferFactory getTransferFactory()
	{
		return this.transferFactory;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getSingleExecutor()
     */
	public SingleExecutor getSingleExecutor()
	{
		return this.singleExecutor;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getGlobalTimer()
     */
	public Timer getGlobalTimer()
	{
		return this.globalTimer;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getFetionUser()
     */
	public User getFetionUser()
	{
		return this.user;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getFetionStore()
     */
	public FetionStore getFetionStore()
	{
		return this.store;
	}
	
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getLoginListener()
     */
    public LoginListener getLoginListener()
    {
    	return loginListener;
    }
    
    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getNotifyListener()
     */
    public NotifyListener getNotifyListener()
    {
    	return notifyListener;
    }


	/**
	 * 完整的构造函数
	 * @param user				登录用户
	 * @param transferFactory	连接工厂
	 * @param fetionStore		存储接口
	 * @param notifyListener	通知监听器
	 * @param loginListener		登录监听器
	 */
    public FetionClient(User user, 
    					TransferFactory transferFactory,
    					FetionStore fetionStore, 
    					NotifyListener notifyListener, 
    					LoginListener loginListener)
    {
    	this.user            = user;
    	this.transferFactory = transferFactory;
    	this.store           = fetionStore;
    	this.loginListener   = loginListener;
		this.notifyListener  = notifyListener;
		
    }
    
    /**
     * 初始化
     */
    private void init()
    {
    	this.globalTimer     = new Timer(true);
		this.singleExecutor  = new SingleExecutor();
    	this.dialogFactory   = new DialogFactory(this);
    	this.loginWork       = new LoginWork(this, Presence.ONLINE);
    	
		this.store.init(user);
    }
    
    
    /**
     * 系统退出或者发生异常后释放系统资源
     */
    private void dispose()
    {
         this.transferFactory.closeFactory();
         this.singleExecutor.close();
         this.globalTimer.cancel();
    }

    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#updateStatus(int)
     */
    public synchronized void updateState(int status)
    {
    	this.state = status;
    	if(this.state==FetionClient.STATE_DISCONNECTED ||
    			this.state==FetionClient.STATE_IO_ERROR ||
    			this.state==FetionClient.STATE_OTHER_LOGIN ||
    			this.state==FetionClient.STATE_UNKOWN_ERROR) {
    		this.dispose();
    	}
    	this.notifyListener.clientStateChanged(status);
    }
    
    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getStatus()
     */
    public synchronized int getState()
    {
    	return this.state;
    }
    
    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#handleException(net.solosky.maplefetion.FetionException)
     */
    public void handleException(FetionException exception)
    {
    	//记录错误信息
    	logger.fatal("client internal error.", exception);
    	try {
	        this.dialogFactory.closeAllDialog();
        } catch (FetionException e) {
        	logger.warn("Close All Dialog error.", e);
        }
    	this.dispose();
    	//更新客户端状态
    	if(exception instanceof TransferException) {
    		this.updateState(STATE_IO_ERROR);
    	}else {
    		this.updateState(STATE_UNKOWN_ERROR);
    	}
    }

    
    /**
     * 获取验证图片
     * @return
     */
    public VerifyImage fetchVerifyImage()
    {
    	return VerifyImageFetcher.fetch();
    }

	/**
     * 退出登录
     */
    public void logout()
    {
    	if(this.state==FetionClient.STATE_ONLINE) {
        	Runnable r = new Runnable(){
    			public void run(){
    				try {
                        dialogFactory.closeAllDialog();
                        transferFactory.closeFactory();
                        globalTimer.cancel();
                    	updateState(STATE_LOGOUT);
                    } catch (FetionException e) {
                    	logger.warn("closeDialog error.", e);
                    }
    			}
    		};
    		this.singleExecutor.submit(r);
    		this.singleExecutor.close();
    	}
    }


	/**
	 * 以验证码登录
     * @param verifyImage 验证图片
     */
    public void login(VerifyImage img)
    {
    	this.loginWork.setVerifyImage(img);
		this.singleExecutor.submit(this.loginWork);
    }


	/**
	 * 客户端登录
	 * 这是个异步操作，会把登录的操作封装在单线程池里去执行，登录结果应该通过LoginListener异步通知结果
	 * @param presence 在线状态 定义在Presence中 
	 */
	public void login(int presence)
	{
		//为了便于掉线后可以重新登录，把初始化对象的工作放在登录函数做
		this.init();
		this.loginWork.setPresence(presence);
		this.singleExecutor.submit(this.loginWork);
	}
	
	/**
	 * 客户端登录
	 */
	public void login()
	{
		this.login(Presence.ONLINE);
	}
	
	/////////////////////////////////////////////用户操作开始/////////////////////////////////////////////
	
	/**
	 * 发送聊天消息，如果好友不在线将会发送到手机
	 * @param toBuddy  发送聊天消息的好友
	 * @param message  需发送的消息
	 * @param listener 操作结果监听器
	 */
	public void sendChatMessage(final Buddy toBuddy, final Message message, final ActionListener listener)
	{
		ChatDialog dialog = this.dialogFactory.findChatDialog(toBuddy);
		if(dialog==null) {
			//建立对话框，建立和打开对话框都是同步操作，为了这里实现异步操作，这里封装为一个Runnable对象交给单线程池去运行
			Runnable r = new Runnable(){
				public void run()
				{
					try {
	                    ChatDialog newDialog = dialogFactory.createChatDialog(toBuddy);
	                    synchronized (newDialog) {
		                    newDialog.openDialog();
		                    newDialog.sendChatMessage(message, listener);
                        }
                    } catch (FetionException e) {
                    	logger.warn("openDialog failed.", e);
                    	listener.actionFinished(ActionStatus.IO_ERROR);
                    }
				}
			};
			this.singleExecutor.submit(r);
		}else {
			synchronized (dialog) {
				dialog.sendChatMessage(message, listener);
            }
		}
	}
	
	
	/**
	 * 发送短信消息 这个消息一定是发送到手机上
	 * @param toBuddy	发送消息的好友
	 * @param message	需发送的消息
	 * @param listener	操作监听器
	 */
	public void sendSMSMessage(Buddy toBuddy, Message message, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().sendSMSMessage(toBuddy, message, listener);
	}
	
	/**
	 * 设置个人信息
	 * 
	 * <code>
	 * 这是一个强大的API，基本上可以改变自己的任何信息
	 * 建议使用client.setNickName()和client.setImpresa()简单接口
	 * 比如要更改用户昵称和签名可以这样
	 * User user = client.getFetionUser();
	 * user.setNickName("GoodDay");
	 * user.setImpresa("I'd love it..");
	 * client.setPersonalInfo(new ActionListener(){
	 * 		public void actionFinished(int status){
	 * 			if(status==ActionStatus.ACTION_OK)
	 * 				System.out.println("set personal info success!");
	 * 			else
	 * 				System.out.println("set personal info failed!");
	 * 		}
	 * });
	 * </code>
	 * @param listener
	 */
	public void setPersonalInfo(ActionListener listener)
	{
		this.dialogFactory.getServerDialog().setPesonalInfo(listener);
	}
	
	/**
	 * 设置好友本地姓名
	 * @param buddy		 好友
	 * @param localName	 本地姓名
	 * @param listener
	 */
	public void setBuddyLocalName(Buddy buddy,String localName, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().setBuddyLocalName(buddy, localName, listener);
	}
	
	/**
	 * 设置好友分组
	 * @param buddy
	 * @param cordIds
	 * @param listener
	 */
	public void setBuddyCord(Buddy buddy, Collection<Cord> cordList, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().setBuddyCord(buddy, cordList, listener);
	}
	
	/**
	 * 添加飞信好友
	 * @param mobile		手机号码
	 * @param listener		结果监听器
	 */
	public void addBuddy(long mobile, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().addBuddy("tel:"+Long.toString(mobile), 0, 1, user.getNickName(), listener);
	}
	
	/**
	 * 删除好友
	 * @param buddy
	 * @param listener
	 */
	public void deleteBuddy(Buddy buddy, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().deleteBuddy(buddy, listener);
	}
	
	/**
	 * 同意对方添加好友
	 * @param buddy			飞信好友
	 * @param listener
	 */
	public void agreedApplication(Buddy buddy, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().agreedApplication(buddy, listener);
	}
	
	/**
	 * 拒绝对方添加请求
	 * @param buddy			飞信好友
	 * @param listener
	 */
	public void declinedApplication(Buddy buddy, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().declinedAppliction(buddy, listener);
	}
	
	
	/**
	 * 设置用户状态
	 * @param presence		用户状态定义在Presence中
	 * @param listener
	 */
	public void setPresence(int presence, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().setPresence(presence, listener);
	}
	
	/**
	 * 获取好友的详细信息
	 * @param buddy		飞信好友，只能是飞信好友
	 * @param listener
	 */
	public void getBuddyDetail(FetionBuddy buddy, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().getBuddyDetail(buddy, listener);
	}
}
