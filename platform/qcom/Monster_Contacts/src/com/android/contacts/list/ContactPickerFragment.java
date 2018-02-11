/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.contacts.list;

import com.android.contacts.common.mst.FragmentCallbacks;

import com.android.contacts.common.list.ProfileAndContactsLoader;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.common.list.ContactEntryListAdapter;
import com.android.contacts.common.list.ContactEntryListFragment;
import com.android.contacts.common.list.ContactListAdapter;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.DirectoryListLoader;
import com.android.contacts.common.list.ShortcutIntentBuilder;
import com.android.contacts.common.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.common.list.ContactListItemView;

import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.util.Log;

/**
 * Fragment for the contact list used for browsing contacts (as compared to
 * picking a contact with one of the PICK or SHORTCUT intents).
 */
public class ContactPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter>
implements OnShortcutIntentCreatedListener {

	private static final String TAG = "ContactPickerFragment";
	private static final String KEY_EDIT_MODE = "editMode";
	private static final String KEY_CREATE_CONTACT_ENABLED = "createContactEnabled";
	private static final String KEY_SHORTCUT_REQUESTED = "shortcutRequested";

	private OnContactPickerActionListener mListener;
	private boolean mCreateContactEnabled;
	private boolean mEditMode;
	private boolean mShortcutRequested;
	private int mAccountType = ContactsSettingsUtils.ALL_TYPE_ACCOUNT;

	public ContactPickerFragment() {
		setPhotoLoaderEnabled(false);
		setSectionHeaderDisplayEnabled(true);
		setVisibleScrollbarEnabled(true);
		setQuickContactEnabled(false);
		setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_CONTACT_SHORTCUT);
	}

	public void setOnContactPickerActionListener(OnContactPickerActionListener listener) {
		mListener = listener;
	}

	public boolean isCreateContactEnabled() {
		return mCreateContactEnabled;
	}

	public void setCreateContactEnabled(boolean flag) {
		this.mCreateContactEnabled = flag;
	}

	public boolean isEditMode() {
		return mEditMode;
	}

	public void setEditMode(boolean flag) {
		mEditMode = flag;
	}

	public void setAccountType(int type) {
		mAccountType = type;
	}

	public void setShortcutRequested(boolean flag) {
		mShortcutRequested = flag;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_EDIT_MODE, mEditMode);
		outState.putBoolean(KEY_CREATE_CONTACT_ENABLED, mCreateContactEnabled);
		outState.putBoolean(KEY_SHORTCUT_REQUESTED, mShortcutRequested);
		outState.putInt(ContactsSettingsUtils.ACCOUNT_TYPE, mAccountType);
	}

	@Override
	public void restoreSavedState(Bundle savedState) {
		super.restoreSavedState(savedState);

		if (savedState == null) {
			return;
		}

		mEditMode = savedState.getBoolean(KEY_EDIT_MODE);
		mCreateContactEnabled = savedState.getBoolean(KEY_CREATE_CONTACT_ENABLED);
		mShortcutRequested = savedState.getBoolean(KEY_SHORTCUT_REQUESTED);
		mAccountType = savedState.getInt(ContactsSettingsUtils.ACCOUNT_TYPE);
	}

	private ContactListItemView headerView=null;
	@Override
	protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
		super.onCreateView(inflater, container);
		Log.d(TAG,"onCreateView1");
//		if (mCreateContactEnabled/* && isLegacyCompatibilityMode()*/) {
//			Log.d(TAG,"onCreateView2");
//			// Since we are using the legacy adapter setShowCreateContact(true) isn't supported.
//			// So we need to add an ugly header above the list.			
//			getListView().addHeaderView(headerView, null, true);
//		}

		/** M: Bug Fix for ALPS00384304. Notify the user that there is no Contacts. */
