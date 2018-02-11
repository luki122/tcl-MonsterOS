package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-9-5.
 */
public class EssentialRequestData extends BasePageInfoData {

    private int appType;   // 1.游戏 2.应用

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }
}
