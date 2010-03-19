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
 * File     : ActionStatus.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-11
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.sipc.SipcStatus;

/**
 *
 * 操作结果枚举
 *
 * @author solosky <solosky772@qq.com>
 */
public interface ActionStatus extends SipcStatus
{
	public static final int TIME_OUT = 600;		//超时
	public static final int IO_ERROR = 700;		//网络错误
	public static final int OTHER_ERROR = 800;	//其他未知错误
	
	public static final int INVALD_CORD_ID = 901;		//无效的分组编号
}