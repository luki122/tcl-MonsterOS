package com.mst.wallpaper.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.activity.SetDesktopWallpaperActivity;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbColumns;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.utils.task.KeyguardWallpaperHandler;
import com.mst.wallpaper.utils.task.KeyguardWallpaperHandlerImpl;
import com.mst.wallpaper.utils.task.KeyguardWallpaperHandlerView;
import com.mst.wallpaper.widget.CropImageView;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.mst.wallpaper.R;

public class ThemeWallpaperReceiver extends BroadcastReceiver implements OnRequestListener,KeyguardWallpaperHandlerView{

	private static final String TAG = "ThemeWallpaperReceiver";
	private static final String ACTION = "monster.intent.action.APPLY_WALLPAPER_FROM_THEME";
	private static final String KEY_WALLPAPER_FROM_THEME = "system_wallpaper_from_theme";
	private static final String KEY_CUSTOM_WALLPAPER = "custom_wallpaper_from_theme";
	private static final String KEY_KEYGUARD_WALLPAPER_FROM_THEME = "system_keyguard_wallpaper_from_theme";
	private static final String KEY_KEYGUARD_CUSTOM_WALLPAPER = "custom_keyguard_wallpaper_from_theme";
	private static final String KEY_THEME_NAME = "theme_name";
	private WallpaperManager mWm;
	private KeyguardWallpaperHandlerImpl mWallpaperHandler;
	@Override
	public void onReceive(Context context, Intent intent) {
		mWallpaperHandler = new  KeyguardWallpaperHandlerImpl(this,context);
		// TODO Auto-generated method stub
		String action = intent.getAction();
		mWm = WallpaperManager.getInstance();
		if(Config.DEBUG){
			Log.d(TAG, "apply theme wallpaper--->"+action);
		}
		if(ACTION.equals(action)){
			int wallpaperNumber = intent.getIntExtra(KEY_WALLPAPER_FROM_THEME, -1);
			String keyguardGroupName = intent.getStringExtra(KEY_KEYGUARD_WALLPAPER_FROM_THEME);
			if(Config.DEBUG){
				Log.d(TAG, "wallpaperNumber--->"+wallpaperNumber+"  keyguardGroupName-->"+keyguardGroupName);
			}
			if(wallpaperNumber != -1){
				if(wallpaperNumber < Config.LOCAL_DESKTOP_WALLPAPERS.length){
					mWm.addWallpaperHandleListener(this.getClass().getName(), this);
					String wallpaperPath = Config.LOCAL_DESKTOP_WALLPAPERS[wallpaperNumber];
					Wallpaper wallpaper = new Wallpaper();
					wallpaper.type = Wallpaper.TYPE_DESKTOP;
					wallpaper.addPaths(Config.SYSTEM_DESKTOP_WALLPAPER+wallpaperPath);
					mWm.applyWallpaper(wallpaper, context, this.getClass().getName(), wallpaperNumber);
				}
		        
			}else{
				String customWallpaperPath = intent.getStringExtra(KEY_CUSTOM_WALLPAPER);
				if(!TextUtils.isEmpty(customWallpaperPath)){
					mWm.addWallpaperHandleListener(this.getClass().getName(), this);
					File wallpaperFile = new File(customWallpaperPath);
					if(wallpaperFile.exists()){
						applyCustomDeskWallpaper(customWallpaperPath, context);
					}
				}
			}
			if(!TextUtils.isEmpty(keyguardGroupName)){
				mWm.addWallpaperHandleListener(this.getClass().getName(), this);
				WallpaperDbController wdbc = new WallpaperDbController(context);
				Wallpaper w = wdbc.queryKeyguardWallpaperByName(keyguardGroupName);
				w.type = Wallpaper.TYPE_KEYGUARD;
				mWm.applyWallpaper(w, context, this.getClass().getName(), Integer.MAX_VALUE);
			}else{
				String customKeyguardWallpaper = intent.getStringExtra(KEY_KEYGUARD_CUSTOM_WALLPAPER);
				if(!TextUtils.isEmpty(customKeyguardWallpaper)){
					mWm.addWallpaperHandleListener(this.getClass().getName(), this);
					File wallpaperFile = new File(customKeyguardWallpaper);
					if(wallpaperFile.exists()){
						applyCustomKeyguardWallpaper(customKeyguardWallpaper, context);
					}
				}
			}
		}

	}
	
