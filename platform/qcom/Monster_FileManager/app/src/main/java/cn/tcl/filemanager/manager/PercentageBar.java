/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collection;

import cn.tcl.filemanager.R;

public class PercentageBar extends View {

    private final Paint mEmptyPaint;
    private Collection<Entry> mEntries;
    private int mMinTickWidth = 1;
    private int h_offset = getResources().getDimensionPixelSize(R.dimen.percentage_h_offset);
    private int v_offset = getResources().getDimensionPixelSize(R.dimen.percentage_v_offset);
    private int sd_offset = getResources().getInteger(R.integer.percentage_sd_offset);

    public static class Entry implements Comparable<Entry> {
        public final int order;
        public float percentage;
        public final Paint paint;
        public final Drawable drawable;

        protected Entry(int order, float percentage, Paint paint, Drawable drawable) {
            this.order = order;
            this.percentage = percentage;
            this.paint = paint;
            this.drawable = drawable;
        }

        @Override
        public int compareTo(Entry another) {
            return order - another.order;
        }
    }

    public PercentageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEmptyPaint = new Paint();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PercentageBar);
        mMinTickWidth = a.getDimensionPixelSize(R.styleable.PercentageBar_minTickWidth, 1);
        a.recycle();
        mEmptyPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int left = getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        final int top = getPaddingTop();
        final int bottom = getHeight() - getPaddingBottom();

        final int width = right - left;

        float lastX = left;

        if (mEntries != null) {
            for (final Entry e : mEntries) {
                //add for PR1008249 by yane.wang@jrdcom.com 20150525 begin
				if (e.percentage < 0.05) {
					e.percentage = (float) 0.05;
				}
              //add for PR1008249 by yane.wang@jrdcom.com 20150525 end
				final float entryWidth;
				entryWidth = Math.max(mMinTickWidth, width * e.percentage);

				final float nextX = lastX + entryWidth;
				Rect src = new Rect();
				src.top = top + v_offset;
				src.bottom = bottom;
				if (e.order == 0) {
					src.left = (int) lastX - h_offset;
				} else {
					src.left = (int) lastX - h_offset - sd_offset;
				}

				src.right = (int) nextX;
				try {
					Bitmap bitmap = getBitmapByNinePatchDrawable(e.drawable, src);
					if (bitmap != null) {
						canvas.drawBitmap(bitmap, null, src, e.paint);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				lastX = nextX;
            }
        }
    }

    public static Entry createEntry(int order, float percentage, Drawable drawable) {
        final Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        return new Entry(order, percentage, p, drawable);
    }

    public void setEntries(Collection<Entry> entries) {
        mEntries = entries;
    }

    private Bitmap getBitmapByNinePatchDrawable(Drawable drawable, Rect src) {
        Bitmap bitmap = null;
        try {
			bitmap = Bitmap.createBitmap(src.width(), src.height(),
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_4444);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, src.width(),src.height());
            drawable.draw(canvas);
        }
        return bitmap;
    }
}
