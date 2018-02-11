package com.monster.appmanager.widget.panel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

public class SlidingIndicator extends ImageView {
	private int mScreenWidth;
	private Paint mPaint;
	private int mWidth = 11;
	private int mHeight = 4;
	private int mFirstPointX;
	private int mSecondPointX;
	private int mSecondPointY;
	private int mThirdPointX;
	private int offsetY = 7 + 4;

	public SlidingIndicator(Context context) {
		super(context);
		init();
	}
	
	public SlidingIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public SlidingIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public SlidingIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float y = getTranslationY();
		y += offsetY;
		// y -= mHeight;
		canvas.drawLine(mFirstPointX, y + mHeight, mSecondPointX, y + mSecondPointY, mPaint);
		canvas.drawLine(mThirdPointX, y + mHeight, mSecondPointX, y + mSecondPointY, mPaint);
	}

	private void init() {
		mScreenWidth = getScreenWidth(getContext());
		mWidth = dip2px(getContext(), mWidth);
		mHeight = dip2px(getContext(), mHeight);
		mFirstPointX = mScreenWidth / 2 - mWidth / 2;
		mSecondPointX = mScreenWidth / 2;
		mThirdPointX = mScreenWidth / 2 + mWidth / 2;

		mPaint = new Paint();
		mPaint.setColor(0xffacabad);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2);
		offsetY = dip2px(getContext(), offsetY);
	}

	private int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getWidth();
	}

	private int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public void invalidOffset(float offset) {
		int y = (int) (mHeight * offset);
		y *= 2;
		if (y != mSecondPointY) {
			mSecondPointY = y;
			invalidate();
		}
	}
}
