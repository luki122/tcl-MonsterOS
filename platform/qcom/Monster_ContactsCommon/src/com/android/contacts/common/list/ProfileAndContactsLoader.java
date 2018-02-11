/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2011 The Android Open Source Project
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

import mst.provider.ContactsContract.RawContacts;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Profile;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.common.preference.ContactsPreferences;
import com.google.android.collect.Lists;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.ContactsCommonListUtils;
import com.mediatek.dialer.dialersearch.DialerSearchHelper;
import com.mediatek.dialer.util.DialerSearchUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A loader for use in the default contact list, which will also query for the user's profile
 * if configured to do so.
 */
public class ProfileAndContactsLoader extends CursorLoader {
	private Cursor mCursor;
	private boolean mLoadProfile;
	private String[] mProjection;
	private String mQuery;
	private Context mContext;
	private boolean mUseCallableUri = false;

	private boolean isForDialerSearch=false;	

	public boolean isForDialerSearch() {
		return isForDialerSearch;
	}
	public void setForDialerSearch(boolean isForDialerSearch) {
		this.isForDialerSearch = isForDialerSearch;
	}

	private boolean isForChoiceSearch=false;
	public void setForChoiceSearch(boolean isForChoiceSearch) {
		this.isForChoiceSearch = isForChoiceSearch;
	}
	public ProfileAndContactsLoader(Context context) {
		super(context);
		mContext = context;
	}
	private ContactListFilter filter;
	public void setContactListFilter(ContactListFilter filter){
		this.filter=filter;
	}
	public ProfileAndContactsLoader(Context context, boolean useCallable) {
		super(context);
		mContext = context;
		mUseCallableUri = useCallable;
	}

	private boolean mEnableDefaultSearch = false;
	/**
	 * Configures the query string to be used to find SmartDial matches.
	 * @param query The query string user typed.
	 */
	public void configureQuery(String query, boolean isSmartQuery) {

		Log.d(TAG, "MTK-DialerSearch, Configure new query to be " + query);

		mQuery = query;
		//		if (!isSmartQuery) {
		//			mQuery = DialerSearchUtils.stripTeleSeparators(query);
		//		}
		if (!DialerSearchUtils.isValidDialerSearchString(mQuery)) {
			mEnableDefaultSearch = true;
		}
	}

	public void setLoadProfile(boolean flag) {
		mLoadProfile = flag;
	}

	public void setProjection(String[] projection) {
		super.setProjection(projection);
		mProjection = projection;
	}

	private int mCount = 0;
	public int getCursorCount() {
		return mCount;
	}
	private boolean mLoadStarred=false;
	public void setLoadStars(boolean flag) {
		Log.d(TAG,"setLoadStars:"+flag);
		mLoadStarred = flag;
	}

