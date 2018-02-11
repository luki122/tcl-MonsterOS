package com.monster.cloud.http.data;

/**
 * Created by xiaobin on 16-10-27.
 */
public class CloudAppRecoveryAppInfoRequestData {

    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "CloudAppRecoveryAppInfoRequestData{" +
                "packageName='" + packageName + '\'' +
                '}';
    }
}
