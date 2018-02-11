/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.android.gallery3d.R;

/**
 * The time bar view, which includes the current and total time, the progress
 * bar, and the scrubber.
 */
public class MovieTimeBar extends TimeBar {

    public interface Listener {
        void onScrubbingStart();

        void onScrubbingMove(int time);

        void onScrubbingEnd(int time, int start, int end);
    }

    // Padding around the scrubber to increase its touch target
    private static final int SCRUBBER_PADDING_IN_DP = 1;
    private static final float SCRUBBER_HEIGHT_IN_DP = 1.5f;

    // The total padding, top plus bottom
    private static final int V_PADDING_IN_DP = 30;

    // TCL BaiYuan on 2016.10.18
    // Original:
    /*
    private static final int TEXT_SIZE_IN_DP = 14;
    */
    // Modify To:
    private static final int TEXT_SIZE_IN_SP = 11;
    // TCL BaiYuan on 2016.10.18
    
    //protected Listener mListener;

    // the bars we use for displaying the progress
    protected final Rect mProgressBar;
    protected final Rect mPlayedBar;

    protected final Paint mProgressPaint;
    protected final Paint mPlayedPaint;
    protected final Paint mTimeTextPaint;

    protected final Bitmap mScrubber;
    protected int mScrubberPadding; // adds some touch tolerance around the scrubber

    protected int mScrubberLeft;
    protected int mScrubberTop;
    protected int mScrubberCorrection;
    protected boolean mScrubbing;
    protected boolean mShowTimes;
    protected boolean mShowScrubber;

    protected int mTotalTime;
    protected int mCurrentTime;

    protected final Rect mTimeBounds;

    protected int mVPaddingInPx;
    
    private int mTimeBarHeight;
    private FontMetricsInt mFontMetrics;
    private int mScrubberHeight;
    // TCL ShenQianfeng Begin on 2016.08.05
    
    // // TCL BaiYuan Begin on 2016.10.19
    private static final int PROGRESS_BAR_IN_DP = 208 ; 
    private static final int PROGRESS_BAR_IN_DP_HORIZONTAL = 467;
    private int mProgressBarWidth;
    private static final int TIME_MARGIN_IN_DP = 9;
    private int mTimeBarMargin;
    private int PROGRESS_BAR_WIDTH_LANDSPACE;
    private int PROGRESS_BAR_WIDTH_PORTRAIT;
    // TCL BaiYuan End on 2016.10.19

    public MovieTimeBar(Context context) {
        this(context, null);
    }
    
    /*
    public TimeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    */

    public MovieTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mTimeBarHeight = context.getResources().getDimensionPixelSize(R.dimen.mst_movie_bottom_bar_height);
        
        mShowTimes = true;
        mShowScrubber = true;

        mProgressBar = new Rect();
        mPlayedBar = new Rect();

        mProgressPaint = new Paint();
        mProgressPaint.setColor(0xFF808080);
        mPlayedPaint = new Paint();
        // TCL BaiYuan Begin on 2016.10.17
        // Original:
        /*
        mPlayedPaint.setColor(0xFFFFFFFF);
        */
        // Modify To:
        mPlayedPaint.setColor(0xFFFBE896);
        // TCL BaiYuan End on 2016.10.17
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        // TCL BaiYuan Begin on 2016.10.17
        // Original:
        /*
        float textSizeInPx = metrics.density * TEXT_SIZE_IN_DP;
        */
        float textSizeInPx = metrics.scaledDensity * TEXT_SIZE_IN_SP + 0.5f;
        // Modify To:
        // TCL BaiYuan End on 2016.10.17
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setColor(0x66FFFFFF);
        mTimeTextPaint.setTextSize(textSizeInPx);
        mTimeTextPaint.setTextAlign(Paint.Align.CENTER);

        mTimeBounds = new Rect();
        mTimeTextPaint.getTextBounds("0:00:00", 0, 7, mTimeBounds);
        // TCL BaiYuan Begin on 2016.10.17
        // Original:
        /*
        mScrubber = BitmapFactory.decodeResource(getResources(), R.drawable.scrubber_knob);
        */
        // Modify To:
        mScrubber = BitmapFactory.decodeResource(getResources(), R.drawable.scrubber_knob_video);
        // TCL BaiYuan End on 2016.10.17
        mScrubberPadding = 0;//(int) (metrics.density * SCRUBBER_PADDING_IN_DP);
        mScrubberHeight = (int) (metrics.density * SCRUBBER_HEIGHT_IN_DP);

        mVPaddingInPx = 0;//(int) (metrics.density * V_PADDING_IN_DP);
        
