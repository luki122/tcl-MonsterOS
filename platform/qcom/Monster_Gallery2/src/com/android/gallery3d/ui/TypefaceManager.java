package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Typeface;

public class TypefaceManager {
    
    public static Typeface sTypefaceRobotoRegular;
    public static Typeface sTypefaceRobotoLight;
    public static Typeface sTypefaceSourceHanSansCNNormal;
    
    public static final int TFID_MONSTER_REGULAR = 1;
    public static final int TFID_MONSTER_LIGHT = 2;
    public static final int TFID_MONSTER_MEDIUM = 3;
    
    public static Typeface get(Context context, int tfid) {
        switch(tfid) {
        case TFID_MONSTER_LIGHT:
            if(null == sTypefaceRobotoLight ) {
                sTypefaceRobotoLight = Typeface.createFromFile("/system/fonts/Roboto-Light.ttf");
            }
            return sTypefaceRobotoLight;
        case TFID_MONSTER_REGULAR:
            if(null == sTypefaceRobotoRegular ) {
                sTypefaceRobotoRegular = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
            }
            return sTypefaceRobotoRegular;
        case TFID_MONSTER_MEDIUM:
            if(null == sTypefaceSourceHanSansCNNormal ) {
                //sTypefaceSourceHanSansCNNormal = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
                //sTypefaceSourceHanSansCNNormal = Typeface.createFromAsset(context.getAssets(), "SourceHanSansCN-Normal.otf");
                sTypefaceSourceHanSansCNNormal = Typeface.createFromFile("/system/fonts/Monster-Light.ttf");
            }
            return sTypefaceSourceHanSansCNNormal;
        }
        return null;
    }
    
    // TCL BaiYuan Begin on 2016.11.09
    public static Typeface obtain(int textSizeInSp){
        Typeface typeface = null;
        if (textSizeInSp > 33) {
            typeface = Typeface.createFromFile("/system/fonts/Monster-Thin.ttf");
        }else if(textSizeInSp < 24){
            typeface = Typeface.createFromFile("/system/fonts/Monster-Light.ttf");
        }else {
            typeface = Typeface.createFromFile("/system/fonts/Monster-Medium.ttf");
        }
        if (null == typeface) {
            typeface = Typeface.createFromFile("/system/fonts/Roboto-Light.ttf");
        }
        return typeface;
    }
    // TCL BaiYuan End on 2016.11.09
}
