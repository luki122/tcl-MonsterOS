package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.bean.CityLifeIndexInfo;
import cn.tcl.weather.service.ICityManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-26.
 */
public class BeanI extends JSONBean {

    @JSONBeanField(name = "i1")
    public String indexShortName;
    @JSONBeanField(name = "i2")
    public String indexChineseName;
    @JSONBeanField(name = "i3")
    public String indexOtherChineseName;
    @JSONBeanField(name = "i4")
    public String indexValueCurrentDay;
    @JSONBeanField(name = "i5")
    public String indexDescribeCurrentDay;
    @JSONBeanField(name = "i6")
    public String indexValueTwoDaysLater;
    @JSONBeanField(name = "i7")
    public String indexDescribeTwoDaysLater;
    @JSONBeanField(name = "i8")
    public String indexValueThreeDaysLater;
    @JSONBeanField(name = "i9")
    public String indexDescribeThreeDaysLater;

    public CityLifeIndexInfo getLifeIndexInfo(String lang) {
        CityLifeIndexInfo info = new CityLifeIndexInfo();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            info.indexShortName = indexShortName;
            info.indexChineseName = indexChineseName;
            info.indexOtherChineseName = indexOtherChineseName;
            info.indexValueCurrentDay = indexValueCurrentDay;
            info.indexDescribeCurrentDay = indexDescribeCurrentDay;
            info.indexValueTwoDaysLater = indexValueTwoDaysLater;
            info.indexDescribeTwoDaysLater = indexDescribeTwoDaysLater;
            info.indexValueThreeDaysLater = indexValueThreeDaysLater;
            info.indexValueThreeDaysLater = indexValueThreeDaysLater;
            info.indexDescribeThreeDaysLater = indexDescribeThreeDaysLater;
        } else {
            info.indexShortName = indexShortName;
            info.indexChineseName = indexChineseName;
            info.indexOtherChineseName = indexOtherChineseName;
            info.indexValueCurrentDay = indexValueCurrentDay;
            info.indexDescribeCurrentDay = indexDescribeCurrentDay;
            info.indexValueTwoDaysLater = indexValueTwoDaysLater;
            info.indexDescribeTwoDaysLater = indexDescribeTwoDaysLater;
            info.indexValueThreeDaysLater = indexValueThreeDaysLater;
            info.indexValueThreeDaysLater = indexValueThreeDaysLater;
            info.indexDescribeThreeDaysLater = indexDescribeThreeDaysLater;
        }
        return info;
    }

    @Override
    public String toString() {
        return "BeanI{" +
                "indexShortName='" + indexShortName + '\'' +
                ", indexChineseName='" + indexChineseName + '\'' +
                ", indexOtherChineseName='" + indexOtherChineseName + '\'' +
                ", indexValueCurrentDay='" + indexValueCurrentDay + '\'' +
                ", indexDescribeCurrentDay='" + indexDescribeCurrentDay + '\'' +
                ", indexValueTwoDaysLater='" + indexValueTwoDaysLater + '\'' +
                ", indexDescribeTwoDaysLater='" + indexDescribeTwoDaysLater + '\'' +
                ", indexValueThreeDaysLater='" + indexValueThreeDaysLater + '\'' +
                ", indexDescribeThreeDaysLater='" + indexDescribeThreeDaysLater + '\'' +
                '}';
    }
}