        mFontMetrics = mTimeTextPaint.getFontMetricsInt();
        // TCL BaiYuan Begin on 2016.10.19
        PROGRESS_BAR_WIDTH_PORTRAIT = (int) (metrics.densityDpi * PROGRESS_BAR_IN_DP / 160);
        PROGRESS_BAR_WIDTH_LANDSPACE = (int) (metrics.densityDpi * PROGRESS_BAR_IN_DP_HORIZONTAL / 160);
        if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
            mProgressBarWidth = PROGRESS_BAR_WIDTH_PORTRAIT;
        }else{
            mProgressBarWidth = PROGRESS_BAR_WIDTH_LANDSPACE;
        }
        mTimeBarMargin = (int) (metrics.densityDpi * TIME_MARGIN_IN_DP / 160);
        // TCL BaiYuan End on 2016.10.19
    }
    // TCL ShenQianfeng End on 2016.08.05

    // TCL ShenQianfeng Begin on 2016.08.05
//    @Override
//    public void setScrubbingListener(Listener listen) {
//        super.setScrubbingListener(listen);
//        mListener = listen;
//    }
//    
    private int getTextWidth(Paint paint, String text) {
        float width = paint.measureText(text);
        return (int)width;
    }
    
    
    private int getBaseline(FontMetricsInt fontMetricsInt) {
        return fontMetricsInt.bottom - fontMetricsInt.descent;
    }
    
    private int getTextHeight(Paint paint, String text) {
        FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        return fontMetricsInt.bottom - fontMetricsInt.top;
    }
    // TCL ShenQianfeng End on 2016.08.05

    private void update() {
        mPlayedBar.set(mProgressBar);

        if (mTotalTime > 0) {
            mPlayedBar.right =
                    // TCL BaiYuan Begin on 2016.11.16
                    // Original:
                    /*
                    mPlayedBar.left + (int) ((mProgressBar.width() * (long) mCurrentTime) / mTotalTime);
                    */
                    // Modify To:
                    mPlayedBar.left + (int) ((mProgressBar.width() * (double) mCurrentTime) / (double)mTotalTime);
            // TCL BaiYuan Begin on 2016.11.16
        } else {
            mPlayedBar.right = mProgressBar.left;
        }

        if (!mScrubbing) {
            mScrubberLeft = mPlayedBar.right - mScrubber.getWidth() / 2;
        }
        invalidate();
    }

    /**
     * @return the preferred height of this view, including invisible padding
     */
    public int getPreferredHeight() {
        return mTimeBounds.height() + mVPaddingInPx + mScrubberPadding;
    }

    /**
     * @return the height of the time bar, excluding invisible padding
     */
    public int getBarHeight() {
        return mTimeBounds.height() + mVPaddingInPx;
    }

    public void setTime(int currentTime, int totalTime,
            int trimStartTime, int trimEndTime) {
        if (mCurrentTime == currentTime && mTotalTime == totalTime) {
            return;
        }
        mCurrentTime = currentTime;
        mTotalTime = totalTime;
        update();
    }

    private boolean inScrubber(float x, float y) {
        int scrubberRight = mScrubberLeft + mScrubber.getWidth();
        int scrubberBottom = mScrubberTop + mScrubber.getHeight();
        return mScrubberLeft - mScrubberPadding < x && x < scrubberRight + mScrubberPadding
                && mScrubberTop - mScrubberPadding < y && y < scrubberBottom + mScrubberPadding;
    }

    private void clampScrubber() {
        int half = mScrubber.getWidth() / 2;
        int max = mProgressBar.right - half;
        int min = mProgressBar.left - half;
        mScrubberLeft = Math.min(max, Math.max(min, mScrubberLeft));
    }

    private int getScrubberTime() {
        return (int) ((long) (mScrubberLeft + mScrubber.getWidth() / 2 - mProgressBar.left)
                * mTotalTime / mProgressBar.width());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
         int w = r - l;
        int h = b - t;
        if (!mShowTimes && !mShowScrubber) {
            mProgressBar.set(0, 0, w, h);
        } else {
            // TCL BaiYuan Begin End 2016.10.19 
            // Original:
            /*
            int margin = mScrubber.getWidth() / 3;
            */
            // Modify To:
            int margin = mTimeBarMargin;
            // TCL BaiYuan Begin on 2016.10.19
            if (mShowTimes) {
                margin += mTimeBounds.width();
            }
            int progressY = (h - mScrubberHeight)  / 2;
            mScrubberTop = progressY - mScrubber.getHeight() / 2 + 1;
            // TCL BaiYuan Begin End 2016.10.19 
            // Original:
            /*
            mProgressBar.set(
                    getPaddingLeft() + margin, progressY,
                    w - getPaddingRight() - margin, progressY + mScrubberHeight);
            */
            // Modify To:
            mProgressBar.set(
                    getPaddingLeft() + margin, progressY,
                    getPaddingLeft() + margin + mProgressBarWidth, progressY + mScrubberHeight);
            // TCL BaiYuan Begin on 2016.10.19
        }
        update();
    }
    
    /*
     * 
             int left = (viewWidth - fixedDescWidth) / 2;
        int top = (viewHeight - secondLineHeight - mLineSpacing - fixedDescHeight) / 2;
        int right = left + fixedDescWidth;
        int bottom = top + fixedDescHeight;
        canvas.drawText(mFixedDescriptionText, 
                (left + right) / 2, 
                (top + bottom - mFontMetricsOfFixedDesc.bottom - mFontMetricsOfFixedDesc.top) / 2, 
                mFixedTextPaint);
     */

    @Override
    protected void onDraw(Canvas canvas) {
        // draw progress bars
        canvas.drawRect(mProgressBar, mProgressPaint);
        canvas.drawRect(mPlayedBar, mPlayedPaint);

        // draw scrubber and timers
        if (mShowScrubber) {
            canvas.drawBitmap(mScrubber, mScrubberLeft, mScrubberTop, null);
        }
        if (mShowTimes) {
            String timeText = stringForTime(mCurrentTime);
            // TCL BaiYuan Begin on 2016.11.16
            /*
            int textWidth = getTextWidth(mTimeTextPaint, timeText);
            int textHeight = getTextHeight(mTimeTextPaint, timeText);
            */
            // TCL BaiYuan End on 2016.11.16

            int left = getPaddingLeft();
            int top = 0;
            int bottom = mTimeBarHeight;
            // TCL BaiYuan Begin End 2016.10.19 
            // Original:
            /*
            canvas.drawText(timeText, 
                    (left + right) / 2, 
                    (top + bottom - mFontMetrics.bottom - mFontMetrics.top) / 2, 
                    mTimeTextPaint);
            */
            // Modify To:
            canvas.drawText(timeText, 
                    left + mTimeBounds.width() / 2 ,
                    (top + bottom - mFontMetrics.bottom - mFontMetrics.top) / 2, 
                    mTimeTextPaint);
            // TCL BaiYuan End on 2016.10.19
            // TCL BaiYuan Begin on 2016.11.16
            // Original:
            /*
            timeText = stringForTime(mTotalTime);
            left = mProgressBar.right + mTimeBarMargin;
            */
            // Modify To:
            timeText = stringForTime(mTotalTime);
            left = mProgressBar.right + mTimeBarMargin;
            // TCL BaiYuan End on 2016.11.16
            // TCL BaiYuan Begin 2016.10.19 
            // Original:
            /*
            canvas.drawText(timeText, 
                    (left + right) / 2, 
                    (top + bottom - mFontMetrics.bottom - mFontMetrics.top) / 2, 
                    mTimeTextPaint);
            */
            // Modify To:
            canvas.drawText(timeText, 
                    left + mTimeBounds.width() / 2,
                    (top + bottom - mFontMetrics.bottom - mFontMetrics.top) / 2, 
                    mTimeTextPaint);
            // TCL BaiYuan End on 2016.10.19
            /*
            canvas.drawText(
                    timeText,
                            mTimeBounds.width() / 2 + getPaddingLeft(),
                            //mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + 1,
                            mTimeBarHeight / 2 - mFontMetricsOfFixedDesc.bottom - mFontMetricsOfFixedDesc.top,
                    mTimeTextPaint);
            canvas.drawText(
                    stringForTime(mTotalTime),
                            getWidth() - getPaddingRight() - mTimeBounds.width() / 2,
                            mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + 1,
                    mTimeTextPaint);
                    */
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mShowScrubber) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mScrubberCorrection = inScrubber(x, y)
                            ? x - mScrubberLeft
                            : mScrubber.getWidth() / 2;
                    mScrubbing = true;
                    mListener.onScrubbingStart();
                }
                // fall-through
                case MotionEvent.ACTION_MOVE: {
                    mScrubberLeft = x - mScrubberCorrection;
                    clampScrubber();
                    mCurrentTime = getScrubberTime();
                    // TCL BaiYuan Begin on 2016.10.18
                    mPlayedBar.right =  mPlayedBar.left + (int) ((mProgressBar.width() * (long) mCurrentTime) / mTotalTime);
                    // TCL BaiYuan End on 2016.10.18
                    mListener.onScrubbingMove(mCurrentTime);
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    mListener.onScrubbingEnd(getScrubberTime(), 0, 0);
                    mScrubbing = false;
                    return true;
                }
            }
        }
        return false;
    }

    // TCL BaiYuan Begin on 2016.11.16
    // Original:
    /*
    protected String stringForTime(long millis) {
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
    }
    */
    // Modify:
    protected String stringForTime(long millis) {
        if (millis % 1000 >= 500) {
            millis += 1000;
        }
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
    }
    // TCL BaiYuan Begin on 2016.11.16

    public void setSeekable(boolean canSeek) {
        mShowScrubber = canSeek;
    }

    // TCL BaiYuan Begin on 2016.10.28
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation) {
            mProgressBarWidth = PROGRESS_BAR_WIDTH_LANDSPACE;
        }else if(Configuration.ORIENTATION_PORTRAIT == newConfig.orientation){
            mProgressBarWidth = PROGRESS_BAR_WIDTH_PORTRAIT;
        }
        mProgressBar.right = mProgressBar.left + mProgressBarWidth;
    }
    // TCL BaiYuan End on 2016.10.28
}
