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
 * File     : MessageTemplate.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-15
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.FetionClient;

/**
 *
 * 消息模板
 *
 * @author solosky <solosky772@qq.com>
 */
public class MessageTemplate
{
	public static final String TMPL_USER_AUTH = "<args><device type=\"PC\" version=\"0\" client-version=\""+FetionClient.PROTOCOL_VERSION+"\" /><caps value=\"{caps}\" /><events value=\"contact;permission;system-message;personal-group;compact\" /><user-info attributes=\"all\" /><presence><basic value=\"{presence}\" desc=\"\" /></presence></args>";
	public static final String TMPL_GET_PERSONAL_INFO = "<args><personal attributes=\"all\" /><services version=\"\" attributes=\"all\" /><quota attributes=\"all\" /></args>";
	public static final String TMPL_GET_CONTACT_LIST = "<args><contacts><buddy-lists /><buddies attributes=\"all\" /><mobile-buddies attributes=\"all\" /><chat-friends /><blacklist /><allow-list /></contacts></args>";
	public static final String TMPL_GET_CONTACTS_INFO = "<args><contacts attributes=\"provisioning;impresa;mobile-no;nickname;name;gender;portrait-crc;ivr-enabled\" extended-attributes=\"score-level\">{contactList}</contacts></args>";
	public static final String TMPL_GET_CONTACT_DETAIL = "<args><contacts attributes=\"all\" extended-attributes=\"score-level\"><contact {args}/></contacts></args>";
	public static final String TMPL_SUBSCRIBE = "<args><subscription><contacts>{contactList}</contacts></subscription></args>";
	public static final String TMPL_FETION_SHOW_1 = "<is-composing><state>fetion-show:";
	public static final String TMPL_FETION_SHOW_2 = "0x000101010000010001000000000000010000000</state></is-composing>";
	public static final String TMPL_INVATE_BUDDY = "<args><contacts><contact uri=\"{uri}\" /></contacts></args>";
	public static final String TMPL_ADD_BUDDY = "<args><contacts><buddies><buddy uri=\"{uri}\" buddy-lists=\"{cordId}\" {localName} desc=\"{desc}\" expose-mobile-no=\"1\" expose-name=\"1\" addbuddy-phrase-id=\"{promptId}\" /></buddies></contacts></args>";
	public static final String TMPL_ADD_MOBILE_BUDDY = "<args><contacts><mobile-buddies><mobile-buddy uri=\"{uri}\" buddy-lists=\"{cordId}\" desc=\"{desc}\" invite=\"0\" addbuddy-phrase-id=\"0\" /></mobile-buddies></contacts></args>";
	public static final String TMPL_DELETE_BUDDY = "<args><contacts><buddies><buddy uri=\"{uri}\" /></buddies></contacts></args>";
	public static final String TMPL_DELETE_MOBILE_BUDDY = "<args><contacts><mobile-buddies><mobile-buddy uri=\"{uri}\" /></mobile-buddies></contacts></args>";
	public static final String TMPL_APPLICATION_AGREED = "<args><contacts><buddies><buddy uri=\"{uri}\" result=\"1\" buddy-lists=\"\" expose-mobile-no=\"1\" expose-name=\"1\" /></buddies></contacts></args>";
	public static final String TMPL_APPLICATION_DECLINED = "<args><contacts><buddies><buddy uri=\"{uri}\" result=\"0\" /></buddies></contacts></args>";
	public static final String TMPL_SET_PERSONAL_INFO = "<args>{personal}</args>";
	public static final String TMPL_SET_BUDDY_LOCAL_NAME = "<args><contacts><buddies><buddy uri=\"{uri}\" local-name=\"{localName}\" /></buddies></contacts></args>";
	public static final String TMPL_SET_BUDDY_CORD = "<args><contacts><buddies><buddy uri=\"{uri}\" buddy-lists=\"{cordId}\" /></buddies></contacts></args>";
	public static final String TMPL_SET_PRESENCE = "<args><presence><basic value=\"{presence}\" /></presence></args>";
	
	public static final String TMPL_ADD_TO_BLACKLIST = "<args><contacts><blacklist><blocked uri=\"{uri}\" /></blacklist></contacts></args>";
	public static final String TMPL_REMOVE_FROM_BLACKLIST = "<args><contacts><blacklist><blocked uri=\"{uri}\" /></blacklist></contacts></args>";
	
	public static final String TMPL_CREATE_CORD = "<args><contacts><buddy-lists><buddy-list name=\"{title}\" /></buddy-lists></contacts></args>";
	public static final String TMPL_DELETE_CORD = "<args><contacts><buddy-lists><buddy-list id=\"{cordId}\" /></buddy-lists></contacts></args>";
	public static final String TMPL_UPDATE_CORD = "<args><contacts><buddy-lists><buddy-list id=\"{cordId}\" name=\"{title}\"/></buddy-lists></contacts></args>";
	
	public static final String TMPL_GET_GROUP_LIST = "<args><group-list attributes=\"name;identity\" version=\"{version}\"/></args>";
	public static final String TMPL_GET_GROUP_INFO = "<args><groups attributes=\"all\">{groupList}</groups></args>";
	public static final String TMPL_GET_MEMBER_LIST ="<args><groups attributes=\"member-uri;member-nickname;member-iicnickname;member-identity;member-t6svcid\">{groupList}</groups></args>";
	public static final String TMPL_SUBSCRIBE_GROUP_NOPTIFY = "<args><subscription><groups><group uri=\"{uri}\" /></groups><presence><basic attributes=\"all\" /><member attributes=\"identity\" /><management attributes=\"all\" /></presence></subscription></args>";
	public static final String TMPL_GROUP_KEEP_LIVE = "<is-composing><state>keep-alive</state></is-composing>";
	public static final String TMPL_GROUP_SET_PRESENCE ="<args><groups><group uri=\"{uri}\"><presence><basic value=\"{presence}\" client-type=\"PC\" /></presence></group></groups></args>";

	public static final String TMPL_GET_SCHEDULE_SMS_LIST = "<args><schedule-sms-list version=\"{version}\" /></args>";
	public static final String TMPL_GET_SCHEDULE_SMS_INFO = "<args><schedule-sms-list>{scheduleSMSList}</schedule-sms-list></args>";
	public static final String TMPL_CREATE_SCHEDULE_SMS = "<args><schedule-sms send-time=\"{sendDate}\"><message>{message}</message><receivers>{receiverList}</receivers></schedule-sms></args>";
	public static final String TMPL_DELETE_SCHEDULE_SMS = "<args><schedule-sms-list>{scheduleSMSList}</schedule-sms-list></args>";
}
