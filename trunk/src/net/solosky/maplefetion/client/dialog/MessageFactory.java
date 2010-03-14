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

package net.solosky.maplefetion.client.dialog;

import java.util.Collection;
import java.util.Iterator;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.net.Port;
import net.solosky.maplefetion.sipc.SipcBody;
import net.solosky.maplefetion.sipc.SipcHeader;
import net.solosky.maplefetion.sipc.SipcMethod;
import net.solosky.maplefetion.sipc.SipcReceipt;
import net.solosky.maplefetion.sipc.SipcRequest;
import net.solosky.maplefetion.util.AuthGenerator;
import net.solosky.maplefetion.util.NodeBuilder;

/**
 *	
 *	默认的SIP信令建立工厂
 *
 * @author solosky <solosky772@qq.com> 
 */
public class MessageFactory
{
	/**
	 * CALLID
	 */
	private int callID;
	
	private int sequence;
	
	private User user;
	
	private String lastMethod;
	
	
	/**
	 * 构造函数
	 * @param sid		飞信号
	 * @param domain	飞信域
	 */
	public MessageFactory(User user)
	{
		this.user = user;
		this.callID = 0;
		this.lastMethod = "";
		this.sequence = 1;
	}
	
	/**
	 * 创建默认的SipcRequest
	 * @param m
	 * @return
	 */
    public SipcRequest createDefaultSipcRequest(String m)
    {
	    SipcRequest req = new SipcRequest(m,this.user.getDomain());
	    req.addHeader(SipcHeader.FROM, Integer.toString(this.user.getFetionId()));
	    if(m.equals(this.lastMethod)) {
	    	req.addHeader(SipcHeader.CALLID,   Integer.toString(this.callID));
	    	req.addHeader(SipcHeader.SEQUENCE, Integer.toString(this.getNextSequence())+" "+m);
	    }else {
	    	req.addHeader(SipcHeader.CALLID,   Integer.toString(this.getNextCallID()));
	    	req.addHeader(SipcHeader.SEQUENCE, "1 "+m);
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
    public SipcRequest createServerRegisterRequest(int presence)
    {
    	 SipcRequest req =this.createDefaultSipcRequest(SipcMethod.REGISTER);
         req.setBody(new SipcBody(MessageTemplate.TMPL_USER_AUTH.replace("{presence}", Integer.toString(presence))));
         
         return req;
    }
    
    /**
     * 用户登录验证
     * @return
     */
    public SipcRequest createUserAuthRequest(String nonce, int presence)
    {
    	SipcRequest  req = this.createDefaultSipcRequest(SipcMethod.REGISTER);
    	
    	AuthGenerator auth = new AuthGenerator(Integer.toString(this.user.getFetionId()), this.user.getPassword(), this.user.getDomain(), nonce);
    	auth.generate();
    	
    	String authString = "Digest algorithm=\"SHA1-sess\",response=\""
    					+auth.getResponse()+"\",cnonce=\""+auth.getCnonce()
    					+"\",salt=\""+auth.getSalt()+"\",ssic=\""+user.getSsic()+"\"";
    	req.addHeader(SipcHeader.AUTHORIZATION, authString);
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_USER_AUTH.replace("{presence}", Integer.toString(presence))));
    	
    	return req;
    }
    
    /**
     * 获取个人详细信息
     * @return
     */
    public SipcRequest createGetPersonalInfoRequest()
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
	    req.addHeader(SipcHeader.EVENT, "GetPersonalInfo");
    	req.setBody(new SipcBody(MessageTemplate.TMPL_GET_PERSONAL_INFO));
    	
