/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.contacts.list;

import com.android.contacts.R;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.DefaultContactListAdapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Equivalent to DefaultContactListAdapter, except with an optional header entry that has the same
 * formatting as the other entries in the list.
 *
 * This header entry is hidden when in search mode. Should not be used with lists that contain a
 * "Me" contact.
 */
public class HeaderEntryContactListAdapter extends DefaultContactListAdapter {

	private static final String TAG = "HeaderEntryContactListAdapter";
	private boolean mShowCreateContact;

	public HeaderEntryContactListAdapter(Context context) {
		super(context);
		headerPaddingLeft=context.getResources().getDimensionPixelOffset(R.dimen.contact_listview_header_padding_left);

	}

	private int getHeaderEntryCount() {
		return isSearchMode() || !mShowCreateContact ? 0 : 1;
	}

	/**
	 * Whether the first entry should be "Create contact", when not in search mode.
	 */
	public void setShowCreateContact(boolean showCreateContact) {
		mShowCreateContact = showCreateContact;
		invalidate();
	}

	@Override
	public int getCount() {
		return super.getCount() + getHeaderEntryCount();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG,"getView:"+position);
		if (position == 0 && getHeaderEntryCount() > 0) {
			Log.d(TAG,"position=0");		
				              
			final LinearLayout mLayout;
			ContactListItemView itemView;
			if (convertView != null) {
				mLayout = (LinearLayout) convertView;
				itemView=(ContactListItemView)mLayout.getChildAt(0);
			} else {
				mLayout =new LinearLayout(getContext()); 
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(      
		                   LinearLayout.LayoutParams.MATCH_PARENT,      
		                   LinearLayout.LayoutParams.WRAP_CONTENT      
		           );    
				p.leftMargin=headerPaddingLeft;
				
				itemView = new ContactListItemView(getContext(), null);	
				mLayout.addView(itemView, p);  
			}
			itemView.setShowLeftPhoto(false);
			itemView.setDisplayName(getContext().getResources().getString(R.string.pickerNewContactHeader));
			return mLayout;
		}
		return super.getView(position - getHeaderEntryCount(), convertView, parent);
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG,"getItem,position:"+position);
		return super.getItem(position - getHeaderEntryCount());
	}

	@Override
	public boolean isEnabled(int position) {
		return position < getHeaderEntryCount() || super
				.isEnabled(position - getHeaderEntryCount());
	}

	@Override
	public int getPartitionForPosition(int position) {
		return super.getPartitionForPosition(position - getHeaderEntryCount());
	}

	private int headerPaddingLeft;
	protected void bindSectionHeaderAndDividerV2(TextView textView, int position,
			Cursor cursor) {
		if (isSectionHeaderDisplayEnabled()) {			
			Placement placement = getItemPlacementInSection(position);
			if (!TextUtils.isEmpty(placement.sectionHeader)) {				
				textView.setText(placement.sectionHeader);				
				textView.setPadding(headerPaddingLeft, 0, 0, 0);
				textView.setVisibility(View.VISIBLE);
			}
		}else{
			textView.setVisibility(View.GONE);
		}
	}
	private void bindCheckBoxForMst(CheckBox checkBox, Cursor cursor, int position) {
		Log.d(TAG,"[bindCheckBox]position = " + position);
		//checkBox.setChecked(mSelectedContactIds.contains(contactId));
		// checkBox.setChecked(mListView.isItemChecked(position));
		final long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
		checkBox.setChecked(mSelectedContactIds.contains(contactId));
		checkBox.setTag(contactId);
		//        checkBox.setOnClickListener(mCheckBoxClickListener);
	}
	
	
	@Override
	protected void bindView(View itemView, int partition, Cursor cursor, int position) {
		Log.d(TAG,"bindview,position:"+position);
//		super.bindView(itemView, partition, cursor, position);

		final ViewHolderForContacts viewHolder = (ViewHolderForContacts) itemView.getTag();
		if(viewHolder==null) return;
		
		final ContactListItemView view=viewHolder.view;

		view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString() : null);
		if (isSelectionVisible()) {
			view.setActivated(isSelectedContact(partition, cursor));
		}

		bindSectionHeaderAndDividerV2(viewHolder.header, position, cursor);
		// M: disable header view.
		view.setIsSectionHeaderEnabled(false);

		Log.d(TAG,"bindview,isQuickContactEnabled():"+isQuickContactEnabled()+" getDisplayPhotos:"+getDisplayPhotos());
		if (isQuickContactEnabled()) {
			bindQuickContact(view, partition, cursor, ContactQuery.CONTACT_PHOTO_ID,
					ContactQuery.CONTACT_PHOTO_URI, ContactQuery.CONTACT_ID,
					ContactQuery.CONTACT_LOOKUP_KEY, ContactQuery.CONTACT_DISPLAY_NAME);
		} else {
			if (getDisplayPhotos()) {
				bindPhoto(view, partition, cursor);
			}
		}

		bindNameAndViewId(view, cursor);
//		bindPresenceAndStatusMessage(view, cursor);

//		if (isSearchMode()) {
//			bindSearchSnippet(view, cursor);
//		} else {
//			view.setSnippet(null);
//		}

//		bindCheckBox(view, cursor, position);
		view.setSnippet(null);
//		bindCheckBoxForMst(viewHolder.checkBox, cursor, position);
		viewHolder.sliderLayout.setLockDrag(true);
		if(viewHolder.sliderLayout.isOpened()){
			viewHolder.sliderLayout.close(false);
		}
		viewHolder.checkBox.setVisibility(View.GONE);
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 && getHeaderEntryCount() > 0) {
			return getViewTypeCount() - 1;
		}
		return super.getItemViewType(position - getHeaderEntryCount());
	}

	@Override
	public int getViewTypeCount() {
		// One additional view type, for the header entry.
		return super.getViewTypeCount() + 1;
	}

	//    @Override
	//    protected boolean getExtraStartingSection() {
	//        return getHeaderEntryCount() > 0;
	//    }
}
