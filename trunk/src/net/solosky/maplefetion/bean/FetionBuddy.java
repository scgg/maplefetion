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
 * Package  : net.solosky.maplefetion.bean
 * File     : FetionBuddy.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-5
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;


/**
 *
 * 飞信好友 是指已经开通飞信的用户
 *
 * @author solosky <solosky772@qq.com>
 */
public class FetionBuddy extends Buddy
{
	
	/**
	 * 好友昵称
	 */
	protected String nickName;
	
	/**
	 * 真实姓名
	 */
	protected String trueName;
	
	/**
	 * 好友签名
	 */
	protected String impresa;
	
	/**
	 * 好友邮件
	 */
	protected String email;
	/**
	 * 级别
	 */
	protected int level;
	
	/**
	 * 好友扩展信息
	 */
	protected BuddyExtend extend;
	
	/**
	 * 权限设置
	 */
	protected Permission permission;
	
	/**
	 * 短信策略
	 */
	protected SMSPolicy smsPolicy;
	
	public FetionBuddy()
	{
		this.permission = new Permission();
		this.smsPolicy  = new SMSPolicy();
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
     * @return the level
     */
    public int getLevel()
    {
    	return level;
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
     * @return the permission
     */
    public Permission getPermission()
    {
    	return permission;
    }
    
    
	/**
     * @return the smsPolicy
     */
    public SMSPolicy getSMSPolicy()
    {
    	return smsPolicy;
    }
    
    

	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}

	/**
     * 返回可以显示的名字
     */
    public String getDisplayName()
    {
    	if(getLocalName()!=null && getLocalName().length()>0)
    		return getLocalName();
    	if(getNickName()!=null && getNickName().length()>0)
    		return getNickName();
    	if(getTrueName()!=null && getTrueName().length()>0)
    		return getTrueName();
    	if(getFetionId()>0)
    		return Integer.toString(getFetionId());
    	if(getMobile()!=0)
    		return Long.toString(getMobile());
    	return null;
    }
	
    
	
}
