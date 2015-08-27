package com.base;

import android.os.Bundle;
import android.view.View;

import com.img.util.service.impl.ImageSDCardCache;
import com.util.CacheManager;
/**
 * 所有Activity需要实现类(最顶层)
 * @author ZCS
 *
 */
public interface BaseMain {
	public static final ImageSDCardCache IMAGE_SD_CACHE = CacheManager.getImageSDCardCache();
	/**
	 * 创建当前的 View 需要返回当前R.layout.
	 */
	//public int createView();
	
	public int createView(int layoutResID);
	
	public View createView(View v);
	/**
	 * bundle 可以从上个activity中获得值
	 * 
	 * @param bundle
	 */
	public void initBundle(Bundle bundle);
	/**
	 * 初始化当前的控件（当前界面下的view）
	 * 
	 * @param v
	 */
	public void initView(View v);
	/**
	 * 得到控件
	 * 
	 * @param layoutId
	 *            控件id
	 * @return 根据id返回指定的类型
	 */
	public <T extends View> T getView(int layoutId);
	/**
	 * 设置点击事件
	 * @param view
	 */
	public void setOnClick(View view);
	/**
	 * 设置点击事件
	 * @param layoutId
	 */
	public void setOnClick(int layoutId);

}
