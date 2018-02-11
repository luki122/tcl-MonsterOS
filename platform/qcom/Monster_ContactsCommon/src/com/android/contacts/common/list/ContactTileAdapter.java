/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
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
package com.android.contacts.common.list;

import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.widget.SliderView;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import mst.app.dialog.AlertDialog;
//import android.app.AlertDialog;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPresenceIconUtil;
import com.android.contacts.common.ContactStatusUtil;
import com.android.contacts.common.ContactTileLoaderFactory;
import com.android.contacts.common.R;

import android.R.integer;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import mst.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
//import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPresenceIconUtil;
import com.android.contacts.common.ContactStatusUtil;
import com.android.contacts.common.ContactTileLoaderFactory;
import com.android.contacts.common.MoreContactUtils;
import com.android.contacts.common.R;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.util.ViewUtil;

import com.mediatek.contacts.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Arranges contacts favorites according to provided {@link DisplayType}.
 * Also allows for a configurable number of columns and {@link DisplayType}
 */
public class ContactTileAdapter extends BaseAdapter{
	private static final String TAG = ContactTileAdapter.class.getSimpleName();

	private DisplayType mDisplayType;
	private ContactTileView.Listener mListener;
	private Context mContext;
	private Resources mResources;
	protected Cursor mContactCursor = null;
	protected HashMap<String,String> companyHashMap=null;
	private ContactPhotoManager mPhotoManager;
	protected int mNumFrequents;

	private final String unKnowTitleString,unKnowCompanyString,deleteString,closeString;

	public void setCompanyHashMap(HashMap<String, String> companyHashMap) {
		this.companyHashMap = companyHashMap;
	}

	public FragmentCallbacks mCallbacks;
	public void setmCallbacks(FragmentCallbacks mCallbacks) {
		this.mCallbacks = mCallbacks;
	}

	/**
	 * Index of the first NON starred contact in the {@link Cursor}
	 * Only valid when {@link DisplayType#STREQUENT} is true
	 */
	private int mDividerPosition;
	protected int mColumnCount;
	private int mStarredIndex;

	protected int mIdIndex;
	protected int mLookupIndex;
	protected int mPhotoUriIndex;
	protected int mNameIndex;
	protected int mPresenceIndex;
	protected int mStatusIndex;

	private boolean mIsQuickContactEnabled = false;
	private final int mPaddingInPixels;
	private final int mWhitespaceStartEnd;
	private boolean is_editor_mode = false;
	private boolean[] checkeds;
	private boolean is_all_checked = false;
	private boolean is_listitem_changing = false;
	private int count;

	private SliderView.OnSliderButtonLickListener onSliderButtonLickListener;
	public void setOnSliderButtonLickListener(
			SliderView.OnSliderButtonLickListener onSliderButtonLickListener) {
		this.onSliderButtonLickListener = onSliderButtonLickListener;
	}
	public int getmCount() {
		return count;
	}

	public void setEditMode(boolean is_edit) {
		is_editor_mode = is_edit;
	}

	public boolean getEditMode() {
		return is_editor_mode;
	}

	public void createCheckedArray(int count) {
		this.count=count;
		if (checkeds == null || count != checkeds.length)
			checkeds = new boolean[count];
		for (int i = 0; i < count; i++)
			checkeds[i] = false;
	}

	public boolean isIs_listitem_changing() {
		return is_listitem_changing;
	}

	public void setIs_listitem_changing(boolean is_listitem_changing) {
		this.is_listitem_changing = is_listitem_changing;
		Log.d(TAG,"setIs_listitem_changing:"+is_listitem_changing);
	}

	public void setCheckedArrayValue(int position, boolean value) {
		checkeds[position] = value;
	}

	public void setAllSelect(boolean isAllselect){
		for(int i=0;i<checkeds.length;i++){
			checkeds[i]=isAllselect;
		}
	}

	public boolean getCheckedArrayValue(int position) {
		return checkeds[position];
	}

