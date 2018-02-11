package com.monster.cloud.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.monster.cloud.R;
import com.monster.cloud.activity.MainActivity;
import com.monster.cloud.constants.Constant;
import com.monster.cloud.preferences.FilePreferences;
import com.monster.cloud.preferences.Preferences;
import com.monster.cloud.receiver.AlarmReceiver;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.monster.cloud.utils.SystemUtil;
import com.tencent.qqpim.sdk.accesslayer.LoginMgrFactory;
import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.ILoginMgr;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;
import com.tencent.qqpim.sdk.object.sms.SmsTimeType;
import com.tencent.qqpim.sdk.utils.AccountUtils;
import com.tencent.qqpim.softbox.SoftBoxProtocolModel;
import com.tencent.tclsdk.sync.SyncCallLog;
import com.tencent.tclsdk.sync.SyncContact;
import com.tencent.tclsdk.sync.SyncSMS;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yubai on 16-11-8.
 */
public class SyncService extends Service implements ISyncProcessorObsv, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SyncService";

    private FilePreferences preferences;
    private SharedPreferences sharedPreferences;

    private ILoginMgr loginMgr;
    private String token;
    private String openId;
    private String APPID = "101181845";

    private boolean isContactAutoSync, isContactSyncNow, isSyncOnlyWifi;

    private PendingIntent pendingIntent;
    private BroadcastReceiver receiver;

    private boolean[] labels;
    private int current = 0;

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e(TAG, "SyncService onCreate");
        // sdk login
        loginMgr = LoginMgrFactory.getLoginMgr(this, AccountUtils.ACCOUNT_TYPE_QQ_SDK);
        token = LoginUtil.getToken(this);
        openId = LoginUtil.getOpenId(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = loginMgr.qqSDKlogin(openId, token, APPID);
                Log.e(TAG, "result " + result);

                if (result == 5001) {
                    // token expired to pop notification
                    Log.e(TAG, "notification");
                    actionNotification();
                }
            }
        }).start();


        // set AlarmManager to synchronize periodically
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        setAlarmManager();

        // set broadcast receiver to start synchronize all
        try {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        if (Constant.AUTO_SYNC_ALL.equals(intent.getAction())) {
                            //TODO synchronize all
                            if (!isSyncOnlyWifi || SystemUtil.isWifiNetwork(SyncService.this)) {
                                checkLabel();
                            }

                        } else {
                            // DO NOTHING
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constant.AUTO_SYNC_ALL);
            registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set content observer
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer);

        //listen to the sharedPreference
        preferences = (FilePreferences) Preferences.Factory.getInstance(this, Constant.FILE_TYPE);
        sharedPreferences = preferences.getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getAllSyncAutoLabel();

        if (SyncTimeUtil.getContactChangedLabel(this)) {
            //contact sync job to do
            if (isContactAutoSync) {
                startTimer();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "SyncService onCreate");
        getContentResolver().unregisterContentObserver(observer);
        preferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSyncStateChanged(PMessage pMessage) {
        Message msg = syncHandler.obtainMessage();
        msg.what = 0;
        msg.obj = pMessage;
        syncHandler.sendMessage(msg);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        getAllSyncAutoLabel();
    }

    MainActivity.Type type;

    private Handler syncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    uiProgressChanged((PMessage) msg.obj);
                    break;
                case 1:
                    //start contact synchronize
                    isContactSyncNow = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncContact(SyncService.this, SyncService.this).sync();
                        }
                    }).start();
                    break;
                case 2:
                    //start to sync messages
                    type = MainActivity.Type.MSG_TYPE;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncSMS(SyncService.this, SyncService.this, SmsTimeType.TIME_ALL).sync();
                        }
                    }).start();
                    break;
                case 3:
                    //start to sync calllog
                    type = MainActivity.Type.RCD_TYPE;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncCallLog(SyncService.this, SyncService.this).sync();
                        }
                    }).start();
                    break;
                case 4:
                    //start to sync app list
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int result = SoftBoxProtocolModel.backupSoft(SyncService.this);
                            if (result == SoftBoxProtocolModel.RESULT_SUCCESS) {
                                // alarm cancel
                                cancel();
                                SyncTimeUtil.updateListSyncTime(SyncService.this, System.currentTimeMillis());
                            } else if (result == SoftBoxProtocolModel.RESULT_FAIL) {
                                // TODO 异常处理

                            } else if (result == SoftBoxProtocolModel.RESULT_LOGINKEY_EXPIRE) {
                                // TODO 异常处理
                            }
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        }
    };

    private void uiProgressChanged(PMessage msg) {
        switch (msg.msgId) {
            case ISyncMsgDef.ESTATE_SYNC_ALL_BEGIN:
                //同步开始（全部任务）
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_BEGIN:
                //数据库扫描开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_FINISHED:
                //数据库扫描结束
                break;
            case ISyncMsgDef.ESTATE_SYNC_PROGRESS_CHANGED:
                //同步进度变化
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_BEGIN:
                //数据同步完成，数据整理开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_FINISHED:
                //数据同步完成，数据整理完成
                break;
            case ISyncMsgDef.ESTATE_SYNC_ALL_FINISHED:
                //同步结束（全部）
                syncAllFinished(msg);
                break;
            default:
                break;
        }
    }

    private void syncAllFinished(PMessage msg) {
        List<DataSyncResult> resultList = null;
        if (null != msg.obj1) {
            resultList = (List<DataSyncResult>) msg.obj1;
        }
        if (null == resultList) {
            return;
        }
        int size = resultList.size();
        DataSyncResult result = null;

        for (int i = 0; i < size; i++) {
            result = resultList.get(i);
            if (null == result) {
                return;
            }
            switch (result.getResult()) {
                case ISyncDef.SYNC_ERR_TYPE_SUCCEED:
                    if (isContactSyncNow) {
                        //set contact change label false
                        SyncTimeUtil.setContactChangedLabel(SyncService.this, false);
                        //TODO set last sync time
                        SyncTimeUtil.updateContactSyncTime(SyncService.this, System.currentTimeMillis());
                    } else {
                        switch (type) {
                            case MSG_TYPE:
                                //TODO set last sync time
                                SyncTimeUtil.updateSmsSyncTime(SyncService.this, System.currentTimeMillis());
                                break;
                            case RCD_TYPE:
                                SyncTimeUtil.updateRecordSyncTime(SyncService.this, System.currentTimeMillis());
                                break;
                            default:
                                break;
                        }
                        current ++;
                        checkLabel();
                        if(current == 3) {
                            //表示均不进行自动更新。
                            cancel();
                        }
                    }

                    break;
                case ISyncDef.SYNC_ERR_TYPE_RELOGIN:
                    //需要重新登录
                    break;
                case ISyncDef.SYNC_ERR_TYPE_CLIENT_ERR:
                    //客户端错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_SERVER_ERR:
                    //网络错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_USER_CANCEL:
                    //用户取消
                    break;
                case ISyncDef.SYNC_ERR_TYPE_FAIL_CONFLICT:
                    //由于其他软件的同步模块正在使用导致的错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_TIME_OUT:
                    //网络超时错误
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onLoginkeyExpired() {
        actionNotification();
    }

    private ContentObserver observer = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.e(TAG, "-------contact changed-------");
            //set data changed tag
            SyncTimeUtil.setContactChangedLabel(SyncService.this, true);

            //TimerTask 如果在10mins内发生新的变化
            //当同步仅在Wifi条件下 判断是否wifi连接 如果没有 就reset timer
            if (timer != null || (isSyncOnlyWifi && !SystemUtil.isWifiNetwork(SyncService.this))) {
                stopTimer();
            }

            if (isContactAutoSync) {
                startTimer();
            }
        }
    };

    //不需要Handler 不更新UI
    private Timer timer;
    private TimerTask timerTask;

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }

        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //synchronize task
                    syncHandler.sendEmptyMessage(1);
                }
            };
        }

        if (timer != null && timerTask != null) {
            timer.schedule(timerTask, 10 * 60 * 1000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timerTask.cancel();
            timer = null;
            timerTask = null;
        }
    }

    private void setAlarmManager() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 8);
