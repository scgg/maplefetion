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
 * Package  : net.solosky.maplefetion
 * File     : LoginListener.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-23
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

/**
 * 
 * 登陆过程监听器
 * 
 * 在GUI界面中，可以使用监听器监听登陆过程
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface ILoginListener
{
	/**
	 * 初始化
	 */
	public final static int LOGIN_INIT = 0x100;
	
	/**
	 * 获取系统配置
	 */
	public final static int LOGIN_QUERY_SYSTEM_CONFIG = 0x101;
	
	/**
	 * 登陆账户服务器
	 */
	public final static int LOGIN_SSI_SIGN_IN = 0x102;

	/**
	 * 连接主服务器
	 */
	public final static int LOGIN_SERVER_CONNECT = 0x103;
	
	/**
	 * 发送登陆请求
	 */
	public final static int LOGIN_SERVER_REGISTER = 0x104;
	
	/**
	 * 发送验证请求
	 */
	public final static int LOGIN_SERVER_AUTH = 0x105;
	
	/**
	 * 获取用户信息
	 */
	public final static int LOGIN_SERVER_GET_PERSONAL_INFO = 0x106;
	
	/**
	 * 获取好友列表
	 */
	public final static int LOGIN_SERVER_GET_CONTECT_LIST = 0x107;
	
	/**
	 * 获取群列表
	 */
	public final static int LOGIN_SERVER_GET_GROUP_LIST = 0x108;
	
	/**
	 * 订阅异步通知
	 */
	public final static int LOGIN_SERVER_SUB_NOTIFY = 0x109;
	
	/**
	 * 获取好友详细信息
	 */
	public final static int LOGIN_SERVER_GET_CONTACTS_INFO = 0x110;
	
	/**
	 * 登陆完成
	 */
	public final static int LOGIN_SUCCESS = 0x200;
	
	/**
	 * 用户已经停机
	 */
	public final static int LOGIN_USER_OVER_DRAW = 0x401;
	
	/**
	 * 连接SSI服务失败
	 */
	public final static int LOGIN_SSI_CONNECT_FALIED = 0x411;
	
	/**
	 * SSI验证失败
	 */
	public final static int LOGIN_SSI_AUTH_FAILED = 0x412;
	
	/**
	 * 连接主服务失败
	 */
	public final static int LOGIN_SERVER_CONNECT_FAILED = 0x421;
	
	/**
	 * 连接主服务验证失败
	 */
	public final static int LOGIN_SERVER_AUTH_FAILED = 0x422;
	
	
	/**
	 * 未知异常
	 */
	public final static int LOGIN_UNKOWN_FAILED = 0x440;
	
	/**
	 * 登陆状态改变
	 * @param i
	 */
	public void loginStatusChanged(int i);
}
