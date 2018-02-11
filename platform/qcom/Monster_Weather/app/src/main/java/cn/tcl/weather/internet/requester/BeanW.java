package cn.tcl.weather.internet.requester;

import android.content.Context;
import android.content.res.Resources;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.CityWeatherWarning;
import cn.tcl.weather.internet.StatusWarning;
import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.utils.CommonUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-30.
 */
public class BeanW extends JSONBean {

    private static int deubgIndex = 0;

    @JSONBeanField(name = "w1")
    public String warnProvince;

    @JSONBeanField(name = "w2")
    public String warnCity;

    @JSONBeanField(name = "w3")
    public String warnCounty;

    @JSONBeanField(name = "w4")
    public String warnNo;

    @JSONBeanField(name = "w5")
    public String warnCategoryName;

    @JSONBeanField(name = "w6")
    public String warnGradeNo;

    @JSONBeanField(name = "w7")
    public String warnGradeName;

    @JSONBeanField(name = "w8")
    public String warnTime;

    @JSONBeanField(name = "w9")
    public String warnContent;

    @JSONBeanField(name = "w10")
    public String warnID;

    @JSONBeanField(name = "w11")
    public String warnLink;

    public CityWeatherWarning getCityWarnInfo(String lang) {
        if (CommonUtils.IS_DEBUG)
            return mockWarning(WeatherCNApplication.getWeatherCnApplication());
        CityWeatherWarning warn = new CityWeatherWarning();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            warn.warnProvince = warnProvince;
            warn.warnCity = warnCity;
            warn.warnCounty = warnCounty;
            warn.warnNo = warnNo;
            warn.setWarnCategoryName(warnCategoryName);
            warn.warnGradeNo = warnGradeNo;
            warn.warnGradeName = warnGradeName;
            warn.warnTime = warnTime;
            warn.warnContent = warnContent;
            warn.warnID = warnID;
            warn.warnLink = warnLink;
        } else {
            warn.warnProvince = warnProvince;
            warn.warnCity = warnCity;
            warn.warnCounty = warnCounty;
            warn.warnNo = warnNo;
            warn.setWarnCategoryName(warnCategoryName);
            warn.warnGradeNo = warnGradeNo;
            warn.warnGradeName = warnGradeName;
            warn.warnTime = warnTime;
            warn.warnContent = warnContent;
            warn.warnID = warnID;
            warn.warnLink = warnLink;
        }
        return warn;
    }

    // for debug test weather info
    public CityWeatherWarning mockWarning(Context context) {

        if (null != StatusWarning.sDebugWarning) {
            deubgIndex++;
            if ((deubgIndex & 0x3) == 0) {
                Resources resources = context.getResources();
                CityWeatherWarning warn = new CityWeatherWarning();
                warn.warnProvince = resources.getString(R.string.warnProvince);
                warn.warnCity = resources.getString(R.string.warnCity) + deubgIndex;
                warn.warnCounty = resources.getString(R.string.warnCounty);
                warn.warnContent = resources.getString(R.string.warnContent);
                warn.setWarnCategoryName(resources.getString(StatusWarning.sDebugWarning.warningStrID));
                warn.warnGradeName = resources.getString(R.string.warnGradeName);
                warn.warnGradeNo = resources.getString(R.string.warnGradeNo);
                warn.warnNo = StatusWarning.sDebugWarning.warningNo;
                warn.warnTime = resources.getString(R.string.warnTime);
                return warn;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "BeanW{" +
                "warnProvince='" + warnProvince + '\'' +
                ", warnCity='" + warnCity + '\'' +
                ", warnCounty='" + warnCounty + '\'' +
                ", warnNo='" + warnNo + '\'' +
                ", warnCategoryName='" + warnCategoryName + '\'' +
                ", warnGradeNo='" + warnGradeNo + '\'' +
                ", warnGradeName='" + warnGradeName + '\'' +
                ", warnTime='" + warnTime + '\'' +
                ", warnContent='" + warnContent + '\'' +
                ", warnID='" + warnID + '\'' +
                ", warnLink='" + warnLink + '\'' +
                '}';
    }
}