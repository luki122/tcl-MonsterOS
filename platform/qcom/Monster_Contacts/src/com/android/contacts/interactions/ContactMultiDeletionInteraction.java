/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.contacts.interactions;

import com.mediatek.contacts.list.service.MultiChoiceService.MstDeleteRequestListener;
import com.google.common.collect.Sets;
import mst.app.dialog.ProgressDialog;
import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;

import android.app.Activity;
//import android.app.AlertDialog;
import mst.app.dialog.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnDismissListener;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;
import mst.provider.ContactsContract.RawContacts;


import com.mediatek.contacts.list.service.MultiChoiceHandlerListener;
import com.mediatek.contacts.list.service.MultiChoiceRequest;
import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.util.Log;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
/**
 * An interaction invoked to delete multiple contacts.
 *
 * This class is very similar to {@link ContactDeletionInteraction}.
 */
public class ContactMultiDeletionInteraction extends Fragment
implements LoaderCallbacks<Cursor> {

	public interface MultiContactDeleteListener {
		void onDeletionFinished();
	}

	private static final String FRAGMENT_TAG = "deleteMultipleContacts";
	private static final String TAG = "ContactMultiDeletionInteraction";
	private static final String KEY_ACTIVE = "active";
	private static final String KEY_CONTACTS_IDS = "contactIds";
	public static final String ARG_CONTACT_IDS = "contactIds";

	private static final String[] RAW_CONTACT_PROJECTION = new String[] {
		RawContacts._ID,
		RawContacts.ACCOUNT_TYPE,
		RawContacts.DATA_SET,
		RawContacts.CONTACT_ID,
		///M:
		RawContacts.INDICATE_PHONE_SIM, // 4
		RawContacts.INDEX_IN_SIM, //5
		RawContacts.DISPLAY_NAME_PRIMARY, //6
	};

	private static final int COLUMN_INDEX_RAW_CONTACT_ID = 0;
	private static final int COLUMN_INDEX_ACCOUNT_TYPE = 1;
	private static final int COLUMN_INDEX_DATA_SET = 2;
	private static final int COLUMN_INDEX_CONTACT_ID = 3;

	private static final int COLUMN_INDICATE_PHONE_SIM = 4;
	private static final int COLUMN_INDEX_IN_SIM = 5;
	private static final int COLUMN_INDEX_DISPLAY_NAME_PRIMARY = 6;

	private boolean mIsLoaderActive;
	private TreeSet<Long> mContactIds;

	private Context mContext;
	private AlertDialog mDialog;

	List<MultiChoiceRequest> mRequests = null;
	/**
	 * Starts the interaction.
	 *
	 * @param activity the activity within which to start the interaction
	 * @param contactIds the IDs of contacts to be deleted
	 * @return the newly created interaction
	 */
	private static Handler mHandler;
	private static int selectedCount=0;
	public static ContactMultiDeletionInteraction start(
			Activity activity, TreeSet<Long> contactIds,Handler handler) {
		if (contactIds == null) {
			return null;
		}

		mHandler=handler;
		selectedCount=contactIds.size();
		final FragmentManager fragmentManager = activity.getFragmentManager();
		ContactMultiDeletionInteraction fragment =
				(ContactMultiDeletionInteraction) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
		if (fragment == null) {
			fragment = new ContactMultiDeletionInteraction();
			fragment.setContactIds(contactIds);
			fragmentManager.beginTransaction().add(fragment, FRAGMENT_TAG)
			.commitAllowingStateLoss();
		} else {
			fragment.setContactIds(contactIds);
		}

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		Log.i(TAG, "[onAttach].");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.setOnDismissListener(null);
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public void setContactIds(TreeSet<Long> contactIds) {
		Log.i(TAG, "[setContactIds]...");
		mContactIds = contactIds;
		mIsLoaderActive = true;
		if (isStarted()) {
			Log.i(TAG, "[setContactIds]isStarted.");
			Bundle args = new Bundle();
			args.putSerializable(ARG_CONTACT_IDS, mContactIds);
			getLoaderManager().restartLoader(R.id.dialog_delete_multiple_contact_loader_id,
					args, this);
		}
	}

	private boolean isStarted() {
		return isAdded();
	}

	//	private ContactsDeleteReceiver myReceiver;
	private int totalContactsForExport=0;
	//	public class ContactsDeleteReceiver extends BroadcastReceiver{
	//		@Override
	//		public void onReceive(Context context, Intent intent) {
	//			String action=intent.getAction();
	//			Bundle bundle=intent.getExtras();
	//			
	//			if(TextUtils.equals(action, "CONTACTS_DELETING")){
	//				int currentCount=bundle.getInt("currentCount");
	//				int totalCount=bundle.getInt("totalCount");
	//				String contactName=bundle.getString("contactName");
	//				Log.d(TAG,"onReceive,action:"+action+" currentCount:"+currentCount+" totalCount:"+totalCount);
	//				mProgressDialog.incrementProgressBy(1);
	//				mProgressDialog.setMessage("正在删除 "+contactName);
	//				if(currentCount==totalCount){
	//					mProgressDialog.dismiss();
	////					Toast.makeText(mContext, getString(R.string.mst_delete_success), Toast.LENGTH_LONG).show();
	//				}
	//			}
	//		}
	//	}

	@Override
	public void onStart() {
		Log.i(TAG, "[onStart]mIsLoaderActive = " + mIsLoaderActive);
		if (mIsLoaderActive) {
			Bundle args = new Bundle();
			args.putSerializable(ARG_CONTACT_IDS, mContactIds);
			getLoaderManager().initLoader(
					R.id.dialog_delete_multiple_contact_loader_id, args, this);
		}

		//		myReceiver = new ContactsDeleteReceiver();
		//		IntentFilter intentFilter=new IntentFilter();
		//		intentFilter.addAction("CONTACTS_DELETING");
		//		mContext.registerReceiver(myReceiver, intentFilter);
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mDialog != null) {
			mDialog.hide();
		}

		//		try {
		//			mContext.unregisterReceiver(myReceiver);
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.i(TAG, "[onCreateLoader]...");

		final TreeSet<Long> contactIds = (TreeSet<Long>) args.getSerializable(ARG_CONTACT_IDS);
		final Object[] parameterObject = contactIds.toArray();
		final StringBuilder builder = new StringBuilder();

		/// M: fixed only can delete 1000 contacts issue @{
		// Original code:
		// final String[] parameters = new String[contactIds.size()];
		// for (int i = 0; i < contactIds.size(); i++) {
		//     parameters[i] = String.valueOf(parameterObject[i]);
		//     builder.append(RawContacts.CONTACT_ID + " =?");
		//     if (i == contactIds.size() - 1) {
		//         break;
		//    }
		//    builder.append(" OR ");
		// }
		//  return new CursorLoader(mContext, RawContacts.CONTENT_URI, RAW_CONTACT_PROJECTION,
		//          builder.toString(), parameters, null);

		builder.append(RawContacts.CONTACT_ID + " IN (");
		for (int i = 0; i < contactIds.size(); i++) {
			builder.append(String.valueOf(parameterObject[i]));
			if (i < contactIds.size() - 1) {
				builder.append(",");
			}
		}
		builder.append(")");

		return new CursorLoader(mContext, RawContacts.CONTENT_URI, RAW_CONTACT_PROJECTION,
				builder.toString(), null, null);
		/// @}
	}

	int writableCount=0;
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i(TAG, "[onLoadFinished]...");

		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}

		if (!mIsLoaderActive) {
			Log.e(TAG, "[onLoadFinished]mIsLoaderActive is false, return!");
			return;
		}

		if (cursor == null || cursor.isClosed()) {
			Log.e(TAG, "Failed to load contacts");
			return;
		}

		// This cursor may contain duplicate raw contacts, so we need to de-dupe them first
		final HashSet<Long> readOnlyRawContacts = Sets.newHashSet();
		final HashSet<Long> writableRawContacts = Sets.newHashSet();
		final HashSet<Long> contactIds = Sets.newHashSet();

		AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
		cursor.moveToPosition(-1);
		mRequests = new ArrayList<MultiChoiceRequest>();
		while (cursor.moveToNext()) {
			final long rawContactId = cursor.getLong(COLUMN_INDEX_RAW_CONTACT_ID);
			final String accountType = cursor.getString(COLUMN_INDEX_ACCOUNT_TYPE);
			final String dataSet = cursor.getString(COLUMN_INDEX_DATA_SET);
			final long contactId = cursor.getLong(COLUMN_INDEX_CONTACT_ID);
			final long indicatePhoneSimId = cursor.getLong(COLUMN_INDICATE_PHONE_SIM);
			final long indexInSim = cursor.getLong(COLUMN_INDEX_IN_SIM);
			final String displayName = cursor.getString(COLUMN_INDEX_DISPLAY_NAME_PRIMARY);
			Log.d(TAG, "[onLoadFinished]rawContactId = " + rawContactId + ",contactId = "
					+ contactId + ",accountType = " + accountType + ",indicatePhoneSimId = "
					+ indicatePhoneSimId + ",indexInSim = " + indexInSim
					+ ",displayName = " + displayName);
			mRequests.add(new MultiChoiceRequest((int) indicatePhoneSimId, (int) indexInSim,
					(int) contactId, displayName));
			final AccountType type = accountTypes.getAccountType(accountType, dataSet);
			boolean writable = type == null || type.areContactsWritable();
			if (writable) {
				writableRawContacts.add(rawContactId);
			} else {
				readOnlyRawContacts.add(rawContactId);
			}
		}

		final int readOnlyCount = readOnlyRawContacts.size();
		writableCount = writableRawContacts.size();

		int messageId=R.string.batch_delete_confirmation;
		if (readOnlyCount > 0 && writableCount > 0) {
			messageId = R.string.batch_delete_multiple_accounts_confirmation;
			showDialog(getString(messageId));
		} else if (readOnlyCount > 0 && writableCount == 0) {
			messageId = R.string.batch_delete_read_only_contact_confirmation;
			showDialog(getString(messageId));
		} else {
			if(selectedCount==1) showDialog(getString(R.string.mst_delete_one_contact));
			else if(selectedCount>1) showDialog(getString(R.string.mst_delete_multi_contacts,selectedCount));
		}

		// Convert set of contact ids into a format that is easily parcellable and iterated upon
		// for the sake of ContactSaveService.
		///M:
		//  final Long[] contactIdObjectArray = contactIds.toArray(new Long[contactIds.size()]);
		//    final long[] contactIdArray = new long[contactIds.size()];
		//    for (int i = 0; i < contactIds.size(); i++) {
		//    contactIdArray[i] = contactIdObjectArray[i];
		//  }
