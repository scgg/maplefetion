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
 * Package  : net.solosky.maplefetion.client
 * File     : HttpApplication.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-15
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.util.HttpHelper;
import net.solosky.maplefetion.util.LocaleSetting;

/**
 * 
 * 飞信的HTTP应用
 * 
 * 飞信的很多地方都是使用了HTTP这样的模式
 * 比如设置头像，设置自定义头像等
 * 
 * @author solosky <solosky772@qq.com>
 */
public class HttpApplication
{
	/**
	 *  
	 *  设置自定义头像
	 * 
	 * @param user			用户编号
	 * @param setting		自适应配置
	 * @param imgStream		图片流
	 * @return 	返回自定义头像编号
	 * @throws IOException 
	 */
	public static long setPortrait(User user, LocaleSetting setting, InputStream imgStream) throws IOException
	{
		String setPortraitUrl = setting.getNodeText("/config/http-applications/set-portrait");
		byte [] bytes = HttpHelper.doFetchData(setPortraitUrl, "POST", "image/jpeg", user, imgStream);
		return Long.parseLong(new String(bytes));
	}
	
	/**
	 * 替换SSIC
	 * @param user
	 * @param setting
	 * @throws IOException
	 */
	public static void replaceSsic(User user, LocaleSetting setting) throws IOException
	{
		String v2Url = setting.getNodeText("/config/servers/ssi-app-sign-in-v2");
		v2Url += "?domains=fetion.com.cn%3bm161.com.cn%3bwww.ikuwa.cn";
		HttpURLConnection conn = HttpHelper.openConnection(v2Url, "GET", "application/x-www-form-urlencoded", user.getSsic());
		if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
			String header = conn.getHeaderField("Set-Cookie");
        	int s = header.indexOf("ssic=");
        	int e = header.indexOf(';');
        	String ssic = header.substring(s+5,e);
        	user.setSsic(ssic);
		}else{
			throw new IOException("Http response is not OK. code="+conn.getResponseCode());
		}
	}
}
