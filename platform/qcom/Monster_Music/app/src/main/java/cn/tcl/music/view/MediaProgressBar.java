package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import cn.tcl.music.model.MediaInfo;


public class MediaProgressBar extends ProgressBar {

	public MediaProgressBar(Context context) {
		super(context);
	}

	public MediaProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MediaProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

}
