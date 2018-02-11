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
 * limitations under the License
 */

package com.android.contacts.activities;

import com.android.contacts.common.mst.FragmentCallbacks;

import mst.widget.SliderView;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor.SaveMode;
import com.android.contacts.common.list.ContactEntry;
import com.mediatek.contacts.util.MtkToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import  mst.view.menu.bottomnavigation.BottomNavigationView;
import  mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.toolbar.Toolbar;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;
import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.group.GroupDetailDisplayUtils;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.group.GroupDetailFragment.SendGroupEmailTask;
import com.android.contacts.group.GroupDetailFragment.SendGroupSmsTask;
import com.android.contacts.group.GroupEditorFragment.Member;
import com.android.contacts.interactions.GroupDeletionDialogFragment;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.editor.ContactEditorFragment;

import com.mediatek.contacts.ContactSaveServiceEx;
import com.mediatek.contacts.ContactsApplicationEx;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.activities.GroupBrowseActivity;
import com.mediatek.contacts.activities.GroupBrowseActivity.AccountCategoryInfo;
import com.mediatek.contacts.list.ContactListMultiChoiceActivity;
import com.mediatek.contacts.list.MultiDuplicationPickerFragment;
import com.mediatek.contacts.list.PhoneAndEmailsPickerFragment;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.util.Log;

