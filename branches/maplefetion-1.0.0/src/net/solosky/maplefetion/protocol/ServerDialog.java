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
 * Project  : MapleFetion
 * Package  : net.solosky.maplefetion.protocol
 * File     : ServerDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.ILoginListener;
import net.solosky.maplefetion.IMessageCallback;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.FetionCord;
import net.solosky.maplefetion.bean.FetionUser;
import net.solosky.maplefetion.net.TransferService;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;
import net.solosky.maplefetion.store.IFetionStore;
import net.solosky.maplefetion.util.ParseHelper;
import net.solosky.maplefetion.util.XMLHelper;

import org.jdom.Element;

/**
 *
 *	服务器对话
 *  有且只有一个服务器对话
 * @author solosky <solosky772@qq.com> 
 */
public class ServerDialog extends AbstractDialog
{

	/**
	 * 保持在线的定时任务
	 */
	private KeepAliveTimerTask keepAliveTask;
	
	/**
     * @param client
	 * @throws Exception 
     */
    public ServerDialog(IFetionClient client, String host, int port) throws Exception
    {
	    super(client);
	    this.transferService = new TransferService(
	    		client.getTransferFactory().createTransfer(host, port),
	    		this.messageListener);
	    this.keepAliveTask =  new KeepAliveTimerTask();
	    
	    this.client.getGlobalTimer().schedule(
	    		this.transferService.getTimeOutCheckTask(), 0, 
	    		FetionConfig.getInteger("fetion.sip.check-alive-interval")*1000);
    }
   
	/**
	 * 关闭对话框的过程就是退出登录过程
	 */
    public void closeDialog() throws Exception
    {
    	//好像服务对话直接关闭就行了。。没有发出任何包
		this.keepAliveTask.cancel();
		this.client.getGlobalTimer().purge();
		this.transferService.stopService();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.AbstractDialog#getName()
     */
    @Override
    public String getName()
    {
	    return "MainServer:"+this.address.toString();
    }
    
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.AbstractDialog#excetionCaught(java.lang.Exception)
     */
    @Override
    public void excetionCaught(Throwable e)
    {
    	//当主服务器线程发生读写异常时，只能结束这个线程，并结束整个客户端
    	try {
    		logger.warn("ServerDialog exception caught, cannot fix it, the client will exit...");
	        this.client.exceptionCaught(e);		//客户端处理异常
        } catch (Exception e1) {
	       logger.warn("handle ServerDailog exception:"+e);
        }
	    
    }

	/**
	 * 打开服务器对话框的过程就是登陆的过程
	 */
    public boolean openDialog() throws Exception
    {
    	//连接服务器
    	this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_CONNECT);
    	try {
    		this.transferService.startService();
    	}catch (Exception e) {
    		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_CONNECT_FAILED);
    		throw new Exception(e);
		}
    	
