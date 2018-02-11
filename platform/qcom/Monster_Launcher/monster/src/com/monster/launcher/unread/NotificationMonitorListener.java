package com.monster.launcher.unread;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.monster.launcher.Launcher;
import com.monster.launcher.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by antino on 16-7-13.
 */
@SuppressLint("OverrideAbstract")
public class NotificationMonitorListener extends NotificationListenerService {
    private static final String TAG = "--lijun--NotificationMonitorListener";
    public static final String NOTIFICATION_CONTROL_ACTION = "com.monster.launcher.notification_control";
    HashMap sendMap;
    private NotificationReceiver mReceiver = new NotificationReceiver();
    @Override
    public void onCreate() {
        super.onCreate();
        sendMap = new HashMap();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_CONTROL_ACTION);
        registerReceiver(mReceiver, filter);
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        refrushNotifications();
        Log.i(TAG, "onListenerConnected");
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        super.onInterruptionFilterChanged(interruptionFilter);
        Log.i(TAG, "onInterruptionFilterChanged");
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        Log.i(TAG, "onNotificationRankingUpdate sbn = " + rankingMap.toString());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        try {
            Bundle extras = null;
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                Log.i(TAG, "onNotificationPosted return as version min ");
                return;
            } else {
                extras = sbn.getNotification().extras;
//                String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
//                Bitmap notificationLargeIcon = ((Bitmap)
//                        extras.getParcelable(Notification.EXTRA_LARGE_ICON)); //这个可能为空，得注意
//                CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
//                CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);//这个可能为空，得注意
                Notification mNotification = sbn.getNotification();
//                PendingIntent pi = mNotification.contentIntent;//获取通知点击时相应的intent
                String key = sbn.getKey();//这方法只在api level>21时可以用，即android5.0以上
//                int id = sbn.getId();//对应发送Notification时传入的id
                String tag = sbn.getTag();//发送Notification时传入的tag，可能为空，一般都没人传这个参数
                String pkg_name = sbn.getPackageName();//发送这条通知的应用包名
                boolean isClearable = sbn.isClearable();//这条通知是否可以被清除
//                long postTime = sbn.getPostTime();//通知发送时间
                UserHandle user = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    user = sbn.getUser();
                }
//                Log.d(TAG, "onNotificationPosted title:" + notificationTitle +
//                        "  text:" + notificationSubText + "  subText:"
//                        + notificationSubText + "  key:" + key + "  id:"
//                        + id + "  tag:" + tag + "  pakageName:" + pkg_name +
//                        "  isClearable:" + isClearable + "  postTime:" + postTime+"  user:"+user);

