package net.solosky.maplefetion.demo;

import net.solosky.maplefetion.FetionClient;
import net.solosky.maplefetion.LoginListener;
import net.solosky.maplefetion.LoginState;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.client.dialog.ActionStatus;
import net.solosky.maplefetion.client.dialog.DialogException;
import net.solosky.maplefetion.net.RequestTimeoutException;
import net.solosky.maplefetion.net.TransferException;


/**
 * 这是个简单的飞信演示程序
 * 可以给指定的手机号码发送消息然后退出，前提是这个手机号码的飞信用户是你的好友，否则会发送失败。
 * 参数说明： SimpleFetion 手机号 密码 发送消息的手机号 消息内容
 * @author solosky
 *
 */
public class SimpleFetion {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length<4){
			System.out.println("参数不正确。参数格式为：手机号 密码 发送消息的手机号 消息内容");
			System.out.println("说明：可以给指定的手机号码发送消息然后退出，前提是这个手机号码的飞信用户是你的好友，否则会发送失败。");
		}else{
			FetionClient client = new FetionClient(Long.parseLong(args[0]), args[1]);
			System.out.println("正在登录中，可能需要1分钟左右，请稍候...");
			
			//这里设置一个
			client.setLoginListener(new LoginListener() {
				public void loginStateChanged(LoginState state) {
					System.out.println("登录状态："+state.name());
				}
			});
			//禁用掉群，登录可以变得快一点
			client.enableGroup(false);		
			
			//这里为了编程方便使用了同步登录，当然推荐异步登录，使用登录状态回调函数来完成登录成功后的操作
			LoginState state = client.syncLogin();
			if(state==LoginState.LOGIN_SUCCESS){	//登录成功
				System.out.println("登录成功，正在发送消息至 "+args[2]+",请稍候...");
				try{
					int status = client.sendChatMessage(Long.parseLong(args[2]), new Message(args[3]));
					if(status==ActionStatus.ACTION_OK){
						System.out.println("发送成功，消息已通过服务直接发送到对方客户端！");
					}else if(status==ActionStatus.SEND_SMS_OK){
						System.out.println("发送成功，消息已通过短信发送到对方手机！");
					}else if(status==ActionStatus.INVALD_BUDDY){
						System.out.println("发送失败, 该用户可能不是你好友，请尝试添加该用户为好友后再发送消息。");
		        	}else if(status==ActionStatus.NOT_FOUND) {
		        		System.out.println("发送失败, 该用户不是移动用户。");
		        	}else {
		        		System.out.println("发送失败, 其他错误，代码"+status);
		        	}
				} catch (RequestTimeoutException e) {
					System.out.println("发送失败, 超时");
	            } catch (TransferException e) {
	            	System.out.println("发送失败, 网络异常");
	            } catch (InterruptedException e) {
	            	System.out.println("发送失败, 发送被中断");
	            } catch (DialogException e) {
	            	System.out.println("发送失败, 建立会话失败");
	            }
	            
	            //无论发送成功还是失败，因为登录了，就必须退出，释放线程资源，否则客户端不会主动退出
	            //如果登录失败了，客户端会主动释放线程资源，不需要退出
	            System.out.print("正在退出客户端...");
	            client.logout();
	            System.out.println("已经退出。");
	            
			}else if(state==LoginState.SSI_AUTH_FAIL){
				System.out.println("你输入的手机号或者密码不对，请检查后重试!");
			}else if(state==LoginState.SSI_CONNECT_FAIL){
				System.out.println("SSI连接失败！！");
			}else if(state==LoginState.SIPC_CONNECT_FAIL){
				System.out.println("SIPC服务器连接失败！！");
			}else{
				System.out.println("登录失败，原因："+state.name());
			}
		}
		
	}

}
