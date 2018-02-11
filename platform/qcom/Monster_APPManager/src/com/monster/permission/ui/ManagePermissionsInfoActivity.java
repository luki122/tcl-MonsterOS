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

package com.monster.permission.ui;

import java.util.ArrayList;
import java.util.List;

import com.monster.appmanager.R;
import com.monster.appmanager.applications.ManageApplications;
import com.monster.appmanager.utils.dialog.SingleChoicePopWindow;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import mst.view.menu.PopupMenu;

public final class ManagePermissionsInfoActivity extends OverlayTouchActivity implements OnClickListener {
    private static final String LOG_TAG = "ManagePermissionsInfoActivity";
	public static final String MANAGE_PERMISSIONS = "android.permission.action.MANAGE_PERMISSIONS";
	public static final String MANAGE_APP_PERMISSIONS = "android.permission.action.MANAGE_APP_PERMISSIONS";
	public static final String MANAGE_PERMISSION_APPS = "android.permission.action.MANAGE_PERMISSION_APPS";
    private boolean mObscuredTouch;
    /* MODIFIED-BEGIN by Ding Tang, 2016-05-20,BUG-2162981*/
    private boolean mPartiallyObscuredTouch;
    private LayoutInflater mInflater;
    private TextView mSpinnerTitle;
    
    public boolean isObscuredTouch() {
        return mObscuredTouch;
    }

    public boolean isPartiallyObscuredTouch() {
        return mPartiallyObscuredTouch;
    }
    private Button button;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Fragment fragment = null;
    	switch (item.getItemId()) {
		case R.id.permissions:
			fragment = ManagePermissionsFragment.newInstance();
			break;
		case R.id.apps:
			fragment = ManageApplications.newInstance();
			break;
		}
    	getFragmentManager().beginTransaction().replace(com.mst.R.id.content, fragment);
    	return super.onOptionsItemSelected(item);
    }
    
//    public void initButton(){
//		FrameLayout frameLayout = (FrameLayout)findViewById(android.R.id.content);
//    	View view  = frameLayout.getChildAt(0);
//    	if(view instanceof LinearLayout){
//    		((LinearLayout)view).addView(button, 1);
//    	}
//    }
	public static ManagePermissionsInfoActivity managePermissionsInfoActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		managePermissionsInfoActivity = this;
        button = new Button(this);
    	button.setText(R.string.permissions);
    	button.setVisibility(View.GONE);
    	button.setOnClickListener(this);
