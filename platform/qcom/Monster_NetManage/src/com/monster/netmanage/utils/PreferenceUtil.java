
package com.monster.netmanage.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 *  Description:SharedPreference 管理类.
 *  
 *  @author Zhaolaichao
 */
public class PreferenceUtil {
	
    public static final String SIM_1 = "SIM_1";
    public static final String SIM_2 = "SIM_2";
    
    /**
	 * 当前上网卡的imsi号
	 */
	public static final String CURRENT_ACTIVE_IMSI_KEY = "CURRENT_ACTIVE_IMSI_KEY";
	/**
	 * 初始化加载
	 */
	public static final String LOAD_FIRST_KEY = "_LOAD_FIRST_KEY";
	/**
	 * imsi号
	 */
	public static final String IMSI_KEY = "_IMSI_KEY";
	/**
	 * sim卡subid
	 */
	public static final String SIM_SUBID_KEY = "_SIM_SUBID_KEY";
	/**
	 * 流量校正
	 */
	public static final String CORRECT_DATA_KEY = "_CORRECT_DATA_KEY";
	/**
	 * sim卡基本信息设置
	 */
	public static final String SIM_BASEINFO_KEY = "_SIM_BASEINFO_KEY";
	/**
	 * sim所在的省份
	 */
	public static final String PROVINCE_KEY = "_PROVINCE_KEY";
	/**
	 * sim所在的省份代码
	 */
	public static final String PROVINCE_CODE_KEY = "_PROVINCE_CODE_KEY";
	/**
	 * sim所在城市
	 */
	public static final String CITY_KEY = "_CITY_KEY";
	/**
	 * sim所在城市代码
	 */
	public static final String CITY_CODE_KEY = "_CITY_CODE_KEY";
	/**
	 * sim所属运营商
	 */
	public static final String OPERATOR_KEY = "_OPERATOR_KEY";
	/**
	 * sim所属运营商代码
	 */
	public static final String OPERATOR_CODE_KEY = "_OPERATOR_CODE_KEY";
	/**
	 * sim套餐类型
	 */
	public static final String DATAPLAN_TYPE_KEY = "_DATAPLAN_TYPE_KEY";
	/**
	 * sim套餐类型代码
	 */
	public static final String DATAPLAN_TYPE_CODE_KEY = "_DATAPLAN_TYPE_CODE_KEY";
	/**
	 * sim套餐流量校正是否首次
	 */
	public static final String DATAPLAN_CORRECT_FIRST_KEY = "_DATAPLAN_CORRECT_FIRST_KEY";
	/**
	 * sim套餐流量
	 */
	public static final String DATAPLAN_COMMON_KEY = "_DATAPLAN_COMMON_KEY";
	/**
	 * sim已使用通用套餐流量
	 */
	public static final String USED_DATAPLAN_COMMON_KEY = "_USED_DATAPLAN_COMMON_KEY";
	/**
	 * sim剩余通用套餐流量
	 */
	public static final String REMAIN_DATAPLAN_COMMON_KEY = "_REMAIN_DATAPLAN_COMMON_KEY";
	/**
	 * 月结日
	 */
	public static final String CLOSEDAY_KEY = "_CLOSEDAY_KEY";
	/**
	 * 闲时流量开关状态
	 */
	public static final String FREE_DATA_STATE_KEY = "_FREE_DATA_STATE_KEY";
	/**
	 * 闲时套餐流量大小
	 */
	public static final String FREE_DATA_TOTAL_KEY = "_FREE_DATA_TOTAL_KEY";
	/**
	 * 剩余闲时套餐流量
	 */
	public static final String REMAIN_FREE_DATA_TOTAL_KEY = "_REMAIN_FREE_DATA_TOTAL_KEY";
	/**
	 * 已用闲时套餐流量
	 */
	public static final String USED_FREE_DATA_TOTAL_KEY = "_USED_FREE_DATA_TOTAL_KEY";
	/**
	 * 闲时套餐流量每天开始时间
	 */
	public static final String FREE_DATA_START_TIME_KEY = "_FREE_DATA_START_TIME_KEY";
	/**
	 * 闲时套餐流量每天结束时间
	 */
	public static final String FREE_DATA_END_TIME_KEY = "_FREE_DATA_END_TIME_KEY";
	/**
	 * 超额预警状态
	 */
	public static final String PASS_WARNING_STATE_KEY = "_PASS_WARNING_STATE_KEY";
	/**
	 * 超额预警值
	 */
	public static final String PASS_WARNING_VALUE_KEY = "_PASS_WARNING_VALUE_KEY";
	/**
	 * 是否自动校正流量
	 */
	public static final String AUTO_CORRECT_STATE_KEY = "_AUTO_CORRECT_STATE_KEY";
	/**
	 * 是否自动校正流量计时
	 */
	public static final String AUTO_CORRECT_REPEAT_KEY = "_AUTO_CORRECT_REPEAT_KEY";
	/**
	 * 流量校正完成时间
	 */
	public static final String CORRECT_OK_TIME_KEY = "_CORRECT_OK_TIME_KEY";
	/**
	 * 已添加的定向应用
	 */
	public static final String ORIENT_APP_ADDED_KEY = "_ORIENT_APP_ADDED_KEY";
	/**
	 * 每个月预警提示（包括通用流量和闲时流量）
	 */
	public static final String NOTIFY_WARN_MONTH_KEY = "_NOTIFY_WARN_MONTH_KEY";
	/**
	 * 每个月预警提示 达到99%
	 */
	public static final String NOTIFY_WARN_MONTH_99_KEY = "_NOTIFY_WARN_MONTH_99_KEY";
	/**
	 * 每日提示 当天流量使用超过套餐流量的5%
	 */
	public static final String NOTIFY_WARN_DAY_KEY = "_NOTIFY_WARN_DAY_KEY";
	/**
	 * 未设置流量套餐时,提示最多弹出3次
	 */
	public static final String NOTIFY_UNDATA_COUNT_KEY = "_NOTIFY_UNDATA_COUNT_KEY";
	/**
	 * 使用移动流量联网被禁提示
	 */
	public static final String WARN_DATA_USED_KEY = "_WARN_DATA_USED_KEY";
	/**
	 * 每分钟用的流量
	 */
	public static final String MINUTE_DATA_USED_KEY = "_MINUTE_DATA_USED_KEY";
	/**
	 * 单位时间内（1分钟）后台跑了至少10M流量则满足提示条件
	 */
	public static final String MINUTE_DATA_USED_DIALOG_KEY = "_MINUTE_DATA_USED_DIALOG_KEY";
	/**
	 * 手动输入校正
	 */
	public static final String MAN_INPUT_CORRECT_KEY = "_MAN_INPUT_CORRECT_KEY";
	/**
	 * 最顶层的应用名字
	 */
	public static final String TOP_APP_NAME_KEY = "_TOP_APP_NAME_KEY";
	/**
	 * 日期发生改变
	 */
	public static final String DATE_CHANGE_KEY = "_DATE_CHANGE_KEY";
	/**
	 * 选择网络类型
	 */
	public static final String SELECTED_NETTYPE_KEY = "_SELECTED_NETTYPE_KEY";
	
	public static SharedPreferences getDefaultPreference(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean putString(Context context, String simImsi, String key, String value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(simImsi + key, value).commit();
	}
	
	public static boolean putBoolean(Context context, String simImsi,  String key, boolean value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(simImsi + key, value).commit();
	}
	
	public static boolean putFloat(Context context, String simImsi, String key, float value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(simImsi + key, value).commit();
	}
	
	public static boolean putLong(Context context, String simImsi, String key, long value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(simImsi + key, value).commit();
	}
	
	public static boolean putInt(Context context, String simImsi, String key, int value){
		 return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(simImsi + key, value).commit();
	}
	
	public static String getString(Context context, String simImsi, String key, String defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getString(simImsi + key, defValue);
	}
	
	public static int getInt(Context context, String simImsi, String key, int defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getInt(simImsi + key, defValue);
	}
	
	public static float getFloat(Context context, String simImsi, String key, float defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getFloat(simImsi + key, defValue);
	}
	
	public static long getLong(Context context,String simImsi, String key, long defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getLong(simImsi + key, defValue);
	}
	
	public static boolean getBoolean(Context context, String simImsi, String key, boolean defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(simImsi + key, defValue);
	}
}

