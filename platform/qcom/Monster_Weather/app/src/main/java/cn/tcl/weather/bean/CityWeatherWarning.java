/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.internet.StatusWarning;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-4.
 * the weather of city
 */
public class CityWeatherWarning extends JSONBean implements Cloneable, Parcelable {
    @JSONBeanField(name = "warnProvince")
    public String warnProvince;
    @JSONBeanField(name = "warnCity")
    public String warnCity;
    @JSONBeanField(name = "warnCounty")
    public String warnCounty;
    @JSONBeanField(name = "warnNo")
    public String warnNo;
    @JSONBeanField(name = "warnCategoryName")
    private String warnCategoryName;
    @JSONBeanField(name = "warnGradeNo")
    public String warnGradeNo;
    @JSONBeanField(name = "warnGradeName")
    public String warnGradeName;
    @JSONBeanField(name = "warnTime")
    public String warnTime;
    @JSONBeanField(name = "warnContent")
    public String warnContent;
    @JSONBeanField(name = "warnID")
    public String warnID;
    @JSONBeanField(name = "warnLink")
    public String warnLink;

    public CityWeatherWarning() {
    }

    protected CityWeatherWarning(Parcel in) {
        warnProvince = in.readString();
        warnCity = in.readString();
        warnCounty = in.readString();
        warnNo = in.readString();
        warnCategoryName = in.readString();
        warnGradeNo = in.readString();
        warnGradeName = in.readString();
        warnTime = in.readString();
        warnContent = in.readString();
        warnID = in.readString();
        warnLink = in.readString();
    }

    public static final Creator<CityWeatherWarning> CREATOR = new Creator<CityWeatherWarning>() {
        @Override
        public CityWeatherWarning createFromParcel(Parcel in) {
            return new CityWeatherWarning(in);
        }

        @Override
        public CityWeatherWarning[] newArray(int size) {
            return new CityWeatherWarning[size];
        }
    };

    public void setCityWeatherWarning(CityWeatherWarning warn) {
        this.warnProvince = warn.warnProvince;
        this.warnCity = warn.warnCity;
        this.warnCounty = warn.warnCounty;
        this.warnNo = warn.warnNo;
        this.warnCategoryName = warn.warnCategoryName;
        this.warnGradeNo = warn.warnGradeNo;
        this.warnGradeName = warn.warnGradeName;
        this.warnTime = warn.warnTime;
        this.warnContent = warn.warnContent;
        this.warnID = warn.warnID;
        this.warnLink = warn.warnLink;
    }

    @Override
    public CityWeatherWarning clone() {
        try {
            return (CityWeatherWarning) super.clone();
        } catch (CloneNotSupportedException e) {
            CityWeatherWarning warn = new CityWeatherWarning();
            warn.setCityWeatherWarning(this);
            return warn;
        }
    }

    /**
     * show warning txt
     *
     * @return
     */
    public boolean canShowWeatherWarning() {
        return !TextUtils.isEmpty(warnContent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(warnProvince);
        dest.writeString(warnCity);
        dest.writeString(warnCounty);
        dest.writeString(warnNo);
        dest.writeString(warnCategoryName);
        dest.writeString(warnGradeNo);
        dest.writeString(warnGradeName);
        dest.writeString(warnTime);
        dest.writeString(warnContent);
        dest.writeString(warnID);
        dest.writeString(warnLink);
    }

    public int getWarningTextColor() {
        return StatusWarning.getWarningColorByInfo(this);
    }

    public String getWarnCategoryName() {
        if (warnCategoryName.isEmpty() || "null".equals(warnCategoryName)) {
            return "";
        }
        return warnCategoryName;
    }

    public void setWarnCategoryName(String warnCategoryName){
        this.warnCategoryName = warnCategoryName;
    }
}
