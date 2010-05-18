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
 * Package  : net.solosky.maplefetion.demo
 * File     : FetionPool.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-5-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import net.solosky.maplefetion.ClientState;
import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.LoginListener;
import net.solosky.maplefetion.LoginState;
import net.solosky.maplefetion.NotifyListener;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.util.SharedExecutor;
import net.solosky.maplefetion.util.SharedTimer;

/**
 *
 *  飞信实例池，可以在这个实例池上运行多个飞信客户端，各个客户端运行互不干扰
 *
 * @author solosky <solosky772@qq.com>
 */
public class FetionPool
{
	/**
	 * 飞信实例列表
	 */
	private ArrayList<FetionClient> clientList;
	
	/**
	 * 共享的timer
	 */
	private SharedTimer sharedTimer;
	
	/**
	 * 共享的线程池
	 */
	private SharedExecutor shareExecutor;
	
	/**
	 * 读取控制台输入字符
	 */
	private BufferedReader reader;
	
	/**
	 * 写入控制台字符
	 */
	private BufferedWriter writer;
	
	public FetionPool()
	{
		this.reader = new BufferedReader(new InputStreamReader(System.in));
		this.writer = new BufferedWriter(new OutputStreamWriter(System.out));
		this.sharedTimer = new SharedTimer();
		this.shareExecutor = new SharedExecutor();
		this.clientList = new ArrayList<FetionClient>();
	}
	
	
	public void mainloop() throws Exception
	{
		this.help();
		while(true) {
    		String line = reader.readLine();
    		if(!this.dispatch(line)) {
    			Iterator<FetionClient> it = this.clientList.iterator();
    			while(it.hasNext()) {
    				FetionClient client = it.next();
    				if(client.getState()==ClientState.ONLINE) {
    					client.logout();
    				}
    			}
    			//等待10秒，因为退出是异步的。。
    			Thread.sleep(10000);
    			this.sharedTimer.reallyStopTimer();
    			this.shareExecutor.reallyStopExecutor();
    			break;
    		}
    	}
	}
	
	public void ls()
	{
		println("当前飞信池的客户端：");
		Iterator<FetionClient> it = this.clientList.iterator();
		int i = 0;
		while(it.hasNext()) {
			FetionClient client = it.next();
			println("["+(i++)+"]	"+client.getFetionUser().getDisplayName()+"("+client.getFetionUser().getMobile()+")		" + client.getState().name());
		}
	}
	
	 /**
     * 解析用户输入的命令，并调用不同的程序
     * @throws Exception 
     */
    public boolean dispatch(final String line) throws Exception
    {
    	String[] cmd = line.split(" ");
    	if(cmd[0].equals("exit")) {
			return false;
    	}else if(cmd[0].equals("ls")) {
    		this.ls();
    	}else if(cmd[0].equals("login")) {
    		this.login(cmd[1]);
    	}else if(cmd[0].equals("logout")) {
    		this.logout(cmd[1]);
    	}else if(cmd[0].equals("add")) {
    		if(cmd.length>2)
    		this.add(cmd[1], cmd[2]);
    	}else if(cmd[0].equals("del")) {
    		this.del(cmd[1]);
    	}else if(cmd[0].equals("help")) {
    		this.help();
    	}else {
    		println("未知命令");
    	}
    	
    	return true;
    	
    }
	/**
     * 
     */
    private void help()
    {
    	println("这是一个飞信实例池，里面可以运行多个飞信实例而不互相干扰。");
    	println("并且使用共享的Timer和Executor，可以减少资源占用，提高效率。");
    	println("可用命令如下：");
    	println("ls					查看当前已经添加的客户端");
    	println("add 手机号 密码 	添加客户端到飞信池");
    	println("del 客户端编号 		从飞信池删除客户端");
    	println("login 客户端编号	开始登陆指定的客户端");
    	println("logout 客户端编号	退出指定的客户端");
    	println("exit 				退出飞信池");
    	println("help				帮助信息");
    }


	/**
     * @param string
     */
    private void del(String s)
    {
    	Integer i = Integer.parseInt(s);
	    FetionClient client = this.clientList.get(i);
	    if(client!=null) {
	    	if(client.getState()==ClientState.ONLINE) {
	    		println("正在退出 "+client.getFetionUser().getDisplayName()+"..");
	    		client.logout();
	    	}
	    	this.clientList.remove(i);
	    }
	    
    }


