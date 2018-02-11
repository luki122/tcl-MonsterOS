package com.monster.netmanage.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.monster.netmanage.DataCorrect;
import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.R;
import com.monster.netmanage.receiver.NetManagerReceiver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 工具类
 * 
 * @author zhaolaichao
 */
public class ToolsUtil {
    public static ArrayList<PackageInfo> mAllApps;
    public static final String NET_TYPE_MOBILE = "MOBILE";
    public static final String NET_TYPE_WIFI = "WIFI";
	/**
	 *  获得包含联网权限的应用
	 * @param context
	 * @return
	 */
	public static ArrayList<PackageInfo>  getPackageInfos(Context context) {
		    ArrayList<PackageInfo> netAppInfos = new ArrayList<PackageInfo>();
	        PackageManager pm = context.getPackageManager();
	        ArrayList<PackageInfo> packageInfos =  (ArrayList<PackageInfo>) pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
	        try {
	               for (PackageInfo packageInfo : packageInfos) {
	                  if (0 != (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)) {
	             	   //过虑系统自身应用
	                       continue;
	                   }
	                  if (isNetworkApp(context, packageInfo.applicationInfo.packageName)) {
	                	  Log.v("PackageInfo", "uid>>" + packageInfo.applicationInfo.uid + ">>pkName>>" + packageInfo.applicationInfo.packageName);
	                	  netAppInfos.add(packageInfo);
	                  }
	               }
              } catch ( Exception e) {
                 e.printStackTrace();
             }
	       return netAppInfos;
	  }
	
	 public static boolean isNetworkApp(Context context, String packageName) {
	        return context.getPackageManager().checkPermission("android.permission.INTERNET", packageName) == 0;
	 }
	 
