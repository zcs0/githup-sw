package com.bean;
/**
 * @ClassName:     CityBean.java
 * @author         zcs
 * @Date           2015年4月5日 下午2:06:19 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class CityBean {
	
	public int _id;
	public int adcode;
	public int city_code;
	public String name;
	public String center;
	public String level;
	public CityBean(int _id, int adcode, int city_code, String name,
			String center, String level) {
		this._id = _id;
		this.adcode = adcode;
		this.city_code = city_code;
		this.name = name;
		this.center = center;
		this.level = level;
	}
	
}
