package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;

import cn.tcl.weather.bean.CityWeatherWarning;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;
import cn.tcl.weather.utils.CommonUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created by on 16-8-30.
 */
public class CityWeatherWarnRequester extends BaseWeatherRequester<CityWeatherWarnRequester.BeanWarn> {
    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     * @param cb
     */
    public CityWeatherWarnRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_ALARM, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    public static class BeanWarn extends JSONBean {
        @JSONBeanField(name = "w")
        public ArrayList<BeanW> warnInfos = new ArrayList<>(5);

        public ArrayList<CityWeatherWarning> getWarnInfos(String lang) {
            ArrayList<CityWeatherWarning> warnings = new ArrayList<>(5);
            if (warnInfos.isEmpty()) {
                if (CommonUtils.IS_DEBUG) {
                    BeanW bean = new BeanW();
                    CityWeatherWarning warning = bean.getCityWarnInfo(lang);
                    if (null != warning) {
                        warnings.add(warning);
                    }
                }
            } else {
                for (BeanW w : this.warnInfos) {
                    CityWeatherWarning warning = w.getCityWarnInfo(lang);
                    if (null != warning) {
                        warnings.add(warning);
                    }
                }
            }


            return warnings;
        }
    }
}