	public boolean isAllSelect() {
		for (int i = 0; i < checkeds.length; i++) {
			if (!checkeds[i])
				return false;
		}
		return true;
	}

	public int getCheckedCount() {
		int mChecked = 0;
		for (int i = 0; i < checkeds.length; i++) {
			if (checkeds[i]) {
				mChecked++;
			}
		}
		return mChecked;
	}

	public void clearAllcheckes() {
		for (int i = 0; i < checkeds.length; i++)
			checkeds[i] = false;
	}
	/**
	 * Configures the adapter to filter and display contacts using different view types.
	 * TODO: Create Uris to support getting Starred_only and Frequent_only cursors.
	 */
	public enum DisplayType {
		/**
		 * Displays a mixed view type of starred and frequent contacts
		 */
		STREQUENT,

		/**
		 * Display only starred contacts
		 */
		STARRED_ONLY,

		/**
		 * Display only most frequently contacted
		 */
		FREQUENT_ONLY,

		/**
		 * Display all contacts from a group in the cursor
		 * Use {@link com.android.contacts.GroupMemberLoader}
		 * when passing {@link Cursor} into loadFromCusor method.
		 *
		 * Group member logic has been moved into GroupMemberTileAdapter.  This constant is still
		 * needed by calling classes.
		 */
		GROUP_MEMBERS
	}
	private final LayoutInflater inflater;
	protected long mGroupId;


	public long getGroupId() {
		return mGroupId;
	}

	public void setGroupId(long mGroupId) {
		this.mGroupId = mGroupId;
		Log.d(TAG,"setGroupId:"+mGroupId);
	}

	public ContactTileAdapter(Context context, ContactTileView.Listener listener, int numCols,
			DisplayType displayType) {
		mListener = listener;
		mContext = context;
		mResources = context.getResources();
		mColumnCount = (displayType == DisplayType.FREQUENT_ONLY ? 1 : numCols);
		mDisplayType = displayType;
		mNumFrequents = 0;

		// Converting padding in dips to padding in pixels
		mPaddingInPixels = mContext.getResources()
				.getDimensionPixelSize(R.dimen.contact_tile_divider_padding);
		mWhitespaceStartEnd = mContext.getResources()
				.getDimensionPixelSize(R.dimen.contact_tile_start_end_whitespace);

		bindColumnIndices();
		inflater = LayoutInflater.from(context);
		unKnowCompanyString=context.getString(R.string.mst_business_card_unknow_company);
		unKnowTitleString=context.getString(R.string.mst_business_card_unknow_title);
		deleteString=context.getString(R.string.mst_remove);
		closeString=context.getString(R.string.mst_close);
		
		photoHashMap=new HashMap<Integer, Bitmap>();
	}

	public void setPhotoLoader(ContactPhotoManager photoLoader) {
		mPhotoManager = photoLoader;
	}

	public void setColumnCount(int columnCount) {
		mColumnCount = columnCount;
	}

	public void setDisplayType(DisplayType displayType) {
		mDisplayType = displayType;
	}

	public void enableQuickContact(boolean enableQuickContact) {
		mIsQuickContactEnabled = enableQuickContact;
	}

	/**
	 * Sets the column indices for expected {@link Cursor}
	 * based on {@link DisplayType}.
	 */
	protected void bindColumnIndices() {
		mIdIndex = ContactTileLoaderFactory.CONTACT_ID;
		mLookupIndex = ContactTileLoaderFactory.LOOKUP_KEY;
		mPhotoUriIndex = ContactTileLoaderFactory.PHOTO_URI;
		mNameIndex = ContactTileLoaderFactory.DISPLAY_NAME;
		mStarredIndex = ContactTileLoaderFactory.STARRED;
		mPresenceIndex = ContactTileLoaderFactory.CONTACT_PRESENCE;
		mStatusIndex = ContactTileLoaderFactory.CONTACT_STATUS;
	}

	private static boolean cursorIsValid(Cursor cursor) {
		return cursor != null && !cursor.isClosed();
	}

