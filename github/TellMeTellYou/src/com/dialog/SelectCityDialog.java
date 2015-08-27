package com.dialog;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.bean.CityBean;
import com.db.SelectCity;
import com.tell.R;
import com.wheel.wheelView.OnWheelScrollListener;
import com.wheel.wheelView.WheelAdapter;
import com.wheel.wheelView.WheelView;

/**
 * @ClassName:     SelectCityDialog.java
 * @author         zcs
 * @Date           2015年4月7日 上午10:53:32 
 * @Description:   滑动选择城市
 */
public class SelectCityDialog extends Dialog {
	
	private WheelView province;
	private WheelView city;
	private WheelView area;
	private CityBean cityBean1;
	private CityBean cityBean2;
	private CityBean cityBean3;
	Context cxt;
	private View view;
	private SelectCity selectCity3;
	private SelectCity selectCity2;
	private SelectCity selectCity1;
	private SelectItemState addSelectListener;
	public SelectCityDialog(Context context) {
		super(context, R.style.transparentFrameWindowStyle);
		this.cxt = context;
		view = View.inflate(context, R.layout.dialog_region, null);
		/*TextView view  = new TextView(context);
		view.setText("dafasdfs");*/
		this.setContentView(view, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
		Window window = this.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = window.getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		/*wl.width = window.getWindowManager().getDefaultDisplay().getWidth();
		//wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		// 设置显示位置
		this.onWindowAttributesChanged(wl);*/
		// 设置点击外围解散
		this.setCanceledOnTouchOutside(true);
		province = (WheelView) view.findViewById(R.id.wv_province);
		city = (WheelView) view.findViewById(R.id.wv_city);
		area = (WheelView) view.findViewById(R.id.wv_area);
		initData();
	}
	public void initData(){
		province.addScrollingListener(new WheelScrollListener());
		city.addScrollingListener(new WheelScrollListener());
		area.addScrollingListener(new WheelScrollListener());
		onItemSelect1(null);
	}
	// 监听滚动时的状态
		class WheelScrollListener implements OnWheelScrollListener {

			@Override
			public void onScrollingStarted(WheelView wheel) {// 按下时
				// System.out.println("SSSSS");

			}

			@Override
			public void onScrollingFinished(WheelView wheel) {// 结束时
				int currentItem = wheel.getCurrentItem();
				
				//System.out.println(currentItem);
				switch (wheel.getId()) {
				case R.id.wv_province:
					cityBean1 = selectCity1.getCityBeanByIndex(currentItem);
					onItemSelect2(cityBean1);//显第二级
					break;
				case R.id.wv_city:
					cityBean2 = selectCity2.getCityBeanByIndex(currentItem);
					onItemSelect3(cityBean2);//显第三级
					break;
				case R.id.wv_area:
					cityBean3 = selectCity3.getCityBeanByIndex(currentItem);
					addSelectListener.item3(cityBean3);//第三级滚动事件
					if(addSelectListener!=null){
						addSelectListener.lastItem(cityBean3);//最后一级
					}
					break;
				}

			}

		}

		/**
		 * 显示第一级
		 * @param currentItem
		 */
		private void onItemSelect1(CityBean bean) {
			selectCity1 = new SelectCity(cxt);
			List<CityBean> node = selectCity1.getNode(bean);
			selectCity1.closeDataBase();
			cityBean1 = node==null||node.size()<=0?null:node.get(0);
			//Log.e("node", cityBean1.name+" "+cityBean1.adcode+" "+cityBean1.level);
			province.setAdapter(new MyAdapter(node));
			if(cityBean1!=null){
				if(addSelectListener!=null){
					addSelectListener.item1(cityBean1);
				}
				onItemSelect2(cityBean1);
			}
			
		}
		/**
		 * 显示第二级
		 * @param currentItem
		 */
		private void onItemSelect2(CityBean bean) {
			selectCity2 = new SelectCity(cxt);
			List<CityBean> node2 = selectCity2.getNode(bean);
			selectCity2.closeDataBase();
			if(node2==null||node2.size()<=0){
				city.setVisibility(View.GONE);
				addSelectListener.lastItem(bean);
				area.setVisibility(View.GONE);
				return;
			}else{
				city.setVisibility(View.VISIBLE);
			}
			cityBean2 = node2==null||node2.size()<=0?null:node2.get(0);
			//Log.e("node", cityBean2.name+" "+cityBean2.adcode+" "+cityBean2.level);
			city.setAdapter(new MyAdapter(node2));
			city.setCurrentItem(0);
			if(cityBean2!=null){
				if(addSelectListener!=null){
					addSelectListener.item2(cityBean2);
				}
				onItemSelect3(cityBean2);
			}
		}
		/**
		 * 显示第三级
		 * @param currentItem
		 */
		private void onItemSelect3(CityBean bean) {//110100
			selectCity3 = new SelectCity(cxt);
			List<CityBean> node3 = selectCity3.getNode(bean);
			selectCity2.closeDataBase();
			if(node3==null||node3.size()<=0){
				area.setVisibility(View.GONE);
				return;
			}else{
				area.setVisibility(View.VISIBLE);
			}
			cityBean3 = node3==null||node3.size()<=0?null:node3.get(0);
			if(addSelectListener!=null){
				addSelectListener.item3(cityBean3);
				addSelectListener.lastItem(cityBean3);
			}
			//Log.e("node", cityBean3.name+" "+cityBean3.adcode+" "+cityBean3.level);
			area.setAdapter(new MyAdapter(node3));
			area.setCurrentItem(0);
		}

	/*public SelectCityDialog(Context cxt){
		this.cxt = cxt;
	}*/
	public void addSelectListener(SelectItemState ItemState){
		this.addSelectListener = ItemState;
		
	}
	class MyAdapter implements WheelAdapter {
		List<CityBean> list;

		public MyAdapter(List<CityBean> list) {
			this.list = list;
		}

		@Override
		public int getItemsCount() {
			return list.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getItem(int index) {
			// System.out.println("当前 "+index);
			// System.out.println("总大小：" +list.size());
			return (T) list.get(index).name;
		}

		@Override
		public int getMaximumLength() {

			return 0;
		}

		@Override
		public <T> T getItemView(int index) {
			// TODO Auto-generated method stub
			return (T) list.get(index).name;
		}

	}
	public interface SelectItemState{
		public void item1(CityBean bean);
		public void item2(CityBean bean);
		public void item3(CityBean bean);
		public void lastItem(CityBean bean);
	}

}
