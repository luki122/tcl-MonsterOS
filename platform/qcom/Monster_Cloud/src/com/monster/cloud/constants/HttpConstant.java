package com.monster.cloud.constants;

/**
 * Created by xiaobin on 16-10-25.
 */
public class HttpConstant {

    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_FAIL = 1;

    public static String HTTP_BASE = "http://appstore.tclchn.com";
    public static String getBaseApi() {
        return HTTP_BASE + "/appstore-web-api/";
    }

    public static String getCloudAppRecoveryUrl() {
        return getBaseApi() + "cloudAppRecovery";
    }

    /*
	12  云服务
	*/
    public static final int REPORT_MODULID_CLOUD = 12;

}
