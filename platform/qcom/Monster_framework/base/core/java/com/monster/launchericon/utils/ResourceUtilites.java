package com.monster.launchericon.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by antino on 16-11-9.
 */
public class ResourceUtilites {
    public static int  getDimen(Context context, String resPkg, String resName, int defaultValue){
        int dimen =1;
        int resId =0;
        Resources resources = context.getResources();
        if (resources != null) {
            resId = resources.getIdentifier(resName, "dimen",
                    resPkg);
        }
        if(resId==0){
            return defaultValue;
        }
        dimen = resources.getDimensionPixelSize(resId);
        Log.d(Contents.TAG, "getDimen: resId = "+resId+" , dimen = "+dimen);
        return dimen;
    }

    public static boolean getBoolean(Context context, String resPkg, String resName, boolean defaultValue){
        boolean  result =false;
        int resId =0;
        Resources resources = context.getResources();
        if (resources != null) {
            resId = resources.getIdentifier(resName, "bool",
                    resPkg);

        }
        if(resId==0){
            return false;
        }
        result = resources.getBoolean(resId);
        Log.i(Contents.TAG,"resId = "+resId+"  resourceName = "+resName+"  ,  context = "+context+"  , result =  "+result);
        return result;
    }

    public static Drawable getIconDrawable(String packageName, String resourceName,
                                           Context context, int iconDpi) {
        Drawable drawable = null;
        Resources resources = context.getResources();
        if (resources != null) {
            int resId = resources.getIdentifier(resourceName, "drawable",
                    packageName);
            Log.i(Contents.TAG,"resId = "+resId);
            if (resId == 0) {
                return null;
            }
            drawable = resources.getDrawableForDensity(resId,iconDpi);
            Log.d(Contents.TAG, "getDimen: resId = "+resId+" , dimen = "+drawable);
        }
        return drawable;
    }
    public static String getString(Context context, String resPkg, String resName){
        String  result = "";
        int resId =0;
        Resources resources = context.getResources();
        if (resources != null) {
            resId = resources.getIdentifier(resName, "string",
                    resPkg);
        }
        if(resId==0){
            return null;
        }
        result = resources.getString(resId);
        Log.i(Contents.TAG,"getString resId = "+resId+"  resourceName = "+resName+"  ,  context = "+context+"  , result =  "+result);
        return result;
    }
}
