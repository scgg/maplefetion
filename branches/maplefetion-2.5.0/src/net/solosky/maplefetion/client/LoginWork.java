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
 * File     : LoginWork.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client;


import java.io.IOException;
import java.util.Iterator;
import java.util.TimerTask;

import net.solosky.maplefetion.ClientState;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.LoginState;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.StoreVersion;
import net.solosky.maplefetion.bean.VerifyImage;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.client.dialog.DialogException;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.client.dialog.ServerDialog;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.ActionEventType;
import net.solosky.maplefetion.event.action.ActionEventFuture;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.event.action.FailureEvent;
import net.solosky.maplefetion.event.action.FailureType;
import net.solosky.maplefetion.event.action.FutureActionEventListener;
import net.solosky.maplefetion.event.notify.LoginStateEvent;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.CrushBuilder;
import net.solosky.maplefetion.util.LocaleSetting;
import net.solosky.maplefetion.util.ObjectWaiter;

import org.apache.log4j.Logger;

/**
 *
 * 登录过程
 *
 * @author solosky <solosky772@qq.com>
 */
public class LoginWork implements Runnable
{
	/**
	 * 飞信运行上下文
	 */
	private FetionContext context;

	/**
	 * 用户登录的验证图片信息
	 */
	private VerifyImage verifyImage;
	/**
	 * SSI登录对象
	 */
	private SSISign signAction;
	/**
	 * 当前登录状态
	 */
	private LoginState state;
	
	/**
	 * 用户状态
	 */
	private int presence;
	
	/**
	 * 是否使用SSI登录
	 */
	private boolean isSSISign;
	
	/**
	 * 替换SSIC的定时任务
	 */
	private TimerTask replaceSsicTask;
	
	/**
	 * 同步登陆等待对象
	 */
	private ObjectWaiter<LoginState> loginWaiter;
	/**
	 * LOGGER
	 */
	private static final Logger logger = Logger.getLogger(LoginWork.class);
	
	/**
	 * 构造函数
	 * @param context
	 */
	public LoginWork(FetionContext context, int presence)
	{
		this.context = context;
		this.presence = presence;
		this.signAction = new SSISignV4();
		this.loginWaiter = new ObjectWaiter<LoginState>();
		this.replaceSsicTask = new ReplaceSSICWork();
		this.isSSISign  = true;
		
		this.signAction.setLocaleSetting(this.context.getLocaleSetting());
		this.signAction.setFetionContext(this.context);
	}
	
	
	/**
	 * 尝试登录
	 */
	public void login()
	{
		this.context.updateState(ClientState.LOGGING);
		if( this.updateSystemConfig() && 	//获取自适应配置
			(this.isSSISign ? this.SSISign() : true)  &&  			//SSI登录 			NOTE:去掉SSI登录的过程，也能登录，防止出现验证码
			this.openServerDialog() && 		//服务器连接并验证
			this.getContactsInfo()) {		//获取联系人列表和信息
			boolean groupEnabled = FetionConfig.getBoolean("fetion.group.enable");
			if(groupEnabled) {	//启用了群
				if( this.getGroupsInfo() &&	 	//获取群信息
					this.openGroupDialogs()) {	//建立群会话
					this.updateLoginState(LoginState.LOGIN_SUCCESS);
				}
			}else {
				this.updateLoginState(LoginState.LOGIN_SUCCESS);
			}
		}
		
	}
	

    @Override
    public void run()
    {
    	try {
    		this.login();
    	}catch(Throwable e) {
    		logger.fatal("Unkown login error..", e);
    		this.updateLoginState(LoginState.OTHER_ERROR);
    		CrushBuilder.handleCrushReport(e);
    	}
    }
    
    
    ////////////////////////////////////////////////////////////////////
    /**
     * 更新自适应配置
     */
    private boolean updateSystemConfig()
    {
    	LocaleSetting localeSetting = this.context.getLocaleSetting();
    	if(!localeSetting.isLoaded()) {
    		try {
    			logger.debug("Loading locale setting...");
				this.updateLoginState(LoginState.SEETING_LOAD_DOING);
				localeSetting.load(this.context.getFetionUser());
				this.updateLoginState(LoginState.SETTING_LOAD_SUCCESS);
				return true;
			} catch (Exception e) {
				logger.debug("Load localeSetting error", e);
				this.updateLoginState(LoginState.SETTING_LOAD_FAIL);
				return false;
			}
    	}else {
    		return true;
    	}
    }
    
