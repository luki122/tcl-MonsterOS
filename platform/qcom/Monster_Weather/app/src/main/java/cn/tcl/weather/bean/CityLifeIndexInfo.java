package cn.tcl.weather.bean;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

/**
 * Created by on 16-8-22.
 */
public class CityLifeIndexInfo extends JSONBean {
    @JSONBeanField(name = "indexShortName")
    public String indexShortName;
    @JSONBeanField(name = "indexChineseName")
    public String indexChineseName;
    @JSONBeanField(name = "indexOtherChineseName")
    public String indexOtherChineseName;
    @JSONBeanField(name = "indexValueCurrentDay")
    public String indexValueCurrentDay;
    @JSONBeanField(name = "indexDescribeCurrentDay")
    public String indexDescribeCurrentDay;
    @JSONBeanField(name = "indexValueTwoDaysLater")
    public String indexValueTwoDaysLater;
    @JSONBeanField(name = "indexDescribeTwoDaysLater")
    public String indexDescribeTwoDaysLater;
    @JSONBeanField(name = "indexValueThreeDaysLater")
    public String indexValueThreeDaysLater;
    @JSONBeanField(name = "indexDescribeThreeDaysLater")
    public String indexDescribeThreeDaysLater;

    public void setCityLifeIndexInfo(CityLifeIndexInfo info) {
        this.indexShortName = info.indexShortName;
        this.indexChineseName = info.indexChineseName;
        this.indexOtherChineseName = info.indexOtherChineseName;
        this.indexValueCurrentDay = info.indexValueCurrentDay;
        this.indexDescribeCurrentDay = info.indexDescribeCurrentDay;
        this.indexValueTwoDaysLater = info.indexValueTwoDaysLater;
        this.indexDescribeTwoDaysLater = info.indexDescribeTwoDaysLater;
        this.indexValueThreeDaysLater = info.indexValueThreeDaysLater;
        this.indexDescribeThreeDaysLater = info.indexDescribeThreeDaysLater;
    }

    public void conbineCityLifeIndexInfo(CityLifeIndexInfo info) {
        this.indexShortName = City.getString(info.indexShortName, indexShortName);
        this.indexChineseName = City.getString(info.indexChineseName, indexChineseName);
        this.indexOtherChineseName = City.getString(info.indexOtherChineseName, indexOtherChineseName);
        this.indexValueCurrentDay = City.getString(info.indexValueCurrentDay, indexValueCurrentDay);
        this.indexDescribeCurrentDay = City.getString(info.indexDescribeCurrentDay, indexDescribeCurrentDay);
        this.indexValueTwoDaysLater = City.getString(info.indexValueTwoDaysLater, indexValueTwoDaysLater);
        this.indexDescribeTwoDaysLater = City.getString(info.indexDescribeTwoDaysLater, indexDescribeTwoDaysLater);
        this.indexValueThreeDaysLater = City.getString(info.indexValueThreeDaysLater, indexValueThreeDaysLater);
        this.indexDescribeThreeDaysLater = City.getString(info.indexDescribeThreeDaysLater, indexDescribeThreeDaysLater);

    }

    @Override
    public String toString() {
        return "CityLifeIndexInfo{" +
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
