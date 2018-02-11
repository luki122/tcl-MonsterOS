package com.monster.launcher.blur;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * Created by antino on 16-8-17.
 */
public class BlurFactory {
    static IBlur mIBlur =null;
    public static Bitmap  blur(Context context, Bitmap source, float scaleFactor, int radius){
        if(mIBlur==null){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
                mIBlur = new RenderscriptBlur();
            }else{
                mIBlur = new FastBlur();
            }
        }
        return mIBlur.blur(context,source,scaleFactor,radius);
    }
}
