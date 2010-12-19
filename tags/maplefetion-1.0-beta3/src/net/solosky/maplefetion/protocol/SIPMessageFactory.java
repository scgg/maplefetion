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
 * File     : DefaultSIPMessageFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

import java.util.Collection;
import java.util.Iterator;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.IFetionClient;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.FetionUser;
import net.solosky.maplefetion.sip.SIPBody;
import net.solosky.maplefetion.sip.SIPHeader;
import net.solosky.maplefetion.sip.SIPMethod;
import net.solosky.maplefetion.sip.SIPReceipt;
import net.solosky.maplefetion.sip.SIPRequest;
import net.solosky.maplefetion.util.AuthGenerator;
import net.solosky.maplefetion.util.ByteArrayBuffer;

/**
 *	
 *	默认的SIP信令建立工厂
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SIPMessageFactory
{
	/**
	 * CALLID
	 */
	private int callID;
	
	private int sequence;
	
	private IFetionClient fetionClient;
	
	
	private int sid;
	
	private String lastMethod;
	
	private String domain;
	
	/**
	 * 构造函数
	 * @param sid		飞信号
	 * @param domain	飞信域
	 */
	public SIPMessageFactory(IFetionClient c)
	{
		this.fetionClient = c;
		this.callID = 0;
		this.sid = fetionClient.getFetionUser().getSid();
		this.domain = fetionClient.getFetionUser().getDomain();
		this.lastMethod = "";
		this.sequence = 1;
	}
	
	/**
	 * 创建默认的SIPRequest
	 * @param m
	 * @return
	 */
    public SIPRequest createDefaultSIPRequest(String m)
    {
	    SIPRequest req = new SIPRequest(m,this.domain);
	    req.addHeader(SIPHeader.FIELD_FROM, Integer.toString(this.sid));
	    if(m.equals(this.lastMethod)) {
	    	req.addHeader(SIPHeader.FIELD_CALLID,   Integer.toString(this.callID));
	    	req.addHeader(SIPHeader.FIELD_SEQUENCE, Integer.toString(this.getNextSequence())+" "+m);
	    }else {
	    	req.addHeader(SIPHeader.FIELD_CALLID,   Integer.toString(this.getNextCallID()));
	    	req.addHeader(SIPHeader.FIELD_SEQUENCE, "1 "+m);
	    	this.sequence = 1;
	    }
	    req.setAliveTime((int) (FetionConfig.getInteger("fetion.sip.default-alive-time")+System.currentTimeMillis()/1000));
	    this.lastMethod = m;
	    return req;
    }
    
    /**
     * 服务器登录请求
     * @return
     */
    public SIPRequest createServerRegisterRequest()
    {
    	 SIPRequest req =this.createDefaultSIPRequest(SIPMethod.METHOD_REGISTER);
         req.setBody(new SIPBody(SIPRequestTemplate.TMPL_USER_AUTH));
         
         return req;
    }
    
    /**
     * 用户登录验证
     * @return
     */
    public SIPRequest createUserAuthRequest(String nonce)
    {
    	SIPRequest  req = this.createDefaultSIPRequest(SIPMethod.METHOD_REGISTER);
    	
    	FetionUser user = this.fetionClient.getFetionUser();
    	AuthGenerator auth = new AuthGenerator(Integer.toString(user.getSid()), user.getPassword(), user.getDomain(), nonce);
    	auth.generate();
    	
    	String authString = "Digest algorithm=\"SHA1-sess\",response=\""
    					+auth.getResponse()+"\",cnonce=\""+auth.getCnonce()
    					+"\",salt=\""+auth.getSalt()+"\",ssic=\""+user.getSsic()+"\"";
    	req.addHeader(SIPHeader.FIELD_AUTHORIZATION, authString);
    	
    	req.setBody(new SIPBody(SIPRequestTemplate.TMPL_USER_AUTH));
    	
    	return req;
    }
    
    /**
     * 获取个人详细信息
     * @return
     */
    public SIPRequest createGetPersonalInfoRequest()
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
	    req.addHeader(SIPHeader.FIELD_EVENT, "GetPersonalInfo");
    	req.setBody(new SIPBody(SIPRequestTemplate.TMPL_GET_PERSONAL_INFO));
    	
    	return req;
    }
    
    /**
     * 发送在线消息
     */
    public SIPRequest createChatMessageRequest(String toUri, String m)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_MESSAGE);
    	
    	req.addHeader(SIPHeader.FIELD_TO, toUri);
    	req.addHeader(SIPHeader.FIELD_CONTENT_TYPE, "text/plain");
    	req.addHeader(SIPHeader.FIELD_EVENT, "CatMsg");
    	
    	req.setBody(new SIPBody(m));
    	
    	return req;
    }
    
    /**
     * 发送手机短消息
     * @param uri
     * @param m
     * @return
     */
    public SIPRequest createSendSMSRequest(String uri, String m)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_MESSAGE);
    	
    	req.addHeader(SIPHeader.FIELD_TO, uri);
    	req.addHeader(SIPHeader.FIELD_EVENT, "SendCatSMS");
    	
    	req.setBody(new SIPBody(m));
    	
    	return req;
    }
    
    /**
     * 保持在线的请求
     * 也就是需要每隔一定时间需要注册一次
     * @return
     */
    public SIPRequest createKeepAliveRequest()
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_REGISTER);
    	
    	return req;
    }

    /**
     *  获取联系人详细信息
     * @param buddyList
     * @return
     */
    public SIPRequest createGetContactsInfoRequest(Collection<FetionBuddy> buddyList)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	
    	StringBuffer buffer = new StringBuffer();
    	Iterator<FetionBuddy> it = buddyList.iterator();
    	String contactTemplate = "<contact uri=\"{uri}\" />";
    	while(it.hasNext()){
    		FetionBuddy b = it.next();
    		if(b.isRegistered())
    			buffer.append(contactTemplate.replace("{uri}", b.getUri()));
    	}
    	String body = SIPRequestTemplate.TMPL_GET_CONTACTS_INFO;
    	body = body.replace("{contactList}", buffer.toString());
    	req.setBody(new SIPBody(body));
    	
    	req.addHeader(SIPHeader.FIELD_EVENT, "GetContactsInfo");
    	
    	
    	return req;
    }
    
    /**
     *  获取联系人详细信息
     * @param buddyList
     * @return
     */
    public SIPRequest createGetContactDetailRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	
    	String body = SIPRequestTemplate.TMPL_GET_CONTACT_DETAIL;
    	body = body.replace("{uri}", uri);
    	req.setBody(new SIPBody(body));
    	
    	req.addHeader(SIPHeader.FIELD_EVENT, "GetContactsInfo");
    	
    	return req;
    }
    
    /**
     * 获取联系人列表
     */
    public SIPRequest createGetContactListRequest()
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT, "GetContactList");
    	
    	req.setBody(new SIPBody(SIPRequestTemplate.TMPL_GET_CONTACT_LIST));
    	return req;
    }
    
    /**
     * 订阅异步通知
     */
    public SIPRequest createSubscribeRequest(Collection<FetionBuddy> buddyList)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SUBSCRIBE);
    	
    	StringBuffer buffer = new StringBuffer();
    	Iterator<FetionBuddy> it = buddyList.iterator();
    	String contactTemplate = "<contact uri=\"{uri}\" type=\"{type}\" />";
    	while(it.hasNext()){
    		FetionBuddy b = it.next();
    		String t = contactTemplate;
    		if(b.isRegistered()) {
    			t = contactTemplate.replace("{type}", "3");
    		}else {
    			t = contactTemplate.replace("{type}", "2");
    		}
    		t = t.replace("{uri}", b.getUri());
    		buffer.append(t);
    	}
    	String body = SIPRequestTemplate.TMPL_SUBSCRIBE;
    	body = body.replace("{contactList}", buffer.toString());
    	req.setBody(new SIPBody(body));
    	
    	req.addHeader(SIPHeader.FIELD_EVENT, "compactlist");
    	
    	return req;
    }
    
    /**
     * 开始聊天请求
     */
    public SIPRequest createStartChatRequest()
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	req.addHeader(SIPHeader.FIELD_EVENT,"StartChat");
    	return req;
    }
    
    /**
     * 注册聊天服务器
     */
    public SIPRequest createRegisterChatRequest(String ticket)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_REGISTER);
    	req.addHeader(SIPHeader.FIELD_AUTHORIZATION,"TICKS auth=\""+ticket+"\"");
    	//req.addHeader(SIPHeader.FIELD_SUPPORTED,"text/html-fragment");
    	req.addHeader(SIPHeader.FIELD_SUPPORTED,"multiparty");
    	req.addHeader(SIPHeader.FIELD_SUPPORTED,"nudge");
    	req.addHeader(SIPHeader.FIELD_SUPPORTED,"share-background");
    	req.addHeader(SIPHeader.FIELD_SUPPORTED,"fetion-show");
    	
    	return req;
    }
    
    /**
     * 邀请好友加入会话
     */
    public SIPRequest createInvateBuddyRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	
    	String body = SIPRequestTemplate.TMPL_INVATE_BUDDY;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"InviteBuddy");
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 飞信秀（有空再研究）
     */
    public SIPRequest createFetionShowRequest()
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_INFO);
    	ByteArrayBuffer buffer = new ByteArrayBuffer(100);
    	buffer.append(SIPRequestTemplate.TMPL_FETION_SHOW_1.getBytes(), 0,SIPRequestTemplate.TMPL_FETION_SHOW_1.getBytes().length);
    	buffer.append(0xE5);
    	buffer.append(0x9B);	//飞信太变态了，这里居然有几个字节无法用字符表示
    	buffer.append(0xA7);
    	buffer.append(SIPRequestTemplate.TMPL_FETION_SHOW_2.getBytes(), 0,SIPRequestTemplate.TMPL_FETION_SHOW_2.getBytes().length);
    	
    	byte[] bodyArr = buffer.toByteArray();
    	req.setBody(new SIPBody(new String(bodyArr)));
    	return req;
    }
    
    /**
     * 添加飞信好友请求
     * @param uri
     * @param promptId
     * @param cordId
     * @param desc
     * @return
     */
    public SIPRequest createAddBuddyRequest(String uri, int promptId, int cordId, String desc)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String body = SIPRequestTemplate.TMPL_ADD_BUDDY;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{promptId}", Integer.toString(promptId));
    	body = body.replace("{cordId}", "");
    	//body = body.replace("{cordId}", Integer.toString(cordId));
    	body = body.replace("{desc}", desc);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"AddBuddy");
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 添加手机好友请求
     * @param uri		好友的URI，这里应该是tel:159xxxxx
     * @param cordId	添加的组编号
     * @param desc		对好友的自我描述
     * @return
     */
    public SIPRequest createAddMobileBuddyRequest(String uri, int cordId, String desc)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String body = SIPRequestTemplate.TMPL_ADD_MOBILE_BUDDY;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{cordId}", "");
    	//body = body.replace("{cordId}", Integer.toString(cordId));
    	body = body.replace("{desc}", desc);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"AddMobileBuddy");
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 删除好友
     * @param uri
     * @return
     */
    public SIPRequest createDeleteBuddyRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String body = SIPRequestTemplate.TMPL_DELETE_BUDDY;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"DeleteBuddy");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 删除手机好友
     * @param uri  好友手机uri(类似tel:159xxxxxxxx)
     * @return
     */
    public SIPRequest createDeleteMobileBuddyRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String body = SIPRequestTemplate.TMPL_DELETE_MOBILE_BUDDY;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"DeleteMobileBuddy");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 同意对方添加好友
     * @param uri
     * @param localName
     * @param cordId
     * @return
     */
    public SIPRequest createAgreeApplicationRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String body = SIPRequestTemplate.TMPL_APPLICATION_AGREED;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"HandleContactRequest");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 拒绝陌生人添加好友请求
     * @param uri
     * @return
     */
    public SIPRequest createDeclineApplicationRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String body = SIPRequestTemplate.TMPL_APPLICATION_DECLINED;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"HandleContactRequest");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 更改个人资料
     * 这里只支持更改昵称和个性签名
     * @param nickName		昵称，为null不更新
     * @param impresa		个性签名，为null不更新
     * @return
     */
    public SIPRequest createSetPersonalInfoRequest(String nickName, String impresa)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	String attrs = "";
    	if(nickName!=null)
    		attrs += " nickname=\""+nickName+"\"";
    	if(impresa!=null)
    		attrs +=" impresa=\""+impresa+"\"";
    	
    	String body = SIPRequestTemplate.TMPL_SET_PERSONAL_INFO;
    	body = body.replace("{attrs}", attrs);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"SetPersonalInfo");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    }
    
    /**
     * 设置好友本地显示名字
     * @param uri		好友飞信地址
     * @param localName	本地显示名字
     * @return
     */
    public SIPRequest createSetBuddyLocalName(String uri, String localName)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	
    	String body = SIPRequestTemplate.TMPL_SET_BUDDY_LOCAL_NAME;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{localName}", localName);
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"SetBuddyInfo");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    	
    }
    
    /**
     * 设置在线状态
     */
    public SIPRequest createSetPresenceRequest(int presence)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_SERVICE);
    	
    	String body = SIPRequestTemplate.TMPL_SET_PRESENCE;
    	body = body.replace("{presence}", Integer.toString(presence));
    	
    	req.addHeader(SIPHeader.FIELD_EVENT,"SetPresence");
    	
    	req.setBody(new SIPBody(body));
    	return req;
    }
    /**
     * 退出客户端
     */
    public SIPRequest createLogoutRequest(String uri)
    {
    	SIPRequest req = this.createDefaultSIPRequest(SIPMethod.METHOD_BYE);
    	
    	req.addHeader(SIPHeader.FIELD_TO, uri);
    	return req;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 默认收据
     */
    public SIPReceipt createDefaultReceipt(String callId, String sequence)
    {
    	SIPReceipt receipt = new SIPReceipt(200, "OK");
    	receipt.addHeader(SIPHeader.FIELD_CALLID, callId);
    	receipt.addHeader(SIPHeader.FIELD_SEQUENCE, sequence);
    	
    	return receipt;
    }
    
    /**
     * 信息收到收据
     */
    public SIPReceipt createChatMessageReceipt(String fromUri, String callId,String sequence)
    {
    	SIPReceipt receipt = this.createDefaultReceipt(callId, sequence);
    	receipt.addHeader(SIPHeader.FIELD_FROM, fromUri);
    	
    	return receipt;
    }
    
    /**
     * 飞信秀收据
     */
    public SIPReceipt createFetionShowReceipt(String fromUri, String callId,String sequence)
    {
    	SIPReceipt receipt = this.createDefaultReceipt(callId, sequence);
    	receipt.addHeader(SIPHeader.FIELD_FROM, fromUri);
    	
    	return receipt;
    }
    /**
     * 下一次CALLID
     * @return
     */
    private int getNextCallID()
    {
    	return ++callID;
    }
    
    /**
     * 下一次Sequence
     */
    private int getNextSequence()
    {
    	return ++sequence;
    }
}
