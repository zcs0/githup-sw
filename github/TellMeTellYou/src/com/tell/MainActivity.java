package com.tell;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.base.BaseActivity;
import com.bean.CityBean;
import com.dialog.SelectCityDialog;
import com.map.AMapUtil;
import com.map.ToastUtil;
import com.sms.SmsObserver;

public class MainActivity extends BaseActivity implements LocationSource,
		AMapLocationListener, OnMapClickListener, OnMapLongClickListener,
		OnCameraChangeListener, OnMapTouchListener, OnGeocodeSearchListener {
	private static final String TAG = "MainActivity";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private AMap aMap;
	private MapView mapView;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private LatLonPoint latLonPoint = new LatLonPoint(39.90865, 116.39751);
	private GeocodeSearch geocoderSearch;
	private int compass;
	// private RadioGroup mGPSModeGroup;
	private Dialog dialog;
	private Marker geoMarker;
	private Marker regeoMarker;// 显示位置
	private SelectCityDialog dilog;
	private TextView phoneTv;
	private LatLng selectLatLng;// 保存选中的位置
	private JSONObject json;
	private boolean isLon = false;//是否在Marker显示地理位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createView(R.layout.activity_main);
	}

	@Override
	public void initBundle(Bundle bundle) {

	}

	@Override
	public void initView(View v) {
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写id
		getView(R.id.compassTv).setOnClickListener(this);// 指南针
		init();
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);

		SmsObserver observer = new SmsObserver(this);
		// 得到接收和短信
		observer.addSmsListener(new SmsObserver.SmsBroadcastReceiver() {

			@Override
			public void content(String phone, String message, String time) {
				if (TextUtils.isEmpty(message) || !message.contains(LATITUDE)
						|| !message.contains(LONGITUDE)) {
					return;
				}
				try {
					if(!message.contains("{")){
						message = "{"+message+"}";
					}
					Log.d(TAG, "接收短信的位置：" + message);
					json = new JSONObject(message);
					latLonPoint = new LatLonPoint(json.getDouble(LATITUDE),
							json.getDouble(LONGITUDE));
					showSmsLocationDialog(latLonPoint);
					// getAddress(latLonPoint);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.compassTv:// 指南针
			compass += compass >= 3 ? -2 : 1;
			setCompass(compass);
			break;
		case R.id.confirmTv:// 发短信
			String phone = phoneTv.getText().toString();
			sendSMSMessage(phone);
			dialog.dismiss();
			break;
		case R.id.cancelTv:// 取消
			dialog.dismiss();
			break;
		case R.id.smsLocationTv://加载信息位置
			regeoMarker.setVisible(true);
			getAddress(latLonPoint);
			dialog.dismiss();
			break;
		case R.id.cancelSmsTv:
			dialog.dismiss();
			break;
		}
		super.onClick(v);
	}
	/**
	 * 发送本机的地理位置
	 * @param phone 手机号/地理位置
	 */
	private void sendSMSMessage(String phone){
		if(phone.contains(LONGITUDE)&&phone.contains(LATITUDE)){//{"longitude":116.376598,"latitude":39.99814}
			try {
				json = new JSONObject(phone);
				latLonPoint = new LatLonPoint(json.getDouble(LATITUDE),
						json.getDouble(LONGITUDE));
				getAddress(latLonPoint);//地图移动到指定位置
				Log.d(TAG, "显示位置：" + json.toString());
			} catch (JSONException e) {
				Log.d(TAG, "显示位置失败：" + e.toString());
				e.printStackTrace();
			}
			
		}else{
			json = new JSONObject();
			try {
				json.put(LONGITUDE, selectLatLng.longitude);
				json.put(LATITUDE, selectLatLng.latitude);
				Log.d(TAG, "发送信息：" + json.toString());
			} catch (JSONException e) {
				Log.d(TAG, "发送信息失败：" + e.toString());
				// e.printStackTrace();
			}
			Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
			sendIntent.setData(Uri.parse("smsto:" + phone));
			sendIntent.putExtra("sms_body", json.toString());
			startActivity(sendIntent);
		}
	}

	/**
	 * 长按出现滑动选择城市
	 */
	private void showCityDialog() {
		dilog = dilog == null ? new SelectCityDialog(this) : dilog;
		dilog.show();
		dilog.addSelectListener(new SelectItem());
	}

	/**
	 * 点击指南针后的
	 * 
	 * @param state
	 */
	private void setCompass(int state) {
		Log.d("指南针", "状态" + state);
		switch (state) {
		case 1:
			aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			break;
		case 2:
			aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
			break;
		case 3:
			aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
			break;
		}
	}

	/**
	 * 初始化
	 */
	private void init() {
		aMap = aMap == null ? aMap = mapView.getMap() : aMap;
		setUpMap();
		geoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).icon(
				BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
		regeoMarker.setPerspective(true);

		aMap.setOnMarkerClickListener(new MyMarker());
	}

	// 点击书签
	class MyMarker implements AMap.OnMarkerClickListener {

		@Override
		public boolean onMarkerClick(Marker arg0) {
			//latLonPoint = (LatLonPoint) arg0.getObject();
			selectLatLng = arg0.getPosition();
			arg0.setTitle("发送位置:");
			arg0.setSnippet(latLonPoint.toString());
			isLon = true;
			getAddress(new LatLonPoint(selectLatLng.latitude, selectLatLng.longitude));//地图移动到指定位置
			showPhoneDialog();
			return true;
		}

	}

	/**
	 * 弹出发送短信的dialog
	 * 
	 * @param view
	 */
	private void showPhoneDialog() {
		View phoneView = View.inflate(this, R.layout.dialog_hint, null);
		phoneView.findViewById(R.id.cancelTv).setOnClickListener(this);
		phoneView.findViewById(R.id.confirmTv).setOnClickListener(this);
		phoneTv = (TextView) phoneView.findViewById(R.id.phoneEt);
		dialog = new Dialog(this, R.style.transparentFrameWindowStyle);

		// dialog.setCancelable(false);//是否允许返回
		dialog.addContentView(phoneView, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		dialog.show();

	}

	/**
	 * 弹出接收短信位置
	 * 
	 * @param latLonPoint
	 */
	private void showSmsLocationDialog(LatLonPoint latLonPoint) {
		dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
		View upView = View.inflate(this, R.layout.dialog_select, null);
		upView.findViewById(R.id.smsLocationTv).setOnClickListener(this);
		upView.findViewById(R.id.cancelSmsTv).setOnClickListener(this);

		// dialog.setCancelable(false);//是否允许返回
		dialog.addContentView(upView, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		dialog.show();
		// getAddress(latLonPoint);
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		UiSettings uiSettings = aMap.getUiSettings();

		uiSettings.setScaleControlsEnabled(true);// 比例尺
		// uiSettings.setAllGesturesEnabled(true);
		// uiSettings.setMyLocationButtonEnabled(true);
		uiSettings.setCompassEnabled(true);// 启用指南针
		// uiSettings.setMyLocationButtonEnabled(true);
		// uiSettings.setScrollGesturesEnabled(true);
		// uiSettings.setTiltGesturesEnabled(true);
		// uiSettings.setZoomControlsEnabled(true);
		// uiSettings.setZoomGesturesEnabled(true);

		aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
		aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
		aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
		aMap.setOnMapTouchListener(this);// 对amap添加触摸地图事件监听器
	}

	// 三级滚动时调用
	private CityBean bean1;
	private CityBean bean2;
	private CityBean bean3;
	private CityBean cityBean;
	private String addressName;

	// 滚动监听
	class SelectItem implements SelectCityDialog.SelectItemState {
		@Override
		public void item1(CityBean bean) {// 第一级
			bean1 = bean;
		}

		@Override
		public void item2(CityBean bean) {// 第二级
			bean2 = bean;
		}

		@Override
		public void item3(CityBean bean) {// 第三级
			bean3 = bean;
		}

		@Override
		public void lastItem(CityBean bean) {
			cityBean = bean;
			Log.d("最后滚动选择", bean.name+":"+bean.center);
			String str = bean.center;
			if(TextUtils.isEmpty(str)||!str.contains(",")){
				return;
			}
			String[] split = str.split(",");
			latLonPoint = new LatLonPoint(Double.valueOf(split[0]),
					Double.valueOf(split[1]));
			//Log.d(TAG, msg)
			getAddress(latLonPoint);//地图移动到指定位置
			// getAddress(new LatLonPoint(39.90865, 116.39751));
			// getLatlon(bean.name);
		}

	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		// showDialog();
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

	/**
	 * 响应地理编码
	 */
	public void getLatlon(final String name) {
		// showCityDialog();
		GeocodeQuery query = new GeocodeQuery(name, "010");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
		geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);// 显示系统小蓝点

		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	// 对amap添加单击地图事件监听器
	@Override
	public void onMapClick(LatLng arg0) {
		if (regeoMarker.isVisible()) {
			regeoMarker.setVisible(false);
			regeoMarker.hideInfoWindow();
		}
	}

	// 对amap添加长按地图事件监听器
	@Override
	public void onMapLongClick(LatLng latLng) {
		showMarkerLocation(latLng);
		showCityDialog();

	}

	/**
	 * 长按显示点
	 * 
	 * @param latLng
	 */
	private void showMarkerLocation(LatLng latLng) {
		regeoMarker.setVisible(true);
		regeoMarker.setPosition(latLng);
		this.selectLatLng = latLng;
	}

	// 对amap添加移动地图事件监听器
	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}

	// 对amap添加移动地图事件监听器
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}

	// 对amap添加触摸地图事件监听器
	@Override
	public void onTouch(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * 地理编码查询回调
	 */
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		Log.e("MainActivity", "地区名查询");
		if (rCode == 0) {
			if (result != null && result.getGeocodeAddressList() != null
					&& result.getGeocodeAddressList().size() > 0) {
				GeocodeAddress address = result.getGeocodeAddressList().get(0);
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
				geoMarker.setPosition(AMapUtil.convertToLatLng(address
						.getLatLonPoint()));
				addressName = address.getFormatAddress();
				// addressName = "经纬度值:" + address.getLatLonPoint()
				// + "\n位置描述:" + address.getFormatAddress();

			} else {
				ToastUtil.show(this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(this, R.string.error_key);
		} else {
			ToastUtil.show(this, getString(R.string.error_other) + rCode);
		}
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		Log.e("MainActivity", "经纬度查询");
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				RegeocodeAddress regeocodeAddress = result.getRegeocodeAddress();
				String city = regeocodeAddress.getCity();
				String Province= regeocodeAddress.getProvince();//天津市-0
				String District= regeocodeAddress.getDistrict();//和平区-1
				String Township = regeocodeAddress.getTownship();//南营门街道-2
				String Building = regeocodeAddress.getBuilding();//吉利大厦写字楼-3
				StreetNumber Street = regeocodeAddress.getStreetNumber();
				String Streets = Street.getStreet();//东四南大街
				String Number = Street.getNumber();//164号
				//String Neighborhood = regeocodeAddress.getNeighborhood();//天津中心公寓-
				String Direction= Street.getDirection();//东南（方向）
				float Distance= Street.getDistance();
				//String str = Street.toString();
				
				String addressName = result.getRegeocodeAddress()
						.getFormatAddress() + "附近";
				if(isLon){//是否是Marker调用的，显示地理位置
					regeoMarker.setSnippet(Province+District+Township+Township+Building+Streets+Number);
					regeoMarker.setObject(latLonPoint);
					regeoMarker.showInfoWindow();
				}else{
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						AMapUtil.convertToLatLng(latLonPoint), 15));// 移动视图
					regeoMarker.hideInfoWindow();//隐藏提示信息
					regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));//设置Marker的位置
				}
				isLon=false;

				
				// ToastUtil.show(this, addressName);
			} else {
				ToastUtil.show(this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(this, R.string.error_key);
		} else {
			ToastUtil.show(this, getString(R.string.error_other) + rCode);
		}
	}

}
