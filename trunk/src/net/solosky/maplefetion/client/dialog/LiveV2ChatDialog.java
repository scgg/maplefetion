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
 * Package  : net.solosky.maplefetion.client.dialog
 * File     : LiveV2ChatDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-14
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import java.util.ArrayList;
import java.util.Iterator;

import net.solosky.maplefetion.ExceptionHandler;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.chain.ProcessorChain;
import net.solosky.maplefetion.client.dispatcher.LiveV2MessageDispatcher;
import net.solosky.maplefetion.client.response.DefaultResponseHandler;
import net.solosky.maplefetion.net.Port;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.Transfer;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.net.TransferFactory;
import net.solosky.maplefetion.net.TransferService;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcNotify;
import net.solosky.maplefetion.sipc.SipcOutMessage;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.sipc.SipcResponse;
import net.solosky.maplefetion.sipc.SipcStatus;
import net.solosky.maplefetion.util.BuddyEnterHelper;
import net.solosky.maplefetion.util.MessageLogger;
import net.solosky.maplefetion.util.ResponseFuture;
import net.solosky.maplefetion.util.SipcParser;
import net.solosky.maplefetion.util.TicketHelper;

import org.apache.log4j.Logger;

/**
 * 
 * 第二版和在线好友聊天对话框
 * 
 * @author solosky <solosky772@qq.com>
 */
