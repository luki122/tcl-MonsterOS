package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-8-8.
 */
public class ReportAdClickRequestData {

    private int clickNum;
    private String adName;
    private int adId;

    public int getClickNum() {
        return clickNum;
    }

    public void setClickNum(int clickNum) {
        this.clickNum = clickNum;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    @Override
    public String toString() {
        return "ReportAdClickRequestData{" +
                "clickNum=" + clickNum +
                ", adName='" + adName + '\'' +
                ", adId=" + adId +
                '}';
    }
}
