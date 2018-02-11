package com.monster.appmanager.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monster.appmanager.applications.ManageApplications.RunningAppInfo;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Debug;

public class MemoryInfoUtil {

	public static int getProcessCount(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		return am.getRunningAppProcesses().size();
	}

	public static long getAvailMem(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		return outInfo.availMem;
	}

	public static long getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
//				Log.i(str2, num + "\t");
			}
			// 获得系统总内存，单位是KB，乘以1024转换为Byte
			initial_memory = Long.valueOf(arrayOfString[1]).longValue() * 1024;
			localBufferedReader.close();
		} catch (IOException e) {
		}
//		 return Formatter.formatFileSize(context, initial_memory);//
		// Byte转换为KB或者MB，内存大小规格化
		return initial_memory;
	}
	
	public static List<String> getRunningProcessPackages(Context mContext) {
		List<String> packageList = new ArrayList<>();
		ActivityManager activityManger = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if (list != null)
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
				if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					String[] pkgList = apinfo.pkgList;
					for (int j = 0; j < pkgList.length; j++) {
						if(!packageList.contains(pkgList[j])){
							packageList.add(pkgList[j]);
						}
					}
				}
			}
		
		return packageList;
	}
	
	public static Map<String, RunningAppInfo> getRunningProcessInfo(Context mContext) {
		Map<String, RunningAppInfo> runningAppInfoMap = new HashMap<>();
		List<String> packageList = new ArrayList<>();
		ActivityManager activityManger = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if (list != null)
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
				if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					int[] myMempid = new int[] {apinfo.pid}; 
					Debug.MemoryInfo[] memInfo = activityManger.getProcessMemoryInfo(myMempid);
					
					String[] pkgList = apinfo.pkgList;
					for (int j = 0; j < pkgList.length; j++) {
						if(!packageList.contains(pkgList[j])){
							packageList.add(pkgList[j]);
							
							if(apinfo.processName.contains(pkgList[j])
									&& !runningAppInfoMap.containsKey(pkgList[j])) {
								RunningAppInfo info = new RunningAppInfo();
								info.setPackageName(pkgList[j]);
								info.setMemSize(memInfo[0].getSummaryTotalPss());
								runningAppInfoMap.put(info.getPackageName(), info);
							}
						}
					}
				}
			}
		
		return runningAppInfoMap;
	}
}
