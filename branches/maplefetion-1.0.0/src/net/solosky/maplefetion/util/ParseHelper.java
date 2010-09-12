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
 * Package  : net.solosky.maplefetion.util
 * File     : ParseHelper.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-30
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.text.DateFormat;
import java.text.ParseException;

import org.jdom.Element;

import net.solosky.maplefetion.bean.BuddyExtend;
import net.solosky.maplefetion.bean.FetionBuddy;

/**
 *
 *	工具类，解析XML到对象
 *
 * @author solosky <solosky772@qq.com> 
 */
public class ParseHelper
{
	/**
	 * 解析基本的好友信息
	 * @param buddy
	 * @param personal
	 */
	public static void parseBuddyPersonalBasic(FetionBuddy buddy, Element personal)
	{
		if(buddy==null || personal==null)	return;

		//这下面就是读取好友信息的详细字段，这里只实现了一部分，其余的下个版本添加。。
		String userId = personal.getAttributeValue("user-id");
		if(userId!=null && userId.length()>0)
			buddy.setUid(Integer.parseInt(userId));										//用户编号，这个不是飞信号
		buddy.setGender(personal.getAttributeValue("gender"));							//好友性别
		buddy.setImpresa(personal.getAttributeValue("impresa"));						//好友签名
		buddy.setNickName(personal.getAttributeValue("nickname"));						//好友昵称
		buddy.setPortrait(personal.getAttributeValue("portrait-crc"));					//头像
		String mobileNo = personal.getAttributeValue("mobile-no");
		if(mobileNo!=null && mobileNo.length()>0)
			buddy.setMobileNo(Long.parseLong(personal.getAttributeValue("mobile-no")));	//手机号码
		
		//TODO ..下个版本添加好友的其他属性
	}
	
	
	/**
	 * 解析好友的详细信息
	 * @param buddy
	 * @param personal
	 */
	public static void parseBuddyPersonalExtend(FetionBuddy buddy, Element personal)
	{
		if(buddy==null || personal==null)	return;

		//检查好友详细信息是否有效
		BuddyExtend extend = buddy.getExtend();
		if(extend==null) {
			extend = new BuddyExtend();
			buddy.setExtend(extend);
		}
		
		//逐个解析详细信息
		try {
	        extend.setBirth(DateFormat.getInstance().parse(personal.getAttributeValue("birth-date")));
        } catch (Exception e) {
        	extend.setBirth(null);
        }
		extend.setBlood( personal.getAttributeValue("blood-type"));
		extend.setNation( personal.getAttributeValue("nation"));
		extend.setProvince( personal.getAttributeValue("province"));
		extend.setCity( personal.getAttributeValue("city"));
		extend.setHobby( personal.getAttributeValue("hobby"));
		extend.setHoroscope(personal.getAttributeValue("horoscope"));
		extend.setLunarAnimal(personal.getAttributeValue("lunar-animal"));
		extend.setOccupation(personal.getAttributeValue("occupation"));
		extend.setEmail(personal.getAttributeValue("personal-email"));
	}
	
}
