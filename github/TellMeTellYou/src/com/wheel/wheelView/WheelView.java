/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wheel.wheelView;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.tell.R;

/**
 * Numeric wheel view.
 * 
 * @author Yuri Kanivets
 */
public class WheelView extends View {
	/** 滚动的时间 */
	private static final int SCROLLING_DURATION = 400;

	/** 滚动的最小的 scrolling */
	private static final int MIN_DELTA_FOR_SCROLLING = 1;

	/** 顶部和底部的阴影颜色 */
	private static final int[] SHADOWS_COLORS = new int[] { 0xffFFFFFF,
			0xeeFFFFFF, 0xccFFFFFF, 0xbbFFFFFF, 0xaaFFFFFF, 0x99FFFFFF,
			0x80FFFFFF, 0x55FFFFFF, 0x20FFFFFF, 0x00FFFFFF, 0x00FFFFFF,
			0x00FFFFFF };

	/** 附加项目高度 (添加到标准文本项目高度) */
	private static final int ADDITIONAL_ITEM_HEIGHT = 25;

	/** 项目布局额外的宽度 */
	private static final int ADDITIONAL_ITEMS_SPACE = 65;

	/** 标签偏移 */
	private static final int LABEL_OFFSET = 18;

	/** 左边和右边的空白值 */
	private static final int PADDING = 20;

	/** 可见项的默认计数 */
	private static final int DEF_VISIBLE_ITEMS = 5;

	// 轮值
	private WheelAdapter adapter = null;
	private int currentItem = 0;

	// 宽度
	private int itemsWidth = 0;
	private int labelWidth = 0;

	// 可见项计数
	private int visibleItems = DEF_VISIBLE_ITEMS;

	// 项目高度
	private int itemHeight = 0;

	// 布局
	private StaticLayout itemsLayout;
	private StaticLayout labelLayout;
	private StaticLayout valueLayout;

	// 标签 & 背景
	private String label;
	private Drawable centerDrawable;

	// 影图片
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

	// 上下滚动
	private boolean isScrollingPerformed;
	private int scrollingOffset;

	// 滚动动画
	private GestureDetector gestureDetector;
	private Scroller scroller;
	private int lastScrollY;

	// 循环滚动
	boolean isCyclic = false;

	// 滚动开始/结束/过程中
	private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
	private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();

	// 阴影偏移量
	private int shadowOffset = 3;

	// 文字描绘
	private TextPaint itemsPaint;// 文字
	private TextPaint valuePaint;// 选中的文字
	/** 文字大小 */
	private static final int TEXT_SIZE = 20;// 默认文字大小
	private float VALUE_TEXT_SIZE = TEXT_SIZE + 5;// 选中的文字大小

	/** 项目的文本颜色 */
	private static final int VALUE_TEXT_COLOR = Color.RED;// 选中的字体颜色

	/** Current value & 标签文本的颜色 */
	private final static int TEXT_COLOR = Color.BLUE;// 未选中的文字
	private static final int ITEMS_TEXT_COLOR = Color.BLUE;// 未选中的文字
	
	/** 顶部和底部的物品偏移（隐藏） */
	private static final int ITEM_OFFSET = TEXT_SIZE / 10;

	/**
	 * 构造器
	 */
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	/**
	 * 构造器
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	/**
	 * 构造器
	 */
	public WheelView(Context context) {
		super(context);
		initData(context);
	}

	/**
	 * 初始化类的数据
	 * 
	 * @param context
	 *            the context
	 */
	private void initData(Context context) {
		gestureDetector = new GestureDetector(context, gestureListener);
		gestureDetector.setIsLongpressEnabled(false);

		scroller = new Scroller(context);
	}

	/**
	 * 获取车轮适配器
	 * 
	 * @return the adapter
	 */
	public WheelAdapter getAdapter() {
		return adapter;
	}

	/**
	 * 设置车轮适配器
	 * 
	 * @param adapter
	 *            the new wheel adapter
	 */
	public void setAdapter(WheelAdapter adapter) {
		this.adapter = adapter;
		invalidateLayouts();
		invalidate();
	}

