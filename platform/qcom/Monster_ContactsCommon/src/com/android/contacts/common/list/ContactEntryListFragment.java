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

import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import java.util.ArrayList;
import com.mst.t9search.MstSearchContactsAdapter.OnClickGroupListener;
import com.mst.t9search.ContactsHelper.OnContactsLoad;
import mst.widget.MstIndexBar;
import mst.widget.MstIndexBar.Letter;
//import com.android.contacts.common.mst.HanziToPinyin;
//import com.android.contacts.common.mst.HanziToPinyin.Token;
import com.android.contacts.common.R;
import com.mst.t9search.ContactsHelper.OnContactsChanged;
import com.mst.t9search.MstSearchContactsAdapter;
import com.mst.t9search.ContactsHelper;
import com.mst.t9search.ViewUtil;
import com.android.contacts.common.list.ContactListAdapter.ContactQuery;
import com.android.contacts.common.mst.ContactForIndex;
import com.android.contacts.common.mst.DensityUtil;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.mst.StarContactsAdapter;

import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import mst.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.MstSearchView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.preference.ContactsPreferences;
import com.android.contacts.common.util.ContactListViewUtils;
import com.mst.t9search.ContactsHelper;
import com.mst.t9search.ViewUtil;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.ActionMode;
import mst.widget.MstIndexBar;
import mst.widget.SliderView;
import com.android.contacts.common.util.PermissionsUtil;

/**
 * Common base class for various contact-related list fragments.
 */
