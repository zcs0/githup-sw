/*
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.wheel.wheelView;


public interface WheelAdapter {
	/**
	 * 获取项目数
	 * @return the count of wheel items
	 */
	public int getItemsCount();
	
	/**
	 * 获取一个轮项目指标。
	 * 
	 * @param index the item index
	 * @return the wheel item text or null
	 */
	public <T> T getItem(int index);
	
	/**
	 * 获取最大项目长度。它是用来确定车轮的宽度。 
	 * 如果返回1将使用默认的车轮宽度。
	 * 
	 * @return the maximum item length or -1
	 */
	public int getMaximumLength();
	
	public <T> T getItemView(int index);
	
}
