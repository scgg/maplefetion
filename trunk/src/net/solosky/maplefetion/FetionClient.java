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
import net.solosky.maplefetion.client.dialog.ActionFuture;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.ChatDialogProxy;
import net.solosky.maplefetion.client.dialog.ChatDialogProxyFactory;
import net.solosky.maplefetion.client.dialog.DialogException;
import net.solosky.maplefetion.client.dialog.DialogFactory;
import net.solosky.maplefetion.client.dialog.DialogSession;
import net.solosky.maplefetion.client.dialog.FutureActionListener;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.client.dialog.ServerDialog;
import net.solosky.maplefetion.client.dialog.SessionKey;
import net.solosky.maplefetion.net.AutoTransferFactory;
import net.solosky.maplefetion.net.RequestTimeoutException;
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
	 * 登录监听器
	 */
	private LoginListener loginListener;
	
	/**
	 * 通知监听器
	 */
	private NotifyListener notifyListener;
	
	/**
	 * 客户端状态，尽量使用简单的同步方法
	 */
	private volatile ClientState state;
	
	/**
	 * 登录结果同步对象
	 */
	private ObjectWaiter<LoginState> loginWaiter;
	
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
						LoginListener loginListener,
						FetionTimer timer,
						FetionExecutor executor)
	{
		this(new User(mobileNo, pass, "fetion.com.cn"),
				transferFactory,
				fetionStore, 
				notifyListener,
				loginListener,
				timer,
				executor);
	}
	
	
	/**
	 * 使用默认的传输模式和存储模式构造
	 * @param mobileNo			手机号码
	 * @param pass				密码
	 * @param notifyListener	通知监听器
	 * @param loginListener		登录监听器
	 */
	public FetionClient(long mobileNo, 
						String pass, 
						NotifyListener notifyListener,
						LoginListener loginListener)
	{
		this(new User(mobileNo, pass, "fetion.com.cn"), 
				new AutoTransferFactory(),
				new SimpleFetionStore(), 
				notifyListener, 
				loginListener,
				new ThreadTimer(),
				new SingleExecutor());
	}
	
	/**
	 * 简单的构造函数
	 * @param mobile	用户手机号码
	 * @param pass		用户密码
	 */
	public FetionClient(long mobile, String pass)
	{
		this(mobile, pass, null, null);
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
    					LoginListener loginListener,
    					FetionTimer timer,
						FetionExecutor executor)
    {
    	FetionConfig.init();
    	
    	this.state           = ClientState.LOGOUT;
    	this.user            = user;
    	this.transferFactory = transferFactory;
    	this.store           = fetionStore;
    	this.loginListener   = loginListener;
		this.notifyListener  = notifyListener;
		this.loginWaiter     = new ObjectWaiter<LoginState>();
		this.proxyFactory    = new ChatDialogProxyFactory(this);
		this.timer           = timer;
		this.executor        = executor;
		
		
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
    
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getLoginWaiter()
     */
    @Override
    public ObjectWaiter<LoginState> getLoginWaiter()
    {
	    return this.loginWaiter;
    }
    
    /**
     * 设置登录监听器
     * @param loginListener
     */

    public void setLoginListener(LoginListener loginListener) {
		this.loginListener = loginListener;
	}

    /**
     * 设置通知监听器
     * @param notifyListener
     */
	public void setNotifyListener(NotifyListener notifyListener) {
		this.notifyListener = notifyListener;
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

    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#updateStatus(ClientState)
     */
    public void updateState(ClientState state)
    {
    	this.state = state;
    	if(this.notifyListener!=null)
    		this.notifyListener.clientStateChanged(state);
    }
    
    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#getStatus()
     */
    public ClientState getState()
    {
    	return this.state;
    }
    
    /* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionContext#handleException(net.solosky.maplefetion.FetionException)
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
    			logger.fatal("Unkown registration exception", exception);
    		}
		}else if(exception instanceof SystemException){		//系统错误
			logger.fatal("System error. Please email crush report or log to the author and wait for bug fix, thanks.", exception);
			this.state = ClientState.SYSTEM_ERROR;
			CrushBuilder.handleCrushReport(exception, ((SystemException) exception).getArgs());
    	}else if(exception instanceof LoginException){		//登录错误
    		logger.fatal("Login error. state="+((LoginException)exception).getState().name());
    		this.state = ClientState.LOGIN_ERROR;
    	}else {
    		logger.fatal("Unkown error. Please email crush report or log below to the author and wait for bug fix, thanks.", exception);
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
    	if(this.notifyListener!=null)
    		this.notifyListener.clientStateChanged(this.state);
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
	 * 以验证码登录
     * @param verifyImage 验证图片
     */
    public void login(VerifyImage img)
    {
		this.init();
    	this.loginWork.setVerifyImage(img);
		this.executor.submitTask(this.loginWork);
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
		this.executor.submitTask(this.loginWork);
	}
	
	/**
	 * 客户端异步登录登录
	 */
	public void login()
	{
		this.login(Presence.ONLINE);
	}
	
	/**
	 * 客户端同步登录
	 * @return 登录结果
	 */
	public LoginState syncLogin()
	{
		return this.syncLogin(Presence.ONLINE);
	}
	
	/**
	 * 客户端同步登录
	 * @param presence 登录状态
	 * @return 登录结果
	 */
	public LoginState syncLogin(int presence)
	{
		this.login(presence);
		try {
	        return this.loginWaiter.waitObject();
        } catch (Exception e) {
	        return LoginState.OHTER_ERROR;
        }
	}
	
	/////////////////////////////////////////////用户操作开始/////////////////////////////////////////////
	
	/**
	 * 发送聊天消息，如果好友不在线将会发送到手机
	 * @param toBuddy  发送聊天消息的好友
	 * @param message  需发送的消息
	 * @param listener 操作结果监听器
	 * @throws DialogException 如果对话框建立失败抛出
	 */
	public void sendChatMessage(final Buddy toBuddy, final Message message, final ActionListener listener) throws DialogException
	{
		ChatDialogProxy proxy = this.proxyFactory.create(toBuddy);
		proxy.sendChatMessage(message, listener);
	}
	
	/**
	 * 发送聊天消息，如果好友不在线将会发送到手机
	 * @param toBuddy  发送聊天消息的好友
	 * @param message  需发送的消息
	 * @return 操作结果等待对象， 可以在这个对象上调用waitStatus()等待操作结果
	 * @throws DialogException 如果对话框建立失败抛出
	 */
	public ActionFuture sendChatMessage(Buddy toBuddy, Message message) throws DialogException
	{
		ActionFuture future = new ActionFuture();
		this.sendChatMessage(toBuddy, message, new FutureActionListener(future));
		return future;
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
	 * 给自己发送短信到手机上
	 * @param message  需发送的消息
	 * @param listener 操作监听器
	 */
	public void sendSMSMessageToSelf(Message message, ActionListener listener)
	{
		this.sendSMSMessage(this.getFetionUser(), message, listener);
	}
	
	/**
	 * 通过手机号给好友发送消息，前提是该手机号对应的飞信用户必须已经是好友，否则会发送失败
	 * 这是个同步方法，因为调用了findBuddyByMobile
	 * @param mobile		手机号码
	 * @param message		消息
	 * @return 			操作结果的状态码，定义在ActionStatus中
	 * @throws InterruptedException 	同步出现异常抛出中断异常
	 * @throws RequestTimeoutException 请求超时抛出超时异常
	 * @throws TransferException 		传输失败抛出传输异常
	 * @throws DialogException 		如果对话框建立失败抛出
	 */
	public int sendChatMessage(long mobile,Message message) throws RequestTimeoutException, TransferException, InterruptedException, DialogException
	{
		Buddy buddy = this.findBuddyByMobile(mobile);
		if(buddy!=null) {
    		ActionFuture future  = new ActionFuture();
    		this.sendChatMessage(buddy, message, new FutureActionListener(future));
    		return future.waitStatus();
		}else {
			return ActionStatus.INVALD_BUDDY;
		}
	}
	
	
	/**
	 * 通过手机号给好友发送短信，前提是该手机号对应的飞信用户必须已经是好友，否则会发送失败，同步方法
	 * @param mobile		手机号码
	 * @param message		消息
	 * @return 			操作结果的状态码，定义在ActionStatus中
	 * @throws InterruptedException 	同步出现异常抛出中断异常
	 * @throws RequestTimeoutException 请求超时抛出超时异常
	 * @throws TransferException 		传输失败抛出传输异常
	 */
	public int sendSMSMessage(long mobile, Message message) throws RequestTimeoutException, TransferException, InterruptedException
	{
		Buddy buddy = this.findBuddyByMobile(mobile);
		if(buddy!=null) {
    		ActionFuture future  = new ActionFuture();
    		this.sendSMSMessage(buddy, message, new FutureActionListener(future));
    		return future.waitStatus();
		}else {
			return ActionStatus.INVALD_BUDDY;
		}
	}

	
	
	/**
	 * 以手机号码查找好友
	 * 
	 * 因为飞信权限的原因，直接去遍历手机好友是不行的，因为部分好友设置为对方看不见好友的手机号码，
	 * 这里需要发起一个获取好友信息的请求然后返回user-id，用这个user-id去遍历好友列表就可以查询到好友
	 * 这是个同步方法因为ActionListener只能返回一个状态码，没法返回对象，所以只能把结果放在DialogSession里
	 * 如果设置为异步接口，可能一次进行多个请求，但结果同时只有一个，为了保证程序的正确性，所以这里设置为同步方法
	 * 这个是设计上的缺陷，暂时这样，可能在下一版本更新为事件驱动设计模式
	 * @param mobile	手机号码
	 * @return		如果找到返回好友对象，如果没有找到返回null
	 * @throws InterruptedException 	同步出现异常抛出中断异常
	 * @throws RequestTimeoutException 请求超时抛出超时异常
	 * @throws TransferException 		传输失败抛出传输异常
	 */
	public synchronized Buddy findBuddyByMobile(long mobile) throws RequestTimeoutException, InterruptedException, TransferException
	{
		if(!Validator.validateMobile(mobile))
			throw new IllegalArgumentException(mobile+" is not a valid CMCC mobile number.. ");
		
		ActionFuture future  = new ActionFuture();
		this.dialogFactory.getServerDialog().findBuddyByMobile(mobile, new FutureActionListener(future));
		int status = future.waitStatus();
		if(status==ActionStatus.ACTION_OK) {
			DialogSession session = dialogFactory.getServerDialog().getSession();
    		Buddy buddy = (Buddy)session.getAttribute(SessionKey.FIND_BUDDY_BY_MOBILE_RESULT);
    		return buddy;
		}else {
			return null;
		}
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
	 * 设置好友分组
	 * @param buddy
	 * @param cordIds
	 * @param listener
	 */
	public void setBuddyCord(Buddy buddy, Cord cord, ActionListener listener)
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
	
	/**
	 * 批量获取获取好友的详细信息
	 * 注意：如果一次请求的获取好友的详细信息过多，这个方法可能不能完全的返回好友的详细信息，其他结果则通过BN通知返回
	 * 建议一次最多不要获取超过10个好友信息，如果超出10个，建议循环请求
	 * @param buddy		飞信好友列表，只能是飞信好友
	 * @param listener
	 */
	public void getBuddyDetail(Collection<FetionBuddy> buddyList, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().getContactsInfo(buddyList, listener);
	}
	
	/**
	 * 创建新的好友分组
	 * @param title		分组名称
	 * @param listener
	 */
	public void createCord(String title, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().createCord(title, listener);
	}
	
	/**
	 * 删除一个分组
	 * @param cord		分组对象
	 * @param listener
	 */
	public void deleteCord(Cord cord, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().deleteCord(cord, listener);
	}
	
	/**
	 * 设置分组名称
	 * @param cord		分组对象
	 * @param title		分组名称
	 * @param listener
	 */
	public void setCordTitle(Cord cord, String title, ActionListener listener)
	{
		this.dialogFactory.getServerDialog().setCordTitle(cord, title, listener);
	}
}
