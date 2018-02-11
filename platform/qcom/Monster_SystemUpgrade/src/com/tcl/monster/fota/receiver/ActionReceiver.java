package com.tcl.monster.fota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * This class handle Fota action. auto check, send update result, report location.
 */
public class ActionReceiver extends BroadcastReceiver{
    private static final String TAG = "ActionReceiver";

    public static final String ACTION_AUTO_CHECK = "com.tcl.ota.action.AUTO_CHECK";
    public static final String ACTION_LOCATION_INFO = "com.tcl.ota.action.LOCATION_INFO";

    private static String mBaseUrl =  FotaConstants.GOTU_URL_1;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        FotaLog.d(TAG, "onReceive -> action = " + action);
        String lon = intent.getStringExtra("Longitude");
        String lat = intent.getStringExtra("Latitude");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (ACTION_AUTO_CHECK.equals(action)) {
            FotaUIPresenter.getInstance(context)
                    .scheduleCheck(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO);
        } else if (ACTION_LOCATION_INFO.equals(action)
                && prefs.getBoolean(FotaConstants.FIRST_TIME_SEND_LOCATION, true)){
            FotaLog.d(TAG, "onReceive -> report location, lon = " + lon + ", lat = " + lat);
            new reportLocationThread(context, lon, lat).start();
        }
    }

    class reportLocationThread extends Thread{
        private  Context mContext;
        private String mLon;
        private String mLat;
        public  reportLocationThread(Context context,String lon,String lat){
            mContext = context;
            mLon = lon;
            mLat = lat;
        }

        public void run() {
            boolean isRooted = FotaUtil.isDeviceRooted();
            List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
            params.add(new BasicNameValuePair("id", FotaUtil
                    .IMEI(mContext)));
            params.add(new BasicNameValuePair("curef", FotaUtil.REF()));
            params.add(new BasicNameValuePair("fv", FotaUtil.VERSION()));
            if (isRooted) {
                params.add(new BasicNameValuePair("mode", "7"));
            } else {
                params.add(new BasicNameValuePair("mode", "2"));
            }
            params.add(new BasicNameValuePair("type", "Firmware"));
            params.add(new BasicNameValuePair("cltp", "10"));
            params.add(new BasicNameValuePair("cktp", "1"));
            params.add(new BasicNameValuePair("rtd",
                    isRooted ? FotaConstants.FOTA_ROOT_FLAG_VALUE_YES
                            : FotaConstants.FOTA_ROOT_FLAG_VALUE_NO));
            params.add(new BasicNameValuePair("chnl",
                    FotaUtil.isWifiOnline(mContext) ? FotaConstants.FOTA_CONNECT_TYPE_VALUE_WIFI
                            : FotaConstants.FOTA_CONNECT_TYPE_VALUE_3G));
            params.add(new BasicNameValuePair("lng",mLon));
            params.add(new BasicNameValuePair("lat",mLat));

            String param = URLEncodedUtils.format(params, "UTF-8");
            // baseUrl
            mBaseUrl = FotaUtil.getRandomUrl();
            String baseUrl = "http://" + mBaseUrl + "/check.php";
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl + "?" + param);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "*/*");
                connection.setRequestProperty("User-Agent", FotaUtil.getUserAgentString(mContext));
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setReadTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
                connection.setConnectTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
                connection.connect();
                int statusCode = connection.getResponseCode();
                FotaLog.v(TAG, "httpGetForCheck statusCode   = " + statusCode);
                switch (statusCode) {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_NO_CONTENT:
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                .putBoolean(FotaConstants.FIRST_TIME_SEND_LOCATION,false)
                                .apply();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}