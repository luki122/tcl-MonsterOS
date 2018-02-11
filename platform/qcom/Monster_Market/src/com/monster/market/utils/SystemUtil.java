package com.monster.market.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.bean.AppDetailInfo;
import com.monster.market.bean.AppListInfo;
import com.monster.market.bean.AppUpgradeInfo;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.constants.Constant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.download.AppDownloadData;
import com.monster.market.http.data.ReportDownloadInfoRequestData;
import com.monster.market.install.InstallAppManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SystemUtil {

	public static final String TAG = "SystemUtil";

	public static String getImei(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		return manager.getDeviceId();
	}

	/**
	 * @Title: bytes2kb
	 * @Description: byte转为KB或者MB字符串
	 * @param @param bytes
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String bytes2kb(long bytes) {
		BigDecimal fileSize = new BigDecimal(bytes);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = fileSize.divide(megabyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		if (returnValue > 1)
			return (returnValue + "M");
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		return (returnValue + "K");
	}

	public static boolean hasNetwork() {
		Context context = MarketApplication.getInstance();
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
			if (wifiState == State.CONNECTED || mobileState == State.CONNECTED) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static boolean isWifiNetwork(Context context) {
		if (!isNetworkConnected(context)) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			LogUtil.d(TAG, "isWifiNetwork None network");
			return false;
		}
		// 连接后判断当前WIFI
		if (getConnectingType(context) == Constant.NETWORK_WIFI) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int getConnectingType(Context context) {
		ConnectivityManager mConnectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

		NetworkInfo info = mConnectivity.getActiveNetworkInfo();

		if (info == null || !mConnectivity.getBackgroundDataSetting()) {
			return -1;
		}

		int netType = info.getType();
		int netSubtype = info.getSubtype();

		if (netType == ConnectivityManager.TYPE_WIFI) {
			return Constant.NETWORK_WIFI;
		} else {
			if ((netSubtype == TelephonyManager.NETWORK_TYPE_GPRS) || (netSubtype == TelephonyManager.NETWORK_TYPE_EDGE)
					|| (netSubtype == TelephonyManager.NETWORK_TYPE_CDMA)) {
				return Constant.NETWORK_2G;
			} else {
				return Constant.NETWORK_3G;
			}
		}

	}

	public static boolean isMobileNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");

		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if (info == null) {
			return false;
		}

		int netType = info.getType();

		// Check if Mobile Network is connected
		if (netType == ConnectivityManager.TYPE_MOBILE) {
			return info.isConnected();
		} else {
			return false;
		}
	}

	public static String buildDownloadTaskId(String packageName, int versionCode) {
		return packageName + "_" + versionCode;
	}

	public static AppDownloadData buildAppDownloadData(AppListInfo info) {
		if (info != null) {
			AppDownloadData tmp_data = new AppDownloadData();
			tmp_data.setTaskId(buildDownloadTaskId(info.getPackageName(), info.getVersionCode()));
			tmp_data.setApkId(info.getAppId());
			tmp_data.setApkDownloadPath(info.getDownloadUrl());
			tmp_data.setApkLogoPath(info.getBigAppIcon());
			tmp_data.setApkName(info.getAppName());
			tmp_data.setPackageName(info.getPackageName());
			tmp_data.setVersionCode(info.getVersionCode());
			tmp_data.setVersionName(info.getVersionName());
			tmp_data.setDownload_type(WandoujiaDownloadConstant.TYPE_NORMAL);

			// 检查是否更新(上报数据用)
			InstalledAppInfo appInfo = InstallAppManager
					.getInstalledAppInfo(MarketApplication.getInstance(),
							info.getPackageName());
			if (appInfo != null) {
				if (info.getVersionCode() > appInfo.getVersionCode()) {
					tmp_data.setDownload_type(WandoujiaDownloadConstant.TYPE_UPDATE);
				}
			}
			return tmp_data;
		}
		return null;
	}

	public static AppDownloadData buildAppDownloadData(AppUpgradeInfo info) {
		if (info != null) {
			AppDownloadData tmp_data = new AppDownloadData();
			tmp_data.setTaskId(buildDownloadTaskId(info.getPackageName(), info.getVersionCodeNew()));
			tmp_data.setApkId(info.getAppId());
			tmp_data.setApkDownloadPath(info.getDownloadUrl());
			tmp_data.setApkLogoPath(info.getAppIcon());
			tmp_data.setApkName(info.getAppName());
			tmp_data.setPackageName(info.getPackageName());
			tmp_data.setVersionCode(info.getVersionCodeNew());
			tmp_data.setVersionName(info.getVersionNameNew());
			tmp_data.setDownload_type(WandoujiaDownloadConstant.TYPE_NORMAL);

			// 检查是否更新(上报数据用)
			InstalledAppInfo appInfo = InstallAppManager
					.getInstalledAppInfo(MarketApplication.getInstance(),
							info.getPackageName());
			if (appInfo != null) {
				if (info.getVersionCode() > appInfo.getVersionCode()) {
					tmp_data.setDownload_type(WandoujiaDownloadConstant.TYPE_UPDATE);
				}
			}
			return tmp_data;
		}
		return null;
	}

	public static AppDownloadData buildAppDownloadData(AppDetailInfo info) {
		if (info != null) {
			AppDownloadData tmp_data = new AppDownloadData();
			tmp_data.setTaskId(buildDownloadTaskId(info.getPackageName(), info.getVersionCode()));
			tmp_data.setApkId(info.getAppId());
			tmp_data.setApkDownloadPath(info.getDownloadUrl());
			tmp_data.setApkLogoPath(info.getBigAppIcon());
			tmp_data.setApkName(info.getAppName());
			tmp_data.setPackageName(info.getPackageName());
			tmp_data.setVersionCode(info.getVersionCode());
			tmp_data.setVersionName(info.getVersionName());
			tmp_data.setDownload_type(WandoujiaDownloadConstant.TYPE_NORMAL);

			// 检查是否更新(上报数据用)
			InstalledAppInfo appInfo = InstallAppManager
					.getInstalledAppInfo(MarketApplication.getInstance(),
							info.getPackageName());
			if (appInfo != null) {
				if (info.getVersionCode() > appInfo.getVersionCode()) {
					tmp_data.setDownload_type(WandoujiaDownloadConstant.TYPE_UPDATE);
				}
			}
			return tmp_data;
		}
		return null;
	}

	public static ReportDownloadInfoRequestData buildReportDownloadInfoRequestData(AppDownloadData downloadData) {
		if (downloadData != null) {
			ReportDownloadInfoRequestData requestData = new ReportDownloadInfoRequestData();
			requestData.setAppId(downloadData.getApkId());
			requestData.setDownloads(1);
			requestData.setPackageName(downloadData.getPackageName());
			requestData.setDownloadTime(String.valueOf(System.currentTimeMillis()));
			if (downloadData.getDownload_type().equals(WandoujiaDownloadConstant.TYPE_NORMAL)) {
				requestData.setDownloadType(1);
			} else if (downloadData.getDownload_type().equals(WandoujiaDownloadConstant.TYPE_UPDATE)) {
				requestData.setDownloadType(2);
			}
			requestData.setModulId(downloadData.getReportModulId());
			return requestData;
		}
		return null;
	}

	public static DisplayImageOptions buildAppListDisplayImageOptions(Context context) {
		DisplayImageOptions optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.icon_app_default)
				.showImageForEmptyUri(R.drawable.icon_app_default)
				.showImageOnFail(R.drawable.icon_app_default)
//				.displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelOffset(R.dimen.app_icon_displayer)))
				.cacheInMemory(true).cacheOnDisk(true).build();
		return optionsImage;
	}

	public static DisplayImageOptions buildTopicDisplayImageOptions(Context context) {
		DisplayImageOptions optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_launcher)
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
//				.displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelOffset(R.dimen.app_icon_displayer)))
				.cacheInMemory(true).cacheOnDisk(true).build();
		return optionsImage;
	}

	/**
	 * @Title: intstallApp @Description: 安装应用 @param @param
	 * context @param @param apkFile @param @param observer @return void @throws
	 */
	public static int intstallApp(Context context, String packageName, File apkFile,
			IPackageInstallObserver.Stub observer) {
		PackageManager pm = context.getPackageManager();
		if (TextUtils.isEmpty(packageName)) {
			PackageParser.Package parsed = getPackageInfo(apkFile);
			packageName = parsed.packageName;
		}
		int result = 0;
		int installFlags = 0;
		try {
			PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
				result = 1;
			}
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}

		Uri mPackageURI = Uri.fromFile(apkFile);
		String filepath = mPackageURI.getPath();
		pm.installPackage(mPackageURI, observer, installFlags, null);
		return result;
	}

	private static PackageParser.Package getPackageInfo(File sourceFile) {

		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		Object pkg = null;
		final String archiveFilePath = sourceFile.getAbsolutePath();
		try {
			Class<?> clazz = Class.forName("android.content.pm.PackageParser");
			Object instance = getParserObject(archiveFilePath);
			if (Build.VERSION.SDK_INT >= 21) {
				Method method = clazz.getMethod("parsePackage", File.class, int.class);
				pkg = method.invoke(instance, sourceFile, 0);
			} else {
				Method method = clazz.getMethod("parsePackage", File.class, String.class, DisplayMetrics.class,
						int.class);
				pkg = method.invoke(instance, sourceFile, archiveFilePath, metrics, 0);
			}
			instance = null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (PackageParser.Package) pkg;
	}

	private static Object getParserObject(String archiveFilePath) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		Class<?> clazz = Class.forName("android.content.pm.PackageParser");
		return Build.VERSION.SDK_INT >= 21 ? clazz.getConstructor().newInstance()
				: clazz.getConstructor(String.class).newInstance(archiveFilePath);
	}

	public static String getIp(Context context) {
		if (isWifiNetwork(context)) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			String ip = intToIp(ipAddress);
			return ip;
		} else {
			return getLocalIpAddress();
		}
	}

	public static String getMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	public static int getApiLevel() {
		return Build.VERSION.SDK_INT;
	}

	private static String intToIp(int i) {

		return (i & 0xFF ) + "." +
				((i >> 8 ) & 0xFF) + "." +
				((i >> 16 ) & 0xFF) + "." +
				( i >> 24 & 0xFF) ;
	}

	private static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			LogUtil.e("WifiPreference IpAddress", ex.toString());
		}
		return null;
	}

	public static String getApkMD5(Context context, String packageName) {
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (pkgInfo != null) {
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			return FileUtil.getFileMD5(new File(appInfo.sourceDir));
		}
		return "";
	}

	/**
	 * 获取已安装apk签名
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getInstallPackageSignature(Context context, String packageName) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			String thisName = packageInfo.packageName;
			if (thisName.equals(packageName)) {
				return DigestUtil.computeMd5forPkg(packageInfo.signatures[0].toByteArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getNetStatus(Context context) {
		if (!SystemUtil.isNetworkConnected(context)) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			LogUtil.d(TAG, "isWifiNetwork None network");
			return 0;
		}
		// 连接后判断当前WIFI
		if (SystemUtil.getConnectingType(context) == Constant.NETWORK_WIFI) {
			return 1;
		} else {
			return 2;
		}
	}

}
