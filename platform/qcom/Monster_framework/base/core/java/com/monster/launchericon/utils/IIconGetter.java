package com.monster.launchericon.utils;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by antino on 16-7-20.
 */
public interface IIconGetter {
    Bitmap getIcon(ResolveInfo info);
    Bitmap getIcon(ActivityInfo info);
    Bitmap getIcon(ServiceInfo info);
    Bitmap getIcon(String pkg,String cls);
    Bitmap getIcon(String pkg);
    Bitmap getIcon(ComponentName componentName);
    Drawable getIconDrawable(ResolveInfo info);
    Drawable getIconDrawable(ActivityInfo info);
    Drawable getIconDrawable(ServiceInfo info);
    Drawable getIconDrawable(String pkg, String cls);
    Drawable getIconDrawable(String pkg);
    Drawable getIconDrawable(ComponentName componentName);
    Bitmap standardIcon(Bitmap source,boolean asThirdpartIcon);
}
