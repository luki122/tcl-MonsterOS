package mst.widget;


import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.mst.R;

public class FoldProgressBar extends ImageView {


	private WeakReference<AnimationDrawable> mAnimation;
	public FoldProgressBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public FoldProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public FoldProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public FoldProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		init();
	}

	
	public void init(){
		setImageResource(R.drawable.fold_progress_light);
		mAnimation = new WeakReference<AnimationDrawable>((AnimationDrawable) getDrawable());
	}
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		start();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		stop();
	}
	
	
	
	public void start(){
		if(mAnimation.get() != null){
			if(mAnimation.get().isRunning()){
				mAnimation.get().stop();
			}
			mAnimation.get().start();
		}
	}
	
	
	public void stop(){
		if(mAnimation.get() != null){
			if(mAnimation.get().isRunning()){
				mAnimation.get().stop();
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	

}
