/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.contacts.list;

import android.widget.CheckBox;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.list.ContactListFilter;
import java.util.Arrays;
import java.util.TreeSet;
import mst.app.dialog.ProgressDialog;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MstSearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.MstSearchView.OnQueryTextListener;
import android.widget.MstSearchView.OnCloseListener;
import android.widget.MstSearchView.OnSuggestionListener;
import mst.view.menu.BottomWidePopupMenu;
import  mst.view.menu.bottomnavigation.BottomNavigationView;
import  mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.toolbar.Toolbar;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;
import mst.view.menu.BottomWidePopupMenu;

import mst.widget.MstIndexBar;
import com.android.contacts.common.list.ContactListAdapter.ContactQuery;
import android.util.Log;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import mst.provider.ContactsContract.Data;
import mst.provider.ContactsContract.RawContacts;
import mst.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;
//import android.widget.SearchView.OnCloseListener;
//import android.widget.SearchView.OnQueryTextListener;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.editor.AggregationSuggestionEngine.RawContact;
import com.android.contacts.list.ContactsRequest;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.DropMenu.DropDownMenu;
import com.mst.privacy.PrivacyUtils;

/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting multiple contacts.
 */

public class ContactListMultiChoiceActivity extends ContactsActivity implements
View.OnCreateContextMenuListener, OnQueryTextListener, OnClickListener, OnCloseListener,
OnFocusChangeListener,FragmentCallbacks {
	private static final String TAG = "ContactListMultiChoiceActivity";

	private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;
	public static final int CONTACTGROUPLISTACTIVITY_RESULT_CODE = 1;
	public static final int RESULT_PICK_CONTACT_FOR_RECORD_CONTACTS=21;
	public static final int RESULT_PICK_CONTACT_FOR_PRIVACY_CONTACTS=22;

	private static final String KEY_ACTION_CODE = "actionCode";
	private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

	public static final String RESTRICT_LIST = "restrictlist";

	private ContactsIntentResolverEx mIntentResolverEx;
	protected AbstractPickerFragment mListFragment;

	private int mActionCode = -1;

	private ContactsRequest mRequest;
	public MstSearchView mSearchView;
	public View mSearchContainer;

	// the dropdown menu with "Select all" and "Deselect all"
	private DropDownMenu mSelectionMenu;
	private boolean mIsSelectedAll = true;
	private boolean mIsSelectedNone = true;
	// if Search Mode now, decide the menu display or not.
	private boolean mIsSearchMode = false;

	// for CT NEW FEATURE
	private int mNumberBalance = 100;

	private enum SelectionMode {
		SearchMode, ListMode
	};

	public ContactListMultiChoiceActivity() {
		Log.i(TAG, "[ContactListMultiChoiceActivity]new.");
		mIntentResolverEx = new ContactsIntentResolverEx(this);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		if (fragment instanceof AbstractPickerFragment) {
			mListFragment = (AbstractPickerFragment) fragment;
		}
	}

	private boolean isOnResumeInSearchMode=false;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mIsSearchMode) isOnResumeInSearchMode=true;
		//		showActionMode(true);
		if(mRequest.getActionCode()==ContactsRequestAction.ACTION_MST_PRIVACY_CONTACTS_LIST||mRequest.getActionCode()==ContactsRequestAction.ACTION_MST_PRIVACY_ADD_CONTACTS_LIST){
			Log.d(TAG,"ACTION_MST_PRIVACY_CONTACTS_LIST");
			if(PrivacyUtils.getCurrentAccountId()<=0){
				Toast.makeText(ContactListMultiChoiceActivity.this, "当前非隐私模式", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	private Handler mHandler = new Handler();
	private Runnable mShowActionModeRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG,"mActionCode:"+mActionCode);
			if(ContactsRequestAction.ACTION_MST_RECORD_CONTACTS_LIST!=mActionCode
					&&ContactsRequestAction.ACTION_MST_PRIVACY_CONTACTS_LIST!=mActionCode){
				showActionMode(true);
			}
		}
	};

	private MenuItem addRecordContactsMenuItem;
	private BottomWidePopupMenu bottomWidePopupMenu;
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		if (RequestPermissionsActivity.startPermissionActivity(this)) {
			Log.i(TAG,"[onCreate]startPermissionActivity,return.");
			return;
		}
		// for ct new feature
		Intent mmsIntent = this.getIntent();
		Log.i(TAG, "[onCreate1]...");
		if (mmsIntent != null) {
			mNumberBalance = mmsIntent.getIntExtra("NUMBER_BALANCE", 100);
			Log.i(TAG, "[onCreate]mNumberBalance from intent = " + mNumberBalance);

		}

		if (savedState != null) {
			mActionCode = savedState.getInt(KEY_ACTION_CODE);
			mNumberBalance = savedState.getInt("NUMBER_BALANCE");
			Log.i(TAG, "[onCreate]mNumberBalance from savedState = " + mNumberBalance);

		}

		// Extract relevant information from the intent
		mRequest = mIntentResolverEx.resolveIntent(getIntent());
		if (!mRequest.isValid()) {
			Log.w(TAG, "[onCreate]Request is invalid!");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		setMstContentView(R.layout.contact_picker);

		toolbar = getToolbar();
		//		toolbar.setElevation(0f);		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"NavigationOnClickListener");
				if(mIsSearchMode){
					back();
				}else{
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
			}
		});

		setupActionModeWithDecor(toolbar);

		initializeSearchView();
		Bundle bundle = new Bundle();
		mActionCode = mRequest.getActionCode();
		actionMode=getActionMode();
		mHandler.postDelayed(mShowActionModeRunnable,300);
		actionMode.setNagativeText(getString(R.string.mst_cancel));
		actionMode.setPositiveText(getString(R.string.mst_actionmode_selectall));
		actionMode.bindActionModeListener(new ActionModeListener(){
			/**
			 * ActionMode上面的操作按钮点击时触发，在这个回调中，默认提供两个ID使用，
			 * 确定按钮的ID是ActionMode.POSITIVE_BUTTON,取消按钮的ID是ActionMode.NAGATIVE_BUTTON
			 * @param view
			 */
			public void onActionItemClicked(Item item){
				Log.d(TAG,"onActionItemClicked,itemid:"+item.getItemId());
				switch (item.getItemId()) {
				case ActionMode.POSITIVE_BUTTON:	{
					//					int selected=mListFragment.getAdapter().getSelectedContactIds().size();
					int all=mListFragment.getAdapter().getCount();
					//					Log.d(TAG,"all:"+all+" selected:"+selected);
					boolean mIsSelectedAll = mListFragment.isSelectedAll();
					TreeSet<Long> SelectedContactIds = mListFragment.getAdapter().getSelectedContactIds();
					Log.d(TAG,"mIsSelectedAll:"+mIsSelectedAll);
					if(mIsSelectedAll){
						SelectedContactIds.clear();
						mListFragment.updateSelectedItemsView();
					}else{
						SelectedContactIds.clear();
						for(int i=0;i<all;i++){
							Cursor cursor = (Cursor)(mListFragment.getAdapter().getItem(i));
							long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
							Log.d(TAG, "contactId = " + contactId);
							SelectedContactIds.add(contactId);
						}
						mListFragment.updateSelectedItemsView();
					}

					mListFragment.getAdapter().notifyDataSetChanged();
					break;
				}

				case ActionMode.NAGATIVE_BUTTON:
					//					switchToEditMode(false);	
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;
				default:
					break;
				}
			}

			/**
			 * ActionMode显示的时候触发
			 * @param actionMode
			 */
			public void onActionModeShow(ActionMode actionMode){

			}

			/**
			 * ActionMode消失的时候触发
			 * @param actionMode
			 */
			public void onActionModeDismiss(ActionMode actionMode){

			}
		});



		bottomBar = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
		bottomBar.setVisibility(View.GONE);
		bottomBar.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {

			@Override
			public boolean onNavigationItemSelected(MenuItem arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onNavigationItemSelected,arg0.getItemId():"+arg0.getItemId());
				switch (arg0.getItemId()) {
				case R.id.mst_ok:
					Log.d(TAG,"mst_ok");
					if (mListFragment instanceof MultiDuplicationPickerFragment) {
						Log.d(TAG, "[onClick]Send result for copy action");
						setResult(ContactImportExportActivity.RESULT_CODE);
					}
					if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
						PhoneAndEmailsPickerFragment fragment =
								(PhoneAndEmailsPickerFragment) mListFragment;
						fragment.setNumberBalance(mNumberBalance);
						fragment.onOptionAction();
					} else {
						mListFragment.onOptionAction();
					}

					break;

				default:
					break;
				} 
				return false;
			}
		});


		// Disable Search View in listview
		//		if (mSearchView != null) {
		//			mSearchView.setVisibility(View.GONE);
		//		}
		configureListFragment();
		//		showActionBar(SelectionMode.ListMode);

		mIndexBar = (MstIndexBar) findViewById(R.id.index_bar);
		mListFragment.setmIndexBar(mIndexBar);

		bottomWidePopupMenu = new BottomWidePopupMenu(this);
		bottomWidePopupMenu.inflateMenu(R.menu.add_privacy_contacts_bottom_menu);
		bottomWidePopupMenu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onItemClicked(MenuItem item) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onItemClicked Item:"+item.getTitle());
				switch(item.getItemId()){
				case R.id.mst_add_privacy_contacts_from_exist_contact:{
					Intent intent = new Intent("com.android.contacts.action.MST_PRIVACY_ADD_CONTACTS_LIST");
					intent.setType("vnd.android.cursor.dir/contact");
					startActivityForResult(intent, RESULT_PICK_CONTACT_FOR_PRIVACY_CONTACTS);
					break;
				}
				case R.id.mst_add_privacy_contacts_manual:{
					Log.d(TAG, "[onClick]mst_add_privacy_contacts_manual");
					Intent intent = new Intent(Intent.ACTION_INSERT,
							Contacts.CONTENT_URI);
					intent.putExtra("privacy_id", PrivacyUtils.getCurrentAccountId());
					Bundle extras = getIntent().getExtras();
					if (extras != null) {
						intent.putExtras(extras);
					}
					try {
						ImplicitIntentsUtil.startActivityInApp(
								ContactListMultiChoiceActivity.this, intent);
					} catch (ActivityNotFoundException ex) {
						Toast.makeText(ContactListMultiChoiceActivity.this,
								R.string.missing_app, Toast.LENGTH_SHORT)
								.show();
					}
					break;
				}
				default:
					break;
				}
				return true;
			}
		});


		addOnSoftKeyBoardVisibleListener();
	}

	private boolean sLastVisiable=false;
	public void addOnSoftKeyBoardVisibleListener() {
		final View decorView = getWindow().getDecorView();
		decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if(!mIsSearchMode||isOnResumeInSearchMode) {
					isOnResumeInSearchMode=false;
					return;
				}
				Rect rect = new Rect();
				decorView.getWindowVisibleDisplayFrame(rect);
				int displayHight = rect.bottom - rect.top;
				int hight = decorView.getHeight();
				boolean visible = (double) displayHight / hight < 0.8;

				Log.d(TAG, "DecorView display hight = " + displayHight);
				Log.d(TAG, "DecorView hight = " + hight);
				Log.d(TAG, "softkeyboard visible = " + visible);

				if(!visible && visible != sLastVisiable){
					mSearchView.clearFocus();
				}
				sLastVisiable = visible;
			}
		});
	}


	private MstIndexBar mIndexBar;
	private String mQueryString;
	public ImageView backIcon;
	private void initializeSearchView() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		//		View backIconView=inflater.inflate(R.layout.mst_back_icon_view, null);
		//
		//		backIcon=(ImageView)backIconView.findViewById(R.id.mst_back_icon_img);
		//		backIconView.setOnClickListener(new View.OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				Log.d(TAG,"backIconView onclick");
		//				// TODO Auto-generated method stub
		//				back();
		//			}
		//		});
		//		backIcon.setVisibility(View.GONE);
		//		backIcon.setColorFilter(getResources().getColor(R.color.mst_toolbar_icon_normal_color));
		//		toolbar.addView(backIconView,
		//				new mst.widget.toolbar.Toolbar.LayoutParams(Gravity.CENTER_VERTICAL | Gravity.START));

		mSearchContainer = inflater.inflate(R.layout.mst_search_bar_expanded,
				null);
		mSearchView = (MstSearchView) mSearchContainer
				.findViewById(R.id.search_view);

		int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);    
		EditText  textView = (EditText ) mSearchView.findViewById(id); 
		textView.setBackground(getResources().getDrawable(R.drawable.mst_searchview_bg));
		LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams1.setMargins(0,0,0,0);
		textView.setLayoutParams(layoutParams1);

		int search_edit_frame_id=mSearchView.getContext().getResources().getIdentifier("android:id/search_edit_frame", null, null);
		View search_edit_frame=mSearchView.findViewById(search_edit_frame_id); 		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.mst_choice_searchview_edittext_margin_left_or_right),0,
				getResources().getDimensionPixelSize(R.dimen.mst_choice_searchview_edittext_margin_left_or_right),0);
		search_edit_frame.setLayoutParams(layoutParams);	


		//		toolbar.setBackgroundColor(getResources().getColor(R.color.mst_toolbar_background_color));
		mSearchView.needHintIcon(false);
		//		mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		//		mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
		Log.d(TAG,"mSearchContainer:"+mSearchContainer);
		toolbar.addView(mSearchContainer,
				new mst.widget.toolbar.Toolbar.LayoutParams(Gravity.CENTER_VERTICAL | Gravity.START));
		//		mSearchView = (MstSearchView)mToolbar.getMenu().findItem(R.id.menu_search).getActionView();
		mSearchView.setIconifiedByDefault(false);
		mSearchView.setQueryHint(getString(R.string.mst_search_contacts_hint));
		mSearchView.setQueryHintTextColor(getResources().getColor(R.color.mst_searchview_hint_text_color));
		mSearchView.setOnQueryTextListener(new OnQueryTextListener(){
			@Override
			public boolean onQueryTextChange(String queryString) {
				Log.d(TAG,"onQueryTextChange,queryString:"+queryString);
				if (queryString.equals(mQueryString)) {
					return true;
				}
				mQueryString = queryString;

				//				if(mListFragment instanceof MultiGroupAddPickerFragment){
				//					Log.d(TAG,"onQueryTextChange MultiGroupAddPickerFragment");
				//					mListFragment.setQueryString(mQueryString, true);
				//					return false;
				//				}

				mListFragment.startSearch(mQueryString);
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String str) {
				return false;
			}
		});

		mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus){
				if(hasFocus){
					Log.d(TAG,"hasFocus,v:"+v);
					showInputMethod(mSearchView.findFocus());
					mIsSearchMode=true;
					sLastVisiable=false;
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "[onDestroy]");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(TAG, "[onSaveInstanceState]mActionCode = " + mActionCode + ",mNumberBalance = "
				+ mNumberBalance);
		outState.putInt(KEY_ACTION_CODE, mActionCode);
		// for ct new feature
		outState.putInt("NUMBER_BALANCE", mNumberBalance);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		/* MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mtk_list_multichoice, menu);

        MenuItem optionItem = menu.findItem(R.id.search_menu_item);
        optionItem.setTitle(R.string.menu_search);*/

		return true;
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "[onClick]v= " + v);
		final int resId = v.getId();
		switch (resId) {
		case R.id.search_menu_item:
			Log.i(TAG, "[onClick]resId = search_menu_item ");
			mListFragment.updateSelectedItemsView();
			showActionBar(SelectionMode.SearchMode);
			closeOptionsMenu();
			break;

		case R.id.menu_option:
			Log.i(TAG, "[onClick]resId = menu_option ");
			if (mListFragment instanceof MultiDuplicationPickerFragment) {
				Log.d(TAG, "[onClick]Send result for copy action");
				setResult(ContactImportExportActivity.RESULT_CODE);
			}
			if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
				PhoneAndEmailsPickerFragment fragment =
						(PhoneAndEmailsPickerFragment) mListFragment;
				fragment.setNumberBalance(mNumberBalance);
				fragment.onOptionAction();
			} else {
				mListFragment.onOptionAction();
			}
			break;

		case R.id.select_items:
			Log.i(TAG, "[onClick]resId = select_items ");
			// if the Window of this Activity hasn't been created,
			// don't show Popup. because there is no any window to attach .
			if (getWindow() == null) {
				Log.w(TAG, "[onClick]current Activity dinsow is null");
				return;
			}
			if (mSelectionMenu == null || !mSelectionMenu.isShown()) {
				View parent = (View) v.getParent();
				mSelectionMenu = updateSelectionMenu(parent);
				mSelectionMenu.show();
			} else {
				Log.w(TAG, "[onClick]mSelectionMenu is already showing, ignore this click");
			}
			break;

		default:
			break;
		}
		return;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		Log.i(TAG, "[onMenuItemSelected]itemId = " + itemId);
		// if click the search menu, into the SearchMode and disable the search
		// menu
		if (itemId == R.id.search_menu_item) {
			mListFragment.updateSelectedItemsView();
			mIsSelectedNone = mListFragment.isSelectedNone();
			showActionBar(SelectionMode.SearchMode);
			item.setVisible(false);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * Creates the fragment based on the current request.
	 */
	private void configureListFragment() {
		//		if (mActionCode == mRequest.getActionCode()) {
		//			Log.w(TAG, "[configureListFragment]return ,mActionCode = " + mActionCode);
		//			return;
		//		}

		Bundle bundle = new Bundle();
		mActionCode = mRequest.getActionCode();
		Log.i(TAG, "[configureListFragment] action code is " + mActionCode);

		switch (mActionCode) {
		case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS://Mms,Rcse,Email
			mListFragment = new MultiBasePickerFragment();
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setForContactsChoice(true);
			mListFragment.setCallbacks(this);			
			break;

		case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS
		| ContactsIntentResolverEx.MODE_MASK_VCARD_PICKER:
			mListFragment = new MultiVCardPickerFragment();
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setForContactsChoice(true);
			mListFragment.setCallbacks(this);
			break;

		case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS
		| ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:
			mListFragment = new MultiDuplicationPickerFragment();
			bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
			mListFragment.setArguments(bundle);
			break;

		case ContactsRequestAction.ACTION_PICK_MULTIPLE_EMAILS:
			mListFragment = new EmailsPickerFragment();
			break;

		case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONES://从短信选择联系人
			mListFragment = new PhoneNumbersPickerFragment();
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setForContactsChoice(true);
			mListFragment.setCallbacks(this);
			bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
			mListFragment.setArguments(bundle);

			String extrasString=getIntent().getStringExtra("mstFilter");
			Log.d(TAG,"extrasString:"+extrasString);
			if(TextUtils.equals("callRecord", extrasString)){//通话录音
				mListFragment.setMstFilter(ContactListFilter
						.createFilterWithType(ContactListFilter.FILTER_TYPE_SELECT_RECORD_CONTACT));
			}else if(TextUtils.equals("oneKeyAlarm", extrasString)){//一鍵报警
				mListFragment.setMstFilterString("oneKeyAlarm");
			}
			//M:Op01 Rcs get intent data for filter @{
			ExtensionManager.getInstance().getRcsExtension().
			getIntentData(getIntent(), mListFragment);
			/** @} */
			break;

		case ContactsRequestAction.ACTION_PICK_MULTIPLE_DATAS:
			mListFragment = new DataItemsPickerFragment();
			bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
			mListFragment.setArguments(bundle);
			break;

		case ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS:
			mListFragment = new MultiDeletionPickerFragment();
			break;

		case ContactsRequestAction.ACTION_GROUP_MOVE_MULTIPLE_CONTACTS:
			mListFragment = new MultiGroupPickerFragment();
			bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
			mListFragment.setArguments(bundle);			
			break;

		case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS:
			mListFragment = new PhoneAndEmailsPickerFragment();
			break;

		case ContactsRequestAction.ACTION_SHARE_MULTIPLE_CONTACTS:
			mListFragment = new MultiSharePickerFragment();
			break;

		case ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS://从群组选择
			mListFragment = new MultiGroupAddPickerFragment();
			bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
			mListFragment.setArguments(bundle);
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setForContactsChoice(true);
			mListFragment.setCallbacks(this);
			break;


		case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONE_IMS_SIP_CALLS:
			mListFragment = new ConferenceCallsPickerFragment();
			bundle.putParcelable(ConferenceCallsPickerFragment.FRAGMENT_ARGS, getIntent());
			mListFragment.setArguments(bundle);
			break;

		case ContactsRequestAction.ACTION_MST_RECORD_CONTACTS_LIST://录音联系人列表 add by liyang
			mListFragment = new MstRecordContactListFragment();
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setCallbacks(this);
			mListFragment.setForContactsChoice(true);
			showActionMode(false);
			mSearchView.setVisibility(View.GONE);
			toolbar.setTitle(getString(R.string.mst_auto_record_contacts));
			toolbar.inflateMenu(R.menu.mtk_group_browse_options);
			final Menu menu = toolbar.getMenu();
			addRecordContactsMenuItem =menu.findItem(R.id.menu_add_group);
			addRecordContactsMenuItem.setVisible(true);
			addRecordContactsMenuItem.setTitle(getResources().getString(R.string.mst_add_auto_record_contacts));
			//M:Op01 Rcs get intent data for filter @{
			ExtensionManager.getInstance().getRcsExtension().
			getIntentData(getIntent(), mListFragment);
			/** @} */
			break;

		case ContactsRequestAction.ACTION_MST_PRIVACY_CONTACTS_LIST://隐私联系人 add by liyang
			mListFragment = new MstPrivacyContactListFragment();
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setCallbacks(this);
			mListFragment.setForContactsChoice(true);
			//			showActionMode(false);
			mSearchView.setVisibility(View.GONE);
			toolbar.setTitle(getString(R.string.mst_privacy_contacts));
			toolbar.inflateMenu(R.menu.mst_privacy_contacts_options);
			addRecordContactsMenuItem =toolbar.getMenu().findItem(R.id.menu_add_privacy_contacts);
			addRecordContactsMenuItem.setVisible(true);
			addRecordContactsMenuItem.setTitle(getResources().getString(R.string.mst_add_privacy_contacts));
			ExtensionManager.getInstance().getRcsExtension().
			getIntentData(getIntent(), mListFragment);
			/** @} */
			break;

		case ContactsRequestAction.ACTION_MST_PRIVACY_ADD_CONTACTS_LIST://添加隐私联系人 add by liyang
			mListFragment = new MstPrivacyContactsAddPickerFragment();
			mListFragment.setActionMode(actionMode);
			mListFragment.setBottomBar(bottomBar);
			mListFragment.setSearchView(mSearchView);
			mListFragment.setCallbacks(this);
			mListFragment.setForContactsChoice(true);
			//			showActionMode(false);
			mSearchView.setVisibility(View.VISIBLE);			
			ExtensionManager.getInstance().getRcsExtension().
			getIntentData(getIntent(), mListFragment);
			/** @} */
			break;

		default:
			throw new IllegalStateException("Invalid action code: " + mActionCode);
		}

		mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
		mListFragment.setQueryString(mRequest.getQueryString(), false);
		mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);
		mListFragment.setVisibleScrollbarEnabled(true);

		getFragmentManager().beginTransaction().replace(R.id.list_container, mListFragment)
		.commitAllowingStateLoss();
	}

	public void startActivityAndForwardResult(final Intent intent) {
		Log.i(TAG, "[startActivityAndForwardResult]intent = " + intent);
		intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

		// Forward extras to the new activity
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			intent.putExtras(extras);
		}
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mListFragment.startSearch(newText);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onClose() {
		if (mSearchView == null) {
			return false;
		}
		if (!TextUtils.isEmpty(mSearchView.getQuery())) {
			mSearchView.setQuery(null, true);
		}
		showActionBar(SelectionMode.ListMode);
		mListFragment.updateSelectedItemsView();
		return true;
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		//		if (view.getId() == R.id.search_view) {
		//			if (hasFocus) {
		//				showInputMethod(mSearchView.findFocus());
		//			}
		//		}
	}

	public void showInputMethod(View view) {
		if(view==null) return;
		final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(view, 0);
		}
	}

	//	private void showInputMethod(View view) {
	//		final InputMethodManager imm = (InputMethodManager) getSystemService(
	//				Context.INPUT_METHOD_SERVICE);
	//		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);  
	//	}

	/**
	 * Hide software keyboard for the given {@link View}.
	 */
	public void hideInputMethod(Context context, View view) {
		if(view==null) return;
		InputMethodManager imm = (InputMethodManager) context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void returnPickerResult(Uri data) {
		Intent intent = new Intent();
		intent.setData(data);
		returnPickerResult(intent);
	}

	public void returnPickerResult(Intent intent) {
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		Log.i(TAG, "[onActivityResult1]requestCode = " + requestCode + ",resultCode = "
				+ resultCode);
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_PICK_CONTACT_FOR_RECORD_CONTACTS) {
			if (resultCode == Activity.RESULT_OK) {
				//				if (data != null) {
				//					addAutoRecordDataToDb(data);
				//				}
				final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

					@Override
					protected void onPreExecute() {
						prepareProgressDialogSpinner(null, getString(R.string.mst_adding_auto_record_contacts));
					}

					@Override
					protected Void doInBackground(Void... params) {
						if (data != null) {
							addAutoRecordDataToDb(data);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if(mProgressDialog !=null) {
							mProgressDialog.dismiss();
							mProgressDialog=null;
						}                         
					}
				};

				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}		    
			return;
		}else if (requestCode == RESULT_PICK_CONTACT_FOR_PRIVACY_CONTACTS) {
			if (resultCode == Activity.RESULT_OK) {
				final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

					@Override
					protected void onPreExecute() {
						prepareProgressDialogSpinner(null, getString(R.string.mst_adding_privacy_contacts));
					}

					@Override
					protected Void doInBackground(Void... params) {
						if (data != null) {
							addPrivacyContactsToDb(data);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if(mProgressDialog !=null) {
							mProgressDialog.dismiss();
							mProgressDialog=null;
							mListFragment.startLoad();
						}                         
					}
				};
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}else if (requestCode == SUBACTIVITY_ADD_TO_EXISTING_CONTACT) {
			if (resultCode == Activity.RESULT_OK) {
				if (data != null) {
					startActivity(data);
				}
				finish();
			}
		}

		if (resultCode == ContactImportExportActivity.RESULT_CODE) {
			finish();
		}

		if (resultCode == CONTACTGROUPLISTACTIVITY_RESULT_CODE) {
			long[] ids = data.getLongArrayExtra("checkedids");
			if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
				PhoneAndEmailsPickerFragment fragment =
						(PhoneAndEmailsPickerFragment) mListFragment;
				fragment.markItemsAsSelectedForCheckedGroups(ids);
			}
			// M:OP01 RCS will mark item for selected group in phone numbers
			// list@{
			ExtensionManager.getInstance().getRcsExtension().getGroupListResult(mListFragment, ids);
			/** @} */
		}

		if (resultCode == CONTACTGROUPLISTACTIVITY_RESULT_CODE) {

		}

	}

	public void back(){
		Log.d(TAG, "[back],mIsSearchMode:"+mIsSearchMode);
		if (mSearchView != null /*&& !mSearchView.isFocused()*/&&mIsSearchMode) {
			if (!TextUtils.isEmpty(mSearchView.getQuery())) {
				mSearchView.setQuery(null, true);
			}
			hideInputMethod(ContactListMultiChoiceActivity.this,mSearchView.findFocus());
			showActionBar(SelectionMode.ListMode);
			mListFragment.updateSelectedItemsView();
			mListFragment.showHeader(true);
			mSearchView.clearFocus();
			mSearchView.setVisibility(View.GONE);
			if(ContactsRequestAction.ACTION_MST_RECORD_CONTACTS_LIST!=mActionCode){
				showActionMode(true);
			}
			if(addRecordContactsMenuItem!=null) addRecordContactsMenuItem.setVisible(true);
			mListFragment.setSearchMode(false);
			//			mListFragment.getmIndexBar().setVisibility(View.VISIBLE);
			mListFragment.startLoad();
			mIsSearchMode=false;
			toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
			//			toolbar.getNavigationIcon().setVisible(true, false);
			//			backIcon.setVisibility(View.GONE);
			getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar_background_color)); 
			return;
		}
		Log.d(TAG,"back1");
		setResult(Activity.RESULT_CANCELED);		
		super.onBackPressed();
	}
	public void onBackPressed() {
		Log.i(TAG, "[onBackPressed]");
		back();	
	}

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	//		Log.d(TAG,"onKeyDown,keyCode:"+keyCode+" event:"+event);
	//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
	//			back();
	//			return true;
	//		}
	//
	//		return super.onKeyDown(keyCode, event);
	//	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(TAG, "[onConfigurationChanged]" + newConfig);
		super.onConfigurationChanged(newConfig);
		// do nothing
	}

	private ActionMode actionMode;
	private BottomNavigationView bottomBar;
	public android.widget.Toolbar toolbar;
	
	

	private void showActionBar(SelectionMode mode) {/*
		Log.d(TAG, "[showActionBar]mode = " + mode);
		ActionBar actionBar = getActionBar();
		switch (mode) {
		case SearchMode:
			mIsSearchMode = true;
			invalidateOptionsMenu();
			final View searchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
					.inflate(R.layout.mtk_multichoice_custom_action_bar, null);
			// in SearchMode,disable the doneMenu and selectView.
			Button selectView = (Button) searchViewContainer.findViewById(R.id.select_items);
			selectView.setVisibility(View.GONE);

			mSearchView = (SearchView) searchViewContainer.findViewById(R.id.search_view);
			mSearchView.setVisibility(View.VISIBLE);
			mSearchView.setIconifiedByDefault(true);
			mSearchView.setQueryHint(getString(R.string.hint_findContacts));
			mSearchView.setIconified(false);
			mSearchView.setOnQueryTextListener(this);
			mSearchView.setOnCloseListener(this);
			mSearchView.setOnQueryTextFocusChangeListener(this);

			// when no Query String,do not display the "X"
			mSearchView.onActionViewExpanded();

			actionBar.setCustomView(searchViewContainer, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);

			// display the "OK" button.
			Button optionView = (Button) searchViewContainer.findViewById(R.id.menu_option);
			optionView.setTypeface(Typeface.DEFAULT_BOLD);
			if (mIsSelectedNone) {
				// if there is no item selected, the "OK" button is disable.
				optionView.setEnabled(false);
				optionView.setTextColor(Color.LTGRAY);
			} else {
				optionView.setEnabled(true);
				optionView.setTextColor(Color.WHITE);
			}
			optionView.setOnClickListener(this);
			break;

		case ListMode:
			mIsSearchMode = false;
			invalidateOptionsMenu();
			// Inflate a custom action bar that contains the "done" button for
			// multi-choice
			LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.mtk_multichoice_custom_action_bar,
                    null);
            // in the listMode,disable the SearchView
//            mSearchView = (SearchView) customActionBarView.findViewById(R.id.search_view);
//            mSearchView.setVisibility(View.GONE);

            // set dropDown menu on selectItems.
            Button selectItems = (Button) customActionBarView.findViewById(R.id.select_items);
            selectItems.setOnClickListener(this);

            Button menuOption = (Button) customActionBarView.findViewById(R.id.menu_option);
            menuOption.setTypeface(Typeface.DEFAULT_BOLD);
            String optionText = menuOption.getText().toString();
            menuOption.setOnClickListener(this);

            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
            // in onBackPressed() used. If mSearchView is null,return prePage.
            mSearchView = null;
			break;

		default:
			break;
		}
	 */}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "[onOptionsItemSelected]");
		if (item.getItemId() == android.R.id.home) {
			hideSoftKeyboard(mSearchView);
			// Fix CR:ALPS01945610
			if (isResumed()) {
				onBackPressed();
			}
			return true;
		}
		if (item.getItemId() == R.id.groups) {
			startActivityForResult(new Intent(ContactListMultiChoiceActivity.this,
					ContactGroupListActivity.class), CONTACTGROUPLISTACTIVITY_RESULT_CODE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * add dropDown menu on the selectItems.The menu is "Select all" or
	 * "Deselect all"
	 *
	 * @param customActionBarView
	 * @return The updated DropDownMenu
	 */
	private DropDownMenu updateSelectionMenu(View customActionBarView) {
		DropMenu dropMenu = new DropMenu(this);
		// new and add a menu.
		DropDownMenu selectionMenu = dropMenu.addDropDownMenu(
				(Button) customActionBarView.findViewById(R.id.select_items), R.menu.mtk_selection);

		Button selectView = (Button) customActionBarView.findViewById(R.id.select_items);
		// when click the selectView button, display the dropDown menu.
		selectView.setOnClickListener(this);
		MenuItem item = selectionMenu.findItem(R.id.action_select_all);

		// get mIsSelectedAll from fragment.
		mListFragment.updateSelectedItemsView();
		mIsSelectedAll = mListFragment.isSelectedAll();
		// if select all items, the menu is "Deselect all"; else the menu is
		// "Select all".
		if (mIsSelectedAll) {
			// dropDown menu title is "Deselect all".
			item.setTitle(R.string.menu_select_none);
			// click the menu, deselect all items
			dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					showActionBar(SelectionMode.ListMode);
					// clear select all items
					mListFragment.onClearSelect();
					return false;
				}
			});
		} else {
			// dropDown Menu title is "Select all"
			item.setTitle(R.string.menu_select_all);
			// click the menu, select all items.
			dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					showActionBar(SelectionMode.ListMode);
					// select all of itmes
					mListFragment.onSelectAll();
					return false;
				}
			});
		}
		return selectionMenu;
	}

	private ProgressDialog mProgressDialog;
	void prepareProgressDialogSpinner(String title, String message) {
		Log.d(TAG,"prepareProgressDialogSpinner");
		if(mProgressDialog!=null) {
			mProgressDialog.dismiss();
		}

		mProgressDialog = new ProgressDialog(ContactListMultiChoiceActivity.this);
		if(title!=null) mProgressDialog.setTitle(title);
		mProgressDialog.setMessage(message);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCanceledOnTouchOutside(false);

		mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onKey:"+keyCode);
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});

		mProgressDialog.show();
	}

	private void addAutoRecordDataToDb(Intent data) {
		final long[] dataIds = data.getLongArrayExtra(RESULTINTENTEXTRANAME);
		if (dataIds == null || dataIds.length <= 0) {
			return;
		}


		//		prepareProgressDialogSpinner(null, getString(R.string.mst_adding_auto_record_contacts));//liyang：此处无效，下面需新开线程才行
		ContentValues values = new ContentValues();

		StringBuilder selection = new StringBuilder();
		selection.append(Data._ID);
		selection.append(" IN (");
		selection.append(dataIds[0]);
		for (int i = 1; i < dataIds.length; i++) {
			selection.append(",");
			selection.append(dataIds[i]);
		}
		selection.append(")");

		values.put("auto_record", 1);
		getContentResolver().update(Data.CONTENT_URI, values,
				selection.toString(), null);
		//		if(mProgressDialog!=null) {
		//			mProgressDialog.dismiss();
		//			mProgressDialog=null;
		//		}
	}

	private void addPrivacyContactsToDb(Intent data) {
		final long[] contactsIds = data.getLongArrayExtra(RESULT_INTENT_EXTRA_NAME);

		if (contactsIds == null || contactsIds.length < 1) {
			return;
		}

		Log.d(TAG,"contactsIds:"+Arrays.toString(contactsIds));
		ContentValues values = new ContentValues();

		for (long contact_id : contactsIds) {
			values.put("is_privacy", PrivacyUtils.getCurrentAccountId());
			int updateRaw = getContentResolver().update(RawContacts.CONTENT_URI, values, 
					RawContacts.CONTACT_ID + "=" + contact_id, null);
			int updateData = getContentResolver().update(Data.CONTENT_URI, values, 
					Data.CONTACT_ID + "=" + contact_id + " AND is_privacy > -1", null);
			values.clear();
			Log.i(TAG, "addPrivacyContactsToDb,privacyId = " + PrivacyUtils.getCurrentAccountId()
					+ "  updateRaw = " + updateRaw + "  updateData = " + updateData);
		}
	}

	private static final String RESULTINTENTEXTRANAME = "com.mediatek.contacts.list.pickdataresult";
	protected static final String RESULT_INTENT_EXTRA_NAME =
			"com.mediatek.contacts.list.pickcontactsresult";

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMenuItemClick--->"+item.getTitle());

		switch (item.getItemId()) {
		case R.id.menu_add_group:{
			Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONES");
			intent.setType("vnd.android.cursor.dir/phone");
			//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("mstFilter", "callRecord");
			startActivityForResult(intent, RESULT_PICK_CONTACT_FOR_RECORD_CONTACTS);
			break;
		}

		case R.id.menu_add_privacy_contacts:{
			bottomWidePopupMenu.show();
			break;
		}
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		ExtensionManager.getInstance().getOp01Extension().
		addGroupMenu(this, menu, mListFragment);

		/*
        MenuItem menuItem = menu.findItem(R.id.search_menu_item);
        if (mIsSearchMode) {
            // if SearchMode, search Menu is disable.
            menuItem.setVisible(false);
            return false;
        } else {
            // if ListMode, search Menu is display.
            menuItem.setVisible(true);
            if (mListFragment instanceof MultiPhoneAndEmailsPickerFragment) {
                MenuItem groupsItem = menu.findItem(R.id.groups);
                groupsItem.setVisible(true);
            }
            //M:OP01 RCS will add menu item in list@{
            ExtensionManager.getInstance().getRcsExtension().
                    addListMenuOptions(this, menu, menuItem, mListFragment);
            return super.onPrepareOptionsMenu(menu);
        }

		 */
		//M:OP01 RCS will add menu item in list@{
		ExtensionManager.getInstance().getRcsExtension().
		addListMenuOptions(this, menu, null, mListFragment);
		/** @} */
		return true;
	}

	private void hideSoftKeyboard(View view) {
		final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		if (imm != null && view != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

    private boolean mDeleteContactsFromDB = false;
	@Override
	public Object onFragmentCallback(int what, final Object obj) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onFragmentCallback,what:" + what + " obj:" + obj);
		switch (what) {
		case FragmentCallbacks.REMOVE_AUTO_RECORD_CONTACTS:
			new AlertDialog.Builder(ContactListMultiChoiceActivity.this)
			.setTitle(null) 
			.setMessage(getString(R.string.mst_remove_record_contacts_prompt))
			.setPositiveButton(getString(R.string.mst_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					long[] dataIds=(long[])obj;
					removeAutoRecordDataToDb(dataIds);
				}
			})
			.setNegativeButton(getString(R.string.mst_cancel),null)
			.show();
			break;

		case FragmentCallbacks.REMOVE_PRIVACY_CONTACTS: 
			final int position=Integer.parseInt(obj.toString());
			
    		final View dialogView = LayoutInflater.from(this).inflate(R.layout.black_remove, null);
    		final TextView messageView = (TextView)dialogView.findViewById(R.id.textView1);
    		final CheckBox black_remove=(CheckBox)dialogView.findViewById(R.id.check_box);
    		
    		messageView.setText(getString(R.string.mst_remove_privacy_contacts_dialog_message, mListFragment.getAdapter().getName(position)));
    		black_remove.setText(getString(R.string.mst_remove_privacy_contacts_dialog_message2));
    		black_remove.setChecked(false);
    		
    		AlertDialog dialog = new AlertDialog.Builder(ContactListMultiChoiceActivity.this)
    		.setTitle(getString(R.string.mst_remove_privacy_contacts_dialog_title))
    		.setView(dialogView)
    		.setPositiveButton(android.R.string.ok,
    				new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                	mDeleteContactsFromDB = black_remove.isChecked();
                	Log.i(TAG, "mDeleteContactsFromDB = " + mDeleteContactsFromDB);
                	removePrivacyContacts(position);
                }
    		})
    		.setNegativeButton(android.R.string.cancel,   
            		new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                	dialog.dismiss();
                }
            }).show();
    		dialog.setCanceledOnTouchOutside(false);    	
			break;

		case FragmentCallbacks.SWITCH_TO_SEARCH_MODE:
			Log.d(TAG, "onFragmentCallback SWITCH_TO_SEARCH_MODE");
			showActionMode(false);
			toolbar.setVisibility(View.VISIBLE);
			mSearchView.setVisibility(View.VISIBLE);
			mSearchView.requestFocus();
			mListFragment.setSearchMode(true);
			mListFragment.getmIndexBar().setVisibility(View.GONE);
			toolbar.setBackgroundColor(getResources().getColor(R.color.mst_toolbar_background_color));
			//			toolbar.getNavigationIcon().setVisible(false, false);
			//			backIcon.setVisibility(View.VISIBLE);
			getWindow().setStatusBarColor(getResources().getColor(R.color.mst_toolbar_background_color)); 
			if(addRecordContactsMenuItem!=null) addRecordContactsMenuItem.setVisible(false);
			break;
		default:
			break;
		}
		return null;
	}

	private void removePrivacyContacts(int position) {
		int contactId = mListFragment.getAdapter().getContactID(position);
		if (mDeleteContactsFromDB) {
			int rawContactId = mListFragment.getAdapter().getRawContactID(position);
			getContentResolver().delete(
                    RawContacts.CONTENT_URI, 
                    RawContacts._ID + "=?" + " and deleted=0 and is_privacy=" + PrivacyUtils.getCurrentAccountId(), 
                    new String[] {String.valueOf(rawContactId)});
			int deleteCount = getContentResolver().delete(
					Contacts.CONTENT_URI.buildUpon().appendQueryParameter("batch", "true").build(),
					Contacts._ID + " IN (?)", new String[]{String.valueOf(contactId)});
		} else {
			
			ContentValues values = new ContentValues();
    		values.put("is_privacy", 0);
    		
			getContentResolver().update(RawContacts.CONTENT_URI, values, 
            		RawContacts.CONTACT_ID + "=" + contactId, null);
            getContentResolver().update(Data.CONTENT_URI, values, 
                    Data.CONTACT_ID + "=" + contactId, null);
            values.clear();
		}
	}
	
	
	private void removeAutoRecordDataToDb(long[] dataIds) {
		if (dataIds == null || dataIds.length <= 0) {
			return;
		}
		ContentValues values = new ContentValues();
		StringBuilder selection = new StringBuilder();
		selection.append(Data._ID);
		selection.append(" IN (");
		selection.append(dataIds[0]);
		for (int i = 1; i < dataIds.length; i++) {
			selection.append(",");
			selection.append(dataIds[i]);
		}
		selection.append(")");

		values.put("auto_record", 0);
		getContentResolver().update(Data.CONTENT_URI, values,
				selection.toString(), null);
		Toast.makeText(ContactListMultiChoiceActivity.this, "移除成功", Toast.LENGTH_LONG).show();
		mListFragment.reloadData();
	}
}
