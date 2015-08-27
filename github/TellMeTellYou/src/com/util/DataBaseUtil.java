package com.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tell.R;

/**
 * @ClassName: DataBaseUtil.java
 * @author zcs
 * @Date 2015年4月5日 上午11:47:13
 * @Description: TODO(用一句话描述该文件做什么)
 */
public class DataBaseUtil {
	
	
	// 从Raw读取
	public static SQLiteDatabase readResources(Context context, String FileName) {
		SQLiteDatabase db = null;
		InputStream is = context.getResources().openRawResource(R.raw.city);
		String path = context.getApplicationContext().getFilesDir()
				.getAbsolutePath();
		try {
			OutputStream os;
			File file = new File(path, FileName);
			if (!file.exists()) {// 如果文件不存在
				file.createNewFile();
				os = new FileOutputStream(file);
				byte b[] = new byte[1024 * 2];
				int len;
				while ((len = is.read(b)) != -1) {
					os.write(b, 0, len);
				}
				os.close();
				is.close();
			}
			db = SQLiteDatabase.openOrCreateDatabase(file, null);
		} catch (Exception e) {
			Log.e("Test", "读取数据库失败");
			e.printStackTrace();
		}

		return db;
	}

	// 从Assets中读取
	public static SQLiteDatabase readAssets(Context context,String FileName) {
		SQLiteDatabase db = null;
		String path = context.getApplicationContext().getFilesDir()
				.getAbsolutePath();
		try {
			InputStream is = context.getAssets().open(FileName);
			OutputStream os;
			File file = new File(path, "city.db");
			if (!file.exists()) {// 如果文件不存在
				file.createNewFile();
				os = new FileOutputStream(file);
				byte b[] = new byte[1024 * 2];
				int len;
				while ((len = is.read(b)) != -1) {
					os.write(b, 0, len);
				}
				os.close();
				is.close();
			}
			db = SQLiteDatabase.openOrCreateDatabase(file, null);
		} catch (Exception e) {
			Log.e("Test", "读取数据库失败");
			e.printStackTrace();
		}

		return db;
	}

}
