package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import org.json.JSONArray;
import org.json.JSONException;

import cn.tcl.weather.utils.Debugger;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-1.
 * $desc
 */
public class ObserveBeanArray extends JSONBean {

    private BeanObserve mBeanObserve = new BeanObserve();

    public BeanObserve getBeaObserve() {
        return mBeanObserve;
    }

    @Override
    public boolean parserString(String string) {
        try {
            JSONArray array = new JSONArray(string);
            return mBeanObserve.info.parserString(array.getJSONObject(0).getString("cityinfo")) && mBeanObserve.l.parserString(array.getJSONObject(1).getString("l"));
        } catch (JSONException e) {
            Debugger.DEBUG_D(true, "BeanObserveArray", "parserString err: ", e.toString());
        }
        return false;
    }

    @Override
    public String packString() {
        return null;
    }

    public static class BeanObserve extends JSONBean {
        @JSONBeanField(name = "cityinfo")
        public CityInfo info = new CityInfo();
        @JSONBeanField(name = "l")
        public BeanL l = new BeanL();
    }
}
