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
 * File     : FetionUser.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;

/**
 *
 *	飞信用户
 *	
 *	代表了一个登陆用户
 *
 * @author solosky <solosky772@qq.com> 
 */
public class FetionUser extends FetionBuddy
{
	/**
	 * 用户密码
	 */
	private String password;
	
	/**
	 * 域名
	 */
	private String domain;
	
	/**
	 * 会话标志
	 */
	private String ssic;
	
	/**
	 * 默认构造函数
	 */
	public FetionUser(long mobileNo, String password,String domain)
	{
		this.setMobileNo(mobileNo);
		this.setPassword(password);
		this.domain = domain;
	}
	
	
	
	/**
     * @return the password
     */
    public String getPassword()
    {
    	return password;
    }



	/**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
    	this.password = password;
    }



	/**
     * @return the ssic
     */
    public String getSsic()
    {
    	return ssic;
    }

	/**
     * @param ssic the ssic to set
     */
    public void setSsic(String ssic)
    {
    	this.ssic = ssic;
    }



	/**
     * @return the domain
     */
    public String getDomain()
    {
    	return domain;
    }



	/**
     * @param domain the domain to set
     */
    public void setDomain(String domain)
    {
    	this.domain = domain;
    }
	
	
}
