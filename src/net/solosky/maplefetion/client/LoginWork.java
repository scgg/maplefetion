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


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import net.solosky.maplefetion.ClientState;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.LoginState;
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
import net.solosky.maplefetion.util.CrushBuilder;
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
	private LoginState state;
	
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
		this.context.updateState(ClientState.LOGGING);
		if( this.updateSystemConfig() && 	//获取自适应配置
			this.SSISign()  &&  			//SSI登录
			this.openServerDialog() && 		//服务器连接并验证
			this.getContactsInfo()) {		//获取联系人列表和信息
			boolean groupEnabled = FetionConfig.getBoolean("fetion.group.enabled");
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
    		logger.fatal("Unkown login error..",e);
    		this.updateLoginState(LoginState.OHTER_ERROR);
    		//建立错误日志
        	if(FetionConfig.getBoolean("crush.build")) {
        		DateFormat df = new SimpleDateFormat("y.M.d.H.m.s");
        		String name = "MapleFetion-CrushReport-["+df.format(new Date())+"].txt";
        		try {
    	            CrushBuilder.buildAndSaveCrushReport(e, new File(name));
                } catch (IOException ex) {
                	logger.warn("build crush report failed.",ex);
                }
        	}
    	}
    }
    
    
    ////////////////////////////////////////////////////////////////////
    
    /**
     * 更新自适应配置
     */
    private boolean updateSystemConfig()
    {
    	if(!this.isConfigFetched) {
    		try {
    			logger.debug("Loading locale setting...");
				this.updateLoginState(LoginState.SEETING_LOAD_DOING);
				Document doc = LocaleSettingHelper.load(this.context.getFetionUser());
				LocaleSettingHelper.active(doc);
				this.updateLoginState(LoginState.SETTING_LOAD_SUCCESS);
				this.isConfigFetched = true;
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
    	this.updateLoginState(LoginState.SIPC_REGISTER_DOING);
		ServerDialog serverDialog = this.context.getDialogFactory().createServerDialog();
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
	    	
	    	state = LoginState.SIPC_REGISGER_SUCCESS;
		} catch (TransferException e) {
			logger.warn("serverDialog: failed to connect to server.", e);
			this.state = LoginState.SIPC_CONNECT_FAIL;
		} catch (DialogException e) {
			logger.warn("serverDialog: login failed.", e);
			this.state = LoginState.OHTER_ERROR;
		} catch (RequestTimeoutException e) {
			logger.warn("serverDialog: login request timeout.", e);
			state = LoginState.SIPC_TIMEOUT;
        } catch (InterruptedException e) {
        	logger.warn("serverDialog: login thread interrupted.", e);
        	state = LoginState.OHTER_ERROR;
        }
    	this.updateLoginState(this.state);
    	
    	return this.state == LoginState.SIPC_REGISGER_SUCCESS;
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
    		this.updateLoginState(LoginState.GET_CONTACTS_INFO_DOING);
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
	        
	        this.updateLoginState(LoginState.GET_CONTACTS_INFO_SUCCESS);
	        
	        return true;
        } catch (Exception e) {
        	//TODO 这里应该分别处理不同的异常，通知登录监听器的错误更详细点。。暂时就这样了
        	logger.fatal("get contacts info failed.", e); 
        	this.updateLoginState(LoginState.OHTER_ERROR);
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
    		this.updateLoginState(LoginState.GET_GROUPS_INFO_DOING);
	        //获取群列表
	        future.clear();
	        dialog.getGroupList(listener);
	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
	        
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
    	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
	        
	        	//获取群成员
    	        future.clear();
    	        dialog.getMemberList(this.context.getFetionStore().getGroupList(), listener);
    	        Dialog.assertStatus(future.waitStatus(), ActionStatus.ACTION_OK);
    	        
    	        storeVersion.setGroupVersion(userVersion.getGroupVersion());
	        }
	        
	    	this.updateLoginState(LoginState.GET_GROUPS_INFO_SUCCESS);
	        return true;
        } catch (Exception e) {
        	//TODO 这里应该分别处理不同的异常，通知登录监听器的错误更详细点。。暂时就这样了
        	logger.fatal("get groups info failed.", e); 
        	this.updateLoginState(LoginState.OHTER_ERROR);
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
        } catch (Exception e) {
        	logger.fatal("open group dialogs failed.", e);
        	this.updateLoginState(LoginState.OHTER_ERROR);
        	return false;
        }
    }
    
    /**
     * 更新登录状态
     * @param status
     */
    private void updateLoginState(LoginState state)
    {
    	if(this.context.getLoginListener()!=null)
    		this.context.getLoginListener().loginStateChanged(state);
    	
    	if(state.getValue()>0x400) {	//大于400都是登录出错
    		this.context.updateState(ClientState.LOGIN_ERROR);
    		this.context.getLoginWaiter().objectArrive(state);
    	}else if(state==LoginState.LOGIN_SUCCESS) {
    		this.context.updateState(ClientState.ONLINE);
    		this.context.getLoginWaiter().objectArrive(state);
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