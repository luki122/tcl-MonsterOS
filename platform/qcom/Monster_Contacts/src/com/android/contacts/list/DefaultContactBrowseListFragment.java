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

import com.android.contacts.activities.PeopleActivity;
import java.text.CollationKey;
//import net.sourceforge.pinyin4j.PinyinHelper;  
//import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;  
//import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;  
//import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;  
//import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination; 
import com.mst.t9search.ContactsHelper;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import mst.widget.MstIndexBar;
import mst.widget.MstIndexBar.Letter;
import com.android.contacts.common.mst.ContactForIndex;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.common.list.ContactEntryListFragment.ViewContactListener;
import com.android.contacts.common.list.ContactListAdapter.ContactQuery;
import com.android.contacts.common.list.IndexerListAdapter.Placement;

import com.android.contacts.common.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.common.mst.StarContactsAdapter;
import com.android.contacts.common.mst.FragmentCallbacks;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import mst.provider.ContactsContract;
import android.provider.Settings;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.list.ContactListAdapter;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.list.ProfileAndContactsLoader;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.common.util.AccountFilterUtil;
//import com.android.contacts.common.mst.HanziToPinyin;
//import com.android.contacts.common.mst.HanziToPinyin.Token;
import com.mst.t9search.MstSearchContactsAdapter;
//import com.android.contacts.common.mst.AbsListIndexer;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.activities.GroupBrowseActivity;
import com.mediatek.contacts.activities.GroupBrowseActivity.AccountCategoryInfo;
import com.mediatek.contacts.util.ContactsListUtils;
//import com.mediatek.contacts.vcs.VcsController;
import com.mediatek.contacts.widget.WaitCursorView;

/**
 * Fragment containing a contact list used for browsing (as compared to
 * picking a contact with one of the PICK intents).
 */
public class DefaultContactBrowseListFragment extends ContactBrowseListFragment implements OnClickListener,
AbsListView.OnScrollListener{
	private static final String TAG = DefaultContactBrowseListFragment.class.getSimpleName();

	private static final int REQUEST_CODE_ACCOUNT_FILTER = 1;

	private View mSearchHeaderView;
	private View mAccountFilterHeader;
	private View mSearchLayout;
	private FrameLayout mProfileHeaderContainer;
	private View mProfileHeader;
	private View starViewTitle;
	private Button mProfileMessage;
	private TextView mProfileTitle;
	private View mSearchProgress;
	private TextView mSearchProgressText;



	private class FilterHeaderClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			AccountFilterUtil.startAccountFilterActivityForResult(
					DefaultContactBrowseListFragment.this,
					REQUEST_CODE_ACCOUNT_FILTER,
					getFilter());
		}
	}
	private OnClickListener mFilterHeaderClickListener = new FilterHeaderClickListener();

	public DefaultContactBrowseListFragment() {
		setPhotoLoaderEnabled(true);
		// Don't use a QuickContactBadge. Just use a regular ImageView. Using a QuickContactBadge
		// inside the ListView prevents us from using MODE_FULLY_EXPANDED and messes up ripples.
		setQuickContactEnabled(false);
		setSectionHeaderDisplayEnabled(true);
		setVisibleScrollbarEnabled(false);
//		mLetterComparator=new AlphabetComparator();
	}

