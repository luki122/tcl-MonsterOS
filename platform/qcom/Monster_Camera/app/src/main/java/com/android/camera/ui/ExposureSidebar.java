package com.android.camera.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
/* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.android.camera.debug.Log;
import com.android.camera.widget.CustomizeScalesBar;
import com.tct.camera.R;

/**
 * Created by Sean Scott on 8/23/16.
 */
public class ExposureSidebar extends CustomizeScalesBar {

    private final static Log.Tag TAG = new Log.Tag("ExposureSidebar");

    private final int STEPS = 12;
    private final int DEFAULT_INDEX = STEPS / 2;
    private final int COMBINATION = 1;

    // Default is right scales bar.
    private boolean mShowOpposite = false;
    private ScaleLine[] mRightScaleLines;
    private ScaleLine[] mLeftScaleLines;
    /* MODIFIED-END by xuan.zhou,BUG-3178291*/

    // Fade in/out animator.
    private final long ANIMATION_DURATION = 300l;
    private final int START = 0;
    // 100 is a magic number here, I wanna set a big value to get high precision.
    private final int END = STEPS * 100;
    private ValueAnimator mAnimator;
    private int mAnimatedValue = START;
    // The target value of current animator.
    private int mTarget;

    // Show sidebar when auto focus but set it disabled when no callback received yet.
    private boolean mReady = false;
    private final float ALPHA_PREPARED = 0.6f;
    private final float ALPHA_READY = 1.0f;

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
    private int mScreenWidth;
    private int mScreenHeight;

    private int mFocusRadius;

    private ExposureSidebarListener mListener;
    public interface ExposureSidebarListener {
        void onExposureCompensationChanged(int value);
    }

    @Override
    protected boolean isScaleLineIllegal(ScaleLine line) {
        if (line == null) {
            return true;
        }
        return (line.startY != line.endY || line.startX >= line.endX);
    }

    public ExposureSidebar(Context context) {
        super(context);
    }

    public ExposureSidebar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExposureSidebar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setProperty() {
        // In GD design the exposure side bar is vertical and has 13 lines, the interval for each is
        // about 11dp, line height 1dp, and if we define vertical 16dp, then the bar height should be
        // 177(Steps*interval + lines*height + 2*padding, or (12*11 + 13*1 + 2*16))dp, likewise if
        // marginTop in visual is 192dp, the practical marginTop should be 176(visual margin - padding,
        // or (192 - 16))dp. Main/sub line width is 8/4dp, in visual the line end margin is 16dp, it's
        // better to set horizontal padding 16dp and practical marginEnd 0dp to get better touch effect.
        // And in visual the pointer end margin is 32dp and pointer width is 9dp, so the bar width
        // should be wider than 41(pointer marginEnd + pointer width + pointer padding, or (32 + 9 + ?))
        // dp. I may like to set the pointer padding the same value with the visual marginEnd of the
        // line, so the bar width is 57(32 + 9 + 16)dp.

        Resources res = mContext.getResources();

        setGeneralProperty(STEPS, DEFAULT_INDEX, COMBINATION);

        int width = res.getDimensionPixelSize(R.dimen.exposure_sidebar_width);
        int height = res.getDimensionPixelSize(R.dimen.exposure_sidebar_height);
        int topMargin = res.getDimensionPixelSize(R.dimen.exposure_sidebar_margin_top);
        int endMargin = res.getDimensionPixelSize(R.dimen.exposure_sidebar_margin_end);
        int horizontalPadding = res.getDimensionPixelSize(R.dimen.exposure_sidebar_padding_horizontal);
        int verticalPadding = res.getDimensionPixelSize(R.dimen.exposure_sidebar_padding_vertical);
        setBarProperty(false, width, height, 0, topMargin, 0, endMargin,
                horizontalPadding, verticalPadding);

        int mainScaleWidth = res.getDimensionPixelSize(R.dimen.exposure_sidebar_main_scale_width);
        int subScaleWidth = res.getDimensionPixelSize(R.dimen.exposure_sidebar_sub_scale_width);
        int scaleHeight = res.getDimensionPixelSize(R.dimen.exposure_sidebar_scale_height);
        int scaleInterval = res.getDimensionPixelSize(R.dimen.exposure_sidebar_scale_interval);
        int selectedColor = res.getColor(R.color.exposure_sidebar_selected_color);
        int unselectedColor = res.getColor(R.color.exposure_sidebar_unselected_color);
        setScalesProperty(mainScaleWidth, subScaleWidth, scaleHeight,
                scaleInterval, selectedColor, unselectedColor);

        Drawable pointer = res.getDrawable(R.drawable.ic_exposure);
        int pointerWidth = res.getDimensionPixelSize(R.dimen.exposure_sidebar_pointer_width);
        int pointerHeight = res.getDimensionPixelSize(R.dimen.exposure_sidebar_pointer_height);
        int pointerPadding = res.getDimensionPixelSize(R.dimen.exposure_sidebar_pointer_padding);
        int pointerColor = res.getColor(R.color.exposure_sidebar_pointer_color);
        setPointerProperty(pointer, pointerWidth, pointerHeight,
                pointerPadding, pointerColor);

        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Point display = new Point();
        windowManager.getDefaultDisplay().getSize(display);
        mScreenWidth = display.x;
        mScreenHeight = display.y;

        mFocusRadius = res.getDimensionPixelSize(R.dimen.focus_inner_ring_size);
        /* MODIFIED-END by xuan.zhou,BUG-3178291*/
    }

