package cn.tcl.weather.bean;

import android.text.TextUtils;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-4.
 * the weather of city
 */
public class CityWeatherInfo extends JSONBean implements Cloneable {

    private final static String TAG = "CityWeatherInfo";
    public final static float DEFAULT_ERRO_TEMP_VALUE = -1000f;

    @JSONBeanField(name = "areaId")
    public String areaId;

    @JSONBeanField(name = "temperature")
    public String temperature;

    @JSONBeanField(name = "humidity")
    public String humidity;

    @JSONBeanField(name = "windGrade")
    public String windGrade;

    @JSONBeanField(name = "windDirectNo")
    public String windDirectionNo;

    @JSONBeanField(name = "weatherNo")
    public String weatherNo;

    @JSONBeanField(name = "precipitation")
    public String precipitation;

    @JSONBeanField(name = "observationtime")
    public String observationtime;

    @JSONBeanField(name = "visibilityMeter")
    public String visibilityMeter;

    @JSONBeanField(name = "pressure")
    public String pressure;

    @JSONBeanField(name = "windspeed")
    public String windspeed;

    @JSONBeanField(name = "feelingTemp")
    public String feelingTemp;

    @JSONBeanField(name = "pmValue")
    public String pmValue;

    @JSONBeanField(name = "aqiValue")
    public String aqiValue;

    @JSONBeanField(name = "weatherWarnTxt")
    public String weatherWarnTxt;

    @JSONBeanField(name = "weatherConditionRemind")
    public String weatherConditionRemind;

    @JSONBeanField(name = "yesterdayWeatherInfo")
    public DayWeather yesterdayWeatherInfo = new DayWeather();

    @JSONBeanField(name = "dayWeathers")
    public ArrayList<DayWeather> dayWeathers = new ArrayList<>(7);

    @JSONBeanField(name = "hourWeathers")
    public ArrayList<HourWeather> hourWeathers = new ArrayList<>(24);

    @JSONBeanField(name = "lifeIndexs")
    public ArrayList<CityLifeIndexInfo> lifeIndexs = new ArrayList<>(30);

    public void setCityWeatherInfo(CityWeatherInfo info) {
        this.areaId = info.areaId;
        this.temperature = info.temperature;
        this.humidity = info.humidity;
        this.windGrade = info.windGrade;
        this.windDirectionNo = info.windDirectionNo;
        this.weatherNo = info.weatherNo;
        this.precipitation = info.precipitation;
        this.observationtime = info.observationtime;
        this.visibilityMeter = info.visibilityMeter;
        this.pressure = info.pressure;
        this.windspeed = info.windspeed;
        this.feelingTemp = info.feelingTemp;

        this.pmValue = info.pmValue;
        this.aqiValue = info.aqiValue;

        this.weatherWarnTxt = info.weatherWarnTxt;
        this.weatherConditionRemind = info.weatherConditionRemind;

        setYesterdayWeather(info.yesterdayWeatherInfo);
        setDayWeathers(info.dayWeathers);
        setHourWeathers(info.hourWeathers);
        setCityLifeIndexInfos(info.lifeIndexs);
    }

    public String getMaxandMinusTemperature() {
        ArrayList<Float> temperatures = new ArrayList<>(24);

        for (int i = 0; i < hourWeathers.size(); i++) {
            temperatures.add(hourWeathers.get(i).temperature);
        }

        int max = Collections.max(temperatures).intValue();
        int min = Collections.min(temperatures).intValue();

        return min + "°/" + max + "°";
    }

