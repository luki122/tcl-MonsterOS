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

public class DragTopAnimView extends View {
	private static final String TAG = "DragTopAnimView";
	private Paint mPaint;
	private Path mPath;
	private Point startPoint;
	private Point endPoint;
	private static float POINTWIDTH = 5.0f;
	public static int height = 70 * 3;
	public static int width = 1080;
	public float mPx = width / 2 ;
	public float mPy = 0;

	public DragTopAnimView(Context context) {
		this(context, null);
	}

	public DragTopAnimView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragTopAnimView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void setAssistPointY(float py) {
//		Log.i(TAG, "setAssistPointY = " + py);
		mPy = py;
	}

	public float getAssistPointY() {
		return mPy;
	}
	
	public void setAssistPointX(float px) {
//		Log.i(TAG, "setAssistPointX = " + px);
		mPx = px;
	}

	public float getAssistPointX() {
		return mPx;
	}
	
	private boolean mIsDrag = false;
	public void setDrag(boolean value) {
		mIsDrag =  value;
	}
	
	public boolean getDrag() {
		return mIsDrag;
	}
	
	private boolean mIsRestore = false;
	public void setRestore(boolean value) {
		mIsRestore =  value;
	}
	
	public void restore() {
		mPx = width / 2 ;
		mPy = 0;
	}

	private void init(Context context) {
		mPaint = new Paint();
		mPath = new Path();
		startPoint = new Point(0, 0);
		endPoint = new Point(width, 0);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(POINTWIDTH);
		mPaint.setStyle(Paint.Style.FILL);
	}

	// 在onDraw中画二阶贝塞尔
	int startY = 0;
	protected void onDraw(Canvas canvas) {		
		super.onDraw(canvas);
		
//		 Log.i("DragTopAnimView", "onDraw mIsDrag = " + mIsDrag + " mIsRestore= " + mIsRestore);
		if((!mIsDrag && !mIsRestore) || mPy < 0) {
//			canvas.drawRect(0, 0, width, height, mPaint);
			canvas.drawColor(Color.WHITE);
			return;
		}
		
		canvas.save();
		mPath.reset();
		startY = startPoint.y;				
		mPath.moveTo(startPoint.x, startY);
		mPath.quadTo(mPx, mPy, endPoint.x, startY);
		
		mPath.lineTo(endPoint.x, height);
		mPath.lineTo(startPoint.x, height);
		mPath.lineTo(startPoint.x, startY);		
		canvas.drawPath(mPath, mPaint);
		
		
//		mPath.lineTo(startPoint.x, startY);
//		canvas.clipPath(mPath, Region.Op.DIFFERENCE);
//		canvas.drawColor(Color.WHITE);
		
		canvas.restore();
	}

}