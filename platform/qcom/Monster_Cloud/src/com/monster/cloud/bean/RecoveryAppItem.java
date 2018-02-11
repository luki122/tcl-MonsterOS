package com.monster.cloud.bean;

/**
 * Created by xiaobin on 16-10-25.
 */
public class RecoveryAppItem {

    public static final int ITEM_VIEW_TYPE_HEADER = 0;
    public static final int ITEM_VIEW_TYPE_ITEM = 1;;

    public static final int ITEM_VIEW_HEADER_TYPE_RECOVERY = 0;
    public static final int ITEM_VIEW_HEADER_TYPE_RECOMMEND = 1;

    public static final int ITEM_VIEW_APP_TYPE_RECOVERY = 0;
    public static final int ITEM_VIEW_APP_TYPE_RECOMMEND = 1;

    private int type = ITEM_VIEW_TYPE_ITEM;
    private int headerType = ITEM_VIEW_HEADER_TYPE_RECOVERY;
    private int appType =ITEM_VIEW_APP_TYPE_RECOVERY;
    private RecoveryAppInfo appInfo;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHeaderType() {
        return headerType;
    }

    public void setHeaderType(int headerType) {
        this.headerType = headerType;
    }

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public RecoveryAppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(RecoveryAppInfo appInfo) {
        this.appInfo = appInfo;
    }

    @Override
    public String toString() {
        return "RecoveryAppItem{" +
                "type=" + type +
                ", headerType=" + headerType +
                ", appType=" + appType +
                ", appInfo=" + appInfo +
                '}';
    }
}
