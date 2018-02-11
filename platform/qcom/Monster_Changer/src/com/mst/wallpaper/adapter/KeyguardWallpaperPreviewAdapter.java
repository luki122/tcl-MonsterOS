package com.mst.wallpaper.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker;
import com.mst.wallpaper.imageutils.ImageWorker.ImageLoaderCallback;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.BitmapUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import mst.widget.PagerAdapter;
import mst.widget.ViewPager;

import com.mst.wallpaper.R;
public class KeyguardWallpaperPreviewAdapter extends AbsViewPagerAdapter {

	private List<Object> mImagePath ;
	private List<View> mViews;
	private ViewPager mViewPager;
	
	private ImageResizer mImageResizer;
	
	private Context mContext;
	
	
	
	public KeyguardWallpaperPreviewAdapter(List<Object> images,Context context){
		mContext = context;
		if(images != null && images.size() > 0){
			mImagePath = images;
		}
		mViews = initLayout(mImagePath);
	}
	
	public ImageView getItemImageView(int position){
		View view = mViews.get(position);
		return (ImageView) view
				.findViewById(R.id.wallpaper_preview_item_background);
	}
	
	@Override
	public int getCount() {
		if (mViews != null) {
			return mViews.size();
		}
		return 0;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		ViewPager viewPager = ((ViewPager) container);
		viewPager.addView(mViews.get(position));
		refreshData(position);
		return mViews.get(position);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		ViewPager viewPager = (ViewPager) container;
		ImageView mImageView = (ImageView) mViews.get(position).findViewById(
				R.id.wallpaper_preview_item_background);
		if (mImageView != null) {
			ImageWorker.cancelWork(mImageView);
			mImageView.setImageDrawable(null);
		}
		viewPager.removeView(mViews.get(position));
	}


	public void setViewPager(ViewPager viewPager) {
		mViewPager = viewPager;
	}
	
	public void setImageResizer(ImageResizer resizer){
		mImageResizer = resizer;
	}

	public List<View> initLayout(List<Object> wallpapers) {
		int size = wallpapers.size();
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < size; i++) {
			View view = View.inflate(mContext,
					R.layout.desktop_wallpaper_preview_item, null);
			views.add(view);
		}
		return views;
	}

	public void refreshData(int position) {
		View view = mViews.get(position);
		ImageView iv = (ImageView) view
				.findViewById(R.id.wallpaper_preview_item_background);
		iv.setOnClickListener(this);
//		if(position == 0){
			mImageResizer.loadImage(mImagePath.get(position), iv,position);
//		}else{
//			mImageResizer.loadImage(mImagePath.get(position), iv);
//		}
	}

	

	
	
	
}