	/**
	 * Gets the number of frequents from the passed in cursor.
	 *
	 * This methods is needed so the GroupMemberTileAdapter can override this.
	 *
	 * @param cursor The cursor to get number of frequents from.
	 */
	protected void saveNumFrequentsFromCursor(Cursor cursor) {

		// count the number of frequents
		switch (mDisplayType) {
		case STARRED_ONLY:
			mNumFrequents = 0;
			break;
		case STREQUENT:
			mNumFrequents = cursorIsValid(cursor) ?
					cursor.getCount() - mDividerPosition : 0;
					break;
		case FREQUENT_ONLY:
			mNumFrequents = cursorIsValid(cursor) ? cursor.getCount() : 0;
			break;
		default:
			throw new IllegalArgumentException("Unrecognized DisplayType " + mDisplayType);
		}
	}

	/**
	 * Creates {@link ContactTileView}s for each item in {@link Cursor}.
	 *
	 * Else use {@link ContactTileLoaderFactory}
	 */
	public void setContactCursor(Cursor cursor) {
		mContactCursor = cursor;
		mDividerPosition = getDividerPosition(cursor);

		saveNumFrequentsFromCursor(cursor);

		// cause a refresh of any views that rely on this data
		notifyDataSetChanged();
	}

	/**
	 * Iterates over the {@link Cursor}
	 * Returns position of the first NON Starred Contact
	 * Returns -1 if {@link DisplayType#STARRED_ONLY}
	 * Returns 0 if {@link DisplayType#FREQUENT_ONLY}
	 */
	protected int getDividerPosition(Cursor cursor) {
		/** M: get the status of cursor @{ */
		Log.d(TAG, "cursor: " + cursor);
		if (cursor == null || cursor.isClosed()) {
			throw new IllegalStateException("Unable to access cursor");
		}
		/** @} */
		switch (mDisplayType) {
		case STREQUENT:
			if (!cursorIsValid(cursor)) {
				return 0;
			}
			cursor.moveToPosition(-1);
			while (cursor.moveToNext()) {
				if (cursor.getInt(mStarredIndex) == 0) {
					return cursor.getPosition();
				}
			}

			// There are not NON Starred contacts in cursor
			// Set divider positon to end
			return cursor.getCount();
		case STARRED_ONLY:
			// There is no divider
			return -1;
		case FREQUENT_ONLY:
			// Divider is first
			return 0;
		default:
			throw new IllegalStateException("Unrecognized DisplayType " + mDisplayType);
		}
	}

	protected ContactEntry createContactEntryFromCursor(Cursor cursor, int position) {
		// If the loader was canceled we will be given a null cursor.
		// In that case, show an empty list of contacts.
		if (!cursorIsValid(cursor) || cursor.getCount() <= position) {
			return null;
		}

		Log.d(TAG,"createContactEntryFromCursor,position:"+position);

		//      for(int i=0;i<cursor.getColumnCount();i++) Log.d(TAG,"createContactEntryFromCursor,i:"+i+"-"+cursor.getColumnName(i));

		cursor.moveToPosition(position);
		long id = cursor.getLong(mIdIndex);
		String photoUri = cursor.getString(mPhotoUriIndex);
		String lookupKey = cursor.getString(mLookupIndex);

		ContactEntry contact = new ContactEntry();
		String name = cursor.getString(mNameIndex);
		contact.name = (name != null) ? name : mResources.getString(R.string.missing_name);
		contact.status = cursor.getString(mStatusIndex);
		contact.photoUri = (photoUri != null ? Uri.parse(photoUri) : null);
		contact.lookupKey = lookupKey;
		contact.id=id;
		contact.rawContactId=cursor.getLong(9);
		contact.lookupUri = ContentUris.withAppendedId(
				Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey), id);
		contact.isFavorite = cursor.getInt(mStarredIndex) > 0;

		// Set presence icon and status message
		Drawable icon = null;
		int presence = 0;
		if (!cursor.isNull(mPresenceIndex)) {
			presence = cursor.getInt(mPresenceIndex);
			icon = ContactPresenceIconUtil.getPresenceIcon(mContext, presence);
		}
		contact.presenceIcon = icon;

