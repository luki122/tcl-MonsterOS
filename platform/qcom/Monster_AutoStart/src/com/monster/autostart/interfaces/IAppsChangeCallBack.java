package com.monster.autostart.interfaces;

public interface IAppsChangeCallBack {
    void onPackageRemoved(String packageName);
    void onPackageAdded(String packageName);
    void onPackageChanged(String packageName);
    void onPackagesAvailable(String[] packageNames,boolean replacing);
    void onPackagesUnavailable(String[] packageNames, boolean replacing);
}
