package com.monster.paymentsecurity.bean;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

/**
 * Created by sandysheny on 16-11-22.
 */

public class PayAppInfo extends BaseAppInfo implements Comparable<PayAppInfo>{

    private boolean isNeedDetect; //是否开启检测

    public boolean isNeedDetect() {
        return isNeedDetect;
    }

    public void setNeedDetect(boolean needDetect) {
        isNeedDetect = needDetect;
    }

    public static PayAppInfo from(Context context,ApplicationInfo entity) {
        PayAppInfo payAppInfo = new PayAppInfo();
        payAppInfo.setName(context.getPackageManager().getApplicationLabel(entity).toString());
        payAppInfo.setPackageName(entity.packageName);
        payAppInfo.setNeedDetect(true);
        return payAppInfo;
    }

    @Override
    public int compareTo(@NonNull PayAppInfo o) {
        return (this.isNeedDetect == o.isNeedDetect()) ? 0 : (this.isNeedDetect ? -1 : 1);
    }
}
