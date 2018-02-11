package com.android.deskclock.view;

import java.util.ArrayList;
import java.util.List;

import com.android.deskclock.worldclock.CityObj;

public class WorldClockBean {
    
    float centerX;//圆点的中心坐标
    float centerY;
    
    int time_hour_24;//24小时制的时间
    int time_min_24;
    
    int show_index = 0;//多个时间一样的时候　显示哪个
    
    public List<CityObj> cities_list = new ArrayList<CityObj>();

}
