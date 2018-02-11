package com.mst.wallpaper.managerservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class WallpaperManagerServices extends Service{

	private static final String TAG = "MstWallpaperManager";

	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	
	
	private final IWallpaperManagerServices.Stub mBinder = new IWallpaperManagerServices.Stub() {
		
		@Override
		public boolean applySystemKeyguardWallpaper(String wallpaperName)
				throws RemoteException {
			// TODO Auto-generated method stub
			Log.d(TAG, "applySystemKeyguardWallpaper-->"+wallpaperName);
			return false;
		}
		
		@Override
		public boolean applySystemDesktopWallpaper(int wallpaperId)
				throws RemoteException {
			// TODO Auto-generated method stub
			Log.d(TAG, "applySystemDesktopWallpaper-->"+wallpaperId);
			return false;
		}
		
		@Override
		public boolean applyCustomKeyguardWallpaper(String path)
				throws RemoteException {
			// TODO Auto-generated method stub
			Log.d(TAG, "applyCustomKeyguardWallpaper-->"+path);
			return false;
		}
		
		@Override
		public boolean applyCustomDesktopWallpaper(String path)
				throws RemoteException {
			// TODO Auto-generated method stub
			Log.d(TAG, "applyCustomDesktopWallpaper-->"+path);
			return false;
		}
	}; 

}
