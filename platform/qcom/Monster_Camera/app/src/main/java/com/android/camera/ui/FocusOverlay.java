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

package com.android.camera.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.android.camera.FocusOverlayManager;
import com.android.camera.debug.DebugPropertyHelper;
import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * Displays a focus indicator.
 */
public class FocusOverlay extends View implements FocusOverlayManager.FocusUI {
    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    //    private static final Log.Tag TAG = new Log.Tag("FocusOverlay");
    private static final Log.Tag TAG = new Log.Tag("FocusOverlay");

    /**
     * System Properties switch to enable debugging focus UI.
     */
    private static final boolean CAPTURE_DEBUG_UI = DebugPropertyHelper.showCaptureDebugUI();

    // No focus/metering drawable assets available now, draw them directly.
    private static final boolean DRAW_FOCUS_UI = true;

    private static final int FOCUS_DURATION_MS = 200;
    private static final int FOCUS_HIDE_DELAY = 500;
    private static final int FOCUS_ALPHA_GRADIENT_START = 255;
    private static final int FOCUS_ALPHA_GRADIENT_END = 120;

    private static final int FOCUS_STAY_GRADIENT_DELAY = 1000;
    /* MODIFIED-END by sichao.hu,BUG-2743263*/

    private Drawable mFocusBound;
    private final Rect mBounds = new Rect();
    private final ValueAnimator mFocusAnimation = new ValueAnimator();
    private final ValueAnimator mFocusRingGradientAnimation;

    private Paint mDebugSolidPaint;
    private Paint mDebugCornersPaint;
    private Paint mDebugTextPaint;
    private int mDebugStartColor;
    private int mDebugSuccessColor;
    private int mDebugFailColor;
    private Rect mFocusDebugSolidRect;
    private Rect mFocusDebugCornersRect;
    private boolean mIsPassiveScan;
    private String mDebugMessage;

    private int mPositionX;
    private int mPositionY;
    private int mMeteringX;
    private int mMeteringY;
    private int mFocusIndicatorSize;
    private boolean mShowIndicator;
    private int mFocusOuterRingSize;
    private int mFocusBoundRadius;
    private int mAlpha;
    private Rect mPreviewRect;
    private final Drawable mFocusSuccessBound;
    private final Drawable mFocusFailureBound;

    private Paint mFocusCirclePaint;
    private int mFocusingColor;
    private int mFocusSuccessColor;
    private int mFocusFailColor;
    private int mInnerFocusCircleRadius;
    private int mOuterFocusCircleRadius;
    private boolean mShowMetering;
    private int mMeteringCircleRadius;
    private Paint mMeteringPaint;
    private int mMeteringColor;

    private Handler mHandler;
    private boolean mPaused; //MODIFIED by sichao.hu, 2016-04-15,BUG-1951866

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    private static final int MSG_HIDE_FOCUS=0x11001100;

