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
 * Package  : net.solosky.maplefetion.executor
 * File     : SingleExecutor.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-1-14
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * 单线程执行器
 * 因为有些操作要实现异步操作就需要使用一个单独的线程操作，
 * 为了提高效率，这里可以使用一个单独的线程执行器完成操作
 *
 * @author solosky <solosky772@qq.com>
 */
public class SingleExecutor implements FetionExecutor
{	
	/**
	 * 执行服务 
	 */
	private ExecutorService executorService;
	
	/**
	 * 构造函数
	 */
	public SingleExecutor()
	{
		executorService = Executors.newSingleThreadExecutor();
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.util.FetionExecutor#submit(java.lang.Runnable)
     */
	public void submit(Runnable task)
	{
		this.executorService.submit(task);
	}
	
	/* (non-Javadoc)
     * @see net.solosky.maplefetion.util.FetionExecutor#close()
     */
	public void close()
	{
		this.executorService.shutdown();
	}
}