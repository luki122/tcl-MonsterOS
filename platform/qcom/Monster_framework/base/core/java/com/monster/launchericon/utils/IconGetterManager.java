package com.monster.launchericon.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
import android.util.Log;

/**
 * Created by antino on 16-11-8.
 */
public class IconGetterManager implements IIconGetter {
    public static String TAG = "IconGetterManager";
    private static IconGetterManager instance;
    private static IconGetterAbsImpl instance2;
    private static BitmapMemoryCache memoryCache;
    private static BitmapSdcardCache sdcarCache;
    private static Resources res;
    private static Object mLock = new Object();
    public static boolean USEPKG = true;//use pkg(true) or zip(false)?

    private static String themeName = "";
    private static String themeVersion = "";

    /**
     * context can't be null
     *
     * @param context
     * @param usePkg
     * @return
     */
    private IconGetterManager(Context context, boolean usePkg) {
        if (usePkg) {
            instance2 = new PKGIcongetter(context);
        } else {
            instance2 = new ZIPIconGetter(context);
        }

        res = context.getResources();
        sdcarCache = new BitmapSdcardCache(res);
    }


    public static IconGetterManager getInstance(Context context) {
        return getInstance(context, USEPKG,false);
    }
    public static IconGetterManager getInstance(Context context,boolean usePkg) {
        return getInstance(context, usePkg,false);
    }
    public static IconGetterManager getInstance(Context context, boolean usePkg,boolean useMemoryCache) {
        synchronized (mLock) {
            if (instance == null) {
                instance = new IconGetterManager(context, usePkg);
                if (!instance.init(context)) {
                    instance = null;
                    Log.e(TAG, "can't getInstance");
                } else {
                    String tn = instance2.getThemeName();
                    String tv = instance2.getThemeVersion();
                    if (!themeName.equals(tn) || !themeVersion.equals(tv)) {
                        if(sdcarCache!=null) {
                            sdcarCache.clean();
                        }
                        themeName = tn;
                        themeVersion = tv;
                        saveConfig(context);
                    }
                }
            }
            if (instance != null && useMemoryCache) {
                if (memoryCache == null) {
                    memoryCache = new BitmapMemoryCache(res);
                }
            } else {
                memoryCache = null;
            }
            return instance;
        }
    }

    @Override
    public boolean init(Context context) {
        String[] config = getConfig(context);
        if (config != null && config.length == 2 && config[0] != null && config[1] != null) {
            themeName = config[0];
            themeVersion = config[1];
        }
        Log.d("theme.icon", "init themeName:" + themeName + ",themeVersion:" + themeVersion);
        if (instance2 != null && instance2.init(context)) {
            return true;
        }
        return false;
    }

    private Bitmap getDefaultIcon() {
        Drawable drawable = getDefaultIconDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return PhotoUtils.drawable2bitmap(drawable);
    }

    public Drawable getDefaultIconDrawable() {
        return instance2.getDefaultIconDrawable();
    }

    @Override
    public IconGetterManager setUseMemoryCache(boolean value) {
        if (value) {
			if (memoryCache == null) {
				memoryCache = new BitmapMemoryCache(res);
			}
        } else {
            memoryCache = null;
        }
        return instance;
    }

    @Override
    public IconGetterManager setUseSdcardCache(boolean value) {
        if (value) {
			if (sdcarCache == null) {
				sdcarCache = new BitmapSdcardCache(res);
			}
        } else {
            sdcarCache = null;
        }
        return instance;
    }

    @Override
    public Bitmap getIcon(ResolveInfo info) {
        if (info.activityInfo != null) {
            return getIcon(info.activityInfo);
        } else {
            return getIcon(info.serviceInfo);
        }
    }

    @Override
    public Bitmap getIcon(ActivityInfo info) {
        return getIcon(info, null);
    }

    @Override
    public Bitmap getIcon(ServiceInfo info) {
        return getIcon(info, null);
    }

    @Override
    public Bitmap getIcon(String pkg) {
        return getIcon(pkg, null);
    }

    @Override
    public Bitmap getIcon(ComponentName componentName) {
        return getIcon(componentName, null);
    }

