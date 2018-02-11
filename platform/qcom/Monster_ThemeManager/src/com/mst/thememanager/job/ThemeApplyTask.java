package com.mst.thememanager.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageInstallObserver.Stub;
import android.net.Uri;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.mst.thememanager.database.SharePreferenceManager;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeApplyListener;
import com.mst.thememanager.utils.CommonUtil;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.DensityUtils;
import com.mst.thememanager.utils.FileUtils;

public class ThemeApplyTask extends IPackageInstallObserver.Stub implements Runnable ,IPackageDeleteObserver{

	private static final int POST_RESULT_TIME = 300;
	
	private Theme mTheme;
	private Handler mHandler = new Handler();
	private volatile boolean running = true;
	private OnThemeApplyListener mListener;
	private Context mContext;
	private boolean mUserApk = false;
	private HashMap<String, ArrayList<String>> mSystemDefaultIconMap = new HashMap<String, ArrayList<String>>();
	private String mWallpaperDensityDir;
	public void setTheme(Theme theme){
		mTheme = theme;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mSystemDefaultIconMap.clear();
		File systemIconsFile = new File(Config.THEME_APPLY_ICON_DIR);
		File systemIconBackupFile = new File(Config.THEME_APPLY_ICON_BACKUP_DIR);
		
		if(systemIconBackupFile.exists() && systemIconBackupFile.isDirectory()){
			String[] childFiles = systemIconBackupFile.list();
			if(childFiles != null && childFiles.length > 0){
				for(String density:childFiles){
					if(density.endsWith("xml")){
						continue;
					}
					ArrayList<String> iconList = new ArrayList<String>();
					File iconDir = new File(Config.THEME_APPLY_ICON_BACKUP_DIR+density);
					String[] icons = iconDir.list();
					if(icons != null && icons.length > 0){
						for(String icon : icons){
							iconList.add(icon);
						}
					}
					mSystemDefaultIconMap.put(density, iconList);
				}
			}
		}
			int density = DensityUtils.getBestDensity();
			mWallpaperDensityDir = DensityUtils.getMatchedDrawableDir(density);
			extractThemeFiles();
	}
	
	public void setWallpaper(Theme theme){
		int systemWallpaperNumber = theme.systemWallpaperNumber;
		String systemKeyguardWallpaper = theme.systemKeyguardWallpaperName;
		Intent intent = new Intent();
		intent.setAction("monster.intent.action.APPLY_WALLPAPER_FROM_THEME");
		if(systemWallpaperNumber != -1){
			intent.putExtra("system_wallpaper_from_theme", systemWallpaperNumber);
		}else{
			File file = new File(Config.THEME_APPLY_CUSTOM_DESKTOP_WALLPAPER+mWallpaperDensityDir);
			String wallpaperPath = null;
			if(file.exists() && file.isDirectory()){
				File[] wallpapers = file.listFiles();
				if(wallpapers != null && wallpapers.length > 0){
					wallpaperPath = wallpapers[0].getAbsolutePath();
				}
			}
			if(!TextUtils.isEmpty(wallpaperPath)){
				intent.putExtra("custom_wallpaper_from_theme", wallpaperPath);
			}
		}
		
		if(!TextUtils.isEmpty(systemKeyguardWallpaper)){
			intent.putExtra("system_keyguard_wallpaper_from_theme", systemKeyguardWallpaper);
		}else{
			File file = new File(Config.THEME_APPLY_CUSTOM_KEYGUARD_WALLPAPER);
			if(file.exists() && file.isDirectory()){
				File[] children = file.listFiles();
				if(children != null && children.length > 0){
					intent.putExtra("custom_keyguard_wallpaper_from_theme", Config.THEME_APPLY_CUSTOM_KEYGUARD_WALLPAPER);
				}
			}
			
		}
		
		mContext.sendBroadcast(intent);
	}
	
	public void setApplyListener(OnThemeApplyListener listener){
		mListener = listener;
	}
	
