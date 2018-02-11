package com.android.systemui.qs.customize;

import android.app.Activity;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.os.UserManager;
import android.util.Log;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.phone.FingerprintUnlockController;
import com.android.systemui.statusbar.phone.LightStatusBarController;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.VolumeComponent;

import mst.app.MstActivity;

/**
 * Created by kth on 16-9-27.
 * TCL Monster: kth add for update QS edit layout,
 * Settings invoke QS edit by intent{com.android.settings.action.CUSTOMIZE_EDIT}
 */
public class CustomizeTileActivity extends MstActivity {
    private static final java.lang.String TAG = "CustomizeTileActivity";
    BluetoothControllerImpl mBluetoothController;
    SecurityControllerImpl mSecurityController;
    protected BatteryController mBatteryController;
    LocationControllerImpl mLocationController;
    NetworkControllerImpl mNetworkController;
    HotspotControllerImpl mHotspotController;
    RotationLockControllerImpl mRotationLockController;
    UserInfoController mUserInfoController;
    protected ZenModeController mZenModeController;
    CastControllerImpl mCastController;
    VolumeComponent mVolumeComponent;
    KeyguardUserSwitcher mKeyguardUserSwitcher;
    FlashlightController mFlashlightController;
    protected UserSwitcherController mUserSwitcherController;
    NextAlarmController mNextAlarmController;
    protected KeyguardMonitor mKeyguardMonitor;
    BrightnessMirrorController mBrightnessMirrorController;
    AccessibilityController mAccessibilityController;
    FingerprintUnlockController mFingerprintUnlockController;
    LightStatusBarController mLightStatusBarController;

    private HandlerThread mHandlerThread;
    private MstQSCustomizer edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CustomizeTileActivity", "onCreate: ");

        setMstContentView(R.layout.customize_tile_activity);
        this.setTitle(R.string.qs_edit);
        edit = (MstQSCustomizer) findViewById(R.id.customize_tile_edit);

        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        mBluetoothController = new BluetoothControllerImpl(this, mHandlerThread.getLooper());
        mLocationController = new LocationControllerImpl(this,
                mHandlerThread.getLooper()); // will post a notification
        if (this.getResources().getBoolean(R.bool.config_showRotationLock)) {
            mRotationLockController = new RotationLockControllerImpl(this);
        }
        mNetworkController = new NetworkControllerImpl(this, mHandlerThread.getLooper());
//        mNetworkController.setUserSetupComplete(mUserSetup);
        mHotspotController = new HotspotControllerImpl(this);
        mCastController = new CastControllerImpl(this);

        mSecurityController = new SecurityControllerImpl(this);
        mFlashlightController = new FlashlightController(this);
//        mUserSwitcherController
        mUserInfoController = new UserInfoController(this);
        mSecurityController = new SecurityControllerImpl(this);
        mBatteryController = new BatteryControllerImpl(this);
        mNextAlarmController = new NextAlarmController(this);

        final QSTileHost qsh = SystemUIFactory.getInstance().createQSTileHost(this, null/*phonestatusbar*/,
                mBluetoothController, mLocationController, mRotationLockController,
                mNetworkController, mZenModeController, mHotspotController,
                mCastController, mFlashlightController,
                mUserSwitcherController, mUserInfoController, null/*mKeyguardMonitor*/,
                mSecurityController, mBatteryController, null/*mIconController*/,
                mNextAlarmController);

        Log.d(TAG, "onCreate: qsh=" + qsh + "--edit=" + edit);
        edit.setHost(qsh);

        edit.show(0, 0);
    }


    @Override
    public void onNavigationClicked(View v) {
        edit.hide(0,0);
        finish();
    }
}
