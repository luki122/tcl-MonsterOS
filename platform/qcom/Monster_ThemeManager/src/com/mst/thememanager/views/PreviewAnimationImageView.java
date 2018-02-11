package com.mst.thememanager.views;

import mst.utils.AnimationUtils;

import com.mst.thememanager.entities.PreviewTransitionInfo;

import android.R.anim;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.NonNull;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mst.thememanager.R;
/**
 * 
 * This view will play scale animation when its's
 * parent activity started or finished.
 *
 */
public class PreviewAnimationImageView extends ImageView implements AnimatorListener{
	
	private static final int POSITION_TYPE_X = 0;
	private static final int POSITION_TYPE_Y = 1;
	
	private static final int SIZE_TYPE_W = 2;
	private static final int SIZE_TYPE_H= 3;
	
	private static final int ANIMATION_DURATION = 500; 
	
	public static final int ANIMATION_ENTER = 0;
	
	public static final int ANIMATION_EXIT = 1;
	
	public static final int ANIMATION_NULL = -1;
	
	private int mAnimationType = ANIMATION_NULL;
	
	private int mPreviewImageWidth;
	private int mPreviewImageHeight;
	private int mScreenWidth;
	private int mScreenHeight;
	private PreviewTransitionInfo mInfo;
	private ValueAnimator mXPositionAnimator;
	private ValueAnimator mYPositionAnimator;
	private ValueAnimator mWidthAnimator;
	private ValueAnimator mHeightAnimator;
	private ViewGroup.LayoutParams	mLayoutParams;
	
	private Callback mCallback;
	
	/**
	 *
	 * Callback for scale animation.
	 *
	 */
	public interface Callback{
		/**
		 * 
		 * @param animationType one of {@link #ANIMATION_ENTER} or {@link #ANIMATION_EXIT}
		 */
		public void onAnimationEnd(int animationType);
	}
	
	public PreviewAnimationImageView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		init();
	}

	public PreviewAnimationImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public PreviewAnimationImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public PreviewAnimationImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init(){
		mPreviewImageWidth = getResources().getDimensionPixelSize(R.dimen.theme_detail_preview_img_width);
		mPreviewImageHeight = getResources().getDimensionPixelSize(R.dimen.theme_detail_preview_img_height);
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels
				+getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
	}
	
	public void setCallback(Callback callback){
		mCallback = callback;
	}
	
	/**
	 * Setup transition info for this view,see{@link com.mst.thememanager.entities.PreviewTransitionInfo}
	 * @param info
	 */
	public void setTransitionInfo(@NonNull PreviewTransitionInfo info){
		mInfo = info;
	}
	
	/**
	 * Call this to play enter scale animation.
	 */
	public void enter(){
		if(mInfo != null){
			postInitAnimation();
		}
		mAnimationType = ANIMATION_ENTER;
	}
	
	/**
	 * Call this to play exit scale animation
	 */
	public void exit(){
		if(mInfo != null){
			postEndAnimation();
		}
		mAnimationType = ANIMATION_EXIT;
	}
	
	private void postInitAnimation(){
		setX(mInfo.x);
		setY(mInfo.y);
		mXPositionAnimator = createPositionAnimator(POSITION_TYPE_X, mInfo.x,0);
		mYPositionAnimator = createPositionAnimator(POSITION_TYPE_Y, mInfo.y,0);
		mWidthAnimator = createSizeAnimator(SIZE_TYPE_W, mPreviewImageWidth,mScreenWidth);
		mHeightAnimator = createSizeAnimator(SIZE_TYPE_H,mPreviewImageHeight,mScreenHeight);
		AnimatorSet set = new AnimatorSet();
		set.setDuration(ANIMATION_DURATION);
		set.addListener(this);
		set.playTogether(mXPositionAnimator,mYPositionAnimator,mWidthAnimator,mHeightAnimator);
		set.start();
		
	}
	
	private void postEndAnimation(){
		mXPositionAnimator = createPositionAnimator(POSITION_TYPE_X, 0,mInfo.x);
		mYPositionAnimator = createPositionAnimator(POSITION_TYPE_Y, 0,mInfo.y);
		mWidthAnimator = createSizeAnimator(SIZE_TYPE_W, mScreenWidth,mPreviewImageWidth);
		mHeightAnimator = createSizeAnimator(SIZE_TYPE_H,mScreenHeight,mPreviewImageHeight);
		AnimatorSet set = new AnimatorSet();
		set.setDuration(ANIMATION_DURATION);
		set.addListener(this);
		set.playTogether(mXPositionAnimator,mYPositionAnimator,mWidthAnimator,mHeightAnimator);
		set.start();
	}
	
	private ValueAnimator createPositionAnimator(int positionType,int startPosition,int endPosition){
		ValueAnimator animator = ValueAnimator.ofInt(startPosition,endPosition);
		final int type = positionType;
		animator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				int position = (int) animation.getAnimatedValue();
				if(type == POSITION_TYPE_X){
					setX(position);
				}else if(type == POSITION_TYPE_Y){
					setY(position);
				}
			}
		});
	 return animator;
	}
	
	private ValueAnimator createSizeAnimator(int sizeType,int startSize,int endSize){
		ValueAnimator animator = ValueAnimator.ofInt(startSize,endSize);
		final int type = sizeType;
		animator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				int size = (int) animation.getAnimatedValue();
				if(mLayoutParams == null){
					mLayoutParams = getLayoutParams();
				}
				if(type == SIZE_TYPE_W){
					mLayoutParams.width = size;
				}else if(type == SIZE_TYPE_H){
					mLayoutParams.height = size;
				}
				setLayoutParams(mLayoutParams);
			}
		});
	 return animator;
	}

	
	@Override
	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		// TODO Auto-generated method stub
			if(mCallback != null){
				mCallback.onAnimationEnd(mAnimationType);
		}
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub
		
	}
	
	

}
