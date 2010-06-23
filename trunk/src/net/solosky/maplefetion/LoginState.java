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
 * 用户可以在NotifyListener.loginStateChanged()监听登录状态改变
 *
 * @author solosky <solosky772@qq.com>
 */
public enum LoginState {
	//操作状态
	NETWORK_CHECK_DOING(0x101),		//检查网络是否畅通
	SEETING_LOAD_DOING(0x102),		//加载自适应配置
	SSI_SIGN_IN_DOING(0x103),		//SSI登录
	SIPC_REGISTER_DOING(0x104),		//注册SIPC服务器
	GET_CONTACTS_INFO_DOING(0x105),	//获取联系人信息
	GET_GROUPS_INFO_DOING(0x106),	//获取群消息
	GROUPS_REGISTER_DOING(0x107),	//注册群
	
	
	//操作成功状态
	SETTING_LOAD_SUCCESS(0x201),	//加载自适应配置成功
	SSI_SIGN_IN_SUCCESS(0x202),		//SSI登录成功
	SIPC_REGISGER_SUCCESS(0x203),	//SIPC注册成功
	GET_CONTACTS_INFO_SUCCESS(0x204),	//获取联系人信息
	GET_GROUPS_INFO_SUCCESS(0x205),	//获取群消息
	GROUPS_REGISTER_SUCCESS(0x206),	//注册群成功
	
	
	//操作失败状态
	NETWORK_UNAVAILABLE(0x401),		//未连接上网络
	SETTING_LOAD_FAIL(0x402),		//加载自适应配置失败
	SSI_NEED_VERIFY(0x403),			//SSI需要图片验证
	SSI_VERIFY_FAIL(0x404),			//图片验证失败
	SSI_AUTH_FAIL(0x405),			//SSI验证失败
	SSI_ACCOUNT_SUSPEND(0x406),		//用户停机
	SSI_ACCOUNT_NOT_FOUND(0x407),	//无效的手机号或者飞信
	GET_CONTACTS_INFO_FAIL(0x408),	//获取联系人信息失败
	GET_GROUPS_INFO_FAIL(0x409),	//获取群信息失败
	GROUPS_REGISTER_FAIL(0x40A),	//注册群失败
	
	
	SSI_CONNECT_FAIL(0x601),			//SSI连接失败
	SIPC_CONNECT_FAIL(0x602),			//SIPC服务器连接失败
	SIPC_REGISTER_FAIL(0x603),			//SIPC服务器注册失败
	SIPC_TIMEOUT(604),					//SIPC服务器注册超时
	
	OHTER_ERROR(0x701),					//其他未知错误
	
	LOGIN_SUCCESS(0x200)				//登录成功
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
