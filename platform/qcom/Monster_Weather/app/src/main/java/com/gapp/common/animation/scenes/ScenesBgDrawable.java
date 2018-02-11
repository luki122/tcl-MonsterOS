package com.gapp.common.animation.scenes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-20.
 * $desc
 */
public class ScenesBgDrawable extends Drawable {

    private Paint mPaint = new Paint();

    private float mStartY, mEndY;
    private int mColors[] = new int[2];

    private LinearGradient mLinearGradient;

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (0f != mEndY) {
            float y0 = mStartY * bounds.height();
            float y1 = mEndY * bounds.height();
            mLinearGradient = new LinearGradient(0, y0, 0, y1, mColors[0], mColors[1], Shader.TileMode.CLAMP);
        }
    }

    public void setLinearColors(float y0, float y1, int color0, int color1) {
        mStartY = y0;
        mEndY = y1;
        mColors[0] = color0;
        mColors[1] = color1;
        onBoundsChange(getBounds());
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setShader(null);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(getBounds(), mPaint);
        mPaint.setShader(mLinearGradient);
        canvas.drawRect(getBounds(), mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