    	//一系列的登录过程
    	if(this.register()&&this.userAuth()) {
    		this.getPersonalInfo();
    		this.getContactList();
    		this.getContactsInfo();
    		this.subscribeNotify();
    		client.getGlobalTimer().schedule(this.keepAliveTask, 5*60*1000, FetionConfig.getInteger("fetion.sip.keep-alive-interval")*1000);
    		return true;
    	}else {
    		return false;
    	}
    }

    /**
	 * 客户端每隔一定的时间需要向服务发起注册请求，保持在线状态
	 * @throws Exception 
	 */
	protected void keepLive() throws Exception
	{
		logger.debug("sending keep live request to server..");
		SIPRequest request = this.messageFactory.createKeepAliveRequest();
		this.transferService.sendSIPMessage(request);
	}
	
	/**
	 * 内部类，实现了定时的发在线请求
	 */
	public class KeepAliveTimerTask extends TimerTask
	{
		/* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
        	try {
	            keepLive();
            } catch (Exception e) {
            	logger.warn("KeepAliveTimerTask exception caught:"+e);
            }
        }        
	}
    
	/**
	 * 注册服务器
	 */
	private boolean register()throws Exception
	{
		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_REGISTER);
		SIPRequest request = this.messageFactory.createServerRegisterRequest();
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		
		//服务返回 401和随机加密字串
    	if(response.getStatusCode()==401) {
    		//获取nonce
    		String w = response.getHeader("W").getValue();
    		int o = w.indexOf("nonce=\"");
    		String nonce = w.substring(o+7,w.length()-1);
    		this.dialogSession.setAttribute("NONCE", nonce);
    		logger.debug("Nonce:"+nonce);
    		return true;
    	}else {
    		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_UNKOWN_FAILED);
    		logger.warn("Invalid response:"+response.getStatusCode()+" "+response.getStatusMessage());
    		return false;
    	}
}
	
	/**
	 * 验证用户
	 */
	private boolean userAuth() throws Exception
	{
		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_AUTH);
		String nonce = (String) this.dialogSession.getAttribute("NONCE");
		SIPRequest request = this.messageFactory.createUserAuthRequest(nonce);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		
		if(response.getStatusCode()==200) {
    		logger.debug("User auth success, processed...");
    		return true;
    	}else {
    		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_AUTH_FAILED);
    		logger.warn("User auth failed:username or password may wrong..");
    		return false;
    	}
    		
	}
	
	/**
	 * 获取个人信息
	 */
	private void getPersonalInfo() throws Exception
	{
		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_GET_PERSONAL_INFO);
		SIPRequest request = this.messageFactory.createGetPersonalInfoRequest();
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		
		Element personal = XMLHelper.build(response.getBody().toSendString()).getChild("personal");
    	FetionUser user = this.client.getFetionUser();
    	
    	user.setNickName(personal.getAttributeValue("nickname"));
    	user.setImpresa(personal.getAttributeValue("impresa"));
    	
    	logger.debug("Name:"+personal.getAttributeValue("nickname")+", impresa="+personal.getAttributeValue("impresa"));
    }
	
	/**
	 * 获取好友列表
	 * @throws Exception 
	 */
	private void getContactList() throws Exception
	{
    	
		SIPRequest request = this.messageFactory.createGetContactListRequest();
    	this.transferService.sendSIPMessage(request);
    	SIPResponse response = request.getResponseWaiter().waitResponse();
    	
    	IFetionStore store = this.client.getFetionStore();
    	Element result = XMLHelper.build(response.getBody().toSendString());
    	Element contacts = result.getChild("contacts");
    	
    	//分组列表
    	List list = contacts.getChild("buddy-lists").getChildren();
    	Iterator it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		store.addCord(new FetionCord(Integer.parseInt(e.getAttributeValue("id")), e.getAttributeValue("name")));
    	}
    	
    	//飞信好友列表
    	list = contacts.getChild("buddies").getChildren();
    	it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		FetionBuddy b = new FetionBuddy();
    		b.setUri(e.getAttributeValue("uri"));
    		b.setUid(Integer.parseInt(e.getAttributeValue("user-id")));
    		b.setLocalName(e.getAttributeValue("local-name"));
    		b.setCordId(e.getAttributeValue("buddy-lists"));
    		b.setRelationStatus(Integer.parseInt(e.getAttributeValue("relation-status")));
    		b.setRegistered(true);
    		store.addBuddy(b);
    	}
    	
    	// 飞信手机好友列表
    	list = contacts.getChild("mobile-buddies").getChildren();
    	it = list.iterator();
    	while(it.hasNext()) {
    		Element e = (Element) it.next();
    		FetionBuddy b = new FetionBuddy();
    		b.setUri(e.getAttributeValue("uri"));
    		b.setUid(Integer.parseInt(e.getAttributeValue("user-id")));
    		b.setLocalName(e.getAttributeValue("local-name"));
    		b.setCordId(e.getAttributeValue("buddy-lists"));
    		b.setRelationStatus(Integer.parseInt(e.getAttributeValue("relation-status")));
    		b.setRegistered(false);
    		store.addBuddy(b);
    	}
    	
    	//TODO 处理allowList...
	}
	
	/**
	 * 获取联系人详细信息
	 * @throws Exception 
	 */
	private void getContactsInfo() throws Exception
	{
		//发起获取联系人详细信息请求
    	this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_GET_CONTACTS_INFO);
    	IFetionStore store  = this.client.getFetionStore();
    	SIPRequest request = this.messageFactory.createGetContactsInfoRequest(store.getBuddyList());
    	this.transferService.sendSIPMessage(request);
    	
    	SIPResponse response = request.getResponseWaiter().waitResponse();
    	
    	Element root = XMLHelper.build(response.getBody().toSendString());
 	    List list = root.getChild("contacts").getChildren();
 	    Iterator it = list.iterator();
 	    while(it.hasNext()) {
 	    	Element e = (Element) it.next();
 	    	Element p = e.getChild("personal");
 	    	
 	    	String uri = e.getAttributeValue("uri");
 	    	if(store.hasBuddy(uri)&& p!=null) {
     	    	FetionBuddy b = store.getBuddy(uri);
     	    	ParseHelper.parseBuddyPersonalBasic(b, p);
 	    	}
 	    }
	}
	
	/**
	 * 订阅异步通知
	 * @throws Exception 
	 */
	private void subscribeNotify() throws Exception
	{
		this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SERVER_SUB_NOTIFY);
		IFetionStore store = this.client.getFetionStore();
 	   	SIPRequest request = this.messageFactory.createSubscribeRequest(store.getBuddyList());
 	   	this.transferService.sendSIPMessage(request);
 	   	SIPResponse response = request.getResponseWaiter().waitResponse();
 	   	//TODO ..处理订阅回复结果
 	   	
 	   	this.client.getLoginListener().loginStatusChanged(ILoginListener.LOGIN_SUCCESS);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 发送聊天消息
	 * @param uri
	 * @param content
	 * @throws Exception 
	 */
	public boolean sendChatMessage(String uri, String content) throws Exception
	{
		SIPRequest request  = this.messageFactory.createChatMessageRequest(uri, content);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		return response.getStatusCode()==200 || response.getStatusCode()==280;
	}
	
	
	/**
	 * 异步发送聊天信息
	 * @param uri
	 * @param content
	 * @param callback
	 * @throws Exception 
	 */
	public void sendChatMessageEx(final String uri, final String content, final IMessageCallback callback) throws Exception
	{
		SIPRequest request  = this.messageFactory.createChatMessageRequest(uri, content);
		ISIPResponseHandler handler = new ISIPResponseHandler()
		{
			public void handle(SIPResponse response) throws Exception
			{
				callback.messageSended(uri, content, response.getStatusCode()==200||response.getStatusCode()==280);
			}
		};
		request.setResponseHandler(handler);
		this.transferService.sendSIPMessage(request);
	}
	
	/**
	 * 发送手机短信
	 * @param uri
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public boolean sendSMSMessage(String uri, String content) throws Exception
	{
		SIPRequest request  = this.messageFactory.createSendSMSRequest(uri, content);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		return response.getStatusCode()==280;
	}
	
	
	/**
	 * 异步发送手机短信
	 * @param uri
	 * @param content
	 * @param callback
	 * @throws Exception 
	 */
	public void sendSMSMessageEx(final String uri, final String content, final IMessageCallback callback) throws Exception
	{
		SIPRequest request  = this.messageFactory.createSendSMSRequest(uri, content);
		ISIPResponseHandler handler = new ISIPResponseHandler()
		{
			public void handle(SIPResponse response) throws Exception
			{
				callback.messageSended(uri, content, response.getStatusCode()==280);
			}
		};
		request.setResponseHandler(handler);
		this.transferService.sendSIPMessage(request);
	}
	
	/**
	 * 添加好友
	 * 注意：无论是添加飞信好友还是手机好友，都可以使用这个方法，这个方法会自动判断
	 * @param uri		好友手机uri(类似tel:159xxxxxxxx)
	 * @param cordId	添加好友的组编号
	 * @param promptId	提示信息编号
	 * @param desc 		“我是xx” xx：名字
	 * @return
	 * @throws Exception 
	 */
	public boolean addBuddy(String uri, int cordId, int promptId, String desc) throws Exception
	{
		SIPRequest request = this.messageFactory.createAddBuddyRequest(uri, promptId, cordId, desc);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==522) {	//如果返回的是522，表明用户没开通飞信，那就添加手机好友
			return this.addMobileBuddy(uri, cordId, desc);
		}else if(response.getStatusCode()==200){		
    		//用户已经开通飞信,返回了用户的真实的uri,建立一个好友对象，并加入到好友列表中
    		FetionBuddy buddy = new FetionBuddy();
    		Element root = XMLHelper.build(response.getBody().toSendString());
    		Element element = XMLHelper.find(root, "/results/contacts/buddies/buddy");
    		if(element==null)	return false;
    		buddy.setUri(element.getAttributeValue("uri"));
    		buddy.setLocalName(element.getAttributeValue("local-name"));
    		buddy.setCordId(element.getAttributeValue("buddy-lists"));
    		buddy.setRelationStatus(Integer.parseInt(element.getAttributeValue("relation-status")));
    		
    		buddy.setRegistered(true);	//已开通飞信
    		this.client.getFetionStore().addBuddy(buddy);
    			
    	    return true;
		}else{
			logger.warn("Error ocurred when adding Buddy:[uri:"+uri+", response:"+response);
			return false;
		}
	}
	
	
	/**
	 * 添加手机好友
	 * @param uri		好友手机uri(类似tel:159xxxxxxxx)
	 * @param cordId	添加好友的组编号
	 * @param desc		“我是xx” xx：名字
	 * @return
	 * @throws Exception
	 */
	private boolean addMobileBuddy(String uri, int cordId, String desc) throws Exception
	{
		SIPRequest request = this.messageFactory.createAddMobileBuddyRequest(uri, cordId, desc);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()!=200) return false;
		
		FetionBuddy buddy = new FetionBuddy();
		Element root = XMLHelper.build(response.getBody().toSendString());
		Element element = XMLHelper.find(root, "/results/contacts/mobile-buddies/mobile-buddy");
		if(element==null)	return false;
		
		buddy.setUri(element.getAttributeValue("uri"));
		buddy.setCordId(element.getAttributeValue("buddy-lists"));
		buddy.setRelationStatus(Integer.parseInt(element.getAttributeValue("relation-status")));
		
		buddy.setRegistered(false);		//未开通飞信
		this.client.getFetionStore().addBuddy(buddy);
		
		return true;
	}
	
	
	/**
	 * 同意对方添加好友
	 * @param uri			飞信地址
	 * @param localName		修改的姓名
	 * @param cordId		分组编号
	 * @return
	 * @throws Exception 
	 */
	public boolean agreedApplication(String uri) throws Exception
	{
		SIPRequest request = this.messageFactory.createAgreeApplicationRequest(uri);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			//将原来是陌生人的状态设置为好友
			FetionBuddy buddy = this.client.getFetionStore().getBuddy(uri);
			buddy.setRelationStatus(FetionBuddy.RELATION_STATUS_AGREED);
			///获取好友详细信息
			request = this.messageFactory.createGetContactDetailRequest(uri);
			this.transferService.sendSIPMessage(request);
			response = request.getResponseWaiter().waitResponse();
			
			Element root = XMLHelper.build(response.getBody().toSendString());
			Element contact = root.getChild("contacts").getChild("contact");
			if(contact!=null) {
				Element personal = contact.getChild("personal");
			    if(personal!=null) {
			    	ParseHelper.parseBuddyPersonalBasic(buddy, personal);
			    }
			}
			return true;
		}else {
			return false;
		}
	}
	
	
	/**
	 * 拒绝对方添加请求
	 * @param uri
	 * @return
	 * @throws Exception 
	 */
	public boolean declinedAppliction(String uri) throws Exception
	{
		SIPRequest request = this.messageFactory.createDeclineApplicationRequest(uri);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		return response.getStatusCode()==200;
	}
	
	/**
	 * 删除好友
	 * @param uri  
	 * @return
	 * @throws Exception
	 */
	public boolean deleteBuddy(String uri) throws Exception
	{
		FetionBuddy buddy = this.client.getFetionStore().getBuddy(uri);
		if(buddy == null) return false;
		
		//如果好友是飞信好友，就直接发起删除好友请求
		if(buddy.isRegistered()) {
    		SIPRequest request = this.messageFactory.createDeleteBuddyRequest(uri);
    		this.transferService.sendSIPMessage(request);
    		SIPResponse response = request.getResponseWaiter().waitResponse();
    		if(response.getStatusCode()==200) {
    			this.client.getFetionStore().deleteBuddy(uri);
    			return true;
    		}else {
    			return false;
    		}
		}else {
			//如果是手机好友，删除手机好友
			return this.deleteMoblieBuddy(uri);
		}
	}
	
	/**
	 * 删除手机好友
	 * @param uri  好友手机uri(类似tel:159xxxxxxxx)
	 * @return
	 * @throws Exception 
	 */
	private boolean deleteMoblieBuddy(String uri) throws Exception
	{
		SIPRequest request = this.messageFactory.createDeleteMobileBuddyRequest(uri);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			this.client.getFetionStore().deleteBuddy(uri);
			return true;
		}else {
			return false;
		}
	}

	/**
	 * 获取好好友详细信息
	 * @param uri
	 * @return
	 * @throws Exception 
	 */
	public boolean readBuddyDetail(String uri) throws Exception
	{
		SIPRequest request = this.messageFactory.createGetContactDetailRequest(uri);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			Element root = XMLHelper.build(response.getBody().toSendString());
			Element personal = XMLHelper.find(root, "/results/contacts/contact/personal");
			FetionBuddy buddy = this.client.getFetionStore().getBuddy(uri);
			if(personal!=null && buddy!=null ) {
				ParseHelper.parseBuddyPersonalBasic(buddy, personal);
				ParseHelper.parseBuddyPersonalExtend(buddy, personal);
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	/**
	 * 更新个人信息
	 * @param nickName		修改后的昵称
	 * @param impresa		修改后的个性签名
	 * @return	更新是否成功
	 * @throws Exception 
	 */
	public boolean setPersonalInfo(String nickName, String impresa) throws Exception
	{
		SIPRequest request = this.messageFactory.createSetPersonalInfoRequest(nickName, impresa);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			this.client.getFetionUser().setNickName(nickName);
			this.client.getFetionUser().setImpresa(impresa);
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 设置好友备注
	 * @param uri			好友飞信地址
	 * @param localName		备注
	 * @return
	 * @throws Exception 
	 */
	public boolean setBuddyLocalName(String uri, String localName) throws Exception
	{
		SIPRequest request = this.messageFactory.createSetBuddyLocalName(uri, localName);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			FetionBuddy buddy = this.client.getFetionStore().getBuddy(uri);
			buddy.setLocalName(localName);
			return true;
		}else {
			return false;
		}
	}
	
	
	/**
	 * 设置当前用户的在线状态
	 * @param presence		在线状态，定义在FetionBuddy里面
	 * @return				成功返回true失败返回false
	 * @throws Exception
	 */
	public boolean setPresence(int presence) throws Exception
	{
		SIPRequest request = this.messageFactory.createSetPresenceRequest(presence);
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			this.client.getFetionUser().setPresence(presence);
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 请求聊天服务器IP和验证字符串
	 * @return 		验证字符串
	 * @throws Exception 
	 */
	
	public String startChat() throws Exception
	{
		SIPRequest request = this.messageFactory.createStartChatRequest();
		this.transferService.sendSIPMessage(request);
		SIPResponse response = request.getResponseWaiter().waitResponse();
		if(response.getStatusCode()==200) {
			return response.getHeader(SIPHeader.FIELD_AUTHORIZATION).getValue();
		}else {
			return null;
		}
	}
}
