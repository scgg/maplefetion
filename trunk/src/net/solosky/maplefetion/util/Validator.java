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
	 * 目前仅判断了是否是以下号码段1340-1348，135-139，150-152,187-188
	 * @param mobile	手机号码
	 * @return	是否合法
	 */
	public static boolean validateMobile(long mobile)
	{
		if(mobile>=13400000000L && mobile<=13489999999L){	//1340-1348
			return true;
		}else if(mobile>=13500000000L && mobile<=13999999999L){	//135-139
			return true;
		}else if(mobile>=15000000000L && mobile<=15299999999L){	//150-152
			return true;
		}else if(mobile>=15700000000L && mobile<=15999999999L){	//157-159
			return true;
		}else if(mobile>=18700000000L && mobile<=18899999999L){	//187-188
			return true;
		}else {
			return false;
		}
	}
}
