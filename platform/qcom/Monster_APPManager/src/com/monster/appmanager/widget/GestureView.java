package com.monster.appmanager.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class GestureView extends FrameLayout implements  OnGestureListener {
	private static final int FLING_MIN_DISTANCE = 20;// 移动最小距离
	private static final int FLING_MIN_VELOCITY = 200;// 移动最大速度
	GestureDetector mygesture = new GestureDetector(this);
	
	private boolean isInterceptClick = false;
	private GestureListener listener;

	public GestureView(Context context) {
		this(context, null);
	}

	public GestureView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GestureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// e1：第1个ACTION_DOWN MotionEvent
		// e2：最后一个ACTION_MOVE MotionEvent
		// velocityX：X轴上的移动速度（像素/秒）
		// velocityY：Y轴上的移动速度（像素/秒）

		// X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
		
		if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE) {
			// && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			isInterceptClick = true;
			if(listener != null) {
				listener.onGestureUp();
			}
		}
		
		if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE ) {
				// && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			isInterceptClick = true;
			if(listener != null) {
				listener.onGestureDown();
			}
		}
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mygesture.onTouchEvent(ev);
		int action = ev.getAction();
		if(action == MotionEvent.ACTION_DOWN) {
			isInterceptClick = false;
		} else if(action == MotionEvent.ACTION_UP) {
			if(isInterceptClick) {
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}
	
	public GestureListener getListener() {
		return listener;
	}

	public void setListener(GestureListener listener) {
		this.listener = listener;
	}

	public static interface GestureListener{
		public void onGestureDown();
		public void onGestureUp();
	}
}