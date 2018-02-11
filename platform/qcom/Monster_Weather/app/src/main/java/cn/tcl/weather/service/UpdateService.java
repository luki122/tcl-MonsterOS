/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.internet.StatusWeather;
import cn.tcl.weather.utils.LogUtils;
import cn.tcl.weather.widget.WeatherWidget;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-4.
 * $desc
 */
public class UpdateService extends Service implements ICityManager, ICityManagerSupporter, ILocator {
    public final static String START_LOCATING = "action.start.locating";

    private static final String TAG = UpdateService.class.getName();

    private final static int HOUR_TIME_MILLS = 30 * 60 * 1000;//half an hour

    public final static String REQUEST_WEATHER_STATE_ACTION = "android.intent.action.REQUEST_WEATHER_CN_STATE";
    public final static String NOTIFY_WEATHER_STATE_CHANGED_ACTION = "android.intent.action.NOTIFY_WEATHER_CN_STATE_CHANGED";
    public final static String WALLPAPER_CHANGED_ACTION = Intent.ACTION_WALLPAPER_CHANGED;

    private static final String WEATHER_CN_PACKAGE_NAME = "WeatherCNPackageName";
    private static final String WEATHER_CN_ICON_ID = "WeatherCNIconID";
    private static final String WEATHER_CN_STATUS = "WeatherCNStatus";
    private static final String WEATHER_CN_TEMP = "WeatherCNTemp";

    private DefaultCityManager mCityManager;
    private ICityManagerSupporter mCityManagerSupporter;
    private BroadcastReceiver mDataConnectChangeReceiver;
    private BroadcastReceiver mRequestWeatherStateReceiver;
    private BroadcastReceiver mWallPaperChangedReceiver;