	private MatrixCursor loadStarred() {
		Cursor cursor = null;
		if (/*mPhoneMode*/false) {/*
            Uri uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
                    GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                    .build();
            String selection = Contacts.STARRED + "=1 AND "
                    + Contacts.IN_VISIBLE_GROUP + "=1" 
                    + " AND " + Contacts.HAS_PHONE_NUMBER + "=1";

            if (mAutoRecordMode) {
                selection += " AND auto_record=0";
            }

            uri.buildUpon()
                    .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
            uri = uri.buildUpon()
                    .appendQueryParameter(GnContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
                    .build();

            cursor = getContext().getContentResolver().query(uri, mProjection, selection, null, Phone.SORT_KEY_PRIMARY);
		 */} 
		else {
			try {
				List<String> selectionArgs = new ArrayList<String>();
				StringBuilder selection=new StringBuilder();
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
						selection.append(Contacts.HAS_PHONE_NUMBER + "=1");
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

				if(!TextUtils.isEmpty(selection)){
					selection.append(" AND "+Contacts.STARRED + "!=0");
				}else{
					selection.append(Contacts.STARRED + "!=0");
				}
				Log.d(TAG, "[configureSelection] selection: " + selection.toString()
						+ ", filter.filterType = " + filter.filterType);

				cursor = getContext().getContentResolver().query(Contacts.CONTENT_URI, mProjection,
						selection.toString() /*AND " + Contacts.IN_VISIBLE_GROUP + "=1"*/, selectionArgs.toArray(new String[0]), 
						null);
				Log.d(TAG,"startcursor:"+(cursor==null?"null":cursor.getCount()));
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		if (cursor == null) {
			return null;
		}

		mCount = cursor.getCount();
		try {
			MatrixCursor matrix = new MatrixCursor(mProjection);
			Object[] row = new Object[mProjection.length];
			while (cursor.moveToNext()) {
				for (int i = 0; i < row.length; i++) {
					row[i] = cursor.getString(i);
				}
				matrix.addRow(row);
			}
			return matrix;
		} finally {
			cursor.close();
		}
	}

	private boolean isCustomFilterForPhoneNumbersOnly() {
		// TODO: this flag should not be stored in shared prefs.  It needs to be in the db.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		return prefs.getBoolean(ContactsPreferences.PREF_DISPLAY_ONLY_PHONES,
				ContactsPreferences.PREF_DISPLAY_ONLY_PHONES_DEFAULT);
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

	@Override
	public Cursor loadInBackground() {
		Log.d(TAG,"loadInBackground");

		Log.d(TAG,"mQuery:"+mQuery);
		if(!TextUtils.isEmpty(mQuery)&&!isForChoiceSearch){
			Log.d(TAG,"mQuery not null");
			Log.d(TAG, "MTK-DialerSearch, Load in background. mQuery: " + mQuery);

			final DialerSearchHelper dialerSearchHelper = DialerSearchHelper.getInstance(mContext);
			Cursor cursor = null;
			if (/*mEnableDefaultSearch*/false) {
				cursor = dialerSearchHelper.getRegularDialerSearchResults(mQuery, mUseCallableUri);
			} else {
				cursor = dialerSearchHelper.getSmartDialerSearchResults(mQuery,isForDialerSearch);
			}
			if (cursor != null) {
				Log.d(TAG, "MTK-DialerSearch, loadInBackground, result.getCount: "
						+ cursor.getCount());

				return cursor;
			} else {
				Log.w(TAG, "MTK-DialerSearch, ----cursor is null----");
				return null;
			}
		}

		Log.d(TAG,"loadInBackground1,mLoadStarred:"+mLoadStarred);
		// First load the profile, if enabled.
		List<Cursor> cursors = Lists.newArrayList();
		//        if (mLoadProfile) {
		//            cursors.add(loadProfile());
		//        }

		/** M: New Feature SDN @{ */
		mSdnContactCount = 0;
		mSdnContactCount = ContactsCommonListUtils.addCursorAndSetSelection(getContext(),
				this, cursors, mSdnContactCount);
		/** @} */

		// ContactsCursor.loadInBackground() can return null; MergeCursor
		// correctly handles null cursors.
		Cursor cursor = null;
		try {
			cursor = super.loadInBackground();
		} catch (NullPointerException | SecurityException e) {
			// Ignore NPEs and SecurityExceptions thrown by providers
		}
		final Cursor contactsCursor = cursor;
		cursors.add(contactsCursor);

		if (mLoadStarred) {
			cursors.add(loadStarred());
		}

		return new MergeCursor(cursors.toArray(new Cursor[cursors.size()])) {
			@Override
			public Bundle getExtras() {
				// Need to get the extras from the contacts cursor.
				return contactsCursor == null ? new Bundle() : contactsCursor.getExtras();
			}
		};
	}

	/**
	 * Loads the profile into a MatrixCursor. On failure returns null, which
	 * matches the behavior of CursorLoader.loadInBackground().
	 *
	 * @return MatrixCursor containing profile or null on query failure.
	 */
	private MatrixCursor loadProfile() {
		Cursor cursor = getContext().getContentResolver().query(Profile.CONTENT_URI, mProjection,
				null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			MatrixCursor matrix = new MatrixCursor(mProjection);
			Object[] row = new Object[mProjection.length];
			while (cursor.moveToNext()) {
				for (int i = 0; i < row.length; i++) {
					row[i] = cursor.getString(i);
				}
				matrix.addRow(row);
			}
			return matrix;
		} finally {
			cursor.close();
		}
	}

	/** M: modify. @{ */
	private static final String TAG = "ProfileAndContactsLoader";
	private int mSdnContactCount = 0;

	public int getSdnContactCount() {
		return this.mSdnContactCount;
	}

	//    @Override
	//    protected void onStartLoading() {
	//        forceLoad();
	//    }

	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			Log.d(TAG, "MTK-DialerSearch, deliverResult releaseResources " + this);
			/** The Loader has been reset; ignore the result and invalidate the data. */
			releaseResources(cursor);
			return;
		}

		/** Hold a reference to the old data so it doesn't get garbage collected. */
		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			/** If the Loader is in a started state, deliver the results to the client. */
			super.deliverResult(cursor);
		}

		/** Invalidate the old data as we don't need it any more. */
		if (oldCursor != null && oldCursor != cursor) {
			releaseResources(oldCursor);
		}
	}

	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			/** Deliver any previously loaded data immediately. */
			deliverResult(mCursor);
		}
		if (mCursor == null) {
			/** Force loads every time as our results change with queries. */
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		/** The Loader is in a stopped state, so we should attempt to cancel the current load. */
		cancelLoad();
	}

	@Override
	protected void onReset() {
		Log.d(TAG, "MTK-DialerSearch, onReset() "  + this);
		/** Ensure the loader has been stopped. */
		onStopLoading();

		/** Release all previously saved query results. */
		if (mCursor != null) {
			Log.d(TAG, "MTK-DialerSearch, onReset() releaseResources "  + this);
			releaseResources(mCursor);
			mCursor = null;
		}
	}

	@Override
	public void onCanceled(Cursor cursor) {
		super.onCanceled(cursor);

		Log.d(TAG, "MTK-DialerSearch, onCanceled() " + this);

		/** The load has been canceled, so we should release the resources associated with 'data'.*/
		releaseResources(cursor);
	}

	private void releaseResources(Cursor cursor) {
		if (cursor != null) {
			Log.w(TAG, "MTK-DialerSearch, releaseResources close cursor " + this);
			cursor.close();
			cursor = null;
		}
	}

	/** @} */
}
