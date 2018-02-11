package com.android.camera.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.tct.camera.R;

/**
 * Created by hoperun on 12/25/15.
 */

class MT_PanoProgressBarDrawable extends Drawable {
    private static final String TAG = "MT_ProgressBarDrawable";

    private int mBlockSizes[] = null;
    private int mPadding;

    private Drawable mCleanBlock;
    private Drawable mDirtyBlock;

    private View mAttachedView;
    private final Paint mPaint = new Paint();

    public MT_PanoProgressBarDrawable(Context context, View view, int[] blockSizes, int padding) {
        Resources res = context.getResources();
        mBlockSizes = blockSizes;
        mPadding = padding;
        mCleanBlock = res.getDrawable(R.drawable.ic_panorama_block);
        mDirtyBlock = res.getDrawable(R.drawable.ic_panorama_block_highlight);
        mAttachedView = view;
    }

    @Override
    protected boolean onLevelChange(int level) {
        Log.d(TAG, "[onLevelChange:]level = " + level);
        invalidateSelf();
        return true;
    }

    @Override
    public int getIntrinsicWidth() {
        int width = 0;
        for (int i = 0, len = mBlockSizes.length; i < len - 1; i++) {
            width += mBlockSizes[i] + mPadding;
        }
        width += mBlockSizes[mBlockSizes.length - 1];
        Log.d(TAG, "[getIntrinsicWidth]" + width);

        return width;
    }

    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    public void draw(Canvas canvas) {
        int xoffset = 0;
        int level = getLevel();
        // draw dirty block according to the number captured.
        for (int i = 0; i < level; i++) {
            int yoffset = (mAttachedView.getHeight() - mBlockSizes[i]) / 2;
            mDirtyBlock.setBounds(xoffset, yoffset, xoffset + mBlockSizes[i], yoffset
                    + mBlockSizes[i]);
            mDirtyBlock.draw(canvas);
            Log.v(TAG, "[draw]dirty block,i=" + i + " xoffset = " + xoffset + " yoffset = "
                    + yoffset);
            xoffset += (mBlockSizes[i] + mPadding);
        }

        // draw the rest as clean block.
        for (int i = level, len = mBlockSizes.length; i < len; i++) {
            int yoffset = (mAttachedView.getHeight() - mBlockSizes[i]) / 2;
            mCleanBlock.setBounds(xoffset, yoffset, xoffset + mBlockSizes[i], yoffset
                    + mBlockSizes[i]);
            mCleanBlock.draw(canvas);
            Log.d(TAG, "[draw]rest,i=" + i + " xoffset = " + xoffset + " yoffset = " + yoffset);
            xoffset += (mBlockSizes[i] + mPadding);
        }
    }
}