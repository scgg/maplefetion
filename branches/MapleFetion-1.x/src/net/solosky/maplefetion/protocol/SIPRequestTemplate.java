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
 * Package  : net.solosky.maplefetion.protocol.request
 * File     : SIPRequestTemplate.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol;

/**
 *	
 *	请求模板
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SIPRequestTemplate
{
	public static final String TMPL_USER_AUTH = "<args><device type=\"PC\" version=\"2009113004\" client-version=\"3.5.2540\" /><caps value=\"simple-im;im-session;temp-group;personal-group;im-relay;xeno-im;direct-sms;sms2fetion\" /><events value=\"contact;permission;system-message;personal-group;compact\" /><user-info attributes=\"all\" /><presence><basic value=\"400\" desc=\"\" /></presence></args>";
	public static final String TMPL_GET_PERSONAL_INFO = "<args><personal attributes=\"all\" /><services version=\"\" attributes=\"all\" /><quota attributes=\"all\" /></args>";
	public static final String TMPL_GET_CONTACT_LIST = "<args><contacts><buddy-lists /><buddies attributes=\"all\" /><mobile-buddies attributes=\"all\" /><chat-friends /><blacklist /><allow-list /></contacts></args>";
	public static final String TMPL_GET_CONTACTS_INFO = "<args><contacts attributes=\"provisioning;impresa;mobile-no;nickname;name;gender;portrait-crc;ivr-enabled\" extended-attributes=\"score-level\">{contactList}</contacts></args>";
	public static final String TMPL_GET_CONTACT_DETAIL = "<args><contacts attributes=\"all\" extended-attributes=\"score-level\"><contact uri=\"{uri}\" /></contacts></args>";
	public static final String TMPL_SUBSCRIBE = "<args><subscription><contacts>{contactList}</contacts></subscription></args>";
	public static final String TMPL_FETION_SHOW_1 = "<is-composing><state>fetion-show:";
	public static final String TMPL_FETION_SHOW_2 = "0x000101010000010001000000000000010000000</state></is-composing>";
	public static final String TMPL_INVATE_BUDDY = "<args><contacts><contact uri=\"{uri}\" /></contacts></args>";
	public static final String TMPL_ADD_BUDDY = "<args><contacts><buddies><buddy uri=\"{uri}\" buddy-lists=\"{cordId}\" desc=\"{desc}\" expose-mobile-no=\"1\" expose-name=\"1\" addbuddy-phrase-id=\"{promptId}\" /></buddies></contacts></args>";
	public static final String TMPL_ADD_MOBILE_BUDDY = "<args><contacts><mobile-buddies><mobile-buddy uri=\"{uri}\" buddy-lists=\"{cordId}\" desc=\"{desc}\" invite=\"0\" addbuddy-phrase-id=\"0\" /></mobile-buddies></contacts></args>";
	public static final String TMPL_DELETE_BUDDY = "<args><contacts><buddies><buddy uri=\"{uri}\" /></buddies></contacts></args>";
	public static final String TMPL_DELETE_MOBILE_BUDDY = "<args><contacts><mobile-buddies><mobile-buddy uri=\"{uri}\" /></mobile-buddies></contacts></args>";
	public static final String TMPL_APPLICATION_AGREED = "<args><contacts><buddies><buddy uri=\"{uri}\" result=\"1\" buddy-lists=\"\" expose-mobile-no=\"1\" expose-name=\"1\" /></buddies></contacts></args>";
	public static final String TMPL_APPLICATION_DECLINED = "<args><contacts><buddies><buddy uri=\"{uri}\" result=\"0\" /></buddies></contacts></args>";
	public static final String TMPL_SET_PERSONAL_INFO = "<args><personal {attrs}/></args>";
	public static final String TMPL_SET_BUDDY_LOCAL_NAME = "<args><contacts><buddies><buddy uri=\"{uri}\" local-name=\"{localName}\" /></buddies></contacts></args>";
	public static final String TMPL_SET_PRESENCE = "<args><presence><basic value=\"{presence}\" /></presence></args>";
}