    	return req;
    }
    
    /**
     * 发送在线消息
     */
    public SipcRequest createSendChatMessageRequest(String toUri, Message m)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.MESSAGE);
    	
    	req.addHeader(SipcHeader.TO, toUri);
    	//req.addHeader(SipcHeader.CONTENT_TYPE, "text/plain");text/html-fragment
    	req.addHeader(SipcHeader.CONTENT_TYPE, "text/html-fragment");
    	req.addHeader(SipcHeader.EVENT, "CatMsg");
    	
    	req.setBody(new SipcBody(m.toString()));
    	
    	return req;
    }
    
    /**
     * 发送手机短消息
     * @param uri
     * @param m
     * @return
     */
    public SipcRequest createSendSMSRequest(String uri, Message m)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.MESSAGE);
    	
    	req.addHeader(SipcHeader.TO, uri);
    	req.addHeader(SipcHeader.EVENT, "SendCatSMS");
    	
    	req.setBody(new SipcBody(m.getText()));
    	
    	return req;
    }
    
    /**
     * 保持在线的请求
     * 也就是需要每隔一定时间需要注册一次
     * @return
     */
    public SipcRequest createKeepAliveRequest()
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.REGISTER);
    	
    	return req;
    }

    /**
     *  获取联系人详细信息
     * @param buddyList
     * @return
     */
    public SipcRequest createGetContactsInfoRequest(Collection<Buddy> buddyList)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	StringBuffer buffer = new StringBuffer();
    	Iterator<Buddy> it = buddyList.iterator();
    	String contactTemplate = "<contact uri=\"{uri}\" />";
    	while(it.hasNext()){
    		Buddy b = it.next();
    		if(b instanceof FetionBuddy)
    			buffer.append(contactTemplate.replace("{uri}", b.getUri()));
    	}
    	String body = MessageTemplate.TMPL_GET_CONTACTS_INFO;
    	body = body.replace("{contactList}", buffer.toString());
    	req.setBody(new SipcBody(body));
    	
    	req.addHeader(SipcHeader.EVENT, "GetContactsInfo");
    	
    	
    	return req;
    }
    
    /**
     *  获取联系人详细信息
     * @param buddyList
     * @return
     */
    public SipcRequest createGetContactDetailRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	String body = MessageTemplate.TMPL_GET_CONTACT_DETAIL;
    	body = body.replace("{uri}", uri);
    	req.setBody(new SipcBody(body));
    	
    	req.addHeader(SipcHeader.EVENT, "GetContactsInfo");
    	
    	return req;
    }
    
    /**
     * 获取联系人列表
     */
    public SipcRequest createGetContactListRequest()
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	req.addHeader(SipcHeader.EVENT, "GetContactList");
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_GET_CONTACT_LIST));
    	return req;
    }
    
    /**
     * 订阅异步通知
     */
    public SipcRequest createSubscribeRequest(Collection<Buddy> buddyList)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SUBSCRIBE);
    	req.setSequence("2 SUB");
    	
    	StringBuffer buffer = new StringBuffer();
    	Iterator<Buddy> it = buddyList.iterator();
    	String contactTemplate = "<contact uri=\"{uri}\" type=\"{type}\" />";
    	while(it.hasNext()){
    		Buddy b = it.next();
    		String t = contactTemplate;
    		if(b instanceof FetionBuddy) {
    			t = contactTemplate.replace("{type}", "3");
    		}else {
    			t = contactTemplate.replace("{type}", "2");
    		}
    		t = t.replace("{uri}", b.getUri());
    		buffer.append(t);
    	}
    	String body = MessageTemplate.TMPL_SUBSCRIBE;
    	body = body.replace("{contactList}", buffer.toString());
    	req.setBody(new SipcBody(body));
    	
    	req.addHeader(SipcHeader.EVENT, "compactlist");
    	
    	return req;
    }
    
    /**
     * 开始聊天请求
     */
    public SipcRequest createStartChatRequest()
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	req.addHeader(SipcHeader.EVENT,"StartChat");
    	return req;
    }
    
    /**
     * 注册聊天服务器
     */
    public SipcRequest createRegisterChatRequest(String ticket)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.REGISTER);
    	req.addHeader(SipcHeader.AUTHORIZATION,"TICKS auth=\""+ticket+"\"");
    	req.addHeader(SipcHeader.SUPPORTED,"text/html-fragment");
    	req.addHeader(SipcHeader.SUPPORTED,"multiparty");
    	req.addHeader(SipcHeader.SUPPORTED,"nudge");
    	req.addHeader(SipcHeader.SUPPORTED,"share-background");
    	//req.addHeader(SipcHeader.FIELD_SUPPORTED,"fetion-show");
    	
    	return req;
    }
    
    /**
     * 邀请好友加入会话
     */
    public SipcRequest createInvateBuddyRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	String body = MessageTemplate.TMPL_INVATE_BUDDY;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SipcHeader.EVENT,"InviteBuddy");
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 飞信秀（有空再研究）
     */
    public SipcRequest createFetionShowRequest()
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.INFO);
//    	ByteArrayBuffer buffer = new ByteArrayBuffer(100);
//    	buffer.append(MessageTemplate.TMPL_FETION_SHOW_1.getBytes(), 0,MessageTemplate.TMPL_FETION_SHOW_1.getBytes().length);
//    	buffer.append(0xE5);
//    	buffer.append(0x9B);	//飞信太变态了，这里居然有几个字节无法用字符表示
//    	buffer.append(0xA7);
//    	buffer.append(MessageTemplate.TMPL_FETION_SHOW_2.getBytes(), 0,MessageTemplate.TMPL_FETION_SHOW_2.getBytes().length);
//    	
//    	byte[] bodyArr = buffer.toByteArray();
//    	req.setBody(new SipcBody(new String(bodyArr)));
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
    public SipcRequest createAddBuddyRequest(String uri, int promptId, int cordId, String desc)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	String body = MessageTemplate.TMPL_ADD_BUDDY;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{promptId}", Integer.toString(promptId));
    	body = body.replace("{cordId}", "");
    	//body = body.replace("{cordId}", Integer.toString(cordId));
    	body = body.replace("{desc}", desc);
    	
    	req.addHeader(SipcHeader.EVENT,"AddBuddy");
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 添加手机好友请求
     * @param uri		好友的URI，这里应该是tel:159xxxxx
     * @param cordId	添加的组编号
     * @param desc		对好友的自我描述
     * @return
     */
    public SipcRequest createAddMobileBuddyRequest(String uri, int cordId, String desc)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	String body = MessageTemplate.TMPL_ADD_MOBILE_BUDDY;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{cordId}", "");
    	//body = body.replace("{cordId}", Integer.toString(cordId));
    	body = body.replace("{desc}", desc);
    	
    	req.addHeader(SipcHeader.EVENT,"AddMobileBuddy");
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 删除好友
     * @param uri
     * @return
     */
    public SipcRequest createDeleteBuddyRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	String body = MessageTemplate.TMPL_DELETE_BUDDY;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SipcHeader.EVENT,"DeleteBuddy");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 删除手机好友
     * @param uri  好友手机uri(类似tel:159xxxxxxxx)
     * @return
     */
    public SipcRequest createDeleteMobileBuddyRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	String body = MessageTemplate.TMPL_DELETE_MOBILE_BUDDY;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SipcHeader.EVENT,"DeleteMobileBuddy");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 同意对方添加好友
     * @param uri
     * @param localName
     * @param cordId
     * @return
     */
    public SipcRequest createAgreeApplicationRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	String body = MessageTemplate.TMPL_APPLICATION_AGREED;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SipcHeader.EVENT,"HandleContactRequest");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 拒绝陌生人添加好友请求
     * @param uri
     * @return
     */
    public SipcRequest createDeclineApplicationRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	String body = MessageTemplate.TMPL_APPLICATION_DECLINED;
    	body = body.replace("{uri}", uri);
    	
    	req.addHeader(SipcHeader.EVENT,"HandleContactRequest");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 更改个人资料
     * 这里只支持更改昵称和个性签名
     * @param updateXML 更新的XML
     * @return
     */
    public SipcRequest createSetPersonalInfoRequest()
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	NodeBuilder builder = new NodeBuilder();
    	//因为用户可以改变自己的信息，这里权限改变了所以不使用BeanHelper来处理
    	builder.add("nickname", user.getNickName());
    	builder.add("impresa", user.getImpresa());
    	//用户扩展信息。.TODO ..
        //BeanHelper.toUpdateXML(BuddyExtend.class, this.client.getFetionUser(), builder);
    	
    	String body = MessageTemplate.TMPL_SET_PERSONAL_INFO;
    	body = body.replace("{personal}", builder.toXML("personal"));
    	
    	req.addHeader(SipcHeader.EVENT,"SetPersonalInfo");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 设置好友本地姓名
     * @param uri		好友飞信地址
     * @param localName	本地显示名字
     * @return
     */
    public SipcRequest createSetBuddyLocalName(String uri, String localName)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	String body = MessageTemplate.TMPL_SET_BUDDY_LOCAL_NAME;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{localName}", localName);
    	
    	req.addHeader(SipcHeader.EVENT,"SetBuddyInfo");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 设置好友分组信息
     * @param uri
     * @param cordId
     * @return
     */
    public SipcRequest createSetBuddyCord(String uri, String cordId)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	String body = MessageTemplate.TMPL_SET_BUDDY_CORD;
    	body = body.replace("{uri}", uri);
    	body = body.replace("{cordId}", cordId!=null?cordId:"");
    	
    	req.addHeader(SipcHeader.EVENT,"SetBuddyLists");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    
    /**
     * 设置在线状态
     */
    public SipcRequest createSetPresenceRequest(int presence)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	
    	String body = MessageTemplate.TMPL_SET_PRESENCE;
    	body = body.replace("{presence}", Integer.toString(presence));
    	
    	req.addHeader(SipcHeader.EVENT,"SetPresence");
    	
    	req.setBody(new SipcBody(body));
    	return req;
    }
    /**
     * 退出客户端
     */
    public SipcRequest createLogoutRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.BYE);
    	
    	req.addHeader(SipcHeader.TO, uri);
    	return req;
    }
    
    //////////////////////////////////////群操作///////////////////////////////////////////////
    /**
     * 获取群列表
     */
    public SipcRequest createGetGroupListRequest(int localVersion)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	req.addHeader(SipcHeader.EVENT, "PGGetGroupList");
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_GET_GROUP_LIST.replace("{version}", Integer.toString(localVersion))));
    	
    	return req;
    }
    
    /**
     * 获取群信息
     */
    public SipcRequest createGetGroupInfoRequest(Collection<Group> groupList)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	req.addHeader(SipcHeader.EVENT, "PGGetGroupInfo");
    	
    	Iterator<Group> it = groupList.iterator();
    	StringBuffer buffer = new StringBuffer();
    	String node = "<group uri=\"{uri}\" />";
    	while(it.hasNext()){
    		buffer.append(node.replace("{uri}", it.next().getUri()));
    	}
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_GET_GROUP_INFO.replace("{groupList}", buffer.toString())));
    	
    	return req;
    }
    
    /**
     * 获取群成员列表
     */
    public SipcRequest createGetMemberListRequest(Collection<Group> groupList)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	req.addHeader(SipcHeader.EVENT, "PGGetGroupMembers");
    	
    	Iterator<Group> it = groupList.iterator();
    	StringBuffer buffer = new StringBuffer();
    	String node = "<group uri=\"{uri}\" />";
    	while(it.hasNext()){
    		buffer.append(node.replace("{uri}", it.next().getUri()));
    	}
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_GET_MEMBER_LIST.replace("{groupList}", buffer.toString())));
    	
    	return req;
    }
    
    /**
     * 订阅群通知
     */
    public SipcRequest createSubscribeGroupNotifyRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SUBSCRIBE);
    	req.addHeader(SipcHeader.EVENT, "PGPresence");
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_SUBSCRIBE_GROUP_NOPTIFY.replace("{uri}", uri)));
    	
    	return req;
    }
    
    
    /**
     * 开始群会话
     */
    public SipcRequest createGroupInviteRequest(String uri, Port localPort)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.INVATE);
    	req.addHeader(SipcHeader.TO, uri);
    	req.addHeader(SipcHeader.SUPPORTED,"text/html-fragment");
    	//req.addHeader(SipcHeader.SUPPORTED, "text/plain");
    	req.addHeader(SipcHeader.SUPPORTED,"multiparty");
    	req.addHeader(SipcHeader.SUPPORTED,"nudge");
    	req.addHeader(SipcHeader.SUPPORTED,"share-background");
    	req.addHeader(SipcHeader.SUPPORTED,"fetion-show");
    	
    	req.setNeedReplyTimes(2);	// 需回复两次
    	
    	//正文是一些固定的参数
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("v=0\r\n");
    	buffer.append("o=-0 0 IN "+localPort.toString()+"\r\n");
    	buffer.append("s=session\r\n");
    	buffer.append("c=IN IP4 "+localPort.toString()+"\r\n");
    	buffer.append("t=0 0\r\n");
    	buffer.append("m=message "+Integer.toString(localPort.getPort())+" sip "+uri);
    	
    	req.setBody(new SipcBody(buffer.toString()));
    	
    	return req;
    }
    
    
    /**
     * 确认会话收到请求
     */
    public SipcRequest createGroupAckRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.ACK);
    	req.addHeader(SipcHeader.TO, uri);
    	
    	req.setNeedReplyTimes(0);
    	return req;
    	
    }
    
    /**
     * 群在线请求
     */
    public SipcRequest createGroupKeepLiveRequest(String uri)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.INFO);
    	req.addHeader(SipcHeader.TO, uri);
    	
    	req.setBody(new SipcBody(MessageTemplate.TMPL_GROUP_KEEP_LIVE));
    	
    	return req;
    }
    
    /**
     * 群消息
     */
    public SipcRequest createSendGroupChatMessageRequest(String uri, String message)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.MESSAGE);
    	req.addHeader(SipcHeader.CONTENT_TYPE, "text/html-fragment");
    	//req.addHeader(SipcHeader.CONTENT_TYPE, "text/plain");
    	req.addHeader(SipcHeader.SUPPORTED, "SaveHistory");
    	req.addHeader(SipcHeader.TO, uri);
    	req.setBody(new SipcBody(message));
    	
    	return req;
    }
     
    /**
     * 设置群状态
     */
    public SipcRequest createSetGroupPresenceRequest(String uri, int presense)
    {
    	SipcRequest req = this.createDefaultSipcRequest(SipcMethod.SERVICE);
    	req.addHeader(SipcHeader.EVENT, "PGSetPresence");
    	String body = MessageTemplate.TMPL_GROUP_SET_PRESENCE;
		body = body.replace("{uri}", uri);
		body = body.replace("{presence}", Integer.toString(presense));
    	
    	req.setBody(new SipcBody(body));
    	
    	return req;
    }
    
    ///////////////////////////////////////收据///////////////////////////////////////////////
    
    /**
     * 默认收据
     */
    public SipcReceipt createDefaultReceipt(String callId, String sequence)
    {
    	SipcReceipt receipt = new SipcReceipt(200, "OK");
    	receipt.addHeader(SipcHeader.CALLID, callId);
    	receipt.addHeader(SipcHeader.SEQUENCE, sequence);
    	
    	return receipt;
    }
    
    /**
     * 信息收到收据
     */
    public SipcReceipt createChatMessageReceipt(String fromUri, String callId,String sequence)
    {
    	SipcReceipt receipt = this.createDefaultReceipt(callId, sequence);
    	receipt.addHeader(SipcHeader.FROM, fromUri);
    	
    	return receipt;
    }
    
    /**
     * 飞信秀收据
     */
    public SipcReceipt createFetionShowReceipt(String fromUri, String callId,String sequence)
    {
    	SipcReceipt receipt = this.createDefaultReceipt(callId, sequence);
    	receipt.addHeader(SipcHeader.FROM, fromUri);
    	
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
