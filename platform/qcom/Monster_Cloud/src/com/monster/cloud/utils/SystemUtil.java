package com.monster.cloud.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.monster.cloud.CloudApplication;
import com.monster.cloud.constants.Constant;

import java.math.BigDecimal;

public class SystemUtil {

    public static final String TAG = "SystemUtil";

    public static final int REQUEST_RELOGIN_QQ = 1000;
    public static final int REQUEST_LOGIN_QQ_INIT = 1001;
    public static final int REQUEST_LOGIN_QQ_CONTACT = 1002;
    public static final int REQUEST_LOGIN_QQ_SMS = 1003;
    public static final int REQUEST_LOGIN_QQ_CALLLOG = 1004;
    public static final int REQUEST_LOGIN_QQ_APPLIST = 1005;
    public static final int REQUEST_LOGIN_QQ_ONEKEY_RECOVERY = 1006;

    /**
     * 获取imei号
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        return manager.getDeviceId();
    }

    /**
     * 是否有网络
     * @return
     */
    public static boolean hasNetwork() {
        Context context = CloudApplication.getInstance();
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState == NetworkInfo.State.CONNECTED || mobileState == NetworkInfo.State.CONNECTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 是否连接移动网络
     * @param context
     * @return
     */
    public static boolean isMobileNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if (info == null) {
            return false;
        }

        int netType = info.getType();

        // Check if Mobile Network is connected
        if (netType == ConnectivityManager.TYPE_MOBILE) {
            return info.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 是否连接WIFI网络
     * @param context
     * @return
     */
    public static boolean isWifiNetwork(Context context) {
        if (!isNetworkConnected(context)) {
            // 当前网络获取判断，如无网络连接，直接后台日志
            LogUtil.d(TAG, "isWifiNetwork None network");
            return false;
        }
        // 连接后判断当前WIFI
        if (getConnectingType(context) == Constant.NETWORK_WIFI) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        } else {
            NetworkInfo[] info = manager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取网络连接类型
     * @param context
     * @return
     */
    public static int getConnectingType(Context context) {
        ConnectivityManager mConnectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

        NetworkInfo info = mConnectivity.getActiveNetworkInfo();

        if (info == null || !mConnectivity.getBackgroundDataSetting()) {
            return -1;
        }

        int netType = info.getType();
        int netSubtype = info.getSubtype();

        if (netType == ConnectivityManager.TYPE_WIFI) {
            return Constant.NETWORK_WIFI;
        } else {
            if ((netSubtype == TelephonyManager.NETWORK_TYPE_GPRS) || (netSubtype == TelephonyManager.NETWORK_TYPE_EDGE)
                    || (netSubtype == TelephonyManager.NETWORK_TYPE_CDMA)) {
                return Constant.NETWORK_2G;
            } else {
                return Constant.NETWORK_3G;
            }
        }

    }

    /**
     * @Title: bytes2kb
     * @Description: byte转为KB或者MB字符串
     * @param @param bytes
     * @param @return
     * @return String
     * @throws
     */
    public static String bytes2kb(long bytes) {
        BigDecimal fileSize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = fileSize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "M");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "K");
    }

    public static Bitmap createCircleImage(Bitmap source, int min)
    {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        /**
         * 产生一个同样大小的画布
         */
        Canvas canvas = new Canvas(target);
        /**
         * 首先绘制圆形
         */
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        /**
         * 使用SRC_IN
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        /**
         * 绘制图片
         */
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }
}
