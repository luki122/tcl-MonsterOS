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
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.monster.cloud.ICallBack;
import com.monster.cloud.ProgressConnection;
import com.monster.cloud.R;
import com.monster.cloud.activity.MainActivity;
import com.monster.cloud.activity.contacts.SyncContactsActivity;
import com.monster.cloud.activity.sms.SyncSmsActivity;
import com.monster.cloud.constants.Constant;
import com.monster.cloud.sync.BaseSyncTask;
import com.monster.cloud.sync.SyncHelper;
import com.monster.cloud.sync.TCLSyncManager;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.monster.cloud.utils.SystemUtil;

import java.util.Calendar;

/**
 * Created by yubai on 16-11-8.
 */
public class SyncService extends Service implements TCLSyncManager.SyncMgrStateObserver {

    private static final String TAG = "SyncService";

    private PendingIntent pendingIntent;
    private AlarmReceiver receiver;

    private NotificationManager notificationManager;

    //TODO
    private boolean isOneStep;
    private boolean isUIVisible = true;


    private TCLSyncManager mTCLSyncManager;
    private TCLSyncManager.SyncTaskProgressObserver mSyncTaskProgressObserver;
    private @TCLSyncManager.MgrState int mgrState;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e(TAG, "SyncService onCreate");
        // sdk login
        if (LoginUtil.isQQLogIn(this)) {
            SyncHelper.qqSdkLogin(this);
        }

        mTCLSyncManager = new TCLSyncManager(this);
        mTCLSyncManager.setMgrStateObserver(this);
        mTCLSyncManager.start();
        mSyncTaskProgressObserver = new TCLSyncManager.SyncTaskProgressObserver() {
            @Override
            public void onSyncProgressChange(BaseSyncTask task, int progress) {
                Log.e("BaseSyncTask", "onSyncProgressChange");
                updateProgress(task.getTaskType(), progress);
                if (!isUIVisible) {
                    setNotificationContent(task.getTaskType(), progress);
                }
            }

            @Override
            public void onSyncTaskState(BaseSyncTask task, @TCLSyncManager.TaskState int taskState) {
                // TODO: 16-12-16
                if (taskState == TCLSyncManager.TASK_STATE_FINSIHED
                        /*&& task.getResultCode() == BaseSyncTask.SYNC_ERR_TYPE_SUCCEED*/){
                    notifyCallbackSyncTaskFinished(task.getTaskType());

                    Log.e("BaseSyncTask", "task remains: " + mTCLSyncManager.getTasksNum());
                    if (mTCLSyncManager.getTasksNum() <= 0) {
                        notifyCallbackSyncTaskAllFinished();
                    }

                } else if (taskState == TCLSyncManager.TASK_STATE_BEGIN){
                    // TODO: 16-12-16 任务开始 e.g. MainActivity adapter change

                }
            }
        };

        receiver = new AlarmReceiver();
        IntentFilter filter = new IntentFilter(Constant.AUTO_SYNC_ALL);
//        filter.addAction(Constant.AUTO_SYNC_ALL);
        registerReceiver(receiver, filter);

        // set AlarmManager to synchronize periodically
//        Intent alarmIntent = new Intent(this, AlarmReceiver.class); only use when AlarmReceiver registered static
        Intent alarmIntent = new Intent(Constant.AUTO_SYNC_ALL);
        alarmIntent.setAction(Constant.AUTO_SYNC_ALL);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        setAlarmManager();

        //set content observer
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "SyncService onDestroy");
        mTCLSyncManager.stop();
        unregisterReceiver(receiver);
        cancelAlarm();
        getContentResolver().unregisterContentObserver(observer);
        Runtime.getRuntime().gc();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void setAlarmManager() {
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);
//        calendar.add(Calendar.SECOND, 30);

