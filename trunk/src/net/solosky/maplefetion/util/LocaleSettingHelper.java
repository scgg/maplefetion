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
 * File     : LocaleSettingHelper.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-27
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.bean.User;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * 自适应系统配置工具类
 * 因为不同的地方登陆飞信是登陆到不同的服务器上的，所以服务器的IP需要根据用户登录的地址去获取
 * 我称之为自适应的系统配置
 *
 * @author solosky <solosky772@qq.com>
 */
public class LocaleSettingHelper
{
	/**
	 * 获取自适应系统配置
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static Document load(User user) throws IOException, JDOMException
	{
        URL url = new URL(FetionConfig.getString("server.nav-system-uri"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String content = "<config><user mobile-no=\""+user.getMobile()+"\" /><client type=\"PC\" version=\"3.5.1170\" platform=\"W5.1\" /><servers version=\"0\" /><service-no version=\"0\" /><parameters version=\"0\" /><hints version=\"0\" /><http-applications version=\"0\" /><client-config version=\"0\" /><services version=\"0\" /></config>";
        OutputStream out = conn.getOutputStream();
        out.write(content.getBytes());
        out.flush();
        
        SAXBuilder builder = new SAXBuilder();
        return builder.build(conn.getInputStream());
        
	}
	
	/**
	 * 激活自适应配置
	 * @param doc
	 */
	public static void active(Document doc)
	{
		Element root = doc.getRootElement();
        
        Element servers = root.getChild("servers");
        FetionConfig.setString("server.ssi-sign-in",  servers.getChildText("ssi-app-sign-in"));
        FetionConfig.setString("server.sipc-proxy",   servers.getChildText("sipc-proxy"));
        FetionConfig.setString("server.http-tunnel",  servers.getChildText("http-tunnel"));
	}
	
}
