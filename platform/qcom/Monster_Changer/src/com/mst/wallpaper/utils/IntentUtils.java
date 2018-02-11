package com.mst.wallpaper.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.mst.wallpaper.activity.WallpaperPreviewActivity;
import com.mst.wallpaper.object.Wallpaper;

public class IntentUtils {

	public static final int REQUEST_PICK_DESKTOP_WALLPAPER_CODE = 100;
	public static final int REQUEST_PICKER_KEYGUARD_WALLPAPER = 101;
	public static final String IMAGE_TYPE="image/*";
	
	public static Intent buildWallpaperPreviewIntent(int position,int wallpaperType,ArrayList<Wallpaper> wallpapers){
		Intent intent = new Intent();
    	intent.putExtra(Config.Action.KEY_WALLPAPER_PREVIEW_POSITION, position);
    	intent.putExtra(Config.Action.KEY_WALLPAPER_PREVIEW_TYPE, wallpaperType);
    	intent.putParcelableArrayListExtra(Config.Action.KEY_WALLPAPER_PREVIEW_DATA_LIST, wallpapers);
    	return intent;
	}
	
	public static Intent buildWallpaperPreviewIntent(int position,int wallpaperType,Wallpaper wallpaper){
		Intent intent = new Intent();
    	intent.putExtra(Config.Action.KEY_WALLPAPER_PREVIEW_POSITION, position);
    	intent.putExtra(Config.Action.KEY_WALLPAPER_PREVIEW_TYPE, wallpaperType);
    	intent.putExtra(Config.Action.KEY_WALLPAPER_PREVIEW_DATA_LIST, wallpaper);
    	return intent;
	}
	
	public static Intent buildPickerDesktopWallpaperIntent(){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("cn.tcl.filemanager", "cn.tcl.filemanager.photopicker.ImagePickerPlusActivity"));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setType(IMAGE_TYPE);
		
		return intent;
	}
	
	public static Intent buildPickerKeyguardWallpaperIntent(){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("cn.tcl.filemanager", "cn.tcl.filemanager.photopicker.ImagePickerPlusActivity"));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}
	
}