	/**
	 * 设置指定的滚动器
	 * 
	 * @param interpolator
	 *            the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.forceFinished(true);
		scroller = new Scroller(getContext(), interpolator);
	}

	/**
	 * 获取可见项的数目
	 * 
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * 设置可见项的数目
	 * 
	 * @param count
	 *            the new count
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
		invalidate();
	}

	/**
	 * 获取标签
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 设置标签
	 * 
	 * @param newLabel
	 *            the label to set
	 */
	public void setLabel(String newLabel) {
		if (label == null || !label.equals(newLabel)) {
			label = newLabel;
			labelLayout = null;
			invalidate();
		}
	}

	/**
	 * 增加了换轮的听众
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * 除去换轮的听众
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}

	/**
	 * 通知改变听众
	 * 
	 * @param oldValue
	 *            the old wheel value
	 * @param newValue
	 *            the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * 增加了车轮滚动的听众
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * 消除车轮滚动的听众
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	/**
	 * 通知听众开始滚动
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * 通知听众结束滚动
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * 获取当前值
	 * 
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * 设置当前选中的下标
	 * 
	 * @param index
	 *            选中的下标
	 * @param animated
	 *            是否有动画
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (adapter == null || adapter.getItemsCount() == 0) {
			return; // throw?
		}
		if (index < 0 || index >= adapter.getItemsCount()) {
			if (isCyclic) {
				while (index < 0) {
					index += adapter.getItemsCount();
				}
				index %= adapter.getItemsCount();
			} else {
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {
				scroll(index - currentItem, SCROLLING_DURATION);
			} else {
				invalidateLayouts();

				int old = currentItem;
				currentItem = index;

				notifyChangingListeners(old, currentItem);

				invalidate();
			}
		}
	}

	/**
	 * 设置当前选中的下标
	 * 
	 * @param index
	 *            the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/**
	 * 如果轮循环。这意味着第一项之前有显示最后一个
	 * 
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * 集轮循环标志
	 * 
	 * @param isCyclic
	 *            the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;

		invalidate();
		invalidateLayouts();
	}

	/**
	 * 无效的布局
	 */
	private void invalidateLayouts() {
		itemsLayout = null;
		valueLayout = null;
		scrollingOffset = 0;
	}

	/**
	 * 初始化资源
	 */
	private void initResourcesIfNecessary() {
		if (itemsPaint == null) {
			itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
					| Paint.FAKE_BOLD_TEXT_FLAG);
			// itemsPaint.density = getResources().getDisplayMetrics().density;
			itemsPaint.setTextSize(TEXT_SIZE);
		}

		if (valuePaint == null) {
			valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
					| Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
			// valuePaint.density = getResources().getDisplayMetrics().density;
			valuePaint.setTextSize(TEXT_SIZE);// 设置文字的大小
			// valuePaint.
			valuePaint.setShadowLayer(0.1f, 0, 0.1f, TEXT_COLOR);// 文字的样式(颜色)
		}