    /**
     * SSI登录
     */
    private boolean SSISign()
    {
    	this.updateLoginState(LoginState.SSI_SIGN_IN_DOING);
    	if (this.verifyImage == null) {
        	this.state = this.signAction.signIn(this.context.getFetionUser());
        } else {
        	this.state = this.signAction.signIn(this.context.getFetionUser(), this.verifyImage);
        }
		this.updateLoginState(this.state);
		return this.state==LoginState.SSI_SIGN_IN_SUCCESS;
    }
    
    /**
     * 建立服务器会话
     */
    private boolean openServerDialog()
    {
    	//判断是否有飞信号
    	if(this.context.getFetionUser().getFetionId()==0){
    		throw new IllegalArgumentException("Invalid fetion id. if disabled SSI sign, you must login with fetion id..");
    	}
    	
    	this.updateLoginState(LoginState.SIPC_REGISTER_DOING);
		ServerDialog serverDialog = this.context.getDialogFactory().createServerDialog();
		try {
	        serverDialog.openDialog();
	        
	        ActionEventFuture future = new ActionEventFuture();
	    	ActionEventListener listener = new FutureActionEventListener(future);
	    	
	    	//注册服务器
	    	serverDialog.register(presence, listener);
	    	Dialog.assertActionEvent(future.waitActionEventWithException(), ActionEventType.SUCCESS);
	    	
	    	//用户验证
	    	future.clear();
	    	serverDialog.userAuth(presence, listener);
	    	ActionEvent event = future.waitActionEventWithoutException();
	    	if(event.getEventType()==ActionEventType.SUCCESS){
	    		state = LoginState.SIPC_REGISGER_SUCCESS;
	    	}else if(event.getEventType()==ActionEventType.FAILURE){
	    		FailureEvent evt = (FailureEvent) event;
	    		FailureType type =  evt.getFailureType();
	    		if(type==FailureType.REGISTER_FORBIDDEN){
	    			state = LoginState.SIPC_ACCOUNT_FORBIDDEN;	//帐号限制登录，可能存在不安全因素，请修改密码后再登录
	    		}else if(type==FailureType.AUTHORIZATION_FAIL){
	    			state = LoginState.SIPC_AUTH_FAIL;			//登录验证失败
	    		}else{
	    			Dialog.assertActionEvent(event, ActionEventType.SUCCESS);
	    		}
	    	}
	    	
		} catch (TransferException e) {
			logger.warn("serverDialog: failed to connect to server.", e);
			this.state = LoginState.SIPC_CONNECT_FAIL;
		} catch (DialogException e) {
			logger.warn("serverDialog: login failed.", e);
			this.state = LoginState.OTHER_ERROR;
		} catch (RequestTimeoutException e) {
			logger.warn("serverDialog: login request timeout.", e);
			state = LoginState.SIPC_TIMEOUT;
        } catch (InterruptedException e) {
        	logger.warn("serverDialog: login thread interrupted.", e);
        	state = LoginState.OTHER_ERROR;
        } catch (SystemException e) {
        	logger.warn("serverDialog: login system error.", e);
        	state = LoginState.OTHER_ERROR;
		}
    	this.updateLoginState(this.state);
    	
    	return this.state == LoginState.SIPC_REGISGER_SUCCESS;
    }
    