//		showDialog(getString(messageId));

		// We don't want onLoadFinished() calls any more, which may come when the database is
		// updating.
		getLoaderManager().destroyLoader(R.id.dialog_delete_multiple_contact_loader_id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	///M:remove the contactIds parameter
	private void showDialog(String message) {
		mDialog = new AlertDialog.Builder(getActivity())
		.setIconAttribute(android.R.attr.alertDialogIcon)
		.setMessage(message)
		.setNegativeButton(android.R.string.cancel, null)
		.setPositiveButton(R.string.mst_delete,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				doDeleteContact();
			}
		}
				)
				.create();

		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mIsLoaderActive = false;
				mDialog = null;
			}
		});
		mDialog.show();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_ACTIVE, mIsLoaderActive);
		outState.putSerializable(KEY_CONTACTS_IDS, mContactIds);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			mIsLoaderActive = savedInstanceState.getBoolean(KEY_ACTIVE);
			mContactIds = (TreeSet<Long>) savedInstanceState.getSerializable(KEY_CONTACTS_IDS);
		}
	}

	public static final int SHOW_PROGRESS_DIALOG = 400;
	protected void doDeleteContact() {
		///M:
		//mContext.startService(ContactSaveService.createDeleteMultipleContactsIntent(mContext,
		//         contactIds));
		if(mHandler!=null){
			Message message=Message.obtain();
			message.what=SHOW_PROGRESS_DIALOG;
			message.arg1=writableCount;
			message.obj=new String[]{getString(R.string.mst_delete_contacts),getString(R.string.mst_deleting_contacts)};
			mHandler.sendMessage(message);
		}

		long time1=System.currentTimeMillis();
		handleDelete();
		notifyListenerActivity();
		Log.d(TAG,"delete spend:"+(System.currentTimeMillis()-time1));
	}

	private void notifyListenerActivity() {
		if (getActivity() instanceof MultiContactDeleteListener) {
			final MultiContactDeleteListener listener = (MultiContactDeleteListener) getActivity();
			listener.onDeletionFinished();
		}
	}

	///M: MTK delete flow include SIM
	private SendRequestHandler mRequestHandler;
	private HandlerThread mHandlerThread;

	private DeleteRequestConnection mConnection = null;

	private void handleDelete() {
		Log.d(TAG, "[handleDelete]...");
		if (mConnection != null) {
			Log.w(TAG, "[handleDelete]abort due to mConnection is not null,return.");
			return ;
		}

		startDeleteService();

		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(TAG);
			mHandlerThread.start();
			mRequestHandler = new SendRequestHandler(mHandlerThread.getLooper());
		}

		if (mRequests.size() > 0) {
			mRequestHandler.sendMessage(mRequestHandler.obtainMessage(
					SendRequestHandler.MSG_REQUEST, mRequests));
		} else {
			mRequestHandler.sendMessage(mRequestHandler.obtainMessage(SendRequestHandler.MSG_END));
		}
	}

	private MstDeleteRequestListener listener=new MstDeleteRequestListener() {

		@Override
		public void onProcessed(int requestType, int jobId, int currentCount,
				int totalCount, String contactName) {
			// TODO Auto-generated method stub
			Log.d(TAG,"onProcessed currentCount:"+currentCount+" totalCount:"+totalCount);
			if(mHandler!=null){
				Message msg=Message.obtain();
				msg.what=UPDATE_PROGRESS_DIALOG;
				msg.arg1=totalCount;
				msg.arg2=currentCount;
				msg.obj=contactName;
				mHandler.sendMessage(msg);
			}
		}
	};


	private class DeleteRequestConnection implements ServiceConnection {
		private MultiChoiceService mService;

		public boolean sendDeleteRequest(final List<MultiChoiceRequest> requests) {
			Log.d(TAG, "Send an delete request");
			if (mService == null) {
				Log.i(TAG, "mService is not ready");
				return false;
			}
			//			mService.handleDeleteRequest(requests, new MultiChoiceHandlerListener(mService));
			mService.mstHandleDeleteRequest(requests, listener,new MultiChoiceHandlerListener(mService));
			return true;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			Log.d(TAG, "onServiceConnected");
			mService = ((MultiChoiceService.MyBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "Disconnected from MultiChoiceService");
		}
	}
	public static final int UPDATE_PROGRESS_DIALOG = 300;
	private class SendRequestHandler extends Handler {
		public static final int MSG_REQUEST = 100;
		public static final int MSG_END = 200;


		private int mRetryCount = 20;

		public SendRequestHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "[handleMessage]msg.what = " + msg.what);
			if (msg.what == MSG_REQUEST) {
				if (!mConnection.sendDeleteRequest((List<MultiChoiceRequest>) msg.obj)) {
					Log.i(TAG, "[handleMessage]send fail, mRetryCount = " + mRetryCount);
					if (mRetryCount-- > 0) {
						sendMessageDelayed(obtainMessage(msg.what, msg.obj), 25);
					} else {
						sendMessage(obtainMessage(MSG_END));
					}
				} else {
					sendMessage(obtainMessage(MSG_END));
				}
				return;
			} else if (msg.what == MSG_END) {
				destroyMyself();
				return;
			}
			super.handleMessage(msg);
		}

	}

	void startDeleteService() {
		Log.i(TAG, "startDeleteService.");
		mConnection = new DeleteRequestConnection();
		// We don't want the service finishes itself just after this connection.
		Intent intent = new Intent(this.getActivity(), MultiChoiceService.class);
		getContext().startService(intent);
		getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	void destroyMyself() {
		Log.i(TAG, "[destroyMyself]mHandlerThread:" + mHandlerThread);
		if (mConnection != null) {
			getContext().unbindService(mConnection);
			mConnection = null;
		}
		if (mHandlerThread != null) {
			mHandlerThread.quit();
			mHandlerThread = null;
		}
		//		//add by lgy for 3301279
		//	      if(mProgressDialog!=null) {
		//	            mProgressDialog.dismiss();
		//	        }
	}
}
