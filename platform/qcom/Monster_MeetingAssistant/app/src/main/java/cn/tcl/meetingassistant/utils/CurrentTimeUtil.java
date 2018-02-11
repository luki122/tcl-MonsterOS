/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import java.util.Calendar;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-10.
 * get the detail int num of now time
 */
public class CurrentTimeUtil {
    public static Calendar calendar;

    public static int getYear() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getDay() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


    public static int getHour() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR);
    }

    public static int getMinute() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.MINUTE);
    }

    public static int getSecond() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.SECOND);
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return String.format("%d-%02d-%02d %02d:%02d", year, month, day, hour, minute);
    }

    public static String getTimeAfterNHour(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar. HOUR ,n );
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return String.format("%02d:%02d", hour, minute);
    }

    public static String getDateAfterNDay(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH , Calendar.DAY_OF_MONTH + n );
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%02d-%02d-%02d", year, month, day);
    }

    public static String getCurrentTimeByMillisecond(){
        return  String.format("%d%02d%02d%02d%02d%02d",
                CurrentTimeUtil.getYear(), CurrentTimeUtil.getMonth(), CurrentTimeUtil.getDay(),
                CurrentTimeUtil.getHour(), CurrentTimeUtil.getMinute(),CurrentTimeUtil.getSecond());
    }
}
