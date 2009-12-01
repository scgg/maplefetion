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
 * Package  : test
 * File     : FetionClientTest.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.solosky.maplefetion.IMessageCallback;
import net.solosky.maplefetion.MapleFetionClient;
import net.solosky.maplefetion.bean.BuddyExtend;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.net.tcp.TCPTransferFactory;
import net.solosky.maplefetion.store.IFetionStore;

/**
 *
 *
 * @author solosky <solosky772@qq.com> 
 */
public class FetionClientTest implements IMessageCallback
{
	private MapleFetionClient client;
	
	public FetionClientTest()
	{
		client = new MapleFetionClient("13880918643","peter3140263", new TCPTransferFactory(),
				null, null, null);
		//client = new MapleFetionClient("15982070573","359640181");
	}
	
	public void login() throws Exception
	{
		//Logger.getRootLogger().setLevel(Level.INFO);
		if(client.login()) {
			System.out.println("用户名:"+client.getFetionUser().getNickName()+"; 心情短语:"+client.getFetionUser().getImpresa());
			System.out.println("请输入命令:");
			printBuddy();
			readLoop();
		}else {
			System.out.println("用户名或者密码错误");
		}
			
	}
	
	public void readLoop() throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line=null;
		while(true) {
			line = reader.readLine();
			if(line.length()==0)
				continue;
			if(line.equals("ls")) {
				printBuddy();
			}else if(line.startsWith("to")){
				String[] ms = line.split(" ");
				client.sendChatMessageEx(ms[1],ms[2],this);
//				if(true){
//					System.out.println("发送聊天消息成功");
//				}else {
//					System.out.println("发送聊天消息失败");
//				}
			}else if(line.startsWith("sms")){
				String[] ms = line.split(" ");
				if(client.sendSMSMessage(ms[1],ms[2])) {
					System.out.println("发送短信成功");
				}else {
					System.out.println("发送短信失败");
				}
			}else if(line.startsWith("del")) {
				String[] ms = line.split(" ");
				if(client.deleteBuddy(ms[1])) {
					System.out.println("删除好友成功");
				}else {
					System.out.println("删除好友失败");
				}
			}else if(line.startsWith("add")){
				String[] ms = line.split(" ");
				if(client.addBuddy(ms[1])) {
					System.out.println("发出添加请求好友成功");
				}else {
					System.out.println("发出添加请求好友失败");
				}
			}else if(line.startsWith("agree")) {
				String[] ms = line.split(" ");
				if(client.agreedApplication(ms[1])) {
					System.out.println("同意对方添加请求好友成功");
				}else {
					System.out.println("同意对方添加请求好友失败");
				}
			}else if(line.startsWith("decline")) {
				String[] ms = line.split(" ");
				if(client.declinedApplication(ms[1])) {
					System.out.println("拒绝对方添加请求好友成功");
				}else {
					System.out.println("拒绝对方添加请求好友失败");
				}
			}else if(line.startsWith("detail")) {
				String[] ms = line.split(" ");
				if(client.readBuddyDetail(ms[1])) {
					System.out.println("获取好友详细资料成功");
					printBuddyDetail(ms[1]);
				}else {
					System.out.println("获取好友详细资料失败");
				}
			}else if(line.startsWith("debug")) {
				String[] ms = line.split(" ");
				if(ms[1].equals("true")) {
					Logger.getRootLogger().setLevel(Level.ALL);
					System.out.println("打开调试输出");
				}else {
					Logger.getRootLogger().setLevel(Level.OFF);
					System.out.println("关闭调试输出");
				}
			}else if(line.equals("q")) {
				client.logout();
				break;
			}
				
		}
	}
	/**
     * 
     */
    private void printBuddyDetail(String uri)
    {
    	FetionBuddy buddy = client.getFetionStore().getBuddy(uri);
    	if(buddy==null)
    		return;
    	System.out.println("----------------------------------");
	    System.out.println("好友详细信息:");
	    System.out.println("----------------------------------");
	    System.out.println("URI:"+buddy.getUri());
	    System.out.println("昵称:"+buddy.getNickName());
	    System.out.println("个性签名:"+buddy.getImpresa());
	    System.out.println("状态:"+fomartPresence(buddy.getPresence()));
	    System.out.println("备注:"+buddy.getLocalName());
	    System.out.println("真实姓名:"+buddy.getTrueName());
	    System.out.println("手机号码:"+buddy.getTrueName());
	    System.out.println("头像编号:"+buddy.getPortrait());
	    BuddyExtend extend = buddy.getExtend();
	    System.out.println("国家:"+extend.getNation());
	    System.out.println("省份:"+extend.getProvince());
	    System.out.println("城市:"+extend.getCity());
	    System.out.println("EMAIL:"+extend.getEmail());
	    System.out.println("----------------------------------");
	    
    }

	public void printBuddy()
	{
		IFetionStore store = client.getFetionStore();
		Iterator<FetionBuddy> it = store.getBuddyList().iterator();
		System.out.println("ID\t姓名\t\t在线\t\t个性签名");
		while(it.hasNext()) {
			FetionBuddy b =it.next();
			//String id = b.isRegistered() ?Integer.toString(b.getSid()) : b.getMobileNo();
			String id = b.getUri();
			String online = fomartPresence(b.getPresence());
			System.out.println(id+"\t\t"+b.getDisplayName()+"\t"+online+"\t\t"+b.getImpresa());
		}
	}
	public static void main(String args[]) throws Exception
	{
		new FetionClientTest().login();
		//System.out.println("成都理工大学".getBytes().length);
	}
	
	public String fomartPresence(int pre)
	{
		switch (pre)
        {
        case FetionBuddy.PRESENCE_PC_ONLINE:return "电脑在线";
        case FetionBuddy.PRESENCE_PC_AWAY:return "电脑离开";
        case FetionBuddy.PRESENCE_PC_BUSY:return "电脑忙碌";
        default:return "短信在线";
        }
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.IMessageCallback#messageSended(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void messageSended(String uri, String content, boolean isSuccess)
    {
	    if(isSuccess)
	    	System.out.println("发送消息成功");
	    else
	    	System.out.println("发送消息失败");
	    
    }
}
