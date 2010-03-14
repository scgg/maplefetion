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


import java.util.Iterator;

import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.LoginListener;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.StoreVersion;
import net.solosky.maplefetion.bean.VerifyImage;
import net.solosky.maplefetion.client.dialog.ActionFuture;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.Dialog;
import net.solosky.maplefetion.client.dialog.DialogException;
import net.solosky.maplefetion.client.dialog.FutureActionListener;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.client.dialog.ServerDialog;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.LocaleSettingHelper;

import org.apache.log4j.Logger;
import org.jdom.Document;

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
	 * 是否已经获取配置信息
	 */
	private boolean isConfigFetched;

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
	private int status;
	
	/**
	 * 用户状态
	 */
	private int presence;
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
		this.isConfigFetched = false;
		this.presence = presence;
		this.signAction = new SSISignV2();
	}
	
	/**
	 * 尝试登录
	 */
	public void login()
	{
		this.context.updateState(FetionClient.STATE_LOGGING);
		if( this.updateSystemConfig() && 	//获取自适应配置
			this.SSISign()  &&  			//SSI登录
			this.openServerDialog() && 		//服务器连接并验证
			this.getContactsInfo()) {		//获取联系人列表和信息
			boolean groupEnabled = FetionConfig.getBoolean("fetion.group.enabled");
			if(groupEnabled) {	//启用了群
				if( this.getGroupsInfo() &&	 	//获取群信息
					this.openGroupDialogs()) {	//建立群会话
					this.updateLoginStatus(LoginListener.LOGIN_SUCCESS);
				}
			}else {
				this.updateLoginStatus(LoginListener.LOGIN_SUCCESS);
			}
		}
		
	}
	

    @Override
    public void run()
    {
	    this.login();
    }
    
    
    ////////////////////////////////////////////////////////////////////
    
    /**
     * 更新自适应配置
     */
    private boolean updateSystemConfig()
    {
    	if(!this.isConfigFetched) {
    		try {
				this.updateLoginStatus(LoginListener.LOGIN_LOAD_LOCALE_SETTING_DOING);
				Document doc = LocaleSettingHelper.load(this.context.getFetionUser());
				LocaleSettingHelper.active(doc);
				this.updateLoginStatus(LoginListener.LOGIN_LOAD_LOCALE_SETTING_SUCCESS);
				this.isConfigFetched = true;
				return true;
			} catch (Exception e) {
				logger.debug("Load localeSetting error - exception=" + e);
				this.updateLoginStatus(LoginListener.LOGIN_LOCALE_SEETING_CONNECT_FIALED);
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
    	if (this.verifyImage == null) {
        	this.updateLoginStatus(LoginListener.LOGIN_SSI_SIGN_IN_DOING);
        	this.status = this.signAction.signIn(this.context.getFetionUser());
        } else {
        	this.updateLoginStatus(LoginListener.LOGIN_SSI_SEND_VERIFY_CODE_DOING);
        	this.status = this.signAction.signIn(this.context.getFetionUser(), this.verifyImage);
        }
		this.updateLoginStatus(this.status);
		return this.status==LoginListener.LOGIN_SSI_SIGN_IN_SUCCESS;
    }
    
    /**
     * 建立服务器会话
     */
    private boolean openServerDialog()
    {
    	this.updateLoginStatus(LoginListener.LOGIN_SERVER_USER_LOGIN_DOING);
		ServerDialog serverDialog = this.context.getDialogFactory().createServerDialog();
		this.status = LoginListener.LOGIN_SERVER_USER_LOGIN_SUCCESS;
		try {
	        serverDialog.openDialog();
	        
	        ActionFuture future = new ActionFuture();
	    	ActionListener listener = new FutureActionListener(future);
	    	
	    	//注册服务器
	    	serverDialog.register(presence, listener);
	    	Dialog.assertStatus(future.waitStatus(), ActionStatus.NOT_AUTHORIZED);
	    	
	    	//用户验证
	    	future.clear();
	    	serverDialog.userAuth(presence, listener);
	    	Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);	
	        
		} catch (TransferException e) {
			logger.warn("serverDialog: failed to connect to server.", e);
			this.status = LoginListener.LOGIN_SERVER_CONNECT_FAILED;
		} catch (DialogException e) {
			logger.warn("serverDialog: login failed.", e);
			this.status = LoginListener.LOGIN_OHTER_FAILED;
		} catch (RequestTimeoutException e) {
			logger.warn("serverDialog: login request timeout.", e);
			status = LoginListener.LOGIN_SERVER_LOGIN_TIMEOUT;
        } catch (InterruptedException e) {
        	logger.warn("serverDialog: login thread interrupted.", e);
        	status = LoginListener.LOGIN_OHTER_FAILED;
        }
    	this.updateLoginStatus(this.status);
    	
    	return this.status == LoginListener.LOGIN_SERVER_USER_LOGIN_SUCCESS;
    }
    
    /**
     * 获取联系人信息
     */
    private boolean getContactsInfo()
    {
    	ActionFuture future = new ActionFuture();
    	ActionListener listener = new FutureActionListener(future);
    	ServerDialog dialog = this.context.getDialogFactory().getServerDialog();
    	StoreVersion storeVersion   = this.context.getFetionStore().getStoreVersion();
    	StoreVersion userVersion    = this.context.getFetionUser().getStoreVersion();
    	FetionStore  store          = this.context.getFetionStore();
    	
    	try {
    		//获取个人信息
    		logger.debug("PersonalVersion: server="+userVersion.getPersonalVersion()+
    				", local="+storeVersion.getPersonalVersion());
    		if(storeVersion.getPersonalVersion()!=userVersion.getPersonalVersion()) {
    			logger.debug("PersonalVersions were not exactly match, now getting personal details...");
    			
    			//清除存储对象
    			future.clear();
    			dialog.getPersonalInfo(listener);
    			Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
    			storeVersion.setPersonalVersion(userVersion.getPersonalVersion());
    		}
	        
    		//获取联系人信息
    		logger.debug("ContactVersion: server="+userVersion.getContactVersion()+
    				", local="+storeVersion.getContactVersion());
    		if(storeVersion.getContactVersion()!=userVersion.getContactVersion()) {
    			logger.debug("ContactVersions were not exactly match, now getting contact details...");
    			
    	        //获取联系人列表
    	        future.clear();
    			store.clearBuddyList();
    			store.clearCordList();
    	        dialog.getContactList(listener);
    	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
    	        
    	        //获取联系人详细信息
    	        future.clear();
    	        dialog.getContactsInfo(listener);
    	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
    	        
       	        storeVersion.setContactVersion(userVersion.getContactVersion());
    		}
	        
	        //订阅异步通知
	        future.clear();
	        dialog.subscribeBuddyNotify(this.context.getFetionStore().getBuddyList(), listener);
	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
	        
	        return true;
        } catch (Exception e) {
        	//TODO 这里应该分别处理不同的异常，通知登录监听器的错误更详细点。。暂时就这样了
        	logger.fatal("get contacts info failed.", e); 
        	this.updateLoginStatus(LoginListener.LOGIN_OHTER_FAILED);
        	return false;
        }
    	
    }
    
    /**
     * 获取群信息
     */
    private boolean getGroupsInfo()
    {
    	ActionFuture future = new ActionFuture();
    	ActionListener listener = new FutureActionListener(future);
    	ServerDialog dialog = this.context.getDialogFactory().getServerDialog();
    	StoreVersion storeVersion   = this.context.getFetionStore().getStoreVersion();
    	StoreVersion userVersion    = this.context.getFetionUser().getStoreVersion();
    	
    	try {
	        //获取群列表
	        future.clear();
	        dialog.getGroupList(listener);
	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
	        
	        //如果当前存储版本和服务器相同，就不获取群信息和群成员列表，
	        //TODO ..这里只是解决了重新登录的问题，事实上这里问题很大，群信息分成很多
	        //用户加入的群列表 groupListVersion
	        //某群的信息		  groupInfoVersion
	        //群成员列表		  groupMemberListVersion
	        //暂时就这样，逐步完善中.....
	        logger.debug("GroupListVersion: server="+userVersion.getGroupVersion()+", local="+storeVersion.getGroupVersion());
	        if(storeVersion.getGroupVersion()!=userVersion.getGroupVersion()) {
    	        //获取群信息
    	        future.clear();
    	        dialog.getGroupsInfo(this.context.getFetionStore().getGroupList(), listener);
    	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
	        
	        	//获取群成员
    	        future.clear();
    	        dialog.getMemberList(this.context.getFetionStore().getGroupList(), listener);
    	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
    	        
    	        storeVersion.setGroupVersion(userVersion.getGroupVersion());
	        }
	        
	        return true;
        } catch (Exception e) {
        	//TODO 这里应该分别处理不同的异常，通知登录监听器的错误更详细点。。暂时就这样了
        	logger.fatal("get groups info failed.", e); 
        	this.updateLoginStatus(LoginListener.LOGIN_OHTER_FAILED);
        	return false;
        }
    }
    
    /**
     * 打开群会话
     */
    private boolean openGroupDialogs()
    {
    	this.updateLoginStatus(LoginListener.LOGIN_SERVER_GROUP_LOGIN_DOING);
		Iterator<Group> it = this.context.getFetionStore().getGroupList().iterator();
		try {
	        while (it.hasNext()) {
	        	GroupDialog groupDialog = this.context.getDialogFactory().createGroupDialog(it.next());
	        	groupDialog.openDialog();
	        }
	        this.updateLoginStatus(LoginListener.LOGIN_SERVER_GROUP_LOGIN_SUCCESS);
	        return true;
        } catch (Exception e) {
        	logger.fatal("open group dialogs failed.", e);
        	return false;
        }
    }
    
    /**
     * 更新登录状态
     * @param status
     */
    private void updateLoginStatus(int status)
    {
    	if(this.context.getLoginListener()!=null)
    		this.context.getLoginListener().loginStatusUpdated(status);
    	if(status>0x400) {	//大于400都是登录出错
    		this.context.updateState(FetionClient.STATE_LOGOUT);
    	}else if(status==LoginListener.LOGIN_SUCCESS) {
    		this.context.updateState(FetionClient.STATE_ONLINE);
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
    
}
