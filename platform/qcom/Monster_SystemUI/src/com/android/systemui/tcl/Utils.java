package com.android.systemui.tcl;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.UserHandle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.monster.launchericon.utils.PKGIcongetter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static final String ACTION_NOTIFY_BANNED_CHANGE = "action.notify_banned_change";
    public static final String ACTION_NOTIFY_SETTING_CHANGE = "action.notify_setting_change";
    public static final String ACTION_NOTIFICATION_DATA_CHANGE = "action.notification_data_change";

    /**
     * 通过包名获取应用程序的名称。
     *
     * @param context     Context对象。
     * @param packageName 包名。
     * @return 返回包名所对应的应用程序的名称。
     */

    public static String getApplicationLabel(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static String getApplicationLabelAsUser(Context context, String packageName, int uid) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfoAsUser(packageName,
                            PackageManager.GET_META_DATA, uid)).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 通过包名获取应用程序的ICON。
     */
    public static Drawable getIconByPackageName(Context context,
                                                String packageName) {
        PackageManager pm = context.getPackageManager();
        Drawable d = null;
        try {
            d = pm.getApplicationIcon(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return d;
    }


    public static void setViewShown(final View view, boolean shown, boolean animate) {
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                    shown ? android.R.anim.fade_in : android.R.anim.fade_out);
            if (shown) {
                view.setVisibility(View.VISIBLE);
            } else {
                animation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.GONE);
                    }
                });
            }
            view.startAnimation(animation);
        } else {
            view.clearAnimation();
            view.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Determine whether a package is a "system package", in which case certain things (like
     * disabling notifications or disabling the package altogether) should be disallowed.
     */
    public static boolean isSystemPackage(PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(pm)};
        }
        return sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg));
    }

    private static Signature[] sSystemSignature;

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg != null && pkg.signatures != null && pkg.signatures.length > 0) {
            return pkg.signatures[0];
        }
        return null;
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            final PackageInfo sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return getFirstSignature(sys);
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    /**
     * 获取应用图标，和launcher图标保存一致
     */
    public static Drawable getIconDrawable(Context context, String pkg) {

        Drawable drawable = null;
        try {
            drawable = PKGIcongetter.getInstance(context).getIconDrawable(pkg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (drawable == null) {
                drawable = getIconByPackageName(context, pkg);
            }
        }
        return drawable;
    }

    public static Intent getIntentByPackageName(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packagename);
        return launchIntent;
    }

    public static Notification getNotification(String pkg, CharSequence title, CharSequence text, CharSequence ticker, CharSequence subText) {
        Notification notification = null;
        try {
            Application context = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(title);
            builder.setContentText(text);
            builder.setTicker(ticker);
            builder.setSubText(subText);
            Intent launchIntent = Utils.getIntentByPackageName(context, pkg);
            if (launchIntent != null) {
                PendingIntent pIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
                builder.setContentIntent(pIntent);
            }
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
            notification = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return notification;
    }

    /**
     * 序列化对象
     *
     * @param Object
     * @return
     * @throws IOException
     */
    public static <T> String serialize(T object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        String serStr = byteArrayOutputStream.toString("ISO-8859-1");
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serStr;
    }

    /**
     * 反序列化对象
     *
     * @param str
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> T deSerialize(String str) throws IOException,
            ClassNotFoundException {
        String redStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                redStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        T person = (T) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return person;
    }

    /**
     * 获取应用通知数量
     */
    public static ContentValues getNotifyCount(Context context, String pkg) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.android.systemui.tcl.WdjNotificationProvider/count");
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        ContentValues values = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (pkg.equals(cursor.getString(cursor.getColumnIndex("package")))) {
                    values = new ContentValues();
                    values.put("package", pkg);
                    values.put("total_count", cursor.getLong(cursor.getColumnIndex("total_count")));
                    values.put("clear_count", cursor.getLong(cursor.getColumnIndex("clear_count")));
                    break;
                }
            }
            cursor.close();
        }
        return values;
    }

    /**
     * 保存应用通知数量
     *
     * @param total true-总通知数 false-被清理通知数
     */
    public static void saveNotifyCount(Context context, String pkg, boolean total) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.android.systemui.tcl.WdjNotificationProvider/count");
        ContentValues counts = getNotifyCount(context, pkg);
        long total_count = 0;
        long clear_count = 0;
        try {
            total_count = counts.getAsLong("total_count");
            clear_count = counts.getAsLong("clear_count");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ContentValues values = new ContentValues();
        if (total) {
            values.put("package", pkg);
            values.put("total_count", total_count + 1);
            values.put("clear_count", clear_count);
        } else {
            values.put("package", pkg);
            values.put("total_count", total_count);
            values.put("clear_count", clear_count + 1);
        }

        if (counts == null) {
            //数据不存在，则insert
            contentResolver.insert(uri, values);
        } else {
            contentResolver.update(uri, values, "package" + "=?", new String[]{pkg});
        }
    }

    //这些应用默认关闭桌面角标提醒
    public static List<String> getScripWhiteList() {
        List<String> list = new ArrayList<>();
        list.add("cn.tcl.note");//备忘录
        list.add("cn.tcl.meetingassistant");//会议助理
        list.add("com.android.calculator2");//计算器
        list.add("com.sohu.inputmethod.sogou");//搜狗输入法
        list.add("com.android.gallery3d");//相册
        list.add("com.tct.camera");//相机
        list.add("com.mst.thememanager");//主题商店
        return list;
    }

    public static ApplicationInfo getAppInfoByPackageName(Context context, String pkg) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_ACTIVITIES);
            return ai;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PackageInfo findPackageInfo(PackageManager pm, String pkg, int uid) {
        final String[] packages = pm.getPackagesForUid(uid);
        if (packages != null && pkg != null) {
            final int N = packages.length;
            for (int i = 0; i < N; i++) {
                final String p = packages[i];
                if (pkg.equals(p)) {
                    try {
                        return pm.getPackageInfoAsUser(pkg, PackageManager.GET_SIGNATURES, UserHandle.getUserId(uid));
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        }
        return null;
    }

}
