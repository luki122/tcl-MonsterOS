package com.monster.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;

public class AppSearchView extends ImageView implements IChangeColors.IItemColorChange{
	
	private Scroller mScroller;
    private Context mContext;
	
	public AppSearchView(Context context) {
		super(context);
	}
	
	public AppSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext = context;
		mScroller = new Scroller(getContext(),new ScrollInterpolator());
	}
	
	public Scroller getScroller() {
		return mScroller;
	}
	
	@Override
    public void scrollTo(int x, int y) {
		setTranslationX(-x);
    }
	
	@Override
    public void computeScroll() {
        computeScrollHelper();
    }
	
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
            if (getScrollX() != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public void changeColors(int[] colors) {
        if(colors[0] != -1){
            this.setImageDrawable(mContext.getResources().getDrawable(R.drawable.apps_search_black));
        }else{
            this.setImageDrawable(mContext.getResources().getDrawable(R.drawable.apps_search));
        }
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            return (float)(1.0f - Math.pow((1.0f - t), 2 * 1.5));
        }
    }
}
