package com.monster.launcher;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by antino on 16-6-21.
 */

public class ShortcutFactory {
    private static ShortcutFactory mInstance = null;

    private ShortcutFactory() {

    }

    public static ShortcutFactory getInstance() {
        if (mInstance == null) {
            mInstance = new ShortcutFactory();
        }
        return mInstance;
    }

    public BubbleTextView createShortcut(LayoutInflater inflater, ViewGroup parent, ItemInfo info) {
        View result = null;
        if (isDeskClockApp(info)) {
            //result = inflater.inflate(R.layout.dynamic_deskclock_icon, parent, false);
            result = inflater.inflate(R.layout.app_icon, parent, false);
        } else if (isCalendarApp(info)) {
            result = inflater.inflate(R.layout.dynamic_calendar_icon, parent, false);
        } else if (isWeatherApp(info)) {
            result = inflater.inflate(R.layout.dynamic_weather_icon, parent, false);
        } else if (isDataTraffic(info)) {
            result = inflater.inflate(R.layout.dynamic_datatraffic_icon, parent, false);
        } else {
            result = inflater.inflate(R.layout.app_icon, parent, false);
        }
        if(!(info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP||info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)){
            ((BubbleTextView) result).setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextExtraColor());
        }else{
            ((BubbleTextView) result).setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
        }

        return ((BubbleTextView) result);
    }

    public BubbleTextView createIcon(LayoutInflater inflater, int resId, ViewGroup parent) {
        View result = inflater.inflate(resId, parent,false);
        ((BubbleTextView) result).setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
        return ((BubbleTextView) result);
    }

    public DragView createDragView(ItemInfo info, Launcher launcher, Bitmap bitmap, int registrationX, int registrationY,
                                   int left, int top, int width, int height, final float initialScale) {
        View result = null;
        if (isDeskClockApp(info)) {
            result = new DragView(launcher, bitmap, registrationX,
                    registrationY, 0, 0, bitmap.getWidth(), bitmap.getHeight(), initialScale);
        } else if (isCalendarApp(info)) {

        } else if (isWeatherApp(info)) {

        } else if (isDataTraffic(info)) {

        } else {
            result = new DragView(launcher, bitmap, registrationX,
                    registrationY, 0, 0, bitmap.getWidth(), bitmap.getHeight(), initialScale);
        }
        return (DragView) result;
    }


    public boolean isDeskClockApp(ItemInfo info) {
        if(info instanceof ShortcutInfo||info instanceof AppInfo){
            if (info != null && info.getIntent() != null) {
            ComponentName cn = info.getIntent().getComponent();
               if (cn != null
                    && "com.android.deskclock".equals(cn.getPackageName())
                    && "com.android.deskclock.DeskClock".equals(cn.getClassName())) {
                   return true;
               }
            }
        }
        return false;
    }

    public boolean isCalendarApp(ItemInfo info) {

      /*  if(info instanceof ShortcutInfo){
            if (info != null && info.getIntent() != null) {
                ComponentName cn = info.getIntent().getComponent();
                if (cn != null
                        && "com.android.calendar".equals(cn.getPackageName())
                        && "com.android.calendar.AllInOneActivity".equals(cn.getClassName())) {
                    return true;
                }
            }
        }*/
        return false;
    }

    public boolean isWeatherApp(ItemInfo info) {
        return false;
    }

    public boolean isDataTraffic(ItemInfo info) {
        return false;
    }
}
