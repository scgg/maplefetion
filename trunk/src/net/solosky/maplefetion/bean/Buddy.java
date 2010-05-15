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
 * Package  : net.solosky.maplefetion.bean
 * File     : FetionBuddy.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;


/**
 *  飞信好友
 *
 * @author solosky <solosky772@qq.com> 
 */
public abstract class Buddy extends Person
{
	/**
	 * 和用户的关系， 只读属性
	 */
	protected Relation relation;
	
	/**
	 * 所属分组编号
	 */
	protected String cordId;
	
	/**
     * 用户设置的好友备注 
     */
	protected String localName;

	
	/**
	 * 默认构造函数
	 */
    public Buddy()
    {
    	relation =  Relation.BUDDY;
    }


	/**
     * @return the relation
     */
    public Relation getRelation()
    {
    	return relation;
    }

	/**
     * @return the cordId
     */
    public String getCordId()
    {
    	return cordId;
    }


	/**
     * @return the localName
     */
    public String getLocalName()
    {
    	return localName;
    }
    

	/**
     * @param cordId the cordId to set
     */
    public void setCordId(String cordId)
    {
    	this.cordId = cordId;
    }


	/**
     * @param localName the localName to set
     */
    public void setLocalName(String localName)
    {
    	this.localName = localName;
    }


	/**
     * 返回可以显示的名字
     */
    
    public abstract String getDisplayName();
}
