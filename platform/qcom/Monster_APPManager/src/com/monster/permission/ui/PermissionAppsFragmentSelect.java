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
import java.util.List;
import java.util.Map;

import com.monster.appmanager.R;
import com.monster.appmanager.applications.MstSpacePreference;
import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.appmanager.utils.LocationUtils;
import com.monster.appmanager.utils.PermissionApps;
import com.monster.appmanager.utils.PermissionApps.Callback;
import com.monster.appmanager.utils.PermissionApps.PermissionApp;
import com.monster.appmanager.utils.SafetyNetLogger;
import com.monster.appmanager.utils.Utils;
import com.monster.appmanager.widget.PermissionsSelectPreference;
import com.monster.permission.ui.MstPermission.MstAppGroup;
import com.monster.permission.ui.MstPermission.MstPermEntry;
import com.monster.permission.ui.MstPermission.MstPermGroup;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceCategory;
import mst.preference.PreferenceScreen;
import mst.preference.SwitchPreference;

public final class PermissionAppsFragmentSelect extends PermissionsFrameFragment implements Callback,
        OnPreferenceChangeListener {

    private static final int MENU_SHOW_SYSTEM = Menu.FIRST;
    private static final int MENU_HIDE_SYSTEM = Menu.FIRST + 1;
    private static final String KEY_SHOW_SYSTEM_PREFS = "_showSystem";

    public static PermissionAppsFragmentSelect newInstance(String permissionName) {
        return setPermissionName(new PermissionAppsFragmentSelect(), permissionName);
    }

    private static <T extends Fragment> T setPermissionName(T fragment, String permissionName) {
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_PERMISSION_NAME, permissionName);
        fragment.setArguments(arguments);
        return fragment;
    }

    private PermissionApps mPermissionApps;

    private PreferenceScreen mExtraScreen;

    private ArrayMap<String, AppPermissionGroup> mToggledGroups;
    private ArraySet<String> mLauncherPkgs;
    private boolean mHasConfirmedRevoke;

    private boolean mShowSystem;
    private MenuItem mShowSystemMenu;
    private MenuItem mHideSystemMenu;

    private Callback mOnPermissionsLoadedListener;
    private String[] permitionTypes;
    private String groupName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permitionTypes = getContext().getResources().getStringArray(R.array.permition_types);
        setHasOptionsMenu(true);
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mLauncherPkgs = Utils.getLauncherPackages(getContext());

        groupName = getArguments().getString(Intent.EXTRA_PERMISSION_NAME);
