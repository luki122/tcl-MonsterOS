package com.android.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.android.camera.debug.Log;

/**
 * Created by Sean Scott on 10/22/16.
 */
public abstract class CustomizeScalesBar extends View {

    private static final Log.Tag TAG = new Log.Tag("CustomizeScalesBar");

    protected Context mContext;

    private boolean mGeneralInitialized;
    private boolean mBarInitialized;
    private boolean mScalesInitialized;
    private boolean mPointerInitialized;

    private int mSteps;
    private int mDefaultIndex;
    private int mIndex;

    // The combination scale of main scale and sub scale.
    // e.g. if the value is 1, the scales will be listed as main-sub on the bar, and if the value
    // is 2, the order will be main-sub-sub.
    private int mCombination;

    // If mIsHorizontal is true, the bar is horizontal(Zoom bar); and if mIsHorizontal is false,
    // the bar is vertical(Exposure sidebar).
    private boolean mIsHorizontal;

    // Practical size and margin.
    private int mWidth;
    private int mHeight;
    private int mLeftMargin;
    private int mTopMargin;
    private int mRightMargin;
    private int mBottomMargin;
    // Inner padding of the lines.
    private int mHorizontalPadding;
    private int mVerticalPadding;

    // Scale lines.
    private int mMainScaleHeight;
    private int mSubScaleHeight;
    private int mScaleWidth;
    private int mScaleInterval;

    // The position of scale line.
    private ScaleLine[] mScales;

    protected abstract boolean isScaleLineIllegal(ScaleLine line);

    public class ScaleLine {

        public float startX, startY, endX, endY;

        public ScaleLine(float startX, float startY, float endX, float endY) {
            set(startX, startY, endX, endY);
        }

        public void set(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(32);
            sb.append("ScaleLine("); sb.append(startX); sb.append(", ");
            sb.append(startY); sb.append(" - "); sb.append(endX);
            sb.append(", "); sb.append(endY); sb.append(")");
            return sb.toString();
        }
    }

    // Pointer triangle drawable.
    private Drawable mPointer;
    private int mPointerWidth;
    private int mPointerHeight;
    private int mPointerPadding;

    // Scale line color and pointer color.
    private int mSelectedColor;
    private int mUnselectedColor;
    private int mPointerColor;

    // Now the bar thumb asset(ic_exposure) doesn't match its bound, so the visual size is abnormal.
    // Draw the pointer now.
    private final boolean DRAW_POINTER = true;

    // Bar progress.
    private int MIN;
    private int MAX;
    private int mScaledTouchSlop;
    private float mTouchDownX;
    private float mTouchDownY;
    private boolean mIsDragging;
    private OnScalesBarChangedListener mListener;

    public interface OnScalesBarChangedListener {
        void onStartTrackingTouch(CustomizeScalesBar scalesBar);
        void onStopTrackingTouch(CustomizeScalesBar scalesBar);
        void onProgressChanged(CustomizeScalesBar scalesBar, int value, boolean fromUser);
    }

    public int getSteps() {
        return mSteps;
    }

    public int getScaleCombination() {
        return mCombination;
    }

    public boolean isHorizontal() {
        return mIsHorizontal;
    }

    public int getBarWidth() {
        return mWidth;
    }

    public int getBarHeight() {
        return mHeight;
    }

    public int getLeftMargin() {
        return mLeftMargin;
    }

    public int getTopMargin() {
        return mTopMargin;
    }

    public int getRightMargin() {
        return mRightMargin;
    }

    public int getBottomMargin() {
        return mBottomMargin;
    }

    public int getHorizontalPadding() {
        return mHorizontalPadding;
    }

    public int getVerticalPadding() {
        return mVerticalPadding;
    }

    public int getMainScaleHeight() {
        return mMainScaleHeight;
    }

    public int getSubScaleHeight() {
        return mSubScaleHeight;
    }

    public int getScaleWidth() {
        return mScaleWidth;
    }

    public int getScaleInterval() {
        return mScaleInterval;
    }

    public ScaleLine[] getScales() {
        return mScales;
    }

    public int getPointerWidth() {
        return mPointerWidth;
    }

    public int getPointerHeight() {
        return mPointerHeight;
    }

    public int getPointerPadding() {
        return mPointerPadding;
    }

    public CustomizeScalesBar(Context context) {
        super(context);
        init(context);
    }

    public CustomizeScalesBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomizeScalesBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mContext = context;
        mScaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        setProperty();
        checkInitialization();

