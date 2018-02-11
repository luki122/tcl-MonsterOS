package cn.tcl.weather.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 16-9-18.
 */
public class DateFormatUtils {

    private final static String TAG = DateFormatUtils.class.getName();
    private final static int MILLS_MINUTE = 60 * 1000;
    private final static int MILLS_HOUR = 60 * MILLS_MINUTE;
    private final static int MILLS_DAY = 24 * MILLS_HOUR;


    public static Date parseStringToDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        try {
            if (!TextUtils.isEmpty(str)) {
                date = sdf.parse(str);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "date parse exception : ", e);
        }
        return date;
    }

    public static String parseDateToString(long timeMili) {
        Date date = new Date(timeMili);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return sdf.format(date);
    }


    public static String caculateOffsetTime(String yyyyMMddHHmm, int offsetDays, int offsetHours, int offsetMinutes) {
        return caculateOffsetTime(yyyyMMddHHmm, offsetDays * MILLS_DAY + offsetHours * MILLS_HOUR + offsetMinutes * MILLS_MINUTE);
    }


    public static String caculateOffsetTime(String yyyyMMddHHmm, int offsetmm){
        return caculateOffsetTime(yyyyMMddHHmm,"yyyyMMddHHmm",offsetmm);
    }


    public static String caculateOffsetTime(String timeString,String timeFormate, int offsetDays, int offsetHours, int offsetMinutes) {
        return caculateOffsetTime(timeString, timeFormate, offsetDays * MILLS_DAY + offsetHours * MILLS_HOUR + offsetMinutes * MILLS_MINUTE);
    }

    public static String caculateOffsetTime(String timeString,String timeFormate, int offsetmm) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(timeFormate);
            Date dateTime = format.parse(timeString);
            long dateTimeL = dateTime.getTime();
            return format.format(new Date(dateTimeL + offsetmm));
        } catch (Exception e) {
            LogUtils.e(TAG, "caculateOffsetTime parse exception : ", e);
        }
        return "";
    }


}
