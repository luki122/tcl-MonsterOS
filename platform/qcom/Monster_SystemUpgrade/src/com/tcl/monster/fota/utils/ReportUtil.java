package com.tcl.monster.fota.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.downloadengine.DownloadEngine;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.model.UpdatePackageInfo;
import com.tcl.monster.fota.provider.Fota;
import com.tcl.monster.fota.service.LogReportService;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is responsible for send user operation and update result to the server.
 */
public class ReportUtil {
    public static final String TAG = ReportUtil.class.getSimpleName();

    public static final String DEFAULT_FOTA_STATUS = "999";

    // define the op list.See to document "GOTU-Three-Tier-Structure-V1.7-Design" 4.1.1.3 section
    public static final String OP_CHECK = "50";
    public static final String OP_CANCELDOWNLOAD_POP = "51";
    public static final String OP_STARTDOWNLOAD_POP = "52";
    public static final String OP_POSTPONE_INSTALL = "53";
    public static final String OP_STARTINSTALL_POP = "54";
    public static final String OP_PAUSEDOWNLOAD = "55";
    public static final String OP_DELETEPACKAGE = "56";
    public static final String OP_ONWIFIONLY = "57";
    public static final String OP_OFFWIFIONLY = "58";
    public static final String OP_DOWNLOAD_REQUEST = "100"; // fetch download uris
    public static final String OP_DOWNLOAD = "1000";// download complete
    public static final String OP_UPGRADE = "2000";
    public static final String OP_OTU_report = "3000";
    public static final String OP_Watch = "5000";
    public static final String OP_Sugar_Report = "5010";
    public static final String OP_Candy = "6000";

    /**
     * Record the operation and trigger send.
     *
     * @param op     operation code
     * @param status operation value
     */
    public static void recordOperation(final Context context, String op, String status) {
        FotaLog.d(TAG, "recordOperation -> op = " + op);
        DownloadTask task = null;
        if (TextUtils.equals(op, OP_UPGRADE)) {
            DownloadEngine downloadEngine = DownloadEngine.getInstance();
            downloadEngine.init(context);
            String downloadId = FotaPref.getInstance(context).getString(FotaConstants.DOWNLOAD_ID, "");
            task = downloadEngine.findDownloadTaskByTaskId(downloadId);
        } else {
            task = FotaUIPresenter.getInstance(context).getCurrentDownloadTask();
        }
        if (task == null) {
            return;
        }

        boolean isFullUpdate = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(FotaConstants.DOWNLOAD_IS_FULL_PACKAGE, false);
        UpdatePackageInfo info = task.getUpdateInfo();
        String salt = FotaUtil.salt();
        final List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil.IMEI(context.getApplicationContext())));
        params.add(new BasicNameValuePair("salt", salt));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));
        params.add(new BasicNameValuePair("fv", info.mFv));
        params.add(new BasicNameValuePair("tv", info.mTv));
        if (isFullUpdate) {
            params.add(new BasicNameValuePair("mode", "7"));
        } else {
            params.add(new BasicNameValuePair("mode", "2"));
        }
        params.add(new BasicNameValuePair("cltp", "10"));
        params.add(new BasicNameValuePair("type", "Firmware"));
        params.add(new BasicNameValuePair("vk", generateVk(context, info, salt, op, status, isFullUpdate)));
        params.add(new BasicNameValuePair("op", op));
        params.add(new BasicNameValuePair("status", status));

        final String param = URLEncodedUtils.format(params, "UTF-8");
        FotaLog.d(TAG, "recordOperation -> param = " + param);
		queueReport(context, param, Fota.Report.FIRMWARE_REPORT);
    }

    private static void queueReport(final Context context, final String param, final int origin) {
        new Thread(new Runnable() {
            public void run() {
                ContentValues values = new ContentValues();
				values.put(Fota.Report.PARAM, param);
				values.put(Fota.Report.ORIGIN, origin);
				context.getContentResolver().insert(Fota.Report.CONTENT_URI, values);
                Intent downloadintent = new Intent(context, LogReportService.class);
                context.startService(downloadintent);
            }
        }, "queueReport").start();
    }

    /**
     * VK needed by post request. Do not change this method .
     */
    private static String generateVk(Context context, UpdatePackageInfo info, String salt,
            String op, String status, boolean isFullUpdate) {

        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil
                .IMEI(context)));
        params.add(new BasicNameValuePair("salt", salt));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));

        params.add(new BasicNameValuePair("fv", info.mFv));
        params.add(new BasicNameValuePair("tv", info.mTv));
        if (isFullUpdate) {
            params.add(new BasicNameValuePair("mode", "7"));
        } else {
            params.add(new BasicNameValuePair("mode", "2"));
        }
        params.add(new BasicNameValuePair("cltp", "10"));
        params.add(new BasicNameValuePair("type", "Firmware"));
        params.add(new BasicNameValuePair("op", op));
        params.add(new BasicNameValuePair("status", status + FotaUtil.appendTail()));

        String param = URLEncodedUtils.format(params, "UTF-8");
        return FotaUtil.SHA1(param);
    }
}