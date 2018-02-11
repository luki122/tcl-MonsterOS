package com.mst.recordsettings;

import mst.preference.*;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.incallui.R;
import com.android.contacts.common.util.PermissionsUtil;

import mst.app.dialog.ProgressDialog;

public class RecordSettingsV2 extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static String TAG = "RecordSettingsV2";
	private Context mContext;
	
	private static final int RECORD_MODE_NONE = 0;
	private static final int RECORD_MODE_ALL = 1;
	private static final int RECORD_MODE_SELECT = 2;
	private int mRecordMode = 0;
		
	SharedPreferences mPrefs;

	private ListPreference mAutoRecordMode;
	private PreferenceScreen mPickContact;

	private static final String CALL_RECORD_TYPE = "call.record.type";
	private String[] mRecordModeStrs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = RecordSettingsV2.this;

		addPreferencesFromResource(R.xml.record_setting_v2);
		PreferenceScreen prefSet = getPreferenceScreen();
		mAutoRecordMode = (ListPreference) prefSet
				.findPreference("auto_record");
		mAutoRecordMode.setOnPreferenceChangeListener(this);
		mRecordModeStrs = getResources().getStringArray(R.array.auto_record_choices);

		mPickContact = (PreferenceScreen) prefSet
				.findPreference("record_contacts");

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mRecordMode = mPrefs.getInt(CALL_RECORD_TYPE, RECORD_MODE_NONE);
	
		boolean hasReadContactPermission = PermissionsUtil.hasPermission(this,
				CONTACT_PERMISSION);
		if (!hasReadContactPermission) {
			requestPermissions(new String[] { CONTACT_PERMISSION },
					CONTACT_PERMISSION_REQUEST_CODE);
			return;
		}
		
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		// TODO Auto-generated method stub
		super.onResume();
		updateUI();
	}

	private boolean isPickContactVisble = true;

	private void updateUI() {
		PreferenceScreen prefSet = getPreferenceScreen();
		mAutoRecordMode.setValueIndex(mRecordMode);
		mAutoRecordMode.setSummary(mRecordModeStrs[mRecordMode]);
		if(mRecordMode == RECORD_MODE_SELECT) {		
			if(!isPickContactVisble) {
				prefSet.addPreference(mPickContact);
				isPickContactVisble = true;
			}
		} else {
			if(isPickContactVisble) {
				prefSet.removePreference(mPickContact);
				isPickContactVisble = false;
			}
		}
	}

	public boolean onPreferenceChange(Preference preference, Object objValue) {
		if (preference == mAutoRecordMode) {
			mAutoRecordMode.setValue((String) objValue);
			mRecordMode = Integer.valueOf((String) objValue).intValue();
			mPrefs.edit().putInt(CALL_RECORD_TYPE, mRecordMode).apply();
			updateUI();
			
//			boolean isFirst = mPrefs.getBoolean("first", true);
//			if(RECORD_MODE_SELECT == mRecordMode && isFirst) {
			if(RECORD_MODE_SELECT == mRecordMode) {
//			    mPrefs.edit().putBoolean("first", false).apply();			    
			    Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONES");
	            intent.setType("vnd.android.cursor.dir/phone");
	            intent.putExtra("mstFilter", "callRecord");
	            startActivityForResult(intent, RESULT_PICK_CONTACT);
			}
		}
		return true;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mPickContact) {
            Intent intent2=new Intent("com.android.contacts.action.MST_AUTO_RECORD_CONTACTS_LIST");
            intent2.setType("vnd.android.cursor.dir/phone");
            startActivity(intent2);        
			return true;
		} 
		return false;
	}

	private static final int RESULT_PICK_CONTACT = 1;
	private static final String RESULTINTENTEXTRANAME = "com.mediatek.contacts.list.pickdataresult";

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data == null) {
			return;
		}

		 final Intent mData = data;
		switch (requestCode) {
		case RESULT_PICK_CONTACT:
	          if (resultCode == Activity.RESULT_OK) {
	              final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
	                  
	                  @Override
	                  protected void onPreExecute() {
	                      if (!isFinishing()) {
	                          if (null == mSaveProgressDialog) {
	                              mSaveProgressDialog = new RecordProgressDialog(mContext);
	                          }
	                          // mSaveProgressDialog.setTitle(R.string.save_title);
	                          mSaveProgressDialog.setIndeterminate(false);
	                          mSaveProgressDialog
	                                  .setProgressStyle(ProgressDialog.STYLE_SPINNER);
	                          try {
	                              mSaveProgressDialog.show();
	                          } catch (Exception e) {

	                          }
	                      }
	                  }
	                  
	                  @Override
	                  protected Void doInBackground(Void... params) {
	                      if (mData != null) {
	                          addAutoRecordDataToDb(mData);
	                      }
	                      return null;
	                  }

	                  @Override
	                  protected void onPostExecute(Void result) {
	                      if (!isFinishing() && null != mSaveProgressDialog
	                              && mSaveProgressDialog.isShowing()) {
	                          try {
	                              mSaveProgressDialog.dismiss();
	                              mSaveProgressDialog = null;
	                          } catch (Exception e) {

	                          }
	                      }
	                  }
	              };
	            
	              task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	         }
			break;

		default:
			break;
		}
	}
	
    private void addAutoRecordDataToDb(Intent data) {
        final long[] dataIds = data.getLongArrayExtra(RESULTINTENTEXTRANAME);
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

        values.put("auto_record", 1);
        getContentResolver().update(Data.CONTENT_URI, values,
                selection.toString(), null);
    }


	private static final String CONTACT_PERMISSION = "android.permission.READ_CONTACTS";
	private static final int CONTACT_PERMISSION_REQUEST_CODE = 1;

	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		if (requestCode == CONTACT_PERMISSION_REQUEST_CODE) {
			if (grantResults.length >= 1
					&& PackageManager.PERMISSION_GRANTED == grantResults[0]) {
				// Force a refresh of the data since we were missing the
				// permission before this.
				updateUI();
			}
		}
	}
	
    private RecordProgressDialog mSaveProgressDialog = null;

    private static class RecordProgressDialog extends ProgressDialog {
        public RecordProgressDialog(Context context) {
            super(context);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (this.isShowing()) {
                    return true;
                }
                break;
            }
            }

            return super.onKeyDown(keyCode, event);
        }
    };


}