        mScales = buildBasicScales();
    }

    // Set all properties.
    protected abstract void setProperty();

    protected void setGeneralProperty(int steps, int defaultIndex, int suggestCombination) {
        mSteps = steps;
        mDefaultIndex = defaultIndex;
        mIndex = mDefaultIndex;
        mCombination = getSuitableCombination(suggestCombination);

        mGeneralInitialized = true;
    }

    private int getSuitableCombination(int suggestCombination) {
        int comb = suggestCombination;
        if (!isMainScale(mSteps, comb)) {
            for (int combination = 1; combination < mSteps; combination++) {
                if (isMainScale(mSteps, combination)) {
                    comb = combination;
                    break;
                }
            }
        }
        return comb;
    }

    public boolean isMainScale(int index) {
        return isMainScale(index, getScaleCombination());
    }

    private boolean isMainScale(int index, int comb) {
        return ((index % (comb + 1)) == 0);
    }

    protected void setBarProperty(boolean isHorizontal, int width, int height,
                                  int leftMargin, int topMargin, int rightMargin, int bottomMargin,
                                  int horizontalPadding, int verticalPadding) {
        mIsHorizontal = isHorizontal;
        mWidth = width;
        mHeight = height;

        mLeftMargin = leftMargin;
        mTopMargin = topMargin;
        mRightMargin = rightMargin;
        mBottomMargin = bottomMargin;

        mHorizontalPadding = horizontalPadding;
        mVerticalPadding = verticalPadding;

        mBarInitialized = true;
    }

    public void setScalesProperty(int mainScaleHeight, int subScaleHeight, int scaleWidth,
                                  int interval, int selectedColor, int unselectedColor) {
        mMainScaleHeight = mainScaleHeight;
        mSubScaleHeight = subScaleHeight;
        mScaleWidth = scaleWidth;
        mScaleInterval = interval;
        mSelectedColor = selectedColor;
        mUnselectedColor = unselectedColor;

        mScalesInitialized = true;
    }

    public void setPointerProperty(Drawable pointer, int pointerWidth, int pointerHeight,
                                   int pointerPadding, int pointerColor) {
        mPointer = pointer;
        mPointerWidth = pointerWidth;
        mPointerHeight = pointerHeight;
        mPointerPadding = pointerPadding;
        mPointerColor = pointerColor;

        mPointerInitialized = true;
    }

    private void checkInitialization() {
        if (!mGeneralInitialized || !mBarInitialized ||
                !mScalesInitialized || !mPointerInitialized) {
            Log.e(TAG, "Initialize not finish");
            throw new IllegalStateException();
        }
    }

    protected abstract ScaleLine[] buildBasicScales();

    protected abstract ScaleLine getScalePosition(int index);

    protected abstract Path getPointerPath(int index);
    protected abstract Rect getPointerBound(int index);

    protected Paint getBasicPaint() {
        Paint mPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mScaleWidth);
        mPaint.setStyle(Paint.Style.FILL);
        return mPaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawScaleLines(canvas);
        drawPointer(canvas);
    }

    protected void drawScaleLines(Canvas canvas) {
        if (mScales == null || mScales.length != (mSteps + 1)) {
            Log.d(TAG, "Empty scales");
            return;
        }

        Paint mPaint = getBasicPaint();

        for (int index = 0; index <= mSteps; index ++) {
            ScaleLine line = getScalePosition(index);
            if (line == null || isScaleLineIllegal(line)) {
                continue;
            }

            if (index > mIndex) {
                mPaint.setColor(mUnselectedColor);
            } else {
                mPaint.setColor(mSelectedColor);
            }

            canvas.drawLine(
                    line.startX, line.startY, line.endX, line.endY, mPaint);
        }
    }

    protected void drawPointer(Canvas canvas) {
        if (DRAW_POINTER) {
            Path trianglePath = getPointerPath(mIndex);
            if (trianglePath != null) {
                Paint mPaint = getBasicPaint();
                mPaint.setColor(mPointerColor);
                canvas.drawPath(trianglePath, mPaint);
            }
            return;
        }

        if (mPointer == null) {
            return;
        }

        Rect bound = getPointerBound(mIndex);
        mPointer.setBounds(bound);
        mPointer.draw(canvas);
    }


    // Bar operation.
    public void setRange(int min, int max) {
        MIN = min;
        MAX = max;
    }

    public void setOnScalesBarChangedListener(OnScalesBarChangedListener listener) {
        mListener = listener;
    }

    public void onStartTrackingTouch() {
        mIsDragging = true;
        if (mListener != null) {
            mListener.onStartTrackingTouch(this);
        }
    }

    public void onStopTrackingTouch() {
        mIsDragging = false;
        if (mListener != null) {
            mListener.onStopTrackingTouch(this);
        }
    }

    public void onProgressChanged(int value, boolean fromUser) {
        if (mListener != null) {
            mListener.onProgressChanged(this, value, fromUser);
        }
    }

    public boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        float scale;
        if (isHorizontal()) {
            final int available = mWidth - mHorizontalPadding * 2;
            final int x = (int) event.getX();
            if (x < mHorizontalPadding) {
                scale = 0.0f;
            } else if (x > mWidth - mHorizontalPadding) {
                scale = 1.0f;
            } else {
                scale = 1.0f * (x - mHorizontalPadding) / available;
            }
        } else {
            final int available = mHeight - mVerticalPadding * 2;
            final int y = (int) event.getY();
            if (y > mHeight - mVerticalPadding) {
                scale = 0.0f;
            } else if (y < mVerticalPadding) {
                scale = 1.0f;
            } else {
                scale = (float)(available - y + mVerticalPadding) / (float)available;
            }
        }

        mIndex = (int) (scale * mSteps);
        invalidate();

        int range = MAX - MIN;
        if (range > 0) {
            int value = MIN + (int)(scale * range);
            onProgressChanged(value, true);
        }
    }

    public void reset() {
        mIndex = mDefaultIndex;
        invalidate();

        int range = MAX - MIN;
        if (range > 0) {
            int value = MIN + (int)(1.0f * mIndex / mSteps * range);
            onProgressChanged(value, false);
        }
    }

    public void setProgress(int value) {
        if (value > MAX || value < MIN) {
            return;
        }
        onProgressChanged(value, false);
        int range = MAX - MIN;
        if (range > 0) {
            float scale = 1.0f * (value - MIN) / range;
            mIndex = (int) (scale * mSteps);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInScrollingContainer()) {
                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                } else {
                    setPressed(true);
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    trackTouchEvent(event);
                } else {
                    final float x = event.getX();
                    final float y = event.getY();
                    if ((isHorizontal() && Math.abs(x - mTouchDownX) > mScaledTouchSlop) ||
                            (!isHorizontal() && Math.abs(y - mTouchDownY) > mScaledTouchSlop)) {
                        setPressed(true);
                        onStartTrackingTouch();
                        trackTouchEvent(event);
                        attemptClaimDrag();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }
}