/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


public final class MetaUtil {

    private MetaUtil(){
    }

    public static String getMetaData(Context applicationContext, String name) {
        if (null==applicationContext) {
            throw new IllegalArgumentException("null==applicationContext");
        }
        PackageManager packageManager = applicationContext.getPackageManager();
        ApplicationInfo applicationInfo;
        Object value = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(applicationContext.getPackageName(), 128);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                value = applicationInfo.metaData.get(name);
            }
        } catch (NameNotFoundException e) {
            return null;
        }

        return value == null? null: value.toString();
    }
}

