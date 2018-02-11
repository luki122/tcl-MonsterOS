package com.mst.wallpaper.utils.task;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mst.wallpaper.imageutils.AsyncTask;
import com.mst.wallpaper.utils.BitmapUtils;

public class WidgetColorThread extends AsyncTask<Drawable, Integer, Integer>{

	private WeakReference<ImageView> mTargetImageView;
	private OnColorPickerListener mListener;
	private Bitmap mDrawable;
	private volatile int mPosition = -1;
	/**
	 * 
	 *Callback for pick color from bitmap
	 */
	public interface OnColorPickerListener{
		/**
		 * @param color  color picked from bitmap
		 */
		public void onColorPicked(int color,int position);
	}
	
	public void setOnColorPickerListener(OnColorPickerListener listener){
		mListener = listener;
	}
	public  WidgetColorThread(ImageView imageView) {
		// TODO Auto-generated constructor stub
		this(imageView,-1);
	}
	
	public  WidgetColorThread(ImageView imageView,int position) {
		// TODO Auto-generated constructor stub
		mTargetImageView = new WeakReference<ImageView>(imageView);
		mPosition = position;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		ImageView imageView = mTargetImageView.get();
		if(imageView != null){
			final Drawable imageDrawable = imageView.getDrawable();
			if(imageDrawable != null){
				mDrawable = BitmapUtils.drawable2bitmap(imageDrawable);
			}
		}
	}
	
	@Override
	protected Integer doInBackground(Drawable... params) {
		// TODO Auto-generated method stub
		if(mDrawable == null){
			return Color.WHITE;
		}
		//long start = System.currentTimeMillis();
		final int color = BitmapUtils.calcTextColor(mDrawable);
		//Log.d("picker", "calcTextColor-->"+(System.currentTimeMillis() - start));
		mDrawable.recycle();
		mDrawable = null;
		return color;
	}

	
	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if(mListener != null){
			mListener.onColorPicked(result.intValue(),mPosition);
		}
	}
	
	
	
}
