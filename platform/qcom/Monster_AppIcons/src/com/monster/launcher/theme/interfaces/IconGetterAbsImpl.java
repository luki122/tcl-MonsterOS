package com.monster.launcher.theme.interfaces;

import android.content.ComponentName;
import android.content.Context;
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
import android.os.UserHandle;
import android.util.Log;

import com.monster.launcher.theme.utils.PhotoUtils;
import com.monster.launcher.theme.utils.Utilites;

import java.util.HashMap;

/**
 * Created by lj on 16-11-15.
 */
public abstract class IconGetterAbsImpl implements IIconGetter {

    protected Context mPreContext;
    protected HashMap<String, String> mLabel_Icons;
    protected int mIconDpi;
    protected Resources res;
    protected static boolean isHeteromorphicTheme = false;
    protected Rect mPositionInBg = new Rect();
    protected String themeName;
    protected String themeVersion;

    @Override
    public boolean init(Context context) {
        return true;
    }

    @Override
    public Bitmap getIcon(ResolveInfo info) {
        return getIcon(info,null);
    }

    @Override
    public Bitmap getIcon(ActivityInfo info) {
        return getIcon(info,null);
    }

    @Override
    public Bitmap getIcon(ServiceInfo info) {
        return getIcon(info,null);
    }

    @Override
    public Bitmap getIcon(String pkg) {
        return getIcon(pkg,null);
    }

    @Override
    public Bitmap getIcon(ComponentName componentName) {
        return getIcon(componentName,null);
    }

    @Override
    public Drawable getIconDrawable(ResolveInfo info) {
        return getIconDrawable(info,null);
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info) {
        return getIconDrawable(info,null);
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info) {
        return getIconDrawable(info,null);
    }

    @Override
    public Drawable getIconDrawable(String pkg) {
        return getIconDrawable(pkg,null);
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName) {
        return getIconDrawable(componentName,null);
    }

    @Override
    public Bitmap getIcon(ResolveInfo info, UserHandle user) {
        Bitmap result = null;
        if (info.activityInfo != null) {
            result = getIcon(info.activityInfo,user);
        } else if (info.serviceInfo != null) {
            result = getIcon(info.serviceInfo,user);
        }
        return result;
    }

