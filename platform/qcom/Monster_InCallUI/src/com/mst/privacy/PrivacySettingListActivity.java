/**
  * Generated by smali2java 1.0.0.558
  * Copyright (C) 2013 Hensence.com
  */

package com.mst.privacy;

import com.android.incallui.InCallApp;
import com.android.incallui.R;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.os.Looper;
import android.os.Message;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.util.Log;
import java.lang.ref.WeakReference;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.database.Cursor;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import android.widget.AdapterView;
import mst.preference.PreferenceManager;
import mst.widget.toolbar.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import android.widget.ListAdapter;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.net.Uri;
import android.os.Bundle;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Parcelable;
import android.view.KeyEvent;

public class PrivacySettingListActivity extends MstActivity implements AdapterView.OnItemClickListener {
    private static final int END = 1;
    private static final int ERROR = 2;
    private static final int REFRESH = 3;
    private static final int START = 0;
    
    private SharedPreferences mPrefs;
    
    private static final String TAG = "PrivacySettingListActivity";
    private static final int WAIT_CURSOR_START = 1230;
    private static final long WAIT_CURSOR_DELAY_TIME = 500;
    private static int QUERY_TIMEOUT = 120;
    private boolean isFinished;
    private PrivacyCallContactListAdapter mAdapter;
    private Context mContext;
    private long mCurrentPrivacyAccountId;
    private boolean mDeleteContactsFromDB;

    private ListView mListView;
    private TextView mLoadingContact;
    private View mLoadingContainer;
    private TextView mNoContactsEmptyView;
    private ProgressBar mProgress;
    private QueryHandler mQueryHandler;
    
    public static final String RAW_CONTACT_ID = "name_raw_contact_id"; 
    public static final String[] CONTACT_PROJECTION_PRIMARY = new String[] {
        Contacts._ID,                           // 0
        Contacts.DISPLAY_NAME_PRIMARY,          // 1
        Contacts.CONTACT_PRESENCE,              // 2
        Contacts.CONTACT_STATUS,                // 3
        Contacts.PHOTO_ID,                      // 4
        Contacts.PHOTO_THUMBNAIL_URI,           // 5
        Contacts.LOOKUP_KEY,                    // 6
        Contacts.IS_USER_PROFILE,               // 7
        
        RAW_CONTACT_ID,
        "call_notification_type"
    };
    
    
    public static class ContactsProgressDialog extends ProgressDialog {
        public ContactsProgressDialog(Context context) {
            super(context);
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK: {
                        return true;                
                }
            }
            
