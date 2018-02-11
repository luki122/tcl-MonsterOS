package com.android.deskclock.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.android.deskclock.net.tcl.LegalHolidayResponseBody;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

public class SharePreferencesUtils {

    public static HashMap<String, String> workdayAlarmMap = new HashMap<String, String>();

    public static void setSkipThisALarm(Context context, long id, boolean is) {
        if(context ==null){
            return;
        }

        SharedPreferences mySharedPreferences = context.getSharedPreferences("skip_alarm", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("" + id, is);
        editor.commit();
    }

    public static boolean isSkipThisAlarm(Context context, long id) {
        if(context ==null){
            return false;
        }

        SharedPreferences mySharedPreferences = context.getSharedPreferences("skip_alarm", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("" + id, false);
    }

    public static void setWorkDayALarm(Context context, long id, boolean is) {
        if(context ==null){
            return;
        }
        SharedPreferences mySharedPreferences = context.getSharedPreferences("workday_alarm", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("" + id, is);
        editor.commit();
    }

    public static boolean isWorkDayALarm(Context context, long id) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("workday_alarm", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("" + id, false);
    }
    public static void setNoWorkDayALarm(Context context, long id, boolean is) {
        if(context ==null){
            return;
        }

        SharedPreferences mySharedPreferences = context.getSharedPreferences("no_workday_alarm", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("" + id, is);
        editor.commit();
    }
    
    public static boolean isNoWorkDayALarm(Context context, long id) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("no_workday_alarm", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("" + id, false);
    }

    public static void setRemindLaterALarm(Context context, long id, boolean is) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("remind_later", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("" + id, is);
        editor.commit();
    }

    public static boolean istRemindLaterALarm(Context context, long id) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("remind_later", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("" + id, false);
    }

    public static void setSleepCount(Context context, long id, int count) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("sleep_count", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt("" + id, count);
        editor.commit();
//        editor.apply();//异步操作
    }

    public static int getSleepCount(Context context, long id) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("sleep_count", Activity.MODE_PRIVATE);
        return mySharedPreferences.getInt("" + id, 0);
    }

    public static void setAlarmVibrate(Context context, boolean is) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("alarm_vibrate", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("alarm_vibrate", is);
        editor.commit();
    }

    public static boolean isAlarmVibrate(Context context) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("alarm_vibrate", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("alarm_vibrate", true);
    }
    
    public static void setInterferenceFee(Context context, boolean is) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("interference_fee", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("interference_fee", is);
        editor.commit();
    }
    
    public static boolean isInterferenceFee(Context context) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("interference_fee", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("interference_fee", false);
    }

    public static HashMap<String, String> getWorkDayAlarmMap(Context context) {//1响铃 0 不响铃
        if (workdayAlarmMap.size() > 0) {
            return workdayAlarmMap;
        }
//        workdayAlarmMap.put("2016-08-22", "1");
//        workdayAlarmMap.put("2016-08-23", "1");
//        workdayAlarmMap.put("2016-08-24", "1");
//        workdayAlarmMap.put("2016-08-25", "0");
//        workdayAlarmMap.put("2016-08-26", "0");
//        workdayAlarmMap.put("2016-08-27", "0");
//        workdayAlarmMap.put("2016-08-28", "1");
//        workdayAlarmMap.put("2016-08-29", "0");
//        workdayAlarmMap.put("2016-08-30", "1");
//        workdayAlarmMap.put("2016-08-31", "0");
        getHolidayMap(context);
        return workdayAlarmMap;

    }

    public static boolean shouldAlarm(Context context,Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String str_month;
        if (month < 10) {
            str_month = "0" + month;
        } else {
            str_month = "" + month;
        }
        String str_day;
        if (day < 10) {
            str_day = "0" + day;
        } else {
            str_day = "" + day;
        }
        String key = year + "-" + str_month + "-" + str_day;
        String temp = getWorkDayAlarmMap(context).get(key);
        if (temp == null) {//没找到 就周六周日响
            int week_index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (week_index < 0) {
                week_index = 0;
            }
            if (week_index == 0 || week_index == 6) {
                return false;
            } else {
                return true;
            }
        } else if (temp.equals("0")) {
            return false;
        }
        return true;
    }
    
    public static void setShowThisCity(Context context, String mCityId, boolean is) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("show_city", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        if(is){
            editor.putBoolean(mCityId, is);
        } else {
            editor.remove(mCityId);
        }
        editor.commit();
    }

    public static boolean isSHowThisCity(Context context, String mCityId) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("show_city", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean(mCityId, false);
    }
    
    public static void saveHolidayMap(Context context,String key,String value){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("my_holiday", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    public static void getHolidayMap(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("my_holiday", Activity.MODE_PRIVATE);
        workdayAlarmMap = (HashMap<String, String>)mySharedPreferences.getAll();
    }
    
    
    public static void saveHolidayToLocal(Context context,LegalHolidayResponseBody data){
        
        List<Integer> holidayList = data.getHolidayList();
        List<Integer> workdayList = data.getWorkdayList();
        
        if(holidayList !=null){
            for(int i=0;i<holidayList.size();i++){
                int get_time = holidayList.get(i);
                Time mTime = new Time();
                mTime.setJulianDay(get_time);
                Date mDate = new Date(mTime.toMillis(true));
                SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                String sDateTime = sdf.format(mDate);
                workdayAlarmMap.put(sDateTime, "0");
                saveHolidayMap(context,sDateTime,"0");
            }
        }
        
        if(workdayList !=null){
            for(int i=0;i<workdayList.size();i++){
                int get_time = workdayList.get(i);
                Time mTime = new Time();
                mTime.setJulianDay(get_time);
                Date mDate = new Date(mTime.toMillis(true));
                SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                String sDateTime = sdf.format(mDate);
                workdayAlarmMap.put(sDateTime, "1");
                saveHolidayMap(context,sDateTime,"1");
            }
        }

        
    }
    
    public static void setUpdateHolidayLab(Context context){
        Calendar  calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String str_month;
        if (month < 10) {
            str_month = "0" + month;
        } else {
            str_month = "" + month;
        }
        String str_day;
        if (day < 10) {
            str_day = "0" + day;
        } else {
            str_day = "" + day;
        }
        String key = year + "-" + str_month + "-" + str_day;
        
        SharedPreferences mySharedPreferences = context.getSharedPreferences("have_update_this_day", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.clear();
        editor.putBoolean(key, true);
        editor.commit();
    }
    
    public static boolean isUpdatedThisDay(Context context){
        Calendar  calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String str_month;
        if (month < 10) {
            str_month = "0" + month;
        } else {
            str_month = "" + month;
        }
        String str_day;
        if (day < 10) {
            str_day = "0" + day;
        } else {
            str_day = "" + day;
        }
        String key = year + "-" + str_month + "-" + str_day;
        
        SharedPreferences mySharedPreferences = context.getSharedPreferences("have_update_this_day", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean(key, false);

    }


}
