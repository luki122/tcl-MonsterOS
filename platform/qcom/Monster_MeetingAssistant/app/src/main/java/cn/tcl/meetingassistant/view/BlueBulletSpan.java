/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;

import cn.tcl.meetingassistant.utils.DensityUtil;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-21.
 * the blue bullet span for text
 */
public class BlueBulletSpan extends BulletSpan {
    private final int mGapWidth;
    private final boolean mWantColor;
    private final int mColor;
    private int DEFAULT_COLOR = 0x00abb0;
    private int mRectSize = 10;
    private Context mContext;

    private static Path sBulletPath = null;
    public static final int STANDARD_GAP_WIDTH = 2;

    public BlueBulletSpan(Context context) {
        mContext = context;
        mGapWidth = STANDARD_GAP_WIDTH;
        mWantColor = false;
        mColor = 0;
    }

    public BlueBulletSpan(Context context,int gapWidth) {
        mContext = context;
        mGapWidth = gapWidth;
        mWantColor = false;
        mColor = 0;
    }

    public BlueBulletSpan(Context context,int gapWidth, int color) {
        mContext = context;
        mGapWidth = gapWidth;
        mWantColor = true;
        mColor = color;
    }

    public BlueBulletSpan(Context context,int gapWidth, int color, int size) {
        mContext = context;
        mGapWidth = gapWidth;
        mWantColor = true;
        mColor = color;
        mRectSize = DensityUtil.dip2px(mContext,size);
    }

    public BlueBulletSpan(Context context,Parcel src) {
        mContext = context;
        mGapWidth = src.readInt();
        mWantColor = src.readInt() != 0;
        mColor = src.readInt();
    }

    /**
     * @hide
     */
    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(mGapWidth);
        dest.writeInt(mWantColor ? 1 : 0);
        dest.writeInt(mColor);
    }

    public int getLeadingMargin(boolean first) {
        return mRectSize + mGapWidth;
    }

    /**
     * draw the bullet span in the textView
     * @param c Canvas
     * @param p Paint
     * @param x
     * @param dir
     * @param top
     * @param baseline
     * @param bottom
     * @param text
     * @param start
     * @param end
     * @param first
     * @param layout
     */
    //TODO  the bullet color and position has some problem to solve
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        float ascent =  fontMetrics.ascent;
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldColor = 0;

            if (mWantColor) {
                oldColor = p.getColor();
                p.setColor(mColor);
            }

            p.setStyle(Paint.Style.FILL);

            if (c.isHardwareAccelerated()) {
                if (sBulletPath == null) {
                    sBulletPath = new Path();
                    sBulletPath.addRect(0.0f, 0.0f, mRectSize, mRectSize, Direction.CW);
                }

                c.save();
                c.translate(0, baseline + (ascent - mRectSize + mRectSize) / 2.0f);
                c.drawPath(sBulletPath, p);
                c.restore();
            } else {
                c.drawRect(0, baseline + (ascent - mRectSize) / 2, mRectSize, baseline + (ascent - mRectSize) / 2, p);
            }

            if (mWantColor) {
                p.setColor(oldColor);
            }

            p.setStyle(style);
        }
    }
}
