/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.R;
import cn.tcl.weather.bean.HourWeather;
import cn.tcl.weather.utils.store.FontUtils;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-11.
 * $desc
 */
public class WeatherDetailView extends View {
    private final static float TEXT_SIZE_SP = 11f;
    private final static float LEFT_LINE_SKIP = 22f;//line offset
    //    private final static float LEFT_CONTENT_SKIP = LEFT_LINE_SKIP + 11f;
    private final static float ICON_WIDTH = 16f;// weahter icon width

    private final static int SCROLL_LEFT_SKIP = 3;

    public final static int STATE_NULL = 0;

    public final static int STATE_SCROLLING = 1;

    public final static int STATE_SCROLLING_SCROLL = 3;

    public final static int STATE_SCROLLING_FLING = 5;

    public final static float ITEM_COUNTS = 6f;

    private GestureDetector mDetector;
    private Scroller mScroller;
    private int mMaxScrollingX;

    private int mItemWidth = 200;

    private Rect mDrawingRect = new Rect();// child drawing Rect

    private WeatherDetailAdapter mAdapter;

    private int mSCrollState;

    private float mVelocityX;

    private OnClickListener mClickListener;

    private float mLeftLineSkip;// the frame line x relate padding

    private float mLeftContentSkip;// the content x relate padding


    public WeatherDetailView(Context context) {
        super(context);
        initView();
    }

