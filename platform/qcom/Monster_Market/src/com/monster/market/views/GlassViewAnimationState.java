package com.monster.market.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;

public class GlassViewAnimationState  {
	
	int mStepNumber = 10;
	int mInitStep = 0;
	Handler mHandler;
	GlassView mView;
	Context mContext;

	int mLightPoints = 0;
	boolean mDirection = true;

	public GlassViewAnimationState(Context context, Handler h,
			GlassView view) {
		mHandler = h;
		mView = view;
		mContext = context;
	}

	public void draw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		mView.rotateCenterY = mView.BigRadius;
		mView.panRotate.setTranslate(mView.rotateCenterX + mView.mDotRadius,
				mView.rotateCenterY + mView.mDotRadius);
		canvas.concat(mView.panRotate);

		for (int i = 0; i < mView.COUNT; i += 2) {
			canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i + 1],
					mView.mDotRadius, mView.mDarkPaint);
		}
		for (int i = 2 * mLightPoints + mView.COUNT; i < 2 * mView.COUNT; i += 2) {
			canvas.drawCircle(mView.ptsDraw[i], mView.ptsDraw[i + 1],
					mView.mDotRadius, mView.mDarkPaint);
		}
		if (mLightPoints > 0) {
			for (int i = 0; i < 2 * mLightPoints; i += 2) {
				canvas.drawCircle(mView.ptsDraw[i + mView.COUNT],
						mView.ptsDraw[i + mView.COUNT + 1], mView.mDotRadius,
						mView.mLightPaint);
			}
		}
	}

	public void startAnimation() {
		directionChangeTimes = 0;
		mLightPoints = 0;
		mDirection = true;
		mHandler.removeCallbacks(mRunnable);
		mHandler.post(mRunnable);
	}

	public void cancelAnimation() {
		mHandler.removeCallbacks(mRunnable);
		mLightPoints = 0;
		mView.invalidate();
	}

	int directionChangeTimes = 0;
	Runnable mRunnable = new Runnable() {
		public void run() {
			int top = directionChangeTimes * 2 + 2;
			if (mLightPoints == top) {
				mDirection = false;
				directionChangeTimes++;
			} else if (mLightPoints == 1) {
				mDirection = true;
				directionChangeTimes = directionChangeTimes % 3;
			}
			mLightPoints += 1 * (mDirection ? 1 : -1);
			mView.invalidate();
			mHandler.postDelayed(this, (int) (250 * (1 - mLightPoints / 8f)));

		}
	};

}
