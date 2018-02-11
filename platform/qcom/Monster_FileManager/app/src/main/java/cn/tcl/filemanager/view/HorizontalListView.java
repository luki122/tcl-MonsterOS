/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter> {

	public boolean mAlwaysOverrideTouch = true;
	protected ListAdapter mAdapter;
	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	public int mCurrentX;
	public int mNextX;
	public int mMaxX = Integer.MAX_VALUE;
	public int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemLongClicked;
	private boolean mDataChanged = false;
	public static int count=0;

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d("SNS",
				"this is enter HorizontalListView(Context context, AttributeSet attrs)");
		initView();
	}

	private synchronized void initView() {
		Log.d("SNS", "this is enter initView()");
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mScroller = new Scroller(getContext());
		mGesture = new GestureDetector(getContext(), mOnGesture);
	}

	@Override
	public void setOnItemSelectedListener(
			AdapterView.OnItemSelectedListener listener) {
		mOnItemSelected = listener;
	}

	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
		mOnItemClicked = listener;
	}

	@Override
	public void setOnItemLongClickListener(
			AdapterView.OnItemLongClickListener listener) {
		mOnItemLongClicked = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			Log.d("SHO", "this is setAdapter ---555");
			Log.d("SNS", "this is enter onChanged()");
			synchronized (HorizontalListView.this) {
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			Log.d("SHO", "this is setAdapter ---666");
			reset();
			invalidate();
			requestLayout();
		}

	};

	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		// TODO: implement
		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		Log.d("SHO", "this is setAdapter ---111");
		if (mAdapter != null) {
			Log.d("SHO", "this is setAdapter ---222");
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
		Log.d("SHO", "this is setAdapter ---333");
	}

	public synchronized void reset() {
		Log.d("SNS", "this is enter reset()");
		Log.d("SHO", "this is setAdapter ---444");
		initView();
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(int position) {
		Log.d("SNS", "this is enter setSelection(int position)");
		// mCurrentX = position;
		// LogUtils.d("SNS","end position"+position);
		//[BUG-FIX]-BEGIN by Junyong.Sun,2016/01/28,For PR-1529224
		if(isLayoutRtl()){
			scrollTo(0);
		}else {
			scrollTo(mMaxX);
		}
		//[BUG-FIX]-END by Junyong.Sun,2016/01/28,For PR-1529224
		// requestLayout();
		// fillList(position);
	}

	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);
		}

		addViewInLayout(child, viewPos, params, true);
		child.measure(
				MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top,
			int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mAdapter == null) {
			Log.d("SHO", "this is setAdapter ---666");
			return;
		}

		if (mDataChanged) {
			Log.d("SHO", "this is setAdapter ---mDataChanged");
			int oldCurrentX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}

		if (mScroller.computeScrollOffset()) {
			// postInvalidate();
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
			Log.d("SHO",
					"this is setAdapter ---mScroller.computeScrollOffset()"
							+ mNextX + "this is scrollx" + scrollx);

		}

		if (mNextX <= 0) {
			Log.d("SHO", "this is setAdapter ---mNextX <= 0");
			mNextX = 0;
			mScroller.forceFinished(true);
		}
		Log.d("SHO", "this is mNextX " + mNextX + "this is mMaxX" + mMaxX);
		if (mNextX >= mMaxX) {
			Log.d("SHO", "this is setAdapter ---mNextX >= mMaxX");
			mNextX = mMaxX;
			mScroller.forceFinished(true);
		}

		int dx = mCurrentX - mNextX;

		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);

		mCurrentX = mNextX;

		if (!mScroller.isFinished()) {
			post(new Runnable() {
				@Override
				public void run() {
					Log.d("SHO", "this is setAdapter ---777");
					++count;
					requestLayout();
				}
			});

		}
		if(mScroller.isFinished() && count < 1){
			post(new Runnable() {
				@Override
				public void run() {
					Log.d("SHO", "this is setAdapter ---888");
					++count;
					requestLayout();
				}
			});
		}
	}

	private void fillList(final int dx) {
		int edge = 0;
		View child = getChildAt(getChildCount() - 1);
		if (child != null) {
			edge = child.getRight();
		}
		fillListRight(edge, dx);

		edge = 0;
		child = getChildAt(0);
		if (child != null) {
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);

	}

	private void fillListRight(int rightEdge, final int dx) {
		while (rightEdge + dx < getWidth()
				&& mRightViewIndex < mAdapter.getCount()) {

			View child = mAdapter.getView(mRightViewIndex,
					mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();

			if (mRightViewIndex == mAdapter.getCount() - 1) {
				mMaxX = mCurrentX + rightEdge - getWidth();
			}

			if (mMaxX < 0) {
				mMaxX = 0;
			}
			Log.d("SNS", "this is enter mMaxX" + mMaxX);
			mRightViewIndex++;
		}

	}

	private void fillListLeft(int leftEdge, final int dx) {
		while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
			View child = mAdapter.getView(mLeftViewIndex,
					mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}

	private void removeNonVisibleItems(final int dx) {
		View child = getChildAt(0);
		while (child != null && child.getRight() + dx <= 0) {
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);

		}

		child = getChildAt(getChildCount() - 1);
		while (child != null && child.getLeft() + dx >= getWidth()) {
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mRightViewIndex--;
			child = getChildAt(getChildCount() - 1);
		}
	}

	private void positionItems(final int dx) {
		if (getChildCount() > 0) {
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth,
						child.getMeasuredHeight());
				left += childWidth + child.getPaddingRight();
			}
		}
	}

	public synchronized void scrollTo(int x) {
		Log.d("SHO", "this is setAdapter ---121212");
		mScroller.startScroll(mNextX, 0, x - mNextX, 0);
		requestLayout();
		// Log.d("SHO","this is setAdapter ---888");
		// Log.d("SHO","this is setAdapter ---999");
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= mGesture.onTouchEvent(ev);
		return handled;
	}

	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		synchronized (HorizontalListView.this) {
			mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
		}
		requestLayout();

		return true;
	}

	protected boolean onDown(MotionEvent e) {
		mScroller.forceFinished(true);
		return true;
	}

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return HorizontalListView.this
					.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			synchronized (HorizontalListView.this) {
				mNextX += (int) distanceX;
			}
			requestLayout();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (mOnItemClicked != null) {
						mOnItemClicked.onItemClick(HorizontalListView.this,
								child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					if (mOnItemSelected != null) {
						mOnItemSelected.onItemSelected(HorizontalListView.this,
								child, mLeftViewIndex + 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (mOnItemLongClicked != null) {
						Log.e("HorizontalListView","Enter HorizontalListView onItemLongClick....");
						mOnItemLongClicked.onItemLongClick(
								HorizontalListView.this, child, mLeftViewIndex
										+ 1 + i,
								mAdapter.getItemId(mLeftViewIndex + 1 + i));
					}
					break;
				}

			}
		}

		private boolean isEventWithinView(MotionEvent e, View child) {
			Rect viewRect = new Rect();
			int[] childPosition = new int[2];
			child.getLocationOnScreen(childPosition);
			int left = childPosition[0];
			int right = left + child.getWidth();
			int top = childPosition[1];
			int bottom = top + child.getHeight();
			viewRect.set(left, top, right, bottom);
			return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
		}
	};
}
