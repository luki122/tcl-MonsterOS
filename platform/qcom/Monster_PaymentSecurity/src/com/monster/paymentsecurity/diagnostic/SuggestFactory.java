package com.monster.paymentsecurity.diagnostic;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.util.PackageUtils;
import com.monster.paymentsecurity.util.SettingUtil;

import java.io.File;
import java.lang.reflect.Method;

import tmsdk.common.module.qscanner.QScanResultEntity;

import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_QSCANER_INSTALLED_APK;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_QSCANER_UNINSTALLED_APK;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_QSCANER_UNINSTALLED_APKS;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_SMS_APP;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_ARP;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_DNS;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_SECURITY;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_STATE;

/**
 * Created by logic on 16-12-5.
 */
public final class SuggestFactory {

    private final Context mContext;

    public SuggestFactory(Context act){
        this.mContext = act.getApplicationContext();
    }

    @SuppressWarnings("unchecked")
    public Suggest create(final RiskOrError risk){
        Action action;
        Suggest suggest = new Suggest();
        switch (risk.getScanType()) {
            case SCAN_TYPE_WIFI_STATE:
            case SCAN_TYPE_WIFI_ARP:
            case SCAN_TYPE_WIFI_DNS:
            case SCAN_TYPE_WIFI_SECURITY:
                action = () -> {
//                        Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(intent);
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    return wifiManager.setWifiEnabled(false);
                };
                break;
            case SCAN_TYPE_QSCANER_INSTALLED_APK:
                action = () -> {
                    AppRisk appRisk = (AppRisk) risk;
                    PackageUtils.uninstallApp(appRisk.getEntity().packageName);
                    return true;
                };
                break;
            case SCAN_TYPE_QSCANER_UNINSTALLED_APKS:
            case SCAN_TYPE_QSCANER_UNINSTALLED_APK:
                action = () -> {
                    AppRisk appRisk = (AppRisk) risk;
                    QScanResultEntity entity = appRisk.getEntity();
                    boolean result = new File(entity.path).delete();
                    Log.w("ScanningEngine", "delete apk: " + entity.path + ", result = " + result);
                    if (result) {
                        Toast.makeText(mContext, R.string.success_clean, Toast.LENGTH_SHORT).show();
                    }
                    return result;
                };
                break;
            case SCAN_TYPE_SMS_APP:
                action = () -> {
                    try {
                        Class clazz = Class.forName("com.android.internal.telephony.SmsApplication");
                        Method method = clazz.getMethod("setDefaultApplication", String.class, Context.class);
                        method.invoke(null, "com.android.sms", mContext);
                        Settings.Secure.putStringForUser(mContext.getContentResolver(),
                                Settings.Secure.SMS_DEFAULT_APPLICATION, "com.android.sms",
                                UserHandle.USER_CURRENT);
                        Toast.makeText(mContext, R.string.success_restore, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent =
                                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                "com.android.sms");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                    return true;
                };
                break;
            case SCAN_TYPE_SYSTEM_PAYMENT_ENV:
                action = () -> {
                    SettingUtil.setPayAppDetectionEnable(mContext, true);
                    Toast.makeText(mContext, R.string.success_open, Toast.LENGTH_SHORT).show();
                    return true;
                };
                break;
            case SCAN_TYPE_SYSTEM_UPDATE:
                action = () -> {
                    Intent intent = new Intent();
                    intent.setAction(Constant.ACTION_VIEW_SYSTEM_UPDATE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return true;
                };
                break;
            default:
                action = () -> false;
        }
        suggest.setAction(action);
        return suggest;
    }
}
