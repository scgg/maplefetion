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
 * Package  : net.solosky.maplefetion.util
 * File     : ThreadTimer.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-5-10
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 *
 * 线程定时器，
 * 这个计时器委托给java.util.Timer，因为使用了一个线程来完成定时
 *
 * @author solosky <solosky772@qq.com>
 */
public class ThreadTimer implements FetionTimer
{
	/**
	 * 任务列表
	 */
	private HashMap<String , TimerTask> taskTable;
	/**
	 * 定时器
	 */
	private Timer timer;
	
	/**
	 * LOGGER
	 */
	private static Logger logger = Logger.getLogger(ThreadTimer.class); 
	
	/**
	 * 构造函数
	 */
	public ThreadTimer()
	{
		this.taskTable = new HashMap<String, TimerTask>();
	}
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.util.FetionTimer#cancelTask(java.lang.String)
     */
    @Override
    public void cancelTask(String name)
    {
    	TimerTask task = this.taskTable.get(name);
    	if(task!=null) {
    		task.cancel();
    		taskTable.remove(name);
    		timer.purge();
    		logger.debug("Canceled timer task:"+name);
    	}else {
    		throw new IllegalArgumentException("Cannot find timer task named "+name);
    	}
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.util.FetionTimer#scheduleTask(java.lang.String, java.util.TimerTask, long, long)
     */
    @Override
    public void scheduleTask(String name, TimerTask task, long delay,
            long period)
    {
    	this.timer.schedule(task, delay, period);
    	this.taskTable.put(name, task);
    	logger.debug("Scheduled timer task:"+name);
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.util.FetionTimer#startTimer()
     */
    @Override
    public void startTimer()
    {
    	this.timer = new Timer();
    }

	/* (non-Javadoc)
     * @see net.solosky.maplefetion.util.FetionTimer#stopTimer()
     */
    @Override
    public void stopTimer()
    {
    	this.timer.cancel();
    	this.taskTable.clear();
    }
}
