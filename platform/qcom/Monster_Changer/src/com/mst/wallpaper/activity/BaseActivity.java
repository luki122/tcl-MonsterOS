package com.mst.wallpaper.activity;

import android.content.ContentValues;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker.ImageLoaderCallback;

import mst.app.MstActivity;
import mst.utils.DisplayUtils;

public class BaseActivity extends MstActivity {
	
	

	/**
	 * 创建图片加载器
	 * @param cacheDir 缓存目录
	 * @param scale   缩放比例
	 * @param callback  加载回调
	 * @return
	 */
	public ImageResizer initImageResizer(String cacheDir,float scale,ImageLoaderCallback callback){
        final int height = DisplayUtils.getHeightPixels(this);
        final int width = DisplayUtils.getWidthPixels(this);
        ImageResizer imageResizer = new ImageResizer(this, (int)(width/scale), (int)(height/scale));
        imageResizer.addImageCache(this, cacheDir);
        imageResizer.setImageLoaderCallback(callback);
        return imageResizer;
	}
	
}