//		setEmptyView();
		
		header= inflater.inflate(R.layout.mst_listview_search_header, null, false);
        header.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"search header onclick");
				showHeader(false);
				if(mCallbacks!=null) mCallbacks.onFragmentCallback(FragmentCallbacks.SWITCH_TO_SEARCH_MODE,null);
			}
		});
		getListView().addHeaderView(header, null, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG,"onItemClick0,pos:"+position);
		//modify by lgy for 3463273
		if (position == 1 && mCreateContactEnabled && mListener != null && TextUtils.isEmpty(mSearchView.getQuery())) {
			mListener.onCreateNewContactAction();
		} else {
			super.onItemClick(parent, view, position, id);
		}
	}

	@Override
	protected void onItemClick(int position, long id) {
		Log.d(TAG,"onItemClick1,pos:"+position);
		Uri uri;
		if (isLegacyCompatibilityMode()) {
			uri = ((LegacyContactListAdapter)getAdapter()).getPersonUri(position);
		} else {
			Log.d(TAG,"--2");
			uri = ((ContactListAdapter)getAdapter()).getContactUri(position);
		}
		Log.d(TAG,"uri:"+uri);
		if (uri == null) {
			return;
		}
		if (mEditMode) {
			editContact(uri);
		} else  if (mShortcutRequested) {
			ShortcutIntentBuilder builder = new ShortcutIntentBuilder(getActivity(), this);
			builder.createContactShortcutIntent(uri);
		} else {
			pickContact(uri);
		}
	}

	public void createNewContact() {
		if (mListener != null) {
			mListener.onCreateNewContactAction();
		}
	}

	public void editContact(Uri contactUri) {
		if (mListener != null) {
			mListener.onEditContactAction(contactUri);
		}
	}

	public void pickContact(Uri uri) {
		if (mListener != null) {
			mListener.onPickContactAction(uri);
		}
	}

	
	@Override
	public CursorLoader createCursorLoader(Context context) {
		/** M: Bug Fix for ALPS00115673 Descriptions: add wait cursor. @{ */
		Log.d(TAG, "createCursorLoader");
//		if (mLoadingContainer != null) {
//			mLoadingContainer.setVisibility(View.GONE);
//		}
		/** @} */

		return new ProfileAndContactsLoader(context);
	}
	
	@Override
	protected ContactEntryListAdapter createListAdapter() {
		if (!isLegacyCompatibilityMode()) {
			Log.d(TAG,"createListAdapter1");
			HeaderEntryContactListAdapter adapter
			= new HeaderEntryContactListAdapter(getActivity());
			adapter.setFilter(ContactListFilter.createFilterWithType(
					ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
			/** M: Bug Fix For ALPS00112614. Descriptions:
                only show phone contact if it's from sms. */
			setOnlyShowPhoneContacts(adapter, true);
			adapter.setSectionHeaderDisplayEnabled(true);
			adapter.setDisplayPhotos(false);
			adapter.setQuickContactEnabled(false);
			adapter.setShowCreateContact(mCreateContactEnabled);
//			if(headerView==null){
//				headerView = new ContactListItemView(getActivity(), null);
//				headerView.setShowLeftPhoto(false);
//				headerView.setDisplayName(getActivity().getResources().getString(R.string.pickerNewContactHeader));
//			}
//			adapter.setHeaderView(headerView);
			return adapter;
		} else {
			Log.d(TAG,"createListAdapter2");
			LegacyContactListAdapter adapter = new LegacyContactListAdapter(getActivity());
			adapter.setSectionHeaderDisplayEnabled(false);
			adapter.setDisplayPhotos(false);
			return adapter;
		}
	}

	@Override
	protected void configureAdapter() {
		super.configureAdapter();

		ContactEntryListAdapter adapter = getAdapter();

		// If "Create new contact" is shown, don't display the empty list UI
		adapter.setEmptyListEnabled(!isCreateContactEnabled());
	}

	@Override
	protected View inflateView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.contact_picker_content, null);
	}

	@Override
	public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
		if (mListener != null) {
			mListener.onShortcutIntentCreated(shortcutIntent);
		}
	}

	@Override
	public void onPickerResult(Intent data) {
		if (mListener != null) {
			mListener.onPickContactAction(data.getData());
		}
	}

	/**
	 * M: Bug Fix for CR ALPS00384304
	 * Notify the user that there is no Contacts.
	 */
	private void setEmptyView() {
		TextView emptyView = (TextView) getView().findViewById(R.id.contact_list_empty);
		if (emptyView != null) {
			emptyView.setText(R.string.listFoundAllContactsZero);
		}
	}

	/**
	 * M: Bug Fix For ALPS00112614
	 * Descriptions: only show phone contact if it's from sms.
	 */
	private void setOnlyShowPhoneContacts(HeaderEntryContactListAdapter adapter, boolean isTure) {
		if (isEditMode() || mShortcutRequested
				|| mAccountType == ContactsSettingsUtils.PHONE_TYPE_ACCOUNT) {
			Log.d(TAG, "only show phone contact");
			adapter.setOnlyShowPhoneContacts(isTure);
		}
	}
}
