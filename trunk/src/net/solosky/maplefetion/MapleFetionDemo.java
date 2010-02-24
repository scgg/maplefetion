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
 * Package  : net.solosky.maplefetion
 * File     : MapleFetion.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-12-1
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import net.solosky.maplefetion.bean.BuddyExtend;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.FetionCord;
import net.solosky.maplefetion.net.mina.MinaTransferFactory;
import net.solosky.maplefetion.net.tcp.TCPTransferFactory;
import net.solosky.maplefetion.protocol.ChatDialog;
import net.solosky.maplefetion.store.IFetionStore;
import net.solosky.maplefetion.store.SimpleFetionStore;

/**
 *
 *	这个是MapleFetion的演示程序，也提供了一个完整的命令行下的飞信
 *	
 * @author solosky <solosky772@qq.com> 
 */
public class MapleFetionDemo implements INotifyListener,ILoginListener,IMessageCallback
{
	
	/**
	 * 飞信客户端
	 */
	private MapleFetionClient client;
	
	/**
	 * 读取控制台输入字符
	 */
	private BufferedReader reader;
	
	/**
	 * 写入控制台字符
	 */
	private BufferedWriter writer;
	
	/**
	 * 当前聊天好友
	 */
	private FetionBuddy activeBuddy;
	
	/**
	 * 好友序号到好友飞信地址的映射
	 */
	private Hashtable<String, String> map;

	/**
	 * 主线程
	 */
	 private Thread mainThread;
	 
	 /**
	  * 是否有错误
	  */
	 private boolean errorFlag;
	
	/**
	 * 构造函数
	 */
	public MapleFetionDemo(long mobileNo, String pass)
	{
		this.client = new MapleFetionClient(mobileNo,
											pass,
											new TCPTransferFactory(),
											new SimpleFetionStore(),
											this,
											this);
		this.reader = new BufferedReader(new InputStreamReader(System.in));
		this.writer = new BufferedWriter(new OutputStreamWriter(System.out));
		this.map = new Hashtable<String, String>();
		this.mainThread = Thread.currentThread();
		this.errorFlag = false;
	}

	/**
	 * 接收到陌生人加用户为好友的请求
	 */
    @Override
    public void buddyApplication(FetionBuddy buddy, String desc)
    {
    	println("[好友请求]:"+desc+" 想加你为好友。请输入 【agree/decline 好友编号】 同意/拒绝添加请求。");
    	prompt();
    }

	/**
	 * 接受到好友或者陌生人的消息
	 */
    @Override
    public void buddyMessageRecived(FetionBuddy from, String message)
    {
    	if(from.getRelationStatus()==FetionBuddy.RELATION_STATUS_AGREED)
    		println("[好友消息]"+from.getDisplayName()+" 说:"+message);
    	else 
    		println("[陌生人消息]"+from.getDisplayName()+" 说:"+message);
    	prompt();
    }

	/**
	 * 好友在线状态改变
	 */
    @Override
    public void presenceChanged(FetionBuddy b)
    {
    	if(b.getPresence()==FetionBuddy.PRESENCE_PC_ONLINE) {
    		println("[系统通知]:"+b.getDisplayName()+" 上线了。");
        	prompt();
    	}else if(b.getPresence()==FetionBuddy.PRESENCE_PC_OFFLINE){
    		println("[系统通知]:"+b.getDisplayName()+" 下线了。");
        	prompt();
    	}
    }

	/**
	 * 好友回复了你的加对方好友请求
	 */
    @Override
    public void buddyConfirmed(FetionBuddy b, boolean isAgreed)
    {
    	if(isAgreed)
    		println("[系统通知]:"+b.getDisplayName()+" 同意了你的好友请求。");
    	else 
    		println("[系统通知]:"+b.getDisplayName()+" 拒绝了你的好友请求。");
    	
    	prompt();
    }

	/**
	 * 欢迎信息
	 * @throws Exception 
	 */
	 public void welcome() throws Exception
	{
		println("================================================");
		println("|              MapleFetion 1.0 beta3           |");
		println("|----------------------------------------------|");
		println("| Author:solosky <solosky772@qq.com>           |");
		println("| Home:http://maplefetion.googlecode.com       |");
		println("-----------------------------------------------|");
		println("|这是一个命令行下的飞信，实现了飞信的基本功能。|");
		println("|如果需要帮助，请输入help。欢迎提出BUG和建议。 |");
		println("================================================");
	
	}
	 