	private void extractThemeFiles(){
		
		if(mTheme == null){
			running = false;
			return;
		}
		if(mTheme.id == Config.DEFAULT_THEME_ID){
			mTheme.themeFilePath = "";
		}
		String themeFilePath = mTheme.themeFilePath;
		File themeFile = new File(themeFilePath);
		boolean finish = false;
		if(!themeFile.exists() && mTheme.id != Config.DEFAULT_THEME_ID){
			if(mListener != null){
				mListener.onApply(Config.ThemeApplyStatus.STATUS_THEME_FILE_NOT_EXITS);
			}
			running = false;
			return;
		}else{
			try {
					FileUtils.deleteDirectory(Config.THEME_APPLY_DIR);
					if(mTheme.id == Config.DEFAULT_THEME_ID){
						mTheme.systemKeyguardWallpaperName = Config.DEFAUTL_THEME_KEYGUARD_WALLPAPER;
						mTheme.systemWallpaperNumber = Config.DEFAULT_THEHE_DESKTOP_WALLPAPER;
						FileUtils.copyDirectiory(Config.THEME_DEFAULT_PATH, Config.THEME_APPLY_DIR);
						finish = true;
					}else{
					if (FileUtils.unZipFile(themeFile.getAbsolutePath(),Config.THEME_APPLY_DIR)) {
						try{
						FileUtils.copyDirectiory(Config.THEME_APPLY_ICON_DIR, Config.THEME_APPLY_ICON_DIR_TMP);
					    FileUtils.deleteDirectory(Config.THEME_APPLY_ICON_DIR);
						FileUtils.copyDirectiory(Config.THEME_APPLY_ICON_BACKUP_DIR, Config.THEME_APPLY_ICON_DIR);
						FileUtils.copyDirectiory(Config.THEME_APPLY_ICON_DIR_TMP, Config.THEME_APPLY_ICON_DIR);
						FileUtils.deleteDirectory(Config.THEME_APPLY_ICON_DIR_TMP);

						finish = true;
						}catch(Exception e){
							finish = false;
						}
			/*			File tmpIconDir = new File(Config.THEME_APPLY_ICON_DIR);
						File iconBackupDir = new File(Config.THEME_APPLY_ICON_BACKUP_DIR);
						String[] tmpDensities = tmpIconDir.list();
						for (String tmpDensity : tmpDensities) {
							if (mSystemDefaultIconMap.containsKey(tmpDensity)) {
								File tmpDensityFile = new File(
										tmpIconDir.getAbsolutePath() + "/"
												+ tmpDensity);
								String[] tmpIcons = tmpDensityFile.list();
								File backupDensityFile = new File(
										iconBackupDir.getAbsolutePath() + "/"
												+ tmpDensity);
								String[] backupIcoms = backupDensityFile.list();
								if (backupIcoms.length > tmpIcons.length) {
									List<String> deltaIcons = CommonUtil.compareArray(backupIcoms,tmpIcons);
									 long start = System.currentTimeMillis();
									 Log.d("apply", "copyFile detalIcons size-->"+deltaIcons.size());
									for (String icon : deltaIcons) {
										final String iconPath = backupDensityFile
												.getCanonicalPath()
												+ "/"
												+ icon;
										final String targetPath = tmpDensityFile
												.getAbsolutePath() + "/" + icon;
										FileUtils.copyFile(iconPath,
												targetPath, mContext);
									}
									 long end = System.currentTimeMillis();
									Log.d("apply", "copyFile total time-->"+(end -start));
								}
							}
						}*/
						
						
					} else {
						finish = false;
					}
					
				}
					CommonUtil.chmodFile(Config.THEME_APPLY_DIR);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d("apply", ""+e);
			}
			saveAppliedThemeInfo(mContext, mTheme);
			finishApply(finish);
		}
	}
	
	private void saveAppliedThemeInfo(Context context,Theme theme){
		SharePreferenceManager.setIntPreference(mContext, SharePreferenceManager.KEY_APPLIED_THEME_ID, mTheme.id);
		SharePreferenceManager.setStringPreference(mContext, SharePreferenceManager.KEY_APPLIED_THEME_NAME, mTheme.name);
		SharePreferenceManager.setStringPreference(mContext, SharePreferenceManager.KEY_APPLIED_THEME_PATH, mTheme.themeFilePath);
	}

	private void finishApply(boolean finish){
			if(finish){
			setWallpaper(mTheme);
			ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			am.forceStopPackage(Config.LAUNCHER_PKG_NAME);
	
		   mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mListener != null){
					mListener.onApply(Config.ThemeApplyStatus.STATUS_SUCCESS);
				}
			}
		}, POST_RESULT_TIME);
		}else{
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(mListener != null){
						mListener.onApply(Config.ThemeApplyStatus.STATUS_FAILED);
					}
				}
			}, POST_RESULT_TIME);
		}
	}
	
	public void setContext(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
	}

	@Override
	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void packageDeleted(String packageName, int resultCode) throws RemoteException {
		// TODO Auto-generated method stub
		CommonUtil.intstallApp(mContext, "", new File(Config.LAUNCHER_THEME_APK_NAME), this);
		
	}

	@Override
	public void packageInstalled(String packageName, int resultCode) throws RemoteException {
		// TODO Auto-generated method stub
		if(resultCode == 1){
			FileUtils.deleteFile(Config.LAUNCHER_THEME_APK_NAME);
			setWallpaper(mTheme);
			ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			am.forceStopPackage(Config.LAUNCHER_PKG_NAME);

		   mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mListener != null){
					mListener.onApply(Config.ThemeApplyStatus.STATUS_SUCCESS);
				}
				mContext.startActivity(CommonUtil.getHomeIntent());
			}
		}, POST_RESULT_TIME);
		}else{
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(mListener != null){
						mListener.onApply(Config.ThemeApplyStatus.STATUS_FAILED);
					}
				}
			}, POST_RESULT_TIME);
		}
	}
	

   

}
