/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.VolleyNetHolder;
import cn.tcl.weather.notification.NotificationHelper;
import cn.tcl.weather.provider.DataParam;
import cn.tcl.weather.provider.FindCityParam;
import cn.tcl.weather.provider.IDataService;
import cn.tcl.weather.provider.ProviderDataService;
import cn.tcl.weather.provider.StoreCityParam;
import cn.tcl.weather.utils.ThreadHandler;
import cn.tcl.weather.utils.store.ClassStore;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created by on 16-8-1.
 * <p/>
 * this class manager cities what are added. when you add a city ,
 * it will search from db first, then net
 */
class DefaultCityManager implements ICityManager {
    public final static String TAG = "DefaultCityManager";

    private final static int DELAY_TIME = 70;

    private final static String LANGUAGE_KEY = "DefaultCityManager_language";

    private List<CityObserver> mCityObservers = new ArrayList<>(8);

    private ThreadHandler mThreadHandler;

//    private ReentrantReadWriteLock.WriteLock mWriteLock;
//    private ReentrantReadWriteLock.ReadLock mReadLock;

    private final Context mContext;

    private IDataService mDataService;//sql service

    private ClassStore mClsStore;// cls store (sharedperference)

    private CityStoreData mCityStoreData;

    private IWeatherNetHolder mWeatherNetHolder;// weather requester for net

    private String mLanguage;//default value

    private Handler mMainHandler;

    private boolean isInit;

    public DefaultCityManager(Context context, Handler mainHandler) {
        mContext = context;
        mMainHandler = mainHandler;
        mThreadHandler = new ThreadHandler("UpdateService", android.os.Process.THREAD_PRIORITY_AUDIO);
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//        mWriteLock = lock.writeLock();
//        mReadLock = lock.readLock();
        mDataService = new ProviderDataService(context, mainHandler, mThreadHandler);
        mClsStore = new ClassStore(context);
        mWeatherNetHolder = new VolleyNetHolder(context, mainHandler);
//        mWeatherNetHolder = new MockWeatherNetHolder(mainHandler);// add weather mock data
        mCityStoreData = new CityStoreData(this, mainHandler);
    }

    IWeatherNetHolder getWeatherNetHolder() {
        return mWeatherNetHolder;
    }

    String getCurrentLanguage() {
        return mLanguage;
    }


    CityStoreData getCityStoreData() {
        return mCityStoreData;
    }


    Context getContext() {
        return mContext;
    }

    @Override
    public boolean hasAddedCity() {
        return mCityStoreData.hasAddedCity();
    }

    @Override
    public boolean isCityFull() {
        return mCityStoreData.isFull();
    }

    @Override
    public City getLocationCity() {
        List<City> cities = listAllCity();
        if (cities.isEmpty())
            return null;
        return cities.get(0);
    }

    @Override
    public List<City> listAllCity() {
        return mCityStoreData.listAllCity();
    }

    @Override
    public void addCity(City city) {
        if (mCityStoreData.hasCity(city)) {
            city = mCityStoreData.getCity(city);
            onAddCityCallback(city, CityObserver.ADD_STATE_HAS_ADDED);
            new DataController(city, true).request();
        } else if (mCityStoreData.isFull()) {
            onAddCityCallback(city, CityObserver.ADD_STATE_FULL);
        } else {
//            new DataController(city, false).request();
            new NetWeatherCb(city, false).request();
        }
    }

    /**
     * update city from db
     *
     * @param city
     */
    void updateCityFromeDb(City city) {
        new DataController(city, true).request();
    }

    @Override
    public void removeCity(City city) {
        onRemoveCityCallback(city, CityObserver.REMOVE_STATE_START);
        onRemoveCityCallback(city, CityObserver.REMOVE_STATE_REMOVING);
        mCityStoreData.removeCity(city);
        mClsStore.store(mCityStoreData);
        onRemoveCityCallback(city, CityObserver.REMOVE_STATE_REMOVED);
    }

    @Override
    public void addCity(City city, int index) {
        if(mCityStoreData.addCity(city, index)){
            mClsStore.store(mCityStoreData);
            callOnCityListChanged();
        }
    }

    @Override
    public void exchangeCity(City firstCity, City secondCity) {
        if (firstCity != secondCity) {
            mCityStoreData.changeCity(firstCity, secondCity);
            mClsStore.store(mCityStoreData);
            callOnCityListChanged();
        }
    }

