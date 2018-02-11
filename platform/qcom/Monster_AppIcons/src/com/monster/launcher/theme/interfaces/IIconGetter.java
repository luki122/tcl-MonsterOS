package com.monster.launcher.theme.interfaces;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

/**
 * Created by antino on 16-11-8.
 */
public interface IIconGetter {
    boolean init(Context context);
    Bitmap getIcon(ResolveInfo info);
    Bitmap getIcon(ActivityInfo info);
    Bitmap getIcon(ServiceInfo info);
    Bitmap getIcon(String pkg);
    Bitmap getIcon(ComponentName componentName);
    Drawable getIconDrawable(ResolveInfo info);
    Drawable getIconDrawable(ActivityInfo info);
    Drawable getIconDrawable(ServiceInfo info);
    Drawable getIconDrawable(String pkg);
    Drawable getIconDrawable(ComponentName componentName);
    Bitmap getIcon(ResolveInfo info, UserHandle user);
    Bitmap getIcon(ActivityInfo info, UserHandle user);
    Bitmap getIcon(ServiceInfo info, UserHandle user);
    Bitmap getIcon(String pkg, UserHandle user);
    Bitmap getIcon(ComponentName componentName, UserHandle user);
    Drawable getIconDrawable(ResolveInfo info, UserHandle user);
    Drawable getIconDrawable(ActivityInfo info, UserHandle user);
    Drawable getIconDrawable(ServiceInfo info, UserHandle user);
    Drawable getIconDrawable(String pkg, UserHandle user);
    Drawable getIconDrawable(ComponentName componentName, UserHandle user);
    Drawable standardIcon(Drawable source, boolean asThirdpartIcon);
    Bitmap standardIcon(Bitmap source, boolean asThirdpartIcon);
    Drawable getDefaultIconDrawable();
    IIconGetter setUseMemoryCache(boolean value);
    IIconGetter setUseSdcardCache(boolean value);
}
