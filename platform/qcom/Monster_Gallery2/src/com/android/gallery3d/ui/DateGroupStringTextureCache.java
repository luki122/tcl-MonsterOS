package com.android.gallery3d.ui;

/*
 * This file is added by ShenQianfeng on 2016.06.25
 */

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.android.gallery3d.glrenderer.CustomStringTexture;
import com.android.gallery3d.util.LogUtil;


public class DateGroupStringTextureCache {
    
    private static final String TAG = "DateGroupStringTextureCache";
    
    private static final int CACHE_SIZE = 20;
    private static final int LEFT_SIZE = CACHE_SIZE / 5;
    
    private static HashMap<String, WeakReference<CustomStringTexture>> mCache = new HashMap<String, WeakReference<CustomStringTexture>>();
  
    private static void deleteInvalidItems(){
        ArrayList<String> keys = new ArrayList<String>();
        for (Map.Entry<String, WeakReference<CustomStringTexture>> entry : mCache.entrySet()) {
            if(null == entry.getValue().get()){
                keys.add(entry.getKey());
                //LogUtil.d(TAG, "deleteInvalidItems 1111111111111 mCache.add() ---: " + entry.getKey());
            }
        }
        
        if(keys.size() <= LEFT_SIZE ||  keys.size() == mCache.size()){
            //LogUtil.d(TAG, "deleteInvalidItems 222222222222222222 mCache.clear();");
            mCache.clear();
            keys.clear();
            return;
        }
        
        for (String key : keys) {
            //LogUtil.d(TAG, "deleteInvalidItems 333333333333333 mCache.remove() ---: " + key);
            mCache.remove(key);
        }
    }
    
    public static void clear(){
        mCache.clear();
    }
    
    /**
     * 
     * @param context
     * @param yyyyMMText
     * @param dayText
     * @param yearText
     * @param monthText
     * @param dateMode
     * @param languageCode see CustomStringTexture
     * @return
     */
    public static CustomStringTexture getStringTexture(Context context, String yyyyMMText, String dayText, String yearText, String monthText,  boolean dateMode, int languageCode) {
        String key = yyyyMMText + dayText + yearText + monthText + (dateMode ? "1" : "0") + languageCode;
        WeakReference<CustomStringTexture> ref = mCache.get(key);
        
        CustomStringTexture nt = null;
        if(ref != null){
            nt = ref.get();
            if(nt != null) {
                //LogUtil.d(TAG, "reuse custom string texture : " + key);
                return nt;
            } else {
                //LogUtil.d(TAG, "reuse custom string texture : " + key + " ref not null , but content released");
            }
        } else {
            //LogUtil.d(TAG, "reuse custom string texture ref:" + key + " ref is null....");
        }
        if(CACHE_SIZE <= mCache.size()){
            deleteInvalidItems();
        }
        nt = new CustomStringTexture(context, yyyyMMText, dayText, yearText, monthText,  dateMode, languageCode);
        WeakReference<CustomStringTexture> wt = new WeakReference<CustomStringTexture>(nt);
        mCache.put(key, wt);
        //LogUtil.i(TAG, "PUT custom string texture : " + key);
        return nt;
    }
}