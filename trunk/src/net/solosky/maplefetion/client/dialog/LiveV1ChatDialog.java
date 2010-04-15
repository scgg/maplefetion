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
 * Package  : net.solosky.maplefetion.client.dialog
 * File     : LiveV1ChatDialog.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-14
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.client.dialog;

import net.solosky.maplefetion.FetionContext;
import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Message;
import net.solosky.maplefetion.net.TransferException;
import net.solosky.maplefetion.sipc.SipcOutMessage;

/**
 *
 * 第一版和在线好友聊天对话框
 *
 * @author solosky <solosky772@qq.com>
 */
public class LiveV1ChatDialog extends ChatDialog
{

	/**
     * @param mainBuddy
     * @param client
     */
    public LiveV1ChatDialog(Buddy mainBuddy, FetionContext client)
    {
	    super(mainBuddy, client);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.ChatDialog#isMutipartySupported()
     */
    @Override
    public boolean isMutipartySupported()
    {
	    return false;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.ChatDialog#sendChatMessage(java.lang.String, net.solosky.maplefetion.client.dialog.ActionListener)
     */
    @Override
    public void sendChatMessage(Message message, ActionListener listener)
    {
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#closeDialog()
     */
    @Override
    public void closeDialog()
    {
	    // TODO Auto-generated method stub
	    
    }

    /* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#openDialog()
     */
    @Override
    public void openDialog() throws TransferException, DialogException
    {
	    // TODO Auto-generated method stub
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#process(net.solosky.maplefetion.sipc.SipcOutMessage)
     */
    @Override
    public void process(SipcOutMessage out) throws TransferException
    {
    	this.context.getDialogFactory().getServerDialog().process(out);
    }
    
    
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.client.dialog.Dialog#getMessageFactory()
     */
    @Override
    public MessageFactory getMessageFactory()
    {
    	return this.context.getDialogFactory().getServerDialog().getMessageFactory();
    }


	
}