    /**
     * 获取联系人信息
     */
    private boolean getContactsInfo()
    {
    	ActionEventFuture future = new ActionEventFuture();
    	ActionEventListener listener = new FutureActionEventListener(future);
    	ServerDialog dialog = this.context.getDialogFactory().getServerDialog();
    	StoreVersion storeVersion   = this.context.getFetionStore().getStoreVersion();
    	StoreVersion userVersion    = this.context.getFetionUser().getStoreVersion();
    	FetionStore  store          = this.context.getFetionStore();
    	
    	try {
			this.updateLoginState(LoginState.GET_CONTACTS_INFO_DOING);
			
	        
	        //订阅异步通知
    		if(this.context.getFetionStore().getBuddyList().size()>0){
		        future.clear();
		        dialog.subscribeBuddyNotify(listener);
		        Dialog.assertActionEvent(future.waitActionEventWithException(), ActionEventType.SUCCESS);
    		}
	        
	        this.updateLoginState(LoginState.GET_CONTACTS_INFO_SUCCESS);
	        
	        return true;
    	} catch (TransferException e) {
			this.state = LoginState.SIPC_CONNECT_FAIL;
			return false;
		} catch (RequestTimeoutException e) {
			this.state = LoginState.SIPC_TIMEOUT;
			return false;
		}catch (Exception e) {
        	//TODO 这里应该分别处理不同的异常，通知登录监听器的错误更详细点。。暂时就这样了
        	logger.fatal("get contacts info failed.", e); 
        	this.updateLoginState(LoginState.GET_CONTACTS_INFO_FAIL);
        	return false;
        }
    	
    }
    
    /**
     * 获取群信息
     */
    private boolean getGroupsInfo()
    {
    	ActionEventFuture future = new ActionEventFuture();
    	ActionEventListener listener = new FutureActionEventListener(future);
    	ServerDialog dialog = this.context.getDialogFactory().getServerDialog();
    	StoreVersion storeVersion   = this.context.getFetionStore().getStoreVersion();
    	StoreVersion userVersion    = this.context.getFetionUser().getStoreVersion();
    	
    	try {
    		this.updateLoginState(LoginState.GET_GROUPS_INFO_DOING);
	        //获取群列表
	        future.clear();
	        dialog.getGroupList(listener);
	        Dialog.assertActionEvent(future.waitActionEventWithException(), ActionEventType.SUCCESS);
	        
			//如果群列表为空，就不发送下面的一些请求了
			FetionStore store = this.context.getFetionStore();
			if(store.getGroupList().size()==0){
				logger.debug("The group list is empty, group dialog login is skipped.");
				return true;
			}

	        //如果当前存储版本和服务器相同，就不获取群信息和群成员列表，
	        //TODO ..这里只是解决了重新登录的问题，事实上这里问题很大，群信息分成很多
	        //用户加入的群列表 groupListVersion
	        //某群的信息		  groupInfoVersion
	        //群成员列表		  groupMemberListVersion
	        //暂时就这样，逐步完善中.....
	        logger.debug("GroupListVersion: server="+userVersion.getGroupVersion()+", local="+storeVersion.getGroupVersion());
	        if(storeVersion.getGroupVersion()!=userVersion.getGroupVersion()) {
				//更新存储版本
				storeVersion.setGroupVersion(userVersion.getGroupVersion());
    	        //获取群信息
    	        future.clear();
    	        dialog.getGroupsInfo(this.context.getFetionStore().getGroupList(), listener);
    	        Dialog.assertActionEvent(future.waitActionEventWithException(), ActionEventType.SUCCESS);
	        
	        	//获取群成员
    	        future.clear();
    	        dialog.getMemberList(this.context.getFetionStore().getGroupList(), listener);
    	        Dialog.assertActionEvent(future.waitActionEventWithException(), ActionEventType.SUCCESS);
    	        
    	        storeVersion.setGroupVersion(userVersion.getGroupVersion());
	        }
	        
	    	this.updateLoginState(LoginState.GET_GROUPS_INFO_SUCCESS);
	        return true; 
        } catch (TransferException e) {
			this.state = LoginState.SIPC_CONNECT_FAIL;
			return false;
		} catch (RequestTimeoutException e) {
			this.state = LoginState.SIPC_TIMEOUT;
			return false;
        } catch (Exception e) {
        	logger.fatal("get groups info failed.", e); 
        	this.updateLoginState(LoginState.GET_GROUPS_INFO_FAIL);
        	return false;
        }
    }
    
