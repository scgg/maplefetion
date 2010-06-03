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
 * File     : NotifyEventListener.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-6-3
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion;

import net.solosky.maplefetion.event.NotifyEvent;

/**
 *
 *	通知事件监听接口
 *
 *	收到服务器发回的相关通知便会调用
 *
 *  警告：
 *  1) 所有的回调方法应该很快的完成，不能有等待的操作，因为回调的方法是在读线程调用的
 *     这里的所有函数如果要进行有关客户端同步的操作，（如接受到好友请求马上回复同意）必须在另外一个线程调用
 *     如果在收到通知后马上调用有关客户端的同步方法就会会造成死锁，因为回调函数还在读数据线程的调用栈上，等待回复永远不可能成功
 *     可以调用异步的方法，如异步发送消息等。。
 *  2) 这里的回调方法如果发生任何异常，如NullPointerException等未受查的异常时，客户端会自动退出
 *     因为回调的方法仍在读线程，任何异常都会沿栈向上传递到读数据的方法AbstractTransfer.bytesRecieved(byte[] buff, int offset, int len)上
 *     这个方法默认捕获了所有的异常，包括受查的FetionException和不受查的RuntimeException，如果是受查的FetionException不会退出客户端，
 *     （但TransferExcetpion例外），如果是不受查的RuntimeException客户端就会认为是系统异常，封装为SystemException在栈上传递，最终会
 *     传递到客户端对象，之后客户端就会自动退出，并报告NotifyListener客户端状态改变为SYSTEM_ERROR。
 *     所以，请务必保证回调方法捕获所有的异常，防止传递到客户端使客户端退出
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public interface NotifyEventListener
{
	/**
	 * 触发通知事件
	 * @param event	通知事件，定义在net.solosky.maplefetion.event.notify.* 中
	 */
	public void fireEvent(NotifyEvent event);
}
