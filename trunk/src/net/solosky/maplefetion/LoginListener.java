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
 * File     : ActionListener.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-11
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;


/**
 *
 * 登录监听器
 *
 * @author solosky <solosky772@qq.com>
 */
public interface LoginListener
{
	
	/////////////////////////////////////////////////////////////////////
	/**
	 * 获取系统配置
	 */
	public final static int LOGIN_LOAD_LOCALE_SETTING_DOING = 0x101;
	
	/**
	 * 登陆账户服务器
	 */
	public final static int LOGIN_SSI_SIGN_IN_DOING = 0x102;
	
	/**
	 * 发送验证码
	 */
	public final static int LOGIN_SSI_SEND_VERIFY_CODE_DOING = 0x103;

	/**
	 * 主服务器登录
	 */
	public final static int LOGIN_SERVER_USER_LOGIN_DOING = 0x104;
	
	/**
	 * 群登录
	 */
	public final static int LOGIN_SERVER_GROUP_LOGIN_DOING = 0x105;
	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 登陆完成
	 */
	public final static int LOGIN_SUCCESS = 0x200;
	
	/**
	 * 获取自适应系统配置成功
	 */
	public final static int LOGIN_LOAD_LOCALE_SETTING_SUCCESS = 0x201;
	
	/**
	 * SSI登录成功
	 */
	public final static int LOGIN_SSI_SIGN_IN_SUCCESS = 0x202;
	
	/**
	 * 主服务器登录成功
	 */
	public final static int LOGIN_SERVER_USER_LOGIN_SUCCESS = 0x203;
	
	/**
	 * 群登录成功
	 */
	public final static int LOGIN_SERVER_GROUP_LOGIN_SUCCESS = 0x204;
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 用户已经停机
	 */
	public final static int LOGIN_USER_ACCOUNT_SUSPEND = 0x501;
	
	/**
	 * 用户需要验证
	 */
	public final static int LOGIN_SSI_NEED_VERIFY = 0x501;
	
	/**
	 * SSI用户密码验证失败
	 */
	public final static int LOGIN_SSI_AUTH_FAILED = 0x512;
	
	/**
	 * SSI验证码验证失败
	 */
	public final static int LOGIN_SSI_VERIFY_FAILED = 0x523;
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 连接主服务失败
	 */
	public final static int LOGIN_SERVER_CONNECT_FAILED = 0x421;
	
	/**
	 * 主服务器登录超时
	 */
	public final static int LOGIN_SERVER_LOGIN_TIMEOUT = 0x422;
	
	/**
	 * SSI连接服务器失败
	 */
	public final static int LOGIN_SSI_CONNECT_FAILED = 0x423;
	
	/**
	 * 获取本地化配置失败
	 */
	public final static int LOGIN_LOCALE_SEETING_CONNECT_FIALED = 0x424;
	
	/**
	 * 其他错误
	 */
	public final static int LOGIN_OHTER_FAILED = 0x440;
	
	/**
	 * 状态更改之后回调函数
	 * @param status	操作状态
	 */
	public void loginStatusUpdated(int status);
}
