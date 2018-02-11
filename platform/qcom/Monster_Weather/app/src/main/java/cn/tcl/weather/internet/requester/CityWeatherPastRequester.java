package cn.tcl.weather.internet.requester;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;

import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;
import cn.tcl.weather.utils.SharedPrefUtils;

/**
 * Created on 16-9-19.
 */
public class CityWeatherPastRequester extends BaseWeatherRequester<CityWeatherPastRequester.BeanPastWeather> {
    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     * @param cb
     */
    public CityWeatherPastRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_PAST_WEATHER, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    @Override
    protected void onSucceed(BeanPastWeather bean) {
        //select yesterday weather info from history database and put it in current bean
        String areaId = getParamByKey(ServerConstant.AREA_ID);
        String pastWeatherNo = SharedPrefUtils.getInstance().getWeather(areaId, bean.getPastWeather(null).getLastDate());
        if (!SharedPrefUtils.NO_WEATHER_FLAG.equals(pastWeatherNo)) {
            bean.addWeatherNoToPastWeather(pastWeatherNo);
        }
        super.onSucceed(bean);
    }


    public static class BeanPastWeather extends JSONBean {
        @JSONBeanField(name = "p")
        public ArrayList<BeanPW> pastWeatherInfos = new ArrayList<>(5);

        public DayWeather getPastWeather(String lang) {
            DayWeather dayWeather = new DayWeather();
            if (pastWeatherInfos.size() > 0) {
                dayWeather = pastWeatherInfos.get(0).getPastWeatherInfo(lang);
            }
            return dayWeather;
        }

        //the interface do not give the weather phenomenon data , so we add it by hand
        public void addWeatherNoToPastWeather(String no) {
            if (pastWeatherInfos.size() > 0) {
                pastWeatherInfos.get(0).setWeatherNO(no);
            }
        }
    }
}
