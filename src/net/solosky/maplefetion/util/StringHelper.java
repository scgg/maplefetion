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
 * File     : StringHelper.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : StringHelper.java
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

/**
 * 字符串工具类
 * 
 * @author solosky <solosky772@qq.com>
 *
 */
public class StringHelper {

	/**
	 * 转义HTML中特殊的字符
	 * @param html
	 * @return
	 */
	public static String qouteHtmlSpecialChars(String html)
	{
		if(html==null)	return null;
		String[] specialChars = { "&", "\"", "'", "<", ">"};
		String[] qouteChars = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"};
		for(int i=0; i<specialChars.length; i++){
			html = html.replace(specialChars[i], qouteChars[i]);
		}
		return html;
	}
	
	/**
	 * 反转义HTML中特殊的字符
	 * @param html
	 * @return
	 */
	public static String unqouteHtmlSpecialChars(String html)
	{
		if(html==null)	return null;
		String[] specialChars = { "&", "\"", "'", "<", ">", " "};
		String[] qouteChars = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;", "&nbsp;"};
		for(int i=0; i<specialChars.length; i++){
			html = html.replace(qouteChars[i], specialChars[i]);
		}
		return html;
	}
	
	
	/**
	 * 去掉HTML标签
	 * @param html
	 * @return
	 */
	public static String stripHtmlSpecialChars(String html)
	{
		if(html==null)	return null;
		 html=html.replaceAll("</?[^>]+>",""); 
		 html=html.replace("&nbsp;"," "); 
		 
		 return html;
	}
}
