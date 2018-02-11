/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import cn.tcl.weather.widget.WeatherWidget;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-8.
 * the service for update Widget
 */
public class WidgetService extends Service {

    private final static String TAG = WidgetService.class.getName();
    public final static String UPDATE_WIDGET_ACTION = "cn.tcl.weather.update_widget";
    private UpdateService mUpdateService;

//    public WidgetService() {
//        super("WidgetService");
//    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return super.onBind(intent);
//    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindUpdateService(this);
    }

    @Override
    public void onDestroy() {
        unbindUpdateService(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            final String action = intent.getAction();
            if (UPDATE_WIDGET_ACTION.equals(action)) {
                WeatherWidget.updateWidgets(this, mUpdateService);
                if (null == mUpdateService) {
                    bindUpdateService(this);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private ServiceConnection mSerivceConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (null != mUpdateService) {
                mUpdateService.removeCityObserver(mCityObserver);
                mUpdateService = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUpdateService = ((UpdateService.UpdateBinder) service).getService();
            if (null != mUpdateService) {
                mUpdateService.addCityObserver(mCityObserver);
            }
        }

        private ICityManager.CityObserver mCityObserver = new ICityManager.CityObserver() {

            @Override
            protected void onCityListChanged() {
                WeatherWidget.updateWidgets(mUpdateService, mUpdateService);
            }
        };
    };

    private void bindUpdateService(Context context) {
        Intent bindServiceIntent = new Intent(context, UpdateService.class);
        context.bindService(bindServiceIntent, mSerivceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindUpdateService(Context context) {
        context.unbindService(mSerivceConn);
    }


    /**
     * start widget service
     *
     * @param context
     */
    public static void startWidgetService(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(WidgetService.UPDATE_WIDGET_ACTION);
        context.startService(intent);
    }

    public static void stopWidgetService(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(WidgetService.UPDATE_WIDGET_ACTION);
        context.stopService(intent);
    }

}
