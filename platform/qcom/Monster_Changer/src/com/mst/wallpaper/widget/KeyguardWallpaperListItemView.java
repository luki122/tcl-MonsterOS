package com.mst.wallpaper.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class KeyguardWallpaperListItemView extends ViewGroup{

	private static final String TAG = "KeyguardWallpaperListItemView";
	private static final int MAX_CHILD  = 5;
	
	private int mImageIndex = 0;
	
	
	private LayoutInflater mInflater;
	
	
	public KeyguardWallpaperListItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public KeyguardWallpaperListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public KeyguardWallpaperListItemView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	
	public KeyguardWallpaperListItemView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	
	
	
	
	private ImageView  createImageView(){
		if(mInflater == null){
			mInflater = LayoutInflater.from(getContext());
		}
		switch (mImageIndex) {
		case 0:
			
			break;
		case 1:
			
			break;
		case 2:
			
			break;
		case 3:
			
			break;
		case 4:
		
		break;
		}		
		return null;
	}
	
	/**
	 * add an image into item to show
	 * @param drawable
	 */
	public void addDrawable(Drawable drawable){
		if(mImageIndex > MAX_CHILD){
			Log.e(TAG, "Keyguard Wallpaper List Item only show less than 6 images");
			return;
		}
		mImageIndex ++;
		postInvalidate();
	}
	

	/**
	 * see {@link #addDrawable(Drawable drawable)} 
	 * @param bitmap
	 */
	public void addBitmap(Bitmap bitmap){
		addDrawable(new BitmapDrawable(getResources(),bitmap));
	}
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		final LayoutParams params = getLayoutParams();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = 0;
        int desiredHeight = 0;
        
	}
	
	


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		
	}
	
	
	


}