    public void combineWeatherInfo(CityWeatherInfo info) {
        if (null != info) {
            this.areaId = City.getString(info.areaId, areaId);
            this.temperature = City.getString(info.temperature, temperature);
            this.humidity = City.getString(info.humidity, humidity);
            this.windGrade = City.getString(info.windGrade, windGrade);
            this.windDirectionNo = City.getString(info.windDirectionNo, windDirectionNo);
            this.weatherNo = City.getString(info.weatherNo, weatherNo);
            this.precipitation = City.getString(info.precipitation, precipitation);
            this.observationtime = City.getString(info.observationtime, observationtime);
            this.visibilityMeter = City.getString(info.visibilityMeter, visibilityMeter);
            this.pressure = City.getString(info.pressure, pressure);
            this.windspeed = City.getString(info.windspeed, windspeed);
            this.feelingTemp = City.getString(info.feelingTemp, feelingTemp);

            this.pmValue = City.getString(info.pmValue, pmValue);
            this.aqiValue = City.getString(info.aqiValue, aqiValue);

            this.weatherWarnTxt = City.getString(info.weatherWarnTxt, weatherWarnTxt);
            this.weatherConditionRemind = City.getString(info.weatherConditionRemind, weatherConditionRemind);

            combineYesterdayWeather(info.yesterdayWeatherInfo);
            combineDayWeathers(info.dayWeathers);
            combineHourWeathers(info.hourWeathers);
            combineCityLifeIndexInfos(info.lifeIndexs);
        }
    }

    public void setYesterdayWeather(DayWeather yesterdayWeather) {
        this.yesterdayWeatherInfo = yesterdayWeather;
    }

    public void setCityLifeIndexInfos(ArrayList<CityLifeIndexInfo> infos) {
        lifeIndexs.clear();
        this.lifeIndexs.addAll(infos);
    }

    public void setHourWeathers(List<HourWeather> hourWeathers) {
        this.hourWeathers.clear();
        this.hourWeathers.addAll(hourWeathers);
    }

    public void setDayWeathers(List<DayWeather> weathers) {
        dayWeathers.clear();
        this.dayWeathers.addAll(weathers);
    }

    public void combineCityLifeIndexInfos(ArrayList<CityLifeIndexInfo> infos) {
        if (!infos.isEmpty()) {
            lifeIndexs.clear();
            this.lifeIndexs.addAll(infos);
        }
    }

    public void combineYesterdayWeather(DayWeather yesterdayWeather) {
        if (yesterdayWeatherInfo != null) {
            this.yesterdayWeatherInfo = yesterdayWeather;
        }
    }

    public void combineHourWeathers(List<HourWeather> hourWeathers) {
        if (!hourWeathers.isEmpty()) {
            this.hourWeathers.clear();
            this.hourWeathers.addAll(hourWeathers);
        }
    }

    public void combineDayWeathers(List<DayWeather> weathers) {
        if (!weathers.isEmpty()) {
            dayWeathers.clear();
            this.dayWeathers.addAll(weathers);
        }
    }

    @Override
    public CityWeatherInfo clone() {
        try {
            return (CityWeatherInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            CityWeatherInfo info = new CityWeatherInfo();
            info.setCityWeatherInfo(this);
            return info;
        }
    }

    public String getTempWithSymbol() {
        return TextUtils.isEmpty(temperature) ? "--" : temperature + "°";
    }

    public String getAbsTempWithoutSymbol() {
        return TextUtils.isEmpty(temperature) ? "--" : (int) Math.abs(Float.parseFloat(temperature)) + "";
    }

    public float getTempValue() {
        try {
            return TextUtils.isEmpty(temperature) ? DEFAULT_ERRO_TEMP_VALUE : (int) Float.parseFloat(temperature);
        } catch (Exception e) {
            return DEFAULT_ERRO_TEMP_VALUE;
        }
    }

    public ArrayList<CityLifeIndexInfo> getLifeIndexsInfo() {
        return lifeIndexs;
    }

    /**
     * @return dayWeathers
     */
    public ArrayList<DayWeather> getDayWeathers() {
        return new ArrayList<>(dayWeathers);
    }

    public ArrayList<HourWeather> getHourWeathers() {
        return hourWeathers;
    }

    public String getHumidityWithSymbol() {
        return TextUtils.isEmpty(humidity) ? "--" : humidity + "%";
    }

}
