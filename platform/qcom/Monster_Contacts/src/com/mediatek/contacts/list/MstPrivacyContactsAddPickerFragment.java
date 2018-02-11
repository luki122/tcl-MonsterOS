//add by liyang
package com.mediatek.contacts.list;

import com.android.contacts.common.list.ContactListAdapter;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.mediatek.contacts.list.MstRecordContactListAdapter;
import com.android.contacts.R;

import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import mst.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.contacts.common.list.ContactEntryListAdapter;

import com.android.contacts.common.list.ContactListFilter;

public class MstPrivacyContactsAddPickerFragment extends MultiBasePickerFragment {

	private static final String TAG = "MstPrivacyContactListFragment";
	@Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
//        header.setVisibility(View.GONE);
//        header.setPadding(0, -2000, 0, 0);
        
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.i(TAG, "[onLoadFinished]");
		super.onLoadFinished(loader, data);
		Log.d(TAG,"onLoadFinished1");
	}
	
//	@Override
//	protected void onItemClick(int position, long id) {
//		Log.d(TAG, "[onItemClick]");
//		Intent intent = new Intent(Intent.ACTION_VIEW, getContactUri(position));
//		ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);		
//	}
	
	public Uri getContactUri(int position){
		Cursor cursor = (Cursor)getAdapter().getItem(position);
		long contactId = cursor.getLong(6);	
		String lookupKey = cursor.getString(7);		
		Uri uri = Contacts.getLookupUri(contactId, lookupKey);
		Log.d(TAG, "[onItemClick] contactId = " + contactId+" lookupKey:"+lookupKey+" uri:"+uri);
		return uri;
	}
	
    @Override
    protected ContactListAdapter createListAdapter() {
    	Log.d(TAG,"createListAdapter1");
    	MultiBasePickerAdapter adapter = new MultiBasePickerAdapter(getActivity(),
                getListView());
        adapter.setFilter(ContactListFilter
                .createFilterWithType(ContactListFilter.FILTER_TYPE_ADD_PRIVACY_CONTACT));
        adapter.setDisplayPhotos(false);
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setQuickContactEnabled(false);
        return adapter;
    }
    
}