	 /**
	  * 输出用户自己信息
	  */
	 public void my()
	 {
		println("--------------------------------------------------"); 
		println("你好，"+client.getFetionUser().getDisplayName()+"! - ["+client.getFetionUser().getImpresa()+"]");
		println("--------------------------------------------------");
	 }
	 
    
    /**
     * 开始登录
     * @throws Exception 
     */
    public boolean login() throws Exception
    {
    	this.welcome();
    	if(client.login()) {
    		this.my();
    		this.list();
    		return true;
    	}else {
    		return false;
    	}
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
    /**
     * 显示帮助信息
     */
    public void help()
    {
    	println("=========================================");
    	println("帮助：");
    	println("=========================================");
    	println("welcome                    显示欢迎信息");
		println("ls                         显示所有好友列表");
		println("my                         显示我的信息");
    	println("detail 好友编号            显示好友详细信息");
    	println("add 手机号码               添加好友（必须是手机号码）");
    	println("del 好友编号               删除好友");
    	println("agree 好友编号             同意陌生人添加好友请求");
    	println("decline 好友编号           拒绝陌生人添加好友请求");
    	println("to 好友编号 消息内容       给好友发送消息");
    	println("sms 好友编号 消息内容      给好友发送短信");
    	println("enter 好友编号             和好友对话");
    	println("leave                      离开当前对话");
    	println("dialog                     显示当前所有会话");
		println("update                     更新好友信息");
    	println("nickname 新昵称            修改自己昵称");
    	println("impresa 个性签名           修改个性签名");
		println("localname 好友编号 新名字  修改好友的显示名字");
		println("self 消息内容              给自己发送短信");
		println("presence away/online/busy/hiden   改变自己在线状态");
    	println("exit                       退出登录");
    	println("help                       帮助信息");
    	println("=========================================");
    }
    
    /**
     * 主循环
     * @throws Exception 
     */
    public void mainloop() throws Exception
    {
    	String line = null;
    	this.prompt();
    	while(!this.errorFlag) {
    		line = reader.readLine();
    		if(!this.dispatch(line)) {
    			break;
    		}
    		this.prompt();
    	}
    }
    
    /**
     * 解析用户输入的命令，并调用不同的程序
     * @throws Exception 
     */
    public boolean dispatch(String line) throws Exception
    {
    	String[] cmd = line.split(" ");
    	if(cmd[0].equals("welcome")) {
			this.welcome();
		}else if(cmd[0].equals("ls")) {
			this.list();
		}else if(cmd[0].equals("my")) {
			this.my();
		}else if(cmd[0].equals("exit")) {
			this.exit();
			return false;
		}else if(cmd[0].equals("detail")) {
			if(cmd.length>=2)
				this.detail(this.map.get(cmd[1]));
		}else if(cmd[0].equals("enter")) {
			if(cmd.length>=2)
				this.enter(this.map.get(cmd[1]));
		}else if(cmd[0].equals("leave")) {
			this.leave();
		}else if(cmd[0].equals("dialog")) {
			this.dialog();
		}else if(cmd[0].equals("update")) {
			this.update();
		}else if(cmd[0].equals("to")) {
			if(cmd.length>=3)
				this.to(this.map.get(cmd[1]),line.substring(line.indexOf(cmd[2])));
		}else if(cmd[0].equals("sms")) {
			if(cmd.length>=3)
				this.sms(this.map.get(cmd[1]),line.substring(line.indexOf(cmd[2])));
		}else if(cmd[0].equals("nickname")) {
			if(cmd.length>=2)
				this.nickname(cmd[1]);
		}else if(cmd[0].equals("impresa")) {
			if(cmd.length>=2)
				this.impresa(cmd[1]);
		}else if(cmd[0].equals("del")) {
			if(cmd.length>=2)
				this.del(this.map.get(cmd[1]));
		}else if(cmd[0].equals("add")) {
			if(cmd.length>=2)
				this.add(cmd[1]);
		}else if(cmd[0].equals("agree")) {
			if(cmd.length>=2)
				this.agree(this.map.get(cmd[1]));
		}else if(cmd[0].equals("decline")) {
			if(cmd.length>=2)
				this.decline(this.map.get(cmd[1]));
		}else if(cmd[0].equals("self")) {
			if(cmd.length>=2)
				this.sms(client.getFetionUser().getMobileNo(), cmd[1]);
		}else if(cmd[0].equals("localname")) {
			if(cmd.length>=3)
				this.localname(this.map.get(cmd[1]), cmd[2]);
		}else if(cmd[0].equals("presence")) {
			if(cmd.length>=2)
				this.presence(cmd[1]);
		}else if(cmd[0].equals("help")) {
			this.help();
		}
		else {
			if( line!=null && line.length()>0 ){
				if(activeBuddy!=null) {
					this.to(activeBuddy.getUri(), line);
				}else{
					println("未知命令："+cmd[0]+"，请检查后再输入。如需帮助请输入help。");
				}

			}
		}
		return true;
    }
    
    /**
     * 改变自己昵称
     * @throws Exception 
     */
    public void nickname(String nickName) throws Exception
    {
    	if(this.client.setPersonalInfo(nickName, null)) {
    		println("更改昵称成功！");
    	}else {
    		println("更改昵称失败！");
    	}
    }
    
    /**
     * 改变自己的个性签名
     * @throws Exception 
     */
    public void impresa(String impresa) throws Exception
    {
    	if(this.client.setPersonalInfo(null, impresa)) {
    		println("更改个性签名成功！");
    	}else {
    		println("更改个性签名失败！");
    	}
    }
    
    
    /**
     * 设置登录用户的状态
     * @param presence
     * @throws Exception 
     */
    public void presence(String presence) throws Exception
    {
    	int to = -1; 
    	if(presence.equals("away")) {
    		to = FetionBuddy.PRESENCE_PC_AWAY;
    	}else if(presence.endsWith("online")) {
    		to = FetionBuddy.PRESENCE_PC_ONLINE;
    	}else if(presence.equals("busy")) {
    		to = FetionBuddy.PRESENCE_PC_BUSY;
    	}else if(presence.equals("hiden")) {
    		to = FetionBuddy.PRESENCE_PC_HIDEN;
    	}else {
    		println("未知状态:"+presence);
    	}
    	if(to!=-1) {
    		if(this.client.setPresence(to)) {
    			println("改变状态成功！");
    		}else {
    			println("改变状态失败！");
    		}
    	}
    }
    
    /**
     * 改变好友的显示姓名
     * @param uri
     * @param localName
     * @throws Exception 
     */
    public void localname(String uri, String localName) throws Exception
    {
    	if(this.client.setBuddyLocalName(uri, localName)) {
    		println("更改好友显示姓名成功！");
    	}else {
    		println("更改好友显示姓名失败！");
    	}
    }
    /**
     * 显示所有用户列表
     */
    public void list()
    {
    	println("\n=================================");
    	println("所有好友列表");
    	println("-------------------------------");
    	println("#ID\t好友昵称\t在线状态\t个性签名");
    	IFetionStore store = this.client.getFetionStore();
    	Iterator<FetionCord> it = store.getCordList().iterator();
    	int id=0;
    	this.map.clear();
    	//分组显示好友
    	while(it.hasNext()) {
    		FetionCord cord = it.next();
    		id = cord(cord.getTitle(),id, store.getBuddyList(cord.getId()));
    	}
    	id = cord("默认分组", id, store.getBuddyList(-1));
    	id = cord("陌生人", id, store.getStrangerList());
    	id = cord("请求加对方加为好友", id, store.getUnconfirmedList());
    	id = cord("对方拒绝加为好友", id, store.getDeclinedList());
    }
    
    /**
     * 显示一个组的用户
     */
    public int cord(String name,int startId,Collection<FetionBuddy> buddyList)
    {
    	Iterator<FetionBuddy> it = buddyList.iterator();
    	FetionBuddy buddy = null;
    	println("\n-------------------------------");
    	println("【"+name+"】");
    	println("-------------------------------");
    	if(buddyList.size()==0) {
    		println("暂无好友。。");
    	}
		while(it.hasNext()) {
			buddy = it.next();
			this.map.put(Integer.toString(startId), buddy.getUri());
			println(Integer.toString(startId)+"\t"+fomartString(buddy.getDisplayName(),10)+"\t"
					+fomartPresence(buddy.getPresence())
					+"\t"+fomartString(buddy.getImpresa(), 25));
			startId++;
		}
		return startId;
    }
    
    /**
     * 显示当前活动的对话
     */
    public void dialog()
    {
    	Iterator<ChatDialog> it =this.client.getChatDialogFactory().getChatDialogList().iterator();
    	println("\n===============================");
    	println("当前会话窗口");
    	println("-------------------------------");
    	println("#ID\t好友名字\t空闲时间");
    	println("-------------------------------");
    	int id=0;
    	if(this.client.getChatDialogFactory().getChatDialogList().size()>0) {
    	while(it.hasNext()) {
    		ChatDialog cd = it.next();
        	println(Integer.toString(id++)+"\t"
        			+this.fomartString(cd.getBuddy().getDisplayName(), 10)+"\t"
        			+Integer.toString((int)(System.currentTimeMillis()-cd.getLastActiveTime())/1000));
    		}
    	}else {
    		println("暂无会话。。");
    	}
    	println("-------------------------------");
    }
    
    /**
     * 进入会话
     */
    public void enter(String uri)
    {
    	FetionBuddy buddy = this.client.getFetionStore().getBuddy(uri);
    	if(buddy!=null) {
    		this.activeBuddy = buddy;
    		println("你现在开始和"+buddy.getDisplayName()+"交谈。");
    	}else {
    		println("找不到这个好友，请检查你的输入！");
    	}
    }
    
    /**
     * 退出会话
     * @throws Exception 
     */
    public void leave() throws Exception
    {
    	if(this.activeBuddy!=null) {
    		println("你已经离开和"+this.activeBuddy.getDisplayName()+"交谈。");
    		this.activeBuddy = null;
    	}else {
    		println("找不到这个好友，请检查你的输入！");
    	}
    }
    
    /**
     * 发送消息
     * @throws Exception 
     */
    public void to(String uri,String message) throws Exception
    {
    	FetionBuddy buddy = this.client.getFetionStore().getBuddy(uri);
    	if(buddy!=null) {
    		this.activeBuddy = buddy;
    		this.client.sendChatMessageEx(uri, message, this);
    	}else {
    		println("找不到这个好友，请检查你的输入！");
    	}
    	
    }
    
    /**
     * 发送手机短信
     * @throws Exception 
     */
    public void sms(String uri, String message) throws Exception
    {
    	if(this.client.sendSMSMessage(uri, message)) {
    		println("发送手机短信成功！");
    	}else {
    		println("发送手机短信失败！");
    	}
    }
    
    /**
     * 发送手机短信
     * @throws Exception 
     */
    public void sms(long mobileNo, String message) throws Exception
    {
    	if(this.client.sendSMSMessage(mobileNo, message)) {
    		println("发送手机短信成功！");
    	}else {
    		println("发送手机短信失败！");
    	}
    }
    /**
     * 添加好友
     * @throws Exception 
     */
    public void add(String uri) throws Exception
    {
    	if(client.addBuddy(Long.parseLong(uri))) {
    		println("发出添加好友请求成功！请耐性地等待用户回复。");
    	}else {
    		println("发出添加好友请求失败！");
    	}
    }
    
    /**
     * 删除好友
     * @throws Exception 
     */
    public void del(String uri) throws Exception
    {
    	if(client.deleteBuddy(uri)) {
    		println("删除好友成功！");
    	}else {
    		println("删除好友失败！");
    	}
    }
    
    /**
     * 查看好友详细资料
     * @throws Exception 
     */
    public void detail(String uri) throws Exception
    {
    	if(this.client.readBuddyDetail(uri)) {
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
    }
    /**
     * 更新好友信息
	 * 如果好友列表有部分没有显示好友昵称，就逐个获取详细信息并显示
     * @return
     */
	 public void update() throws Exception
	{
		 //为了提高效率，这里新建立一个线程完成这个任务
		 Runnable r = new Runnable(){
			public void run(){
				try {
					println("正在后台更新好友列表，你可以进行其他操作。");
					prompt();
					 Iterator<FetionBuddy> it = client.getFetionStore().getBuddyList().iterator();
					 while(it.hasNext()) {
						 FetionBuddy buddy = it.next();
						 String display = null;
						 
						 //判断当前好友是否可以显示出昵称
						 if(buddy.getLocalName()!=null && buddy.getLocalName().length()>0) {
					    		display = buddy.getLocalName();
						 }else if(buddy.getNickName()!=null && buddy.getNickName().length()>0) {
				    		display = buddy.getNickName();
						 }else if(buddy.getTrueName()!=null && buddy.getTrueName().length()>0) {
				    		display = buddy.getTrueName();
						 }else {}
						 
						 //如果不能显示，就获取好友详细信息
						 if(display==null || display.length()==0)
							 client.readBuddyDetail(buddy.getUri());
					 }
					 println("更新好友信息完成。请输入ls显示更新后的好友信息。");
					 prompt();
                } catch (Exception e) {
                	println("更新好友信息失败:"+e);
                	prompt();
                }
			}
		};
		Thread t = new Thread(r);
		t.start();
	}

    
    /**
     * 格式化状态
     * @param pre
     * @return
     */
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
    
    /**
     * 格式化字符串
     */
    public String fomartString(String str, int len)
    {
    	if(str!=null) {
    		if(str.length()>len)
    			return str.substring(0,len)+".";
    		else
    			return str;
    	}else {
    		return "";
    	}
    }

    
    /**
     * 同意对方请求
     * @throws Exception 
     */
    public void agree(String uri) throws Exception
    {
    	this.client.agreedApplication(uri);
    }
    
    /**
     * 拒绝对方请求
     * @throws Exception 
     */
    public void decline(String uri) throws Exception
    {
    	this.client.declinedApplication(uri);
    	
    }
    
    /**
     * 退出程序
     * @throws Exception 
     */
    public void exit() throws Exception
    {
    	this.client.logout();
    	println("你已经成功的退出！");
    }
    
    /**
     * 输入提示符
     */
    public void prompt()
    {
    	try {
    	if(this.activeBuddy!=null)
			writer.append(this.client.getFetionUser().getDisplayName()+"@maplefetion^["+this.activeBuddy.getDisplayName()+"]>>");
		else
			writer.append(this.client.getFetionUser().getDisplayName()+"@maplefetion>>");
    	writer.flush();
    	}catch (Exception e) {
    		e.printStackTrace();
		}
    }
    /**
	 * 接受到系统消息
	 */
    @Override
    public void systemMessageRecived(String m)
    {
    	println("[系统消息]:"+m);
    	prompt();
    }

	/**
	 * 客户端致命错误
	 */
    @Override
    public void exceptionCaught(Throwable exception)
    {
	    this.errorFlag = true;
	    this.mainThread.interrupt();
		println("程序内部错误，将退出并关闭，对此感到很抱歉，请将以下信息发布到项目首页反馈错误信息或者给作者发电子邮件寻求帮助。");
		exception.printStackTrace();
    }

    /**
     * 登陆状态改变的回调函数
     */
    @Override
    public void loginStatusChanged(int i)
    {
    	String msg = null;
    	switch(i) {
    	case LOGIN_INIT:
    		msg = "初始化登陆。。"; 
    		break;
    	case LOGIN_QUERY_SYSTEM_CONFIG: 
    		msg = "获取系统配置。。"; 
    		break;
    	case LOGIN_SSI_SIGN_IN: 
    		msg = "SSI登陆中。。"; 
    		break;
    	case LOGIN_SERVER_CONNECT: 
    		msg = "连接至主服务器。。";
    		break;
    	case LOGIN_SERVER_REGISTER:
    		msg = "注册主服务器。。";
    		break;
    	case LOGIN_SERVER_AUTH:
    		msg = "验证用户名和密码。。";
    		break;
    	case LOGIN_SERVER_GET_PERSONAL_INFO:
    		msg = "获取用户个人信息。。";
    		break;
    	case LOGIN_SERVER_GET_CONTECT_LIST:
    		msg = "获取好友列表。。";
    		break;
    	case LOGIN_SERVER_GET_CONTACTS_INFO:
    		msg = "获取好友详细信息。。";
    		break;
    	case LOGIN_SERVER_SUB_NOTIFY:
    		msg = "订阅异步通知。。";
    		break;
    	
    	case LOGIN_USER_OVER_DRAW:
    		msg = "该用户已经停机！";
    		break;
    		
    	case LOGIN_SSI_AUTH_FAILED:
    		msg = "用户名或者密码错误！";
    		break;
    		
    	case LOGIN_SUCCESS: 
    		msg = "登陆成功！";
    		break;
    	
    	//TODO .. 添加更多的状态信息
    		
    	default:
    		msg = Integer.toString(i); 
    	}
    	println("[登陆状态]:"+msg);
	    
    }
    
	/**
	 * 消息发送完毕的回调函数
	 */
    @Override
    public void messageSended(String uri, String content, boolean isSuccess)
    {
    	FetionBuddy buddy = client.getFetionStore().getBuddy(uri);
    	if(!isSuccess && buddy!=null) {
    		println("[系统消息]:你发给 "+buddy.getDisplayName()+" 的消息   "+content+" 发送失败！");
    		prompt();
    	}else {
    		//如果发送成功啥也不显示，没有消息是最好的消息
    	}
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args.length<2) {
			System.out.println("参数不正确，用法：java -d MapleFetion.jar net.solosky.maplefetion.MapleFetion 手机号码 飞信密码");
		}else {
			try {
				MapleFetionDemo  fetion = new MapleFetionDemo(Long.parseLong(args[0]), args[1]);
	            if(fetion.login()) {
	            	fetion.mainloop();
	            }
            } catch (Exception e) {
            	System.out.println("程序内部错误，将退出并关闭，对此感到很抱歉，请将以下信息发布到项目首页反馈错误信息或者给作者发电子邮件寻求帮助。");
        		e.printStackTrace();
            }
		}
	}

}
