package com.monster.market.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.monster.market.R;

public class GlassView extends ImageView {
	
	public final int COUNT = 24;

	public float ptsDraw[] = new float[2 * COUNT];
	public float BigRadius = 0;
	public float mDotRadius = 2;
	public int mMainColor = Color.GREEN;
	Matrix panRotate;

	public float rotateCenterX, rotateCenterY;
	public final Paint mLightPaint = new Paint();
	public final Paint mDarkPaint = new Paint();
	private int darkColor = 0;
	private int lightColor = 0;

	int mLightRunningPoints = 0;
	public long timeLeft;
	public long time;
	
	private GlassViewAnimationState mAnimState;

	public void setTotalTime(long value) {
		time = value;
	}

	public void setTimeleft(long value) {
		timeLeft = value;
		this.invalidate();
	}

	public long getTimeleft() {
		return timeLeft;
	}

	public void setLightRunningPoints(int number) {
		this.mLightRunningPoints = number;
		this.invalidate();
	}

	public void setPtsDraw(float[] ptsValues) {
		System.arraycopy(ptsValues, 0, ptsDraw, 0, 2 * COUNT);
	}

	boolean showAnim = true;

	public void setShowAnim(boolean value) {
		showAnim = value;
	}

	public GlassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mAnimState = new GlassViewAnimationState(context, new Handler(), this);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.glass_view);
		BigRadius = a.getDimensionPixelOffset(R.styleable.glass_view_big_radius, 140);
		mDotRadius = a.getDimensionPixelOffset(R.styleable.glass_view_dot_radius, 2);
		darkColor = a.getColor(R.styleable.glass_view_dark_color, android.R.color.background_dark);
		lightColor = a.getColor(R.styleable.glass_view_light_color, android.R.color.background_light);
		a.recycle();

		panRotate = new Matrix();
		rotateCenterX = BigRadius;
		rotateCenterY = BigRadius;
		panRotate.setTranslate(rotateCenterX, rotateCenterY);
		mLightPaint.setStyle(Style.FILL);
		mLightPaint.setColor(lightColor);
		mLightPaint.setAntiAlias(true);
		mDarkPaint.setColor(darkColor);
		mDarkPaint.setAntiAlias(true);
		mDarkPaint.setStyle(Style.FILL);
		double angle = Math.PI / 2;
		angle += Math.PI / 2 / (COUNT / 4);
		
		ptsDraw[0] = 0;
		ptsDraw[1] = BigRadius;
		ptsDraw[COUNT / 2] = (-1) * BigRadius;
		ptsDraw[COUNT / 2 + 1] = 0;
		ptsDraw[COUNT] = 0;
		ptsDraw[COUNT + 1] = (-1) * BigRadius;
		ptsDraw[COUNT + COUNT / 2] = BigRadius;
		ptsDraw[COUNT + COUNT / 2 + 1] = 0;

		for (int i = 2; i <= COUNT - 1; i += 2) {
			ptsDraw[i] = (float) (BigRadius * Math.cos(angle));
			ptsDraw[i + 1] = (float) (BigRadius * Math.sin(angle));
			
			ptsDraw[COUNT - i] = ptsDraw[i];
			ptsDraw[COUNT + 1 - i] = (-1) * ptsDraw[i + 1];
			ptsDraw[COUNT + i] = (-1) * ptsDraw[i];
			ptsDraw[COUNT + 1 + i] = (-1) * ptsDraw[i + 1];
			ptsDraw[COUNT * 2 - i] = (-1) * ptsDraw[i];
			ptsDraw[COUNT * 2 + 1 - i] = ptsDraw[i + 1];
			angle += Math.PI / 2 / (COUNT / 4);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mAnimState != null && showAnim) {
			mAnimState.draw(canvas);
		}
	}
	
	public void start() {
		mAnimState.startAnimation();
	}
	
	public void stop() {
		mAnimState.cancelAnimation();
	}

}
