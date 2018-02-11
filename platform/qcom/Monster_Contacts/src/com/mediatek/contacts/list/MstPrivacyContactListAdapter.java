//add by liyang 
package com.mediatek.contacts.list;

import mst.widget.SliderView;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.android.contacts.common.mst.FragmentCallbacks;

import mst.widget.SliderView;

public class MstPrivacyContactListAdapter extends MultiBasePickerAdapter implements SliderView.OnSliderButtonLickListener{
	protected static final String TAG = "MstPrivacyContactListAdapter";
	public MstPrivacyContactListAdapter(Context context, ListView lv) {
		super(context, lv);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected void bindView(View itemView, int partition, Cursor cursor, int position) {
		super.bindView(itemView,partition,cursor,position);
		Log.d(TAG,"bindView,pos:"+position);
		final ViewHolderForContacts viewHolder = (ViewHolderForContacts) itemView.getTag();
		viewHolder.checkBox.setVisibility(View.GONE);
		final SliderView sliderView=viewHolder.sliderLayout;
		sliderView.setLockDrag(false);
		sliderView.setTag(position);
		sliderView.setOnSliderButtonClickListener(this);
	}
	
	
	@Override
	public void onSliderButtonClick(int id, View view, ViewGroup parent) {
		Log.d(TAG,"onSliderButtonClick,id:"+id+" view:"+view+" parent:"+parent+" callbacks:"+mCallbacks);
		switch (id) {
		case 1:
			// TODO Auto-generated method stub
			if(((SliderView)parent).isOpened()){
				((SliderView)parent).close(false);
			}

//			Cursor cursor = (Cursor)getItem(Integer.parseInt(parent.getTag().toString()));
//			long _id = cursor.getLong(0);	
//			Log.d(TAG,"onclick delete_view,_id:"+_id);
//			if(_id<=0) return;	
			if(mCallbacks!=null) mCallbacks.onFragmentCallback(FragmentCallbacks.REMOVE_PRIVACY_CONTACTS,Integer.parseInt(parent.getTag().toString()));
			break;
		}
	}	
}
