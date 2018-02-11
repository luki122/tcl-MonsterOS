package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.R;

/**
 * Created by chenhl on 16-11-7.
 */
public class MstPageIndicator extends ViewGroup {

    private static final float SINGLE_SCALE = .4f;
    private final int mPageIndicatorWidth;
    private final int mPageIndicatorHeight;
    private final int mPageDotWidth;
    private int mPosition = 0;

    public MstPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPageIndicatorWidth =
                (int) mContext.getResources().getDimension(R.dimen.qs_page_indicator_width);
        mPageIndicatorHeight =
                (int) mContext.getResources().getDimension(R.dimen.qs_page_indicator_height);
        mPageDotWidth = (int) (mPageIndicatorWidth * SINGLE_SCALE);
    }

    public void setNumPages(int numPages) {
        setVisibility(numPages > 1 ? View.VISIBLE : View.INVISIBLE);
        while (numPages < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        while (numPages > getChildCount()) {
            ImageView v = new ImageView(mContext);
            v.setImageResource(R.drawable.tcl_minor_b);
			v.setScaleType(ImageView.ScaleType.CENTER);
            addView(v, new LayoutParams(mPageIndicatorWidth, mPageIndicatorHeight));
        }
		setLocation(mPosition);
    }

    public void setLocation(int location) {
        Log.d("chenhl","location:"+location);
		mPosition = location;
		final int N = getChildCount();
		for (int i = 0; i < N; i++) {
			ImageView v = (ImageView) getChildAt(i);
			// Clear out any animation positioning.
			if(location==i) {
				v.setImageResource(R.drawable.tcl_minor_a);
			}else{
				v.setImageResource(R.drawable.tcl_minor_b);
			}
		}
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int N = getChildCount();
        if (N == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int widthChildSpec = MeasureSpec.makeMeasureSpec(mPageIndicatorWidth,
                MeasureSpec.EXACTLY);
        final int heightChildSpec = MeasureSpec.makeMeasureSpec(mPageIndicatorWidth,
                MeasureSpec.EXACTLY);
        for (int i = 0; i < N; i++) {
            getChildAt(i).measure(widthChildSpec, heightChildSpec);
        }
        int width = (mPageIndicatorWidth - mPageDotWidth) * N + mPageDotWidth;
        setMeasuredDimension(width, mPageIndicatorHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int N = getChildCount();
        if (N == 0) {
            return;
        }
        for (int i = 0; i < N; i++) {
            int left = (mPageIndicatorWidth - mPageDotWidth) * i;
            getChildAt(i).layout(left, 0, mPageIndicatorWidth + left, mPageIndicatorHeight);
        }
    }
}