//        mPermissionApps = new PermissionApps(getActivity(), groupName, this);
//        mPermissionApps.refresh(true);
    }

    @Override
    public void onResume() {
        super.onResume();
//        mPermissionApps.refresh(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mShowSystemMenu = menu.add(Menu.NONE, MENU_SHOW_SYSTEM, Menu.NONE,
                R.string.menu_show_system);
        mHideSystemMenu = menu.add(Menu.NONE, MENU_HIDE_SYSTEM, Menu.NONE,
                R.string.menu_hide_system);
        updateMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case MENU_SHOW_SYSTEM:
            case MENU_HIDE_SYSTEM:
                mShowSystem = item.getItemId() == MENU_SHOW_SYSTEM;
                if (mPermissionApps.getApps() != null) {
                    onPermissionsLoaded(mPermissionApps);
                }
                updateMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMenu() {
        mShowSystemMenu.setVisible(!mShowSystem);
        mHideSystemMenu.setVisible(mShowSystem);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        bindUi(this, mPermissionApps);
        hideEmptyText();
    }

    private static void bindUi(Fragment fragment, PermissionApps permissionApps) {
        final Drawable icon = permissionApps.getIcon();
        final CharSequence label = permissionApps.getLabel();
        final ActionBar ab = fragment.getActivity().getActionBar();
        if (ab != null) {
            ab.setTitle(fragment.getString(R.string.permission_title, label));
        }
    }

    private void setOnPermissionsLoadedListener(Callback callback) {
        mOnPermissionsLoadedListener = callback;
    }

    @Override
    public void onPermissionsLoaded(PermissionApps permissionApps) {
        Context context = getContext();

        if (context == null) {
            return;
        }

        boolean isTelevision = Utils.isTelevision(context);
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if(screen.getPreferenceCount() <= 0) {
        	MstSpacePreference sp = new MstSpacePreference(context);
        	sp.setHeight(getContext().getResources().getDimensionPixelSize(R.dimen.permission_category_header_padding_top));
        	screen.addPreference(sp);
        }

        ArraySet<String> preferencesToRemove = new ArraySet<>();
        for (int i = 0, n = screen.getPreferenceCount(); i < n; i++) {
            preferencesToRemove.add(screen.getPreference(i).getKey());
        }
        if (mExtraScreen != null) {
            for (int i = 0, n = mExtraScreen.getPreferenceCount(); i < n; i++) {
                preferencesToRemove.add(mExtraScreen.getPreference(i).getKey());
            }
        }
        int[] indexCount = permissionApps.getAllTypeCount(mLauncherPkgs);
        int addIndex = 0;
        for (PermissionApp app : permissionApps.getApps()) {
            if (!Utils.shouldShowPermission(app)) {
                continue;
            }

            String key = app.getKey();
            preferencesToRemove.remove(key);
            Preference existingPref = screen.findPreference(key);
            if (existingPref == null && mExtraScreen != null) {
                existingPref = mExtraScreen.findPreference(key);
            }

            boolean isSystemApp = Utils.isSystem(app, mLauncherPkgs);
            if (isSystemApp && !isTelevision && !mShowSystem) {
                if (existingPref != null) {
                    screen.removePreference(existingPref);
                }
                continue;
            }

            if (existingPref != null) {
                // If existing preference - only update its state.
                if (app.isPolicyFixed()) {
                    existingPref.setSummary(getString(
                            R.string.permission_summary_enforced_by_policy));
                }
                existingPref.setPersistent(false);
                existingPref.setEnabled(!app.isPolicyFixed());
                if (existingPref instanceof SwitchPreference) {
                    ((SwitchPreference) existingPref)
                            .setChecked(app.areRuntimePermissionsGranted());
                }
                continue;
            }
            String stringIndex = null;
            int titleRes = 0;
            int itemCount = 0;
            if(indexCount[0]>0 && addIndex==0){
            	stringIndex = permitionTypes[0];
            	titleRes = R.string.permission_title_allow_count;
            	itemCount = indexCount[0];
            }else if(indexCount[1]>0 && addIndex==indexCount[0]){
            	stringIndex = permitionTypes[1];
            	titleRes = R.string.permission_title_ask_count;
            	itemCount = indexCount[1];
            }else if(indexCount[2]>0 && addIndex==(indexCount[0]+indexCount[1])){
            	stringIndex = permitionTypes[2];
            	titleRes = R.string.permission_title_disallow_count;
            	itemCount = indexCount[2];
            }
            if(stringIndex!=null){
            	PreferenceCategory preferenceTitle = new PreferenceCategory(context);
            	preferenceTitle.setLayoutResource(R.layout.permission_category2);
                screen.addPreference(preferenceTitle);
                preferenceTitle.setTitle(getString(titleRes, itemCount));
            }
            addIndex++;
            PermissionsSelectPreference pref = new PermissionsSelectPreference(context, R.layout.permission_app_item2);
            pref.setOnPreferenceChangeListener(this);
            pref.setKey(app.getKey());
            pref.setIcon(app.getIcon());
            pref.setTitle(app.getLabel());
            if (app.isPolicyFixed()) {
                pref.setSummary(getString(R.string.permission_summary_enforced_by_policy));
            }
            pref.setPersistent(false);
            pref.setEnabled(!app.isPolicyFixed());
            pref.setSelected(app.mAppPermissionGroup.areRuntimePermissionsGranted(), app.mAppPermissionGroup.isUserFixed());
            pref.setGroup(app.mAppPermissionGroup);
            if (isSystemApp && isTelevision) {
                if (mExtraScreen == null) {
                    mExtraScreen = getPreferenceManager().createPreferenceScreen(context);
                }
                mExtraScreen.addPreference(pref);
            } else {
                screen.addPreference(pref);
            }
        }

        if (mExtraScreen != null) {
            preferencesToRemove.remove(KEY_SHOW_SYSTEM_PREFS);
            Preference pref = screen.findPreference(KEY_SHOW_SYSTEM_PREFS);

            if (pref == null) {
                pref = new Preference(context);
                pref.setKey(KEY_SHOW_SYSTEM_PREFS);
                pref.setIcon(Utils.applyTint(context, R.drawable.ic_toc,
                        android.R.attr.colorControlNormal));
                pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SystemAppsFragment frag = new SystemAppsFragment();
                        setPermissionName(frag, getArguments().getString(Intent.EXTRA_PERMISSION_NAME));
                        frag.setTargetFragment(PermissionAppsFragmentSelect.this, 0);
                        getFragmentManager().beginTransaction()
                            .replace(com.mst.R.id.content, frag)
                            .addToBackStack("SystemApps")
                            .commit();
                        return true;
                    }
                });
                screen.addPreference(pref);
            }

            int grantedCount = 0;
            for (int i = 0, n = mExtraScreen.getPreferenceCount(); i < n; i++) {
                if (((SwitchPreference) mExtraScreen.getPreference(i)).isChecked()) {
                    grantedCount++;
                }
            }
            pref.setSummary(getString(R.string.app_permissions_group_summary,
                    grantedCount, mExtraScreen.getPreferenceCount()));
        }

        for (String key : preferencesToRemove) {
            Preference pref = screen.findPreference(key);
            if (pref != null) {
                screen.removePreference(pref);
            } else if (mExtraScreen != null) {
                pref = mExtraScreen.findPreference(key);
                if (pref != null) {
                    mExtraScreen.removePreference(pref);
                }
            }
        }

        if (mOnPermissionsLoadedListener != null) {
            mOnPermissionsLoadedListener.onPermissionsLoaded(permissionApps);
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
        String pkg = preference.getKey();
        final PermissionApp app = mPermissionApps.getApp(pkg);

        if (app == null) {
            return false;
        }

        OverlayTouchActivity activity = (OverlayTouchActivity) getActivity();
        if (activity.isObscuredTouch()) {
            activity.showOverlayDialog();
            return false;
        }

        addToggledGroup(app.getPackageName(), app.getPermissionGroup());

        if (LocationUtils.isLocationGroupAndProvider(mPermissionApps.getGroupName(),
                app.getPackageName())) {
            LocationUtils.showLocationDialog(getContext(), app.getLabel());
            return false;
        }
        if (newValue == Boolean.TRUE) {
            app.grantRuntimePermissions();
        } else {
            final boolean grantedByDefault = app.hasGrantedByDefaultPermissions();
            if (grantedByDefault || (!app.hasRuntimePermissions() && !mHasConfirmedRevoke)) {
                new AlertDialog.Builder(getContext())
                        .setMessage(grantedByDefault ? R.string.system_warning
                                : R.string.old_sdk_deny_warning)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.grant_dialog_button_deny,
                                new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((SwitchPreference) preference).setChecked(false);
                                app.revokeRuntimePermissions();
                                if (!grantedByDefault) {
                                    mHasConfirmedRevoke = true;
                                }
                            }
                        })
                        .show();
                return false;
            } else {
                app.revokeRuntimePermissions();
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        logToggledGroups();
    }

    private void addToggledGroup(String packageName, AppPermissionGroup group) {
        if (mToggledGroups == null) {
            mToggledGroups = new ArrayMap<>();
        }
        // Double toggle is back to initial state.
        if (mToggledGroups.containsKey(packageName)) {
            mToggledGroups.remove(packageName);
        } else {
            mToggledGroups.put(packageName, group);
        }
    }

    private void logToggledGroups() {
        if (mToggledGroups != null) {
            final int groupCount = mToggledGroups.size();
            for (int i = 0; i < groupCount; i++) {
                String packageName = mToggledGroups.keyAt(i);
                List<AppPermissionGroup> groups = new ArrayList<>();
                groups.add(mToggledGroups.valueAt(i));
                SafetyNetLogger.logPermissionsToggled(packageName, groups);
            }
            mToggledGroups = null;
        }
    }

    public static class SystemAppsFragment extends PermissionsFrameFragment implements Callback {
        PermissionAppsFragmentSelect mOuterFragment;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            mOuterFragment = (PermissionAppsFragmentSelect) getTargetFragment();
            super.onCreate(savedInstanceState);
            onCreatePreferences();
        }

        public void onCreatePreferences() {
            if (mOuterFragment.mExtraScreen != null) {
                setPreferenceScreen();
            } else {
                mOuterFragment.setOnPermissionsLoadedListener(this);
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            String groupName = getArguments().getString(Intent.EXTRA_PERMISSION_NAME);
            PermissionApps permissionApps = new PermissionApps(getActivity(), groupName, null);
//            bindUi(this, permissionApps);
        }

        @Override
        public void onPermissionsLoaded(PermissionApps permissionApps) {
            setPreferenceScreen();
            mOuterFragment.setOnPermissionsLoadedListener(null);
        }

        private void setPreferenceScreen() {
            setPreferenceScreen(mOuterFragment.mExtraScreen);
        }
    }
    
    @Override
	protected void onRefreshPermission() {
		super.onRefreshPermission();
        setLoading(true, false);
        
        List<String> permissionList = new ArrayList<>();
        permissionList.add(groupName);
        mMstPermission.refreshAsync(null, permissionList);
	}
	
	@Override
	public void onPermRefreshComplete() {
		super.onPermRefreshComplete();
		updatePermissionsUi();
		setLoading(false, false);
	}

	@Override
	public void onPermRefreshError() {
		super.onPermRefreshError();
	}	
	
    private void updatePermissionsUi() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        if(screen.getPreferenceCount() <= 0) {
        	MstSpacePreference sp = new MstSpacePreference(context);
        	sp.setHeight(getContext().getResources().getDimensionPixelSize(R.dimen.permission_category_header_padding_top));
        	screen.addPreference(sp);
        }
        
        MstPermGroup permGroup =  mMstPermission.getPermGroupMap().get(groupName);
        List<MstAppGroup> appGroupList =  permGroup.getAppGroupList();
        if(appGroupList.size() > 0) {
        	List<MstAppGroup> allowAppGroupList =  new ArrayList<>();
        	List<MstAppGroup> askAppGroupList =  new ArrayList<>();
        	List<MstAppGroup> disableAppGroupList =  new ArrayList<>();

        	for (MstAppGroup app : appGroupList) {
        		if(app.get(groupName).getStatus() == MstPermission.ALLOW_MODE) {
        			allowAppGroupList.add(app);
        		} else if(app.get(groupName).getStatus() == MstPermission.ASK_MODE) {
        			askAppGroupList.add(app);
        		} else {
        			disableAppGroupList.add(app);
        		}
        	}
        	
        	if(allowAppGroupList.size() > 0) {
        		addCategoryTitle(screen, MstPermission.ALLOW_MODE, allowAppGroupList.size());
        		addPreferenceSelect(screen, allowAppGroupList);
        	}
        	if(disableAppGroupList.size() > 0) {
        		addCategoryTitle(screen, MstPermission.DISABLE_MODE, disableAppGroupList.size());
        		addPreferenceSelect(screen, disableAppGroupList);
        	}
        	if(askAppGroupList.size() > 0) {
        		addCategoryTitle(screen, MstPermission.ASK_MODE, askAppGroupList.size());
        		addPreferenceSelect(screen, askAppGroupList);
        	}
        }
    }
    
    private void addPreferenceSelect(PreferenceScreen screen, List<MstAppGroup> appGroupList) {
    	Context context = getContext();
    	MstPermEntry permEntry = null;        	
    	for (MstAppGroup app : appGroupList) {
    		permEntry = app.get(groupName);
    		PermissionsSelectPreference pref = new PermissionsSelectPreference(context, R.layout.permission_app_item2);
    		pref.setOnPreferenceChangeListener(this);
    		pref.setKey(app.getPackageName());
    		pref.setIcon(app.loadAppIcon(context));
    		pref.setTitle(app.getAppName());
    		pref.setPersistent(false);
    		pref.setEnabled(true);
    		pref.setSelected(permEntry);
    		screen.addPreference(pref);
    	}
    }

    private void addCategoryTitle(PreferenceScreen screen, int mode, int itemCount) {
    	int titleRes = R.string.permission_title_disallow_count;
    	switch (mode) {
		case MstPermission.ALLOW_MODE:
			titleRes = R.string.permission_title_allow_count;
			break;
		case MstPermission.ASK_MODE:
			titleRes = R.string.permission_title_ask_count;
			break;
		}
    	
    	PreferenceCategory preferenceTitle = new PreferenceCategory(getContext());
    	preferenceTitle.setLayoutResource(R.layout.permission_category2);
    	screen.addPreference(preferenceTitle);
    	preferenceTitle.setTitle(getString(titleRes, itemCount));
    }
}
