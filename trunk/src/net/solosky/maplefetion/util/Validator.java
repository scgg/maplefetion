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
 * Package  : net.solosky.maplefetion.util
 * File     : Validator.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-4-22
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

/**
 *
 * 验证工具 用于验证手机号是否合法等
 *
 * @author solosky <solosky772@qq.com>
 */
public class Validator
{
	/**
	 * 判断手机号是否合法
	 * @param mobile	手机号码
	 * @return	是否合法
	 */
	public static boolean validateMobile(long mobile)
	{
		String ms = Long.toString(mobile);
		return ms.length()!=11			//TODO 现在仅验证手机号码长度，需要添加更加详细的规则。。
					? false:true; 
		
	}
}
