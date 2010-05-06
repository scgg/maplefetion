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
 * File     : FetionDemo.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-18
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.BuddyExtend;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.MobileBuddy;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.bean.VerifyImage;
import net.solosky.maplefetion.client.dialog.ActionListener;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.ChatDialog;
import net.solosky.maplefetion.client.dialog.GroupDialog;
import net.solosky.maplefetion.net.AutoTransferFactory;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.store.FetionStore;
import net.solosky.maplefetion.store.SimpleFetionStore;

/**
 * 这个是MapleFetion的演示程序，也提供了一个完整的命令行下的飞信
 * 
 * @author solosky <solosky772@qq.com>
 */
public class FetionDemo implements LoginListener, NotifyListener
{
	/**
	 * 飞信客户端
	 */
	private FetionClient client;
	
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
	private ChatDialog activeChatDialog;
	
	/**
	 * 好友序号到好友飞信地址的映射
	 */
	private Hashtable<String, String> buddymap;
	
	/**
	 * 群序号到群地址的映射
	 */
	private Hashtable<String, String> groupmap;
	 
	 /**
	  * 默认构造函数
	  * @param mobile
	  * @param pass
	  */
	
	public FetionDemo(long mobile, String pass)
	{
		this.client = new FetionClient(mobile, pass, new AutoTransferFactory(),
				new SimpleFetionStore(), this, this);
		this.reader = new BufferedReader(new InputStreamReader(System.in));
		this.writer = new BufferedWriter(new OutputStreamWriter(System.out));
		this.buddymap = new Hashtable<String, String>();
		this.groupmap = new Hashtable<String, String>();
	}
	
	
	public void login()
	{
		this.client.login();
	}
	
	
	public static void main(String[] args) throws Exception
	{
		if(args.length<2) {
			System.out.println("参数不正确，用法：java net.solosky.maplefetion.FetionDemo 手机号码 飞信密码");
		}else {
    		FetionDemo demo = new FetionDemo(Long.parseLong(args[0]), args[1]);
    		demo.welcome();
    		demo.login();
		}
	}
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.LoginListener#loginStateChanged(LoginState)
     */
	@Override
    public void loginStateChanged(LoginState state)
    {
	    switch (state)
        {
	    	
        case	SEETING_LOAD_DOING:		//加载自适应配置
        	println("获取自适应系统配置...");
	    	break;
        case	SSI_SIGN_IN_DOING:		//SSI登录
        	println("SSI登录...");
	    	break;
        case	SIPC_REGISTER_DOING:		//注册SIPC服务器
        	println("服务器验证...");
	    	break;
        case	GET_CONTACTS_INFO_DOING:	//获取联系人信息
        	println("获取联系人...");
	    	break;
        case	GET_GROUPS_INFO_DOING:	//获取群消息
        	println("获取群信息...");
	    	break;
        case	GROUPS_REGISTER_DOING:	//注册群
        	println("群登录...");
	    	break;
        	
        //以下是成功信息，不提示
        case	SETTING_LOAD_SUCCESS:
        case	SSI_SIGN_IN_SUCCESS:
        case	SIPC_REGISGER_SUCCESS:
        case	GET_CONTACTS_INFO_SUCCESS:
        case	GET_GROUPS_INFO_SUCCESS:
        case	GROUPS_REGISTER_SUCCESS:
        	break;


        case LOGIN_SUCCESS:
        	println("登录成功");
        	this.loginSuccess();
        	break;
        	
        	
        case SSI_NEED_VERIFY:
        case SSI_VERIFY_FAIL:
        	if(state==LoginState.SSI_NEED_VERIFY)
        		println("需要验证, 请输入目录下的v.png里面的验证码:");
        	else
        		println("验证码验证失败，刷新验证码中...");
        	
        	VerifyImage img = client.fetchVerifyImage();
        	if(img!=null) {
        		saveImage(img.getImageData());
        		img.setVerifyCode(readLine());
        		client.login(img);
        	}else {
        		println("刷新验证码失败");
        	}
	        break;

        case SSI_CONNECT_FAIL:
        	println("SSI连接失败!");
        	break;
        	
        case SIPC_TIMEOUT:
        	println("登陆超时！");
        	break;
        	
        case SSI_AUTH_FAIL:
        	println("用户名或者密码错误!");
        	break;
        
        default:
        	println("其他状态:"+state.name());
	        break;
        }
	    
    }  
    
