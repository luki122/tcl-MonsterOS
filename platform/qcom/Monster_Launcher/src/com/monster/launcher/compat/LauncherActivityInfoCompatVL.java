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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.monster.launcher.Log;
import com.monster.launcher.theme.IconGetterManager;
import com.monster.launcher.theme.interfaces.IIconGetter;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {
    private LauncherActivityInfo mLauncherActivityInfo;

    LauncherActivityInfoCompatVL(LauncherActivityInfo launcherActivityInfo) {
        super();
        mLauncherActivityInfo = launcherActivityInfo;
    }

    public ComponentName getComponentName() {
        return mLauncherActivityInfo.getComponentName();
    }

    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(mLauncherActivityInfo.getUser());
    }

    public CharSequence getLabel() {
        return mLauncherActivityInfo.getLabel();
    }

    public Drawable getIcon(int density) {
        return mLauncherActivityInfo.getIcon(density);
    }

    public ApplicationInfo getApplicationInfo() {
        return mLauncherActivityInfo.getApplicationInfo();
    }

    public long getFirstInstallTime() {
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    public Drawable getBadgedIcon(int density) {
        return mLauncherActivityInfo.getBadgedIcon(density);
    }

    @Override
    public Drawable getBadgedIcon(int density, Context context) {
        Log.i("icons","1:mLauncherActivityInfo.getComponentName() = "+mLauncherActivityInfo.getComponentName());
        IIconGetter iconGetter = IconGetterManager.getInstance(context);
        Drawable result=null;
        if(iconGetter!=null){
            result = iconGetter.getIconDrawable(mLauncherActivityInfo.getComponentName(),getUser().getUser());
        }
        Log.i("icons","2:mLauncherActivityInfo.getComponentName() = "+mLauncherActivityInfo.getComponentName());

        if(result==null){
            result = mLauncherActivityInfo.getBadgedIcon(density);
        }
        return result;
    }
}