    private int getMeasuredDimension(int measureSpec, int value) {
        int ret;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                ret = value;
                break;
            case MeasureSpec.EXACTLY:
                ret = size;
                break;
            case MeasureSpec.AT_MOST:
            default:
                ret = Math.min(value, size);
                break;
        }

        return ret;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        FrameLayout.LayoutParams params  = (FrameLayout.LayoutParams)this.getLayoutParams();
        if (params == null) {
            Log.d(TAG, "LayoutParams not ready yet");
            return;
        }

        /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
        int width = getBarWidth();
        int height = getBarHeight();
        int verticalMargin = getTopMargin();
        int horizontalMargin = getRightMargin();
        width = getMeasuredDimension(widthMeasureSpec, width);
        height = getMeasuredDimension(heightMeasureSpec, height);
        params.width = width;
        params.height = height;
        if (mShowOpposite) {
            params.gravity = Gravity.START;
            params.setMargins(0, verticalMargin, horizontalMargin, 0);
        } else {
            params.gravity = Gravity.END;
            params.setMargins(horizontalMargin, verticalMargin, 0, 0);
        }
        setLayoutParams(params);
        setMeasuredDimension(width, height);
    }

    public RectF getVisualSidebarRectF() {
        return getVisualSidebarRectF(mShowOpposite);
    }

    public RectF getVisualSidebarRectF(boolean isOpposite) {
        int left, top, right, bottom;
        int verticalMargin = getTopMargin();
        int horizontalMargin = getRightMargin();

        if (isOpposite) {
            left = horizontalMargin;
            right = horizontalMargin + getBarWidth() - getPointerPadding();
        } else {
            right = mScreenWidth - horizontalMargin;
            left = mScreenWidth - horizontalMargin - getBarWidth() + getPointerPadding();
        }
        top = verticalMargin + getVerticalPadding();
        bottom = verticalMargin + getBarHeight() - getVerticalPadding();
        return new RectF(left, top, right, bottom);
        /* MODIFIED-END by xuan.zhou,BUG-3178291*/
    }

    private ValueAnimator getAnimator(int progress, final boolean show) {
        ValueAnimator animator = new ValueAnimator();

        int start  = progress;
        int end = show ? END : START;
        animator.setIntValues(start, end);
        mTarget = end;

        int length = Math.abs(end - start);
        float ratio = 1.0f * length / (END - START);
        long time = (long) (ANIMATION_DURATION * ratio);
        animator.setDuration(time);

        animator.setInterpolator(new AccelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedValue = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    ExposureSidebar.this.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    ExposureSidebar.this.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        return animator;
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
    public void fadeIn(int x, int y) {
        checkTouchCoordinate(x, y);
        /* MODIFIED-END by xuan.zhou,BUG-3178291*/
        if (mAnimator != null && mAnimator.isRunning()) {
            if (mTarget == END) {
                // Fade in animator is running already.
                return;
            }
            mAnimator.cancel();
            mAnimator = null;
        }
        if (getVisibility() == VISIBLE && mAnimatedValue == END) {
            return;
        }
        mAnimator = getAnimator(mAnimatedValue, true);
        mAnimator.start();
    }

    public void fadeOut() {
        if (mAnimator != null && mAnimator.isRunning()) {
            if (mTarget == START) {
                // Fade out animator is running already.
                return;
            }
            mAnimator.cancel();
            mAnimator = null;
        }
        if (getVisibility() != VISIBLE && mAnimatedValue == START) {
            return;
        }
        mAnimator = getAnimator(mAnimatedValue, false);
        mAnimator.start();
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
    private void checkTouchCoordinate(int x, int y) {
        boolean isOpposite = isIntersect(getVisualSidebarRectF(false), x, y, mFocusRadius);
        if (mShowOpposite != isOpposite) {
            mShowOpposite = isOpposite;
            directionChanged();
        }
    }

    private boolean isIntersect(RectF barRectF, int x, int y, int radius) {
        return barRectF == null || barRectF.isEmpty() ?  false :
                (Math.abs(barRectF.centerX() - x) <= (barRectF.width()/2 + radius)) &&
                        (Math.abs(barRectF.centerY() - y) <= (barRectF.height()/2 + radius));

    }

    private void directionChanged() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        hide();
    }
    /* MODIFIED-END by xuan.zhou,BUG-3178291*/

    public void show() {
        setVisibility(VISIBLE);
        mAnimatedValue = END;
        invalidate();
    }

    public void hide() {
        mAnimatedValue = START;
        invalidate();
        setVisibility(GONE);
    }

    public void prepared(boolean ready) {
        mReady = ready;
        invalidate();
    }

    public boolean isReady() {
        return mReady;
    }


    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
    @Override
    protected ScaleLine[] buildBasicScales() {
        mRightScaleLines = buildRightScales();
        mLeftScaleLines = buildLeftScales();
        return mShowOpposite ? mLeftScaleLines : mRightScaleLines;
    }

    private ScaleLine[] buildRightScales() {
        int steps = getSteps();
        int mainScaleWidth = getMainScaleHeight();
        int subScaleWidth = getSubScaleHeight();
        int scaleHeight = getScaleWidth();
        int interval = getScaleInterval();

        ScaleLine[] scaleLines = new ScaleLine[steps + 1];

        int endX = getBarWidth() - getHorizontalPadding();
        int startMainX = endX - mainScaleWidth;
        int startSubX = endX - subScaleWidth;
        int y = getBarHeight() - getVerticalPadding() - scaleHeight;

        // The scale lines are layout from bottom to top.
        for (int index = 0; index <= steps; index ++) {
            if (isMainScale(index)) {
                scaleLines[index] = new ScaleLine(startMainX, y, endX, y);
            } else {
                scaleLines[index] = new ScaleLine(startSubX, y, endX, y);
            }
            y -= (interval + scaleHeight);
        }

        return scaleLines;
    }

    private ScaleLine[] buildLeftScales() {
        int steps = getSteps();
        int mainScaleWidth = getMainScaleHeight();
        int subScaleWidth = getSubScaleHeight();
        int scaleHeight = getScaleWidth();
        int interval = getScaleInterval();

        ScaleLine[] scaleLines = new ScaleLine[steps + 1];

        int startX = getHorizontalPadding();
        int endMainX = startX + mainScaleWidth;
        int endSubX = startX + subScaleWidth;
        int y = getBarHeight() - getVerticalPadding() - scaleHeight;

        // The scale lines are layout from bottom to top.
        for (int index = 0; index <= steps; index ++) {
            if (isMainScale(index)) {
                scaleLines[index] = new ScaleLine(startX, y, endMainX, y);
            } else {
                scaleLines[index] = new ScaleLine(startX, y, endSubX, y);
            }
            y -= (interval + scaleHeight);
        }

        return scaleLines;
    }

    // When animated, the positions of lines can be changed.
    @Override
    protected ScaleLine getScalePosition(int index) {
        // The basic scale line.
        ScaleLine line;
        if (mLeftScaleLines == null || mRightScaleLines == null) {
            buildBasicScales();
        }
        line = mShowOpposite ? mLeftScaleLines[index] : mRightScaleLines[index];
        if (line == null || isScaleLineIllegal(line)) {
        /* MODIFIED-END by xuan.zhou,BUG-3178291*/
            return null;
        }

        // The last scale line just get started when the first scale line animates end, so the
        // duration for single scale line is half of the total duration.
        int duration = (END - START) / 2;

        // The delay for the next line.
        int delay = duration / STEPS;

        // The start/end value for single line.
        int start = index * delay;
        int end = start + duration;

        float processingRatio;
        if (mAnimatedValue > end) {
            // The animation of this scale line is finished.
            processingRatio = 1.0f;
        } else if (mAnimatedValue < start) {
            // The animation of this scale line doesn't start yet.
            processingRatio = 0.0f;
        } else {
            processingRatio = 1.0f * (mAnimatedValue - start) / duration;
        }

        // The scale height can be either main or sub, so use (endX - startX) here.
        /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
        float width = (line.endX - line.startX) * processingRatio;
        float startX;
        float endX;

        if (mShowOpposite) {
            startX = getHorizontalPadding() * processingRatio;
            endX = startX + width;
        } else {
            endX = getBarWidth() - getHorizontalPadding() * processingRatio;
            startX = endX - width;
        }

        return new ScaleLine(startX, line.startY, endX, line.endY);
    }

    @Override
    protected Path getPointerPath(int index) {
        // The position for certain pointer triangle is fixed and
        // won't be change because of the animation.
        ScaleLine line = getScalePosition(index);
        if (line == null || isScaleLineIllegal(line)) {
            return null;
        }

        PointF a = new PointF();
        PointF b = new PointF();
        PointF c = new PointF();

        int pointerWidth = getPointerWidth();
        int pointerHeight = getPointerHeight();
        int pointerPadding = getPointerPadding();

        if (mShowOpposite) {
            a.x = getBarWidth() - pointerPadding;
            b.x = a.x;
            c.x = a.x - pointerWidth;
        } else {
            a.x = pointerPadding;
            b.x = a.x;
            c.x = a.x + pointerWidth;
        }
        c.y = line.startY;
        a.y = c.y - pointerHeight / 2;
        b.y = c.y + pointerHeight / 2;


        Path trianglePath = new Path();
        trianglePath.moveTo(a.x, a.y);
        trianglePath.lineTo(b.x, b.y);
        trianglePath.lineTo(c.x, c.y);
        trianglePath.close();
        return trianglePath;
    }

    @Override
    protected Rect getPointerBound(int index) {
        ScaleLine line = getScalePosition(index);
        if (line == null || isScaleLineIllegal(line)) {
            return new Rect();
        }
        int left, top, right, bottom;

        left = getPointerPadding();
        top = (int) (line.startY - getPointerHeight()/2);
        right = left + getPointerWidth();
        bottom = top + getPointerHeight();
        return new Rect(left, top, right, bottom);
    }

    @Override
    /* MODIFIED-END by xuan.zhou,BUG-3178291*/
    protected void drawPointer(Canvas canvas) {
        if (!isReady() || mAnimatedValue != END) {
            return;
        }

        super.drawPointer(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            return;
        }

        this.setAlpha(isReady() ? ALPHA_READY : ALPHA_PREPARED);
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAnimator != null && mAnimator.isRunning()) {
            return false;
        }

        // I don't want to use isEnabled() here.
        if (!isReady()) {
            return false;
        }

        return super.onTouchEvent(event);
    }

    public void loadExposureCompensation(int min, int max) {
        setRange(min, max);
    }

    // I don't need the onStart/StopTrackingTouch event, so only override onProgressChanged here.
    public void setExposureSidebarListener(ExposureSidebarListener listener) {
        mListener = listener;
    }

    @Override
    public void onProgressChanged(int value, boolean fromUser) {
        if (mListener != null) {
            mListener.onExposureCompensationChanged(value);
        }
    }
}