                if (isClearable) {
                    Intent intent = new Intent();
                    intent.setAction(Launcher.INTENT_ACTION_UNREAD_CHANGE);
                    intent.putExtra(Launcher.EXTRA_UNREAD_NUMBER, MonsterUnreadLoader.UNREAD_NUMBER_ADD);
                    intent.putExtra(Launcher.EXTRA_UNREAD_COMPONENT, new ComponentName(pkg_name, "null"));
                    intent.putExtra(Launcher.EXTRA_UNREAD_USER, user);
                    sendBroadcast(intent);
                    if (sendMap == null) {
                        sendMap = new HashMap();
                    }
                    if (!sendMap.containsKey(key)) {
                        sendMap.put(key, 1);
                    } else {
                        sendMap.put(key, (int) sendMap.get(key) + 1);
                    }
                }

            }
        } catch (Exception e) {
            Log.d(TAG, "onNotificationPosted some error : " + e);
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        try {
            Bundle extras = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Log.i(TAG, "onNotificationRemoved return as version min ");
                return;
            } else {
                extras = sbn.getNotification().extras;
//                String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
//                Bitmap notificationLargeIcon = ((Bitmap)
//                        extras.getParcelable(Notification.EXTRA_LARGE_ICON)); //这个可能为空，得注意
//                CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
//                CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);//这个可能为空，得注意
                Notification mNotification = sbn.getNotification();
//                PendingIntent pi = mNotification.contentIntent;//获取通知点击时相应的intent
                String key = sbn.getKey();//这方法只在api level>21时可以用，即android5.0以上
                int id = sbn.getId();//对应发送Notification时传入的id
                String tag = sbn.getTag();//发送Notification时传入的tag，可能为空，一般都没人传这个参数
                String pkg_name = sbn.getPackageName();//发送这条通知的应用包名
                boolean isClearable = sbn.isClearable();//这条通知是否可以被清除
//                long postTime = sbn.getPostTime();//通知发送时间
                UserHandle user = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    user = sbn.getUser();
                }
//                Log.d(TAG, "onNotificationRemoved title:" + notificationTitle +
//                        "  text:" + notificationSubText + "  subText:"
//                        + notificationSubText + "  key:" + key + "  id:"
//                        + id + "  tag:" + tag + "  pakageName:" + pkg_name +
//                        "  isClearable:" + isClearable + "  postTime:" + postTime);
                if (isClearable) {
                    int unread;
                    if (sendMap == null) {
                        sendMap = new HashMap();
                        unread = 0;
                    } else {
                        unread = (int) sendMap.get(key);
                    }
                    sendMap.remove(key);
                    Intent intent = new Intent();
                    intent.setAction(Launcher.INTENT_ACTION_UNREAD_CHANGE);
                    intent.putExtra(Launcher.EXTRA_UNREAD_NUMBER, MonsterUnreadLoader.UNREAD_NUMBER_REMOVE_WITHKEY);
                    intent.putExtra(Launcher.EXTRA_UNREAD_NUMBER_REMOVE, unread);
                    intent.putExtra(Launcher.EXTRA_UNREAD_COMPONENT, new ComponentName(pkg_name, "null"));
                    intent.putExtra(Launcher.EXTRA_UNREAD_USER, user);
                    sendBroadcast(intent);
                }

            }
        } catch (Exception e) {
            Log.d(TAG, "onNotificationRemoved some error : " + e);
            e.printStackTrace();
            return;
        }
    }

    private boolean isMissCallNotification(String pkg_name) {
        if ("com.android.server.telecom".equals(pkg_name)) {
            return true;
        }
        return false;
    }

    private int hadSend(String key) {
        if (sendMap == null) return 0;
        if (sendMap.containsKey(key)) {
            return (int) sendMap.get(key);
        }
        return 0;
    }

    private void refrushNotifications() {
        try {
            sendMap.clear();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                StatusBarNotification[] notifications = getActiveNotifications();
                Bundle extras = null;
                if (notifications == null) return;
                for (StatusBarNotification sbn : notifications) {
                    extras = sbn.getNotification().extras;
//                    String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
//                    Bitmap notificationLargeIcon = ((Bitmap)
//                            extras.getParcelable(Notification.EXTRA_LARGE_ICON)); //这个可能为空，得注意
//                    CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
//                    CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);//这个可能为空，得注意
                    Notification mNotification = sbn.getNotification();
//                    PendingIntent pi = mNotification.contentIntent;//获取通知点击时相应的intent
                    String key = sbn.getKey();//这方法只在api level>21时可以用，即android5.0以上
                    int id = sbn.getId();//对应发送Notification时传入的id
//                    String tag = sbn.getTag();//发送Notification时传入的tag，可能为空，一般都没人传这个参数
                    String pkg_name = sbn.getPackageName();//发送这条通知的应用包名
                    boolean isClearable = sbn.isClearable();//这条通知是否可以被清除
//                    long postTime = sbn.getPostTime();//通知发送时间
                    UserHandle user = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        user = sbn.getUser();
                    }
//                    Log.d(TAG, "onNotificationPosted title:" + notificationTitle +
//                            "  text:" + notificationSubText + "  subText:"
//                            + notificationSubText + "  key:" + key + "  id:"
//                            + id + "  tag:" + tag + "  pakageName:" + pkg_name +
//                            "  isClearable:" + isClearable + "  postTime:" + postTime);

                    if (isClearable) {
                        Intent intent = new Intent();
                        intent.setAction(Launcher.INTENT_ACTION_UNREAD_CHANGE);
                        intent.putExtra(Launcher.EXTRA_UNREAD_NUMBER, MonsterUnreadLoader.UNREAD_NUMBER_ADD);
                        intent.putExtra(Launcher.EXTRA_UNREAD_COMPONENT, new ComponentName(pkg_name, "null"));
                        intent.putExtra(Launcher.EXTRA_UNREAD_USER, user);
                        sendBroadcast(intent);
                        if (!sendMap.containsKey(key)) {
                            sendMap.put(key, 1);
                        } else {
                            sendMap.put(key, (int) sendMap.get(key) + 1);
                        }
                    }

                }
            }
        } catch (Exception e) {
            Log.d(TAG, "refrushNotifications some error : " + e);
            e.printStackTrace();
            return;
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NOTIFICATION_CONTROL_ACTION.equals(action)) {
                refrushNotifications();
            }
        }
    }
}
