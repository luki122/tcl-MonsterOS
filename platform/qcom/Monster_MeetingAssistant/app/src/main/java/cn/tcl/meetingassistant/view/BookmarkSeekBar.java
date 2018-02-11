/*Copyright (C) 2016 Tcl Corporation Limited*/
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;

public class BookmarkSeekBar extends View {

    private final int DEFAULT_WIDTH = 200;//unit: dp
    private final int DEFAULT_HEIGHT = 23;//unit: dp
    private final int DEFAULT_SLIDE_WIDTH = 2;//unit:dp
    private final int DEFAULT_SLIDE_COLOR = 0x82abd8;
    private final int DEFAULT_SMALL_ROUND_RADIO = 8;//unit:dp
    private final int DEFAULT_SMALL_ROUND_COLOR = 0x82abd8;
    private final int DEFAULT_LARGE_ROUND_RADIO = 11;//unit: dp
    private final int DEFAULT_LARGE_ROUND_COLOR = 0x82abd8;
    private final int DEFAULT_TEXT_COLOR = 0x000000;
    private final float DEFAULT_TEXT_ALPHA = 1f;
    private final float DEFAULT_LARGE_ROUND_ALPHA = 0.3f;
    private final int DEFAULT_TEXT_SIZE = 24;
    private final float DEFAULT_BAR_TEXT_GAP = 0f;
    private final int DEFAULT_BAR_Height = 4;
    private final int DEFAULT_BAR_MARGIN_TOP = 28;//unit:px
    private final int DEFAULT_MARK_BAR_GAP = 1;//unit:px

    private final String TAG = BookmarkSeekBar.class.getSimpleName();

    private int mWidth;
    private int mHeight;

    private int mRealWidth;//real width
    private int mRealHeight;// real height

    private int mSlideHeight;
    private int mSlideColor;
    private int mSmallRoundRadio;
    private int mSmallRoundColor;
    private int mLargeRoundRadio;
    private int mLargeRoundColor;
    private float mLargeRoundAlpha;
    private int mBackground;
    private int mTextColor;
    private float mTextAlpha;
    private float mTextSize;
    private int mMaxProgress;//total progress
    private int mProgress;//current progress
    private long mTotalMillisecond;
    private float mBarTextGap;
    private int mBarHeightPixel;
    private int mBarMarginTop;
    private int mMarkBarGap;
    private String mFont;

    private Bitmap mMarkBitmap;//bitmap for a mark

    private Paint mSlidePaint;
    private Paint mRoundPaint;
    private Paint mDrawablePaint;
    private Paint mBgPaint;
    private Paint mTextPaint;

    private boolean mIsShowRound;//is draw round

    private boolean mSetIsCanUse = true;//when don't use onTouchEvent,setProgress is can used

    private List<Long> mBookmarks = new ArrayList<>();

    private OnProgressChangeListener mOnProgressChangeListener;

    public BookmarkSeekBar(Context context) {
        this(context, null);
    }