    /**
     * 打开群会话
     */
    private boolean openGroupDialogs()
    {
    	this.updateLoginState(LoginState.GROUPS_REGISTER_DOING);
		Iterator<Group> it = this.context.getFetionStore().getGroupList().iterator();
		try {
	        while (it.hasNext()) {
	        	GroupDialog groupDialog = this.context.getDialogFactory().createGroupDialog(it.next());
	        	groupDialog.openDialog();
	        }
	        
	        this.updateLoginState(LoginState.GROUPS_REGISTER_SUCCESS);
	        return true;
		} catch (TransferException e) {
				this.state = LoginState.SIPC_CONNECT_FAIL;
				return false;
		} catch (RequestTimeoutException e) {
				this.state = LoginState.SIPC_TIMEOUT;
				return false;
        } catch (Exception e) {
        	logger.fatal("open group dialogs failed.", e);
        	this.updateLoginState(LoginState.GROUPS_REGISTER_FAIL);
        	return false;
        }
    }
    
    /**
     * 更新登录状态
     * @param status
     */
    private void updateLoginState(LoginState state)
    {
    	if(this.context.getNotifyEventListener()!=null)
    		this.context.getNotifyEventListener().fireEvent(new LoginStateEvent(state));
    		
    	if(state.getValue()>0x400) {	//大于400都是登录出错
    		this.loginWaiter.objectArrive(state);
    		this.context.handleException(new LoginException(state));
    	}else if(state==LoginState.LOGIN_SUCCESS) {
    		this.loginWaiter.objectArrive(state);
    		this.context.updateState(ClientState.ONLINE);
    		this.context.getDialogFactory().getServerDialog().startKeepAlive();
    		if(this.isSSISign){
	    		int ssicReplaceInterval = FetionConfig.getInteger("fetion.ssi.replace-interval")*1000;
	    		this.context.getFetionTimer().scheduleTask(this.replaceSsicTask, ssicReplaceInterval, ssicReplaceInterval);
    		}
    	}
    }
    ////////////////////////////////////////////////////////////////////

	/**
     * @param verifyImage the verifyImage to set
     */
    public void setVerifyImage(VerifyImage verifyImage)
    {
    	this.verifyImage = verifyImage;
    }

	/**
     * @param presence the presence to set
     */
    public void setPresence(int presence)
    {
    	if(Presence.isValidPresenceValue(presence)) {
    		this.presence = presence;
    	}else {
    		throw new IllegalArgumentException("presence "+presence+" is invalid. Presense const is defined in Presence class.");
    	}
    }
    
    /**
     * @return the presence
     */
    public int getPresence()
    {
    	return presence;
    }


	public void setSSISign(boolean isSSISign) {
		this.isSSISign = isSSISign;
	}


	/**
     * 释放资源
     */
    public void dispose()
    {
    	this.replaceSsicTask.cancel();
    }
    
    /**
     * 等待登陆结果通知
     * 事实上这个方法不会永远超时，因为客户端登陆已经包含了超时控制
     * @return
     */
    public LoginState waitLoginState()
    {
    	try {
			return this.loginWaiter.waitObject();
		} catch (Exception e) {
			return LoginState.OTHER_ERROR;
		}
    }
    
    /**
     * 定时替换SSIC的任务
     */
    public class ReplaceSSICWork extends TimerTask
    {
		@Override
		public void run()
		{
			try {
				logger.debug("Replacing ssic...");
				HttpApplication.replaceSsic(context.getFetionUser(), context.getLocaleSetting());
				logger.debug("Replaced ssic:"+context.getFetionUser().getSsic());
			} catch (IOException e) {
				logger.warn("replaceSsic failed." , e);
			}
		}
    	
    }
}