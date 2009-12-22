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

import net.solosky.maplefetion.bean.FetionBuddy;
import net.solosky.maplefetion.bean.FetionCord;

/**
 * 
 * 保存飞信的数据
 * 如好友，消息历史等
 *
 * @author solosky <solosky772@qq.com> 
 */
public class SimpleFetionStore implements IFetionStore
{
	/**
	 * 好友列表
	 * 使用HASH便于查找
	 */
	private Hashtable<String, FetionBuddy> buddyList;
	
	/**
	 * 分组列表
	 */
	private ArrayList<FetionCord> cordList;

	
	/**
	 * 构造函数
	 */
	public SimpleFetionStore()
	{
		this.buddyList = new Hashtable<String, FetionBuddy>();
		this.cordList  = new ArrayList<FetionCord>();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#addBuddy(net.solosky.maplefetion.bean.FetionBuddy)
     */
	public void addBuddy(FetionBuddy buddy)
	{
		this.buddyList.put(buddy.getUri(), buddy);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#isBuddy(java.lang.String)
     */
	public boolean hasBuddy(String uri)
	{
		if(uri==null)	return false;
		return this.buddyList.containsKey(uri);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getBuddy(java.lang.String)
     */
	public FetionBuddy getBuddy(String uri)
	{
		if(uri==null)	return null;
		return this.buddyList.get(uri);
	}
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#removeBuddy(java.lang.String)
     */
	public void removeBuddy(String uri)
	{
		if(uri==null)	return;
		this.buddyList.remove(uri);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getBuddyList()
     */
	public Collection<FetionBuddy> getBuddyList()
	{
		return this.buddyList.values();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getBuddyList(java.lang.String)
     */
	public Collection<FetionBuddy> getBuddyList(int cordId)
	{
		ArrayList<FetionBuddy> list = new ArrayList<FetionBuddy>();
		Iterator<FetionBuddy> it = this.buddyList.values().iterator();
		FetionBuddy buddy = null;
		String  buddyCordId = null;
		while(it.hasNext()) {
			buddy = it.next();
			buddyCordId = buddy.getCordId();
			if(buddy.getRelationStatus()==FetionBuddy.RELATION_STATUS_AGREED) {
				if(cordId==-1) {	//参数为负数，意思是获取没有分组的好友
					if(buddyCordId==null || buddy.getCordId().length()==0) {
						list.add(buddy);
					}
				}else {		//获取指定分组的好友
					if(buddyCordId!=null && buddyCordId.indexOf(Integer.toString(cordId))!=-1) {
						list.add(buddy);
					}
				}
			}
		}
		return list;
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#addCord(net.solosky.maplefetion.bean.FetionCord)
     */
	public void addCord(FetionCord cord)
	{
		this.cordList.add(cord);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getCordList()
     */
	public Collection<FetionCord> getCordList()
	{
		return this.cordList;
	}

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#deleteBuddy(java.lang.String)
     */
    @Override
    public void deleteBuddy(String uri)
    {
    	if(uri==null)	return;
	    this.buddyList.remove(uri);  
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getDeclinedList()
     */
    @Override
    public Collection<FetionBuddy> getDeclinedList()
    {
    	return this.getRelationBuddylist(FetionBuddy.RELATION_STATUS_DECLINED);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getStrangerList()
     */
    @Override
    public Collection<FetionBuddy> getStrangerList()
    {
    	return this.getRelationBuddylist(FetionBuddy.RELATION_STATUS_STRANGER);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.store.IFetionStore#getUnconfirmedList()
     */
    @Override
    public Collection<FetionBuddy> getUnconfirmedList()
    {
    	return this.getRelationBuddylist(FetionBuddy.RELATION_STATUS_UNCONFIRMED);
    }
    
    /**
     * 返回指定关系的列表
     * @param relation
     * @return
     */
    private Collection<FetionBuddy> getRelationBuddylist(int relation)
    {
    	ArrayList<FetionBuddy> list = new ArrayList<FetionBuddy>();
 	   Iterator<FetionBuddy> it = this.buddyList.values().iterator();
 	   while(it.hasNext()) {
 		   FetionBuddy buddy = it.next();
 		   if(buddy.getRelationStatus()==relation)
 			   list.add(buddy);
 	   }
 	   return list;
    }
	
}
