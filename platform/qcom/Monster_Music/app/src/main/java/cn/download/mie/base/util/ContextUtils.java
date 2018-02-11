package cn.download.mie.base.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.tcl.music.util.LogUtil;

public class ContextUtils {
	public static final String TAG = "ContextUtils";
	public static boolean isMainThread() {
		long id = Thread.currentThread().getId();
		return id == Looper.getMainLooper().getThread().getId();
	}

	public static void detectUINetwork(Context context) {

        if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
        	  ThreadPolicy.Builder threadPolicyBuilder = new ThreadPolicy.Builder();
        	  threadPolicyBuilder.detectDiskReads()
        	  					.detectDiskWrites()
        	  					.detectAll();
        	  
        	  StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        	  VmPolicy.Builder vmPolicyBuilder = new VmPolicy.Builder();
        	  vmPolicyBuilder.detectLeakedSqlLiteObjects();
        	  
        	  if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
        	      //vmPolicyBuilder.detectLeakedClosableObjects();
        	  }
        	  vmPolicyBuilder.penaltyLog();
        	  StrictMode.setVmPolicy(vmPolicyBuilder.penaltyDeath().build());
        }
	}

	public static Map<String, String> getPushCommonParams(Context context) {
		Map<String,String> params = new HashMap<>();
		String region = getMetaData(context, "REGION");
		if (!TextUtils.isEmpty(region)) {
			params.put("region", region);
		}
		PackageManager packageManager = context.getPackageManager();
		try {

			PackageInfo pi = packageManager.getPackageInfo(context.getPackageName(), 0);
			if (pi != null ) {
				params.put("version_name", pi.versionName);
				params.put("version_code", String.valueOf(pi.versionCode));
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (!TextUtils.isEmpty(telephonyManager.getSubscriberId())) {
			params.put("imsi", telephonyManager.getSubscriberId());
		}
		if (!TextUtils.isEmpty(telephonyManager.getDeviceId())) {
			params.put("imei", telephonyManager.getDeviceId());
		}
		params.put("model", Build.MODEL);
		String lang = context.getResources().getConfiguration().locale.toString();
		params.put("language",lang);
		params.put("os", "android");
		params.put("os_version", Build.VERSION.RELEASE);
		params.put("os_sdk", String.valueOf(Build.VERSION.SDK_INT));

		WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

		if (wifiMgr != null) {
			WifiInfo info = wifiMgr.getConnectionInfo();
			if (info != null) {
				String mac = info.getMacAddress();// 获得本机的MAC地址
				if (!TextUtils.isEmpty(mac)) {
					mac = mac.replaceAll(":", "-");
					params.put("mac", mac);
				}
			}
		}

		params.put("pkg", context.getPackageName());

		return params;
	}

	public static String getMetaData(Context context, String name) {
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo applicationInfo;
		Object value = null;
		try {

			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
			if (applicationInfo != null && applicationInfo.metaData != null) {
				value = applicationInfo.metaData.get(name);
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			LogUtil.d(TAG, "Could not read the name(%s) in the manifest file." + name);
			return null;
		}

		return value == null ? null : value.toString();
	}	
	
	public static String getVersionName(Context context) {
		PackageManager packageManager = context.getPackageManager();
		try {

			PackageInfo pi = packageManager.getPackageInfo(context.getPackageName(), 0);
			if (pi != null ) {
				return pi.versionName;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static int getVersionCode(Context context) {
		PackageManager packageManager = context.getPackageManager();
		try {

			PackageInfo pi = packageManager.getPackageInfo(context.getPackageName(), 0);
			if (pi != null ) {
				return pi.versionCode;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	/**
	 * Open the activity to let user allow wifi feature in Settings app.
	 * 
	 * @param context
	 *            from which invoke this method
	 */
	public static void openWIFISettings(Context context) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_WIFI_SETTINGS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static String getCurrentProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		
		List<ActivityManager.RunningAppProcessInfo> processInfos = mActivityManager
				.getRunningAppProcesses();
		
		for (ActivityManager.RunningAppProcessInfo appProcess : processInfos) {
			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		
		return null;
	}

	public static boolean isChildProcess(Context context) {
		String process = getCurrentProcessName(context);
		String pkName = context.getPackageName();

		return !pkName.equals(process);
	}
	
	public static boolean install(Context context, String apkPath) {
		if (TextUtils.isEmpty(apkPath)) {
			LogUtil.d(TAG, "download complete intent has no path param");
			return false;
		}
		
		File file = new File(apkPath);
		if (!file.exists()) {
			LogUtil.d(TAG, "file %s not exists" + apkPath);
			return false;
		}
		
		if(isSystemApp(context)){
			return systemInstall(apkPath);
		}else{

			try{
				File apkFile = new File(apkPath);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}

	        return true;
		}
	}

    /***
     *
     * @param context
     * @param uri
     * @return
     */
	public static boolean install(Context context, Uri uri) {
		if (uri == null) {
			LogUtil.d(TAG, "download complete intent has no path param");
			return false;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW); 
	    intent.setDataAndType(uri, "application/vnd.android.package-archive"); 
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(intent);
	    return true;
	}
	/**
	 * 是否为系统应用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSystemApp(Context context) {
		return ((context.getApplicationInfo()).flags & ApplicationInfo.FLAG_SYSTEM) > 0;
	}
	
	public static boolean systemInstall(String apkPath) {
		String result = sysInstall(apkPath).trim();
		int lastIndex = result.lastIndexOf("/n");
		if (lastIndex == -1) {
			return false;
		}
		result = result.substring(lastIndex + 2);
		return "success".equalsIgnoreCase(result);
	}
	
	/**
	 * 系统级自动安装
	 * 
	 * @param apkPath
	 * @return
	 */
	public static String sysInstall(String apkPath) {
		String[] args = { "pm", "install", "-r", apkPath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write("\n".getBytes("utf-8"));
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
	
	/**
	 * 静默安装APK， 需要ROOT权限
	 * 
	 * @param apkPath APK的文件路径
	 * @return
	 */
	public static boolean installSilent(String apkPath) {
		int result = -1;
		DataOutputStream dos = null;
		String cmd = "pm install -r " + apkPath;
		try {
			Process p = Runtime.getRuntime().exec("su");
			dos = new DataOutputStream(p.getOutputStream());
			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			p.waitFor();
			result = p.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result == 0;
	}
	
	public static boolean isHome(Context context) {
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		List<String> homePackageNames = getHomes(context);
		return homePackageNames.contains(rti.get(0).topActivity
				.getPackageName());
	}

	private static List<String> getHomes(Context context) {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = context.getPackageManager();
		// 属性
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}
	
	public static ComponentName topActivity(Context context) {
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE) ;
		try {
			List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
			 if(runningTaskInfos != null && runningTaskInfos.size() > 0) {
				 ComponentName component = runningTaskInfos.get(0).topActivity;
				 return component;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private final static int kSystemRootStateUnknow = -1;
	private final static int kSystemRootStateDisable = 0;
	private final static int kSystemRootStateEnable = 1;
	private static int sRootState = kSystemRootStateUnknow;

	/**
	 * 判断系统是否已经ROOT
	 * @return
	 */
	public static boolean hasSystemRooted() {
		
		if (sRootState == kSystemRootStateEnable) {
			return true;
		} else if (sRootState == kSystemRootStateDisable) {
			return false;
		}
		
		File f = null;
		final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/",
				"/system/sbin/", "/sbin/", "/vendor/bin/" };
		try {
			for (int i = 0; i < kSuSearchPaths.length; i++) {
				f = new File(kSuSearchPaths[i] + "su");
				if (f != null && f.exists()) {
					sRootState = kSystemRootStateEnable;
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		sRootState = kSystemRootStateDisable;
		return false;
	}

    public static boolean isNewerThan21SystemApp(Context context,String packageName){
        PackageManager pManager = context.getPackageManager();
        try{
            PackageInfo packageInfo = pManager.getPackageInfo(packageName,0);
            if( packageInfo!=null ){
                // API>21,5.1及以上
                if( Build.VERSION.SDK_INT > 21 && "android.uid.system".equals( packageInfo.sharedUserId ) ){
                    return true;
                }

            }

        }catch (Exception e){

        }
        return false;
    }

    public static boolean isExistOfApp(Context context,String packageName){
        PackageManager pManager = context.getPackageManager();
        try{
            PackageInfo packageInfo = pManager.getPackageInfo(packageName,0);
            if( packageInfo!=null ){
                return true;
            }

        }catch (Exception e){

        }
        return false;
    }
    /**
	 * 打开系统浏览器
	 * @param url
	 * @param context
	 */
	public static void openWebByURL(String url, Context context) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(url);
		intent.setData(uri);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);

		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getDeviceId(Context context){

		String device_id = getTCLTVDeviceId(context);
		LogUtil.e(TAG,"device_id-->"+device_id);
		android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if(TextUtils.isEmpty(device_id)){
			device_id = tm.getDeviceId();
		}
		android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(TextUtils.isEmpty(device_id)){
			device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
		}
		String mac = wifi.getConnectionInfo().getMacAddress();
		if(TextUtils.isEmpty(device_id)){
			device_id = mac;
		}
		if(TextUtils.isEmpty(device_id)){
			device_id = SharedPreferenceUtil.readString(context, "deviceId");
			if(TextUtils.isEmpty(device_id)){
				device_id = UUID.randomUUID().toString();
				SharedPreferenceUtil.saveString(context,"deviceId",device_id);
			}
		}
		device_id = device_id.replaceAll(" ","");
		device_id = device_id.replaceAll("-","");
		device_id = device_id.replaceAll(":","");
		LogUtil.e(TAG, "device_id--->"+device_id);
		LogUtil.d("deviceid", "deviceid = " + device_id);
		return device_id;
//		return "aaaeb15633fcaac9";
//		return "1a26409357ae180e";
	}

	private static String getTCLTVDeviceId(Context context) {
		ContentResolver resolver = context.getContentResolver();
		String[] columns = new String[]{"deviceid"};
		Uri myUri = Uri.parse("content://com.tcl.xian.StartandroidService.MyContentProvider/devicetoken");
		Cursor cur = resolver.query(myUri, columns, null, null, null);
		String deviceid = "";
		if (cur != null) {
			if (cur.moveToFirst()) {
				do {
					deviceid = cur.getString(cur.getColumnIndex("deviceid"));
				} while (cur.moveToNext());
			}

			cur.close();
		}

		if (deviceid == null) {
			deviceid = "";
		}
		return deviceid;
	}

	public static int dip2px(Context context, int dp){
		return (int)(dp * context.getResources().getDisplayMetrics().density + 0.5f);
	}
}
