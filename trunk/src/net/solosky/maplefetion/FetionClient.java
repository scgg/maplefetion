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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.bean.VerifyImage;
import net.solosky.maplefetion.client.LoginException;
import net.solosky.maplefetion.client.LoginWork;
import net.solosky.maplefetion.client.RegistrationException;
import net.solosky.maplefetion.client.SystemException;
import net.solosky.maplefetion.client.dialog.ChatDialogProxy;
import net.solosky.maplefetion.client.dialog.ChatDialogProxyFactory;
import net.solosky.maplefetion.client.dialog.DialogException;
import net.solosky.maplefetion.client.dialog.DialogFactory;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.client.dialog.ServerDialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.ActionEventType;
import net.solosky.maplefetion.event.action.ActionEventFuture;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.event.action.FailureEvent;
import net.solosky.maplefetion.event.action.FailureType;
import net.solosky.maplefetion.event.action.FutureActionEventListener;
import net.solosky.maplefetion.event.action.SystemErrorEvent;
import net.solosky.maplefetion.event.action.success.FindBuddySuccessEvent;
import net.solosky.maplefetion.event.notify.ClientStateEvent;
import net.solosky.maplefetion.net.AutoTransferFactory;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferFactory;
import net.solosky.maplefetion.sipc.SipcMessage;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.store.SimpleFetionStore;
import net.solosky.maplefetion.util.CrushBuilder;
import net.solosky.maplefetion.util.FetionExecutor;
import net.solosky.maplefetion.util.FetionTimer;
import net.solosky.maplefetion.util.ObjectWaiter;
import net.solosky.maplefetion.util.SingleExecutor;
import net.solosky.maplefetion.util.ThreadTimer;
import net.solosky.maplefetion.util.Validator;
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
	private FetionExecutor executor;
	
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
	private FetionTimer timer;
	
	/**
	 * 登录过程
	 */
	private LoginWork loginWork;
	
	/**
	 * 通知监听器
	 */
	private NotifyEventListener notifyEventListener;
	
	/**
	 * 客户端状态，尽量使用简单的同步方法
	 */
	private volatile ClientState state;
	
	/**
	 * 聊天对话代理工厂
	 */
	private ChatDialogProxyFactory proxyFactory;
	
	 /**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(FetionClient.class);
	
	
	/**
	 * 详细的构造函数
	 * @param mobileNo			手机号
	 * @param password			用户密码
	 * @param notifyListener	通知监听器
	 * @param transferFactory	传输工厂
	 * @param fetionStore		分析存储对象
	 * @param loginListener		登录监听器
	 */
	public FetionClient(long mobileNo,
						String password, 
						NotifyEventListener notifyEventListener,
						TransferFactory transferFactory,
						FetionStore fetionStore,
						FetionTimer timer,
						FetionExecutor executor)
	{
		this(new User(mobileNo, password, "fetion.com.cn"),
				notifyEventListener,
				transferFactory,
				fetionStore, 
				timer,
				executor);
	}
	
	
	/**
	 * 使用默认的传输模式和存储模式构造
	 * @param mobileNo			手机号码
	 * @param password			密码
	 * @param notifyListener	通知监听器
	 */
	public FetionClient(long mobileNo, 
						String password, 
						NotifyEventListener notifyEventListener)
	{
		this(new User(mobileNo, password, "fetion.com.cn"), 
				notifyEventListener, 
				new AutoTransferFactory(),
				new SimpleFetionStore(), 
				new ThreadTimer(),
				new SingleExecutor());
	}
	
	/**
	 * 简单的构造函数
	 * @param mobile	用户手机号码
	 * @param password	用户密码
	 */
	public FetionClient(long mobile, String pass)
	{
		this(mobile, pass, null);
	}
	
	
	/**
	 * 完整的构造函数
	 * @param user				登录用户，每一个客户端都只有一个唯一的登录用户对象
	 * @param notifyListener	通知监听器，需实现NotifyEventListener接口，用户设置的通知监听器，处理客户端主动发起的事件
	 * @param transferFactory	连接工厂，需实现TransferFactory接口，用于创建连接对象
	 * @param fetionStore		飞信存储对象，需实现FetionStore接口，存储了飞信好友列表等信息
	 * @param fetionTimer		飞信定时器，需实现FetionTimer接口，用于完成不同的定时任务
	 * @param fetionExecutor	飞信执行器，需实现FetionExecutor接口，可以在另外一个单独的线程中执行任务
	 */
    public FetionClient(User user, 
    					NotifyEventListener notifyEventListener, 
    					TransferFactory transferFactory,
    					FetionStore fetionStore, 
    					FetionTimer fetionTimer,
						FetionExecutor fetionExecutor)
    {
    	FetionConfig.init();
    	
    	this.state           = ClientState.LOGOUT;
    	this.user            = user;
    	this.transferFactory = transferFactory;
    	this.store           = fetionStore;
		this.proxyFactory    = new ChatDialogProxyFactory(this);
		this.timer           = fetionTimer;
		this.executor        = fetionExecutor;
		this.notifyEventListener  = notifyEventListener;
		
		
    }
	
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getDialogFactory()
     */
	public DialogFactory getDialogFactory()
	{
		return this.dialogFactory;
	}
	

	/* (non-Javadoc)
	 * @see net.solosky.maplefetion.FetionContext#getChatDialogProxyFactoy()
	 */
	@Override
	public ChatDialogProxyFactory getChatDialogProxyFactoy()
	{
		return this.proxyFactory;
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
	public FetionExecutor getFetionExecutor()
	{
		return this.executor;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getGlobalTimer()
     */
	public FetionTimer getFetionTimer()
	{
		return this.timer;
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
     * @see net.solosky.maplefetion.FetionContext#getNotifyListener()
     */
    public NotifyEventListener getNotifyEventListener()
    {
    	return this.notifyEventListener;
    }
    
    /**
     * 设置通知监听器
     * @param notifyListener
     */
	public void setNotifyEventListener(NotifyEventListener notifyEventListener) {
		this.notifyEventListener = notifyEventListener;
	}
	
	/**
	 * 设置是否启用群
	 * @param enabled
	 */
	public void enableGroup(boolean enabled)
	{
		FetionConfig.setBoolean("fetion.group.enable", enabled);
	}
	
	/**
	 * 设置飞信执行池
     * @param executor the executor to set
     */
    public void setFetionExecutor(FetionExecutor executor)
    {
    	this.executor = executor;
    }

	/**
	 * 设置飞信定时器
     * @param timer the timer to set
     */
    public void setFetionTimer(FetionTimer timer)
    {
    	this.timer = timer;
    }


	/**
     * @return the proxyFactory
     */
    public ChatDialogProxyFactory getChatDialogProxyFactory()
    {
    	return proxyFactory;
    }


	/**
     * 初始化
     */
    private void init()
    {
    	this.timer.startTimer();
    	this.executor.startExecutor();
    	this.transferFactory.openFactory();
    	
    	this.dialogFactory   = new DialogFactory(this);
    	this.loginWork       = new LoginWork(this, Presence.ONLINE);
    	
    	this.transferFactory.setFetionContext(this);
		this.store.init(user);
    }
    
    
    /**
     * 系统退出或者发生异常后释放系统资源
     */
    private void dispose()
    {
         this.transferFactory.closeFactory();
         this.dialogFactory.closeFactory();
         this.executor.stopExecutor();
         this.timer.stopTimer();
    }

    /**
     * 更新客户端状态 
     * 这个方法是不同步的，因为属性state是volatile的
     */
    public void updateState(ClientState state)
    {
    	this.state = state;
    	if(this.notifyEventListener!=null)
    		this.notifyEventListener.fireEvent(new ClientStateEvent(state));
    }
    
    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getStatus()
     */
    public ClientState getState()
    {
    	return this.state;
    }
    
    /**
     * 处理客户端异常
     * 通常交给客户端处理的异常都是致命的，也就是说如果调用了这个方法客户端都会主动退出
     */
    public void handleException(FetionException exception)
    {
    	
    	//根据不同的异常类型，来设置客户端的状态
    	if(exception instanceof TransferException) {				//网络错误
    		logger.fatal("Connection error. Please try to login again after several time.");
    		this.state = ClientState.CONNECTION_ERROR;
    	}else if(exception instanceof RegistrationException) {	//注册异常
    		RegistrationException re = (RegistrationException) exception;
    		if(re.getRegistrationType()==RegistrationException.DEREGISTERED) {
    			logger.fatal("You have logined by other client.");
    			this.state = ClientState.OTHER_LOGIN;		//用户其他地方登陆
    		}else if(re.getRegistrationType()==RegistrationException.DISCONNECTED) {
    			logger.fatal("Server closed connecction. Please try to login again after several time.");
    			this.state = ClientState.DISCONNECTED;		//服务器关闭了连接
    		}else {
    			logger.fatal("Unknown registration exception", exception);
    		}
		}else if(exception instanceof SystemException){		//系统错误
			logger.fatal("System error. Please email the crush report or the system log to the project owner and wait for bug fix, thank you.", exception);
			this.state = ClientState.SYSTEM_ERROR;
			CrushBuilder.handleCrushReport(exception, ((SystemException) exception).getArgs());
    	}else if(exception instanceof LoginException){		//登录错误
    		logger.fatal("Login error. state="+((LoginException)exception).getState().name());
    		this.state = ClientState.LOGIN_ERROR;
    	}else {
    		logger.fatal("Unknown error. Please email the crush report or the system log to the project owner and wait for bug fix, thank you.", exception);
    		this.state = ClientState.SYSTEM_ERROR;			//其他错误
    		CrushBuilder.handleCrushReport(exception);
    	}

    	//尝试关闭所有的对话框，对话框应该判断当前客户端状态然后决定是否进行某些操作
    	try {
	        this.dialogFactory.closeAllDialog();
        } catch (FetionException e) {
        	logger.warn("Close All Dialog error.", e);
        }
        
        //最后才能释放系统资源，因为关闭对话可以使用到了系统资源
    	this.dispose();
    	
    	//上面只是更新了客户端状态，还没有回调状态改变函数，为了防止用户在回调函数里面做一些
    	//可能会影响客户端状态的操作，如马上登陆，所以把回调用户函数放在最后面来完成
    	if(this.notifyEventListener!=null)
    		this.notifyEventListener.fireEvent(new ClientStateEvent(state));
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
     * 返回服务器对话
     * @return
     */
    public ServerDialog getServerDialog()
    {
    	return this.dialogFactory.getServerDialog();
    }
    
    /**
     * 返回群对话框
     * @param group		对应的群对象
     * @return
     */
    public GroupDialog getGroupDialog(Group group)
    {
    	return this.dialogFactory.findGroupDialog(group);
    }
    
    /**
     * 返回聊天对话代理
     * @param buddy		对应的好友
     * @return		如果找到，返回聊天对话代理，出现错误，或者不存在，返回null
     */
    public ChatDialogProxy getChatDialogProxy(Buddy buddy)
    {
    	try {
	        return this.proxyFactory.create(buddy);
        } catch (DialogException e) {
	       return null;
        }
    }

	/**
     * 退出登录
     */
    public void logout()
    {
    	if(this.state==ClientState.ONLINE) {
    		try {
                dialogFactory.closeAllDialog();
                dialogFactory.closeFactory();
                transferFactory.closeFactory();
                timer.stopTimer();
                this.executor.stopExecutor();
            	updateState(ClientState.LOGOUT);
            } catch (FetionException e) {
            	logger.warn("logout error.", e);
            }catch(Throwable t) {
            	logger.warn("logout error ", t);
            }
    	}
    }



	/**
	 * 客户端登录
	 * 这是个异步操作，会把登录的操作封装在单线程池里去执行，登录结果应该通过NotifyEventListener异步通知结果
	 * @param presence 		在线状态 定义在Presence中 
	 * @param verifyImage	验证图片，如果没有可以设置为null
	 */
	public void login(int presence, VerifyImage verifyImage)
	{
		//为了便于掉线后可以重新登录，把初始化对象的工作放在登录函数做
		this.init();
		this.loginWork.setPresence(presence);
		this.loginWork.setVerifyImage(verifyImage);
		this.executor.submitTask(this.loginWork);
	}
	
	
	/**
	 *  客户端异步登陆登录
	 *  登陆结果在NotifyEventListener监听LoginStateEvent事件
     */
    public void login()
    {
		this.login(Presence.ONLINE, null);
    }

	
	/**
	 * 客户端同步登录
	 * @param presence 		在线状态 定义在Presence中 
	 * @param verifyImage	验证图片，如果没有可以设置为null
	 * @return 登录结果,定义在LoginState中
	 */
	public LoginState syncLogin(int presence, VerifyImage verifyImage)
	{
		this.login(presence, verifyImage);
		return this.loginWork.waitLoginState();
	}
	
	/**
	 * 客户端同步登录
	 * 不设置超时时间，超时由客户端控制
	 * @return 登录结果,定义在LoginState中
	 */
	public LoginState syncLogin()
	{
		return this.syncLogin(Presence.ONLINE, null);
	}
	
	/////////////////////////////////////////////用户操作开始/////////////////////////////////////////////
	
	/**
	 * 发送聊天消息，如果好友不在线将会发送到手机
	 * @param toBuddy  发送聊天消息的好友
	 * @param message  需发送的消息
	 * @param listener 操作结果监听器
	 */
	public void sendChatMessage(final Buddy toBuddy, final Message message, ActionEventListener listener)
	{
		try {
			ChatDialogProxy proxy = this.proxyFactory.create(toBuddy);
			proxy.sendChatMessage(message, listener);
		} catch (DialogException e) {
			listener.fireEevent(new SystemErrorEvent(e));
		}
	}
	
	/**
	 * 发送聊天消息，如果好友不在线将会发送到手机
	 * @param toBuddy  发送聊天消息的好友
	 * @param message  需发送的消息
	 * @return 操作结果等待对象， 可以在这个对象上调用waitStatus()等待操作结果
	 */
	public ActionEventFuture sendChatMessage(Buddy toBuddy, Message message)
	{
		ActionEventFuture future = new ActionEventFuture();
		this.sendChatMessage(toBuddy, message, new FutureActionEventListener(future));
		return future;
	}
	
	/**
	 * 发送短信消息 这个消息一定是发送到手机上
	 * @param toBuddy	发送消息的好友
	 * @param message	需发送的消息
	 * @param listener	操作监听器
	 */
	public void sendSMSMessage(Buddy toBuddy, Message message, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().sendSMSMessage(toBuddy, message, listener);
	}
	
	/**
	 * 给自己发送短信到手机上
	 * @param message  需发送的消息
	 * @param listener 操作监听器
	 */
	public void sendSMSMessageToSelf(Message message, ActionEventListener listener)
	{
		this.sendSMSMessage(this.getFetionUser(), message, listener);
	}
	
	/**
	 * 通过手机号给好友发送消息，前提是该手机号对应的飞信用户必须已经是好友，否则会发送失败
	 * @param mobile		手机号码
	 * @param message		消息
	 * @param listener 		操作事件监听器
	 */
	public void sendChatMessage(long mobile,final Message message, final ActionEventListener listener)
	{
		//要做的第一件事是找到这个好友，因为通过手机查找好友需要向服务器发起请求，所以这里先建立一个临时的事件监听器
		//当找到好友操作完成之后，判断是否找到，如果找到就发送消息
		ActionEventListener tmpListener = new ActionEventListener() {
			public void fireEevent(ActionEvent event){
				if(event.getEventType()==ActionEventType.SUCCESS){
					//成功的找到了好友，获取这个好友，然后发送消息
					FindBuddySuccessEvent evt = (FindBuddySuccessEvent) event;
					sendChatMessage(evt.getFoundBuddy(), message, listener);
				}else{
					//查找失败直接回调设置的方法
					listener.fireEevent(event);
				}
			}
		};
		//开始查找好友请求
		this.findBuddyByMobile(mobile, tmpListener);
	}
	
	
	/**
	 * 通过手机号给好友发送短信，前提是该手机号对应的飞信用户必须已经是好友，否则会发送失败
	 * @param mobile		手机号码
	 * @param message		消息
	 * @param listener		操作事件监听器
	 */
	public void sendSMSMessage(long mobile, final Message message, final ActionEventListener listener)
	{
		//注释同上一个方法，这里不赘述了
		ActionEventListener tmpListener = new ActionEventListener() {
			public void fireEevent(ActionEvent event){
				if(event.getEventType()==ActionEventType.SUCCESS){
					FindBuddySuccessEvent evt = (FindBuddySuccessEvent) event;
					sendSMSMessage(evt.getFoundBuddy(), message, listener);
				}else{
					listener.fireEevent(event);
				}
			}
		};
		//开始查找好友请求
		this.findBuddyByMobile(mobile, tmpListener);
		
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
	public void setPersonalInfo(ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().setPesonalInfo(listener);
	}
	
	
	/**
	 * 以手机号码查找好友
	 * 
	 * 因为飞信权限的原因，直接去遍历手机好友是不行的，因为部分好友设置为对方看不见好友的手机号码，
	 * 这里需要发起一个获取好友信息的请求然后返回user-id，用这个user-id去遍历好友列表就可以查询到好友
	 * @param mobile	手机号码
	 * @param listener  操作结果监听器
	 */
	public void findBuddyByMobile(long mobile, ActionEventListener listener)
	{
		//先判断是否是合法的移动号码
		if(!Validator.validateMobile(mobile)){
			if(listener!=null){
				listener.fireEevent(new FailureEvent(FailureType.INVALID_CMCC_MOBILE));
			}
		}
		
		//可能部分好友已经获取到了手机号，特别是没有开通飞信的好友手机号码就是已知的
		//为了提高效率，这里先遍历好友，查看是否有好友的手机号码和给定的号码相同
		Iterator<Buddy> it = this.getFetionStore().getBuddyList().iterator();
		while(it.hasNext()){
			Buddy buddy = it.next();
			if(buddy.getMobile()==mobile){	//找到了好友，直接通知监听器
				if(listener!=null){
					listener.fireEevent(new FindBuddySuccessEvent(buddy));
					return;
				}
			}
		}
		
		//仍然没找到，这才向服务器发起查询
		this.dialogFactory.getServerDialog().findBuddyByMobile(mobile, listener);
	}
	
	/**
	 * 设置好友本地姓名
	 * @param buddy		 好友
	 * @param localName	 本地姓名
	 * @param listener
	 */
	public void setBuddyLocalName(Buddy buddy,String localName, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().setBuddyLocalName(buddy, localName, listener);
	}
	
	/**
	 * 移动好友到多个分组
	 * 飞信好友分组有点奇怪，一个好友可以属于多个分组，谁想出的这个需求····
	 * @param buddy			好友对象
	 * @param cordIds		多个好友分组列表
	 * @param listener
	 */
	public void setBuddyCord(Buddy buddy, Collection<Cord> cordList, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().setBuddyCord(buddy, cordList, listener);
	}
	
	/**
	 * 移动好友到单个分组
	 * @param buddy		好友对象
	 * @param cordIds	单个好友分组对象
	 * @param listener
	 */
	public void setBuddyCord(Buddy buddy, Cord cord, ActionEventListener listener)
	{
		ArrayList<Cord> list = new ArrayList<Cord>();
		list.add(cord);
		this.dialogFactory.getServerDialog().setBuddyCord(buddy, list, listener);
	}
	
	/**
	 * 添加飞信好友
	 * @param mobile		手机号码
	 * @param listener		结果监听器
	 */
	public void addBuddy(long mobile, ActionEventListener listener)
	{
		if(!Validator.validateMobile(mobile)){
			if(listener!=null)
				listener.fireEevent(new FailureEvent(FailureType.INVALID_CMCC_MOBILE));
		}else{
			this.dialogFactory.getServerDialog().addBuddy("tel:"+Long.toString(mobile), 0, 1, user.getNickName(), listener);
		}
	}
	
	/**
	 * 删除好友
	 * @param buddy		需删除的好友
	 * @param listener
	 */
	public void deleteBuddy(Buddy buddy, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().deleteBuddy(buddy, listener);
	}
	
	/**
	 * 同意对方添加好友
	 * @param buddy			飞信好友
	 * @param listener
	 */
	public void agreedApplication(Buddy buddy, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().agreedApplication(buddy, listener);
	}
	
	/**
	 * 拒绝对方添加请求
	 * @param buddy			飞信好友
	 * @param listener
	 */
	public void declinedApplication(Buddy buddy, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().declinedAppliction(buddy, listener);
	}
	
	
	/**
	 * 设置用户状态
	 * @param presence		用户状态定义在Presence中
	 * @param listener
	 */
	public void setPresence(int presence, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().setPresence(presence, listener);
	}
	
	/**
	 * 获取好友的详细信息
	 * @param buddy		飞信好友，只能是飞信好友
	 * @param listener
	 */
	public void getBuddyDetail(FetionBuddy buddy, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().getBuddyDetail(buddy, listener);
	}
	
	/**
	 * 批量获取获取好友的详细信息
	 * 注意：如果一次请求的获取好友的详细信息过多，这个方法可能不能完全的返回好友的详细信息，其他结果则通过BN通知返回
	 * 建议一次最多不要获取超过10个好友信息，如果超出10个，建议循环请求
	 * @param buddy		飞信好友列表，只能是飞信好友
	 * @param listener
	 */
	public void getBuddyDetail(Collection<FetionBuddy> buddyList, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().getContactsInfo(buddyList, listener);
	}
	
	/**
	 * 创建新的好友分组
	 * @param title		分组名称
	 * @param listener
	 */
	public void createCord(String title, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().createCord(title, listener);
	}
	
	/**
	 * 删除一个分组
	 * @param cord		分组对象
	 * @param listener
	 */
	public void deleteCord(Cord cord, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().deleteCord(cord, listener);
	}
	
	/**
	 * 设置分组名称
	 * @param cord		分组对象
	 * @param title		分组名称
	 * @param listener
	 */
	public void setCordTitle(Cord cord, String title, ActionEventListener listener)
	{
		this.dialogFactory.getServerDialog().setCordTitle(cord, title, listener);
	}
}
