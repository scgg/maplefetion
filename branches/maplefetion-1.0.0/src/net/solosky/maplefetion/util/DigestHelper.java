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
 * Package  : net.solosky.maplefetion.util
 * File     : DigestHelper.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 *	
 *	加密算法工具
 *
 * @author solosky <solosky772@qq.com> 
 */
public class DigestHelper
{
	
	/**
	 * 计算MD5值
	 * @param bytes		须计算的字节数组
	 * @return			计算结果
	 */
	public static byte[] MD5(byte[] bytes)
	{
		return digest("MD5", bytes);
	}
	
	/**
	 * 计算SHA1值
	 * @param bytes		须计算的字节数组
	 * @return			计算结果
	 */
	public static byte[] SHA1(byte[] bytes)
	{
		return digest("SHA-1",bytes);
	}
	
	/**
	 * 计算HASH值
	 * @param type		Hash类型
	 * @param bytes		需计算的字节
	 * @return			HASH结果
	 */
	private static byte[] digest(String type, byte[] bytes)
	{
		 MessageDigest dist = null;
		 byte[] result  	= null; 
	        try {
		        dist = MessageDigest.getInstance(type);
		        result = dist.digest(bytes);
	        } catch (NoSuchAlgorithmException e) {
		      Logger.getLogger(DigestHelper.class).warn("Cannot find digest:"+type);
	        }
	    	  return result;
	}
}
