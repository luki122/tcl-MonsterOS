/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import java.util.ArrayList;

import cn.tcl.weather.OtherWeatherWarningActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.TclWeatherWarningActivity;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityWeatherWarning;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-5.
 * Send notification
 */
public class NotificationHelper {
    private static final int S_ID = 1001;


    public final static void createNotifycation(Context context, City city) {
        ArrayList<CityWeatherWarning> warnings = city.getCityWeatherWarnings();
        if (city.isLocateCity() && !warnings.isEmpty()) {
            CityWeatherWarning warning = warnings.get(0);
            Notification.Builder builder = new Notification.Builder(context);
            Intent intent = new Intent();
            if (WeatherCNApplication.getWeatherCnApplication().getCurrentSystemType() == WeatherCNApplication.SYSTEM_TYPE_LONDON) {
                intent.setClass(context, TclWeatherWarningActivity.class);
            } else {
                intent.setClass(context, OtherWeatherWarningActivity.class);

            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(OtherWeatherWarningActivity.WARN_PARAM, warning);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // Set notification
            builder.setSmallIcon(R.drawable.weather_icon);
            builder.setContentTitle(((warning.warnCity.equals("")) ? warning.warnProvince : warning.warnCity)
                    + context.getString(R.string.txt_release) + warning.getWarnCategoryName() + warning.warnGradeName + context.getString(R.string.txt_early_warning));
            builder.setContentText(warning.warnContent);
            builder.setContentIntent(pendingIntent);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_icon));
            builder.setAutoCancel(true);

            // Create NotificationManager
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // TODO: 16-9-19 Via content to judge S_ID
            notificationManager.notify(S_ID, builder.build());
        }
    }
}
