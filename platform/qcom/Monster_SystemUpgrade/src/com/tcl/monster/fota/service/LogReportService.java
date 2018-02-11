package com.tcl.monster.fota.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.provider.Fota;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogReportService extends Service {
    private static final String TAG = "LogReportService";

    private ServiceHandler mServiceHandler;
    static HandlerThread sTHandler = null;

    private static final String[] PROJECTION = new String[] {
            Fota.Report._ID, // 0
            Fota.Report.PARAM, // 1
            Fota.Report.ORIGIN, // 2
    };

    public void onCreate() {
        if (sTHandler == null) {
            sTHandler = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
            sTHandler.start();
        }
        mServiceHandler = new ServiceHandler(sTHandler.getLooper());
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests. The incoming requests are
         * initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            handleSendReport();
            stopSelf();
        }
    }

    private void handleSendReport() {
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(Fota.Report.CONTENT_URI, PROJECTION, null, null, null);
        String baseUrl;
        if (c != null) {
            try {
                c.moveToFirst();
                for (int i = 0; i<c.getCount(); i++) {
                //for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    if (c.getInt(2) == 0) {
                        baseUrl = "http://" + FotaUtil.getRandomUrl() + "/notify.php";
                        try {
                            long id = c.getLong(0);
                            FotaLog.d(TAG, "handleSendReport -> " + c.getString(1) + "-----> " + id);
                            httpPost(this, baseUrl, c.getString(1),id, true);
                        } catch (IOException e) {
                            FotaLog.d(TAG, "handleSendReport -> sendUserOperation Exception");
                            e.printStackTrace();
                        }
                    }
                    c.moveToNext();
                }
            } finally {
                c.close();
            }
        }
    }

    private static void httpPost(Context context, String realUrl, String params, long id,boolean isChunck) throws IOException {
        // http post request is required by server.
        HttpURLConnection connection = null;
        try {
            URL url = new URL(realUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            byte[] content = params.getBytes();
            if(isChunck){
                connection.setChunkedStreamingMode(0);
            }else {
                connection.setFixedLengthStreamingMode(content.length);
            }
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", FotaUtil.getUserAgentString(context));
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setReadTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            connection.setConnectTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(content);
            out.flush();
            out.close();
            int statusCode = connection.getResponseCode();
            FotaLog.d(TAG, "httpPost -> realUrl = " + realUrl + "\nparams= " + params + "\n" + "statusCode "+ statusCode);

            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:
                    context.getContentResolver().delete(ContentUris.withAppendedId(Fota.Report.CONTENT_URI,
                            id), null, null);
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    break;
                case HttpURLConnection.HTTP_LENGTH_REQUIRED:
                    httpPost(context, realUrl, params, id, false);
                    break;
                default:
                    break;
            }
        }catch (IOException e){
         FotaLog.d(TAG, "httpPost -> Exception msg = " + e.getMessage());
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceHandler.sendEmptyMessage(0);

        return START_NOT_STICKY;
    }
}