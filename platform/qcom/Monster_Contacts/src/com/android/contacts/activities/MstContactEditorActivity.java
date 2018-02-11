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
 * limitations under the License
 */

package com.android.contacts.activities;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor.SaveMode;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.editor.CompactContactEditorFragment;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.editor.MstContactEditorFragment;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.interactions.GroupCreationDialogFragment;
import com.android.contacts.util.DialogManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.mediatek.contacts.ContactSaveServiceEx;
import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.simservice.SIMEditProcessor;

import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;

/**M: Add SIMEditProcessor.Listener*/
/**
 * Contact editor with all fields displayed.
 */
public class MstContactEditorActivity extends ContactEditorBaseActivity
        implements DialogManager.DialogShowingViewActivity
        {
    private static final String TAG = "MstContactEditorActivity";
    /** M: @{ */
    public static final String KEY_ACTION = "key_action";
    private String mAction;
    private ActionMode actionMode;
    /** @} */

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.d(TAG, "[onCreate]");

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.w(TAG, "[onCreate]no permission,return.");
            return;
        }

        /// M: Descriptions: can not add contact when in delete processing
        if (ActivitiesUtils.isDeleteingContact(this)) {
            Log.w(TAG, "[onCreate]deleting contact,return.");
            return;
        }

        setMstContentView(R.layout.mst_contact_editor_activity);


        mFragment = (MstContactEditorFragment) getFragmentManager().findFragmentById(
                R.id.contact_editor_fragment);
        mFragment.setListener(mFragmentListener);

        final String action = getIntent().getAction();
        final Uri uri = ContactEditorBaseActivity.ACTION_EDIT.equals(action)
                || Intent.ACTION_EDIT.equals(action) ? getIntent().getData() : null;
        mFragment.load(action, uri, getIntent().getExtras());
        
        getToolbar().setElevation(0f);
        setupActionModeWithDecor(getToolbar());
        actionMode = getActionMode();
        actionMode.setNagativeText(getString(R.string.mst_cancel));
		actionMode.setPositiveText(getString(R.string.mst_ok));
		actionMode.bindActionModeListener(new ActionModeListener(){
			public void onActionItemClicked(Item item){
				Log.d(TAG,"onActionItemClicked,itemid:"+item.getItemId());
				switch (item.getItemId()) {
				case ActionMode.POSITIVE_BUTTON:
					mFragment.save(SaveMode.CLOSE, /* backPressed =*/ true);
					break;
				case ActionMode.NAGATIVE_BUTTON:
					mFragment.revert();
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
		mHandler.postDelayed(mShowActionModeRunnable,300);
		
	    mFromMstBusiness = getIntent().getBooleanExtra("fromMstBusiness", false);
	    if(mFromMstBusiness) {
	        View view = findViewById(R.id.for_input_method);
	        view.setVisibility(View.VISIBLE);
	    }
    }
    
    

    /** M: Bug Fix CR ID: ALPS00251666 @{
     * Description:Can't open the Join Contact Activity when change screen orientation.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(mAction)) {
            outState.putString(KEY_ACTION, mAction);
        }
        super.onSaveInstanceState(outState);
    }
    /** @} */

    @Override
    public void onBackPressed() {
         if (mFragment != null) {
        	 Log.d(TAG, "[onBackPressed]mFragment.revert.");
        	 mFragment.revert();
             //Log.d(TAG, "[onBackPressed]save,SaveMode.COMPACT.");
            //mFragment.save(ContactEditor.SaveMode.COMPACT, /* backPressed =*/ true);
        }
    }

    private Handler mHandler = new Handler();
	private Runnable mShowActionModeRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			showActionMode(true);
		}
	};
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (mFragment == null) {
			Log.w(TAG, "[onNewIntent] the mFragment is null,return.");
			return;
		}

		// / M: @{
		int mSubId = intent.getIntExtra(ContactSaveServiceEx.EXTRA_SUB_ID, -1);
		int saveMode = intent
				.getIntExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.CLOSE);
//		Log.d(TAG, "[onNewIntent] mSubId:" + mSubId + ",saveMode:" + saveMode + ",action:"
//				+ intent.getAction());
		// / @}
		String action = intent.getAction();
		if (GroupCreationDialogFragment.ACTION_COMPLETED.equals(action)) {
			Log.d(TAG, "[onNewIntent] mSubId:" + mSubId + ",saveMode:" + saveMode + ",action:"
					+ intent.getAction());
			((MstContactEditorFragment)mFragment).onGroupCreated(true, intent.getData());
			// / M: @{
			boolean isSuccess = intent.getData() != null;
			if (isSuccess && saveMode != SaveMode.RELOAD) {
//				Toast.makeText(getApplicationContext(), R.string.groupSavedToast,
//						Toast.LENGTH_SHORT).show();
			}
			// / @}
		}
		
	}
	//add by lgy
	public static boolean mFromMstBusiness;
}
