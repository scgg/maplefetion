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
 * File     : LocaleSetting.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-6-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.bean.User;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * 区域配置，部分配置和区域有关
 *
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public class LocaleSetting
{
	/**
	 * 配置的Dom文档
	 */
	private Document document;
	
	/**
	 * 配置是否被加载
	 */
	private boolean isLoaded;
	/**
	 * 默认的构造函数
	 */
	public LocaleSetting()
	{
		this.isLoaded = false;
		this.document = null;
	}
	
	
	/**
	 * 返回一个节点的文本
	 * @param path		路径
	 * @return
	 */
	public String getNodeText(String path)
	{
		Element el = XMLHelper.find(this.document.getRootElement(), path);
		return el!=null?el.getText():null;
	}
	
	/**
	 * 获取自适应系统配置
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public void load(User user) throws IOException, JDOMException
	{
        URL url = new URL(FetionConfig.getString("server.nav-system-uri"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.addRequestProperty("User-Agent", "IIC2.0/PC 3.5.2540");
        String content = "<config><user mobile-no=\""+user.getMobile()+"\" /><client type=\"PC\" version=\""+FetionClient.PROTOCOL_VERSION+"\" platform=\"W5.1\" /><servers version=\"0\" /><service-no version=\"0\" /><parameters version=\"0\" /><hints version=\"0\" /><http-applications version=\"0\" /><client-config version=\"0\" /><services version=\"0\" /></config>";
        OutputStream out = conn.getOutputStream();
        out.write(content.getBytes());
        out.flush();
        
        SAXBuilder builder = new SAXBuilder();
        this.document = builder.build(conn.getInputStream());
        this.isLoaded = true;
	}


	/**
	 * 是否被加载
	 * @return the isLoaded
	 */
	public boolean isLoaded()
	{
		return isLoaded;
	}
}
