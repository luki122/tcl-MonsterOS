package com.monster.appmanager.widget;

import com.monster.appmanager.MainActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class AppCountContainer extends LinearLayout {
	public AppCountContainer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public AppCountContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public AppCountContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AppCountContainer(Context context) {
		super(context);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		MainActivity activity = (MainActivity)getContext();
		if(activity.isPannelOpen()) {
			if(ev.getAction() == MotionEvent.ACTION_UP) {
				performClick();
			}
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}
}
