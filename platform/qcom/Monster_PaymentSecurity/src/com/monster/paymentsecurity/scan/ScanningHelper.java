package com.monster.paymentsecurity.scan;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.monster.paymentsecurity.db.WhiteListDao;
import com.monster.paymentsecurity.diagnostic.RiskOrError;
import com.monster.paymentsecurity.scan.qscanner.ScanInstalledApk;
import com.monster.paymentsecurity.scan.mms.MmsScanTask;
import com.monster.paymentsecurity.scan.qscanner.ScanUninstalledApks;
import com.monster.paymentsecurity.scan.system.PayEnvirementScanTask;
import com.monster.paymentsecurity.scan.system.SystemUpdateScanTask;
import com.monster.paymentsecurity.scan.wifi.DetectARPTask;
import com.monster.paymentsecurity.scan.wifi.DetectSecurityTask;
import com.monster.paymentsecurity.scan.wifi.DnsPhishingTask;
import com.monster.paymentsecurity.util.Utils;

import java.util.ArrayList;
import java.util.List;

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
 * Created by logic on 16-11-22.
 */
public final class ScanningHelper {

    /**
     *  所有扫描任务
     *
     * @param context
     * @return
     */
    public static List<BaseScanTask> getAllScanTasks(Context context){
        return new ArrayList<BaseScanTask>(){
            {
                addAll(ScanningHelper.getInstalledApksScanTasks(context));
                addAll(ScanningHelper.getSystemScanTasks(context));
                addAll(ScanningHelper.getWifiScanTasks(context));
                add(ScanningHelper.getSmsScanTask(context));
                add(ScanningHelper.getUninstalledApksScanTask(context));
            }
        };
    }

    /**
     * wifi相关扫描task
     *
     * @param con
     * @return
     */
    public static List<BaseScanTask> getWifiScanTasks(final Context con) {
        if (Utils.isWifiOk(con)) {
            return new ArrayList<BaseScanTask>() {
                {
//                    add(new WifiStateTask(con));
                    add(new DetectSecurityTask(con));
                    add(new DnsPhishingTask(con));
                    add(new DetectARPTask(con));
                }
            };
        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * 已安装应用扫描task
     *
     * @param con
     * @return
     */
    public static List<BaseScanTask> getInstalledApksScanTasks(Context con) {
        if (con == null) return new ArrayList<>(0) ;
        PackageManager pm = con.getApplicationContext().getPackageManager();
        List<PackageInfo> appInfos = pm.getInstalledPackages(0);
        List<BaseScanTask> tasks = new ArrayList<>(appInfos.size());
        WhiteListDao dao = new WhiteListDao(con);
        for (int i = 0; i < appInfos.size(); i++) {
            PackageInfo info = appInfos.get(i);

            if(((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 )
                    && (dao.getWhiteListApp(info.applicationInfo.packageName) == null))
            {
                tasks.add(new ScanInstalledApk(con, info.packageName));
            }
        }
        return tasks;
    }

    /**
     * 扫描ＳＤ卡中apk
     * @param con
     * @return
     */
    public static BaseScanTask getUninstalledApksScanTask(Context con){
        return new ScanUninstalledApks(con);
    }

    /**
     * Sms相关扫描task
     *
     * @param con
     * @return
     */
    public static MmsScanTask getSmsScanTask(Context con) {
        return new MmsScanTask(con);
    }

    /**
     * 系统相关扫描task
     *
     * @param con
     * @return
     */
    public static List<BaseScanTask> getSystemScanTasks(final Context con) {
        return new ArrayList<BaseScanTask>() {
            {
                add(new PayEnvirementScanTask(con));
                add(new SystemUpdateScanTask(con));
            }
        };
    }

    /**
     * 扫描类型
     *
     * @param scanType
     * @return
     */
    public static String getScanName(@BaseScanTask.ScanType int scanType) {
        switch (scanType) {
            case SCAN_TYPE_WIFI_STATE:
                return "WiFi连接状态检测:";
            case SCAN_TYPE_WIFI_SECURITY:
                return "WiFi加密检测:";
            case SCAN_TYPE_WIFI_DNS:
                return "DNS服务劫持检测:";
            case SCAN_TYPE_WIFI_ARP:
                return "ARP攻击检测:";
            case SCAN_TYPE_SMS_APP:
                return "默认短信应用检测:";
            case SCAN_TYPE_SYSTEM_PAYMENT_ENV:
                return "支付安全环境检测:";
            case SCAN_TYPE_SYSTEM_UPDATE:
                return "系统更新检测:";
            case SCAN_TYPE_QSCANER_INSTALLED_APK:
                return "应用程序扫描:";
            case BaseScanTask.SCAN_TYPE_QSCANER_UNINSTALLED_APKS:
            case SCAN_TYPE_QSCANER_UNINSTALLED_APK:
                return "安装包扫描:";
            default:
                break;
        }
        return "未知扫描";
    }


    public static @RiskOrError.RiskCategory int convertScanTypeToCategory(@BaseScanTask.ScanType int scanType) {
        @RiskOrError.RiskCategory int riskCategory = RiskOrError.RISK_CATEGORY_WIFI;
        switch (scanType) {
            case SCAN_TYPE_WIFI_STATE:
            case SCAN_TYPE_WIFI_SECURITY:
            case SCAN_TYPE_WIFI_DNS:
            case SCAN_TYPE_WIFI_ARP:
                riskCategory = RiskOrError.RISK_CATEGORY_WIFI;
                break;
            case SCAN_TYPE_SMS_APP:
                riskCategory = RiskOrError.RISK_CATEGORY_SMS_SECURITY;
                break;
            case SCAN_TYPE_SYSTEM_PAYMENT_ENV:
            case SCAN_TYPE_SYSTEM_UPDATE:
                riskCategory = RiskOrError.RISK_CATEGORY_SYSTEM_BUG;
                break;
            case SCAN_TYPE_QSCANER_INSTALLED_APK:
            case SCAN_TYPE_QSCANER_UNINSTALLED_APK:
            case SCAN_TYPE_QSCANER_UNINSTALLED_APKS:
                riskCategory = RiskOrError.RISK_CATEGORY_APP_SECURITY;
                break;
        }
        return riskCategory;
    }
}