	/**
	 * 欢迎信息
	 * @throws Exception 
	 */
	 public void welcome() throws Exception
	{
		println("================================================");
		println("|              "+FetionClient.CLIENT_VERSION +"           |");
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
	    	println("tel 手机号码 消息内容       通过手机号给好友发送消息（对方必须是好友才行）");
	    	println("enter 好友编号             和好友对话");
	    	println("leave                      离开当前对话");
	    	println("dialog                     显示当前所有会话");
	    	println("group                     显示所有的群列表");
	    	println("say 群编号 消息内容         给群发送消息");
	    	println("nickname 新昵称            修改自己昵称");
	    	println("impresa 个性签名           修改个性签名");
			println("localname 好友编号 新名字  修改好友的显示名字");
			println("cord 好友编号 新组编号     修改好友分组");
			println("newcord 分组标题           创建新的分组");
			println("delcord 分组编号           删除分组");
			println("cordtitle 分组编号 分组标题   修改分组标题");
			println("self 消息内容              给自己发送短信");
			println("presence away/online/busy/hiden   改变自己在线状态");
	    	println("exit                       退出登录");
	    	println("help                       帮助信息");
	    	println("=========================================");
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
	    	FetionStore store = this.client.getFetionStore();
	    	Iterator<Cord> it = store.getCordList().iterator();
	    	int id=0;
	    	this.buddymap.clear();
	    	//分组显示好友
	    	while(it.hasNext()) {
	    		Cord cord = it.next();
	    		id = cord(cord.getId(),cord.getTitle(),id, store.getBuddyListByCord(cord));
	    	}
	    	id = cord(-1,"默认分组", id, store.getBuddyListWithoutCord());
	    }
	    
	    /**
	     * 显示一个组的用户
	     */
	    public int cord(int cordId, String name,int startId,Collection<Buddy> buddyList)
	    {
	    	Iterator<Buddy> it = buddyList.iterator();
	    	Buddy buddy = null;
	    	println("\n-------------------------------");
	    	println("【"+cordId+"::"+name+"】");
	    	println("-------------------------------");
	    	if(buddyList.size()==0) {
	    		println("暂无好友。。");
	    	}
			while(it.hasNext()) {
				buddy = it.next();
				this.buddymap.put(Integer.toString(startId), buddy.getUri());
				String impresa = null;
				if(buddy instanceof FetionBuddy) {
					impresa = ((FetionBuddy) buddy).getImpresa();
				}else {
					impresa = "";
				}
				println(Integer.toString(startId)+" "+formatRelation(buddy.getRelation().getValue())+" "+fomartString(buddy.getDisplayName(),10)+"\t"
						+fomartPresence(buddy)
						+"\t"+impresa);
				startId++;
			}
			return startId;
	    }
	    
	   public void group()
	   {
		   Iterator<Group> it = this.client.getFetionStore().getGroupList().iterator();
		   int groupId = 0;
		   println("===========================群列表=================================");
		   while(it.hasNext()) {
			   Group group = it.next();
			   this.groupmap.put(Integer.toString(groupId), group.getUri());
			   println(groupId+"::"+group.getName()+'\t'+group.getBulletin()+"\t"+ group.getIntro());
			   println("-----------------------------------------------------------------");
			   Iterator<Member> mit = this.client.getFetionStore().getGroupMemberList(group).iterator();
			   while(mit.hasNext()) {
				   Member member = mit.next();
				   println('\t'+member.getDisplayName()+"\t"+member.getUri());
			   }
			   println("-----------------------------------------------------------------");
			   groupId++;
		   }
	   }
	    
