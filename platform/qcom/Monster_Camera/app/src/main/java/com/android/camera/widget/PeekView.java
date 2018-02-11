/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.camera.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.android.camera.ui.PeekImageView;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.Gusterpolator;

/**
 * An ImageView which has the built-in peek animation support.
 */
public class PeekView extends ImageView {

    private static final float ROTATE_ANGLE = -7f;
    public static final long PEEK_IN_DURATION_MS = 300;
    public static final long PEEK_IN_FOR_UPDATE_MS=600;
    private static final long PEEK_STAY_DURATION_MS = 100;
    private static final long PEEK_OUT_DURATION_MS = 200;
    private static final float FILMSTRIP_SCALE = 0.8f;

    private AnimatorSet mPeekAnimator;
    private float mPeekRotateAngle;
    private Point mRotationPivot;
    private float mRotateScale;
    private boolean mAnimationCanceled;
    private Drawable mImageDrawable;
    private Rect mDrawableBound;

    public PeekView(Context context) {
        super(context);
        init();
    }

    public PeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PeekView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mRotationPivot = new Point();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (mImageDrawable == null) {
            return;
        }
        c.save();
//        c.rotate(mPeekRotateAngle, mRotationPivot.x, mRotationPivot.y);
        if(mScaleX<0.4f) {
            Path path = new Path();
            float circleX=c.getWidth();
            float circleY=c.getHeight();
            float centerXInDrawable=mDrawableBound.width()/2;
            float centerYinDrawable=mDrawableBound.height()/2;
            float layoutX=centerXInDrawable/mWidth;
            float layoutY=centerYinDrawable/mHeight;
            if(mDrawableBound.width()<mDrawableBound.height()) {
                path.addCircle(c.getWidth() * layoutX, c.getHeight() * layoutY,
                        c.getWidth() * layoutX, Path.Direction.CW);
            }else{
                path.addCircle(c.getWidth() * layoutX, c.getHeight() * layoutY,
                        c.getHeight() * layoutY, Path.Direction.CW);
            }
            c.clipPath(path);
        }
        mImageDrawable.setBounds(mDrawableBound);
        try {
            mImageDrawable.draw(c);
        }catch (Exception e){

        }
        this.setPivotX(0);
        this.setPivotY(0);
        this.setTranslationX(mTransX);
        this.setTranslationY(mTransY);
        this.setScaleX(mScaleX);
        this.setScaleY(mScaleX);

