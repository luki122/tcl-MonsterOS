package com.android.gallery3d.util;

import android.graphics.Color;

public class ColorUtil {
    public static int getAnimatedColor(int fromBgColor, int toBgColor, float progress) {
        int fromAlpha = Color.alpha(fromBgColor);
        int fromRed = Color.red(fromBgColor);
        int fromGreen = Color.green(fromBgColor);
        int fromBlue = Color.blue(fromBgColor);
        
        int toAlpha = Color.alpha(toBgColor);
        int toRed = Color.red(toBgColor);
        int toGreen = Color.green(toBgColor);
        int toBlue = Color.blue(toBgColor);
        
        int a = (int)(fromAlpha + ( toAlpha - fromAlpha) * progress);
        int r = (int)(fromRed + ( toRed - fromRed) * progress);
        int g = (int)(fromGreen + ( toGreen - fromGreen) * progress);
        int b = (int)(fromBlue + ( toBlue - fromBlue) * progress);
        
        //LogUtil.i("ColorUtil", "a:" + a + " r:" + r + " b:" + b + " g:" + g);
        
        return Color.argb(a, r, g, b);
    }
    
    private static int getSrcOverDstColorDimen(int bgColor, int fgColor, float alpha) {
        return (int)((1 - alpha) * bgColor  + alpha * fgColor);  
    }
    
    public static int getSrcOverDstColor(int bgColor, int fgColor, float fgAlpha) {
        int bgRed = Color.red(bgColor);
        int bgGreen = Color.green(bgColor);
        int bgBlue = Color.blue(bgColor);
        
        int fgRed = Color.red(fgColor);
        int fgGreen = Color.green(fgColor);
        int fgBlue = Color.blue(fgColor);

        int a = 255;//(int)(255 * fgAlpha);
        int r = getSrcOverDstColorDimen(bgRed, fgRed, fgAlpha);
        int g = getSrcOverDstColorDimen(bgGreen, fgGreen, fgAlpha);
        int b = getSrcOverDstColorDimen(bgBlue, fgBlue, fgAlpha);
        
        //LogUtil.i2("ColorUtil", "a:" + a + " r:" + r + " b:" + b + " g:" + g);
        
        return Color.argb(a, r, g, b);
    }
}
