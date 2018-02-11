package com.monster.launchericon.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Created by antino on 16-7-20.
 */
public class PKGIcongetter implements IIconGetter {
    public static String TAG = PKGIcongetter.class.getSimpleName();
    public static String LABEL_ICON_MAP_NAME = "icon_array";
    private static Context mPreContext;
    private static String mThemePakageName;
    private static HashMap<String, String> mLabel_Icons;
    private static  int mIconDpi;
    private static Rect mPositionInBg =null;
    private static boolean isHeteromorphicTheme = false;
    private static final String PTOP = "ptop";
    private static final String PLEFT = "pleft";
    private static final String PRIGHT = "pright";
    private static final String PBOTTOM = "pbottom";
    private static final String HETEROMORPHIC_THEME ="is_heteromorphic_theme";

    public static final String BACKGROUND = "aaa_bg";
    public static final String SHADOW = "aaa_shadow";
    public static final String MASK_REGULAR = "aaa_mask_regular";
    public static final String MASK_UNREGULAR = "aaa_mask_unregular";
    public static final String DEFAULT = "aaa_default";
    private static final String  default_theme_package = "com.monster.launcher";
    private static final String outside_theme_package ="com.monster.launcher.theme";

    private static PKGIcongetter mInstance;
    private PKGIcongetter(){

    }

    public static synchronized PKGIcongetter getInstance(Context context) {
        //if(true)return null;
    	if(mInstance == null){
    		mInstance = new PKGIcongetter();
    	}
        try {
            String currentThemePKG = getCurrentThemePkg(context);
            if (mInstance == null || isThemePackageExist(context,currentThemePKG)) {
                clear();
                mThemePakageName = currentThemePKG;
                init(context);
            }
        }catch (Exception e){
            try{
                clear();
                mThemePakageName = default_theme_package;
                init(context);
            }catch (Exception e2){
            	 Log.d(TAG, "Exception222-->"+e2);
                mInstance = null;
            }
        }
        
        Log.d(TAG, "getter is null-->"+(mInstance == null));
        return mInstance;
    }

    public static String getCurrentThemePkg(Context context){
        return outside_theme_package;
    }

    private static void  clear(){
        if(mLabel_Icons!=null){
            mLabel_Icons.clear();
            mLabel_Icons=null;
        }
        if(mPositionInBg!=null){
            mPositionInBg =null;
        }
        if(mPreContext!=null){
            mPreContext = null;
        }
    }


    private static void init(Context cxt) throws Exception {
        //First,get the Theme app Context
        Context context = mPreContext = creatThemeContext(cxt);
        if (context == null){
            throw new Exception("PKGIcongetter init error at init context.");
        }
        //Second,get the key-value about label-bitmapName
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        mIconDpi = activityManager.getLauncherLargeIconDensity();
        Resources res = context.getResources();
        HashMap<String, String> packageClassMap = new HashMap<String, String>();
        final int resId = res.getIdentifier(LABEL_ICON_MAP_NAME, "array", mThemePakageName);
        Log.d(TAG, "packageName--->"+mThemePakageName);
        if (resId == 0){
            throw new Exception("PKGIcongetter init error at getting icon array.");
        }
        final String[] packageClasseIcons = context.getResources()
                .getStringArray(resId);

        for (String packageClasseIcon : packageClasseIcons) {
            String[] packageClasses_Icon = packageClasseIcon.split("#");
            if (packageClasses_Icon.length == 2) {
                String[] packageClasses = packageClasses_Icon[0].split("\\|");
                for (String s : packageClasses) {
                    packageClassMap.put(s.trim(), packageClasses_Icon[1]);
                    String[] packageClass = s.split("\\$");
                    if (packageClass.length == 2) {
                        packageClassMap.put(packageClass[0],
                                packageClasses_Icon[1]);
                    }
                }
            }
        }
        mLabel_Icons = packageClassMap;
        mPositionInBg = new Rect();
        int top = PhotoUtils.getDimen(mThemePakageName,PTOP,mPreContext,0);
        int left = PhotoUtils.getDimen(mThemePakageName,PLEFT,mPreContext,0);
        int right = PhotoUtils.getDimen(mThemePakageName,PRIGHT,mPreContext,0);
        int bottom = PhotoUtils.getDimen(mThemePakageName,PBOTTOM,mPreContext,0);
        isHeteromorphicTheme = PhotoUtils.getBoolean(mThemePakageName,HETEROMORPHIC_THEME,mPreContext,false);
        mPositionInBg.top = top;
        mPositionInBg.left = left;
        mPositionInBg.right = right;
        mPositionInBg.bottom = bottom;
    }

