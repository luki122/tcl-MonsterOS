package com.monster.launcher.theme.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.monster.launcher.theme.interfaces.Contents;

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

}
