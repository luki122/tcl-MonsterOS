/* Copyright (C) 2016 Tcl Corporation Limited */
/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.monster.appmanager.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.monster.appmanager.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.AttributeSet;

public class DefaultAppPreferenceBase extends LayoutGridViewPreference {

    final public PackageManager mPm;
    private Intent intent;
    private String intentKey;
    private List<ResolveInfo> resolverInfoList;
    private String defaultPackageName;
    
	public void setIntent(Intent intent) {
		this.intent = intent;
		refreshApps();
	}
    
    public Intent getCustomIntent() {
    	return this.intent;
	}
    
	public DefaultAppPreferenceBase(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWidgetLayoutResource(R.layout.pref_widget);

        mPm = context.getPackageManager();
    }
    
    public void refreshApps() {
    	defaultPackageName = null;
        List<String> cameras = resolveApps();
        setPackageNames(cameras.toArray(new String[cameras.size()]), null);
        setValue(defaultPackageName);
    }

    private List<String> resolveApps() {
        List<String> result = new ArrayList<>();
//        List<ResolveInfo> list = mPm.queryIntentActivitiesAsUser(intent, PackageManager.MATCH_ALL,
//                UserHandle.myUserId());
        boolean shouldGetResolvedFilter = true;
        List<ResolveInfo> list = mPm.queryIntentActivities(intent,
        		PackageManager.MATCH_DEFAULT_ONLY
        		| (shouldGetResolvedFilter ? PackageManager.GET_RESOLVED_FILTER : 0));

        final int count = list.size();
        for (int i=0; i<list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo == null || result.contains(info.activityInfo.packageName)) {
            	list.remove(i);
            	i--;
                continue;
            }
            if(info.activityInfo.packageName.equals("com.android.settings")) {
            	list.remove(i);
            	i--;
            	continue;
            }
            
            if(isDefaultPreferredActivities(mPm, info.activityInfo.packageName, info)) {
            	defaultPackageName = info.activityInfo.packageName;
            }

            result.add(info.activityInfo.packageName);
        }
        resolverInfoList = list;

        return result;
    }

	public String getIntentKey() {
		return intentKey;
	}

	public void setIntentKey(String intentKey) {
		this.intentKey = intentKey;
	}

	public List<ResolveInfo> getResolverInfoList() {
		return resolverInfoList;
	}

	public ResolveInfo getResolverInfo(String packageName) {
		ResolveInfo info = null;
		for (ResolveInfo resolveInfo : resolverInfoList) {
			if(packageName.equals(resolveInfo.activityInfo.packageName)) {
				info = resolveInfo;
				break;
			}
		}
		
		return info;
	}

	public Intent getResolvedIntent(String packageName, ResolveInfo info) {
		if(info == null) {
			return null;
		}
		Intent resolverdIntent = new Intent(intent);
		resolverdIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		resolverdIntent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
		return resolverdIntent;
	}
	
	private boolean isDefaultPreferredActivities(PackageManager pm, String packageName, ResolveInfo info) {
		boolean result = false;
		ComponentName compareComponentName = info.getComponentInfo().getComponentName();
        List<ComponentName> prefActList = new ArrayList<>();
        List<IntentFilter> intentList = new ArrayList<>();
        pm.getPreferredActivities(intentList, prefActList, packageName);
        if(prefActList.size() > 0 && intentList.size() > 0) {
        	final int N = prefActList.size() > intentList.size() ? prefActList.size() : intentList.size();
        	for (int i = 0; i < N; i++) {
				 ComponentName componentName = prefActList.get(i);
        		if(componentName.getPackageName().equals(compareComponentName.getPackageName()) 
        				&& componentName.getClassName().equals(compareComponentName.getClassName())
        				&& compareIntentFilter(intent, intentList.get(i))) {
        			result = true;
        			break;
        		}
			}
        }
        return result;
    }
	
	private boolean compareIntentFilter(Intent intent, IntentFilter filter) {
		boolean compare = true;
		Set<String> categories = intent.getCategories();
		String action = intent.getAction();
		String type = intent.getType();
		String scheme = intent.getScheme();
		
		if (compare && type != null) {
			compare = filter.hasDataType(type);
		}

		if (compare && categories != null && categories.size() > 0) {
			String[] categoriesString = new String[categories.size()];
			categories.toArray(categoriesString);
			for (int k = 0; k < categoriesString.length; k++) {
				if (!filter.hasCategory(categoriesString[k])) {
					compare = false;
					break;
				}
			}
		}
		
		if (compare && action != null) {
			compare = filter.hasAction(action);
		}

		if (getIntentKey().equals(ManageDefaultApps.KEY_DEFAULT_MAIL_APP) && compare && scheme != null) {
			compare = filter.hasDataScheme(scheme);
		}

		return compare;
	}
}
