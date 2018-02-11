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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.monster.appmanager.R;
import com.monster.appmanager.SettingsPreferenceFragment;

//[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


public class ManageDefaultApps extends SettingsPreferenceFragment implements
		Preference.OnPreferenceClickListener {

	private static final String TAG = ManageDefaultApps.class.getSimpleName();

	private static final String KEY_DEFAULT_PHONE_APP = "default_phone_app";
	private static final String KEY_SMS_APPLICATION = "default_sms_app";

	private static final String KEY_DEFAULT_BROWSER = "default_browser";
	private static final String KEY_DEFAULT_LAUNCHER_APP = "default_launcher_app";
	private static final String KEY_DEFAULT_CAMERA_APP = "default_camera_app";
	private static final String KEY_DEFAULT_GALLERY_APP = "default_gallery_app";
	private static final String KEY_DEFAULT_MUSIC_APP = "default_music_app";
	private static final String KEY_DEFAULT_VIDEO_APP = "default_video_app";
	private static final String KEY_DEFAULT_TEXT_APP = "default_text_app";
	public static final String KEY_DEFAULT_MAIL_APP = "default_mail_app";
	private static final String KEY_DEFAULT_IME_APP = "default_ime";
	private static final String KEY_DEFAULT_CONTACT_APP = "default_contact_app";

	private static final String[] ALL_KEYS = { KEY_DEFAULT_BROWSER,
			KEY_DEFAULT_LAUNCHER_APP, KEY_DEFAULT_CONTACT_APP, KEY_DEFAULT_CAMERA_APP,
			KEY_DEFAULT_GALLERY_APP, KEY_DEFAULT_MUSIC_APP,
			KEY_DEFAULT_VIDEO_APP, KEY_DEFAULT_TEXT_APP, KEY_DEFAULT_MAIL_APP };

	private static final HashMap<String, Intent> INTENT_MAP = new HashMap<>();
	private static final LinkedList<String> ALL_ADD_TYPES = new LinkedList<>();
	static {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse("http:"));
		INTENT_MAP.put(KEY_DEFAULT_BROWSER, intent);

		intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		INTENT_MAP.put(KEY_DEFAULT_LAUNCHER_APP, intent);

		intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		INTENT_MAP.put(KEY_DEFAULT_CAMERA_APP, intent);

		intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(Uri.fromParts("file", "", null), "image/*");
		INTENT_MAP.put(KEY_DEFAULT_GALLERY_APP, intent);

		intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(Uri.fromParts("file", "", null), "audio/*");
		INTENT_MAP.put(KEY_DEFAULT_MUSIC_APP, intent);

		intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(Uri.fromParts("file", "", null), "video/*");
		INTENT_MAP.put(KEY_DEFAULT_VIDEO_APP, intent);

		intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(Uri.fromParts("file", "", null), "text/plain");
		INTENT_MAP.put(KEY_DEFAULT_TEXT_APP, intent);

		intent = new Intent();
		intent.setAction(Intent.ACTION_SENDTO);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.fromParts("mailto", "", null));
		INTENT_MAP.put(KEY_DEFAULT_MAIL_APP, intent);
		
		intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(ContactsContract.Contacts.CONTENT_URI);
		INTENT_MAP.put(KEY_DEFAULT_CONTACT_APP, intent);
		
		ALL_ADD_TYPES.add("image/*");
		ALL_ADD_TYPES.add("audio/*");
		ALL_ADD_TYPES.add("video/*");
		ALL_ADD_TYPES.add("text/plain");
	}

	// 所有prefrence
	private DefaultAppPreferenceBase[] allPreference = new DefaultAppPreferenceBase[ALL_KEYS.length];

	// [BUGFIX]-Add-END by TCTNB.caixia.chen

	private DefaultAppPreferenceBase mDefaultBrowserPreference;
	private DefaultPhonePreference mDefaultPhonePreference;
	private DefaultSmsPreference mDefaultSmsPreference;
	private DefaultImePreference mDefaultImePreference;
	
	// 输入法
	// private WidgetPreference curPref ;
	private PackageManager mPm;
	private int myUserId;

	private static final long DELAY_UPDATE_BROWSER_MILLIS = 500;

	private final Handler mHandler = new Handler();

	private final Runnable mUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			updateDefaultBrowserPreference();
			// [BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
			updateDefaultAppPreference();
			// [BUGFIX]-Add-END by TCTNB.caixia.chen
		}
	};

	private final PackageMonitor mPackageMonitor = new PackageMonitor() {
		@Override
		public void onPackageAdded(String packageName, int uid) {
			sendUpdate();
		}

		@Override
		public void onPackageAppeared(String packageName, int reason) {
			sendUpdate();
		}

		@Override
		public void onPackageDisappeared(String packageName, int reason) {
			sendUpdate();
		}

		@Override
		public void onPackageRemoved(String packageName, int uid) {
			sendUpdate();
		}

		private void sendUpdate() {
			mHandler.postDelayed(mUpdateRunnable, DELAY_UPDATE_BROWSER_MILLIS);
		}
	};

	private void updateDefaultBrowserPreference() {
		((DefaultBrowserPreference)mDefaultBrowserPreference).refreshBrowserApps();

		final PackageManager pm = getPackageManager();

		String packageName = pm.getDefaultBrowserPackageNameAsUser(UserHandle.myUserId());
		if (!TextUtils.isEmpty(packageName)) {
			// Check if the default Browser package is still there
			Intent intent = new Intent();
			intent.setPackage(packageName);
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(Uri.parse("http:"));

			ResolveInfo info = mPm.resolveActivityAsUser(intent, 0, myUserId);
			if (info != null) {
				mDefaultBrowserPreference.setValue(packageName);
				// [BUGFIX]-Del-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect
				// 1864320
				// CharSequence label = info.loadLabel(pm);
				// mDefaultBrowserPreference.setSummary(label);
				// [BUGFIX]-Del-END by TCTNB.caixia.chen
			} else {
				// [BUGFIX]-Del-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect
				// 1864320
				// mDefaultBrowserPreference.setSummary(R.string.default_browser_title_none);
				// [BUGFIX]-Del-END by TCTNB.caixia.chen
			}
		} else {
			// [BUGFIX]-Del-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
			// mDefaultBrowserPreference.setSummary(R.string.default_browser_title_none);
			// [BUGFIX]-Del-END by TCTNB.caixia.chen
			Log.d(TAG, "Cannot set empty default Browser value!");
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.default_apps);

		mPm = getPackageManager();
		myUserId = UserHandle.myUserId();
		mDefaultBrowserPreference = (DefaultAppPreferenceBase) findPreference(KEY_DEFAULT_BROWSER);
		mDefaultPhonePreference = (DefaultPhonePreference) findPreference(KEY_DEFAULT_PHONE_APP);
		mDefaultSmsPreference = (DefaultSmsPreference) findPreference(KEY_SMS_APPLICATION);
		mDefaultImePreference = (DefaultImePreference) findPreference(KEY_DEFAULT_IME_APP);
		mDefaultBrowserPreference.setIntentKey(KEY_DEFAULT_BROWSER);
		mDefaultPhonePreference.setIntentKey(KEY_DEFAULT_PHONE_APP);
		mDefaultSmsPreference.setIntentKey(KEY_SMS_APPLICATION);
		mDefaultImePreference.setIntentKey(KEY_DEFAULT_IME_APP);
		
		for (int i = 0; i < ALL_KEYS.length; i++) {
			final int index = i;
			final DefaultAppPreferenceBase nowDefaultAppPreferenceBase = allPreference[i] = (DefaultAppPreferenceBase) findPreference(ALL_KEYS[i]);
			nowDefaultAppPreferenceBase.setIntentKey(ALL_KEYS[i]);
			nowDefaultAppPreferenceBase.setIntent(INTENT_MAP.get(ALL_KEYS[i]));
			nowDefaultAppPreferenceBase
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							final String packageName = (String) newValue;
							if (ALL_KEYS[index].equals(KEY_DEFAULT_BROWSER)) {
									boolean result = mPm.setDefaultBrowserPackageNameAsUser(
											packageName, myUserId);
									if (result) 
										mDefaultBrowserPreference.setValue(packageName);
		                            //[BUGFIX]-Del-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
		                            //final CharSequence appName = mDefaultBrowserPreference.getEntry();
		                            //mDefaultBrowserPreference.setSummary(appName);
		                            //[BUGFIX]-Del-END by TCTNB.caixia.chen
									return result;
							} else {
								DefaultAppPreferenceBase pre = (DefaultAppPreferenceBase)preference;
								setPreferredActivity(pre, packageName, pre.getValue());
								return true;
							}
						}
					});
		}
		final boolean isRestrictedUser = UserManager.get(getActivity())
				.getUserInfo(myUserId).isRestricted();

		// Restricted users cannot currently read/write SMS.
		// Remove SMS Application if the device does not support SMS
		if (isRestrictedUser
				|| !DefaultSmsPreference.isAvailable(getActivity())) {
			removePreference(KEY_SMS_APPLICATION);
		}

		if (!DefaultPhonePreference.isAvailable(getActivity())) {
			removePreference(KEY_DEFAULT_PHONE_APP);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateDefaultBrowserPreference();
		// [BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
		updateDefaultAppPreference();
		mPackageMonitor.register(getActivity(), getActivity().getMainLooper(),
				false);
	}

	@Override
	public void onPause() {
		super.onPause();

		mPackageMonitor.unregister();
	}

	@Override
	protected int getMetricsCategory() {
		return MetricsLogger.APPLICATIONS_DEFAULT_APPS;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	// [BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
	private void updateDefaultAppPreference() {
//		List<ComponentName> prefActList = new ArrayList<>();
//		List<IntentFilter> intentList = new ArrayList<>();
//		mPm.getPreferredActivities(intentList, prefActList, null);
		for (int i = 0; i < ALL_KEYS.length; i++) {
			if(ALL_KEYS[i].equals(KEY_DEFAULT_BROWSER)){
				continue;
			}
			allPreference[i].refreshApps();
			if(updatePreferenceVisible(allPreference[i])) {
				continue;
			}
//			Intent intent = INTENT_MAP.get(ALL_KEYS[i]);
//			for (int j = 0; j < intentList.size(); j++) {
//				if (j < prefActList.size()) {
//					ComponentName appName = prefActList.get(j);
//					IntentFilter filter = intentList.get(j);
//
//					Set<String> categories = intent.getCategories();
//					String action = intent.getAction();
//					String type = intent.getType();
//					String scheme = intent.getScheme();
//					boolean compare = true;
//					if (categories != null && categories.size() > 0) {
//						String[] categoriesString = new String[categories
//								.size()];
//						categories.toArray(categoriesString);
//						for (int k = 0; k < categoriesString.length; k++) {
//							if (!filter.hasCategory(categoriesString[k])) {
//								compare = false;
//								break;
//							}
//						}
//					}
//					if (compare && action != null) {
//						compare = filter.hasAction(action);
//					}
//
//					if (compare && type != null) {
//						compare = filter.hasDataType(type);
//					}
//
//					if (allPreference[i].getIntentKey().equals(KEY_DEFAULT_MAIL_APP)
//							&& compare && scheme != null) {
//						compare = filter.hasDataScheme(scheme);
//					}
//
//					if (compare) {
//						allPreference[i].setValue(appName.getPackageName());
//						break;
//					}
//				}
//			}
		}
		
		updatePreferenceVisible(mDefaultBrowserPreference);
		updatePreferenceVisible(mDefaultPhonePreference);
		updatePreferenceVisible(mDefaultSmsPreference);
		updatePreferenceVisible(mDefaultImePreference);
	}
	
	private void setPreferredActivity(DefaultAppPreferenceBase preference, String packageName, String lastPackageName){
		String clearPackageName;
		if(preference.getKey().equals(KEY_DEFAULT_LAUNCHER_APP)) {
			clearPackageName = setDefaultApp(getIntentFilter(preference), preference.getCustomIntent(), packageName, lastPackageName);
		} else {
			clearPackageName = setDefaultApp2(preference, packageName, lastPackageName);
		}
		if(!TextUtils.isEmpty(clearPackageName)) {
			for (int i = 0; i < allPreference.length; i++) {
				if(preference == allPreference[i]) {
					continue;
				}
				if(clearPackageName.equals(allPreference[i].getValue())) {
					if (!allPreference[i].getIntentKey().equals(KEY_DEFAULT_BROWSER)) {
						setPreferredActivity(allPreference[i], allPreference[i].getValue(), allPreference[i].getLastPackageName());
					}
				}
			}
		}
	}
	
	private IntentFilter getIntentFilter(DefaultAppPreferenceBase preference){
		Intent intent = preference.getCustomIntent();
		Set<String> categories = intent.getCategories();
		IntentFilter filter = new IntentFilter();
		filter.addAction(intent.getAction());
		if (categories != null && categories.size() > 0) {
			for (String categoriesString : categories) {
				filter.addCategory(categoriesString);
			}
		}
		filter.addCategory(Intent.CATEGORY_DEFAULT);

		if (preference.getIntentKey().equals(KEY_DEFAULT_MAIL_APP) && intent.getScheme()!= null){
			filter.addDataScheme(intent.getScheme());
		}

		if (intent.getType()!= null){
			try {
				filter.addDataType(intent.getType());
			} catch (MalformedMimeTypeException e) {
				e.printStackTrace();
			}
		}
		
		return filter;
	}

	private String setDefaultApp(IntentFilter filter, Intent intent, String packageName, String lastPackageName) {
		String clearPackageName = null;
		String type = intent.getType();
		List<ResolveInfo> list = mPm.queryIntentActivitiesAsUser(intent, 0, myUserId);
		ComponentName[] set = new ComponentName[list.size()];
		String cls = null;
		int bestMatch = 0;
		for (int i = 0; i < list.size(); i++) {
			ResolveInfo r = list.get(i);
			set[i] = new ComponentName(r.activityInfo.packageName,
					r.activityInfo.name);
			if (r.match > bestMatch)
				bestMatch = r.match;
			if (!TextUtils.isEmpty(packageName) && packageName.equals(r.activityInfo.packageName)) {
				cls = r.activityInfo.name;
			}
			if(type!=null && ALL_ADD_TYPES.contains(type)){
			}
			
			if(!TextUtils.isEmpty(lastPackageName) && lastPackageName.equals(r.activityInfo.packageName)) {
				mPm.clearPackagePreferredActivities(r.activityInfo.packageName);
				clearPackageName = r.activityInfo.packageName;
			}
		}
		
		if (!TextUtils.isEmpty(cls)) {
			ComponentName cn = new ComponentName(packageName, cls);
			if(type!=null && ALL_ADD_TYPES.contains(type)){
				mPm.addPreferredActivity(filter, bestMatch, set, cn);
			}else{
				mPm.replacePreferredActivity(filter, bestMatch, set, cn);
			}
		}
		
		return clearPackageName;
	}
	
	private boolean updatePreferenceVisible(DefaultAppPreferenceBase preference) {
		boolean result = false;
		CharSequence[] packages = preference.getEntryValues();
		int itemCount = packages == null ? 0 : packages.length; 
		itemCount -= (preference.isShowItemNone() ? 1 : 0);
		if(itemCount <= 1) {
			removePreference(preference.getIntentKey());
			result = true;
		}
		return result;
	}
	
	private String setDefaultApp2(DefaultAppPreferenceBase preference, String packageName, String lastPackageName) {
		if(!TextUtils.isEmpty(lastPackageName)) {
			mPm.clearPackagePreferredActivities(lastPackageName);
		}
		if(!TextUtils.isEmpty(packageName)) {
			onTargetSelected(preference, packageName);
		}
		return lastPackageName;
	}
	
	private boolean onTargetSelected(DefaultAppPreferenceBase preference, String pkgName) {
        boolean alwaysCheck = true;
        final ResolveInfo ri = preference.getResolverInfo(pkgName);
        final Intent intent = preference.getResolvedIntent(pkgName, ri);
        List<ResolveInfo> resolverInfoList = preference.getResolverInfoList();
        
        if (intent != null) {
            // Build a reasonable intent filter, based on what matched.
            IntentFilter filter = new IntentFilter();
            Intent filterIntent;

            if (intent.getSelector() != null) {
                filterIntent = intent.getSelector();
            } else {
                filterIntent = intent;
            }

            String action = filterIntent.getAction();
            if (action != null) {
                filter.addAction(action);
            }
            Set<String> categories = filterIntent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            int cat = ri.match & IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = filterIntent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = filterIntent.resolveType(getContext());
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Log.w("ResolverActivity", e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                // We need the data specification if there was no type,
                // OR if the scheme is not one of our magical "file:"
                // or "content:" schemes (see IntentFilter for the reason).
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                                && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());

                    // Look through the resolved filter to determine which part
                    // of it matched the original Intent.
                    Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                    if (pIt != null) {
                        String ssp = data.getSchemeSpecificPart();
                        while (ssp != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(ssp)) {
                                filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                    Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                    if (aIt != null) {
                        while (aIt.hasNext()) {
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                filter.addDataAuthority(a.getHost(),
                                        port >= 0 ? Integer.toString(port) : null);
                                break;
                            }
                        }
                    }
                    pIt = ri.filter.pathsIterator();
                    if (pIt != null) {
                        String path = data.getPath();
                        while (path != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(path)) {
                                filter.addDataPath(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                }
            }

            if (filter != null) {
                final int N = resolverInfoList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i=0; i<N; i++) {
                    ResolveInfo r = resolverInfoList.get(i);
                    set[i] = new ComponentName(r.activityInfo.packageName,
                            r.activityInfo.name);
                    if (r.match > bestMatch) bestMatch = r.match;
                }
                if (alwaysCheck) {
                    final int userId = getContext().getUserId();
                    final PackageManager pm = getPackageManager();

                    // Set the preferred Activity
                    pm.addPreferredActivity(filter, bestMatch, set, intent.getComponent());

//                    if (ri.handleAllWebDataURI) {
//                        // Set default Browser if needed
//                        final String packageName = pm.getDefaultBrowserPackageNameAsUser(userId);
//                        if (TextUtils.isEmpty(packageName)) {
//                            pm.setDefaultBrowserPackageNameAsUser(ri.activityInfo.packageName, userId);
//                        }
//                    } else {
//                        // Update Domain Verification status
//                        ComponentName cn = intent.getComponent();
//                        String packageName = cn.getPackageName();
//                        String dataScheme = (data != null) ? data.getScheme() : null;
//
//                        boolean isHttpOrHttps = (dataScheme != null) &&
//                                (dataScheme.equals(IntentFilter.SCHEME_HTTP) ||
//                                        dataScheme.equals(IntentFilter.SCHEME_HTTPS));
//
//                        boolean isViewAction = (action != null) && action.equals(Intent.ACTION_VIEW);
//                        boolean hasCategoryBrowsable = (categories != null) &&
//                                categories.contains(Intent.CATEGORY_BROWSABLE);
//
//                        if (isHttpOrHttps && isViewAction && hasCategoryBrowsable) {
//                            pm.updateIntentVerificationStatusAsUser(packageName,
//                                    PackageManager.INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS,
//                                    userId);
//                        }
//                    }
                }
            }
        }
        return true;
    }
}