public class LiveV2ChatDialog extends ChatDialog implements MutipartyDialog, ExceptionHandler
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
	 * 好友进入等待对象
	 */
	private BuddyEnterHelper buddyEnterHelper;
	
	/**
	 * 邀请通知
	 */
	private SipcNotify inviteNotify;
	
	/**
	 * 所有进入这个对话框的好友列表
	 */
	private ArrayList<Buddy> buddyList;
	
	/**
	 * LOGGER
	 */
	private static Logger logger = Logger.getLogger(LiveV2ChatDialog.class);
	
	/**
	 * @param mainBuddy
	 * @param client
	 */
	public LiveV2ChatDialog(Buddy mainBuddy, FetionContext client)
	{	
		super(mainBuddy, client);
		this.messageFactory   = new MessageFactory(client.getFetionUser());
		this.buddyEnterHelper = new BuddyEnterHelper();
		this.buddyList        = new ArrayList<Buddy>();
	}

	/**
	 * 以一个邀请通知构建
	 * @param inviteNotify
	 * @param client
	 */
	public LiveV2ChatDialog(SipcNotify inviteNotify, FetionContext client)
	{
		super(client);
		this.inviteNotify   = inviteNotify;
		this.mainBuddy      = client.getFetionStore().getBuddyByUri(inviteNotify.getFrom());
		this.messageFactory = new MessageFactory(client.getFetionUser());
		this.buddyEnterHelper = new BuddyEnterHelper();
		this.buddyList        = new ArrayList<Buddy>();
	}
	
	/**
	 * 建立处理链
	 * 
	 * @param host
	 * @param port
	 * @throws FetionException 
	 * @throws TransferException
	 */
	private void buildProcessorChain(Transfer transfer) throws FetionException
	{

		TransferService transferService = new TransferService();
		
		this.processorChain = new ProcessorChain();
		this.processorChain.addLast(new LiveV2MessageDispatcher(context, this, this)); 				// 消息分发服务
		if(FetionConfig.getBoolean("log.sipc.enable"))
			this.processorChain.addLast(new MessageLogger("LiveV2ChatDialog-" + mainBuddy.getFetionId())); 								// 日志记录
		this.processorChain.addLast(transferService); 													// 传输服务
		this.processorChain.addLast(new SipcParser());													//信令解析器
		this.processorChain.addLast(transfer);															//传输对象
		
		this.processorChain.startProcessorChain();
		
		this.context.getGlobalTimer().schedule(transferService.getTimeOutCheckTask(), 0, 60*1000);
	}
	
	/**
	 * 尝试建立连接，在可用的端口建立连接，直到建立一个可用的连接
	 * @param portList
	 * @return
	 */
	private Transfer buildTransfer(ArrayList<Port> portList)
	{
		TransferFactory factory = this.context.getTransferFactory();
		Transfer transfer = null;
		Port     port     = null;
		Iterator<Port> it = portList.iterator();
		while(it.hasNext()) {
			port = it.next();
			//尝试建立连接
			try {
				logger.trace("try to connect to port = "+port+" ..");
	            transfer = factory.createTransfer(port);
            } catch (TransferException e) {
            	logger.trace("Connect to port failed - Port = "+port, e);
            }
            
            //如果建立成功就跳出循环，否则继续尝试建立下一个端口的连接
			if(transfer!=null) {
				logger.trace("Transfer created success. - Transfer="+transfer.getTransferName());
				break;
			}
		}
		return transfer;
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
        	if(out instanceof SipcRequest) {
        		SipcRequest request = (SipcRequest) out;
        		if(request.getResponseHandler()!=null)
        			request.getResponseHandler().ioerror(request);
        	}
        }
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
     * 用户是否被邀请进入这个对话框的
     * @return
     */
    public boolean isBeenInvited()
    {
    	return this.inviteNotify!=null;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.ChatDialog#isMutipartySupported()
     */
    @Override
    public boolean isMutipartySupported()
    {
	    return false;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.ChatDialog#sendChatMessage(java.lang.String, net.solosky.maplefetion.client.dialog.ActionListener)
     */
    @Override
    public void sendChatMessage(Message message, ActionListener listener)
    {
    	 this.ensureOpened();
    	 SipcRequest request = this.messageFactory.createSendChatMessageRequest(this.mainBuddy.getUri(), message);
  	   	 request.setResponseHandler(new DefaultResponseHandler(listener));
  	   	 this.process(request);
  	   	 this.updateActiveTime();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#closeDialog()
     */
    @Override
    public synchronized void closeDialog()
    {
    	try {
    		//TODO NOTE:如果发生了传输异常这里就不应该发送离开消息，否则会抛出第二个TransferException
        	if(this.processorChain!=null && !this.processorChain.isChainClosed()) {
        		this.bye();
        		this.processorChain.stopProcessorChain();
        	}
        } catch (FetionException e) {
        	logger.warn("closeLiveV2ChatDialog failed.", e);
        }
        this.setState(DialogState.CLOSED);
    }

	/**
	 * 打开第二版聊天对话框，这个比较麻烦
	 */
    @Override
    public synchronized void openDialog() throws TransferException, DialogException, RequestTimeoutException
    {
    	//检查对话状态，防止多次打开一个对话
    	if(this.getState()==DialogState.CREATED) {
			this.setState(DialogState.OPENNING);
		}else {
			return;
		}
    	
    	try {
    		this.setState(DialogState.OPENNING);
    		//首先要获取进入聊天服务器的凭证
    		String ticket = null;
    		if(this.isBeenInvited()) {
    			//如果被邀请，进入凭证是在邀请通知里
    			ticket = this.inviteNotify.getHeader(SipcHeader.AUTHORIZATION).getValue();
    		}else {
    			//如果不是，用户主动发起会话请求，需要向服务器获取进入凭证
    			ticket = this.context.getDialogFactory().getServerDialog().startChat();
    		}
    		TicketHelper helper = new TicketHelper(ticket);
    		
    		//然后连接聊天服务器，建立处理链
    		Transfer transfer = this.buildTransfer(helper.getPortList());
    		if(transfer==null) throw new TransferException("Cannot connect to chat server.");
    		this.buildProcessorChain(transfer);
    		
    		//发送注册信息
    		this.register(helper.getCredential());
    		
    		//如果是主动邀请,邀请好友进入对话框
    		if(!this.isBeenInvited())
    			this.invite();
    		//等待好友进入对话框
    		this.buddyEnterHelper.waitBuddyEnter(this.mainBuddy);
    		
    		//对话框建立成功
    		this.setState(DialogState.OPENED);
    		
        }catch (TransferException te) {        	//传输异常，直接抛出
        	this.setState(DialogState.FAILED);
        	throw te;
        }catch (DialogException de) {			//对话框异常，直接抛出
        	this.setState(DialogState.FAILED);
			throw de;
		}catch (RequestTimeoutException re) {	//请求超时
			this.setState(DialogState.FAILED);
			throw re;
		}catch (InterruptedException ie) {		//等待被中断
			this.setState(DialogState.FAILED);
			throw new DialogException("Wait response interrupted.");
		} catch (FetionException fe) {			//其他异常，也抛出
			this.setState(DialogState.FAILED);
	        throw new DialogException(fe);		
        }
    }
    
    
    /**
     * 注册服务器
     * @throws InterruptedException 
     * @throws RequestTimeoutException 
     * @throws IllegalResponseException 
     * @throws TransferException 
     */
    private void register(String credential) throws RequestTimeoutException, InterruptedException, IllegalResponseException, TransferException
    {
    	//发送注册信息
		SipcRequest request = this.messageFactory.createRegisterChatRequest(credential);
		ResponseFuture future = ResponseFuture.wrap(request);
		this.process(request);
		SipcResponse response = future.waitResponse();
		assertStatus(response.getStatusCode(), SipcStatus.ACTION_OK);
    }
    
    
    /**
     * 邀请好友进入对话框
     * @param buddy
     * @throws IllegalResponseException
     * @throws RequestTimeoutException
     * @throws InterruptedException
     * @throws TransferException 
     */
    private void invite() throws IllegalResponseException, RequestTimeoutException, InterruptedException, TransferException
    {
    	ActionFuture future = new ActionFuture();
    	this.inviteBuddy(this.mainBuddy, new FutureActionListener(future));
    	int status = future.waitStatus();
    	assertStatus(status, SipcStatus.ACTION_OK);
    }
    
    /**
     * 离开对话
     * @throws TransferException 
     */
    private void bye() throws TransferException
    {
    	SipcRequest request = this.getMessageFactory().createLogoutRequest(this.mainBuddy.getUri());
    	this.process(request);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.MutipartyDialog#getBuddyByUriList()
     */
    @Override
    public ArrayList<Buddy> getBuddyList()
    {
    	return this.buddyList;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.MutipartyDialog#inviteBuddy(net.solosky.maplefetion.bean.FetionBuddy, net.solosky.maplefetion.client.dialog.ActionListener)
     */
    @Override
    public void inviteBuddy(Buddy buddy, ActionListener listener) throws TransferException
    {
	   SipcRequest request = this.messageFactory.createInvateBuddyRequest(this.mainBuddy.getUri());
	   request.setResponseHandler(new DefaultResponseHandler(listener));
   	   this.process(request);
    }

	/**
	 * 异常回调函数
	 * 注意：这里的异常不是在调用者调用方法的时候发生的，而是在传输对象读取一个接受消息时产生的异常，是由客户端内部产生的
	 * 比如读取数据时产生传输异常，或者处理异步通知的时发生的异常均由这个方法处理，如果调用者调用某个方法，一般只发生传输异常
	 * 
	 * 如：
	 *	//首先要创建对话框
	 * try{
	 * 		Buddy buddy = client.getFetionStore().getBuddyByUri(uri);
	 *  	ChatDialog dialog = client.getDialogFactory().createChatDialog(buddy);
	 *      dialog.openDialog();
	 *   	dialog.sendChatMessage(msg, actionListener);
	 *   	//其他操作
	 *   }catch(TransferException e){
	 *   	//发生了传输异常
	 *   }finally{
	 *   	client.getDialogFactory.closeDialog(dialog);
	 *   }
	 */
    @Override
    public void handleException(FetionException e)
    {
    	//主要处理传输异常
    	if(e instanceof TransferException) {
    		try {
    			this.processorChain.stopProcessorChain();
	            this.context.getDialogFactory().closeDialog(this);
            } catch (FetionException e1) {
            	logger.warn("close LiveV2ChatDialog failed.", e1);
            }
    	}
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#getMessageFactory()
     */
    @Override
    public MessageFactory getMessageFactory()
    {
	    return this.messageFactory;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.MutipartyDialog#buddyEntered(java.lang.String)
     */
    @Override
    public void buddyEntered(String uri)
    {
    	Buddy buddy = this.context.getFetionStore().getBuddyByUri(uri);
    	if(buddy!=null) {
    		this.buddyEnterHelper.buddyEntered(buddy);
    		this.buddyList.add(buddy);
    	}
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.MutipartyDialog#buddyLeft(java.lang.String)
     */
    @Override
    public void buddyLeft(String uri)
    {
    	//如果是当前对话框的所有者离开，关闭这个对话框
    	if(uri.equals(this.mainBuddy.getUri())) {
    		try {
	            this.context.getDialogFactory().closeDialog(this);
            } catch (TransferException e) {
            	this.handleException(e);
            } catch (DialogException e) {
            	logger.warn("Close LiveV2ChatDialog failed.", e);
            }
    	}else {		//移除对应离开的好友
    		Iterator<Buddy> it = this.buddyList.iterator();
	    	while(it.hasNext()) {
	    		Buddy b = it.next();
	    		if(b.getUri().endsWith(uri)) {
	    			it.remove();
	    			break;
	    		}
	    	}
    	}
    }
    
    public String toString()
    {
    	return "[LiveV2ChatDialog - " +
    			"Transfer=" +((Transfer) processorChain.getProcessor(Transfer.class.getName())).getTransferName()+
    			", MainBuddy= "+mainBuddy.getDisplayName()+", "+mainBuddy.getUri()+" ]";
    }
}
