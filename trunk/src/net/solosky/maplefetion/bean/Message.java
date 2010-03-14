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
 * File     : Message.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2010-2-12
 * License  : Apache License 2.0 
 */
package net.solosky.maplefetion.bean;

import org.jdom.Element;

import net.solosky.maplefetion.util.ParseException;
import net.solosky.maplefetion.util.XMLHelper;

/**
 *
 * 聊天消息
 * 
 * 聊天的内容是很丰富的，有字体，颜色，大小，图片等。。
 * 这里暂时只对字体，颜色，大小做支持，其他的逐步添加
 *
 * @author solosky <solosky772@qq.com>
 */
public class Message
{
	/**
	 * 字体
	 */
	private String font;
	
	/**
	 * 颜色
	 */
	private int color;
	
	/**
	 * 大小
	 */
	private double size;
	
	/**
	 * 消息正文
	 */
	private String text;
	
	/**
	 * 默认构造函数
	 */
	public Message()
	{
	}
	
	/**
	 * 解析带有消息格式的字符串为消息对象
	 * 如<Font Face='宋体' Color='-16777216' Size='10.5'>sadsads</Font>
	 * @throws ParseException 
	 */
	public static Message parse(String msg) throws ParseException
	{
		Element root = XMLHelper.build(msg);
		Message m = new Message();
		if(root!=null) {
			m.setFont( root.getAttributeValue("Face"));
			m.setColor( Integer.parseInt(root.getAttributeValue("Color")));
			m.setSize( Float.parseFloat(root.getAttributeValue("Size")));
			m.setText( root.getText());
		}
		
		return m;
	}

	
	/**
	 * 包装普通的文本为消息对象
	 * @param plain
	 * @return
	 */
	public static Message wrap(String plain)
	{
		Message m = new Message();
		m.setColor(-16777216);	//默认为黑色，具体是怎么定义的还得研究。。
		m.setFont("宋体");		//默认为宋体
		m.setSize(10.5);		//默认大小为10.5
		m.setText(plain);		//消息文本
		
		return m;
	}
	/**
     * @return the font
     */
    public String getFont()
    {
    	return font;
    }

	/**
     * @param font the font to set
     */
    public void setFont(String font)
    {
    	this.font = font;
    }

	/**
     * @return the color
     */
    public int getColor()
    {
    	return color;
    }

	/**
     * @param color the color to set
     */
    public void setColor(int color)
    {
    	this.color = color;
    }

	/**
     * @return the size
     */
    public double getSize()
    {
    	return size;
    }

	/**
     * @param size the size to set
     */
    public void setSize(double size)
    {
    	this.size = size;
    }

	/**
     * @return the text
     */
    public String getText()
    {
    	return text;
    }

	/**
     * @param text the text to set
     */
    public void setText(String text)
    {
    	this.text = text;
    }
    
    /**
     * ToString
     */
    public String toString()
    {
    	return "<Font Face='"+font+"' Color='"+Integer.toString(color)+"' Size='"+Double.toString(size)+"'>"+text+"</Font>";
    }
}
