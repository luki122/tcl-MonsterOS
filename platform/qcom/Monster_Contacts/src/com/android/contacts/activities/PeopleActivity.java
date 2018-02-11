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

package com.android.contacts.activities;

import com.android.contacts.common.list.ShortcutIntentBuilder;
import android.view.ViewTreeObserver;
import com.mst.t9search.ContactsHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import mst.app.dialog.ProgressDialog;

import com.mediatek.contacts.util.ContactsIntent;
import com.intsig.openapilib.OpenApi;
import com.mediatek.storage.StorageManagerEx;
import mst.widget.FloatingActionButton.OnFloatActionButtonClickListener;
import mst.widget.FloatingActionButton;
import mst.widget.toolbar.Toolbar;
import android.widget.MstSearchView;
import android.widget.MstSearchView.OnQueryTextListener;
import android.widget.MstSearchView.OnCloseListener;
import android.widget.MstSearchView.OnSuggestionListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.MstIndexBar;
import mst.widget.ActionMode.Item;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import android.R.integer;
import android.accounts.Account;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.os.UserManager;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import mst.provider.ContactsContract;
import android.provider.MediaStore;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Groups;
import mst.provider.ContactsContract.ProviderStatus;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Choreographer.FrameCallback;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.activities.ActionBarAdapter.Listener.Action;
import com.android.contacts.activities.PeopleActivity.ContactSaveCompletedReceiver;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.dialog.ClearFrequentsDialog;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.list.ContactEntryListFragment;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.ContactTileAdapter.DisplayType;
import com.android.contacts.interactions.ContactMultiDeletionInteraction;
import com.android.contacts.interactions.ContactMultiDeletionInteraction.MultiContactDeleteListener;
import com.android.contacts.interactions.JoinContactsDialogFragment;
import com.android.contacts.interactions.JoinContactsDialogFragment.JoinContactsListener;
import com.android.contacts.list.MultiSelectContactsListFragment;
import com.android.contacts.list.MultiSelectContactsListFragment.OnCheckBoxListActionListener;
import com.android.contacts.list.ContactTileListFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.ContactsUnavailableFragment;
import com.android.contacts.common.list.DirectoryListLoader;
import com.android.contacts.common.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.list.OnContactBrowserActionListener;
import com.android.contacts.list.OnContactsUnavailableActionListener;
import com.android.contacts.list.ProviderStatusWatcher;
import com.android.contacts.list.ProviderStatusWatcher.ProviderStatusListener;
import com.android.contacts.mst.MstBusinessCardResults;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.list.ViewPagerTabs;
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.common.util.AccountFilterUtil;
import com.android.contacts.common.util.ViewUtil;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.util.AccountPromptUtils;
import com.android.contacts.common.util.Constants;
import com.android.contacts.common.vcard.ExportVCardActivity;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.util.DialogManager;
import com.android.contactsbind.HelpUtils;
import com.android.contacts.util.PhoneCapabilityTester;
import com.mediatek.contacts.ContactsApplicationEx;
import com.mediatek.contacts.ContactsSystemProperties;
import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.model.AccountTypeManagerEx;
import com.mediatek.contacts.simcontact.BootCmpReceiver;
import com.mediatek.contacts.util.PDebug;
//import com.mediatek.contacts.util.SetIndicatorUtils;
import com.mediatek.contacts.util.VolteUtils;
//import com.mediatek.contacts.vcs.VcsController;
//import com.mediatek.contacts.vcs.VcsUtils;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.activities.GroupBrowseActivity;
import com.mediatek.contacts.activities.ActivitiesUtils;

import com.mediatek.contacts.list.DropMenu;
import com.mediatek.contacts.list.DropMenu.DropDownMenu;
import com.mediatek.contacts.simcontact.SlotUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.mst.MstBusinessCardResults;
import android.accounts.Account;
import com.android.vcard.VCardEntry;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.android.vcard.VCardEntryCommitter;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.mst.AddressBean;
import com.android.contacts.mst.MainActivity;
import com.android.contacts.mst.MstVcfUtils;
import com.android.contacts.mst.TestActivity;
import com.intsig.openapilib.OpenApi;
import com.intsig.openapilib.OpenApiParams;
import com.mediatek.contacts.ExtensionManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays a list to browse contacts.
 */
