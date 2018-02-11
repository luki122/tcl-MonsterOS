package com.mst.wallpaper.utils.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.mst.wallpaper.AbsView;
import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.Presenter;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.presenter.PresenterBridge;
import com.mst.wallpaper.presenter.PresenterBridge.WallpaperView;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperManager;

public class WallpaperLoader implements PresenterBridge.WallpaperPresenter 
,OnRequestListener<WallpaperLoader, ArrayList<Wallpaper>>{

	private static final String RQUEST_KEY = WallpaperLoader.class.getName();
	private WallpaperView mView;
	private WallpaperManager mWallpaperManager;
	
	 public WallpaperLoader(WallpaperView view) {
		// TODO Auto-generated constructor stub
		 mView = view;
		 mWallpaperManager = WallpaperManager.getInstance();
		 mWallpaperManager.addWallpaperHandleListener(RQUEST_KEY, this);
	}
	
	@Override
	public void onCreate(Bundle onSaveInstance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mWallpaperManager.loadDeskWallpaper(RQUEST_KEY, mView.getViewContext());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		mWallpaperManager.removeWallpaperHandleListener(RQUEST_KEY, this);
	}

	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSuccess(ArrayList<Wallpaper> wallpapers, WallpaperLoader t,
			int statusCode) {
			if(statusCode == Config.LocalWallpaperStatus.STATUS_LOAD_SUCCESS){
				if(wallpapers.size() > 0){
					mView.updateView(wallpapers, statusCode);
				}
			}else{
				mView.updateView(wallpapers, Config.LocalWallpaperStatus.STATUS_LOAD_SUCCESS);
			}
	}

	@Override
	public void onStartRequest(ArrayList<Wallpaper> data, int statusCode) {
		// TODO Auto-generated method stub
		onStart();
	}
	
}
