package com.monster.market.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;

import android.content.Context;
import android.os.Environment;

public class FileUtil {
	
	public static boolean isExistSDcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean deleteFile(File file) {
		if (file != null && file.exists()) {
			return file.delete();
		}
		return false;
	}
	
	public static String getFromAssets(Context context, String fileName) {
		try {
			InputStreamReader inputReader = new InputStreamReader(
					context.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getFileMD5(File file) {
		if (!file.isFile()){
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in=null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	//===============================================================
	
	public static String getAPKFilePath(Context context) {
		String pathString = "";
		if (isExistSDcard()) {
			pathString = getSDcardApkPath();
		} else {
			pathString = getDataApkPath(context);
		}
		File temp = new File(pathString);
		if (!temp.exists()) {
			temp.mkdirs();
		}
		return pathString;
	}
	
	private static String getSDcardApkPath() {
		return Environment.getExternalStorageDirectory()
				+ "/TCLStore Download/apk/";
	}
	
	private static String getDataApkPath(Context ctx) {
		return "/data/data/" + ctx.getPackageName() + "/cache/apk/";
	}

}
