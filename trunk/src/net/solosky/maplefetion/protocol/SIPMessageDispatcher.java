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
 * Package  : net.solosky.maplefetion.net
 * File     : DefaultSIPMessageDispatcher.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import java.util.Hashtable;

import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPMethod;
import net.solosky.maplefetion.sip.SIPNotify;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.sip.SIPResponse;

import org.apache.log4j.Logger;

/**
 * 
 * 默认SIP信令分发器
 * 
 * @author solosky <solosky772@qq.com> 
 * 
 */
public class SIPMessageDispatcher
{

	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(SIPMessageDispatcher.class);

	/**
	 * 通知处理器缓存
	 */
	private Hashtable<String, ISIPNotifyHandler> notifyHandlers;

	/**
	 * 飞信客户端对象
	 */
	private IFetionClient client;

	/**
	 * 当前对话
	 */
	private AbstractDialog dialog;

	/**
	 * 构造函数
	 */
	public SIPMessageDispatcher(IFetionClient client, AbstractDialog dialog)
	{
		this.notifyHandlers = new Hashtable<String, ISIPNotifyHandler>();
		this.client = client;
		this.dialog = dialog;
	}

	/**
	 * 分发SIP回复
	 * 
	 * @param response
	 *            SIP回复
	 */
	public void dispatchSIPResponse(SIPResponse response, SIPRequest request)
	{

		if (request.getResponseHandler() != null) {
			try {
				request.getResponseHandler().handle(response);
			} catch (Exception e) {
				logger.warn("SIPResponseHandlerException [" + request.getResponseHandler().getClass().getName()+ "]:" + e);
			}
		} else {
			if(request!=null) {
				request.getResponseWaiter().responseRecived(response);
			}else {
				//这里可能是收到了重复的回复了，忽略掉
				logger.warn("Unknown Response:[ "+response+"]");
			}
		}

	}

	/**
	 * 分发SIP通知
	 * 
	 * @param notify
	 *            SIP通知
	 */
	public void dispatchSIPNotify(SIPNotify notify)
	{
			if(notify==null)	return;
    		String method = notify.getMethod();
    		if(method==null) {
    			logger.warn("Unknown Notify method:["+notify+"]");
    		}
    		String clazz = "net.solosky.maplefetion.protocol.notify.";
    		if (method.equals(SIPMethod.METHOD_BENOTIFY)) {
    			SIPHeader eventHeader = notify.getHeader(SIPHeader.FIELD_EVENT);
    			if(eventHeader==null || eventHeader.getValue()==null) {
    				logger.warn("Unknown Notify event:["+notify+"]");
    				return;
    			}
    			String event = notify.getHeader(SIPHeader.FIELD_EVENT).getValue();
    			if (event.equals("presence")) {
    				clazz += "PresenceSIPNotifyHandler";					//好友在线状态改变
    			} else if (event.equals("contact")) {
    				clazz += "ContactNotifyHandler";						//联系人消息，这下面还有子请求，如添加好友请求，延迟好友资料返回
    			}else if(event.equals("Conversation")){
    				clazz +="ConversationNotifyHandler";					//对话消息，表示好友已经进入了对话里面
    			}else if(event.equals("registration")){
    				clazz +="RegistrationNotifyHandler";					//注册消息，这里只有一个，用户在其他地方登陆
    			}else if(event.equals("compact")) {
    																		//列表改变事件 TODO ..
    			}
    		}else if(method.equals(SIPMethod.METHOD_MESSAGE)) {
    			clazz += "MessageNotifyHandler";							//消息，包括系统消息和好友消息
    		}else if(method.equals(SIPMethod.METHOD_INVATE)) {
    			clazz += "InviteBuddyNotifyHandler";						//被邀请进入对话
    		}else if(method.equals(SIPMethod.METHOD_INFO)) {
    			clazz += "FetionShowNotifyHandler";							//飞信秀，可能会有其他的
    		}
    		// TODO ..其他通知。。。
    
    		ISIPNotifyHandler handler = (ISIPNotifyHandler) this
    		        .loadNotifyHandler(clazz);
    		try {
    			if (handler != null) {
    				handler.handle(notify);
    				logger.debug("finished handle Notify:"
    				        + handler.getClass().getName());
    			} else {
    				logger.debug("GOT a SIPNotify, waiting for handle..." + notify);
    			}
    		} catch (Exception e) {
    			logger.warn("SIPNotifyHandlerException [" + handler.getClass().getName()
    			        + "]:" + e);
    			e.printStackTrace();
    		}
	}

	/**
	 * 查找对象，如果没有找到就实例化
	 * 
	 * @param clazz
	 * @return
	 */
	private ISIPNotifyHandler loadNotifyHandler(String clazz)
	{
		ISIPNotifyHandler handler = null;
		handler = this.notifyHandlers.get(clazz);
		if (handler == null) {
			try {
				handler = (ISIPNotifyHandler) Class.forName(clazz)
				        .newInstance();
				handler.setDailog(dialog);
				handler.setFetionClient(client);
			} catch (InstantiationException e) {
				logger.warn("loadHandler:Can nnot instance class:" + clazz);
			} catch (IllegalAccessException e) {
				logger.warn("loadHandler:Ilegal access when instancing class:"
				        + clazz);
			} catch (ClassNotFoundException e) {
				logger.warn("loadHandler:Class not found:" + clazz);
			}
		}

		return handler;
	}

}
