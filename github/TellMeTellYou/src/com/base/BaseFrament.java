package com.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * v4下的Frament第二级别
 * 使用frament时可以继承于此
 * @author ZCS
 *
 */
public abstract class BaseFrament extends Fragment {
	private View view;
	private String TAG="BaseFrament";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), createView(), null);
		initBundle(savedInstanceState);
		initView(view);
		return view;
	}
	public abstract void initBundle(Bundle savedInstanceState);
	public abstract void initView(View view);
	public abstract int createView();
	public <T extends View> T getView(int layoutId) {
		if (view != null) {
			return (T) view.findViewById(layoutId);
		} else {
			Log.e(TAG, "获得此控件失败（id无效），");
			return null;
		}
	}
	

}
