package cn.tcl.music.view;

import cn.tcl.music.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ColoredLinearLayout extends LinearLayout {
	
	private final static int DEFAULT_COLOR = Color.TRANSPARENT;
	
	private int mColor = DEFAULT_COLOR;
	
	public ColoredLinearLayout(Context context) {
		super(context);
	}
	
	public ColoredLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ColoredLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}
	

	public ColoredLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs)
	{
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColoredLinearLayout);
		mColor = a.getColor(R.styleable.ColoredLinearLayout_colorBackground, DEFAULT_COLOR);
		setWillNotDraw(false);
		a.recycle();
	}
	
	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(mColor);
		super.draw(canvas);
	}

}
