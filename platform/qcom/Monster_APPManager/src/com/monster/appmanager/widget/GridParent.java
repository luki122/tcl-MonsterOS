package com.monster.appmanager.widget;

import com.monster.appmanager.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GridParent extends LinearLayout implements View.OnClickListener {
	private ImageView function_open;
	private View anim_view;
	public GridParent(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public GridParent(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public GridParent(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
//		function_open = (ImageView)findViewById(R.id.function_open);
//		anim_view = findViewById(R.id.anim_view);
//		function_open.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
//		anim_view.setVisibility(anim_view.getVisibility()==View.GONE?View.VISIBLE:View.GONE);
//		function_open.setImageResource(anim_view.getVisibility()==View.GONE?R.drawable.function_close:R.drawable.function_open);
	}

}
