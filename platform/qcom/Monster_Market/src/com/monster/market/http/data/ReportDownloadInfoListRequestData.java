package com.monster.market.http.data;

import java.util.List;

/**
 * Created by xiaobin on 16-11-10.
 */
public class ReportDownloadInfoListRequestData {

    private List<ReportDownloadInfoRequestData> downList;

    public List<ReportDownloadInfoRequestData> getDownList() {
        return downList;
    }

    public void setDownList(List<ReportDownloadInfoRequestData> downList) {
        this.downList = downList;
    }

    @Override
    public String toString() {
        return "ReportDownloadInfoListRequestData{" +
                "downList=" + downList +
                '}';
    }
}