    public FocusOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler(){
            /* MODIFIED-END by sichao.hu,BUG-2743263*/
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_HIDE_FOCUS:
                        setVisibility(INVISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };
        mFocusIndicatorSize = getResources().getDimensionPixelSize(R.dimen.focus_inner_ring_size);
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mFocusSuccessBound = getResources().getDrawable(R.drawable.ic_focus);
        mFocusFailureBound =getResources().getDrawable(R.drawable.ic_focus_fail);
        mFocusBound = mFocusSuccessBound;
        mFocusOuterRingSize = getResources().getDimensionPixelSize(R.dimen.focus_outer_ring_size);

        mInnerFocusCircleRadius = getResources().getDimensionPixelSize(
                R.dimen.focus_inner_circle_radius);
        mOuterFocusCircleRadius = getResources().getDimensionPixelSize(
                R.dimen.focus_outer_circle_radius);
        if (DRAW_FOCUS_UI) {
            mFocusIndicatorSize = mInnerFocusCircleRadius;
            mFocusOuterRingSize = mOuterFocusCircleRadius;
        }
        mMeteringCircleRadius = getResources().getDimensionPixelSize(
                R.dimen.metering_circle_radius);

        mFocusAnimation.setDuration(FOCUS_DURATION_MS);
        mFocusAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFocusBoundRadius = (int) animation.getAnimatedValue();
                mAlpha = (mFocusBoundRadius-mFocusOuterRingSize) * 255 / (mFocusIndicatorSize-mFocusOuterRingSize) ;
                invalidate();
            }
        });


        mFocusRingGradientAnimation=ValueAnimator.ofInt(FOCUS_ALPHA_GRADIENT_START,FOCUS_ALPHA_GRADIENT_END);
        mFocusRingGradientAnimation.setDuration(FOCUS_DURATION_MS);


        mFocusRingGradientAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAlpha=(int) valueAnimator.getAnimatedValue();
                 /* MODIFIED-END by sichao.hu,BUG-2743263*/
                invalidate();
            }
        });

        if (CAPTURE_DEBUG_UI) {
            Resources res = getResources();
            mDebugStartColor = res.getColor(R.color.focus_debug);
            mDebugSuccessColor = res.getColor(R.color.focus_debug_success);
            mDebugFailColor = res.getColor(R.color.focus_debug_fail);
            mDebugTextPaint= new Paint(); // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
            mDebugTextPaint.setColor(res.getColor(R.color.focus_debug_text));
            mDebugTextPaint.setStyle(Paint.Style.FILL);
            mDebugSolidPaint = new Paint();
            mDebugSolidPaint.setColor(res.getColor(R.color.focus_debug));
            mDebugSolidPaint.setAntiAlias(true);
            mDebugSolidPaint.setStyle(Paint.Style.STROKE);
            mDebugSolidPaint.setStrokeWidth(res.getDimension(R.dimen.focus_debug_stroke));
            mDebugCornersPaint = new Paint(mDebugSolidPaint);
            mDebugCornersPaint.setColor(res.getColor(R.color.focus_debug));
            mFocusDebugSolidRect = new Rect();
            mFocusDebugCornersRect = new Rect();
        }

        if (DRAW_FOCUS_UI) {
            Resources res = getResources();
            mFocusingColor = Color.WHITE;
            mFocusSuccessColor = res.getColor(R.color.exposure_sidebar_selected_color);
            mFocusFailColor = Color.RED;
            mFocusCirclePaint = new Paint();
            mFocusCirclePaint.setAntiAlias(true);
            mFocusCirclePaint.setStyle(Paint.Style.STROKE);
            mFocusCirclePaint.setStrokeWidth(res.getDimension(R.dimen.focus_debug_stroke));
        }
        mMeteringPaint = new Paint();
        mMeteringPaint.setAntiAlias(true);
        mMeteringPaint.setStyle(Paint.Style.STROKE);
        mMeteringPaint.setStrokeWidth(getResources().getDimension(R.dimen.focus_debug_stroke));
        mMeteringPaint.setColor(Color.WHITE);
    }

    @Override
    public boolean hasFaces() {
        // TODO: Add face detection support.
        return false;
    }

    @Override
    public void clearFocus() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mShowIndicator = false;
        mFocusRingGradientAnimation.cancel();
        if (CAPTURE_DEBUG_UI) {
            setVisibility(INVISIBLE);
        }
        invalidate();
    }

    @Override
    public void keepFocusFrame() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusRingGradientAnimation.cancel();
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        if (mAlpha != FOCUS_ALPHA_GRADIENT_START) {
            mAlpha = FOCUS_ALPHA_GRADIENT_START;
            /* MODIFIED-END by sichao.hu,BUG-2743263*/
            invalidate();
        }
    }

    @Override
    public void setFocusPosition(int x, int y, boolean isPassiveScan) {
        setFocusPosition(x, y, isPassiveScan, 0, 0);
    }

    @Override
    public void setFocusPosition(int x, int y, boolean isPassiveScan, int aFsize, int aEsize) {
        mIsPassiveScan = isPassiveScan;
        mPositionX = x;
        mPositionY = y;
        mMeteringX = x;
        mMeteringY = y;
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mFocusBoundRadius = mFocusOuterRingSize / 2;
        mBounds.set(x - mFocusBoundRadius, y - mFocusBoundRadius,
        /* MODIFIED-END by sichao.hu,BUG-2743263*/
                x + mFocusBoundRadius, y + mFocusBoundRadius);
        mFocusBound.setBounds(mBounds);

        if (CAPTURE_DEBUG_UI) {
            if (isPassiveScan) {
                // Use AE rect only.
                mFocusDebugSolidRect.setEmpty();
                int avg = (aFsize + aEsize) / 2;
                mFocusDebugCornersRect.set(x - avg / 2, y - avg / 2, x + avg / 2, y + avg / 2);
            } else {
                mFocusDebugSolidRect.set(x - aFsize / 2, y - aFsize / 2, x + aFsize / 2,
                        y + aFsize / 2);
                // If AE region is different size than AF region and active scan.
                if (aFsize != aEsize) {
                    mFocusDebugCornersRect.set(x - aEsize / 2, y - aEsize / 2, x + aEsize / 2,
                            y + aEsize / 2);
                } else {
                    mFocusDebugCornersRect.setEmpty();
                }
            }
            mDebugSolidPaint.setColor(mDebugStartColor);
            mDebugCornersPaint.setColor(mDebugStartColor);
        }

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        invalidate();
    }

    /**
     * This is called in:
     * <ul>
     * <li>API1 non-CAF after autoFocus().</li>
     * <li>API1 CAF mode for onAutoFocusMoving(true).</li>
     * <li>API2 for transition to ACTIVE_SCANNING or PASSIVE_SCANNING.</li>
     * <ul>
     * TODO after PhotoModule/GcamModule deprecation: Do not use this for CAF.
     */
    @Override
    public void onFocusStarted() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusBound = mFocusSuccessBound; // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        if (DRAW_FOCUS_UI) {
            mFocusCirclePaint.setColor(mFocusingColor);
        }
        mShowIndicator = true;
        mFocusAnimation.setIntValues(mFocusOuterRingSize, mFocusIndicatorSize);
        mFocusRingGradientAnimation.cancel();
        mFocusAnimation.start();
        if (CAPTURE_DEBUG_UI) {
            mDebugMessage = null;
        }
    }

    /**
     * This is called in:
     * <ul>
     * <li>API1 non-CAF for onAutoFocus(true).</li>
     * <li>API2 non-CAF for transition to FOCUSED_LOCKED.</li>
     * <li>API1 CAF mode for onAutoFocusMoving(false).</li>
     * <ul>
     * TODO after PhotoModule/GcamModule deprecation: Do not use this for CAF.
     */
    @Override
    public void onFocusSucceeded() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusAnimation.cancel();
        mFocusRingGradientAnimation.cancel();
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mFocusBound = mFocusSuccessBound;
        if (DRAW_FOCUS_UI) {
            mFocusCirclePaint.setColor(mFocusSuccessColor);
        }
        mShowIndicator = false;
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS,FOCUS_HIDE_DELAY);
         /* MODIFIED-END by sichao.hu,BUG-2743263*/
        if (CAPTURE_DEBUG_UI && !mIsPassiveScan) {
            mDebugSolidPaint.setColor(mDebugSuccessColor);
        }
        invalidate();
    }

    @Override
    public void onFocusSucceededAndStay() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusAnimation.cancel();
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mShowIndicator=true;
        mFocusBound=mFocusSuccessBound;
        /* MODIFIED-END by sichao.hu,BUG-2743263*/
        if (DRAW_FOCUS_UI) {
            mFocusCirclePaint.setColor(mFocusSuccessColor);
        }
        mFocusRingGradientAnimation.setStartDelay(FOCUS_STAY_GRADIENT_DELAY);
        mFocusRingGradientAnimation.start();
        invalidate();
    }

    /**
     * This is called in:
     * <ul>
     * <li>API1 non-CAF for onAutoFocus(false).</li>
     * <li>API2 non-CAF for transition to NOT_FOCUSED_LOCKED.</li>
     * <ul>
     */
    @Override
    public void onFocusFailed() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusAnimation.cancel();
        mFocusRingGradientAnimation.cancel();
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mFocusBound = mFocusFailureBound;
        if (DRAW_FOCUS_UI) {
            mFocusCirclePaint.setColor(mFocusFailColor);
        }
        if (CAPTURE_DEBUG_UI && !mIsPassiveScan) {
            mDebugSolidPaint.setColor(mDebugFailColor);
        }
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, FOCUS_HIDE_DELAY);
         /* MODIFIED-END by sichao.hu,BUG-2743263*/
        invalidate();
    }

    @Override
    public void onFocusFailedAndStay() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusAnimation.cancel();
        mShowIndicator = true; // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        mFocusRingGradientAnimation.setStartDelay(FOCUS_STAY_GRADIENT_DELAY);
        mFocusRingGradientAnimation.start();
        if (CAPTURE_DEBUG_UI && !mIsPassiveScan) {
            mDebugSolidPaint.setColor(mDebugFailColor);
        }
        invalidate();
    }


    /*MODIFIED-BEGIN by sichao.hu, 2016-04-15,BUG-1951866*/
    @Override
    public void pauseFocusFrame() {
         /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mPaused = true;
        postInvalidate();
    }

    @Override
    public void resumeFocusFrame() {
        mPaused = false;
        postInvalidate();
        /* MODIFIED-END by sichao.hu,BUG-2743263*/
    }
    /*MODIFIED-END by sichao.hu,BUG-1951866*/

    /**
     * This is called in:
     * API2 for CAF state changes to PASSIVE_FOCUSED or PASSIVE_UNFOCUSED.
     */
    @Override
    public void setPassiveFocusSuccess(boolean success) {
        mFocusAnimation.cancel();
        mFocusRingGradientAnimation.cancel();
        mShowIndicator = false;
        if (CAPTURE_DEBUG_UI) {
            mDebugCornersPaint.setColor(success ? mDebugSuccessColor : mDebugFailColor);
        }
        invalidate();
    }

    @Override
    public void showDebugMessage(String message) {
        if (CAPTURE_DEBUG_UI) {
            mDebugMessage = message;
        }
    }

    @Override
    public void pauseFaceDetection() {
        // TODO: Add face detection support.
    }

    @Override
    public void resumeFaceDetection() {
        // TODO: Add face detection support.
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*MODIFIED-BEGIN by sichao.hu, 2016-04-15,BUG-1951866*/
        if (mPaused) {
            return;
        }
        /*MODIFIED-END by sichao.hu,BUG-1951866*/
        if (mShowIndicator) {
            mBounds.set(mPositionX - mFocusBoundRadius, mPositionY - mFocusBoundRadius, mPositionX + mFocusBoundRadius, mPositionY + mFocusBoundRadius);
            mFocusBound.setBounds(mBounds);
            mFocusBound.setAlpha(mAlpha);

            if ((mPositionY - mFocusBoundRadius) < mPreviewRect.top) {
                canvas.clipRect(mPositionX - mFocusBoundRadius, mPreviewRect.top, mPositionX + mFocusBoundRadius, mPositionY + mFocusBoundRadius);
            }
            if ((mPositionY + mFocusBoundRadius) > mPreviewRect.bottom) {
                canvas.clipRect(mPositionX - mFocusBoundRadius, mPositionY - mFocusBoundRadius, mPositionX + mFocusBoundRadius, mPreviewRect.bottom);
            }

            if (DRAW_FOCUS_UI) {
                mFocusCirclePaint.setAlpha(mAlpha);
                canvas.drawCircle(mPositionX, mPositionY, mFocusBoundRadius, mFocusCirclePaint);
            } else {
                mFocusBound.draw(canvas);
            }
        }

        if (mShowMetering) {
            canvas.drawCircle(mMeteringX, mMeteringY, mMeteringCircleRadius, mMeteringPaint);
        }

        if (CAPTURE_DEBUG_UI) {
            canvas.drawRect(mFocusDebugSolidRect, mDebugSolidPaint);
            float delta = 0.1f * mFocusDebugCornersRect.width();
            float left = mFocusDebugCornersRect.left;
            float top = mFocusDebugCornersRect.top;
            float right = mFocusDebugCornersRect.right;
            float bot = mFocusDebugCornersRect.bottom;

            canvas.drawLines(new float[]{left, top + delta, left, top, left, top, left + delta, top}, mDebugCornersPaint);
            canvas.drawLines(new float[]{right, top + delta, right, top, right, top, right - delta, top}, mDebugCornersPaint);
            canvas.drawLines(new float[]{left, bot - delta, left, bot, left, bot, left + delta, bot}, mDebugCornersPaint);
            canvas.drawLines(new float[]{right, bot - delta, right, bot, right, bot, right - delta, bot}, mDebugCornersPaint);

            if (mDebugMessage != null) {
                mDebugTextPaint.setTextSize(40);
                canvas.drawText(mDebugMessage, left - 4, bot + 44, mDebugTextPaint);
            }
        }
    }

    @Override
    public void setPreviewRect(Rect previewRect) {
        mPreviewRect = previewRect;
    }

    @Override
    public void showMetering() {
        mShowMetering = true;
    }

    @Override
    public void hideMetering() {
        mShowMetering = false;
    }

    @Override
    public boolean isMeteringShowing() {
        return mShowMetering;
    }


    @Override
    public Point getMeteringPosition() {
        return new Point(mMeteringX, mMeteringY);
    }

    @Override
    public void setMeteringPosition(int x, int y) {
        mMeteringX = x;
        mMeteringY = y;
        invalidate();
    }
}
