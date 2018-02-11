package com.monster.launcher.theme;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import com.monster.launcher.theme.cache.BitmapMemoryCache;
import com.monster.launcher.theme.interfaces.IIconGetter;
import com.monster.launcher.theme.cache.BitmapSdcardCache;
import com.monster.launcher.theme.interfaces.PKGIconGetter;
import com.monster.launcher.theme.interfaces.ZIPIconGetter;
import com.monster.launcher.theme.utils.PhotoUtils;
import com.monster.launcher.theme.utils.Utilites;

/**
 * Created by antino on 16-11-8.
 */
public class IconGetterManager implements IIconGetter {
    private static IIconGetter instance;
    private static IIconGetter instance2;
    private static BitmapMemoryCache memoryCache;
    private static BitmapSdcardCache sdcarCache;
    private static Resources res;
    private static Object mLock = new Object();
    boolean useMemoryCache = false;
    boolean useSdcardCache = false;
    public static boolean USEPKG = true;//use pkg(true) or zip(false)?


    /**
     * context can't be null
     * @param context
     * @param usePkg
     * @return
     */
    private IconGetterManager(Context context, boolean usePkg) {
        if (usePkg) {
            instance2 = new PKGIconGetter(context);
        } else {
            instance2 = new ZIPIconGetter(context);
        }

        res = context.getResources();
        sdcarCache = new BitmapSdcardCache(res);
    }



    public static  IIconGetter getInstance(Context context){
        return getInstance(context,USEPKG);
    }

    public static  IIconGetter getInstance(Context context,boolean usePkg){
        synchronized (mLock){
            if(instance==null) {
                instance = new IconGetterManager(context, usePkg);
                if(!instance.init(context)){
                    instance = null;
                }
            }
            return instance;
        }
    }

    @Override
    public boolean init(Context context) {
        if(instance2!=null && instance2.init(context)){
            return true;
        }
        return false;
    }

    private Bitmap getDefaultIcon(){
        Drawable drawable = getDefaultIconDrawable();
        if(drawable instanceof  BitmapDrawable){
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return PhotoUtils.drawable2bitmap(drawable);
    }
    public Drawable getDefaultIconDrawable(){
        return instance2.getDefaultIconDrawable();
    }

    @Override
    public IIconGetter setUseMemoryCache(boolean value) {
        if(value){
            memoryCache = new BitmapMemoryCache(res);
        }else{
            memoryCache = null;
        }
        return instance;
    }

    @Override
    public IIconGetter setUseSdcardCache(boolean value) {
        if(value){
            sdcarCache = new BitmapSdcardCache(res);
        }else{
            sdcarCache = null;
        }
        return instance;
    }

    @Override
    public Bitmap getIcon(ResolveInfo info) {
        if(info.activityInfo!=null){
            return getIcon(info.activityInfo);
        }else{
            return getIcon(info.serviceInfo);
        }
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
        if(info.activityInfo!=null){
            return getIconDrawable(info.activityInfo);
        }else{
            return getIconDrawable(info.serviceInfo);
        }
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info) {
        Bitmap bitmap = getIcon(info);
        return bitmap==null?null:new BitmapDrawable(res,bitmap);
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info) {
        Bitmap bitmap = getIcon(info);
        return bitmap==null?null:new BitmapDrawable(res,bitmap);
    }

    @Override
    public Drawable getIconDrawable(String pkg) {
        Bitmap bitmap = getIcon(pkg);
        return bitmap==null?null:new BitmapDrawable(res,bitmap);
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName) {
        Bitmap bitmap = getIcon(componentName);
        return bitmap==null?null:new BitmapDrawable(res,bitmap);
    }

    @Override
    public Bitmap getIcon(ResolveInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info,user);
        return drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ActivityInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info,user);
        return drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ServiceInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info,user);
        return drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(String pkg, UserHandle user) {
        Drawable drawable = getIconDrawable(pkg,user);
        return drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ComponentName componentName, UserHandle user) {
        Drawable drawable = getIconDrawable(componentName,user);
        return drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Drawable getIconDrawable(ResolveInfo info, UserHandle user) {
        if(info.activityInfo!=null){
            return getIconDrawable(info.activityInfo,user);
        }else{
            return getIconDrawable(info.serviceInfo,user);
        }
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info, UserHandle user) {
        String key = info.packageName + "$" + info.name;
        if(Utilites.isOtherUser(user)){
            key=key+"$"+"clone";
        }
        Drawable drawable = null;
        if(memoryCache !=null){
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if(drawable==null) {
            if(sdcarCache !=null){
                drawable = sdcarCache.getIconDrawable(key);
            }
        }
        if(drawable == null ){
            drawable = instance2.getIconDrawable(info,user);
            if(drawable != null){
                Bitmap bitmap = drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
                sdcarCache.save(key,bitmap);
                if(memoryCache !=null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }else{
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info, UserHandle user) {
        String key = info.packageName + "$" + info.name;
        if(Utilites.isOtherUser(user)){
            key=key+"$"+"clone";
        }
        Drawable drawable = null;
        if(memoryCache !=null){
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if(drawable==null) {
            if(sdcarCache !=null){
                drawable = sdcarCache.getIconDrawable(key);
            }
        }
        if(drawable == null ){
            drawable = instance2.getIconDrawable(info,user);
            if(drawable != null){
                Bitmap bitmap = drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
                sdcarCache.save(key,bitmap);
                if(memoryCache !=null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }else{
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable getIconDrawable(String pkg, UserHandle user) {
        String key = pkg;
        if(Utilites.isOtherUser(user)){
            key=key+"$"+"clone";
        }
        Drawable drawable = null;
        if(memoryCache !=null){
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if(drawable==null) {
            if(sdcarCache !=null){
                drawable = sdcarCache.getIconDrawable(key);
            }
        }
        if(drawable == null ){
            drawable = instance2.getIconDrawable(pkg,user);
            if(drawable != null){
                Bitmap bitmap = drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
                sdcarCache.save(key,bitmap);
                if(memoryCache !=null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }else{
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName, UserHandle user) {
        String key = componentName.getPackageName()+"$"+componentName.getClassName();
        if(Utilites.isOtherUser(user)){
            key=key+"$"+"clone";
        }
        Drawable drawable = null;
        if(memoryCache !=null){
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if(drawable==null) {
            if(sdcarCache !=null){
                drawable = sdcarCache.getIconDrawable(key);
            }
        }
        if(drawable == null ){
            drawable = instance2.getIconDrawable(componentName,user);
            if(drawable != null){
                Bitmap bitmap = drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():PhotoUtils.drawable2bitmap(drawable);
                sdcarCache.save(key,bitmap);
                if(memoryCache !=null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }else{
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable standardIcon(Drawable source, boolean asThirdpartIcon) {
        return instance2.standardIcon(source,asThirdpartIcon);
    }

    @Override
    public Bitmap standardIcon(Bitmap source, boolean asThirdpartIcon) {
        return instance2.standardIcon(source,asThirdpartIcon);
    }

}
