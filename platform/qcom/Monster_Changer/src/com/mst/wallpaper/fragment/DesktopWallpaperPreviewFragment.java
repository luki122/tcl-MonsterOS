package com.mst.wallpaper.fragment;

import java.util.ArrayList;

import mst.utils.DisplayUtils;
import mst.widget.PagerAdapter;
import mst.widget.ViewPager;
import mst.widget.toolbar.Toolbar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Window;
import com.mst.wallpaper.imageutils.AsyncTask;
import com.mst.wallpaper.ActivityPresenter;
import com.mst.wallpaper.activity.WallpaperPreviewActivity;
import com.mst.wallpaper.adapter.WallpaperPreviewAdapter;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker.ImageLoaderCallback;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.presenter.PreviewContract;
import com.mst.wallpaper.presenter.PreviewPresenter;
import com.mst.wallpaper.presenter.PreviewContract.Presenter;
import com.mst.wallpaper.utils.BitmapUtils;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.loader.IconPreviewLoader;
import com.mst.wallpaper.utils.task.WidgetColorThread;
import com.mst.wallpaper.utils.task.WidgetColorThread.OnColorPickerListener;
import com.mst.wallpaper.R;
public class DesktopWallpaperPreviewFragment extends PreviewFragment implements
ViewPager.OnPageChangeListener,ImageLoaderCallback,OnColorPickerListener{

	private View mContentView;
	
	private ViewPager mViewPager;
	
	private LinearLayout mPreViewIconBottomParent;
	
	private LinearLayout mPreViewIconTopParent;
	private IconPreviewLoader mIconPreviewLoader;
	private WallpaperPreviewAdapter mAdapter;
	private Handler mHandler = new Handler();
	private ActivityPresenter mPresenter;
	
	private ArrayList<Wallpaper> mWallpaper;
	private Intent mIntent;

	private int mWallpaperType;

	private int mCurrentPosition;

	private ImageResizer mImageResizer;
	
	private ImageView mWidgetImage;
	
	private Handler mColorHandler = new Handler();
	private volatile int mColoredPosition = 0;
	private int mInitWidgetColor = Color.WHITE;
	boolean mFromIntent = false;
	private Runnable mColorWidgetRunnable = new Runnable(){
		@Override
		 public void run() {
			 updateWidgetColor(mColoredPosition);
			 
		 };
	};
	
	private Runnable mPreviewIconRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mIconPreviewLoader.setupWallPaperPreviewBottomIcons(mPreViewIconBottomParent);
			mIconPreviewLoader.setupWallpaperPreviewTopIcons(mPreViewIconTopParent);
			updateWidgetColorInner(mInitWidgetColor);
			mFromIntent = false;
		}
	};
	
	private void initImageCache(String updatePath) {
		final int height = DisplayUtils.getHeightPixels(getActivity());
		final int width = DisplayUtils.getWidthPixels(getActivity());
		mImageResizer = new ImageResizer(getActivity(), width / 2, height / 2);
		mImageResizer.setImageLoaderCallback(this);
		mImageResizer.addImageCache(getActivity(),
				Config.WALLPAPER_PREVIEW_IMAGE_CACHE_DIR, updatePath);
	}
	
	private void handleIntent(){
		 mIntent = getActivity().getIntent();
		mWallpaperType = mIntent.getIntExtra(
				Config.Action.KEY_WALLPAPER_PREVIEW_TYPE, Wallpaper.TYPE_OTHER);
		mCurrentPosition = mIntent.getIntExtra(
				Config.Action.KEY_WALLPAPER_PREVIEW_POSITION, 0);
		mInitWidgetColor = mIntent.getIntExtra(Config.Action.KEY_WALLPAPER_PREVIEW_WIDGET_INIT_COLOR, mInitWidgetColor);
		mWallpaper = mIntent
				.getParcelableArrayListExtra(Config.Action.KEY_WALLPAPER_PREVIEW_DATA_LIST);
		mFromIntent = true;
		if (mWallpaper != null) {
			mAdapter = new WallpaperPreviewAdapter(getActivity(),
					mWallpaper, mWallpaperType);
			mAdapter.setImageResizer(mImageResizer);
			mAdapter.setToolbar(getToolbar());
			mViewPager.setAdapter(mAdapter);
			mViewPager.setCurrentItem(mCurrentPosition);
			setCurrentWallpaper(mCurrentPosition);
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.desktop_wallpaper_preview_activity, container,false);
		mIconPreviewLoader = new IconPreviewLoader(getContext());
		initImageCache(null);
		initialView();
		handleIntent();
		return mContentView;
	}
	
	private void initialView(){
		mViewPager = (ViewPager)mContentView.findViewById(R.id.wallpaper_preview_pager);
		mPreViewIconBottomParent = (LinearLayout)mContentView.findViewById(R.id.bottom);
        mPreViewIconTopParent = (LinearLayout)mContentView.findViewById(R.id.top);
        mWidgetImage = (ImageView)mContentView.findViewById(R.id.preview_widget);
		mViewPager.setOnPageChangeListener(this);
		mHandler.post(mPreviewIconRunnable);
	}
	
	
	@Override
	public void postIntent(){
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	

	
	
	@Override
	public void setToolbar(Toolbar toolbar) {
		// TODO Auto-generated method stub
		super.setToolbar(toolbar);
		toolbar.inflateMenu(R.menu.wallpaper_set_done);
	}


	@Override
	public void onPageScrollStateChanged(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		setCurrentWallpaper(position);
		if(!mFromIntent){
			mColoredPosition = position;
			mColorHandler.post(mColorWidgetRunnable);
		}
	}
	
	private void setCurrentWallpaper(int position){
		getWallpaperActivity().setCurrentWallpaper(getWallpaper(position),position);
	}
	
	private void updateWidgetColor(int position) {
		ImageView iv = mAdapter.getItemImageView(mViewPager.getCurrentItem());
		WidgetColorThread task = new WidgetColorThread(iv);
		task.setOnColorPickerListener(this);
		task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, null);
	}
	
	private void updateWidgetColorInner(int color){
		mWidgetImage.getDrawable().setTint(color);
		mIconPreviewLoader.setIconNameColor(color);
		updateNavigationBar(color);
	}
	
	
	
	@Override
	public Wallpaper getWallpaper(int position){
		
		return ((WallpaperPreviewAdapter)mViewPager.getAdapter()).getWallpaper(position);
	}

	@Override
	public void onImageLoad(boolean success, int position) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onImageLoadFailed(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onColorPicked(int color,int position) {
		// TODO Auto-generated method stub
		updateWidgetColorInner(color);
	}

	
}