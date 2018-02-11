package com.mst.wallpaper.utils.loader;

import java.util.ArrayList;

import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.presenter.PresenterBridge;
import com.mst.wallpaper.presenter.PresenterBridge.DrawableView;

import android.app.WallpaperManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class DrawableLoader implements PresenterBridge.DrawablePresenter {

	
	private static final String KEY_SAVE_LOAD_WALLPAPER = "load_wallpaper";
	
	private static final String KEY_CURRENT_INDEX = "current_index";
	
	/**
	 * V need to load drawable
	 */
	private DrawableView mDrawableView;

	/**
	 * List to save drawable paths.In this case,drawable will load
	 * from file.
	 */
	private ArrayList<String> mDrawablePath = new ArrayList<String>();
	
	/**
	 * If V want to load current wallpaper,set it to true
	 */
	private boolean mLoadWallpaper;
	
	private boolean mStart = false;
	
	private boolean mPaused =false;
	
	private int mCurrentIndex = -1;
	
	private Context mContext;
	
	
	public DrawableLoader(DrawableView view) {
		// TODO Auto-generated constructor stub
		this(view, false);
	}

	public DrawableLoader(DrawableView view, boolean  loadWallpaper) {
		this(view, null,loadWallpaper);
	}

	public DrawableLoader(DrawableView view, ArrayList<String> drawablePath,boolean loadWallpaper) {
		mDrawableView = view;
		if(drawablePath != null && drawablePath.size() > 0){
			mDrawablePath.addAll(drawablePath);
		}
		mLoadWallpaper = loadWallpaper;
		mContext = view.getViewContext();
	}

	@Override
	public void onCreate(Bundle onSaveInstance) {
		// In this situation,do nothing here
	}

	@Override
	public void onStart() {
		

	}
	
	@Override
	public void stop() {
		if(mLoadWallpaper){
			return;
		}
		
		if(mPaused){
			return;
		}
		mPaused = true;
		onStop();
	}

	@Override
	public void onStop() {
		if(mStart){
			
		}

	}

	@Override
	public void finish() {
		mStart = false;

	}

	@Override
	public void onDestory() {
		
		mDrawablePath.clear();
		mStart = false;
		mPaused = false;
		mCurrentIndex = -1;

	}
	
	

	@Override
	public void start() {
		mStart = true;
		mPaused = false;
		onStart();
		if(mLoadWallpaper){
			WallpaperManager wm = WallpaperManager.getInstance(mContext);
			if(mDrawableView != null){
				mDrawableView.updateView(wm.getDrawable(), Config.LocalWallpaperStatus.STATUS_LOAD_SUCCESS);
			}
			finish();
		}
		
		if(mCurrentIndex != -1){
			
		}

	}

	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		// TODO Auto-generated method stub
		Parcelable state = BaseSavedState.EMPTY_STATE;
		
		SaveState newState = new SaveState(state);
		newState.loadWallpaper = mLoadWallpaper;
		newState.currentIndex = mCurrentIndex;
		instanceState.putParcelable(this.getClass().getName(), newState);
		
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		if(state == null || !state.getClass().equals(SaveState.class)){
			return;
		}
		SaveState oldState = new SaveState(state);
		mLoadWallpaper = oldState.loadWallpaper;
		mCurrentIndex = oldState.currentIndex;
	}

	static class SaveState extends BaseSavedState {
		boolean loadWallpaper;
		int currentIndex;

		public SaveState(Parcel source) {
			super(source);
			// TODO Auto-generated constructor stub
			loadWallpaper = source.readInt() == 1;
			currentIndex = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(loadWallpaper ? 1 : 0);
			dest.writeInt(currentIndex);
		}

		public SaveState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SaveState> CREATOR = new Parcelable.Creator<SaveState>() {
			public SaveState createFromParcel(Parcel in) {
				return new SaveState(in);
			}

			public SaveState[] newArray(int size) {
				return new SaveState[size];
			}
		};

	}

	

}
