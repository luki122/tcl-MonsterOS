/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.monster.autostart.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.db.MulwareProvider;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

	public static Utilities mUtilities;

	static final int DOWNLOADED_FLAG = 1;
	static final int UPDATED_SYSTEM_APP_FLAG = 2;

	private final int THIRD_APP = 1;

	public static final String TAG = "sunset";

	public static boolean CAN_ACCESS_DATA = false;

	public static final int COMPONENT_AUTO_START_DISABLE = 0;
	public static final int COMPONENT_AUTO_START_ENABLE = 1;

	public static final boolean ENABLE_MUTI_BROCASTRECEIVER = true;

	// TODO: use Build.VERSION_CODES when available
	public static final boolean ATLEAST_ANDROID_N = Build.VERSION.SDK_INT >= 24;
	
	public static final boolean ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= 23;

	public static final boolean ATLEAST_LOLLIPOP_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

	public static final boolean ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

	public static final boolean ATLEAST_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	public static final boolean ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

	public static final boolean ATLEAST_JB_MR2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

	public static Utilities getInstance() {
		if (mUtilities == null) {
			mUtilities = new Utilities();
		}
		return mUtilities;
	}
 
	public Intent makeLaunchIntent(ResolveInfo info) {
		ComponentName mComponentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
		Log.e(Utilities.TAG, "#info.activityInfo.packageName#"+info.activityInfo.packageName+";"+"#info.activityInfo.name#"+info.activityInfo.name);
		return new Intent().setComponent(mComponentName);
//		return new Intent(Intent.ACTION_MAIN)
//				.addCategory(Intent.CATEGORY_LAUNCHER)
//				.setComponent(mComponentName)
//				.setFlags(
//						Intent.FLAG_ACTIVITY_NEW_TASK
//								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	}

	public int initFlags(int f) {
		int appFlags = f;
		int flags = 0;
		if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
			flags |= DOWNLOADED_FLAG;

			if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				flags |= UPDATED_SYSTEM_APP_FLAG;
			}
		}
		return flags;
	}

	public Drawable getAppDrawable(Context c, String pkgName) {
		Drawable d = null;
		try {
			PackageInfo info = c.getPackageManager().getPackageInfo(pkgName, 0);
			ApplicationInfo appInfo = info.applicationInfo;
			if (appInfo == null) {
				throw new NameNotFoundException("ApplicationInfo is null");
			}
			d = appInfo.loadIcon(c.getPackageManager());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return d;
	}

	public List<AppInfo> getAppsList(List<ResolveInfo> l,String[] whiteList,
			PackageManager packageManager) {
		List<AppInfo> list = new ArrayList<AppInfo>();
		for (ResolveInfo info : l) {
			int flag = initFlags(info.activityInfo.applicationInfo.flags);
			if (flag == THIRD_APP) {
				AppInfo appInfo = new AppInfo();
				String title = packageManager.getApplicationLabel(
						info.activityInfo.applicationInfo).toString();
				Intent intent = makeLaunchIntent(info);
				appInfo.setTitle(title);
				appInfo.setIntent(intent);
				
				/**M:Hazel add for process sBgApplist that which item will set enable in according to the white list begin at 2016-11-6*/
				for(String content : whiteList){
					String pkg = intent.getComponent().getPackageName();
					if(content.equals(pkg)){
						appInfo.setStatus(Utilities.COMPONENT_AUTO_START_ENABLE);
					}
				}
				/**M:Hazel add for process sBgApplist that which item will set enable in according to the white list end at 2016-11-6*/
				list.add(appInfo);	
			}
		}
		return list;
	}

	public String getPackageName(Context context, int uid) {
		if (context == null) {
			return null;
		}
		String packageName = "";
		try {
			packageName = context.getPackageManager().getNameForUid(uid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packageName;
	}

	public String getTitle(Context context, Intent intent) {
		String title = new String();

		ComponentName cp = intent.getComponent();
		String packageName = cp.getPackageName();

		PackageManager pm = context.getPackageManager();

		try {
			ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
			title = pm.getApplicationLabel(info).toString();
			return title;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return title;
	}

	public void setComponentEnabledSetting(PackageManager pm, ComponentName cp,
			int newState) {
		try {
			int state = pm.getComponentEnabledSetting(cp);
			Log.e(Utilities.TAG, "newState="+newState+";"+"state="+state+"cp.pkg="+cp.getPackageName()+";"+"cp.cls="+cp.getClassName()+";");
			pm.setComponentEnabledSetting(cp, newState,
			PackageManager.DONT_KILL_APP);
//			switch (newState) {
//			case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
//				if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
//						|| state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
//					pm.setComponentEnabledSetting(cp, newState,
//							PackageManager.DONT_KILL_APP);
//					Log.e(Utilities.TAG, "#PackageManager.COMPONENT_ENABLED_STATE_DISABLED#");
//				}
//				
//				break;
//
//			case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
//				if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
//						|| state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
//					pm.setComponentEnabledSetting(cp, newState,
//							PackageManager.DONT_KILL_APP);
//					Log.e(Utilities.TAG, "#PackageManager.COMPONENT_ENABLED_STATE_ENABLED#");
//				}
//				break;
//			default: 
//				break;
//			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.e(Utilities.TAG, "_CLS_:" + "ProccessBroadcastFilterApp" + ";"
					+ "_FUNCTION_:"
					+ "setComponentEnabledSetting catch exception=" + e);
		}
	}

	public List<AppInfo> removeDuplicate(List<AppInfo> list) {
		List<AppInfo> result = new ArrayList<AppInfo>();
		for (int i = 0; i < list.size(); i++) {
			boolean same = false;
			AppInfo info = list.get(i);

			for (int j = 0; j < result.size(); j++) {
				AppInfo info1 = result.get(j);
				if (info.getTitle().equals(info1.getTitle())) {
					same = true;
				}
			}
			if (!same)
				result.add(info);
		}
		return result;
	}

	public void setAppStatus(String title, int status, List<AppInfo> list,int flag) {
		Iterator<AppInfo> iter = list.iterator();
		AppManagerState state = AppManagerState.getInstance();
		PackageManager pm = state.getContext().getPackageManager();
		
		MulwareProvider provider = state.getAppProvider();
		while (iter.hasNext()) {
			AppInfo info = iter.next();
			String t = info.getTitle();
			if (t.equals(title)) {
				info.setStatus(status);
				provider.update(info);
				ComponentName cp  = info.getIntent().getComponent();
				/**M:Hazel start to enable application components begin*/
				Utilities.getInstance().setComponentEnabledSetting(pm, cp, flag);
				/**M:Hazel start to enable application components end*/
			}
		}
	}

	public List<ResolveInfo> queryBroadcastReceivers(Context c, Intent intent) {
		return c.getPackageManager().queryBroadcastReceivers(intent,
				PackageManager.MATCH_DISABLED_COMPONENTS);
	}

}
