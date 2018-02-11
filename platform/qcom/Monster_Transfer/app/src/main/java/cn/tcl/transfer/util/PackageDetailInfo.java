/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import android.content.pm.PackageInfo;

public class PackageDetailInfo {
    public PackageInfo packageInfo;
    public long apkSize = 0;
    public long sysDataSize = 0;
    public long externalDataSize = 0;

    public PackageDetailInfo(PackageInfo info) {
        packageInfo = info;
    }
}
