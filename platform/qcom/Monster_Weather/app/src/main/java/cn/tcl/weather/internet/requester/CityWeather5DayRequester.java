package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;
import java.util.Date;

import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;
import cn.tcl.weather.utils.SharedPrefUtils;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class CityWeather5DayRequester extends BaseWeatherRequester<CityWeather5DayRequester.Bean7Day> {

    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     */
    public CityWeather5DayRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_FORECAST_5_DAY, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.DATE, ServerConstant.createYesterdayDateHourMinutes());
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    @Override
    protected void onSucceed(Bean7Day bean) {
        String areaId = getParamByKey(ServerConstant.AREA_ID);
        ArrayList<DayWeather> weahters = bean.weather.getDayWeathers(null);
        for (int i = weahters.size() - 1; i >= 0; i--) {
            DayWeather dayWeather = weahters.get(i);
            SharedPrefUtils.getInstance().saveWeather(areaId, dayWeather.date, dayWeather.dayWeatherPhenomena);
        }
        super.onSucceed(bean);
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
        public ArrayList<BeanF> weahters = new ArrayList<>(5);

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
