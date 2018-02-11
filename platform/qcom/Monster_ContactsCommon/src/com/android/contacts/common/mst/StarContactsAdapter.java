package com.android.contacts.common.mst;

import com.android.contacts.common.list.ContactEntryListFragment.ViewContactListener;
import java.util.ArrayList;

import com.android.contacts.common.R;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.list.ContactListAdapter;
import com.android.contacts.common.list.DirectoryPartition;
import com.android.contacts.common.list.ContactListAdapter.ContactQuery;

import android.content.Context;
import android.net.Uri;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Directory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StarContactsAdapter extends BaseAdapter{

	private static final String TAG = "StarContactsAdapter";
	private ArrayList<Object[]> list;
	private Context mContext;
	private ContactPhotoManager mPhotoManager;
	protected ViewContactListener mListener;
	public void setList(ArrayList<Object[]> list) {
		Log.d(TAG, "setList,size:"+list.size());
		this.list = list;
	}

	public StarContactsAdapter(ArrayList<Object[]> list,Context context,ViewContactListener mListener) {
		super();
		this.list=list;
		this.mListener=mListener;
		mContext=context;
		if (mPhotoManager == null) {
			mPhotoManager = ContactPhotoManager.getInstance(mContext);
		}
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list==null?0:list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.d(TAG,"getView,position:"+position);
		if(list==null) {		
			return new TextView(mContext);
		}
		View v = LayoutInflater.from(mContext).inflate(R.layout.mst_star_contact_listitem,null);
		ImageView imageView=(ImageView)v.findViewById(R.id.star_contacts_header);
		TextView textView=(	TextView)v.findViewById(R.id.star_contacts_name);
		LinearLayout layout=(LinearLayout)v.findViewById(R.id.star_contacts_header_layout);

		final Object[] objects=list.get(position);
//		for(Object obj:objects){
//			Log.d(TAG,"obj:"+obj);
//		}

		String displayName=(String) objects[ContactQuery.CONTACT_DISPLAY_NAME];
		final String lookupKey=(String) objects[ContactQuery.CONTACT_LOOKUP_KEY];

		textView.setText(displayName);

		// Set the photo, if available			
		long photoId = 0;
		if(list.get(position)[ContactQuery.CONTACT_PHOTO_ID]!=null) {
			photoId=Long.valueOf((String) objects[ContactQuery.CONTACT_PHOTO_ID]);
		}
		if (photoId != 0) {
			Log.d(TAG,"liyang1");
			mPhotoManager.loadThumbnail(imageView, photoId, false,
					true, null);
		} else {
			String photoUriString=null;
			if(list.get(position)[ContactQuery.CONTACT_PHOTO_URI]!=null){
				photoUriString =(String) list.get(position)[ContactQuery.CONTACT_PHOTO_URI];
			}			
			final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
			DefaultImageRequest request=null;
			
			if (photoUri == null) {				
				request = new DefaultImageRequest(displayName, lookupKey,
						true);
			}
			Log.d(TAG,"imageView:"+imageView+" photoUri:"+photoUri+" displayName:"+displayName+" lookupKey:"+lookupKey+" request:"+request);
			mPhotoManager.loadDirectoryPhoto(imageView, photoUri, false,
					true, request);
		}
//		Log.d(TAG,"layout:"+getViewWidth(layout)+","+getViewHeight(layout)+" img:"+getViewWidth2(imageView)+","+getViewHeigh2(imageView));

		v.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				long contactId = Long.valueOf((String) objects[ContactQuery.CONTACT_ID]);
				Uri uri = Contacts.getLookupUri(contactId, lookupKey);	
				Log.d(TAG,"onclick,id:"+contactId+" uri:"+uri);
				mListener.onViewContactAction(uri);
			}
		});
		return v;
	}
}