    @Override
    public Bitmap getIcon(ActivityInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info,user);
        return (drawable!=null&&drawable instanceof BitmapDrawable)?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ServiceInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info, user);
        return (drawable != null && drawable instanceof BitmapDrawable) ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(String pkg, UserHandle user) {
        Drawable drawable = getIconDrawable(pkg,user);
        return (drawable!=null&&drawable instanceof BitmapDrawable)?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ComponentName componentName, UserHandle user) {
        Drawable drawable = getIconDrawable(componentName,user);
        return (drawable!=null&&drawable instanceof BitmapDrawable)?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }
    @Override
    public Drawable getIconDrawable(ResolveInfo info, UserHandle user) {
        Drawable d = null;
        if(info.activityInfo!=null){
            d=getIconDrawable(info.activityInfo);
        }else if(info.serviceInfo!=null){
            d=getIconDrawable(info.serviceInfo);
        }
        return d;
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info, UserHandle user) {
        String iconName = getIconName(info.packageName,info.name);
        Drawable d = null;
        if(iconName!=null){
            d = getIconByName(iconName,user);
        }
        if(d==null){
            d = standardThirdPardIcon(getIconFromApk(info));
        }
        return d;
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info, UserHandle user) {
        String iconName = getIconName(info.packageName,info.name);
        Drawable d = null;
        if(iconName!=null){
            d = getIconByName(iconName,user);
        }
        if(d==null){
            d = getIconFromApk(info);
        }
        return d;
    }

    /**
     *
     * @param pkg
     * @param user
     * @return
     */
    @Override
    public Drawable getIconDrawable(String pkg, UserHandle user) {
        String iconName = getIconName(pkg,null);
        Drawable d = null;
        if(iconName!=null){
            d = getIconByName(iconName,user);
        }
        if(d==null){
            d = getIconFromApk(getApplicationInfo(pkg,mPreContext));
        }
        return d;
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName, UserHandle user) {
        Drawable d = null;
        String iconName = getIconName(componentName.getPackageName(),componentName.getClassName());
        if(iconName!=null){
            d = getIconByName(iconName,user);
        }
        if(d==null){
            d=getIconFromApk(getApplicationInfo(componentName.getPackageName(),mPreContext));
        }
        return d;
    }

    @Override
    public Drawable standardIcon(Drawable source, boolean asThirdpartIcon) {
        Drawable d;
        if(asThirdpartIcon){
            d = standardThirdPardIcon(source);
        }else{
            d = starndardOurSelfIcon(source);
        }
        return d;
    }

    @Override
    public Bitmap standardIcon(Bitmap source, boolean asThirdpartIcon) {
        Drawable d = null;
        if(source!=null&&source.getWidth()>0&&source.getHeight()>0){
            d =  standardIcon(new BitmapDrawable(res,source),asThirdpartIcon);
        }
        if(d instanceof BitmapDrawable){
            return  ((BitmapDrawable) d).getBitmap();
        }else{
            return PhotoUtils.drawable2bitmap(d);
        }
    }

    @Override
    public Drawable getDefaultIconDrawable() {
        return getIconByName(Contents.DEFAULT,null);
    }

    @Override
    public IIconGetter setUseMemoryCache(boolean value) {
        return null;
    }

    @Override
    public IIconGetter setUseSdcardCache(boolean value) {
        return null;
    }

    private Drawable getIconByName(String fileName,UserHandle user){
        //TODO:consider the clone icon.
        String iconName = fileName;
        boolean isOhterUser = false;
        if (Utilites.isOtherUser(user)) {
            isOhterUser = true;
        }
        if(isOhterUser){
            iconName = getCloneIconName(iconName);
        }
        Context context = mPreContext;
        Drawable drawable = null;
        if (context != null) {
            drawable = getResurceDrawable(iconName);
            if(drawable == null && isOhterUser){
                drawable = getResurceDrawable(fileName);
            }
        }
        if(drawable == null)return null;
        Log.i(Contents.TAG,"drawable = "+drawable+"  ,  context = "+context + ", iconName = " + iconName);
        return standardIcon(drawable,false);
    }

    private String getCloneIconName(String iconName){
        return iconName+Contents.Clone_Icon_Suffix;
    }

    protected String getIconName(String pkg,String cls){
        CharSequence key = pkg + "$" + cls;
        //first use pkg&cls to match
        String iconName = getIconName(key.toString());
        if (iconName == null) {
            //second use pkg to match
            iconName = getIconName(pkg);
        }
        if (iconName == null) {
            //thid use label to match
            ApplicationInfo info = getApplicationInfo(pkg,mPreContext);
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

    protected String getIconName(String key) {
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

    private Drawable getIconFromApk(ComponentInfo info){
        Drawable d = null;
        if (info != null) {
            PackageManager pm = mPreContext.getPackageManager();
            Resources res;
            try {
                res = pm.getResourcesForApplication(info.applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                res = null;
                Log.i(Contents.TAG, "exception has catched!", e);
            }
            // try to get the icon from application
            if (res != null) {
                int iconId = info.getIconResource();
                Log.i(Contents.TAG,"iconId = "+iconId);
                if (iconId != 0) {
                    try {
                        d = res.getDrawableForDensity(iconId, mIconDpi);
                    } catch (Resources.NotFoundException e) {
                        d = pm.getApplicationIcon(info.applicationInfo);
                    }
                } else {
                    d = pm.getApplicationIcon(info.applicationInfo);
                }
            }
        }
        return standardThirdPardIcon(d);
    }

    private Drawable getIconFromApk(ApplicationInfo info) {
        PackageManager pm = mPreContext.getPackageManager();
        Drawable d = null;
        if (info != null) {
            try {
                d = info.loadIcon(pm);
            } catch (Resources.NotFoundException e) {
                d = null;
            }
        }
        return standardThirdPardIcon(d);
    }

    protected Drawable standardThirdPardIcon(Drawable source){
        if(source==null||source.getIntrinsicWidth()<=0||source.getIntrinsicHeight()<=0)return null;
        Drawable mask_regular = getIconByName(Contents.MASK_REGULAR,null);
        Drawable bg = getIconByName(Contents.BACKGROUND,null);
        Drawable mask_unregular = getIconByName(Contents.MASK_UNREGULAR,null);
        if(mask_regular == null || bg == null || mask_unregular == null)return source;
        Rect bound = new Rect();
        //The source opaque area
        int area;
        int opaqueWidth;
        int opaqueHeight;
        if(source instanceof BitmapDrawable){
            area = PhotoUtils.calcClipBounds(((BitmapDrawable) source).getBitmap(),bound);
        }else{
            area = PhotoUtils.calcClipBounds(PhotoUtils.drawable2bitmap(source),bound);
        }
        opaqueWidth = bound.right - bound.left;
        opaqueHeight = bound.bottom - bound.top;
        Bitmap result;
        if(isHeteromorphicTheme){
            result = PhotoUtils.composite(source,mask_unregular,bg,bound,mPositionInBg);
        }else{
            if (area > 0.91f*opaqueWidth * opaqueHeight&&opaqueWidth > 0.97f * opaqueHeight && opaqueHeight>0.97*opaqueWidth) {
                Log.i("realScale","  deal rectangle ");
                result = PhotoUtils.composite(source,mask_regular,bg,bound,null);
            }else{
                Log.i("realScale"," deal circle ");
                result = PhotoUtils.composite(source,mask_unregular,bg,bound,null);
            }
        }
        return (result==null?null:new BitmapDrawable(res,result));
    }

    private Drawable starndardOurSelfIcon(Drawable source){
        return source;
    }
    private ApplicationInfo getApplicationInfo(String pkg,Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e1) {
            Log.i(Contents.TAG, "getApkFileIcon  :  NameNotFoundException  :  " + pkg);
            info = null;
        }
        return info;
    }

    abstract protected Drawable getResurceDrawable(String iconName);

    public String getThemeName() {
        return themeName;
    }

    public String getThemeVersion() {
        return themeVersion;
    }
}