    @Override
    public void changeCityPosition(City city, int dstPosition) {
        if(mCityStoreData.moveCityToPosition(city,dstPosition)){
            mClsStore.store(mCityStoreData);
            callOnCityListChanged();
        }
    }

    @Override
    public void addCityObserver(final CityObserver observer) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCityObservers.add(observer);
                observer.onCityListChanged();
            }
        });
    }

    @Override
    public void removeCityObserver(final CityObserver observer) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCityObservers.remove(observer);
            }
        });
    }

    @Override
    public void init() {
        if (!isInit) {
            mClsStore.init();
            mThreadHandler.init();
            mDataService.init();
            mWeatherNetHolder.init();

            initCityStoreData();
            checkCurrentLanguage();
            isInit = true;
        }
    }

    /**
     * get thread handler
     *
     * @return
     */
    @NonNull
    ThreadHandler getThreadHandler() {
        return mThreadHandler;
    }

    /**
     * post runnable to thread handler
     *
     * @param runnable
     * @param delayTimeMills
     */
    void postOnThreadHandler(Runnable runnable, int delayTimeMills) {
        mThreadHandler.post(runnable, delayTimeMills);
    }

    /**
     * remove runnable from thread handler
     *
     * @param runnable
     */
    void removeOnThreadHandler(Runnable runnable) {
        mThreadHandler.remove(runnable);
    }

    @Override
    public String checkCurrentLanguage() {
        if (TextUtils.isEmpty(mLanguage)) {
            mLanguage = mClsStore.getRealStore().read(LANGUAGE_KEY);
        }
        String curLanguage = getLanguage();
        if (!curLanguage.equals(mLanguage)) {// if language changed
            mLanguage = curLanguage;
            mClsStore.getRealStore().store(LANGUAGE_KEY, mLanguage);
        }
        mCityStoreData.setCurrentLanguage(mLanguage);
        return mLanguage;
    }

    @Override
    public void updateAllCitiesInfos() {
        mCityStoreData.updateAllCitiesRunnable(true);
    }

    /*store cities*/
    private void initCityStoreData() {
        CityStoreData data = mClsStore.read(CityStoreData.class);
        if (null != data) {
            data.initStoreData(this, mMainHandler);
            mCityStoreData = data;
        }
    }

    @Override
    public void recycle() {
        if (isInit) {
            isInit = false;
            mWeatherNetHolder.recycle();
            mClsStore.recycle();
            mDataService.recycle();
            mThreadHandler.recycle();
            mMainHandler = null;
        }
    }

    IDataService getDataService() {
        return mDataService;
    }


    @Override
    public void onTrimMemory(int level) {
        mClsStore.onTrimMemory(level);
        mDataService.onTrimMemory(level);
        mThreadHandler.onTrimMemory(level);
    }

    void onAddCityCallback(final City city, final int state) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (CityObserver ob : mCityObservers) {
                    ob.onCityAdding(city, state);
                }
                if (state == CityObserver.ADD_STATE_ADDED || state == CityObserver.ADD_STATE_LOCATE_ADDED) {//if add succeed, should call city list changed
                    callOnCityListChanged();
                }
            }
        });

    }

    private void callOnCityListChanged() {
        if (null != mMainHandler) {
            mMainHandler.removeCallbacks(onCityListChangedRunnable);
            mMainHandler.postDelayed(onCityListChangedRunnable, DELAY_TIME);
        }
    }

    void onRemoveCityCallback(final City city, final int state) {

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (CityObserver ob : mCityObservers) {
                    ob.onCityRemoving(city, state);
                }
                if (state == CityObserver.REMOVE_STATE_REMOVED) {//if remove succeed, should call city list changed
                    callOnCityListChanged();
                }
            }
        });

    }

    void onUpdateCityCallback(final City city, final int state) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (CityObserver ob : mCityObservers) {
                    ob.onCityUpdate(city, state);
                }
                if (state == CityObserver.UPDATE_STATE_SUCCEED) {
                    callOnCityListChanged();
                }
            }
        });

    }

    /**
     * add city to storages
     *
     * @param city
     * @param isStoreToDb
     * @return true the city is a new city
     */
    boolean requestCitySucceed(City city, boolean isStoreToDb) {
        city.isRefreshSucceed = true;
        boolean isAdded = mCityStoreData.addCity(city);
        if (isAdded) {
            mClsStore.store(mCityStoreData);
        }
        if (isStoreToDb) {
            city.refreshTimeMills = System.currentTimeMillis();
            StoreCityParam storeCityParam = new StoreCityParam(city);
            mDataService.requestData(storeCityParam);
        }
        return isAdded;
    }


    boolean requestCityFailed(City city) {
        City c = mCityStoreData.getCity(city);
        if (null != c && c.isRefreshSucceed) {
            c.isRefreshSucceed = false;
            StoreCityParam storeCityParam = new StoreCityParam(city);
            mDataService.requestData(storeCityParam);
            return false;
        }
        return true;
    }

    /*
     * the city info will get db data first then from net
     */
    private class DataController implements DataParam.OnRequestDataListener {
        private City mCity;
        private boolean isUpdateCallback;

        DataController(City city, boolean isUpdateCallback) {
            mCity = city;
            this.isUpdateCallback = isUpdateCallback;
            mCity.setLanguage(mLanguage);
        }

        void request() {
            if (!isUpdateCallback) {
                onAddCityCallback(mCity, CityObserver.ADD_STATE_START);
                onAddCityCallback(mCity, CityObserver.ADD_STATE_ADDING);
            }
            FindCityParam findCityParam = new FindCityParam(mCity);
            findCityParam.setOnRequestDataListener(this);
            mDataService.requestData(findCityParam);
        }

        @Override
        public void onRequestDataCallback(DataParam param) {
            if (param instanceof FindCityParam) {
                FindCityParam findCityParam = (FindCityParam) param;
                if (mCity != findCityParam.getRequestCity())
                    mCity.setCity(findCityParam.getRequestCity());
                requestCitySucceed(mCity, false);
                if (isUpdateCallback) {
                    onUpdateCityCallback(mCity, CityObserver.UPDATE_STATE_SUCCEED);
                } else {
                    onAddCityCallback(mCity, CityObserver.ADD_STATE_ADDED);
                }
                new NetWeatherCb(findCityParam.getRequestCity(), true).request();
            }
        }

        @Override
        public void onRequestDataFailed(DataParam param, DataParam.RequestError error) {
            if (param instanceof FindCityParam) {
                FindCityParam findCityParam = (FindCityParam) param;
                new NetWeatherCb(findCityParam.getRequestCity(), isUpdateCallback).request();
            }
        }
    }

    /*
     * net work data
     */
    class NetWeatherCb implements IWeatherNetHolder.IWeatherNetCallback {
        private City mCity;
        private boolean isUpdateCallback;
//        private NotificationHelper mNotificationHelper;

        NetWeatherCb(City city, boolean isUpdateCallback) {
            this.isUpdateCallback = isUpdateCallback;
            mCity = city;
            mCity.setLanguage(mLanguage);
        }

        void request() {
            if (!isUpdateCallback) {
                onAddCityCallback(mCity, CityObserver.ADD_STATE_START);
                onAddCityCallback(mCity, CityObserver.ADD_STATE_ADDING);
            }
            mWeatherNetHolder.requestCityByCity(mCity, mLanguage, this);
        }

        @Override
        public void onReceivedData(String action, Object obj) {
            City city = (City) obj;
            mCity.setCity(city);
            requestCitySucceed(mCity, true);
            if (isUpdateCallback) {
                onUpdateCityCallback(mCity, CityObserver.UPDATE_STATE_SUCCEED);
            } else {
                onAddCityCallback(mCity, CityObserver.ADD_STATE_ADDED);
            }

            NotificationHelper.createNotifycation(mContext, mCity);
        }

        @Override
        public void onFailed(String action, int state) {
            requestCityFailed(mCity);
            if (isUpdateCallback) {
                onUpdateCityCallback(mCity, CityObserver.UPDATE_STATE_FAILED);
            } else {
                onAddCityCallback(mCity, CityObserver.ADD_STATE_FAILED);
            }
        }
    }

    /*
     * get current langguage
     * @reutrn not null
     */
    private static String getLanguage() {
//        try {
//            String localLanguage = Locale.getDefault().getLanguage();
//            String localCountry = Locale.getDefault().getCountry().toLowerCase();
//
//            String langcode = localLanguage + "-" + localCountry;
//            if (langcode.contains("zh")) {//if is in china
//                return "zh-cn";
//            }
//        } catch (Exception e) {
//            Debugger.DEBUG_D(true, TAG, e.toString());
//        }
//        return "en";// else is english
        return "zh-cn";//all return "zh-cn"
    }

    private Runnable onCityListChangedRunnable = new Runnable() {
        @Override
        public void run() {
            for (CityObserver ob : mCityObservers) {
                ob.onCityListChanged();
            }
        }
    };

}


