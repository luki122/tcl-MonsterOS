package com.mst.recordsettings;

import java.util.ArrayList;

import mst.widget.toolbar.Toolbar;
import android.preference.PreferenceManager;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.R.integer;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

//import com.android.contacts.ContactsUtils;
import com.android.incallui.R;
import com.android.contacts.common.util.PermissionsUtil;
import mst.preference.*;

public class RecordSettings extends PreferenceActivity{
    
    private static String TAG = "RecordSettings";
	private Context mContext;
	private RelativeLayout mGotoHistory;
	private Switch mAutoRecord;
	private TextView mPickContactsTv, mContactsName;
	SharedPreferences mPrefs;
	
    // send to phone (0:close; 1:all; 2:select)
	private int mRecordType = 0;
	private RadioGroup mRadio;
	private RadioButton mAll, mSelect; 
	
	private static final String CALL_RECORD_TYPE = "call.record.type";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = RecordSettings.this;
		setContentView(R.layout.record_setting);
        
        mGotoHistory = (RelativeLayout) findViewById(R.id.record_history);
        if (null != mGotoHistory) {
            mGotoHistory.setOnClickListener(mGotoHistoryClickListener);
        }
        
        mPickContactsTv = (TextView) findViewById(R.id.edit_contacts);
        if (mPickContactsTv != null) {
            mPickContactsTv.setOnClickListener(mPickContactsClickListener);
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mRecordType = mPrefs.getInt(CALL_RECORD_TYPE, 0);
        mIsAll = (mRecordType == 1) || (mRecordType == 0);
        mAutoRecord = (Switch) findViewById(R.id.auto_record_switch);
        mAutoRecord.setChecked(mRecordType > 0);
        mAutoRecord.setOnCheckedChangeListener(mAutoSwitchListener);	
        
        mAll = (RadioButton) findViewById(R.id.all_record);
        mSelect = (RadioButton) findViewById(R.id.select_record);
        mRadio = (RadioGroup) findViewById(R.id.auto_group);
        if(mIsAll) {
        	mAll.setChecked(true);
        } else {
        	mSelect.setChecked(true);
        }
        mRadio.setOnCheckedChangeListener(mAutoRadioSelectListener);
        
        mContactsName = (TextView) findViewById(R.id.record_contacts);
        mContactsName.setMovementMethod(new ScrollingMovementMethod());
		 boolean hasReadContactPermission = PermissionsUtil.hasPermission(this,CONTACT_PERMISSION);
         if(!hasReadContactPermission){
       	   requestPermissions(new String[] {CONTACT_PERMISSION}, CONTACT_PERMISSION_REQUEST_CODE);
             return;
         }
         
         initToolBar();
		
        updateUI();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		// TODO Auto-generated method stub
		super.onResume();
		if(mIsNeedRefresh) {
			mIsNeedRefresh = false;
		     updateUI();
		}
	}
        	
    

    
    
    private OnClickListener mGotoHistoryClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub                	
        	Intent in = new Intent(RecordSettings.this, RecordHistory.class);
            startActivity(in);
        }
    };
    
    private static final int RESULT_PICK_CONTACT = 1;
    private OnClickListener mPickContactsClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
                        
            Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONES");  
//            String exsitNumbers = "";
//            if (!TextUtils.isEmpty(exsitNumbers)) {
//                intent.putExtra(Intents.EXTRA_PHONE_URIS, exsitNumbers);
//            }
            intent.setType("vnd.android.cursor.dir/phone");
            startActivityForResult(intent, RESULT_PICK_CONTACT);  
            mIsNeedRefresh = true;
        }
    };
    
    
    private static final String RESULTINTENTEXTRANAME = "com.mediatek.contacts.list.pickdataresult";
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data == null) {
			return;
		}

		switch (requestCode) {
		case RESULT_PICK_CONTACT:
            addAutoRecordDataToDb(data);
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
        mContext.getContentResolver().update(Data.CONTENT_URI, values, 
                selection.toString(), null);
    }
    
    private OnCheckedChangeListener mAutoSwitchListener = new OnCheckedChangeListener() {    
    	@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    		int value = isChecked ? 1 : 0;
    		if(isChecked) {
    			value = mIsAll ? 1 : 2;
    		} else {
    			value = 0;
    		}
    		mRecordType = value;
	    	setValue();
    	}
    };
    
    private boolean mIsAll = false;
    private RadioGroup.OnCheckedChangeListener mAutoRadioSelectListener = new RadioGroup.OnCheckedChangeListener() {    
    	@Override
 		public void onCheckedChanged(RadioGroup group, int checkedId) {
	    	if(mAll.getId()==checkedId){
	    		mIsAll = true;
	    	} else if(mSelect.getId()==checkedId){
	    		mIsAll = false;
	    	}
	    	mRecordType = mIsAll ? 1 : 2;
	    	setValue();
	    }    	
    };
    
    private void setValue() {
        mPrefs.edit().putInt(CALL_RECORD_TYPE, mRecordType).apply();                
        updateUI();
    }
    
    private boolean mIsNeedRefresh;
	private void updateUI() {
		if(mAutoRecord.isChecked()) {
			mRadio.setVisibility(View.VISIBLE);
			mPickContactsTv.setVisibility(View.VISIBLE);
			if(mSelect.isChecked()) {
				updateContactNames();
				mPickContactsTv.setEnabled(true);
			} else {
				mPickContactsTv.setEnabled(false);
				mContactsName.setText("");
			}		
		} else {
			mRadio.setVisibility(View.GONE);
			mPickContactsTv.setVisibility(View.GONE);
			mContactsName.setText("");
			mPickContactsTv.setEnabled(false);
		}			
	}
	
	private void updateContactNames() {		
		
		StringBuilder selection = new StringBuilder();
	//	selection.append(RawContacts.INDICATE_PHONE_SIM + " < 0");
		selection.append(" AND auto_record = 1");
		Uri uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
				ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
				.build();
		uri = uri.buildUpon()
				.appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
				.build();
				
		final String[] PROJECTION_PRIMARY = new String[] {
			Phone._ID,                          // 0
			Phone.TYPE,                         // 1
			Phone.LABEL,                        // 2
			Phone.NUMBER,                       // 3
			Phone.CONTACT_ID,                   // 4
			Phone.LOOKUP_KEY,                   // 5
			Phone.PHOTO_ID,                     // 6
			Phone.DISPLAY_NAME_PRIMARY,         // 7
	//		RawContacts.INDICATE_PHONE_SIM,     // 8

			"auto_record"
		};

		Cursor cursor = getContentResolver().query(uri, PROJECTION_PRIMARY, selection.toString(), null, null);
		StringBuilder contactNames = new StringBuilder();
		int count = 0;
		if(cursor != null) {
			while (cursor.moveToNext()) {		
				count ++;
				String name = cursor.getString(cursor
						.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
				contactNames.append(name);
				contactNames.append("\n");
				Log.d(TAG, "Name is: : " + name);
			}
			cursor.close();
		}
		if(count > 0) {
			mSelect.setText(getString(R.string.record_select_contact) + "(" + count + ")");
		} else {
			mSelect.setText(R.string.record_select_contact);
		}
		mContactsName.setText(contactNames.toString()); 
	}
	
    private static final String CONTACT_PERMISSION = "android.permission.READ_CONTACTS";
    private static final int CONTACT_PERMISSION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (requestCode == CONTACT_PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Force a refresh of the data since we were missing the permission before this.
                updateUI();
            }
        }
    }
    
	private void initToolBar() {
		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		myToolbar.setTitle(R.string.call_record);
	}

}