	/**
	 * 检测Service是否已启动
	 * @param context
	 * @param serviceClassName
	 * @return
	 */
	public static boolean isServiceRunning(Context context, String serviceClassName){ 
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE); 
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE); 

        for (RunningServiceInfo runningServiceInfo : services) { 
        	Log.v("Tootuls", "service>>" + runningServiceInfo.service.getClassName() + "serviceClassName>>>" + serviceClassName);
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){ 
                return true; 
            } 
        } 
        return false; 
     }
	
	/** 
	 * 获取双卡手机的两个卡的IMSI 需要 READ_PHONE_STATE 权限 
	 * @hide
	 * @param context     上下文 
	 * @return 下标0为一卡的IMSI，下标1为二卡的IMSI 
	 */  
	@SuppressWarnings("unused")
	private static String[] getIMSI1(Context context) {  
		 // 双卡imsi的数组  
	    String[] imsis = new String[2];  
	    try {  
	    	TelephonyManager tm = getTeleManager(context); 
	        // 先使用默认的获取方式获取一卡IMSI  
	        imsis[0] = tm.getSubscriberId();  
	        // 然后进行二卡IMSI的获取,默认先获取展讯的IMSI  
	        try {  
	            Method method = tm.getClass().getDeclaredMethod("getSubscriberIdGemini", int.class);  
	            method.setAccessible(true);  
	            // 0 表示 一卡，1 表示二卡，下方获取相同  
	            imsis[1] = (String) method.invoke(tm, 1);  
	        } catch (Exception e) {  
	            // 异常清空数据，继续获取下一个  
	            imsis[1] = null;  
	        }  
	        if (TextUtils.isEmpty(imsis[1])) { // 如果二卡为空就获取mtk  
	            try {  
	                Class<?> c = Class.forName("com.android.internal.telephony.PhoneFactory");  
	                Method m = c.getMethod("getServiceName", String.class, int.class);  
	                String spreadTmService = (String) m.invoke(c, Context.TELEPHONY_SERVICE, 1);  
	                TelephonyManager tm1 = (TelephonyManager) context.getSystemService(spreadTmService);  
	                imsis[1] = tm1.getSubscriberId();  
	            } catch (Exception ex) {  
	                imsis[1] = null;  
	            }  
	        }  
	        if (TextUtils.isEmpty(imsis[1])) { // 如果二卡为空就获取高通 IMSI获取  
	            try {  
	                Method addMethod2 = tm.getClass().getDeclaredMethod("getSubscriberId", int.class);  
	                addMethod2.setAccessible(true);  
	                imsis[1] = (String) addMethod2.invoke(tm, 1);  
	                Log.e("imsis[1]", "imsis[1]------------>>>" + imsis[1]);
	            } catch (Exception ex) {  
	                imsis[1] = null;  
	            }  
	        }  
	    } catch (IllegalArgumentException e) { 
	    	e.printStackTrace();
	    } 
	    return imsis;  
	}

	/**
	 * 获取双卡手机的两个卡的IMSI
	 * @param context
	 * @return
	 */
	public static String[] getIMSI(Context context) {
	    TelephonyManager tm = getTeleManager(context);
	    int phoneCount = tm.getPhoneCount();
		List<SubscriptionInfo> mSelectableSubInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
		if ( null == mSelectableSubInfos || mSelectableSubInfos.size() == 0) {
			return new String[phoneCount];
		}
		// 根据卡状态来创建卡imsi的数组  
		String[] imsis = new String[phoneCount];  
		for (int i = 0; i < mSelectableSubInfos.size(); i++) {
			SubscriptionInfo subscriptionInfo = mSelectableSubInfos.get(i);
			//获得subId;
			int subscriptionId = subscriptionInfo.getSubscriptionId();
			int simSlotIndex = subscriptionInfo.getSimSlotIndex();
	        try {
	        	Method addMethod = tm.getClass().getDeclaredMethod("getSubscriberId", int.class);  
	        	addMethod.setAccessible(true);  
				imsis[simSlotIndex] = (String) addMethod.invoke(tm, subscriptionId);
			}  catch (Exception e) {
				e.printStackTrace();
			}  
		}
		return imsis;
	}
	/**
	 * 获得sim卡运营商
	 * @param context
	 * @param imsi
	 */
	public static  String  getSimOperator(Context context, String imsi) {
		String simOperator = null;
		try {
			/** 
			 * 获取SIM卡的IMSI码 
			 * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志， 
			 * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成， 
			 * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成， 
			 * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。 
			 * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
			 *  */ 
			if (imsi != null) { 
				if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
					//因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
					//中国移动 
					simOperator = context.getString(R.string.china_mobile);
				} else if (imsi.startsWith("46001")) {
					//中国联通 
					simOperator = context.getString(R.string.china_unicom);
				} else if (imsi.startsWith("46003") || imsi.startsWith("46011")) { 
					//中国电信
					simOperator = context.getString(R.string.china_telecom);
				} else {
					simOperator = context.getString(R.string.un_operator);
				}
			} else {
				simOperator = context.getString(R.string.un_operator);
			}
		} catch (Exception e) {
			e.printStackTrace();
			simOperator = context.getString(R.string.un_operator);
		}
		return simOperator;
	}
	
	/**
	 * 获取当前上网卡的卡槽索引
	 * @param context
	 * @return
	 */
	public static int getCurrentNetSimSubInfo(Context context) {
		SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
		Method method;
		SubscriptionInfo  subscriptionInfo;
		int simSlotIndex = -1;
		try {
			//通过反射来获取当前上网卡的信息
			method = subscriptionManager.getClass().getDeclaredMethod("getDefaultDataSubscriptionInfo");
			method.setAccessible(true);  
			subscriptionInfo = (SubscriptionInfo) method.invoke(subscriptionManager);
			if (subscriptionInfo != null) {
				simSlotIndex = subscriptionInfo.getSimSlotIndex();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		return simSlotIndex;
	}
	
	/**
	 * 通过simId来获得subId
	 * @param simId 当前sim卡所在的卡槽位置
	 * @return
	 */
	public static int getIdInDbBySimId(Context context, int simId) {
		Cursor cursor = null;
		Uri uri = Uri.parse("content://telephony/siminfo");
		ContentResolver resolver = context.getContentResolver();
		try {
			cursor = resolver.query(uri, new String[]{"_id", "sim_id"}, "sim_id = ?", new String[]{String.valueOf(simId)}, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					return cursor.getInt(cursor.getColumnIndex("_id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return -1;
	}
	
	/**
	 * 切换成当前上网卡
	 * @param context
	 */
	public static boolean changeNetSim(Context context, int simIndex) {
		boolean state = false;
		SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
		Method method;
		try {
//			getDefaultDataSubId
			method = subscriptionManager.getClass().getDeclaredMethod("getDefaultDataSubscriptionId");
			method.setAccessible(true);  
			int mDefaultDataSubid = (Integer) method.invoke(subscriptionManager);
			//获得要上网卡的subId
			int simSub = getIdInDbBySimId(context, simIndex);
			if (mDefaultDataSubid != simSub && simSub >= 0) {
				//设置目标上网卡的subId
				Method defaultDataSubIdMethod = subscriptionManager.getClass().getDeclaredMethod("setDefaultDataSubId", int.class);
				defaultDataSubIdMethod.invoke(subscriptionManager, simSub);
				Toast.makeText(context, R.string.data_switch_started, Toast.LENGTH_SHORT).show();
				state = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return state;
	}

	 /**
     * Returns the unique subscriber ID, for example, the IMSI for a GSM phone.
     * Return null if it is unavailable.
     * <p>
     * Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     */
	public static String getActiveSubscriberId(Context context, int subId) {
	      final TelephonyManager tm = getTeleManager(context);
	      Method addMethod =  null;
      	  String retVal = null;
		  try {
		  	addMethod = tm.getClass().getDeclaredMethod("getSubscriberId", int.class); 
		  	addMethod.setAccessible(true);  
		  	retVal = (String) addMethod.invoke(tm, subId);
		  	Log.d("ToolsUtil", "getActiveSubscriberId=" + retVal + " subId=" + subId);
		  } catch (Exception e) {
		  	e.printStackTrace();
		  } 
	      return retVal;
	 }
	
	/**
	 * 获得TelephonyManager
	 * @param context
	 * @return
	 */
	public static TelephonyManager getTeleManager(Context context) {
	      TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
           return tm;
	}
	
	/**
	 *获得当前上网卡的IMSI
	 * @param context
	 * @return
	 */
	public static String getActiveSimImsi(Context context) {
		 int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(context);
         if (simSlotIndex == -1) {
       	  return null;
         }
		  int subId = ToolsUtil.getIdInDbBySimId(context, simSlotIndex);
		  String activeDataImsi = ToolsUtil.getActiveSubscriberId(context, subId);
		  return activeDataImsi;
	}
	
	/**
     * 获取网络状态，wifi,wap,2g,3g.
     *
     * @param context 上下文
     * @return 联网类型 
     * 
     */
    public static String getNetWorkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
           if (type.equalsIgnoreCase(NET_TYPE_MOBILE)) {
                String proxyHost = System.getProperty("http.proxyHost");
                if(TextUtils.isEmpty(proxyHost)) {
                	return NET_TYPE_MOBILE;
                }
            } else if (type.equalsIgnoreCase(NET_TYPE_WIFI)) {
            	return NET_TYPE_WIFI;
            }
        }
        return null;
    }
    
    /**
     * 设置手机流量上网状态
     * @param context
     * @param mobileDataEnabled
     */
    public static void setMobileDataState(Context context, boolean mobileDataEnabled) {
		TelephonyManager telephonyService = getTeleManager(context);
		try {
			Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
			if (null != setMobileDataEnabledMethod) {
				setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * 判断手机流量上网状态
     * @param context
     * @return
     */
	public static boolean getMobileDataState(Context context) {
		TelephonyManager telephonyService = getTeleManager(context);
		try {
			Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
			if (null != getMobileDataEnabledMethod) {
				boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
				return mobileDataEnabled;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 更新桌面图标广播
	 */
	public static void updateIconReceiver() {
		String activeImsi = ToolsUtil.getActiveSimImsi(DataManagerApplication.getInstance());
		Intent dataIntent = new Intent(DataCorrect.UPDATE_DATAPLAN_ACTION);
		int sim1TotalData = 0;
		int sim2TotalData = 0;
		sim1TotalData = PreferenceUtil.getInt(DataManagerApplication.getInstance(), DataManagerApplication.mImsiArray[0], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		sim2TotalData = PreferenceUtil.getInt(DataManagerApplication.getInstance(), DataManagerApplication.mImsiArray[1], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		int totalData = PreferenceUtil.getInt(DataManagerApplication.getInstance(), activeImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		int remainData = PreferenceUtil.getInt(DataManagerApplication.getInstance(), activeImsi, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
		dataIntent.putExtra("package_name", DataManagerApplication.getInstance().getPackageName());
		dataIntent.putExtra("sim1_total_data", sim1TotalData);
		dataIntent.putExtra("sim2_total_data", sim2TotalData);
		if (totalData == 0) {
			dataIntent.putExtra("data_icon", R.drawable.tcl_undata);
		} else if (totalData > 0) {
			float reaminRate = remainData * 100 / totalData;
			if (reaminRate > 0 && reaminRate <= 20) {
				dataIntent.putExtra("remain_data_unit", StringUtil.formatFloatDataFlowSizeByKB(DataManagerApplication.getInstance(), remainData));
				dataIntent.putExtra("data_icon", R.drawable.tcl_data_20);
			} else if (reaminRate > 20 && reaminRate <= 40) {
				dataIntent.putExtra("remain_data_unit", StringUtil.formatFloatDataFlowSizeByKB(DataManagerApplication.getInstance(), remainData));
				dataIntent.putExtra("data_icon", R.drawable.tcl_data_40);
			} else if (reaminRate > 40 && reaminRate <= 60) {
				dataIntent.putExtra("remain_data_unit", StringUtil.formatFloatDataFlowSizeByKB(DataManagerApplication.getInstance(), remainData));
				dataIntent.putExtra("data_icon", R.drawable.tcl_data_60);
			} else if (reaminRate > 60 && reaminRate <= 80) {
				dataIntent.putExtra("remain_data_unit", StringUtil.formatFloatDataFlowSizeByKB(DataManagerApplication.getInstance(), remainData));
				dataIntent.putExtra("data_icon", R.drawable.tcl_data_80);
			} else if (reaminRate > 80 && reaminRate <= 100) {
				dataIntent.putExtra("remain_data_unit", StringUtil.formatFloatDataFlowSizeByKB(DataManagerApplication.getInstance(), remainData));
				dataIntent.putExtra("data_icon", R.drawable.tcl_data_100);
			} else {
				dataIntent.putExtra("data_icon", R.drawable.tcl_out_data);
			}
		}
		DataManagerApplication.getInstance().sendBroadcast(dataIntent);
	}
	
	public static void registerHomeKeyReceiver(Context context) {
	     IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	     context.registerReceiver(NetManagerReceiver.getInstance(), homeFilter);
	 }

	 public static  void unregisterHomeKeyReceiver(Context context) {
	     if (null != NetManagerReceiver.getInstance()) {
	    	 context.unregisterReceiver(NetManagerReceiver.getInstance());
	     }
	 }
	 
}
