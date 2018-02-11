/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.utils;

import android.app.Activity;
import android.content.ComponentName;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;

import java.lang.reflect.Method;
import java.util.List;

public class CommonUtils {

    private static final String TAG = "CommonUtils";
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    private static final String EXTRA_PREFS_PORTRAIT_LOCK = "extra_prefs_portrait_lock";
    // Extra containing the resource name of the theme to be used
    private static final String EXTRA_THEME = "theme";
    private static final String THEME_HOLO_LIGHT = "holo_light";
    // the request code for wifi settings
    public final static int REQUEST_CODE_WIFISETTING = 100;

    /**
     * calculate the freqiency for wifi
     *
     * @param frequency
     * @return
     */
    public static String calculateFrequency(int frequency) {
        try {
            if (frequency > 1024) {
                return String.format("%.1f GHz", frequency / 1024.0);
            } else {
                return (frequency + "MHz");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * int convert to IP format
     * @param ip
     * @return
     */
    public static String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
    }

    public static void enterWifiSetting(Activity activity) {
        try {
            // Skip to the WiFi setup page
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.android.settings",
                    "com.android.settings.wifi.WifiSetupActivity");
            intent.setComponent(comp);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRA_PREFS_PORTRAIT_LOCK, true);
            intent.putExtra(EXTRA_THEME, THEME_HOLO_LIGHT);
            intent.putExtra("useImmersiveMode", true);
            /* intent.putExtra(EXTRA_SCRIPT_URI, "SetupWizard"); */
            activity.startActivityForResult(intent, REQUEST_CODE_WIFISETTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    /**
     * return the status of location service
     * @param context
     * @return
     */
    public static boolean getLocationEnabled(Context context) {
        int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
        if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * set the status fo location service
     * @param context
     * @param enabled
     */
    public static void setLocationEnabled(Context context, boolean enabled) {
        if (enabled) {
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        } else {
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    public static boolean hasFingerprint(Context context) {
        boolean result = false;
        try {
            FingerprintManager fm = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            Class<?> fmClass = fm.getClass();
            Method operatorMethod = fmClass.getDeclaredMethod("tctGetEnrolledFingerprints", int.class);
            operatorMethod.setAccessible(true);
            List<Object> items = (List<Object>) operatorMethod.invoke(fm,  0);
            final int fingerprintCount = items != null ? items.size() : 0;
            LogUtils.i(TAG, "fingerprintCount: " + fingerprintCount);
            if (fingerprintCount == 0) {
                result = false;
            } else {
                result = true;
            }
        } catch (NoSuchMethodException e) {
            LogUtils.e(TAG, "hasFingerprint: " + e.toString());
            e.printStackTrace();
        } finally {
            LogUtils.i(TAG, "hasFingerprint: " + result);
            return result;
        }
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
}
