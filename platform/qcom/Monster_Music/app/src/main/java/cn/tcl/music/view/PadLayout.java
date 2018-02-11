package cn.tcl.music.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class PadLayout extends FrameLayout {

	public PadLayout(Context context) {
		super(context);
	}
	
	public PadLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@SuppressLint("NewApi")
	public PadLayout(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
}
