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
 * File     : Relation.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-5-15
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;

import net.solosky.maplefetion.util.ParseException;

/**
 *
 * 好友关系
 *
 * @author solosky <solosky772@qq.com>
 */
public enum Relation {
	UNCONFIRMED(0),
	BUDDY(1),
	DECLINED(2),
	STRANGER(3),
	BANNED(4);
	
	
	private int value;
	
	Relation(int value)
	{
		this.value = value;
	}
	
	public static Relation parseRelation(String s) throws ParseException
	{
		int v = 0;
		try {
	        v = Integer.parseInt(s);
        } catch (NumberFormatException e) {
        	throw new ParseException("Cannot parse relation value:"+s+", expected 0,1,2,3,4.");
        }
		switch(v) {
    		case 0: 	return UNCONFIRMED;
    		case 1:		return BUDDY;
    		case 2: 	return DECLINED;
    		case 3:		return STRANGER;
    		case 4:		return BANNED;
    		default: throw new ParseException("Invalid relation value:"+s+", expected 0,1,2,3,4.");
		}
	}
}
