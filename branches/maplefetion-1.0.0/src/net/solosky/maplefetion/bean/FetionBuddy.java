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
 * Package  : net.solosky.maplefetion.bean
 * File     : FetionBuddy.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;


/**
 *  飞信好友
 *
 * @author solosky <solosky772@qq.com> 
 */
public class FetionBuddy
{
	
	public static final int PRESENCE_PC_ONLINE = 400;
	public static final int PRESENCE_PC_OFFLINE = 000;
	public static final int PRESENCE_PC_BUSY = 600;
	public static final int PRESENCE_PC_AWAY = 100;
	public static final int PRESENCE_PC_HIDEN = 000;
	public static final int PRESENCE_MOBILE_ONLINE = 400;
	
	public static final int RELATION_STATUS_UNCONFIRMED = 000;
	public static final int RELATION_STATUS_AGREED = 001;
	public static final int RELATION_STATUS_DECLINED = 002;
	public static final int RELATION_STATUS_STRANGER = 003;
	public static final int RELATION_STATUS_BANNED = 004;
	
	/**
	 * 用户ID，每个飞信用户都有
	 */
	private int uid;
	
	/**
	 * 是否开通飞信
	 */
	private boolean isRegistered;
	
	/**
	 * 是否是好友
	 */
	private int relationStatus;
	/**
	 * 用户URI，唯一的
	 */
	private String uri;
	
	/**
	 * 飞信号
	 */
	private int sid;
	
	/**
	 * 所属分组编号
	 */
	private String cordId;
	
	/**
	 * 头像
	 */
	private String portrait;
	/**
	 * 在线状态
	 */
	private int presence;
	
	/**
	 * 用户手机号
	 */
	private long mobileNo;
	
	/**
	 * 好友昵称
	 */
	private String nickName;
	
	/**
     * 用户设置的好友备注 
     */
	private String localName;
	
	/**
	 * 真实姓名
	 */
	private String trueName;
	
	/**
	 * 性别
	 */
	private String gender;
	
	/**
	 * 心情短语
	 */
	private String impresa;
	
	/**
	 * 用户等级
	 */
	private int level;
	
	/**
	 * 用户的详细信息
	 */
	private BuddyExtend extend;
	
	/**
	 * 默认构造函数
	 */
    public FetionBuddy()
    {
    }

	/**
     * @return the uri
     */
    public String getUri()
    {
    	return uri;
    }

	/**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
    	this.uri = uri;
    	if(uri.startsWith("sip")) {
    		this.setSid(Integer.parseInt(uri.substring(4, uri.indexOf('@'))));
    	}else {
    		this.setMobileNo(Long.parseLong(uri.substring(4)));
    	}
    }

	/**
     * @return the sid
     */
    public int getSid()
    {
    	return sid;
    }

	/**
     * @param sid the sid to set
     */
    public void setSid(int sid)
    {
    	this.sid = sid;
    }

	/**
     * @return the mobileNo
     */
    public long getMobileNo()
    {
    	return this.mobileNo;
    }

	/**
     * @param mobileNo the mobileNo to set
     */
    public void setMobileNo(long mobileNo)
    {
    	this.mobileNo = mobileNo;
    }

	/**
     * @return the nickName
     */
    public String getNickName()
    {
    	return nickName;
    }

	/**
     * @param nickName the nickName to set
     */
    public void setNickName(String nickName)
    {
    	this.nickName = nickName;
    }

	/**
     * @return the localName
     */
    public String getLocalName()
    {
    	return localName;
    }

	/**
     * @param localName the localName to set
     */
    public void setLocalName(String localName)
    {
    	this.localName = localName;
    }

	/**
     * @return the trueName
     */
    public String getTrueName()
    {
    	return trueName;
    }

	/**
     * @param trueName the trueName to set
     */
    public void setTrueName(String trueName)
    {
    	this.trueName = trueName;
    }

	/**
     * @return the gender
     */
    public String getGender()
    {
    	return gender;
    }

	/**
     * @param gender the gender to set
     */
    public void setGender(String gender)
    {
    	this.gender = gender;
    }

	/**
     * @return the impresa
     */
    public String getImpresa()
    {
    	return impresa;
    }

	/**
     * @param impresa the impresa to set
     */
    public void setImpresa(String impresa)
    {
    	this.impresa = impresa;
    }

	/**
     * @return the level
     */
    public int getLevel()
    {
    	return level;
    }

	/**
     * @param level the level to set
     */
    public void setLevel(int level)
    {
    	this.level = level;
    }

	/**
     * @return the cordId
     */
    public String getCordId()
    {
    	return cordId;
    }

	/**
     * @param cordId the cordId to set
     */
    public void setCordId(String cordId)
    {
    	this.cordId = cordId;
    }

	/**
     * @return the uid
     */
    public int getUid()
    {
    	return uid;
    }

	/**
     * @param uid the uid to set
     */
    public void setUid(int uid)
    {
    	this.uid = uid;
    }

	/**
     * @return the isRegistered
     */
    public boolean isRegistered()
    {
    	return isRegistered;
    }

	/**
     * @param isRegistered the isRegistered to set
     */
    public void setRegistered(boolean isRegistered)
    {
    	this.isRegistered = isRegistered;
    }

	/**
     * @return the presence
     */
    public int getPresence()
    {
    	return presence;
    }

	/**
     * @param presence the presence to set
     */
    public void setPresence(int presence)
    {
    	this.presence = presence;
    }

	/**
     * @return the relationStatus
     */
    public int getRelationStatus()
    {
    	return relationStatus;
    }

	/**
     * @param relationStatus the relationStatus to set
     */
    public void setRelationStatus(int relationStatus)
    {
    	this.relationStatus = relationStatus;
    }
    
    
    /**
     * @return the extend
     */
    public BuddyExtend getExtend()
    {
    	return extend;
    }

	/**
     * @param extend the extend to set
     */
    public void setExtend(BuddyExtend extend)
    {
    	this.extend = extend;
    }

	/**
     * @return the portrait
     */
    public String getPortrait()
    {
    	return portrait;
    }

	/**
     * @param portrait the portrait to set
     */
    public void setPortrait(String portrait)
    {
    	this.portrait = portrait;
    }

	/**
     * 返回可以显示的名字
     */
    public String getDisplayName()
    {
    	if(localName!=null && localName.length()>0)
    		return localName;
    	if(nickName!=null && nickName.length()>0)
    		return nickName;
    	if(trueName!=null && trueName.length()>0)
    		return trueName;
    	if(sid>0)
    		return Integer.toString(sid);
    	if(mobileNo!=0)
    		return Long.toString(mobileNo);
    	return null;
    }
    
}
