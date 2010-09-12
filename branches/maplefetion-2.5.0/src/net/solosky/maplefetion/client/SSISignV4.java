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
 * Project  : MapleFetion2.5
 * Package  : net.solosky.maplefetion.client
 * File     : SSISignV4.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-9-12
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.LoginState;
import net.solosky.maplefetion.NotifyEventListener;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.bean.VerifyImage;
import net.solosky.maplefetion.event.notify.ImageVerifyEvent;
import net.solosky.maplefetion.util.LocaleSetting;
import net.solosky.maplefetion.util.ParseException;
import net.solosky.maplefetion.util.PasswordEncrypterV4;
import net.solosky.maplefetion.util.XMLHelper;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 *  SSI V4登录
 *
 * @author solosky <solosky772@qq.com>
 */
public class SSISignV4 implements SSISign
{
	private LocaleSetting localeSetting;
	private FetionContext fetionContext;
	private static Logger logger = Logger.getLogger(SSISignV2.class);

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.SSISign#setLocaleSetting(net.solosky.maplefetion.util.LocaleSetting)
     */
    @Override
    public void setLocaleSetting(LocaleSetting localeSetting)
    {
    	this.localeSetting = localeSetting;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.SSISign#setFetionContext(net.solosky.maplefetion.FetionContext)
     */
    @Override
    public void setFetionContext(FetionContext context)
    {
    	this.fetionContext = context;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.SSISign#signIn(net.solosky.maplefetion.bean.User)
     */
    @Override
    public LoginState signIn(User user)
    {
	   return this.signIn(user, null);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.SSISign#signIn(net.solosky.maplefetion.bean.User, net.solosky.maplefetion.bean.VerifyImage)
     */
    @Override
    public LoginState signIn(User user, VerifyImage img)
    {
    	LoginState state = null;
		String url = this.buildUrl(user, img);
		try {
	        URL doUrl = new URL(url);
	        HttpsURLConnection conn = (HttpsURLConnection) doUrl.openConnection();
	        int status = conn.getResponseCode();
	        logger.info("SSISignV4:status="+status);
	        
	        switch(status){
	        
	        	case 401:
	            	logger.debug("Invalid password...");
	            	state = LoginState.SSI_AUTH_FAIL;
	            	break;
	            	
	            case 421:
	            	Element e = XMLHelper.build(conn.getErrorStream());
	        		Element vn = XMLHelper.find(e, "/results/verification");
	        		this.processVerifyAction(vn.getAttributeValue("algorithm"), vn.getAttributeValue("type"),
	        				vn.getAttributeValue("text"), vn.getAttributeValue("tips"));
	        		return LoginState.SSI_NEED_VERIFY;
	        		
	            
	            case 420:
	            	logger.debug("SSISignV4: invalid verify code.");
	            	Element e1 = XMLHelper.build(conn.getErrorStream());
	        		Element vn1 = XMLHelper.find(e1, "/results/verification");
	        		this.processVerifyAction(vn1.getAttributeValue("algorithm"), vn1.getAttributeValue("type"),
	        				vn1.getAttributeValue("text"), vn1.getAttributeValue("tips"));
	            	state = LoginState.SSI_VERIFY_FAIL;
	            	break;
	            	
	            case 433:
	            	logger.debug("SSISignV4: User account suspend.");
	            	state = LoginState.SSI_ACCOUNT_SUSPEND;
	            	break;
	            	
	            case 404:
	            	logger.debug("SSISignV4: User not found..");
	            	state = LoginState.SSI_ACCOUNT_NOT_FOUND;
	            	break;
	            	
	            case 503:
	            	logger.debug("SSISignV4: SSIServer overload...");
	            	state = LoginState.SSI_CONNECT_FAIL;
	            	break;
	            	
	            case 200:
	            	logger.debug("SSISignIn: sign in success.");
	            	state = LoginState.SSI_SIGN_IN_SUCCESS;
	            	
	            	String h = conn.getHeaderField("Set-Cookie");
	        		int s = h.indexOf("ssic=");
	        		int m = h.indexOf(';');
	        		user.setSsic( h.substring(s+5,m) );
	        		
	        		Element root = XMLHelper.build(conn.getInputStream());
	        		Element userEl = XMLHelper.find(root, "/results/user");
	        		user.setUri(userEl.getAttributeValue("uri"));
	        		user.setUserId(Integer.parseInt(userEl.getAttributeValue("user-id")));
	        		String mobileStr = userEl.getAttributeValue("mobile-no");
	        		if(mobileStr!=null && mobileStr.length()>0)
	        			user.setMobile(Long.parseLong(mobileStr));
	        		
	        		logger.info("SSISignV4:ssic="+user.getSsic());
	        		
	        		break;
	        		
	        	default:
	        		logger.debug("SSISignV4: Unhandled ssi status="+status);
	        		state = LoginState.OTHER_ERROR;
	        }
        } catch (NumberFormatException e) {
        	state = LoginState.OTHER_ERROR;
        	logger.warn("SSI SignV4 failed.", e);
        } catch (MalformedURLException e) {
        	state = LoginState.OTHER_ERROR;
        	logger.warn("SSI SignV4 failed.", e);
        } catch (ParseException e) {
        	state = LoginState.OTHER_ERROR;
        	logger.warn("SSI SignV4 failed.", e);
        } catch (IOException e) {
        	state = LoginState.SSI_CONNECT_FAIL;
        	logger.warn("SSI SignV4 failed.", e);
        }catch(Throwable e) {
        	state = LoginState.OTHER_ERROR;
        	logger.warn("SSI SignV4 failed.", e);
        }
        
        return state;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.SSISign#signOut(net.solosky.maplefetion.bean.User)
     */
    @Override
    public LoginState signOut(User user)
    {
	    return null;
    }
    
    
    /**
     * 获取验证图片并回调用户设置的通知方法处理验证码事件
     * @param alg
     */
    private void processVerifyAction(String alg, String type, String text, String tips)
    {
    	try {
	        VerifyImage verifyImage = HttpApplication.fetchVerifyImage(
	        		this.fetionContext.getFetionUser(), this.fetionContext.getLocaleSetting(), alg, type);
	        
	        NotifyEventListener listener = this.fetionContext.getNotifyEventListener();
	        
	        if(listener!=null) {
	        	listener.fireEvent(new ImageVerifyEvent(ImageVerifyEvent.SSI_VERIFY, verifyImage, text, tips,null, null));
	        }else {
	        	throw new IllegalArgumentException("SSI need verify, but found no notifyEventListener" +
	        			" to handle verify action, please set NotifyEventListener frist.");
	        }
        } catch (ParseException e) {
        	logger.warn("fetch verify image failed.",e );
        } catch (IOException e) {
        	logger.warn("fetch verify image failed.",e );
        }
    }
    
    
    private String buildUrl(User user, VerifyImage img)
	{
		StringBuffer b = new StringBuffer();
		b.append("https://uid.fetion.com.cn/ssiportal/SSIAppSignInV4.aspx?");
		if(user.getMobile()>0){
			b.append("mobileno="+Long.toString(user.getMobile()));
		}else if(user.getFetionId()>0){
			b.append("sid="+Integer.toString(user.getFetionId()));
		}else{
			throw new IllegalStateException("couldn't find valid mobile or fetionId to sign in..");
		}
		b.append("&domains=fetion.com.cn%3bm161.com.cn%3bwww.ikuwa.cn");
		b.append("&v4digest-type=1");
		b.append("&v4digest="+(PasswordEncrypterV4.encryptV4(user.getPassword())));		//就是用以前写的密码加密工具，我在这里很郁闷。。
		if(img!=null) {		//这两个字段是用于验证码验证的
			b.append("&pid="+img.getImageId());
			b.append("&pic="+img.getVerifyCode());
			b.append("&algorithm="+img.getAlgorithm());
		}
		return b.toString();
	}
}
