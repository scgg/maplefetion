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
 * Package  : net.solosky.maplefetion
 * File     : LoginState.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-3-31
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

/**
 *
 * 登录状态枚举
 * 
 * <pre>
 * 用户可以在NotifyEventListener监听LoginStateEvent事件来获取登录状态改变
 * 
 * 所有的状态代码均为16进制
 * 1xx：为中间状态，表示正在进行登陆中的某一步
 * 2xx：为成功状态，表示登陆过程中的某一步成功了，特别地，0x200表示登陆的整个操作成功了
 * 4xx: 登陆过程中某一步失败了
 * 6xx: 连接失败错误
 * 7xx: 其他错误
 *</pre>
 * @author solosky <solosky772@qq.com>
 */
public enum LoginState {
	//操作状态
	/**
	 * 加载自适应配置
	 */
	SEETING_LOAD_DOING(0x102),	
	
	/**
	 * SSI登录
	 */
	SSI_SIGN_IN_DOING(0x103),		//SSI登录
	
	/**
	 * 注册SIPC服务器
	 */
	SIPC_REGISTER_DOING(0x104),	
	
	/**
	 * 获取联系人信息
	 */
	GET_CONTACTS_INFO_DOING(0x105),	
	
	/**
	 * 获取群消息
	 */
	GET_GROUPS_INFO_DOING(0x106),
	
	/**
	 * 注册群
	 */
	GROUPS_REGISTER_DOING(0x107),
	
	
	//操作成功状态
	/**
	 * //登录成功
	 */
	LOGIN_SUCCESS(0x200),
	
	/**
	 * 加载自适应配置成功
	 */
	SETTING_LOAD_SUCCESS(0x201),
	
	/**
	 * SSI登录成功
	 */
	SSI_SIGN_IN_SUCCESS(0x202),
	
	/**
	 * SIPC注册成功
	 */
	SIPC_REGISGER_SUCCESS(0x203),
	
	/**
	 * 获取联系人信息成功
	 */
	GET_CONTACTS_INFO_SUCCESS(0x204),
	
	/**
	 * 获取群信息成功
	 */
	GET_GROUPS_INFO_SUCCESS(0x205),
	
	/**
	 * 注册群成功
	 */
	GROUPS_REGISTER_SUCCESS(0x206),
	
	
	//操作失败状态
	/**
	 * 加载自适应配置失败
	 */
	SETTING_LOAD_FAIL(0x402),	
	
	/**
	 * SSI需要图片验证
	 */
	SSI_NEED_VERIFY(0x403),	
	
	/**
	 * 图片验证失败
	 */
	SSI_VERIFY_FAIL(0x404),	
	
	/**
	 * SSI验证失败
	 */
	SSI_AUTH_FAIL(0x405),
	
	/**
	 * 用户停机
	 */
	SSI_ACCOUNT_SUSPEND(0x406),	
	
	/**
	 * 无效的手机号或者飞信
	 */
	SSI_ACCOUNT_NOT_FOUND(0x407),
	
	/**
	 * 获取联系人信息失败
	 */
	GET_CONTACTS_INFO_FAIL(0x408),
	
	/**
	 * 获取群信息失败
	 */
	GET_GROUPS_INFO_FAIL(0x409),
	
	/**
	 * 注册群失败
	 */
	GROUPS_REGISTER_FAIL(0x40A),
	
	/**
	 * SSI连接失败
	 */
	SSI_CONNECT_FAIL(0x601),
	
	/**
	 * SIPC服务器连接失败
	 */
	SIPC_CONNECT_FAIL(0x602),
	
	/**
	 * SIPC服务器注册失败
	 */
	SIPC_REGISTER_FAIL(0x603),	
	
	/**
	 * SIPC服务器注册超时
	 */
	SIPC_TIMEOUT(604),
	
	
	/**
	 * 其他未知错误
	 */
	OHTER_ERROR(0x701),
	
	;
	
	private int value;
	
	LoginState(int code)
	{
		this.value = code;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
	
}