//    	initButton();
        if (savedInstanceState != null) {
            return;
        }

        Fragment fragment;
        String action = getIntent().getAction();

        boolean isShowHeader = false;
        switch (action) {
            case MANAGE_PERMISSIONS: {
            	isShowHeader = true;
            	button.setVisibility(View.VISIBLE);
                fragment = ManageApplications.newInstance();
            } break;

            case MANAGE_APP_PERMISSIONS: {
                String packageName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
                if (packageName == null) {
                    Log.i(LOG_TAG, "Missing mandatory argument EXTRA_PACKAGE_NAME");
                    finish();
                    return;
                }
                fragment = AppPermissionsFragmentSelect.newInstance(packageName);
            } break;

            case MANAGE_PERMISSION_APPS: {
                String permissionName = getIntent().getStringExtra(Intent.EXTRA_PERMISSION_NAME);
                if (permissionName == null) {
                    Log.i(LOG_TAG, "Missing mandatory argument EXTRA_PERMISSION_NAME");
                    finish();
                    return;
                }
                fragment = PermissionAppsFragmentSelect.newInstance(permissionName);
            } break;

            default: {
                Log.w(LOG_TAG, "Unrecognized action " + action);
                finish();
                return;
            }
        }

        getFragmentManager().beginTransaction().replace(com.mst.R.id.content, fragment).commit();
        
        if(isShowHeader) {
        	initSpinnerHeader();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		managePermissionsInfoActivity = null;
	}

	public void checkAndDestroy(String packageName){
		String packageNameActivity = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
		if (packageNameActivity != null && packageName.indexOf(packageNameActivity)>0) {
			finish();
		}
	}
	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mObscuredTouch = (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0 ;
        mPartiallyObscuredTouch=(event.getFlags() & MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED) != 0;
        /* MODIFIED-END by Ding Tang,BUG-2162981*/
        return super.dispatchTouchEvent(event);
    }

    public void showOverlayDialog() {
        startActivity(new Intent(this, OverlayWarningDialog.class));
    }

	@Override
	public void onClick(View v) {
		Fragment fragment = null;
		if(((Button)v).getText().toString().equals(getResources().getString(R.string.permissions))){
			fragment = ManagePermissionsFragment.newInstance();
			((Button)v).setText(R.string.apps);
		}else{
			fragment = ManageApplications.newInstance();
			((Button)v).setText(R.string.permissions);
		}
		
		FragmentTransaction transaction =getFragmentManager().beginTransaction();
    	transaction.replace(com.mst.R.id.content, fragment);
    	//提交修改
    	transaction.commit();
    	button.setVisibility(View.VISIBLE);
	}
	
	private void initSpinnerHeader() {
		mInflater = getLayoutInflater();
		View container = mInflater.inflate(R.layout.preference_container, null);
		View headerView = mInflater.inflate(R.layout.permission_header, (ViewGroup)container);
		mSpinnerTitle = (TextView)headerView.findViewById(R.id.spinner_header);
		
		FrameLayout frameLayout = (FrameLayout)findViewById(android.R.id.content);
    	View view  = frameLayout.getChildAt(0);
    	if(view instanceof LinearLayout){
    		((LinearLayout)view).addView(headerView, 1);
    	}	
    	
    	mSpinnerTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				showSelectOrderTypeDialog();
					
				PopupMenu popupMenu = new PopupMenu(ManagePermissionsInfoActivity.this, mSpinnerTitle, Gravity.START);
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {

						int position = 0;
						switch (item.getItemId()) {
						case R.id.sort_by_apps:
							position = 0;
							break;
						case R.id.sort_by_permission:
							position = 1;
							break;
						}
						
						if(position != getSelectedSortTypePosition()){
							onShowTypeToggle(position);
						}

						return true;
					}
				});
				popupMenu.inflate(R.menu.permission_sort_type);
				popupMenu.show();
			}
		});
	}
	
	private void showSelectOrderTypeDialog(){
    	List<String> typelist = new ArrayList<>();
    	typelist.add(getString(R.string.apps));
    	typelist.add(getString(R.string.permissions));

    	View mRootView = mInflater.inflate(R.layout.dialog_layout, null);
    	final SingleChoicePopWindow mSingleChoicePopWindow = new SingleChoicePopWindow(this, mRootView, typelist);
    	mSingleChoicePopWindow.setListner(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if(position != getSelectedSortTypePosition()){
					onShowTypeToggle(position);
				}
				mSingleChoicePopWindow.show(false);
			}
		});
    	mSingleChoicePopWindow.setTitle(getString(R.string.permission_select_show_type));
    	mSingleChoicePopWindow.setSelectItem(getSelectedSortTypePosition());
    	mSingleChoicePopWindow.show(true);
    }
	
	private int getSelectedSortTypePosition (){
		int result = 0;
		if(mSpinnerTitle.getText().toString().equals(getResources().getString(R.string.permissions))) {
			result = 1;
		}
		return result;
	}
	
	private void onShowTypeToggle(int position) {
		Fragment fragment = null;
		if(position == 0) {
			mSpinnerTitle.setText(R.string.apps);
			fragment = ManageApplications.newInstance();
		} else {
			mSpinnerTitle.setText(R.string.permissions);
			fragment = ManagePermissionsFragment.newInstance();
		}
		
		FragmentTransaction transaction =getFragmentManager().beginTransaction();
    	transaction.replace(com.mst.R.id.content, fragment);
    	//提交修改
    	transaction.commit();		
	}
	
	protected void onMyActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == 0 && resultCode == Activity.RESULT_OK) {
    		setResult(Activity.RESULT_OK);
    		finish();
    		return;
    	}
	}
}
