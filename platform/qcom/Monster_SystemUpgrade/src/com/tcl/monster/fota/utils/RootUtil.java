package com.tcl.monster.fota.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootUtil {
	private final static String TAG = "RootUtil";

	/**
	 * If su exists on filesystem and it is executable . We think that the
	 * device is rooted .
	 * 
	 * @return
	 */
	public static boolean isDeviceRooted() {
		if (hasRootFlag()) {
			return true;
		}
		String[] places = { "/sbin/", "/system/bin/", "/system/xbin/",
				"/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/",
				"/system/bin/failsafe/", "/data/local/" };
		String binaryName = "su";

		for (String where : places) {
			if (new File(where + binaryName).exists())
				return true;
		}
		return false;
	}

	private static boolean isExecutable(String filePath) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec("ls -l " + filePath);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String str = in.readLine();
			if (str != null && str.length() >= 4) {
				FotaLog.v(TAG, str);
				char flag = str.charAt(3);
				if (flag == 's' || flag == 'x')
					return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
		return false;
	}

	private static boolean hasRootFlag() {
		String rootFlag = "";// ;SystemProperties.get("persist.su_flag");
//		FotaLog.v(TAG, "hasRootFlag -> rootFlag = " + rootFlag);
		if (rootFlag != null && rootFlag.endsWith("1")) {
			return true;
		}
		return false;
	}
}
