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
 * Project  : MapleSMS
 * Package  : net.solosky.maplesms.gateway
 * File     : FetionGateway.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-4-6
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.demo.robot;

import net.solosky.maplefetion.ClientState;
import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.FetionException;
import net.solosky.maplefetion.LoginListener;
import net.solosky.maplefetion.LoginState;
import net.solosky.maplefetion.NotifyListener;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.ChatDialogProxy;
import net.solosky.maplefetion.client.dialog.GroupDialog;

/**
 *
 *
 * @author solosky <solosky772@qq.com>
 */
public class FetionGateway implements Gateway, LoginListener,NotifyListener
{

	private FetionClient client;
	private SMSListener listener;
	
	public FetionGateway(long mobile, String pass)
	{
		this.client = new FetionClient(mobile, pass, this, this);
	}
	/* (non-Javadoc)
     * @see net.solosky.maplesms.gateway.Gateway#login()
     */
    @Override
    public void login()
    {
    	this.client.syncLogin();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplesms.gateway.Gateway#logout()
     */
    @Override
    public void logout()
    {
    	this.client.logout();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplesms.gateway.Gateway#sendSMS(java.lang.String, java.lang.String)
     */
    @Override
    public void sendSMS(String uri, final String msg)
    {
    	try{
        	Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
        	ChatDialogProxy dialog = this.client.getChatDialogProxyFactory().create(buddy);
    		dialog.sendChatMessage(new Message(msg), new DefaultActionListener("发送消息[ "+msg+" ]给"+buddy.getDisplayName()));
        } catch (FetionException e) {
        	println("建立对话框时出错"+e.getMessage());
        }
    }

	/* (non-Javadoc)
     * @see net.solosky.maplesms.gateway.Gateway#setSMSListener(net.solosky.maplesms.gateway.SMSListener)
     */
    @Override
    public void setSMSListener(SMSListener listener)
    {
    	this.listener = listener;
    }
    
    
    public void buddyApplication(Buddy buddy, String desc)
    {
    	//Cord testCord = this.client.getFetionStore().getCord(10);
    	System.out.println("请求加为好友-"+buddy.getDisplayName()+"-"+desc);
	    this.client.agreedApplication(buddy, new DefaultActionListener("同意对方添加好友请求-"+buddy.getDisplayName()));
	    //this.client.setBuddyCord(buddy, testCord, new DefaultActionListener("添加好友至测试组-"+buddy.getDisplayName()));
    }
    @Override
    public void buddyConfirmed(Buddy arg0, boolean arg1)
    {
	    
    }
    public void buddyMessageRecived(Buddy buddy, Message msg, ChatDialog dialog)
    {
	    println(buddy.getDisplayName()+" 说: "+msg.getText());
	    this.listener.smsRecived(buddy.getUri(), msg.getText(), this);
	    
    }
    public void clientStateChanged(ClientState state)
    {
	    if(state!=ClientState.ONLINE&& state!=ClientState.LOGGING&& state!=ClientState.LOGOUT) {
	    	println("客户端错误，等待10秒后重新登录 - "+state.name());
	    	try {
	            Thread.sleep(10*1000);
            } catch (InterruptedException e) {
            	println("等待登录的过程被中断。");
            }
            client.login();
	    }
    }
    public void groupMessageRecived(Group arg0, Member arg1, Message arg2,
            GroupDialog arg3)
    {
	    
    }
    public void presenceChanged(FetionBuddy b)
    {
    	if(b.getPresence().getValue()==Presence.ONLINE) {
    		println(b.getDisplayName()+" 上线了。");
    	}else if(b.getPresence().getValue()==Presence.OFFLINE){
    		println(b.getDisplayName()+" 下线了。");
    	}
    }
    public void systemMessageRecived(String arg0)
    {
    }
    

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.LoginListener#loginStateChanged(net.solosky.maplefetion.LoginState)
     */
    @Override
    public void loginStateChanged(LoginState state)
    {
    	println(state.name());
    }
    
    public void println(String msg)
    {
    	System.out.println("[FetionGateway] "+msg);
    }
    
    public class DefaultActionListener implements ActionListener{

    	private String title;
    	
    	public DefaultActionListener(String title) {
    		this.title = title;
    	}
        @Override
        public void actionFinished(int status)
        {
        	if(status>=200&&status<300) {
        		println(title+" 【成功】");
        	}else {
        		println(title+" 【失败】");
        	}
        }
    }

	/* (non-Javadoc)
     * @see net.solosky.maplesms.gateway.Gateway#getName()
     */
    @Override
    public String getName()
    {
    	return "[Fetion user="+this.client.getFetionUser().getDisplayName()+", uri="+this.client.getFetionUser().getUri()+"]";
    }

}
