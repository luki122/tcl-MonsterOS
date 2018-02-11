/* Copyright (C) 2016 Tcl Corporation Limited*/
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;

public class TimerView extends View {

    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int DEFAULT_TEXT_SIZE = 24;
    private static final int DEFAULT_TEXT_COLOR = 0x000000;
    private static final float DEFAULT_TEXT_ALPHA = 1.0f;
    private static final int DEFAULT_INTERVAL = 5;
    private static final int DEFAULT_SHOW_TOTAL_SECOND = 3;
    private static final int DEFAILT_MARK_TEXT_GAP = 20;
    private final String TAG = TimerView.class.getSimpleName();
    private int mWidth;//View's width
    private int mHeight;//View's Height;

    private float mDistancePerSecond;
    private int mTotalSecond;//total second for can show

    private long mDuration = 0;//current mDuration

    private Paint mTextPaint;//text paint
    private float mTextSize;
    private int mTextColor;
    private float mTextAlpha;
    private int mInterval;//Draw the number of seconds interval
    private int mIconId;

    private boolean isNeedDrawDuration = false;
    private boolean isNeedDrawMarkTime = true;
    private boolean isMarkAppearCenter = false;

    private Bitmap mBitmap;
    private Paint mBitmapPaint;

    private String mFont;
    private int mMarkTextGap;


    private List<Long> mBookmarks = new ArrayList<>();

    public TimerView(Context context) {
        this(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimerView);
        mTextSize = a.getDimension(R.styleable.TimerView_timer_view_text_size,
                DEFAULT_TEXT_SIZE);
        mTextColor = a.getColor(R.styleable.TimerView_timer_view_text_color, DEFAULT_TEXT_COLOR);
        mTextAlpha = a.getFloat(R.styleable.TimerView_timer_view_text_alpha, DEFAULT_TEXT_ALPHA);
        mInterval = a.getInt(R.styleable.TimerView_timer_view_interval, DEFAULT_INTERVAL);
        mTotalSecond = a.getInt(R.styleable.TimerView_timer_view_total_second,
                DEFAULT_SHOW_TOTAL_SECOND);
        mIconId = a.getResourceId(R.styleable.TimerView_timer_view_bookmark_icon, R.drawable
                .ic_voice_marker);
        mFont = a.getString(R.styleable.TimerView_timer_view_font);
        mMarkTextGap = a.getDimensionPixelSize(R.styleable.TimerView_timer_mark_text_gap,DEFAILT_MARK_TEXT_GAP);
        a.recycle();

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAlpha((int) (mTextAlpha * 255));
        mTextPaint.setTextSize(mTextSize);
        if(!TextUtils.isEmpty(mFont)){
            mTextPaint.setTypeface(Typeface.create(mFont,Typeface.NORMAL));
        }
        Resources resources = getResources();
        mBitmap = BitmapFactory.decodeResource(resources, mIconId);
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public void setNeedDrawDuration(boolean isNeedDrawDuration){
        this.isNeedDrawDuration = isNeedDrawDuration;
    }

    public void setNeedDrawMarkTime(boolean isNeedDrawMarkTime){
        this.isNeedDrawMarkTime = isNeedDrawMarkTime;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = measureHandler(widthMeasureSpec, DEFAULT_WIDTH);
        mHeight = measureHandler(heightMeasureSpec, DEFAULT_HEIGHT);
        setMeasuredDimension(mWidth, mHeight);
        mDistancePerSecond = mWidth * 1.0f / mTotalSecond;
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

    public void setDuration(long duration) {
        this.mDuration = duration;
        invalidate();
    }

    public void reset() {
        this.mDuration = 0;
        mBookmarks.clear();
        invalidate();
    }

    public void addMark(Long bookmark) {
        mBookmarks.add(bookmark);
        invalidate();
    }

    public void setMarks(List<Long> bookmarks) {
        this.mBookmarks = bookmarks;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mDuration == 0) {
            return;
        }
        if(isMarkAppearCenter){
            canvas.translate(mWidth/2, 0);
        }else {
            canvas.translate(mWidth, 0);
        }
        canvas.save();
        long shouldMaxDrawDuration = mDuration + mInterval * 1000;
        long shouldMinDrawDuration = mDuration - mTotalSecond * 1000 - mInterval * 1000;
        int maxSecond = (int) (shouldMaxDrawDuration / 1000);
        int minSecond = (int) (shouldMinDrawDuration / 1000) - 1;
        if (minSecond < 0) {
            minSecond = 0;
        }
        float totalWidth = mDuration * 1.0f / 1000 * mDistancePerSecond;
        int size = mBookmarks.size();
        for (int i = 0; i < size; i++) {
            long markDuration = mBookmarks.get(i);
            if (markDuration <= shouldMaxDrawDuration && markDuration >= shouldMinDrawDuration) {
                float x = (markDuration - mDuration) * 1.0f / 1000 * mDistancePerSecond - mBitmap
                        .getWidth() / 2;
                float y = mHeight - mBitmap.getHeight();
                canvas.drawBitmap(mBitmap, x, y, mBitmapPaint);
                // draw mark time
                if(isNeedDrawMarkTime){
                    String textString = TimeFormatUtil.getHourMuniteSecondString(markDuration/1000);
                    Rect rect = new Rect();
                    mTextPaint.getTextBounds(textString, 0, textString.length(), rect);
                    float xText = (markDuration - mDuration) * 1.0f / 1000 * mDistancePerSecond - rect.width()
                            / 2;
                    float yText = mHeight - mBitmap.getHeight() - mMarkTextGap;
                    canvas.drawText(textString, xText, yText, mTextPaint);
                }
            }
        }

        if(isNeedDrawDuration){
            for (int i = maxSecond; i >= minSecond; i--) {
                if ((i % 5 == 0 && i != 0) || i == 1) {
                    String textString = TimeFormatUtil.getHourMuniteSecondString(i);
                    Rect rect = new Rect();
                    mTextPaint.getTextBounds(textString, 0, textString.length(), rect);
                    float intervalDistance = i * mDistancePerSecond - totalWidth;
                    float x = intervalDistance - rect.width() / 2;
                    canvas.drawText(textString, x, rect.height(), mTextPaint);
                }
            }
        }
        canvas.restore();
        super.onDraw(canvas);
    }

    public void setMarkAppearCenter(boolean isMarkAppearCenter) {
        this.isMarkAppearCenter = isMarkAppearCenter;
    }
}
