package com.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
/**
 * 
 * @ClassName:     BaseActivity.java
 * @author         zcs
 * @Date           2015年3月31日 下午8:53:17 
 * @Description:   所有Activity的父类(createView方法)
 */
public abstract class BaseActivity extends Activity implements BaseMain, OnClickListener {
	private View thisView;
	private String TAG = "BaseActivity";
	protected Bundle savedInstanceState;
	protected View otherV;
	
	// 所有子Activity
	protected static List<? super Activity> listActivity = new ArrayList<Activity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntent();
		this.savedInstanceState = savedInstanceState;
	}
	
	public int createView(int layoutResID){
		thisView = View.inflate(this, layoutResID, null);
		createView(thisView);
		return layoutResID;
	}
	public View createView(View v){
		this.setContentView(v);
		listActivity.add(this);
		initBundle(savedInstanceState);
		initView(thisView);
		return v;
	}
	@Override
	public void setOnClick(View view) {
		view.setOnClickListener(this);
		
	}
	@Override
	public void setOnClick(int layoutResID) {
		getView(layoutResID).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		
	}
	/**
	 * 根据一个布局查找一个控件
	 * @param layoutResID
	 * @param viewId
	 * @return 返回这查找到的控件
	 */
	@SuppressWarnings("unchecked")
	protected <T extends View> T getViewById(int layoutResID,int viewId){
		if(otherV==null){
			otherV = View.inflate(this, layoutResID, null);
		}
		return (T) otherV.findViewById(viewId);
	}
	/**
	 * 根据一个View查找一个控件
	 * @param layoutResID
	 * @param viewId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends View> T getViewByView(View view,int viewId){
		return (T) view.findViewById(viewId);
	}
	/**
	 * 得到控件
	 * 
	 * @param layoutId
	 *            控件id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int layoutId) {
		if (thisView != null) {
			return (T) findViewById(layoutId);
		} else {
			Log.e(TAG, "获得此控件失败（id无效），");
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		listActivity.remove(this);// 移除当前的子Activity
		// System.out.println("剩余"+listActivity.size()+"个");
	}

	@Override
	protected void onResume() {
		super.onResume();

		// System.out.println("有"+listActivity.size()+"个");
	}

	/**
	 * 跳转到指定的Activity
	 * @param cls
	 */
	protected void toActivity(Class<? extends Activity> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}
	/**
	 * 跳转到指定的Activity
	 * @param <T>
	 * @param cls
	 */
	protected void toActivityForResult(Class<? extends Activity> cls,Intent intent,int requestCode) {
		//intent = new Intent(this, cls);
		startActivityForResult(intent, requestCode);
	}
	

}
