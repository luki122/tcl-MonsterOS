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

import com.monster.appmanager.R;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public final class ManagePermissionsActivity extends OverlayTouchActivity {
    private static final String LOG_TAG = "ManagePermissionsActivity";
	public static final String MANAGE_PERMISSIONS = "android.permission.action.MANAGE_PERMISSIONS";
	public static final String MANAGE_APP_PERMISSIONS = "android.permission.action.MANAGE_APP_PERMISSIONS";
	public static final String MANAGE_PERMISSION_APPS = "android.permission.action.MANAGE_PERMISSION_APPS";
    private boolean mObscuredTouch;
    /* MODIFIED-BEGIN by Ding Tang, 2016-05-20,BUG-2162981*/
    private boolean mPartiallyObscuredTouch;

    public boolean isObscuredTouch() {
        return mObscuredTouch;
    }

    public boolean isPartiallyObscuredTouch() {
        return mPartiallyObscuredTouch;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            return;
        }

        Fragment fragment;
        String action = getIntent().getAction();

        switch (action) {
            case MANAGE_PERMISSIONS: {
                fragment = ManagePermissionsFragment.newInstance();
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
                fragment = PermissionAppsFragment.newInstance(permissionName);
            } break;

            default: {
                Log.w(LOG_TAG, "Unrecognized action " + action);
                finish();
                return;
            }
        }

        getFragmentManager().beginTransaction().replace(com.mst.R.id.content, fragment).commit();
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
}
