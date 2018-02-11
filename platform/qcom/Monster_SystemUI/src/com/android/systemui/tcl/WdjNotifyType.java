package com.android.systemui.tcl;

import java.io.Serializable;

/**
 * Created by liuzhicang on 16-10-12.
 */

public class WdjNotifyType implements Serializable {
    private String packageName;
    private int uid;
    private String category;

    public String getPackageName() {
        return packageName;
    }

    public String getCategory() {
        return category;
    }

    public int getUid() {
        return uid;
    }

    public WdjNotifyType(String packageName, int uid, String category) {
        this.packageName = packageName;
        this.uid = uid;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WdjNotifyType)) return false;

        WdjNotifyType type = (WdjNotifyType) o;

        if (getUid() != type.getUid()) return false;
        if (!getPackageName().equals(type.getPackageName())) return false;
        return getCategory().equals(type.getCategory());

    }

    @Override
    public int hashCode() {
        int result = getPackageName().hashCode();
        result = 31 * result + getUid();
        result = 31 * result + getCategory().hashCode();
        return result;
    }
}
