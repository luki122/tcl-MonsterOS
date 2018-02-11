package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-26.
 * $desc
 */
public class CityInfo extends JSONBean {
    @JSONBeanField(name = "areaid")
    public String areaid;
    @JSONBeanField(name = "namecn")
    public String namecn;
    @JSONBeanField(name = "districtcn")
    public String districtcn;
    @JSONBeanField(name = "provcn")
    public String provcn;
    @JSONBeanField(name = "nationcn")
    public String nationcn;
    @JSONBeanField(name = "lon")
    public String lon;
    @JSONBeanField(name = "lat")
    public String lat;
    @JSONBeanField(name = "time")
    public String time;
    @JSONBeanField(name = "time_zone")
    public String time_zone;
    @JSONBeanField(name = "zone_abb")
    public String zone_abb;
}
