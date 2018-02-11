package mst.widget;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import mst.view.animation.BezierInterpolator;
import mst.view.animation.StatusAnimation;
import mst.widget.recycleview.RecyclerView;
import mst.view.animation.BezierInterpolator;

public class MstCheckListAdapter extends BaseAdapter implements Runnable{
	private static final String TAG = "MstRecyclerAdapter";
	private boolean mCheck = false;
	private boolean hasAnimation = false;

	private ListView mListView;
	private ArrayList<View> mArray;
	private StatusAnimation mAnimation;
	private Handler mHandler;

	private static final long ANIMATION_START_TIME = 100;

	public MstCheckListAdapter(){
		mArray = new ArrayList<>();
		mAnimation = new StatusAnimation();
		mAnimation.setDuration(200);
//		mAnimation.setInterpolator(new BezierInterpolator(0.17f,0,0.67f,1));
		mHandler = new Handler();
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(canCreateView(position,convertView,parent)){
			convertView = onCreateView(position,parent);
		}
		onBindView(position,convertView);
		if(mListView == null){
			mListView = (ListView) parent;
		}
		View checkbox = getCheckBox(position,convertView);
		View movelayout = getMoveView(position,convertView);
		if(hasAnimation) {
			if(mArray.contains(convertView)){
				mArray.remove(convertView);
				mAnimation.setStatus(!mCheck, false, checkbox, movelayout);
//				android.util.Log.e("test","CheckBoxListActivity startAnimation : position = "+position+";mCheck = "+mCheck);
				mAnimation.addAnimationView(movelayout);
				startAnimation();
			}else{
				mAnimation.setStatus(mCheck, false, checkbox, movelayout);
			}
		}else{
			mAnimation.setStatus(mCheck, false, checkbox, movelayout);
		}
		return convertView;
	}

	protected View onCreateView(int position, ViewGroup parent){return null;}

	protected void onBindView(int position, View convertView){}

	protected boolean canCreateView(int position, View convertView, ViewGroup parent){
		return convertView == null;
	}

	private void startAnimation(){
		mHandler.removeCallbacks(this);
		mHandler.postDelayed(this,ANIMATION_START_TIME);
	}

	@Override
	public void run() {
		mAnimation.start(mCheck);
	}

	public void setChecked(boolean checked){
		mCheck = checked;
		hasAnimation = true;
		if(mListView != null){
			mListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
			final int N = mListView.getChildCount();
//			android.util.Log.e("test","CheckBoxListActivity setOnLongClickListener N = "+N);
			for(int i=0;i<N;i++){
				View child = mListView.getChildAt(i);
				mArray.add(child);
			}
		}
		notifyDataSetChanged();
	}

	public boolean isChecked(){
		return mCheck;
	}

	protected View getCheckBox(int position, View itemview){
		return null;
	}

	protected View getMoveView(int position, View itemview){
		return null;
	}

//	public static class MstCheckViewHodler extends RecyclerView.ViewHolder{
//		public View mCheckbox;
//		public View mMoveLayout;
//		public StatusAnimation mAnimation;
//
//		public MstCheckViewHodler(View itemView) {
//			super(itemView);
//			mCheckbox = itemView.findViewById(getCheckBoxResourceId());
//			mMoveLayout = itemView.findViewById(getMoveViewResourceId());
//			mAnimation = new StatusAnimation(mCheckbox,mMoveLayout);
//			mAnimation.setDuration(200);
//			mAnimation.setInterpolator(new BezierInterpolator(0.17f,0,0.67f,1));
//		}
//
//
//
//	}

}
