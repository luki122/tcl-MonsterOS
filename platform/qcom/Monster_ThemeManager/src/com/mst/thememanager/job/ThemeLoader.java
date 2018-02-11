package com.mst.thememanager.job;

import android.graphics.Bitmap;

import com.mst.thememanager.entities.Theme;

public abstract class ThemeLoader implements FutureListener<Theme>{
     private static final int STATE_INIT = 0;
	 private static final int STATE_REQUESTED = 1;
	 private static final int STATE_LOADED = 2;
	 private static final int STATE_ERROR = 3;
	 private static final int STATE_RECYCLED = 4;

	 private int mState = STATE_INIT;
	 // mTask is not null only when a task is on the way
	 private Future<Theme> mTask;
	 private Theme mTheme;

	 @Override
	 public void onFutureDone(Future<Theme> future) {
	     synchronized (this) {
	         mTask = null;
	         mTheme = future.get();
	         if (mState == STATE_RECYCLED) {
	             if (mTheme != null) {
	                 ThemePool.getInstance().put(mTheme);
	                 mTheme = null;
	             }
	             return; // don't call callback
	         }
	         if (future.isCancelled() && mTheme == null) {
	             if (mState == STATE_REQUESTED) mTask = submitThemeTask(this);
	             return; // don't call callback
	         } else {
	             mState = mTheme == null ? STATE_ERROR : STATE_LOADED;
	         }
	     }
	     onLoadComplete(mTheme);
	 }

	 public synchronized void startLoad() {
	     if (mState == STATE_INIT) {
	         mState = STATE_REQUESTED;
	         if (mTask == null) mTask = submitThemeTask(this);
	     }
	 }

	 public synchronized void cancelLoad() {
	     if (mState == STATE_REQUESTED) {
	         mState = STATE_INIT;
	         if (mTask != null) mTask.cancel();
	     }
	 }

	 // Recycle the loader and the theme
	 public synchronized void recycle() {
	     mState = STATE_RECYCLED;
	     if (mTheme != null) {
	    	 ThemePool.getInstance().put(mTheme);
	         mTheme = null;
	     }
	     if (mTask != null) mTask.cancel();
	 }

	 public synchronized boolean isRequestInProgress() {
	     return mState == STATE_REQUESTED;
	 }

	 public synchronized boolean isRecycled() {
	     return mState == STATE_RECYCLED;
	 }

	 public synchronized Theme getBitmap() {
	     return mTheme;
	 }

	 abstract protected Future<Theme> submitThemeTask(FutureListener<Theme> l);
	 abstract protected void onLoadComplete(Theme theme);
	
	
}
