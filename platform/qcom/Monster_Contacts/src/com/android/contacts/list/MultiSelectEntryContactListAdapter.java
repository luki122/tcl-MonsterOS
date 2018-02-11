/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.contacts.common.list.ContactEntryListAdapter.ViewHolderForContacts;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.ProfileAndContactsLoader;
import com.android.contacts.R;
import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.list.ContactEntryListAdapter.ViewHolderForContacts;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import java.util.TreeSet;

/**
 * An extension of the default contact adapter that adds checkboxes and the ability
 * to select multiple contacts.
 */
public class MultiSelectEntryContactListAdapter extends DefaultContactListAdapter {

    private static final String TAG = "MultiSelectEntryContactListAdapter";
	private SelectedContactsListener mSelectedContactsListener;
    private TreeSet<Long> mSelectedContactIds = new TreeSet<Long>();
    private boolean mDisplayCheckBoxes;

    public interface SelectedContactsListener {
        void onSelectedContactsChanged();
        void onSelectedContactsChangedViaCheckBox();
    }

    public MultiSelectEntryContactListAdapter(Context context) {
        super(context);
    }

    @Override
	public void configureLoader(CursorLoader loader, long directoryId) {
    	super.configureLoader(loader,directoryId);
    	if (loader instanceof ProfileAndContactsLoader) {
    		((ProfileAndContactsLoader) loader).setLoadStars(!isSearchMode());
    	}
    }
    public void setSelectedContactsListener(SelectedContactsListener listener) {
        mSelectedContactsListener = listener;
    }

    /**
     * Returns set of selected contacts.
     */
    public TreeSet<Long> getSelectedContactIds() {
        return mSelectedContactIds;
    }

    /**
     * Update set of selected contacts. This changes which checkboxes are set.
     */
    public void setSelectedContactIds(TreeSet<Long> selectedContactIds) {
    	Log.d(TAG,"setSelectedContactIds");
        this.mSelectedContactIds = selectedContactIds;
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

    /**
     * Shows checkboxes beside contacts if {@param displayCheckBoxes} is {@code TRUE}.
     * Not guaranteed to work with all configurations of this adapter.
     */
    public void setDisplayCheckBoxes(boolean showCheckBoxes) {
    	Log.d(TAG,"setDisplayCheckBoxes:"+showCheckBoxes);
        if (!mDisplayCheckBoxes && showCheckBoxes) {
            setSelectedContactIds(new TreeSet<Long>());
        }
        mDisplayCheckBoxes = showCheckBoxes;
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }
    
    private boolean needChangeCheckboxState=false;
    public void setDisplayCheckBoxesV2(boolean showCheckBoxes,boolean mDisplayCheckBoxes) {
    	Log.d(TAG,"setDisplayCheckBoxesV2:"+showCheckBoxes);
//        if (!mDisplayCheckBoxes && showCheckBoxes) {
//            setSelectedContactIds(new TreeSet<Long>());
//        }
//        setSelectedContactIds(new TreeSet<Long>());
        this.mDisplayCheckBoxes = mDisplayCheckBoxes;
        needChangeCheckboxState=true;
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

    /**
     * Checkboxes are being displayed beside contacts.
     */
    public boolean isDisplayingCheckBoxes() {
        return mDisplayCheckBoxes;
    }

    /**
     * Toggle the checkbox beside the contact for {@param contactId}.
     */
    public void toggleSelectionOfContactId(long contactId) {
        if (mSelectedContactIds.contains(contactId)) {
            mSelectedContactIds.remove(contactId);
        } else {
            mSelectedContactIds.add(contactId);
        }
        notifyDataSetChanged();
        if (mSelectedContactsListener != null) {
            mSelectedContactsListener.onSelectedContactsChanged();
        }
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
//    	final ContactListItemView view=(ContactListItemView)itemView.findViewById(R.id.listview_item_id);
//    	final ViewGroup v1=(ViewGroup) ((ViewGroup)itemView).getChildAt(1);
//        final  ContactListItemView view=(ContactListItemView) v1.getChildAt(0);
        
    	
        final ViewHolderForContacts viewHolder = (ViewHolderForContacts) itemView.getTag();
		if(viewHolder==null) return;
		final  ContactListItemView view=viewHolder.view;        
        super.bindView(itemView, partition, cursor, position);             
//        final ContactListItemView view = (ContactListItemView)itemView;
        bindCheckBox(view, cursor, position,viewHolder.checkBox);
    }

    private void bindCheckBox(ContactListItemView view, Cursor cursor, int position,CheckBox checkBox) {
        // Disable clicking on the first entry when showing check boxes. We do this by
        // telling the view to handle clicking itself.
        view.setClickable(position == 0 && hasProfile() && mDisplayCheckBoxes);
        // Only show checkboxes if mDisplayCheckBoxes is enabled. Also, never show the
        // checkbox for the Me profile entry.
//        Log.d(TAG,"bindCheckBox,mDisplayCheckBoxes:"+mDisplayCheckBoxes);
        if (position == 0 && hasProfile() || !mDisplayCheckBoxes) {
            view.hideCheckBox();
            checkBox.setVisibility(View.GONE);
            checkBox.setChecked(false);
//            if(needChangeCheckboxState){
//            	Log.d(TAG,"setchecked false,position:"+position);
//            	needChangeCheckboxState=false;
//            	checkBox.setChecked(false);
//            }
            return;
        }
        /* M: If is sdn contact, do not show the checkbox in multiselectFragment @{*/
        if (cursor.getLong(ContactQuery.IS_SDN_CONTACT) == 1) {
            view.setClickable(false);
            view.hideCheckBox();
            checkBox.setVisibility(View.GONE);
            return;
        }
        /*@}*/

//        final CheckBox checkBox = view.getCheckBox();
        final long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
        checkBox.setChecked(mSelectedContactIds.contains(contactId));
        checkBox.setTag(contactId);
        checkBox.setOnClickListener(mCheckBoxClickListener);
        checkBox.setVisibility(View.VISIBLE);
//        Log.d(TAG,"checkbox visibility:"+checkBox.getVisibility()+" mSelectedContactIds.contains(contactId):"+mSelectedContactIds.contains(contactId));
    }

    private final OnClickListener mCheckBoxClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final CheckBox checkBox = (CheckBox) v;
            final Long contactId = (Long) checkBox.getTag();
            if (checkBox.isChecked()) {
                mSelectedContactIds.add(contactId);
            } else {
                mSelectedContactIds.remove(contactId);
            }
            if (mSelectedContactsListener != null) {
                mSelectedContactsListener.onSelectedContactsChangedViaCheckBox();
            }
        }
    };

    /// M: Add for SelectAll/DeSelectAll Feature. @{
    public long getContactId(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        return cursor.getLong(ContactQuery.CONTACT_ID);
    }
    /// @}
}
