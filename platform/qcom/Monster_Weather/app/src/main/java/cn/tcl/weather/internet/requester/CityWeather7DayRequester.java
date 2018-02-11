package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;
import java.util.Date;

import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class CityWeather7DayRequester extends BaseWeatherRequester<CityWeather7DayRequester.Bean7Day> {

    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     */
    public CityWeather7DayRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_FORECAST_7_DAY, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.DATE, ServerConstant.createYesterdayDateHourMinutes());
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    public static class Bean7Day extends JSONBean {
        @JSONBeanField(name = "c")
        public BeanC beanC = new BeanC();
        @JSONBeanField(name = "f")
        public BeanWeather weather = new BeanWeather();
    }

    public static class BeanWeather extends JSONBean {
        @JSONBeanField(name = "f0")
        public String date;
        @JSONBeanField(name = "f1")
        public ArrayList<BeanF> weahters = new ArrayList<>(7);

        public ArrayList<DayWeather> getDayWeathers(String lang) {
            int i = 0;
            Date date = ServerConstant.parseDate(this.date);
            ArrayList<DayWeather> weathers = new ArrayList<>(this.weahters.size());
            for (BeanF bean : this.weahters) {
                weathers.add(bean.getDayWeather(lang, date, i++));
            }
            return weathers;
        }
    }
}
