package com.android.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Region;

public class DetailTopAnimView extends View {
	private static final String TAG = "DetailTopAnimView";
	private Paint mPaint;
	private Path mPath;
	private Point startPoint;
	private Point endPoint;
	private static float POINTWIDTH = 5.0f;
	public static int height = 48 * 3;
	public static int width = 1080;
	private Point assistPoint;
	public float mPx = 0;
	public float mPy = height;

	public DetailTopAnimView(Context context) {
		this(context, null);
	}

	public DetailTopAnimView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DetailTopAnimView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void setAssistPointY(float py) {
		// Log.i(TAG, "setAssistPointY = " + py);
		mPy = py;
	}

	public float getAssistPointY() {
		return mPy;
	}

	public void setAssistPointX(float px) {
		// Log.i(TAG, "setAssistPointX = " + px);
		mPx = px;
	}

	public float getAssistPointX() {
		return mPx;
	}

	private boolean mDirection = true;

	public void setDirection(boolean value) {
		mDirection = value;
		if (mDirection) {
			startPoint.set(0, height);
			endPoint.set(width, height);
		} else {
			startPoint.set(0, 0);
			endPoint.set(width, 0);
		}
	}

	public boolean getDirection() {
		return mDirection;
	}

	private boolean mIsDrag = false;

	public void setDrag(boolean value) {
		mIsDrag = value;
	}

	public boolean getDrag() {
		return mIsDrag;
	}

	public void restore() {
		mPx = width / 2;
		mPy = getTopAnimHeight();
	}

	private void init(Context context) {
		mPaint = new Paint();
		mPath = new Path();
		startPoint = new Point(0, height);
		endPoint = new Point(width, height);
		assistPoint = new Point(width / 2, -height);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(POINTWIDTH);
		mPaint.setStyle(Paint.Style.FILL);
	}

	// 在onDraw中画二阶贝塞尔
	int startY = 0;

	protected void onDraw(Canvas canvas) {
		// Log.i(TAG, "onDraw assistPoint.x  = " + mPx + "assistPoint.y  = " +
		// mPy);
		super.onDraw(canvas);

		drawV2(canvas);
	}

	private void drawV1(Canvas canvas) {
		canvas.save();

		mPath.reset();

		// int startY = (mDirection && mPy > 60) ? startPoint.y - mPy
		// :startPoint.y;
		startY = startPoint.y;

		mPath.moveTo(startPoint.x, startY);
		// 头两个是控制点，最后两个是终点
		mPath.cubicTo(mPx, mPy, 1080 - mPx, mPy, endPoint.x, startY);

		// if(!mDirection) {
		// mPath.lineTo(width, height);
		// mPath.lineTo(0, height);
		// }

		mPath.lineTo(startPoint.x, startY);

		canvas.drawPath(mPath, mPaint);

		// if(mDirection && mPy > 60) {
		// canvas.drawRect(0, startY, width, height, mPaint);
		// }


		canvas.restore();
	}

	private void drawV2(Canvas canvas) {
		if(mPy > 0) {
			return;
		}
		canvas.save();

		mPath.reset();		
		startY = getTopAnimHeight();
		mPath.moveTo(startPoint.x, startY);
		mPath.quadTo(mPx, mPy + startY, endPoint.x, startY);
		mPath.lineTo(startPoint.x, startY);
		canvas.drawPath(mPath, mPaint);
		
		canvas.restore();
	}

	
    private int getTopAnimHeight() {
        return this.getLayoutParams().height;
    }
}