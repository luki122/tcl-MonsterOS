package com.tcl.monster.fota.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.tcl.monster.fota.FotaApp;
import com.tcl.monster.fota.R;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.model.UpdatePackageInfo;
import com.tcl.monster.fota.receiver.ActionReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

//import android.os.SystemProperties;

@SuppressLint("NewApi")
public class FotaUtil {

    private static final String TAG = "FotaUtil";
    public static final String KEY_CHECK_WIFI_ONLY = "check_wifi_only";
    public static final String KEY_DOWNLOAD_WIFI_ONLY = "download_wifi_only";
    public static final String KEY_AUTO_DOWNLOAD = "auto_download_on_wifi";
    public final static String KEY_POSTPONE_TIMES = "postponetimes";
    public static final String KEY_LOG_UPLOAD = "log_upload";
    public static final String KEY_CHECK_FREQUENCY = "auto_check_frequency";
    public final static String ROOT_UPDATE = "rootupdate";

    public static final String UPDATE_STATUS_FILE_PATH = "/cache/recovery/last_fota.status";
    public static final String FLAG_UPDATE_RESULT = "package install result";
    public static final String FLAG_UPDATE_KIND = "package install kind";
    private static final String FOTA_EVT_FULL_UPGRADE_SUCCESS = "fota_full_update_success";
    private static final String FOTA_EVT_FULL_UPGRADE_FAILURE = "fota_full_update_failure";
    public static final String FOTA_EVT_UPGRADE_SUCCESS = "fota_update_success";
    public static final String FOTA_EVT_UPGRADE_FAILURE = "fota_update_failure";
    public static final String FOTA_EVT_UPGRADE_NOSTART = "fota_update_nostart";
    public static final String FOTA_EVT_UPGRADE_ERROR = "Update.zip is not correct";
    public static final String FOTA_EVT_UPGRADE_NOT_FIND = "Can not find update.zip.";
    public static final String FOTA_EVT_UPGRADE_SIGNATURE_ERROR = "Signature verification failed";
    private static List mBaseUrlList;

