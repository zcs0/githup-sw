package com.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * SqliteUtils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-10-21
 */
public class SqliteUtils {

	private static volatile SqliteUtils instance;

	private DbHelper dbHelper;
	private SQLiteDatabase db;

	private SqliteUtils(Context context, String DBName) {
		dbHelper = new DbHelper(context, DBName);
		db = dbHelper.getWritableDatabase();
	}

	private SqliteUtils(Context context,String DBName,String tableName,String[] columnName) {
		dbHelper = new DbHelper(context,DBName,tableName,columnName);
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 创建一个数据(如果不存在)
	 * 
	 * @param context
	 * @param DBName
	 *            数据库名
	 * @return
	 */
	public static SqliteUtils getInstance(Context context, String DBName) {
		if (instance == null) {
			synchronized (SqliteUtils.class) {
				if (instance == null) {
					instance = new SqliteUtils(context, DBName);
				}
			}
		}
		return instance;
	}
	/**
	 * 创建一个数据(如果不存在)
	 * @param context
	 * @param DBName  数据库名
	 * @param tableName 表名
	 * @param columnName 列表
	 * @return
	 */
	public static SqliteUtils getInstance(Context context,String DBName,String tableName,String[] columnName) {
		if (instance == null) {
			synchronized (SqliteUtils.class) {
				if (instance == null) {
					instance = new SqliteUtils(context,DBName,tableName,columnName);
				}
			}
		}
		return instance;
	}

	public SQLiteDatabase getDb() {
		return db;
	}
}
