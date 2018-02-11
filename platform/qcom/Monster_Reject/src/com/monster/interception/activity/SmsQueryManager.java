package com.monster.interception.activity;

import java.util.List;

import mst.app.MstActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.monster.interception.adapter.SmsAdapter;
import com.monster.interception.database.MmsDatabaseHelper;
import com.monster.interception.database.MmsItem;
import com.monster.interception.database.SmsDatabaseHelper;
import com.monster.interception.database.SmsEntity;
import com.monster.interception.R;
import mst.widget.MstListView;

public class SmsQueryManager {

	private InterceptionActivity mActivity;
	private AsyncQueryHandler mQueryHandlerSms;
	private AsyncQueryHandler mQueryHandlerMms;
	private static Uri uriSms = Uri.parse("content://sms");
	private static Uri uriMms = Uri.parse("content://mms");
	private static Uri uriName = ContactsContract.Contacts.CONTENT_URI;
	private static Uri uriNumber = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	private ContentResolver mCr;
	private boolean isQuery = false;
	private boolean smsFlag = false;
	private SmsAdapter mSmsListAdapter = null;
	private List<MmsItem> mmsItems;

	public SmsQueryManager(InterceptionActivity context) {
		mActivity = context;
		mCr = context.getContentResolver();
		mQueryHandlerSms = new QueryHandlerSms(mCr, context);
		mQueryHandlerMms = new QueryHandlerMms(mCr, context);
		mCr.registerContentObserver(uriSms, true, changeObserverMms);
		mCr.registerContentObserver(uriMms, true, changeObserverMms);
		mCr.registerContentObserver(
				android.provider.Telephony.MmsSms.CONTENT_URI, true,
				changeObserverMms);
		mCr.registerContentObserver(uriName, true, changeObserverMms);
		mCr.registerContentObserver(uriNumber, true, changeObserverMms);
	}
	
	
	void startQuerySms() {
		isQuery = true;
		mQueryHandlerSms.startQuery(0, null, uriSms, null,
				"reject=? and type=?", new String[] { "1", "1" }, "_id desc");

	}

	void startQueryMms() {
		mQueryHandlerMms
				.startQuery(0, null, uriMms, null, "reject=? and msg_box=?",
						new String[] { "1", "1" }, "_id desc");
		smsFlag = false;
		isQuery = true;
	}

	boolean isQuery() {
		return isQuery;
	}

	boolean isShouldRequery() {
		return smsFlag;
	}

	void destroy() {
		mCr.unregisterContentObserver(changeObserverMms);
		if(mSmsListAdapter != null) {
			mSmsListAdapter.changeCursor(null);
		}
	}

	private ContentObserver changeObserverMms = new ContentObserver(
			new Handler()) {

		@Override
		public void onChange(boolean selfUpdate) {

			if (mActivity.isResumed() && !mActivity.mInterceptionItemClickHelper.smsBath) {
				startQueryMms();
			} else {
				smsFlag = true;
			}

		}
	};

	private MatrixCursor cursors=null;
	private List<SmsEntity> mSmsEntityList;
	private Handler smsHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String[] tableCursor = new String[] { "_id", "address", "date",
					"body", "count", "thread_id", "ismms", "name", "read" };
			cursors = new MatrixCursor(tableCursor);
			for (int i = 0; i < mSmsEntityList.size(); i++) {
				cursors.addRow(new Object[] { i,
						mSmsEntityList.get(i).getDBPhomeNumber(),
						mSmsEntityList.get(i).getLastDate(), mSmsEntityList.get(i).getBody(),
						mSmsEntityList.get(i).getCount(),
						mSmsEntityList.get(i).getThread_id(),
						mSmsEntityList.get(i).getIsMms(), mSmsEntityList.get(i).getName(),
						mSmsEntityList.get(i).getRead() });
			}
			if (mSmsListAdapter == null) {
				cursors.moveToFirst();
				mSmsListAdapter = new SmsAdapter(mActivity,
						cursors);
				mActivity.setSmsAdapter(mSmsListAdapter);
				mSmsList.setAdapter(mSmsListAdapter);
				updateState(SHOW_SMS_LIST);
			} else {
				mSmsListAdapter.changeCursor(cursors);
				mSmsListAdapter.notifyDataSetChanged();
				updateState(SHOW_SMS_LIST);
			}
			isQuery = false;
		};
	};

	private class QueryHandlerSms extends AsyncQueryHandler {
		private final Context context;

		public QueryHandlerSms(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie,
				final Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			if (cursor != null) {
				if (!cursor.moveToFirst() && mmsItems == null) {
					updateState(SHOW_SMS_EMPTY);
					isQuery = false;
					cursor.close();
				} else {

					new Thread() {
						public void run() {
							mSmsEntityList = SmsDatabaseHelper.querySms(cursor, mmsItems,
									mActivity);
							smsHandler.obtainMessage().sendToTarget();
						};
					}.start();
				}
			} else {
				if (mSmsListAdapter != null) {
					mSmsListAdapter.changeCursor(null);
					mSmsListAdapter.notifyDataSetChanged();
				}
				updateState(SHOW_SMS_EMPTY);
				isQuery = false;
			}

		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}

	private class QueryHandlerMms extends AsyncQueryHandler {
		private final Context context;

		public QueryHandlerMms(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					mmsItems = null;
				} else {
					mmsItems = MmsDatabaseHelper.queryMms(cursor, mActivity);
				}
				//startQuerySms();
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			startQuerySms();
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}
	
	View sms;
	private MstListView mSmsList;
	private TextView mSmsEmpty;
	void setView(View v){
		sms = v;
		mSmsList=(MstListView) sms.findViewById(R.id.sms_list);
		mSmsEmpty=(TextView) sms.findViewById(R.id.sms_empty);
	}
	

	private static final int SHOW_SMS_BUSY = 3;
	private static final int SHOW_SMS_LIST = 4;
	private static final int SHOW_SMS_EMPTY = 5;
	private int mState;
	private void updateState(int state) {
		if (mState == state) {
			return;
		}

		mState = state;
		switch (state) {
		case SHOW_SMS_LIST:
			mSmsEmpty.setVisibility(View.GONE);
			mSmsList.setVisibility(View.VISIBLE);
			break;
		case SHOW_SMS_EMPTY:
			mSmsList.setVisibility(View.GONE);
			mSmsEmpty.setVisibility(View.VISIBLE);
			break;
		case SHOW_SMS_BUSY:
			mSmsList.setVisibility(View.GONE);
			mSmsEmpty.setVisibility(View.GONE);
			break;
		
		}
	}
}