    public BookmarkSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BookmarkSeekBar);
        mSlideHeight = dp2px(context, a.getDimension(R.styleable.BookmarkSeekBar_slide_width,
                DEFAULT_SLIDE_WIDTH));
        mSlideColor = a.getColor(R.styleable.BookmarkSeekBar_slide_color, DEFAULT_SLIDE_COLOR);
        mSmallRoundRadio = a.getDimensionPixelSize(R.styleable
                .BookmarkSeekBar_small_round_radio, DEFAULT_SMALL_ROUND_RADIO);
        mSmallRoundColor = a.getColor(R.styleable.BookmarkSeekBar_small_round_color,
                DEFAULT_SMALL_ROUND_COLOR);
        mLargeRoundRadio = a.getDimensionPixelSize(R.styleable
                .BookmarkSeekBar_large_round_radio, DEFAULT_LARGE_ROUND_RADIO);
        mLargeRoundColor = a.getColor(R.styleable.BookmarkSeekBar_large_round_color,
                DEFAULT_LARGE_ROUND_COLOR);
        mLargeRoundAlpha = a.getFloat(R.styleable.BookmarkSeekBar_large_round_alpha,
                DEFAULT_LARGE_ROUND_ALPHA);
        mTextColor =  a.getColor(R.styleable.BookmarkSeekBar_seek_bar_textColor,
                DEFAULT_TEXT_COLOR);
        mTextAlpha = a.getFloat(R.styleable.BookmarkSeekBar_seek_bar_text_alpha,
                DEFAULT_TEXT_ALPHA);
        mTextSize = a.getDimension(R.styleable.BookmarkSeekBar_seek_bar_text_size,
                DEFAULT_TEXT_SIZE);
        mBarTextGap = a.getDimension(R.styleable.BookmarkSeekBar_seek_bar_text_gap,
                DEFAULT_BAR_TEXT_GAP);
        mBarHeightPixel = a.getDimensionPixelSize(R.styleable.BookmarkSeekBar_seek_bar_height,
                DEFAULT_BAR_Height);
        mMarkBitmap = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable
                .BookmarkSeekBar_mark_drawable_resource, R.drawable.ic_voice_marker));
        mMaxProgress = a.getInteger(R.styleable.BookmarkSeekBar_max, 100);
        mBackground = a.getColor(R.styleable.BookmarkSeekBar_seek_bar_background, 0xF7F7F7);
        mFont = a.getString(R.styleable.BookmarkSeekBar_num_text_front);

        mBarMarginTop =  a.getDimensionPixelSize(R.styleable.BookmarkSeekBar_bar_margin_top,
                DEFAULT_BAR_MARGIN_TOP);
        mMarkBarGap = a.getDimensionPixelSize(R.styleable.BookmarkSeekBar_mark_bar_gap,
                DEFAULT_MARK_BAR_GAP);

        a.recycle();
        mSlidePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSlidePaint.setStyle(Paint.Style.FILL);
        mSlidePaint.setColor(mSlideColor);
        mSlidePaint.setAlpha(255);

        mRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mRoundPaint.setStyle(Paint.Style.FILL);
        mRoundPaint.setColor(mSmallRoundColor);
        mRoundPaint.setAlpha(255);

        mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mBgPaint.setColor(mBackground);
        mBgPaint.setAlpha(255);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAlpha((int) (mTextAlpha * 255));
        mTextPaint.setTextSize(mTextSize);
        if(!TextUtils.isEmpty(mFont)){
            mTextPaint.setTypeface(Typeface.create(mFont,Typeface.NORMAL));
        }

        mTotalMillisecond = mMaxProgress;
    }

    public BookmarkSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = measureHandler(widthMeasureSpec, DEFAULT_WIDTH);
        mHeight = measureHandler(heightMeasureSpec, DEFAULT_HEIGHT);
        mRealWidth = mWidth - getPaddingLeft() - getPaddingRight();
        mRealHeight = mHeight - getPaddingTop() - getPaddingBottom();
        if (mRealHeight < (mLargeRoundRadio + mMarkBitmap.getHeight() + mSlideHeight / 2)) {
            mRealHeight = mLargeRoundRadio + mMarkBitmap.getHeight() + mSlideHeight / 2;
            mHeight = mRealHeight + getPaddingTop() + getPaddingBottom();
        }
        if (mRealHeight < mLargeRoundRadio * 2) {
            mRealHeight = mLargeRoundRadio * 2;
            mHeight = mRealHeight + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    private int measureHandler(int measureSpec, int defaultValues) {
        int resultSize = defaultValues;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                resultSize = defaultValues;
                break;
            case MeasureSpec.AT_MOST:
                resultSize = Math.min(defaultValues, size);
                break;
            case MeasureSpec.EXACTLY:
                resultSize = size;
                break;
        }
        return resultSize;
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect bgRect = new Rect(getPaddingLeft(), mBarMarginTop , mWidth - getPaddingRight(), mBarMarginTop + mBarHeightPixel);
        canvas.drawRect(bgRect, mBgPaint);
        MeetingLog.i(TAG,"mBarMarginTop " + mBarMarginTop);
        int progressWidth = (int) (mProgress * 1.0f / mMaxProgress * mRealWidth);
        if(progressWidth >= mRealWidth){
            progressWidth = mRealWidth;
        }
        //MeetingLog.d(TAG, "progressWidth = " + progressWidth);
        Rect slideRect = new Rect();
        slideRect.left = getPaddingLeft();
        slideRect.top = mBarMarginTop;
        slideRect.right = slideRect.left + progressWidth;
        slideRect.bottom = mBarMarginTop + mBarHeightPixel;
        canvas.drawRect(slideRect, mSlidePaint);

        int size = mBookmarks.size();
        for (int i = 0; i < size; i++) {
            Long bookmark = mBookmarks.get(i);
            int bitmapLeft = getPaddingLeft() + (int)
                    (bookmark* 1.0f / mTotalMillisecond * mRealWidth);
            canvas.drawBitmap(mMarkBitmap, bitmapLeft, slideRect.top - mMarkBitmap.getHeight() - mMarkBarGap,
                    mDrawablePaint);
        }
        if (mIsShowRound) {
            canvas.drawCircle(getPaddingLeft() + progressWidth, mBarMarginTop + mBarHeightPixel/2,
                    mSmallRoundRadio, mRoundPaint);
        }

        // draw current time text
        String textString = TimeFormatUtil.getHourMuniteSecondString(mProgress/1000);
        Rect rect = new Rect();
        mTextPaint.getTextBounds(textString, 0, textString.length(), rect);
        float textX = getPaddingLeft();
        float textY = (mHeight + mBarHeightPixel)/2 + mBarTextGap + rect.height();
        canvas.drawText(textString,textX,textY,mTextPaint);

       // draw total time text
        String totalString = TimeFormatUtil.getHourMuniteSecondString(mTotalMillisecond/1000);
        Rect rectTotal = new Rect();
        mTextPaint.getTextBounds(totalString, 0, totalString.length(), rectTotal);
        float textTotalX = mWidth - getPaddingRight() - rectTotal.width();
        float textTotalY = (mHeight + mBarHeightPixel)/2 + mBarTextGap + rectTotal.height();
        canvas.drawText(totalString,textTotalX,textTotalY,mTextPaint);

        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Rect slideRect = new Rect();
        slideRect.left = getPaddingLeft();
        slideRect.top = getPaddingTop();
        slideRect.right = slideRect.left + mRealWidth;
        slideRect.bottom = slideRect.top + mRealHeight;
        int x;
        int y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(null != mOnProgressChangeListener){
                    mOnProgressChangeListener.onProgressPress();
                }
                mSetIsCanUse = false;
                x = (int) event.getX();
                y = (int) event.getY();
//                MeetingLog.d(TAG, "ACTION_DOWN:slideRect: left = " + slideRect.left + ",top = " +
//                        slideRect.top +
//                        ",right = " + slideRect.right
//                        + ",bottom =  " + slideRect.bottom);
//                MeetingLog.d(TAG, "x = " + x + ",y = " + y);
                if (slideRect.contains(x, y)) {
                    mIsShowRound = true;
                    mProgress = (int) ((x - getPaddingLeft()) / (float) mRealWidth * mMaxProgress);
                    if(mProgress<=0){
                        mProgress = 0;
                    }else if(mProgress >= mMaxProgress){
                        mProgress = mMaxProgress;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX();
                y = (int) event.getY();
//                MeetingLog.d(TAG, "ACTION_MOVE slideRect: left = " + slideRect.left + ",top = " +
//                        slideRect.top +
//                        ",right = " + slideRect.right
//                        + ",bottom =  " + slideRect.bottom);
//                MeetingLog.d(TAG, "x = " + x + ",y = " + y);
                mIsShowRound = true;
                mProgress = (int) ((x - getPaddingLeft()) / (float) mRealWidth * mMaxProgress);
                if(mProgress<=0){
                    mProgress = 0;
                }else if(mProgress >= mMaxProgress){
                    mProgress = mMaxProgress;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                x = (int) event.getX();
                mProgress = (int) ((x - getPaddingLeft()) / (float) mRealWidth * mMaxProgress);
                if (mOnProgressChangeListener != null) {
                    mOnProgressChangeListener.onProgressChange(mProgress);
                }
                mIsShowRound = false;
                if(mProgress<=0){
                    mProgress = 0;
                }else if(mProgress >= mMaxProgress){
                    mProgress = mMaxProgress;
                }
                invalidate();
                mSetIsCanUse = true;
//                MeetingLog.d(TAG, "MotionEvent.ACTION_UP");
                break;
        }
        return true;
    }

    /**
     * set Current Progress
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (mSetIsCanUse) {
            this.mProgress = progress;
            //MeetingLog.d(TAG, "setProgress:" + progress);
            invalidate();
        }
    }

    /**
     * set max progress
     *
     * @param max
     */
    public void setMax(int max) {
        this.mMaxProgress = max;
        this.mTotalMillisecond = max;
    }


    public int getCurrentProgress() {
        return mProgress;
    }


    public void setBookmarks(List<Long> bookmarks) {
        this.mBookmarks = bookmarks;
        //this.mTotalMillisecond = totalMillisecond;
        invalidate();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.mOnProgressChangeListener = onProgressChangeListener;
    }

    public interface OnProgressChangeListener {
        void onProgressChange(int progress);
        void onProgressPress();
    }

}
