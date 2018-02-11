package com.android.external.plantform;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by bin.zhang2-nb on 1/11/16.
 */
public class ExtStorage {
    private static Class<?> mClassType = null;
    private static Method[] mMethod = new Method[10];

    private static void init() {
        try {
            if (mClassType == null) {
                if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                    mClassType = Class.forName("com.mediatek.storage.StorageManagerEx");
/*
                    Method[] methods = mClassType.getMethods();
                    Log.d("ExStorage", "[init] START" );
                    for (Method method : methods) {
                        Log.d("ExStorage", "[init] " + method);
                    }
                    Log.d("ExStorage", "[init] END" );
*/
                    mMethod[0] = mClassType.getDeclaredMethod("getDefaultPath");
                    mMethod[1] = mClassType.getDeclaredMethod("isExternalSDCard", String.class);
                    mMethod[2] = mClassType.getDeclaredMethod("getExternalStoragePath");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getExternalStoragePath() {
        init();

        String value = null;

        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            try {
                value = (String) mMethod[2].invoke(mClassType);
                Log.d("ExStorage", "[getExternalStoragePath]" + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }
}
