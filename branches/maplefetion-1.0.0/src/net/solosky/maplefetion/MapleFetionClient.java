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
 * Package  : net.solosky.maplefetion
 * File     : MapleFetionClient.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.FetionUser;
import net.solosky.maplefetion.net.ITransferFactory;
import net.solosky.maplefetion.protocol.ChatDialog;
import net.solosky.maplefetion.protocol.ChatDialogFactory;
import net.solosky.maplefetion.protocol.ServerDialog;
import net.solosky.maplefetion.store.IFetionStore;
import net.solosky.maplefetion.util.XMLHelper;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 *
 * @author solosky <solosky772@qq.com> 
 */
public class MapleFetionClient implements IFetionClient
{
	
	/**
	 * MapleFetion版本
	 */
	public static final String CLIENT_VERSION = "MapleFetion 1.0 Beta2";
	
	/**
	 * 协议版本
	 */
	public static final String PROTOCOL_VERSION = "2009 3.5.1170";
	
	
	/**
	 * 飞信会话
	 */
	private FetionSession fetionSession;
	
	/**
	 * 飞信存储对象
	 */
	private IFetionStore   fetionStore;
	
	/**
	 * 飞信用户对象
	 */
	private FetionUser     fetionUser;
	
	/**
	 * 通知监听器
	 */
	private INotifyListener notifyListener;
	
	
	/**
	 * 飞信登录监听器
	 */
	private ILoginListener loginListener;
	
	
	/**
	 * 传输工厂
	 */
	private ITransferFactory transferFactory;
	
	/**
	 * 服务器对话
	 */
	private ServerDialog serverDialog;
	
	/**
	 * 聊天会话工厂
	 */
	private ChatDialogFactory chatDialogFactory;
	
	/**
	 * 全局定时器
	 */
	private Timer globalTimer;
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(MapleFetionClient.class);
	
	/**
	 * 详细的构造函数
	 * @param mobileNo			手机号
	 * @param pass				用户密码
	 * @param transferFactory	传输工厂
	 * @param fetionStore		分析存储对象
	 * @param notifyListener	通知监听器
	 * @param loginListener		登录监听器
	 */
	public MapleFetionClient(long mobileNo,
							String pass, 
							ITransferFactory transferFactory,
							IFetionStore fetionStore,
							INotifyListener notifyListener, 
							ILoginListener loginListener)
	{
		this.fetionSession   = new FetionSession();
		this.fetionStore     = fetionStore;
		this.fetionUser      = new FetionUser(mobileNo, pass, "fetion.com.cn");
		this.transferFactory = transferFactory;
		this.loginListener   = loginListener;
		this.notifyListener  = notifyListener;
		this.globalTimer     = new Timer(true);
		this.chatDialogFactory = new ChatDialogFactory(this);
	}
	
	/**
	 * 开始登录
	 * @return 是否登录成功
	 * @throws Exception 
	 */
	public boolean login() throws Exception
	{
    		this.querySystemConfig();
    		if(this.SSISignIn()) {
    			String[] proxy = FetionConfig.getString("server.sipc-proxy").split(":");
    			serverDialog = new ServerDialog(this, proxy[0], Integer.parseInt(proxy[1]));
    			return serverDialog.openDialog();
    		}else {
    			return false;
    		}
	}
	
	
	/**
	 * SSI登录
	 * @return	是否登录成功
	 */
	private boolean SSISignIn() throws Exception
	{
		this.loginListener.loginStatusChanged(ILoginListener.LOGIN_SSI_SIGN_IN);
		URL url = new URL(FetionConfig.getString("server.ssi-sign-in")+"?mobileno="+
				this.fetionUser.getMobileNo()+"&pwd="+this.fetionUser.getPassword()
				);
		
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		int resCode = conn.getResponseCode();
		logger.debug("SSIstatusCode:"+resCode);
		
		//登录成功
		if(resCode==200) {
			String h = conn.getHeaderField("Set-Cookie");
			if(h==null) 	return false;
			int s = h.indexOf("ssic=");
			int e = h.indexOf(';');
			String ssic = h.substring(s+5,e);
			
			
			Element root = XMLHelper.build(conn.getInputStream());
			String statusCode = root.getAttributeValue("status-code");
			Element user = root.getChild("user");
			String uri = user.getAttributeValue("uri");
			String uid = user.getAttributeValue("user-id");
			String sid = uri.substring(4, uri.indexOf('@'));
			
			FetionUser fetionUser = this.getFetionUser(); 
			fetionUser.setSsic(ssic);
			fetionUser.setUri(uri);
			fetionUser.setUid(Integer.parseInt(uid));
			fetionUser.setSid(Integer.parseInt(sid));
			
			logger.debug("SSISignIn success:SSIC="+ssic);
			return true;
		}else {
			//登录失败
			switch (resCode)
			{
				case 401:this.loginListener.loginStatusChanged(ILoginListener.LOGIN_SSI_AUTH_FAILED);break;
				case 433:this.loginListener.loginStatusChanged(ILoginListener.LOGIN_USER_OVER_DRAW);break;
				default:this.loginListener.loginStatusChanged(ILoginListener.LOGIN_UNKOWN_FAILED);
			}
			return false;
		}
	}
	
