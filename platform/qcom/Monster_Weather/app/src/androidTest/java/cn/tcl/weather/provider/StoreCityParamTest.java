/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import junit.framework.TestCase;

import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created by Leon.Zhang on 16-8-3.
 */
public class StoreCityParamTest extends BaseTestCase {


    public void testStoreCityParam() {
        City city = new City();
        city.setLanguage("en");

        StoreCityParam param = new StoreCityParam(city);
        param.setOnRequestDataListener(new DataParam.OnRequestDataListener() {
            @Override
            public void onRequestDataCallback(DataParam param) {
                lock.lockNotify();
            }

            @Override
            public void onRequestDataFailed(DataParam param, DataParam.RequestError error) {
                lock.lockNotify();
            }
        });
        mDefaultService.requestData(param);
        lock.lockWait();

        assertNotSame("id is not empty: ", 0, param.getRequestCity().getId());
    }

}
