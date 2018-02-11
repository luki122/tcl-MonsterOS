/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
/* MODIFIED-END by xinlei.sheng,BUG-3356295*/
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
import java.util.Timer;
import java.util.TimerTask;
/* MODIFIED-END by xinlei.sheng,BUG-3356295*/

import cn.tcl.setupwizard.LanguageSetActivity;
import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.LogUtils;

public class FinishActivity extends BaseActivity {

    public static final String TAG = "FinishActivity";

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
    private Timer timer = new Timer(true);
    private TimerTask mTimerTask;
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        findViewById(R.id.header_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent fingerIntent = new Intent(FinishActivity.this, FingerprintActivity.class);
                fingerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(fingerIntent);
            }
        });
        findViewById(R.id.finish_btn_begin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/

                /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
                timer.schedule(mTimerTask,120000);
                finishSetupWizard();
            }
        });
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                sendOneKeyChangePhoneNotify();
            }
        };
    }

    private void sendOneKeyChangePhoneNotify() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent();
        mIntent.setComponent(new ComponentName("cn.tcl.transfer","cn.tcl.transfer.activity.MainActivity"));
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent changePhonePI = PendingIntent.getActivity(this,0,mIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification changePhoneNotify = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notify_change_phone_title))
                .setContentText(getString(R.string.notify_change_phone_text))
                .setSmallIcon(R.drawable.ic_enter)
                .setContentIntent(changePhonePI)
                .setFullScreenIntent(changePhonePI,false)
                .setAutoCancel(true)
                /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
                .build();
        manager.notify(0,changePhoneNotify);
    }
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
    }

    private void finishSetupWizard() {
        // Add a persistent setting to allow other apps to know the device has been provisioned.
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
        } catch (Exception e) {
            LogUtils.i(TAG, "finishSetupWizard: " + e.toString());
        }

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, StartActivity.class); // MODIFIED by xinlei.sheng, 2016-10-13,BUG-2669930
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );

        // send the finished broadcast
        Intent finishIntent = new Intent();
        finishIntent.setAction(BaseActivity.ACTION_SETUP_FINISHED);
        sendBroadcast(finishIntent);
    }
}