	/**
     * @param string
     * @param string2
     */
    private void add(String mobile, String password)
    {
    	FetionClient client = new FetionClient(Long.parseLong(mobile), password);
    	FetionPoolListener listener = new FetionPoolListener(client);
    	client.setLoginListener(listener);
    	client.setNotifyListener(listener);
    	this.clientList.add(client);
    	println("已经添加 "+mobile +"..");
    }


	/**
     * @param string
     */
    private void logout(String s)
    {
    	Integer i = Integer.parseInt(s);
	    FetionClient client = this.clientList.get(i);
	    if(client!=null && client.getState()==ClientState.ONLINE) {
	    	println("正在退出 "+client.getFetionUser().getDisplayName()+"..");
	    	client.logout();
	    }
    }


	/**
     * @param string
     */
    private void login(String s)
    {
	    Integer i = Integer.parseInt(s);
	    FetionClient client = this.clientList.get(i);
	    if(client!=null && client.getState()!=ClientState.ONLINE) {
	    	println("正在登陆 "+client.getFetionUser().getDisplayName()+"..");
	    	client.login();
	    }
    }


	/**
	 * @param args
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws Exception
	{
		new FetionPool().mainloop();
	}
	
	
	/**
     * 打印一行字符
     */
    public void println(String s)
    {
    	
    	try {
    		this.writer.append(s);
    		this.writer.append('\n');
	        this.writer.flush();
        } catch (IOException e) {
	        e.printStackTrace();
        }
    }
	
	
	private class FetionPoolListener implements NotifyListener, LoginListener
	{
		private FetionClient client;
		public FetionPoolListener(FetionClient client)
		{
			this.client = client;
		}
        public void buddyApplication(Buddy buddy, String desc)
        {
        	printlnx("[好友请求]:"+desc+" 想加你为好友。请输入 【agree/decline 好友编号】 同意/拒绝添加请求。");
        }
        public void buddyConfirmed(Buddy buddy, boolean isAgreed)
        {
        	if(isAgreed)
        		printlnx("[系统通知]:"+buddy.getDisplayName()+" 同意了你的好友请求。");
        	else 
        		printlnx("[系统通知]:"+buddy.getDisplayName()+" 拒绝了你的好友请求。");
        }
        public void buddyMessageRecived(Buddy from, Message message,
                ChatDialog dialog)
        {
        	if(from.getRelation()==Relation.BUDDY)
        		printlnx("[好友消息]"+from.getDisplayName()+" 说:"+message.getText());
        	else 
        		printlnx("[陌生人消息]"+from.getDisplayName()+" 说:"+message.getText());
        }
        public void clientStateChanged(ClientState state)
        {
        	switch (state)
            {
                case OTHER_LOGIN:
                	printlnx("你已经从其他客户端登录。");
                	printlnx("30秒之后重新登录..");
                	//新建一个线程等待登录，不能在这个回调函数里做同步操作
                	new Thread(new Runnable() {
                		public void run() {
                			try {
        	                    Thread.sleep(30000);
                            } catch (InterruptedException e) {
                            	System.out.println("重新登录等待过程被中断");
                            }
                            client.login();
                		}
                	}).start();
        	        break;
                case CONNECTION_ERROR:
                	printlnx("客户端连接异常");
        	        break;
                case DISCONNECTED:
                	printlnx("服务器关闭了连接");
                	break;
                case LOGOUT:
                	printlnx("已经退出。。");
                	break;
                case ONLINE:
                	printlnx("当前是在线状态。");
                	break;
                default:
        	        break;
            }
        }
        public void groupMessageRecived(Group group, Member from,
                Message message, GroupDialog dialog)
        {
        	 printlnx("[群消息] 群 "+group.getName()+" 里的 "+from.getDisplayName()+" 说："+message.getText());   
        }
        public void presenceChanged(FetionBuddy b)
        {
        	if(b.getPresence().getValue()==Presence.ONLINE) {
        		printlnx("[系统通知]:"+b.getDisplayName()+" 上线了。");
        	}else if(b.getPresence().getValue()==Presence.OFFLINE){
        		printlnx("[系统通知]:"+b.getDisplayName()+" 下线了。");
        	}
        }
        public void systemMessageRecived(String m)
        {
        	printlnx("[系统消息]:"+m);
        }
        public void loginStateChanged(LoginState state)
        {
        }
        public void printlnx(String m)
        {
        	println("["+client.getFetionUser().getDisplayName()+"]"+m);
        }
	}
}
