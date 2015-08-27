package com.sms;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * @ClassName:     SmsObserver.java
 * @author         zcs
 * @Date           2015年4月9日 下午1:27:01 
 * @Description:   接收短信
 */
public class SmsObserver {
	Context cxt;
	/*private final String SMS_ACTION="sms_action";
	private final String SMS_PHONE="sms_phone";
	private final String SMS_MESSAGE="sms_message";
	private final String SMS_TIME="sms_time";*/
	private SmsBroadcastReceiver smsReceiver;
	public SmsObserver(Context cxt){
		this.cxt = cxt;
		ContentResolver cr = cxt.getContentResolver();
		Uri uri = Uri.parse("content://sms/");
		cr.registerContentObserver(uri, true, new MyObserver(new Handler()));
	}
	private class MyObserver extends ContentObserver{

		public MyObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			Cursor cursor = cxt.getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{"address","body","date"}, "type=?", new String[]{"1"}, null);
			//Cursor cursor = cxt.getContentResolver().query(Uri.parse("content://sms/"), new String[]{"address","body","date"}, "type=?", new String[]{"1"}, null);
			//String time = cursor.getString(2);
			if(smsReceiver!=null&&cursor!=null&&cursor.getCount()>0){
				cursor.moveToFirst();
				Log.d("SmsObserver","接收到短信"+cursor.getString(1));//1428629828181
				long date = System.currentTimeMillis();
				long date2 = cursor.getLong(2);
				if((date-date2)<(45*1000)){//如果是15秒内的信息
					smsReceiver.content(cursor.getString(0), cursor.getString(1), cursor.getString(2));
				}
			}
		}
	}
	public void addSmsListener(SmsBroadcastReceiver smsReceiver){
		this.smsReceiver = smsReceiver;
		
	}
	public interface SmsBroadcastReceiver{
		public void content(String phone,String message,String time);
	}

}
