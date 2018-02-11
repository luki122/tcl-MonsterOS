package com.android.camera.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.android.camera.debug.Log;

/**
 * Created by sichao.hu on 12/15/15.
 */
public abstract class CustomizeDrawable extends Drawable {
    private Paint mPaint;
    private int mColor;
    private int mAlpha=0xff;
    public CustomizeDrawable(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }
    @Override
    public abstract void draw(Canvas canvas) ;

    @Override
    public final void setAlpha(int i) {
        mAlpha=i;
        updatePaintColor();
    }

    private void updatePaintColor() {
        int paintColor = (mAlpha << 24) | (mColor & 0x00ffffff);
        mPaint.setColor(paintColor);
        invalidateSelf();
    }

    public final Paint getPaint(){
        return mPaint;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    public final void setColor(int color){
        mColor = color;
        updatePaintColor();
    }

    public void setARGB(int color){
        mAlpha=(color&0xff000000)>>24;
        mColor=color&0x00ffffff;
        updatePaintColor();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
