package cn.tcl.weather.provider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.provider.cityname.CityNameBean;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-12.
 * $desc
 */
public class FindCityListParam extends AbsProviderDataParam {
    private final static String TAG = "FindCityListParam";

    private String mName;
    private String mLang;

    private List<City> mCities = new ArrayList<>();

    public FindCityListParam(String name, String lang) {
        mName = name;
        mLang = lang;
    }


    public List<City> getCities() {
        return mCities;
    }


    @Override
    protected void onDataScan(ProviderDataService service) {
        try {
            List<CityNameBean> beans = service.getCityDataHelper().queueForList(mName);
            List<City> cities = new ArrayList<>(beans.size());
            for (CityNameBean bean : beans) {
                cities.add(bean.getCity(mLang));
            }
            mCities = cities;
            service.requestCallback(this, null);
        } catch (SQLException e) {
            service.requestCallback(this, new DataParam.RequestError(DataParam.RequestError.ERR_TYPE_DB_ERR, "TAG:" + TAG + "-----name is: " + mName));
        }

    }
}