//        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 10, pendingIntent);
//        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        //API level > 19 use setExact
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    private RemoteCallbackList<ICallBack> mCallbacks = new RemoteCallbackList<>();

    ProgressConnection.Stub mBinder = new ProgressConnection.Stub() {
        @Override
        public void registerCallback(ICallBack callback) throws RemoteException {
            if (callback != null) {
                Log.e("BaseSyncTask", "service register...");
                mCallbacks.register(callback);
            }
        }

        @Override
        public void unregisterCallback(ICallBack callback) throws RemoteException {
            if (callback != null) {
                Log.e("BaseSyncTask", "service unregister...");
                mCallbacks.unregister(callback);
            }
        }

        @Override
        public void startSynchronize(@BaseSyncTask.SyncTaskType int type, boolean isOneStep) throws RemoteException {
            //set if to synchronize all
            SyncService.this.isOneStep = isOneStep;
            if (mgrState == TCLSyncManager.MGR_STATE_STOP){
                mTCLSyncManager.start();
            }
            if (isOneStep) {
                mTCLSyncManager.onekeySync(mSyncTaskProgressObserver);
            } else {
                mTCLSyncManager.syncSignleTask(type, mSyncTaskProgressObserver);
            }
        }

        @Override
        public void stopSynchronize() throws RemoteException {
            mTCLSyncManager.stop(mSyncTaskProgressObserver);
            notifyCallbackSyncTaskAllFinished();
        }

        @Override
        public void notifyServiceOnStop() throws RemoteException {
            isUIVisible = false;
            actionProgressNotification();
        }

        @Override
        public void notifyServiceOnStart() throws RemoteException {
            isUIVisible = true;
            //cancel notification
            if (mBuilder != null) {
                NotificationManagerCompat.from(SyncService.this).cancel(1234);
                mBuilder = null;
            }
        }
    };

    private void notifyCallbackSyncTaskFinished(@BaseSyncTask.SyncTaskType int taskType) {
        int size = mCallbacks.beginBroadcast();
        try {
            for (int i = 0; i < size; ++i) {
                mCallbacks.getBroadcastItem(i).notifyCurrentSyncFinished(taskType);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mCallbacks.finishBroadcast();
    }

    private void notifyCallbackSyncTaskAllFinished() {
        int size = mCallbacks.beginBroadcast();
        try {
            for (int i = 0; i < size; ++i) {
                mCallbacks.getBroadcastItem(i).notifyAllSyncFinished();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mCallbacks.finishBroadcast();
    }

    private void updateProgress(@BaseSyncTask.SyncTaskType int taskType, int progress) {
        int size = mCallbacks.beginBroadcast();
//        Log.e("BaseSyncTask", "updateProgress mCallbacks size: " + size);
        try {
            for (int i = 0; i < size; ++i) {
                mCallbacks.getBroadcastItem(i).updateProgress(taskType, progress);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mCallbacks.finishBroadcast();
    }

    NotificationCompat.Builder mBuilder = null;
    private void actionProgressNotification() {
        Intent intent = null;
        if (isOneStep || SyncHelper.isWifiSyncEnable(this)) {
            intent = new Intent(this, MainActivity.class);

        } else {
            // TODO: 16-12-23
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mBuilder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setContentIntent(pIntent)
                    .setSmallIcon(R.drawable.cloud_icon);

        }
    }

    private void setNotificationContent(int taskType, int progress) {
        if (mBuilder != null) {
            mBuilder.setContentText(getString(R.string.is_synchonizing));
            mBuilder.setProgress(100, progress, false);
            switch (taskType) {
                case BaseSyncTask.TASK_TYPE_SYNC_CONTACT:
                    mBuilder.setContentTitle(getString(R.string.contact));
                    break;
                case BaseSyncTask.TASK_TYPE_SYNC_SMS:
                    mBuilder.setContentTitle(getString(R.string.message));
                    break;
                case BaseSyncTask.TASK_TYPE_SYNC_CALLLOG:
                    mBuilder.setContentTitle(getString(R.string.call_log));
                    break;
                case BaseSyncTask.TASK_TYPE_SYNC_SOFT:
                    mBuilder.setContentTitle(getString(R.string.app_list));
                    break;
                default:
                    break;
            }
            notificationManager.notify(1234, mBuilder.build());
        }
    }

    @Override
    public void notifySyncMgrStateChange(@TCLSyncManager.MgrState int mgrState) {
        this.mgrState = mgrState;
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.AUTO_SYNC_ALL.equals(intent.getAction())) {
//                Log.e("BaseSyncTask", "auto sync all ");
                if (mgrState == TCLSyncManager.MGR_STATE_STOP){
                    mTCLSyncManager.start();
                }
                if (SyncHelper.isWifiSyncEnable(SyncService.this)
                        && SystemUtil.isWifiNetwork(SyncService.this)) {
                    mTCLSyncManager.backgroundSync();
                }else if(SystemUtil.isNetworkConnected(SyncService.this)){
                    mTCLSyncManager.backgroundSync();
                }
            }
        }
    }

    private ContentObserver observer = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //set data changed tag
            SyncTimeUtil.setContactChangedLabel(SyncService.this, true);
            //移除之前的消息 重新计时
            contactBackgourndSyncHandler.removeMessages(0);
            if (SyncHelper.isCTTAutoSyncEnable(SyncService.this)) {
//                Log.e(TAG, "contact has been changed and handler has been set");
                contactBackgourndSyncHandler
                        .sendMessageDelayed(Message.obtain(contactBackgourndSyncHandler, 0), 10 * 60 * 1000);
            }
        }
    };

    private Handler contactBackgourndSyncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mgrState == TCLSyncManager.MGR_STATE_STOP) {
                        mTCLSyncManager.start();
                    }
                    if (SyncHelper.isWifiSyncEnable(SyncService.this)
                            && SystemUtil.isWifiNetwork(SyncService.this)) {
                        mTCLSyncManager.syncSignleTask(BaseSyncTask.TASK_TYPE_SYNC_CONTACT, mSyncTaskProgressObserver);
                    } else if (SystemUtil.isNetworkConnected(SyncService.this)) {
                        mTCLSyncManager.syncSignleTask(BaseSyncTask.TASK_TYPE_SYNC_CONTACT, mSyncTaskProgressObserver);
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
