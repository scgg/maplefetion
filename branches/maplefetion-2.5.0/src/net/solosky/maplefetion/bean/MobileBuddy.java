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
 * Package  : net.solosky.maplefetion.bean
 * File     : MobileBuddy.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-5
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;

/**
 *
 * 手机好友，是指没有开通飞信的用户
 *
 * @author solosky <solosky772@qq.com>
 */
public class MobileBuddy extends Buddy
{

    @Override
    public String getDisplayName()
    {
    	if(getLocalName()!=null && getLocalName().length()>0)
    		return getLocalName();
    	if(getFetionId()>0)
    		return Integer.toString(getFetionId());
    	if(getMobile()!=0)
    		return Long.toString(getMobile());
    	return null;
    }

	/* (non-Javadoc)
	 * @see net.solosky.maplefetion.bean.Person#getDisplayPresence()
	 */
	@Override
	public String getDisplayPresence() {
		if(this.getRelation()!=Relation.BUDDY){
			return "离线";
		}else{
			return "短信在线";
		}
	}
    
    

}
