package com.android.camera.permission;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import com.android.camera.debug.Log;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.QuickActivity;
import com.tct.camera.R;

/**
 * Created by Sean Scott on 8/31/16.
 */
public class PermissionActivity extends QuickActivity
        implements WelcomeScreen.Listener, RationaleScreen.Listener {

    private static final Log.Tag TAG = new Log.Tag("PermissionActivity");

    private Handler mHandler;
    private boolean mPaused;
    private int mRequestCode;
    private boolean mRationaleDefault;

    private WelcomeScreen mWelcomeScreen;
    private RationaleScreen mRationaleScreen;
    private Fragment mCurrentFragment;

    private final int REQUEST_DELAY = 1000;

    private static final int PREPARE = 0;
    private static final int REQUEST = 1;
    private static final int RATIONALIZE = 2;
    private static final int GO_SETTINGS = 3;
    private static final int FINISH = 4;

    private int mState;
    private void setState(int state) {
        mState = state;
    }

    // Mark previous state.
    private final String PREVIOUS_STATE = "previous_state";

    // If it's true, ignore the permission checking intent and finish.
    private boolean mIgnore = false;

    @Override
    public void onLoadComplete() {
        if (!mPaused && mRequestCode > 0) {
            mHandler.postDelayed(permissionRequestRunnable, REQUEST_DELAY);
        }
    }

    @Override
    public void onExitClicked() {
        if (mState != RATIONALIZE) {
            if (PermsInfo.DEBUG) {
                Log.e(TAG, "Exit button clicked when state is " + mState);
            }
            return;
        }

        setState(FINISH);
        returnResult(PermsInfo.RESULT_CRITICAL_DENIED);
    }

    @Override
    public void onSettingsClicked() {
        if (mState != RATIONALIZE) {
            if (PermsInfo.DEBUG) {
                Log.e(TAG, "Settings button clicked when state is " + mState);
            }
            return;
        }

        // Goto phone settings.
        setState(GO_SETTINGS);
        PermissionUtil.gotoSettings(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PREVIOUS_STATE, mState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int previousState = savedInstanceState.getInt(PREVIOUS_STATE, -1);
        if (previousState == GO_SETTINGS) {
            // If the previous state got here is GO_SETTINGS, perhaps user has denied one or more
            // granted permissions in settings and application was killed. In this case I'd like
            // to check permissions again here.
            boolean isCriticalLimited = ((mRequestCode & PermsInfo.REQUEST_LOCATION) == 0);
            mRequestCode = PermissionUtil.getRequestCode(this, isCriticalLimited);
            if (mRequestCode == 0) {
                onPermissionGranted();
            } else if (mRequestCode == PermsInfo.REQUEST_LOCATION) {
                onNonCriticalPermissionDenied();
            } else {
                onCriticalPermissionDenied();
            }
        }
    }

    @Override
    protected void onCreateTasks(Bundle savedInstanceState) {
        super.onCreateTasks(savedInstanceState);
        setContentView(R.layout.permission);

        ComponentName current = CameraUtil.getComponentNameByOrder(this, 0);
        if (current != null &&
                current.getClassName().equalsIgnoreCase(this.getLocalClassName())) {
            // Method getComponentNameByOrder return the baseActivity in order, and CameraActivity
            // was killed already if PermissionActivity is the first activity in the task. Ignore
            // the permission request and just finish.
            mIgnore = true;
            finish();
            return;
        }

        setState(PREPARE);
        mHandler = new Handler(getMainLooper());
        Intent mIntent = getIntent();
        mRequestCode = mIntent.getIntExtra(PermsInfo.TAG_REQUEST_CODE, 0);
        mRationaleDefault = mIntent.getBooleanExtra(PermsInfo.TAG_RATIONALIZE, false);
    }

    @Override
    protected void onResumeTasks() {
        super.onResumeTasks();
        mPaused = false;

        if (mIgnore) {
            return;
        }

        switch (mState) {
            case PREPARE:
                if (mRationaleDefault) {
                    setState(RATIONALIZE);
                    transactToScreen(getRationaleScreen());
                } else if (mCurrentFragment != null && mCurrentFragment instanceof WelcomeScreen) {
                    onLoadComplete();
                } else {
                    transactToScreen(getWelcomeScreen());
                }
                break;

            case RATIONALIZE:
                // It's OK to pause/resume in rationale screen.
                break;

            case GO_SETTINGS:
                if (mCurrentFragment == null || !(mCurrentFragment instanceof RationaleScreen)) {
                    if (PermsInfo.DEBUG) {
                        Log.e(TAG, "Current screen is not rational when return from Settings.");
                    }
                } else {
                    checkPermissionsAgain();
                }
                break;

            default:
                if (PermsInfo.DEBUG) {
                    Log.e(TAG, "state is " + mState + " when resume.");
                }
                break;
        }
    }

    @Override
    protected void onPauseTasks() {
        super.onPauseTasks();
        mPaused = true;
        mHandler.removeCallbacks(permissionRequestRunnable);
    }

    @Override
    public void onBackPressed() {
        if (mState == RATIONALIZE) {
            setState(FINISH);
            returnResult(PermsInfo.RESULT_CRITICAL_DENIED);
        }
    }

    private Runnable permissionRequestRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mPaused) {
                doPermissionRequest();
            }
        }
    };

    private void doPermissionRequest() {
        if (mRequestCode <= 0) {
            return;
        }

        String[] premissions = PermissionUtil.getRequestPermissions(mRequestCode, false);
        if (premissions == null || premissions.length == 0) {
            if (PermsInfo.DEBUG) {
                Log.e(TAG, "getRequestPermissions is empty");
            }
            return;
        }

        setState(REQUEST);
        if (ApiHelper.isKitKatOrHigher()) {
            requestPermissions(premissions, mRequestCode);
        } else {
            PermissionUtil.requestPermissions(this, mRequestCode, premissions);
        }
    }

    private void checkPermissionsAgain() {
        // If in GO_SETTINGS state, one or more critical permissions have been denied.
        // I need to check the critical permissions status again here. If one of them is still
        // not granted, it means that user may not grant all critical permissions in settings
        // page, and I should keep current screen(RationaleScreen). And if all critical
        // permissions are granted, before set result GRANTED I'd also check whether permissions
        // location should be checked or not depend on the request code.
        if (!PermissionUtil.isCriticalPermissionGranted(this)) {
            // Still in rationale screen, do nothing except setting the state.
            setState(RATIONALIZE);
            return;
        }

        if ((mRequestCode & PermsInfo.REQUEST_LOCATION) != 0) {
            // Permissions location have been requested.
            if (!PermissionUtil.isNoncriticalPermissionGranted(this)) {
                onNonCriticalPermissionDenied();
                return;
            }
        }

        // Permissions location are not requested this round or have been granted.
        onPermissionGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0 || permissions == null || grantResults == null ||
                permissions.length == 0 || permissions.length != grantResults.length) {
            if (PermsInfo.DEBUG) {
                Log.e(TAG, "Empty result, request may be cancelled.");
            }
            return;
        }

        if (PermsInfo.DEBUG) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i(TAG, i + " For permission " + permissions[i] + ",  grantResults is "
                        + grantResults[i]);
            }
        }

        int length = permissions.length;
        boolean criticalPermissionDenied = false;
        boolean locationPermissionDenied = false;

        for (int i = 0; i < length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (permissions[i].equalsIgnoreCase(PermsInfo.PERMS_ACCESS_COARSE_LOCATION) ||
                        permissions[i].equalsIgnoreCase(PermsInfo.PERMS_ACCESS_FINE_LOCATION)) {
                    locationPermissionDenied = true;
                } else {
                    criticalPermissionDenied = true;
                }
            }
        }

        if (criticalPermissionDenied) {
            onCriticalPermissionDenied();
        } else if (locationPermissionDenied) {
            onNonCriticalPermissionDenied();
        } else {
            onPermissionGranted();
        }
    }

    private void onCriticalPermissionDenied() {
        setState(RATIONALIZE);
        transactToScreen(getRationaleScreen());
    }

    private void onNonCriticalPermissionDenied() {
        setState(FINISH);
        returnResult(PermsInfo.RESULT_LOCATION_DENIED);
    }

    private void onPermissionGranted() {
        setState(FINISH);
        returnResult(PermsInfo.RESULT_GRANTED);
    }

    private void returnResult(int result) {
        Intent intent = new Intent();
        // In CameraActivity, requestCode may be still needed to check whether
        // permissions location have been requested or not.
        intent.putExtra(PermsInfo.TAG_REQUEST_CODE, mRequestCode);
        intent.putExtra(PermsInfo.TAG_REQUEST_RESULT, result);
        // Always set result OK in Finish state.
        this.setResult(RESULT_OK, intent);
        // Finish PermissionActivity and back to Camera.
        finish();
    }

    private void transactToScreen(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        mCurrentFragment = fragment;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out,
                R.anim.fragment_slide_in, R.anim.fragment_slide_out);
        fragmentTransaction.replace(R.id.permission, fragment);
        fragmentTransaction.commit();
    }

    private WelcomeScreen getWelcomeScreen() {
        if (mWelcomeScreen == null) {
            mWelcomeScreen = new WelcomeScreen();
            mWelcomeScreen.setListener(this);
        }
        return mWelcomeScreen;
    }

    private RationaleScreen getRationaleScreen() {
        if (mRationaleScreen == null) {
            mRationaleScreen = new RationaleScreen();
            mRationaleScreen.setListener(this);
        }
        return mRationaleScreen;
    }
}