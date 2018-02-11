/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mst.privacy;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.incallui.R;

public class PrivacyCallContactListAdapter extends CursorAdapter {
	
	private Context mContext;
	
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
        
        RAW_CONTACT_ID
    };
	
	private static final int CONTACT_ID_INDEX = 0;
	private static final int NAME_INDEX = 1;
	private static final int LOOKUP_KEY_INDEX = 6;
	private static final int RAW_CONTACT_ID_INDEX = 8;
	
	public PrivacyCallContactListAdapter(Context context) {
		super(context, null, false);		
		mContext = context;
	}
	
   

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {		
		View v = LayoutInflater.from(mContext).inflate(R.layout.privacy_contacts_setting_item, parent, false);		
		return v;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		return super.getView(position, convertView, parent);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

        
        RelativeLayout layout = (RelativeLayout)view.findViewById(R.id.item);
	    TextView nameView = (TextView)view.findViewById(R.id.name);
		TextView dateView = (TextView)view.findViewById(R.id.date);
		int position = cursor.getPosition();

        CharSequence name = cursor.getString(NAME_INDEX);
		nameView.setText(name);

        int index = cursor.getInt(0x9);
        dateView.setText(mContext.getResources().getString(index == 0x1 ? R.string.private_noti_radio1_title : R.string.private_noti_radio2_title));
        
	}
	
	public int getContactID(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getInt(CONTACT_ID_INDEX);
			}
		}
		
		return 0;
	}
	
	public int getRawContactID(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getInt(RAW_CONTACT_ID_INDEX);
			}
		}
		
		return 0;
	}
	
	public String getName(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getString(NAME_INDEX);
			}
		}
		
		return null;
	}
	
	public Uri getContactUri(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				long contactId = cursor.getLong(CONTACT_ID_INDEX);
		        String lookupKey = cursor.getString(LOOKUP_KEY_INDEX);
		        Uri uri = Contacts.getLookupUri(contactId, lookupKey);
		        return uri;
			}
		}
		
		return null;
	}
	
}

