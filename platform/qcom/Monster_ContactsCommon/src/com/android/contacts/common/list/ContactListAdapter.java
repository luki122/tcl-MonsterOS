/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.contacts.common.list;

import android.graphics.Paint;
import java.util.TreeSet;

import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.widget.SliderView;
import android.R.integer;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Directory;
import mst.provider.ContactsContract.Groups;
import mst.provider.ContactsContract.SearchSnippets;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.R;
import com.android.contacts.common.list.ContactEntryListAdapter.ViewHolderForContacts;
import com.android.contacts.common.mst.DialerSearchHelperForMst.DialerSearchResultColumnsForMst;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.preference.ContactsPreferences;
import com.android.contacts.common.util.ViewUtil;

/**
 * A cursor adapter for the {@link Contacts.Contacts#CONTENT_TYPE} content type.
 * Also includes support for including the {@link ContactsContract.Profile} record in the
 * list.
 */
public abstract class ContactListAdapter extends ContactEntryListAdapter {

	/// M: New Feature Gemini
	public static class ContactQuery {
		public static final String[] CONTACT_PROJECTION_PRIMARY = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_PRIMARY,          // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			/** M: New Feature Gemini @{ */
			Contacts.INDICATE_PHONE_SIM,            // 8
			Contacts.INDEX_IN_SIM,                  // 9
			Contacts.IS_SDN_CONTACT,                // 10
			Contacts.IS_SDN_CONTACT,                // 11,
			"phonebook_bucket",//12
			"phonebook_label",//13
			"quanpinyin",//14
			"jianpinyin",//15
			 "name_raw_contact_id"//16
		};

