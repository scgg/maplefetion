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
 * Package  : net.solosky.net.maplefetion.client.dialog
 * File     : ServerDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-10
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import java.util.Collection;
import java.util.Iterator;
import java.util.TimerTask;

import net.solosky.maplefetion.ExceptionHandler;
import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.chain.ProcessorChain;
import net.solosky.maplefetion.client.dispatcher.ServerMessageDispatcher;
import net.solosky.maplefetion.client.response.AddBuddyResponseHandler;
import net.solosky.maplefetion.client.response.AddMobileBuddyResponseHandler;
import net.solosky.maplefetion.client.response.AgreeApplicationResponseHandler;
import net.solosky.maplefetion.client.response.DefaultResponseHandler;
import net.solosky.maplefetion.client.response.DeleteBuddyResponseHandler;
import net.solosky.maplefetion.client.response.GetContactDetailResponseHandler;
import net.solosky.maplefetion.client.response.GetContactListResponseHandler;
import net.solosky.maplefetion.client.response.GetContactsInfoResponseHander;
import net.solosky.maplefetion.client.response.GetGroupListResponseHandler;
import net.solosky.maplefetion.client.response.GetGroupsInfoResponseHandler;
import net.solosky.maplefetion.client.response.GetMemberListResponseHandler;
import net.solosky.maplefetion.client.response.GetPersonalInfoResponseHandler;
import net.solosky.maplefetion.client.response.ServerRegisterResponseHandler;
import net.solosky.maplefetion.client.response.SetBuddyInfoResponseHandler;
import net.solosky.maplefetion.client.response.SetPresenceResponseHandler;
import net.solosky.maplefetion.client.response.UserAuthResponseHandler;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferService;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.MessageLogger;
import net.solosky.maplefetion.util.ParseException;
import net.solosky.maplefetion.util.ResponseFuture;

import org.apache.log4j.Logger;


/**
 * 服务器对话框
 * 
 * @author solosky <solosky772@qq.com>
 */
public class ServerDialog extends Dialog implements ExceptionHandler
{	
	/**
	 * 处理链
	 */
	private ProcessorChain processorChain;
	
	/**
	 * 消息工厂
	 */
	private MessageFactory messageFactory;
	
	/**
	 * 心跳包请求任务
	 */
	private TimerTask keepLiveTask;
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(ServerDialog.class);
	/**
	 * 构造函数
	 * @param client	客户端对象
	 * @throws FetionException 
	 */
	public ServerDialog(FetionContext client)
	{
		super(client);
		this.messageFactory = new MessageFactory(client.getFetionUser());
		this.keepLiveTask = new ServerKeepLiveTask();
	}
	
	/**
	 * 返回处理链
     * @return
     */
    public ProcessorChain getProcessorChain()
    {
	    return this.processorChain;
    }
	
	/**
	 * 关闭对话框
	 */
    @Override
    public void closeDialog()
    {
    	//不需要发送任何离开消息，直接关闭对话框即可
    	this.keepLiveTask.cancel();
    	this.context.getGlobalTimer().purge();
    	
    	//停止处理链
    	try {
    		if(!this.processorChain.isChainClosed())
    			this.processorChain.stopProcessorChain();
        } catch (FetionException e) {
        	logger.warn("close ServerDialog failed.",e);
        }
    }

	/**
	 * 打开对话框
	 * @throws TransferException 
	 */
    @Override
    public void openDialog() throws DialogException, TransferException
    {
    	try {
        	//建立处理链
        	this.buildProcessorChain();
    		
    		//注册定时任务
        	int keepInterval = FetionConfig.getInteger("fetion.sip.check-alive-interval")*1000;
    		this.context.getGlobalTimer().schedule(this.keepLiveTask, keepInterval, keepInterval);
    		
    		this.state = STATE_OPENED;
    		} catch (FetionException fe) {
    			
    			if(fe instanceof TransferException) {
    				throw (TransferException) fe;
    			}else {
    				throw new DialogException(fe);
    			}
        }		
		
    }
    
    
    public void buildProcessorChain() throws FetionException
    {
    	TransferService transferService = new TransferService();
    	
		this.processorChain = new ProcessorChain();
		this.processorChain.addLast(new ServerMessageDispatcher(context, this, this));						//消息分发服务
		this.processorChain.addLast(new MessageLogger("ServerDialog"));									//日志记录
		this.processorChain.addLast(transferService);														//传输服务
		this.processorChain.addLast(this.context.getTransferFactory().createDefaultTransfer());				//信令传输对象
		
		this.processorChain.startProcessorChain();
		
		this.context.getGlobalTimer().schedule(transferService.getTimeOutCheckTask(), 50*1000, 60*1000);
    }
    
