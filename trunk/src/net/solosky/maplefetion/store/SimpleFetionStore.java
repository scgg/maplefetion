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
 * Package  : net.solosky.maplefetion
 * File     : FetionStore.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-20
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import net.solosky.maplefetion.bean.Buddy;
import net.solosky.maplefetion.bean.Cord;
import net.solosky.maplefetion.bean.Group;
import net.solosky.maplefetion.bean.Member;
import net.solosky.maplefetion.bean.StoreVersion;
import net.solosky.maplefetion.bean.User;

/**
 * 
 * 保存飞信的数据
 * 如好友，消息历史等
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SimpleFetionStore implements FetionStore
{
	/**
	 * 好友列表
	 * 使用HASH便于查找
	 */
	private Hashtable<String, Buddy> buddyList;
	
	/**
	 * 分组列表
	 */
	private ArrayList<Cord> cordList;
	
	/**
	 * 群列表
	 */
	private Hashtable<String, Group> groupList;
	
	/**
	 * 群成员列表
	 */
	private Hashtable<String, Hashtable<String, Member>> groupMemberList;
	
	/**
	 * 存储版本
	 */
	private StoreVersion storeVersion;

	
	/**
	 * 构造函数
	 */
	public SimpleFetionStore()
	{
		this.buddyList = new Hashtable<String, Buddy>();
		this.cordList  = new ArrayList<Cord>();
		this.groupList = new Hashtable<String, Group>();
		this.groupMemberList = new Hashtable<String, Hashtable<String,Member>>();
		this.storeVersion = new StoreVersion();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#addBuddy(net.solosky.maplefetion.bean.FetionBuddy)
     */
	public void addBuddy(Buddy buddy)
	{
		this.buddyList.put(buddy.getUri(), buddy);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getBuddyByUri(java.lang.String)
     */
    @Override
    public Buddy getBuddyByUri(String uri)
    {
    	if(uri==null)	return null;
		return this.buddyList.get(uri);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getBuddyByUserId(int)
     */
    @Override
    public Buddy getBuddyByUserId(int userId)
    {
    	Iterator<Buddy> it = this.buddyList.values().iterator();
    	while(it.hasNext()) {
    		Buddy buddy = it.next();
    		if(buddy.getUserId()==userId)
    			return buddy;
    	}
    	return null;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#removeBuddyByUri(java.lang.String)
     */
    @Override
    public void deleteBuddy(Buddy buddy)
    {
		this.buddyList.remove(buddy.getUri());
    }
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getBuddyList()
     */
	public Collection<Buddy> getBuddyList()
	{
		return this.buddyList.values();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getBuddyList(java.lang.String)
     */
	public Collection<Buddy> getBuddyListByCord(Cord cord)
	{
		ArrayList<Buddy> list = new ArrayList<Buddy>();
		Iterator<Buddy> it = this.buddyList.values().iterator();
		Buddy buddy = null;
		String  buddyCordId = null;
		while(it.hasNext()) {
			buddy = it.next();
			buddyCordId = buddy.getCordId();
			if(buddyCordId!=null && buddyCordId.indexOf(Integer.toString(cord.getId()))!=-1) {
				list.add(buddy);
			}
		}
		return list;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getBuddyListWithoutCord()
     */
    @Override
    public Collection<Buddy> getBuddyListWithoutCord()
    {
    	ArrayList<Buddy> list = new ArrayList<Buddy>();
		Iterator<Buddy> it = this.buddyList.values().iterator();
		Buddy buddy = null;
		String  buddyCordId = null;
		while(it.hasNext()) {
			buddy = it.next();
			buddyCordId = buddy.getCordId();
			if(buddyCordId==null || buddyCordId.length()==0) {
				list.add(buddy);
			}
		}
		return list;
    }
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#addCord(net.solosky.maplefetion.bean.FetionCord)
     */
	public void addCord(Cord cord)
	{
		this.cordList.add(cord);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getCord(int)
     */
    @Override
    public Cord getCord(int cordId)
    {
	   Iterator<Cord> it = this.cordList.iterator();
	   while(it.hasNext()) {
		   Cord cord = it.next();
		   if(cord.getId()==cordId)
			   return cord;
	   }
	   return null;
    }
    
    public void deleteCord(Cord cord)
    {
    	this.cordList.remove(cord);
    }
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getCordList()
     */
	public Collection<Cord> getCordList()
	{
		return this.cordList;
	}

    /**
     * 返回指定关系的列表
     * @param relation
     * @return
     */
	@Override
    public Collection<Buddy> getBuddyListByRelation(int relation)
    {
    	ArrayList<Buddy> list = new ArrayList<Buddy>();
 	   Iterator<Buddy> it = this.buddyList.values().iterator();
 	   while(it.hasNext()) {
 		   Buddy buddy = it.next();
 		   if(buddy.getRelation().getValue()==relation)
 			   list.add(buddy);
 	   }
 	   return list;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#flush()
     */
    @Override
    public void flush()
    {
	    // TODO Auto-generated method stub
	    
    }


	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#init(net.solosky.maplefetion.bean.FetionUser)
     */
    @Override
    public void init(User user)
    {
	    // TODO Auto-generated method stub
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getStoreVersion()
     */
    @Override
    public StoreVersion getStoreVersion()
    {
    	return this.storeVersion;
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#addGroup(net.solosky.maplefetion.bean.FetionGroup)
     */
    @Override
    public void addGroup(Group group)
    {
    	this.groupList.put(group.getUri(), group);
    	this.groupMemberList.put(group.getUri(), new Hashtable<String,Member>());
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getGroup(java.lang.String)
     */
    @Override
    public Group getGroup(String uri)
    {
	    return this.groupList.get(uri);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getGroupList()
     */
    @Override
    public Collection<Group> getGroupList()
    {
	    return this.groupList.values();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#removeGroup(java.lang.String)
     */
    @Override
    public void deleteGroup(Group group)
    {
	    this.groupList.remove(group.getUri());
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#clear()
     */
    @Override
    public void clear()
    {
    	this.buddyList.clear();
    	this.cordList.clear();
    	this.groupList.clear();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#clearBuddyList()
     */
    @Override
    public void clearBuddyList()
    {
	    this.buddyList.clear();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#clearCordList()
     */
    @Override
    public void clearCordList()
    {
    	this.cordList.clear();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#clearGroupList()
     */
    @Override
    public void clearGroupList()
    {
    	this.groupList.clear();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#addGroupMember(net.solosky.maplefetion.bean.Group, net.solosky.maplefetion.bean.Member)
     */
    @Override
    public void addGroupMember(Group group, Member member)
    {
    	Hashtable<String,Member> table = this.groupMemberList.get(group.getUri());
    	if(table!=null) {
    		table.put(member.getUri(), member);
    	}
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getGroupMemberList(net.solosky.maplefetion.bean.Group)
     */
    @Override
    public Collection<Member> getGroupMemberList(Group group)
    {
    	return this.groupMemberList.get(group.getUri()).values();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#removeGroupMember(net.solosky.maplefetion.bean.Group, net.solosky.maplefetion.bean.Member)
     */
    @Override
    public void deleteGroupMember(Group group, Member member)
    {
    	Hashtable<String,Member> table = this.groupMemberList.get(group.getUri());
    	if(table!=null) {
    		table.remove(member.getUri());
    	}
	    
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.FetionStore#getGroupMember(net.solosky.maplefetion.bean.Group, java.lang.String)
     */
    @Override
    public Member getGroupMember(Group group, String uri)
    {
    	Hashtable<String,Member> table = this.groupMemberList.get(group.getUri());
    	if(table!=null) {
    		return table.get(uri);
    	}
    	return null;
    }
}
