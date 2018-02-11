package com.monster.paymentsecurity.util;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.db.PayListDao;
import com.monster.paymentsecurity.diagnostic.RiskOrError;
import com.monster.paymentsecurity.scan.BaseScanTask;
import com.monster.paymentsecurity.tmsdk.PaymentSecureService;
import com.monster.paymentsecurity.tmsdk.TMSApplicationConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;

import tmsdk.common.TMSDKContext;

/**
 * 工具
 * Created by logic on 16-8-1.
 */
public class Utils {

    public static boolean initTMSDK(Context context){
        if (!TMSDKContext.isInitialized()){
            TMSDKContext.setTMSDKLogEnable(false);
            String version = TMSDKContext.getSDKVersionInfo();
            long start = System.currentTimeMillis();
            TMSDKContext.setAutoConnectionSwitch(false);
            boolean mBresult = TMSDKContext.init(context.getApplicationContext(), PaymentSecureService.class, new TMSApplicationConfig());
            long end = System.currentTimeMillis();
            Log.v("TMSDKInit", "TMSDK init spend = " + (end - start) + ", result =" + mBresult + ", version = " + version);
            return mBresult;
        }
        return true;
    }

    //
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    //获取系统framework版本
    public static String getSystemVersion() {
        String sysVer = SystemProperties.get("ro.tct.sys.ver");
        if (!TextUtils.isEmpty(sysVer)) {
            return sysVer.substring(1, 4) +
                    sysVer.substring(6, 7) +
                    sysVer.substring(11, 12) +
                    sysVer.substring(11, 12) +
                    sysVer.substring(6, 8);
        }
        return "";
    }

    public static boolean isWifiOk(Context context) {
        if (context == null)
            return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo == null || !ConnectivityManager.isNetworkTypeWifi(nInfo.getType()))
            return false;
        return  nInfo.isAvailable() && nInfo.isConnected();
    }


    public static boolean isNetworkOk(Context context) {
        if (context == null)
            return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        return (nInfo != null ) && nInfo.isAvailable() && nInfo.isConnected();
    }


    public static String getRiskCategoryTitle(@BaseScanTask.ScanType int scanType){

        if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV){
            return "支付应用环境监测";
        }else if (scanType == BaseScanTask.SCAN_TYPE_SMS_APP){
            return "发现短信风险１个";
        }else if (scanType == BaseScanTask.SCAN_TYPE_WIFI_SECURITY ||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_DNS||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_ARP){
            return "发现Wi-Fi安全风险";
        }else if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE){
            return "发现系统新版本";
        }
        return "";
    }

    public static int getRiskCategoryLogo(@BaseScanTask.ScanType int scanType) {
        if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV){
            return R.drawable.payment_env_risk_logo;
        }else if (scanType == BaseScanTask.SCAN_TYPE_SMS_APP){
            return R.drawable.mms_risk_logo;
        }else if (scanType == BaseScanTask.SCAN_TYPE_WIFI_SECURITY ||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_DNS||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_ARP){
            return R.drawable.wifi_risk_logo;
        }else if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE){
            return R.drawable.system_update_risk_logo;
        }
        return -1;
    }

    public static String getRiskTitle(Context context, RiskOrError risk){
        @BaseScanTask.ScanType int scanType = risk.getScanType();
        if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV){
            return context.getString(R.string.payment_app_env_detected_title);
        }else if (scanType == BaseScanTask.SCAN_TYPE_SMS_APP){
            return context.getString(R.string.mms_risk_detected_title);
        }else if (scanType == BaseScanTask.SCAN_TYPE_WIFI_SECURITY ||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_DNS||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_ARP){
            return context.getString(R.string.wifi_risk_detected_title, getCurrentWiFiName(context));
        }else if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE){
            return context.getString(R.string.system_upgrade_risk_detected_title, getSystemVersion());
        }
        return "";
    }

    public static String getCurrentWiFiName(Context context){
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifiMgr.getConnectionInfo();

        return info != null ? info.getSSID() : "";
    }

    public static String getRiskSummary(Context context, @BaseScanTask.ScanType int scanType) {

        if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV){
            return context.getString(R.string.payment_app_env_risk_detected_summary);
        }else if (scanType == BaseScanTask.SCAN_TYPE_SMS_APP){
            return context.getString(R.string.mms_risk_detected_summary);
        }else if (scanType == BaseScanTask.SCAN_TYPE_WIFI_SECURITY ||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_DNS||
                scanType == BaseScanTask.SCAN_TYPE_WIFI_ARP){
            return context.getString(R.string.wifi_risk_detected_summary);
        }else if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE){
            return context.getString(R.string.system_upgrade_risk_detected_summary);
        }
        return "";
    }

    public static String getSuggestText(Context context, @BaseScanTask.ScanType int scanType){
        if (scanType == BaseScanTask.SCAN_TYPE_SMS_APP){
           return   context.getString(R.string.restore_now);
        }else if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV){
           return context.getString(R.string.open_now);
        }else if (scanType == BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE){
           return context.getString(R.string.upgrade_now);
        } else {
           return context.getString(R.string.stop_now);
        }
    }

    public static boolean showPayListCard(Context context){
        Context context1 = context.getApplicationContext();
        PayListDao dao = new PayListDao(context1);
        return SettingUtil.isPayAppDetectionEnable(context1) && dao.getPayList(true).size() > 0;
    }

    public static  String formatTime(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return sdf.format(new Date(time));
    }

    /**
     *  字符串md5加密
     *
     * @param str
     * @return
     */
    public static String md5str(String str)
    {
        byte[] digest = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("md5");
            digest = md.digest(str.getBytes());
            return bytes2hex02(digest);

        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * byte to hex
     *
     * @param bytes bytes
     * @return
     */
    private static String bytes2hex02(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes)
        {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1)// 每个字节8为，转为16进制标志，2个16进制位
            {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }

        return sb.toString();

    }
}