    private static Context creatThemeContext(Context context){
        if(mThemePakageName!=null&&mThemePakageName.equals(context.getPackageName())){
            return context;
        }
        Context themeContext = null;
        try {
            themeContext = context.createPackageContext(mThemePakageName, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
        } catch (Exception e) {
            Log.e(TAG, "Create Res Apk Failed");
        }
        return themeContext;
    }

    @Override
    public Bitmap getIcon(ResolveInfo info) {
        Bitmap result = null;
        if (info.activityInfo != null) {
            result = getIcon(info.activityInfo);
        } else if (info.serviceInfo != null) {
            result = getIcon(info.serviceInfo);
        }
        if (result == null) {
            result = getDefaultIcon();
        }
        return result;
    }

    @Override
    public Bitmap getIcon(ActivityInfo info) {
        Bitmap result = null;
        String iconName = getIconName(info);
        if (iconName != null) {
            result = getBitmapByName(iconName);
        }
        if (result == null) {
            result = getIconFromApkOrDefault(info);
        }
        return result;
    }

    @Override
    public Bitmap getIcon(ServiceInfo info) {
        Bitmap result = null;
        String iconName = getIconName(info);
        if (iconName != null) {
            result = getBitmapByName(iconName);
        }

        if (result == null) {
            result = getIconFromApkOrDefault(info);
        }
        return result;
    }

    @Override
    public Bitmap getIcon(String pkg, String cls) {
        Bitmap result = null;
        String iconName = getIconName(pkg, cls);
        if (iconName != null) {
            result = getBitmapByName(iconName);
        }
        if (result == null) {
            result = getIconFromApkOrDefault(pkg, cls);
        }
        return result;
    }

    @Override
    public Bitmap getIcon(String pkg) {
        Bitmap result = null;
        String iconName = getIconName(pkg);
        if (iconName == null) {
            CharSequence key = getAppLabel(pkg);
            if (key != null) {
                iconName = getIconName(key.toString());
            }
        }
        if (iconName != null) {
            result = getBitmapByName(iconName);
        }
        if (result == null) {
            ApplicationInfo info = getApplicationInfo(pkg);
            result = getIconFromApkOrDefault(info);
        }
        return result;
    }

    @Override
    public Bitmap getIcon(ComponentName componentName) {
        return getIcon(componentName.getPackageName(),componentName.getClassName());
    }


    private Bitmap getDefaultIcon() {
        return getBitmapByName(DEFAULT);
    }

    @Override
    public Drawable getIconDrawable(ResolveInfo info) {
        return new BitmapDrawable(mPreContext.getResources(),getIcon(info));
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info) {
        return new BitmapDrawable(mPreContext.getResources(),getIcon(info));
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info) {
        return new BitmapDrawable(mPreContext.getResources(),getIcon(info));
    }

    @Override
    public Drawable getIconDrawable(String pkg, String cls) {
        return new BitmapDrawable(mPreContext.getResources(),getIcon(pkg,cls));
    }

    @Override
    public Drawable getIconDrawable(String pkg) {
        return new BitmapDrawable(mPreContext.getResources(),getIcon(pkg));
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName) {
        return new BitmapDrawable(mPreContext.getResources(),getIcon(componentName));
    }

    @Override
    public Bitmap standardIcon(Bitmap source, boolean asThirdpartIcon) {
        if(asThirdpartIcon){
            return  standardThirdPardIcon(new BitmapDrawable(mPreContext.getResources(),source));
        }else{
            return standardOurselfIcon(source);
        }
    }

    /**
     * Get the Icon Name use Component Info in Icon package res.
     *
     * @param info
     * @return
     */
    private String getIconName(ComponentInfo info) {
        return getIconName(info.packageName, info.name);
    }

    /**
     * Get the Icon Name use pkg & cls in Icon package res.
     *
     * @param pkg
     * @param cls
     * @return
     */
    private String getIconName(String pkg, String cls) {
        CharSequence key = pkg + "$" + cls;
        //first use pkg&cls to match
        String iconName = getIconName(key.toString().replace("_fensheng",""));
        if (iconName == null) {
            //second use pkg to match
            iconName = getIconName(pkg);
        }
        if (iconName == null) {
            //thid use label to match
            ApplicationInfo info = getApplicationInfo(pkg);
            PackageManager pm = mPreContext.getPackageManager();
            if (info != null) {
                CharSequence label = info.loadLabel(pm);
                if (label != null) {
                    iconName = getIconName(label.toString());
                }
            }

        }
        return iconName;
    }

    private String getIconName(String key) {
        String bitmapName = mLabel_Icons.get(key.trim());
        String iconName = null;
        if (bitmapName != null) {
            String[] s = bitmapName.split("\\.");
            if (s.length == 2) {
                iconName = s[0];
            }
        }
        return iconName;
    }

    private Bitmap getIconFromApkOrDefault(ApplicationInfo info) {
        Bitmap result = getIconFromApk(info);
        if (result == null) {
            result = getDefaultIcon();
        }
        return result;
    }

    private Bitmap getIconFromApkOrDefault(ComponentInfo info) {
        Bitmap bmp = getIconFromApk(info);
        if (bmp == null) {
            bmp = getDefaultIcon();
        }
        return bmp;
    }

    private Bitmap getIconFromApkOrDefault(String pkg, String cls) {
        Bitmap result = getIconFromApk(pkg, cls);
        if (result == null) {
            result = getDefaultIcon();
        }
        return result;
    }

    private Bitmap getIconFromApk(String pkg, String cls) {
        Bitmap bitmap = null;
        if (pkg != null) {
            if (cls != null) {
                ResolveInfo info = getResolveInfo(pkg, cls);
                ComponentInfo cpInfo = info.activityInfo != null ? info.activityInfo : info.serviceInfo;
                bitmap = getIconFromApkOrDefault(cpInfo);
            } else {
                //Load application icon
                ApplicationInfo info = getApplicationInfo(pkg);
                bitmap = getIconFromApkOrDefault(info);
            }
        }
        return bitmap;
    }

    private Bitmap getIconFromApk(ApplicationInfo info) {
        PackageManager pm = mPreContext.getPackageManager();
        Drawable d = null;
        if (info != null) {
            try {
                d = info.loadIcon(pm);
            } catch (Resources.NotFoundException e) {
                d = null;
            }
        }
        //TODO:xiejun
        //TODO: may be here we need dual the icons.
        return standardThirdPardIcon(d);//PhotoUtils.drawable2bitmap(d);
    }

    private Bitmap getIconFromApk(ComponentInfo info) {
        Drawable d = null;
        if (info != null) {
            PackageManager pm = mPreContext.getPackageManager();
            Resources res;
            try {
                res = pm.getResourcesForApplication(info.applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                res = null;
            }
            // try to get the icon from application

            if (res != null) {
                int iconId = info.getIconResource();
                //Log.i(TAG,"iconId = "+iconId);
                if (iconId != 0) {
                    try {
                        d = res.getDrawableForDensity(iconId, mIconDpi);
                    } catch (Resources.NotFoundException e) {
                        d = null;
                    }
                } else {
                    d = pm.getApplicationIcon(info.applicationInfo);
                }
            }
        }
        //TODO:xiejun
        return standardThirdPardIcon(d);//PhotoUtils.drawable2bitmap(d);
    }

    private ApplicationInfo getApplicationInfo(String pkg) {
        Context context = mPreContext;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e1) {
            info = null;
        }
        return info;
    }

    private ResolveInfo getResolveInfo(String pkg, String cls) {
        Context context = mPreContext;
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.setClassName(pkg, cls);
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        ResolveInfo info = null;
        if (!infos.isEmpty()) {
            info = infos.get(0);
        }
        return info;
    }

    private CharSequence getAppLabel(String pkg) {
        Context context = mPreContext;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        CharSequence key = null;
        if (info != null) {
            key = info.loadLabel(pm);
        }
        return key;
    }

    private Bitmap getBitmapByName(String iconName) {
        Drawable d = getIconByName(iconName);
        return standardOurselfIcon(PhotoUtils.drawable2bitmap(d));//PhotoUtils.drawable2bitmap(d);
    }

    private Drawable getIconByName(String iconName) {
        Context context = mPreContext;
        Drawable drawable = null;
        if (context != null) {
            drawable = PhotoUtils.getIconDrawable(mThemePakageName, iconName, context, mIconDpi);
        }
        return drawable;
    }



    private Bitmap standardOurselfIcon(Bitmap sourceBmp) {
        return sourceBmp;
    }

    private Bitmap standardThirdPardIcon(Drawable sourceDrawable) {
        //Thread.dumpStack();
        Drawable mask_regular = getIconByName(MASK_REGULAR);
        Drawable bg = getIconByName(BACKGROUND);
        Drawable mask_unregular = getIconByName(MASK_UNREGULAR);
        Rect bound = new Rect();
        int area = PhotoUtils.calcClipBounds(PhotoUtils.drawable2bitmap(sourceDrawable),bound);
        int w = bound.right - bound.left;
        int h = bound.bottom - bound.top;
        Resources res = mPreContext.getResources();
        Bitmap result = null;
        if(isHeteromorphicTheme){
            result = PhotoUtils.maskBitmap(PhotoUtils.drawable2bitmap(sourceDrawable),PhotoUtils.drawable2bitmap(mask_unregular),bound);
            result = PhotoUtils.composite(result,PhotoUtils.drawable2bitmap(bg),mPositionInBg);
        }else{
            if (area > 0.91f*w * h&&w > 0.97f * h && h>0.97*w) {
                result = PhotoUtils.maskBitmap(PhotoUtils.drawable2bitmap(sourceDrawable),PhotoUtils.drawable2bitmap(mask_regular),bound);
                result = PhotoUtils.composite(result,PhotoUtils.drawable2bitmap(bg),null);
            }else{
                result = PhotoUtils.maskBitmap(PhotoUtils.drawable2bitmap(sourceDrawable),PhotoUtils.drawable2bitmap(mask_unregular),bound);
                result = PhotoUtils.composite(result,PhotoUtils.drawable2bitmap(bg),null);
            }
        }
        return result;
    }

    public static boolean isThemePackageExist(Context context,String pkg){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e1) {
            info = null;
        }
        return (info==null?false:true);
    }
}
