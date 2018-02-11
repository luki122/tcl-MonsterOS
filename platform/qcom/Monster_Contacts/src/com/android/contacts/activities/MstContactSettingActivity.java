package com.android.contacts.activities;

import java.util.List;

import com.android.contacts.R;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceGroup;
import mst.widget.toolbar.Toolbar;

import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.util.AccountFilterUtil;

public class MstContactSettingActivity extends PreferenceActivity implements OnPreferenceClickListener {
	private Preference mNormalPrefs1;
	private Preference mNormalPrefs2;
	
	private static final int SUBACTIVITY_ACCOUNT_FILTER = 2;

    public static final String KEY_EXTRA_CURRENT_FILTER = "currentFilter";

    private ContactListFilter mCurrentFilter;
	private Toolbar toolbar;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		toolbar = getToolbar();
        toolbar.setTitle(getResources().getString(R.string.mst_contact_setting));
		toolbar.setElevation(0f);
		addPreferencesFromResource(R.xml.preference_contact_setting);
		findPreferences();
		bindListenerToPreference();
		mCurrentFilter = getIntent().getParcelableExtra(KEY_EXTRA_CURRENT_FILTER);
		
	}

	/**
	 * Find all the Preference by key,
	 */
	private void findPreferences() {
		// normal preference
//		mNormalPrefs1 = findPreference("preference_accounts");

		mNormalPrefs2 = findPreference("preference_contactio");
	}

	private void bindListenerToPreference() {
		/*
		 * bind ClickListener
		 */
//		mNormalPrefs1.setOnPreferenceClickListener(this);
		mNormalPrefs2.setOnPreferenceClickListener(this);

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
//		if (preference == mNormalPrefs1) {
//			AccountFilterUtil.startAccountFilterActivityForResult(
//					this, SUBACTIVITY_ACCOUNT_FILTER,
//					mCurrentFilter);
//			return true;
//		} else 
		if (preference == mNormalPrefs2) {
			Intent intent = new Intent(this, MstContactImportExportActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

}
