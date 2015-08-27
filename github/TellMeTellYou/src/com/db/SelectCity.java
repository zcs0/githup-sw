package com.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bean.CityBean;
import com.util.DataBaseUtil;

/**
 * @ClassName: SelectCity.java
 * @author zcs
 * @Date 2015年4月5日 下午3:36:25
 * @Description: TODO(用一句话描述该文件做什么)
 */
public class SelectCity {
	Context context;

	public SelectCity(Context context) {
		this.context = context;
	}

	List<CityBean> list = new ArrayList<CityBean>();
	private SQLiteDatabase readAssets;
	private Cursor query;
	private String TAG="SelectCity";

	/**
	 * 获得子集
	 * 
	 * @param adcode
	 * @return
	 */
	public List<CityBean> getNode(CityBean bean) {
		//Log.d("node", "adcode "+adcode +"  level"+level);
		list.clear();
		readAssets = DataBaseUtil.readResources(context, "city.db");
		if(bean==null){//查询第一级
			query = readAssets.query("city", null, "level=?",
					new String[] { "province" }, "name,center", null, "_id");
		}else{
			if(bean.level.equals("province")){//查询第二级
				query = readAssets.query(
						"city",
						null,
						"adcode>=? and adcode<? and level = ?",
						new String[] { String.valueOf(bean.adcode),
								String.valueOf(bean.adcode + 10000), "city" }, "name,center",
						null, "_id");
			}else{//第三级
				query = readAssets.query("city", null, "city_code=? and level=?",
						new String[] { String.valueOf(bean.city_code) ,"district"}, "name,center", null, "_id");
			}
		} 
		while (query != null && query.moveToNext()) {//SELECT * FROM city WHERE adcode>=? and adcode<=? and level = ? GROUP BY name ORDER BY _id
			int _id = query.getInt(query.getColumnIndex("_id"));
			int code = query.getInt(query.getColumnIndex("adcode"));
			int city_code = query.getInt(query.getColumnIndex("city_code"));
			String name = query.getString(query.getColumnIndex("name"));
			String center = query.getString(query.getColumnIndex("center"));
			String le = query.getString(query.getColumnIndex("level"));
			list.add(new CityBean(_id, code, city_code, name, center, le));
		}
		return list;
	}
	public void closeDataBase(){
		Log.e(TAG, "关闭数据库");
		if(query!=null){
			query.close();
		}
		if(readAssets!=null){
			readAssets.close();
		}
	}
	
	public CityBean getCityBeanByIndex(int index){
		return list==null||list.size()<index?null:list.get(index);
	}
}
