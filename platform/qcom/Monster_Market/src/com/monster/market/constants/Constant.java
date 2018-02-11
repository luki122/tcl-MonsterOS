package com.monster.market.constants;

public class Constant {

	// wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	// 2G网络类型标志
	public static final int NETWORK_2G = 1;
	// 3G网络类型标志
	public static final int NETWORK_3G = 2;

	// 广播
	public static final String ACTION_APP_DOWNLOAD_FINISH = "action_app_download_finish";
	public static final String ACTION_MARKET_UPDATE = "com.monster.market.action.update";

	// 仅Wi-Fi下载开关
	public static final String SP_WIFI_DOWNLOAD_KEY = "wifi_download_key";

	// 搜索历史记录 文件保存 关键字定义
	public static final String HISTORY_RECORDS_FILENAME = "market_search_history_records";
	public static final int HISTORY_MAX_LIMIT = 3;
	public static final String HISTORY_RECORDS = "history_records_";

	/*SharedPreferences*/
	public static final String SHARED_WIFI_UPDATE = "com.monster.market.wifi.update";
	//当前的网络状态
	//0 -无网络  1-wifi  2-手机网络
	public static final String SHARED_NETSTATUS_KEY = "net_status";
	public static final int SHARED_NETSTATUS_NO_NETWORK = 0;
	public static final int SHARED_NETSTATUS_WIFI = 1;
	public static final int SHARED_NETSTATUS_MOBILE = 2;

	//通知栏更新时间
	public static final String SHARED_WIFI_APPUPDATE_KEY_UPDATETIME = "update_time";

	// 最后一次检查更新的APP可更新数量
	public static final String SP_LAST_APP_UPDATE_COUNT_KEY = "app_update_count_key";

	// 是否弹出WIFI断开后提示下载对话框
	public static final String SP_WIFI_DISCONNECT_ALERT_KEY = "wifi_disconnect_alert_key";
	// 弹出WIFI断开后提示下载对话框后,保存的操作(是或者否)
	public static final String SP_WIFI_DISCONNECT_ALERT_OPERATION_KEY = "sp_wifi_disconnect_alert_operation_key";

}
