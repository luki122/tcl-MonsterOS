/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import android.content.Context;
import android.os.*;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import cn.tcl.weather.provider.cityname.CityDataHelper;
import cn.tcl.weather.utils.FlyWeightUtils;
import cn.tcl.weather.utils.ThreadHandler;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created by thundersoft on 16-7-28.
 */
public class ProviderDataService implements IDataService {
    private Handler mMainHandler;//main thread handler
    private Context mContext;
    private ThreadHandler mThreadHandler;

    private CityDataHelper mCityDataHelper;


    public ProviderDataService(Context context, Handler mainHandler, ThreadHandler threadHandler) {
        mContext = context;
        mMainHandler = mainHandler;
        mThreadHandler = threadHandler;
    }

    @Override
    public void init() {
        mCityDataHelper = new CityDataHelper(mContext);
    }

    @Override
    public boolean requestData(DataParam params) {
        if (params instanceof AbsProviderDataParam) {
            DataRunnable runnable = mDataGarbage.getElement();
            runnable.startRequestDataParam((AbsProviderDataParam) params);
            return true;
        }
        return false;
    }

    @Override
    public void recycle() {
    }

    /**
     * get DatabaseHelper
     *
     * @return
     */
    DatabaseHelper getDatabaseHelper() {
        return DatabaseHelper.getDatabaseHelperInstance(mContext);
    }

    private FlyWeightUtils<DataRunnable> mDataGarbage = new FlyWeightUtils<DataRunnable>() {
        @Override
        protected DataRunnable newInstance() {
            return new DataRunnable();
        }
    };

    private class DataRunnable implements Runnable {
        AbsProviderDataParam iParam;
        boolean isCallback;
        DataParam.RequestError iErr;

        void startRequestDataParam(AbsProviderDataParam param) {
            iParam = param;
            iErr = null;
            this.isCallback = false;
            mThreadHandler.post(this);
        }

        void startDataParamCallback(AbsProviderDataParam param, DataParam.RequestError err) {
            iParam = param;
            iErr = err;
            this.isCallback = true;
            mMainHandler.post(this);
        }

        @Override
        public void run() {
            if (isCallback) {
                iParam.requestDataCallback(iErr);
                mDataGarbage.recycleElement(this);
            } else {
                iParam.dataScan(ProviderDataService.this);
                mDataGarbage.recycleElement(this);
            }
        }
    }

    public final void postOnMainThread(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    public final void postOnMainThread(Runnable runnable, int delayTimeMills) {
        mMainHandler.postDelayed(runnable, delayTimeMills);
    }

    public final void postOnAsynThread(Runnable runnable) {
        mThreadHandler.post(runnable);
    }

    public final void postOnAsynThread(Runnable runnable, int delayTimeMills) {
        mThreadHandler.post(runnable, delayTimeMills);
    }

    /**
     * Callback this method when you return data
     *
     * @param param
     * @param err
     */
    public final void requestCallback(final AbsProviderDataParam param, final DataParam.RequestError err) {
        DataRunnable runnable = mDataGarbage.getElement();
        runnable.startDataParamCallback(param, err);
    }


    CityDataHelper getCityDataHelper() {
        return mCityDataHelper;
    }

    <T, ID> Dao<T, ID> getDbDao(Class<T> cls) throws SQLException {
        return getDatabaseHelper().getDao(cls);
    }


    @Override
    public void onTrimMemory(int level) {
    }

    final static DbTableCityData queueryForDbTableCityData(Dao<DbTableCityData, Integer> tableCityDao, DbTableCityData tableCityData) throws SQLException {
        return tableCityDao.queryBuilder().where().eq(DbTableCityData.LOCATION_KEY, tableCityData.locationKey).and().eq(DbTableCityData.LANGUAGE, tableCityData.language).queryForFirst();
    }

    final static void storeCityDataToDb(Dao<DbTableCityData, Integer> tableCityDao, DbTableCityData tableCityData) throws SQLException {
        DbTableCityData oldTableCityData = queueryForDbTableCityData(tableCityDao, tableCityData);
        if (null == oldTableCityData) {
            tableCityDao.create(tableCityData);
        } else {
            tableCityData.setId(oldTableCityData.getId());
            tableCityDao.update(tableCityData);
        }
    }

}
