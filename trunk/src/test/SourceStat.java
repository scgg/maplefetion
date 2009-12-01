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
 * Package  : test
 * File     : SourceStat.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2009-11-23
 * License  : Apache License 2.0 
 */
package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <pre>
 *
 * @author solosky <solosky772@qq.com> 
 * </pre>
 *  <code>
 * ChangeLog:
 *   2009-11-23	SourceStat.java created. 
 *  </code>
 */
public class SourceStat
{

	private int lineCount;
	private int dirCount;
	private int fileCount;
	private String dir;
	
	public SourceStat()
	{
		this.lineCount = 0;
		this.dirCount = 0;
		this.fileCount = 0;
		this.dir = "E:\\App\\Java\\MapleFetion\\src";
	}
	
	public void stat(File dir) throws IOException
	{
		if(dir.isDirectory()) {
			dirCount++;
			File[] files = dir.listFiles();
			for(int i=0; i<files.length; i++)
				stat(files[i]);
		}else {
			fileCount++;
			boolean iscode = false;
			File tmpFile = new File(dir.getAbsolutePath()+".tmp");
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
			BufferedReader reader = new BufferedReader(new FileReader(dir));
			String line = null;
			while((line=reader.readLine())!=null) {
				if(line.indexOf("<pre>")!=-1) {
					continue;
				}
				if(line.indexOf("</pre>")!=-1) {
					writer.append(" * @author solosky <solosky772@qq.com> \r\n");
					continue;
				}
				if(line.indexOf("<code>")!=-1) {
					iscode = true;
					continue;
				}
				if(line.indexOf("</code>")!=-1) {
					iscode =false;
					continue;
				}
				if(!iscode) {
					writer.append(line);
					writer.append("\r\n");
				}
				lineCount++;
			}
			writer.close();
			reader.close();
			dir.delete();
			tmpFile.renameTo(dir.getAbsoluteFile());
		}
	}
	
	/**
     * @return the lineCount
     */
    public int getLineCount()
    {
    	return lineCount;
    }

	/**
     * @return the dirCount
     */
    public int getDirCount()
    {
    	return dirCount;
    }

	/**
     * @return the fileCount
     */
    public int getFileCount()
    {
    	return fileCount;
    }

	public static void main(String[] args) throws IOException
	{
		String dir = "E:\\App\\Java\\MapleFetion\\src";
		SourceStat s = new SourceStat();
		s.stat(new File(dir));
		System.out.println("DIRS:"+s.getDirCount());
		System.out.println("FILES:"+s.getFileCount());
		System.out.println("LINES:"+s.getLineCount());
		
		/*
		 * DIRS:15
            FILES:71
            LINES:7971
		 * 
		 */
		
	}
}
