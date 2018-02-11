package com.mst.wallpaper.activity;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mst.wallpaper.imageutils.AsyncTask;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker.ImageLoaderCallback;
import com.mst.wallpaper.presenter.PresenterBridge;
import com.mst.wallpaper.presenter.PresenterBridge.DrawablePresenter;
import com.mst.wallpaper.R;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.loader.DrawableLoader;
import com.mst.wallpaper.utils.loader.IconPreviewLoader;
import com.mst.wallpaper.utils.task.WidgetColorThread;
import com.mst.wallpaper.utils.task.WidgetColorThread.OnColorPickerListener;

public class MainActivity extends BaseActivity implements View.OnClickListener,
PresenterBridge.DrawableView,OnColorPickerListener{

	public static final String TAG = "MainActivity";
	private static final String KEYGUARD_THUMB_CACHE = "main_page_keyguard";
	private static final float KEYGUARD_THUMB_SCALE = 1.0f;
	private static final int PERMISSION_REQUEST_CODE = 100;

	private ImageView mWallpaper;
	private ImageView mKeyguardWallpaper;
	
	
	private ImageResizer mImageResize;
	
	private LinearLayout mPreViewIconBottomParent;
	private WallpaperManager mWm ;
	private LinearLayout mPreViewIconTopParent;
	private IconPreviewLoader mIconPreviewLoader;
	private WidgetColorThread mWallpaperColorPicker;
	private WidgetColorThread mKeyguardWallpaperColorPicker;
	private ImageView mKeyguardTimeWidget;
	private ImageView mLauncherWidget;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setMstContentView(R.layout.main_activity);
		mWm = WallpaperManager.getInstance(this);
		initial(savedInstanceState);
	}

	private void initial(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mIconPreviewLoader = new IconPreviewLoader(this,true);
		mWallpaper = (ImageView)findViewById(R.id.wallpaper);
		mKeyguardWallpaper = (ImageView)findViewById(R.id.keyguard_wallpaper);
		mPreViewIconBottomParent = (LinearLayout)findViewById(R.id.bottom);
        mPreViewIconTopParent = (LinearLayout)findViewById(R.id.top);
        mKeyguardTimeWidget = (ImageView)findViewById(R.id.keyguard_time_widget_small);
        mLauncherWidget = (ImageView)findViewById(R.id.launcher_widget_small);
		mWallpaper.setOnClickListener(this);
		mKeyguardWallpaper.setOnClickListener(this);
		mIconPreviewLoader.setupWallPaperPreviewBottomIcons(mPreViewIconBottomParent);
		mIconPreviewLoader.setupWallpaperPreviewTopIcons(mPreViewIconTopParent);
		mImageResize = initImageResizer(KEYGUARD_THUMB_CACHE, KEYGUARD_THUMB_SCALE, new ImageLoaderCallback() {
			
			@Override
			public void onImageLoadFailed(int position) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onImageLoad(boolean success, int position) {
				// TODO Auto-generated method stub
				updateKeyguardWidgetColor();
			}
		});
		loadWallpaperPreview();
		requestPermission();
	}
	
	
	private void loadWallpaperPreview(){
		if(mKeyguardWallpaper != null && mImageResize != null){
			mImageResize.loadImage(Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH, mKeyguardWallpaper,0);
		}
	}
	
	
	private void requestPermission() {
		checkPermission();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void checkPermission() {
		int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) & 
    			checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    	if(permission != PackageManager.PERMISSION_GRANTED){
    		requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}
    		, PERMISSION_REQUEST_CODE);
    	}
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		if(view == mWallpaper){
			intent.setAction(Config.Action.ACTION_DESKTOP_WALLPAPER_LIST);
		}else{
			intent.setAction(Config.Action.ACTION_KEYGUARD_WALLPAPER_LIST);
		}
		startActivity(intent);
	}

	@Override
	public void updateView(final Drawable wallpaper, Integer status) {
		// TODO Auto-generated method stub
		if(wallpaper != null){
			
			 new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mWallpaper.setImageDrawable(wallpaper);
					updateWidgetColor();
				}
			});
			
		}
	}
	
	@Override
	public void onNavigationClicked(View view) {
		// TODO Auto-generated method stub
		super.onNavigationClicked(view);
		onBackPressed();
	}
	
	private void updateWidgetColor(){
		Drawable wallpaper = mWm.getDrawable();
		mWallpaperColorPicker = new WidgetColorThread(mWallpaper,1);
		mWallpaperColorPicker.setOnColorPickerListener(this);
		mWallpaperColorPicker.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, null);
		
	}
	
	private void updateKeyguardWidgetColor(){
		mKeyguardWallpaperColorPicker = new WidgetColorThread(mKeyguardWallpaper,0);
		mKeyguardWallpaperColorPicker.setOnColorPickerListener(this);
		mKeyguardWallpaperColorPicker.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, null);
	}

	@Override
	public void updateProgress(Integer progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Context getViewContext() {
		// TODO Auto-generated method stub
		return getApplicationContext();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadWallpaperPreview();
		Drawable wallpaper = mWm.getDrawable();
		updateView(wallpaper, 0);
		
	}
	
	
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onColorPicked(int color,int position) {
		// TODO Auto-generated method stub
		if(position == 0){
			mKeyguardTimeWidget.getDrawable().setTint(color);
		}else{
			mIconPreviewLoader.setIconNameColor(color);
			mLauncherWidget.getDrawable().setTint(color);
		}
	}
	
	
	
	
}