    @Override
    public Drawable getIconDrawable(ResolveInfo info) {
        if (info.activityInfo != null) {
            return getIconDrawable(info.activityInfo);
        } else {
            return getIconDrawable(info.serviceInfo);
        }
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info) {
        Bitmap bitmap = getIcon(info);
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info) {
        Bitmap bitmap = getIcon(info);
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    @Override
    public Drawable getIconDrawable(String pkg) {
        Bitmap bitmap = getIcon(pkg);
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName) {
        Bitmap bitmap = getIcon(componentName);
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    @Override
    public Bitmap getIcon(ResolveInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info, user);
        return drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ActivityInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info, user);
        return drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ServiceInfo info, UserHandle user) {
        Drawable drawable = getIconDrawable(info, user);
        return drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(String pkg, UserHandle user) {
        Drawable drawable = getIconDrawable(pkg, user);
        return drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Bitmap getIcon(ComponentName componentName, UserHandle user) {
        Drawable drawable = getIconDrawable(componentName, user);
        return drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
    }

    @Override
    public Drawable getIconDrawable(ResolveInfo info, UserHandle user) {
        if (info.activityInfo != null) {
            return getIconDrawable(info.activityInfo, user);
        } else {
            return getIconDrawable(info.serviceInfo, user);
        }
    }

    @Override
    public Drawable getIconDrawable(ActivityInfo info, UserHandle user) {
        String key = info.packageName + "$" + info.name;
        if (Utilites.isOtherUser(user)) {
            key = key + "$" + "clone";
        }
        Drawable drawable = null;
        if (memoryCache != null) {
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if (drawable == null) {
            if (sdcarCache != null) {
                drawable = sdcarCache.getIconDrawable(key);
            }
            if(drawable!=null){
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if (memoryCache != null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }

        }
        if (drawable == null) {
            drawable = instance2.getIconDrawable(info, user);
            if (drawable != null) {
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if(!BitmapSdcardCache.CLEANING) {
                    if (sdcarCache != null) {
                        sdcarCache.save(key, bitmap);
                    }
                    if (memoryCache != null) {
                        memoryCache.addBitmapToMemoryCache(key, bitmap);
                    }
                }else {
                    Log.d("theme.icon", "is cleaning sdcard caches,don't save");
                }
            } else {
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable getIconDrawable(ServiceInfo info, UserHandle user) {
        String key = info.packageName + "$" + info.name;
        if (Utilites.isOtherUser(user)) {
            key = key + "$" + "clone";
        }
        Drawable drawable = null;
        if (memoryCache != null) {
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if (drawable == null) {
            if (sdcarCache != null) {
                drawable = sdcarCache.getIconDrawable(key);
            }
            if(drawable!=null){
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if (memoryCache != null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }

        }
        if (drawable == null) {
            drawable = instance2.getIconDrawable(info, user);
            if (drawable != null) {
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if(!BitmapSdcardCache.CLEANING) {
                    if (sdcarCache != null) {
                        sdcarCache.save(key, bitmap);
                    }
                    if (memoryCache != null) {
                        memoryCache.addBitmapToMemoryCache(key, bitmap);
                    }
                }else {
                    Log.d("theme.icon", "is cleaning sdcard caches,don't save");
                }
            } else {
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable getIconDrawable(String pkg, UserHandle user) {
        String key = pkg;
        if (Utilites.isOtherUser(user)) {
            key = key + "$" + "clone";
        }
        Drawable drawable = null;
        if (memoryCache != null) {
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if (drawable == null) {
            if (sdcarCache != null) {
                drawable = sdcarCache.getIconDrawable(key);
            }
            if(drawable!=null){
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if (memoryCache != null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }

        }
        if (drawable == null) {
            drawable = instance2.getIconDrawable(pkg, user);
            if (drawable != null) {
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if(!BitmapSdcardCache.CLEANING) {
                    if (sdcarCache != null) {
                        sdcarCache.save(key, bitmap);
                    }
                    if (memoryCache != null) {
                        memoryCache.addBitmapToMemoryCache(key, bitmap);
                    }
                }else {
                    Log.d("theme.icon", "is cleaning sdcard caches,don't save");
                }
            } else {
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable getIconDrawable(ComponentName componentName, UserHandle user) {
        String key = componentName.getPackageName() + "$" + componentName.getClassName();
        if (Utilites.isOtherUser(user)) {
            key = key + "$" + "clone";
        }
        Drawable drawable = null;
        if (memoryCache != null) {
            drawable = memoryCache.getDrawableFromMemCache(key);
        }
        if (drawable == null) {
            if (sdcarCache != null) {
                drawable = sdcarCache.getIconDrawable(key);
            }
            if(drawable!=null){
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if (memoryCache != null) {
                    memoryCache.addBitmapToMemoryCache(key, bitmap);
                }
            }

        }
        if (drawable == null) {
            drawable = instance2.getIconDrawable(componentName, user);
            if (drawable != null) {
                Bitmap bitmap = drawable instanceof BitmapDrawable ? ((BitmapDrawable) drawable).getBitmap() : PhotoUtils.drawable2bitmap(drawable);
                if(!BitmapSdcardCache.CLEANING) {
                    if (sdcarCache != null) {
                        sdcarCache.save(key, bitmap);
                    }
                    if (memoryCache != null) {
                        memoryCache.addBitmapToMemoryCache(key, bitmap);
                    }
                }else {
                    Log.d("theme.icon", "is cleaning sdcard caches,don't save");
                }
            } else {
                drawable = getDefaultIconDrawable();
            }
        }
        return drawable;
    }

    @Override
    public Drawable standardIcon(Drawable source, boolean asThirdpartIcon) {
        return instance2.standardIcon(source, asThirdpartIcon);
    }

    @Override
    public Bitmap standardIcon(Bitmap source, boolean asThirdpartIcon) {
        return instance2.standardIcon(source, asThirdpartIcon);
    }

    private static void saveConfig(Context context) {
        try {
            File file = new File(BitmapSdcardCache.CONFIG_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
                Log.e("theme.icon", "saveConfig file not exists and create it");
            }
            FileOutputStream fos = new FileOutputStream(file);
            String content = themeName + "\n" + themeVersion;
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
            Log.e("theme.icon", "saveConfig success");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] getConfig(Context context) {
        String[] result = new String[2];
        String content = "";
        try {
            File file = new File(BitmapSdcardCache.CONFIG_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
                Log.e("theme.icon", "getConfig file not exists and create it");
                return null;
            }
            FileInputStream fin = new FileInputStream(file);
            if (fin != null) {
                InputStreamReader inputreader = new InputStreamReader(fin);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while (( line = buffreader.readLine()) != null) {
                    content += line + "/";
                }
                result = content.split("/");
                Log.d("theme.icon", "getConfig result:" + result);
                fin.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
