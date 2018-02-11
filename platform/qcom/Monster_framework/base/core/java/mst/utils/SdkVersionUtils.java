
package mst.utils;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;


public class SdkVersionUtils {
    private SdkVersionUtils() {};

    @TargetApi(11)
    public static void enableStrictMode() {
    }

    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    
    public static boolean hasExternalStorage() {
    	return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    
}
