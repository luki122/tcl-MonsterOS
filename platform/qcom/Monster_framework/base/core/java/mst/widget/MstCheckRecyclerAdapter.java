package mst.widget;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import mst.view.animation.StatusAnimation;
import mst.widget.recycleview.RecyclerView;
import mst.view.animation.BezierInterpolator;

public class MstCheckRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter implements Runnable{
	private static final String TAG = "MstRecyclerAdapter";
	private boolean mCheck = false;
	private boolean hasAnimation = false;
	private StatusAnimation mAnimation;
	private Handler mHandler;
	private static final long ANIMATION_START_TIME = 100;

	private RecyclerView mRecyclerView;
	private ArrayList<Integer> mArray;

	public MstCheckRecyclerAdapter(){
		mArray = new ArrayList<>();
		mAnimation = new StatusAnimation();
		mAnimation.setDuration(200);
//		mAnimation.setInterpolator(new BezierInterpolator(0.17f,0,0.67f,1));
		mHandler = new Handler();
	}

	public VH onCreateViewHolder(ViewGroup parent, int viewType){
		if(mRecyclerView == null){
			mRecyclerView = (RecyclerView) parent;
		}
		return null;
	}

	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
		View checkbox = getCheckBox(position,viewHolder);
		View movelayout = getMoveView(position,viewHolder);
		if(hasAnimation) {
			if(mArray.contains(position)){
				mArray.remove(Integer.valueOf(position));
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
//		if(hasAnimation) {
//			if(mArray.contains(position)){
//				mArray.remove(Integer.valueOf(position));
//				((MstCheckViewHodler)viewHolder).mAnimation.init(!mCheck, false);
//				((MstCheckViewHodler)viewHolder).mAnimation.start(mCheck);
//			}else{
//				((MstCheckViewHodler)viewHolder).mAnimation.init(mCheck, false);
//			}
//		}else{
//			((MstCheckViewHodler)viewHolder).mAnimation.init(mCheck, false);
//		}
	}

	@Override
	public int getItemCount() {
		return 0;
	}


	public void setChecked(boolean checked){
		mCheck = checked;
		hasAnimation = true;
		if(mRecyclerView != null){
			mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
			final int N = mRecyclerView.getChildCount();
//			android.util.Log.e("test","CheckBoxListActivity setOnLongClickListener N = "+N);
			for(int i=0;i<N;i++){
				View child = mRecyclerView.getChildAt(i);
				mArray.add(mRecyclerView.getChildAdapterPosition(child));
			}
		}
		notifyDataSetChanged();
	}

	private void startAnimation(){
		mHandler.removeCallbacks(this);
		mHandler.postDelayed(this,ANIMATION_START_TIME);
	}

	@Override
	public void run() {
		mAnimation.start(mCheck);
	}

	public boolean isChecked(){
		return mCheck;
	}

	protected View getCheckBox(int position, RecyclerView.ViewHolder viewHolder){
		return null;
	}

	protected View getMoveView(int position, RecyclerView.ViewHolder viewHolder){
		return null;
	}

	/*public static class MstCheckViewHodler extends RecyclerView.ViewHolder{
		public StatusAnimation mAnimation;

		public MstCheckViewHodler(View itemView) {
			super(itemView);
			mAnimation = new StatusAnimation(itemView.findViewById(getCheckBox()),itemView.findViewById(getMoveView()));
			mAnimation.setDuration(200);
			mAnimation.setInterpolator(new BezierInterpolator(0.17f,0,0.67f,1));
		}

		protected int getCheckBox(){
			return 0;
		}

		protected int getMoveView(){
			return 0;
		}

	}*/

}