public class PeopleActivity extends ContactsActivity implements
View.OnCreateContextMenuListener, View.OnClickListener,
ActionBarAdapter.Listener, DialogManager.DialogShowingViewActivity,
ContactListFilterController.ContactListFilterListener,
ProviderStatusListener, MultiContactDeleteListener,
JoinContactsListener, FragmentCallbacks,
mst.widget.toolbar.Toolbar.OnMenuItemClickListener
{
	private MstIndexBar mIndexBar;
	private static final String TAG = "PeopleActivity";
	private static final String SEARCH_BEGIN_STRING = "mst_querystring_for_contact_search_begin";
	private static final String ENABLE_DEBUG_OPTIONS_HIDDEN_CODE = "debug debug!";

	// These values needs to start at 2. See {@link ContactEntryListFragment}.
	private static final int SUBACTIVITY_ACCOUNT_FILTER = 2;

	private final DialogManager mDialogManager = new DialogManager(this);

	private ContactsIntentResolver mIntentResolver;
	private ContactsRequest mRequest;

	private ActionBarAdapter mActionBarAdapter;
	private FloatingActionButtonController mFloatingActionButtonController;
	private FloatingActionButton mFloatingActionButtonContainer;
	private boolean wasLastFabAnimationScaleIn = false;

	private ContactTileListFragment.Listener mFavoritesFragmentListener = new StrequentContactListFragmentListener();

	private ContactListFilterController mContactListFilterController;

	private ContactsUnavailableFragment mContactsUnavailableFragment;
	private ProviderStatusWatcher mProviderStatusWatcher;
	private Integer mProviderStatus;
	private boolean mOptionsMenuContactsAvailable;

	/**
	 * Showing a list of Contacts. Also used for showing search results in
	 * search mode.
	 */
	private MultiSelectContactsListFragment mAllFragment;
	private ContactTileListFragment mFavoritesFragment;

	/** ViewPager for swipe */
	private ViewPager mTabPager;
	private ViewPagerTabs mViewPagerTabs;
	private TabPagerAdapter mTabPagerAdapter;
	private String[] mTabTitles;
	private final TabPagerListener mTabPagerListener = new TabPagerListener();

	private boolean mEnableDebugMenuOptions;

	/**
	 * True if this activity instance is a re-created one. i.e. set true after
	 * orientation change. This is set in {@link #onCreate} for later use in
	 * {@link #onStart}.
	 */
	private boolean mIsRecreatedInstance;

	/**
	 * If {@link #configureFragments(boolean)} is already called. Used to avoid
	 * calling it twice in {@link #onStart}. (This initialization only needs to
	 * be done once in onStart() when the Activity was just created from scratch
	 * -- i.e. onCreate() was just called)
	 */
	private boolean mFragmentInitialized;

	/**
	 * This is to disable {@link #onOptionsItemSelected} when we trying to stop
	 * the activity.
	 */
	private boolean mDisableOptionItemSelected;

	/** Sequential ID assigned to each instance; used for logging */
	private final int mInstanceId;
	private static final AtomicInteger sNextInstanceId = new AtomicInteger();
	private BottomNavigationView bottomBar;
	private ActionMode actionMode;

	public PeopleActivity() {
		Log.d(TAG, "[PeopleActivity]new");
		mInstanceId = sNextInstanceId.getAndIncrement();
		mIntentResolver = new ContactsIntentResolver(this);
		/** M: Bug Fix for ALPS00407311 @{ */
		mProviderStatusWatcher = ProviderStatusWatcher
				.getInstance(ContactsApplicationEx.getContactsApplication());
		/** @} */
	}

	@Override
	public String toString() {
		// Shown on logcat
		return String.format("%s@%d", getClass().getSimpleName(), mInstanceId);
	}

	public boolean areContactsAvailable() {
		Log.d(TAG, "[areContactsAvailable]mProviderStatus = " + mProviderStatus);
		return ((mProviderStatus != null) && mProviderStatus
				.equals(ProviderStatus.STATUS_NORMAL))
				|| ExtensionManager.getInstance().getOp01Extension()
				.areContactAvailable(mProviderStatus);
	}

	private boolean areContactWritableAccountsAvailable() {
		return ContactsUtils.areContactWritableAccountsAvailable(this);
	}

	private boolean areGroupWritableAccountsAvailable() {
		return ContactsUtils.areGroupWritableAccountsAvailable(this);
	}

	/**
	 * Initialize fragments that are (or may not be) in the layout.
	 * 
	 * For the fragments that are in the layout, we initialize them in
	 * {@link #createViewsAndFragments(Bundle)} after inflating the layout.
	 * 
	 * However, the {@link ContactsUnavailableFragment} is a special fragment
	 * which may not be in the layout, so we have to do the initialization here.
	 * 
	 * The ContactsUnavailableFragment is always created at runtime.
	 */
	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.d(TAG, "[onAttachFragment]");
		if (fragment instanceof ContactsUnavailableFragment) {
			mContactsUnavailableFragment = (ContactsUnavailableFragment) fragment;
			mContactsUnavailableFragment
			.setOnContactsUnavailableActionListener(new ContactsUnavailableFragmentListener());
		}
	}

	private void getDisplayInfomation() {
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getRealSize(point);
		Log.d(TAG, "the screen size is " + point.toString());
	}

	private void getDensity() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		Log.d(TAG, "Density is " + displayMetrics.density + " densityDpi is "
				+ displayMetrics.densityDpi + " height: "
				+ displayMetrics.heightPixels + " width: "
				+ displayMetrics.widthPixels);
	}

	private void getScreenSizeOfDevice() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		double x = Math.pow(width, 2);
		double y = Math.pow(height, 2);
		double diagonal = Math.sqrt(x + y);

		int dens = dm.densityDpi;
		double screenInches = diagonal / (double) dens;
		Log.d(TAG, "The screenInches " + screenInches);
	}

	private void getScreenSizeOfDevice2() {
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getRealSize(point);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		double x = Math.pow(point.x / dm.xdpi, 2);
		double y = Math.pow(point.y / dm.ydpi, 2);
		double screenInches = Math.sqrt(x + y);
		Log.d(TAG, "dm.xdpi:" + dm.xdpi + " dm.ydpi:" + dm.ydpi
				+ " Screen inches : " + screenInches);
	}
	//	Handler handler=new Handler(); 
	//	Runnable runnable=new Runnable(){  
	//		@Override  
	//		public void run() {  
	//			Log.d(TAG,"runnable run");
	//			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PeopleActivity.this);
	//			OpenApi openApi = OpenApi.instance("SAU6QXVYNdXJHL6ateEtBy4T");
	//			Log.d(TAG,"openApi:"+openApi);
	//			
	////			prefs.edit().putInt("hasValidateBcrApi", 1).commit();
	//
	//		}   
	//	}; 

	public boolean isNetworkConnected(){
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
		if (manager == null) {
			return false;
		}
		//        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
		//        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
		NetworkInfo activeInfo = manager.getActiveNetworkInfo();  
		if(activeInfo != null && activeInfo.isConnected()) {
			Log.d(TAG, "wifi connected");
			return true;
		}
		return false;
	}
	@Override
	protected void onCreate(Bundle savedState) {
		Log.i(TAG, "[onCreate]");
		super.onCreate(savedState);

		if (RequestPermissionsActivity.startPermissionActivity(this)) {
			Log.i(TAG, "[onCreate]startPermissionActivity,return.");
			return;
		}


		// / M: Add for ALPS02383518, when BootCmpReceiver received PHB_CHANGED
		// intent but has no
		// READ_PHONE permission, marked NEED_REFRESH_SIM_CONTACTS as true. So
		// refresh
		// all SIM contacts after open all permission and back to contacts at
		// here. @{
		boolean needRefreshSIMContacts = getSharedPreferences(getPackageName(),
				Context.MODE_PRIVATE).getBoolean(
						BootCmpReceiver.NEED_REFRESH_SIM_CONTACTS, false);
		if (needRefreshSIMContacts) {
			Log.d(TAG, "[onCreate] refresh all SIM contacts");
			Intent intent = new Intent(
					BootCmpReceiver.ACTION_REFRESH_SIM_CONTACT);
			sendBroadcast(intent);
		}
		// / @}

		Intent intent=getIntent();
		if (!processIntent(false)) {
			finish();
			Log.w(TAG, "[onCreate]can not process intent:" + intent);
			return;
		}

		Log.d(TAG, "[Performance test][Contacts] loading data start time: ["
				+ System.currentTimeMillis() + "]");

		mContactListFilterController = ContactListFilterController
				.getInstance(this);
		mContactListFilterController.checkFilterValidity(false);
		mContactListFilterController.addListener(this);

		mProviderStatusWatcher.addListener(this);

		mIsRecreatedInstance = (savedState != null);

		PDebug.Start("createViewsAndFragments");

		createViewsAndFragments(savedState);

		// /// M: Modify for SelectAll/DeSelectAll Feature. @{
		// Button selectcount = (Button) mActionBarAdapter.mSelectionContainer
		// .findViewById(R.id.selection_count_text);
		// selectcount.setOnClickListener(this);
		// /// @}
		// getWindow().setBackgroundDrawable(null);

		/**
		 * M: For plug-in @{ register context to plug-in, so that the plug-in
		 * can use host context to show dialog
		 */
		// / M: [vcs] VCS featrue. @{
		// if (VcsUtils.isVcsFeatureEnable()) {
		// Log.i(TAG, "[onCreate]init VCS");
		// mVcsController = new VcsController(this, mActionBarAdapter,
		// mAllFragment);
		// mVcsController.init();
		// }
		// / @}
		/** @} */

		// getDensity();
		// getDisplayInfomation();
		// getScreenSizeOfDevice();
		// getScreenSizeOfDevice2();
		PDebug.End("Contacts.onCreate");
		mstBusinessCardResults = new MstBusinessCardResults(
				PeopleActivity.this);
		myReceiver = new ContactSaveCompletedReceiver();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(ContactsIntent.MULTICHOICE.ACTION_MULTICHOICE_PROCESS_FINISH);
		intentFilter.addAction(BROADCASTACTION_STRING);
		registerReceiver(myReceiver, intentFilter);


		Log.d(TAG, "[onCreate],intent:"+intent+" getExtras:"+(intent==null?"null":intent.getExtras()));
		if (TextUtils.equals(intent.getStringExtra("from"), "importfromsim")) {
			int count = intent.getIntExtra("importSimContactsCount", 0);
			Toast.makeText(PeopleActivity.this, getString(R.string.mst_import_sim_contacts_result,count),
					Toast.LENGTH_LONG).show();
		}
		
		if(mContactsHelper==null) {
			mContactsHelper=new ContactsHelper();
		}
		mContactsHelper.setContext(this);
		mAllFragment.setContactsHelper(mContactsHelper);

		addOnSoftKeyBoardVisibleListener();

	}
	
	private ContactsHelper mContactsHelper;

	/**监听软键盘状态
	 * @param activity
	 * @param listener
	 */
	private boolean sLastVisiable=false;
	public void addOnSoftKeyBoardVisibleListener() {
		final View decorView = getWindow().getDecorView();
		decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if(!mActionBarAdapter.isSearchMode()) return;
				Rect rect = new Rect();
				decorView.getWindowVisibleDisplayFrame(rect);
				int displayHight = rect.bottom - rect.top;
				int hight = decorView.getHeight();
				boolean visible = (double) displayHight / hight < 0.8;

				Log.d(TAG, "DecorView display hight = " + displayHight);
				Log.d(TAG, "DecorView hight = " + hight);
				Log.d(TAG, "softkeyboard visible = " + visible);

				if(!visible && visible != sLastVisiable){
					mActionBarAdapter.mSearchView.clearFocus();
				}
				sLastVisiable = visible;
			}
		});
	}


	@Override
	protected void onNewIntent(Intent intent) {
		PDebug.Start("onNewIntent");
		setIntent(intent);
		if (!processIntent(true)) {
			finish();
			Log.w(TAG, "[onNewIntent]can not process intent:" + getIntent());
			return;
		}
		Log.d(TAG, "[onNewIntent],intent:"+intent+" getExtras:"+(intent==null?"null":intent.getExtras()));
		// mActionBarAdapter.initialize(null, mRequest);

		mContactListFilterController.checkFilterValidity(false);

		// Re-configure fragments.
		// configureFragments(true /* from request */);
		initializeFabVisibility();
		invalidateOptionsMenuIfNeeded();
		PDebug.End("onNewIntent");

		if (TextUtils.equals(intent.getStringExtra("from"), "importfromsim")) {
			int count = intent.getIntExtra("importSimContactsCount", 0);
			Toast.makeText(PeopleActivity.this, getString(R.string.mst_import_sim_contacts_result,count),
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Resolve the intent and initialize {@link #mRequest}, and launch another
	 * activity if redirect is needed.
	 * 
	 * @param forNewIntent
	 *            set true if it's called from {@link #onNewIntent(Intent)}.
	 * @return {@code true} if {@link PeopleActivity} should continue running.
	 *         {@code false} if it shouldn't, in which case the caller should
	 *         finish() itself and shouldn't do farther initialization.
	 */
	private boolean processIntent(boolean forNewIntent) {
		// Extract relevant information from the intent
		mRequest = mIntentResolver.resolveIntent(getIntent());
		// if (Log.isLoggable(TAG, Log.DEBUG)) {
		// Log.d(TAG, this + " processIntent: forNewIntent=" + forNewIntent
		// + " intent=" + getIntent() + " request=" + mRequest);
		// }
		if (!mRequest.isValid()) {
			Log.w(TAG, "[processIntent]request is inValid");
			setResult(RESULT_CANCELED);
			return false;
		}

		if (mRequest.getActionCode() == ContactsRequest.ACTION_VIEW_CONTACT) {
			Log.d(TAG, "[processIntent]start QuickContactActivity");
			final Intent intent = ImplicitIntentsUtil
					.composeQuickContactIntent(mRequest.getContactUri(),
							QuickContactActivity.MODE_FULLY_EXPANDED);
			ImplicitIntentsUtil.startActivityInApp(this, intent);
			return false;
		}
		return true;
	}

	public void updateActionMode() {
		int selected = mAllFragment.getSelectedContactIds().size();
		if (mAllFragment.isSelectedAll()) {
			actionMode
			.setPositiveText(getString(R.string.mst_actionmode_selectnone));
		} else {
			actionMode
			.setPositiveText(getString(R.string.mst_actionmode_selectall));
		}
		actionMode.setTitle(String.format(
				getString(R.string.mst_menu_actionbar_selected_items), selected));
	}

	private Toolbar toolbar;


	protected void initialWindowParams(Window window) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

	}
	public static final int UPDATE_PROGRESS_DIALOG = 300;
	public static final int SHOW_PROGRESS_DIALOG = 400;
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG,"handleMessage:"+msg.what);
			if(msg.what==UPDATE_PROGRESS_DIALOG){	
				mProgressDialog.incrementProgressBy(1);
				mProgressDialog.setMessage("正在删除 "+msg.obj.toString());
				ShortcutIntentBuilder.deleteShortCut(PeopleActivity.this,msg.obj.toString());
				if(/*msg.arg1*/selectedCount==msg.arg2&&mProgressDialog!=null){
					mProgressDialog.dismiss();
					//						Toast.makeText(mContext, getString(R.string.mst_delete_success), Toast.LENGTH_LONG).show();
				}
			}else if(msg.what==SHOW_PROGRESS_DIALOG){
				String title=((String[])msg.obj)[0];
				String message=((String[])msg.obj)[1];
				prepareProgressDialog(title, message, /*msg.arg1*/selectedCount);
			}
		}
	};

	private ProgressDialog mProgressDialog;
	private void prepareProgressDialog(String title, String message,int count) {
		if(mProgressDialog!=null) {
			mProgressDialog.dismiss();

		}
		mProgressDialog = new ProgressDialog(PeopleActivity.this);
		mProgressDialog.setTitle(title);
		mProgressDialog.setMessage(message);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setProgress(0);
		mProgressDialog.setMax(count);
		mProgressDialog.show();
		mProgressDialog.setCanceledOnTouchOutside(false);

		mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
	}

	private int selectedCount=0;
	private Runnable mRunnable=new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			deleteSelectedContacts();
			switchToEditMode(false);
		}
	};
	private Menu menu;
	private void createViewsAndFragments(Bundle savedState) {
		Log.d(TAG, "[createViewsAndFragments]");
		PDebug.Start("createViewsAndFragments, prepare fragments");
		// Disable the ActionBar so that we can use a Toolbar. This needs to be
		// called before
		// setContentView().

		setMstContentView(R.layout.people_activity);
		actionMode = getActionMode();
		Log.d(TAG, "actionMode:" + actionMode);

		actionMode.setNagativeText(getApplicationContext().getString(
				R.string.mst_cancel));
		actionMode.bindActionModeListener(new ActionModeListener() {
			/**
			 * ActionMode上面的操作按钮点击时触发，在这个回调中，默认提供两个ID使用，
			 * 确定按钮的ID是ActionMode.POSITIVE_BUTTON
			 * ,取消按钮的ID是ActionMode.NAGATIVE_BUTTON
			 * 
			 * @param view
			 */
			public void onActionItemClicked(Item item) {
				Log.d(TAG, "onActionItemClicked1,itemid:" + item.getItemId()
						+ " ActionMode.NAGATIVE_BUTTON:"
						+ ActionMode.NAGATIVE_BUTTON);
				switch (item.getItemId()) {
				case ActionMode.POSITIVE_BUTTON:
					int all = mAllFragment.getAdapter().getCount();
					// get mIsSelectedAll from fragment.
					mAllFragment.updateSelectedItemsView();
					// the menu will show "Deselect_All/ Select_All".
					if (mAllFragment.isSelectedAll()) {
						Log.d(TAG, "onActionItemClicked,1");
						mAllFragment.updateCheckBoxState(false);
						mAllFragment.displayCheckBoxesV2(false,true);
						// mActionBarAdapter.setSelectionMode(false);
						// initializeFabVisibility();
					} else {
						Log.d(TAG, "onActionItemClicked,2");
						mAllFragment.updateCheckBoxState(true);
						mAllFragment.displayCheckBoxes(true);

					}
					updateActionMode();
					break;

				case ActionMode.NAGATIVE_BUTTON:
					Log.d(TAG,
							"click nagativebutton:"
									+ mActionBarAdapter.isSelectionMode());
					if (mActionBarAdapter.isSelectionMode()) {						
						switchToEditMode(false);
					}
					break;
				default:
					break;
				}
			}

			/**
			 * ActionMode显示的时候触发
			 * 
			 * @param actionMode
			 */
			public void onActionModeShow(ActionMode actionMode) {

			}

			/**
			 * ActionMode消失的时候触发
			 * 
			 * @param actionMode
			 */
			public void onActionModeDismiss(ActionMode actionMode) {

			}
		});

		bottomBar = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
		bottomBar
		.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {

			@Override
			public boolean onNavigationItemSelected(MenuItem arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onNavigationItemSelected,arg0.getItemId():"
						+ arg0.getItemId());
				switch (arg0.getItemId()) {
				case R.id.mst_contacts_delete:
					//add by lgy for 3471723
					selectedCount=mAllFragment.getSelectedContactIds().size();
					if(selectedCount == 0) {
						Toast.makeText(PeopleActivity.this,
								R.string.no_contact_selected, Toast.LENGTH_SHORT)
								.show();
						return false;
					}
					mHandler.removeCallbacks(mRunnable);
					mHandler.postDelayed(mRunnable, 300);
					break;

					// case R.id.mst_contacts_share:
						// shareSelectedContacts();
					// break;
				default:
					break;
				}
				return false;
			}
		});

		final FragmentManager fragmentManager = getFragmentManager();

		// Hide all tabs (the current tab will later be reshown once a tab is
		// selected)
		final FragmentTransaction transaction = fragmentManager
				.beginTransaction();

		/*
		 * mTabTitles = new String[TabState.COUNT];
		 * mTabTitles[TabState.FAVORITES] =
		 * getString(R.string.favorites_tab_label); mTabTitles[TabState.ALL] =
		 * getString(R.string.all_contacts_tab_label); mTabPager =
		 * getView(R.id.tab_pager); mTabPagerAdapter = new TabPagerAdapter();
		 * mTabPager.setAdapter(mTabPagerAdapter);
		 * mTabPager.setOnPageChangeListener(mTabPagerListener);
		 */

		// Configure toolbar and toolbar tabs. If in landscape mode, we
		// configure tabs differntly.

		// toolbar = getView(R.id.toolbar);
		// toolbar.setElevation(0f);
		// toolbar.inflateMenu(R.menu.people_options);
		// toolbar.setOnMenuItemClickListener(this);

		toolbar = getToolbar();
		toolbar.setElevation(0f);
		toolbar.inflateMenu(R.menu.people_options);

		// final Menu menu = toolbar.getMenu();
		// final MenuItem menu_contacts_filter =
		// menu.findItem(R.id.menu_contacts_filter);
		// menu_contacts_filter.setVisible(true);
		mActionBarAdapter = new ActionBarAdapter(this, this, getActionBar(),
				null, null, toolbar);


		mActionBarAdapter.initialize(savedState, mRequest);

		// / @}

		setupActionModeWithDecor(toolbar);
		// setActionBar((android.widget.Toolbar)toolbar);

		/*
		 * final ViewPagerTabs portraitViewPagerTabs = (ViewPagerTabs)
		 * findViewById(R.id.lists_pager_header); ViewPagerTabs
		 * landscapeViewPagerTabs = null; if (portraitViewPagerTabs == null) {
		 * landscapeViewPagerTabs = (ViewPagerTabs) getLayoutInflater().inflate(
		 * R.layout.people_activity_tabs_lands, toolbar, attachToRoot = false);
		 * mViewPagerTabs = landscapeViewPagerTabs; } else { mViewPagerTabs =
		 * portraitViewPagerTabs; } mViewPagerTabs.setViewPager(mTabPager);
		 */

		final String FAVORITE_TAG = "tab-pager-favorite";
		final String ALL_TAG = "tab-pager-all";

		// Create the fragments and add as children of the view pager.
		// The pager adapter will only change the visibility; it'll never
		// create/destroy
		// fragments.
		// However, if it's after screen rotation, the fragments have been
		// re-created by
		// the fragment manager, so first see if there're already the target
		// fragments
		// existing.
		// mFavoritesFragment = (ContactTileListFragment)
		// fragmentManager.findFragmentByTag(FAVORITE_TAG);
		mAllFragment = (MultiSelectContactsListFragment) fragmentManager
				.findFragmentByTag(ALL_TAG);

		if (mAllFragment == null) {
			// mFavoritesFragment = new ContactTileListFragment();
			mAllFragment = new MultiSelectContactsListFragment();

			// transaction.add(R.id.tab_pager, mFavoritesFragment,
			// FAVORITE_TAG);
			transaction.add(R.id.tab_pager, mAllFragment, ALL_TAG);
		}

		// mFavoritesFragment.setListener(mFavoritesFragmentListener);

		mAllFragment
		.setOnContactListActionListener(new ContactBrowserActionListener());
		mAllFragment.setCheckBoxListListener(new CheckBoxListListener());
		mIndexBar = (MstIndexBar) findViewById(R.id.index_bar);
		mAllFragment.setmIndexBar(mIndexBar);

		// Hide all fragments for now. We adjust visibility when we get
		// onSelectedTabChanged()
		// from ActionBarAdapter.
		// transaction.hide(mFavoritesFragment);
		// transaction.hide(mAllFragment);

		transaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();

		// Setting Properties after fragment is created
		// mFavoritesFragment.setDisplayType(DisplayType.STREQUENT);

		// Add shadow under toolbar
		// ViewUtil.addRectangularOutlineProvider(findViewById(R.id.toolbar_parent),
		// getResources());

		// Configure floating action button
		mFloatingActionButtonContainer = (FloatingActionButton) findViewById(R.id.floating_action_button_container);
		// final ImageButton floatingActionButton
		// = (ImageButton) findViewById(R.id.floating_action_button);
		// floatingActionButton.setOnClickListener(this);

		mFloatingActionButtonContainer
		.setOnFloatingActionButtonClickListener(new OnFloatActionButtonClickListener() {
			public void onClick(View view) {
				Log.d(TAG, "[onClick]floating_action_button");
				Intent intent = new Intent(Intent.ACTION_INSERT,
						Contacts.CONTENT_URI);
				Bundle extras = getIntent().getExtras();
				if (extras != null) {
					intent.putExtras(extras);
				}
				try {
					ImplicitIntentsUtil.startActivityInApp(
							PeopleActivity.this, intent);
				} catch (ActivityNotFoundException ex) {
					Toast.makeText(PeopleActivity.this,
							R.string.missing_app, Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		mFloatingActionButtonController = new FloatingActionButtonController(
				this, mFloatingActionButtonContainer, null);
		initializeFabVisibility();

		invalidateOptionsMenuIfNeeded();

		mAllFragment.setmCallbacks(this);

		mOptionsMenuContactsAvailable = areContactsAvailable();
		if (/* mOptionsMenuContactsAvailable */true) {

			menu = toolbar.getMenu();
			// Get references to individual menu items in the menu
			final MenuItem contactsFilterMenu = menu
					.findItem(R.id.menu_contacts_filter);

			/** M: New Feature @{ */
			final MenuItem groupMenu = menu.findItem(R.id.menu_groups);
			/** @} */
			// / M: [VoLTE ConfCall]
			final MenuItem conferenceCallMenu = menu
					.findItem(R.id.menu_conference_call);

			final MenuItem clearFrequentsMenu = menu
					.findItem(R.id.menu_clear_frequents);
			final MenuItem helpMenu = menu.findItem(R.id.menu_help);

			final boolean isSearchOrSelectionMode = mActionBarAdapter
					.isSearchMode() || mActionBarAdapter.isSelectionMode();
			if (isSearchOrSelectionMode) {
				contactsFilterMenu.setVisible(false);
				clearFrequentsMenu.setVisible(false);
				helpMenu.setVisible(false);
				/** M: New Feature @{ */
				groupMenu.setVisible(true);
				/** @} */
				// / M: [VoLTE ConfCall]
				conferenceCallMenu.setVisible(false);
			} else {
				switch (getTabPositionForTextDirection(mActionBarAdapter
						.getCurrentTab())) {
						case TabState.FAVORITES:
							contactsFilterMenu.setVisible(false);
							clearFrequentsMenu.setVisible(hasFrequents());
							break;
						case TabState.ALL:
							contactsFilterMenu.setVisible(false);
							clearFrequentsMenu.setVisible(false);
							break;
						default:
							break;
				}
				helpMenu.setVisible(HelpUtils.isHelpAndFeedbackAvailable());
			}
			contactsFilterMenu.setVisible(false);
			final boolean showMiscOptions = !isSearchOrSelectionMode;
			makeMenuItemVisible(menu, R.id.menu_vcs, false);
			// makeMenuItemVisible(menu, R.id.menu_search, true);
			makeMenuItemVisible(menu, R.id.menu_import_export, false
					/* showMiscOptions && ActivitiesUtils.showImportExportMenu(this) */);
			makeMenuItemVisible(menu, R.id.mst_menu_export, false
					/* showMiscOptions && ActivitiesUtils.showImportExportMenu(this) */);
			makeMenuItemVisible(menu, R.id.menu_accounts, /* showMiscOptions */
					false);
			makeMenuItemVisible(menu, R.id.menu_settings, /*showMiscOptions*/true);// !ContactsPreferenceActivity.isEmpty(this));

			final boolean showSelectedContactOptions = mActionBarAdapter
					.isSelectionMode()
					&& mAllFragment.getSelectedContactIds().size() != 0;
			makeMenuItemVisible(menu, R.id.menu_share,
					showSelectedContactOptions);
			makeMenuItemVisible(menu, R.id.menu_delete,
					showSelectedContactOptions);
			makeMenuItemVisible(menu, R.id.menu_join,
					showSelectedContactOptions);
			// /M: Bug fix, if selected contacts just only one, it will show an
			// dialog to remind user.
			makeMenuItemEnabled(menu, R.id.menu_join, mAllFragment
					.getSelectedContactIds().size() >= 1);

			// Debug options need to be visible even in search mode.
			makeMenuItemVisible(menu, R.id.export_database,
					mEnableDebugMenuOptions);

			/** M: For VCS new feature */
			// ActivitiesUtils.prepareVcsMenu(menu, mVcsController);
			PDebug.End("onPrepareOptionsMenu");

			// / M: [VoLTE ConfCall] @{
			if (!VolteUtils.isVoLTEConfCallEnable(this)) {
				conferenceCallMenu.setVisible(false);
			}
			// / @}

			// / M: add for A1 @ {
			if (SystemProperties.get("ro.mtk_a1_feature").equals("1")) {
				Log.i(TAG, "[onPrepareOptionsMenu]enable a1 feature.");
				groupMenu.setVisible(true);
			}
			// / @ }
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMenuItemClick--->" + item.getTitle());

		switch (item.getItemId()) {
		case android.R.id.home: {
			// The home icon on the action bar is pressed
			if (mActionBarAdapter.isUpShowing()) {
				// "UP" icon press -- should be treated as "back".
				onBackPressed();
			}
			return true;
		}
		case R.id.menu_settings: {
			//			if(true){
			//				Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONES");
			//				intent.setType("vnd.android.cursor.dir/phone");
			////				intent.putExtra("mstFilter", "oneKeyAlarm");
			//				long[] ids=new long[]{2,4};
			//				intent.putExtra("data_ids", ids);
			//				startActivityForResult(intent, /*RESULT_PICK_CONTACT*/888);
			//				return true;
			//			}

			final Intent intent = new Intent(this,
					MstContactSettingActivity.class);
			// final Intent intent = new Intent(this,
			// ContactsPreferenceActivity.class);
			// Since there is only one section right now, make sure it is
			// selected on
			// small screens.
			// intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
			// DisplayOptionsPreferenceFragment.class.getName());
			// // By default, the title of the activity should be equivalent to
			// the fragment
			// // title. We set this argument to avoid this. Because of a bug,
			// the following
			// // line isn't necessary. But, once the bug is fixed this may
			// become necessary.
			// // b/5045558 refers to this issue, as well as another.
			// intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE,
			// R.string.activity_title_settings);
			startActivity(intent);
			return true;
		}
		case R.id.menu_contacts_filter: {
			AccountFilterUtil.startAccountFilterActivityForResult(this,
					SUBACTIVITY_ACCOUNT_FILTER,
					mContactListFilterController.getFilter());
			return true;
		}
		case R.id.menu_search: {
			onSearchRequested();
			return true;
		}
		case R.id.menu_share:
			shareSelectedContacts();
			return true;
		case R.id.menu_join:
			joinSelectedContacts();
			return true;
		case R.id.menu_delete:
			deleteSelectedContacts();
			return true;
		case R.id.menu_import_export: {
			/** M: Change Feature */
			return ActivitiesUtils.doImportExport(this);
		}
		case R.id.menu_clear_frequents: {
			ClearFrequentsDialog.show(getFragmentManager());
			return true;
		}
		case R.id.menu_help:
			HelpUtils.launchHelpAndFeedbackForMainScreen(this);
			return true;
		case R.id.menu_accounts: {
			final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
			intent.putExtra(Settings.EXTRA_AUTHORITIES,
					new String[] { ContactsContract.AUTHORITY });
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			ImplicitIntentsUtil.startActivityInAppIfPossible(this, intent);
			return true;
		}
		case R.id.export_database: {
			final Intent intent = new Intent(
					"com.android.providers.contacts.DUMP_DATABASE");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			ImplicitIntentsUtil.startActivityOutsideApp(this, intent);
			return true;
		}
		/** M: New feature @{ */
		/** M: [vcs] */
		case R.id.menu_vcs: {
			Log.d(TAG, "[onOptionsItemSelected]menu_vcs");
			// if (mVcsController != null) {
			// mVcsController.onVcsItemSelected();
			// }
			return true;
		}
		/** M: Group related */
		case R.id.menu_groups: {
			startActivity(new Intent(PeopleActivity.this,
					GroupBrowseActivity.class));
			return true;
		}
		/** @} */
		/** M: [VoLTE ConfCall]Conference call @{ */
		case R.id.menu_conference_call: {
			Log.d(TAG, "[onOptionsItemSelected]menu_conference_call");
			return ActivitiesUtils.conferenceCall(this);
		}

		case R.id.mst_menu_export: {
			String exportselection = Contacts._ID + ">0";
			Intent it = new Intent(this, ExportVCardActivity.class);
			it.putExtra("multi_export_type", 1); // TODO: 1 ,what's meaning?
			it.putExtra("exportselection", exportselection);
			it.putExtra("dest_path", getExternalStorageDirectory().getPath());
			it.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,
					PeopleActivity.this.toString());
			Log.d(TAG, "test:" + PeopleActivity.this);
			this.startActivity(it);
			return true;
		}

		case R.id.mst_busyness_card_scan: {
			sendBroadcast(new Intent("com.android.contacts.MST_GRANT_BUSINESS_CARD_PERMISSION"));//打开全能名片王存储空间权限
			Cursor cursor = null;
			final ContentResolver resolver = getContentResolver();
			try {

				cursor = resolver.query(Groups.CONTENT_URI, null, "_id=0",
						null, null);
				Log.d(TAG,
						"cursor:"
								+ (cursor == null ? "null" : cursor.getCount()));
				if (cursor == null || cursor.getCount() == 0) {
					createBusinessCardGroup();
				} else {
					cursor.moveToFirst();
					int delete = cursor.getInt(cursor
							.getColumnIndex(Groups.DELETED));
					String title = cursor.getString(cursor
							.getColumnIndex(Groups.TITLE));
					if (delete == 1) {
						ContentValues values = new ContentValues();
						values.put(Groups.DELETED, 0);
						values.put(Groups.ACCOUNT_TYPE, "Local Phone Account");
						values.put(Groups.ACCOUNT_NAME, "Phone");
						values.put(Groups.TITLE, getString(R.string.mst_business_card));
						values.put(Groups.GROUP_IS_READ_ONLY, 1);
						int rows = resolver.update(Groups.CONTENT_URI, values,
								"_id=0", null);
						if (rows == 0) {
							addGroupSuccess = false;
						}
					}
				}
			} catch (Exception e) {
				Log.d(TAG, "e:" + e);
				addGroupSuccess = false;
			} finally {
				cursor.close();
				cursor = null;
			}			

			Log.d(TAG,"addGroupSuccess:"+addGroupSuccess);
			if (addGroupSuccess){
				try{
					Intent intent = new Intent(/*MediaStore.ACTION_IMAGE_CAPTURE*/"com.android.contacts.MST_SCAN_BUSINESSCARD_ACTION");  
					intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());  
					//					intent.putExtra("crop", "false");
					intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
					intent.putExtra("return-data", false);
					intent.putExtra("noFaceDetection", true);
					startActivityForResult(intent,MST_SCAN_BUSINESS_CARD);
				}catch(ActivityNotFoundException e){
					Toast.makeText(PeopleActivity.this, getString(R.string.mst_business_card_scan_camera_activity_not_found), Toast.LENGTH_LONG).show();
				}
			}
			return true;
		}
		/** @} */

		}
		return false;
	}

	private String tempPhotoPath;
	private static final int MST_SCAN_BUSINESS_CARD = 10000;
	private static final int MST_PICK_PHOTO_FROM_GALLARY = 10001;
	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}

	private File getTempFile() {
		if (isSDCARDMounted()) {

			File file =new File(Environment.getExternalStorageDirectory()+"/businesscard/");
			//如果文件夹不存在则创建
			if  (!file .exists()  && !file .isDirectory())      
			{       
				file .mkdir();    
			}

			File f = new File(file.getAbsolutePath(),System.currentTimeMillis()+".jpg");
			tempPhotoPath=f.getAbsolutePath();
			Log.d(TAG,"tempPhotoPath:"+tempPhotoPath);
			//			try {
			//				f.createNewFile();
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
			return f;
		}
		return null;
	}

	private boolean isSDCARDMounted(){
		String status = Environment.getExternalStorageState();
		Log.d(TAG,"status:"+status);
		if (status.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}

	private File getExternalStorageDirectory() {
		// String path = StorageManagerEx.getDefaultPath();
		// String path = StorageManagerEx.getExternalStoragePath();
		// final File file = getDirectory(path,
		// Environment.getExternalStorageDirectory().toString());
		final File file = new File(Environment.getExternalStorageDirectory()
				.toString());
		Log.d(TAG, "[getExternalStorageDirectory]file.path : " + file.getPath());

		return file;
	}

	private File getDirectory(String path, String defaultPath) {
		Log.d(TAG, "[getDirectory]path : " + path + ",defaultPath :"
				+ defaultPath);
		return path == null ? new File(defaultPath) : new File(path);
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "[onStart]mFragmentInitialized = " + mFragmentInitialized
				+ ",mIsRecreatedInstance = " + mIsRecreatedInstance);
		if (!mFragmentInitialized) {
			mFragmentInitialized = true;
			/*
			 * Configure fragments if we haven't.
			 * 
			 * Note it's a one-shot initialization, so we want to do this in
			 * {@link #onCreate}.
			 * 
			 * However, because this method may indirectly touch views in
			 * fragments but fragments created in {@link #configureContentView}
			 * using a {@link FragmentTransaction} will NOT have views until
			 * {@link Activity#onCreate} finishes (they would if they were
			 * inflated from a layout), we need to do it here in {@link
			 * #onStart()}.
			 * 
			 * (When {@link Fragment#onCreateView} is called is different in the
			 * former case and in the latter case, unfortunately.)
			 * 
			 * Also, we skip most of the work in it if the activity is a
			 * re-created one. (so the argument.)
			 */
			configureFragments(!mIsRecreatedInstance);
		}
		// / M: register sim change @{
		//		AccountTypeManagerEx.registerReceiverOnSimStateAndInfoChanged(this,
		//				mBroadcastReceiver);
		// / @}

		//		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PeopleActivity.this);
		//		int hasValidateBcrApi=prefs.getInt("hasValidateBcrApi", 0);//是否已经验证过名片全能王接口有效性
		//		if(hasValidateBcrApi==0){
		//			handler.postDelayed(runnable, 10000);
		//		}
		super.onStart();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "[onPause]");
		mOptionsMenuContactsAvailable = false;
		mProviderStatusWatcher.stop();
		/** M: New Feature CR ID: ALPS00112598 */
		if (SlotUtils.isGeminiEnabled()) {
			// SetIndicatorUtils.getInstance().showIndicator(this, false);
		}
		// / M:[vcs] VCS Feature. @{
		// if (mVcsController != null) {
		// mVcsController.onPauseVcs();
		// }
		// / @}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "[onResume]");
		mProviderStatusWatcher.start();
		updateViewConfiguration(true);
		mActionBarAdapter.setListener(this);
		//		if(mActionBarAdapter.isSearchMode()&&TextUtils.isEmpty(mActionBarAdapter.getQueryString())){
		//			Log.d(TAG,"onResume1");
		//			exitSearchMode();
		//
		//			setQueryTextToFragment("");
		//			updateFragmentsVisibility();
		//			//			invalidateOptionsMenu();
		//			showFabWithAnimation(/* showFabWithAnimation = */true);
		//			showActionMode(false);
		//			mAllFragment.getAdapter().setSelectMode(false);
		//			return;
		//		}


		// Re-register the listener, which may have been cleared when
		// onSaveInstanceState was
		// called. See also: onSaveInstanceState

		mDisableOptionItemSelected = false;
		if (mTabPager != null) {
			mTabPager.setOnPageChangeListener(mTabPagerListener);
		}
		// Current tab may have changed since the last onSaveInstanceState().
		// Make sure
		// the actual contents match the tab.
		updateFragmentsVisibility();
		/** M: New Feature CR ID: ALPS00112598 */
		if (SlotUtils.isGeminiEnabled()) {
			// SetIndicatorUtils.getInstance().showIndicator(this, true);
		}

		Log.d(TAG, "[Performance test][Contacts] loading data end time: ["
				+ System.currentTimeMillis() + "]");
		// / M: [vcs] VCS feature @{
		// if (mVcsController != null) {
		// mVcsController.onResumeVcs();
		// }
		// / @}

		PDebug.End("Contacts.onResume");
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "[onStop]");
		PDebug.Start("onStop");
		// / M: @{
		if (PhoneCapabilityTester.isUsingTwoPanes(this)) {
			mActionBarAdapter.setSearchMode(false);
			invalidateOptionsMenu();
		}
		// / @
		// / M: unregister sim change @{
		//		unregisterReceiver(mBroadcastReceiver);
		// / @
		super.onStop();
		PDebug.End("onStop");
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "[onDestroy]");
		PDebug.Start("onDestroy");
		mProviderStatusWatcher.removeListener(this);

		// Some of variables will be null if this Activity redirects Intent.
		// See also onCreate() or other methods called during the Activity's
		// initialization.
		if (mActionBarAdapter != null) {
			mActionBarAdapter.setListener(null);
		}
		if (mContactListFilterController != null) {
			mContactListFilterController.removeListener(this);
		}

		// / M: [vcs] VCS feature.
		// if (mVcsController != null) {
		// mVcsController.onDestoryVcs();
		// }

		super.onDestroy();
		PDebug.End("onDestroy");
		try {
			unregisterReceiver(myReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		ContactsHelper.getInstance().destroy();
		if(mContactsHelper!=null) mContactsHelper.destroy();
	}

	private void configureFragments(boolean fromRequest) {
		Log.d(TAG, "[configureFragments]fromRequest = " + fromRequest);
		if (fromRequest) {
			ContactListFilter filter = null;
			int actionCode = mRequest.getActionCode();
			boolean searchMode = mRequest.isSearchMode();
			final int tabToOpen;
			switch (actionCode) {
			case ContactsRequest.ACTION_ALL_CONTACTS:
				filter = ContactListFilter
				.createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
				tabToOpen = TabState.ALL;
				break;
			case ContactsRequest.ACTION_CONTACTS_WITH_PHONES:
				filter = ContactListFilter
				.createFilterWithType(ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY);
				tabToOpen = TabState.ALL;
				break;

			case ContactsRequest.ACTION_FREQUENT:
			case ContactsRequest.ACTION_STREQUENT:
			case ContactsRequest.ACTION_STARRED:
				tabToOpen = TabState.FAVORITES;
				break;
			case ContactsRequest.ACTION_VIEW_CONTACT:
				tabToOpen = TabState.ALL;
				break;
			default:
				tabToOpen = -1;
				break;
			}
			if (tabToOpen != -1) {
				mActionBarAdapter.setCurrentTab(tabToOpen);
			}

			if (filter != null) {
				mContactListFilterController
				.setContactListFilter(filter, false);
				searchMode = false;
			}

			if (mRequest.getContactUri() != null) {
				searchMode = false;
			}

			mActionBarAdapter.setSearchMode(searchMode);
			configureContactListFragmentForRequest();
		}

		configureContactListFragment();

		invalidateOptionsMenuIfNeeded();
	}

	private boolean hideFabForIndexerScroll=false;
	private void initializeFabVisibility() {		
		final boolean hideFab = mActionBarAdapter.isSearchMode()
				|| mActionBarAdapter.isSelectionMode()||hideFabForIndexerScroll;
		Log.d(TAG,"initializeFabVisibility,hideFab:"+hideFab+" hideFabForIndexerScroll:"+hideFabForIndexerScroll);
		mFloatingActionButtonContainer.setVisibility(hideFab ? View.GONE
				: View.VISIBLE);
		//		mFloatingActionButtonController.resetIn();
		wasLastFabAnimationScaleIn = !hideFab;
	}

	private void showFabWithAnimation(boolean showFab) {
		Log.d(TAG,"showFabWithAnimation:"+showFab);
		mFloatingActionButtonContainer.setVisibility(showFab?View.VISIBLE:View.GONE);
		//		if (mFloatingActionButtonContainer == null) {
		//			return;
		//		}
		//		if (showFab) {
		//			if (!wasLastFabAnimationScaleIn) {
		//				mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
		//				mFloatingActionButtonController.scaleIn(0);
		//			}
		//			wasLastFabAnimationScaleIn = true;
		//
		//		} else {
		//			if (wasLastFabAnimationScaleIn) {
		//				mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
		//				mFloatingActionButtonController.scaleOut();
		//			}
		//			wasLastFabAnimationScaleIn = false;
		//		}
	}

	@Override
	public void onContactListFilterChanged() {
		if (mAllFragment == null || !mAllFragment.isAdded()) {
			return;
		}

		mAllFragment.setFilter(mContactListFilterController.getFilter());

		invalidateOptionsMenuIfNeeded();
	}

	/**
	 * Handler for action bar actions.
	 */
	@Override
	public void onAction(int action) {
		Log.d(TAG, "[onAction]action = " + action);
		// / M: [vcs] @{
		// if (mVcsController != null) {
		// mVcsController.onActionVcs(action);
		// }
		// / @}
		switch (action) {
		case ActionBarAdapter.Listener.Action.START_SELECTION_MODE:
			Log.d(TAG, "START_SELECTION_MODE");
			mAllFragment.displayCheckBoxes(true);
			bottomBar.setVisibility(View.VISIBLE);
			showFabWithAnimation(false);
			showActionMode(true);
			// mToolBarFrame.setVisibility(View.GONE);
			mAllFragment.getAdapter().setSelectMode(true);
			break;
			// Fall through:
		case ActionBarAdapter.Listener.Action.START_SEARCH_MODE:
			Log.d(TAG, "START_SEARCH_MODE");
			// Tell the fragments that we're in the search mode or selection
			// mode
			configureFragments(false /* from request */);
			updateFragmentsVisibility();
			invalidateOptionsMenu();
			showFabWithAnimation(/* showFabWithAnimation = */false);
			showActionMode(false);
			// mToolBarFrame.setVisibility(View.VISIBLE);
			break;
		case ActionBarAdapter.Listener.Action.BEGIN_STOPPING_SEARCH_AND_SELECTION_MODE:
			Log.d(TAG, "BEGIN_STOPPING_SEARCH_AND_SELECTION_MODE");
			showFabWithAnimation(/* showFabWithAnimation = */true);
			bottomBar.setVisibility(View.GONE);
			showActionMode(false);
			// mToolBarFrame.setVisibility(View.GONE);
			mAllFragment.getAdapter().setSelectMode(false);
			break;
		case ActionBarAdapter.Listener.Action.STOP_SEARCH_AND_SELECTION_MODE:
			Log.d(TAG, "STOP_SEARCH_AND_SELECTION_MODE");
			setQueryTextToFragment("");
			updateFragmentsVisibility();
			//			invalidateOptionsMenu();
			showFabWithAnimation(/* showFabWithAnimation = */true);
			// mToolBarFrame.setVisibility(View.GONE);
			showActionMode(false);
			mAllFragment.getAdapter().setSelectMode(false);
			break;
		case ActionBarAdapter.Listener.Action.CHANGE_SEARCH_QUERY:
			Log.d(TAG, "CHANGE_SEARCH_QUERY");
			String queryString = mActionBarAdapter.getQueryString();

			if (TextUtils.isEmpty(queryString)) {
				queryString = SEARCH_BEGIN_STRING;
			}
			setQueryTextToFragment(queryString);
			updateDebugOptionsVisibility(ENABLE_DEBUG_OPTIONS_HIDDEN_CODE
					.equals(queryString));
			break;
		case ActionBarAdapter.Listener.Action.EXIT_SEARCH_MODE:
			Log.d(TAG, "EXIT_SEARCH_MODE");
			exitSearchMode();
			break;
		default:
			throw new IllegalStateException("Unkonwn ActionBarAdapter action: "
					+ action);
		}
	}

	@Override
	public void onSelectedTabChanged() {
		Log.d(TAG, "[onSelectedTabChanged]");
		// / M: [vcs] @{
		// if (mVcsController != null) {
		// mVcsController.onSelectedTabChangedEx();
		// }
		// / @}
		updateFragmentsVisibility();
	}

	@Override
	public void onUpButtonPressed() {
		Log.d(TAG, "[onUpButtonPressed]");
		onBackPressed();
	}

	private void updateDebugOptionsVisibility(boolean visible) {
		if (mEnableDebugMenuOptions != visible) {
			mEnableDebugMenuOptions = visible;
			invalidateOptionsMenu();
		}
	}

	/**
	 * Updates the fragment/view visibility according to the current mode, such
	 * as {@link ActionBarAdapter#isSearchMode()} and
	 * {@link ActionBarAdapter#getCurrentTab()}.
	 */
	private void updateFragmentsVisibility() {
		// int tab = mActionBarAdapter.getCurrentTab();
		//
		// if (mActionBarAdapter.isSearchMode() ||
		// mActionBarAdapter.isSelectionMode()) {
		// mTabPagerAdapter.setTabsHidden(true);
		// } else {
		// // No smooth scrolling if quitting from the search/selection mode.
		// final boolean wereTabsHidden = mTabPagerAdapter.areTabsHidden()
		// || mActionBarAdapter.isSelectionMode();
		// mTabPagerAdapter.setTabsHidden(false);
		// if (mTabPager.getCurrentItem() != tab) {
		// mTabPager.setCurrentItem(tab, !wereTabsHidden);
		// }
		// }
		if (!mActionBarAdapter.isSelectionMode()) {
			mAllFragment.displayCheckBoxes(false);
		}
		invalidateOptionsMenu();
		// showEmptyStateForTab(tab);
	}

	private void showEmptyStateForTab(int tab) {

		if (mContactsUnavailableFragment != null) {
			switch (getTabPositionForTextDirection(tab)) {
			case TabState.FAVORITES:
				mContactsUnavailableFragment.setMessageText(
						R.string.listTotalAllContactsZeroStarred, -1);
				break;
			case TabState.ALL:
				mContactsUnavailableFragment.setMessageText(
						R.string.noContacts, -1);
				break;
			default:
				break;
			}
			// When using the mContactsUnavailableFragment the ViewPager doesn't
			// contain two views.
			// Therefore, we have to trick the ViewPagerTabs into thinking we
			// have changed tabs
			// when the mContactsUnavailableFragment changes. Otherwise the tab
			// strip won't move.
			// mViewPagerTabs.onPageScrolled(tab, 0, 0);
		}
	}

	private class TabPagerListener implements ViewPager.OnPageChangeListener {

		// This package-protected constructor is here because of a possible
		// compiler bug.
		// PeopleActivity$1.class should be generated due to the private
		// outer/inner class access
		// needed here. But for some reason, PeopleActivity$1.class is missing.
		// Since $1 class is needed as a jvm work around to get access to the
		// inner class,
		// changing the constructor to package-protected or public will solve
		// the problem.
		// To verify whether $1 class is needed, javap
		// PeopleActivity$TabPagerListener and look for
		// references to PeopleActivity$1.
		//
		// When the constructor is private and PeopleActivity$1.class is
		// missing, proguard will
		// correctly catch this and throw warnings and error out the build on
		// user/userdebug builds.
		//
		// All private inner classes below also need this fix.
		TabPagerListener() {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (!mTabPagerAdapter.areTabsHidden()) {
				mViewPagerTabs.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if (!mTabPagerAdapter.areTabsHidden()) {
				mViewPagerTabs.onPageScrolled(position, positionOffset,
						positionOffsetPixels);
			}
		}

		@Override
		public void onPageSelected(int position) {
			// Make sure not in the search mode, in which case position !=
			// TabState.ordinal().
			if (!mTabPagerAdapter.areTabsHidden()) {
				mActionBarAdapter.setCurrentTab(position, false);
				mViewPagerTabs.onPageSelected(position);
				showEmptyStateForTab(position);
				// / M: [vcs] @{
				// if (mVcsController != null) {
				// mVcsController.onPageSelectedVcs();
				// }
				// / @}
				invalidateOptionsMenu();
			}
		}
	}

	/**
	 * Adapter for the {@link ViewPager}. Unlike {@link FragmentPagerAdapter},
	 * {@link #instantiateItem} returns existing fragments, and
	 * {@link #instantiateItem}/ {@link #destroyItem} show/hide fragments
	 * instead of attaching/detaching.
	 * 
	 * In search mode, we always show the "all" fragment, and disable the swipe.
	 * We change the number of items to 1 to disable the swipe.
	 * 
	 * TODO figure out a more straight way to disable swipe.
	 */
	private class TabPagerAdapter extends PagerAdapter {
		private final FragmentManager mFragmentManager;
		private FragmentTransaction mCurTransaction = null;

		private boolean mAreTabsHiddenInTabPager;

		private Fragment mCurrentPrimaryItem;

		public TabPagerAdapter() {
			mFragmentManager = getFragmentManager();
		}

		public boolean areTabsHidden() {
			return mAreTabsHiddenInTabPager;
		}

		public void setTabsHidden(boolean hideTabs) {
			if (hideTabs == mAreTabsHiddenInTabPager) {
				return;
			}
			mAreTabsHiddenInTabPager = hideTabs;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mAreTabsHiddenInTabPager ? 1 : TabState.COUNT;
		}

		/** Gets called when the number of items changes. */
		@Override
		public int getItemPosition(Object object) {
			if (mAreTabsHiddenInTabPager) {
				if (object == mAllFragment) {
					return 0; // Only 1 page in search mode
				}
			} else {
				if (object == mFavoritesFragment) {
					return getTabPositionForTextDirection(TabState.FAVORITES);
				}
				if (object == mAllFragment) {
					return getTabPositionForTextDirection(TabState.ALL);
				}
			}
			return POSITION_NONE;
		}

		@Override
		public void startUpdate(ViewGroup container) {
		}

		private Fragment getFragment(int position) {
			position = getTabPositionForTextDirection(position);
			if (mAreTabsHiddenInTabPager) {
				if (position != 0) {
					// This has only been observed in monkey tests.
					// Let's log this issue, but not crash
					Log.w(TAG, "Request fragment at position=" + position
							+ ", eventhough we " + "are in search mode");
				}
				return mAllFragment;
			} else {
				if (position == TabState.FAVORITES) {
					return mFavoritesFragment;
				} else if (position == TabState.ALL) {
					return mAllFragment;
				}
			}
			throw new IllegalArgumentException("position: " + position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if (mCurTransaction == null) {
				mCurTransaction = mFragmentManager.beginTransaction();
			}
			Fragment f = getFragment(position);
			mCurTransaction.show(f);

			// Non primary pages are not visible.
			f.setUserVisibleHint(f == mCurrentPrimaryItem);
			return f;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (mCurTransaction == null) {
				mCurTransaction = mFragmentManager.beginTransaction();
			}
			mCurTransaction.hide((Fragment) object);
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			if (mCurTransaction != null) {
				mCurTransaction.commitAllowingStateLoss();
				mCurTransaction = null;
				mFragmentManager.executePendingTransactions();
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return ((Fragment) object).getView() == view;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			Fragment fragment = (Fragment) object;
			if (mCurrentPrimaryItem != fragment) {
				if (mCurrentPrimaryItem != null) {
					mCurrentPrimaryItem.setUserVisibleHint(false);
				}
				if (fragment != null) {
					fragment.setUserVisibleHint(true);
				}
				mCurrentPrimaryItem = fragment;
			}
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTabTitles[position];
		}
	}

	private void setQueryTextToFragment(String query) {
		mAllFragment.setQueryStringMst(query);
		mAllFragment.setVisibleScrollbarEnabled(!mAllFragment.isSearchMode());
	}

	private void configureContactListFragmentForRequest() {
		Uri contactUri = mRequest.getContactUri();
		if (contactUri != null) {
			mAllFragment.setSelectedContactUri(contactUri);
		}

		mAllFragment.setFilter(mContactListFilterController.getFilter());
		setQueryTextToFragment(mActionBarAdapter.getQueryString());

		if (mRequest.isDirectorySearchEnabled()) {
			mAllFragment
			.setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_DEFAULT);
		} else {
			mAllFragment
			.setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_NONE);
		}
	}

	private void configureContactListFragment() {
		// Filter may be changed when this Activity is in background.
		mAllFragment.setFilter(mContactListFilterController.getFilter());

		mAllFragment.setVerticalScrollbarPosition(getScrollBarPosition());
		mAllFragment.setSelectionVisible(false);
	}

	private int getScrollBarPosition() {
		return isRTL() ? View.SCROLLBAR_POSITION_LEFT
				: View.SCROLLBAR_POSITION_RIGHT;
	}

	private boolean isRTL() {
		final Locale locale = Locale.getDefault();
		return TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
	}

	@Override
	public void onProviderStatusChange() {
		Log.d(TAG, "[onProviderStatusChange]");
		updateViewConfiguration(false);
	}

	private void updateViewConfiguration(boolean forceUpdate) {
		Log.d(TAG, "[updateViewConfiguration]forceUpdate = " + forceUpdate);
		int providerStatus = mProviderStatusWatcher.getProviderStatus();
		if (!forceUpdate && (mProviderStatus != null)
				&& (mProviderStatus.equals(providerStatus)))
			return;
		mProviderStatus = providerStatus;

		View contactsUnavailableView = findViewById(R.id.contacts_unavailable_view);

		if (mProviderStatus.equals(ProviderStatus.STATUS_NORMAL)
				|| ExtensionManager.getInstance().getRcsExtension()
				.isRcsServiceAvailable()) {
			// Ensure that the mTabPager is visible; we may have made it
			// invisible below.
			contactsUnavailableView.setVisibility(View.GONE);
			if (mTabPager != null) {
				mTabPager.setVisibility(View.VISIBLE);
			}

			if (mAllFragment != null) {
				mAllFragment.setEnabled(true);
			}
		} else {
			// If there are no accounts on the device and we should show the
			// "no account" prompt
			// (based on {@link SharedPreferences}), then launch the account
			// setup activity so the
			// user can sign-in or create an account.
			//
			// Also check for ability to modify accounts. In limited user mode,
			// you can't modify
			// accounts so there is no point sending users to account setup
			// activity.
			final UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
			final boolean disallowModifyAccounts = userManager
					.getUserRestrictions().getBoolean(
							UserManager.DISALLOW_MODIFY_ACCOUNTS);
			if (!disallowModifyAccounts
					&& !areContactWritableAccountsAvailable()
					&& AccountPromptUtils.shouldShowAccountPrompt(this)) {
				Log.i(TAG, "[updateViewConfiguration]return.");
				AccountPromptUtils.neverShowAccountPromptAgain(this);
				AccountPromptUtils.launchAccountPrompt(this);
				return;
			}

			// Otherwise, continue setting up the page so that the user can
			// still use the app
			// without an account.
			if (mAllFragment != null) {
				mAllFragment.setEnabled(false);
			}
			if (mContactsUnavailableFragment == null) {
				mContactsUnavailableFragment = new ContactsUnavailableFragment();
				mContactsUnavailableFragment
				.setOnContactsUnavailableActionListener(new ContactsUnavailableFragmentListener());
				getFragmentManager()
				.beginTransaction()
				.replace(R.id.contacts_unavailable_container,
						mContactsUnavailableFragment)
						.commitAllowingStateLoss();
			}
			mContactsUnavailableFragment.updateStatus(mProviderStatus);

			// Show the contactsUnavailableView, and hide the mTabPager so that
			// we don't
			// see it sliding in underneath the contactsUnavailableView at the
			// edges.
			/**
			 * M: Bug Fix @{ CR ID: ALPS00113819 Descriptions: remove
			 * ContactUnavaliableFragment Fix wait cursor keeps showing while no
			 * contacts issue
			 */
			ActivitiesUtils.setAllFramgmentShow(contactsUnavailableView,
					mAllFragment, this, mTabPager,
					mContactsUnavailableFragment, mProviderStatus);

			showEmptyStateForTab(mActionBarAdapter.getCurrentTab());
		}

		invalidateOptionsMenuIfNeeded();
	}

	private final class ContactBrowserActionListener implements
	OnContactBrowserActionListener {
		ContactBrowserActionListener() {
		}

		@Override
		public void onSelectionChange() {

		}

		@Override
		public void onViewContactAction(Uri contactLookupUri) {
			Log.d(TAG, "[onViewContactAction]contactLookupUri = "
					+ contactLookupUri);
			final Intent intent = ImplicitIntentsUtil
					.composeQuickContactIntent(contactLookupUri,
							QuickContactActivity.MODE_FULLY_EXPANDED);
			ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
		}

		@Override
		public void onDeleteContactAction(Uri contactUri) {
			Log.d(TAG, "[onDeleteContactAction]contactUri = " + contactUri);
			ContactDeletionInteraction.start(PeopleActivity.this, contactUri,
					false);
		}

		@Override
		public void onFinishAction() {
			Log.d(TAG, "[onFinishAction]call onBackPressed");
			onBackPressed();
		}

		@Override
		public void onInvalidSelection() {
			ContactListFilter filter;
			ContactListFilter currentFilter = mAllFragment.getFilter();
			if (currentFilter != null
					&& currentFilter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
				filter = ContactListFilter
						.createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
				mAllFragment.setFilter(filter);
			} else {
				filter = ContactListFilter
						.createFilterWithType(ContactListFilter.FILTER_TYPE_SINGLE_CONTACT);
				mAllFragment.setFilter(filter, false);
			}
			mContactListFilterController.setContactListFilter(filter, true);
		}
	}

	public void switchToEditMode(boolean flag) {
		if (flag) {
			showFabWithAnimation(false);
			mActionBarAdapter.setSelectionMode(true);
			mAllFragment.getAdapter().setSelectMode(true);
			showActionMode(true);
			bottomBar.setVisibility(View.VISIBLE);
			mAllFragment.displayCheckBoxes(true);
			mAllFragment.configureHeaderDisplay(true);
		} else {
			mActionBarAdapter.setSelectionMode(false);
			mAllFragment.getAdapter().setSelectMode(false);
			// / M: Fix add contact button disappear bug
			initializeFabVisibility();
			showActionMode(false);
			bottomBar.setVisibility(View.GONE);
			mAllFragment.displayCheckBoxesV2(false,false);
			mAllFragment.configureHeaderDisplay(false);
		}
	}

	private final class CheckBoxListListener implements
	OnCheckBoxListActionListener {
		@Override
		public void onStartDisplayingCheckBoxes() {
			Log.d(TAG, "[onStartDisplayingCheckBoxes]");
			// mActionBarAdapter.setSelectionMode(true);
			switchToEditMode(true);
			// invalidateOptionsMenu();

		}

		@Override
		public void onSelectedContactIdsChanged() {
			// int all =
			// mAllFragment.getAdapter().getCount()-mAllFragment.getAdapter().getStarredCount();
			Log.d(TAG, "[onSelectedContactIdsChanged]size = "
					+ mAllFragment.getSelectedContactIds().size());
			mAllFragment.updateSelectedItemsView();
			mActionBarAdapter.setSelectionCount(mAllFragment
					.getSelectedContactIds().size());

			updateActionMode();

			// invalidateOptionsMenu();
		}

		@Override
		public void onStopDisplayingCheckBoxes() {
			Log.d(TAG, "[onStopDisplayingCheckBoxes]");
			mActionBarAdapter.setSelectionMode(false);
			// / M:[vcs] VCS Feature. @{
			// if (mVcsController != null) {
			// int count = mAllFragment.getAdapter().getCount();
			// if (count <= 0) {
			// mVcsController.onPauseVcs();
			// } else {
			// mVcsController.onResumeVcs();
			// }
			// }
			// / @}
		}
	}

	private class ContactsUnavailableFragmentListener implements
	OnContactsUnavailableActionListener {
		ContactsUnavailableFragmentListener() {
		}

		@Override
		public void onCreateNewContactAction() {
			Log.d(TAG, "[onCreateNewContactAction]");
			ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this,
					EditorIntents.createCompactInsertContactIntent());
		}

		@Override
		public void onAddAccountAction() {
			Log.d(TAG, "[onAddAccountAction]");
			Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.putExtra(Settings.EXTRA_AUTHORITIES,
					new String[] { ContactsContract.AUTHORITY });
			ImplicitIntentsUtil.startActivityOutsideApp(PeopleActivity.this,
					intent);
		}

		@Override
		public void onImportContactsFromFileAction() {
			Log.d(TAG, "[onImportContactsFromFileAction]");
			/**
			 * M: New Feature.use mtk importExport function,use the encapsulate
			 * class do this.@{
			 */
			ActivitiesUtils.doImportExport(PeopleActivity.this);
			/** @} */

		}
	}

	private final class StrequentContactListFragmentListener implements
	ContactTileListFragment.Listener {
		StrequentContactListFragmentListener() {
		}

		@Override
		public void onContactSelected(Uri contactUri, Rect targetRect) {
			final Intent intent = ImplicitIntentsUtil
					.composeQuickContactIntent(contactUri,
							QuickContactActivity.MODE_FULLY_EXPANDED);
			ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
		}

		@Override
		public void onCallNumberDirectly(String phoneNumber) {
			// No need to call phone number directly from People app.
			Log.w(TAG, "unexpected invocation of onCallNumberDirectly()");
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// Log.d(TAG, "[onCreateOptionsMenu]");
	//
	// if (!areContactsAvailable()) {
	// Log.i(TAG,
	// "[onCreateOptionsMenu]contacts aren't available, hide all menu items");
	// // If contacts aren't available, hide all menu items.
	// /// M:Fix option menu disappearance issue when change language. @{
	// mOptionsMenuContactsAvailable = false;
	// /// @}
	//
	// // M: fix ALPS02454655.only sim contacts selected,menu show in
	// screen-left when
	// // turn on airmode.@{
	// if (menu != null) {
	// Log.d(TAG, "[onCreateOptionsMenu] close menu if open!");
	// menu.close();
	// }
	// //@}
	//
	// return false;
	// }
	// super.onCreateOptionsMenu(menu);
	//
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(R.menu.people_options, menu);
	//
	//
	// /// M: Op01 will add "show sim capacity" item
	// ExtensionManager.getInstance().getOp01Extension().addOptionsMenu(this,
	// menu);
	//
	// /// M:OP01 RCS will add people menu item
	// ExtensionManager.getInstance().getRcsExtension().addPeopleMenuOptions(menu);
	//
	// /// M: [vcs] VCS new feature @{
	// // if (mVcsController != null) {
	// // mVcsController.onCreateOptionsMenuVcs(menu);
	// // }
	// /// @}
	// PDebug.End("onCreateOptionsMenu");
	// return true;
	// }

	private void invalidateOptionsMenuIfNeeded() {
		if (isOptionsMenuChanged()) {
			invalidateOptionsMenu();
		}
	}

	public boolean isOptionsMenuChanged() {
		if (mOptionsMenuContactsAvailable != areContactsAvailable()) {
			return true;
		}

		if (mAllFragment != null && mAllFragment.isOptionsMenuChanged()) {
			return true;
		}

		return false;
	}

	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// Log.d(TAG, "[onPrepareOptionsMenu]");
	// PDebug.Start("onPrepareOptionsMenu");
	// /// M: Fix ALPS01612926,smartbook issue @{
	// if (mActionBarAdapter == null) {
	// Log.w(TAG, "[onPrepareOptionsMenu]mActionBarAdapter is null,return..");
	// return true;
	// }
	// /// @}
	// mOptionsMenuContactsAvailable = areContactsAvailable();
	// if (!mOptionsMenuContactsAvailable) {
	// Log.w(TAG,
	// "[onPrepareOptionsMenu]areContactsAvailable is false,return..");
	// return false;
	// }
	// // Get references to individual menu items in the menu
	// final MenuItem contactsFilterMenu =
	// menu.findItem(R.id.menu_contacts_filter);
	//
	// /** M: New Feature @{ */
	// final MenuItem groupMenu = menu.findItem(R.id.menu_groups);
	// /** @} */
	// /// M: [VoLTE ConfCall]
	// final MenuItem conferenceCallMenu =
	// menu.findItem(R.id.menu_conference_call);
	//
	//
	// final MenuItem clearFrequentsMenu =
	// menu.findItem(R.id.menu_clear_frequents);
	// final MenuItem helpMenu = menu.findItem(R.id.menu_help);
	//
	// final boolean isSearchOrSelectionMode = mActionBarAdapter.isSearchMode()
	// || mActionBarAdapter.isSelectionMode();
	// if (isSearchOrSelectionMode) {
	// contactsFilterMenu.setVisible(false);
	// clearFrequentsMenu.setVisible(false);
	// helpMenu.setVisible(false);
	// /** M: New Feature @{ */
	// groupMenu.setVisible(false);
	// /** @} */
	// /// M: [VoLTE ConfCall]
	// conferenceCallMenu.setVisible(false);
	// } else {
	// switch
	// (getTabPositionForTextDirection(mActionBarAdapter.getCurrentTab())) {
	// case TabState.FAVORITES:
	// contactsFilterMenu.setVisible(false);
	// clearFrequentsMenu.setVisible(hasFrequents());
	// break;
	// case TabState.ALL:
	// contactsFilterMenu.setVisible(true);
	// clearFrequentsMenu.setVisible(false);
	// break;
	// default:
	// break;
	// }
	// helpMenu.setVisible(HelpUtils.isHelpAndFeedbackAvailable());
	// }
	// final boolean showMiscOptions = !isSearchOrSelectionMode;
	// makeMenuItemVisible(menu, R.id.menu_vcs, false);
	// makeMenuItemVisible(menu, R.id.menu_search, showMiscOptions);
	// makeMenuItemVisible(menu, R.id.menu_import_export,
	// showMiscOptions && ActivitiesUtils.showImportExportMenu(this));
	// makeMenuItemVisible(menu, R.id.menu_accounts, showMiscOptions);
	// makeMenuItemVisible(menu, R.id.menu_settings,
	// showMiscOptions && !ContactsPreferenceActivity.isEmpty(this));
	//
	// final boolean showSelectedContactOptions =
	// mActionBarAdapter.isSelectionMode()
	// && mAllFragment.getSelectedContactIds().size() != 0;
	// makeMenuItemVisible(menu, R.id.menu_share, showSelectedContactOptions);
	// makeMenuItemVisible(menu, R.id.menu_delete, showSelectedContactOptions);
	// makeMenuItemVisible(menu, R.id.menu_join, showSelectedContactOptions);
	// ///M: Bug fix, if selected contacts just only one, it will show an dialog
	// to remind user.
	// makeMenuItemEnabled(menu, R.id.menu_join,
	// mAllFragment.getSelectedContactIds().size() >= 1);
	//
	// // Debug options need to be visible even in search mode.
	// makeMenuItemVisible(menu, R.id.export_database, mEnableDebugMenuOptions);
	//
	// /** M: For VCS new feature */
	// // ActivitiesUtils.prepareVcsMenu(menu, mVcsController);
	// PDebug.End("onPrepareOptionsMenu");
	//
	// /// M: [VoLTE ConfCall] @{
	// if (!VolteUtils.isVoLTEConfCallEnable(this)) {
	// conferenceCallMenu.setVisible(false);
	// }
	// /// @}
	//
	// /// M: add for A1 @ {
	// if (SystemProperties.get("ro.mtk_a1_feature").equals("1")) {
	// Log.i(TAG, "[onPrepareOptionsMenu]enable a1 feature.");
	// groupMenu.setVisible(false);
	// }
	// /// @ }
	// return true;
	// }

	/**
	 * Returns whether there are any frequently contacted people being displayed
	 * 
	 * @return
	 */
	private boolean hasFrequents() {
		return mFavoritesFragment == null ? false : mFavoritesFragment
				.hasFrequents();
	}

	private void makeMenuItemVisible(Menu menu, int itemId, boolean visible) {
		final MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setVisible(visible);
		}
	}

	private void makeMenuItemEnabled(Menu menu, int itemId, boolean visible) {
		final MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setEnabled(visible);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {/*
	 * Log.d(TAG,
	 * "[onOptionsItemSelected] mDisableOptionItemSelected = "
	 * +
	 * mDisableOptionItemSelected
	 * ); if (
	 * mDisableOptionItemSelected
	 * ) { return false; }
	 * 
	 * switch
	 * (item.getItemId()) {
	 * case
	 * android.R.id.home: {
	 * // The home icon on
	 * the action bar is
	 * pressed if
	 * (mActionBarAdapter
	 * .isUpShowing()) { //
	 * "UP" icon press --
	 * should be treated as
	 * "back".
	 * onBackPressed(); }
	 * return true; } case
	 * R.id.menu_settings: {
	 * final Intent intent =
	 * new Intent(this,
	 * ContactsPreferenceActivity
	 * .class); // Since
	 * there is only one
	 * section right now,
	 * make sure it is
	 * selected on // small
	 * screens.
	 * intent.putExtra
	 * (PreferenceActivity
	 * .EXTRA_SHOW_FRAGMENT,
	 * DisplayOptionsPreferenceFragment
	 * .class.getName()); //
	 * By default, the title
	 * of the activity
	 * should be equivalent
	 * to the fragment //
	 * title. We set this
	 * argument to avoid
	 * this. Because of a
	 * bug, the following //
	 * line isn't necessary.
	 * But, once the bug is
	 * fixed this may become
	 * necessary. //
	 * b/5045558 refers to
	 * this issue, as well
	 * as another.
	 * intent.putExtra
	 * (PreferenceActivity.
	 * EXTRA_SHOW_FRAGMENT_TITLE
	 * , R.string.
	 * activity_title_settings
	 * );
	 * startActivity(intent
	 * ); return true; }
	 * case
	 * R.id.menu_contacts_filter
	 * : {
	 * AccountFilterUtil.
	 * startAccountFilterActivityForResult
	 * ( this,
	 * SUBACTIVITY_ACCOUNT_FILTER
	 * ,
	 * mContactListFilterController
	 * .getFilter()); return
	 * true; } case
	 * R.id.menu_search: {
	 * onSearchRequested();
	 * return true; } case
	 * R.id.menu_share:
	 * shareSelectedContacts
	 * (); return true; case
	 * R.id.menu_join:
	 * joinSelectedContacts
	 * (); return true; case
	 * R.id.menu_delete:
	 * deleteSelectedContacts
	 * (); return true; case
	 * R
	 * .id.menu_import_export
	 * : {
	 */
		/** M: Change Feature */
		/*
		 * return ActivitiesUtils.doImportExport(this); } case
		 * R.id.menu_clear_frequents: {
		 * ClearFrequentsDialog.show(getFragmentManager()); return true; } case
		 * R.id.menu_help: HelpUtils.launchHelpAndFeedbackForMainScreen(this);
		 * return true; case R.id.menu_accounts: { final Intent intent = new
		 * Intent(Settings.ACTION_SYNC_SETTINGS);
		 * intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
		 * ContactsContract.AUTHORITY });
		 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		 * ImplicitIntentsUtil.startActivityInAppIfPossible(this, intent);
		 * return true; } case R.id.export_database: { final Intent intent = new
		 * Intent("com.android.providers.contacts.DUMP_DATABASE");
		 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		 * ImplicitIntentsUtil.startActivityOutsideApp(this, intent); return
		 * true; }
		 *//** M: New feature @{ */
		/*
		 *//** M: [vcs] */
		/*
		 * case R.id.menu_vcs: { Log.d(TAG,"[onOptionsItemSelected]menu_vcs");
		 * // if (mVcsController != null) { //
		 * mVcsController.onVcsItemSelected(); // } return true; }
		 *//** M: Group related */
		/*
		 * case R.id.menu_groups: { startActivity(new
		 * Intent(PeopleActivity.this, GroupBrowseActivity.class)); return true;
		 * }
		 *//** @} */
		/*
		 *//** M: [VoLTE ConfCall]Conference call @{ */
		/*
		 * case R.id.menu_conference_call: {
		 * Log.d(TAG,"[onOptionsItemSelected]menu_conference_call"); return
		 * ActivitiesUtils.conferenceCall(this); }
		 *//** @} */
		/*
		 * 
		 * } return false;
		 */
		return false;
	}

	@Override
	public boolean onSearchRequested() { // Search key pressed.
		Log.d(TAG, "[onSearchRequested]");
		if (!mActionBarAdapter.isSelectionMode()) {

			//			Log.d(TAG,"ContactsHelper.isSearchContactsLoaded:"+ContactsHelper.isSearchContactsLoaded);
			//			if(!ContactsHelper.isSearchContactsLoaded){
			//				prepareProgressDialogSpinner("提示", "正在准备数据,请稍候");
			//				return false;
			//			}

			showFabWithAnimation(false);
			mAllFragment.configureHeaderDisplay(true);
			mActionBarAdapter.setSearchMode(true);
			mActionBarAdapter.backIcon.setVisibility(View.VISIBLE);
			mActionBarAdapter.mSearchView.needHintIcon(false);
			mAllFragment.getAdapter().setCurrentSliderView(null);
			mAllFragment.getAllCountTextView().setVisibility(View.GONE);
			View contactsUnavailableView = findViewById(R.id.contacts_unavailable_view);
			contactsUnavailableView.setVisibility(View.GONE);
			mAllFragment.setEnabled(true);
			mAllFragment.closeWaitCursor();	
			setMenuVisibility(false);	
			sLastVisiable=false;

			//			onAction(Action.CHANGE_SEARCH_QUERY);
			return true;
		}
		return false;
	}

	//    public void showInputMethod(View view) {
	//        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
	//                Context.INPUT_METHOD_SERVICE);
	//        if (imm != null) {
	//            imm.showSoftInput(view, 0);
	//        }
	//    }



	private void setMenuVisibility(boolean isvisible){
		menu.findItem(R.id.mst_busyness_card_scan).setVisible(isvisible);
		menu.findItem(R.id.menu_groups).setVisible(isvisible);
		menu.findItem(R.id.menu_settings).setVisible(isvisible);
	}

	/**
	 * Share all contacts that are currently selected in mAllFragment. This
	 * method is pretty inefficient for handling large numbers of contacts. I
	 * don't expect this to be a problem.
	 */
	private void shareSelectedContacts() {
		Log.d(TAG, "[shareSelectedContacts],set ARG_CALLING_ACTIVITY.");
		final StringBuilder uriListBuilder = new StringBuilder();
		boolean firstIteration = true;
		for (Long contactId : mAllFragment.getSelectedContactIds()) {
			if (!firstIteration)
				uriListBuilder.append(':');
			final Uri contactUri = ContentUris.withAppendedId(
					Contacts.CONTENT_URI, contactId);
			final Uri lookupUri = Contacts.getLookupUri(getContentResolver(),
					contactUri);
			if (lookupUri != null) { // /M:fix null point exception(AOSP orginal
				// issue:ALPS02246075)
				List<String> pathSegments = lookupUri.getPathSegments();
				uriListBuilder
				.append(pathSegments.get(pathSegments.size() - 2));
			}
			firstIteration = false;
		}
		final Uri uri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI,
				Uri.encode(uriListBuilder.toString()));
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(Contacts.CONTENT_VCARD_TYPE);
		intent.putExtra(Intent.EXTRA_STREAM, uri);

		intent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,
				PeopleActivity.class.getName());
		ImplicitIntentsUtil.startActivityOutsideApp(this, intent);
	}

	private void joinSelectedContacts() {
		Log.d(TAG, "[joinSelectedContacts]");
		JoinContactsDialogFragment.start(this,
				mAllFragment.getSelectedContactIds());
	}

	@Override
	public void onContactsJoined() {
		Log.d(TAG, "[onContactsJoined]");
		mActionBarAdapter.setSelectionMode(false);
	}

	private void deleteSelectedContacts() {
		Log.d(TAG, "[deleteSelectedContacts]...");
		selectedCount=mAllFragment.getSelectedContactIds().size();
		ContactMultiDeletionInteraction.start(PeopleActivity.this,
				mAllFragment.getSelectedContactIds(),mHandler);
	}



	@Override
	public void onDeletionFinished() {
		Log.d(TAG, "[onDeletionFinished]");
		mActionBarAdapter.setSelectionMode(false);
	}

	/**
	 * 删除单个文件
	 * @param   sPath    被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String sPath) {
		Log.d(TAG,"deleteFile,path:"+sPath);
		boolean flag = false;
		try{
			File file = new File(sPath);
			// 路径为文件且不为空则进行删除
			if (file.isFile() && file.exists()) {
				boolean result=file.delete();
				Log.d(TAG,"deleteFile,result:"+result);
				flag = true;
			}else{
				flag=false;
			}
		}catch(Exception e){
			flag=false;
		}
		return flag;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "[onActivityResult]requestCode = " + requestCode
				+ ",resultCode = " + resultCode+" data:"+data+" bundle:"+(data==null?"null":data.getExtras()));

		switch (requestCode) {
		//		case 888:{
		//			final long[] dataIds = data.getLongArrayExtra("com.mediatek.contacts.list.pickdataresult");
		//			Log.d(TAG,"dataIds:"+dataIds+" length:"+(dataIds==null?"null":dataIds.length));
		//			if (dataIds == null || dataIds.length <= 0) {
		//				return;
		//			}			
		//			Toast.makeText(PeopleActivity.this, "dataId:"+dataIds[0], Toast.LENGTH_LONG).show();
		//			break;
		//		}
		case MST_SCAN_BUSINESS_CARD:{
			if (resultCode == RESULT_OK) {
				if (data != null && data.getBooleanExtra("GoToGallery", false)) {
					Log.d("TAG","GoToGallery");
					Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, 10001);
				}else{
					testRecognizeImage(tempPhotoPath,REQUEST_CODE_RECOGNIZE);					
				}
			}			
			return;
		}	

		case MST_PICK_PHOTO_FROM_GALLARY:
			if (resultCode==RESULT_OK) {//从相册选择照片不裁切  
				try {
					Uri selectedImage = data.getData(); //获取系统返回的照片的Uri  
					String[] filePathColumn = { MediaStore.Images.Media.DATA };   
					Cursor cursor =getContentResolver().query(selectedImage,   
							filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片  
					cursor.moveToFirst();   
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
					String resultPhotoPath = cursor.getString(columnIndex);  //获取照片路径
					cursor.close(); 
					testRecognizeImage(resultPhotoPath,REQUEST_CODE_RECOGNIZE);
				} catch (Exception e) {  
					// TODO Auto-generatedcatch block  
					e.printStackTrace();  
				}  
			}
			return;

		case SUBACTIVITY_ACCOUNT_FILTER: {
			AccountFilterUtil.handleAccountFilterResult(
					mContactListFilterController, resultCode, data);
			break;
		}

		case REQUEST_CODE_RECOGNIZE:		
			if(data!=null){
				if (resultCode == RESULT_OK) {
					showResult(data.getStringExtra(OpenApi.EXTRA_KEY_VCF),
							data.getStringExtra(OpenApi.EXTRA_KEY_IMAGE));
				}else {
					int errorCode = data.getIntExtra(openApi.ERROR_CODE, 200);
					if(errorCode!=300){
						String errorMessage = data.getStringExtra(openApi.ERROR_MESSAGE);
						Log.d(TAG, "ddebug error " + errorCode + "," + errorMessage);
						Toast.makeText(
								this,errorMessage, Toast.LENGTH_LONG).show();
					}
				}
			}else{
				Toast.makeText(PeopleActivity.this, getString(R.string.mst_business_card_scan_fail), Toast.LENGTH_LONG).show();
			}
			deleteFile(tempPhotoPath);//用户拍摄，删除
			break;

			//		case REQUEST_CODE_RECOGNIZE_NOT_DELETE:			
			//			if(data!=null){
			//				if (resultCode == RESULT_OK) {
			//					showResult(data.getStringExtra(OpenApi.EXTRA_KEY_VCF),
			//							data.getStringExtra(OpenApi.EXTRA_KEY_IMAGE));
			//				}else {
			//					int errorCode = data.getIntExtra(openApi.ERROR_CODE, 200);
			//					if(errorCode!=300){
			//						String errorMessage = data.getStringExtra(openApi.ERROR_MESSAGE);
			//						Log.d(TAG, "ddebug error " + errorCode + "," + errorMessage);
			//						Toast.makeText(
			//								this,errorMessage, Toast.LENGTH_LONG).show();
			//					}
			//				}
			//			}else{
			//				Toast.makeText(PeopleActivity.this, "名片扫描失败", Toast.LENGTH_LONG).show();
			//			}
			//			break;

			// TODO: Using the new startActivityWithResultFromFragment API this
			// should not be needed
			// anymore
		case ContactEntryListFragment.ACTIVITY_REQUEST_CODE_PICKER:
			if (resultCode == RESULT_OK) {
				mAllFragment.onPickerResult(data);
			}

			// TODO fix or remove multipicker code
			// else if (resultCode == RESULT_CANCELED && mMode ==
			// MODE_PICK_MULTIPLE_PHONES) {
			// // Finish the activity if the sub activity was canceled as back
			// key is used
			// // to confirm user selection in MODE_PICK_MULTIPLE_PHONES.
			// finish();
			// }
			break;
		}


		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO move to the fragment
		Log.d(TAG, "onKeyDown:" + mActionBarAdapter.isSearchMode());
		// if(true) return true;
		// Bring up the search UI if the user starts typing
		final int unicodeChar = event.getUnicodeChar();
		if ((unicodeChar != 0)
				// If COMBINING_ACCENT is set, it's not a unicode character.
				&& ((unicodeChar & KeyCharacterMap.COMBINING_ACCENT) == 0)
				&& !Character.isWhitespace(unicodeChar)) {
			if (mActionBarAdapter.isSelectionMode()) {
				// Ignore keyboard input when in selection mode.
				return true;
			}
			String query = new String(new int[] { unicodeChar }, 0, 1);
			Log.d(TAG, "onKeyDown:" + mActionBarAdapter.isSearchMode());
			if (!mActionBarAdapter.isSearchMode()) {
				mActionBarAdapter.setSearchMode(true);
				mActionBarAdapter.setQueryString(query);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	// public void switchToNormalMode(){
	// mActionBarAdapter.setSelectionMode(false);
	// mAllFragment.getAdapter().setSelectMode(false);
	// /// M: Fix add contact button disappear bug
	// initializeFabVisibility();
	// if(actionMode.isShowing()) actionMode.dismiss();
	// bottomBar.setVisibility(View.GONE);
	// mAllFragment.displayCheckBoxes(false);
	// }

	public void exitSearchMode(){
		Log.d(TAG,"exitSearchMode");

		updateViewConfiguration(true);		

		mActionBarAdapter.setSearchMode(false);
		mAllFragment.exitSearchMode();
		//		mActionBarAdapter.mSearchView.setQuery(null, false);
		mAllFragment.setQueryString(null, false);
		// / M: Fix add contact button disappear bug
		mAllFragment.configureHeaderDisplay(false);
		mActionBarAdapter.mSearchView.clearFocus();
		mActionBarAdapter.backIcon.setVisibility(View.GONE);
		mActionBarAdapter.mSearchView.needHintIcon(true);
		initializeFabVisibility();

		setMenuVisibility(true);
	}
	@Override
	public void onBackPressed() {
		Log.d(TAG, "[onBackPressed]");

		if(mAllFragment.getAdapter().getCurrentSliderView()!=null){
			mAllFragment.getAdapter().getCurrentSliderView().close(true);
			mAllFragment.getAdapter().setCurrentSliderView(null);
			return;
		}

		if (mActionBarAdapter.isSelectionMode()) {
			switchToEditMode(false);
		} else if (mActionBarAdapter.isSearchMode()) {
			Log.d(TAG, "[onBackPressed12]");
			exitSearchMode();
			/** M: New Feature @{ */
		} else if (!ContactsSystemProperties.MTK_PERF_RESPONSE_TIME
				&& isTaskRoot()) {
			// Instead of stopping, simply push this to the back of the stack.
			// This is only done when running at the top of the stack;
			// otherwise, we have been launched by someone else so need to
			// allow the user to go back to the caller.
			moveTaskToBack(false);
			/** @} */
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mActionBarAdapter.onSaveInstanceState(outState);

		// Clear the listener to make sure we don't get callbacks after
		// onSaveInstanceState,
		// in order to avoid doing fragment transactions after it.
		// TODO Figure out a better way to deal with the issue.
		mDisableOptionItemSelected = true;
		mActionBarAdapter.setListener(null);
		if (mTabPager != null) {
			mTabPager.setOnPageChangeListener(null);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// In our own lifecycle, the focus is saved and restore but later taken
		// away by the
		// ViewPager. As a hack, we force focus on the SearchView if we know
		// that we are searching.
		// This fixes the keyboard going away on screen rotation
		if (mActionBarAdapter.isSearchMode()) {
			Log.d(TAG, "will setFocusOnSearchView");
			mActionBarAdapter.setFocusOnSearchView();
		}
	}

	@Override
	public DialogManager getDialogManager() {
		return mDialogManager;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.floating_action_button:
			Log.d(TAG, "[onClick]floating_action_button");
			Intent intent = new Intent(Intent.ACTION_INSERT,
					Contacts.CONTENT_URI);
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				intent.putExtras(extras);
			}
			try {
				ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this,
						intent);
			} catch (ActivityNotFoundException ex) {
				Toast.makeText(PeopleActivity.this, R.string.missing_app,
						Toast.LENGTH_SHORT).show();
			}
			break;
			// / M: Add for SelectAll/DeSelectAll Feature. @{
		case R.id.selection_count_text:
			Log.d(TAG, "[onClick]selection_count_text");
			// if the Window of this Activity hasn't been created,
			// don't show Popup. because there is no any window to attach .
			if (getWindow() == null) {
				Log.w(TAG, "[onClick]current Activity window is null");
				return;
			}
			if (mSelectionMenu == null || !mSelectionMenu.isShown()) {
				View parent = (View) view.getParent();
				mSelectionMenu = updateSelectionMenu(parent);
				mSelectionMenu.show();
			} else {
				Log.w(TAG,
						"mSelectionMenu is already showing, ignore this click");
			}
			break;

			// / @}
		default:
			Log.wtf(TAG, "Unexpected onClick event from " + view);
		}
	}

	/**
	 * Returns the tab position adjusted for the text direction.
	 */
	private int getTabPositionForTextDirection(int position) {
		if (isRTL()) {
			return TabState.COUNT - 1 - position;
		}
		return position;
	}

	// / M: [VCS]Voice Search Contacts Feature @{
	// private VcsController mVcsController = null;

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent ev) {
	// // if (mVcsController != null) {
	// // mVcsController.dispatchTouchEventVcs(ev);
	// // }
	// return super.dispatchTouchEvent(ev);
	// }

	/**
	 * M: Used to dismiss the dialog floating on.
	 * 
	 * @param v
	 */
	@SuppressWarnings({ "UnusedDeclaration" })
	public void onClickDialog(View v) {
		// if (mVcsController != null) {
		// mVcsController.onVoiceDialogClick(v);
		// }
	}

	// / @}

	// / M: Add for SelectAll/DeSelectAll Feature. @{
	private DropDownMenu mSelectionMenu;

	/**
	 * add dropDown menu on the selectItems.The menu is "Select all" or
	 * "Deselect all"
	 * 
	 * @param customActionBarView
	 * @return The updated DropDownMenu
	 */
	private DropDownMenu updateSelectionMenu(View customActionBarView) {
		Log.d(TAG, "[updateSelectionMenu]");
		DropMenu dropMenu = new DropMenu(this);
		// new and add a menu.
		DropDownMenu selectionMenu = dropMenu.addDropDownMenu(
				(Button) customActionBarView
				.findViewById(R.id.selection_count_text),
				R.menu.mtk_selection);

		Button selectView = (Button) customActionBarView
				.findViewById(R.id.selection_count_text);
		// when click the selectView button, display the dropDown menu.
		selectView.setOnClickListener(this);
		MenuItem item = selectionMenu.findItem(R.id.action_select_all);

		// get mIsSelectedAll from fragment.
		mAllFragment.updateSelectedItemsView();
		// the menu will show "Deselect_All/ Select_All".
		if (mAllFragment.isSelectedAll()) {
			// dropDown menu title is "Deselect all".
			item.setTitle(R.string.menu_select_none);
			dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					// clear select all items
					mAllFragment.updateCheckBoxState(false);
					mAllFragment.displayCheckBoxes(false);
					mActionBarAdapter.setSelectionMode(false);
					initializeFabVisibility();
					return true;
				}
			});
		} else {
			// dropDown Menu title is "Select all"
			item.setTitle(R.string.menu_select_all);
			dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					mAllFragment.updateCheckBoxState(true);
					mAllFragment.displayCheckBoxes(true);
					return true;
				}
			});
		}
		return selectionMenu;
	}

	//	// / @}
	//
	//	// / M: Listen sim change intent @{
	//	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	//		@Override
	//		public void onReceive(Context context, Intent intent) {
	//			Log.i(TAG, "[onReceive] Received Intent:" + intent);
	//			// M: fix ALPS02477744 "select all" menu show left when turn on
	//			// airmode@{
	//			if (mSelectionMenu != null && mSelectionMenu.isShown()) {
	//				Log.i(TAG, "[onReceive] mSelectionMenu is diss!");
	//				mSelectionMenu.diss();
	//			}
	//			// @}
	//
	//			updateViewConfiguration(true);
	//			updateFragmentsVisibility();
	//		}
	//	};

	// / @}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object onFragmentCallback(int what, Object obj) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onFragmentCallback,what:" + what + " obj:" + obj);
		switch (what) {
		case FragmentCallbacks.SWITCH_TO_SEARCH_MODE: {
			onSearchRequested();
			break;
		}
		case FragmentCallbacks.DELETE_CONTACTS: {
			Log.d(TAG, "[deleteSelectedContacts]...");
			selectedCount=1;
			ContactMultiDeletionInteraction.start(PeopleActivity.this,
					(TreeSet<Long>) obj,null);
			break;
		}

		case FragmentCallbacks.MENU_CONTACTS_FILTER: {
			Log.d(TAG, "[MENU_CONTACTS_FILTER]...");
			AccountFilterUtil.startAccountFilterActivityForResult(this,
					SUBACTIVITY_ACCOUNT_FILTER,
					mContactListFilterController.getFilter());
			break;
		}

		case FragmentCallbacks.SHOW_ADD_FAB: {
			hideFabForIndexerScroll=Integer.parseInt(obj.toString())==0?true:false;
			mFloatingActionButtonContainer.setVisibility(!hideFabForIndexerScroll?View.VISIBLE:View.GONE);
			//			showFabWithAnimation(!hideFabForIndexerScroll);
			//			initializeFabVisibility();
			break;
		}
		}
		return null;
	}

	private ContactSaveCompletedReceiver myReceiver;

	//	public void testRecognizeCapture() {
	//		if (openApi.isCamCardInstalled(this)) {
	//			if (openApi.isExistAppSupportOpenApi(this)) {
	//				openApi.recognizeCardByCapture(this, REQUEST_CODE_RECOGNIZE,
	//						params);
	//			} else {
	//				Toast.makeText(this, "No app support openapi",
	//						Toast.LENGTH_LONG).show();
	//				Log.d(TAG, "camcard download link:" + openApi.getDownloadLink());
	//			}
	//		} else {
	//			Toast.makeText(this, "No CamCard", Toast.LENGTH_LONG).show();
	//			Log.d(TAG, "camcard download link:" + openApi.getDownloadLink());
	//		}
	//	}

	public void testRecognizeImage(String path,int requestCode) {
		Log.d(TAG,"testRecognizeImage:"+path);
		if (openApi.isExistAppSupportOpenApi(this)) {
			openApi.recognizeCardByImage(this, path, requestCode,
					params);
		} else {
			Toast.makeText(this, "No app support openapi", Toast.LENGTH_LONG)
			.show();
			Log.d(TAG, "camcard download link:" + openApi.getDownloadLink());
		}
	}

	private String photoPath;

	private void showResult(String vcf, String path) {
		// Intent intent = new Intent(this,
		// MstBusinessCardShowResultActivity.class);
		// intent.putExtra("result_vcf", vcf);
		// intent.putExtra("result_trimed_image", path);
		// startActivity(intent);
		//		textView.setText(vcf);
		Log.d(TAG,"showResult,path:"+path+" vcf:"+vcf);
		if(TextUtils.isEmpty(vcf)||TextUtils.isEmpty(path)){
			Toast.makeText(PeopleActivity.this, getString(R.string.mst_business_card_scan_fail), Toast.LENGTH_LONG).show();
			return;
		}
		photoPath = path;

		// List<AddressBean> addressBeans =
		// MstVcfUtils.importVCFFileContact(vcf);
		// Log.d(TAG,addressBeans.size()+"");
		// for (AddressBean addressBean : addressBeans) {
		// Log.d(TAG,"tureName : " + addressBean.getTrueName());
		// Log.d(TAG,"mobile : " + addressBean.getMobile());
		// Log.d(TAG,"workMobile : " + addressBean.getWorkMobile());
		// Log.d(TAG,"Email : " + addressBean.getEmail());
		// Log.d(TAG,"--------------------------------");
		// }

		final VCardEntryConstructor constructor = new VCardEntryConstructor(0,
				new Account("Phone", "Local Phone Account"), null);
		constructor.addEntryHandler(mstBusinessCardResults);
		InputStream is = new ByteArrayInputStream(vcf.getBytes());
		Log.d(TAG, "vcf:" + vcf + " is:" + is + " path:" + path);
		boolean successful = false;
		try {
			// if (uri != null) {
			// Log.i(TAG, "start importing one vCard (Uri: " + uri + ")");
			// is = mResolver.openInputStream(uri);
			// } else if (request.data != null){
			// Log.i(TAG, "start importing one vCard (byte[])");
			// is = new ByteArrayInputStream(request.data);
			// }

			if (is != null) {
				successful = readOneVCard(is, 0, null, constructor,
						possibleVCardVersions);
			}
		} catch (Exception e) {
			successful = false;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	final static int VCARD_VERSION_V21 = 1;
	final static int VCARD_VERSION_V30 = 2;
	int[] possibleVCardVersions = new int[] { VCARD_VERSION_V21,
			VCARD_VERSION_V30 };

	private VCardParser mVCardParser;

	private boolean readOneVCard(InputStream is, int vcardType, String charset,
			final VCardInterpreter interpreter,
			final int[] possibleVCardVersions) {
		boolean successful = false;
		final int length = possibleVCardVersions.length;
		for (int i = 0; i < length; i++) {
			final int vcardVersion = possibleVCardVersions[i];
			try {
				if (i > 0 && (interpreter instanceof VCardEntryConstructor)) {
					// Let the object clean up internal temporary objects,
					((VCardEntryConstructor) interpreter).clear();
				}

				// We need synchronized block here,
				// since we need to handle mCanceled and mVCardParser at once.
				// In the worst case, a user may call cancel() just before
				// creating
				// mVCardParser.
				synchronized (this) {
					mVCardParser = (vcardVersion == VCARD_VERSION_V30 ? new VCardParser_V30(
							vcardType) : new VCardParser_V21(vcardType));
					// if (isCancelled()) {
					// Log.i(TAG,
					// "ImportProcessor already recieves cancel request, so " +
					// "send cancel request to vCard parser too.");
					// mVCardParser.cancel();
					// }
				}
				Log.d(TAG, "mVCardParser:" + mVCardParser);
				mVCardParser.parse(is, interpreter);

				successful = true;
				break;
			} catch (IOException e) {
				Log.e(TAG, "IOException was emitted: " + e.getMessage());
			} catch (VCardNestedException e) {
				// This exception should not be thrown here. We should instead
				// handle it
				// in the preprocessing session in ImportVCardActivity, as we
				// don't try
				// to detect the type of given vCard here.
				//
				// TODO: Handle this case appropriately, which should mean we
				// have to have
				// code trying to auto-detect the type of given vCard twice
				// (both in
				// ImportVCardActivity and ImportVCardService).
				Log.e(TAG, "Nested Exception is found.");
			} catch (VCardNotSupportedException e) {
				Log.e(TAG, e.toString());
			} catch (VCardVersionException e) {
				if (i == length - 1) {
					Log.e(TAG,
							"Appropriate version for this vCard is not found.");
				} else {
					// We'll try the other (v30) version.
				}
			} catch (VCardException e) {
				Log.e(TAG, e.toString());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return successful;
	}

	OpenApi openApi = OpenApi.instance("09FDTD0Ma8BV0SFRP2JNU74L7C351C3D");

	OpenApiParams params = new OpenApiParams() {
		{
			this.setRecognizeLanguage(""); //设置识别语言，为空则以 名片全能王APP里面设置的识别语言为准
			this.setReturnCropImage(true); //是否需要返回切边矫正的名片图片
			//Local Key
			this.setSaveCard(false);//返回的名片识别结果是否需要保存在名片全能王APP中
			this.setTrimeEnhanceOption(false);//名片识别过程中是否需要显示名片切边动画
		}
	};

	public void createBusinessCardGroup(){
		ContentValues values = new ContentValues();
		values.put(Groups.ACCOUNT_TYPE, "Local Phone Account");
		values.put(Groups.ACCOUNT_NAME, "Phone");
		values.put(Groups.TITLE, getString(R.string.mst_business_card));
		values.put(Groups._ID,0);
		values.put(Groups.GROUP_IS_READ_ONLY,1);

		// Create the new group
		final Uri groupUri = getContentResolver().insert(Groups.CONTENT_URI, values);

		// If there's no URI, then the insertion failed. Abort early because group members can't be
		// added if the group doesn't exist
		if (groupUri == null) {
			Log.e(TAG, "Couldn't create group with label " + "名片");
			addGroupSuccess=false;
			return;
		}

		//向搜索表中插入名片名称
		Uri baseUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "add_to_mst_dialer_search_table");
		Uri uri = baseUri.buildUpon().appendPath(getString(R.string.mst_business_card)).build();
		Log.d(TAG,"uri"+uri.toString());
		Cursor c =null;
		try{
			c =  getContentResolver().query(uri, null, null, null, null);
			Log.d(TAG,"c:"+c);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//        	c.close();
			c=null;
		}
	}


	private int mSubId = SubInfoUtils.getInvalidSubId();
	public class ContactSaveCompletedReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			Bundle bundle=intent.getExtras();
			if(bundle==null) return;
			Log.d(TAG,"onReceive,action:"+action+" bundle:"+bundle);
			if(TextUtils.equals(action, BROADCASTACTION_STRING)){				
				boolean isForBusinessCard=bundle.getBoolean("isForBusinessCard");
				Log.d(TAG,"onReceive,isForBusinessCard:"+isForBusinessCard+" bundle:"+bundle);
				if(!isForBusinessCard) return;
				String lookupUriString=bundle.getString("lookupUri");
				Uri uri=Uri.parse(lookupUriString);
				List<String> pathSegments = uri.getPathSegments();
				int segmentCount = pathSegments.size();
				if (segmentCount < 3) {
					return;
				}
				String lookupKey = pathSegments.get(2);
				Log.d(TAG,"onReceive,action:"+action+" lookupUriString:"+lookupUriString+" intent:"+intent+" bundle:"+bundle);
				final ContentResolver resolver=getContentResolver();
				Cursor c = resolver.query(uri, new String[]{"_id","name_raw_contact_id","index_in_sim"}, null, null, null);
				if (c == null) {
					return;
				}
				long rawContactId=0;
				int indexInSim=0;
				try {
					if (c.moveToFirst()) {
						//                    long contactId = c.getLong(0);
						rawContactId=c.getLong(1);
						indexInSim=c.getInt(2);
					}
				} finally {
					c.close();
				}

				if(TextUtils.equals(action, BROADCASTACTION_STRING)){
					Log.d(TAG,"add1 to business group&&attach photo");
					Intent saveIntent = ContactSaveService.createGroupUpdateIntentForIcc(PeopleActivity.this,
							0, null, new long[]{rawContactId},
							null, 
							PeopleActivity.this.getClass(),
							"0",
							getString(R.string.mst_business_card), mSubId, new int[]{indexInSim},
							null,
							new AccountWithDataSet("Phone", "Local Phone Account", null));
					Log.d(TAG,"saveIntent:"+saveIntent);
					PeopleActivity.this.startService(saveIntent);				
				}

				//处理名片图片			
				Log.d(TAG, "photoPath:"+photoPath);
				if(TextUtils.isEmpty(photoPath)) return;
				renameFile(photoPath, lookupKey+".jpg");
			}else if(TextUtils.equals(action, ContactsIntent.MULTICHOICE.ACTION_MULTICHOICE_PROCESS_FINISH)){
				//				int total=bundle.getInt("total");
				if(mProgressDialog!=null){
					mProgressDialog.dismiss();
					mProgressDialog=null;
				}
				//modify by lgy for 3376400
				if(selectedCount > 1) {	                
					Toast.makeText(PeopleActivity.this, "已删除"+selectedCount+"个联系人", Toast.LENGTH_LONG).show();
				} else if(selectedCount == 1){	                
					Toast.makeText(PeopleActivity.this, R.string.slide_contact_deleted, Toast.LENGTH_LONG).show();
				}

			}
		}

	}

	/** *//**文件重命名 
	 * @param path 文件目录 
	 * @param oldname  原来的文件名 
	 * @param newname 新文件名 
	 */ 
	public void renameFile(String path,String newname){
		try{
			String oldname=path.substring(path.lastIndexOf("/")+1);
			Log.d(TAG,"path:"+path+" oldname:"+oldname+" newname:"+newname);
			if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名 
				File oldfile=new File(path); 
				File newfile=new File(path.substring(0,path.lastIndexOf("/")+1)+newname); 
				Log.d(TAG,"oldfile:"+oldfile+" newfile:"+newfile+" path:"+newfile.getPath());
				if(!oldfile.exists()){
					return;//重命名文件不存在
				}
				if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
					Log.d(TAG,newname+"已经存在！"); 
				else{ 
					oldfile.renameTo(newfile); 
				} 
			}else{
				Log.d(TAG,"新文件名和旧文件名相同...");
			}
		}catch(Exception e){
			Log.d(TAG,"renameFile,e:"+e);
		}
	}

	private static final int REQUEST_CODE_RECOGNIZE = 0x1001;
	private static final int REQUEST_CODE_RECOGNIZE_NOT_DELETE=0x1002;
	private boolean addGroupSuccess=true;
	private MstBusinessCardResults mstBusinessCardResults;
	public static final String BROADCASTACTION_STRING="com.android.contacts.mst.MstBusinessCardResults";

	//add by lgy for 2824400
	public void updateEmptyOrNot() {
		mProviderStatusWatcher.start();
	} 
}
