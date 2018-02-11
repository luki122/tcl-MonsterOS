/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;


import android.text.TextUtils;
import android.util.Base64;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.tcl.weather.R;
import cn.tcl.weather.utils.Debugger;

/**
 * Created by on 16-8-17.
 */
public class ServerConstant {

    private final static String TAG = "ServerConstant";

    private static final String HMAC_SHA1 = "HmacSHA1";


    /**
     * request param key
     */
    public static final String SERVER_URL_DATA = "http://webapi.weather.com.cn/data/";
    public static final String SERVER_URL_SEARCH = "http://webapi.weather.com.cn/data/citysearch/n/";
    public static final String SERVER_URL_GEO = "http://geo.weathercn.com/ag9/";
    public static final String AREA_ID = "areaid";
    public static final String CITY = "city";
    public static final String TYPE = "type";
    public static final String DATE = "date";
    public static final String APP_ID = "appid";
    public static final String KEY = "key";

    public static final String LON = "lon";
    public static final String LAT = "lat";


    /**
     * request type constant
     */
    public static final String TYPE_FORECAST_7_DAY = "forecast7d";
    public static final String TYPE_FORECAST_5_DAY = "forecast5d";
    public static final String TYPE_OBSERVE = "observe";
    public static final String TYPE_INDEX = "index";
    public static final String TYPE_ALARM = "alarm";
    public static final String TYPE_AIR = "air";
    public static final String TYPE_HOUR_FC = "hourfc";
    public static final String TYPE_PAST_WEATHER = "pastweather";

    /**
     * request appid value
     */
    public static final String APP_ID_VALUE = "d7018a";
    public static final String APP_ID_VALUE_KEY = "d7018aee10b7e9f1";
    public static final String APP_PRIVATE_KEY = "tcl_data";

    /**
     * date string format
     */
    public static final String DATE_STR_FORMAT = "yyyyMMddHHmm";
    public static final String DATE_STR_FORMAT_NO_MINUTE = "yyyyMMdd";

    private static final String[] ARGS_ORDER = new String[]{AREA_ID, CITY, LON, LAT, TYPE, DATE, APP_ID, KEY};


    public static String createSignature(String data) {
        try {
            byte[] keyBytes = APP_PRIVATE_KEY.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());

            return new String(Base64.encode(rawHmac, Base64.NO_WRAP));
        } catch (Exception e) {
            Debugger.DEBUG_D(true, TAG, "createSignature failed: ", e.toString());
        }
        return "";
    }


    public static String buildUrl(String root, HashMap<String, String> params) {
        try {
            StringBuilder url = new StringBuilder(root);
            url.append("?");
            String value;
            for (String key : ARGS_ORDER) {
                value = params.get(key);
                if (!TextUtils.isEmpty(value)) {
                    url.append(key);
                    url.append("=");
                    url.append(URLEncoder.encode(value, "utf-8"));
                    url.append("&");
                }
            }
            String realUrl = url.toString();
            return realUrl.substring(0, realUrl.length() - 1);
        } catch (Exception e) {
            Debugger.DEBUG_D(true, TAG, "buildUrl failed: ", e.toString());
        }
        return "";
    }

    public static String appendUrlParam(String url, String key, String value) {
        try {
            StringBuilder builder = new StringBuilder(url);
            builder.append("&");
            builder.append(key);
            builder.append("=");
            builder.append(URLEncoder.encode(value, "utf-8"));
            return builder.toString();
        } catch (Exception e) {
            Debugger.DEBUG_D(true, TAG, "buildUrl failed: ", e.toString());
        }
        return "";
    }

    public static String createCurrentDateYYYYMMDDHHSS() {
        return formateDate(new Date());
    }

    public static String createYesterdayDateHourMinutes() {
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(new Date().getTime());
        cl.add(Calendar.DATE, -1);
        Date date = new Date(cl.getTimeInMillis());
        return formateDate(date);
    }

    public static String formateDate(Date date) {
        SimpleDateFormat formate = new SimpleDateFormat(DATE_STR_FORMAT);
        return formate.format(date);
    }

    public static String formateDateNoMinute(Date date) {
        SimpleDateFormat formate = new SimpleDateFormat(DATE_STR_FORMAT_NO_MINUTE);
        return formate.format(date);
    }

    public static Date parseDate(String date) {
        try {
            SimpleDateFormat formate = new SimpleDateFormat(DATE_STR_FORMAT);
            return formate.parse(date);
        } catch (ParseException e) {
            Debugger.DEBUG_D(true, TAG, "buildUrl failed: ", e.toString());
        }
        return new Date();
    }


    public static int getWeatherNoIcon(int weatherNo) {
        return R.drawable.tcl_cloudy_icon;
    }

}