public abstract class ContactEntryListFragment<T extends ContactEntryListAdapter>
extends Fragment
implements OnItemClickListener, OnScrollListener, OnFocusChangeListener, OnTouchListener,OnContactsLoad,MstIndexBar.OnTouchStateChangedListener,MstIndexBar.OnSelectListener,
OnItemLongClickListener, LoaderCallbacks<Cursor> {
	private static final String TAG = "ContactEntryListFragment";
	public static final String SEARCH_BEGIN_STRING="mst_querystring_for_contact_search_begin";

	// TODO: Make this protected. This should not be used from the PeopleActivity but
	// instead use the new startActivityWithResultFromFragment API
	public static final int ACTIVITY_REQUEST_CODE_PICKER = 1;

	private static final String KEY_LIST_STATE = "liststate";
	private static final String KEY_SECTION_HEADER_DISPLAY_ENABLED = "sectionHeaderDisplayEnabled";
	private static final String KEY_PHOTO_LOADER_ENABLED = "photoLoaderEnabled";
	private static final String KEY_QUICK_CONTACT_ENABLED = "quickContactEnabled";
	private static final String KEY_ADJUST_SELECTION_BOUNDS_ENABLED =
			"adjustSelectionBoundsEnabled";
	private static final String KEY_INCLUDE_PROFILE = "includeProfile";
	private static final String KEY_SEARCH_MODE = "searchMode";
	private static final String KEY_VISIBLE_SCROLLBAR_ENABLED = "visibleScrollbarEnabled";
	private static final String KEY_SCROLLBAR_POSITION = "scrollbarPosition";
	private static final String KEY_QUERY_STRING = "queryString";
	private static final String KEY_DIRECTORY_SEARCH_MODE = "directorySearchMode";
	private static final String KEY_SELECTION_VISIBLE = "selectionVisible";
	private static final String KEY_REQUEST = "request";
	private static final String KEY_DARK_THEME = "darkTheme";
	private static final String KEY_LEGACY_COMPATIBILITY = "legacyCompatibility";
	private static final String KEY_DIRECTORY_RESULT_LIMIT = "directoryResultLimit";

	private static final String DIRECTORY_ID_ARG_KEY = "directoryId";

	private static final int DIRECTORY_LOADER_ID = -1;

	private static final int DIRECTORY_SEARCH_DELAY_MILLIS = 300;
	private static final int DIRECTORY_SEARCH_MESSAGE = 1;

	private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

	private boolean mSectionHeaderDisplayEnabled;
	private boolean mPhotoLoaderEnabled;
	private boolean mQuickContactEnabled = true;
	private boolean mAdjustSelectionBoundsEnabled = true;
	private boolean mIncludeProfile;
	private boolean mSearchMode;
	private boolean mVisibleScrollbarEnabled;
	private boolean mShowEmptyListForEmptyQuery;
	private int mVerticalScrollbarPosition = getDefaultVerticalScrollbarPosition();
	private String mQueryString;
	private int mDirectorySearchMode = DirectoryListLoader.SEARCH_MODE_NONE;
	private boolean mSelectionVisible;
	private boolean mLegacyCompatibility;

	private boolean mEnabled = true;
	private T mAdapter;
	public StarContactsAdapter starAdapter;
	public GridView starGridView;
	private View mView;
	protected ListView mListView;

	public FragmentCallbacks mCallbacks;
	public void setCallbacks(FragmentCallbacks mCallbacks) {
		this.mCallbacks = mCallbacks;
	}
	protected ActionMode actionMode;
	protected BottomNavigationView bottomBar;
	protected View header;
	protected MstSearchView mSearchView;
	protected View getHeader() {
		return header;
	}
	public MstSearchView getSearchView() {
		return mSearchView;
	}
	public void setSearchView(MstSearchView mSearchView) {
		this.mSearchView = mSearchView;
	}

	public void showHeader(boolean show){
		if(show){
			header.setVisibility(View.VISIBLE);
			header.setPadding(0, 0, 0, 0);
		}else{
			header.setVisibility(View.GONE);
			header.setPadding(0, -2000, 0, 0);
		}
	}
	public void setBottomBar(BottomNavigationView bottomBar) {
		this.bottomBar = bottomBar;
	}
	public void setActionMode(ActionMode actionMode) {
		Log.d(TAG,"setActionMode:"+actionMode);
		this.actionMode = actionMode;
	}

	/**
	 * Used for keeping track of the scroll state of the list.
	 */
	private Parcelable mListState;
	protected boolean isForContactsChoice=false;
	public void setForContactsChoice(boolean isForContactsChoice) {
		this.isForContactsChoice=isForContactsChoice;
	}
	private int mDisplayOrder;
	private int mSortOrder;
	private int mDirectoryResultLimit = DEFAULT_DIRECTORY_RESULT_LIMIT;

	private ContactPhotoManager mPhotoManager;
	private ContactsPreferences mContactsPrefs;

	private boolean mForceLoad;

	private boolean mDarkTheme;

	protected boolean mUserProfileExists;

	/** M: for ALPS01766595 */
	protected static final int PROFILE_NUM = 1;
	private static final int STATUS_NOT_LOADED = 0;
	private static final int STATUS_LOADING = 1;
	private static final int STATUS_LOADED = 2;

	private int mDirectoryListStatus = STATUS_NOT_LOADED;

	/**
	 * Indicates whether we are doing the initial complete load of data (false) or
	 * a refresh caused by a change notification (true)
	 */
	private boolean mLoadPriorityDirectoriesOnly;

	private Context mContext;

	private LoaderManager mLoaderManager;
	//	protected AbsListIndexer mAlphbetIndexView;


	private Handler mDelayedDirectorySearchHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == DIRECTORY_SEARCH_MESSAGE) {
				loadDirectoryPartition(msg.arg1, (DirectoryPartition) msg.obj);
			}
		}
	};
	private int defaultVerticalScrollbarPosition;

	protected abstract View inflateView(LayoutInflater inflater, ViewGroup container);
	protected abstract T createListAdapter();

	/**
	 * @param position Please note that the position is already adjusted for
	 *            header views, so "0" means the first list item below header
	 *            views.
	 */
	protected abstract void onItemClick(int position, long id);

	/**
	 * @param position Please note that the position is already adjusted for
	 *            header views, so "0" means the first list item below header
	 *            views.
	 */
	protected boolean onItemLongClick(int position, long id) {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");
		//		final ContentResolver contentResolver = getActivity().getContentResolver();
		//		//add by lgy start
		//		if(PermissionsUtil.hasContactsPermissions(getActivity())) {
		//			contentResolver.query(Uri.parse("content://com.android.contacts/mst_dialer_search_init"),
		//					null, null, null, null);
		//		}
		//		//add by lgy end
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG,"onAttacth,activity:"+activity);
		super.onAttach(activity);
		setContext(activity);
		setLoaderManager(super.getLoaderManager());
	}

	/**
	 * Sets a context for the fragment in the unit test environment.
	 */
	public void setContext(Context context) {
		mContext = context;		
	}

	public Context getContext() {
		return mContext;
	}

	public void setEnabled(boolean enabled) {
		if (mEnabled != enabled) {
			mEnabled = enabled;
			if (mAdapter != null) {
				if (mEnabled) {
					reloadData();
				} else {
					mAdapter.clearPartitions();
				}
			}
		}
	}

	/**
	 * Overrides a loader manager for use in unit tests.
	 */
	public void setLoaderManager(LoaderManager loaderManager) {
		mLoaderManager = loaderManager;
	}

	@Override
	public LoaderManager getLoaderManager() {
		return mLoaderManager;
	}

	public T getAdapter() {
		return mAdapter;
	}

	@Override
	public View getView() {
		return mView;
	}

	public ListView getListView() {
		return mListView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_SECTION_HEADER_DISPLAY_ENABLED, mSectionHeaderDisplayEnabled);
		outState.putBoolean(KEY_PHOTO_LOADER_ENABLED, mPhotoLoaderEnabled);
		outState.putBoolean(KEY_QUICK_CONTACT_ENABLED, mQuickContactEnabled);
		outState.putBoolean(KEY_ADJUST_SELECTION_BOUNDS_ENABLED, mAdjustSelectionBoundsEnabled);
		outState.putBoolean(KEY_INCLUDE_PROFILE, mIncludeProfile);
		outState.putBoolean(KEY_SEARCH_MODE, mSearchMode);
		outState.putBoolean(KEY_VISIBLE_SCROLLBAR_ENABLED, mVisibleScrollbarEnabled);
		outState.putInt(KEY_SCROLLBAR_POSITION, mVerticalScrollbarPosition);
		outState.putInt(KEY_DIRECTORY_SEARCH_MODE, mDirectorySearchMode);
		outState.putBoolean(KEY_SELECTION_VISIBLE, mSelectionVisible);
		outState.putBoolean(KEY_LEGACY_COMPATIBILITY, mLegacyCompatibility);
		outState.putString(KEY_QUERY_STRING, mQueryString);
		outState.putInt(KEY_DIRECTORY_RESULT_LIMIT, mDirectoryResultLimit);
		outState.putBoolean(KEY_DARK_THEME, mDarkTheme);

		if (mListView != null) {
			outState.putParcelable(KEY_LIST_STATE, mListView.onSaveInstanceState());
		}
	}

	protected ContactListFilter mstFilter;
	public void setMstFilter(ContactListFilter filter) {
		mstFilter = filter;
	}

	protected String mstFilterString;	
	public void setMstFilterString(String mstFilterString) {
		this.mstFilterString = mstFilterString;
	}
	@Override
	public void onCreate(Bundle savedState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedState);
		//M:moved to last
		//restoreSavedState(savedState);
		mAdapter = createListAdapter();
		mContactsPrefs = new ContactsPreferences(mContext);
		restoreSavedState(savedState); 
	}

	public void restoreSavedState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		mSectionHeaderDisplayEnabled = savedState.getBoolean(KEY_SECTION_HEADER_DISPLAY_ENABLED);
		mPhotoLoaderEnabled = savedState.getBoolean(KEY_PHOTO_LOADER_ENABLED);
		mQuickContactEnabled = savedState.getBoolean(KEY_QUICK_CONTACT_ENABLED);
		mAdjustSelectionBoundsEnabled = savedState.getBoolean(KEY_ADJUST_SELECTION_BOUNDS_ENABLED);
		mIncludeProfile = savedState.getBoolean(KEY_INCLUDE_PROFILE);
		mSearchMode = savedState.getBoolean(KEY_SEARCH_MODE);
		mVisibleScrollbarEnabled = savedState.getBoolean(KEY_VISIBLE_SCROLLBAR_ENABLED);
		mVerticalScrollbarPosition = savedState.getInt(KEY_SCROLLBAR_POSITION);
		mDirectorySearchMode = savedState.getInt(KEY_DIRECTORY_SEARCH_MODE);
		mSelectionVisible = savedState.getBoolean(KEY_SELECTION_VISIBLE);
		mLegacyCompatibility = savedState.getBoolean(KEY_LEGACY_COMPATIBILITY);
		mQueryString = savedState.getString(KEY_QUERY_STRING);
		mDirectoryResultLimit = savedState.getInt(KEY_DIRECTORY_RESULT_LIMIT);
		mDarkTheme = savedState.getBoolean(KEY_DARK_THEME);

		// Retrieve list state. This will be applied in onLoadFinished
		mListState = savedState.getParcelable(KEY_LIST_STATE);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG,"onStart(),isSearchMode:"+isSearchMode());
		mContactsPrefs.registerChangeListener(mPreferencesChangeListener);

		mForceLoad = loadPreferences();
		mForceLoad=true;

		mDirectoryListStatus = STATUS_NOT_LOADED;
		mLoadPriorityDirectoriesOnly = true;

		if(!isSearchMode()){
			startLoading();
		}
	}

	public void startLoad(){
		startLoading();
	}

	protected void startLoading() {
		Log.d(TAG, "startLoading");
		if (mAdapter == null) {
			// The method was called before the fragment was started
			Log.d(TAG, "[statLoading] mAdapter is null");
			return;
		}

		configureAdapter();
		int partitionCount = mAdapter.getPartitionCount();
		Log.d(TAG, "startLoading,partitionCount:"+partitionCount);
		for (int i = 0; i < partitionCount; i++) {
			Partition partition = mAdapter.getPartition(i);
			if (partition instanceof DirectoryPartition) {
				Log.d(TAG, "startLoading1");
				DirectoryPartition directoryPartition = (DirectoryPartition)partition;
				if (directoryPartition.getStatus() == DirectoryPartition.STATUS_NOT_LOADED||isSearchMode()) {
					if (directoryPartition.isPriorityDirectory() || !mLoadPriorityDirectoriesOnly) {
						startLoadingDirectoryPartition(i);
					}
				}
			} else {
				Log.d(TAG,"startLoading2");
				getLoaderManager().initLoader(i, null, this);
			}
		}

		// Next time this method is called, we should start loading non-priority directories
		mLoadPriorityDirectoriesOnly = false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader,id:"+id);
		if (id == DIRECTORY_LOADER_ID) {
			DirectoryListLoader loader = new DirectoryListLoader(mContext);
			loader.setDirectorySearchMode(mAdapter.getDirectorySearchMode());
			loader.setLocalInvisibleDirectoryEnabled(
					ContactEntryListAdapter.LOCAL_INVISIBLE_DIRECTORY_ENABLED);
			return loader;
		} else {
			CursorLoader loader = createCursorLoader(mContext);
			long directoryId = args != null && args.containsKey(DIRECTORY_ID_ARG_KEY)
					? args.getLong(DIRECTORY_ID_ARG_KEY)
							: Directory.DEFAULT;
					/// M: Set the value whether show the sdn number.
					mAdapter.setShowSdnNumber(isShowSdnNumber());

					Log.d(TAG, "[onCreateLoader] loader: " + loader + ",id:" + id+" directoryId:"+directoryId);
					mAdapter.configureLoader(loader, directoryId);
					return loader;
		}
	}

	public CursorLoader createCursorLoader(Context context) {
		return new CursorLoader(context, null, null, null, null, null) {
			@Override
			protected Cursor onLoadInBackground() {
				try {
					return super.onLoadInBackground();
				} catch (RuntimeException e) {
					// We don't even know what the projection should be, so no point trying to
					// return an empty MatrixCursor with the correct projection here.
					Log.w(TAG, "RuntimeException while trying to query ContactsProvider.");
					return null;
				}
			}
		};
	}

	private void startLoadingDirectoryPartition(int partitionIndex) {
		Log.d(TAG, "startLoadingDirectoryPartition:"+partitionIndex);
		DirectoryPartition partition = (DirectoryPartition)mAdapter.getPartition(partitionIndex);
		partition.setStatus(DirectoryPartition.STATUS_LOADING);
		long directoryId = partition.getDirectoryId();
		if (mForceLoad) {
			if (directoryId == Directory.DEFAULT) {
				Log.d(TAG, "startLoadingDirectoryPartition1:"+partitionIndex);
				loadDirectoryPartition(partitionIndex, partition);
			} else {
				Log.d(TAG, "startLoadingDirectoryPartition2:"+partitionIndex);
				loadDirectoryPartitionDelayed(partitionIndex, partition);
			}
		} else {
			Log.d(TAG, "startLoadingDirectoryPartition3:"+partitionIndex);
			Bundle args = new Bundle();
			args.putLong(DIRECTORY_ID_ARG_KEY, directoryId);
			getLoaderManager().initLoader(partitionIndex, args, this);
		}
	}

	/**
	 * Queues up a delayed request to search the specified directory. Since
	 * directory search will likely introduce a lot of network traffic, we want
	 * to wait for a pause in the user's typing before sending a directory request.
	 */
	private void loadDirectoryPartitionDelayed(int partitionIndex, DirectoryPartition partition) {
		mDelayedDirectorySearchHandler.removeMessages(DIRECTORY_SEARCH_MESSAGE, partition);
		Message msg = mDelayedDirectorySearchHandler.obtainMessage(
				DIRECTORY_SEARCH_MESSAGE, partitionIndex, 0, partition);
		mDelayedDirectorySearchHandler.sendMessageDelayed(msg, DIRECTORY_SEARCH_DELAY_MILLIS);
	}

	/**
	 * Loads the directory partition.
	 */
	protected void loadDirectoryPartition(int partitionIndex, DirectoryPartition partition) {
		Log.d(TAG, "loadDirectoryPartition:"+partitionIndex);
		Bundle args = new Bundle();
		args.putLong(DIRECTORY_ID_ARG_KEY, partition.getDirectoryId());
		Log.d(TAG,"restartLoader");
		getLoaderManager().restartLoader(partitionIndex, args, this);
	}

	/**
	 * Cancels all queued directory loading requests.
	 */
	private void removePendingDirectorySearchRequests() {
		mDelayedDirectorySearchHandler.removeMessages(DIRECTORY_SEARCH_MESSAGE);
	}

	// 将字母转换成数字  
	public int letterToNum(String input) {
		return input.getBytes()[0] - 96;
	} 

	public interface ViewContactListener{
		void onViewContactAction(Uri uri);
	}

	public TextView getAllCountTextView() {
		return allCountTextView;
	}

	protected int mStarredCount;
	int groupCount=0;
	int isQueryCommon=0;
	protected int contactsCount=0;

	protected ArrayList<ContactForIndex> contactForIndexs;
	protected HashMap<String, Integer> indexHashMap;
	protected ArrayList<Integer> indexArrayList;
	protected MstIndexBar mIndexBar;
	public MstIndexBar getmIndexBar() {
		return mIndexBar;
	}

	public void setmIndexBar(MstIndexBar mIndexBar) {
		this.mIndexBar = mIndexBar;
		this.mIndexBar.setOnSelectListener(this);
		this.mIndexBar.setOnTouchStateChangedListener(this);

	}
	protected MatrixCursor allCursor;
	public int allCount=0;
	protected TextView allCountTextView;

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		long time1=System.currentTimeMillis();
		allCount=data==null?0:data.getCount();
		if(!isSearchMode()&&mCallbacks!=null) mCallbacks.onFragmentCallback(FragmentCallbacks.updateViewConfiguration, allCount);
		Log.d(TAG, "[onLoadFinished02] loader:" + loader+" loadId:"+loader.getId() + " data:" + data+" size:"+(data==null?0:data.getCount())+" isForContactsChoice:"+isForContactsChoice);
		if(data==null) {
			onPartitionLoaded(loader.getId(), new MatrixCursor(new String[]{}));
			if(allCountTextView!=null) allCountTextView.setVisibility(View.GONE);
			return;
		}
		//		Log.d(TAG, "isLastItemVisible:"+isLastItemVisible());


		int mCommonlyCount=0;
		groupCount=0;
		isQueryCommon=0;

		Bundle extra=data.getExtras();
		if(extra!=null){
			mCommonlyCount=extra.getInt("commonlyCount",0);
			isQueryCommon=extra.getInt("isQueryCommon",0);
		}
		Log.d(TAG, "[onLoadFinished22] loader:" + loader+" loadId:"+loader.getId() + " data:" + data+" size:"+(data==null?0:allCount)
				+" extra:"+extra+" mCommonlyCount:"+mCommonlyCount+" groupCount:"+groupCount+" isSearchMode():"+isSearchMode()
				+" isForDialerSearch:"+isForDialerSearch());
		/// M: check whether the fragment still in Activity @{
		if (!isAdded()) {
			Log.d(TAG, "onLoadFinished(),This Fragment is not add to the Activity now.data:"
					+ data);
			return;
		}
		/// @}

		if(mIndexBar!=null){
			if(allCount>0&&!isSearchMode()){
				mIndexBar.setVisibility(View.VISIBLE);
			}else{
				mIndexBar.setVisibility(View.GONE);
			}
		}
		else mIndexBar.setVisibility(View.VISIBLE);
		if (!mEnabled) {
			Log.d(TAG, "return in onLoad finish,mEnabled:" + mEnabled);
			return;
		}

		if (loader instanceof ProfileAndContactsLoader && data.getCount()>0&&!isSearchMode()) {
			mStarredCount = ((ProfileAndContactsLoader) loader).getCursorCount();
			contactsCount=data.getCount()-mStarredCount;
			Log.d(TAG,"mStarredCount:"+mStarredCount);
			mAdapter.setStarredCount(mStarredCount);

			Cursor resultData=null;
			if(data!=null&&!isSearchMode()/*&&starAdapter!=null*/){
				//				MatrixCursor starCursor = new MatrixCursor(ContactQuery.CONTACT_PROJECTION_PRIMARY);
				ArrayList<Object[]> starArraylist=new ArrayList<Object[]>();
				allCursor = new MatrixCursor(ContactQuery.CONTACT_PROJECTION_PRIMARY){
					@Override
					public Bundle getExtras() {
						// Need to get the extras from the contacts cursor.
						return data == null ? new Bundle() : data.getExtras();
					}
				};

				data.moveToFirst();
				Log.d(TAG,"onLoadfinished1,spend0:"+(System.currentTimeMillis()-time1));
				Object[] objs=null;
				try{
					for(int i=0;i<allCount;i++){
						objs = new Object[ContactQuery.CONTACT_PROJECTION_PRIMARY.length];
						for(int j=0;j<ContactQuery.CONTACT_PROJECTION_PRIMARY.length;j++){
							objs[j] = data.getString(j);
						}
						if(i>=allCount-mStarredCount){				
							starArraylist.add(objs);
						}else{
							allCursor.addRow(objs);
						}
						data.moveToNext();
					}
				}catch(Exception e){
					e.printStackTrace();
				}

				Log.d(TAG,"onLoadfinished2,spend1:"+(System.currentTimeMillis()-time1));

				Log.d(TAG,"starArraylist:"+starArraylist+" count:"+(starArraylist==null?0:starArraylist.size()));
				Log.d(TAG,"allCursor:"+allCursor+" count:"+(allCursor==null?0:allCursor.getCount()));
				resultData=data;
				if(starAdapter!=null) {
					starAdapter.setList(starArraylist);
					setListViewHeightBasedOnChildren();
					starAdapter.notifyDataSetChanged();
				}
			}else if(isSearchMode()&&!isForContactsChoice){	
				return;
				//				resultData=data;
				/*
				data.moveToLast();
				while(true){
					int contactId=Integer.parseInt(data.getString(1));
					Log.d(TAG,"contactId:"+contactId);
					if(contactId<=0) 
						groupCount++;
					else 
						break;
					if(!data.moveToPrevious()) break;
				}
				Log.d(TAG,"groupCount:"+groupCount);
				mAdapter.setCommonlyCount(mCommonlyCount);
				mAdapter.setGroupCount(groupCount);
				mAdapter.setIsQueryCommon(isQueryCommon);
				 */}else{
					 resultData=data;
				 }

			int loaderId = loader.getId();
			if (loaderId == DIRECTORY_LOADER_ID) {
				mDirectoryListStatus = STATUS_LOADED;
				mAdapter.changeDirectories(resultData);
				Log.d(TAG, "onLoadFinished startloading,loaderId:" + loaderId);
				startLoading();
			} else {
				onPartitionLoaded(loaderId, resultData);
				if (isSearchMode()) {/*
					int directorySearchMode = getDirectorySearchMode();
					Log.d(TAG,"onLoadFinished,directorySearchMode:"+directorySearchMode+" mDirectoryListStatus:"+mDirectoryListStatus);
					if (directorySearchMode != DirectoryListLoader.SEARCH_MODE_NONE) {
						if (mDirectoryListStatus == STATUS_NOT_LOADED) {
							mDirectoryListStatus = STATUS_LOADING;
							getLoaderManager().initLoader(DIRECTORY_LOADER_ID, null, this);
						} else {
							startLoading();
						}
					}
				 */} else {
					 mDirectoryListStatus = STATUS_NOT_LOADED;
					 getLoaderManager().destroyLoader(DIRECTORY_LOADER_ID);
				 }
			}

			if(mIndexBar!=null){
				if(allCount>0&&!isSearchMode()){
					Log.d(TAG,"onLoadFinished111");
					mTask=null;
					mTask =new InitIndexBarTask(1);
					mTask.execute();
					//					mIndexBar.setVisibility(View.VISIBLE);
				}else{
					//					mIndexBar.setVisibility(View.GONE);
				}
			}
			return;
		}


		if(mIndexBar!=null&&data!=null&&!isSearchMode()){
			allCursor = new MatrixCursor(PHONES_PROJECTION){
				@Override
				public Bundle getExtras() {
					// Need to get the extras from the contacts cursor.
					return data == null ? new Bundle() : data.getExtras();
				}
			};
			data.moveToFirst();
			Object[] objs=null;
			for(int i=0;i<allCount;i++){
				objs = new Object[PHONES_PROJECTION.length];
				for(int j=0;j<PHONES_PROJECTION.length;j++){
					objs[j] = data.getString(j);
				}
				allCursor.addRow(objs);
				data.moveToNext();
			}
		}


		int loaderId = loader.getId();
		if (loaderId == DIRECTORY_LOADER_ID) {
			mDirectoryListStatus = STATUS_LOADED;
			mAdapter.changeDirectories(data);
			Log.d(TAG, "onLoadFinished startloading,loaderId:" + loaderId);
			startLoading();
		} else {
			onPartitionLoaded(loaderId, data);
			if (isSearchMode()) {/*
				int directorySearchMode = getDirectorySearchMode();
				Log.d(TAG,"onLoadFinished,directorySearchMode:"+directorySearchMode+" mDirectoryListStatus:"+mDirectoryListStatus);
				if (directorySearchMode != DirectoryListLoader.SEARCH_MODE_NONE) {
					if (mDirectoryListStatus == STATUS_NOT_LOADED) {
						mDirectoryListStatus = STATUS_LOADING;
						getLoaderManager().initLoader(DIRECTORY_LOADER_ID, null, this);
					} else {
						startLoading();
					}
				}
			 */} else {
				 mDirectoryListStatus = STATUS_NOT_LOADED;
				 getLoaderManager().destroyLoader(DIRECTORY_LOADER_ID);
			 }
		}


		if(mIndexBar!=null){
			if(allCount>0&&!isSearchMode()){
				Log.d(TAG,"onLoadFinished123");
				mTask=null;
				mTask =new InitIndexBarTask(2);
				mTask.execute();
				//				mIndexBar.setVisibility(View.VISIBLE);
			}else{
				//				mIndexBar.setVisibility(View.GONE);
			}
		}
	}


	public boolean isLetter(String cha){
		if(cha == null || cha.length() <= 0) return false;
		char first = cha.toUpperCase().charAt(0);
		if(first >= 'A' && first <= 'Z'){
			return true;
		}
		return false;
	}

	protected boolean isLastItemVisible() {
		final int lastItemPosition = getAdapter().getCount() - 1;
		final int lastVisiblePosition = getListView().getLastVisiblePosition();

		if (lastVisiblePosition >= lastItemPosition - 1) {
			final int childIndex = lastVisiblePosition - getListView().getFirstVisiblePosition();
			final int childCount = getListView().getChildCount();
			final int index = Math.min(childIndex, childCount - 1);
			final View lastVisibleChild = getListView().getChildAt(index);
			if (lastVisibleChild != null) {
				return lastVisibleChild.getBottom() <= getListView().getBottom();
			}
		}
		return false;
	}

	protected static final String[] PHONES_PROJECTION = new String[] { Phone._ID, // 0
		Phone.TYPE, // 1
		Phone.LABEL, // 2
		Phone.NUMBER, // 3
		Phone.DISPLAY_NAME_PRIMARY, // 4
		Phone.DISPLAY_NAME_ALTERNATIVE, // 5
		Phone.CONTACT_ID, // 6
		Phone.LOOKUP_KEY, // 7
		Phone.PHOTO_ID, // 8
		Phone.PHONETIC_NAME, // 9
		Contacts.INDICATE_PHONE_SIM, // 10
		Contacts.IS_SDN_CONTACT, // 11
		"quanpinyin",//12
		"jianpinyin",//13
		"phonebook_bucket",//14
	};

	public void setListViewHeightBasedOnChildren() {  
		// 获取listview的adapter  
		if (starAdapter == null||starGridView==null) {  
			return;  
		}  
		// 固定列宽，有多少列  
		int col = 5;// listView.getNumColumns();  
		int totalHeight = 0;  
		// i每次加5，相当于listAdapter.getCount()小于等于5时 循环一次，计算一次item的高度，  
		// listAdapter.getCount()小于等于10时计算两次高度相加  
		for (int i = 0; i <mStarredCount; i += col) {  
			// 获取listview的每一个item  
			View listItem = starAdapter.getView(i, null, starGridView);  
			listItem.measure(0, 0);  
			// 获取item的高度和  
			totalHeight += listItem.getMeasuredHeight();  
			totalHeight+=starGridView.getVerticalSpacing();
		}

		Log.d(TAG,"totalHeight:"+totalHeight);

		// 获取listview的布局参数  
		ViewGroup.LayoutParams params = starGridView.getLayoutParams();  
		// 设置高度  
		params.height = totalHeight;  
		// 设置margin  
		//           ((MarginLayoutParams) params).setMargins(10, 10, 10, 10);  
		// 设置参数  
		starGridView.setLayoutParams(params);  
	}  




	protected void onPartitionLoaded(int partitionIndex, Cursor data) {
		if (partitionIndex >= mAdapter.getPartitionCount()) {
			// When we get unsolicited data, ignore it.  This could happen
			// when we are switching from search mode to the default mode.
			Log.d(TAG, "[onPartitionLoaded] return");
			return;
		}

		Log.d(TAG, "[onPartitionLoaded],data:"+data+" count:"+(data==null?0:data.getCount()));
		mAdapter.setCursor(data);
		mAdapter.changeCursor(partitionIndex, data);

		setProfileHeader();

		if (!isLoading()) {
			completeRestoreInstanceState();
		}
	}

	public boolean isLoading() {
		if (mAdapter != null && mAdapter.isLoading()) {
			return true;
		}

		if (isLoadingDirectoryList()) {
			return true;
		}

		return false;
	}

	public boolean isLoadingDirectoryList() {
		return isSearchMode() && getDirectorySearchMode() != DirectoryListLoader.SEARCH_MODE_NONE
				&& (mDirectoryListStatus == STATUS_NOT_LOADED
				|| mDirectoryListStatus == STATUS_LOADING);
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		mContactsPrefs.unregisterChangeListener();
		mAdapter.clearPartitions();
	}

	@Override
	public void onDestroy(){

		super.onDestroy();
	}

	public void reloadData() {
		Log.d(TAG,"reloadData()");
		removePendingDirectorySearchRequests();
		mAdapter.onDataReload();
		mLoadPriorityDirectoriesOnly = true;
		mForceLoad = true;
		startLoading();
	}

	/**
	 * Shows a view at the top of the list with a pseudo local profile prompting the user to add
	 * a local profile. Default implementation does nothing.
	 */
	protected void setProfileHeader() {
		mUserProfileExists = false;
	}

	/**
	 * Provides logic that dismisses this fragment. The default implementation
	 * does nothing.
	 */
	protected void finish() {
	}

	public void setSectionHeaderDisplayEnabled(boolean flag) {
		if (mSectionHeaderDisplayEnabled != flag) {
			mSectionHeaderDisplayEnabled = flag;
			if (mAdapter != null) {
				mAdapter.setSectionHeaderDisplayEnabled(flag);
			}
			configureVerticalScrollbar();
		}
	}

	public boolean isSectionHeaderDisplayEnabled() {
		return mSectionHeaderDisplayEnabled;
	}

	public void setVisibleScrollbarEnabled(boolean flag) {
		if (mVisibleScrollbarEnabled != flag) {
			mVisibleScrollbarEnabled = flag;
			configureVerticalScrollbar();
		}
	}

	public boolean isVisibleScrollbarEnabled() {
		return mVisibleScrollbarEnabled;
	}

	public void setVerticalScrollbarPosition(int position) {
		if (mVerticalScrollbarPosition != position) {
			mVerticalScrollbarPosition = position;
			configureVerticalScrollbar();
		}
	}

	private void configureVerticalScrollbar() {
		//		boolean hasScrollbar = isVisibleScrollbarEnabled() && isSectionHeaderDisplayEnabled();

		boolean hasScrollbar=false;//liyang modify
		if (mListView != null) {
			mListView.setFastScrollEnabled(hasScrollbar);
			mListView.setFastScrollAlwaysVisible(hasScrollbar);
			mListView.setVerticalScrollbarPosition(mVerticalScrollbarPosition);
			mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		}
	}

	public void setPhotoLoaderEnabled(boolean flag) {
		mPhotoLoaderEnabled = flag;
		configurePhotoLoader();
	}

	public boolean isPhotoLoaderEnabled() {
		return mPhotoLoaderEnabled;
	}

	/**
	 * Returns true if the list is supposed to visually highlight the selected item.
	 */
	public boolean isSelectionVisible() {
		return mSelectionVisible;
	}

	public void setSelectionVisible(boolean flag) {
		this.mSelectionVisible = flag;
	}

	public void setQuickContactEnabled(boolean flag) {
		this.mQuickContactEnabled = flag;
	}

	public void setAdjustSelectionBoundsEnabled(boolean flag) {
		mAdjustSelectionBoundsEnabled = flag;
	}

	public void setIncludeProfile(boolean flag) {
		mIncludeProfile = flag;
		if (mAdapter != null) {
			mAdapter.setIncludeProfile(flag);
		}
	}

	/**
	 * Enter/exit search mode. This is method is tightly related to the current query, and should
	 * only be called by {@link #setQueryString}.
	 *
	 * Also note this method doesn't call {@link #reloadData()}; {@link #setQueryString} does it.
	 */
	public void setSearchMode(boolean flag) {
		if (mSearchMode != flag) {
			mSearchMode = flag;
			setSectionHeaderDisplayEnabled(!mSearchMode);

			if (!flag) {
				mDirectoryListStatus = STATUS_NOT_LOADED;
				getLoaderManager().destroyLoader(DIRECTORY_LOADER_ID);
			}

			if (mAdapter != null) {
				mAdapter.setSearchMode(flag);

				mAdapter.clearPartitions();
				if (!flag) {
					// If we are switching from search to regular display, remove all directory
					// partitions after default one, assuming they are remote directories which
					// should be cleaned up on exiting the search mode.
					mAdapter.removeDirectoriesAfterDefault();
				}
				mAdapter.configureDefaultPartition(false, flag);
			}

			if (mListView != null) {
				//				mListView.setFastScrollEnabled(!flag);
				mListView.setFastScrollEnabled(false);
			}
		}
	}

	public final boolean isSearchMode() {
		return mSearchMode;
	}

	public final String getQueryString() {
		return mQueryString;
	}

	private boolean isForDialerSearch=false;	

	public boolean isForDialerSearch() {
		return isForDialerSearch;
	}
	public void setForDialerSearch(boolean isForDialerSearch) {
		this.isForDialerSearch = isForDialerSearch;
	}
	public void setQueryString(String queryString, boolean delaySelection) {
		Log.d(TAG,"setQueryString,queryString:"+queryString+" delaySelection:"+delaySelection);

		if (!TextUtils.equals(mQueryString, queryString)) {
			if (mShowEmptyListForEmptyQuery && mAdapter != null && mListView != null) {
				if (TextUtils.isEmpty(mQueryString)) {
					// Restore the adapter if the query used to be empty.
					mListView.setAdapter(mAdapter);
				} else if (TextUtils.isEmpty(queryString)) {
					// Instantly clear the list view if the new query is empty.
					mListView.setAdapter(null);
				}
			}

			mQueryString = queryString;
			//			setSearchMode(!TextUtils.isEmpty(mQueryString) || mShowEmptyListForEmptyQuery);

			if (mAdapter != null) {
				mAdapter.setQueryString(queryString);
				//				mAdapter.setCommonlyCount(0);
				reloadData();
			}
		}
	}

	protected MstSearchContactsAdapter mContactsAdapter;
	private void refreshContactsLv() {
		if (null == mListView) {
			return;
		}
		mContactsAdapter.setContacts(mContactsHelper
				.getSearchContacts());
		Log.d(TAG,"mListView:"+mListView+"\ncontactsAdapter:"+mContactsAdapter+"\nlistview.getAdapter:"+mListView.getAdapter()
				+"\nsize:"+mContactsHelper
				.getSearchContacts().size()+"\nmContactsAdapter.getCount() :"+mContactsAdapter.getCount());
		if (null != mContactsAdapter) {
			mContactsAdapter.notifyDataSetChanged();
			if (mContactsAdapter.getCount() > 0) {
				if(mSearchZero!=null) mSearchZero.setVisibility(View.GONE);
				ViewUtil.showView(mListView);
			} else {
				if(mSearchZero!=null){
					mSearchZero.setVisibility(View.VISIBLE);
					//					mSearchZero.setText(R.string.mst_search_contacts_empty);
				}
				ViewUtil.hideView(mListView);
			}
		}
	}

	protected Uri getGroupUriFromIdAndAccountInfo(long groupId, String accountName,
			String accountType){
		return null;
	}
	protected void onViewGroupAction(Uri groupUri){}

	private OnClickGroupListener onClickGroupListener=new OnClickGroupListener() {

		@Override
		public void click(long groupId) {
			// TODO Auto-generated method stub
			Log.d(TAG,"click group,:"+groupId);
			final Uri uri=getGroupUriFromIdAndAccountInfo(groupId,"Phone","Local Phone Account");
			onViewGroupAction(uri);
		}
	};

	public void exitSearchMode(){
		mContactsAdapter = null;
		ListView listView=(ListView)getView().findViewById(R.id.mst_search_listview);
		listView.setVisibility(View.GONE);
		if(mSearchZero!=null) mSearchZero.setVisibility(View.GONE);
		showSearchProgress(false);
		createView();	
		//		mIndexBar.setVisibility(View.VISIBLE);
	}
	protected ContactsHelper mContactsHelper;

	public void setContactsHelper(ContactsHelper mContactsHelper) {
		this.mContactsHelper = mContactsHelper;
	}
	public void setQueryStringMst(String queryString){
		Log.d(TAG,"setQueryStringMst1:"+queryString+" isForDialerSearch:"+isForDialerSearch);

		if(mContactsHelper==null) return;

		if(TextUtils.equals(SEARCH_BEGIN_STRING, queryString)){

			mContactsAdapter = new MstSearchContactsAdapter(getContext(),/*mContactsHelper
					.getSearchContacts(),*/false);
			mContactsAdapter.setContacts(mContactsHelper.getSearchContacts());
			mContactsAdapter.setActivity(getActivity());
			mContactsAdapter.setOnClickGroupListener(onClickGroupListener);
			ListView listview = (ListView)getView().findViewById(android.R.id.list);
			listview.setVisibility(View.GONE);
			mListView = (ListView)getView().findViewById(R.id.mst_search_listview);
			mListView.setBackgroundColor(mContext.getResources().getColor(R.color.contact_main_background));
			if(mSearchZero==null){		
				mSearchProgress = getView().findViewById(R.id.search_progress);
				mSearchZero=(TextView)getView().findViewById(R.id.mSearchZero);
			}

			//			mListView.addHeaderView(headerContainer, null, false);
			mListView.setOnItemClickListener(this);
			mListView.setOnItemLongClickListener(this);
			mListView.setOnFocusChangeListener(this);
			mListView.setOnTouchListener(this);
			//		mListView.setFastScrollEnabled(!isSearchMode());
			mListView.setFastScrollEnabled(false);
			// Tell list view to not show dividers. We'll do it ourself so that we can *not* show
			// them when an A-Z headers is visible.
			mListView.setDividerHeight(0);
			// We manually save/restore the listview state
			mListView.setSaveEnabled(false);		
			mListView.setAdapter(mContactsAdapter);
			mIndexBar.setVisibility(View.GONE);
			mContactsHelper.setmOnContactsChanged(onContactsChanged);
			mContactsHelper.t9Search(null);
			mContactsHelper.setOnContactsLoad(this);
		}

		if(isForDialerSearch&&TextUtils.equals(queryString, mQueryString)) return;

		if(mContactsAdapter!=null&&mListView!=null){
			if (TextUtils.isEmpty(mQueryString)) {
				Log.d(TAG,"mQueryString null");
				// Restore the adapter if the query used to be empty.
				mListView.setAdapter(mContactsAdapter);
				mContactsHelper.setmOnContactsChanged(onContactsChanged);
				mContactsHelper.t9Search(null);
			}
			mQueryString = queryString;
			if (TextUtils.isEmpty(queryString)) {
				// Instantly clear the list view if the new query is empty.
				mContactsHelper.t9Search(null);
				mListView.setAdapter(null);
				mContactsHelper.setOnContactsLoad(this);
			}else{
				//				if(!isForDialerSearch&&allCount==0){
				//					if(mSearchZero!=null){
				//						mSearchZero.setVisibility(View.VISIBLE);
				//						mSearchZero.setText(R.string.listFoundAllContactsZero);
				//					}
				//					ViewUtil.hideView(mListView);
				//					return;
				//				}
				Log.d(TAG,"allCount:"+allCount+" isLoadingContacts:"+mContactsHelper.isLoadingContacts()+" isStartedLoadingContacts:"+mContactsHelper.isStartedLoadingContacts());
				if(mContactsHelper.isLoadingContacts()||!mContactsHelper.isStartedLoadingContacts()){//如果联系人正在准备中					
					isWaitingQuerying=true;
					//					mSearchHeaderView.setVisibility(View.VISIBLE);
					//						mSearchProgressText.setText(R.string.search_results_searching);
					showSearchProgress(true);
					//					mSearchZero.setText("正在准备搜索数据...");
					//					mSearchZero.setVisibility(View.VISIBLE);
					return;
				}
				mContactsHelper.query(queryString);		        
			}

			mContactsAdapter.setQueryString(queryString);
			setSearchMode(!TextUtils.isEmpty(mQueryString) || mShowEmptyListForEmptyQuery);

			refreshContactsLv();
		}
	}
	private TextView mSearchZero;
	private View mSearchProgress;
	//	private FrameLayout headerContainer;
	//	private TextView mSearchProgressText;
	private boolean isWaitingQuerying=false;
	public void setLoadSearchContactsState(int loadSearchContactsState){

	}

	//	/** Show or hide the directory-search progress spinner. */
	private void showSearchProgress(boolean show) {
		if (mSearchProgress != null) {
			mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onContactsLoadSuccess() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onContactsLoadSuccess");
		setLoadSearchContactsState(2);
		Log.d(TAG,"isWaitingQuerying:"+isWaitingQuerying);

		if(isWaitingQuerying){
			setQueryStringMst(mQueryString);
			isWaitingQuerying=false;
			//			mSearchZero.setVisibility(View.GONE);
			showSearchProgress(false);
		}
		//		showSearchProgress(false);
	}

	@Override
	public void onContactsLoadFailed() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onContactsLoadFailed");
		setLoadSearchContactsState(0);
		if(isWaitingQuerying){
			setQueryStringMst(mQueryString);
			isWaitingQuerying=false;
			//			mSearchZero.setVisibility(View.GONE);
			showSearchProgress(false);
		}
	}
	private OnContactsChanged onContactsChanged=new OnContactsChanged() {

		@Override
		public void onContactsChanged() {
			// TODO Auto-generated method stub
			Log.d(TAG,"onContactsChanged");
			setQueryStringMst(mQueryString);
		}
	};
	public void setShowEmptyListForNullQuery(boolean show) {
		mShowEmptyListForEmptyQuery = show;
	}

	public int getDirectoryLoaderId() {
		return DIRECTORY_LOADER_ID;
	}

	public int getDirectorySearchMode() {
		return mDirectorySearchMode;
	}

	public void setDirectorySearchMode(int mode) {
		mDirectorySearchMode = mode;
	}

	public boolean isLegacyCompatibilityMode() {
		return mLegacyCompatibility;
	}

	public void setLegacyCompatibilityMode(boolean flag) {
		mLegacyCompatibility = flag;
	}

	protected int getContactNameDisplayOrder() {
		return mDisplayOrder;
	}

	protected void setContactNameDisplayOrder(int displayOrder) {
		mDisplayOrder = displayOrder;
		if (mAdapter != null) {
			mAdapter.setContactNameDisplayOrder(displayOrder);
		}
	}

	public int getSortOrder() {
		return mSortOrder;
	}

	public void setSortOrder(int sortOrder) {
		mSortOrder = sortOrder;
		if (mAdapter != null) {
			mAdapter.setSortOrder(sortOrder);
		}
	}

	public void setDirectoryResultLimit(int limit) {
		mDirectoryResultLimit = limit;
	}

	protected boolean loadPreferences() {
		boolean changed = false;
		if (getContactNameDisplayOrder() != mContactsPrefs.getDisplayOrder()) {
			setContactNameDisplayOrder(mContactsPrefs.getDisplayOrder());
			changed = true;
		}

		if (getSortOrder() != mContactsPrefs.getSortOrder()) {
			setSortOrder(mContactsPrefs.getSortOrder());
			changed = true;
		}

		return changed;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		onCreateView(inflater, container);
		/// M: should create list Adapter here for child class init the right status.
		mAdapter = createListAdapter();

		boolean searchMode = isSearchMode();
		mAdapter.setSearchMode(searchMode);
		mAdapter.configureDefaultPartition(false, searchMode);
		mAdapter.setPhotoLoader(mPhotoManager);
		Log.d(TAG,"setAdapter");

		if(mListView!=null){
			mListView.setAdapter(mAdapter);

			if (!isSearchMode()) {
				mListView.setFocusableInTouchMode(true);
				mListView.requestFocus();
			}
		}

		getAdapter().setFragmentRootView(getView());
		return mView;
	}

	protected View emptyView;
	protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
		//modify by lgy for 3408038
		try {
			mView = inflateView(inflater, container);
		} catch(Exception e) {
			e.printStackTrace();
			if (getActivity() != null) {
				getActivity().finish();
			}
			return;
		}

		createView();

	}

	private void createView(){
		mListView = (ListView)mView.findViewById(android.R.id.list);
		Log.d(TAG,"createView(),mListView:"+mListView);
		if (mListView == null) {
			throw new RuntimeException(
					"Your content must have a ListView whose id attribute is " +
					"'android.R.id.list'");
		}

		View emptyView = mView.findViewById(android.R.id.empty);
		if (emptyView != null) {
			Log.d(TAG,"setEmptyView");
			mListView.setEmptyView(emptyView);
		}

		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnFocusChangeListener(this);
		mListView.setOnTouchListener(this);
		//		mListView.setFastScrollEnabled(!isSearchMode());
		mListView.setFastScrollEnabled(false);

		// Tell list view to not show dividers. We'll do it ourself so that we can *not* show
		// them when an A-Z headers is visible.
		mListView.setDividerHeight(0);

		// We manually save/restore the listview state
		mListView.setSaveEnabled(false);

		configureVerticalScrollbar();

		configurePhotoLoader();

		getAdapter().setFragmentRootView(getView());

		ContactListViewUtils.applyCardPaddingToView(getResources(), mListView, mView);
		mListView.setVisibility(View.VISIBLE);
		mListView.setOnScrollListener(this);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (getActivity() != null && getView() != null && !hidden) {
			// If the padding was last applied when in a hidden state, it may have been applied
			// incorrectly. Therefore we need to reapply it.
			ContactListViewUtils.applyCardPaddingToView(getResources(), mListView, getView());
		}
	}

	protected void configurePhotoLoader() {
		if (isPhotoLoaderEnabled() && mContext != null) {
			if (mPhotoManager == null) {
				mPhotoManager = ContactPhotoManager.getInstance(mContext);
			}			
			if (mAdapter != null) {
				mAdapter.setPhotoLoader(mPhotoManager);
			}
		}
	}

	protected void configureAdapter() {
		if (mAdapter == null) {
			return;
		}

		mAdapter.setQuickContactEnabled(mQuickContactEnabled);
		mAdapter.setAdjustSelectionBoundsEnabled(mAdjustSelectionBoundsEnabled);
		mAdapter.setIncludeProfile(mIncludeProfile);
		mAdapter.setQueryString(mQueryString);
		mAdapter.setDirectorySearchMode(mDirectorySearchMode);
		mAdapter.setPinnedPartitionHeadersEnabled(false);
		mAdapter.setContactNameDisplayOrder(mDisplayOrder);
		mAdapter.setSortOrder(mSortOrder);
		mAdapter.setSectionHeaderDisplayEnabled(mSectionHeaderDisplayEnabled);
		mAdapter.setSelectionVisible(mSelectionVisible);
		mAdapter.setDirectoryResultLimit(mDirectoryResultLimit);
		mAdapter.setDarkTheme(mDarkTheme);
		mAdapter.setmCallbacks(mCallbacks);
		mAdapter.setForContactsChoice(isForContactsChoice);
	}



	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//		if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
		//			mPhotoManager.pause();
		//		} else if (isPhotoLoaderEnabled()) {
		//			mPhotoManager.resume();
		//		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		hideSoftKeyboard();

		int adjPosition = position - mListView.getHeaderViewsCount();
		if (adjPosition >= 0) {
			onItemClick(adjPosition, id);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		int adjPosition = position - mListView.getHeaderViewsCount();

		if (adjPosition >= 0) {
			return onItemLongClick(adjPosition, id);
		}
		return false;
	}

	private void hideSoftKeyboard() {
		// Hide soft keyboard, if visible
		InputMethodManager inputMethodManager = (InputMethodManager)
				mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
	}

	/**
	 * Dismisses the soft keyboard when the list takes focus.
	 */
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (view == mListView && hasFocus) {
			hideSoftKeyboard();
		}
	}

	/**
	 * Dismisses the soft keyboard when the list is touched.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view == mListView) {
			hideSoftKeyboard();
		}
		return false;
	}

	@Override
	public void onPause() {
		Log.d(TAG,"onPause");
		super.onPause();
		removePendingDirectorySearchRequests();
	}

	/**
	 * Restore the list state after the adapter is populated.
	 */
	protected void completeRestoreInstanceState() {
		if (mListState != null) {
			mListView.onRestoreInstanceState(mListState);
			Log.d(TAG, "completeRestoreInstanceState(),the Activity may be killed." +
					"Restore the listview state last");
			mListState = null;
		}
	}

	public void setDarkTheme(boolean value) {
		mDarkTheme = value;
		if (mAdapter != null) mAdapter.setDarkTheme(value);
	}

	/**
	 * Processes a result returned by the contact picker.
	 */
	public void onPickerResult(Intent data) {
		throw new UnsupportedOperationException("Picker result handler is not implemented.");
	}

	private ContactsPreferences.ChangeListener mPreferencesChangeListener =
			new ContactsPreferences.ChangeListener() {
		@Override
		public void onChange() {
			Log.d(TAG,"ContactsPreferences onChange");
			loadPreferences();
			reloadData();
		}
	};

	private int getDefaultVerticalScrollbarPosition() {
		final Locale locale = Locale.getDefault();
		final int layoutDirection = TextUtils.getLayoutDirectionFromLocale(locale);
		switch (layoutDirection) {
		case View.LAYOUT_DIRECTION_RTL:
			return View.SCROLLBAR_POSITION_LEFT;
		case View.LAYOUT_DIRECTION_LTR:
		default:
			return View.SCROLLBAR_POSITION_RIGHT;
		}
	}


	/**
	 * M: in some case,restore the ListView last user will replace the new state.
	 * so should provider this function to child class to prevent restore state.
	 */
	protected void clearListViewLastState() {
		Log.d(TAG, "#clearListViewLastState()");
		mListState = null;
	}

	/**
	 * M: By default we show SDN Number in screen.
	 */
	public boolean isShowSdnNumber() {
		return true;
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}

	//	/**  
	//	 * 获取汉字串拼音，英文字符不变  
	//	 * @param chinese 汉字串  	
	//	 * @return 汉语拼音  
	//	 */   
	//	private HanyuPinyinOutputFormat format =null;
	//	private StringBuilder quanPinyinBuilder=new StringBuilder();
	//	private StringBuilder jianPinyinBuilder=new StringBuilder();
	//	public void getFullSpell(String chinese) {
	//		if(TextUtils.isEmpty(chinese)) return;
	//		quanPinyinBuilder=new StringBuilder();
	//		jianPinyinBuilder=new StringBuilder();
	//		StringBuffer pybf = new StringBuffer();   
	//		char[] arr = chinese.toCharArray();    
	//		for (int i = 0; i < arr.length; i++) {   
	//			if (arr[i] > 128) {   
	//				try {   
	//					String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], format);   
	//					if (temp != null) {   						
	//						quanPinyinBuilder.append(temp[0]); 
	//						jianPinyinBuilder.append(temp[0].charAt(0)); 
	//					}   
	//
	//				} catch (BadHanyuPinyinOutputFormatCombination e) {   
	//					e.printStackTrace();   
	//				}   
	//			} else {   
	//				quanPinyinBuilder.append(arr[i]);   
	//				jianPinyinBuilder.append(arr[i]); 
	//			}   
	//		}   
	//	}  

	//add by liyang	
	private enum TaskStatus {
		NEW, RUNNING, FINISHED, CANCELED
	}

	private class InitIndexBarTask extends AsyncTask<Void, Void, Boolean> {
		private InitIndexBarTask mInstance = null;
		private TaskStatus mTaskStatus;
		private boolean mResult;
		private int flag;
		private String str_other = mContext.getString(R.string.mst_other_string);

		private InitIndexBarTask(int flag) {
			super();
			mTaskStatus = TaskStatus.NEW;
			this.flag=flag;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mTaskStatus = TaskStatus.CANCELED;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG,"onPostExecute");
			mIndexBar.invalidate();
			//			mIndexBar.setVisibility(result?View.VISIBLE:View.GONE);

			super.onPostExecute(result);

		}

		private String getJianPinyin(String[] pinyinArray) {
			StringBuilder sb = new StringBuilder();
			for (String p : pinyinArray) {
				sb.append(p.charAt(0));
			}
			return sb.toString().toLowerCase();
		}
		public String getQuanPinyin(String[] pinyinArray) {
			if (null != pinyinArray) {
				StringBuilder sb = new StringBuilder();
				for (String str : pinyinArray) {
					sb.append(str);
				}
				return sb.toString();
			}
			return "";
		}

		@Override
		protected Boolean doInBackground(Void... params) {			
			if(allCursor==null||allCursor.getCount()==0) return false;
			long time1=System.currentTimeMillis();
			final Cursor data=allCursor;
			Log.d(TAG,"doInBackground,data:"+data);
			contactForIndexs=new ArrayList<ContactForIndex>();
			indexHashMap=new HashMap<String, Integer>();
			indexArrayList=new ArrayList<Integer>();
			data.moveToFirst();
			String last = null;
			//			StringBuilder sb,sb1;
			//			Token token;
			//			ArrayList<Token> tokens;
			String pinyin="*";
			String jian="*";
			String name;
			ContactForIndex contactForIndex;
			int phonebook_bucket;
			String fistLetter;
			ContactForIndex g;
			int indexCount=0;
			boolean hasAddOther=false;
			int nameColumnIndex=flag==1?ContactQuery.CONTACT_DISPLAY_NAME:4;
			int quanpinyinColumnIndex=flag==1?ContactQuery.QUAN_PINYIN:12;
			int jianpinyinColumnIndex=flag==1?ContactQuery.JIAN_PINYIN:13;
			int phonebookbucketColumnIndex=flag==1?ContactQuery.PHONEBOOK_BUCKET:14;
			for(int i=0;i<allCount-mStarredCount;i++){
				contactForIndex=new ContactForIndex();
				if(data.isAfterLast()) break;
				name=data.getString(nameColumnIndex);
				String tempPinyin=data.getString(quanpinyinColumnIndex);
				String tempJian=data.getString(jianpinyinColumnIndex);
				//				Log.d(TAG, "name:"+name+" pinyin:"+tempPinyin+" jian:"+tempJian);
				if(!TextUtils.isEmpty(tempPinyin)) {
					tempPinyin=tempPinyin.toUpperCase();
					pinyin=tempPinyin;
				}
				if(!TextUtils.isEmpty(tempJian)) {
					tempJian=tempJian.toUpperCase();
					jian=tempJian;
				}
				//				if(TextUtils.isEmpty(pinyin))
				//				tokens = HanziToPinyin.getInstance().getTokens(name);
				//				sb=new StringBuilder();
				//				sb1=new StringBuilder();
				//				for (int k = 0;k<2&&k<tokens.size(); k++) {
				//					token = tokens.get(k);
				//					sb.append(token.target);     
				//					sb1.append(token.target.substring(0,1));
				//				}
				//				//				getFullSpell(name);
				//
				//				//				if(quanPinyinBuilder.toString().length()>0) pinyin=quanPinyinBuilder.toString().toUpperCase();
				//				//				if(jianPinyinBuilder.toString().length()>0) jian=jianPinyinBuilder.toString().toUpperCase();
				//
				//				//				Log.d(TAG,"name:"+name+" pinyin:"+pinyin+" jian:"+jian);
				//				
				//				if(sb.length()>0) pinyin=sb.toString().toUpperCase();
				//				if(sb1.length()>0) jian=sb1.toString().toUpperCase();


				contactForIndex.name=name;
				contactForIndex.pinyin=pinyin;

				phonebook_bucket=data.getInt(phonebookbucketColumnIndex);
				//				Log.d(TAG,"phonebook_label:"+phonebook_label);
				if(phonebook_bucket<27){
					for (int k = 0;k<jian.length(); k++) {
						fistLetter = jian.substring(k,k+1);
						//						Log.d(TAG,"firstLetter:"+fistLetter);
						if(k == 0){
							if(!fistLetter.equals(last)){
								g = new ContactForIndex();
								//								if(isLetter(fistLetter)) {
								//									g.name = fistLetter;
								//									g.type = 1;
								//								}else{
								//									g.name = str_other;
								//									g.type = 1;
								//									fistLetter = str_other;
								//								}
								g.name = fistLetter;
								g.type = 1;
								contactForIndexs.add(g);
								indexHashMap.put(fistLetter,indexCount);
								indexArrayList.add(i+indexCount);
								indexCount++;
							}
							last = fistLetter;
						}
						contactForIndex.firstLetter.add(fistLetter);
					}
				}else{
					if(!hasAddOther){
						g = new ContactForIndex();
						g.name = str_other;
						g.type = 1;
						contactForIndexs.add(g);
						hasAddOther=true;
					}
					contactForIndex.firstLetter.add(str_other);
				}

				contactForIndexs.add(contactForIndex);
				data.moveToNext();
			}
			Log.d(TAG,"spend1:"+(System.currentTimeMillis()-time1));

			initIndexBar(contactForIndexs);
			Log.d(TAG,"spend2:"+(System.currentTimeMillis()-time1));

			return true;

		}

		public boolean isTaskRunning() {
			return mTaskStatus == TaskStatus.RUNNING;
		}

		public boolean getResult() {
			return mResult;
		}

		public boolean isTaskFinished() {
			return mTaskStatus == TaskStatus.FINISHED;
		}

		public void abort() {
			if (mInstance != null) {
				Log.d(TAG, "mInstance.cancel(true)");
				mInstance.cancel(true);
				mInstance = null;
			}
		}

		public InitIndexBarTask createNewTask() {
			if (mInstance != null) {
				Log.d(TAG, "cancel existing task instance");
				mInstance.abort();
			}
			mInstance = new InitIndexBarTask(1);
			return mInstance;
		}

		public InitIndexBarTask getExistTask() {
			return mInstance;
		}

	}


	private InitIndexBarTask mTask;


	@Override
	public void onStateChanged(MstIndexBar.TouchState old, MstIndexBar.TouchState news) {
		Log.d(TAG,"Touch state : "+news);
		if(mCallbacks==null) return;
		if(getAdapter().isSelectMode()) return;
		if(TextUtils.equals("DOWN", news.toString())){
			mCallbacks.onFragmentCallback(FragmentCallbacks.SHOW_ADD_FAB, 0);
		}else if(TextUtils.equals("UP", news.toString())){
			mCallbacks.onFragmentCallback(FragmentCallbacks.SHOW_ADD_FAB, 1);
		}
	}



	@Override
	public void onSelect(int index, int layer, MstIndexBar.Letter letter) {
		Log.d(TAG,"onSelect0,index:"+index+" layer:"+layer+" letter:"+letter.text);
		if(index==0&&layer == 0){
			getListView().setSelection(0);
			return;
		}
		int listindex = letter.list_index;
		Log.d(TAG,"listindex0:"+listindex);
		listindex--;
		if(layer == 0){
		}
		int offset=0;
		//		if(indexHashMap!=null&&letter.text!=null&&indexHashMap.get(letter.text)!=null){
		//			offset=indexHashMap.get(letter.text);
		//		}
		for(int k:indexArrayList){
			if(k<listindex) offset++;
			if(k>=listindex) break;
		}
		Log.d(TAG,"onSelect,index:"+index+" letter:"+letter.text+" listindex:"+listindex+" offset:"+offset
				+" indexArrayList:"+indexArrayList+"getListView().getHeaderViewsCount():"+getListView().getHeaderViewsCount());

		getListView().setSelectionFromTop(listindex+getListView().getHeaderViewsCount()-offset,
				layer==0?-DensityUtil.dip2px(getContext(),9):0);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		//		Placement placement = getAdapter().getItemPlacementInSection(firstVisibleItem);
		//		String fir=placement.sectionHeader;
		//		Log.d(TAG,"onScroll,firstVisibleItem:"+firstVisibleItem);

		if(mIndexBar==null) return;
		if(firstVisibleItem<2&&mStarredCount==0) {
			firstVisibleItem=2;
		}
		if(firstVisibleItem<2) {
			mIndexBar.setFocus(0);
			return;
		}
		int position=firstVisibleItem-2;
		int section = getAdapter().getSectionForPosition(position);
		if(section<0) return;
		String fir= (String)getAdapter().getSections()[section];
		Log.d(TAG,"onScroll,position:"+position+" section:"+section+" fir:"+fir);

		int index = -1;
		index = mIndexBar.getIndex(fir);
		//		if (contact.firstLetter != null && contact.firstLetter.size() > 0) {
		//			String fir = contact.firstLetter.get(0);
		//			index = mIndexBar.getIndex(fir);
		//		} else {
		//			String fir = contact.name;
		//			index = mIndexBar.getIndex(fir);
		//		}

		if(index == -1){
			index = mIndexBar.size() - 1;
		}

		Log.d(TAG,"onScroll,index:"+index);
		mIndexBar.setFocus(index);
	}


	private void initIndexBar(ArrayList<ContactForIndex> array){
		//		Log.d(TAG,"initIndexBar1:"+array);
		//		for(ContactForIndex item:array){
		//			Log.d(TAG,"item:"+item.toString());
		//		}
		if(array==null) return;
		for(int m=0;m<28;m++){
			mIndexBar.setEnables(false,m);
		}
		if(mStarredCount>0) mIndexBar.setEnables(true,0);

		List<MstIndexBar.Letter> sub = null;
		String last = "";
		int lastindex = -1;
		int otherindex = -1;
		boolean changed = false;
		ContactForIndex c=null;
		int namesize;
		int index;
		String firletter=null,secletter=null;
		Letter letter=null;
		Letter letter2=null;
		for(int p=0;p<array.size();p++){
			c = (ContactForIndex) array.get(p);
			namesize = c.firstLetter.size();
			firletter = "";
			secletter = "";
			for(int i=0;i<namesize;i++) {
				if(i == 0) {
					firletter = c.firstLetter.get(0);
					changed = !firletter.equals(last);
					last = firletter;
				}else if(i == 1){
					secletter = c.firstLetter.get(1);
				}
			}
			if(changed){
				if(sub != null && lastindex != -1){
					//					Log.d(TAG,"setSubList1,,lastindex:"+lastindex+" sub:"+sub);
					//					for(Letter letter:sub) {
					//						Log.d(TAG,"setSubList1 letter:"+letter.text);
					//					}
					//					Collections.sort(sub, mLetterComparator);
					mIndexBar.setSubList(lastindex,sub);
				}
				if(!"".equals(firletter)) {
					index = mIndexBar.getIndex(firletter);
					if(index != -1) {
						lastindex = index;
						sub = new ArrayList<>();
					}else{
						sub = null;
					}
					if(index == -1){//其他（#）的索引
						index = 27;
						if(otherindex == -1){
							otherindex = p;
						}
					}
					//设置第一个字母对应的列表索引
					letter = mIndexBar.getLetter(index);
					if(letter != null){
						//						Log.d(TAG,"index:"+index+" letter:"+letter.text+" list_index:"+p);
						letter.list_index = /*index == 1 ? otherindex : */p;
					}
					mIndexBar.setEnables(true,index);
				}
			}
			//设置第二个字母的列表索引
			if(sub != null && secletter != "") {
				if(!sub.contains(Letter.valueOf(secletter))) {
					letter2 = Letter.valueOf(secletter);
					letter2.enable = true;
					letter2.list_index = p;
					sub.add(letter2);
				}
			}
		}
		if(sub != null && lastindex != -1){
			Log.d(TAG,"setSubList2,lastindex:"+lastindex+" sub:"+sub);

			//			Collections.sort(sub, mLetterComparator);
			mIndexBar.setSubList(lastindex,sub);
		}
	}

	//	/** 
	//	 * 按字母表对Collection列表进行排序 
	//	 * */  
	//	public class AlphabetComparator implements Comparator<MstIndexBar.Letter> {
	//
	//		// java提供的对照器  
	//		//		private RuleBasedCollator collator = null;  
	//
	//		/** 
	//		 * 默认构造器是按中文字母表进行排序 
	//		 * */  
	//		public AlphabetComparator() {  
	//			//			collator = (RuleBasedCollator) Collator  
	//			//					.getInstance(java.util.Locale.CHINA);  
	//		}  
	//
	//		/** 
	//		 * 可以通过传入Locale值实现按不同语言进行排序 
	//		 * */  
	//		public AlphabetComparator(Locale locale) {  
	//			//			collator = (RuleBasedCollator) Collator.getInstance(locale);  
	//		}  
	//
	//		public int compare(MstIndexBar.Letter obj1, MstIndexBar.Letter obj2) {  
	//			//			CollationKey c1 = collator.getCollationKey(obj1.text);  
	//			//			CollationKey c2 = collator.getCollationKey(obj2.text);  
	//			//
	//			//			return collator.compare(((CollationKey) c1).getSourceString(),  
	//			//					((CollationKey) c2).getSourceString());  
	//			char c1 = obj1.text.charAt(0);
	//			char c2 = obj2.text.charAt(0);
	//			return c1-c2;
	//		}  
	//	}
}