package com.util;

import com.img.util.constant.DbConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/**
 * db helper
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-10-21
 */
public class DbHelper extends SQLiteOpenHelper {
	String[] columnName;
	String tableName;
    public DbHelper(Context context,String DBName) {
        super(context, DbConstants.DB_NAME, null, DbConstants.DB_VERSION);
    }
    
    public DbHelper(Context context,String DBName,String tableName,String[] columnName) {
        super(context, DbConstants.DB_NAME, null, DbConstants.DB_VERSION);
        this.columnName = columnName;
        this.tableName = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
        	if(!TextUtils.isEmpty(tableName)&&columnName!=null&&columnName.length>0){//自定义db
        		StringBuffer sb = new StringBuffer();
        		sb.append("CREATE TABLE ").append(tableName).append("(").append("_id integer primary key autoincrement,");
        		//"adcode", "city_code","name", "center", "level" 
        		
        		sb.append("adcode integer,city_code integer,name,center,level);");
        		/*for (int i = 0; i < columnName.length; i++) {
        			//db.execSQL(columnName[i]);
        			sb.append(columnName[i]);
        			if(i==columnName.length-1){
        				sb.append(");");
        			}else{
        				sb.append(",");
        			}
				}*/
        		db.execSQL(sb.toString());
        	}else{//图片文件缓存
	            db.execSQL(DbConstants.CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.toString());
	            db.execSQL(DbConstants.CREATE_IMAGE_SDCARD_CACHE_TABLE_INDEX_SQL.toString());
	            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_SQL.toString());
	            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_INDEX_SQL.toString());
	            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_UNIQUE_INDEX.toString());
        	}
        	db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
