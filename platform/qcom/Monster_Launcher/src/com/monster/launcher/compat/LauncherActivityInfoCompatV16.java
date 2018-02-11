/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.launcher.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import com.monster.launcher.Log;
import com.monster.launcher.dualapp.SmartContainerWrapper;
import com.monster.launcher.theme.IconGetterManager;
import com.monster.launcher.theme.interfaces.IIconGetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class LauncherActivityInfoCompatV16 extends LauncherActivityInfoCompat {
    private final ResolveInfo mResolveInfo;
    private final ActivityInfo mActivityInfo;
    private final ComponentName mComponentName;
    private final PackageManager mPm;

    LauncherActivityInfoCompatV16(Context context, ResolveInfo info) {
        super();
        mResolveInfo = info;
        mActivityInfo = info.activityInfo;
        mComponentName = new ComponentName(mActivityInfo.packageName, mActivityInfo.name);
        mPm = context.getPackageManager();
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }

    public UserHandleCompat getUser() {
        //return UserHandleCompat.myUserHandle();
        //Dual app  begin
        if ( !SmartContainerWrapper.WITH_OUT_VIRTUAL_BOX ) {
            try {
                Class<UserHandle> c = UserHandle.class;
                Method method = c.getMethod("getUserId", int.class);
                method.setAccessible(true);
                int userid = (int)method.invoke(null, mResolveInfo.activityInfo.applicationInfo.uid);
                //generate UserHandleCompat
                Constructor ctor = UserHandle.class.getConstructor(int.class);
                ctor.setAccessible(true);
                UserHandle userhandle = (UserHandle)ctor.newInstance(userid);
                UserHandleCompat user = UserHandleCompat.fromUser(userhandle);
                return user;
            } catch ( Exception e) {
                e.printStackTrace();
                return UserHandleCompat.myUserHandle();
            }
        }
        else{
            return UserHandleCompat.myUserHandle();
        }
        //Dual app end
    }

    public CharSequence getLabel() {
        try {
            return mResolveInfo.loadLabel(mPm);
        } catch (SecurityException e) {
            Log.e("LAInfoCompat", "Failed to extract app display name from resolve info", e);
            return "";
        }
    }

    public Drawable getIcon(int density) {
        int iconRes = mResolveInfo.getIconResource();
        Resources resources = null;
        Drawable icon = null;
        // Get the preferred density icon from the app's resources
        if (density != 0 && iconRes != 0) {
            try {
                resources = mPm.getResourcesForApplication(mActivityInfo.applicationInfo);
                icon = resources.getDrawableForDensity(iconRes, density);
            } catch (NameNotFoundException | Resources.NotFoundException exc) {
            }
        }
        // Get the default density icon
        if (icon == null) {
            icon = mResolveInfo.loadIcon(mPm);
        }
        if (icon == null) {
            resources = Resources.getSystem();
            icon = resources.getDrawableForDensity(android.R.mipmap.sym_def_app_icon, density);
        }
        return icon;
    }

    public ApplicationInfo getApplicationInfo() {
        return mActivityInfo.applicationInfo;
    }

    public long getFirstInstallTime() {
        try {
            PackageInfo info = mPm.getPackageInfo(mActivityInfo.packageName, 0);
            return info != null ? info.firstInstallTime : 0;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public String getName() {
        return mActivityInfo.name;
    }

    public Drawable getBadgedIcon(int density) {
        return getIcon(density);
    }

    @Override
    public Drawable getBadgedIcon(int density, Context context) {
        Log.d("icons","mActivityInfo = "+mActivityInfo);
        IIconGetter iconGetter = IconGetterManager.getInstance(context);
        Drawable result=null;
        if(iconGetter!=null){
            result = iconGetter.getIconDrawable(mActivityInfo,getUser().getUser());
        }
        if(result==null){
            result = getIcon(density);
        }
        return result;
    }
}
