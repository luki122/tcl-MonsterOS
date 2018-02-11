package com.mst.thememanager.job;

import android.util.Log;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.ThemeManagerTask.LocalThemeLoaderBridge;

public class LocalThemeLoader extends ThemeLoader{
	 private static final ThreadPool sThreadPool = new ThreadPool(0, 2);
	
	
	    private LocalThemeLoaderBridge mLocalThemeBridge;
	    
	    
	    private int mType = Theme.THEME_NULL;

        public LocalThemeLoader(LocalThemeLoaderBridge bridge) {
            mLocalThemeBridge = bridge;
        }
        
		@Override
		protected Future<Theme> submitThemeTask(FutureListener<Theme> l) {
			// TODO Auto-generated method stub
			return sThreadPool.submit(mLocalThemeBridge.theme.requestThemeInfo(mLocalThemeBridge.getPath()), this);
		}

		@Override
		protected void onLoadComplete(Theme theme) {
			// TODO Auto-generated method stub
			mLocalThemeBridge.onLoadComplete(theme);
		}

	
}
