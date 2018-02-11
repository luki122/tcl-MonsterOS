/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.util;


import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;


@SuppressLint("NewApi")
public class RecyclingImageView extends ImageView{

	private static final String TAG = RecyclingImageView.class.getSimpleName();
	private Context mContext;
	private int id = -1;
	private GestureDetector mDetector;
	private Boolean m_bScale = false;
	private Matrix mMatrix;
	private float mScaleFactor = .4f;
	private int mImageHeight = -1, mImageWidth = -1;
	private int mAlpha = 255;
	private float mRotationDegrees = 0.f;
	private float mFocusX = 0.f;
    private float mFocusY = 0.f;
	
	private ScaleGestureDetector mImageScaleDetector;
	
	
	
    public RecyclingImageView(Context context) {
        super(context);
        this.mContext = context;
        if (mDetector == null) {
        	mDetector = new GestureDetector(context, new ImageGestureDetector());
		}
		
		if (mImageScaleDetector == null) {
			mImageScaleDetector = new ScaleGestureDetector(mContext, new ImageScaleListener());
		}
		
    }

    public RecyclingImageView(Context context, int id) {
		super(context);
		this.mContext = context;
		this.id = id;
		if (mDetector == null) {
        	mDetector = new GestureDetector(context, new ImageGestureDetector());
		}
		
		mMatrix = new Matrix();
		
		if (mImageScaleDetector == null) {
			mImageScaleDetector = new ScaleGestureDetector(mContext, new ImageScaleListener());
		}
	}


	public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        if (mDetector == null) {
        	mDetector = new GestureDetector(context, new ImageGestureDetector());
		}
        
        if (mImageScaleDetector == null) {
			mImageScaleDetector = new ScaleGestureDetector(mContext, new ImageScaleListener());
		}
        
    }

    @Override
    protected void onDetachedFromWindow() {
        setImageDrawable(null);

        super.onDetachedFromWindow();
    }

    /**
     * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        final Drawable previousDrawable = getDrawable();

        super.setImageDrawable(drawable);
        
        notifyDrawable(drawable, true);
        notifyDrawable(previousDrawable, false);
        
        if (drawable != null) {
        	mImageHeight 	= drawable.getIntrinsicHeight();
    		mImageWidth 	= drawable.getIntrinsicWidth();
		}
        
    }

    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }

	public void ShowImageAnmation()
	{

		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(150);
		animationSet.addAnimation(scaleAnimation);
		
		this.startAnimation(animationSet);
		return;
	}
	
	public void ShowImageBackgroundColor() {
		
		this.setBackgroundColor(Color.parseColor("#87CEFA"));
		return;
	}
	
	public void ScaleImage() {
		
		float scaledImageCenterX = (mImageWidth*mScaleFactor)/2;
        float scaledImageCenterY = (mImageHeight*mScaleFactor)/2;
        mMatrix.reset();
        //mScaleFactor += 0.2f;
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postRotate(mRotationDegrees,  scaledImageCenterX, scaledImageCenterY);
        mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);

		this.setImageMatrix(mMatrix);
		this.setAlpha(mAlpha);
		
		return;
	}
	
	public void SetScaleGesture(boolean bScale) {
		this.m_bScale = bScale;
		return;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		mDetector.onTouchEvent(event);
		
		return super.onTouchEvent(event);
	}
	
	
	private class ImageScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor(); // scale change since previous event
			
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f)); 

			return true;
		}
	}
	
	private class ImageGestureDetector extends GestureDetector.SimpleOnGestureListener
	{
		private ImageView mView = null;

		public ImageGestureDetector() {
			super();
		}

		public ImageGestureDetector(ImageView mView) {
			super();
			this.mView = mView;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			mMatrix.postTranslate(velocityX, velocityY);
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			//ShowImageBackgroundColor();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
	}
}