	@Override
	public void onSuccess(Object data, Object t, int statusCode) {
		// TODO Auto-generated method stub
		mWm.removeWallpaperHandleListener(this.getClass().getName(), this);
	}
	@Override
	public void onStartRequest(Object data, int statusCode) {
		// TODO Auto-generated method stub
		
	}
	
	private String newFileName(File oldFile) {
       	StringBuffer newName = new StringBuffer();
			if (oldFile != null) {
				newName.append("monster_").append(oldFile.getPath().hashCode()).append("_").append(oldFile.getName());
			}
			return newName.toString();
		}
	
	private void applyCustomDeskWallpaper(String path,Context context){
    	if (path != null) {
    		File oldFile = new File(path);
			File newFile = new File(SetDesktopWallpaperActivity.WALLPAPER_DIR, newFileName(oldFile));
			try {
				//if (!WALLPAPER_DIR.exists()) {
				SetDesktopWallpaperActivity.WALLPAPER_DIR.mkdirs();
				//}
        		if (!newFile.exists()) {
        			newFile.createNewFile();
				}
            } catch (IOException e) {
                Log.e(TAG, "fail to create new file: "
                        + newFile.getAbsolutePath(), e);
            }
		    FileUtils.copyFile(path, newFile.getPath(),context);
        	long now = System.currentTimeMillis() / 1000;
            ContentValues values = new ContentValues();
            values.put(Config.WallpaperStored.WALLPAPER_MODIFIED, now);
            values.put(Config.WallpaperStored.WALLPAPER_OLDPATH, path);
            values.put(Config.WallpaperStored.WALLPAPER_FILENAME, newFile.getName());
            Cursor cursor = null;
			
            try {
            	if(Config.DEBUG){
					Log.d(TAG, "applyCustomDeskWallpaper begin--->"+path);
				}
				cursor = context.getContentResolver().query(Config.WallpaperStored.LOCAL_WALLPAPER_URI, new String[] { Config.WallpaperStored.WALLPAPER_FILENAME }, 
						Config.WallpaperStored.WALLPAPER_OLDPATH + " = '" + path + "'", null, null);
				if (cursor != null && cursor.moveToNext()) {
					context.getContentResolver().update(Config.WallpaperStored.LOCAL_WALLPAPER_URI, values, Config.WallpaperStored.WALLPAPER_OLDPATH + " = '" + path + "'", null);
				}else {
					context.getContentResolver().insert(Config.WallpaperStored.LOCAL_WALLPAPER_URI, values);
				}
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(SharePreference.KEY_SELECT_DESKTOP_PATH, newFile.getName());
				editor.commit();
				Wallpaper wallpaper = new Wallpaper(Wallpaper.TYPE_DESKTOP);
				wallpaper.addPaths(newFile.getAbsolutePath());
				if(Config.DEBUG){
					Log.d(TAG, "applyCustomDeskWallpaper end--->"+newFile.getAbsolutePath());
				}
				mWm.applyWallpaper(wallpaper, context, this.getClass().getName(), -2);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
    
	}
	
	private void applyCustomKeyguardWallpaper(String path,Context context){
		ArrayList<String> paths = new ArrayList<String>();
		File file = new File(path);
		if(file.isDirectory()){
			File[] files = file.listFiles();
			if(files != null && files.length > 0){
				for(File f : files){
					paths.add(f.getAbsolutePath());
				}
			}
		}
		
		if(paths.size() > 0){
			if(Config.DEBUG){
				Log.d(TAG, "applyCustomKeyguardWallpaper begin --->"+paths.get(0));
			}
			mWallpaperHandler.setImageList(paths);
			mWallpaperHandler.setWallpaperName(context.getResources().getString(R.string.current_theme_keyguard_wallpaper));
			mWallpaperHandler.setFromTheme();
			for(int i = 0;i< paths.size();i++){
				mWallpaperHandler.cropWallpaper(null, i, null);
			}
		}
	}

	@Override
	public void onWallpaperIntentHandled(List<String> images, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View getItemView(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshStatus(boolean finish) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	

}
