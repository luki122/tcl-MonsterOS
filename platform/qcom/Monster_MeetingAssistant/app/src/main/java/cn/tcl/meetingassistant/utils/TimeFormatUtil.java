/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;


import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-10.
 * format tool of time
 */
public class TimeFormatUtil {
    private final static String TAG = TimeFormatUtil.class.getCanonicalName();

    private static final SimpleDateFormat sDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat sTimeFormatter = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd\b\bHH:mm");
    private static final SimpleDateFormat sMMddFormatter = new SimpleDateFormat("MM-dd HH:mm");
    private static final SimpleDateFormat sPointFotmatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private static final SimpleDateFormat sHourMuniteSecondFormatter = new SimpleDateFormat("HH:mm:ss");
    public static String getDateString(Long date){
        return sDateFormatter.format(date);
    }

    public static String getTimeString(Long date){
        return sTimeFormatter.format(date);
    }

    public static String getDateTimeTimeString(Long date){
        return sFormatter.format(date);
    }

    /**
     *
     * @param timeSecond second
     * @return
     */
    public static String getHourMuniteSecondString(long timeSecond){
        long second = timeSecond % 60;
        long minute = (timeSecond / 60) % 60;
        long hour = timeSecond / 3600;
        String result = String.format("%02d:%02d:%02d",hour,minute,second);
        return result;
    }

    /**
     * get date
     * @param time "yyyy-MM-dd HH:mm"
     * @return
     */
    public static Long getDateFromString(String time){
        Long timeLong = 0L;

        try {
            timeLong = sFormatter.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            return timeLong;
        }
    }

    public static String getDateString(int y,int month,int d,int h,int m){
        return String.format("%04d-%02d-%02d",y,month,d,h,m);
    }

    public static String getPointTimeString(long time){
        return sPointFotmatter.format(time);
    }

    public static String getMMddTimeString(long time){
        return sMMddFormatter.format(time);
    }
}