    private LocateService mLocateService;
    private DynamicIconSwitchService mDynamicIconSwitchService;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
    }

    @Override
    public void onDestroy() {
        recycle();
        super.onDestroy();
    }

    @Override
    public boolean hasAddedCity() {
        return mCityManager.hasAddedCity();
    }

    @Override
    public boolean isCityFull() {
        return mCityManager.isCityFull();
    }

    @Override
    public City getLocationCity() {
        return mCityManager.getLocationCity();
    }

    @Override
    public List<City> listAllCity() {
        return mCityManager.listAllCity();
    }

    @Override
    public void addCity(City city) {
        mCityManager.addCity(city);
    }

    @Override
    public void removeCity(City city) {
        mCityManager.removeCity(city);
    }

    @Override
    public void addCity(City city, int index) {
        mCityManager.addCity(city, index);
    }

    @Override
    public void exchangeCity(City firstCity, City secondCity) {
        mCityManager.exchangeCity(firstCity, secondCity);
    }

    @Override
    public void changeCityPosition(City city, int dstPosition) {
        mCityManager.changeCityPosition(city, dstPosition);
    }

    @Override
    public void addCityObserver(CityObserver observer) {
        mCityManager.addCityObserver(observer);
    }

    @Override
    public void removeCityObserver(CityObserver observer) {
        mCityManager.removeCityObserver(observer);
    }

    @Override
    public String checkCurrentLanguage() {
        return mCityManager.checkCurrentLanguage();
    }

    @Override
    public void updateAllCitiesInfos() {
        mCityManager.updateAllCitiesInfos();
    }

    @Override
    public void init() {
        Handler handler = new Handler();
        mCityManager = new DefaultCityManager(this, handler);
        mCityManager.init();

        mCityManagerSupporter = new DefaultCityManagerSupporter(mCityManager);
        mCityManagerSupporter.init();

        mLocateService = new LocateService(this, handler, mCityManager);

        mLocateService.init();

        mDynamicIconSwitchService = new DynamicIconSwitchService(this);
        mDynamicIconSwitchService.init();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mSystemBraodcastReceiver, filter);

        registerConnectionChangeReceiver();

        registerRequestWeatherStateReceiver();

        registerWallPaperChangedReceiver();

        mCityManager.postOnThreadHandler(mTimerRunnable, HOUR_TIME_MILLS);// start timmer

        WidgetService.startWidgetService(this);
    }

    @Override
    public void recycle() {
        mCityManager.removeOnThreadHandler(mTimerRunnable);//remove timmer
        unregisterReceiver(mSystemBraodcastReceiver);
        mLocateService.recycle();
        unregisterReceiver(mDataConnectChangeReceiver);
        unregisterReceiver(mRequestWeatherStateReceiver);
        unregisterReceiver(mWallPaperChangedReceiver);
        mCityManagerSupporter.recycle();
        mCityManager.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mLocateService.onTrimMemory(level);
        mCityManager.onTrimMemory(level);
        super.onTrimMemory(level);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return updateBinder;
    }


    private UpdateBinder updateBinder = new UpdateBinder();


    @Override
    public void requestHotCities(OnRequestCityListListener l) {
        mCityManagerSupporter.requestHotCities(l);
    }

    @Override
    public void requestCityListByName(String name, OnRequestCityListListener l) {
        mCityManagerSupporter.requestCityListByName(name, l);
    }

    @Override
    public void requestRefreshCity(City city, OnRequestRefreshListener l) {
        mCityManagerSupporter.requestRefreshCity(city, l);
    }

    @Override
    public void regiestLocateObserver(LocateObserver observer) {
        mLocateService.regiestLocateObserver(observer);
    }

    @Override
    public void unregiestLocateObserver(LocateObserver observer) {
        mLocateService.unregiestLocateObserver(observer);
    }

    public class UpdateBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }


    /*update the cities weather info one hour a time*/
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            mCityManager.updateAllCitiesInfos();
            mCityManager.getThreadHandler().post(this, HOUR_TIME_MILLS);
        }
    };

    /*broadcast receiver for system*/
    private BroadcastReceiver mSystemBraodcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {//if the  locale config changed
                checkCurrentLanguage();//cheack the language
            }
        }
    };

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                mCityManager.updateAllCitiesInfos();
                LogUtils.i(TAG, ConnectivityManager.CONNECTIVITY_ACTION);
            }
        }
    }


    public class RequestWeatherStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(REQUEST_WEATHER_STATE_ACTION)) {
                sendWeatherStateChangeBroadcast();
            }
        }
    }

    /**
     * send Weather State Change Broadcast
     */
    public void sendWeatherStateChangeBroadcast() {
        City locateCityInfo = getLocationCity();
        if (hasAddedCity()) {// if has added city?
            if (null != locateCityInfo) {//
                Intent sendDataIntent = new Intent();
                sendDataIntent.setAction(NOTIFY_WEATHER_STATE_CHANGED_ACTION);
                String packageName = getPacagetName();
                    /*need to update after the dynamic icons gived*/
                int iconID = StatusWeather.getDynamicIconByNo(locateCityInfo.getCityWeatherInfo().weatherNo);
                String weatherStatus = StatusWeather.getWeatherStatus(locateCityInfo.getCityWeatherInfo().weatherNo);
                String temp = locateCityInfo.getCityWeatherInfo().getTempWithSymbol();
                sendDataIntent.putExtra(WEATHER_CN_PACKAGE_NAME, packageName);
                sendDataIntent.putExtra(WEATHER_CN_ICON_ID, iconID);
                sendDataIntent.putExtra(WEATHER_CN_STATUS, weatherStatus);
                sendDataIntent.putExtra(WEATHER_CN_TEMP, temp);
                LogUtils.i(TAG, packageName + " | " + iconID + " | " + weatherStatus + " | " + temp);
                sendBroadcast(sendDataIntent);
                LogUtils.i(TAG, "weather state change broadcast has been send...");
            }
        } else {// if there is no added city
            Intent sendDataIntent = new Intent();
            sendDataIntent.setAction(NOTIFY_WEATHER_STATE_CHANGED_ACTION);
            String packageName = getPacagetName();
            sendDataIntent.putExtra(WEATHER_CN_PACKAGE_NAME, packageName);
                    /*need to update after the dynamic icons gived*/
            sendDataIntent.putExtra(WEATHER_CN_ICON_ID, R.drawable.aaa_weather);
            sendBroadcast(sendDataIntent);
            LogUtils.i(TAG, "weather state change broadcast has been send...");
        }
    }

    /**
     * get package name
     *
     * @return
     */
    private String getPacagetName() {
        PackageInfo info = null;
        Context context = WeatherCNApplication.getWeatherCnApplication();
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * register Connection Change Receiver
     */
    private void registerConnectionChangeReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mDataConnectChangeReceiver = new ConnectionChangeReceiver();
        registerReceiver(mDataConnectChangeReceiver, filter);
    }

    private void registerRequestWeatherStateReceiver() {
        IntentFilter filter = new IntentFilter(REQUEST_WEATHER_STATE_ACTION);
        mRequestWeatherStateReceiver = new RequestWeatherStateReceiver();
        registerReceiver(mRequestWeatherStateReceiver, filter);
    }

    class WallPaperChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WALLPAPER_CHANGED_ACTION)) {
                WeatherWidget.updateWidgets(UpdateService.this, UpdateService.this);
                LogUtils.i(TAG, "received the broadcast of wall paper changed..." + intent.getAction());
            }
        }
    }

    private void registerWallPaperChangedReceiver() {
        IntentFilter filter = new IntentFilter(WALLPAPER_CHANGED_ACTION);
        mWallPaperChangedReceiver = new WallPaperChangedReceiver();
        registerReceiver(mWallPaperChangedReceiver, filter);
        LogUtils.i(TAG, "registerWallPaperChangedReceiver...");
    }

}
