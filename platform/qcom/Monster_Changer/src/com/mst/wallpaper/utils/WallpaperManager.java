package com.mst.wallpaper.utils;

import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import mst.utils.DisplayUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mst.wallpaper.MainWorker;
import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.MainWorker.WallpaperHolder;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.object.WallpaperImageInfo;
import com.mst.wallpaper.object.WallpaperThemeInfo;

public class WallpaperManager {
	
	private static final String TAG = "WallpaperManager";
	
	private static WallpaperManager mInstance;
	
	private static Object mLock = new Object();
	
	private MainWorker mWorker;
	
	private WallpaperManager(){
		
		mWorker = MainWorker.getMainWorker();
	};
	
	public static  WallpaperManager getInstance(){
		synchronized (mLock) {
			if(mInstance == null){
				mInstance = new WallpaperManager();
			}
			
			return mInstance;
		}
	}
	

	private MainWorker getMainWorker(){
		if(mWorker == null){
			mWorker = MainWorker.getMainWorker();
		}
		return mWorker;
	}
	
	
	public static  String getCurrentKeyguardPaperPath(Context context,String currentWallpaper){
		return WallpaperTimeUtils.getCurrentKeyguardWallpaperPaperPath(context, currentWallpaper);
	}
	
	
    /**
     * 更新锁屏壁纸信息
     * @param dbController
     * @param wallpaperThemeInfo
     * @param imageCount
     */
    public void updateKeyguardWallpaper(WallpaperDbController dbController, WallpaperThemeInfo wallpaperThemeInfo, int imageCount) {
    	Wallpaper keyguardWallpaper = new Wallpaper();
        keyguardWallpaper.name = (wallpaperThemeInfo.name);
        keyguardWallpaper.themeColor = (wallpaperThemeInfo.nameColor);
        keyguardWallpaper.isDefaultTheme = ("false".equals(wallpaperThemeInfo.isDefault)? 0 : 1);
        keyguardWallpaper.isTimeBlack = ("false".equals(wallpaperThemeInfo.timeBlack)? 0 : 1);
        keyguardWallpaper.isStatusBarBlack = ("false".equals(wallpaperThemeInfo.statusBarBlack)? 0 : 1);
        keyguardWallpaper.count = (imageCount);
        dbController.insertWallpaper(keyguardWallpaper, false);
    	
    }

   
    /**
     * 更新指定锁屏壁纸中图片的信息
     * @param dbController
     * @param wallpaperName
     * @param pictureTitle
     * @param path
     */
    public void updateKeyguardWallpaperThemeInfo(WallpaperDbController dbController, 
    		String wallpaperName, String pictureTitle, String path) {
        Wallpaper belong_group = dbController.queryKeyguardWallpaperByName(wallpaperName);
        int belong_id = belong_group.id;
        WallpaperImageInfo imageInfo = new WallpaperImageInfo();
        imageInfo.setBelongGroup(belong_id);
        imageInfo.setIdentify(pictureTitle);
        imageInfo.setBigIcon(path);
        dbController.insertWallpaperImage(imageInfo);
    }
    
    public void deleteKeyguardWallpaperByName(Context context, String wallpaperName) {
        String path = Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH + wallpaperName;
        FileUtils.deleteDirectory(path);
        WallpaperDbController dbControl = new WallpaperDbController(context);
        Wallpaper wallpaper = dbControl.queryKeyguardWallpaperByName(wallpaperName);
        dbControl.deleteWallpaperByName(wallpaperName);
        dbControl.close();
    }
    
    public void deleteKeyguardWallpaperByName(List<Wallpaper> deleteDatas) {
		// TODO Auto-generated method stub
		WallpaperHolder holder = new WallpaperHolder();
		holder.wallpapers = deleteDatas;
		holder.type = Wallpaper.TYPE_KEYGUARD;
		getMainWorker().requestDeleteWallpaper(holder);
	}

    
    
    /**
     * 应用壁纸，不管是桌面壁纸还是锁屏壁纸的应用都通过这个方法去实现，在方法内部会
     * 通过壁纸的类型see{@link Wallpaper.type}去区分
     * @param wallpaper
     * @param context
     * @param callbackName
     * @param position
     */
    public void applyWallpaper(final Wallpaper wallpaper ,final Context context,String callbackName,final int position){
    	WallpaperHolder holder = new WallpaperHolder();
    	holder.position = position;
    	holder.wallpaper = wallpaper;
    	if(!TextUtils.isEmpty(callbackName)){
    		holder.callbackName = callbackName;
    	}
    	mWorker.requestSetWallpaper(holder);
    }

    public void loadDeskWallpaper(String listenerName,Context context){
    	WallpaperHolder holder = new WallpaperHolder();
    	if(!TextUtils.isEmpty(listenerName)){
    		holder.callbackName = listenerName;
    	}
    	mWorker.requestLoadDesktopWallpaper(holder);
    }
    
    public int getAppliedKeyguardWallpaperId(Context context){
    	return SharePreference.getIntPreference(context,Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER_ID, -1);
    }
    
    /**
     * Get position of applied wallpaper int desktop wallpaper list
     * @param context
     * @return
     */
    public int getAppliedWallpaperPosition(Context context){
    	return SharePreference.getIntPreference(context, SharePreference.KEY_SELECT_DESKTOP_POSITION, -1);
    }
    
    /**
     * Get wallpaper file path of applied .
     * @param context
     * @return
     */
    public String getAppliedWallpaperPath(Context context){
    	return SharePreference.getStringPreference(context, SharePreference.KEY_SELECT_DESKTOP_PATH, "");
    }
    
    /**
     * 添加壁纸处理的回调，通过这个回调可以让在子线程工作的业务和主线程(UI线程)进行交互，
     * 然后将操作的状态或者数据返回给调用方，添加的时候必须给该回调指定一个名称，这个名称
     * 是唯一的
     * @param listenerName
     * @param listener
     */
    public void addWallpaperHandleListener(String listenerName,OnRequestListener listener){
    	if(mWorker == null){
    		mWorker = MainWorker.getMainWorker();
    	}
    	mWorker.addRequestListener(listener, listenerName);
    }
    
    public void removeWallpaperHandleListener(String listenerName,OnRequestListener listener){
    	if(mWorker == null){
    		mWorker = MainWorker.getMainWorker();
    	}
    	mWorker.removeRequestListener(listener,listenerName);
    }
  
    public void deleteWallpaper(List<Wallpaper> wallpapes){
    	if(wallpapes == null || wallpapes.size() ==0){
    		return;
    	}
    	else{
    		WallpaperHolder holder = new WallpaperHolder();
    		holder.wallpapers = wallpapes;
    		mWorker.requestDeleteWallpaper(holder);
    	}
    }

	
    
}
