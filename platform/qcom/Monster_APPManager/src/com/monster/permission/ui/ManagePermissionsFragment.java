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

import java.util.List;
import java.util.Map;

import com.monster.appmanager.R;
import com.monster.appmanager.utils.PermissionGroups;
import com.monster.appmanager.utils.Utils;
import com.monster.permission.ui.MstPermission.MstPermGroup;

import android.annotation.Nullable;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceScreen;

public final class ManagePermissionsFragment extends PermissionsFrameFragment
        implements PermissionGroups.PermissionsGroupsChangeCallback, OnPreferenceClickListener{
    private static final String LOG_TAG = "ManagePermissionsFragment";

    private static final String OS_PKG = "android";

    private static final String EXTRA_PREFS_KEY = "extra_prefs_key";

    private ArraySet<String> mLauncherPkgs;

//    private PermissionGroups mPermissions;

    private PreferenceScreen mExtraScreen;
    private Preference extraScreenPreference ;

    public static ManagePermissionsFragment newInstance() {
        return new ManagePermissionsFragment();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
//        mLauncherPkgs = Utils.getLauncherPackages(getContext());
//        mPermissions = new PermissionGroups(getActivity(), getLoaderManager(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
//        mPermissions.refresh();
//        updatePermissionsUi();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
//        Intent intent = new Intent(ManagePermissionsInfoActivity.MANAGE_PERMISSION_APPS)
//                .putExtra(Intent.EXTRA_PERMISSION_NAME, key);
        Intent intent = new Intent(getContext(), ManagePermissionsInfoActivity.class)
        		.setAction(ManagePermissionsInfoActivity.MANAGE_PERMISSION_APPS)
                .putExtra(Intent.EXTRA_PERMISSION_NAME, key);
        try {
            getActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(LOG_TAG, "No app to handle " + intent);
        }

        return true;
    }

    @Override
    public void onPermissionGroupsChanged() {
        updatePermissionsUi();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindPermissionUi(getActivity(), getView());
        hideEmptyText();
    }

    private static void bindPermissionUi(@Nullable Context context, @Nullable View rootView) {
        if (context == null || rootView == null) {
            return;
        }
    }

    private void updatePermissionsUi() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        Map<String, MstPermGroup> permGroupMap =  mMstPermission.getPermGroupMap();
        if(permGroupMap.size() > 0) {
        	List<Map.Entry<String, MstPermGroup>> mappingList = mMstPermission.getSortedPermGroupList();
        	for(Map.Entry<String, MstPermGroup> entry : mappingList){
//        	for (Map.Entry<String, MstPermGroup> entry : permGroupMap.entrySet()) {
        		MstPermGroup permGroup = entry.getValue();
        		
        		Preference preference = findPreference(permGroup.getPermGroupName());
        		if (preference == null && mExtraScreen != null) {
        			preference = mExtraScreen.findPreference(permGroup.getPermGroupName());
        		}
        		if (preference == null) {
        			preference = new Preference(context);
        			preference.setLayoutResource(R.layout.permission_item);
        			preference.setOnPreferenceClickListener(this);
        			preference.setKey(permGroup.getPermGroupName());
        			preference.setIcon(Utils.applyTint(context, permGroup.getDrawable(),
        					android.R.attr.colorControlNormal));
        			preference.setTitle(permGroup.getLabel());
        			preference.setPersistent(false);
        			
        			if (!permGroup.isSeniorPermission()) {
        				screen.addPreference(preference);
        			} else {
        				if (mExtraScreen == null) {
        					mExtraScreen = getPreferenceManager().createPreferenceScreen(context);
        				}
        				mExtraScreen.addPreference(preference);
        			}
        		}
        		preference.setSummary(getString(R.string.app_permissions_group_summary, 
        				permGroup.getGrantedCount(), permGroup.getAppGroupList().size()));
        	}
        	
        	if (mExtraScreen != null && mExtraScreen.getPreferenceCount() > 0
        			&& screen.findPreference(EXTRA_PREFS_KEY) == null) {
        		extraScreenPreference = new Preference(context);
        		extraScreenPreference.setLayoutResource(R.layout.permission_item);
        		extraScreenPreference.setKey(EXTRA_PREFS_KEY);
        		extraScreenPreference.setIcon(Utils.applyTint(context,
        				com.android.internal.R.drawable.ic_more_items,
        				android.R.attr.colorControlNormal));
        		extraScreenPreference.setTitle(R.string.additional_permissions);
        		extraScreenPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        			@Override
        			public boolean onPreferenceClick(Preference preference) {
        				AdditionalPermissionsFragment frag = new AdditionalPermissionsFragment();
        				frag.setTargetFragment(ManagePermissionsFragment.this, 0);
        				FragmentTransaction ft = getFragmentManager().beginTransaction();
        				ft.replace(com.mst.R.id.content, frag);
        				ft.addToBackStack(null);
        				ft.commit();
        				return true;
        			}
        		});
        		int count = mExtraScreen.getPreferenceCount();
        		extraScreenPreference.setSummary(getResources().getQuantityString(
        				R.plurals.additional_permissions_more, count, count));
        		screen.addPreference(extraScreenPreference);
        	}
        } else {
        	setAllowNoneItem(true);
        	onSetEmptyText();
        }

        if (screen.getPreferenceCount() != 0) {
        }
    }

    public static class AdditionalPermissionsFragment extends PermissionsFrameFragment {
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            getActivity().setTitle(R.string.additional_permissions);
            setHasOptionsMenu(true);
            onCreatePreferences();
        }

        @Override
        public void onDestroy() {
            getActivity().setTitle(R.string.app_permissions);
            super.onDestroy();
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

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            bindPermissionUi(getActivity(), getView());
            hideEmptyText();
        }

        public void onCreatePreferences() {
            setPreferenceScreen(((ManagePermissionsFragment) getTargetFragment()).mExtraScreen);
        }
    }

	@Override
	protected void onRefreshPermission() {
		super.onRefreshPermission();
        setLoading(true, false);
        mMstPermission.refreshAsync(null, null);
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
}
