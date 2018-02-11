package com.android.gallery3d.filtershow.filters;

import com.android.gallery3d.R;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class FilterShowRootView extends LinearLayout {
    
    private Rect mWindowInsets = new Rect();
    private View  mCustomTopBar;
    private View mMainView;
    private int mCustomTopBarHeight;

    public FilterShowRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCustomTopBarHeight = context.getResources().getDimensionPixelSize(R.dimen.filtershow_custom_top_bar_height);
    }
    
    public FilterShowRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mWindowInsets.set(insets);
        return true;
    }
    
    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //super.onLayout(changed, l, t, r, b);
        if(null == mCustomTopBar) {
            mCustomTopBar = findViewById(R.id.custom_top_bar);
        }
        if(null == mMainView) {
            mMainView = findViewById(R.id.mainView);
        }
        int top = mWindowInsets.top;
        int bottom = mWindowInsets.top + mCustomTopBarHeight;
        mCustomTopBar.layout(l, top, r, bottom);
        
        top = bottom;
        bottom = b;// top + (b - t - mCustomTopBarHeight - mWindowInsets.top);
        mMainView.layout(l, top, r, b);
        mMainView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    
    
}
