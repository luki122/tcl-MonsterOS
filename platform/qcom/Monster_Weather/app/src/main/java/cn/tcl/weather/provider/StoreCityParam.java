/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created by on 16-8-1.
 * used for store CityData to SQL
 */
public class StoreCityParam extends AbsProviderDataParam {
    private final static String TAG = "FindCityParam";

    private final City mCity;

    public StoreCityParam(City city) {
        mCity = city;
    }

    /**
     * get the request city
     *
     * @return
     */
    public City getRequestCity() {
        return mCity;
    }

    @Override
    protected void onDataScan(ProviderDataService service) {
        try {
            Dao<DbTableCityData, Integer> tableCityDao = service.getDbDao(DbTableCityData.class);
            DbTableCityData dbTable = new DbTableCityData();
            dbTable.setCity(mCity);
            ProviderDataService.storeCityDataToDb(tableCityDao, dbTable);
            service.requestCallback(this, null);
        } catch (SQLException e) {
            service.requestCallback(this, new DataParam.RequestError(DataParam.RequestError.ERR_TYPE_DB_ERR, "TAG:" + TAG + "-----location key is: " + mCity.getLocationKey()));
        }
    }
}
