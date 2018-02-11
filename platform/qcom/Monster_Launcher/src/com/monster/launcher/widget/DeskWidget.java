package com.monster.launcher.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.monster.launcher.DeskWidgetActivity;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.Log;
import com.monster.launcher.R;

import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class DeskWidget extends AppWidgetProvider {
    private static String DEFAULT_STRING ="摘下怀恋"+"\n"+"记住美妙";
    private static String mEditString ;
    private static final int mLineNum=7;
    private static SharedPreferences mPreferences;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Bundle extras = new Bundle();
        String format = new Date().toString();
        String[] strings = format.split(" ");
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.desk_widget);
        try {
        updateTextColor(views,context);
         }catch (IllegalStateException e){
        Log.d("liuzuo910","Not change the color");
        }
        String day = strings[1].toUpperCase();
        views.setTextViewText(R.id.appwidget_day, day);
        views.setTextViewText(R.id.appwidget_month, strings[2]);
        views.setTextViewText(R.id.appwidget_year, strings[5]);
        DEFAULT_STRING=getPreferences(context).getString(DeskWidgetActivity.mKey,DEFAULT_STRING);
        String text= DEFAULT_STRING;
        if(mEditString!=null&&mEditString.length()>0) {
            text = mEditString;
        }

        format+=" "+text;
        if(text.length()>mLineNum){

        }
        Intent widgetIntent = new Intent(context, DeskWidgetActivity.class);
        extras.putString(DeskWidgetActivity.mKey,format);
        Log.d("liuzuo910","PendingIntent"+format);
        widgetIntent.putExtras(extras);
        //widgetIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle bundle = widgetIntent.getExtras();
        Log.d("liuzuo910","PendingIntent2"+bundle.getString(DeskWidgetActivity.mKey));
        Log.d("liuzuo910","setText="+text);
        views.setTextViewText(R.id.appwidget_text_one, text);
        views.setOnClickPendingIntent(R.id.appwidget_text_container,PendingIntent.getActivity(context, 0, widgetIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.calendar",
                "com.android.calendar.LaunchActivity"));
        views.setOnClickPendingIntent(R.id.appwidget_day,PendingIntent.getActivity(context, 0, intent, 0));
        views.setOnClickPendingIntent(R.id.appwidget_month,PendingIntent.getActivity(context, 0, intent, 0));
        views.setOnClickPendingIntent(R.id.appwidget_year,PendingIntent.getActivity(context, 0, intent, 0));

        // Instruct the widget manager to update the widget
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }catch (Resources.NotFoundException e){
            Log.e("liuzuo55","widget Resources.NotFoundException");
        }
    }

    private static void updateTextColor(RemoteViews views,Context context) {
        int color = LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
        Resources res = context.getResources();
       if(color!=-1){
            int colorText = res.getColor(R.color.desk_widget_text_color_black);
            views.setTextColor(R.id.appwidget_day, colorText);
            views.setTextColor(R.id.appwidget_month, colorText);
            views.setTextColor(R.id.appwidget_year, colorText);
            views.setTextColor(R.id.appwidget_text_one, colorText);
            // mDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            views.setInt(R.id.appwidget_container,"setBackgroundResource",R.drawable.desk_widget_stroke_black);
            views.setInt(R.id.widget_cross,"setBackgroundResource",R.drawable.cross_shaped_black);
        }else{
            int colorText = res.getColor(R.color.desk_widget_text_color_white);
            views.setTextColor(R.id.appwidget_day, colorText);
            views.setTextColor(R.id.appwidget_month, colorText);
            views.setTextColor(R.id.appwidget_year, colorText);
            views.setTextColor(R.id.appwidget_text_one, colorText);
            views.setInt(R.id.appwidget_container,"setBackgroundResource",R.drawable.desk_widget_stroke_white);
            views.setInt(R.id.widget_cross,"setBackgroundResource",R.drawable.cross_shaped_white);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("liuzuo910","onUpdate");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            Log.d("liuzuo910","onUpdate appWidgetId="+appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("liuzuo910","onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.d("liuzuo910","onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("liuzuo910","onReceive");
        if(DeskWidgetActivity.ACTION_DESKWIDGET_CUSTOM_TEXT.equals(intent.getAction())){

            Bundle extras = intent.getExtras();
            mEditString = extras.getString(DeskWidgetActivity.mKey);
            updateSharedPreferences(mEditString,context);
        }
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if(manager!=null){
            ComponentName reminderProvider  = new ComponentName(context, DeskWidget.class);
            int[] appWidgetIds = manager.getAppWidgetIds(reminderProvider);
            for (int appWidgetId : appWidgetIds) {
                    updateAppWidget(context, manager, appWidgetId);
            }
        }

    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        Log.d("liuzuo910","onRestored");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d("liuzuo910","onDeleted");
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d("liuzuo910","onAppWidgetOptionsChanged");
    }
    private void updateSharedPreferences(String text,Context context){
        getPreferences(context).edit().putString(DeskWidgetActivity.mKey,text).apply();
    }

    private static SharedPreferences getPreferences(Context context){
        if(mPreferences==null){
            mPreferences=context.getSharedPreferences(DeskWidgetActivity.ACTION_DESKWIDGET_CUSTOM_TEXT,Context.MODE_PRIVATE);
        }
        return mPreferences;
    }
}

