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
	SIPC_FAIL,			//是指Sipc返回的状态不是成功状态，如果无需区分错误的类型，就可以使用一般错误来简化代码的编写
	BUDDY_NOT_FOUND,	//好友不存在，是指用户存在，但不是好友
	USER_NOT_FOUND,		//用户不存在，无效的号码或者用户由于其他的原因无法访问
	UNKNOWN_FAIL,		//未知错误
}
