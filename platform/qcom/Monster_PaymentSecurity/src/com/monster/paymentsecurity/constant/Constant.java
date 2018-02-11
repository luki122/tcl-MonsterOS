package com.monster.paymentsecurity.constant;

public class Constant {

	// 广播
	public static final String ACTION_APP_CHANGE = "com.monster.paymentsecurity.ACTION_APP_CHANGE";

	//========================Intent action===========================
	public static final String ACTION_GET_NEW_FIRMWARE = "com.tcl.monster.fota.ACTION_GET_NEW_FIRMWARE";
	public static final String KEY_VERSION_CODE = "VERSIONCODE";
	public static final String ACTION_VIEW_SYSTEM_UPDATE = "com.tcl.monster.fota.action.View";
	public static final String ACTION_VIRUS_LIB_CHANGE = "com.monster.paymentsecurity.VIRUS_LIB_CHANGE";
	public static final String ACTION_PAYENV_CHANGE = "com.monster.paymentsecurity.PayEnv_change";


	//======================sp key================================
	// 安装监控是否开启
	public static final String SP_INSTALL_DETECTION = "install_detection";

	// 云端杀毒是否开启
	public static final String SP_SCAN_CLOUD = "scan_cloud";

	// 病毒库联网自动更新是否开启
	public static final String SP_UPDATE_VIRUS_LIB = "update_virus_lib";

	// 病毒库联网自动更新是否开启
	public static final String SP_PAY_APP_MONITOR = "pay_app_monitor";

	//系统版本
	public static final String SP_FRAMEWORK_VERSION = "new_framework_version";

	// 应用是否第一运行
	public static final String SP_FIRST_RUN = "first_run";

	public static final String SP_VIRUS_LIB_UPDATE_TIME = "virus_lib_update_time";


	public static final String ACCESSIBILITY_NAME = "com.monster.paymentsecurity/.detection.WindowChangeDetectingService";

}
