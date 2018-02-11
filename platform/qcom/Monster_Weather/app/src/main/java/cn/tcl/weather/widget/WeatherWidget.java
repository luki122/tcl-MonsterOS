/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.tcl.weather.EntranceActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.internet.StatusAir;
import cn.tcl.weather.internet.StatusWeather;
import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.service.WidgetService;
import cn.tcl.weather.utils.LogUtils;
import cn.tcl.weather.utils.WallPaperUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-8.
 * the widget for weather app
 */
public class WeatherWidget extends AppWidgetProvider {

    public final static String TAG = WeatherWidget.class.getName();
    private final static int WHITE_THEAM_ID = R.layout.other_widget_layout_white_bg;
    private final static int BLACK_THEAM_ID = R.layout.other_widget_layout_black_bg;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            WidgetService.startWidgetService(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetService.startWidgetService(context);
    }


    public static void updateWidgets(Context context, ICityManager cityManager) {
        City city = null;
        if (null != cityManager) {
            city = cityManager.getLocationCity();
        }
        if (null != city) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidget.class));
            if (null != appWidgetIds && appWidgetIds.length > 0) {
                int tagetTheamId = getTargetTheamId();
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), tagetTheamId);
                CityWeatherInfo weatherInfo = city.getCityWeatherInfo();
                String weatherNo = weatherInfo.weatherNo;
                try {
                    Calendar cl = Calendar.getInstance();
                    Date date = new Date();
                    cl.setTimeInMillis(date.getTime());
                    SimpleDateFormat df = new SimpleDateFormat("MMM", Locale.ENGLISH);
                    String year = cl.get(Calendar.YEAR) + "";
                    String month = df.format(date).toUpperCase();
                    String day = cl.get(Calendar.DATE) + "";
                    StringBuilder wetherTemp = new StringBuilder();
                    wetherTemp.append(StatusWeather.getWeatherStatus(weatherNo)).append(" ").
                            append(weatherInfo.getTempWithSymbol());
                    String airState = StatusAir.getAirStrByAQIValue(weatherInfo.aqiValue);
                    int weatherStatusIconId;
                    if (tagetTheamId == WHITE_THEAM_ID) {
                        weatherStatusIconId = StatusWeather.getWidgetBlackIconByNo(weatherNo);
                    } else {
                        weatherStatusIconId = StatusWeather.getWidgetwhiteIconByNo(weatherNo);
                    }
//                    remoteViews.setImageViewResource(R.id.widget_weather_icon, weatherStatusIconId);
                    remoteViews.setTextViewText(R.id.widget_month, month);
                    remoteViews.setTextViewText(R.id.widget_date, day);
                    remoteViews.setTextViewText(R.id.widget_year, year);
                    remoteViews.setTextViewText(R.id.widget_weather_temp, wetherTemp);
                    remoteViews.setTextViewText(R.id.widget_air_state, airState);
                    Intent intent = new Intent(context, EntranceActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
                } catch (Exception e) {
                    LogUtils.e(TAG,"updateWidgets: ",e);
                }
            }
        } else {//if there is no location city

        }
    }

    private static int getTargetTheamId() {
        int textColor = WallPaperUtils.calcTextColor(WallPaperUtils.getWallPaperBitmap());
        if(textColor == WallPaperUtils.CC_TEXT_BLACK_DEFAULT_COLOR){
            return WHITE_THEAM_ID;
        }
        return BLACK_THEAM_ID;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        WidgetService.startWidgetService(context);
    }

    @Override
    public void onDisabled(Context context) {
        WidgetService.stopWidgetService(context);
        super.onDisabled(context);
    }


}
