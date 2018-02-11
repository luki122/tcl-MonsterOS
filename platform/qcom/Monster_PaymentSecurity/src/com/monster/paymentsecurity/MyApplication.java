package com.monster.paymentsecurity;

import android.app.Application;
import android.content.ComponentName;

import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.detection.AccessibilityUtils;
import com.monster.paymentsecurity.util.SettingUtil;

/**
 * Created by logic on 16-11-22.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (SettingUtil.isAppFirstRun(this)) {
//            Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.monster.paymentsecurity/com.monster.paymentsecurity.detection.WindowChangeDetectingService");
            AccessibilityUtils.setAccessibilityServiceState(this,ComponentName.unflattenFromString(Constant.ACCESSIBILITY_NAME),true);
            SettingUtil.setAppFirstRun(this);
        }

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
    }
}
