package com.monster.cloud.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.monster.cloud.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zouxu on 16-10-26.
 */
public class SPUtil {

    public static void saveContactsSyncTime(Context context, String time) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("contacts_xml", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("contacts_sync_time", time);
        editor.commit();
    }

    public static boolean isFirstSyncContact(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("contacts_xml", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("first_sync",true);
    }

    public static void setFirstSync(Context context,boolean is){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("contacts_xml", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("first_sync", is);
        editor.commit();
    }

    public static String getContactsLastSyncTimeInfo(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("contacts_xml", Activity.MODE_PRIVATE);
        String getLastTime = mySharedPreferences.getString("contacts_sync_time","");
        if(TextUtils.isEmpty(getLastTime)){
            return context.getString(R.string.never_sync);
        }

        String last_sync = context.getString(R.string.last_sync);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date_last = null;
        try {
            date_last = dateFormat.parse(getLastTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        Date date_now = new Date();

        long temp = date_now.getTime() - date_last.getTime();    //相差毫秒数
        long seconds = temp / 1000;
        if(seconds<60){
            return last_sync+String.format(context.getString(R.string.time_sec),seconds);

        } else if(seconds>=60 && seconds<60*60){
            long min = seconds/60;
            return last_sync+String.format(context.getString(R.string.time_min),min);
        } else if(seconds>=60*60 && seconds<=60*60*24){
            int hour = date_last.getHours();
            int min = date_last.getMinutes();
            return last_sync+hour+":"+min;
        } else {
            return last_sync+getLastTime.substring(0,getLastTime.length()-2);
        }


    }

    public static boolean isContactsAutoSync(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("contacts_xml", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("contacts_auto_sync",false);

    }

    public static void setContactsAutoSync(Context context, boolean is){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("contacts_xml", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("contacts_auto_sync", is);
        editor.commit();

    }

    public static void saveSMSSyncTime(Context context, String time) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences("sms_xml", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("sms_sync_time", time);
        editor.commit();
    }

    public static String getSMSLastSyncTimeInfo(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("sms_xml", Activity.MODE_PRIVATE);
        String getLastTime = mySharedPreferences.getString("sms_sync_time","");
        if(TextUtils.isEmpty(getLastTime)){
            return context.getString(R.string.never_sync);
        }

        String last_sync = context.getString(R.string.last_sync);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date_last = null;
        try {
            date_last = dateFormat.parse(getLastTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        Date date_now = new Date();

        long temp = date_now.getTime() - date_last.getTime();    //相差毫秒数
        long seconds = temp / 1000;
        if(seconds<60){
            return last_sync+String.format(context.getString(R.string.time_sec),seconds);

        } else if(seconds>=60 && seconds<60*60){
            long min = seconds/60;
            return last_sync+String.format(context.getString(R.string.time_min),min);
        } else if(seconds>=60*60 && seconds<=60*60*24){
            int hour = date_last.getHours();
            int min = date_last.getMinutes();
            return last_sync+hour+":"+min;
        } else {
            return last_sync+getLastTime.substring(0,getLastTime.length()-2);
        }

    }

    public static boolean isSMSAutoSync(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("sms_xml", Activity.MODE_PRIVATE);
        return mySharedPreferences.getBoolean("sms_auto_sync",false);

    }

    public static void setSMSAutoSync(Context context, boolean is){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("sms_xml", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("sms_auto_sync", is);
        editor.commit();

    }


}
