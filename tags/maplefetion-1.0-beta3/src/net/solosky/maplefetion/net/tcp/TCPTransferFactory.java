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
 * Package  : net.solosky.maplefetion.net.tcp
 * File     : TCPTransferFactory.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-26
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.net.tcp;

import net.solosky.maplefetion.net.ITransfer;
import net.solosky.maplefetion.net.ITransferFactory;

/**
 * 
 * TCP连接工厂
 *
 * @author solosky <solosky772@qq.com> 
 */
public class TCPTransferFactory implements ITransferFactory
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransferFactory#createTransfer(java.lang.String, int)
     */
    @Override
    public ITransfer createTransfer(String host, int port) throws Exception
    {
	    return new TCPTransfer(host, port);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.net.ITransferFactory#closeFactory()
     */
    @Override
    public void closeFactory()
    {
	    //Do nothing...
    }

}
