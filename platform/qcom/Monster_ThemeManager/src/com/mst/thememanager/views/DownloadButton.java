package com.mst.thememanager.views;

import com.mst.thememanager.state.DonwloadOption;
import com.mst.thememanager.state.DownloadState;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public abstract class DownloadButton extends Button  implements DonwloadOption{

	
	public DownloadButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public DownloadButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public DownloadButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DownloadButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	

	
}
