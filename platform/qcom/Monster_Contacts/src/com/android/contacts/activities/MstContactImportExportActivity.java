package com.android.contacts.activities;

//add by liyang

import org.codeaurora.internal.IExtTelephony;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.contacts.common.vcard.CancelActivity;
import com.android.contacts.common.vcard.CancelRequest;
import com.android.contacts.common.vcard.ImportVCardActivity;
import com.android.contacts.common.vcard.VCardService;
import com.android.contacts.common.vcard.NotificationImportExportListener;
import android.R.integer;
import android.app.PendingIntent;
import android.widget.RemoteViews;
import com.android.contacts.common.util.MstUtils;
import com.android.contacts.common.util.AccountSelectionUtil;
import mst.app.dialog.AlertDialog;
//import android.app.ProgressDialog;
import mst.view.menu.BottomWidePopupMenu;
import mst.app.dialog.ProgressDialog;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.PeopleActivity.ContactSaveCompletedReceiver;
import com.android.contacts.common.vcard.ExportVCardActivity;
import com.android.contacts.mst.MstExportContactsToSimService;

import static android.view.Window.PROGRESS_VISIBILITY_OFF;
import static android.view.Window.PROGRESS_VISIBILITY_ON;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.ContactListMultiChoiceActivity;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.util.PDebug;
import com.mediatek.storage.StorageManagerEx;
import android.telephony.SubscriptionManager;

import android.accounts.Account;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Data;
import mst.provider.ContactsContract.RawContacts;
import mst.provider.ContactsContract.CommonDataKinds.Email;
import mst.provider.ContactsContract.CommonDataKinds.GroupMembership;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import mst.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceGroup;
import mst.widget.toolbar.Toolbar;

import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.R;


public class MstContactImportExportActivity extends PreferenceActivity implements OnPreferenceClickListener {

	private static final String TAG = "MstContactImportExportActivity";
	/*
	 * To unify the storages(includes internal storage and external storage)
	 * handling, we looks all of storages as one kind of account type.
	 */
	public static final String STORAGE_ACCOUNT_TYPE = "_STORAGE_ACCOUNT";
	// Phone in
	private Preference mSimToPhonePref;
	//	private Preference mSim0ToPhonePref;
	//	private Preference mSim1ToPhonePref;
	private Preference mSDToPhonePref;
	// Phone out
	private Preference mPhoneToSDPref;
	private Preference mPhoneToSimPref;
	//	private Preference mPhoneToSim0Pref;
	//	private Preference mPhoneToSim1Pref;
	private Preference mDeleteSimContacts,mPartDeleteSimContacts;

	private static final int SUBACTIVITY_ACCOUNT_FILTER = 2;

	protected static final int SIM_CONTACTS_LOADED = 0;

