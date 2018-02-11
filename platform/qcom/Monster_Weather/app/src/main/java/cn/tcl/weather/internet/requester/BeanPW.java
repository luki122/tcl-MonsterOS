package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.service.ICityManager;

/**
 * Created on 16-9-19.
 */
public class BeanPW extends JSONBean {
    @JSONBeanField(name = "pc")
    public String yesterdayMaxTemp;

    @JSONBeanField(name = "pd")
    public String yesterdayMinTemp;

    @JSONBeanField(name = "pj")
    public String date;

    public String weatherNO;

    public DayWeather getPastWeatherInfo(String lang) {
        DayWeather pastWeather = new DayWeather();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            pastWeather.nightTemp = this.yesterdayMinTemp;
            pastWeather.dayTemp = this.yesterdayMaxTemp;
            pastWeather.date = this.date;
            pastWeather.dayWeatherPhenomena = this.weatherNO;
        } else {
            pastWeather.nightTemp = this.yesterdayMinTemp;
            pastWeather.dayTemp = this.yesterdayMaxTemp;
            pastWeather.date = this.date;
            //pastWeather.date = "" + (Integer.parseInt(this.date) + 1);
            pastWeather.dayWeatherPhenomena = this.weatherNO;
        }
        return pastWeather;
    }

    public void setWeatherNO(String no){
        this.weatherNO = no;
    }

    @Override
    public String toString() {
        return "BeanPW{" +
                "yesterdayMaxTemp='" + yesterdayMaxTemp + '\'' +
                ", yesterdayMinTemp='" + yesterdayMinTemp + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
