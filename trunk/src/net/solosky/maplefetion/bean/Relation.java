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
 * Created  : 2010-2-5
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;

/**
 *
 * 好友关系
 *
 * @author solosky <solosky772@qq.com>
 */
public class Relation
{
	public static final int RELATION_UNCONFIRMED = 000;
	public static final int RELATION_BUDDY = 001;
	public static final int RELATION_DECLINED = 002;
	public static final int RELATION_STRANGER = 003;
	public static final int RELATION_BANNED = 004;
	
	private int value;
	
	
	/**
	 * 构造函数，用户关系
	 * @param value
	 */
	public Relation(int value)
	{
		this.value = value;
	}


	/**
     * @return the value
     */
    public int getValue()
    {
    	return value;
    }


	/**
     * @param value the value to set
     */
    public void setValue(int value)
    {
    	this.value = value;
    }
	
	
}
