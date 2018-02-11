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

package com.monster.permission.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.monster.appmanager.R;
import com.monster.appmanager.applications.AppInfoWithHeader;
import com.monster.appmanager.applications.InstalledAppDetails;
import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.appmanager.utils.LocationUtils;
import com.monster.appmanager.widget.PermissionsSelectPreference;
import com.monster.permission.ui.MstPermission.MstAppGroup;
import com.monster.permission.ui.MstPermission.MstPermEntry;
import com.monster.permission.ui.MstPermission.MstPermGroup;

import android.annotation.Nullable;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceCategory;
import mst.preference.PreferenceScreen;

public final class AppPermissionsFragmentSelect extends PermissionsFrameFragment
        implements OnPreferenceChangeListener {

    private static final String LOG_TAG = "ManagePermsFragment";

    static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";

//    private List<AppPermissionGroup> mToggledGroups;
//    private AppPermissions mAppPermissions;
    private PreferenceScreen mExtraScreen;

    private boolean mHasConfirmedRevoke;
    private PackageInfo packageInfo;
    private boolean isFirstRefresh = false;
    private List<String> packageNameList = new ArrayList<>();

    public static AppPermissionsFragmentSelect newInstance(String packageName) {
        return setPackageName(new AppPermissionsFragmentSelect(), packageName);
    }

    private static <T extends Fragment> T setPackageName(T fragment, String packageName) {
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
        fragment.setArguments(arguments);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        String packageName = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
        Activity activity = getActivity();
        packageInfo = getPackageInfo(activity, packageName);
        if (packageInfo == null) {
            Toast.makeText(activity, R.string.app_not_found_dlg_title, Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

//        mAppPermissions = new AppPermissions(activity, packageInfo, null, true, new Runnable() {
//            @Override
//            public void run() {
//            	ManagePermissionsInfoActivity activity = (ManagePermissionsInfoActivity)getActivity();
//            	if(!activity.isDestroyed() && !activity.isFinishing()) {
//            		activity.onMyActivityResult(0, Activity.RESULT_OK, null);
//            	}
////                getActivity().finish();
//            }
//        });
        
        packageNameList.add(packageName);
        mMstPermission.refresh(packageNameList, null);
        loadPreferences();
        isFirstRefresh = true;
    }

    @Override
    public void onResume() {
        super.onResume();
//        mAppPermissions.refresh();
//        onSetEmptyText();
        if(isFirstRefresh) {
        	isFirstRefresh = false;
        } else {
        	mMstPermission.refresh(packageNameList, null);
        	setPreferencesCheckedState();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if (mAppPermissions != null) {
            bindUi(getActivity(), packageInfo);
//        }
    }

    private static void bindUi(final Activity activity, PackageInfo packageInfo) {
        PackageManager pm = activity.getPackageManager();
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        final Intent infoIntent = new Intent();
    	infoIntent.setAction(AppInfoWithHeader.ACTION_APPLICATION_DETAILS_SETTINGS);
    	infoIntent.setData(Uri.fromParts("package", packageInfo.packageName, null));
    

        Drawable icon = appInfo.loadIcon(pm);
        CharSequence label = appInfo.loadLabel(pm);

        ActionBar ab = activity.getActionBar();
        if (ab != null) {
            ab.setTitle(R.string.app_permissions);
        }
        
        boolean disableAppdetailBtn = activity.getIntent().getBooleanExtra(InstalledAppDetails.EXTRA_KEY_FROM_APP_DETAIL, false);
        activity.findViewById(R.id.tool_bar).setVisibility(View.VISIBLE);
//        Button button = (Button)activity.findViewById(R.id.app_info);
        Button button = (Button)activity.findViewById(R.id.button1);
        button.setText(R.string.app_info);
        button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.startActivity(infoIntent);
			}
		});
        if(disableAppdetailBtn){
        	button.setEnabled(false);
        }
        
        Uri packageURI = Uri.parse("package:"+packageInfo.packageName);
        final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
        uninstallIntent.putExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, true);
        
//        button = (Button)activity.findViewById(R.id.delete_app);
        button = (Button)activity.findViewById(R.id.button2);
        button.setText(R.string.delete_app);
        button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.startActivityForResult(uninstallIntent, 0);
			}
		});
    }

    private void loadPreferences() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if (mExtraScreen != null) {
            mExtraScreen.removeAll();
        }

        final Preference extraPerms = new Preference(context);
        extraPerms.setLayoutResource(R.layout.permission_item);
        extraPerms.setIcon(R.drawable.ic_toc);
        extraPerms.setTitle(R.string.additional_permissions);
        
        PackageManager pm = getContext().getPackageManager();
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        Drawable appIcon = appInfo.loadIcon(pm);
        CharSequence label = appInfo.loadLabel(pm);
    
        //头部，包括图标,名称,版本号
        Preference preferenceHead = new Preference(context);
        preferenceHead.setLayoutResource(R.layout.preference_app_info);
        preferenceHead.setIcon(appIcon);
        preferenceHead.setTitle(label);
		try {
			PackageInfo pi = pm.getPackageInfo(appInfo.packageName, 0);
	        preferenceHead.setSummary(getResources().getString(R.string.version_text, pi.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}  
        screen.addPreference(preferenceHead);
        
        MstAppGroup appGroup = mMstPermission.getAppGroupMap().get(appInfo.packageName);
        if(appGroup != null && appGroup.size() > 0) {
        	Map<String, MstPermGroup> permGroupMap = mMstPermission.getPermGroupMap();
        	MstPermGroup permGroup;
        	List<MstPermEntry> seniorEntryList = new LinkedList<>();
        	int i = 0;
        	PreferenceCategory preferenceTitle = null;
        	List<Map.Entry<String, MstPermEntry>> mappingList = mMstPermission.getSortedPermEntryList(appGroup.getPermEntryMap());
        	for(Map.Entry<String, MstPermEntry> permEntryMap : mappingList){
//        	for (Map.Entry<String, MstPermEntry> permEntryMap : appGroup.getPermEntryMap().entrySet()) {
        		MstPermEntry entry = permEntryMap.getValue();
        		permGroup = permGroupMap.get(entry.getGroupName());
        		if(permGroup.isSeniorPermission()) {
        			seniorEntryList.add(entry);
        			continue;
        		}
        		
        		if(i == 0) {
        			preferenceTitle = new PreferenceCategory(context);
        			preferenceTitle.setLayoutResource(R.layout.permission_category);
        			screen.addPreference(preferenceTitle);
        		}
        		
        		addPreferenceSelect(screen, permGroup, entry);
        		i++;
        	}
        	if(preferenceTitle != null && i > 0) {
        		preferenceTitle.setTitle(getResources().getString(R.string.sensitive_authority, Integer.valueOf(i).toString()));
        	}

        	
        	if(seniorEntryList.size() > 0) {
        		preferenceTitle = new PreferenceCategory(context);
        		preferenceTitle.setLayoutResource(R.layout.permission_category);
        		screen.addPreference(preferenceTitle);
        		
        		for (MstPermEntry entry : seniorEntryList) {
        			permGroup = permGroupMap.get(entry.getGroupName());
        			addPreferenceSelect(screen, permGroup, entry);
        		}
        		preferenceTitle.setTitle(getResources().getString(R.string.senior_authority, Integer.valueOf(seniorEntryList.size()).toString()));
        	}
        }
    }
    
    private void addPreferenceSelect(PreferenceScreen screen, MstPermGroup permGroup, MstPermEntry entry) {
    	PermissionsSelectPreference preference = new PermissionsSelectPreference(getContext());
    	preference.setOnPreferenceChangeListener(this);
    	preference.setKey(entry.getGroupName());
    	preference.setIcon(permGroup.getDrawable());
    	preference.setTitle(permGroup.getLabel());
    	preference.setPersistent(false);
    	preference.setEnabled(true);
    	preference.setSelected(entry, mMstPermission);
//    	if (!permGroup.isSeniorPermission()) {
    		screen.addPreference(preference);
//    		preferenceTitle.setTitle(getResources().getString(R.string.sensitive_authority, Integer.valueOf(i).toString()));
//    	} else {
//    		if (mExtraScreen == null) {
//    			mExtraScreen = getPreferenceManager().createPreferenceScreen(context);
//    		}
//    		mExtraScreen.addPreference(preference);
//    	}

//    	if (mExtraScreen != null) {
//    		extraPerms.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//    			@Override
//    			public boolean onPreferenceClick(Preference preference) {
//    				AdditionalPermissionsFragment frag = new AdditionalPermissionsFragment();
//    				setPackageName(frag, getArguments().getString(Intent.EXTRA_PACKAGE_NAME));
//    				frag.setTargetFragment(AppPermissionsFragmentSelect.this, 0);
//    				getFragmentManager().beginTransaction()
//    				.replace(com.mst.R.id.content, frag)
//    				.addToBackStack(null)
//    				.commit();
//    				return true;
//    			}
//    		});
//    		int count = mExtraScreen.getPreferenceCount();
//    		extraPerms.setSummary(getResources().getQuantityString(
//    				R.plurals.additional_permissions_more, count, count));
//    		screen.addPreference(extraPerms);
//    	}
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
//        String groupName = preference.getKey();
//        final AppPermissionGroup group = mAppPermissions.getPermissionGroup(groupName);
//
//        if (group == null) {
//            return false;
//        }
//
//        ManagePermissionsInfoActivity activity = (ManagePermissionsInfoActivity) getActivity();
//        if (activity.isObscuredTouch()) {
//            activity.showOverlayDialog();
//            return false;
//        }
//
//        addToggledGroup(group);
//
//        if (LocationUtils.isLocationGroupAndProvider(group.getName(), group.getApp().packageName)) {
//            LocationUtils.showLocationDialog(getContext(), mAppPermissions.getAppLabel());
//            return false;
//        }
//        if (newValue == Boolean.TRUE) {
//            group.grantRuntimePermissions(false);
//        } else {
//            final boolean grantedByDefault = group.hasGrantedByDefaultPermission();
//            if (grantedByDefault || (!group.hasRuntimePermission() && !mHasConfirmedRevoke)) {
//                new AlertDialog.Builder(getContext())
//                        .setMessage(grantedByDefault ? R.string.system_warning
//                                : R.string.old_sdk_deny_warning)
//                        .setNegativeButton(R.string.cancel, null)
//                        .setPositiveButton(R.string.grant_dialog_button_deny,
//                                new OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {/*
//                                ((PermissionsSelectPreference) preference).setChecked(false);
//                                group.revokeRuntimePermissions(false);
//                                if (!grantedByDefault) {
//                                    mHasConfirmedRevoke = true;
//                                }
//                            */}
//                        })
//                        .show();
//                return false;
//            } else {
//                group.revokeRuntimePermissions(false);
//            }
//        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
//        logToggledGroups();
    }

//    private void addToggledGroup(AppPermissionGroup group) {
//        if (mToggledGroups == null) {
//            mToggledGroups = new ArrayList<>();
//        }
//        // Double toggle is back to initial state.
//        if (mToggledGroups.contains(group)) {
//            mToggledGroups.remove(group);
//        } else {
//            mToggledGroups.add(group);
//        }
//    }
//
//    private void logToggledGroups() {
//        if (mToggledGroups != null) {
//            mToggledGroups = null;
//        }
//    }

    private void setPreferencesCheckedState() {
        setPreferencesCheckedState(getPreferenceScreen());
        if (mExtraScreen != null) {
            setPreferencesCheckedState(mExtraScreen);
        }
    }

    private void setPreferencesCheckedState(PreferenceScreen screen) {
        int preferenceCount = screen.getPreferenceCount();
        String groupName;
        MstPermEntry entry;
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = screen.getPreference(i);
            if (preference instanceof PermissionsSelectPreference) {
            	PermissionsSelectPreference switchPref = (PermissionsSelectPreference) preference;
            	groupName = switchPref.getKey();
            	entry = mMstPermission.getPermEntry(packageInfo.packageName, groupName);
            	if(entry != null) {
            		switchPref.setSelected(entry, mMstPermission);
            	}
            	
//                AppPermissionGroup group = mAppPermissions.getPermissionGroup(switchPref.getKey());
//                if (group != null) {
//                	switchPref.setSelected(group.areRuntimePermissionsGranted(), group.isUserFixed());
//                }
            }
        }
    }

    public static PackageInfo getPackageInfo(Context activity, String packageName) {
        try {
            return activity.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static class AdditionalPermissionsFragment extends PermissionsFrameFragment {
        AppPermissionsFragmentSelect mOuterFragment;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            mOuterFragment = (AppPermissionsFragmentSelect) getTargetFragment();
            super.onCreate(savedInstanceState);
            onCreatePreferences();
        }

        public void onCreatePreferences() {
            setPreferenceScreen(mOuterFragment.mExtraScreen);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            String packageName = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
            bindUi(getActivity(), getPackageInfo(getActivity(), packageName));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