            return super.onKeyDown(keyCode, event);
        }
    };
    
    
    private ContactsProgressDialog mSaveProgressDialog;
    
    private final Handler mHandler = new Handler() {
            
            public void handleMessage(Message msg) {
                    
                    switch(msg.what) {
                    case WAIT_CURSOR_START: {
                    	Log.i(TAG, "start WAIT_CURSOR_START !isFinished : "
                                + !isFinished);
                        if (!isFinished) {
                            mLoadingContainer.setVisibility(View.VISIBLE);
                            mLoadingContact.setVisibility(View.VISIBLE);
                            mProgress.setVisibility(View.VISIBLE);
                        } else {
                            mLoadingContainer.setVisibility(View.GONE);
                            mLoadingContact.setVisibility(View.GONE);
                            mProgress.setVisibility(View.GONE);
                        }
                        
                    	break;
                    }
                    
                    case START: {
                        if (!isFinishing()) {
                            if (null == mSaveProgressDialog) {
                                mSaveProgressDialog = new ContactsProgressDialog(mContext);
                            }
                            mSaveProgressDialog.setTitle(R.string.save_group_dialog_title);
                            mSaveProgressDialog.setIndeterminate(false);
                            mSaveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            try {
                                mSaveProgressDialog.show();
                            } catch (Exception e) {
                                
                            }
                        }
                        break;
                    }
                    
                    case END: {
                        if (!isFinishing() 
                                && null != mSaveProgressDialog && mSaveProgressDialog.isShowing()) {
                            try {
                                mSaveProgressDialog.dismiss();
                                mSaveProgressDialog = null;
                                finish();
                            } catch (Exception e) {
                                
                            }
                        }
                        break;
                    }
                    
                    case ERROR: {
                         Toast.makeText(mContext,
                                 R.string.sim_not_ready, Toast.LENGTH_SHORT).show();                                               
                    	finish();
                    	break;
                    }
                    
                    case REFRESH: {
                    	isFinished = true;
        				
        				if (mAdapter.getCount() <= 0) {
        					mNoContactsEmptyView.setVisibility(View.VISIBLE);
        				} else {
        					mNoContactsEmptyView.setVisibility(View.GONE);
        				}
        				
                        break;
                    }
                    
                    }
                    
                    super.handleMessage(msg);
                }
            
        };
    
    
    protected void onCreate(Bundle savedState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedState);
        
        mCurrentPrivacyAccountId = PrivacyUtils.mCurrentAccountId;
        Log.e("wangth", "PrivacyUtils.mCurrentAccountId = " + PrivacyUtils.mCurrentAccountId);
        if(mCurrentPrivacyAccountId <= 0) {
            finish();
            return;
        }
        mContext = this;
        setMstContentView(R.layout.mst_contact_list_content);
        initToolBar();
        mAdapter = new PrivacyCallContactListAdapter(mContext);
    	mQueryHandler = new QueryHandler(this);
        initView();
        InCallApp.mPrivacyActivityList.add(this);
        
    }
    
    public void onStart() {
        super.onStart();
        startQuery();
        Log.e("wangth", "current privacy account id = " + mCurrentPrivacyAccountId);
    }
    
    private void initView() {
    	mNoContactsEmptyView = (TextView)findViewById(
                R.id.no_contacts);
    	mNoContactsEmptyView.setText(mContext.getString(R.string.no_privacy_contacts));
    	
    	mLoadingContainer = findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact = (TextView)findViewById(
                R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar)findViewById(
                R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);
        
        isFinished = false;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                WAIT_CURSOR_DELAY_TIME);
        
        mListView = (ListView)findViewById(android.R.id.list);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
	    mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
    }
    
    private void startQuery() {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = 0;
				while(true) {
					if (PrivacyUtils.mIsServiceConnected) {
						break;
					}
					
					try {
						Thread.sleep(10);
						i++;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (i > 15) {
						break;
					}
				}
				
				Uri uri = Contacts.CONTENT_URI;
		    	Log.i(TAG, "current privacy account id = " + PrivacyUtils.mCurrentAccountId 
		    			+ "  PrivacyUtils.mIsServiceConnected = " + PrivacyUtils.mIsServiceConnected);
		    	
		    	String selection = "is_privacy=" + PrivacyUtils.mCurrentAccountId;
		    	mQueryHandler.startQuery(1, null, uri, CONTACT_PROJECTION_PRIMARY, selection, null, Contacts.SORT_KEY_PRIMARY);
			}
		}).start();
    }
    
    private final class QueryHandler extends AsyncQueryHandler {
		private final WeakReference<PrivacySettingListActivity> mActivity;

		/**
		 * Simple handler that wraps background calls to catch
		 * {@link SQLiteException}, such as when the disk is full.
		 */
		protected class CatchingWorkerHandler extends
				AsyncQueryHandler.WorkerHandler {
			public CatchingWorkerHandler(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				try {
					// Perform same query while catching any exceptions
					super.handleMessage(msg);
				} catch (SQLiteDiskIOException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteFullException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteDatabaseCorruptException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				}
			}
		}

		@Override
		protected Handler createHandler(Looper looper) {
			// Provide our special handler that catches exceptions
			return new CatchingWorkerHandler(looper);
		}

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<PrivacySettingListActivity>(
					(PrivacySettingListActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			final PrivacySettingListActivity activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				final PrivacyCallContactListAdapter adapter = mAdapter;
				adapter.changeCursor(cursor);
				
				isFinished = true;
				mHandler.sendEmptyMessage(WAIT_CURSOR_START);
				
				if (cursor == null) {
					Log.e(TAG, "onQueryCompleted - cursor is null");
					mNoContactsEmptyView.setVisibility(View.VISIBLE);
					return;
				}
                
				if (cursor.getCount() == 0) {
					mNoContactsEmptyView.setVisibility(View.VISIBLE);
				} else {
					mNoContactsEmptyView.setVisibility(View.GONE);
				}				
				
                Log.i(TAG, "onQueryCompleted - Count:" + cursor.getCount());
			} else {
				cursor.close();
			}
		}
	}
    
    protected void onDestroy() {
        super.onDestroy();
        if(mAdapter != null) {
            mAdapter.changeCursor(null);
        }
        InCallApp.mPrivacyActivityList.remove(this);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                finish();
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Uri uri = mAdapter.getContactUri(arg2);
        Intent intent = new Intent();
        intent.putExtra("name", mAdapter.getName(arg2));
        intent.setClassName("com.android.incallui", "com.mst.privacy.PrivacyNotificationSettings");
        intent.putExtra("contactUri", uri);
        try {
            startActivity(intent);
            return;
        } catch(ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    
    private void initToolBar() {
        getToolbar().setTitle(R.string.private_incoming_call);        
    }
    
	
}