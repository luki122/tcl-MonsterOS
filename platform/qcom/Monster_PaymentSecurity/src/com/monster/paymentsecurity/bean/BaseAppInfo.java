package com.monster.paymentsecurity.bean;

/**
 * Created by sandysheny on 16-12-5.
 */

public class BaseAppInfo {
    private String name; // 软件名称
    private String packageName; // 包名


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
