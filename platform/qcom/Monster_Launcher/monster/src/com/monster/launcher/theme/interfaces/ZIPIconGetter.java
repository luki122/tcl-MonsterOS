package com.monster.launcher.theme.interfaces;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;

import com.monster.launcher.Log;
import com.monster.launcher.compat.UserHandleCompat;
import com.monster.launcher.theme.ZIPThemeConfigParseByPull;
import com.monster.launcher.theme.utils.PhotoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by antino on 16-11-8.
 */
public class ZIPIconGetter extends IconGetterAbsImpl{

    public static String TAG = ZIPIconGetter.class.getSimpleName();

    private static String THEME_LOCAL_PATH = "/data/monster/theme/current/icons";
    private static String THEME_CONFIG_PATH = "/config.xml";

    private String mPath;
    private String locaDrawablelPath;
    private String locaConfigPath;

    private String mIconDpiFolder;
    private String mIconDpiFolderHigh;
    private String mIconDpiFolderUnder;

    Bitmap mask_regular;
    Bitmap bg;
    Bitmap mask_unregular;

    public ZIPIconGetter(){

    }
    public ZIPIconGetter(Context context){
//        init(context);
    }
    @Override
    public boolean init(Context context) {
        mPath = THEME_LOCAL_PATH;//Environment.getExternalStorageDirectory() +
        if (context == null || mPath == null) {
            Log.e(TAG, "ZIPIconGetter init failed context="+context+",mPath="+mPath);
            return false;
        }
        mPreContext = context;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        mIconDpi = activityManager.getLauncherLargeIconDensity();
        mIconDpiFolder = getDrawableDpiFolder(mIconDpi, 0);
        mIconDpiFolderHigh = getDrawableDpiFolder(mIconDpi, 1);
        mIconDpiFolderUnder = getDrawableDpiFolder(mIconDpi, -1);
        locaDrawablelPath = mPath + mIconDpiFolder;
        locaConfigPath = mPath + THEME_CONFIG_PATH;

        ZIPThemeConfigParseByPull tp = new ZIPThemeConfigParseByPull();
        try {
            File file = new File(locaConfigPath);
            if (file.exists()) {
//                InputStream is = context.getAssets().open("config.xml");
//                InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream(locaConfigPath);
//                InputStream is = mPreContext.getClassLoader().getResourceAsStream(locaConfigPath);
                InputStream instream = new FileInputStream(locaConfigPath);
                if (instream == null) {
                    Log.e(TAG, "can't create inputStream");
                    return false;
                }
                tp.parse(instream);
            } else {
                Log.e(TAG, "can't find config.xml path : " + locaConfigPath);
                return false;
            }
            if (!tp.hasData()) return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "init IOException : " + e.toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "init Exception : " + e.toString());
            return false;
        }
        mLabel_Icons = tp.getmLabel_Icons();
        Log.d(TAG, "mLabel_Icons size : " + mLabel_Icons.size());
        mPositionInBg = new Rect();
        isHeteromorphicTheme = tp.isHeteromorphicTheme();
        mPositionInBg.top = tp.getpTop();
        mPositionInBg.left = tp.getpLeft();
        mPositionInBg.right = tp.getpRight();
        mPositionInBg.bottom = tp.getpBottom();
        themeName = tp.getThemeName();
        themeVersion = tp.getThemeVersion();
        return true;
    }

    /**
     * get icon from sdcard by iconName
     * @param iconName
     * @return
     */
    @Override
    protected Drawable getResurceDrawable(String iconName) {
        String iconPath = locaDrawablelPath + "/" + iconName + ".png";
        File f = new File(iconPath);
        Log.e(TAG,"f:"+f.getPath()+",can read : " + f.canRead()+",can write : "+f.canWrite());
        Drawable drawable = null;
        float scale = 1.0f;
        if (!f.exists()) {
            iconPath = mPath + mIconDpiFolderHigh + "/" + iconName + ".png";
            scale = getScaleFroDpi(mIconDpi, true);
            f = new File(iconPath);
            if (!f.exists()) {
                iconPath = mPath + mIconDpiFolderUnder + "/" + iconName + ".png";
                scale = getScaleFroDpi(mIconDpi, false);
                f = new File(iconPath);
                if (!f.exists()) {
                    return null;
                }
            }
        }
        drawable = Drawable.createFromPath(iconPath);
        if(drawable == null)return null;
        if(scale!=1.0f){
            drawable = PhotoUtils.zoomDrawable(mPreContext.getResources(),drawable,scale);
            Log.e(TAG,"Drawable iconName:"+iconName+",scale:"+scale + ",icon size : " + drawable.getIntrinsicWidth() + "," + drawable.getIntrinsicHeight());
        }
        return drawable;
    }

    public Bitmap getBitmapByName(String iconN,UserHandle user) {
        String iconName = iconN;
        boolean isCurrentUser = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if(!user.equals(UserHandleCompat.myUserHandle().getUser())){
                isCurrentUser = false;
            }
        }
        if(!isCurrentUser){
            iconName = iconName+"_clone";
            Log.e(TAG,"getBitmapByName iconName 1 : " + iconName);
        }
        Bitmap result = null;
        float scale = 1.0f;
        try {
            String iconPath = locaDrawablelPath + "/" + iconName + ".png";
            File f = new File(iconPath);
            if (!f.exists()) {
                iconPath = mPath + mIconDpiFolderHigh + "/" + iconName + ".png";
                scale = getScaleFroDpi(mIconDpi, true);
                f = new File(iconPath);
                if (!f.exists()) {
                    iconPath = mPath + mIconDpiFolderUnder + "/" + iconName + ".png";
                    scale = getScaleFroDpi(mIconDpi, false);
                    f = new File(iconPath);
                    if (!f.exists()) {
                        if(!isCurrentUser){
                            iconPath = locaDrawablelPath + "/" + iconN + ".png";
                            f = new File(iconPath);
                            if (!f.exists()) {
                                iconPath = mPath + mIconDpiFolderHigh + "/" + iconN + ".png";
                                scale = getScaleFroDpi(mIconDpi, true);
                                f = new File(iconPath);
                                if (!f.exists()) {
                                    iconPath = mPath + mIconDpiFolderUnder + "/" + iconN + ".png";
                                    scale = getScaleFroDpi(mIconDpi, false);
                                    f = new File(iconPath);
                                    if (!f.exists()) {
                                        return null;
                                    }
                                }
                            }
                        }else {
                            return null;
                        }
                    }
                }
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            result = BitmapFactory.decodeStream(new FileInputStream(f), null, options).copy(Bitmap.Config.ARGB_8888, true);
            Log.d(TAG, "Bitmap iconName:" + iconName + ",icon size : " + result.getWidth() + "," + result.getHeight());
            if (scale != 1.0f) {
                Log.e(TAG, "Bitmap iconName:" + iconName + ",scale:" + scale);
                result = PhotoUtils.zoom(result, scale);
            }
        } catch (Exception e) {
            Log.i(TAG, "getBitmapByName() :", e);
            e.printStackTrace();
            return null;
        }
        return result;
    }

    protected Drawable standardThirdPardIcon(Drawable source){
        Bitmap sr = null;
        if(source instanceof BitmapDrawable){
            sr = ((BitmapDrawable) source).getBitmap();
        }else{
            sr =  PhotoUtils.drawable2bitmap(source);
        }
        return standardThirdPardIconBitmap(sr);
    }
    private Drawable standardThirdPardIconBitmap(Bitmap source){
        if(source==null||source.getWidth()<=0||source.getHeight()<=0)return null;
        mask_regular = getBitmapByName(Contents.MASK_REGULAR,UserHandleCompat.myUserHandle().getUser());
        bg = getBitmapByName(Contents.BACKGROUND,UserHandleCompat.myUserHandle().getUser());
        mask_unregular = getBitmapByName(Contents.MASK_UNREGULAR,UserHandleCompat.myUserHandle().getUser());
        if (mask_regular == null || bg == null || mask_unregular == null)
            return new BitmapDrawable(res, source);
        Rect bound = new Rect();
        //The source opaque area
        int area;
        int opaqueWidth;
        int opaqueHeight;
            area = PhotoUtils.calcClipBounds(source,bound);
        opaqueWidth = bound.right - bound.left;
        opaqueHeight = bound.bottom - bound.top;
        Bitmap result;
        if(isHeteromorphicTheme){
            result = PhotoUtils.compositeBitmap(source,mask_unregular,bg,bound,mPositionInBg);
        }else{
            if (area > 0.91f*opaqueWidth * opaqueHeight&&opaqueWidth > 0.97f * opaqueHeight && opaqueHeight>0.97*opaqueWidth) {
                android.util.Log.i("realScale","  deal rectangle ");
                result = PhotoUtils.compositeBitmap(source,mask_regular,bg,bound,null);
            }else{
                android.util.Log.i("realScale"," deal circle ");
                result = PhotoUtils.compositeBitmap(source,mask_unregular,bg,bound,null);
            }
        }
        return (result==null?null:new BitmapDrawable(res,result));
    }

    /**
     *
     * @param dpi
     * @param level -1:under 0:cur 1:high
     * @return
     */
    public String getDrawableDpiFolder(int dpi ,int level){
        String dpiFolder;
        if(level == -1){
            if(dpi <= 120){
                dpiFolder = "/drawable-ldpi";
            }else if(dpi <= 160){
                dpiFolder = "/drawable-ldpi";
            }else if(dpi <= 240){
                dpiFolder = "/drawable-mdpi";
            }else if(dpi <= 320){
                dpiFolder = "/drawable-hdpi";
            }else if(dpi <= 480){
                dpiFolder = "/drawable-xhdpi";
            }else {
                dpiFolder = "/drawable-xxhdpi";
            }
        }else if(level == 1){
            if(dpi <= 120){
                dpiFolder = "/drawable-mdpi";
            }else if(dpi <= 160){
                dpiFolder = "/drawable-hdpi";
            }else if(dpi <= 240){
                dpiFolder = "/drawable-xhdpi";
            }else if(dpi <= 320){
                dpiFolder = "/drawable-xxhdpi";
            }else if(dpi <= 480){
                dpiFolder = "/drawable-xxxhdpi";
            }else {
                dpiFolder = "/drawable-xxxhdpi";
            }
        }else{
            if(dpi <= 120){
                dpiFolder = "/drawable-ldpi";
            }else if(dpi <= 160){
                dpiFolder = "/drawable-mdpi";
            }else if(dpi <= 240){
                dpiFolder = "/drawable-hdpi";
            }else if(dpi <= 320){
                dpiFolder = "/drawable-xhdpi";
            }else if(dpi <= 480){
                dpiFolder = "/drawable-xxhdpi";
            }else {
                dpiFolder = "/drawable-xxxhdpi";
            }
        }

        return dpiFolder;
    }

    public float getScaleFroDpi(int dpi ,boolean isHight){
        float scale = 1.0f;
        if(isHight){
            if(dpi <= 120){
                scale = 120/160f;
            }else if(dpi <= 160){
                scale = 160/240f;
            }else if(dpi <= 240){
                scale = 240/320f;
            }else if(dpi <= 320){
                scale = 320/480f;
            }else if(dpi <= 480){
                scale = 480/640f;
            }else {
                scale = 1.0f;
            }
        }else {
            if(dpi <= 120){
                scale = 1.0f;
            }else if(dpi <= 160){
                scale = 160/120;
            }else if(dpi <= 240){
                scale = 240/160;
            }else if(dpi <= 320){
                scale = 320/240;
            }else if(dpi <= 480){
                scale = 480/320f;
            }else {
                scale = 640/480f;
            }
        }
        return scale;
    }

}
