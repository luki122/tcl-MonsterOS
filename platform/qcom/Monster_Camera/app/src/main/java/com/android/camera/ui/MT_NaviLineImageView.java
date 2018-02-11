package com.android.camera.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import android.util.Log;


/**
 * Created by hoperun on 12/28/15.
 */
public class MT_NaviLineImageView extends ImageView {
    private static final String TAG = "NaviLineImageView";

    private int mLeft = 0;
    private int mTop = 0;
    private int mRight = 0;
    private int mBottom = 0;

    private boolean mFirstDraw = false;

    public MT_NaviLineImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.v(TAG, "[onLayout]changed=" + changed + " left =" + left + " top = " + top
                + " right = " + right + " bottom = " + bottom);

        super.onLayout(changed, left, top, right, bottom);
    }

    public void layout(int l, int t, int r, int b) {
        Log.v(TAG, "[layout]left =" + l + " top = " + t + " right = " + r + " bottom = " + b);
        if (!mFirstDraw || (mLeft == l && mTop == t && mRight == r && mBottom == b)) {
            super.layout(l, t, r, b);
            mFirstDraw = true;
        }
    }

    public void setLayoutPosition(int l, int t, int r, int b) {
        Log.v(TAG, "[setLayoutPosition] left =" + l + " top = " + t + " right = " + r
                + " bottom = " + b);
        mLeft = l;
        mTop = t;
        mRight = r;
        mBottom = b;
        layout(l, t, r, b);
    }
}