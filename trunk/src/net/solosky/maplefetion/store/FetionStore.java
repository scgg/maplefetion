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

import java.util.Collection;

import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.bean.User;
import net.solosky.maplefetion.bean.StoreVersion;

/**
 *
 *
 * @author solosky <solosky772@qq.com> 
 */
public interface FetionStore
{

	/**
	 * 添加好友
	 * @param buddy
	 */
	public abstract void addBuddy(Buddy buddy);

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
	public Buddy getBuddy(String uri);

	/**
	 * 删除好友
	 * @param uid
	 */
	public void removeBuddy(String uid);

	/**
	 * 返回全部好友列表
	 * @return 
	 */
	public Collection<Buddy> getBuddyList();
	
	/**
	 * 清空好友列表
	 */
	public void clearBuddyList();

	/**
	 * 返回指定组的好友
	 * @param cordId
	 * @return
	 */
	public Collection<Buddy> getBuddyListByCord(Cord cord);
	
	
	/**
	 * 返回没有分组的好友列表
	 * @return
	 */
	public Collection<Buddy> getBuddyListWithoutCord();

	/**
	 * 添加好友分组
	 */
	public void addCord(Cord cord);
	
	/**
	 * 返回一个分组
	 */
	public Cord getCord(int cordId);

	/**
	 * 返回所有分组列表
	 * @return
	 */
	public Collection<Cord> getCordList();
	
	/**
	 * 清除所有的组列表
	 */
	public void clearCordList();
	
	/**
	 * 根据关系返回列表
	 * @param relation		好友关系 定义在Relaction中
	 */
	public Collection<Buddy> getBuddyListByRelation(int relation);
    
    /**
     * 存储数据版本
     * @return
     */
    public StoreVersion getStoreVersion();
    /**
     * 初始化
     * @param user
     */
    public void init(User user);
    
    /**
     * 把信息强制刷新
     */
    public void flush();
    
    /**
     * 清除所有的信息
     */
    public void clear();
    
    
    /**
     * 返回所有的群列表
     * @return
     */
    public Collection<Group> getGroupList();
    
    
    /**
     * 清除所有群列表
     */
    public void clearGroupList();
    
    /**
     * 返回群对象
     * @param uri		群的地址
     * @return
     */
    public Group getGroup(String uri);
    
    /**
     * 删除群对象
     * @param uri		群地址
     */
    public void removeGroup(String uri);
    
    /**
     * 添加群对象
     * @param group		群对象
     */
    public void addGroup(Group group);
    
    /**
     * 群内添加一个成员
     * @param group
     * @param member
     */
    public void addGroupMember(Group group, Member member);
    
    /**
     * 群内删除一个成员
     * @param group
     * @param member
     */
    public void removeGroupMember(Group group, Member member);
    
    /**
     * 返回群的成员列表
     * @param group
     * @return
     */
    public Collection<Member> getGroupMemberList(Group group);
    
    
    /**
     * 返回群成员
     * @param group		群对象
     * @param uri		成员的URI
     * @return
     */
    public Member getGroupMember(Group group, String uri);
    
}