    /**
     * Create a dir to save update.zip
     */
    public static void makeUpdateFolder(String sdcard) {
        File f = new File(sdcard + File.separator + FotaConstants.UPDATE_FILE_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * Delete all the files under folder which saving update package .
     */
    public static void clearUpdateFolder() {
        String path = FotaPref.getInstance(FotaApp.getApp()).getString(
                FotaConstants.PATH_SAVING_UPDATE_PACKAGE, "");
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File f = new File(path + File.separator + FotaConstants.UPDATE_FILE_DIR);
        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    public static void clearLogFolder() {
        try {
            updateLog().delete();
            crashLog().delete();
        } catch (Exception e) {
            // not care
        }
    }

    /**
     * Check the size of the storage saving update package.
     *
     * @param path
     * @return
     */
    public static long checkStorageSize(String path) {
        StatFs statfs = new StatFs(path);
        long blockSize = statfs.getBlockSizeLong();
        long availableBlock = statfs.getAvailableBlocksLong();
        long availableSize = (long) (availableBlock * blockSize) - FotaConstants.SIZE_BUFFER;
        FotaLog.d(TAG, "checkStorageSize -> " + path + " availableSize = " + availableSize);
        return availableSize;
    }

    public static long getStorageSize(String path) {
        StatFs statfs = new StatFs(path);
        long all = statfs.getTotalBytes()/1024/1024;
        return  all/10;
    }


    public static long  getAvailableStorageSize(String path) {
        StatFs statfs = new StatFs(path);
        return statfs.getAvailableBytes()/1024/1024;
    }
    /**
     * check the size of reserved space
     *
     * @param path
     * @return
     */
    public static long checkReservedStorageSize(File path) {
        android.os.StatFs statfs = new android.os.StatFs(path.getPath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            long blockSize = statfs.getBlockSizeLong();
            long availableBlock = statfs.getAvailableBlocksLong();
            long availableSize = (long) (availableBlock * blockSize)
                    - FotaConstants.SIZE_CACHE_NEED;
            FotaLog.d(TAG, path.getPath() + " , availableSize :" + availableSize);
            return availableSize;
        } else {
            long blockSize = statfs.getBlockSize();
            long availableBlock = statfs.getAvailableBlocks();
            long availableSize = (long) (availableBlock * blockSize)
                    - FotaConstants.SIZE_CACHE_NEED;
            FotaLog.d(TAG, path.getPath() + " , availableSize :" + availableSize);

            FotaLog.d(TAG, "size buffer :" + formatSize(FotaConstants.SIZE_BUFFER));
            return availableSize;
        }
    }

    /**
     * Get the user agent of the app.
     *
     * @param context
     * @return
     */
    public static String getUserAgentString(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.packageName + "/" + pi.versionName + " , Android";
        } catch (PackageManager.NameNotFoundException nnfe) {
            return null;
        }
    }

    public static int getDefaultAutoCheckVal() {

        String autoCheckInterval = FotaApp.getApp().getString(
                R.string.def_jrdfota_auto_check_interval);

		FotaLog.d(TAG, "getDefaultAutoCheckVal = " + autoCheckInterval);
        if (autoCheckInterval.equals("0")) {
            return 0;
        }
        else if (autoCheckInterval.equals("1")) {
            return 86400;
        }
        else if (autoCheckInterval.equals("7")) {
            return 604800;
        }
        else if (autoCheckInterval.equals("18")) {
            return 1209600;
        }
        else if (autoCheckInterval.equals("14")) {
            return 1209600;
        }
        else if (autoCheckInterval.equals("30")) {
            return 2419200;
        }

        return 86400;
    }

    /**
     * Sechedule check service
     *
     * @param context
     * @param updateFrequency
     */
    public static void scheduleUpdateService(Context context, long updateFrequency) {
        Intent intent = new Intent(context, ActionReceiver.class);
        intent.setAction(ActionReceiver.ACTION_AUTO_CHECK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent autoCheckIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(autoCheckIntent);
        // Load the required settings from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastCheck = prefs.getLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, 0);

		FotaLog.d(TAG, "scheduleUpdateService -> lastCheck = " + lastCheck + ", updateFrequency = "
                + updateFrequency + ", currentTime = " + System.currentTimeMillis());
		if (updateFrequency != FotaConstants.UPDATE_FREQ_NONE
				&& updateFrequency != FotaConstants.UPDATE_FREQ_AT_BOOT
				&& updateFrequency > 0) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, lastCheck + updateFrequency,
                    updateFrequency, autoCheckIntent);
        }
    }
    
	/**
     * return the size of update.zip Formats the file size as a String in kB, MB
     * or GB with a single digit of precision. Ex: 12,315,000 = 12.3 MB
     */
    public static String formatSize(float size) {
        long kb = 1024;
        long mb = (kb * 1024);
        long gb = (mb * 1024);

		String KB = FotaApp.getApp().getString(R.string.kb);
		String MB = FotaApp.getApp().getString(R.string.mb);
        if (size < mb) {
            return String.format("%.1f " + KB, size / kb);
        } else {
            return String.format("%.1f " + MB, size / mb);
        }
    }

    /**
     * Calculate percent according to downloaded size and total size
     *
     * @param downloadSize
     * @param totalSize
     * @return
     */
    public static int percentFrom(long downloadSize, long totalSize) {
        if (downloadSize <= 0 || totalSize <= 0) {
            return 0;
        }
        return (int) (downloadSize * 100 / totalSize);
    }

    /**
     * Use RootUtil to check weather the device is rooted.
     */
    public static boolean isDeviceRooted() {
        return RootUtil.isDeviceRooted();
    }

    /**
     * Current version of this device.
     *
     * @return
     */
    public static String currentVersion(Context context) {
        String versionNum = SystemProperties.get("def.tctfw.build.number");
        String version = SystemProperties.get("ro.def.software.version");
        String svn = SystemProperties.get("ro.def.software.svn");
        String countryCode = SystemProperties.get("ro.def.svn.countrycode");
        FotaLog.v(TAG, "svn = " + svn + ", versionNum = " + versionNum + ", version = " + version
				+ ", countryCode = " + countryCode);
        String cusVer = context.getString(R.string.def_jrdfota_custom_version).trim();
        if(!TextUtils.isEmpty(cusVer)){
            return cusVer;
        }
        if (!TextUtils.isEmpty(svn) && !TextUtils.isEmpty(countryCode)){
            return Build.VERSION.RELEASE + "-" + svn + countryCode;
        } else if (!TextUtils.isEmpty(svn)){
            return Build.VERSION.RELEASE + "-" + svn;
        }else if (!TextUtils.isEmpty(versionNum)){
            return Build.VERSION.RELEASE + "-" + versionNum;
        } else {
            return Build.VERSION.RELEASE + "-" + version;
        }
    }

    /**
     * Format target version string
     *
     * @param v
     * @return
     */
    public static String newVersion(String v) {
        return Build.VERSION.RELEASE + "-" + v;
    }

    /**
     * Get the IMEI of the phone
     *
     * @param context
     * @return
     */
    public static String IMEI(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE));
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FotaApp.getApp());
        boolean testMode = sp.getBoolean("key_test_mode", false);
        if (testMode) {
            String test = sp.getString("key_test_imei", "");
            if (TextUtils.isEmpty(test)) {
                test = "013929000000997";
            }
            return test;
        }
        String imei = getIMEI(context);
        // imei = "358091060050942";
        FotaLog.v(TAG, "IMEI -> imei = " + imei);
        return imei;
    }
    private static String getIMEI(Context context){
        String strIMEI = null;
        final android.telephony.TelephonyManager telephonyManager =
                (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        MyTelephonyManager tm = new MyTelephonyManager(telephonyManager);
        if(tm != null){
            if (tm.getPhoneCount() > 1){
                FotaLog.v(TAG, "getIMEI -> phoneCount > 1");
                strIMEI=tm.getIMEI(0);
            } else{
                FotaLog.v(TAG, "getIMEI -> phoneCount == 1 or Build.VERSION.SDK_INT < 20");
                strIMEI=tm.getDeviceId();
            }
        }
        return strIMEI;
    }
    /**
     * Version of the system
     *
     * @return
     */
    public static String VERSION() {
        String sw_version = "";
        String sysVer = SystemProperties.get("ro.tct.sys.ver");
        if (!TextUtils.isEmpty(sysVer)) {
            sw_version = sysVer.substring(1, 4) + sysVer.substring(6, 7)
                    + sysVer.substring(11, 12) +  sysVer.substring(11, 12) + sysVer.substring(6, 8);
            //sw_version = "7EFYUEY0";
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FotaApp.getApp());
            boolean testMode = sp.getBoolean("key_test_mode", false);
            if (testMode) {
                String test = sp.getString("key_test_version", "");
                if (TextUtils.isEmpty(test)) {
                    test = "C2VUPV0";
                }
                return test;
            }
        }
        FotaLog.v(TAG, "VERSION -> version = " + sw_version);
		return sw_version;
	}

    /**
     * SVN of the system
     *
     * @return
     */
    public static String SVN() {
        String sw_svn = SystemProperties.get("ro.def.software.version");

        return sw_svn;
    }

    public static String MODEL() {
        String sw_model = "Firmware";
        return sw_model;
    }

	public static String PrefixREF() {
		String prefix = "";

		String sysVer = SystemProperties.get("ro.tct.sys.ver");
        if (!TextUtils.isEmpty(sysVer)) {
            String verType = sysVer.substring(11, 12);
            if (verType.equals("A") || verType.equals("B")) {
                prefix = verType + "_";
            }
        }

		return prefix;
	}

    /**
     * get sw_imei,sw_com_ref,sw_model
     *
     * @return 0 if ok, else error
     */
    public static String REF() {
        // Build.MODEL Build.DISPLAY
        // get sw_com_ref
        String sw_com_ref = "sw_com_ref_Test";
        sw_com_ref = SystemProperties.get("ro.tct.curef");
        //sw_com_ref = "5045I-2FOTAUS";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FotaApp.getApp());
        boolean testMode = sp.getBoolean("key_test_mode", false);
        if (testMode) {
            String testSu = sp.getString("key_test_ref", "");
            if (TextUtils.isEmpty(testSu)) {
                testSu = "SOUL4-2FOTAZZ";
            }
            return testSu;
        }
		sw_com_ref = PrefixREF() + sw_com_ref;
		FotaLog.v(TAG, "REF() -> curef = " + sw_com_ref);
		return sw_com_ref.trim();
	}

    public static String salt() {
        String numberChar = "0123456789";
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(numberChar.charAt(random.nextInt(numberChar.length())));
        }
        String str = System.currentTimeMillis() + sb.toString();

        FotaLog.v(TAG, "salt = " + str);
        return str;
    }

    /**
     * Do not change this logic . Or something bad happens.
     *
     * @return
     */
    public static String appendTail() {
        // this is a number .
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append("1");
        }
        sb.replace(1, 2, "27");
        sb.replace(4, 5, "94");
        sb.replace(8, 8, "2");
        String tail = sb.substring(0, 10);
        return "127194112128190539229184515554217196388916936124211541251141761661695824491682352342151692461437713116195"
                +
                "1402261451161002051042011757216713912611682532031591181861081836612643016596231212872211620"
                +
                "5118613021064469246257285710114111214718116411259201236411819755815116023122222618173754624459"
                +
                "66911723844130106116313122624220514";
    }

    public static String SHA1(String decript) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString().toUpperCase(Locale.ENGLISH);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Calculate file checksum
     *
     * @param file
     * @return
     */
    public static String SHA1(File file) {
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("SHA1");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // BigInteger bigInt = new BigInteger(1, digest.digest());
        return encodeHex(digest.digest());
    }

    private static String encodeHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            if (((int) bytes[i] & 0xff) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toString((int) bytes[i] & 0xff, 16));
        }
        return hex.toString();
    }


    /**
     * If there is a network connection connected
     *
     * @param context
     * @return true if there is a network connection connected
     */
    public static boolean isOnline(Context context) {
        Context c = context.getApplicationContext();
        FotaLog.v(TAG, "isOnline -> wifi = " + isWifiOnline(c) + ", mobile = " + isMobileOnline(c)
                + ", bluetooth = " + isBluetooth(c));
        return isWifiOnline(c) || isMobileOnline(c) || isBluetooth(c);
    }

    /**
     * If there is a wifi network connection connected
     *
     * @param context
     * @return true if there is a wifi network connection connected
     */
    public static boolean isWifiOnline(Context context) {
        Context c = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()
                && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * If there is a Bluetooth network connection connected
     *
     * @param context
     * @return true if there is a mobile network connection connected
     */
    public static boolean isBluetooth(Context context) {
        Context c = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()
                && netInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
            return true;
        }
        return false;
    }

    /**
     * If there is a mobile network connection connected
     *
     * @param context
     * @return true if there is a mobile network connection connected
     */
    public static boolean isMobileOnline(Context context) {
        Context c = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()
                && netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     * Set auto download when get values from server
     *
     * @param context
     * @param value
     */
    public static void setAutoDownload(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(KEY_AUTO_DOWNLOAD, value).apply();
    }

    /**
     * Get the settings of auto download
     *
     * @param context
     * @return true if user select auto download
     */
    public static boolean isAutoDownload(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean b = prefs.getBoolean(KEY_AUTO_DOWNLOAD,
                context.getResources().getBoolean(R.bool.def_jrdfota_is_auto_download_on_wifi));
        FotaLog.d(TAG, "isAutoDownload:" + b);
        return b;
    }
	
    /**
     * Read the saved max postpone times.
     *
     * @param context
     * @return
     */
    public static int getMaxPostponeTimes(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_POSTPONE_TIMES, FotaConstants.DEFAULT_MAX_POSTPONE);
    }
	
    /**
     * Get the value from server ,and save.
     *
     * @param context
     * @param times
     */
    public static void setMaxPostponeTimes(Context context, int times) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(KEY_POSTPONE_TIMES, times).apply();
    }

    /**
     * Set default check frequency when get values from server.
     *
     * @param context
     * @param period
     */
    public static void setDefaultCheckFrequency(Context context, int period) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultFrequency = prefs.getInt(FotaConstants.DEFAULT_UPDATE_CHECK_PREF,
                FotaUtil.getDefaultAutoCheckVal());
        FotaLog.d(TAG, "setDefaultCheckFrequency -> defaultFrequency = "
                + defaultFrequency + ", period = " + period);
        if (defaultFrequency != period) {
            prefs.edit().putInt(FotaConstants.DEFAULT_UPDATE_CHECK_PREF, period).apply();
            setCheckFrequency(context, period);
        }
    }

    /**
     * Set check frequency when get values from server
     *
     * @param context
     * @param period
     */
    public static void setCheckFrequency(Context context, int period) {
        FotaLog.d(TAG, "setCheckFrequency -> period = " + period);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(FotaConstants.UPDATE_CHECK_PREF, period).apply();
        if(period == 0){
            prefs.edit().putBoolean(FotaConstants.KEY_AUTO_CHECK_UPDATE, false).apply();
        }
        scheduleUpdateService(context, (long) period * 1000);
    }

    /**
     * get check frequency
     * @param context
     * @return
     */
    public static int getCheckFrequency(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(FotaConstants.UPDATE_CHECK_PREF, FotaUtil.getDefaultAutoCheckVal());
    }

    /**
     * Set root update .
     *
     * @param context
     * @param value
     */
    public static void setRootUpdate(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(ROOT_UPDATE, value).apply();
    }

    /**
     * Get root update .
     *
     * @param context
     */
    public static boolean getRootUpdate(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean b = prefs.getBoolean(ROOT_UPDATE,
                context.getResources().getBoolean(R.bool.def_jrdfota_root_upgrade));
        FotaLog.d(TAG, "ROOT_UPDATE:" + b);
        return b;
    }

    /**
     * If an update action is successful after power up.
     *
     * @param status
     * @return
     */
    public static boolean isUpdateSuccess(String status) {
        if (status.equals(FOTA_EVT_UPGRADE_SUCCESS)) {
            return true;
        }
        return false;
    }

    /**
     * If an update action is performed after power up.
     *
     * @param status
     * @return
     */
    public static boolean isUpdateDone(String status) {
        if (status.equals(FOTA_EVT_UPGRADE_SUCCESS)
                || status.equals(FOTA_EVT_UPGRADE_FAILURE)
                || status.indexOf(FOTA_EVT_UPGRADE_ERROR) > -1
                || status.indexOf(FOTA_EVT_UPGRADE_NOT_FIND) > -1
                || status.indexOf(FOTA_EVT_UPGRADE_SIGNATURE_ERROR) > -1) {
            return true;
        }

        return false;
    }

    /**
     * Return the /cache/recovery/last_fota.status file.
     */
    public static File fetchUpdateStatusFile() {
        File statusFile = new File(UPDATE_STATUS_FILE_PATH);
        if (statusFile.exists()) {
            return statusFile;
        }
        return null;
    }

    /**
     * Once we got status , we showed the user. We need clear the status file .
     */
    public static void clearStatus() {
        File statusFile = new File(UPDATE_STATUS_FILE_PATH);
        statusFile.delete();
    }

    /**
     * Get update status after power up. Read status file generated by recovery
     * from file :/cache/recovery/last_fota.status .
     *
     * @return
     */
    public static String getStatus() {
        File statusFile = new File(UPDATE_STATUS_FILE_PATH);
        String status = FOTA_EVT_UPGRADE_NOSTART;
        String mUpdateStatus = null;
        String mUpdateKind = null;
        if (statusFile.exists()) {
            FotaLog.d(TAG, "exists: " + UPDATE_STATUS_FILE_PATH);
            status = FOTA_EVT_UPGRADE_FAILURE;
            String line;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(statusFile));
                do {
                    line = reader.readLine();
                    String[] pair;

                    if (!TextUtils.isEmpty(line) && line.indexOf(':') > 0) {
                        pair = line.split(":", 2);
                        if (pair[0].equals(FLAG_UPDATE_RESULT)) {
                            mUpdateStatus = pair[1];
                        } else if (pair[0].equals(FLAG_UPDATE_KIND)) {
                            mUpdateKind = pair[1];
                        } else {
                            FotaLog.w(TAG, "wrong status file format : " + line);
                        }
                    }
                } while (line != null);

                reader.close();

            } catch (IOException e) {
                FotaLog.w(TAG, "getting update status failed" + e.getMessage());
            } catch (Exception e) {
                FotaLog.w(TAG, "getting update status failed" + e.getMessage());
            }

            if (mUpdateStatus != null) {
                FotaLog.d(TAG, "update status is : " + mUpdateStatus);
                if (mUpdateStatus.contains("INSTALL SUCCESS")) {
                    status = FOTA_EVT_UPGRADE_SUCCESS;
                }
                else if (mUpdateStatus.contains("is not correct")) {
                    status = mUpdateStatus;
                    FotaLog.d(TAG, "upgrade error is : " + status);
                }
                else if (mUpdateStatus.contains("not find")
                        || mUpdateStatus.contains("verification failed")) {
                    status = mUpdateStatus;
                    FotaLog.d(TAG, "upgrade error is : " + status);
                }

                if (mUpdateKind != null) {
                    FotaLog.d(TAG, "update kind is : " + mUpdateKind);
                    if (mUpdateKind.contains("full package")) {
                        if (TextUtils.equals(status, FOTA_EVT_UPGRADE_FAILURE))
                            status = FOTA_EVT_FULL_UPGRADE_FAILURE;
                        else if (TextUtils.equals(status, FOTA_EVT_UPGRADE_SUCCESS))
                            status = FOTA_EVT_FULL_UPGRADE_SUCCESS;
                    }
                }
            } else {
                FotaLog.d(TAG, "can't find update status in status file");
            }
        }
        FotaLog.d(TAG, "reutrn status : " + status);
        return status;

    }

    /**
     * This method choose best storage for download update package. if there is
     * a storage that is unremoveable and storage size is enough pick that one
     * ,otherwise choose storage size enough one.
     *
     * @return
     */
    public static String chooseSaveLocation(Context context, DownloadTask task) {
        UpdatePackageInfo info = task.getUpdateInfo();
        long fileSize = info.mFiles.get(0).mFileSize;
        boolean reserve = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                FotaConstants.KEY_RESERVE_SPACE_SUPPORT, false);
        FotaLog.d(TAG, "chooseSaveLocation -> fileSize = " + fileSize + ", reserve = " + reserve);
        if (reserve) {
            try {
                File f = new File(FotaConstants.PATH_RESERVED_STORAGE_FOR_FOTA);
                if (!f.exists()) {
                    f.mkdir();
                }
                if (checkReservedStorageSize(f) >= fileSize) {
                    // if there is an reserved space and its size is larger than
                    // package size. Use reserved space.
                    return FotaConstants.PATH_RESERVED_STORAGE_FOR_FOTA;
                }
            } catch (Exception e) {
                FotaLog.d(TAG, "chooseSaveLocation -> checkReservedStorageSize Exception: "
                        + Log.getStackTraceString(e));
            }
        }

        Vector<StorageVolume> storageList = getVolumeList(context, fileSize);
        FotaLog.d(TAG, "chooseSaveLocation -> storageList = " + storageList);

        if (storageList.size() == 0) {
            return String.valueOf(FotaConstants.STORAGE_SPACE_NOT_ENOUGH);
        } else if (storageList.size() == 1) {
            if (storageList.get(0).isPrimary()) {
				return "/data/data/com.tcl.monster.fota";
            } else {
                return storageList.get(0).getPath();
            }
        } else {
            for (StorageVolume sv : storageList) {
                if (sv.isPrimary()) {
					return "/data/data/com.tcl.monster.fota";
                }
            }
            return storageList.get(0).getPath();
        }
    }

    private static Vector<StorageVolume> getVolumeList(Context context, long size) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] svs = sm.getVolumeList();
        Vector<StorageVolume> storageList = new Vector<StorageVolume>();
        int i = 0;
        for (StorageVolume sv : svs) {
            String path = sv.getPath();

            FotaLog.d(TAG, "getVolumeList -> storage path = " + path);
            if (Environment.MEDIA_MOUNTED.equals(Environment.getStorageState(sv.getPathFile()))
                        && checkStorageSize(path) > size) {
                storageList.add(sv);
                FotaLog.d(TAG, "getVolumeList -> valid path = " + path);
            }
        }
        return storageList;
    }
	
    /**
     * Get update zip file
     *
	 * @return update zip file
	 */
    public static File updateZip() {
        String path = FotaPref.getInstance(FotaApp.getApp()).getString(
                FotaConstants.PATH_SAVING_UPDATE_PACKAGE, "");
        makeUpdateFolder(path);
        String fileName = path + File.separator + FotaConstants.UPDATE_FILE_DIR + File.separator
                + FotaConstants.UPDATE_FILE_NAME;
        return new File(fileName);
    }

    /**
     * Get update log file
     *
     * @return update log file
     */
    public static File updateLog() {
        File sdcard = Environment.getExternalStorageDirectory();
        File parent = new File(sdcard.getPath() + File.separator + FotaConstants.UPDATE_FILE_DIR);// ;updateZip().getParentFile();
        if (!parent.exists()) {
            parent.mkdir();
        }
        return new File(parent, FotaConstants.UPDATE_LOG_NAME);
    }

    /**
     * Get the crash log file
     *
     * @return crash log file
     */
    public static File crashLog() {
        File sdcard = Environment.getExternalStorageDirectory();
        File parent = new File(sdcard.getPath() + File.separator + FotaConstants.UPDATE_FILE_DIR);// ;updateZip().getParentFile();
        if (!parent.exists()) {
            parent.mkdir();
        }
        return new File(parent, "crash.log");
    }

    public static boolean isTestMode() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FotaApp.getApp());
        boolean testMode = sp.getBoolean("key_test_mode", false);
        return testMode;
    }

    public static String getCurrentLanguageType() {
        String lCode = Locale.getDefault().getLanguage();
        if (lCode.equals("") || lCode.length() == 0)
            lCode = "en";
        FotaLog.d(TAG, "language type =====" + lCode);
        return lCode;
    }

    public static String getRandomUrl() {
        if (mBaseUrlList == null || mBaseUrlList.size() == 0) {
            initUrlList();
        }
        Random mRandom = new Random();
        int index = mRandom.nextInt(mBaseUrlList.size());

        String mBaseUrl = mBaseUrlList.get(index).toString();
        FotaLog.d(TAG, "getRandomUrl = " + mBaseUrl);
        mBaseUrlList.remove(index);
        return mBaseUrl;
    }

    private static List initUrlList() {
        List list = new ArrayList();
        list.add(FotaConstants.GOTU_URL_1);
        list.add(FotaConstants.GOTU_URL_2);
        list.add(FotaConstants.GOTU_URL_3);
        list.add(FotaConstants.GOTU_URL_4);
        list.add(FotaConstants.GOTU_URL_5);
        list.add(FotaConstants.GOTU_URL_6);
        list.add(FotaConstants.GOTU_URL_7);
        mBaseUrlList = list;
        Collections.shuffle(mBaseUrlList);
        return mBaseUrlList;
    }

	public static void setFotaInstallNeedMinSize(long size, Context contex) {
		SharedPreferences sp = contex.getSharedPreferences("FOTA_INSTALL_SIZE", Context.MODE_PRIVATE);
		sp.edit().putLong("FOTA_INSTALL_SIZE", size).commit();
	}

	public static long getFotaInstallNeedMinSize(Context contex) {
		SharedPreferences sp = contex.getSharedPreferences("FOTA_INSTALL_SIZE", Context.MODE_PRIVATE);
		return sp.getLong("FOTA_INSTALL_SIZE", 0);
	}

    public static String getExtVersion() {
        String extVer = "";
        extVer = SystemProperties.get("ro.build.id");
        FotaLog.v(TAG, "getExtVersion -> extVer = " + extVer);
        return extVer;
    }

    public static boolean isCalling(Context context){

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        /**
         * 返回电话状态
         *
         * CALL_STATE_IDLE 无任何状态时
         * CALL_STATE_OFFHOOK 接起电话时
         * CALL_STATE_RINGING 电话进来时
         */
//        tm.getCallState();
        FotaLog.d("FotaUpdateService", "tm.getCallState()："+tm.getCallState());
        if(tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            return false;
        } else if(tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            return true;
        } else if(tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            return true;
        }
        return false;
    }
}
