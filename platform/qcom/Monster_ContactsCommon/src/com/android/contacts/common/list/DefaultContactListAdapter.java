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
package com.android.contacts.common.list;

import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.widget.SliderView;
import com.android.contacts.common.list.ContactListAdapter.ContactQuery;
import com.android.contacts.common.mst.DialerSearchHelperForMst;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.mst.DialerSearchHelperForMst.DialerSearchResultColumnsForMst;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.R;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.ContactsCommonListUtils;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.preference.PreferenceManager;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Directory;
import mst.provider.ContactsContract.RawContacts;
import mst.provider.ContactsContract.SearchSnippets;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.MotionEvent;
//import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.common.preference.ContactsPreferences;
import com.mediatek.contacts.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * A cursor adapter for the {@link ContactsContract.Contacts#CONTENT_TYPE} content type.
 */
public class DefaultContactListAdapter extends ContactListAdapter implements SliderView.OnSliderButtonLickListener{
	private static final String TAG = "DefaultContactListAdapter";
	private String mCountryIso;
	public static final char SNIPPET_START_MATCH = '[';
	public static final char SNIPPET_END_MATCH = ']';

	public DefaultContactListAdapter(Context context) {
		super(context);
		mCountryIso = GeoUtil.getCurrentCountryIso(context);
	}


	@Override
	public void configureLoader(CursorLoader loader, long directoryId) {
		if (loader instanceof ProfileAndContactsLoader) {
			/** M: New Feature SDN. */
			mSDNLoader = (ProfileAndContactsLoader) loader;
			((ProfileAndContactsLoader) loader).setLoadProfile(shouldIncludeProfile());
			((ProfileAndContactsLoader) loader).setForChoiceSearch(isForContactsChoice());
			((ProfileAndContactsLoader) loader).configureQuery(getQueryString(), false);
			((ProfileAndContactsLoader) loader).setForDialerSearch(isForDialerSearch());
		}

		ContactListFilter filter = getFilter();
		Log.d(TAG, "[configureLoader] filter: " + filter + ",loader:" + loader + ",isSearchMode:"
				+ isSearchMode());
		if (isSearchMode()) {
			String query = getQueryString();
			if (query == null) {
				query = "";
			}
			query = query.trim();

			if(isForContactsChoice()){
				if (TextUtils.isEmpty(query)) {
					// Regardless of the directory, we don't want anything returned,
					// so let's just send a "nothing" query to the local directory.
					loader.setUri(Contacts.CONTENT_URI);
					loader.setProjection(ContactQuery.CONTACT_PROJECTION_PRIMARY_FOR_CHOICE);
					loader.setSelection("0");
				} else {
					Builder builder = Contacts.CONTENT_FILTER_URI.buildUpon();
					builder.appendPath(query);      // Builder will encode the query
					builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
							String.valueOf(directoryId));
					if (directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE) {
						builder.appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
								String.valueOf(getDirectoryResultLimit(getDirectoryById(directoryId))));
					}
					builder.appendQueryParameter(SearchSnippets.DEFERRED_SNIPPETING_KEY,"1");
					loader.setUri(builder.build());
					Log.d(TAG,"uri:"+builder.build());
					loader.setProjection(ContactQuery.CONTACT_PROJECTION_PRIMARY_FOR_CHOICE);
				}
				((ProfileAndContactsLoader) loader).configureQuery("", false);
				String sortOrder=Contacts.SORT_KEY_PRIMARY;
				if (getSortOrder() == ContactsPreferences.SORT_ORDER_PRIMARY) {
					sortOrder = Contacts.SORT_KEY_PRIMARY;
				} else {
					sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
				}
				Log.d(TAG,"sortOrder:"+sortOrder);
				loader.setSortOrder(sortOrder);
				return;
			}
		} else {
			configureUri(loader, directoryId, filter);
			loader.setProjection(getProjection(false));
			configureSelection(loader, directoryId, filter);
		}

		/**
		 * M: Bug Fix for ALPS00112614. Descriptions: only show phone contact if
		 * it's from sms @{
		 */
		if (mOnlyShowPhoneContacts) {
			ContactsCommonListUtils.configureOnlyShowPhoneContactsSelection(loader, directoryId,
					filter);
		}
		/** @} */

		String sortOrder=Contacts.SORT_KEY_PRIMARY;
		if (getSortOrder() == ContactsPreferences.SORT_ORDER_PRIMARY) {
			sortOrder = Contacts.SORT_KEY_PRIMARY;
		} else {
			sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
		}
		Log.d(TAG,"sortOrder:"+sortOrder);
//		String sortOrder="quanpinyin";
		loader.setSortOrder(sortOrder);
	}

	protected void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
		Uri uri = Contacts.CONTENT_URI;
		if (filter != null && filter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
			String lookupKey = getSelectedContactLookupKey();
			if (lookupKey != null) {
				uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
			} else {
				uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, getSelectedContactId());
			}
		}

		if (directoryId == Directory.DEFAULT && isSectionHeaderDisplayEnabled()) {
			uri = ContactListAdapter.buildSectionIndexerUri(uri);
		}

		// The "All accounts" filter is the same as the entire contents of Directory.DEFAULT
		if (filter != null
				&& filter.filterType != ContactListFilter.FILTER_TYPE_CUSTOM
				&& filter.filterType != ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
			final Uri.Builder builder = uri.buildUpon();
			builder.appendQueryParameter(
					ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT));
			/**
			 * M: Change Feature: <br>
			 * As Local Phone account contains null account and Phone Account,
			 * the Account Query Parameter could not meet this requirement. So,
			 * We should keep to query contacts with selection. @{
			 */
			/*
			 * if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
			 * filter.addAccountQueryParameterToUrl(builder); }
			 */
			/** @} */
			uri = builder.build();
		}

		loader.setUri(uri);
	}

	/// M: New Feature SDN.
	protected void configureSelection(
			CursorLoader loader, long directoryId, ContactListFilter filter) {
		if (filter == null) {
			return;
		}

		if (directoryId != Directory.DEFAULT) {
			return;
		}

		StringBuilder selection = new StringBuilder();
		List<String> selectionArgs = new ArrayList<String>();

		switch (filter.filterType) {
		case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
			// We have already added directory=0 to the URI, which takes care of this
			// filter
			/** M: New Feature SDN. */
			selection.append(RawContacts.IS_SDN_CONTACT + " < 1");
			break;
		}
		case ContactListFilter.FILTER_TYPE_SINGLE_CONTACT: {
			// We have already added the lookup key to the URI, which takes care of this
			// filter
			break;
		}
		case ContactListFilter.FILTER_TYPE_STARRED: {
			selection.append(Contacts.STARRED + "!=0");
			break;
		}
		case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY: {
			selection.append(Contacts.HAS_PHONE_NUMBER + "=1");
			/** M: New Feature SDN. */
			selection.append(" AND " + RawContacts.IS_SDN_CONTACT + " < 1");
			break;
		}
		case ContactListFilter.FILTER_TYPE_CUSTOM: {
			selection.append(Contacts.IN_VISIBLE_GROUP + "=1");
			if (isCustomFilterForPhoneNumbersOnly()) {
				selection.append(" AND " + Contacts.HAS_PHONE_NUMBER + "=1");
			}
			/** M: New Feature SDN. */
			selection.append(" AND " + RawContacts.IS_SDN_CONTACT + " < 1");
			break;
		}
		case ContactListFilter.FILTER_TYPE_ACCOUNT: {
			// We use query parameters for account filter, so no selection to add here.
			/** M: Change Feature: As Local Phone account contains null account and Phone
			 * Account, the Account Query Parameter could not meet this requirement. So,
			 * We should keep to query contacts with selection. @{ */
			buildSelectionForFilterAccount(filter, selection, selectionArgs);
			break;
		}
		}
		Log.d(TAG, "[configureSelection] selection: " + selection.toString()
				+ ", filter.filterType = " + filter.filterType);
		loader.setSelection(selection.toString());
		loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
	}

	/**
	 * M: Fix ALPS01398152, Support RTL display for Arabic/Hebrew/Urdu
	 * @param origin
	 * @return
	 */
	private String numberLeftToRight(String origin) {
		return TextUtils.isEmpty(origin) ? origin : '\u202D' + origin + '\u202C';
	}

	@Override
	protected void bindView(View itemView, int partition, Cursor cursor, final int position) {
//		Log.d(TAG,"bindView11,itemView:"+itemView+" position:"+position+" cursor:"+cursor);	
		
		final ViewHolderForContacts viewHolder = (ViewHolderForContacts) itemView.getTag();
		if(viewHolder==null) return;
//		CheckBox checkBox=null;
		ContactListItemView view=viewHolder.view;
		super.bindView(view, partition, cursor, position);
		TextView header=viewHolder.header;
		TextView name=viewHolder.name;
		//		frontView=viewHolder.frontView;
		final SliderView sliderView=viewHolder.sliderLayout;
		sliderView.setTag(position);
		//		frontView.setTag(position);
		//		frontView.setOnLongClickListener(mLongClickListener);
		//		frontView.setOnClickListener(mClickListener);

//		view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString() : null);

		if (isSelectionVisible()) {
			view.setActivated(isSelectedContact(partition, cursor));
		}

		bindSectionHeaderAndDividerV2(header, position, cursor);
		/// M: [RCS-e].
		//		view.bindDataForCustomView(cursor.getLong(cursor.getColumnIndex(Contacts._ID)));

		/// M: [Common Presence]
		//		view.bindDataForCommonPresenceView(cursor.getLong(cursor.getColumnIndex(Contacts._ID)));

		//		if (isQuickContactEnabled()) {
		//			bindQuickContact(view, partition, cursor, ContactQuery.CONTACT_PHOTO_ID,
		//					ContactQuery.CONTACT_PHOTO_URI, ContactQuery.CONTACT_ID,
		//					ContactQuery.CONTACT_LOOKUP_KEY, ContactQuery.CONTACT_DISPLAY_NAME);
		//		} else {
		//			if (getDisplayPhotos()) {
		//				bindPhoto(view, partition, cursor);
		//			}
		//		}

		//		bindNameAndViewId(view, cursor);

		//		bindPresenceAndStatusMessage(view, cursor);

		if(isSelectMode()||isSearchMode()){
			sliderView.setLockDrag(true);
		}else{
			sliderView.setLockDrag(false);
		}
		
		if(sliderView.isOpened()){
			sliderView.close(false);
		}

//		sliderView.setSwipeListener(new SwipeListener(){
//
//			public void onClosed(SliderLayout view){
//				currentSliderView=null;
//			}
//			public void onOpened(SliderLayout view){
//				currentSliderView=sliderView;
//			}
//			public void onSlide(SliderLayout view, float slideOffset){
//				//				Log.d(TAG,"onSlide:"+view+" slideOffset:"+slideOffset);
//			}
//		});

		sliderView.setOnSliderButtonClickListener(this);
		bindNameAndViewId(view, cursor);

//		if (isSearchMode()) {
//			final String number = cursor.getString(DialerSearchResultColumnsForMst.PHONE_NUMBER_INDEX);
//			String formatNumber=null;
//			if(!TextUtils.isEmpty(number)){
//				//				formatNumber = numberLeftToRight(PhoneNumberUtils.formatNumber(number, mCountryIso));
//				formatNumber=PhoneNumberUtils.formatNumber(number, mCountryIso);
//			}
//
//			if (formatNumber == null) {
//				formatNumber = number;
//			}
//			Log.d(TAG,"formatNumber1:"+formatNumber+" isSelectMode():"+isSelectMode()+" isSearchMode:"+isSearchMode());
//
//			DialerSearchHelperForMst.highlightName(name, cursor,getContext().getResources().getColor(R.color.hightlight_color));
//			if (!TextUtils.isEmpty(formatNumber)&&!TextUtils.isEmpty(getQueryString())&&!TextUtils.equals(SEARCH_BEGIN_STRING, getQueryString())){
//				//				bindSearchSnippet(view, cursor);
//				DialerSearchHelperForMst.highlightNumber(viewHolder.snippetView, formatNumber, cursor,getContext().getResources().getColor(R.color.hightlight_color));
////				viewHolder.snippetView.setVisibility(View.VISIBLE);
//			} else {
////				viewHolder.snippetView.setText(null);
////				viewHolder.snippetView.setVisibility(View.GONE);
//			}
//
//			//						bindSearchSnippet(view, cursor);
//		} else {
//			bindNameAndViewId(view, cursor);
//			view.setSnippet(null);
//		}
	}
	
	public SliderView getCurrentSliderView() {
		return currentSliderView;
	}
	public void setCurrentSliderView(SliderView currentSliderView1) {
		currentSliderView = currentSliderView1;
	}


	private boolean isCustomFilterForPhoneNumbersOnly() {
		// TODO: this flag should not be stored in shared prefs.  It needs to be in the db.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		return prefs.getBoolean(ContactsPreferences.PREF_DISPLAY_ONLY_PHONES,
				ContactsPreferences.PREF_DISPLAY_ONLY_PHONES_DEFAULT);
	}

	/** M: Bug Fix for ALPS00112614 Descriptions: only show phone contact if it's from sms @{ */
	private boolean mOnlyShowPhoneContacts = false;
	public ProfileAndContactsLoader mSDNLoader = null;

	public void setOnlyShowPhoneContacts(boolean showPhoneContacts) {
		mOnlyShowPhoneContacts = showPhoneContacts;
	}
	/** @} */
	/**
	 * M: New Feature for SDN.
	 */
	@Override
	public void updateIndexer(Cursor cursor) {
		super.updateIndexer(cursor);
		ContactsSectionIndexer sectionIndexer = (ContactsSectionIndexer) this.getIndexer();
		if (mSDNLoader != null) {
			if (mSDNLoader.getSdnContactCount() > 0) {
				sectionIndexer.setSdnHeader("SDN", mSDNLoader.getSdnContactCount());
			}
		}
	}

	/**
	 * M: Change Feature: As Local Phone account contains null account and Phone
	 * Account, the Account Query Parameter could not meet this requirement. So,
	 * We should keep to query contacts with selection. */
	private void buildSelectionForFilterAccount(ContactListFilter filter, StringBuilder selection,
			List<String> selectionArgs) {
		if (AccountTypeUtils.ACCOUNT_TYPE_LOCAL_PHONE.equals(filter.accountType)) {
			selection.append("EXISTS ("
					+ "SELECT DISTINCT " + RawContacts.CONTACT_ID
					+ " FROM view_raw_contacts"
					+ " WHERE ( ");
			selection.append(RawContacts.IS_SDN_CONTACT + " < 1 AND ");
			selection.append(RawContacts.CONTACT_ID + " = " + "view_contacts."
					+ Contacts._ID
					+ " AND (" + RawContacts.ACCOUNT_TYPE + " IS NULL "
					+ " AND " + RawContacts.ACCOUNT_NAME + " IS NULL "
					+ " AND " +  RawContacts.DATA_SET + " IS NULL "
					+ " OR " + RawContacts.ACCOUNT_TYPE + "=? "
					+ " AND " + RawContacts.ACCOUNT_NAME + "=? ");
		} else {
			selection.append("EXISTS ("
					+ "SELECT DISTINCT " + RawContacts.CONTACT_ID
					+ " FROM view_raw_contacts"
					+ " WHERE ( ");
			selection.append(RawContacts.IS_SDN_CONTACT + " < 1 AND ");
			selection.append(RawContacts.CONTACT_ID + " = " + "view_contacts."
					+ Contacts._ID
					+ " AND (" + RawContacts.ACCOUNT_TYPE + "=?"
					+ " AND " + RawContacts.ACCOUNT_NAME + "=?");
		}
		ContactsCommonListUtils.buildSelectionForFilterAccount(filter, selection, selectionArgs);
	}

	///M: Add for SDN feature, to get SDN number
	public int getSdnNumber() {
		if (mSDNLoader != null) {
			return mSDNLoader.getSdnContactCount();
		}
		return 0;
	}

	@Override
	public void onSliderButtonClick(int id, View view, ViewGroup parent) {
		Log.d(TAG,"onSliderButtonClick,id:"+id+" view:"+view+" parent:"+parent);
		switch (id) {
		case 1:
			// TODO Auto-generated method stub
			if(((SliderView)parent).isOpened()){
				((SliderView)parent).close(false);
			}

			final Uri uri = getContactUri(Integer.parseInt(parent.getTag().toString()));
			//add by lgy for 3408040
			if(uri == null) {
			    return;
			}
			final String contactId = uri.getLastPathSegment();
			Log.d(TAG,"onclick delete_view,contactId:"+contactId);
			if (!TextUtils.isEmpty(contactId)) {
				TreeSet<Long> mSelectedContactIds = new TreeSet<Long>();
				mSelectedContactIds.add(Long.valueOf(contactId));
				if(mCallbacks!=null) mCallbacks.onFragmentCallback(FragmentCallbacks.DELETE_CONTACTS, mSelectedContactIds);
			}
			break;
		}
	}

}
