package com.mst.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;



public class InCallImageButton extends InCallCompoundButton {

	public InCallImageButton(Context context) {
		super(context);
	}
	
	public InCallImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InCallImageButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}
	
	@Override
	public void toggle() {
	}
	
}