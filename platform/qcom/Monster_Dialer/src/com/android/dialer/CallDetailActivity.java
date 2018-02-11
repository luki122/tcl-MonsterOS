/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.dialer;

import com.android.contacts.common.mst.ScrollViewUtil;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import mst.app.dialog.AlertDialog;
import mst.view.menu.BottomWidePopupMenu;
import mst.widget.toolbar.Toolbar;
import android.R.integer;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import mst.provider.ContactsContract;
import mst.provider.CallLog.Calls;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.PinnedPositions;
import mst.provider.ContactsContract.Profile;
import mst.provider.ContactsContract.QuickContact;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.VoicemailContract.Voicemails;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.CallUtil;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.calllog.CallDetailHistoryAdapter;
import com.android.dialer.calllog.CallLogAsyncTaskUtil.CallLogAsyncTaskListener;
import com.android.dialer.calllog.CallLogAsyncTaskUtil;
import com.android.dialer.calllog.CallTypeHelper;
import com.android.dialer.calllog.ContactInfo;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.calllog.PhoneNumberDisplayUtil;
import com.android.dialer.contactinfo.ContactInfoCache;
import com.android.dialer.contactinfo.ContactInfoCache.OnContactInfoChangedListener;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.util.TelecomUtil;
import com.mst.black.BlackUtils;
import com.mst.internal.R.color;
import com.mst.record.PhoneCallRecord;
import com.mst.record.RecordParseUtil;
import com.mst.record.RecorderUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.android.contacts.common.mst.MstScrollListView;
import android.widget.AdapterView.OnItemClickListener;
import com.mst.tms.MarkManager;
import com.mst.tms.MarkResult;
import android.widget.CheckBox;
import android.content.pm.ResolveInfo;