        c.restore();
    }

    /**
     * Starts the peek animation.
     *
     * @param bitmap The bitmap for the animation.
     * @param strong {@code true} if the animation is the strong version which
     *               shows more portion of the bitmap.
     * @param accessibilityString An accessibility String to be announced
                     during the peek animation.
     */
    public void startPeekAnimation(final Bitmap bitmap, boolean strong,
            String accessibilityString) {
        ValueAnimator.AnimatorUpdateListener updateListener =
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        mPeekRotateAngle = mRotateScale * (Float) valueAnimator.getAnimatedValue();
                        invalidate();
                    }
                };
        ValueAnimator peekAnimateIn = ValueAnimator.ofFloat(0f, ROTATE_ANGLE);
        ValueAnimator peekAnimateStay = ValueAnimator.ofFloat(ROTATE_ANGLE, ROTATE_ANGLE);
        ValueAnimator peekAnimateOut = ValueAnimator.ofFloat(ROTATE_ANGLE, 0f);
        peekAnimateIn.addUpdateListener(updateListener);
        peekAnimateOut.addUpdateListener(updateListener);
        peekAnimateIn.setDuration(PEEK_IN_DURATION_MS);
        peekAnimateStay.setDuration(PEEK_STAY_DURATION_MS);
        peekAnimateOut.setDuration(PEEK_OUT_DURATION_MS);
        peekAnimateIn.setInterpolator(new DecelerateInterpolator());
        peekAnimateOut.setInterpolator(new AccelerateInterpolator());
        mPeekAnimator = new AnimatorSet();
        mPeekAnimator.playSequentially(peekAnimateIn, peekAnimateStay, peekAnimateOut);
        mPeekAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                setVisibility(VISIBLE);
                mAnimationCanceled = false;
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!mAnimationCanceled) {
                    clear();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mAnimationCanceled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        mRotateScale = (strong ? 1.0f : 0.5f);
        mImageDrawable = new BitmapDrawable(getResources(), bitmap);
        Point drawDim = CameraUtil.resizeToFill(mImageDrawable.getIntrinsicWidth(),
                mImageDrawable.getIntrinsicHeight(), 0, (int) (getWidth() * FILMSTRIP_SCALE),
                (int) (getHeight() * FILMSTRIP_SCALE));
        int x = getMeasuredWidth();
        int y = (getMeasuredHeight() - drawDim.y) / 2;
        mDrawableBound = new Rect(x, y, x + drawDim.x, y + drawDim.y);
        mRotationPivot.set(x, (int) (y + drawDim.y * 1.1));
        mPeekAnimator.start();

        announceForAccessibility(accessibilityString);
    }



    private int mX;
    private int mY;
    private int mWidth;
    private int mHeight;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int[] coords=new int[2];
        getLocationInWindow(coords);
        mX=coords[0];
        mY=coords[1];
        mWidth=this.getWidth();
        mHeight=this.getHeight();
        coords=null;
    }

    private AnimatorSet mScalingPeekAnimator;
    private float mScaleX;
    private float mScaleY;
    private float mTransX;
    private float mTransY;
    public static class Circle{
        public float centerPoint;
        public float radius;
    }

    public interface OnCaptureStateListener {
        public void onCaptureAnimationComplete();
    }
    public void startScalingPeekAnimation(Rect thumb, final Bitmap bitmap, boolean strong,
                                          String accessibilityString, final OnCaptureStateListener listener){
        if(mScalingPeekAnimator!=null&&mScalingPeekAnimator.isRunning()){
            return;
        }
        mImageDrawable = new BitmapDrawable(getResources(), bitmap);
        Point drawDim = CameraUtil.resizeToFill(mImageDrawable.getIntrinsicWidth(),
                mImageDrawable.getIntrinsicHeight(), 0, (int) getWidth(),
                (int) (getHeight()));
        mDrawableBound=new Rect(0,0,drawDim.x,drawDim.y);

        int deltaX,deltaY;
        int width,height;
        float targetScaleX,targetScaleY;
        if(getWidth()<getHeight()) {
            deltaX = thumb.left - mX;
            int centerMargin = (int) (thumb.width() * drawDim.y / (float) drawDim.x - thumb.height()) / 2;
            deltaY = thumb.top - mY - centerMargin;
        }else{
            deltaY=thumb.top-mY;
            int centerMargin=(int) (thumb.height() * drawDim.x / (float) drawDim.y - thumb.width()) / 2;
            deltaX=thumb.left-mX-centerMargin;

        }
        width=thumb.width();
        height=thumb.height();
        targetScaleX = width / (float) mDrawableBound.width();
        targetScaleY = height / (float) mDrawableBound.height();


        playScalingAnimation(targetScaleX,deltaX,deltaY,listener);

        announceForAccessibility(accessibilityString);
    }

    private void playScalingAnimation(float targetScaleX,int deltaX,int deltaY,final OnCaptureStateListener listener){
        if(mScalingPeekAnimator==null){
            ValueAnimator scalingXAnima=ValueAnimator.ofFloat(1.0f, targetScaleX);

            scalingXAnima.setDuration(PEEK_IN_DURATION_MS);
            scalingXAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScaleX = (Float) animation.getAnimatedValue();
                    invalidate();

                }
            });

            ValueAnimator transXAnima=ValueAnimator.ofFloat(0, deltaX);
            transXAnima.setDuration(PEEK_IN_DURATION_MS);
            transXAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTransX = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });

            ValueAnimator transYAnima=ValueAnimator.ofFloat(0, deltaY);
            transYAnima.setDuration(PEEK_IN_DURATION_MS);
            transYAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTransY = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mScalingPeekAnimator=new AnimatorSet();
            mScalingPeekAnimator.setInterpolator(Gusterpolator.INSTANCE);
            mScalingPeekAnimator.playTogether(scalingXAnima, transXAnima, transYAnima);

        }
        mScalingPeekAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                setTranslationX(0);
                setTranslationY(0);
                setScaleX(1.0f);
                setScaleY(1.0f);
                listener.onCaptureAnimationComplete();
                mScalingPeekAnimator.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setVisibility(View.GONE);
                setTranslationX(0);
                setTranslationY(0);
                setScaleX(1.0f);
                setScaleY(1.0f);
                listener.onCaptureAnimationComplete();
                mScalingPeekAnimator.removeListener(this);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mScalingPeekAnimator.start();
    }

    /**
     * @return whether the animation is running.
     */
    public boolean isPeekAnimationRunning() {
        return mPeekAnimator.isRunning();
    }

    /**
     * Stops the animation. See {@link android.animation.Animator#end()}.
     */
    public void stopPeekAnimation() {
        if (isPeekAnimationRunning()) {
            mPeekAnimator.end();
        } else {
            clear();
        }
    }

    /**
     * Cancels the animation. See {@link android.animation.Animator#cancel()}.
     */
    public void cancelPeekAnimation() {
        if (isPeekAnimationRunning()) {
            mPeekAnimator.cancel();
        } else {
            clear();
        }
    }

    private void clear() {
        setVisibility(INVISIBLE);
        setImageDrawable(null);
    }
}