		public static final String[] CONTACT_PROJECTION_PRIMARY_FOR_CHOICE = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_PRIMARY,          // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			/** M: New Feature Gemini @{ */
			Contacts.INDICATE_PHONE_SIM,            // 8
			Contacts.INDEX_IN_SIM,                  // 9
			Contacts.IS_SDN_CONTACT,                // 10
			Contacts.IS_SDN_CONTACT,                // 11,
			/** @} */
		};

		private static final String[] CONTACT_PROJECTION_ALTERNATIVE = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			/** M: New Feature Gemini @{ */
			Contacts.INDICATE_PHONE_SIM,            // 8
			Contacts.INDEX_IN_SIM,                  // 9
			Contacts.IS_SDN_CONTACT,                // 10
			/// M: for SNS plugin
			Contacts.IS_SDN_CONTACT,                // 11
			"quan_pinyin",
			"jian_pinyin"
			/** @} */
		};

		private static final String[] FILTER_PROJECTION_PRIMARY = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_PRIMARY,          // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			/** M: New Feature Gemini @{ */
			Contacts.INDICATE_PHONE_SIM,            // 8
			Contacts.INDEX_IN_SIM,                  // 9
			SearchSnippets.SNIPPET,                 // 10
			Contacts.IS_SDN_CONTACT,                // 11
			"vds_quan_pinyin",//12 add by liyang
			/** @} */
		};

		private static final String[] FILTER_PROJECTION_ALTERNATIVE = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			/** M: New Feature Gemini @{ */
			Contacts.INDICATE_PHONE_SIM,            // 8
			Contacts.INDEX_IN_SIM,                  // 9
			SearchSnippets.SNIPPET,                 // 10
			Contacts.IS_SDN_CONTACT,                // 11
			//			"vds_quan_pinyin",//12 add by liyang
			/** @} */
		};

		public static final int CONTACT_ID               = 0;
		public static final int CONTACT_DISPLAY_NAME     = 1;
		public static final int CONTACT_PRESENCE_STATUS  = 2;
		public static final int CONTACT_CONTACT_STATUS   = 3;
		public static final int CONTACT_PHOTO_ID         = 4;
		public static final int CONTACT_PHOTO_URI        = 5;
		public static final int CONTACT_LOOKUP_KEY       = 6;
		public static final int CONTACT_IS_USER_PROFILE  = 7;
		/** M: New Feature Gemini @{ */
		protected static final int CONTACT_INDICATE_PHONE_SIM = 8;
		protected static final int CONTACT_INDEX_IN_SIM = 9;
		public static final int CONTACT_SNIPPET          = 10;
		public static final int IS_SDN_CONTACT           = 11;
		public static final int PHONEBOOK_BUCKET           = 12;
		public static final int PHONEBOOK_LABEL           = 13;
		public static final int QUAN_PINYIN           = 14;
		public static final int JIAN_PINYIN           = 15;
		/** @} */
	}

	private static final String TAG = "ContactListAdapter";

	private CharSequence mUnknownNameText;

	private long mSelectedContactDirectoryId;
	private String mSelectedContactLookupKey;
	private long mSelectedContactId;
	private ContactListItemView.PhotoPosition mPhotoPosition;
	protected Context mContext;

	public ContactListAdapter(Context context) {
		super(context);

		mContext=context;
		mUnknownNameText = context.getText(R.string.missing_name);
	}

	public void setPhotoPosition(ContactListItemView.PhotoPosition photoPosition) {
		mPhotoPosition = photoPosition;
	}

	public ContactListItemView.PhotoPosition getPhotoPosition() {
		return mPhotoPosition;
	}

	public CharSequence getUnknownNameText() {
		return mUnknownNameText;
	}

	public long getSelectedContactDirectoryId() {
		return mSelectedContactDirectoryId;
	}

	public String getSelectedContactLookupKey() {
		return mSelectedContactLookupKey;
	}

	public long getSelectedContactId() {
		return mSelectedContactId;
	}

	public void setSelectedContact(long selectedDirectoryId, String lookupKey, long contactId) {
		mSelectedContactDirectoryId = selectedDirectoryId;
		mSelectedContactLookupKey = lookupKey;
		mSelectedContactId = contactId;
	}

	protected static Uri buildSectionIndexerUri(Uri uri) {
		return uri.buildUpon()
				.appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
	}

	@Override
	public String getContactDisplayName(int position) {
		return ((Cursor) getItem(position)).getString(ContactQuery.CONTACT_DISPLAY_NAME);
	}

	/**
	 * Builds the {@link Contacts#CONTENT_LOOKUP_URI} for the given
	 * {@link ListView} position.
	 */
	public Uri getContactUri(int position) {
		Log.d(TAG,"getContactUri:"+position);
		int partitionIndex = getPartitionForPosition(position);
		Cursor item = (Cursor)getItem(position);
		return item != null ? getContactUri(partitionIndex, item) : null;
	}

	public long getGroupId(int position) {
		Cursor item = (Cursor)getItem(position);
		return item != null ? -item.getLong(DialerSearchResultColumnsForMst.CONTACT_ID_INDEX):0;
	}

	public Uri getContactUri(int partitionIndex, Cursor cursor) {
		Log.d(TAG,"getContactUri,partitionIndex:"+partitionIndex+" issearchmode:"+isSearchMode());
		if(isSearchMode()&&!isForContactsChoice()){//add by liyang
			long contactId = cursor.getLong(DialerSearchResultColumnsForMst.CONTACT_ID_INDEX);
			String lookupKey = cursor.getString(DialerSearchResultColumnsForMst.LOOKUP_KEY_INDEX);
			Log.d(TAG,"contactId:"+contactId+" lookupKey:"+lookupKey);
			Uri uri = Contacts.getLookupUri(contactId, lookupKey);
			long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
			if (uri != null && directoryId != Directory.DEFAULT) {
				uri = uri.buildUpon().appendQueryParameter(
						ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
			}
			Log.d(TAG,"uri:"+uri);
			return uri;
		}


		long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
		String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
		Uri uri = Contacts.getLookupUri(contactId, lookupKey);
		long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
		if (uri != null && directoryId != Directory.DEFAULT) {
			uri = uri.buildUpon().appendQueryParameter(
					ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
		}
		return uri;
	}

	/**
	 * Returns true if the specified contact is selected in the list. For a
	 * contact to be shown as selected, we need both the directory and and the
	 * lookup key to be the same. We are paying no attention to the contactId,
	 * because it is volatile, especially in the case of directories.
	 */
	public boolean isSelectedContact(int partitionIndex, Cursor cursor) {
		long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
		if (getSelectedContactDirectoryId() != directoryId) {
			return false;
		}
		String lookupKey = getSelectedContactLookupKey();
		if (lookupKey != null && TextUtils.equals(lookupKey,
				cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY))) {
			return true;
		}

		return directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE
				&& getSelectedContactId() == cursor.getLong(ContactQuery.CONTACT_ID);
	}

	@Override
	protected View newView(
			Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
		if (true) {
			return mstNewView(context, partition, cursor, position, parent);
		}

		/// M: For Dialer customization
		Log.d(TAG,"newView");
		ContactListItemView view = (ContactListItemView) super.newView(context, partition, cursor,
				position, parent);
		view.setUnknownNameText(mUnknownNameText);
		view.setQuickContactEnabled(isQuickContactEnabled());
		view.setAdjustSelectionBoundsEnabled(isAdjustSelectionBoundsEnabled());
		view.setActivatedStateSupported(isSelectionVisible());
		if (mPhotoPosition != null) {
			view.setPhotoPosition(mPhotoPosition);
		}
		return view;
	}


//	private int getViewType(Cursor cursor) {
//		final String number = cursor.getString(DialerSearchResultColumnsForMst.PHONE_NUMBER_INDEX);				
//		if (!TextUtils.isEmpty(number)&&!TextUtils.isEmpty(getQueryString())&&!TextUtils.equals(SEARCH_BEGIN_STRING, getQueryString()))
//			return DoubleLineType;
//		else
//			return SingleLineType;
//	}
	@Override
	public int getItemViewType(int partitionIndex, int position) {
		// TODO Auto-generated method stub
		return super.getItemViewType(partitionIndex, position);
	}


//	@Override  
//	public int getViewTypeCount() {
//		if(isSearchMode())
//			return 2; 
//		else return super.getViewTypeCount();
//	}  

	protected View getView(int partition, Cursor cursor, int position, View convertView,
			ViewGroup parent) {
//		if(isSearchMode()){
//			Log.d(TAG,"position:"+position+" convertView:"+convertView);
//			if(convertView==null||(/*TextUtils.equals(getQueryString(), SEARCH_BEGIN_STRING)&&*/position>-1&&position<10)){
//				int itemType=getViewType(cursor);
//				convertView=mstNewView(mContext, partition, cursor, position, parent,itemType);
//			}
//			bindView(convertView, partition, cursor, position);
//			return convertView;
//		}
		
		View view2=super.getView(partition,cursor,position,convertView,parent);
//		Log.d(TAG,"liyang,view2:"+view2+" convertView:"+convertView
//				+" position:"+position+" name:"+cursor.getString(DialerSearchResultColumnsForMst.NAME_INDEX));
		return view2;
	}

	protected View mstNewView(Context context, int partition, Cursor cursor, final int position,
			ViewGroup parent) {
//		Log.d(TAG,"mstNewView1,partition:"+partition+" position:"+position+" cursor:"+cursor);
		ViewHolderForContacts viewHolder = new ViewHolderForContacts();
		ContactListItemView view = (ContactListItemView) super.newView(context, partition, cursor,position, parent);
		view.setId(R.id.listview_item_id);
		view.setUnknownNameText(mUnknownNameText);
		view.setQuickContactEnabled(isQuickContactEnabled());
		view.setAdjustSelectionBoundsEnabled(isAdjustSelectionBoundsEnabled());
		view.setActivatedStateSupported(isSelectionVisible());
		if (mPhotoPosition != null) {
			view.setPhotoPosition(mPhotoPosition);
		}

		ViewGroup outer = (ViewGroup)LayoutInflater.from(mContext).inflate(R.layout.mst_contacts_listview_item,null);
//		View devider=outer.findViewById(R.id.listview_item_header_devider);
		SliderView sliderView=(SliderView)outer.findViewById(com.mst.R.id.slider_view);
		sliderView.addTextButton(1,context.getString(R.string.mst_delete));
		TextView header=(TextView)outer.findViewById(R.id.listview_item_header);
		header.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
		header.getPaint().setStrokeWidth(0.5f);//设置加粗0.5倍

		ViewGroup front_view=(ViewGroup)outer.findViewById(R.id.front_view);
		CheckBox checkBox=(CheckBox)front_view.findViewById(android.R.id.button1);
		front_view.addView(view, 1, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		viewHolder.view=view;
		viewHolder.header=header;
//		viewHolder.header.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Log.d(TAG,"click header");
//			}
//		});
		viewHolder.checkBox=checkBox;
		viewHolder.checkBox.setChecked(false);
		viewHolder.name=view.getNameTextView();
		viewHolder.sliderLayout=sliderView;
//		viewHolder.devider=devider;
//		viewHolder.snippetView=view.getSnippetView();

		outer.setTag(viewHolder);
		return outer;
	}

	protected void bindSectionHeaderAndDivider(ContactListItemView view, int position,
			Cursor cursor) {
		view.setIsSectionHeaderEnabled(isSectionHeaderDisplayEnabled());
		if (isSectionHeaderDisplayEnabled()) {
			Placement placement = getItemPlacementInSection(position);
			Log.d(TAG,"placement.sectionHeader:"+placement.sectionHeader);
			view.setSectionHeader(placement.sectionHeader);
		} else {
			view.setSectionHeader(null);
		}
	}

	//modify by liyang       
	protected void bindSectionHeaderAndDividerV2(TextView textView, int position,
			Cursor cursor) {

	/*	if(isSearchMode()){
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.contact_header_title_text_size));
			int common=getCommonlyCount();
			int group=getGroupCount();
			int other=getCount()-common-group;
			int allContacts=getCount()-group;
			boolean isQueryCommon=getIsQueryCommon()==1?true:false;
			Log.d(TAG,"common:"+common+" other:"+other+" group:"+group+" allContacts:"+allContacts+" all:"+getCount());
			if(common!=0&&allContacts!=common&&!isQueryCommon){//显示为常用联系人、其它联系人
				if(position==0){
					textView.setText(mContext.getResources().getString(R.string.mst_commonly_contacts_header));				
					textView.setVisibility(View.VISIBLE);
					return;
				}else if(position==common){
					textView.setText(mContext.getResources().getString(R.string.mst_other_contacts_header));				
					textView.setVisibility(View.VISIBLE);
					devider.setVisibility(View.VISIBLE);
					return;
				}else if(position==allContacts){
					textView.setText(mContext.getResources().getString(R.string.mst_group_header));				
					textView.setVisibility(View.VISIBLE);
					devider.setVisibility(View.VISIBLE);
					return;
				}
			}else if(allContacts==0&&group!=0){//只显示为群组　
				if(position==0){
					textView.setText(mContext.getResources().getString(R.string.mst_group_header));				
					textView.setVisibility(View.VISIBLE);
					return;
				}
			}else{//显示为全部联系人
				if(position==0&&!isQueryCommon){
					textView.setText(mContext.getResources().getString(R.string.mst_all_contacts_header));				
					textView.setVisibility(View.VISIBLE);
					return;
				}else if(position==0&&isQueryCommon){
					textView.setText(mContext.getResources().getString(R.string.mst_commonly_contacts_header));				
					textView.setVisibility(View.VISIBLE);
					return;
				}else if(position==allContacts){
					textView.setText(mContext.getResources().getString(R.string.mst_group_header));				
					textView.setVisibility(View.VISIBLE);
					devider.setVisibility(View.VISIBLE);
					return;
				}
			}
		}*/

//		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelOffset(R.dimen.contact_listview_header_text));
		//		TextView textView=(TextView)view.findViewById(R.id.listview_item_header);
//		devider.setVisibility(View.GONE);
		if (isSectionHeaderDisplayEnabled()) {
			Placement placement = getItemPlacementInSection(position);
			Log.d(TAG,"position:"+position+" name:"+cursor.getString(ContactQuery.CONTACT_DISPLAY_NAME)+" sectionHeader:"+placement.sectionHeader);
			if (!TextUtils.isEmpty(placement.sectionHeader)) {
				textView.setText(placement.sectionHeader);		
				textView.setVisibility(View.VISIBLE);
			}
		}else{
			textView.setVisibility(View.GONE);
		}
	}

	protected void bindPhoto(final ContactListItemView view, int partitionIndex, Cursor cursor) {
		if (/*!isPhotoSupported(partitionIndex)*/true) {
			view.removePhotoView();
			return;
		}

		// Set the photo, if available
		long photoId = 0;
		if (!cursor.isNull(ContactQuery.CONTACT_PHOTO_ID)) {
			photoId = cursor.getLong(ContactQuery.CONTACT_PHOTO_ID);
		}

		if (photoId != 0) {
			getPhotoLoader().loadThumbnail(view.getPhotoView(), photoId, false,
					getCircularPhotos(), null);
		} else {
			final String photoUriString = cursor.getString(ContactQuery.CONTACT_PHOTO_URI);
			final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
			DefaultImageRequest request = null;
			if (photoUri == null) {
				request = getDefaultImageRequestFromCursor(cursor,
						ContactQuery.CONTACT_DISPLAY_NAME,
						ContactQuery.CONTACT_LOOKUP_KEY);
			}
			getPhotoLoader().loadDirectoryPhoto(view.getPhotoView(), photoUri, false,
					getCircularPhotos(), request);
		}
	}

	protected void bindNameAndViewId(final ContactListItemView view, Cursor cursor) {
		view.showDisplayName(
				cursor, ContactQuery.CONTACT_DISPLAY_NAME, getContactNameDisplayOrder());
		// Note: we don't show phonetic any more (See issue 5265330)

		bindViewId(view, cursor, ContactQuery.CONTACT_ID);
	}

	protected void bindPresenceAndStatusMessage(final ContactListItemView view, Cursor cursor) {
		view.showPresenceAndStatusMessage(cursor, ContactQuery.CONTACT_PRESENCE_STATUS,
				ContactQuery.CONTACT_CONTACT_STATUS);
	}

	protected void bindSearchSnippet(final ContactListItemView view, Cursor cursor) {
		Log.d(TAG,"bindSearchSnippet");
		view.showSnippet(cursor, DialerSearchResultColumnsForMst.PHONE_NUMBER_INDEX);
	}

	public int getSelectedContactPosition() {
		if (mSelectedContactLookupKey == null && mSelectedContactId == 0) {
			return -1;
		}

		Cursor cursor = null;
		int partitionIndex = -1;
		int partitionCount = getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			DirectoryPartition partition = (DirectoryPartition) getPartition(i);
			if (partition.getDirectoryId() == mSelectedContactDirectoryId) {
				partitionIndex = i;
				break;
			}
		}
		if (partitionIndex == -1) {
			return -1;
		}

		cursor = getCursor(partitionIndex);
		if (cursor == null) {
			return -1;
		}

		cursor.moveToPosition(-1);      // Reset cursor
		int offset = -1;
		while (cursor.moveToNext()) {
			if (mSelectedContactLookupKey != null) {
				String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
				if (mSelectedContactLookupKey.equals(lookupKey)) {
					offset = cursor.getPosition();
					break;
				}
			}
			if (mSelectedContactId != 0 && (mSelectedContactDirectoryId == Directory.DEFAULT
					|| mSelectedContactDirectoryId == Directory.LOCAL_INVISIBLE)) {
				long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
				if (contactId == mSelectedContactId) {
					offset = cursor.getPosition();
					break;
				}
			}
		}
		if (offset == -1) {
			return -1;
		}

		int position = getPositionForPartition(partitionIndex) + offset;
		if (hasHeader(partitionIndex)) {
			position++;
		}
		return position;
	}

	public boolean hasValidSelection() {
		return getSelectedContactPosition() != -1;
	}

	public Uri getFirstContactUri() {
		int partitionCount = getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			DirectoryPartition partition = (DirectoryPartition) getPartition(i);
			if (partition.isLoading()) {
				continue;
			}

			Cursor cursor = getCursor(i);
			if (cursor == null) {
				continue;
			}

			if (!cursor.moveToFirst()) {
				continue;
			}

			return getContactUri(i, cursor);
		}

		return null;
	}

	@Override
	public void changeCursor(int partitionIndex, Cursor cursor) {
		super.changeCursor(partitionIndex, cursor);

		// Check if a profile exists
		if (cursor != null && cursor.moveToFirst()) {
			setProfileExists(cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1);
			/** M: New Feature Gemini @{ */
		} else {
			setProfileExists(false);
			/** @} */
		}
	}

	@Override
	public int getCount() {
		return super.getCount()-getStarredCount();
	}
	/**
	 * @return Projection useful for children.
	 */
	protected final String[] getProjection(boolean forSearch) {
		final int sortOrder = getContactNameDisplayOrder();
		Log.d(TAG,"getProjection,sortOrder:"+sortOrder);
		if (forSearch) {
			if (sortOrder == ContactsPreferences.DISPLAY_ORDER_PRIMARY) {
				return ContactQuery.FILTER_PROJECTION_PRIMARY;
			} else {
				return ContactQuery.FILTER_PROJECTION_ALTERNATIVE;
			}
		} else {
			if (sortOrder == ContactsPreferences.DISPLAY_ORDER_PRIMARY) {
				return ContactQuery.CONTACT_PROJECTION_PRIMARY;
			} else {
				return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE;
			}
		}
	}
}
