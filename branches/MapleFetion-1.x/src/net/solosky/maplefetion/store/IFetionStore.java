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
 * Package  : net.solosky.maplefetion.store
 * File     : IFetionStore.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-27
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.store;

import java.util.ArrayList;
import java.util.Collection;

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.FetionCord;

/**
 *
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface IFetionStore
{

	/**
	 * 添加好友
	 * @param buddy
	 */
	public abstract void addBuddy(FetionBuddy buddy);

	/**
	 * 是否是好友
	 * @param sid
	 * @return
	 */
	public boolean hasBuddy(String uri);

	/**
	 * 返回飞信好友
	 * @param uri
	 * @return
	 */
	public FetionBuddy getBuddy(String uri);

	/**
	 * 删除好友
	 * @param uid
	 */
	public void removeBuddy(String uid);

	/**
	 * 返回全部好友列表
	 * @return 
	 */
	public Collection<FetionBuddy> getBuddyList();

	/**
	 * 返回指定组的好友
	 * @param cordId
	 * @return
	 */
	public Collection<FetionBuddy> getBuddyList(int cordId);

	/**
	 * 添加好友分组
	 */
	public void addCord(FetionCord cord);

	/**
	 * 返回所有分组列表
	 * @return
	 */
	public Collection<FetionCord> getCordList();

	/**
	 * 删除好友
     * @param uri
     */
    public void deleteBuddy(String uri);
    
    /**
     * 返回陌生人列表
     * @return
     */
    public Collection<FetionBuddy> getStrangerList();
    
    /**
     * 返回已发出好友请求但没有回复的列表
     * @return
     */
    public Collection<FetionBuddy> getUnconfirmedList();
    
    /**
     * 返回请求被拒绝的好友列表
     * @return
     */
    public Collection<FetionBuddy> getDeclinedList();
}