    public WeatherDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public WeatherDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(21)
    public WeatherDetailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mLeftLineSkip = AndroidUtils.dip2px(getContext(), LEFT_LINE_SKIP);
            float iconWidth = AndroidUtils.dip2px(getContext(), ICON_WIDTH) / 2.0f;
            float txtWidth = AndroidUtils.sp2px(getContext(), TEXT_SIZE_SP);
            mLeftContentSkip = mLeftLineSkip + (iconWidth > txtWidth ? iconWidth : txtWidth);
            mDrawingRect.set((int) (getPaddingLeft() + mLeftLineSkip), 0, getRight(), getBottom() - getPaddingBottom());
            notifyDataSetChanged();
        }
        mViewFrame.layout(changed);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }

    public void setAdpater(WeatherDetailAdapter adpater) {
        mAdapter = adpater;
        if (null != mAdapter) {
            mAdapter.setWeatherDetailView(this);
        }
        notifyDataSetChanged();
    }


    void notifyDataSetChanged() {
        removeCallbacks(mCaculateRunnable);
        postDelayed(mCaculateRunnable, 50);
    }


    public WeatherDetailAdapter getAdapter() {
        return mAdapter;
    }


    private Runnable mCaculateRunnable = new Runnable() {
        @Override
        public void run() {
            if (getHeight() > 0 && null != mAdapter) {
                mItemWidth = (int) ((float) getWidth() / ITEM_COUNTS);
                mMaxScrollingX = (int) (mAdapter.getCount() * mItemWidth - getWidth() + getPaddingLeft() + getPaddingRight() + mLeftContentSkip);
                mMaxScrollingX = mMaxScrollingX > 0 ? mMaxScrollingX + SCROLL_LEFT_SKIP : 0;
                mAdapter.layout(mItemWidth, getHeight() - getPaddingTop() - getPaddingBottom());
                postInvalidate();
            }
        }
    };

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int scrollX = mScroller.getCurrX();

            if (scrollX < 0) {
                scrollX = 0;
            } else if (scrollX > mMaxScrollingX) {
                scrollX = mMaxScrollingX;
            }
            scrollTo(scrollX, 0);
            postInvalidate();
        }
    }

    private void initView() {
        mDetector = new GestureDetector(getContext(), mGenericMotionListener);
        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != mAdapter && mAdapter.getCount() > 0) {
            Rect drawingRect = new Rect();
            drawingRect.set(mDrawingRect);
            drawingRect.offset(getScrollX(), getScrollY());
            final int size = mAdapter.getCount();
            int start = drawingRect.left / mItemWidth - 1;
            int end = drawingRect.right / mItemWidth + 1;
            start = start > 0 ? start : 0;
            end = end > size ? size : end;
            canvas.save();
            mViewFrame.onDraw(canvas);
            canvas.clipRect(drawingRect);
            canvas.translate(getPaddingLeft(), getPaddingTop());
            drawChildren(canvas, start, end);
            canvas.restore();
        }
    }


    private void drawChildren(Canvas canvas, int start, int end) {
        canvas.save();
        canvas.translate(mLeftContentSkip, 0);
        canvas.translate(start * mItemWidth, 0);
        for (int i = start; i < end; i++) {
            mAdapter.getItem(i).draw(canvas, start, i);
            canvas.translate(mItemWidth, 0);
        }
        canvas.restore();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (direction > 0) {
            return getScrollX() < mMaxScrollingX;
        } else {
            return getScrollX() > 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        final int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        if (MotionEvent.ACTION_UP == action && mSCrollState == STATE_SCROLLING_FLING) {
            mScroller.startScroll(getScrollX(), 0, (int) (-mVelocityX / 10), 0, 500);
            postInvalidate();
        }
        return true;
    }


    private boolean isScrolling() {
        return (mSCrollState & STATE_SCROLLING) == STATE_SCROLLING;
    }

    private GestureDetector.OnGestureListener mGenericMotionListener = new GestureDetector.OnGestureListener() {
        private float mScrollX;

        @Override
        public boolean onDown(MotionEvent e) {
            mScrollX = getScrollX();
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            mSCrollState = STATE_NULL;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

//        private boolean isScrolling;

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (null != mClickListener) {
                mClickListener.onClick(WeatherDetailView.this);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScrolling() || Math.abs(distanceX) > Math.abs(distanceY)) {
                mSCrollState = STATE_SCROLLING_SCROLL;
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                scrollToX(mScrollX + e1.getRawX() - e2.getRawX());
            }
            return true;
        }


        private void scrollToX(float scrollX) {
            if (scrollX < 0) {
                scrollX = 0;
            } else if (scrollX > mMaxScrollingX) {
                scrollX = mMaxScrollingX;
            }
            scrollTo((int) scrollX, 0);
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isScrolling() || Math.abs(velocityX) > Math.abs(velocityY)) {
                mSCrollState = STATE_SCROLLING_FLING;
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                scrollToX(mScrollX + e1.getRawX() - e2.getRawX());
                mVelocityX = velocityX;
            }
            return true;
        }
    };


    /**
     * this is for time item
     */
    public static class DrawingItem extends WeatherDetailAdapter.WeatherDetailItem {
        private final static float ICON_SIZE_DP = 16f;
        private final static float TEXT_OFFSET_Y = 7f;
        private final static float ICON_OFFSET_Y = 1f;

        private final static float CIRCLE_RADIUS = 2f;

        private final static float OFFSET_TOP_TXT = 255f / 3.0f;

        protected float mPaddingTop = 0, mPaddingBottom = 0, mMinHeightPercent = 0.0f;
        protected float mIconOffset = 0;
        protected float mTextOffsetY;

        protected int[] mColors = new int[]{0xffb2c4d4, 0xffb2c4d4, 0xDb000000, 0x43000000};//line color, circle color, txt color, unknow

        protected Paint mPaint = new Paint();
        protected float mHeight, mWidth;
        public HourWeather mWeatherStart, mWeatherEnd;
        protected float mPx1, mPx2, mPy1, mPy2;

        protected Bitmap mIcon1, mIcon2;

//        private Bitmap mCenterIcon;

        protected RectF mBitmapRect = new RectF();

        protected float mTextSize;

        protected float mTextPx, mTextPy;

        protected float mCircleRadius;

        protected String[] mCenterTxt;

        public DrawingItem(HourWeather start, HourWeather end) {
            mWeatherStart = start;
            mWeatherEnd = end;
        }

        public void setIcon(Bitmap icon1, Bitmap icon2) {
            mIcon1 = icon1;
            mIcon2 = icon2;
            caculateIconPosition();
        }


//        public void setCenterIcon(Bitmap centerIcon) {
//            mCenterIcon = centerIcon;
//            caculateIconPosition();
//        }

        public void setCenterTxt(String... txt) {
            mCenterTxt = txt;
            caculateIconPosition();
        }

        @Override
        void setParent(WeatherDetailView view) {
            super.setParent(view);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(AndroidUtils.dip2px(mParent.getContext(), 1f));
            mTextSize = AndroidUtils.sp2px(mParent.getContext(), TEXT_SIZE_SP);
            mIconOffset = AndroidUtils.dip2px(mParent.getContext(), -ICON_OFFSET_Y);
        }

        private void caculateTextPosition() {
            mTextOffsetY = AndroidUtils.dip2px(mParent.getContext(), TEXT_OFFSET_Y);

            mPaint.setColor(mColors[2]);
            mPaint.setTextSize(mTextSize);

            final float width = mPaint.measureText(mWeatherEnd.getTimeHHSS());
            mTextPx = mPx2 - (width / 2f);
            mTextPy = mPaddingTop + mHeight + mTextSize * 1.2f + mTextOffsetY;
        }

        private void caculateIconPosition() {
            if (null != mParent) {
                final float iconSize = AndroidUtils.dip2px(mParent.getContext(), ICON_SIZE_DP);
                float top = mPaddingTop - iconSize;
                mBitmapRect.set(0, 0, iconSize, iconSize);
                mBitmapRect.offset(0, top);
            }
        }

        private float getTimepHeight(float temp, float maxValue, float minValue, float caculateHeight, float minHeight) {
            return mHeight - minHeight - (temp - minValue) * caculateHeight / (maxValue - minValue) + mPaddingTop;
        }

        @Override
        protected void layout(int width, int height, float maxValue, float minValue) {
            mWidth = width;
            mHeight = height - mPaddingTop - mPaddingBottom;
            mCircleRadius = AndroidUtils.dip2px(mParent.getContext(), CIRCLE_RADIUS);

            float minHeight = mHeight * mMinHeightPercent;
            float caculateHeight = mHeight - minHeight;

            mPx1 = 0;
            mPx2 = width;
            mPy1 = getTimepHeight(mWeatherStart.temperature, maxValue, minValue, caculateHeight, minHeight);
            mPy2 = getTimepHeight(mWeatherEnd.temperature, maxValue, minValue, caculateHeight, minHeight);

            caculateIconPosition();

            caculateTextPosition();
        }

        @Override
        public float getMaxValue() {
            return Math.max(mWeatherStart.temperature, mWeatherEnd.temperature);
        }

        @Override
        public float getMinValue() {
            return Math.min(mWeatherStart.temperature, mWeatherEnd.temperature);
        }

        @Override
        public int getIconId() {
            return mWeatherStart.icon;
        }


        private int getNextDiffrentIconIndex(int currentIndex) {
            WeatherDetailAdapter adapter = mParent.getAdapter();
            final int size = adapter.getCount();
            for (int i = currentIndex + 1; i < size; i++) {
                if (adapter.getItem(i).getIconId() != getIconId())
                    return i;
            }

            return -1;
        }


        protected void drawContent(Canvas canvas, int start, int current) {
            // draw sunrise and sunset
//            if (null != mCenterTxt && mCenterTxt.length > 1) {// draw sunset and sun rise
//                float txtWidth;
//                float tx, ty;
//
//                txtWidth = mPaint.measureText(mCenterTxt[0]);
//                tx = mPx1 + mPx2 / 2.0f - txtWidth / 2.0f;
//                ty = mPaddingTop - mTextSize + mTextOffsetY / 1.2f;
//
//                float halfWidth = mWidth / 2.0f;
//                float txInScreen = mWidth * current + halfWidth - mParent.getScrollX();// text in screen
//
//                float offsetX = Math.abs(txInScreen - mWidth);
//
//                if (offsetX < mWidth) {// set the alpha to sunrise and sunset
//                    mPaint.setAlpha((int) (offsetX * 255 / mWidth));
//                }
//                canvas.save();
//                canvas.translate(tx, ty);
//                canvas.drawText(mCenterTxt[1], 0, 0, mPaint);
//                canvas.restore();
//                mPaint.setAlpha(255);
//            }
//
//
//            if (null != mIcon2 && !mIcon2.isRecycled()) {
//
//                float px2 = mPx2;
//
//                boolean drawIcon = false;
//
//                WeatherDetailAdapter adapter = mParent.getAdapter();
//
//                int pre = current - 1;// the pre one
//                if (pre < 0) {
//                    pre = 0;
//                }
//
//                WeatherDetailAdapter.WeatherDetailItem preItem = adapter.getItem(pre);
//
//                if (preItem.getIconId() != getIconId()) {
//                    drawIcon = true;
//                } else {
//                    if (start == current) {
//                        drawIcon = true;
//                    }
//                }
//
//                if (drawIcon) {
//                    int alpha = 255;
//                    // no need to offset and alpha
//                    boolean isOffseted = false;
//                    float offset = current * mWidth - mParent.getScrollX();
//                    if (offset < 0) {// if icon can show and offset if less than 0
//                        px2 = mWidth - offset;
//                        isOffseted = true;
//                    }
//
//                    if (isOffseted) {
//                        final int nextDiffIndex = getNextDiffrentIconIndex(current);// get the index of item , which icon is diffrent with current
//                        if (nextDiffIndex > 0) {// if current icon is different from next icon, then you should reset current icon's status
//                            float nextOffset = nextDiffIndex * mWidth - mParent.getScrollX();// becase icon is on the left of item,
//                            if (nextOffset < mWidth) {// caculate the alpha, when offset less than twice width
//                                if (nextOffset > 0) {
//                                    alpha = (int) (255 * nextOffset / mWidth);
//                                } else {
//                                    alpha = 0;
//                                }
//                            }
//                        }
//                    }
//                    if (alpha > 0) {
//                        mPaint.setAlpha(alpha);
//                        canvas.save();
//                        canvas.translate(px2 - mBitmapRect.width() / 2.0f, mIconOffset);
//                        canvas.drawBitmap(mIcon2, null, mBitmapRect, mPaint);
//                        canvas.restore();
//                        mPaint.setAlpha(255);
//                    }
//                }
//            }


            if (null != mIcon1 && !mIcon1.isRecycled()) {

                float px1 = mPx1;

                boolean drawIcon = false;

                WeatherDetailAdapter adapter = mParent.getAdapter();

                int pre = current - 1;// the pre one
                if (pre < 0) {
                    pre = 0;
                }

                WeatherDetailAdapter.WeatherDetailItem preItem = adapter.getItem(pre);

                if (preItem.getIconId() != getIconId()) {
                    drawIcon = true;
                } else if (current == 0) {
                    drawIcon = true;
                }

                if (drawIcon) {
                    int alpha = 255;
                    if (alpha > 0) {
                        mPaint.setAlpha(alpha);
                        canvas.save();
                        canvas.translate(px1 - mBitmapRect.width() / 2.0f, mIconOffset);
                        canvas.drawBitmap(mIcon1, null, mBitmapRect, mPaint);
                        canvas.restore();
                        mPaint.setAlpha(255);
                    }
                }
            }
        }

        @Override
        protected void draw(Canvas canvas, int start, int current) {
            if (mWidth != 0) {
                mPaint.setColor(mColors[0]);
                canvas.drawLine(mPx1, mPy1, mPx2, mPy2, mPaint);

                mPaint.setColor(mColors[1]);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mPx2, mPy2, mCircleRadius, mPaint);

                mPaint.setTypeface(FontUtils.getTxtTypeface(TEXT_SIZE_SP));
                mPaint.setColor(mColors[2]);
                mPaint.setTextSize(mTextSize);
                canvas.drawText(mWeatherEnd.getTimeHHSS(), mTextPx, mTextPy, mPaint);

                if (current == 0) {// if is first
                    mPaint.setColor(mColors[1]);
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(mPx1, mPy1, mCircleRadius, mPaint);

                    mPaint.setColor(mColors[2]);
                    mPaint.setTypeface(FontUtils.getTxtTypeface(TEXT_SIZE_SP));
                    String text = mParent.getContext().getString(R.string.now);
                    float width = mPaint.measureText(text);
                    float textPx = mPx1 - width / 2f;
                    canvas.drawText(text, textPx, mTextPy, mPaint);
                }

                drawContent(canvas, start, current);
            }
        }
    }


    public static class TxtItem extends DrawingItem {

        private final static float TEXT_SIZE = 10f;

        private String mText;

        private Paint mTxtPaint = new Paint();


        public TxtItem(HourWeather start, HourWeather end, String text) {
            super(start, end);
            mText = text;
        }

        @Override
        void setParent(WeatherDetailView view) {
            super.setParent(view);
            mTxtPaint.setAntiAlias(true);
            mTxtPaint.setTextSize(AndroidUtils.sp2px(view.getContext(), TEXT_SIZE));
            mTxtPaint.setColor(0xDB000000);
            mPaint.setTypeface(FontUtils.getTxtTypeface(TEXT_SIZE));
        }

        @Override
        protected void drawContent(Canvas canvas, int start, int current) {

            if (!TextUtils.isEmpty(mText)) {// draw sunset and sun rise
                float txtWidth;
                float tx, ty;

                txtWidth = mPaint.measureText(mText);
                tx = mPx2 - txtWidth / 2.0f;
                ty = mPaddingTop - mTextSize + mTextOffsetY / 1.2f;

                canvas.save();
                canvas.translate(tx, ty);
                canvas.drawText(mText, 0, 0, mPaint);
                canvas.restore();
                mPaint.setAlpha(255);
            }

            if (null != mIcon1 && !mIcon1.isRecycled()) {

                float px1 = mPx1;

                boolean drawIcon = false;

                WeatherDetailAdapter adapter = mParent.getAdapter();

                int pre = current - 1;// the pre one
                if (pre < 0) {
                    pre = 0;
                }

                WeatherDetailAdapter.WeatherDetailItem preItem = adapter.getItem(pre);

                if (preItem.getIconId() != getIconId()) {
                    drawIcon = true;
                } else if (current == 0) {
                    drawIcon = true;
                }

                if (drawIcon) {
                    int alpha = 255;
                    if (alpha > 0) {
                        mPaint.setAlpha(alpha);
                        canvas.save();
                        canvas.translate(px1 - mBitmapRect.width() / 2.0f, mIconOffset);
                        canvas.drawBitmap(mIcon1, null, mBitmapRect, mPaint);
                        canvas.restore();
                        mPaint.setAlpha(255);
                    }
                }
            }
        }
    }


    private ViewFrame mViewFrame = new ViewFrame();


    private class ViewFrame {

        private Paint mPaint = new Paint();

        private float mTextTopX, mTextTopY, mTextBottomX, mTextBottomY;

        private String mMaxTemp;
        private String mMinTemp;

        void layout(boolean changed) {
            if (changed) {
                final float textSize = AndroidUtils.sp2px(getContext(), TEXT_SIZE_SP);
                final float bottom = getHeight() - getPaddingBottom();
                mPaint.setColor(0x4d000000);
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setTextSize(textSize);
                mPaint.setTypeface(Typeface.create("monster-normal", -1));
                mPaint.setStrokeWidth(AndroidUtils.dip2px(getContext(), 0.3f));

                mTextTopY = getPaddingTop() + textSize / 3.0f;
                mTextBottomY = bottom + textSize / 3.0f;
                int maxTemp = (int) mAdapter.getMaxValue();
                int minTemp = (int) mAdapter.getMinValue();
                mMaxTemp = maxTemp + "°";
                mMinTemp = minTemp + "°";
//                mTextTopX = getPaddingLeft() + mPaint.measureText(mMaxTemp);
//                mTextBottomX = getPaddingLeft() + mPaint.measureText(mMinTemp);

                float topWidth = mPaint.measureText(mMaxTemp);
                float bottomWidth = mPaint.measureText(mMinTemp);

                // we should right
                if (topWidth > bottomWidth) {// if top is lager than bottom
                    mTextTopX = getPaddingLeft();
                    mTextBottomX = mTextTopX + topWidth - bottomWidth;
                } else {
                    mTextBottomX = getPaddingLeft();
                    mTextTopX = mTextBottomX + bottomWidth - topWidth;
                }

            }
        }

        void onDraw(Canvas canvas) {
            final float bottom = getHeight() - getPaddingBottom();
            canvas.save();
            canvas.translate(getScrollX(), getScrollY());
            canvas.drawLine(getPaddingLeft() + mLeftLineSkip, getPaddingTop(), getWidth(), getPaddingTop(), mPaint);
            canvas.drawLine(getPaddingLeft() + mLeftLineSkip, bottom, getWidth(), bottom, mPaint);
            canvas.drawText(mMaxTemp, mTextTopX, mTextTopY, mPaint);
            canvas.drawText(mMinTemp, mTextBottomX, mTextBottomY, mPaint);
            canvas.restore();

        }

    }
}
