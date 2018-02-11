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

package com.android.packageinstaller.permission.ui.handheld;

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
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.packageinstaller.R;
import com.android.packageinstaller.permission.model.AppPermissionGroup;
import com.android.packageinstaller.permission.model.AppPermissions;
import com.android.packageinstaller.permission.model.Permission;
import com.android.packageinstaller.permission.utils.LocationUtils;
import com.android.packageinstaller.permission.utils.SafetyNetLogger;
import com.android.packageinstaller.permission.utils.Utils;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;

import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public final class AppPermissionsFragment extends SettingsWithHeader
        implements OnPreferenceChangeListener ,OnMenuItemClickListener{

    private static final String LOG_TAG = "ManagePermsFragment";

    static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";

    private static final int MENU_ALL_PERMS = 0;

    private List<AppPermissionGroup> mToggledGroups;
    private AppPermissions mAppPermissions;
    private PreferenceScreen mExtraScreen;

    private boolean mHasConfirmedRevoke;
    String mPackageName;

    public static AppPermissionsFragment newInstance(String packageName) {
        return setPackageName(new AppPermissionsFragment(), packageName);
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
        setLoading(true /* loading */, false /* animate */);
        setHasOptionsMenu(true);
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mPackageName = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
        Activity activity = getActivity();
        PackageInfo packageInfo = getPackageInfo(activity, mPackageName);
        if (packageInfo == null) {
            Toast.makeText(activity, R.string.app_not_found_dlg_title, Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        mAppPermissions = new AppPermissions(activity, packageInfo, null, true, new Runnable() {
            @Override
            public void run() {
                getActivity().finish();
            }
        });
        loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAppPermissions.refresh();
        loadPreferences();
        setPreferencesCheckedState();
        
        /***************** 新风格 start、*************************/
        if (mToolbar != null) {
        	mToolbar.setTitle(R.string.app_permissions);
        	mToolbar.setNavigationOnClickListener(new android.view.View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				getActivity().finish() ;
    			}
    		}) ;
        }
        /********************end*************************/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().finish();
                return true;
            }

            case MENU_ALL_PERMS: {
                Fragment frag = AllAppPermissionsFragment.newInstance(
                        getArguments().getString(Intent.EXTRA_PACKAGE_NAME));
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, frag)
                        .addToBackStack("AllPerms")
                        .commit();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAppPermissions != null) {
            bindUi(this, mAppPermissions.getPackageInfo());
            /***************** 新风格 start、*************************/
            if (mToolbar != null) {
            	mToolbar.setTitle(R.string.app_permissions);
            	mToolbar.inflateMenu(R.menu.show_all_perms);
                mToolbar.setOnMenuItemClickListener(this);
            }
            /********************end*************************/
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, MENU_ALL_PERMS, Menu.NONE, R.string.all_permissions);
        HelpUtils.prepareHelpMenuItem(getActivity(), menu, R.string.help_app_permissions,
                getClass().getName());
    }

    private static void bindUi(SettingsWithHeader fragment, PackageInfo packageInfo) {
        Activity activity = fragment.getActivity();
        PackageManager pm = activity.getPackageManager();
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        Intent infoIntent = null;
        if (!activity.getIntent().getBooleanExtra(EXTRA_HIDE_INFO_BUTTON, false)) {
            infoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageInfo.packageName, null));
        }

        Drawable icon = appInfo.loadIcon(pm);
        CharSequence label = appInfo.loadLabel(pm);
        fragment.setHeader(icon, label, infoIntent);

        ActionBar ab = activity.getActionBar();
        if (ab != null) {
            ab.setTitle(R.string.app_permissions);
        }
    }

    private void loadPreferences() {
        Context context = getActivity();
        if (context == null) {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            screen = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(screen);
        }

        screen.removeAll();

        if (mExtraScreen != null) {
            mExtraScreen.removeAll();
        }

        final Preference extraPerms = new Preference(context);
        extraPerms.setIcon(R.drawable.ic_toc);
        extraPerms.setTitle(R.string.additional_permissions);

        for (AppPermissionGroup group : mAppPermissions.getPermissionGroups()) {
            if (!Utils.shouldShowPermission(group, mAppPermissions.getPackageInfo().packageName)) {
                continue;
            }

            boolean isPlatform = group.getDeclaringPackage().equals(Utils.OS_PKG);

            RestrictedSwitchPreference preference = new RestrictedSwitchPreference(context);
            preference.setOnPreferenceChangeListener(this);
            preference.setKey(group.getName());
            Drawable icon = Utils.loadDrawable(context.getPackageManager(),
                    group.getIconPkg(), group.getIconResId());
            preference.setIcon(Utils.applyTint(getContext(), icon,
                    android.R.attr.colorControlNormal));
            preference.setTitle(group.getLabel());
            if (group.isPolicyFixed()) {
                EnforcedAdmin admin = RestrictedLockUtils.getProfileOrDeviceOwner(getContext(),
                        group.getUserId());
                if (admin != null) {
                    preference.setDisabledByAdmin(admin);
                    preference.setSummary(R.string.disabled_by_admin_summary_text);
                } else {
                    preference.setSummary(R.string.permission_summary_enforced_by_policy);
                    preference.setEnabled(false);
                }
            }
            preference.setPersistent(false);
            preference.setChecked(group.areRuntimePermissionsGranted());

            if (isPlatform) {
                screen.addPreference(preference);
            } else {
                if (mExtraScreen == null) {
                    mExtraScreen = getPreferenceManager().createPreferenceScreen(context);
                }
                mExtraScreen.addPreference(preference);
            }
            if (AppPermissionGroup.isStrictOpEnable()) {
                try {
                    PackageManager pm = context.getPackageManager();
                    for (Permission permission : group.getPermissions()) {
                        PermissionInfo perm = pm.getPermissionInfo(permission.getName(), 0);
                        final String[] filterPermissions = new String[]{permission.getName()};

                        if (perm.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                            SwitchPreference preference_permission = new SwitchPreference(context);
                            preference_permission.setOnPreferenceChangeListener(this);
                            preference_permission.setKey(permission.getName());
                            preference_permission.setTitle(perm.loadLabel(pm));
                            preference_permission.setPersistent(false);
                            preference_permission.setEnabled(true);
                            AppPermissionGroup permissionGroup = getPermisssionGroup(perm.group);
                            preference_permission.setChecked(
                                    permissionGroup.areRuntimePermissionsGranted(filterPermissions));
                            screen.addPreference(preference_permission);
                        } else if (perm.protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
                            continue;
                        }
                    }
                } catch (NameNotFoundException e) {
                    Log.e(LOG_TAG, "Problem getting package info for " + mPackageName, e);
                }
            }
        }

        if (mExtraScreen != null) {
            extraPerms.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AdditionalPermissionsFragment frag = new AdditionalPermissionsFragment();
                    setPackageName(frag, getArguments().getString(Intent.EXTRA_PACKAGE_NAME));
                    frag.setTargetFragment(AppPermissionsFragment.this, 0);
                    getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, frag)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
            });
            int count = mExtraScreen.getPreferenceCount();
            extraPerms.setSummary(getResources().getQuantityString(
                    R.plurals.additional_permissions_more, count, count));
            screen.addPreference(extraPerms);
        }

        setLoading(false /* loading */, true /* animate */);
    }

    private AppPermissionGroup getPermisssionGroup(String group) {
        for (AppPermissionGroup mGroup : mAppPermissions.getPermissionGroups()) {
            if (group.equals(mGroup.getName())) {
                return mGroup;
            }
        }
        return null;
    }

    private void updateEveryPermissionPreference(AppPermissionGroup group) {
        PackageManager pm = getContext().getPackageManager();
        PreferenceScreen screen = getPreferenceScreen();
        for (Permission permission : group.getPermissions()) {
            Preference permission_preference
                    = screen.findPreference((CharSequence) permission.getName());
            try {
                PermissionInfo permInfo = pm.getPermissionInfo(permission.getName(), 0);
                AppPermissionGroup permissionGroup = getPermisssionGroup(permInfo.group);
                final String[] filterPermissions = new String[]{permission.getName()};
                ((SwitchPreference) permission_preference).setChecked(
                        permissionGroup.areRuntimePermissionsGranted(filterPermissions));
            } catch (NameNotFoundException e) {
                Log.e(LOG_TAG, "Failed to update permission_preference", e);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
        String key = preference.getKey();
        final String[] filterPermissions = new String[]{key};
        final AppPermissionGroup group = mAppPermissions.getPermissionGroup(key);
        PackageManager pm = getContext().getPackageManager();

        if (group == null) {
            if (AppPermissionGroup.isStrictOpEnable()) {
                try {
                    PermissionInfo permInfo = pm.getPermissionInfo(key, 0);
                    final AppPermissionGroup title_group
                            = mAppPermissions.getPermissionGroup(permInfo.group);
                    if (newValue == Boolean.TRUE) {
                        title_group.grantRuntimePermissions(false, filterPermissions);
                    } else {
                        title_group.revokeRuntimePermissions(false, filterPermissions);
                    }
                    //group preferene update
                    PreferenceScreen screen = getPreferenceScreen();
                    Preference group_preference = screen.findPreference((CharSequence) permInfo.group);
                    AppPermissionGroup permissionGroup = getPermisssionGroup(permInfo.group);
                    ((SwitchPreference) group_preference).setChecked(
                            permissionGroup.areRuntimePermissionsGranted());
                    ((SwitchPreference)preference).setChecked(permissionGroup.areRuntimePermissionsGranted(filterPermissions));
                } catch (NameNotFoundException e) {
                    Log.e(LOG_TAG, "Problem getting package info for ", e);
                }
            }
            return false;
        }

        addToggledGroup(group);

        if (LocationUtils.isLocationGroupAndProvider(group.getName(), group.getApp().packageName)) {
            LocationUtils.showLocationDialog(getContext(), mAppPermissions.getAppLabel());
            return false;
        }
        if (newValue == Boolean.TRUE) {
            group.grantRuntimePermissions(false);
            if (AppPermissionGroup.isStrictOpEnable()) {
                updateEveryPermissionPreference(group);
            }
        } else {
            final boolean grantedByDefault = group.hasGrantedByDefaultPermission();
            if (grantedByDefault || (!group.hasRuntimePermission() && !mHasConfirmedRevoke)) {
                new AlertDialog.Builder(getContext())
                        .setMessage(grantedByDefault ? R.string.system_warning
                                : R.string.old_sdk_deny_warning)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.grant_dialog_button_deny_anyway,
                                new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((SwitchPreference) preference).setChecked(false);
                                group.revokeRuntimePermissions(false);
                                if (AppPermissionGroup.isStrictOpEnable()) {
                                    updateEveryPermissionPreference(group);
                                }
                                if (!grantedByDefault) {
                                    mHasConfirmedRevoke = true;
                                }
                            }
                        })
                        .show();
                return false;
            } else {
            		group.revokeRuntimePermissions(false);
                if (AppPermissionGroup.isStrictOpEnable()) {
                    updateEveryPermissionPreference(group);
                }
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        logToggledGroups();
    }

    private void addToggledGroup(AppPermissionGroup group) {
        if (mToggledGroups == null) {
            mToggledGroups = new ArrayList<>();
        }
        // Double toggle is back to initial state.
        if (mToggledGroups.contains(group)) {
            mToggledGroups.remove(group);
        } else {
            mToggledGroups.add(group);
        }
    }

    private void logToggledGroups() {
        if (mToggledGroups != null) {
            String packageName = mAppPermissions.getPackageInfo().packageName;
            SafetyNetLogger.logPermissionsToggled(packageName, mToggledGroups);
            mToggledGroups = null;
        }
    }

    private void setPreferencesCheckedState() {
        setPreferencesCheckedState(getPreferenceScreen());
        if (mExtraScreen != null) {
            setPreferencesCheckedState(mExtraScreen);
        }
    }

    private void setPreferencesCheckedState(PreferenceScreen screen) {
        int preferenceCount = screen.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = screen.getPreference(i);
            if (preference instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) preference;
                AppPermissionGroup group = mAppPermissions.getPermissionGroup(switchPref.getKey());
                if (group != null) {
                    switchPref.setChecked(group.areRuntimePermissionsGranted());
                }
            }
        }
    }

    private static PackageInfo getPackageInfo(Activity activity, String packageName) {
        try {
            return activity.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(LOG_TAG, "No package:" + activity.getCallingPackage(), e);
            return null;
        }
    }

    public static class AdditionalPermissionsFragment extends SettingsWithHeader {
        AppPermissionsFragment mOuterFragment;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            mOuterFragment = (AppPermissionsFragment) getTargetFragment();
            super.onCreate(savedInstanceState);
            setHeader(mOuterFragment.mIcon, mOuterFragment.mLabel, mOuterFragment.mInfoIntent);
            setHasOptionsMenu(true);
            setPreferenceScreen(mOuterFragment.mExtraScreen);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            String packageName = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
            bindUi(this, getPackageInfo(getActivity(), packageName));
            /***************** 新风格 start、*************************/
            if (mToolbar != null) {
            	mToolbar.setTitle(R.string.app_permissions);
            }
            /********************end*************************/
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
    
    @Override
   	public boolean onMenuItemClick(MenuItem item) {
   		switch (item.getItemId()) {
   		case R.id.show_all_perm:
   		 Fragment frag = AllAppPermissionsFragment.newInstance(
                 getArguments().getString(Intent.EXTRA_PACKAGE_NAME));
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, frag)
                 .addToBackStack("AllPerms")
                 .commit();
   			break;
   		}
   		return false;
   	}
}