		String statusMessage = null;
		if (mStatusIndex != 0 && !cursor.isNull(mStatusIndex)) {
			statusMessage = cursor.getString(mStatusIndex);
		}
		// If there is no status message from the contact, but there was a presence value,
		// then use the default status message string
		if (statusMessage == null && presence != 0) {
			statusMessage = ContactStatusUtil.getStatusString(mContext, presence);
		}
		contact.status = statusMessage;

		return contact;
	}

	/**
	 * Returns the number of frequents that will be displayed in the list.
	 */
	public int getNumFrequents() {
		return mNumFrequents;
	}

	@Override
	public int getCount() {
		Log.d(TAG,"getCount:"+mContactCursor.getCount());
		return mContactCursor==null?0:mContactCursor.getCount();
		/*if (!cursorIsValid(mContactCursor)) {
            return 0;
        }

        switch (mDisplayType) {
            case STARRED_ONLY:
                return getRowCount(mContactCursor.getCount());
            case STREQUENT:
                // Takes numbers of rows the Starred Contacts Occupy
                int starredRowCount = getRowCount(mDividerPosition);

                // Compute the frequent row count which is 1 plus the number of frequents
                // (to account for the divider) or 0 if there are no frequents.
                int frequentRowCount = mNumFrequents == 0 ? 0 : mNumFrequents + 1;

                // Return the number of starred plus frequent rows
                return starredRowCount + frequentRowCount;
            case FREQUENT_ONLY:
                // Number of frequent contacts
                return mContactCursor.getCount();
            default:
                throw new IllegalArgumentException("Unrecognized DisplayType " + mDisplayType);
        }*/
	}

	/**
	 * Returns the number of rows required to show the provided number of entries
	 * with the current number of columns.
	 */
	protected int getRowCount(int entryCount) {
		return entryCount == 0 ? 0 : ((entryCount - 1) / mColumnCount) + 1;
	}

	public int getColumnCount() {
		return mColumnCount;
	}

	//    /**
	//     * Returns an ArrayList of the {@link ContactEntry}s that are to appear
	//     * on the row for the given position.
	//     */
	//    @Override
	//    public ArrayList<ContactEntry> getItem(int position) {
	//        ArrayList<ContactEntry> resultList = new ArrayList<ContactEntry>(mColumnCount);
	//        int contactIndex = position * mColumnCount;
	//
	//        switch (mDisplayType) {
	//            case FREQUENT_ONLY:
	//                resultList.add(createContactEntryFromCursor(mContactCursor, position));
	//                break;
	//            case STARRED_ONLY:
	//                for (int columnCounter = 0; columnCounter < mColumnCount; columnCounter++) {
	//                    resultList.add(createContactEntryFromCursor(mContactCursor, contactIndex));
	//                    contactIndex++;
	//                }
	//                break;
	//            case STREQUENT:
	//                if (position < getRowCount(mDividerPosition)) {
	//                    for (int columnCounter = 0; columnCounter < mColumnCount &&
	//                            contactIndex != mDividerPosition; columnCounter++) {
	//                        resultList.add(createContactEntryFromCursor(mContactCursor, contactIndex));
	//                        contactIndex++;
	//                    }
	//                } else {
	//                    /*
	//                     * Current position minus how many rows are before the divider and
	//                     * Minus 1 for the divider itself provides the relative index of the frequent
	//                     * contact being displayed. Then add the dividerPostion to give the offset
	//                     * into the contacts cursor to get the absoulte index.
	//                     */
	//                    contactIndex = position - getRowCount(mDividerPosition) - 1 + mDividerPosition;
	//                    resultList.add(createContactEntryFromCursor(mContactCursor, contactIndex));
	//                }
	//                break;
	//            default:
	//                throw new IllegalStateException("Unrecognized DisplayType " + mDisplayType);
	//        }
	//        return resultList;
	//    }
	//
	@Override
	public long getItemId(int position) {
		// As we show several selectable items for each ListView row,
		// we can not determine a stable id. But as we don't rely on ListView's selection,
		// this should not be a problem.
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return (mDisplayType != DisplayType.STREQUENT);
	}

	@Override
	public boolean isEnabled(int position) {
		return /*position != getRowCount(mDividerPosition);*/true;
	}

	public class ViewHolder { 
		public TextView title;
		public CheckBox checkBox;
		public ImageView icon;
		public TextView secondLineTitle;
		public TextView thirdLineTitle;
		public SliderView slider; 
	}

	public Uri getContactUri(int position){
		return getItem(position).lookupUri;
	}

	private SliderView currentSliderView;

	public SliderView getCurrentSliderView() {
		return currentSliderView;
	}
	public void setCurrentSliderView(SliderView currentSliderView) {
		this.currentSliderView = currentSliderView;
	}

	private int scrollStauts=0;

	public int getScrollStauts() {
		return scrollStauts;
	}
	public void setScrollStauts(int scrollStauts) {
		this.scrollStauts = scrollStauts;
	}
	private HashMap<Integer, Bitmap> photoHashMap;
	
	public void setPhotoHashMap(HashMap<Integer, Bitmap> photoHashMap) {
		this.photoHashMap = photoHashMap;
	}
	
	public HashMap<Integer, Bitmap> getPhotoHashMap() {
		return photoHashMap;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {    	
		Log.d(TAG,"getView,position:"+position+" mGroupId:"+mGroupId);
		ContactEntry contactEntry=getItem(position);
		long id = contactEntry.id;
		String lookupKey = contactEntry.lookupKey;
		String name = contactEntry.name;
		Uri lookupUri = contactEntry.lookupUri;

		View view=null;
		ViewHolder viewHolder=null;
		if (convertView != null) {
			view=convertView;
			viewHolder=(ViewHolder) convertView.getTag();

		}else{
			if(mGroupId>0){
				//				view=inflater.inflate(com.mst.R.layout.list_item_1_line_multiple_choice,null);
				view=inflater.inflate(R.layout.mst_group_detail_item,null);
				viewHolder=new ViewHolder();
				viewHolder.title=(TextView)view.findViewById(android.R.id.text1);
				viewHolder.title.setSingleLine(true);
				viewHolder.title.setEllipsize(TruncateAt.END);

			}else if(mGroupId==0){//名片夹
				view=inflater.inflate(R.layout.mst_group_detail_item_for_business_card,null);
				viewHolder=new ViewHolder();
				viewHolder.title=(TextView)view.findViewById(android.R.id.text1);
				viewHolder.title.setEllipsize(TruncateAt.END);
				viewHolder.icon=(ImageView)view.findViewById(R.id.icon);
				viewHolder.secondLineTitle=(TextView)view.findViewById(android.R.id.text2);
				viewHolder.thirdLineTitle=(TextView)view.findViewById(R.id.text3);
				viewHolder.secondLineTitle.setEllipsize(TruncateAt.END);
				viewHolder.thirdLineTitle.setEllipsize(TruncateAt.END);
				view.setTag(viewHolder);				
			}

			viewHolder.checkBox=(CheckBox)view.findViewById(android.R.id.button1);
			final SliderView slider=(SliderView)view.findViewById(R.id.slider_view1);
			slider.addTextButton(1,deleteString);
			slider.setTag(R.id.slider_tag,position);
			slider.setOnSliderButtonClickListener(onSliderButtonLickListener);
			slider.setSwipeListener(new SwipeListener(){
				public void onClosed(SliderLayout view){
					currentSliderView=null;
				}
				public void onOpened(SliderLayout view){
					currentSliderView=slider;
				}
				public void onSlide(SliderLayout view, float slideOffset){
				}
			});			

			viewHolder.slider=slider;
			view.setTag(viewHolder);			
		}

		viewHolder.title.setText(name);
		viewHolder.checkBox.setVisibility(is_editor_mode?View.VISIBLE:View.GONE);
		viewHolder.checkBox.setChecked(checkeds[position]);
		if(is_editor_mode){
			viewHolder.slider.setLockDrag(true);
		}else{
			viewHolder.slider.setLockDrag(false);
		}
		if(viewHolder.slider.isOpened()){
			viewHolder.slider.close(false);
		}


		if(mGroupId==0){
			Log.d(TAG,"position:"+position+" photoHashMap.get(position):"+photoHashMap.get(position));
			if (photoHashMap.get(position)!= null) {// 内存中有头像  
				Log.d(TAG,"1");
				viewHolder.icon.setImageBitmap(photoHashMap.get(position));  
			} else{//头像还没有加载到内存  

				//				Log.d(TAG,"filePath:"+filePath+" bitmap:"+bitmap);
				if(scrollStauts==0){
					Log.d(TAG,"2");
					String filePath=Environment.getExternalStorageDirectory()+"/bcr/imgs/"+lookupKey+".jpg";
					final Bitmap bitmap=BitmapFactory.decodeFile(filePath);

					if(bitmap!=null){
						Log.d(TAG,"3");
						photoHashMap.put(position,bitmap);  
						viewHolder.icon.setImageBitmap(bitmap);
						
					}else {
						Log.d(TAG,"4");
						viewHolder.icon.setImageResource(R.drawable.mst_business_card_default_photo);  
					}  
				}else {
					Log.d(TAG,"5");
					viewHolder.icon.setImageResource(R.drawable.mst_business_card_default_photo);  
				}  
			}
			
			viewHolder.icon.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					//					AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
					//					//					alertDialog.setTitle("结果");
					//					//					alertDialog.setMessage(value);
					//					View contentView=inflater.inflate(R.layout.mst_business_card_large_photo, null);
					//					ImageView imageView=(ImageView)contentView.findViewById(R.id.image);
					//					imageView.setImageBitmap(bitmap);
					////					alertDialog.setButton(closeString, new DialogInterface.OnClickListener() {
					////						public void onClick(DialogInterface dialog, int which) {
					////							//Just to provide information to user no need to do anything.
					////						}
					////					});
					//					alertDialog.show();
					//					alertDialog.addContentView(contentView,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
					//					
					//					DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
					//					int width = dm.widthPixels;
					//					int height = dm.heightPixels;
					//					Log.d(TAG,"width:"+width+" height:"+height);
					//					//设置窗口的大小    
					//					alertDialog.getWindow().setLayout(width, height); 

					if(mCallbacks!=null) mCallbacks.onFragmentCallback(FragmentCallbacks.SHOW_BUSINESS_CARD_LARGE_PHOTO, photoHashMap.get(position));
				}
			});

			String companyData=companyHashMap!=null&&companyHashMap.size()>0?companyHashMap.get(lookupKey):null;
			Log.d(TAG,"companyData:"+companyData+" lookupKey:"+lookupKey);
			if(companyData!=null){
				String data1=companyData.substring(0,companyData.indexOf("þ"));//公司
				String data4=companyData.substring(companyData.indexOf("þ")+1);//职位
				Log.d(TAG,"companyData:"+companyData+" data1:"+data1+" data4:"+data4);
				viewHolder.secondLineTitle.setText(TextUtils.isEmpty(data4)||TextUtils.equals("null", data4.trim())?unKnowTitleString:data4);
				viewHolder.thirdLineTitle.setText(TextUtils.isEmpty(data1)||TextUtils.equals("null", data1.trim())?unKnowCompanyString:data1);
			}else{
				viewHolder.secondLineTitle.setText(unKnowTitleString);
				viewHolder.thirdLineTitle.setText(unKnowCompanyString);
			}
		}

		return view;

		/*int itemViewType = getItemViewType(position);

        if (itemViewType == ViewTypes.DIVIDER) {
            // Checking For Divider First so not to cast convertView
            final TextView textView = (TextView) (convertView == null ? getDivider() : convertView);
            setDividerPadding(textView, position == 0);
            return textView;
        }

        ContactTileRow contactTileRowView = (ContactTileRow) convertView;
        ArrayList<ContactEntry> contactList = getItem(position);

        if (contactTileRowView == null) {
            // Creating new row if needed
            contactTileRowView = new ContactTileRow(mContext, itemViewType);
        }

        contactTileRowView.configureRow(contactList, position == getCount() - 1);
        return contactTileRowView;*/
	}



	protected DefaultImageRequest getDefaultImageRequest(String displayName, String lookupKey) {
		return new DefaultImageRequest(displayName, lookupKey, true);
	}

	/**
	 * Divider uses a list_seperator.xml along with text to denote
	 * the most frequently contacted contacts.
	 */
	private TextView getDivider() {
		return MoreContactUtils.createHeaderView(mContext, R.string.favoritesFrequentContacted);
	}

	private void setDividerPadding(TextView headerTextView, boolean isFirstRow) {
		MoreContactUtils.setHeaderViewBottomPadding(mContext, headerTextView, isFirstRow);
	}

	private int getLayoutResourceId(int viewType) {
		switch (viewType) {
		case ViewTypes.STARRED:
			return mIsQuickContactEnabled ?
					R.layout.contact_tile_starred_quick_contact : R.layout.contact_tile_starred;
		case ViewTypes.FREQUENT:
			return R.layout.contact_tile_frequent;
		default:
			throw new IllegalArgumentException("Unrecognized viewType " + viewType);
		}
	}
	@Override
	public int getViewTypeCount() {
		return ViewTypes.COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		/*
		 * Returns view type based on {@link DisplayType}.
		 * {@link DisplayType#STARRED_ONLY} and {@link DisplayType#GROUP_MEMBERS}
		 * are {@link ViewTypes#STARRED}.
		 * {@link DisplayType#FREQUENT_ONLY} is {@link ViewTypes#FREQUENT}.
		 * {@link DisplayType#STREQUENT} mixes both {@link ViewTypes}
		 * and also adds in {@link ViewTypes#DIVIDER}.
		 */
		switch (mDisplayType) {
		case STREQUENT:
			if (position < getRowCount(mDividerPosition)) {
				return ViewTypes.STARRED;
			} else if (position == getRowCount(mDividerPosition)) {
				return ViewTypes.DIVIDER;
			} else {
				return ViewTypes.FREQUENT;
			}
		case STARRED_ONLY:
			return ViewTypes.STARRED;
		case FREQUENT_ONLY:
			return ViewTypes.FREQUENT;
		default:
			throw new IllegalStateException("Unrecognized DisplayType " + mDisplayType);
		}
	}

	/**
	 * Returns the "frequent header" position. Only available when STREQUENT or
	 * STREQUENT_PHONE_ONLY is used for its display type.
	 */
	public int getFrequentHeaderPosition() {
		return getRowCount(mDividerPosition);
	}

	/**
	 * Acts as a row item composed of {@link ContactTileView}
	 *
	 * TODO: FREQUENT doesn't really need it.  Just let {@link #getView} return
	 */
	private class ContactTileRow extends FrameLayout {
		private int mItemViewType;
		private int mLayoutResId;

		public ContactTileRow(Context context, int itemViewType) {
			super(context);
			mItemViewType = itemViewType;
			mLayoutResId = getLayoutResourceId(mItemViewType);

			// Remove row (but not children) from accessibility node tree.
			setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
		}

		/**
		 * Configures the row to add {@link ContactEntry}s information to the views
		 */
		public void configureRow(ArrayList<ContactEntry> list, boolean isLastRow) {
			int columnCount = mItemViewType == ViewTypes.FREQUENT ? 1 : mColumnCount;

			// Adding tiles to row and filling in contact information
			for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
				ContactEntry entry =
						columnCounter < list.size() ? list.get(columnCounter) : null;
						addTileFromEntry(entry, columnCounter, isLastRow);
			}
		}

		private void addTileFromEntry(ContactEntry entry, int childIndex, boolean isLastRow) {
			final ContactTileView contactTile;

			if (getChildCount() <= childIndex) {
				contactTile = (ContactTileView) inflate(mContext, mLayoutResId, null);
				// Note: the layoutparam set here is only actually used for FREQUENT.
				// We override onMeasure() for STARRED and we don't care the layout param there.
				Resources resources = mContext.getResources();
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				params.setMargins(
						mWhitespaceStartEnd,
						0,
						mWhitespaceStartEnd,
						0);
				contactTile.setLayoutParams(params);
				contactTile.setPhotoManager(mPhotoManager);
				contactTile.setListener(mListener);
				addView(contactTile);
			} else {
				contactTile = (ContactTileView) getChildAt(childIndex);
			}
			contactTile.loadFromContact(entry);

			switch (mItemViewType) {
			case ViewTypes.STARRED:
				// Set padding between tiles. Divide mPaddingInPixels between left and right
				// tiles as evenly as possible.
				contactTile.setPaddingRelative(
						(mPaddingInPixels + 1) / 2, 0,
						mPaddingInPixels
						/ 2, 0);
				break;
			case ViewTypes.FREQUENT:
				contactTile.setHorizontalDividerVisibility(
						isLastRow ? View.GONE : View.VISIBLE);
				break;
			default:
				break;
			}
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			switch (mItemViewType) {
			case ViewTypes.STARRED:
				onLayoutForTiles();
				return;
			default:
				super.onLayout(changed, left, top, right, bottom);
				return;
			}
		}

		private void onLayoutForTiles() {
			final int count = getChildCount();

			// Amount of margin needed on the left is based on difference between offset and padding
			int childLeft = mWhitespaceStartEnd - (mPaddingInPixels + 1) / 2;

			// Just line up children horizontally.
			for (int i = 0; i < count; i++) {
				final int rtlAdjustedIndex = ViewUtil.isViewLayoutRtl(this) ? count - i - 1 : i;
				final View child = getChildAt(rtlAdjustedIndex);

				// Note MeasuredWidth includes the padding.
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			switch (mItemViewType) {
			case ViewTypes.STARRED:
				onMeasureForTiles(widthMeasureSpec);
				return;
			default:
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				return;
			}
		}

		private void onMeasureForTiles(int widthMeasureSpec) {
			final int width = MeasureSpec.getSize(widthMeasureSpec);

			final int childCount = getChildCount();
			if (childCount == 0) {
				// Just in case...
				setMeasuredDimension(width, 0);
				return;
			}

			// 1. Calculate image size.
			//      = ([total width] - [total whitespace]) / [child count]
			//
			// 2. Set it to width/height of each children.
			//    If we have a remainder, some tiles will have 1 pixel larger width than its height.
			//
			// 3. Set the dimensions of itself.
			//    Let width = given width.
			//    Let height = wrap content.

			final int totalWhitespaceInPixels = (mColumnCount - 1) * mPaddingInPixels
					+ mWhitespaceStartEnd * 2;

			// Preferred width / height for images (excluding the padding).
			// The actual width may be 1 pixel larger than this if we have a remainder.
			final int imageSize = (width - totalWhitespaceInPixels) / mColumnCount;
			final int remainder = width - (imageSize * mColumnCount) - totalWhitespaceInPixels;

			for (int i = 0; i < childCount; i++) {
				final View child = getChildAt(i);
				final int childWidth = imageSize + child.getPaddingRight() + child.getPaddingLeft()
						// Compensate for the remainder
						+ (i < remainder ? 1 : 0);

				child.measure(
						MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
						);
			}
			setMeasuredDimension(width, getChildAt(0).getMeasuredHeight());
		}
	}

	protected static class ViewTypes {
		public static final int COUNT = 4;
		public static final int STARRED = 0;
		public static final int DIVIDER = 1;
		public static final int FREQUENT = 2;
	}
	@Override
	public ContactEntry getItem(int position) {
		// TODO Auto-generated method stub
		return createContactEntryFromCursor(mContactCursor,position);
	}
}