//        calendar.set(Calendar.MINUTE, 5);
        calendar.add(Calendar.SECOND, 10);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 10, pendingIntent);
    }

    private void cancel() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    private void getAllSyncAutoLabel() {
        if (SyncTimeUtil.getSyncWhenWifiLabel(this)) {
            isSyncOnlyWifi = true;
        } else {
            isSyncOnlyWifi = false;
        }

        if (SyncTimeUtil.getContactSyncLabel(this)) {
            //auto sync on
            isContactAutoSync = true;
        } else {
            //auto sync off
            isContactAutoSync = false;
        }

        labels = new boolean[3];

        if (SyncTimeUtil.getSmsSyncLabel(this)) {
            labels[0] = true;
        } else {
            labels[0] = false;
        }

        if (SyncTimeUtil.getRecordSyncLabel(this)) {
            labels[1] = true;
        } else {
            labels[1] = false;
        }

        if (SyncTimeUtil.getAppListSyncLabel(this)) {
            labels[2] = true;
        } else {
            labels[2] = false;
        }
    }

    private void checkLabel() {
        for (; current < 3; ++current) {
            if (!labels[current]) {
                continue;
            } else {
                syncHandler.sendEmptyMessage(current + 2);
                break;
            }
        }
    }

    private void actionNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(getString(R.string.qq_token_expired))
                .setContentText(getString(R.string.qq_relogin))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.cloud_icon)
                .build();
        notificationManager.notify(5001, notification);
    }

}
