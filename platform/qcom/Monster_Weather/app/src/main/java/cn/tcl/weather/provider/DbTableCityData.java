/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-4.
 * the city weather db table
 */
@DatabaseTable
public class DbTableCityData {
    static final String LOCATION_KEY = "locationKey";
    static final String LANGUAGE = "language";
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    String locationKey;
    @DatabaseField(canBeNull = false)
    String language;
    @DatabaseField
    String latitude;
    @DatabaseField
    String longitude;
    @DatabaseField
    String dateTime;
    @DatabaseField
    String cityInfo;


    void setId(int id) {
        this.id = id;
    }

    int getId() {
        return id;
    }

    public final void setCity(City city, boolean isPackJson) {
        locationKey = city.getLocationKey();
        language = city.getLanguage();
        latitude = city.getLatitude();
        longitude = city.getLongitude();
        if (isPackJson) {
            cityInfo = city.packString();
//            cityInfo = new Gson().toJson(city);
//            cityInfo = "{}";
        }
    }

    public final void setCity(City city) {
        setCity(city, true);
    }


    public final City getCity() {
        City city = new City();
//        City city = new City();
        city.parserString(cityInfo);
//        city.setLocationKey(locationKey);
//        city.setLanguage(language);
//        city.setLatitude(latitude);
//        city.setLongitude(longitude);
        return city;
    }

}
