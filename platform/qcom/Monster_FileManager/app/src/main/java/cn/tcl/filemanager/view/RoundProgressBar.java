/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import cn.tcl.filemanager.R;

/**
 * use porterduff  mask　draw progressbar
 *
 * @author haifeng.tang
 */
public class RoundProgressBar extends View {

    private Paint mPaint;
    private float mRoundWidth;
//    private float mCenter;
//    private int mRadius;


    public static final int TYPE_PHONE = 1;
    public static final int TYPE_SD = 2;
    public static final int TYPE_USB = 3;

    public static final int BIG = 0;
    public static final int SMALL = 1;


    private int mType;

    /**
     * max progress
     */
    private int max;

    /**
     * current progress
     */
    private int progress;


    private Bitmap mBg;
    private Bitmap mFg;

    private float mWidth;

    /**
     * progress style,hollow or solid
     */
    private int roundColor;
    private int roundProgressColor;
    private int style;
    public static final int STROKE = 0;
    public static final int FILL = 1;
    private int mMode;


    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
        mRoundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);
        mTypedArray.recycle();

        //close hardware
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    private void initSrc() {

        mPaint = new Paint();
        mWidth = getLayoutParams().width;
//        mCenter = mWidth / 2; //get round x position
//        mRadius = (int) (mCenter - mRoundWidth);
        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setStrokeWidth(mRoundWidth);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.bg_color));
        mPaint.setFilterBitmap(false);

        switch (mType) {
            case TYPE_PHONE:
                if (mMode == BIG) {
                    mFg = BitmapFactory.decodeResource(getResources(), R.drawable.phone_progressbar_fg);
                    mBg = BitmapFactory.decodeResource(getResources(), R.drawable.phone_progressbar_bg);
                } else {
                    mBg = BitmapFactory.decodeResource(getResources(), R.drawable.phone_small_progressbar_bg);
                    mFg = BitmapFactory.decodeResource(getResources(), R.drawable.phone_small_progressbar_fg);
                }

                break;
            case TYPE_SD:

                if (mMode == BIG) {

                    mFg = BitmapFactory.decodeResource(getResources(), R.drawable.sdcard_progressbar_fg);
                    mBg = BitmapFactory.decodeResource(getResources(), R.drawable.sdcard_progressbar_bg);
                } else {
                    mFg = BitmapFactory.decodeResource(getResources(), R.drawable.sdcard_small_progressbar_fg);
                    mBg = BitmapFactory.decodeResource(getResources(), R.drawable.sdcard_small_progressbar_bg);
                }
                break;
            case TYPE_USB:

                if (mMode == BIG) {
                    mFg = BitmapFactory.decodeResource(getResources(), R.drawable.usb_progressbar_fg);
                    mBg = BitmapFactory.decodeResource(getResources(), R.drawable.usb_progressbar_bg);
                } else {
                    mFg = BitmapFactory.decodeResource(getResources(), R.drawable.usb_small_progressbar_fg);
                    mBg = BitmapFactory.decodeResource(getResources(), R.drawable.usb_small_progressbar_bg);
                }

                break;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(mWidth / mBg.getWidth(), mWidth / mBg.getWidth());
        mBg = Bitmap.createBitmap(mBg, 0, 0, mBg.getWidth(), mBg.getHeight(), matrix, false);
        mFg = Bitmap.createBitmap(mFg, 0, 0, mFg.getWidth(), mFg.getHeight(), matrix, false);

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint == null) {
            initSrc();
        }
        //draw  no use circle
        canvas.drawBitmap(mBg, 0, 0, mPaint);
        mPaint.setXfermode(null);
        canvas.saveLayer(0, 0, mWidth, mWidth, mPaint);
        RectF oval = new RectF(0, 0, mWidth, mWidth);
        canvas.drawArc(oval, -90, 360 * progress / max, true, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mFg, 0, 0, mPaint);
        mPaint.setXfermode(null);

    }

    public void setType(int type, int mode) {
        this.mType = type;
        this.mMode = mode;
    }



    public synchronized int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }


}
