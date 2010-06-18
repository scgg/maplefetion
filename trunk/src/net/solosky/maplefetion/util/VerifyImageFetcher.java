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
 * File     : VerifyImageFetcher.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-3-9
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;

import sun.misc.BASE64Decoder;

import net.solosky.maplefetion.FetionConfig;
import net.solosky.maplefetion.bean.VerifyImage;

/**
 * 获取验证图片
 *
 * @author solosky <solosky772@qq.com>
 */
public class VerifyImageFetcher
{
	
	public static VerifyImage fetch(String picUrl)
	{
		try {
	        URL url = new URL(picUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.addRequestProperty("User-Agent", "IIC2.0/PC 3.5.2540");		//必须要加这个，否则失败  很奇怪
	        if(conn.getResponseCode()==HttpURLConnection.HTTP_OK) {
	        	Element e = XMLHelper.build(conn.getInputStream());
	        	Element pic = XMLHelper.find(e, "/results/pic-certificate");
	        	String code = pic.getAttributeValue("pic");
	        	BASE64Decoder decoder = new BASE64Decoder();
	        	
	        	return new VerifyImage(pic.getAttributeValue("id"),decoder.decodeBuffer(code));
	        }
        } catch (Exception e) {
        	Logger.getLogger(VerifyImageFetcher.class).warn("fetch verify image failed.", e);
        	return null;
        }
		return null;
	}
}
