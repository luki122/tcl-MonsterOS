package com.mst.wallpaper.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import mst.utils.DisplayUtils;
import mst.widget.ViewPager;
import mst.widget.ViewPager.OnPageChangeListener;
import mst.widget.toolbar.Toolbar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.graphics.Bitmap;

import com.mst.wallpaper.R;

import android.view.Window;

import com.mst.wallpaper.imageutils.AsyncTask;
import com.mst.wallpaper.adapter.KeyguardWallpaperPreviewAdapter;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker.ImageLoaderCallback;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.BitmapUtils;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.task.WidgetColorThread;
import com.mst.wallpaper.utils.task.WidgetColorThread.OnColorPickerListener;
import com.mst.wallpaper.widget.CirclePageIndicator;
import com.mst.wallpaper.widget.KeyguardPreviewLayout;
import com.mst.wallpaper.widget.TimeWidget;
public class KeyguardWallpaperPreviewFragment extends PreviewFragment implements OnPageChangeListener, 
ImageLoaderCallback,OnColorPickerListener{

	

	private Toolbar mToolbar;
	private ImageResizer mImageResizer;
	private KeyguardWallpaperPreviewAdapter mAdapter;
	private ViewPager mViewPager;
	private KeyguardPreviewLayout mTimeView;
	private Handler mHander = new Handler();
	private Wallpaper mWallpaper;
	private int mImageCount;
	private int mPositionInList = -1;
	private int mInitPositon;
	
	private HashMap<Integer,Integer> mColorCache = new HashMap<Integer,Integer>();
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.keyguard_wallpaper_preview_layout, container,false);
		mToolbar = getToolbar();
		mViewPager = (ViewPager)view.findViewById(R.id.wallpaper_preview_pager);
		mTimeView = (KeyguardPreviewLayout)view.findViewById(R.id.preview_layout);
		initImageCache(null);
		handleIntent();
		return view;
	}
	
	@Override
	public void setToolbar(Toolbar toolbar) {
		// TODO Auto-generated method stub
		super.setToolbar(toolbar);
		toolbar.inflateMenu(R.menu.wallpaper_set_done);
	}
	
	
	
	private void initImageCache(String updatePath) {
		final int height = DisplayUtils.getHeightPixels(getContext());
		final int width = DisplayUtils.getWidthPixels(getContext());
		mImageResizer = new ImageResizer(getContext(), width / 2, height / 2);
		mImageResizer.setImageLoaderCallback(this);
		mImageResizer.addImageCache((Activity)getContext(),
				Config.WALLPAPER_PREVIEW_IMAGE_CACHE_DIR, updatePath);
	}
	
	
	
	
	
	@Override
	public void postIntent() {
		// TODO Auto-generated method stub
		
	}
	
	private void handleIntent(){
		Intent intent = getActivity().getIntent();
		int imagePosition = intent.getIntExtra(Config.Action.KEY_KEYGUARD_WALLPAPER_PREVIEW_IAMGE_POSITION, 0);
		mInitPositon = imagePosition;
		mWallpaper = intent.getParcelableExtra(Config.Action.KEY_WALLPAPER_PREVIEW_DATA_LIST);
		mPositionInList = intent.getIntExtra(Config.Action.KEY_KEYGUARD_WALLPAPER_PREVIEW_POSITION_IN_LIST, -1);
		if(mWallpaper != null){
			ArrayList<Object> images = new ArrayList<Object>();
			mImageCount = mWallpaper.getWallpaperCount();
			
			if(mImageCount > 0){
				for(int i = 0 ;i < mImageCount;i++){
					images.add(mWallpaper.getObjectByKey(i));
				}
			}
			mAdapter = new KeyguardWallpaperPreviewAdapter(images, getContext());
			mAdapter.setToolbar(getToolbar());
			mAdapter.setImageResizer(mImageResizer);
		}
		if(mAdapter != null){
			mViewPager.setAdapter(mAdapter);
			mTimeView.setViewPager(mViewPager);
			mTimeView.setOnPageChangeListener(this);
			mViewPager.setCurrentItem(imagePosition);
		}
		getWallpaperActivity().setCurrentWallpaper(mWallpaper, mPositionInList);
		setupTitle(imagePosition);
	}


	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	



	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		setWidgetColor(position);		
		setupTitle(position);
	}
	
	private void setupTitle(int position){
		mToolbar.setTitle(getContext().getResources()
				.getString(R.string.keyguard_wallpaper_preview_title, mWallpaper.name,position+1,mImageCount));
		mTimeView.onPageSelected(position);
		
	}
	
	private synchronized void updateWidgetColor(int position) {
		ImageView iv = mAdapter.getItemImageView(position);
		WidgetColorThread task = new WidgetColorThread(iv,position);
		task.setOnColorPickerListener(this);
		task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, null);
	}

	@Override
	public Wallpaper getWallpaper(int position) {
		// TODO Auto-generated method stub
		return mWallpaper;
	}
	
	private void setWidgetColor(int position){
		if(mColorCache.containsKey(position)){
			int color = mColorCache.get(position);
			mTimeView.setBlackStyle(false,color);
			updateNavigationBar(color);
		}
	}


	@Override
	public void onImageLoad(boolean success, int position) {
		if(mColorCache.containsKey(position)){
			return;
		}
			updateWidgetColor(position);
	}

	@Override
	public void onImageLoadFailed(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onColorPicked(int color,int position) {
		// TODO Auto-generated method stub
		int resultColor = color;
			if(position != -1){
				if(!mColorCache.containsKey(position)){
					mColorCache.put(position, color);
				}else{
					resultColor = mColorCache.get(position);
				}
			}
		mInitPositon = -1;
		setWidgetColor(mViewPager.getCurrentItem());
	}
	

}
