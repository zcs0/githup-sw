package com.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
/**
 * v4包下FragmentActivity第二级
 * 使用FragmentActivity可以继承于此
 * @author ZCS
 *
 */
public abstract class BaseFragmentActivity extends FragmentActivity implements BaseMain, OnClickListener {
	private View view;
	private String TAG="IFragmentActivity";
	private Bundle savedInstanceState;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
		
	}
	public int createView(int layoutResID){
		view = View.inflate(this, layoutResID, null);
		createView(view);
		return layoutResID;
	}
	public View createView(View v){
		this.setContentView(v);
		initBundle(savedInstanceState);
		initView(v);
		return v;
	}
	@Override
	public void setOnClick(int layoutResID) {
		getView(layoutResID).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setOnClick(View view) {
		// TODO Auto-generated method stub
		
	}
	
	public <T extends View> T getView(int layoutId) {
		if (view != null) {
			return (T) findViewById(layoutId);
		} else {
			Log.e(TAG, "获得此控件失败（id无效），");
			return null;
		}
	}
	

}