	/**
	 * 获取系统配置
	 * @return void 
	 */
	private void querySystemConfig() throws Exception
	{
		this.loginListener.loginStatusChanged(ILoginListener.LOGIN_QUERY_SYSTEM_CONFIG);
        URL url = new URL(FetionConfig.getString("server.nav-system-uri"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String content = "<config><user mobile-no=\""+this.getFetionUser().getMobileNo()+"\" /><client type=\"PC\" version=\"3.5.1170\" platform=\"W5.1\" /><servers version=\"0\" /><service-no version=\"0\" /><parameters version=\"0\" /><hints version=\"0\" /><http-applications version=\"0\" /><client-config version=\"0\" /><services version=\"0\" /></config>";
        OutputStream out = conn.getOutputStream();
        out.write(content.getBytes());
        out.flush();
        
        Element root = XMLHelper.build(conn.getInputStream());
        
        Element servers = root.getChild("servers");
        FetionConfig.setString("server.ssi-sign-in",  servers.getChildText("ssi-app-sign-in"));
        FetionConfig.setString("server.sipc-proxy",   servers.getChildText("sipc-proxy"));
        FetionConfig.setString("server.http-tunnel",  servers.getChildText("http-tunnel"));
	}
	
	/**
	 * 发送聊天消息
	 * @param uri			好友的URI
	 * @param content		消息内容
	 * @throws Exception 
	 */
	public boolean sendChatMessage(String uri, String content) throws Exception
	{
		FetionBuddy buddy = this.fetionStore.getBuddy(uri);
		if(buddy!=null) {
			//如果好友在线，使用聊天服务器发送消息
			if(buddy.getPresence()==FetionBuddy.PRESENCE_PC_ONLINE ||
					buddy.getPresence()==FetionBuddy.PRESENCE_PC_AWAY ||
					buddy.getPresence()==FetionBuddy.PRESENCE_PC_BUSY) {
    				logger.debug("buddy is online, message will send by chat server..");
    				
    				//检查用户是否在一个对话里
    				ChatDialog cd = this.chatDialogFactory.findBuddyChatDialog(uri);
    				if(cd!=null) {
    					logger.debug("buddy alreay is in a chatDialog, send message by this chatDialog...");
    				}else {
    					cd = this.setupChatDialog(uri);
    				}
    				
        			//发送消息
        			logger.debug("send chat message to buddy..");
        			return cd.sendChatMessage(uri, content);
        			
    			}else {
				//如果好友不在线，使用主服务器直接发送到手机
				logger.debug("buddy is not online, send message by main server..");
				return this.serverDialog.sendChatMessage(uri, content);
			}
		}else {
			logger.warn("cannot send chat message to stranger:"+uri);
			return false;
		}
	}
	
	
	private ChatDialog setupChatDialog(String uri) throws Exception
	{
			logger.debug("buddy is not in a chatDialog, now start a new dialog...");
			ChatDialog cd = this.startChatDialog();
			if(cd!=null) {
				//邀请好友加入会话
				logger.debug("Invite buddy to dialog..");
				cd.invateBuddy(uri);	
				//等待好友加入会话
				logger.debug("wait buddy enter dialog..");
				cd.waitBuddyEnter();
			}
		return cd;
	}
	
	/**
	 * 释放资源
	 * 在客户端出错或者关闭的时候释放资源
	 * @throws Exception 
	 */
	private void dispose() throws Exception
	{
    	this.serverDialog.closeDialog();
    	this.chatDialogFactory.closeAllChatDialog();
    	this.globalTimer.cancel();
    	this.transferFactory.closeFactory();
	}
	
	/**
	 * 发送手机短信
	 * @param uri		发送的uri,注意这里的应该是好友飞信的URI
	 * @param content	发送的内容
	 * @return
	 * @throws Exception
	 */
	public boolean sendSMSMessage(String uri, String content) throws Exception
	{
		return this.serverDialog.sendSMSMessage(uri, content);
	}
	
	/**
	 * 发送手机短信
	 * @param mobileNo		发送的手机号码
	 * @param content		发送的内容
	 * @return
	 * @throws Exception 
	 */
	public boolean sendSMSMessage(long mobileNo, String content) throws Exception
	{
		return this.serverDialog.sendSMSMessage("tel:"+Long.toString(mobileNo), content);
	}
	
	
	/**
	 * 异步发送聊天信息
     * @param uri			飞信地址	
     * @param content		短信内容
     * @param callback		发送完毕后的回调函数
     * @throws Exception
     * @see net.solosky.maplefetion.protocol.ServerDialog#sendChatMessageEx(java.lang.String, java.lang.String, net.solosky.maplefetion.IMessageCallback)
     */
    public void sendChatMessageEx(final String uri, final String content,
            final IMessageCallback callback) throws Exception
    {
    	FetionBuddy buddy = this.fetionStore.getBuddy(uri);
		if(buddy!=null) {
			//如果好友在线，使用聊天服务器发送消息
			if(buddy.getPresence()==FetionBuddy.PRESENCE_PC_ONLINE ||
					buddy.getPresence()==FetionBuddy.PRESENCE_PC_AWAY ||
					buddy.getPresence()==FetionBuddy.PRESENCE_PC_BUSY) {
				
    				//检查用户是否在一个对话里
    				ChatDialog cd = this.chatDialogFactory.findBuddyChatDialog(uri);
    				if(cd!=null) {
    					logger.debug("buddy alreay is in a chatDialog, send message by this chatDialog...");
    				}else {
    					//建立新的对话，这个可能花费的时间较长，这里使用一个线程来完成
    					Runnable r = new Runnable(){
							public void run(){
								synchronized (chatDialogFactory) {
									try {
										ChatDialog cd = setupChatDialog(uri);
										//发送消息
										cd.sendChatMessageEx(uri, content, callback);
	                                } catch (Exception e) {
	                                	//发送失败，通知回调函数
		                                callback.messageSended(uri, content, false);
	                                } 
								}
							}
						};
						Thread thread = new Thread(r);
						thread.start();
						return;
    				}

    				//发送消息
        			logger.debug("send chat message to buddy..");
        			cd.sendChatMessageEx(uri, content, callback);
        			return;
        			
    			}else {
				//如果好友不在线，使用主服务器直接发送到手机
				logger.debug("buddy is not online, send message by main server..");
				this.serverDialog.sendChatMessageEx(uri, content, callback);
				return;
			}
		}else {
			logger.warn("cannot send chat message to stranger:"+uri);
			return;
		}
    }

	/**
	 * 异步发送短信
     * @param uri			飞信地址（注意这里不是手机号，而是 tel:159xxxxxxxx的形式）		
     * @param content		短信内容
     * @param callback		消息发送完成后的回调函数
     * @throws Exception
     * @see net.solosky.maplefetion.protocol.ServerDialog#sendSMSMessageEx(java.lang.String, java.lang.String, net.solosky.maplefetion.IMessageCallback)
     */
    public void sendSMSMessageEx(String uri, String content,
            IMessageCallback callback) throws Exception
    {
	    serverDialog.sendSMSMessageEx(uri, content, callback);
    }

	/**
	 * 建立一个对话
	 * @return		如果建立成功返回对话对象
	 * @throws Exception 
	 */
	public ChatDialog startChatDialog() throws Exception
	{
		//首先要请求服务器IP和验证字符串
		String auth = this.serverDialog.startChat();
		if(auth!=null) {
			Pattern pt = Pattern.compile("address=\"(.*?):(.*?);.*?=\"(.*?)\"");
    		Matcher mc = pt.matcher(auth);
    		if(mc.find()) {
    			ChatDialog cd = this.chatDialogFactory.createChatDialog(mc.group(1), Integer.parseInt(mc.group(2)), mc.group(3));
    			return cd;
    		}else {
    			logger.warn("cannot parse ChatServer address.");
    			return null;
    		}
		}else {
			logger.warn("cannot get ChatServer ticket.");
			return null;
		}
	}
	
	/**
	 * 添加好友
	 * @param long		按手机号码添加
	 * @return			发出好友请求是否成功
	 * @throws Exception 
	 */
	public boolean addBuddy(long mobileNo) throws Exception
	{
		return this.serverDialog.addBuddy("tel:"+Long.toString(mobileNo), 0, 1, this.fetionUser.getNickName());
	}
	
	/**
	 * 删除好友
	 * @param uri 	删除好友的URI
	 * @return		是否删除成功
	 * @throws Exception
	 */
	public boolean deleteBuddy(String uri) throws Exception
	{
		return this.serverDialog.deleteBuddy(uri);
	}
	
	/**
	 * 同意对方添加好友
	 * @param uri			飞信地址
	 * @return				是否同意成功
	 * @throws Exception 
	 */
	public boolean agreedApplication(String uri) throws Exception
	{
		return this.serverDialog.agreedApplication(uri);
	}
	
	
	/**
	 * 获取好友详细信息
     * @param uri		飞信地址
     * @return	是否成功
     * @throws Exception
     * @see net.solosky.maplefetion.protocol.ServerDialog#readBuddyDetail(java.lang.String)
     */
    public boolean readBuddyDetail(String uri) throws Exception
    {
	    return serverDialog.readBuddyDetail(uri);
    }

	/**
	 * 拒绝对方添加请求
	 * @param uri		飞信地址
	 * @return			是否拒绝成功
	 * @throws Exception 
	 */
	public boolean declinedApplication(String uri) throws Exception
	{
		return this.serverDialog.declinedAppliction(uri);
	}
    
	/**
	 * 设置好友备注
	 * @param uri			好友飞信地址
	 * @param localName		备注
     * @return
     * @throws Exception
     * @see net.solosky.maplefetion.protocol.ServerDialog#setBuddyLocalName(java.lang.String, java.lang.String)
     */
    public boolean setBuddyLocalName(String uri, String localName)
            throws Exception
    {
	    return serverDialog.setBuddyLocalName(uri, localName);
    }

	/**
	 * 更新个人信息
	 * @param nickName		修改后的昵称
	 * @param impresa		修改后的个性签名
	 * @return				更新是否成功
     * @throws Exception
     * @see net.solosky.maplefetion.protocol.ServerDialog#setPersonalInfo(java.lang.String, java.lang.String)
     */
    public boolean setPersonalInfo(String nickName, String impresa)
            throws Exception
    {
	    return serverDialog.setPersonalInfo(nickName, impresa);
    }

    
	/**
	 * 设置当前用户的状态
     * @param presence		用户状态，定义在FetionBuddy里面
     * @return				设置是否成功
     * @throws Exception
     * @see net.solosky.maplefetion.protocol.ServerDialog#setPresence(int)
     */
    public boolean setPresence(int presence) throws Exception
    {
	    return serverDialog.setPresence(presence);
    }
    
	/**
	 * 退出登录
	 * @throws Exception
	 */
	public void logout() throws Exception
	{
		this.dispose();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionClient#getSession()
     */
    @Override
    public FetionSession getFetionSession()
    {
	    return this.fetionSession;
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionClient#getStore()
     */
    @Override
    public IFetionStore getFetionStore()
    {
	    return this.fetionStore;
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionClient#getTransfer()
     */
    @Override
    public ITransferFactory getTransferFactory()
    {
	    return this.transferFactory;
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionClient#getUser()
     */
    @Override
    public FetionUser getFetionUser()
    {
	    return this.fetionUser;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionClient#getNotifyListener()
     */
    @Override
    public INotifyListener getNotifyListener()
    {
	    return this.notifyListener;
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.FetionClient#getLoginListener()
     */
    @Override
    public ILoginListener getLoginListener()
    {
	    return this.loginListener;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.IFetionClient#getGlobalTimer()
     */
    @Override
    public Timer getGlobalTimer()
    {
	    return this.globalTimer;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.IFetionClient#getChatDialogFactory()
     */
    @Override
    public ChatDialogFactory getChatDialogFactory()
    {
	    return this.chatDialogFactory;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.IFetionClient#getServerDialog()
     */
    @Override
    public ServerDialog getServerDialog()
    {
    	return this.serverDialog;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.IFetionClient#exceptionCaught(java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(Throwable exception)
    {
    	try {
    		this.dispose();
        	this.notifyListener.exceptionCaught(exception);
    	}catch (Exception e) {
			// TODO: handle exception
		}
    }

}
