package cn.tcl.transfer.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import cn.tcl.transfer.R;

public class GearView extends View {

    private final String TAG = GearView.class.getSimpleName();

    private int mWidth;
    private int mHeight;

    private Bitmap mTopRightBitmap;
    private Bitmap mTopLeftBitmap;
    private Bitmap mBottomLeftBitmap;
    private Bitmap mBottomRightBitmap;

    private Paint mPaint;

    private Matrix mTopRightBitmapMatrix;
    private Matrix mTopLeftBitmapMatrix;
    private Matrix mBottomLeftBitmapMatrix;
    private Matrix mBottomRightBitmapMatrix;

    private float centerX;
    private float centerY;

    private PaintFlagsDrawFilter pfd;

    private Paint mShadePaint;
    private Rect mShadeRect;

    public GearView(Context context) {
        this(context, null);
    }

    public GearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GearView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        float left;
        float top;
        centerX = mWidth / 2f;
        centerY = mHeight / 2f;
        left = -mTopRightBitmap.getWidth() / 2f;
        top = -mTopRightBitmap.getHeight() - 5f;
        mTopRightBitmapMatrix.setTranslate(left, top);

        left = -mBottomLeftBitmap.getWidth() / 2f;
        top = 5f;
        mBottomLeftBitmapMatrix.setTranslate(left, top);

        left = -mTopLeftBitmap.getWidth() - 12f;
        top = -mTopLeftBitmap.getHeight() / 2f;
        mTopLeftBitmapMatrix.setTranslate(left, top);

        left = +15f;
        top = -mBottomRightBitmap.getHeight() / 2;
        mBottomRightBitmapMatrix.setTranslate(left, top);

        mShadeRect.left = 0;
        mShadeRect.top = 0;
        mShadeRect.right = getMeasuredWidth();
        mShadeRect.bottom = getMeasuredHeight();
    }

    private void init() {

        mTopLeftBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_4);
        mTopRightBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_1);
        mBottomLeftBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_2);
        mBottomRightBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_3);

        mTopLeftBitmapMatrix = new Matrix();
        mTopRightBitmapMatrix = new Matrix();
        mBottomLeftBitmapMatrix = new Matrix();
        mBottomRightBitmapMatrix = new Matrix();

        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                .FILTER_BITMAP_FLAG);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        mShadePaint = new Paint();
        mShadePaint.setDither(true);
        mShadePaint.setAntiAlias(true);
        mShadePaint.setStyle(Paint.Style.STROKE);
        mShadePaint.setStrokeWidth(50);
        mShadePaint.setColor(0xFFFFFF);
        mShadePaint.setAlpha(210);

        mShadeRect = new Rect();
        new Thread(new AnimRunnable()).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(centerX, centerY);
        canvas.save();
        canvas.rotate(45);
        canvas.setDrawFilter(pfd);
        canvas.drawBitmap(mTopLeftBitmap, mTopLeftBitmapMatrix, mPaint);
        canvas.drawBitmap(mTopRightBitmap, mTopRightBitmapMatrix, mPaint);
        canvas.drawBitmap(mBottomLeftBitmap, mBottomLeftBitmapMatrix, mPaint);
        canvas.drawBitmap(mBottomRightBitmap, mBottomRightBitmapMatrix, mPaint);
        canvas.translate(5,0);
        canvas.drawCircle(0, 0, 70f, mShadePaint);
        canvas.restore();
    }

    private void setBitmapDegrees(int degrees) {
        mTopRightBitmapMatrix.preRotate(degrees, mTopRightBitmap.getWidth() / 2f, mTopRightBitmap
                .getHeight() / 2f);
        mTopLeftBitmapMatrix.preRotate(degrees, mTopLeftBitmap.getWidth() / 2f, mTopLeftBitmap
                .getHeight() / 2f);
        mBottomLeftBitmapMatrix.preRotate(degrees, mBottomLeftBitmap.getWidth() / 2f,
                mBottomLeftBitmap.getHeight() / 2f);
        mBottomRightBitmapMatrix.preRotate(degrees, mBottomRightBitmap.getWidth() / 2f,
                mBottomRightBitmap.getHeight() / 2f);
    }

    private class AnimRunnable implements Runnable {
        int degrees = 10;
        int time = 0;
        @Override
        public void run() {
            while (true) {
                if (time == 54) {
                    degrees = -degrees;
                    time = 0;
                }
                time++;
                setBitmapDegrees(degrees);
                postInvalidate();
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