/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry, or with the
 * {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log entries.
 */
public class CallDetailActivity extends mst.app.MstActivity
implements MenuItem.OnMenuItemClickListener,mst.widget.toolbar.Toolbar.OnMenuItemClickListener {
	private static final String TAG = "CallDetailActivity";
	private Toolbar toolbar;
	/** A long array extra containing ids of call log entries to display. */
	public static final String EXTRA_CALL_LOG_IDS = "EXTRA_CALL_LOG_IDS";
	public static final String EXTRA_NUMBER = "EXTRA_NUMBER";
	/** If we are started with a voicemail, we'll find the uri to play with this extra. */
	public static final String EXTRA_VOICEMAIL_URI = "EXTRA_VOICEMAIL_URI";
	/** If the activity was triggered from a notification. */
	public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

	public static final String VOICEMAIL_FRAGMENT_TAG = "voicemail_fragment";

	private Uri lookupUri;
	private CallLogAsyncTaskListener mCallLogAsyncTaskListener = new CallLogAsyncTaskListener() {
		@Override
		public void onDeleteCall() {
			finish();
		}

		@Override
		public void onDeleteVoicemail() {
			finish();
		}

		boolean canPlaceCallsTo;
		@Override
		public void onGetCallDetails(PhoneCallDetails[] details) {
			Log.d(TAG,"liyang18-onGetCallDetails0");
			if (details == null ||details.length==0) {
				// Somewhere went wrong: we're going to bail out and show error to users.
				Toast.makeText(mContext, R.string.toast_call_detail_error,
						Toast.LENGTH_SHORT).show();
				finish();
				return;
			}

			// We know that all calls are from the same number and the same contact, so pick the
			// first.
			PhoneCallDetails firstDetails = details[details.length-1];
			mNumber = TextUtils.isEmpty(firstDetails.number) ?
					null : firstDetails.number.toString();
			final int numberPresentation = firstDetails.numberPresentation;
			final Uri contactUri = firstDetails.contactUri;
			final Uri photoUri = firstDetails.photoUri;
			final PhoneAccountHandle accountHandle = firstDetails.accountHandle;
			lookupUri=firstDetails.lookupUri;
			Log.d(TAG,"lookupuri:"+lookupUri);

			// Cache the details about the phone number.
			canPlaceCallsTo =
					PhoneNumberUtil.canPlaceCallsTo(mNumber, numberPresentation);
			mIsVoicemailNumber =
					PhoneNumberUtil.isVoicemailNumber(mContext, accountHandle, mNumber);
			final boolean isSipNumber = PhoneNumberUtil.isSipNumber(mNumber);

			final CharSequence callLocationOrType = getNumberTypeOrLocation(firstDetails);

			final CharSequence displayNumber = firstDetails.displayNumber;
			final String displayNumberStr = mBidiFormatter.unicodeWrap(
					displayNumber.toString(), TextDirectionHeuristics.LTR);

			if (!TextUtils.isEmpty(firstDetails.name)) {
				mCallerName.setText(firstDetails.name);
				mCallerNumber.setText(callLocationOrType + " " + displayNumberStr);
				if(!mIsRejectedDetail) {
					starredMenu.setVisible(true);
					addToContactMenu.setVisible(false);
					viewContactMenu.setVisible(true);
					//				addToExistContactMenu.setVisible(false);
				}
				//add by lgy
				mNameOrig = firstDetails.name.toString();
			} else {
				if(!mIsRejectedDetail) {
					starredMenu.setVisible(false);
					viewContactMenu.setVisible(false);
					addToContactMenu.setVisible(canPlaceCallsTo?true:false);
					//				addToExistContactMenu.setVisible(canPlaceCallsTo?true:false);
				}
				mCallerName.setText(displayNumberStr);
				mCallerNumber.setText(displayNumberStr);
				mCallerNumber.setVisibility(View.VISIBLE);

				//				if (!TextUtils.isEmpty(callLocationOrType)) {
				//					mCallerNumber.setText(displayNumberStr);
				//					mCallerNumber.setVisibility(View.VISIBLE);
				//				} else {
				//					mCallerNumber.setVisibility(View.GONE);
				//				}
			}

			mCallButton.setVisibility(canPlaceCallsTo ? View.VISIBLE : View.GONE);
			mSmsButton.setVisibility(canPlaceCallsTo ? View.VISIBLE : View.GONE);

			//            String accountLabel = PhoneAccountUtils.getAccountLabel(mContext, accountHandle);
			//            if (!TextUtils.isEmpty(accountLabel)) {
			//                mAccountLabel.setText(accountLabel);
			//                mAccountLabel.setVisibility(View.VISIBLE);
			//            } else {
			//                mAccountLabel.setVisibility(View.GONE);
			//            }

			mHasEditNumberBeforeCallOption =
					canPlaceCallsTo && !isSipNumber && !mIsVoicemailNumber;
			mHasReportMenuOption = mContactInfoHelper.canReportAsInvalid(
					firstDetails.sourceType, firstDetails.objectId);
			invalidateOptionsMenu();

			mAdapter = 
					new CallDetailHistoryAdapter(mContext, mInflater, mCallTypeHelper, details);
			historyList.setAdapter(mAdapter);
			//			ScrollViewUtil.setListViewHeightBasedOnChildren(historyList);

			String lookupKey = contactUri == null ? null
					: UriUtils.getLookupKeyFromUri(contactUri);

			final boolean isBusiness = mContactInfoHelper.isBusiness(firstDetails.sourceType);

			final int contactType =
					mIsVoicemailNumber ? ContactPhotoManager.TYPE_VOICEMAIL :
						isBusiness ? ContactPhotoManager.TYPE_BUSINESS :
							ContactPhotoManager.TYPE_DEFAULT;

			String nameForDefaultImage;
			if (TextUtils.isEmpty(firstDetails.name)) {
				nameForDefaultImage = firstDetails.displayNumber;
			} else {
				nameForDefaultImage = firstDetails.name.toString();
			}

			loadContactPhotos(
					contactUri, photoUri, nameForDefaultImage, lookupKey, contactType);
			findViewById(R.id.call_detail).setVisibility(View.VISIBLE);

			if(!mIsRejectedDetail) {
				if(firstDetails.starred==0){
					starredMenu.setIcon(getResources().getDrawable(R.drawable.mst_not_star));
				}else{
					starredMenu.setIcon(getResources().getDrawable(R.drawable.mst_is_star));
				}
			}

			if(!TextUtils.isEmpty(firstDetails.geocode)){
				mCallerArea.setText(firstDetails.geocode);
				mCallerArea.setVisibility(View.VISIBLE);
			}else{
				mCallerArea.setText("未知归属地");
				mCallerArea.setVisibility(View.VISIBLE);
			}
			Log.d(TAG,"liyang18-onGetCallDetails1");
			final Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] {"data1"},
					"mimetype='vnd.android.cursor.item/note'  AND contact_id="+firstDetails.rawContactId, null, null);
			Log.d(TAG,"c:"+c+"；"+(c==null?"null":c.getCount())+" firstDetails.rawContactId:"+firstDetails.rawContactId);
			if(c!=null&&c.getCount()>0){
				c.moveToFirst();
				String data1=c.getString(0);
				Log.d(TAG,"data1:"+data1);
				if(!TextUtils.isEmpty(data1)){
					mCallerNote.setText(data1);
					mCallerNote.setVisibility(View.VISIBLE);
				}else{
					mCallerNote.setText("暂无备注");
					mCallerNote.setVisibility(View.VISIBLE);
				}
			}
			//add by lgy for record

			updateMonsterItems(details);

			Log.d(TAG,"liyang18-onGetCallDetails2");
		}

		/**
		 * Determines the location geocode text for a call, or the phone number type
		 * (if available).
		 *
		 * @param details The call details.
		 * @return The phone number type or location.
		 */
		private CharSequence getNumberTypeOrLocation(PhoneCallDetails details) {
			if (!TextUtils.isEmpty(details.name)) {
				return Phone.getTypeLabel(mResources, details.numberType,
						details.numberLabel);
			} else {
				return details.geocode;
			}
		}
	};

	private Context mContext;
	private CallTypeHelper mCallTypeHelper;
	private QuickContactBadge mQuickContactBadge;
	private TextView mCallerName;
	private TextView mCallerNumber;
	private TextView mCallerNote;
	private TextView mCallerArea;
	//    private TextView mAccountLabel;
	private View mCallButton,mSmsButton,caller_line;
	private ContactInfoHelper mContactInfoHelper;

	protected String mNumber;
	private boolean mIsVoicemailNumber;
	private String mDefaultCountryIso;

	/* package */ LayoutInflater mInflater;
	/* package */ Resources mResources;
	/** Helper to load contact photos. */
	private ContactPhotoManager mContactPhotoManager;

	private Uri mVoicemailUri;
	private BidiFormatter mBidiFormatter = BidiFormatter.getInstance();

	/** Whether we should show "edit number before call" in the options menu. */
	private boolean mHasEditNumberBeforeCallOption;
	private boolean mHasReportMenuOption;
	private MenuItem starredMenu;
	private MenuItem addToContactMenu;
	private MenuItem viewContactMenu;
	//	private MenuItem addToExistContactMenu;

	private boolean needDelayQuery=false;
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mContext = this;

		setMstContentView(R.layout.call_detail);

		Intent intent = getIntent();
		mIsRejectedDetail = intent.getBooleanExtra("reject_detail", false);

		toolbar = getToolbar();
		toolbar.setElevation(0f);
		toolbar.setTitle(getString(R.string.mst_call_detail_title));

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"NavigationOnClickListener");
				finish();
			}
		});

		if (mIsRejectedDetail) {
			//          setMenuEnable(false);        
			//          mActionBar.setTitle(R.string.reject_call_detail_title);
			toolbar.setTitle(getString(R.string.reject_call_detail_title));
			//          mBlackName = intent.getStringExtra("black_name");
			//          mUserMark = intent.getStringExtra("user-mark");
			//          mMarkContent = intent.getStringExtra("mark-content");
			//          mMarkCount = intent.getIntExtra("mark-count", 0);
		} else {
			toolbar.inflateMenu(R.menu.call_details_options);   
			final Menu menu = toolbar.getMenu();
			starredMenu = menu.findItem(R.id.menu_star);
			addToContactMenu=menu.findItem(R.id.menu_add_to_contacts);
			//      addToExistContactMenu=menu.findItem(R.id.menu_add_to_exist_contacts);
			viewContactMenu=menu.findItem(R.id.mst_menu_view_contact);

			starredMenu.setVisible(false);
			addToContactMenu.setVisible(false);
			menu.findItem(R.id.menu_trash).setVisible(false);
			menu.findItem(R.id.menu_edit_number_before_call).setVisible(false);
			menu.findItem(R.id.menu_report).setVisible(false);
			viewContactMenu.setVisible(false);
		}


		//		addToExistContactMenu.setVisible(false);


		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		mResources = getResources();

		mCallTypeHelper = new CallTypeHelper(getResources());

		mVoicemailUri = getIntent().getParcelableExtra(EXTRA_VOICEMAIL_URI);

		mQuickContactBadge = (QuickContactBadge) findViewById(R.id.quick_contact_photo);
		mQuickContactBadge.setOverlay(null);
		mQuickContactBadge.setPrioritizedMimeType(Phone.CONTENT_ITEM_TYPE);
		mCallerName = (TextView) findViewById(R.id.caller_name);
		mCallerNumber = (TextView) findViewById(R.id.caller_number);
		mCallerNote = (TextView) findViewById(R.id.caller_note);
		mCallerArea = (TextView) findViewById(R.id.caller_area);
		//        mAccountLabel = (TextView) findViewById(R.id.phone_account_label);
		mDefaultCountryIso = GeoUtil.getCurrentCountryIso(this);
		mContactPhotoManager = ContactPhotoManager.getInstance(this);

		mCallButton = (View) findViewById(R.id.call_back_button);
		mCallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				needDelayQuery=true;
				Intent intent=IntentUtil.getCallIntent(mNumber);
				intent.putExtra("slot",-1);//-1 不指定；0指定卡槽1拨号；1指定卡槽2拨号
				mContext.startActivity(intent);
			}
		});

		caller_line = (View) findViewById(R.id.caller_line);
		caller_line.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				needDelayQuery=true;
				try{
				Intent intent=IntentUtil.getCallIntent(mNumber);
				intent.putExtra("slot",-1);//-1 不指定；0指定卡槽1拨号；1指定卡槽2拨号
				mContext.startActivity(intent);
				}catch(Exception e){
					Log.d(TAG,"e:"+e);
				}
			}
		});

		//		mCallerNumber.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View view) {
		//				needDelayQuery=true;
		//				Intent intent=IntentUtil.getCallIntent(mNumber);
		//				intent.putExtra("slot",-1);//-1 不指定；0指定卡槽1拨号；1指定卡槽2拨号
		//				mContext.startActivity(intent);
		//			}
		//		});
		//
		//		mCallerArea.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View view) {
		//				needDelayQuery=true;
		//				Intent intent=IntentUtil.getCallIntent(mNumber);
		//				intent.putExtra("slot",-1);//-1 不指定；0指定卡槽1拨号；1指定卡槽2拨号
		//				mContext.startActivity(intent);
		//			}
		//		});

		mSmsButton = (View) findViewById(R.id.sms_button);
		mSmsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mContext.startActivity(IntentUtil.getSendSmsIntent(mNumber));
			}
		});

		mContactInfoHelper = new ContactInfoHelper(this, GeoUtil.getCurrentCountryIso(this));
		//        getActionBar().setDisplayHomeAsUpEnabled(true);

		if (getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
			closeSystemDialogs();
		}

		bottomWidePopupMenu = new BottomWidePopupMenu(this);
		bottomWidePopupMenu.inflateMenu(R.menu.add_to_contacts_bottom_menu);
		bottomWidePopupMenu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onItemClicked(MenuItem item) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onItemClicked Item:"+item.getTitle());
				switch(item.getItemId()){
				case R.id.menu_add_to_contacts:{
					needDelayQuery=true;
					Intent intent = IntentUtil.getNewContactIntent(mNumber);
					intent.setClassName("com.android.contacts", "com.android.contacts.activities.MstContactEditorActivity");
					Log.d(TAG,"SHORTCUT_CREATE_NEW_CONTACT,intent:"+intent);
					DialerUtils.startActivityWithErrorToast(CallDetailActivity.this, intent);
					break;
				}
				case R.id.menu_add_to_exist_contacts:{
					needDelayQuery=true;
					Intent intent=IntentUtil.getAddToExistingContactIntent(mNumber);
					intent.setClassName("com.android.contacts", "com.android.contacts.activities.ContactSelectionActivity");
					DialerUtils.startActivityWithErrorToast(CallDetailActivity.this, intent,
							R.string.add_contact_not_available);
					break;
				}
				default:
					break;
				}
				return true;
			}
		});

		myReceiver = new SimStateChangeReceiver();
		IntentFilter intentFilter=new IntentFilter(ACTION_SIM_STATE_CHANGED);
		registerReceiver(myReceiver, intentFilter);

		//        historyList = (MstScrollListView) findViewById(R.id.history);
		historyList = (ListView) findViewById(R.id.history);
		historyList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick position = " + position);
				mAdapter.playRecord(position);
			}
		});

		mIconPadding = getResources().getDimensionPixelSize(
				R.dimen.sim_icon_margin);

		final boolean hasSDPermission =
				PermissionsUtil.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE");   
		if(!hasSDPermission) {
			requestPermissions(new String[] {"android.permission.READ_EXTERNAL_STORAGE"}, 1);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
			int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults.length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
				// Force a refresh of the data since we were missing the permission before this.
			} else {
				finish();
			}            
		}
	}

	@Override
	public void onDestroy() {

		try {
			unregisterReceiver(myReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}
	protected ContactInfoCache mContactInfoCache;
	protected final OnContactInfoChangedListener mOnContactInfoChangedListener =
			new OnContactInfoChangedListener() {
		@Override
		public void onContactInfoChanged() {
			//			notifyDataSetChanged();
		}
	};
	@Override
	public void onResume() {
		Log.d(TAG, "liyang18-onResume");
		super.onResume();

		if(needDelayQuery){
			mContactInfoCache = new ContactInfoCache(
					mContactInfoHelper, mOnContactInfoChangedListener);
			mContactInfoCache.getValue(mNumber, mDefaultCountryIso, null);
			if (!PermissionsUtil.hasContactsPermissions(mContext)) {
				mContactInfoCache.disableRequestProcessing();
			}
			if (PermissionsUtil.hasPermission(mContext, android.Manifest.permission.READ_CONTACTS)) {
				Log.d(TAG,"onresume2");
				mContactInfoCache.start();
			}
		}

		getCallDetails();
	}


	private Handler mHandler = new Handler();
	private Runnable getCallLogEntryUrisRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG,"run");
			//			CallLogAsyncTaskUtil.getCallDetails(CallDetailActivity.this, getCallLogEntryUris(), mCallLogAsyncTaskListener);
			if(TextUtils.isEmpty(getIntent().getStringExtra(EXTRA_NUMBER))){
				CallLogAsyncTaskUtil.getCallDetails(CallDetailActivity.this, getCallLogEntryUris(), mCallLogAsyncTaskListener);
			}else{
				CallLogAsyncTaskUtil.getCallDetailsForMst(CallDetailActivity.this, getIntent().getStringExtra(EXTRA_NUMBER), mCallLogAsyncTaskListener , mIsRejectedDetail,
						getIntent().getBooleanExtra("mIsPrivate", false));
			}

		}
	}; 



	private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	private SimStateChangeReceiver myReceiver;
	public class SimStateChangeReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			Bundle bundle=intent.getExtras();
			Log.d(TAG,"onReceive,action:"+action+" bundle:"+bundle);
			if(TextUtils.equals(action, ACTION_SIM_STATE_CHANGED)){
				mHandler.removeCallbacks(runnable);
				mHandler.postDelayed(runnable, 2000);
			}
		}
	}

	private Runnable runnable=new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG,"runnable run");
			DialerApplication.reQueryisMultiSimEnabled();
			getCallDetails();
		}
	};

	public void getCallDetails() {
		mHandler.removeCallbacks(getCallLogEntryUrisRunnable);
		Log.d(TAG,"liyang18-needDelayQuery:"+needDelayQuery);
		mHandler.postDelayed(getCallLogEntryUrisRunnable,needDelayQuery?400:0);
		needDelayQuery=false;
	}

	private boolean hasVoicemail() {
		return mVoicemailUri != null;
	}


	public final String[] _PROJECTION = new String[] {
			Calls._ID,                          // 0
			Calls.NUMBER,                       // 1
			Calls.DATE,                         // 2
			Calls.DURATION,                     // 3
			Calls.TYPE,                         // 4
			Calls.COUNTRY_ISO,                  // 5
			Calls.VOICEMAIL_URI,                // 6
			Calls.GEOCODED_LOCATION,            // 7
			Calls.CACHED_NAME,                  // 8
			Calls.CACHED_NUMBER_TYPE,           // 9
			Calls.CACHED_NUMBER_LABEL,          // 10
			Calls.CACHED_LOOKUP_URI,            // 11
			Calls.CACHED_MATCHED_NUMBER,        // 12
			Calls.CACHED_NORMALIZED_NUMBER,     // 13
			Calls.CACHED_PHOTO_ID,              // 14
			Calls.CACHED_FORMATTED_NUMBER,      // 15
			Calls.IS_READ,                      // 16
			Calls.NUMBER_PRESENTATION,          // 17
			Calls.PHONE_ACCOUNT_COMPONENT_NAME, // 18
			Calls.PHONE_ACCOUNT_ID,             // 19
			Calls.FEATURES,                     // 20
			Calls.DATA_USAGE,                   // 21
			Calls.TRANSCRIPTION,                // 22
			Calls.CACHED_PHOTO_URI              // 23
	};
	/**
	 * Returns the list of URIs to show.
	 * <p>
	 * There are two ways the URIs can be provided to the activity: as the data on the intent, or as
	 * a list of ids in the call log added as an extra on the URI.
	 * <p>
	 * If both are available, the data on the intent takes precedence.
	 */
	private Uri[] getCallLogEntryUris() {
		Log.d(TAG,"liyang18-getCallLogEntryUris0");
		final Uri uri = getIntent().getData();
		if (uri != null) {
			// If there is a data on the intent, it takes precedence over the extra.
			return new Uri[]{ uri };
		}
		final long[] ids = getIntent().getLongArrayExtra(EXTRA_CALL_LOG_IDS);
		if(ids==null){//add by liyang:当extra为号码时，从数据库查询所有该号码的call id.   
			final String number=getIntent().getStringExtra(EXTRA_NUMBER);
			if(number==null) return new Uri[0];

			Cursor cursor=getContentResolver().query(Calls.CONTENT_URI, 
					_PROJECTION, 
					"deleted = 0 AND NOT (type = 4) AND number='"+number+"'",
					null, 
					"_id desc");
			if(cursor==null) return new Uri[0];

			final Uri[] uris = new Uri[cursor.getCount()];
			Log.d(TAG,"callLogCursor:"+(cursor==null?"null":cursor.getCount()));
			int index=0;
			while(cursor.moveToNext()){
				int id=cursor.getInt(0);
				Log.d(TAG,"id:"+id);
				uris[index++] = ContentUris.withAppendedId(
						TelecomUtil.getCallLogUri(CallDetailActivity.this), id);
			}
			cursor.close();
			cursor=null;
			Log.d(TAG,"liyang18-getCallLogEntryUris1");
			return uris;
		}

		final int numIds = ids == null ? 0 : ids.length;
		final Uri[] uris = new Uri[numIds];
		for (int index = 0; index < numIds; ++index) {
			uris[index] = ContentUris.withAppendedId(
					TelecomUtil.getCallLogUri(CallDetailActivity.this), ids[index]);
		}
		Log.d(TAG,"getCallLogEntryUris2");
		return uris;
	}

	/** Load the contact photos and places them in the corresponding views. */
	private void loadContactPhotos(Uri contactUri, Uri photoUri, String displayName,
			String lookupKey, int contactType) {

		final DefaultImageRequest request = new DefaultImageRequest(displayName, lookupKey,
				contactType, true /* isCircular */);

		//		mQuickContactBadge.assignContactUri(contactUri);
		//		mQuickContactBadge.setContentDescription(
		//				mResources.getString(R.string.description_contact_details, displayName));

		mContactPhotoManager.loadDirectoryPhoto(mQuickContactBadge, photoUri,
				false /* darkTheme */, true /* isCircular */, request);
	}

	//    @Override
	//    public boolean onCreateOptionsMenu(Menu menu) {
	//        getMenuInflater().inflate(R.menu.call_details_options, menu);
	//        return super.onCreateOptionsMenu(menu);
	//    }

	//    @Override
	//    public boolean onPrepareOptionsMenu(Menu menu) {
	//        // This action deletes all elements in the group from the call log.
	//        // We don't have this action for voicemails, because you can just use the trash button.
	//        menu.findItem(R.id.menu_remove_from_call_log)
	//                .setVisible(!hasVoicemail())
	//                .setOnMenuItemClickListener(this);
	//        menu.findItem(R.id.menu_edit_number_before_call)
	//                .setVisible(mHasEditNumberBeforeCallOption)
	//                .setOnMenuItemClickListener(this);
	//        menu.findItem(R.id.menu_trash)
	//                .setVisible(hasVoicemail())
	//                .setOnMenuItemClickListener(this);
	//        menu.findItem(R.id.menu_report)
	//                .setVisible(mHasReportMenuOption)
	//                .setOnMenuItemClickListener(this);
	//        return super.onPrepareOptionsMenu(menu);
	//    }


	private void setStarred(Uri contactUri,boolean value) {
		if (contactUri == null) {
			Log.e(TAG, "Invalid arguments for setStarred request");
			return;
		}

		final ContentValues values = new ContentValues(1);
		values.put(Contacts.STARRED, value);
		getContentResolver().update(contactUri, values, null, null);

		// Undemote the contact if necessary
		final Cursor c = getContentResolver().query(contactUri, new String[] {Contacts._ID},
				null, null, null);

		if (c == null) {
			return;
		}
		try {
			if (c.moveToFirst()) {
				final long id = c.getLong(0);

				// Don't bother undemoting if this contact is the user's profile.
				if (id < Profile.MIN_ID) {
					PinnedPositions.undemote(getContentResolver(), id);
				}
			}
		} finally {
			c.close();
		}
	}

	private BottomWidePopupMenu bottomWidePopupMenu;
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_remove_from_call_log:

			new AlertDialog.Builder(CallDetailActivity.this)
			.setTitle(null) 
			.setMessage(R.string.slide_delete_calllog_message)
			.setPositiveButton(mContext.getString(R.string.mst_ok), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//					final StringBuilder callIds = new StringBuilder();
					//					for (Uri callUri : getCallLogEntryUris()) {
					//						if (callIds.length() != 0) {
					//							callIds.append(",");
					//						}
					//						callIds.append(ContentUris.parseId(callUri));
					//					}
					//					CallLogAsyncTaskUtil.deleteCalls(
					//							CallDetailActivity.this, callIds.toString(), mCallLogAsyncTaskListener);
					CallLogAsyncTaskUtil.deleteCallsByNumber(
							CallDetailActivity.this, mNumber, mCallLogAsyncTaskListener);
				}
			})
			.setNegativeButton(mContext.getString(R.string.mst_cancel), null)
			.show();			
			break;
		case R.id.menu_edit_number_before_call:
			startActivity(new Intent(Intent.ACTION_DIAL, CallUtil.getCallUri(mNumber)));
			break;
		case R.id.menu_trash:
			CallLogAsyncTaskUtil.deleteVoicemail(
					this, mVoicemailUri, mCallLogAsyncTaskListener);
			break;
		case R.id.menu_star:
			Log.d(TAG,"menu_star");
			final Cursor c = getContentResolver().query(lookupUri, new String[] {"starred"},
					null, null, null);
			if(c!=null&&c.getCount()>0) {
				c.moveToFirst();
				boolean isStarred=c.getInt(0)==1?true:false;
				//			for(int i=0;i<c.getColumnCount();i++) {
				//				Log.d(TAG,"i:"+i+";"+c.getColumnName(i)+"－"+c.getString(i));
				//			}
				Log.d(TAG,"isStarred:"+isStarred+" lookupUri:"+lookupUri);
				setStarred(lookupUri,!isStarred);

				if(isStarred){
					starredMenu.setIcon(getResources().getDrawable(R.drawable.mst_not_star));
				}else{
					starredMenu.setIcon(getResources().getDrawable(R.drawable.mst_is_star));
				}
			}
			break;

		case R.id.menu_add_to_contacts:
			bottomWidePopupMenu.show();
			break;

		case R.id.mst_menu_view_contact:
			//			Intent intent = new Intent(Intent.ACTION_VIEW, lookupUri);
			//			ImplicitIntentsUtil.startActivityInApp(CallDetailActivity.this, intent);
			Log.d(TAG,"lookupuri1:"+lookupUri);
			QuickContact.showQuickContact(CallDetailActivity.this, mQuickContactBadge, lookupUri,
					QuickContact.MODE_LARGE, null);
			break;

			//		case R.id.mst_menu_mark_number:
			//			Toast.makeText(CallDetailActivity.this, "开发中", Toast.LENGTH_LONG).show();
			//			break;
			//
			//		case R.id.mst_menu_add_to_blacklist:
			//		    Toast.makeText(CallDetailActivity.this, "开发中", Toast.LENGTH_LONG).show();
			//			break;

		case R.id.menu_add_black:
			addToBlack();
			return true;
		case R.id.menu_remove_black:
			removeFromBlack();
			return true;
		case R.id.menu_edit_mark: {
			Intent intent = new Intent("com.monster.mark.pick");
			intent.putExtra("user_mark", mUserMark);
			startActivityForResult(intent, EDIT_MARK);
		}
		return true;
		case R.id.menu_add_mark: {
			Intent intent = new Intent("com.monster.mark.pick");
			startActivityForResult(intent, ADD_MARK);
		}

		}
		return true;
	}

	private void closeSystemDialogs() {
		sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	//add by lgy for record
	private void updateMonsterItems(PhoneCallDetails[] details) {
		showPhoneRecords(details);
		checkForBlack(mNumber);      
		if(!TextUtils.isEmpty(mBlackName)){
			mCallerName.setText(mBlackName);
		}
		updateMarkUi();
		updateMenuItemVisbility();
	}


	private CallDetailHistoryAdapter mAdapter;
	//    private MstScrollListView historyList;
	private ListView historyList;

	private void showPhoneRecords(final PhoneCallDetails[] phoneCallDetails) {
		new com.mst.record.SimpleAsynTask() {
			@Override
			protected Integer doInBackground(Integer... params) {
				boolean founded = RecordParseUtil.foundAndSetPhoneRecords(phoneCallDetails);
				return founded ? 1 : 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				if (0 == result) {
					return;
				}
				if (null != historyList && null != historyList.getAdapter()) {
					((BaseAdapter) (historyList.getAdapter()))
					.notifyDataSetChanged();
				}
			}
		}.execute();
	}

	//add for black
	private void checkForBlack(String number) {
		mIsShowRejectFlag = false;
		Cursor cursor = mContext.getContentResolver().query(
				Uri.parse("content://com.android.contacts/black"), new String[]{"reject", "black_name"},
				BlackUtils.getPhoneNumberEqualString(number) + " and isblack=1", null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int reject = cursor.getInt(0);
                mRejectType = reject;
				if (reject == 1 || reject == 3) {
					mIsShowRejectFlag = true;
					mBlackName = cursor.getString(1);
				} 
			}  
			cursor.close();
		}        
	}


	private void addToBlack() {
		Intent intent = new Intent("com.monster.black.add.manually");
		try {
			Bundle bundle = new Bundle();
			bundle.putString("add_name", mNameOrig);
			bundle.putString("add_number", mNumber);
			bundle.putString("user_mark", mUserMark);
			intent.putExtras(bundle);
			intent.putExtra("add", true);
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void removeFromBlack() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
		final CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_box);
		checkBox.setChecked(true);

		AlertDialog dialogs = new AlertDialog.Builder(this)
		.setTitle(mContext.getResources().getString(R.string.black_remove))
		.setView(view)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int whichButton) {
				boolean recoveryLogs = checkBox.isChecked();
				int isblack = 0;
				if (!recoveryLogs) {
					isblack = -1;
				}

				ContentValues values = new ContentValues();
				values.put("isblack", isblack);
				values.put("number", mNumber);
				values.put("reject", 0);
				mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values, BlackUtils.getPhoneNumberEqualString(mNumber), null);
				values.clear();

				mIsShowRejectFlag = false;
				updateMenuItemVisbility();
			}
		})
		.setNegativeButton(android.R.string.cancel, null).show();
	}


	private static boolean mIsRejectedDetail = false;
	private String mBlackName = null;
	private boolean mIsShowRejectFlag = false;
	private int mRejectType = 0;
	private String mUserMark = null;
	private String mMarkContent = null;
	private int mMarkCount = 0;
	private String mNameOrig = null;
	private static final int ADD_MARK = 1;
	private static final int EDIT_MARK = 2;
	private int mIconPadding;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ADD_MARK: {
			if (data != null) {
				mUserMark = data.getStringExtra("user_mark");
				MarkManager.insertUserMark(mContext, mNumber, mUserMark);
				Log.d(TAG, "ADD_MARK mUserMark = " + mUserMark + "  mNumber = " + mNumber + " mMarkContent = " + mMarkContent + "  mMarkCount = " + mMarkCount);

				updateMarkUi();
				updateCallsDatabase();
			}
			break;
		}

		case EDIT_MARK: {
			if (data != null) {
				mUserMark = data.getStringExtra("user_mark");

				if (mUserMark == null) {
					MarkManager.deleteUserMark(mContext, mNumber);
					updateMarkFrom3rd();
				} else {
					MarkManager.insertUserMark(mContext, mNumber, mUserMark);
				}
				updateCallsDatabase();                    
				updateMarkUi();
			}
			break;
		}
		}
		updateMenuItemVisbility();
	}

	private void updateMarkUi () {
		if (mNameOrig == null) {
			mUserMark = MarkManager.getUserMark(this, mNumber);

			updateMarkFrom3rd();

			mCallerNote.setText("");
			if (mUserMark != null) {
				mCallerName.setText(mUserMark+getString(R.string.mst_marked_by_you)); 
				mCallerNote.setCompoundDrawables(null, null, null, null);
			} else if (mMarkContent != null) {
				mCallerName.setText(mMarkContent);
				if (mMarkCount > 0) {
					mCallerNote.setText(String.valueOf(mMarkCount) + mContext.getResources().getString(R.string.reject_marks));
				}
				Drawable right = mContext.getResources().getDrawable(R.drawable.mark_icon);
				right.setBounds(0, 0, right.getMinimumWidth(), right.getMinimumHeight());
				mCallerNote.setCompoundDrawablePadding(mIconPadding);
				mCallerNote.setCompoundDrawables(null, null, right, null);
			}                  

		}

	}

	private void updateBlackUi () {
		if(mIsShowRejectFlag) {            
			Drawable right = mContext.getResources().getDrawable(R.drawable.black_icon);
			right.setBounds(0, 0, right.getMinimumWidth(), right.getMinimumHeight());
			mCallerArea.setCompoundDrawablePadding(mIconPadding);
			mCallerArea.setCompoundDrawables(null, null, right, null);
		} else {
			mCallerArea.setCompoundDrawables(null, null, null, null);
		}
	}

	private void updateMarkFrom3rd() {
		MarkResult mr = MarkManager.getMark(mNumber);
		if (mr != null) {
			mMarkContent = mr.getName();
			mMarkCount = mr.getTagCount();
		} else {
			mMarkContent = null;
			mMarkCount = 0;
		}
	}

	private void updateCallsDatabase () {
		new Thread() {
			public void run() {
				try{
					ContentValues cv = new ContentValues();
					int userMark = -1;
					String mark = mUserMark;
					if (mark == null) {
						mark = mMarkContent;
						userMark = mMarkCount;
					}

					cv.put("mark", mark);
					cv.put("user_mark", userMark);
					mContext.getContentResolver().update(Calls.CONTENT_URI, cv,
							BlackUtils.getPhoneNumberEqualString(mNumber) + " and reject in(0, 1)", null);

					cv.clear();
					Uri blackUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black");
					cv.put("lable", mark);
					cv.put("user_mark", userMark);
					mContext.getContentResolver().update(blackUri, cv,
							BlackUtils.getPhoneNumberEqualString(mNumber), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private  void updateMenuItemVisbility() {
		if(mIsRejectedDetail) {
			return;
		}
		final Menu menu = toolbar.getMenu();
		MenuItem editMark = menu.findItem(R.id.menu_edit_mark);
		MenuItem addMark = menu.findItem(R.id.menu_add_mark);
		MenuItem removeBlack = menu.findItem(R.id.menu_remove_black);
		MenuItem addBlack = menu.findItem(R.id.menu_add_black);

		if(!isRejectInstalled()) {
			addMark.setVisible(false);
			editMark.setVisible(false);
			removeBlack.setVisible(false);
			addBlack.setVisible(false);
			return; 
		}


		if(TextUtils.isEmpty(mNameOrig)) {
			if(TextUtils.isEmpty(mUserMark)) {
				addMark.setVisible(true);
				editMark.setVisible(false);
			} else {
				addMark.setVisible(false);
				editMark.setVisible(true); 
			}
		} else {
			addMark.setVisible(false);
			editMark.setVisible(false); 
		}


		if(mIsShowRejectFlag) {
			removeBlack.setVisible(true);
			addBlack.setVisible(false);
		} else {
			removeBlack.setVisible(false);
			addBlack.setVisible(true);
			Log.d(TAG, "mRejectType = " + mRejectType);
			addBlack.setTitle(mRejectType == 2 ? R.string.add_to_black_call : R.string.add_to_black);
		}

		updateBlackUi();

	}

	private boolean isRejectInstalled() { 
		final PackageManager packageManager = this.getPackageManager();
		final Intent intent = new Intent("com.monster.mark.pick"); 
		//检索所有可用于给定的意图进行的活动。如果没有匹配的活动，则返回一个空列表。 
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY); 
		return list.size() > 0;        
	}


}
