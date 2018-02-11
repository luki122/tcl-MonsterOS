package com.android.systemui.recents.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.statusbar.DismissView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.OnScrollListener;

public class MstTaskStackView extends RecyclerView {

    /**Mst: tangjun add begin*/
    private View mCurrentView;  
    private boolean mWillIntercept= true;
  
    private OnItemScrollChangeListener mItemScrollChangeListener;  
  
    public void setOnItemScrollChangeListener(  
            OnItemScrollChangeListener mItemScrollChangeListener)  {  
        this.mItemScrollChangeListener = mItemScrollChangeListener;  
    }  
  
    public interface OnItemScrollChangeListener  {  
        void onChange(View view, int position);  
    } 
    
    private OnScrollListener mOnScrollListener = new OnScrollListener() {
    	
    	@Override
    	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    		super.onScrollStateChanged(recyclerView, newState);
    	}

    	@Override
    	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    		super.onScrolled(recyclerView, dx, dy);
    		
    		View newView = getChildAt(0);
    		if (mItemScrollChangeListener != null){
    			if (newView != null && newView != mCurrentView) {
    				mCurrentView = newView ;
    				mItemScrollChangeListener.onChange(mCurrentView,
    						getChildPosition(mCurrentView));

    			}
    		}
    	}
	};
	
	public MstTaskStackView(Context context) {
		this(context, null);
	}
    
	public MstTaskStackView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public MstTaskStackView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
        /**Mst: tangjun add begin*/
		this.setOnScrollListener(mOnScrollListener); 
		/**Mst: tangjun add end*/
	}
	
	/**Mst: tangjun add resistance recyclerview begin*/
	public boolean fling(int velocityX, int velocityY) {
		return super.fling(velocityX * 3/5, velocityY);
	}
	/**Mst: tangjun add resistance recyclerview end*/
  
    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b)  {  
        super.onLayout(changed, l, t, r, b);  
        mCurrentView = getChildAt(0);
  
        if (mItemScrollChangeListener != null)  {  
            mItemScrollChangeListener.onChange(mCurrentView,  
                    getChildPosition(mCurrentView));  
        }  
    } 
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
    	// TODO Auto-generated method stub
    	if(mWillIntercept) {
    		return super.onInterceptTouchEvent(e);
    	}else{
    		return false;
    	}
    }

    public void setTouchIntercept(boolean value) {
    	mWillIntercept = value;
    }
    
    public boolean getTouchIntercept() {
    	return mWillIntercept;
    }
    /**Mst: tangjun add end*/
	
}