//	private AlphabetComparator mLetterComparator;
	@Override
	public CursorLoader createCursorLoader(Context context) {
		/** M: Bug Fix for ALPS00115673 Descriptions: add wait cursor. @{ */
		Log.d(TAG, "createCursorLoader");
		if (mLoadingContainer != null) {
			mLoadingContainer.setVisibility(View.GONE);
		}
		/** @} */

		return new ProfileAndContactsLoader(context);
	}

	// The following lines are provided and maintained by Mediatek Inc.
	protected Uri getGroupUriFromIdAndAccountInfo(long groupId, String accountName,
			String accountType) {
		Log.d(TAG,"getGroupUriFromIdAndAccountInfo,groupId:"+groupId+" accountName:"+accountName+" accountType:"+accountType);
		Uri retUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
		if (accountName != null && accountType != null) {
			retUri = retUri.buildUpon().appendPath(String.valueOf("-1")).appendPath(accountName)
					.appendPath(accountType).build();
		}
		return retUri;
	}

	@Override
	public void onViewGroupAction(Uri groupUri) {
		int simId = -1;
		int subId = SubInfoUtils.getInvalidSubId();
		///M: For move to other group feature.
		int count = 0;
		String accountType = "";
		String accountName = "";
		Log.i(TAG, "groupUri" + groupUri.toString());
		List uriList = groupUri.getPathSegments();
		Uri newGroupUri = ContactsContract.AUTHORITY_URI.buildUpon()
				.appendPath(uriList.get(0).toString())
				.appendPath(uriList.get(1).toString()).build();
		if (uriList.size() > 2) {
			subId = Integer.parseInt(uriList.get(2).toString());
			Log.i(TAG, "people subId-----------" + subId);
		}
		if (uriList.size() > 3) {
			accountType = uriList.get(3).toString();
		}
		if (uriList.size() > 4) {
			accountName = uriList.get(4).toString();
		}
		Log.i(TAG, "newUri-----------" + newGroupUri);
		Log.i(TAG, "accountType-----------" + accountType);
		Log.i(TAG, "accountName-----------" + accountName);
		Intent intent = new Intent(getActivity(), GroupDetailActivity.class);
		intent.setData(newGroupUri);
		intent.putExtra("AccountCategory", new AccountCategoryInfo(accountType, subId,
				accountName, count));
		getActivity().startActivity(intent);
	}

	protected void mClickListenerOnClick(int position){
		Log.d(TAG, "[onClick]position = " + position);
		if(isSearchMode()){
			if(position>=getAdapter().getCount()-getAdapter().getGroupCount()){//进入群组详情
				long groupId=getAdapter().getGroupId(position);
				final Uri uri=getGroupUriFromIdAndAccountInfo(groupId,"Phone","Local Phone Account");
				Log.d(TAG,"onClick,uri:"+uri);

				onViewGroupAction(uri);				
				return;
			}
		}
		final Uri uri = getAdapter().getContactUri(position);
		Log.d(TAG,"onItemClick uri:"+uri);
		if (uri == null) {
			return;
		}
		if (ExtensionManager.getInstance().getRcsExtension()
				.addRcsProfileEntryListener(uri, false)) {
			return;
		}
		viewContact(uri);
	}


	@Override
	protected void onItemClick(int position, long id) {
		Log.d(TAG, "[onItemClick][launch]start");
		if(isSearchMode()){
			if(position>=getAdapter().getCount()-getAdapter().getGroupCount()){//进入群组详情
				long groupId=getAdapter().getGroupId(position);
				final Uri uri=getGroupUriFromIdAndAccountInfo(groupId,"Phone","Local Phone Account");
				Log.d(TAG,"onItemClick group,uri:"+uri);
				onViewGroupAction(uri);
				return;
			}
		}
		final Uri uri = getAdapter().getContactUri(position);
		Log.d(TAG,"onItemClick uri:"+uri);
		if (uri == null) {
			return;
		}
		if (ExtensionManager.getInstance().getRcsExtension()
				.addRcsProfileEntryListener(uri, false)) {
			return;
		}
		viewContact(uri);
		Log.d(TAG, "[onItemClick][launch]end");
	}



	@Override
	protected ContactListAdapter createListAdapter() {
		DefaultContactListAdapter adapter = new DefaultContactListAdapter(getContext());
		adapter.setSectionHeaderDisplayEnabled(isSectionHeaderDisplayEnabled());
		adapter.setDisplayPhotos(false);
		adapter.setPhotoPosition(
				ContactListItemView.getDefaultPhotoPosition(/* opposite = */ false));
		return adapter;
	}

	@Override
	protected View inflateView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.contact_list_content, null);
	}


	public void configureHeaderDisplay(boolean hide){
		if(hide){
			starView.setVisibility(View.GONE);
			starView.setPadding(0, -2000, 0, 0);
		}else{
			starView.setVisibility(View.VISIBLE);
			starView.setPadding(0, 0, 0, 0);
		}
	}

	private FrameLayout mMainContent;




	private View starView;

	//	boolean popwindowIsShown=false;
	//	PopupWindow popupWindow;
	@Override
	protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
		super.onCreateView(inflater, container);
		Log.d(TAG,"onCreateView");
		/// Add for ALPS02377518, should prevent accessing SubInfo if has no basic permissions. @{
		//		if (!RequestPermissionsActivity.hasBasicPermissions(getContext())) {
		//			Log.i(TAG, "[onCreateView] has no basic permissions");
		//			return;
		//		}
		//		/// @}
		//		mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
		mMainContent = (FrameLayout) getView().findViewById(R.id.main_frame);
		//		mAccountFilterHeader.setOnClickListener(mFilterHeaderClickListener);

		// Create an entry for public account and show it from now
		//		ExtensionManager.getInstance().getRcsExtension()
		//		.createPublicAccountEntryView(getListView());


		/** M: Bug Fix for ALPS00115673 Descriptions: add wait cursor */
		mWaitCursorView = ContactsListUtils.initLoadingView(this.getContext(),
				getView(), mLoadingContainer, mLoadingContact, mProgress);

		// Putting the header view inside a container will allow us to make
		// it invisible later. See checkHeaderViewVisibility()
		FrameLayout headerContainer = new FrameLayout(inflater.getContext());
		mSearchHeaderView = inflater.inflate(R.layout.search_header, null, false);
		headerContainer.addView(mSearchHeaderView);
		getListView().addHeaderView(headerContainer, null, false);
		checkHeaderViewVisibility();

		mSearchProgress = getView().findViewById(R.id.search_progress);
		mSearchProgressText = (TextView) mSearchHeaderView.findViewById(R.id.totalContactsText);

		starView = inflater.inflate(R.layout.mst_star_contacts_header, null, false);
		//		starView.setVisibility(View.GONE);
		starViewTitle=starView.findViewById(R.id.listview_item_header);
		//		starContainer = new FrameLayout(inflater.getContext());
		//		starContainer.addView(starView);
		getListView().addHeaderView(starView, null, false);

		starAdapter=new StarContactsAdapter(new ArrayList<Object[]>(),getContext(),new ViewContactListener(){
			public void onViewContactAction(Uri uri){				
				Log.d(TAG,"onViewContactAction uri:"+uri);
				if (uri == null) {
					return;
				}
				if (ExtensionManager.getInstance().getRcsExtension()
						.addRcsProfileEntryListener(uri, false)) {
					return;
				}
				viewContact(uri);
			}
		});

		starGridView=(GridView)getView().findViewById(R.id.star_contacts_gridview);
		//		starGridView.setNumColumns(5);
		starGridView.setAdapter(starAdapter);

		footer= inflater.inflate(R.layout.mst_contact_list_footer, null, false);
		allCountTextView=(TextView)footer.findViewById(R.id.mst_contact_list_footer_content);
		getListView().addFooterView(footer, null, false);

		//		if (null == format) {
		//			format = new HanyuPinyinOutputFormat();
		//		}
		//		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);   
		//		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	}

	private View footer;




	protected void startLoading() {
		footer.setVisibility(View.GONE);
		allCountTextView.setVisibility(View.GONE);
		super.startLoading();
	}



	//    private void inflateContacts(){
	//        new AsyncTask<Object,Object,ArrayList>(){
	//            @Override
	//            protected ArrayList doInBackground(Object... params) {
	//                return Been.getContacts();
	//            }
	//
	//            @Override
	//            protected void onPostExecute(ArrayList arrayList) {
	//                mAdapter.setData(arrayList);
	//                mAdapter.notifyDataSetChanged();
	//                initIndexBar(arrayList);
	//            }
	//        }.execute();
	//    }

	@Override
	public void setSearchMode(boolean flag) {
		Log.d(TAG,"setSearchMode,flag:"+flag);
		super.setSearchMode(flag);
		checkHeaderViewVisibility();
		if (!flag) showSearchProgress(false);
	}

	/** Show or hide the directory-search progress spinner. */
	private void showSearchProgress(boolean show) {
		if (mSearchProgress != null) {
			mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	private void checkHeaderViewVisibility() {
		updateFilterHeaderView();

		// Hide the search header by default.
		if (mSearchHeaderView != null) {
			mSearchHeaderView.setVisibility(View.GONE);
		}
	}

	@Override
	public void setFilter(ContactListFilter filter) {
		super.setFilter(filter);
		updateFilterHeaderView();
	}

	private void updateFilterHeaderView() {
		if (mAccountFilterHeader == null) {
			return; // Before onCreateView -- just ignore it.
		}
		final ContactListFilter filter = getFilter();
		if (filter != null && !isSearchMode()) {
			final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPeople(
					mAccountFilterHeader, filter, false);
			mAccountFilterHeader.setVisibility(shouldShowHeader ? View.VISIBLE : View.GONE);
		} else {
			mAccountFilterHeader.setVisibility(View.GONE);
		}
		Log.d(TAG,"updateFilterHeaderView:"+mAccountFilterHeader.getVisibility());
	}

	@Override
	public void setProfileHeader() {
		mUserProfileExists = getAdapter().hasProfile();
		showEmptyUserProfile(!mUserProfileExists && !isSearchMode());

		if (isSearchMode()) {
			ContactListAdapter adapter = getAdapter();
			if (adapter == null) {
				return;
			}

			// In search mode we only display the header if there is nothing found
			if (TextUtils.isEmpty(getQueryString()) || !adapter.areAllPartitionsEmpty()) {
				mSearchHeaderView.setVisibility(View.GONE);
				showSearchProgress(false);
			} else {
				mSearchHeaderView.setVisibility(View.VISIBLE);
				if (adapter.isLoading()) {
					mSearchProgressText.setText(R.string.search_results_searching);
					showSearchProgress(true);
				} else {
					if(TextUtils.isEmpty(getQueryString())||TextUtils.equals(SEARCH_BEGIN_STRING, getQueryString())){
						mSearchProgressText.setText(null);
					}else{
						mSearchProgressText.setText(R.string.listFoundAllContactsZero);
					}
					mSearchProgressText.sendAccessibilityEvent(
							AccessibilityEvent.TYPE_VIEW_SELECTED);
					showSearchProgress(false);
				}
			}
			showEmptyUserProfile(false);
		}

		/// M: [VCS] @{
		int count = getContactCount();
		//        if (mContactsLoadListener != null) {
		//            mContactsLoadListener.onContactsLoad(count);
		//        }
		/// @}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_ACCOUNT_FILTER) {
			if (getActivity() != null) {
				AccountFilterUtil.handleAccountFilterResult(
						ContactListFilterController.getInstance(getActivity()), resultCode, data);
			} else {
				Log.e(TAG, "getActivity() returns null during Fragment#onActivityResult()");
			}
		}
	}

	private void showEmptyUserProfile(boolean show) {
		// Changing visibility of just the mProfileHeader doesn't do anything unless
		// you change visibility of its children, hence the call to mCounterHeaderView
		// and mProfileTitle
		//		Log.d(TAG, "showEmptyUserProfile show : " + show);
		//		mProfileHeaderContainer.setVisibility(show ? View.VISIBLE : View.GONE);
		//		mProfileHeader.setVisibility(show ? View.VISIBLE : View.GONE);
		//		mProfileTitle.setVisibility(show ? View.VISIBLE : View.GONE);
		//		mProfileMessage.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	/**
	 * This method creates a pseudo user profile contact. When the returned query doesn't have
	 * a profile, this methods creates 2 views that are inserted as headers to the listview:
	 * 1. A header view with the "ME" title and the contacts count.
	 * 2. A button that prompts the user to create a local profile
	 */
	private void addEmptyUserProfileHeader(LayoutInflater inflater) {
		ListView list = getListView();
		// Add a header with the "ME" name. The view is embedded in a frame view since you cannot
		// change the visibility of a view in a ListView without having a parent view.
		mProfileHeader = inflater.inflate(R.layout.user_profile_header, null, false);
		mProfileTitle = (TextView) mProfileHeader.findViewById(R.id.profile_title);
		mProfileHeaderContainer = new FrameLayout(inflater.getContext());
		mProfileHeaderContainer.addView(mProfileHeader);
		list.addHeaderView(mProfileHeaderContainer, null, false);

		// Add a button with a message inviting the user to create a local profile
		mProfileMessage = (Button) mProfileHeader.findViewById(R.id.user_profile_button);
		mProfileMessage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (ExtensionManager.getInstance().getRcsExtension()
						.addRcsProfileEntryListener(null, true)) {
					return;
				}
				Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
				intent.putExtra(ContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
				ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
			}
		});
	}

	/** M: Bug Fix For ALPS00115673. @{*/
	private ProgressBar mProgress;
	private View mLoadingContainer;
	private WaitCursorView mWaitCursorView;
	private TextView mLoadingContact;
	/** @} */
	/// M: for vcs
	//    private VcsController.ContactsLoadListener mContactsLoadListener = null;

	/**
	 * M: Bug Fix CR ID: ALPS00279111.
	 */
	public void closeWaitCursor() {
		// TODO Auto-generated method stub
		Log.d(TAG, "closeWaitCursor   DefaultContactBrowseListFragment");
		mWaitCursorView.stopWaitCursor();
	}

	/**
	 * M: [vcs] for vcs.
	 */
	//    public void setContactsLoadListener(VcsController.ContactsLoadListener listener) {
	//        mContactsLoadListener = listener;
	//    }

	/**
	 * M: for ALPS01766595.
	 */
	private int getContactCount() {
		int count = isSearchMode() ? 0 : getAdapter().getCount();
		if (mUserProfileExists) {
			count -= PROFILE_NUM;
		}
		return count;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);

		Cursor cursor=data;
		if(cursor==null) {
			mIndexBar.setVisibility(View.GONE);
			return;
		}
		if(data.getCount()>0&&mStarredCount>0){
			starView.setVisibility(View.VISIBLE);
			starViewTitle.setVisibility(View.VISIBLE);
			starGridView.setVisibility(View.VISIBLE);
			starView.setPadding(0, 0, 0, 0);			
		}else{
			starView.setVisibility(View.GONE);
			starViewTitle.setVisibility(View.GONE);
			starGridView.setVisibility(View.GONE);
			starView.setPadding(0, -2000, 0, 0);
		}
		if(isSearchMode()||contactsCount==0){
			footer.setVisibility(View.GONE);
			allCountTextView.setVisibility(View.GONE);
		}else{
			footer.setVisibility(View.VISIBLE);
			allCountTextView.setVisibility(View.VISIBLE);
			allCountTextView.setText(getContext().getString(R.string.mst_contacts_count,contactsCount+""));
		}

		Log.d(TAG,"onLoadFinished0,isSearchMode:"+isSearchMode());		

		//add by lgy for 2824400
		if(data.getCount() == 1) {
			Activity a = getActivity();
			if(a != null) {
				if(a instanceof PeopleActivity) {
					((PeopleActivity)a).updateEmptyOrNot();
				}
			}
		}
		
		initT9Search();
	}
	private int loadSearchContactsState=0;//0 unload;1 isloading;2 loaded

	public void initT9Search(){
		if(loadSearchContactsState==0){
			loadSearchContactsState=1;
			boolean startLoad = mContactsHelper.startLoadContacts(false);
			if(!startLoad) loadSearchContactsState=0;
		}
	}
	
	@Override
	public void setLoadSearchContactsState(int loadSearchContactsState) {
		this.loadSearchContactsState = loadSearchContactsState;
	}
	private static final int SUBACTIVITY_ACCOUNT_FILTER = 2;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		}		
	}
}
