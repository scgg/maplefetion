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
 * Package  : net.solosky.maplefetion.protocol.response
 * File     : DefaultSIPResponseHandler.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-24
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.protocol.response;

import net.solosky.maplefetion.protocol.ISIPResponseHandler;
import net.solosky.maplefetion.sip.SIPResponse;

/**
 *
 * 默认的SIP回复处理
 *
 * @author solosky <solosky772@qq.com> 
 */
public class DefaultSIPResponseHandler implements ISIPResponseHandler
{

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.protocol.ISIPResponseHandler#handle(net.solosky.maplefetion.sip.SIPResponse)
     */
    @Override
    public void handle(SIPResponse response) throws Exception
    {
    	//DO nothing..
    }

}
