/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created by thundersoft on 16-7-28.
 */
public class FindCityParam extends AbsProviderDataParam {
    private final static String TAG = "FindCityParam";

    private final City mCity;

    public FindCityParam(City city) {
        mCity = city;
    }


    public City getRequestCity() {
        return mCity;
    }

    @Override
    protected void onDataScan(final ProviderDataService service) {
        try {
            DbTableCityData cityData = new DbTableCityData();
            cityData.setCity(mCity, false);
            Dao<DbTableCityData, Integer> tableCityDao = service.getDbDao(DbTableCityData.class);
            DbTableCityData dbCityData = ProviderDataService.queueryForDbTableCityData(tableCityDao, cityData);
            if (null != dbCityData) {
                mCity.setCity(dbCityData.getCity());
                service.requestCallback(this, null);
            } else {
                service.requestCallback(this, new DataParam.RequestError(RequestError.ERR_TYPE_DB_NULL, RequestError.ERR_TYPE_DB_NULL_STR));
            }

        } catch (SQLException e) {
            service.requestCallback(this, new DataParam.RequestError(DataParam.RequestError.ERR_TYPE_DB_ERR, "TAG:" + TAG + "-----location key is: " + mCity.getLocationKey()));
        }
    }
}