	    /**
	     * 发送消息
	     * @throws Exception 
	     */
	    public void to(String uri,final String message)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy!=null) {
	    		ChatDialog dialog = this.client.getDialogFactory().findChatDialog(buddy);
	    		if(dialog!=null) {
	    			this.send(dialog, message);
	    		}else {
	    			try {
	                    dialog = this.client.getDialogFactory().createChatDialog(buddy);
	                    this.open(dialog, message);
                    } catch (FetionException e) {
	                    println("建立对话框失败~"+e.getMessage());
                    }
	    		}
	    	}else {
	    		println("找不到这个好友，请检查你的输入！");
	    	}
	    }
	    
	    public void tel(final String tel, String msg)
	    {
	    	long mobile = Long.parseLong(tel);
	    	int status;
            try {
	            status = this.client.sendChatMessage(mobile, Message.wrap(msg));
	            if(status==ActionStatus.ACTION_OK||status==ActionStatus.SEND_SMS_OK) {
	        		println("发送消息给用户"+tel+"成功！");
	        	}else if(status==ActionStatus.INVALD_BUDDY){
	        		println("发送消息给用户"+tel+"失败, 该用户可能不是你好友，请尝试添加该用户为好友后再发送消息。");
	        	}else if(status==ActionStatus.NOT_FOUND) {
	        		println("发送消息给用户"+tel+"失败, 该用户可能不是你好友，请尝试添加该用户为好友后再发送消息。");
	        	}else {
	        		println("发送消息给用户"+tel+"失败, 其他错误，代码"+status);
	        	}
            } catch (RequestTimeoutException e) {
            	println("发送消息给用户"+tel+"失败, 超时");
            } catch (TransferException e) {
            	println("发送消息给用户"+tel+"失败, 网络异常");
            } catch (InterruptedException e) {
            	println("发送消息给用户"+tel+"失败, 发送被中断");
            }
	    	
	    }
	    
	    /**
	     * 发送手机短信消息
	     */
	    public void sms(String uri, final String message)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy!=null) {
	    		this.client.sendSMSMessage(buddy, Message.wrap(message), new ActionListener(){
					public void actionFinished(int status){
						if(status==ActionStatus.SEND_SMS_OK){
							println("提示：发送给"+buddy.getDisplayName()+" 的短信发送成功！");
						}else{
							println("[系统消息]:你发给 "+buddy.getDisplayName()+" 的短信  "+message+" 发送失败！");
						}
					}
				});
	    	}else {
	    		println("找不到这个好友，请检查你的输入！");
	    	}
	    }
	    
	    /**
	     * 给自己发送短信
	     */
	    public void self(String message)
	    {
	    	this.client.sendSMSMessage(this.client.getFetionUser(), Message.wrap(message), new ActionListener(){
                public void actionFinished(int status){
	                if(status==ActionStatus.ACTION_OK) {
	                	println("给自己发送短信成功！");
	                }else {
	                	println("给自己发送短信失败！");
	                }
                }
	    	});
	    }
	    
	    /**
	     * 发送群消息
	     */
	    public void say(String groupuri, String message) {
	    	if(groupuri==null)	return;
	    	final Group group = this.client.getFetionStore().getGroup(groupuri);
	    	if(group!=null) {
	    		GroupDialog dialog = this.client.getDialogFactory().findGroupDialog(group);
	    		if(dialog!=null) {
	    			dialog.sendChatMessage(Message.wrap(message), new ActionListener() {
	    				public void actionFinished(int status) {
	    					if(status==ActionStatus.ACTION_OK) {
	    						println("提示：发送给群 "+group.getName()+" 的消息发送成功！");
	    	                }else {
	    	            		println("提示：发送给群 "+group.getName()+" 的消息发送失败！");
	    					}
	    				}
	    			});
	    		}
	    	}
	    }
	    
	    /**
	     * 获取好友详细信息
	     */
	    public void detail(String uri)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy==null) {
	    		println("找不到好友，请重新输入好友信息");
	    	}else if(buddy instanceof MobileBuddy) {
	    		printBuddyInfo(buddy);
	    	}else if(buddy instanceof FetionBuddy) {
	    		this.client.getBuddyDetail((FetionBuddy)buddy, new ActionListener() {
	    			public void actionFinished(int status) {
	    				if(status==ActionStatus.ACTION_OK) {
	    					printBuddyInfo(buddy);
	    				}else {
	    					println("获取好友信息失败~");
	    				}
	    			}
	    		});
	    	}
	    }
	    
	    /**
	     * 打印好友信息
	     */
	    public void printBuddyInfo(Buddy buddy)
	    {
	    	System.out.println("----------------------------------");
    	    System.out.println("好友详细信息");
    	    System.out.println("----------------------------------");
    	    System.out.println("URI:"+buddy.getUri());
    	    System.out.println("昵称:"+buddy.getDisplayName());
    	    System.out.println("所在分组:"+buddy.getCordId());
    	    System.out.println("状态:"+fomartPresence(buddy));
    	    System.out.println("备注:"+buddy.getLocalName());
    	    System.out.println("手机号码:"+buddy.getMobile());
    	    
    	    if(buddy instanceof FetionBuddy) {
    	    	FetionBuddy fbuddy = (FetionBuddy) buddy;
        	    BuddyExtend extend = fbuddy.getExtend();
        	    System.out.println("个性签名:"+fbuddy.getImpresa());
        	    System.out.println("国家:"+extend.getNation());
        	    System.out.println("省份:"+extend.getProvince());
        	    System.out.println("城市:"+extend.getCity());
        	    System.out.println("EMAIL:"+extend.getEmail());
    	    }
    	    System.out.println("----------------------------------");
	    }
	    
	    /**
	     * 改变自己昵称
	     * @throws Exception 
	     */
	    public void nickname(String nickName)
	    {
	    	User user = this.client.getFetionUser();
	    	user.setNickName(nickName);
	    	this.client.setPersonalInfo(new ActionListener() {
                public void actionFinished(int status)
                {
                	if(status==ActionStatus.ACTION_OK) {
        	    		println("更改昵称成功！");
        	    	}else {
        	    		println("更改昵称失败！");
        	    	}
                }
	    	});
	    }
	    
	    /**
	     * 改变自己的个性签名
	     * @throws Exception 
	     */
	    public void impresa(String impresa) throws Exception
	    {
	    	User user = this.client.getFetionUser();
	    	user.setImpresa(impresa);
	    	this.client.setPersonalInfo(new ActionListener() {
                public void actionFinished(int status){
                	if(status==ActionStatus.ACTION_OK) {
                		println("更改个性签名成功！");
        	    	}else {
        	    		println("更改个性签名失败！");
        	    	}
                }
	    	});
	    }
	    
	    /**
	     * 改变好友的显示姓名
	     * @param uri
	     * @param localName
	     */
	    public void localname(String uri, String localName)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy!=null) {
	    		this.client.setBuddyLocalName(buddy, localName, new ActionListener() {
	    			public void actionFinished(int status){
	    				if(status==ActionStatus.ACTION_OK) {
	    					println("更改好友显示姓名成功！");
	    		    	}else {
	    		    		println("更改好友显示姓名失败！");
	    				}
	    			}
	    		});
	    	}else {
	    		println("找不到这个好友，请检查你的输入！");
	    	}
	    }
	    
	    
	    /**
	     * 设置好友分组
	     */
	    public void cord(String uri, String cordId)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	Collection<Cord> cordList = new ArrayList<Cord>();
	    	//本来一个好友可以分到多个组的，为了简单这里只实现了修改一个分组
	    	int cid = -1;
            try {
	            cid = Integer.parseInt(cordId);
            } catch (NumberFormatException e) {
            	println("分组编号不是数字，请检查后重新输入。");
            	return;
            }
	    	if(cid!=-1) {
	    		Cord cord = this.client.getFetionStore().getCord(cid);
	    		if(cord==null) {
	    			println("分组编号不存在，请检查后重新输入。");
	    			return;
	    		}else {
	    			cordList.add(cord);
	    		}
	    	}
	    	
	    	if(buddy!=null) {
	    		this.client.setBuddyCord(buddy, cordList,  new ActionListener() {
	    			public void actionFinished(int status){
	    				if(status==ActionStatus.ACTION_OK) {
	    					println("更改好友分组成功！");
	    		    	}else {
	    		    		println("更改好友分组失败！");
	    				}
	    			}
	    		});
	    	}else {
	    		println("找不到这个好友，请检查你的输入！");
	    	}
	    }
	    
	    /**
	     * 创建新的分组
	     * @param title
	     */
	    public void newcord(String title)
	    {
	    	client.createCord(title, new ActionListener() {

				@Override
                public void actionFinished(int status)
                {
					if(status==ActionStatus.ACTION_OK) {
                		println("创建新的分组成功！");
        	    	}else {
        	    		println("创建新的分组失败！");
        	    	}
                }
	    		
	    	});
	    }
	    
	    /**
	     * 设置分组标题
	     * @param cordid
	     * @param title
	     */
	    public void cordtitle(String cordId, String title)
	    {
	    	Cord cord = this.getCord(cordId);
	    	if(cord!=null) {
	    		this.client.setCordTitle(cord, title, new ActionListener() {
                    public void actionFinished(int status)
                    {
                    	if(status==ActionStatus.ACTION_OK) {
                    		println("设置分组标题成功！");
            	    	}else {
            	    		println("设置分组标题失败！");
            	    	}
                    }
	    		});
	    	}
	    }
	    
	    /**
	     * 删除分组
	     * @param cordid
	     */
	    public void delcord(String cordId)
	    {
	    	Cord cord = this.getCord(cordId);
	    	if(cord!=null) {
	    		Collection<Buddy> list = this.client.getFetionStore().getBuddyListByCord(cord);
	    		if(list!=null && list.size()>0) {
	    			println("分组编号 "+cordId+" 中好友不为空，请移除该组的好友后再尝试删除。");
	    			return;
	    		}
	    		this.client.deleteCord(cord, new ActionListener() {
                    public void actionFinished(int status)
                    {
                    	if(status==ActionStatus.ACTION_OK) {
                    		println("删除分组成功！");
            	    	}else {
            	    		println("删除分组失败！");
            	    	}
                    }
	    		});
	    	}
	    }
	    
	    /*
	     * 添加好友
	     * @throws Exception 
	     */
	    public void add(String mobile)
	    {
	    	client.addBuddy(Long.parseLong(mobile), new ActionListener() {
                public void actionFinished(int status){
                	if(status==ActionStatus.ACTION_OK) {
                		println("发出添加好友请求成功！请耐性地等待用户回复。");
        	    	}else {
        	    		println("发出添加好友请求失败！");
        	    	}
                }
	    		
	    	});
	    }
	    
	    /**
	     * 删除好友
	     */
	    public void del(String uri)
	    {
	    	Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy!=null) {
	    		client.deleteBuddy(buddy, new ActionListener() {
	    			public void actionFinished(int status) {
	    				if(status==ActionStatus.ACTION_OK) {
	    					println("删除好友成功！");
	    		    	}else {
	    		    		println("删除好友失败！");
	        	    	}
	    			}
	    		});
	    	}else {
	    		println("对不起，好友"+uri+"不存在！");
	    	}
	    }
	    
	    /**
	     * 同意对方请求
	     */
	    public void agree(String uri)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy!=null) {
	    		this.client.agreedApplication(buddy,  new ActionListener() {
	    			public void actionFinished(int status) {
	    				if(status==ActionStatus.ACTION_OK) {
	    					println("你已经同意"+buddy.getDisplayName()+"的添加你为好友的请求。");
	        	    	}else {
	        	    		println("同意对方请求失败！");
	        	    	}
	    			}
	    		});
	    	}
	    }
	    
	    
	    /**
	     * 拒绝陌生人添加好友请求
	     * @param uri
	     */
	    public void decline(String uri)
	    {
	    	final Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	if(buddy!=null) {
	    		this.client.declinedApplication(buddy,  new ActionListener() {
	    			public void actionFinished(int status) {
	    				if(status==ActionStatus.ACTION_OK) {
	    		    		println("你已经拒绝"+buddy.getDisplayName()+"的添加你为好友的请求。");
	        	    	}else {
	        	    		println("拒绝对方请求失败！");
	        	    	}
	    			}
	    		});
	    	}
	    }
	    
	    /**
	     * 进入对话框
	     */
	    public void enter(String uri)
	    {
	    	Buddy buddy = this.client.getFetionStore().getBuddyByUri(uri);
	    	this.activeChatDialog = this.client.getDialogFactory().findChatDialog(buddy);
	    	if(this.activeChatDialog==null) {
	    		try {
	                this.activeChatDialog = this.client.getDialogFactory().createChatDialog(buddy);
	                this.open(this.activeChatDialog, null);
	                println("提示：你现在可以和 "+ buddy.getDisplayName()+" 聊天了。");
                } catch (FetionException e) {
                	println("建立对话框失败~"+e.getMessage());
                }
	    	}
	    }
	    
	    /**
	     * 离开对话框
	     */
	    public void leave()
	    {
	    	try {
	            this.client.getDialogFactory().closeDialog(this.activeChatDialog);
	            this.activeChatDialog = null;
            } catch (Exception e) {
            	println("关闭对话框失败~"+e.getMessage());
            }
	    }
	    
	    /**
	     * 发送短信
	     * @param dialog
	     * @param message
	     */
	    private void send(ChatDialog dialog,final String message)
	    {
	    	final Buddy buddy = dialog.getMainBuddy();
	    	dialog.sendChatMessage( Message.wrap(message), new ActionListener(){
				public void actionFinished(int status){
					if(status==ActionStatus.ACTION_OK){
						println("提示："+buddy.getDisplayName()+" 在线，消息已经发送到飞信客户端。");
					}else if(status==ActionStatus.SEND_SMS_OK){
						println("提示："+buddy.getDisplayName()+" 不在线，消息已以长短信的方式发送到好友手机。");
					}else{
						println("[系统消息]:你发给 "+buddy.getDisplayName()+" 的短信  "+message+" 发送失败！");
					}
				}
			});
	    }
	    
	    /**
	     * 建立一个对话框并发送消息
	     * @param buddy
	     * @param message
	     */
	    private void open(final ChatDialog dialog, final String message)
	    {
	    	dialog.openDialog(new ActionListener() {
            	public void actionFinished(int status) {
            		if(status==ActionStatus.ACTION_OK) {
            			if(message!=null)
            				send(dialog, message);
            		}else {
            			println("打开聊天对话框失败");
            		}
            	}
            });
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
	    		to = Presence.AWAY;
	    	}else if(presence.endsWith("online")) {
	    		to = Presence.ONLINE;
	    	}else if(presence.equals("busy")) {
	    		to = Presence.BUSY;
	    	}else if(presence.equals("hiden")) {
	    		to = Presence.HIDEN;
	    	}else {
	    		println("未知状态:"+presence);
	    	}
	    	if(to!=-1) {
	    			this.client.setPresence(to, new ActionListener() {
	    			public void actionFinished(int status) {
	    				if(status==ActionStatus.ACTION_OK) {
	    					println("改变状态成功！");
	    	    		}else {
	    	    			println("改变状态失败！");
	    				}
	    			}
	    		});
	    	}
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
	     * 格式化状态
	     * @param pre
	     * @return
	     */
	    public String fomartPresence(Buddy buddy)
		{
	    	if(buddy instanceof MobileBuddy)
	    		return "短信在线";
	    	
	    	FetionBuddy b = (FetionBuddy) buddy;
	    	int p = buddy.getPresence().getValue();
	    	if(p==Presence.ONLINE)
	    		return "电脑在线";
	    	else if(p==Presence.AWAY)
	    		return "电脑离开";
	    	else if(p==Presence.BUSY)
	    		return "电脑忙碌";
	    	else if(p==Presence.OFFLINE && b.getSMSPolicy().isSMSOnline())
	    		return "短信在线";
	    	else
	    		return "离线";
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
	     * 格式化关系
	     */
	    public String formatRelation(int relation)
	    {
	    	switch(relation) {
	    	case Relation.RELATION_BUDDY: return "B";
	    	case Relation.RELATION_UNCONFIRMED: return "W";
	    	case Relation.RELATION_DECLINED: return "X";
	    	case Relation.RELATION_STRANGER: return "？";
	    	case Relation.RELATION_BANNED: return "@";
	    	default: return "-";
	    	}
	    	
	    }
	    
	    /**
	     * 登录成功之后，启动主循环
	     */
	    public void loginSuccess()
	    {
	    	Runnable r = new Runnable() {
	    		public void run()
	    		{
	    			try {
	    				welcome();
		    			my();
		    			list();
	                    mainloop();
                    } catch (Exception e) {
	                    println("程序运行出错");
                    	e.printStackTrace();
                    	if(client.getState()==ClientState.ONLINE) {
                    		client.logout();
                    	}
                    }
	    		}
	    	};
	    	
	    	new Thread(r).start();
	    }
	 
    /**
     * 主循环
     * @throws Exception 
     */
    public void mainloop() throws Exception
    {
    	String line = null;
    	this.prompt();
    	while(true) {
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
    public boolean dispatch(final String line) throws Exception
    {
    	String[] cmd = line.split(" ");
    	if(cmd[0].equals("welcome")) {
			this.welcome();
		}else if(cmd[0].equals("ls")) {
			this.list();
		}else if(cmd[0].equals("my")) {
			this.my();
		}else if(cmd[0].equals("group")) {
			this.group();
		}else if(cmd[0].equals("exit")) {
			this.exit();
			return false;
		}else if(cmd[0].equals("detail")) {
			if(cmd.length>=2)
				this.detail(this.buddymap.get(cmd[1]));
		}else if(cmd[0].equals("enter")) {
			if(cmd.length>=2)
				this.enter(this.buddymap.get(cmd[1]));
		}else if(cmd[0].equals("leave")) {
			this.leave();
		}else if(cmd[0].equals("to")) {
			if(cmd.length>=3)
				this.to(this.buddymap.get(cmd[1]),line.substring(line.indexOf(cmd[2])));
		}else if(cmd[0].equals("say")) {
			if(cmd.length>=3)
				this.say(this.groupmap.get(cmd[1]),line.substring(line.indexOf(cmd[2])));
		}else if(cmd[0].equals("sms")) {
			if(cmd.length>=3)
				this.sms(this.buddymap.get(cmd[1]),line.substring(line.indexOf(cmd[2])));
		}else if(cmd[0].equals("tel")) {
			if(cmd.length>=3)
				this.tel(cmd[1], cmd[2]);
		}else if(cmd[0].equals("nickname")) {
			if(cmd.length>=2)
				this.nickname(cmd[1]);
		}else if(cmd[0].equals("impresa")) {
			if(cmd.length>=2)
				this.impresa(cmd[1]);
		}else if(cmd[0].equals("del")) {
			if(cmd.length>=2)
				this.del(this.buddymap.get(cmd[1]));
		}else if(cmd[0].equals("newcord")) {
			if(cmd.length>=2)
				this.newcord(cmd[1]);
		}else if(cmd[0].equals("delcord")) {
			if(cmd.length>=2)
				this.delcord(cmd[1]);
		}else if(cmd[0].equals("cordtitle")) {
			if(cmd.length>=3)
				this.cordtitle(cmd[1], cmd[2]);
		}else if(cmd[0].equals("add")) {
			if(cmd.length>=2)
				this.add(cmd[1]);
		}else if(cmd[0].equals("agree")) {
			if(cmd.length>=2)
				this.agree(this.buddymap.get(cmd[1]));
		}else if(cmd[0].equals("decline")) {
			if(cmd.length>=2)
				this.decline(this.buddymap.get(cmd[1]));
		}else if(cmd[0].equals("self")) {
			if(cmd.length>=2)
				this.self(cmd[1]);
		}else if(cmd[0].equals("localname")) {
			if(cmd.length>=3)
				this.localname(this.buddymap.get(cmd[1]), cmd[2]);
		}else if(cmd[0].equals("cord")) {
			if(cmd.length>=3)
				this.cord(this.buddymap.get(cmd[1]), cmd[2]);
		}else if(cmd[0].equals("presence")) {
			if(cmd.length>=2)
				this.presence(cmd[1]);
		}else if(cmd[0].equals("help")) {
			this.help();
		}
		else {
			if( line!=null && line.length()>0 ){
				if(this.activeChatDialog!=null) {
					if(this.activeChatDialog.isColsed()) {
						this.activeChatDialog = this.client.getDialogFactory()
											.createChatDialog(this.activeChatDialog.getMainBuddy());
						this.open(this.activeChatDialog, line);
					}else {
						this.send(this.activeChatDialog, line);
					}
				}else{
					println("未知命令："+cmd[0]+"，请检查后再输入。如需帮助请输入help。");
				}

			}
		}
		return true;
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
     * 输入提示符
     */
    public void prompt()
    {
    	try {
    	if(this.activeChatDialog!=null && this.activeChatDialog.isOpened())
			writer.append(this.client.getFetionUser().getDisplayName()+"@maplefetion^["+this.activeChatDialog.getMainBuddy().getDisplayName()+"]>>");
		else
			writer.append(this.client.getFetionUser().getDisplayName()+"@maplefetion>>");
    	writer.flush();
    	}catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    
    public void dispose()
    {
    }
    
    /**
     * 保存验证图片
     * @param img
     */
    private void saveImage(byte[] img)
    {
    	try {
    		FileOutputStream out = new FileOutputStream(new File("verify.png"));
        	out.write(img);
        	out.close();
        } catch (Exception e) {
        }
    }
    
    private Cord getCord(String cordId)
    {
    	int cid = -1;
        try {
            cid = Integer.parseInt(cordId);
        } catch (NumberFormatException e) {
        	println("分组编号不是数字，请检查后重新输入。");
        	return null;
        }
		Cord cord = this.client.getFetionStore().getCord(cid);
		if(cord==null) {
			println("分组编号不存在，请检查后重新输入。");
			return null;
		}else {
			return cord;
		}
    }

    
    private String readLine()
    {
    	try {
	        return this.reader.readLine();
        } catch (IOException e) {
	        e.printStackTrace();
        }
        return null;
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
    


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.NotifyListener#buddyApplication(net.solosky.maplefetion.bean.Buddy, java.lang.String)
     */
    @Override
    public void buddyApplication(Buddy buddy, String desc)
    {
    	println("[好友请求]:"+desc+" 想加你为好友。请输入 【agree/decline 好友编号】 同意/拒绝添加请求。");
    	prompt();
	    
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.NotifyListener#buddyConfirmed(net.solosky.maplefetion.bean.Buddy, boolean)
     */
    @Override
    public void buddyConfirmed(Buddy buddy, boolean isAgreed)
    {
    	if(isAgreed)
    		println("[系统通知]:"+buddy.getDisplayName()+" 同意了你的好友请求。");
    	else 
    		println("[系统通知]:"+buddy.getDisplayName()+" 拒绝了你的好友请求。");
    	
    	prompt();
	    
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.NotifyListener#buddyMessageRecived(net.solosky.maplefetion.bean.Buddy, java.lang.String, net.solosky.maplefetion.client.dialog.ChatDialog)
     */
    @Override
    public void buddyMessageRecived(Buddy from, Message message,
            ChatDialog dialog)
    {
    	if(from.getRelation().getValue()==Relation.RELATION_BUDDY)
    		println("[好友消息]"+from.getDisplayName()+" 说:"+message.getText());
    	else 
    		println("[陌生人消息]"+from.getDisplayName()+" 说:"+message.getText());
    	dialog.sendChatMessage(message, null);
    	prompt();
	    
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.NotifyListener#presenceChanged(net.solosky.maplefetion.bean.Buddy)
     */
    @Override
    public void presenceChanged(FetionBuddy b)
    {
    	if(b.getPresence().getValue()==Presence.ONLINE) {
    		println("[系统通知]:"+b.getDisplayName()+" 上线了。");
        	prompt();
    	}else if(b.getPresence().getValue()==Presence.OFFLINE){
    		println("[系统通知]:"+b.getDisplayName()+" 下线了。");
        	prompt();
    	}
	    
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.NotifyListener#groupMessageRecived(net.solosky.maplefetion.bean.Group, net.solosky.maplefetion.bean.Member, java.lang.String, net.solosky.maplefetion.client.dialog.GroupDialog)
     */
    @Override
    public void groupMessageRecived(Group group, Member from, Message message, GroupDialog dialog)
    {
	    println("[群消息] 群 "+group.getName()+" 里的 "+from.getDisplayName()+" 说："+message.getText());
	    prompt();
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.NotifyListener#statusChanged(ClientState)
     */
    @Override
    public void clientStateChanged(ClientState state)
    {
	    switch (state)
        {
        case OTHER_LOGIN:
        	println("你已经从其他客户端登录。");
        	println("30秒之后重新登录..");
        	try {
	            Thread.sleep(30000);
	            println("重新登录...");
	            client.login();
            } catch (InterruptedException e) {
            }
	        break;
        case CONNECTION_ERROR:
        	println("客户端连接异常");
	        break;
        case DISCONNECTED:
        	println("服务器关闭了连接");
        	break;
        case LOGOUT:
        	println("已经退出。。");
        	break;
        case ONLINE:
        	println("当前是在线状态。");
        	break;
        	
        default:
	        break;
        }
    }
}