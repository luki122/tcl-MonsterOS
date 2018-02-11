package com.monster.paymentsecurity.bean;

/**
 * Created by sandysheny on 16-11-22.
 */

public class WhiteListInfo extends BaseAppInfo {

    private boolean isChecked = false; //是否选中
    private boolean isEnabled = true;
    private String apkPath;
    private int apkType;

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getApkType() {
        return apkType;
    }

    public void setApkType(int apkType) {
        this.apkType = apkType;
    }
}
