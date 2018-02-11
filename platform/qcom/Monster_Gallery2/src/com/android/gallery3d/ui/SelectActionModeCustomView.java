package com.android.gallery3d.ui;

/*
 * This file is added by ShenQianfeng on 2016.07.09
 * added because ActionMode setCustomView cannot match the parent of screen width.
 * so, it's the extra work we need to do.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.gallery3d.R;

public class SelectActionModeCustomView extends RelativeLayout {
    
    private View mLeftButton;
    private View mRightButton;
    
    private int mScreenWidth;
    
    private static final String TAG = "SelectActionModeCustomView";

    public SelectActionModeCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftButton = findViewById(R.id.cancel);
        mRightButton = findViewById(R.id.select_all);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.setMeasuredDimension(mScreenWidth, this.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int childLeft = 0;
        int childTop = 0;
        
        childLeft = 0;
        childTop = ((b - t) - mLeftButton.getMeasuredHeight()) / 2;
        mLeftButton.layout(childLeft, childTop, 
                childLeft + mLeftButton.getMeasuredWidth(), 
                childTop + mLeftButton.getMeasuredHeight());
        
        childLeft = (r - l) - mRightButton.getMeasuredWidth();
        childTop = ((b - t) - mRightButton.getMeasuredHeight()) / 2;
        mRightButton.layout(childLeft, childTop, 
                childLeft + mRightButton.getMeasuredWidth(), 
                childTop + mRightButton.getMeasuredHeight());
    }

    
}
