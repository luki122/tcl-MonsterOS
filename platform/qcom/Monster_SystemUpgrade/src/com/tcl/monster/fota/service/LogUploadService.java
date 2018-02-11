package com.tcl.monster.fota.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;
import com.tcl.monster.fota.utils.ReportUtil;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class LogUploadService extends IntentService {

    private static final String TAG = "LogUploadService";
	public static final String ACTION_UPLOAD_LOG = "com.tcl.fota.action_UPLOAD_LOG";
    public static final String BOUNDARY = "---------------------------7d33a816d302b6";

    public LogUploadService() {
        super(TAG);
    }

    public String param(List<BasicNameValuePair> list) {
        StringBuffer sb = new StringBuffer();
        Iterator<BasicNameValuePair> iter = list.iterator();
        while (iter.hasNext()) {
            BasicNameValuePair entry = iter.next();
            String inputName = (String) entry.getName();
            String inputValue = (String) entry.getValue();
            if (inputValue == null) {
                continue;
            }
            sb.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
            sb.append(inputValue);
        }
        return sb.toString();
    }

    public String file(String inputName, String filename) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\""
                + filename + "\"\r\n");
        strBuf.append("Content-Type:application/octet-stream\r\n\r\n");

        return strBuf.toString();
    }

    /**
     * upload logs to the server
     */
    public void uploadLogs(final Context context, String op, String status) {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(cal.getTime());
        File fileToUpload = FotaUtil.crashLog();

        String salt = FotaUtil.salt();
        final List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil
                .IMEI(context.getApplicationContext())));
        params.add(new BasicNameValuePair("salt", salt));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));
        params.add(new BasicNameValuePair("fv", "5GNDVL1"));
        params.add(new BasicNameValuePair("tv", "5GNDVL6"));
        params.add(new BasicNameValuePair("mode", "2"));
        params.add(new BasicNameValuePair("cltp", "10"));
        params.add(new BasicNameValuePair("type", "Firmware"));
        params.add(new BasicNameValuePair("vk", generateVk(context, salt, op, status)));
        params.add(new BasicNameValuePair("op", op));
        params.add(new BasicNameValuePair("status", status));

        final String baseUrl = "http://54.251.120.225/handset_log.php";

        // http post request is required by server.
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setChunkedStreamingMode(0);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", FotaUtil.getUserAgentString(context));
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Content-Type", "multipart/form-data"
                    + ";boundary=---------------------------7d33a816d302b6");

            connection.setRequestProperty("Connection", "close");
            connection.setReadTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            connection.setConnectTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(param(params).getBytes());

            out.write(file("file", FotaUtil.IMEI(context) + "_" + timestamp + ".log").getBytes());

            InputStream is = new FileInputStream(fileToUpload);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            is.close();
            int statusCode = connection.getResponseCode();
            FotaLog.v(TAG, "httpPost statusCode   = " + statusCode);

            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:
                    fileToUpload.delete();
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                default:
                    InputStream in = new BufferedInputStream(connection.getErrorStream());
                    Reader reader = new InputStreamReader(in, "utf-8");
                    FotaLog.w(TAG, "upload content length :" + connection.getContentLength());
                    CharArrayBuffer charBuffer = new CharArrayBuffer(connection.getContentLength());
                    try {
                        char[] tmp = new char[1024];
                        int l;
                        while ((l = reader.read(tmp)) != -1) {
                            charBuffer.append(tmp, 0, l);
                        }
                    } finally {
                        reader.close();
                        in.close();
                    }
                    FotaLog.w(TAG, "upload content :" + charBuffer.toString());
                    break;
            }
        } catch (Exception e) {
            FotaLog.w(TAG, Log.getStackTraceString(e));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    /**
     * VK needed by post request. Do not change this method .
     */
    private static String generateVk(Context context, String salt, String op, String status) {

        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil
                .IMEI(context)));
        params.add(new BasicNameValuePair("salt", salt));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));

        params.add(new BasicNameValuePair("fv", "5GNDVL1"));
        params.add(new BasicNameValuePair("tv", "5GNDVL6"));
        params.add(new BasicNameValuePair("mode", "2"));
        params.add(new BasicNameValuePair("cltp", "10"));
        params.add(new BasicNameValuePair("type", "Firmware"));
        params.add(new BasicNameValuePair("op", op));
        params.add(new BasicNameValuePair("status", status + FotaUtil.appendTail()));

        String param = URLEncodedUtils.format(params, "UTF-8");
        // FotaLog.v(TAG,"generateVk param  = " + param);
        // FotaLog.v(TAG,"generateVk  = " + FotaUtil.SHA1(param));
        return FotaUtil.SHA1(param);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // use Log instead of FotaLog here
        // Log.d(FotaApp.TAG+ "@@@"+TAG, "start collect logs");
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        FotaLog.v(TAG, "onHandleIntent  action = " + action);
        if (ACTION_UPLOAD_LOG.equals(action)) {
            uploadLogs(getApplicationContext(), ReportUtil.OP_DOWNLOAD,
                    ReportUtil.DEFAULT_FOTA_STATUS);
        }
    }
}
