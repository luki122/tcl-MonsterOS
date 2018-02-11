package cn.tcl.weather.provider.cityname;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.ICityManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-12.
 * $desc
 */
@DatabaseTable(tableName = "cities")
public class CityNameBean {

    public final static String ID = "id";
    public final static String COUNTY_EN = "county_en";
    public final static String COUNTY_CN = "county_cn";
    public final static String CITY_EN = "city_en";
    public final static String CITY_CN = "city_cn";
    public final static String PROVINCE_EN = "province_en";
    public final static String PROVINCE_CN = "province_cn";
    public final static String COUNTRY_EN = "country_en";
    public final static String COUNTRY_CN = "country_cn";


    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    String county_en;
    @DatabaseField
    String county_cn;
    @DatabaseField
    String city_en;
    @DatabaseField
    String city_cn;
    @DatabaseField
    String province_en;
    @DatabaseField
    String province_cn;
    @DatabaseField
    String country_en;
    @DatabaseField
    String country_cn;


    public City getCity(String lang) {
        City city = new City();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            city.setCity(country_cn, province_cn, city_cn, county_cn);
        } else {
            city.setCity(country_en, province_en, city_en, county_en);
        }

        city.setLocationKey(City.createLocationKeyByLocated(country_cn, province_cn, city_cn, county_cn));
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            if (o instanceof CityNameBean) {
                return id == ((CityNameBean) o).id;
            }
            return false;
        }
        return true;
    }
}
