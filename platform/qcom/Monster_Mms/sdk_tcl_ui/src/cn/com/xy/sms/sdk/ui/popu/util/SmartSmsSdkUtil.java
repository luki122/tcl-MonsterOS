package cn.com.xy.sms.sdk.ui.popu.util;

import cn.com.xy.sms.sdk.util.DuoquUtils;

public class SmartSmsSdkUtil {

    public static void smartSdkExceptionLog(String msg, Throwable e) {
        try {
            DuoquUtils.getSdkDoAction().logError("xiaoyuan", msg, e);
        } catch (Throwable e2) {
        }

    }
}