		if (centerDrawable == null) {
			centerDrawable = getContext().getResources().getDrawable(
					R.drawable.wheel_val);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(Orientation.TOP_BOTTOM,
					SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP,
					SHADOWS_COLORS);
		}
		setBackgroundResource(R.drawable.wheel_bg);//设置背景色
		setBackgroundColor(Color.TRANSPARENT);
	}

	/**
	 * 计算布局所需的高度
	 * 
	 * @param layout
	 *            the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(Layout layout) {
		if (layout == null) {
			return 0;
		}

		int desired = getItemHeight() * visibleItems - ITEM_OFFSET * 2
				- ADDITIONAL_ITEM_HEIGHT;

		// Check against our minimum height
		desired = Math.max(desired, getSuggestedMinimumHeight());

		return desired;
	}

	/**
	 * 返回文本项的索引
	 * 
	 * @param index
	 *            the item index
	 * @return the item or null
	 */
	private String getTextItem(int index) {
		if (adapter == null || adapter.getItemsCount() <= 0) {
			return null;
		}
		int count = adapter.getItemsCount();
		if ((index < 0 || index >= count) && !isCyclic) {
			return null;
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		return adapter.getItem(index);
	}

	/**
	 * 建立文本取决于电流值
	 * 
	 * @param useCurrentValue
	 * @return the text
	 */
	private String buildText(boolean useCurrentValue) {
		StringBuilder itemsText = new StringBuilder();
		int addItems = visibleItems / 2 + 1;

		for (int i = currentItem - addItems; i <= currentItem + addItems; i++) {
			if (useCurrentValue || i != currentItem) {
				String text = getTextItem(i);
				if (text != null) {
					itemsText.append(text);
				}
			}
			if (i < currentItem + addItems) {
				itemsText.append("\n");
			}
		}

		return itemsText.toString();
	}

	/**
	 * 返回最大长度可提出项目
	 * 
	 * @return the max length
	 */
	private int getMaxTextLength() {
		// TODO 需要添加代码处
		WheelAdapter adapter = getAdapter();
		if (adapter == null || adapter.getItemsCount() <= 0) {
			return 0;
		}

		int adapterLength = adapter.getMaximumLength();
		if (adapterLength > 0) {
			return adapterLength;
		}

		String maxText = null;
		int addItems = visibleItems / 2;
		for (int i = Math.max(currentItem - addItems, 0); i < Math.min(
				currentItem + visibleItems, adapter.getItemsCount()); i++) {
			String text = adapter.getItem(i);
			if (text != null
					&& (maxText == null || maxText.length() < text.length())) {
				maxText = text;
			}
		}
		//Log.d("textLength", "最大长度" + (maxText != null ? maxText.length() : 0)+" "+maxText);
		return maxText != null ? maxText.length() : 0;
	}

	/**
	 * 车轮项目回报高
	 * 
	 * @return the item height
	 */
	private int getItemHeight() {
		if (itemHeight != 0) {
			return itemHeight;
		} else if (itemsLayout != null && itemsLayout.getLineCount() > 2) {
			itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
			return itemHeight;
		}

		return getHeight() / visibleItems;
	}

	/**
	 * 计算控制宽度和创建文本
	 * 
	 * @param widthSize
	 *            the input layout width
	 * @param mode
	 *            the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		initResourcesIfNecessary();

		int width = widthSize;
		//Log.d("默认宽度", "默认宽度" + width);
		int maxLength = getMaxTextLength();
		if (maxLength > 0) {
			float textWidth = FloatMath.ceil(Layout.getDesiredWidth("0",
					itemsPaint));
			itemsWidth = (int) ((maxLength+1+VALUE_TEXT_SIZE) * textWidth);//文字的宽度+1+变大之后的大小
		} else {
			itemsWidth = 0;
		}
		itemsWidth += ADDITIONAL_ITEMS_SPACE; // 字体的宽度加自定义宽度

		labelWidth = 0;
		if (label != null && label.length() > 0) {
			labelWidth = (int) FloatMath.ceil(Layout.getDesiredWidth(label,
					valuePaint));
		}

		boolean recalculate = false;
		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
			recalculate = true;
		} else {
			width = itemsWidth + labelWidth + 2 * PADDING;
			if (labelWidth > 0) {
				width += LABEL_OFFSET;
			}

			// 检查我们的最小宽度
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
				recalculate = true;
			}
		}

		if (recalculate) {
			// recalculate width
			int pureWidth = width - LABEL_OFFSET - 2 * PADDING;
			if (pureWidth <= 0) {
				itemsWidth = labelWidth = 0;
			}
			if (labelWidth > 0) {
				double newWidthItems = (double) itemsWidth * pureWidth
						/ (itemsWidth + labelWidth);
				itemsWidth = (int) newWidthItems;
				labelWidth = pureWidth - itemsWidth;
			} else {
				itemsWidth = pureWidth + LABEL_OFFSET; // no label
			}
		}

		if (itemsWidth > 0) {
			createLayouts(itemsWidth, labelWidth);
		}
		//Log.d("加上文字后的宽度", "加上文字后的宽度" + width);
		return width;
	}

	/**
	 * 创建布局
	 * 
	 * @param widthItems
	 *            item的宽度
	 * @param widthLabel
	 *            label的宽度
	 */
	private void createLayouts(int widthItems, int widthLabel) {
		if (itemsLayout == null || itemsLayout.getWidth() > widthItems) {
			itemsLayout = new StaticLayout(buildText(isScrollingPerformed),
					itemsPaint, widthItems,
					widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE
							: Layout.Alignment.ALIGN_CENTER, 1,
					ADDITIONAL_ITEM_HEIGHT, false);

		} else {
			itemsLayout.increaseWidthTo(widthItems);
		}
		if (!isScrollingPerformed
				&& (valueLayout == null || valueLayout.getWidth() > widthItems)) {
			String text = adapter == null
					|| currentItem > adapter.getItemsCount() - 1
					|| adapter.getItemsCount() == 0 ? "" : (String) adapter
					.getItem(currentItem);
			valueLayout = new StaticLayout(text != null ? text : "",
					valuePaint, widthItems,
					widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE
							: Layout.Alignment.ALIGN_CENTER, 1,
					ADDITIONAL_ITEM_HEIGHT, false);
		} else if (isScrollingPerformed) {
			valueLayout = null;
		} else {
			valueLayout.increaseWidthTo(widthItems);
		}

		if (widthLabel > 0) {
			if (labelLayout == null || labelLayout.getWidth() > widthLabel) {
				labelLayout = new StaticLayout(label, valuePaint, widthLabel,
						Layout.Alignment.ALIGN_NORMAL, 1,
						ADDITIONAL_ITEM_HEIGHT, false);
			} else {
				labelLayout.increaseWidthTo(widthLabel);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);// 子view的宽度显示模式
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}
		//Log.d("绘制宽高", "宽:" + width + "  高:" + height);
		setMeasuredDimension(width, height);// 绘制条目的宽度
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (itemsLayout == null) {
			if (itemsWidth == 0) {
				calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			} else {
				createLayouts(itemsWidth, labelWidth);
			}
		}

		if (itemsWidth > 0) {
			canvas.save();
			// Skip padding space and hide a part of top and bottom items
			canvas.translate(PADDING, -ITEM_OFFSET);
			drawItems(canvas);
			drawValue(canvas);
			canvas.restore();
		}

		drawCenterRect(canvas);
		drawShadows(canvas);
	}

	/**
	 * 绘制阴影的顶部和底部的控制
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		topShadow.setBounds(0, 0, getWidth(), getHeight() / visibleItems
				* shadowOffset);
		topShadow.draw(canvas);

		bottomShadow.setBounds(0, getHeight() - getHeight() / visibleItems
				* shadowOffset, getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * 得出的值和标签布局
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawValue(Canvas canvas) {
		valuePaint.setColor(VALUE_TEXT_COLOR);
		valuePaint.setTextSize(VALUE_TEXT_SIZE);
		valuePaint.drawableState = getDrawableState();

		Rect bounds = new Rect();
		itemsLayout.getLineBounds(visibleItems / 2, bounds);

		// draw label
		if (labelLayout != null) {
			canvas.save();
			canvas.translate(itemsLayout.getWidth() + LABEL_OFFSET, bounds.top);
			labelLayout.draw(canvas);
			canvas.restore();
		}

		// draw current value
		if (valueLayout != null) {
			canvas.save();
			canvas.translate(0, bounds.top + scrollingOffset);
			valueLayout.draw(canvas);
			canvas.restore();
		}
	}

	/**
	 * 绘制项目
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		canvas.save();

		int top = itemsLayout.getLineTop(1);
		canvas.translate(0, -top + scrollingOffset);

		itemsPaint.setColor(ITEMS_TEXT_COLOR);
		itemsPaint.drawableState = getDrawableState();
		itemsLayout.draw(canvas);

		canvas.restore();
	}

	// 当前的项目
	public String getCurrentItemValue() {
		return ((StrericWheelAdapter) getAdapter()).getStrContents()[getCurrentItem()];
	}

	/**
	 * Draws rect for current value
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		int center = getHeight() / 2;
		int offset = getItemHeight() / 2;
		centerDrawable.setBounds(0, center - offset, getWidth(), center
				+ offset);
		centerDrawable.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		WheelAdapter adapter = getAdapter();
		if (adapter == null) {
			return true;
		}

		if (!gestureDetector.onTouchEvent(event)
				&& event.getAction() == MotionEvent.ACTION_UP) {
			justify();
		}
		return true;
	}

	/**
	 * 滚动轮
	 * 
	 * @param delta
	 *            the scrolling value
	 */
	private void doScroll(int delta) {
		scrollingOffset += delta;

		int count = scrollingOffset / getItemHeight();
		int pos = currentItem - count;
		if (isCyclic && adapter.getItemsCount() > 0) {
			// fix position by rotating
			while (pos < 0) {
				pos += adapter.getItemsCount();
			}
			pos %= adapter.getItemsCount();
		} else if (isScrollingPerformed) {
			//
			if (pos < 0) {
				count = currentItem;
				pos = 0;
			} else if (pos >= adapter.getItemsCount()) {
				count = currentItem - adapter.getItemsCount() + 1;
				pos = adapter.getItemsCount() - 1;
			}
		} else {
			// fix position
			pos = Math.max(pos, 0);
			pos = Math.min(pos, adapter.getItemsCount() - 1);
		}

		int offset = scrollingOffset;
		if (pos != currentItem) {
			setCurrentItem(pos, false);
		} else {
			invalidate();
		}

		// update offset
		scrollingOffset = offset - count * getItemHeight();
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	// 手势的听众
	private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		public boolean onDown(MotionEvent e) {
			if (isScrollingPerformed) {
				scroller.forceFinished(true);
				clearMessages();
				return true;
			}
			return false;
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			startScrolling();
			doScroll((int) -distanceY);
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			lastScrollY = currentItem * getItemHeight() + scrollingOffset;
			int maxY = isCyclic ? 0x7FFFFFFF : adapter.getItemsCount()
					* getItemHeight();
			int minY = isCyclic ? -maxY : 0;
			scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY,
					maxY);
			setNextMessage(MESSAGE_SCROLL);
			return true;
		}
	};

	// Messages
	private final int MESSAGE_SCROLL = 0;
	private final int MESSAGE_JUSTIFY = 1;

	/**
	 * 设置下一个消息队列。清除队列之前。
	 * 
	 * @param message
	 *            the message to set
	 */
	private void setNextMessage(int message) {
		clearMessages();
		animationHandler.sendEmptyMessage(message);
	}

	/**
	 * 清除消息队列
	 */
	private void clearMessages() {
		animationHandler.removeMessages(MESSAGE_SCROLL);
		animationHandler.removeMessages(MESSAGE_JUSTIFY);
	}

	// 动画处理
	private Handler animationHandler = new Handler() {
		public void handleMessage(Message msg) {
			scroller.computeScrollOffset();
			int currY = scroller.getCurrY();
			int delta = lastScrollY - currY;
			lastScrollY = currY;
			if (delta != 0) {
				doScroll(delta);
			}

			// 滚动是没有完成的时候，最后的Y
			// 所以，手动完成它
			if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
				currY = scroller.getFinalY();
				scroller.forceFinished(true);
			}
			if (!scroller.isFinished()) {
				animationHandler.sendEmptyMessage(msg.what);
			} else if (msg.what == MESSAGE_SCROLL) {
				justify();
			} else {
				finishScrolling();
			}
		}
	};

	/**
	 * 证明轮
	 */
	private void justify() {
		if (adapter == null) {
			return;
		}

		lastScrollY = 0;
		int offset = scrollingOffset;
		int itemHeight = getItemHeight();
		boolean needToIncrease = offset > 0 ? currentItem < adapter
				.getItemsCount() : currentItem > 0;
		if ((isCyclic || needToIncrease)
				&& Math.abs((float) offset) > (float) itemHeight / 2) {
			if (offset < 0)
				offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
			else
				offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
		}
		if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
			scroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION);
			setNextMessage(MESSAGE_JUSTIFY);
		} else {
			finishScrolling();
		}
	}

	/**
	 * 开始滚动
	 */
	private void startScrolling() {
		if (!isScrollingPerformed) {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}
	}

	/**
	 * 完成滚动
	 */
	void finishScrolling() {
		if (isScrollingPerformed) {
			notifyScrollingListenersAboutEnd();
			isScrollingPerformed = false;
		}
		invalidateLayouts();
		invalidate();
	}

	/**
	 * 滚动滚轮
	 * 
	 * @param itemsToSkip
	 *            items to scroll
	 * @param 时间滚动的时间
	 */
	public void scroll(int itemsToScroll, int time) {
		scroller.forceFinished(true);

		lastScrollY = scrollingOffset;
		int offset = itemsToScroll * getItemHeight();

		scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time);
		setNextMessage(MESSAGE_SCROLL);

		startScrolling();
	}

	public void refurbish() {

	}

}