	private static final boolean DBG = true;

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event) {
	//		Log.d(TAG,"onKeyDown:"+keyCode+" isThreadRunning:"+isThreadRunning);
	//		if(keyCode==KeyEvent.KEYCODE_BACK &&isThreadRunning){
	//			return true;
	//		}
	//		return super.onKeyDown(keyCode, event);
	//	}
	private final int PROVISIONED = 1;
	private final int NOT_PROVISIONED = 0;
	private final int INVALID_STATE = -1;
	private final int CARD_NOT_PRESENT = -2;
	public boolean isMultiSimEnabled;//是否启用双卡
	public int slot0Status;//卡槽1状态
	public int slot1Status;//卡槽2状态
	public boolean reQueryisMultiSimEnabled(){
		Log.d(TAG,"reQueryisMultiSimEnabled");
		slot0Status=getSlotProvisionStatus(0);
		slot1Status=getSlotProvisionStatus(1);
		Log.d(TAG,"slot0Status:"+slot0Status+" slot1Status:"+slot1Status);
		if(slot0Status==1&&slot1Status==1) isMultiSimEnabled=true;
		else isMultiSimEnabled=false;
		Log.d(TAG,"isMultiSimEnabled:"+isMultiSimEnabled);
		return isMultiSimEnabled;
	}
	private int getSlotProvisionStatus(int slot) {
		int provisionStatus = -1;
		try {
			//get current provision state of the SIM.
			IExtTelephony extTelephony =
					IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
			provisionStatus =  extTelephony.getCurrentUiccCardProvisioningStatus(slot);
		} catch (RemoteException ex) {
			provisionStatus = INVALID_STATE;
			Log.e(TAG,"Failed to get slotId: "+ slot +" Exception: " + ex);
		} catch (NullPointerException ex) {
			provisionStatus = INVALID_STATE;
			Log.e(TAG,"Failed to get slotId: "+ slot +" Exception: " + ex);
		}
		return provisionStatus;
	}

	Context mActivity;
	private Toolbar toolbar;
	//	private boolean hasSimCard;
	BottomWidePopupMenu bottomWidePopupMenu,bottomWidePopupMenu2,bottomWidePopupMenu3,bottomWidePopupMenu4;
	//	private ContactListFilterController mContactListFilterController;
	SubscriptionManager mSubscriptionManager;
	List<SubscriptionInfo> mActiveSimInfoList;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate1");
		nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);  
		toolbar = getToolbar();
		toolbar.setTitle(getResources().getString(R.string.mst_preference_contactio));
		toolbar.setElevation(0f);
		mActivity = MstContactImportExportActivity.this;
		//		hasSimCard=hasSimCard();
		addPreferencesFromResource(R.xml.preference_contact_io);
		isMultiSimEnabled=reQueryisMultiSimEnabled();
		findPreferences();
		//		bindListenerToPreference();
		final int simCount = TelephonyManager.getDefault().getSimCount();

		//		if (simCount == 2) {
		//			((PreferenceGroup)(getPreferenceScreen().getPreference(0))).removePreference(mSimToPhonePref);
		//			((PreferenceGroup)(getPreferenceScreen().getPreference(1))).removePreference(mPhoneToSimPref);
		//		} else {
		//			((PreferenceGroup)(getPreferenceScreen().getPreference(0))).removePreference(mSim0ToPhonePref);
		//			((PreferenceGroup)(getPreferenceScreen().getPreference(0))).removePreference(mSim1ToPhonePref);
		//			((PreferenceGroup)(getPreferenceScreen().getPreference(1))).removePreference(mPhoneToSim0Pref);
		//			((PreferenceGroup)(getPreferenceScreen().getPreference(1))).removePreference(mPhoneToSim1Pref);
		//		}
		mSubscriptionManager = new SubscriptionManager(MstContactImportExportActivity.this);
		//		new Thread(new Runnable() {
		//			public void run() {
		//				mActiveSimInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
		//				Message message = Message.obtain(mHandler, SIM_CONTACTS_LOADED, null);
		//				mHandler.sendMessage(message);
		//			}
		//		});


		mQueryHandler = new QueryHandler(getContentResolver());
		mEmptyText = (TextView) findViewById(android.R.id.empty);
		mAccount = new Account("Phone","Local Phone Account");


		Log.d(TAG,"isMultiSimEnabled:"+isMultiSimEnabled);

		bottomWidePopupMenu = new BottomWidePopupMenu(mActivity);
		bottomWidePopupMenu.inflateMenu(R.menu.sim_import_export_selection);
		bottomWidePopupMenu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onItemClicked(MenuItem item) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onItemClicked Item:"+item.getTitle());
				switch(item.getItemId()){
				case R.id.import_from_sim1_menu:{
					query(0);
					break;
				}
				case R.id.import_from_sim2_menu:{
					query(1);
					break;
				}
				default:
					break;
				}
				return true;
			}
		});

		bottomWidePopupMenu2 = new BottomWidePopupMenu(mActivity);
		bottomWidePopupMenu2.inflateMenu(R.menu.sim_import_export_selection2);
		bottomWidePopupMenu2.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onItemClicked(MenuItem item) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onItemClicked Item:"+item.getTitle());
				switch(item.getItemId()){

				case R.id.export_to_sim1_menu:{
					slotId=0;
					Intent intent = new Intent(
							"android.intent.action.contacts.list.PICKMULTIPHONES");
					intent.setType("vnd.android.cursor.dir/phone");
					try {
						startActivityForResult(intent, REQUEST_CODE_PICK);
					} catch (ActivityNotFoundException ex) {
						Toast.makeText(MstContactImportExportActivity.this, "contact_app_not_found", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				case R.id.export_to_sim2_menu:{
					slotId=1;
					Intent intent = new Intent(
							"android.intent.action.contacts.list.PICKMULTIPHONES");
					intent.setType("vnd.android.cursor.dir/phone");
					try {
						startActivityForResult(intent, REQUEST_CODE_PICK);
					} catch (ActivityNotFoundException ex) {
						Toast.makeText(MstContactImportExportActivity.this, "contact_app_not_found", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				default:
					break;
				}
				return true;
			}
		});

		bottomWidePopupMenu3 = new BottomWidePopupMenu(mActivity);
		bottomWidePopupMenu3.inflateMenu(R.menu.sim_import_export_selection3);
		bottomWidePopupMenu3.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onItemClicked(MenuItem item) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onItemClicked Item:"+item.getTitle());
				switch(item.getItemId()){

				case R.id.mst_delete_sim1_contacts:{
					slotId=0;
					DeleteAllSimContactsThread deleteThread = new DeleteAllSimContactsThread();
					if (mCursor == null) {
						Toast.makeText(mActivity, "SIM卡无联系人，或者先点击导入联系人按钮查询一次（但不要导入）", Toast.LENGTH_LONG).show();
						break;
					}
					prepareProgressDialog("正在删除SIM卡联系人", "正在删除SIM卡联系人",mCursor.getCount());
					mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							"取消", deleteThread);
					mProgressDialog.show();
					deleteThread.start();
					break;
				}
				case R.id.mst_delete_sim2_contacts:{
					slotId=1;
					DeleteAllSimContactsThread deleteThread = new DeleteAllSimContactsThread();
					if (mCursor == null) {
						Toast.makeText(mActivity, "SIM卡无联系人，或者先点击导入联系人按钮查询一次（但不要导入）", Toast.LENGTH_LONG).show();
						break;
					}
					prepareProgressDialog("正在删除SIM卡联系人", "正在删除SIM卡联系人",mCursor.getCount());
					mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							"取消", deleteThread);
					mProgressDialog.show();
					deleteThread.start();
					break;
				}
				default:
					break;
				}

				return true;
			}
		});

		bottomWidePopupMenu4 = new BottomWidePopupMenu(mActivity);
		bottomWidePopupMenu4.inflateMenu(R.menu.sim_import_export_selection3);
		bottomWidePopupMenu4.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onItemClicked(MenuItem item) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onItemClicked Item:"+item.getTitle());
				switch(item.getItemId()){

				case R.id.mst_delete_sim1_contacts:{
					slotId=0;
					DeleteAllSimContactsThread deleteThread = new DeleteAllSimContactsThread(10);
					if (mCursor == null) {
						Toast.makeText(mActivity, "SIM卡无联系人，或者先点击导入联系人按钮查询一次（但不要导入）", Toast.LENGTH_LONG).show();
						break;
					}
					prepareProgressDialog("正在删除SIM卡联系人", "正在删除SIM卡联系人",10);
					mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							"取消", deleteThread);
					mProgressDialog.show();
					deleteThread.start();
					break;
				}
				case R.id.mst_delete_sim2_contacts:{
					slotId=1;
					DeleteAllSimContactsThread deleteThread = new DeleteAllSimContactsThread(10);
					if (mCursor == null) {
						Toast.makeText(mActivity, "SIM卡无联系人，或者先点击导入联系人按钮查询一次（但不要导入）", Toast.LENGTH_LONG).show();
						break;
					}
					prepareProgressDialog("正在删除SIM卡联系人", "正在删除SIM卡联系人",10);
					mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							"取消", deleteThread);
					mProgressDialog.show();
					deleteThread.start();
					break;
				}
				default:
					break;
				}

				return true;
			}
		});

		myReceiver = new ContactsExportCompletedReceiver();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction("CONTACTS_EXPORT_FULL");
		intentFilter.addAction("CONTACTS_EXPORT_PART_FULL");
		intentFilter.addAction("CONTACTS_EXPORT_DOING");
		intentFilter.addAction("USER_CANCEL_EXPORT");
		intentFilter.addAction("EXPORT_TO_SD_CARD_DOING");
		intentFilter.addAction("CONTACTS_IMPORT_FROM_SD_DOING");
		intentFilter.addAction("CONTACTS_IMPORT_CANCEL_COMPLETE");
		intentFilter.addAction("CONTACTS_IMPORT_FROM_SD_COMPLETE");
		intentFilter.addAction(ACTION_SIM_STATE_CHANGED);
		registerReceiver(myReceiver, intentFilter);

		Intent notificationIntent = new Intent(this,MstContactImportExportActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);   
	}
	private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	private ContactsExportCompletedReceiver myReceiver;
	private int totalContactsForExport=0;
	public class ContactsExportCompletedReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			Bundle bundle=intent.getExtras();
			Log.d(TAG,"onReceive,action:"+action+" bundle:"+bundle);
			if(TextUtils.equals(action, "CONTACTS_EXPORT_FULL")){
				if(mProgressDialog!=null) mProgressDialog.dismiss();
				AlertDialog d=new AlertDialog.Builder(MstContactImportExportActivity.this)
				.setTitle(getString(R.string.mst_menu_export))
				.setMessage(getString(R.string.mst_import_sim_contacts_full))
				.setNegativeButton(getString(R.string.mst_ok),new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(mProgressDialog!=null) mProgressDialog.dismiss();
					}
				}).create();
				d.show();
			}else if(TextUtils.equals(action, "CONTACTS_EXPORT_PART_FULL")){
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();
				}

				if(!isRunningForeground()){
					Message message=Message.obtain(mHandler,UPDATE_NOTIFICATION_PROGRESSBAR_FOR_EXPORT_PART_FULL,bundle.getInt("index"));
					mHandler.sendMessage(message);
				}else{
					new AlertDialog.Builder(MstContactImportExportActivity.this)
					.setTitle(getString(R.string.mst_menu_export))
					.setMessage(getString(R.string.mst_import_sim_contacts_part_success,bundle.getInt("index"),bundle.getInt("totalContacts")-bundle.getInt("index")))
					.setNegativeButton(getString(R.string.mst_ok),new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(mProgressDialog!=null) mProgressDialog.dismiss();
						}
					})
					.show();
				}
			}else if(TextUtils.equals(action, "CONTACTS_EXPORT_DOING")){
				int index=bundle.getInt("index");
				totalContactsForExport=bundle.getInt("totalContacts");

				if(index==0){
					prepareProgressDialog(getString(R.string.mst_menu_export), getString(R.string.mst_export_sim_contacts_doing),totalContactsForExport);
					mProgressDialog.setButton(getString(R.string.mst_cancel), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Log.d(TAG,"onClick cancel export");
							MstExportContactsToSimService.isCancel=true;
						}
					});


					//					notification=new Notification(R.drawable.ic_contacts_holo_dark,getString(R.string.mst_export_sim_contacts_doing),
					//							System.currentTimeMillis());  
					//					notification.contentView = new RemoteViews(getPackageName(),R.layout.mst_import_sim_contacts_notification);  
					//					//使用notification.xml文件作VIEW  
					//					notification.contentView.setProgressBar(R.id.pb, totalContactsForExport,0, false);  
					//					notification.contentView.setTextViewText(R.id.text1, getString(R.string.mst_export_sim_contacts_doing));
					//					//设置进度条，最大值 为100,当前值为0，最后一个参数为true时显示条纹  
					//					Intent notificationIntent = new Intent(MstContactImportExportActivity.this,MstContactImportExportActivity.class);
					//					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					//					PendingIntent contentIntent = PendingIntent.getActivity(MstContactImportExportActivity.this,0,notificationIntent,0);   
					//					notification.contentIntent = contentIntent; 
				}else{
					mProgressDialog.incrementProgressBy(1);
					if(index==totalContactsForExport){
						if(mProgressDialog!=null) {
							mProgressDialog.dismiss();
						}
						Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_export_sim_contacts_result,index), Toast.LENGTH_LONG).show();
					}
				}

				if(!isRunningForeground()){
					Message message=Message.obtain(mHandler,UPDATE_NOTIFICATION_PROGRESSBAR_FOR_EXPORT,index);
					mHandler.sendMessage(message);
				}
			}else if(TextUtils.equals(action, "USER_CANCEL_EXPORT")){
				int index=bundle.getInt("index");
				totalContactsForExport=bundle.getInt("totalContacts");
				Message message=Message.obtain(mHandler,USER_CANCEL_EXPORT,index);
				mHandler.sendMessage(message);

			}else if(TextUtils.equals(action, "EXPORT_TO_SD_CARD_DOING")){
				int index=bundle.getInt("index");
				int total=bundle.getInt("totalContacts");
				int mJobId=bundle.getInt("mJobId");
				String displayName=bundle.getString("displayName");

				if(index==0){
					prepareProgressDialog(getString(R.string.mst_menu_export_to_sd), getString(R.string.mst_export_sim_contacts_doing_to_sd,total),total);
					mProgressDialog.setButton(getString(R.string.mst_cancel), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Log.d(TAG,"onClick cancel export");
							//							Intent intent=new Intent("CONTACTS_EXPORT_CANCEL");
							//							MstContactImportExportActivity.this.sendBroadcast(intent);

							if(mProgressDialog!=null) {
								mProgressDialog.dismiss();

							}
							Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_cancel_msg), Toast.LENGTH_LONG).show();

							final Intent intent = new Intent(MstContactImportExportActivity.this, CancelActivity.class);
							final Uri uri = (new Uri.Builder())
									.scheme("invalidscheme")
									.authority("invalidauthority")
									.appendQueryParameter(CancelActivity.JOB_ID, String.valueOf(mJobId))
									.appendQueryParameter(CancelActivity.DISPLAY_NAME, displayName)
									.appendQueryParameter(CancelActivity.TYPE, String.valueOf(VCardService.TYPE_EXPORT)).build();
							intent.setData(uri);
							startActivity(intent);	
						}
					});
				}else{
					mProgressDialog.incrementProgressBy(1);
					if(index==total) {
						if(mProgressDialog!=null) {
							mProgressDialog.dismiss();

						};
						Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_export_sim_contacts_result,index),
								Toast.LENGTH_LONG).show();
					}
				}
			}else if(TextUtils.equals(action, "CONTACTS_IMPORT_FROM_SD_DOING")){
				int index=bundle.getInt("index");
				int total=bundle.getInt("totalContacts");
				int mJobId=bundle.getInt("mJobId");
				String displayName=bundle.getString("displayName");

				Log.d(TAG,"CONTACTS_IMPORT_FROM_SD_DOING,index:"+index+" total:"+total);
				if(index==1){
					Log.d(TAG,"index 1");					
					prepareProgressDialog(getString(R.string.mst_menu_import_from_sd), getString(R.string.mst_import_sim_contacts_doing_from_sd), total);
					mProgressDialog.setButton(getString(R.string.mst_cancel), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(mProgressDialog!=null) {
								mProgressDialog.dismiss();							
							}

							prepareProgressDialogSpinner(null, getString(R.string.mst_import_canceling_message));

							final Intent intent = new Intent(MstContactImportExportActivity.this, CancelActivity.class);
							final Uri uri = (new Uri.Builder())
									.scheme("invalidscheme")
									.authority("invalidauthority")
									.appendQueryParameter(CancelActivity.JOB_ID, String.valueOf(mJobId))
									.appendQueryParameter(CancelActivity.DISPLAY_NAME, displayName)
									.appendQueryParameter(CancelActivity.TYPE, String.valueOf(VCardService.TYPE_IMPORT)).build();
							intent.setData(uri);
							startActivity(intent);	
						}
					});
					mProgressDialog.incrementProgressBy(1);
				}else{
				    //modify by lgy for 3430302
                    if(mProgressDialog != null) {
                        mProgressDialog.incrementProgressBy(1);
                    }
					if(index==total){
						if(mProgressDialog!=null) {
							mProgressDialog.dismiss();
						}
						prepareProgressDialogSpinner(null, getResources().getString(R.string.mst_saving_contacts));						
					}
				}			
			}else if(TextUtils.equals(action, "CONTACTS_IMPORT_CANCEL_COMPLETE")){
				Log.d(TAG,"CONTACTS_IMPORT_CANCEL_COMPLETE1");
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();
				}
				Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_cancel_msg), Toast.LENGTH_LONG).show();
			}else if(TextUtils.equals(action, "CONTACTS_IMPORT_FROM_SD_COMPLETE")){
				Log.d(TAG,"CONTACTS_IMPORT_FROM_SD_COMPLETE");
				mProgressDialog.incrementProgressBy(1);
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();				
					Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_import_sim_contacts_result,String.valueOf(intent.getExtras().getInt("mTotalCount"))), Toast.LENGTH_LONG).show();
				}				
			}else if(TextUtils.equals(action, ACTION_SIM_STATE_CHANGED)){
				isMultiSimEnabled=reQueryisMultiSimEnabled();
				if(mProgressDialog!=null){
					mProgressDialog.dismiss();
					mProgressDialog=null;
				}
				if(slot0Status==-2||slot1Status==-2){
					mHandler.postDelayed(runnable, 2000);
				}else{
					updateSimPreference(slot0Status==1||slot1Status==1);
				}
			}
		}
	}

	private Runnable runnable=new Runnable() {

		@Override
		public void run() {
			Log.d(TAG,"runnable");
			// TODO Auto-generated method stub
			isMultiSimEnabled=reQueryisMultiSimEnabled();
			Message message = Message.obtain(mHandler, UPDATE_SIM_PREFERENCE);
			mHandler.sendMessage(message);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(myReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TextView mEmptyText;
	/**
	 * Find all the Preference by key,
	 */
	private void findPreferences() {
		mSimToPhonePref = findPreference("mst_sim_to_phone");		
		mSDToPhonePref = findPreference("mst_sd_to_phone");	
		mPhoneToSimPref = findPreference("mst_phone_to_sim");
		mPhoneToSDPref = findPreference("mst_phone_to_sd");
		mDeleteSimContacts= findPreference("mst_delete_sim_contacts");
		mPartDeleteSimContacts= findPreference("mst_part_delete_sim_contacts");

		mSDToPhonePref.setEnabled(true);
		mSDToPhonePref.setSelectable(true);

		mPhoneToSDPref.setEnabled(true);
		mPhoneToSDPref.setSelectable(true);	

		updateSimPreference(slot0Status==1||slot1Status==1);

		mSimToPhonePref.setOnPreferenceClickListener(this);
		mSDToPhonePref.setOnPreferenceClickListener(this);
		mPhoneToSimPref.setOnPreferenceClickListener(this);
		mPhoneToSDPref.setOnPreferenceClickListener(this);
		mDeleteSimContacts.setOnPreferenceClickListener(this);
		mPartDeleteSimContacts.setOnPreferenceClickListener(this);
	}

	private void updateSimPreference(boolean hasSimCard){
		mSimToPhonePref.setEnabled(hasSimCard);
		mSimToPhonePref.setSelectable(hasSimCard);

		mPhoneToSimPref.setEnabled(hasSimCard);
		mPhoneToSimPref.setSelectable(hasSimCard);		

		mDeleteSimContacts.setEnabled(hasSimCard);
		mDeleteSimContacts.setSelectable(hasSimCard);

		mPartDeleteSimContacts.setEnabled(hasSimCard);
		mPartDeleteSimContacts.setSelectable(hasSimCard);
	}

	private void bindListenerToPreference() {
		//		/*
		//		 * bind ClickListener
		//		 */
		//		mNormalPrefs1.setOnPreferenceClickListener(this);
		//		mNormalPrefs2.setOnPreferenceClickListener(this);

	}


	@Override
	public boolean onPreferenceClick(Preference preference) {

		Log.d(TAG,"onPreferenceClick:"+preference);
		if(preference==mSimToPhonePref){//从sim导入到手机
			if(isMultiSimEnabled){
				Log.d(TAG,"onPreferenceClick:mSimToPhonePref");		
				bottomWidePopupMenu.show();

			}else{
				query(-1);
			}
		}else if(preference==mPhoneToSimPref){//从手机导出到sim
			Log.d(TAG,"onPreferenceClick:mPhoneToSim0Pref");
			if(isMultiSimEnabled){
				Log.d(TAG,"onPreferenceClick:mSimToPhonePref");	
				bottomWidePopupMenu2.show();
			}else{
				slotId=-1;
				Intent intent = new Intent(
						"android.intent.action.contacts.list.PICKMULTIPHONES");
				intent.setType("vnd.android.cursor.dir/phone");
				try {
					startActivityForResult(intent, REQUEST_CODE_PICK);
				} catch (ActivityNotFoundException ex) {
					Toast.makeText(this, "contact_app_not_found", Toast.LENGTH_SHORT).show();
				}
			}

		}else if(preference==mSDToPhonePref){//SD导入到手机
		    //add by lgy for 3321097
		    if (VCardService.isProcessing(VCardService.TYPE_IMPORT)
	                || VCardService.isProcessing(VCardService.TYPE_EXPORT)) {
	            Toast.makeText(this, R.string.contact_import_export_tips, Toast.LENGTH_SHORT)
	                    .show();
	            return true;
	        }

			//			ActivitiesUtils.doImportExport(this);
			List<AccountWithDataSetEx> stores = getStorageAccounts();
			Log.d(TAG,"mSDToPhonePref:"+stores.size()+" stores:"+stores);
			if (stores != null && stores.size() > 0) {
				AccountWithDataSetEx mCheckedAccount1 = stores.get(0);
				AccountWithDataSetEx mCheckedAccount2 = new AccountWithDataSetEx("Phone","Local Phone Account",null);
				AccountSelectionUtil.doImportFromSdCard(this, mCheckedAccount1.dataSet,
						mCheckedAccount2);
			} else {
				Toast.makeText(mActivity, "未找到内部存储器", Toast.LENGTH_LONG).show();;
			}

		}else if(preference==mPhoneToSDPref){//手机导出到SD
	          //add by lgy for 3321097
		    if (VCardService.isProcessing(VCardService.TYPE_IMPORT)
	                || VCardService.isProcessing(VCardService.TYPE_EXPORT)) {
	            Toast.makeText(this, R.string.contact_import_export_tips, Toast.LENGTH_SHORT)
	                    .show();
	            return true;
	        }
		    
			Intent intent = new Intent(MstContactImportExportActivity.this, ContactListMultiChoiceActivity.class);
			intent.setAction(com.mediatek.contacts.util.ContactsIntent.LIST.ACTION_GROUP_ADD_MULTI_CONTACTS);
			intent.setType(Contacts.CONTENT_TYPE);
			//			intent.putExtra("account_type", mAccountType);
			//			intent.putExtra("account_name", mAccountName);
			startActivityForResult(intent, REQUEST_CODE_PICK_FOR_SDCARD_EXPORT);

		}else if(preference==mDeleteSimContacts){
			if(isMultiSimEnabled){
				Log.d(TAG,"onPreferenceClick:mDeleteSimContacts");	
				bottomWidePopupMenu3.show();
			}else{
				DeleteAllSimContactsThread deleteThread = new DeleteAllSimContactsThread();
				if (mCursor == null) {
					Toast.makeText(mActivity, "SIM卡无联系人，或者先点击导入联系人按钮查询一次（无需导入）", Toast.LENGTH_LONG).show();
					return true;
				}
				prepareProgressDialog("正在删除SIM卡联系人", "正在删除SIM卡联系人",mCursor.getCount());
				mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"取消", deleteThread);
				mProgressDialog.show();
				deleteThread.start();
			}
		}else if(preference==mPartDeleteSimContacts){
			if(isMultiSimEnabled){
				Log.d(TAG,"onPreferenceClick:mDeleteSimContacts");	
				bottomWidePopupMenu4.show();
			}else{
				DeleteAllSimContactsThread deleteThread = new DeleteAllSimContactsThread(10);
				if (mCursor == null) {
					Toast.makeText(mActivity, "SIM卡无联系人，或者先点击导入联系人按钮查询一次（无需导入）", Toast.LENGTH_LONG).show();
					return true;
				}
				prepareProgressDialog("正在删除SIM卡联系人", "正在删除SIM卡联系人",10);
				mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"取消", deleteThread);
				mProgressDialog.show();
				deleteThread.start();
			}
		}

		//		else if(preference==mPhoneToSim1Pref){//从手机导入到sim2
		//			Log.d(TAG,"onPreferenceClick:mPhoneToSim1Pref");
		//			slotId=1;
		//			Intent intent = new Intent(
		//					"android.intent.action.contacts.list.PICKMULTIPHONES");
		//			intent.setType("vnd.android.cursor.dir/phone");
		//			try {
		//				startActivityForResult(intent, REQUEST_CODE_PICK);
		//			} catch (ActivityNotFoundException ex) {
		//				Toast.makeText(this, "contact_app_not_found", Toast.LENGTH_SHORT).show();
		//			}
		//		}
		return true;
	}

	public List<AccountWithDataSetEx> getStorageAccounts() {
		List<AccountWithDataSetEx> storageAccounts = new ArrayList<AccountWithDataSetEx>();
		StorageManager storageManager = (StorageManager) getApplicationContext().getSystemService(
				STORAGE_SERVICE);
		if (null == storageManager) {
			Log.w(TAG, "[getStorageAccounts]storageManager is null!");
			return storageAccounts;
		}
		String defaultStoragePath = StorageManagerEx.getDefaultPath();
		if (!storageManager.getVolumeState(defaultStoragePath).equals(Environment.MEDIA_MOUNTED)) {
			Log.w(TAG, "[getStorageAccounts]State is  not MEDIA_MOUNTED!");
			return storageAccounts;
		}

		// change for ALPS02390380, different user can use different storage, so change the API
		// to user related API.
		StorageVolume volumes[] = StorageManager.getVolumeList(UserHandle.myUserId(),
				StorageManager.FLAG_FOR_WRITE);
		if (volumes != null) {
			Log.d(TAG, "[getStorageAccounts]volumes are: " + volumes);
			for (StorageVolume volume : volumes) {
				String path = volume.getPath();
				//if (!Environment.MEDIA_MOUNTED.equals(path)) {
				//        continue;
				// }
				storageAccounts.add(new AccountWithDataSetEx(volume.getDescription(this),
						STORAGE_ACCOUNT_TYPE, path));
			}
		}
		return storageAccounts;
	}

	private File getExternalStorageDirectory() {
		//String path = StorageManagerEx.getDefaultPath();
		//        String path = StorageManagerEx.getExternalStoragePath();
		//        final File file = getDirectory(path, Environment.getExternalStorageDirectory().toString());
		final File file=new File(Environment.getExternalStorageDirectory().toString());
		Log.d(TAG, "[getExternalStorageDirectory]file.path : " + file.getPath());

		return file;
	}

	private final static int REPLACE_ATTACHMEN_MASK = 1 << 16;
	private int getRequestCode(int requestCode) {
		return requestCode & ~REPLACE_ATTACHMEN_MASK;
	}
	private NotificationManager nm;
	int notification_id=19172439; 
	int finish_notification_id=19172440;

	int notification_id_for_export=19172441; 
	int finish_notification_id_for_export=19172442;
	private Notification notification; 
	private PendingIntent contentIntent;
	@Override
	protected void onActivityResult(int maskResultCode, int resultCode, Intent data) {

		log("onActivityResult: requestCode=" + getRequestCode(maskResultCode) +
				", resultCode=" + resultCode + ", data=" + data);

		if (resultCode != RESULT_OK){
			log("bail due to resultCode=" + resultCode);
			return;
		}
		int requestCode = getRequestCode(maskResultCode);
		switch (requestCode) {
		case REQUEST_CODE_PICK:
			if (data != null) {
				processPickResultMst(data);
			}
			break;

		case REQUEST_CODE_PICK_FOR_SDCARD_EXPORT:
			if(data!=null){
				processPickResultMstForExportToSD(data);
			}
			break;

		case REQUEST_CODE_IMPORT:
			if(data!=null){
				ids=data.getIntegerArrayListExtra("ids");
				Log.d(TAG,"ids:"+ids);
				if(ids!=null&&ids.size()>0){					
					//					notification=new Notification(R.drawable.ic_contacts_holo_dark,getString(R.string.mst_import_sim_contacts_doing),
					//							System.currentTimeMillis());  
					//					notification.contentView = new RemoteViews(getPackageName(),R.layout.mst_import_sim_contacts_notification);   
					//					//使用notification.xml文件作VIEW  
					//					notification.contentView.setProgressBar(R.id.pb, ids.size(),0, false);  
					//					notification.contentView.setTextViewText(R.id.text1, getString(R.string.mst_import_sim_contacts_doing));
					//设置进度条，最大值 为100,当前值为0，最后一个参数为true时显示条纹  

					//					notification.contentIntent = contentIntent; 
					//					nm.notify(notification_id, notification); 


					importAllSimContactsThread = new ImportAllSimContactsThread();
					prepareProgressDialog(getString(R.string.mst_menu_import), getString(R.string.mst_import_sim_contacts_doing),ids.size());				
					mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							getString(R.string.mst_cancel), importAllSimContactsThread);
					importAllSimContactsThread.start();
				}
			}
			break;
		}
	}

	private ImportAllSimContactsThread importAllSimContactsThread;
	private static final String RESULT_INTENT_EXTRA_DATA_NAME = "com.mediatek.contacts.list.pickdataresult";
	private static final String RESULT_INTENT_EXTRA_CONTACTS_NAME = "com.mediatek.contacts.list.pickcontactsresult";
	private int exportContacts=0;
	private void processPickResultMst(Intent data) {

		final long[] dataIds = data.getLongArrayExtra(RESULT_INTENT_EXTRA_DATA_NAME);
		exportContacts=(dataIds==null?0:dataIds.length);
		Log.d(TAG,"dataIds:"+(dataIds==null?"null":Arrays.toString(dataIds)));

		if (dataIds == null || dataIds.length <= 0) {
			return;
		}

		Intent intent=new Intent(MstContactImportExportActivity.this,MstExportContactsToSimService.class);
		Bundle bundle=new Bundle();
		bundle.putLongArray("dataIds", dataIds);
		bundle.putInt("subId", getSubIdbySlot(mActivity, slotId));
		intent.putExtras(bundle);
		startService(intent);
	}

	private void processPickResultMstForExportToSD(Intent data) {

		long[] contactIds = data.getLongArrayExtra("com.mediatek.contacts.list.pickcontactsresult");

		if (contactIds == null || contactIds.length <= 0) {
			return;
		}

		StringBuilder selection = new StringBuilder();
		selection.append(Contacts._ID);
		selection.append(" IN (");
		selection.append(contactIds[0]);
		for (int i = 1; i < contactIds.length; i++) {
			selection.append(",");
			selection.append(contactIds[i]);
		}
		selection.append(")");

		String exportselection = selection.toString();
		Intent it = new Intent(this, ExportVCardActivity.class);
		it.putExtra("multi_export_type", 1); // TODO: 1 ,what's meaning?
		it.putExtra("exportselection", exportselection);
		it.putExtra("dest_path", getExternalStorageDirectory().getPath());
		it.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY, "PeopleActivity");
		startActivity(it);
	}


	private class DeleteAllSimContactsThread extends Thread
	implements OnCancelListener, OnClickListener {

		boolean mCanceled = false;
		int count=0;

		public DeleteAllSimContactsThread() {
			super("deleteAllSimContactsThread");
		}

		public DeleteAllSimContactsThread(int count) {
			super("deleteAllSimContactsThread");
			this.count=count;
		}

		@Override
		public void run() {
			int result = 1;
			mCursor.moveToPosition(-1);

			if(count>0){
				while (!mCanceled && mCursor.moveToNext()&&(count-->0)) {
					result = result & actuallyDeleteOneSimContact(mCursor);
					mProgressDialog.incrementProgressBy(1);
				}
			}else{
				while (!mCanceled && mCursor.moveToNext()) {
					result = result & actuallyDeleteOneSimContact(mCursor);
					mProgressDialog.incrementProgressBy(1);
				}
			}

			if(mProgressDialog!=null) {
				mProgressDialog.dismiss();

			}
			Message message = Message.obtain(mHandler, EVENT_CONTACTS_DELETED, (Integer)result);
			mHandler.sendMessage(message);
		}

		public void onCancel(DialogInterface dialog) {
			mCanceled = true;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				mCanceled = true;
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();

				}
			} else {
				Log.e(TAG, "Unknown button event has come: " + dialog.toString());
			}
		}
	}

	private boolean isRunningForeground()  
	{  
		ActivityManager am = (ActivityManager)mActivity.getSystemService(Context.ACTIVITY_SERVICE);  
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;  
		String currentPackageName = cn.getPackageName();  
		if(!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals("com.android.contacts"))  
		{  
			return true;  
		}         
		return false;  
	}  

	private static final int EVENT_CONTACTS_DELETED = 9;
	private static final int UPDATE_NOTIFICATION_PROGRESSBAR = 10;
	private static final int CANCEL_NOTIFICATION_PROGRESSBAR = 11;
	private static final int UPDATE_NOTIFICATION_PROGRESSBAR_FOR_EXPORT=12;
	private static final int UPDATE_NOTIFICATION_PROGRESSBAR_FOR_EXPORT_PART_FULL=13;
	private static final int  USER_CANCEL_IMPORT=14;
	private static final int  USER_CANCEL_EXPORT=15;
	private static final int  UPDATE_SIM_PREFERENCE=16;
	int alarmCount=0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case EVENT_CONTACTS_DELETED:
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();

				}
				int result = (Integer)msg.obj;
				if (result == 1) {
					showAlertDialog("删除成功");
				} else {
					showAlertDialog("删除失败");
				}
				break;

			case UPDATE_NOTIFICATION_PROGRESSBAR:{
				int rate=(Integer)msg.obj;
				Log.d(TAG,"rate:"+rate+" ids:"+ids.size());
				//				notification.contentView.setProgressBar(R.id.pb, ids.size(),rate, false); 
				//				notification.contentView.setTextViewText(R.id.text2, "("+rate+"/"+ids.size()+")");
				if(rate==ids.size()) {
					nm.cancel(notification_id);	
					nm.cancel(finish_notification_id);	

					String resultString=getString(R.string.mst_import_sim_contacts_result,rate);
					Intent notificationIntent = new Intent(MstContactImportExportActivity.this,PeopleActivity.class);
					notificationIntent.putExtra("from", "importfromsim");
					notificationIntent.putExtra("importSimContactsCount", rate);
					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(MstContactImportExportActivity.this,alarmCount++,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);   

					Notification notification = new Notification(R.drawable.ic_launcher_contacts, 
							resultString, System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.setLatestEventInfo(MstContactImportExportActivity.this, resultString,null, contentIntent);			
					nm.notify(finish_notification_id, notification);
				}else{
					//					nm.notify(notification_id, notification); 

					String textString=getString(R.string.mst_import_sim_contacts_doing);
					final Notification notification1 = NotificationImportExportListener.mstConstructProgressNotification(MstContactImportExportActivity.this,
							VCardService.TYPE_IMPORT, textString, textString, 0, null, ids.size(), rate,contentIntent);
					nm.notify(notification_id,notification1);
				}
				break;
			}

			case UPDATE_NOTIFICATION_PROGRESSBAR_FOR_EXPORT:{
				int rate=(Integer)msg.obj;
				Log.d(TAG,"rate:"+rate+" total:"+totalContactsForExport);;
				//				notification.contentView.setProgressBar(R.id.pb, totalContactsForExport,rate, false); 
				//				notification.contentView.setTextViewText(R.id.text2, "("+rate+"/"+totalContactsForExport+")");
				if(rate==totalContactsForExport) {
					nm.cancel(notification_id_for_export);	
					nm.cancel(finish_notification_id_for_export);	

					String resultString=getString(R.string.mst_export_sim_contacts_result,rate);
					Intent notificationIntent = new Intent(MstContactImportExportActivity.this,MstContactImportExportActivity.class);
					notificationIntent.putExtra("from", "importfromsim");
					notificationIntent.putExtra("importSimContactsCount", rate);
					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

					PendingIntent contentIntent = PendingIntent.getActivity(MstContactImportExportActivity.this,alarmCount++,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);   

					Notification notification = new Notification(R.drawable.ic_launcher_contacts, 
							resultString, System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.setLatestEventInfo(MstContactImportExportActivity.this, resultString,null, contentIntent); 					
					nm.notify(finish_notification_id_for_export, notification);
				}else{
					//					nm.notify(notification_id_for_export, notification); 

					String textString=getString(R.string.mst_export_sim_contacts_doing);
					final Notification notification1 = NotificationImportExportListener.mstConstructProgressNotification(MstContactImportExportActivity.this,
							VCardService.TYPE_IMPORT, textString, textString, 0, null, totalContactsForExport, rate,contentIntent);
					nm.notify(notification_id_for_export,notification1);
				}
				break;
			}

			case UPDATE_NOTIFICATION_PROGRESSBAR_FOR_EXPORT_PART_FULL:{
				int rate=(Integer)msg.obj;
				Log.d(TAG,"rate:"+rate+" total:"+totalContactsForExport);;
				//				notification.contentView.setProgressBar(R.id.pb, totalContactsForExport,rate, false); 	
				nm.cancel(notification_id_for_export);	
				nm.cancel(finish_notification_id_for_export);	

				String resultString=getString(R.string.mst_import_sim_contacts_part_success,rate,totalContactsForExport-rate);
				Intent notificationIntent = new Intent(MstContactImportExportActivity.this,MstContactImportExportActivity.class);
				notificationIntent.putExtra("from", "importfromsim");
				notificationIntent.putExtra("importSimContactsCount", rate);
				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);					
				PendingIntent contentIntent = PendingIntent.getActivity(MstContactImportExportActivity.this,0,notificationIntent,0);   

				Notification notification = new Notification(R.drawable.ic_launcher_contacts, 
						resultString, System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.setLatestEventInfo(MstContactImportExportActivity.this, resultString,null, contentIntent); 					
				nm.notify(finish_notification_id_for_export, notification);
				break;
			}

			case CANCEL_NOTIFICATION_PROGRESSBAR:
				nm.cancel(notification_id);
				int rate1=(Integer)msg.obj;
				if(rate1==ids.size()) {
					Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_import_sim_contacts_result,ids.size()),
							Toast.LENGTH_LONG).show();
				}
				break;

			case USER_CANCEL_IMPORT:{
				int rate2=(Integer)msg.obj;
				Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_import_sim_contacts_result_with_cancel,rate2),
						Toast.LENGTH_LONG).show();
				break;
			}

			case USER_CANCEL_EXPORT:{
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();
				}
				int rate2=(Integer)msg.obj;
				Toast.makeText(MstContactImportExportActivity.this, getString(R.string.mst_export_sim_contacts_result_with_cancel,rate2),
						Toast.LENGTH_LONG).show();
				nm.cancel(notification_id_for_export);	
				nm.cancel(finish_notification_id_for_export);
				break;
			}

			case UPDATE_SIM_PREFERENCE:{
				updateSimPreference(slot0Status==1||slot1Status==1);
				break;
			}

			default:
				break;
			}
		}
	};


	//	/**
	//	 * 判断是否包含SIM卡
	//	 *
	//	 * @return 状态
	//	 */
	//	public boolean hasSimCard() {
	//		TelephonyManager telMgr = (TelephonyManager)
	//				MstContactImportExportActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
	//		int simState = telMgr.getSimState();
	//		boolean result = true;
	//		switch (simState) {
	//		case TelephonyManager.SIM_STATE_ABSENT:
	//			result = false; // 没有SIM卡
	//			break;
	//		case TelephonyManager.SIM_STATE_UNKNOWN:
	//			result = false;
	//			break;
	//		}
	//		Log.d(TAG, result ? "有SIM卡" : "无SIM卡");
	//		return result;
	//	}

	private int actuallyDeleteOneSimContact(Cursor cursor){
		Log.d(TAG,"actuallyDeleteOneSimContact");
		final NamePhoneTypePair namePhoneTypePair =
				new NamePhoneTypePair(cursor.getString(NAME_COLUMN));
		final String name = namePhoneTypePair.name;
		final int phoneType = namePhoneTypePair.phoneType;
		final String phoneNumber = cursor.getString(NUMBER_COLUMN);

		Uri uri = resolveIntent(slotId);
		int result = -1;
		if (uri != null) {
			result = getContentResolver().delete(uri, "tag=" + name
					+ " AND number=" + phoneNumber, null);
		} else {
			Log.e(TAG, "actuallyDeleteOneSimContact: uri is null!!!");
		}
		return result;
	}


	public static final int REQUEST_CODE_PICK             = 109;
	public static final int REQUEST_CODE_IMPORT             = 110;
	public static final int REQUEST_CODE_PICK_FOR_SDCARD_EXPORT             = 111;
	void prepareProgressDialog(String title, String message,int count) {
		if(mProgressDialog!=null) {
			mProgressDialog.dismiss();

		}
		mProgressDialog = new ProgressDialog(this);
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
				Log.d(TAG, "onKey:"+keyCode);
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});

	}

	void prepareProgressDialogSpinner(String title, String message) {
		if(mProgressDialog!=null) {
			mProgressDialog.dismiss();

		}

		mProgressDialog = new ProgressDialog(this);
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

	private static final int FAILURE = 0;
	private static final int NO_CONTACTS = 2;
	private int mResult = SUCCESS;
	protected boolean mIsForeground = false;
	private boolean mSimContactsLoaded = false;

	private static final int CONTACTS_EXPORTED = 1;
	private Uri getUri() {
		final Intent intent = getIntent();
		int subId = -1;
		if (intent.hasExtra("subscription_id")) {
			subId = intent.getIntExtra("subscription_id", -1);
		}
		if (subId != -1) {
			intent.setData(Uri.parse("content://icc/adn/subId/" + subId));
		} else {
			intent.setData(Uri.parse("content://icc/adn"));
		}
		return intent.getData();
	}

	private int slotId=0;


	private ProgressDialog mProgressDialog;
	protected Cursor mCursor = null;
	protected QueryHandler mQueryHandler;
	private Account mAccount;
	private boolean isThreadRunning=false;
	ArrayList<Integer> ids=null;
	private class ImportAllSimContactsThread extends Thread implements OnCancelListener, OnClickListener {

		boolean mCanceled = false;	

		public ImportAllSimContactsThread() {
			super("ImportAllSimContactsThread");
			mCanceled=false;
		}

		@Override
		public void run() {
			isThreadRunning=true;
			final ContentValues emptyContentValues = new ContentValues();
			final ContentResolver resolver = getContentResolver();

			mCursor.moveToPosition(-1);
			int i=0;
			while (!mCanceled &&i<ids.size()&& mCursor!=null&&!mCursor.isClosed()&&mCursor.moveToPosition(ids.get(i))) {
				i++;
				actuallyImportOneSimContact(mCursor, resolver, mAccount);
				mProgressDialog.incrementProgressBy(1);

				if(!isRunningForeground()){
					Message message=Message.obtain(mHandler,UPDATE_NOTIFICATION_PROGRESSBAR,i);
					mHandler.sendMessage(message);
				}else{
					Message message=Message.obtain(mHandler,CANCEL_NOTIFICATION_PROGRESSBAR,i);
					mHandler.sendMessage(message);
				}
			}

			if (/*mIsForeground*/true) {
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();

				}
				nm.cancel(notification_id);  
			}
			isThreadRunning=false;

			if(mCanceled){
				Message message=Message.obtain(mHandler,USER_CANCEL_IMPORT,i);
				mHandler.sendMessage(message);				
			}
			//			Intent intent=new Intent(MstContactImportExportActivity.this,PeopleActivity.class);
			//			intent.putExtra("from", "importfromsim");
			//			intent.putExtra("count", ids.size());
			//			startActivity(intent);

		}

		public void onCancel(DialogInterface dialog) {
			isThreadRunning=false;
			mCanceled = true;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				mCanceled = true;
				if(mProgressDialog!=null) {
					mProgressDialog.dismiss();

				}
				isThreadRunning=false;
			} else {
				Log.e(TAG, "Unknown button event has come: " + dialog.toString());
			}
		}
	}

	private static void actuallyImportOneSimContact(
			final Cursor cursor, final ContentResolver resolver, Account account) {
		final NamePhoneTypePair namePhoneTypePair =
				new NamePhoneTypePair(cursor.getString(NAME_COLUMN));
		final String name = namePhoneTypePair.name;
		final int phoneType = namePhoneTypePair.phoneType;
		final String phoneNumber = cursor.getString(NUMBER_COLUMN);
		final String emailAddresses = cursor.getString(EMAILS_COLUMN);
		final String[] emailAddressArray;
		if (!TextUtils.isEmpty(emailAddresses)) {
			emailAddressArray = emailAddresses.split(",");
		} else {
			emailAddressArray = null;
		}

		final ArrayList<ContentProviderOperation> operationList =
				new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder =
				ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
		String myGroupsId = null;
		if (account != null) {
			builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
			builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
		} else {
			builder.withValues(sEmptyContentValues);
		}
		operationList.add(builder.build());

		builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
		builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(StructuredName.DISPLAY_NAME, name);
		operationList.add(builder.build());

		builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
		builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		builder.withValue(Phone.TYPE, phoneType);
		builder.withValue(Phone.NUMBER, phoneNumber);
		builder.withValue(Data.IS_PRIMARY, 1);
		operationList.add(builder.build());

		if (emailAddresses != null) {
			for (String emailAddress : emailAddressArray) {
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
				builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
				builder.withValue(Email.DATA, emailAddress);
				operationList.add(builder.build());
			}
		}

		if (myGroupsId != null) {
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(GroupMembership.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
			builder.withValue(GroupMembership.GROUP_SOURCE_ID, myGroupsId);
			operationList.add(builder.build());
		}

		try {
			resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
		} catch (RemoteException e) {
			Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
		} catch (OperationApplicationException e) {
			Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mCursor != null) {
			mCursor.deactivate();
		}
	}

	protected static final int NAME_COLUMN = 0;
	protected static final int NUMBER_COLUMN = 1;
	protected static final int EMAILS_COLUMN = 2;
	private static class NamePhoneTypePair {
		final String name;
		final int phoneType;
		public NamePhoneTypePair(String nameWithPhoneType) {
			// Look for /W /H /M or /O at the end of the name signifying the type
			int nameLen = nameWithPhoneType.length();
			if (nameLen - 2 >= 0 && nameWithPhoneType.charAt(nameLen - 2) == '/') {
				char c = Character.toUpperCase(nameWithPhoneType.charAt(nameLen - 1));
				if (c == 'W') {
					phoneType = Phone.TYPE_WORK;
				} else if (c == 'M' || c == 'O') {
					phoneType = Phone.TYPE_MOBILE;
				} else if (c == 'H') {
					phoneType = Phone.TYPE_HOME;
				} else {
					phoneType = Phone.TYPE_OTHER;
				}
				name = nameWithPhoneType.substring(0, nameLen - 2);
			} else {
				phoneType = Phone.TYPE_OTHER;
				name = nameWithPhoneType;
			}
		}
	}


	static final ContentValues sEmptyContentValues = new ContentValues();
	protected class QueryHandler extends AsyncQueryHandler {
		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor c) {
//			if (DBG) log("onQueryComplete: cursor.count=" + c.getCount());
			mCursor = c;

			if(mCursor==null) return;

			if(MstContactImportExportActivity.this==null||MstContactImportExportActivity.this.isFinishing()) return;
			if(mProgressDialog!=null) {
				mProgressDialog.dismiss();

			}

			if(mCursor.getCount()==0){
				new AlertDialog.Builder(MstContactImportExportActivity.this)
				.setTitle(getString(R.string.mst_menu_import))
				.setMessage(getString(R.string.mst_import_sim_contacts_no_contacts))
				.setNegativeButton(getString(R.string.mst_ok),null)
				.show();
				return;
			}

			new AlertDialog.Builder(MstContactImportExportActivity.this)
			.setTitle(getString(R.string.mst_menu_import)) 
			.setMessage(getString(R.string.mst_import_sim_contacts_query_result,mCursor.getCount()+""))
			.setPositiveButton(getString(R.string.mst_contact_import), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String[] names=null;
					String[] phoneNumbers=null;
					int[] phoneTypes=null;
					int count=mCursor.getCount();
					if(count>0){
						names=new String[count];
						phoneNumbers=new String[count];
						phoneTypes=new int[count];
						if(mCursor.moveToFirst()){
							int i=0;
							do{
								final NamePhoneTypePair namePhoneTypePair =
										new NamePhoneTypePair(mCursor.getString(NAME_COLUMN));
								final String name = namePhoneTypePair.name;
								final int phoneType = namePhoneTypePair.phoneType;
								final String phoneNumber = mCursor.getString(NUMBER_COLUMN);
								names[i]=name;
								phoneNumbers[i]=phoneNumber;
								phoneTypes[i++]=phoneType;
							}while(mCursor.moveToNext());
						}
						Intent intent=new Intent(MstContactImportExportActivity.this,MstSimContactsActivity.class);
						Bundle bundle=new Bundle();
						bundle.putStringArray("name", names);
						bundle.putStringArray("number",phoneNumbers);
						bundle.putIntArray("type", phoneTypes);
						intent.putExtras(bundle);
						startActivityForResult(intent, REQUEST_CODE_IMPORT);

					}else{
						Log.e(TAG, "cursor is null. Ignore silently.");
						Toast.makeText(mActivity, getString(R.string.mst_import_sim_contacts_no_contacts), Toast.LENGTH_LONG).show();
					}
				}
			})
			.setNegativeButton(getString(R.string.mst_cancel),null)
			.show();
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			if (DBG) log("onInsertComplete: requery");
			displayProgress(false);
			if (uri != null) {
				showAlertDialog("添加成功");
			} else {
				showAlertDialog("添加失败");
			}
			reQuery();
		}



		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			if (DBG) log("onUpdateComplete: requery");
			displayProgress(false);
			if (result == SUCCESS) {
				showAlertDialog("更新成功");
			} else {
				showAlertDialog("更新失败");
			}
			reQuery();
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			if (DBG) log("onDeleteComplete: requery");
			//			displayProgress(false);
			//			if (result == SUCCESS) {
			//				showAlertDialog(getString(R.string.contactdeleteSuccess));
			//			} else {
			//				showAlertDialog(getString(R.string.contactdeleteFailed));
			//			}
			//			reQuery();
		}
	}

	private void reQuery() {
		// TODO Auto-generated method stub

	}

	protected void showAlertDialog(String value) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("结果");
		alertDialog.setMessage(value);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Just to provide information to user no need to do anything.
			}
		});
		alertDialog.show();
	}

	protected void log(String msg) {
		Log.d(TAG, "[ADNList] " + msg);
	}

	protected void displayProgress(boolean loading) {
		if (DBG) log("displayProgress: " + loading);

	}

	public static int getSubIdbySlot(Context ctx, int slot) {  
		int subid[] =  SubscriptionManager.getSubId(slot);
		if(subid != null) {
			return subid[0];    		
		}
		return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
	}


	protected Uri resolveIntent(int slotId) {
		int subId=getSubIdbySlot(mActivity,slotId);
		Log.d(TAG,"slotId:"+slotId+" subId:"+subId);
		if (subId != -1) {
			return Uri.parse("content://icc/adn/subId/" + subId);
		} else {
			return Uri.parse("content://icc/adn");
		}
	}

	protected static final int QUERY_TOKEN = 0;
	protected static final int INSERT_TOKEN = 1;
	protected static final int UPDATE_TOKEN = 2;
	protected static final int DELETE_TOKEN = 3;
	protected int mInitialSelection = -1;
	protected static final int SUCCESS = 1;
	protected static final int FAIL = 0;

	private static final String[] COLUMN_NAMES = new String[] {
		"name",
		"number",
		"emails"
	};
	private void query(int slotId) {
		Uri uri = resolveIntent(slotId);
		if (DBG) log("query1: starting an async query,uri:"+uri);
		mQueryHandler.startQuery(QUERY_TOKEN, null, uri, COLUMN_NAMES,
				null, null,null);
		prepareProgressDialogSpinner(null, 
				getString(R.string.mst_import_sim_contacts_reading_simcontacts));
	}



}
