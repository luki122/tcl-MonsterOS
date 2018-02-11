/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageButton;

import cn.tcl.meetingassistant.R;

/**
 * Created on 16-9-9.
 */
public class AudioTextDeleteButton extends ImageButton {

    private final int CYCLE_DEFAULT_COLOR = 0xFF000000;
    private final int CYCLE_DEFAULT_DIMEN = 3;
    private final int CYCLE_DEFAULT_BG_COLOR = 0xFFFFFF;

    private int mCycleColor;
    private int mCycleBgColor;
    private float mCycleDimen;

    private Paint mCyclePaint;
    private Paint mCycleBgPaint;

    private float mProgress = 0;

    private RectF mRectF;

    public AudioTextDeleteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AudioTextDeleteButton);
        mCycleColor = a.getColor(R.styleable.AudioTextDeleteButton_audio_text_btn_cycle_color,
                CYCLE_DEFAULT_COLOR);
        mCycleBgColor = a.getColor(R.styleable.AudioTextDeleteButton_audio_text_btn_cycle_bg_color,
                CYCLE_DEFAULT_BG_COLOR);
        mCycleDimen = a.getDimension(R.styleable.AudioTextDeleteButton_audio_text_btn_cycle_dimension,
                CYCLE_DEFAULT_DIMEN);

        mCyclePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCyclePaint.setStyle(Paint.Style.STROKE);
        mCyclePaint.setStrokeWidth(mCycleDimen);
        mCyclePaint.setColor(mCycleColor);

        mCycleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCycleBgPaint.setStyle(Paint.Style.STROKE);
        mCycleBgPaint.setStrokeWidth(mCycleDimen);
        mCycleBgPaint.setColor(mCycleBgColor);

        a.recycle();

    }

    public AudioTextDeleteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public AudioTextDeleteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF = new RectF();
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        mRectF.left = mCycleDimen / 2;
        mRectF.top = mCycleDimen / 2;
        mRectF.right = width - mCycleDimen / 2;
        mRectF.bottom = height - mCycleDimen / 2;

        // draw cycle backgound
        canvas.drawArc(mRectF, -90, 360, false, mCycleBgPaint);

        canvas.drawArc(mRectF, -90, (mProgress / 100) * 360, false, mCyclePaint);
    }

    public void setProgress(float progress){
        mProgress = progress;
        invalidate();
    }
}
