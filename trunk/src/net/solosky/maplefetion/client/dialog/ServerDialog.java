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

import net.solosky.maplefetion.ClientState;
import net.solosky.maplefetion.ExceptionHandler;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.chain.ProcessorChain;
import net.solosky.maplefetion.client.SystemException;
import net.solosky.maplefetion.client.dispatcher.ServerMessageDispatcher;
import net.solosky.maplefetion.client.response.AddBuddyResponseHandler;
import net.solosky.maplefetion.client.response.AddMobileBuddyResponseHandler;
import net.solosky.maplefetion.client.response.AgreeApplicationResponseHandler;
import net.solosky.maplefetion.client.response.CreateCordResponseHandler;
import net.solosky.maplefetion.client.response.DefaultResponseHandler;
import net.solosky.maplefetion.client.response.DeleteBuddyResponseHandler;
import net.solosky.maplefetion.client.response.DeleteCordResponseHandler;
import net.solosky.maplefetion.client.response.FindBuddyByMobileResponseHandler;
import net.solosky.maplefetion.client.response.GetContactDetailResponseHandler;
import net.solosky.maplefetion.client.response.GetContactListResponseHandler;
import net.solosky.maplefetion.client.response.GetContactsInfoResponseHander;
import net.solosky.maplefetion.client.response.GetGroupListResponseHandler;
import net.solosky.maplefetion.client.response.GetGroupsInfoResponseHandler;
import net.solosky.maplefetion.client.response.GetMemberListResponseHandler;
import net.solosky.maplefetion.client.response.GetPersonalInfoResponseHandler;
import net.solosky.maplefetion.client.response.SendChatMessageResponseHandler;
import net.solosky.maplefetion.client.response.ServerRegisterResponseHandler;
import net.solosky.maplefetion.client.response.SetBuddyInfoResponseHandler;
import net.solosky.maplefetion.client.response.SetCordTitleResponseHandler;
import net.solosky.maplefetion.client.response.SetPresenceResponseHandler;
import net.solosky.maplefetion.client.response.UserAuthResponseHandler;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.ActionEventType;
import net.solosky.maplefetion.event.action.ActionEventListener;
import net.solosky.maplefetion.event.action.FailureEvent;
import net.solosky.maplefetion.event.action.FailureType;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferService;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.util.ResponseFuture;
import net.solosky.maplefetion.util.SipcLogger;
import net.solosky.maplefetion.util.SipcParser;

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
	 * 保持在线的定时任务
	 */
	private TimerTask keepAliveTask;
	
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
    	this.keepAliveTask.cancel();
    	this.context.getFetionTimer().clearCanceledTask();
    	
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
    	//检查对话状态，防止多次打开一个对话
    	if(this.getState()==DialogState.CREATED) {
			this.setState(DialogState.OPENNING);
		}else {
			return;
		}
    	
    	try {
        	//建立处理链
        	this.buildProcessorChain();
    		
    		//注册定时任务
        	this.keepAliveTask = new ServerKeepLiveTask();
        	int keepInterval = FetionConfig.getInteger("fetion.sip.keep-alive-interval")*1000;
    		this.context.getFetionTimer().scheduleTask(this.keepAliveTask, keepInterval, keepInterval);
    		
    		//设置对话框状态为打开状态
    		this.setState(DialogState.OPENED);
    		
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
		this.processorChain = new ProcessorChain();
		this.processorChain.addLast(new ServerMessageDispatcher(context, this, this));						//消息分发服务
		if(FetionConfig.getBoolean("log.sipc.enable"))
			this.processorChain.addLast(new SipcLogger("ServerDialog-"+this.context.getFetionUser().getUserId()));									//日志记录
		this.processorChain.addLast(new TransferService(this.context));															//传输服务
		this.processorChain.addLast(new SipcParser());															//信令解析器
		this.processorChain.addLast(this.context.getTransferFactory().createDefaultTransfer());				//信令传输对象
		
		this.processorChain.startProcessorChain();
		
    }
    
    /**
     * 处理异常
     */
    @Override
    public void handleException(FetionException e)
    {
    	if(e instanceof TransferException) {
        	try {
	            this.processorChain.stopProcessorChain(e);
            } catch (FetionException fe) {
            	logger.warn("closeProcessorChain failed.", fe);
            }
            if( this.context.getState()==ClientState.ONLINE ) {
                logger.fatal("ServerDialog fatal error, close the client, please try to login again.");
            	this.context.handleException(e);
            }
    	}else if(this.context.getState()==ClientState.LOGGING){
    		logger.fatal("ServerDialog login error, close the client...");
    		this.context.handleException(e);
    	}else if(e instanceof SystemException) {
    		logger.fatal("ServerDialog system error");
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
	public void register(int presence, ActionEventListener listener)
	{
		SipcRequest request = this.getMessageFactory().createServerRegisterRequest(presence, 
					this.context.getTransferFactory().isMutiConnectionSupported());
		request.setResponseHandler(new ServerRegisterResponseHandler(context, this, listener));
		this.process(request);
		
	}
	
	/**
	 * 验证用户
	 * @param presence		登录状态
	 * @param listener
	 */
	public void userAuth(int presence, ActionEventListener listener) 
	{
		String nonce = (String) this.session.getAttribute("NONCE");
		SipcRequest request = this.getMessageFactory().createUserAuthRequest(nonce, presence, 
				this.context.getTransferFactory().isMutiConnectionSupported());
		request.setResponseHandler(new UserAuthResponseHandler(context, this, listener));
		this.process(request);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取个人信息
	 * @param listener 消息监听器
	 */
	public void getPersonalInfo(ActionEventListener listener) 
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
	public void getContactList(ActionEventListener listener)
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
	public void getContactsInfo(Collection<FetionBuddy> buddyList, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetContactsInfoRequest(buddyList);
		request.setResponseHandler(new GetContactsInfoResponseHander(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 订阅异步通知
	 * @throws Exception 
	 */
	public void subscribeBuddyNotify(Collection<Buddy> buddyList, ActionEventListener listener)
	{
		this.ensureOpened();
 	   	SipcRequest request = this.getMessageFactory().createSubscribeRequest(buddyList);
 	   	request.setResponseHandler(new DefaultResponseHandler(listener));
		this.process(request);
	}
	
	/**
	 * 获取群列表
	 */
	public void getGroupList(ActionEventListener listener)
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
	public void getGroupsInfo(Collection<Group> groupList, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.getMessageFactory().createGetGroupInfoRequest(groupList);
		request.setResponseHandler(new GetGroupsInfoResponseHandler(context, this, listener));
		this.process(request);
	}
	
	/**
	 * 获取群成员列表
	 */
	public void getMemberList(Collection<Group> groupList, ActionEventListener listener)
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
        	ActionEventListener listener = new ActionEventListener()
			{
				public void fireEevent(ActionEvent event)
				{
					if(event.getEventType()!=ActionEventType.SUCCESS){
						logger.fatal("ServerDialog keepAlive failed. event="+event);
						if(context.getState()==ClientState.ONLINE) {
							context.handleException(new TransferException("Client keepAlive failed."));
						}
					}
				}
			};
        	request.setResponseHandler(new DefaultResponseHandler(listener));
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
	 * @param cord		添加好友的分组，如为null，添加到默认分组
	 * @param desc 		“我是xx” xx：名字
	 * @param promptId	提示信息编号
	 * @return
	 * @throws TransferException 
	 * @throws Exception 
	 */
	public void addBuddy(final String uri, final Cord cord,final String desc, int promptId, final ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createAddBuddyRequest(uri, promptId, cord!=null?cord.getId():-1, desc);
		
		//这里需要建立一个新的监听器进行适配，因为返回的结果可能要进行另外一个操作才能确定操作是否完成
		ActionEventListener tmpListener = new ActionEventListener(){
			public void fireEevent(ActionEvent event){
				if(event.getEventType()==ActionEventType.FAILURE){
					FailureEvent evt = (FailureEvent) event;
					if(evt.getFailureType()==FailureType.USER_NOT_FOUND){
						//表明用户没开通飞信，那就添加手机好友,不要调用用户定义的回调函数
						addMobileBuddy(uri, cord, desc, listener);
					}else{
						//其他情况，直接调用用户的回调函数
						listener.fireEevent(event);
					}
				}else{
					listener.fireEevent(event);
				}
			}
		};
		
		request.setResponseHandler(new AddBuddyResponseHandler(context,this, tmpListener));
    	
    	this.process(request);
	}
	
	
	/**
	 * 添加手机好友
	 * @param uri		好友手机uri(类似tel:159xxxxxxxx)
	 * @param cord		设置添加到那个好友分组，如果传递null就表示放入默认分组
	 * @param desc		“我是xx” xx：名字
	 * @return
	 */
	private void addMobileBuddy(String uri, Cord cord, String desc, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createAddMobileBuddyRequest(uri, cord!=null?cord.getId():-1, desc);
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
    public void sendSMSMessage(Buddy buddy, Message message, ActionEventListener listener)
    {
    	this.ensureOpened();
    	SipcRequest request  = this.getMessageFactory().createSendSMSRequest(buddy.getUri(), message);
    	request.setResponseHandler(new SendChatMessageResponseHandler(context, this, listener));
    	this.process(request);
    }
    
    /**
     * 发送聊天消息
     * @param buddy			消息发往的好友对象
     * @param message		消息正文
     * @param listener		操作监听器
     * @throws TransferException 
     */
    public void sendChatMessage(Buddy buddy, Message message, ActionEventListener listener)
    {
    	this.ensureOpened();
    	SipcRequest request  = this.getMessageFactory().createSendChatMessageRequest(buddy.getUri(), message);
    	request.setResponseHandler(new SendChatMessageResponseHandler(context, this, listener));
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
	public void agreedApplication(final Buddy buddy, final ActionEventListener listener)
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
	public void declinedAppliction(Buddy buddy, ActionEventListener listener)
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
	public void deleteBuddy(Buddy buddy, ActionEventListener listener)
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
	public void setPresence(int presence, ActionEventListener listener)
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
	public void setPesonalInfo(ActionEventListener listener)
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
	public void setBuddyLocalName(Buddy buddy,String localName, ActionEventListener listener)
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
	public void setBuddyCord(Buddy buddy, Collection<Cord> cordList, ActionEventListener listener)
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
	public void getBuddyDetail(FetionBuddy buddy, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createGetContactDetailRequest(buddy.getUri());
		request.setResponseHandler(new GetContactDetailResponseHandler(context, this, listener));
		this.process(request);
	}
	
	
	/**
	 * 创建新的好友分组
	 * @param title		分组名称
	 * @param listener
	 */
	public void createCord(String title, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createCreateCordRequest(title);
		request.setResponseHandler(new CreateCordResponseHandler(context, this, listener));
		this.process(request);
	}
	
	
	/**
	 * 删除分组
	 * @param cord		需删除的的分组
	 * @param listener
	 */
	public void deleteCord(Cord cord, ActionEventListener listener)
	{
		this.ensureOpened();
		Collection<Buddy> list = this.context.getFetionStore().getBuddyListByCord(cord);
		if(list!=null && list.size()>0)
			throw new IllegalArgumentException(cord+" is not empty, please move out the buddies in this cord and try again.");
		SipcRequest request = this.messageFactory.createDeleteCordRequest(cord.getId());
		request.setResponseHandler(new DeleteCordResponseHandler(context,this, listener));
		this.process(request);
	}
	
	
	/**
	 * 设置分组标题
	 * @param cord		需设置分组的对象
	 * @param title		分组标题
	 * @param listener	
	 */
	public void setCordTitle(Cord cord, String title, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createSetCordTitleRequest(cord.getId(), title);
		request.setResponseHandler(new SetCordTitleResponseHandler(context, this, listener));
		this.process(request);
	}
	
	
	/**
	 * 以手机号码查找好友
	 * @param mobile		手机号码
	 * @param listener
	 */
	public void findBuddyByMobile(long mobile, ActionEventListener listener)
	{
		this.ensureOpened();
		SipcRequest request = this.messageFactory.createGetContactDetailRequest("tel:"+mobile);
		request.setResponseHandler(new FindBuddyByMobileResponseHandler(context, this, listener));
		this.process(request);
	}
	
}
