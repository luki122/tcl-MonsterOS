/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.ICityManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class BeanC extends JSONBean {

    @JSONBeanField(name = "c1")
    public String areaId;
    @JSONBeanField(name = "c2")
    public String countysideEN;
    @JSONBeanField(name = "c3")
    public String countysideCN;
    @JSONBeanField(name = "c4")
    public String cityNameEN;
    @JSONBeanField(name = "c5")
    public String cityNameCN;
    @JSONBeanField(name = "c6")
    public String provinceEN;
    @JSONBeanField(name = "c7")
    public String provinceCN;
    @JSONBeanField(name = "c8")
    public String countyEN;
    @JSONBeanField(name = "c9")
    public String countyCN;
    @JSONBeanField(name = "c10")
    public String cityLevel;
    @JSONBeanField(name = "c11")
    public String cityCode;
    @JSONBeanField(name = "c12")
    public String zipCode;
    @JSONBeanField(name = "c13")
    public String longitude;
    @JSONBeanField(name = "c14")
    public String latitude;
    @JSONBeanField(name = "c15")
    public String altitude;
    @JSONBeanField(name = "c17")
    public String timeZone;
    @JSONBeanField(name = "c18")
    public String placeNameEN;
    @JSONBeanField(name = "c19")
    public String placeNameCN;

    public City getCity(String lang) {
        City city = new City();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            city.setCity(countyCN, provinceCN, cityNameCN, countysideCN);
            city.latitude = latitude;
            city.longitude = longitude;
        } else {
            city.setCity(countyEN, provinceEN, cityNameEN, countysideEN);
            city.latitude = latitude;
            city.longitude = longitude;
        }
        city.setLocationKey(City.createLocationKeyByLocated(countyCN, provinceCN, cityNameCN, countysideCN));
        return city;
    }
}
