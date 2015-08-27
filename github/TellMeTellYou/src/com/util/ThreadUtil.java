package com.util;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
/**
 * 
 * @author admin
 *
 * @param <K> 在传递给子线程要执行的参数类型
 * @param <V> 在子线程执行完后返回的类型
 */
public abstract class ThreadUtil<K,V> {
	K[] k;
	V v;
	Integer time;
	int what;

	@SuppressLint("HandlerLeak")
	public Handler hanlder= new Handler(){
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			onPostExecute(msg.what,(V)msg.obj);
		};
	};
	
	/**
	 * 
	 * @param what 标识
	 * @param time 线程睡眠时间
	 * @param k 在子线程要执行的参数类型
	 */
	public ThreadUtil(int what,Integer time,K... k){
		this.k = k;
		//this.v = v;
		this.what = what;
		this.time = time;
	}
	
	/**
	 * 线程执行完后调用的方法
	 * @param what 标识
	 * @param v 子线程返回的参数
	 */
	public abstract void onPostExecute(int what,V... v);

	/**
	 * 线程中执行的方法
	 * @param k2 要用到的参数
	 * @return
	 */
	public abstract V doInBackground(K... k);
	
	public ThreadUtil startRun(){
		new Thread(){

			@Override
			public void run() {
				v = doInBackground(k);
				Message msg = new Message();
				msg.what = what;
				msg.obj = v;
				hanlder.sendMessage(msg);
			}
			
		}.start();
		return this;
	}
	
	

}