public class GroupDetailActivity extends ContactsActivity implements mst.widget.toolbar.Toolbar.OnMenuItemClickListener,
SliderView.OnSliderButtonLickListener,FragmentCallbacks{
	private ActionMode actionMode;
	private BottomNavigationView bottomBar;
	public Toolbar toolbar;

	private static final String TAG = "GroupDetailActivity";
	private GroupEditorFragment mGroupEditorFragment;
	private boolean mShowGroupSourceInActionBar;

	private String mAccountTypeString;
	private String mDataSet;

	/// M:
	private static final int SUBACTIVITY_EDIT_GROUP = 1;
	private GroupDetailFragment mFragment;
	private int mAccountGroupMemberCount;



	@Override
	public void onBackPressed() {
		
		if(mFragment.imageFrameLayout.getVisibility()==View.VISIBLE){
			mFragment.imageFrameLayout.setVisibility(View.GONE);
			toolbar.setVisibility(View.VISIBLE);
			return;
		}
		if(mFragment.getAdapter().getEditMode()){
			switchToEditMode(false);
		}else {
			if(mFragment.getAdapter().getCurrentSliderView()!=null){
				mFragment.getAdapter().getCurrentSliderView().close(true);
				mFragment.getAdapter().setCurrentSliderView(null);
				return;
			}
			super.onBackPressed();
		}
	}

	public void switchToEditMode(boolean flag){
		if(flag){
			showActionMode(true);
			bottomBar.setVisibility(View.VISIBLE);
			mFragment.getAdapter().setEditMode(true);				
			updateActionMode();
			mFragment.getAdapter().notifyDataSetChanged();
			mFragment.getAdapter().setCurrentSliderView(null);
	          int bottommenuHeight = getResources().getDimensionPixelOffset(
	                    R.dimen.bottom_menu_height);
	          mFragment.getView().setPadding(0, 0, 0, bottommenuHeight);
		}else{
			showActionMode(false);
			bottomBar.setVisibility(View.GONE);
			mFragment.getAdapter().setAllSelect(false);
			mFragment.getAdapter().setEditMode(false);
			mFragment.getAdapter().notifyDataSetChanged();			
			mFragment.getView().setPadding(0, 0, 0, 0);
		}
	}

	private static final String ARG_GROUP_ID = "groupId";
	private static final String ARG_LABEL = "label";
	private static final String ARG_SHOULD_END_ACTIVITY = "endActivity";
	protected void deleteGroup(long groupId,String groupName,int subId) {
		builder.dismiss();
		startService(ContactSaveService.createGroupDeletionIntentForIcc(
				GroupDetailActivity.this, groupId, subId, groupName));
		Toast.makeText(GroupDetailActivity.this, "删除成功", Toast.LENGTH_LONG).show();
		finish();
	}

	private boolean isForAddFlag=false;
	AlertDialog builder=null;
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Log.d(TAG,"onMenuItemClick:"+item.getItemId());

		// / M: judge contactsapplication is busy or not? fixed cr ALPS00567939
		// & ALPS00542175 @{
		if (ContactsApplicationEx.isContactsApplicationBusy()) {
			Log.w(TAG, "[onOptionsItemSelected]contacts busy doing something");
			MtkToast.toast(GroupDetailActivity.this, R.string.phone_book_busy);
			return false;
		}
		// / @}
		switch (item.getItemId()) {
		case R.id.menu_edit_group: {
			if(mFragment.getGroupId()==0){
				Toast.makeText(getApplicationContext(), "该群组不能重命名",Toast.LENGTH_LONG).show();
				return true;
			}
			// The following lines are provided and maintained by Mediatek Inc.
			Uri groupUri = mFragment.getmGroupUri().buildUpon().appendPath(String.valueOf(mSubId)).build();                
			final Intent intent = new Intent(GroupDetailActivity.this, GroupEditorActivity.class);
			/** M: Bug Fix CR ID :ALPS000116203 @{ */
			mSubId = Integer.parseInt(groupUri.getLastPathSegment().toString());
			String grpId = groupUri.getPathSegments().get(1).toString();
			Log.d(TAG, grpId + "--------grpId");
			Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon()
					.appendPath(grpId).build();
			Log.d(TAG, uri.toString() + "--------groupUri.getPath();");
			intent.setData(uri);
			intent.setAction(Intent.ACTION_EDIT);
			intent.putExtra("SIM_ID", mSubId);
			/// M: For feature move to other groups.
			intent.putExtra("GROUP_NUMS", mAccountGroupMemberCount);
			startActivityForResult(intent, SUBACTIVITY_EDIT_GROUP);
			/** @} */
			break;
		}

		case R.id.menu_delete_group: {
			if(mFragment.getGroupId()==0){
				Toast.makeText(getApplicationContext(), "该群组不能删除",Toast.LENGTH_LONG).show();
				return true;
			}
			// The following lines are provided and maintained by Mediatek Inc.
			//			GroupDeletionDialogFragment.show(getFragmentManager(), mFragment.getGroupId(), mFragment.getmGroupName(),
			//					mFragment.ismCloseActivityAfterDelete(), mSubId);
			// The previous lines are provided and maintained by Mediatek Inc.
			String title=getString(R.string.mst_delete_group_dialog_title);
			String message = getString(R.string.mst_delete_group_dialog_message);
			if(builder==null){
				builder=new AlertDialog.Builder(GroupDetailActivity.this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.mst_dissolved,
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {

						deleteGroup(mFragment.getGroupId(),mFragment.getmGroupName(),mSubId);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create();				
			}
			builder.show();

			return true;
		}
		// The following lines are provided and maintained by Mediatek Inc.
		case R.id.menu_move_group: {
			Intent moveIntent = new Intent(GroupDetailActivity.this, ContactListMultiChoiceActivity.class);
			moveIntent.setAction(com.mediatek.contacts.util.
					ContactsIntent.LIST.ACTION_GROUP_MOVE_MULTI_CONTACTS);
			moveIntent.putExtra("mGroupName", mFragment.getmGroupName());
			moveIntent.putExtra("mSubId", mSubId);
			moveIntent.putExtra("mGroupId", mFragment.getGroupId());
			moveIntent.putExtra("mAccountName", mFragment.getmAccountName());
			if (!TextUtils.isEmpty(mFragment.getmAccountName())
					&& !TextUtils.isEmpty(mAccountTypeString)) {
				Account tmpAccount = new Account(mFragment.getmAccountName(), mAccountTypeString);
				moveIntent.putExtra("account", tmpAccount);
			}

			GroupDetailActivity.this.startActivity(moveIntent);
			if (!PhoneCapabilityTester.isUsingTwoPanes(GroupDetailActivity.this)) {
				GroupDetailActivity.this.finish();
			}
			break;
		}
		case R.id.menu_message_group: {
			mFragment.new SendGroupSmsTask(GroupDetailActivity.this).execute(mFragment.getmAccountName());
			break;
		}
		case R.id.menu_email_group: {
			mFragment.new SendGroupEmailTask(GroupDetailActivity.this).execute(mFragment.getmAccountName());
			break;
			// The previous  lines are provided and maintained by Mediatek Inc.
		}

		case R.id.mst_menu_addGroupMember:{		
			if(mFragment.getGroupId()==0){
				startActivity(new Intent(GroupDetailActivity.this,MstBusinessCardScanActivity.class));
				return true;
			}

			isForAddFlag=true;
			mGroupEditorFragment.getmListMembersToRemove().clear();
			mGroupEditorFragment.getmListMembersToAdd().clear();
			mGroupEditorFragment.addMembers();
			//			addMembers();

			break;
		}
		}
		return false;
	}


	//	private static final int MULTIPLE_ADD_GROUP_MEMBER = 24;
	//	private void addMembers() {
	//		try {
	//			Intent intent = new Intent(GroupDetailActivity.this, ContactListMultiChoiceActivity.class);
	//			intent.setAction(
	//					com.mediatek.contacts.util.ContactsIntent.LIST.ACTION_GROUP_ADD_MULTI_CONTACTS);
	//			intent.setType(Contacts.CONTENT_TYPE);
	//			intent.putExtra("account_type", mFragment.getmAccountTypeString());
	//			intent.putExtra("account_name", mFragment.getmAccountName());
	//			int size = mGroupEditorFragment.getmListToDisplay().size();
	//			long[] mContactIds = new long[size];
	//			int i = 0;
	//			for (GroupEditorFragment.Member member : mGroupEditorFragment.getmListToDisplay()) {
	//				mContactIds[i++] = member.getContactId();
	//			}
	//			intent.putExtra("member_ids", mContactIds);
	//			startActivityForResult(intent, MULTIPLE_ADD_GROUP_MEMBER);
	//		} catch (ActivityNotFoundException e) {
	//			Log.d(TAG, "ActivityNotFoundException for addMembers Intent");
	//		}
	//	}

	//    private static final String RESULTINTENTEXTRANAME =
	//            "com.mediatek.contacts.list.pickcontactsresult";
	//	@Override
	//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	//		Log.d(TAG, "onActivityResult(),requestCode:" + requestCode
	//	            + ",resultCode:" + resultCode + ",data:" + data);
	//	        super.onActivityResult(requestCode, resultCode, data);
	//	        // if the return data is right.
	//	        if (requestCode == MULTIPLE_ADD_GROUP_MEMBER && data != null&&resultCode==Activity.RESULT_OK) {
	//	            // get the contactIds from contact multiple selection.
	//	            long[] contactIds = data.getLongArrayExtra(RESULTINTENTEXTRANAME);
	//	            
	//	            saveIntent = ContactSaveService.createNewGroupIntentForIcc(GroupDetailActivity.this,
	//	                    new AccountWithDataSet(mFragment.getmAccountName(), mFragment.getmAccountTypeString(), mDataSet),
	//	                    ///M:fix CR:ALPS01039938,the group name at contact and contact group not unify
	//	                    mFragment.getmGroupName(),
	//	                    contactIds, /*activity.getClass()*/null,
	//	                   GroupEditorActivity.ACTION_SAVE_COMPLETED, simIndexArray, mSubId);
	//	        }
	//	}

	public void updateActionMode(){
		if(mFragment.getAdapter().isAllSelect()){
			actionMode.setPositiveText(getString(R.string.mst_actionmode_selectnone));
		}else{
			actionMode.setPositiveText(getString(R.string.mst_actionmode_selectall));
		}
		actionMode.setTitle(String.format(
				getString(R.string.mst_menu_actionbar_selected_items),
				mFragment.getAdapter().getCheckedCount()));	

		Log.d(TAG,"mFragment.getAdapter().getCheckedCount():"+mFragment.getAdapter().getCheckedCount());
		if(mFragment.getAdapter().getCheckedCount()>0){
			bottomBar.setVisibility(View.VISIBLE);
		}else{
			bottomBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedState);

		// TODO: Create Intent Resolver to handle the different ways users can get to this list.
		// TODO: Handle search or key down

		setMstContentView(R.layout.group_detail_activity);

		toolbar = getToolbar();
		toolbar.setElevation(0f);
		toolbar.setTitle(getResources().getString(R.string.mst_goto_group_header));
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"NavigationOnClickListener");
				finish();
			}
		});
		setupActionModeWithDecor(toolbar);
		toolbar.inflateMenu(R.menu.view_group);

		Menu menu=toolbar.getMenu();
		MenuItem menu_message_group=menu.findItem(R.id.menu_message_group);
		MenuItem menu_email_group=menu.findItem(R.id.menu_email_group);
		MenuItem menu_edit=menu.findItem(R.id.menu_edit_group);
		MenuItem menu_delete=menu.findItem(R.id.menu_delete_group);
		menu.findItem(R.id.menu_move_group).setVisible(false);

		actionMode=getActionMode();
		actionMode.setNagativeText(getString(R.string.mst_cancel));
		actionMode.bindActionModeListener(new ActionModeListener(){
			/**
			 * ActionMode上面的操作按钮点击时触发，在这个回调中，默认提供两个ID使用，
			 * 确定按钮的ID是ActionMode.POSITIVE_BUTTON,取消按钮的ID是ActionMode.NAGATIVE_BUTTON
			 * @param view
			 */
			public void onActionItemClicked(Item item){
				Log.d(TAG,"onActionItemClicked,itemid:"+item.getItemId());
				switch (item.getItemId()) {
				case ActionMode.POSITIVE_BUTTON:	{
					boolean mIsSelectedAll = mFragment.getAdapter().isAllSelect();
					if(mIsSelectedAll){
						mFragment.getAdapter().setAllSelect(false);
					}else{
						mFragment.getAdapter().setAllSelect(true);
					}
					updateActionMode();
					mFragment.getAdapter().notifyDataSetChanged();
					break;
				}

				case ActionMode.NAGATIVE_BUTTON:
					switchToEditMode(false);
					break;
				default:
					break;
				}
			}

			/**
			 * ActionMode显示的时候触发
			 * @param actionMode
			 */
			public void onActionModeShow(ActionMode actionMode){

			}

			/**
			 * ActionMode消失的时候触发
			 * @param actionMode
			 */
			public void onActionModeDismiss(ActionMode actionMode){

			}
		});



		bottomBar = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
		bottomBar.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {

			@Override
			public boolean onNavigationItemSelected(MenuItem arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onNavigationItemSelected,arg0.getItemId():"+arg0.getItemId());
				switch (arg0.getItemId()) {
				case R.id.mst_remove:
					removeMember();
					break;

				default:
					break;
				} 
				return false;
			}			
		});

		mShowGroupSourceInActionBar = getResources().getBoolean(
				R.bool.config_show_group_action_in_action_bar);

		mFragment = (GroupDetailFragment) getFragmentManager().findFragmentById(
				R.id.group_detail_fragment);
		mFragment.setListener(mFragmentListener);
		mFragment.setShowGroupSourceInActionBar(mShowGroupSourceInActionBar);
		mFragment.setMenu_message_group(menu_message_group);
		mFragment.setMenu_email_group(menu_email_group);
		mFragment.setMenu_edit(menu_edit);
		mFragment.setMenu_delete(menu_delete);
		mFragment.setMenu(menu);
		mFragment.getAdapter().setOnSliderButtonLickListener(this);
		mFragment.dragImageView.setmCallbacks(this);
		/// M: marked google default code. @{
		//mFragment.loadGroup(getIntent().getData());
		//mFragment.closeActivityAfterDelete(true);

		/// @}
		/** M: New feature @{  */
		setAccountCategoryInfo();
		/** @} */
		// We want the UP affordance but no app icon.
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
					ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
					| ActionBar.DISPLAY_SHOW_HOME);
		}

		if(mGroupEditorFragment==null){
			mGroupEditorFragment=new GroupEditorFragment();
			//			mFragment.setListener(mFragmentListener);
			mGroupEditorFragment.setContentResolver(getContentResolver());
			Intent intent=new Intent();
			intent.putExtra("SIM_ID", mSubId);
			/// M: For feature move to other groups.
			intent.putExtra("GROUP_NUMS", mAccountGroupMemberCount);
			Uri groupUri = mFragment.getmGroupUri().buildUpon().appendPath(String.valueOf(mSubId)).build();    
			String grpId = groupUri.getPathSegments().get(1).toString();
			Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon()
					.appendPath(grpId).build();
			mGroupEditorFragment.load(Intent.ACTION_EDIT, uri, intent.getExtras(), mSubId);

			getFragmentManager().beginTransaction()
			.add(R.id.hidden_group_edit_fragment, mGroupEditorFragment,"groupedit")
			.commit();
		}
		mFragment.getAdapter().setmCallbacks(this);
	}

	@Override
	public void onSliderButtonClick(int id, View view, ViewGroup parent) {
		Log.d(TAG,"onSliderButtonClick1,id:"+id+" view:"+view+" parent:"+parent+" parent.getTag():"+parent.getTag(R.id.slider_tag));
		switch (id) {
		case 1:
			// TODO Auto-generated method stub
			mFragment.getAdapter().setCurrentSliderView(null);
			if(((SliderView)parent).isOpened()){
				((SliderView)parent).close(false);
			}
			removeMember(Integer.parseInt(parent.getTag(R.id.slider_tag).toString()));
			mFragment.getAdapter().setPhotoHashMap(new HashMap<Integer, Bitmap>());
		}
	}

	private void removeMember() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new Builder(GroupDetailActivity.this);
		builder.setMessage(getString(R.string.mst_remove_group_member_message));
		builder.setTitle(getString(R.string.mst_remove_group_member_title));
		builder.setNegativeButton(getString(R.string.mst_cancel), null);
		builder.setPositiveButton(getString(R.string.mst_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which)
			{
				isForAddFlag=false;
				dialog.dismiss();
				Log.d(TAG,"mst_remove_member");
				mGroupEditorFragment.getmListMembersToRemove().clear();
				mGroupEditorFragment.getmListMembersToAdd().clear();
				for(int i=0;i<mFragment.getAdapter().getCount();i++){
					if(mFragment.getAdapter().getCheckedArrayValue(i)){
						ContactEntry contactEntry=mFragment.getAdapter().getItem(i);
						Log.d(TAG,"contactEntry.name:"+contactEntry.name
								+" contactEntry.id:"+contactEntry.id
								+" contactEntry.rawContactId:"+contactEntry.rawContactId);
						Member member = new Member(contactEntry.rawContactId, contactEntry.lookupKey,
								contactEntry.id, contactEntry.name, null, contactEntry.indexSimOrPhone);
						mGroupEditorFragment.removeMember(member);
					}
				}
				mGroupEditorFragment.save(SaveMode.CLOSE, false);
				switchToEditMode(false);
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	private void removeMember(final int i) {
		Log.d(TAG,"removeMember:"+i);
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new Builder(GroupDetailActivity.this);
		builder.setMessage(getString(R.string.mst_remove_group_member_message));
		builder.setTitle(getString(R.string.mst_remove_group_member_title));
		builder.setNegativeButton(getString(R.string.mst_cancel), null);
		builder.setPositiveButton(getString(R.string.mst_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which)
			{
				isForAddFlag=false;
				dialog.dismiss();
				Log.d(TAG,"mst_remove_member");
				mGroupEditorFragment.getmListMembersToRemove().clear();
				mGroupEditorFragment.getmListMembersToAdd().clear();
				ContactEntry contactEntry=mFragment.getAdapter().getItem(i);
				if(contactEntry==null) return;
				Log.d(TAG,"contactEntry.name:"+contactEntry.name
						+" contactEntry.id:"+contactEntry.id
						+" contactEntry.rawContactId:"+contactEntry.rawContactId);
				Member member = new Member(contactEntry.rawContactId, contactEntry.lookupKey,
						contactEntry.id, contactEntry.name, null, contactEntry.indexSimOrPhone);
				mGroupEditorFragment.removeMember(member);
				mGroupEditorFragment.save(SaveMode.CLOSE, false);

			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	String titleString="";
	String sizeString="";
	String toolbarTitle="";
	private final GroupDetailFragment.Listener mFragmentListener =
			new GroupDetailFragment.Listener() {

		@Override
		public void onGroupNotFound() {
			/// M:
			finish();
		}

		@Override
		public void onGroupSizeUpdated(String size) {
			//			getActionBar().setSubtitle(size);
			sizeString=size;
			if(TextUtils.isEmpty(sizeString)){
				toolbar.setTitle(titleString);
			}else{
				toolbar.setTitle(titleString+" ("+sizeString+")");
			}
		}

		@Override
		public void onGroupTitleUpdated(String title) {
			//			getActionBar().setTitle(title);
			if(title.length()>12) title=title.substring(0,12)+"...";
			titleString=title;
			if(TextUtils.isEmpty(sizeString)){
				toolbar.setTitle(titleString);
			}else{
				toolbar.setTitle(titleString+" ("+sizeString+")");
			}
		}

		@Override
		public void onAccountTypeUpdated(String accountTypeString, String dataSet) {
			mAccountTypeString = accountTypeString;
			mDataSet = dataSet;
			invalidateOptionsMenu();
		}

		@Override
		public void onEditRequested(Uri groupUri) {
			final Intent intent = new Intent(GroupDetailActivity.this, GroupEditorActivity.class);
			/** M: Bug Fix CR ID :ALPS000116203 @{ */
			mSubId = Integer.parseInt(groupUri.getLastPathSegment().toString());
			String grpId = groupUri.getPathSegments().get(1).toString();
			Log.d(TAG, grpId + "--------grpId");
			Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon()
					.appendPath(grpId).build();
			Log.d(TAG, uri.toString() + "--------groupUri.getPath();");
			intent.setData(uri);
			intent.setAction(Intent.ACTION_EDIT);
			intent.putExtra("SIM_ID", mSubId);
			/// M: For feature move to other groups.
			intent.putExtra("GROUP_NUMS", mAccountGroupMemberCount);
			startActivityForResult(intent, SUBACTIVITY_EDIT_GROUP);
			/** @} */

		}

		@Override
		public void onContactSelected(Uri contactUri) {
			Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
			ImplicitIntentsUtil.startActivityInApp(GroupDetailActivity.this, intent);
		}

		@Override
		public void onItemClick(int position,View view) {
			// TODO Auto-generated method stub
			Log.d(TAG,"onItemClick,p:"+position+" view:"+view);
			if(mFragment.getAdapter().getEditMode()){
				
				CheckBox checkBox=(CheckBox) (((ViewGroup)view).getChildAt(1)).findViewById(android.R.id.button1);
				checkBox.toggle();
				mFragment.getAdapter().setCheckedArrayValue(position,checkBox.isChecked());	
				updateActionMode();			
			}else{
				Intent intent = new Intent(Intent.ACTION_VIEW, mFragment.getAdapter().getContactUri(position));
				ImplicitIntentsUtil.startActivityInApp(GroupDetailActivity.this, intent);
			}
		}

		@Override
		public void onItemLongClick(int position) {
			// TODO Auto-generated method stub			
			mFragment.getAdapter().setCheckedArrayValue(position,true);			
			switchToEditMode(true);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (mShowGroupSourceInActionBar) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.group_source, menu);
		}
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (mFragment == null) {
			Log.w(TAG, "[onNewIntent] the mFragment is null,return.");
			return;
		}

		// / M: @{
		mSubId = intent.getIntExtra(ContactSaveServiceEx.EXTRA_SUB_ID, -1);
		int saveMode = intent
				.getIntExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.CLOSE);
		Log.d(TAG, "[onNewIntent] mSubId:" + mSubId + ",saveMode:" + saveMode + ",action:"
				+ intent.getAction());
		// / @}
		String action = intent.getAction();
		if (ACTION_SAVE_COMPLETED.equals(action)) {
			mGroupEditorFragment.onSaveCompleted(true, intent.getData());
			// / M: @{
			boolean isSuccess = intent.getData() != null;
			if (isSuccess && saveMode != SaveMode.RELOAD) {
				Toast.makeText(getApplicationContext(), isForAddFlag?R.string.mst_menu_addedGroupMember:R.string.mst_menu_removeGroupMember,
						Toast.LENGTH_SHORT).show();
			}
			// / @}
		}
	}
	public static final String ACTION_SAVE_COMPLETED = "saveCompleted";
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!mShowGroupSourceInActionBar) {
			return false;
		}
		MenuItem groupSourceMenuItem = menu.findItem(R.id.menu_group_source);
		if (groupSourceMenuItem == null) {
			return false;
		}
		final AccountTypeManager manager = AccountTypeManager.getInstance(this);
		final AccountType accountType =
				manager.getAccountType(mAccountTypeString, mDataSet);
		if (TextUtils.isEmpty(mAccountTypeString)
				|| TextUtils.isEmpty(accountType.getViewGroupActivity())) {
			groupSourceMenuItem.setVisible(false);
			return false;
		}
		View groupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(this);
		GroupDetailDisplayUtils.bindGroupSourceView(this, groupSourceView,
				mAccountTypeString, mDataSet);
		groupSourceView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
						mFragment.getGroupId());
				final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setClassName(accountType.syncAdapterPackageName,
						accountType.getViewGroupActivity());
				ImplicitIntentsUtil.startActivityInApp(GroupDetailActivity.this, intent);
			}
		});
		groupSourceMenuItem.setActionView(groupSourceView);
		groupSourceMenuItem.setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			/// M: In L, return the prior activity. KK will return home activity.
			onBackPressed();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/** M: New feature @{  */
	private void setAccountCategoryInfo() {
		Bundle intentExtras;
		String category = null;
		String simName = null;

		intentExtras = this.getIntent().getExtras();
		final AccountCategoryInfo accountCategoryInfo = intentExtras == null ? null
				: (AccountCategoryInfo) intentExtras.getParcelable(KEY_ACCOUNT_CATEGORY);
		if (accountCategoryInfo != null) {
			category = accountCategoryInfo.mAccountCategory;
			mSubId = accountCategoryInfo.mSubId;
			simName = accountCategoryInfo.mSimName;
			///M:For Feature move to other groups
			mAccountGroupMemberCount = accountCategoryInfo.mAccountGroupMemberCount;
		}
		Log.d(TAG, mSubId + "----mSubId+++++[groupDetailActivity]");
		Log.d(TAG, simName + "----mSimName+++++[groupDetailActivity]");
		Log.d(TAG, mAccountGroupMemberCount + "GroupCounts");
		mFragment.loadExtras(category, mSubId, simName, mAccountGroupMemberCount);

		String callBackIntent = getIntent().getStringExtra("callBackIntent");
		Log.d(TAG, callBackIntent + "----callBackIntent");
		if (null != callBackIntent) {
			int subId = getIntent().getIntExtra("mSubId", -1);
			mFragment.loadExtras(subId);
			Log.d(TAG, subId + "----subId");
		}

		mFragment.loadGroup(getIntent().getData());
		mFragment.closeActivityAfterDelete(false);
	}
	/** @} */

	/// M: @{
	private int mSubId = SubInfoUtils.getInvalidSubId();
	public static final String KEY_ACCOUNT_CATEGORY = "AccountCategory";
	/// @}
	
	@Override
	public Object onFragmentCallback(int what, Object obj) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onFragmentCallback,what:" + what + " obj:" + obj);
		switch (what) {
		case FragmentCallbacks.SHOW_BUSINESS_CARD_LARGE_PHOTO:
			if(obj==null){
				mFragment.imageFrameLayout.setVisibility(View.GONE);
				toolbar.setVisibility(View.VISIBLE);
				return null;
			}
			Bitmap bitmap=(Bitmap)obj;
			if(bitmap!=null){
				mFragment.imageFrameLayout.setVisibility(View.VISIBLE);
				mFragment.dragImageView.setImageBitmap(bitmap);
				toolbar.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
		return null;
	}		
}