    /**
     * 处理异常
     */
    @Override
    public void handleException(FetionException e)
    {
    	if(e instanceof TransferException ) {
        	try {
	            this.processorChain.stopProcessorChain();
            } catch (FetionException fe) {
            	logger.warn("closeProcessorChain failed.", fe);
            }
            if( this.context.getState()==FetionClient.STATE_ONLINE ) {
                logger.fatal("ServerDialog fatal error, close the client, please try to login again.", e);
            	this.context.handleException(e);
            }
    	}else if(this.context.getState()==FetionClient.STATE_LOGGING){
    		logger.fatal("ServerDialog login error, close the client...", e);
    		this.context.handleException(e);
    	}else {
    		logger.warn("ServerDialog exception, it may not fatal error, ignore it.", e);
    	}
    }

    /**
     * 发送数据包
     */
    @Override
    public void process(SipcOutMessage out)
    {
    	try {
	        this.processorChain.getFirst().processOutcoming(out);
        } catch (FetionException e) {
        	this.handleException(e);
        	//如果是传输异常，就将其抛出，让调用者处理传输错误
        	if(out instanceof SipcRequest) {
        		SipcRequest request = (SipcRequest) out;
        		if(request.getResponseHandler()!=null)
        			request.getResponseHandler().ioerror(request);
        	}
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
    /**
	 * 注册服务器
	 * @param presence		登录状态
	 * @param listener
	 */
	public void register(int presence, ActionListener listener)
	{
		SipcRequest request = this.getMessageFactory().createServerRegisterRequest(presence);
		request.setResponseHandler(new ServerRegisterResponseHandler(context, this, listener));
		this.process(request);
		
	}
	
	/**
	 * 验证用户
	 * @param presence		登录状态
	 * @param listener
	 */
	public void userAuth(int presence, ActionListener listener) 
	{
		String nonce = (String) this.session.getAttribute("NONCE");
		SipcRequest request = this.getMessageFactory().createUserAuthRequest(nonce, presence);
		request.setResponseHandler(new UserAuthResponseHandler(context, this, listener));
		this.process(request);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取个人信息
	 * @param listener 消息监听器
	 */
	public void getPersonalInfo(ActionListener listener) 
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetPersonalInfoRequest();
		request.setResponseHandler(new GetPersonalInfoResponseHandler(context, this, listener));
		this.process(request);
    }
	
	/**
	 * 获取好友列表
	 * @param listener 消息监听器
	 */
	public void getContactList(ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetContactListRequest();
		request.setResponseHandler(new GetContactListResponseHandler(context, this, listener));
		this.process(request);		
	}
	
	/**
	 * 获取联系人详细信息
	 * @return
	 */
	public void getContactsInfo(ActionListener listener) throws TransferException, RequestTimeoutException, InterruptedException, IllegalResponseException, ParseException
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetContactsInfoRequest(context.getFetionStore().getBuddyList());
		request.setResponseHandler(new GetContactsInfoResponseHander(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 订阅异步通知
	 * @throws Exception 
	 */
	public void subscribeBuddyNotify(Collection<Buddy> buddyList, ActionListener listener)
	{
		this.ensureOpened();
 	   	SipcRequest request = this.getMessageFactory().createSubscribeRequest(buddyList);
 	   	request.setResponseHandler(new DefaultResponseHandler(listener));
		this.process(request);
	}
	
	/**
	 * 获取群列表
	 */
	public void getGroupList(ActionListener listener)
	{
		this.ensureOpened();
		FetionStore store = this.context.getFetionStore();
		SipcRequest request = this.getMessageFactory().createGetGroupListRequest(store.getStoreVersion().getGroupVersion());
		request.setResponseHandler(new GetGroupListResponseHandler(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 获取群信息
	 */
	public void getGroupsInfo(Collection<Group> groupList, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetGroupInfoRequest(groupList);
		request.setResponseHandler(new GetGroupsInfoResponseHandler(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 获取群成员列表
	 */
	public void getMemberList(Collection<Group> groupList, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetMemberListRequest((groupList));
		request.setResponseHandler(new GetMemberListResponseHandler(context, this, listener));
		this.process(request);
	}
	

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#getMessageFactory()
     */
    @Override
    public MessageFactory getMessageFactory()
    {
    	return this.messageFactory;
    }
    
    /**
     * 
     * 内部类，实现了定时发送在线请求的任务
     *
     * @author solosky <solosky772@qq.com>
     */
    private class ServerKeepLiveTask extends TimerTask
    {
        @Override
        public void run()
        {
        	SipcRequest request = messageFactory.createKeepAliveRequest();
        	ActionListener listener = new ActionListener()
			{
				public void actionFinished(int status)
				{
					if(status!=ActionStatus.ACTION_OK) {
						logger.warn("ServerDialog keepAlive failed. status="+status);
					}
				}
			};
        	request.setResponseHandler(new DefaultResponseHandler(listener));
	        logger.debug("Sending keeplive request. Request="+request);
	        process(request);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 请求聊天服务器地址
	 * @return
	 * @throws TransferException
	 * @throws RequestTimeoutException
	 * @throws InterruptedException
	 * @throws IllegalResponseException
	 */
	public String startChat() throws TransferException, RequestTimeoutException, InterruptedException, IllegalResponseException
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createStartChatRequest();
    	ResponseFuture future = ResponseFuture.wrap(request);
    	this.process(request);
    	SipcResponse response = future.waitResponse();
    	assertStatus(response.getStatusCode(), SipcStatus.ACTION_OK);
    	return response.getHeader(SipcHeader.AUTHORIZATION).getValue();
	}
	
	
	/**
	 * 添加好友
	 * 注意：无论是添加飞信好友还是手机好友，都可以使用这个方法，这个方法会自动判断
	 * @param uri		好友手机uri(类似tel:159xxxxxxxx)
	 * @param cordId	添加好友的组编号
	 * @param promptId	提示信息编号
	 * @param desc 		“我是xx” xx：名字
	 * @return
	 * @throws TransferException 
	 * @throws Exception 
	 */
	public void addBuddy(final String uri, final int cordId, int promptId, final String desc, final ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createAddBuddyRequest(uri, promptId, cordId, desc);
		
		//这里需要建立一个新的监听器进行适配，因为返回的结果可能要进行另外一个操作才能确定操作是否完成
		ActionListener tmpListener = new ActionListener(){
			public void actionFinished(int status){
				if(status==ActionStatus.NO_SUBSCRIPTION) {
					//如果返回的是522，表明用户没开通飞信，那就添加手机好友,不要调用用户定义的回调函数
	                addMobileBuddy(uri, cordId, desc, listener);
				}else {
					//其他情况，直接调用用户的回调函数
					listener.actionFinished(status);
				}
			}
		};
		
		request.setResponseHandler(new AddBuddyResponseHandler(context,this, tmpListener));
    	
    	this.process(request);
	}
	
	
	/**
	 * 添加手机好友
	 * @param uri		好友手机uri(类似tel:159xxxxxxxx)
	 * @param cordId	添加好友的组编号
	 * @param desc		“我是xx” xx：名字
	 * @return
	 */
	private void addMobileBuddy(String uri, int cordId, String desc, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createAddMobileBuddyRequest(uri, cordId, desc);
		request.setResponseHandler(new AddMobileBuddyResponseHandler(context, this, listener));
    	
    	this.process(request);
	}
	
    
    /**
     * 发送短信消息
     * @param buddy			消息发往的好友对象
     * @param message		消息正文
     * @param listener		操作监听器
     * @throws TransferException 
     */
    public void sendSMSMessage(Buddy buddy, Message message, ActionListener listener)
    {
    	this.ensureOpened();
    	SipcRequest request  = this.getMessageFactory().createSendSMSRequest(buddy.getUri(), message);
    	request.setResponseHandler(new DefaultResponseHandler(listener));
    	this.process(request);
    }
    
    /**
     * 发送聊天消息
     * @param buddy			消息发往的好友对象
     * @param message		消息正文
     * @param listener		操作监听器
     * @throws TransferException 
     */
    public void sendChatMessage(Buddy buddy, Message message, ActionListener listener)
    {
    	this.ensureOpened();
    	SipcRequest request  = this.getMessageFactory().createSendChatMessageRequest(buddy.getUri(), message);
    	request.setResponseHandler(new DefaultResponseHandler(listener));
    	this.process(request);
    }
    /**
	 * 同意对方添加好友
	 * @param uri			飞信地址
	 * @param localName		修改的姓名
	 * @param cordId		分组编号
	 * @return
     * @throws TransferException 
	 * @throws Exception 
	 */
	public void agreedApplication(final Buddy buddy, final ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createAgreeApplicationRequest(buddy.getUri());
		request.setResponseHandler(new AgreeApplicationResponseHandler(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 拒绝对方添加请求
	 * @param uri
	 * @return
	 */
	public void declinedAppliction(Buddy buddy, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createDeclineApplicationRequest(buddy.getUri());
		request.setResponseHandler(new DefaultResponseHandler(listener));
		this.process(request);
	}
	
	/**
	 * 删除好友
	 * @param uri  
	 * @return
	 * @throws TransferException 
	 */
	public void deleteBuddy(Buddy buddy, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = null;
		if(buddy instanceof FetionBuddy) {
			request = this.messageFactory.createDeleteBuddyRequest(buddy.getUri());
		}else {
			request = this.messageFactory.createDeleteMobileBuddyRequest(buddy.getUri());
		}
		request.setResponseHandler(new DeleteBuddyResponseHandler(context, this, listener, buddy));
		this.process(request);
	}
	
	
	/**
	 * 设置当前用户的在线状态
	 * @param presence		在线状态，定义在Presence里面
	 * @return				成功返回true失败返回false
	 * @throws TransferException 
	 * @throws Exception
	 */
	public void setPresence(int presence, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createSetPresenceRequest(presence);
		request.setResponseHandler(new SetPresenceResponseHandler(context, this, listener, presence));
		this.process(request);
	}
	
	/**
	 * 更新用户个人信息
	 * @param listener
	 */
	public void setPesonalInfo(ActionListener listener)
	{
		this.ensureOpened();
        SipcRequest request = this.messageFactory.createSetPersonalInfoRequest();
        request.setResponseHandler(new DefaultResponseHandler(listener));
        this.process(request);
	}
	
	/**
	 * 设置好友信息本地姓名
	 * @param buddy		好友
	 * @param listener	
	 */
	public void setBuddyLocalName(Buddy buddy,String localName, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createSetBuddyLocalName(buddy.getUri(), localName);
		request.setResponseHandler(new SetBuddyInfoResponseHandler(context, this, listener));
		this.process(request);
	}
	
	
	/**
	 * 设置好友分组
	 * @param buddy
	 * @param cordList		分组编号列表 传入为null或者大小为零就是默认分组
	 * @param listener
	 */
	public void setBuddyCord(Buddy buddy, Collection<Cord> cordList, ActionListener listener)
	{
		//把集合改变成22;12;2这样的字符串
		String cordIds = null;
		if(cordList!=null && cordList.size()>0) {
    		StringBuffer buffer = new StringBuffer();
    		Iterator<Cord> it = cordList.iterator();
    		while(it.hasNext()) {
    			Cord cord = it.next();
    			buffer.append(Integer.toString(cord.getId()));
    			buffer.append(";");
    		}
    		cordIds = buffer.toString();	///这里应该是这样的 1;2;3;3; 多了一个分号
    		cordIds = cordIds.substring(0, cordIds.length()-1);
		}else {
			cordIds = "";	//默认分组
		}
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createSetBuddyCord(buddy.getUri(), cordIds);
		request.setResponseHandler(new SetBuddyInfoResponseHandler(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 获取好友详细信息
	 * @param buddy		只能是飞信好友才能获取详细信息
	 * @param listener
	 */
	public void getBuddyDetail(FetionBuddy buddy, ActionListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createGetContactDetailRequest(buddy.getUri());
		request.setResponseHandler(new GetContactDetailResponseHandler(context, this, listener));
		this.process(request);
		
	}
}
