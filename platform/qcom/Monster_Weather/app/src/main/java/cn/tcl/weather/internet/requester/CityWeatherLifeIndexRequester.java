package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;

import cn.tcl.weather.bean.CityLifeIndexInfo;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-26.
 */
public class CityWeatherLifeIndexRequester extends BaseWeatherRequester<CityWeatherLifeIndexRequester.BeanLifeIndex>{
    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     */
    public CityWeatherLifeIndexRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_INDEX, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    public static class BeanLifeIndex extends JSONBean {
        @JSONBeanField(name = "i")
        public ArrayList<BeanI> lifeIndexs = new ArrayList<BeanI>(30);

        public ArrayList<CityLifeIndexInfo> getLifeIndexInfo(String lang) {
            ArrayList<CityLifeIndexInfo> lifeIndexInfoWeathers = new ArrayList<>(24);
            for (BeanI i : this.lifeIndexs) {
                lifeIndexInfoWeathers.add(i.getLifeIndexInfo(lang));
            }
            return lifeIndexInfoWeathers;
        }
    }
}
