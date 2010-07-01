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
 * Package  : net.solosky.maplefetion.event.action
 * File     : FailureType.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-6-3
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.event.action;

/**
 *
 * 失败原因
 *
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public enum FailureType {
	/**
	 * 是指Sipc返回的状态不是成功状态（2xx），如果无需区分错误的类型，就可以使用一般错误来简化代码的编写
	 */
	SIPC_FAIL,
	
	/**
	 * 服务器繁忙，暂时无法响应请求
	 */
	SERVER_BUSY,
	
	/**
	 * 服务器内部错误
	 */
	SERVER_ERROR,	
	
	/**
	 * 请求的数据格式不对
	 */
	NOT_ACCEPTABLE,
	
	/**
	 * 好友不存在，是指用户存在，但不是好友
	 */
	BUDDY_NOT_FOUND,
	
	/**
	 * 用户不存在，无效的号码或者用户由于其他的原因无法访问
	 */
	USER_NOT_FOUND,
	
	/**
	 * 添加好友是好友已经存在
	 */
	BUDDY_EXISTS,
	
	/**
	 * 无效的账号
	 */
	INVALID_ACCOUNT,
	
	/**
	 * 定时短信定时时间不正确
	 */
	INVALID_SEND_DATE,
	
	/**
	 * 未知错误
	 */
	UNKNOWN_FAIL,	
}
