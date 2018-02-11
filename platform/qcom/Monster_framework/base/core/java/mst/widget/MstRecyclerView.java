package mst.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import mst.widget.recycleview.RecyclerView;

public class MstRecyclerView extends RecyclerView{
	private static final String TAG = "MstListView";


	private static final int TOUCH_STATE_NONE = 0;
	private static final int TOUCH_STATE_X = 1;
	private static final int TOUCH_STATE_Y = 2;

	private int MAX_Y = 10;
	private int MAX_X = 10;
	private float mDownX;
	private float mDownY;
	private int mTouchState;
	private int mTouchPosition;
	private int mOldTouchPosition;


	private SliderLayout mSlideLayout;
	private SliderLayout mLastSlideLayout;

	private boolean isSingleSlider = true;

	public MstRecyclerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public MstRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public MstRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}


	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}
	
	
	private void init() {
		MAX_X = dp2px(MAX_X);
		MAX_Y = dp2px(MAX_Y);
		mTouchState = TOUCH_STATE_NONE;
	}
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getContext().getResources().getDisplayMetrics());
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_DOWN) {
			mTouchState = TOUCH_STATE_NONE;
			View view = findChildViewUnder(ev.getX(), ev.getY());
			mOldTouchPosition = mTouchPosition;
			mTouchPosition = getChildAdapterPosition(view);
			if (mSlideLayout != null) {
				if (mTouchPosition != mOldTouchPosition) {
					if (isSingleSlider && !mSlideLayout.isClosed()) {
						mSlideLayout.close(true);
					}
				}
			}
			if(mTouchPosition != -1) {
				if (view instanceof SliderLayout || view instanceof SliderView) {
					mSlideLayout = (SliderLayout) view;
				} else if (view instanceof ViewGroup) {
					mSlideLayout = (SliderLayout) view.findViewById(com.mst.R.id.slider_view);
				}
			}else{
				mSlideLayout = null;
			}
		}else if(ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
			return true;
		}else if(ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP){
//			mTouchPosition = -1;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() != MotionEvent.ACTION_DOWN && mSlideLayout == null)
			return super.onTouchEvent(ev);
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mDownX = ev.getX();
			mDownY = ev.getY();
			mTouchState = TOUCH_STATE_NONE;

			if (mSlideLayout != null) {
				mSlideLayout.onTouchSlideEvent(ev);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float dy = Math.abs((ev.getY() - mDownY));
			float dx = Math.abs((ev.getX() - mDownX));
			if (mTouchState == TOUCH_STATE_X) {
				if (mSlideLayout != null) {
					mSlideLayout.onTouchSlideEvent(ev);
				}
				ev.setAction(MotionEvent.ACTION_CANCEL);
				super.onTouchEvent(ev);
				return true;
			} else if (mTouchState == TOUCH_STATE_NONE) {
				if (Math.abs(dy) > MAX_Y) {
					mTouchState = TOUCH_STATE_Y;
				} else if (dx > MAX_X) {
					mTouchState = TOUCH_STATE_X;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mLastSlideLayout = mSlideLayout;
			if (mTouchState == TOUCH_STATE_X || mTouchState == TOUCH_STATE_Y) {
				if (mSlideLayout != null) {
					mSlideLayout.onTouchSlideEvent(ev);
//					if (!mSlideLayout.isOpened()) {
//						mTouchPosition = -1;
//						mSlideLayout = null;
//					}
				}
				if(mTouchState == TOUCH_STATE_X) {
					ev.setAction(MotionEvent.ACTION_CANCEL);
					super.onTouchEvent(ev);
					return true;
				}
			}
			break;
		}
		
		return super.onTouchEvent(ev);
	}

	public void setSingleSlider(boolean single){
		isSingleSlider = single;
	}


}
