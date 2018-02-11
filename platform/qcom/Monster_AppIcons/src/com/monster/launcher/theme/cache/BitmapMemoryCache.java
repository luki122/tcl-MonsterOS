package com.monster.launcher.theme.cache;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;
/**
 * Created by antino on 16-11-8.
 */
public class BitmapMemoryCache {
    private LruCache<String, Bitmap> mMemoryCache;
    private Resources res;
    public BitmapMemoryCache(Resources res){
        this.res = res;
        int memorySize = 1024*1024*4;
        mMemoryCache = new LruCache<String,Bitmap>(memorySize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount()/1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemCache(key)==null){
            mMemoryCache.put(key,bitmap);
        }
    }

    public  Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public Drawable getDrawableFromMemCache(String key){
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap==null?null:new BitmapDrawable(res,bitmap);
    }
}
