/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeText extends TextView{

	public MarqueeText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MarqueeText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MarqueeText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public boolean isFocused() {
		return true;
	}
		@Override
	protected void onFocusChanged(boolean focused, int direction,
		Rect previouslyFocusedRect) {
	}

}
