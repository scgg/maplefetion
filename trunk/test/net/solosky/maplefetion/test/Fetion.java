package net.solosky.maplefetion.test;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import net.solosky.maplefetion.ClientState;
import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.LoginState;
import net.solosky.maplefetion.NotifyEventAdapter;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.bean.MobileBuddy;
import net.solosky.maplefetion.bean.Presence;
import net.solosky.maplefetion.bean.Relation;
import net.solosky.maplefetion.event.ActionEvent;
import net.solosky.maplefetion.event.ActionEventType;
import net.solosky.maplefetion.event.action.ActionEventFuture;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.store.FetionStore;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @version create: 2010-6-4 上午10:56:55
 */
public class Fetion extends NotifyEventAdapter {

	private FetionClient client;

	private boolean isLogin = false;

	private Hashtable<String, String> buddymap;

	public Fetion(long mobile, String password) {
		client = new FetionClient(mobile, password, this);
		buddymap = new Hashtable<String, String>();
	}

	public static void main(String[] args) throws Exception {
		Fetion fetion = new Fetion(13880918643L, "peter3140263");
		fetion.login(Presence.HIDEN);
	}

	public void login(int presence) {
		this.client.enableGroup(false);
		this.client.login(presence);
	}

	public void loginSuccess() {
		setLogin(true);
		System.out.println("Login Success");
		list();

		ActionEventFuture aef = client.sendChatMessage(client.getFetionStore()
				.getBuddyByUri("sip:483491920@fetion.com.cn;p=7724"),
				new Message("测试4"));
		try {
			ActionEvent ae = aef.waitActionEventWithException(30000);
			if (ae.getEventType() == ActionEventType.SUCCESS) {
				System.out.println("发送成功");
			} else
				System.out.println("发送失败");
		} catch (RequestTimeoutException e) {
			e.printStackTrace();
			System.out.println("发送失败" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("发送失败" + e.getMessage());
		}
		this.exit();
	}

	public void exit() {
		this.client.logout();
		println("你已经成功的退出！");
	}

	public void println(String s) {
		System.out.println(s);
	}

	public void list() {
		println("\n=================================");
		println("所有好友列表");
		println("-------------------------------");
		println("#ID\t好友昵称\t在线状态\t个性签名");
		FetionStore store = this.client.getFetionStore();
		Iterator<Cord> it = store.getCordList().iterator();
		int id = 0;
		this.buddymap.clear();
		// 分组显示好友
		while (it.hasNext()) {
			Cord cord = it.next();
			id = cord(cord.getId(), cord.getTitle(), id, store
					.getBuddyListByCord(cord));
		}
		id = cord(-1, "默认分组", id, store.getBuddyListWithoutCord());
	}

	public int cord(int cordId, String name, int startId,
			Collection<Buddy> buddyList) {
		Iterator<Buddy> it = buddyList.iterator();
		Buddy buddy = null;
		println("\n-------------------------------");
		println("【" + cordId + "::" + name + "】");
		println("-------------------------------");
		if (buddyList.size() == 0) {
			println("暂无好友。。");
		}
		while (it.hasNext()) {
			buddy = it.next();
			this.buddymap.put(Integer.toString(startId), buddy.getUri());
			String impresa = null;
			if (buddy instanceof FetionBuddy) {
				impresa = ((FetionBuddy) buddy).getImpresa();
			} else {
				impresa = "";
			}
			println(Integer.toString(startId) + " "
					+ formatRelation(buddy.getRelation()) + " "
					+ fomartString(buddy.getDisplayName(), 10) + "\t"
					+ fomartPresence(buddy) + "\t" + impresa);
			startId++;
		}
		return startId;
	}

	public String fomartPresence(Buddy buddy) {
		if (buddy instanceof MobileBuddy)
			return "短信在线";

		FetionBuddy b = (FetionBuddy) buddy;
		int p = buddy.getPresence().getValue();
		if (p == Presence.ONLINE)
			return "电脑在线";
		else if (p == Presence.AWAY)
			return "电脑离开";
		else if (p == Presence.BUSY)
			return "电脑忙碌";
		else if (p == Presence.OFFLINE && b.getSMSPolicy().isSMSOnline())
			return "短信在线";
		else
			return "离线";
	}

	public String fomartString(String str, int len) {
		if (str != null) {
			if (str.length() > len)
				return str.substring(0, len) + ".";
			else
				return str;
		} else {
			return "";
		}
	}

	public String formatRelation(Relation relation) {
		switch (relation) {
		case BUDDY:
			return "B";
		case UNCONFIRMED:
			return "W";
		case DECLINED:
			return "X";
		case STRANGER:
			return "？";
		case BANNED:
			return "@";
		default:
			return "-";
		}

	}

	@Override
	public void loginStateChanged(LoginState state) {
		switch (state) {

		case SEETING_LOAD_DOING: // 加载自适应配置
			println("获取自适应系统配置...");
			break;
		case SSI_SIGN_IN_DOING: // SSI登录
			println("SSI登录...");
			break;
		case SIPC_REGISTER_DOING: // 注册SIPC服务器
			println("服务器验证...");
			break;
		case GET_CONTACTS_INFO_DOING: // 获取联系人信息
			println("获取联系人...");
			break;
		case GET_GROUPS_INFO_DOING: // 获取群消息
			println("获取群信息...");
			break;
		case GROUPS_REGISTER_DOING: // 注册群
			println("群登录...");
			break;

		// 以下是成功信息，不提示
		case SETTING_LOAD_SUCCESS:
		case SSI_SIGN_IN_SUCCESS:
		case SIPC_REGISGER_SUCCESS:
		case GET_CONTACTS_INFO_SUCCESS:
		case GET_GROUPS_INFO_SUCCESS:
		case GROUPS_REGISTER_SUCCESS:
			break;

		case LOGIN_SUCCESS:
			println("登录成功");
			//这里需要启用一个新的线程来完成登录，原因请参见NotifyEventListener的注释
			//不能在这个线程做同步操作，但可以进行异步的操作，如异步发送消息
			new Thread(new Runnable(){
				public void run(){
					loginSuccess();
			}}).start();
			
			break;

		case SSI_NEED_VERIFY:
		case SSI_VERIFY_FAIL:
			if (state == LoginState.SSI_NEED_VERIFY)
				println("需要验证, 请输入目录下的[verify.png]里面的验证码:");
			else
				println("验证码验证失败，刷新验证码中...");

			// // 启动一个新线程完成重新登录的操作，让回调函数马上返回，详细信息请参见NotifyListener里面的注释
			// new Thread(new Runnable() {
			// public void run() {
			// VerifyImage img = client.fetchVerifyImage();
			// if (img != null) {
			// saveImage(img.getImageData());
			// img.setVerifyCode(readLine());
			// client.login(img);
			// } else {
			// println("刷新验证图片失败···");
			// }
			// }
			// }).start();
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
			println("其他状态:" + state.name());
			break;
		}

	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}

	public boolean isLogin() {
		return isLogin;
	}
}
