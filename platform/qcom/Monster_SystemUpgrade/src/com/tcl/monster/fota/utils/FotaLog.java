package com.tcl.monster.fota.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.tcl.monster.fota.FotaApp;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.service.LogService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * This class is designed for simple finding of log information when we got logs.
 * We can simply search "FotaApp@@@" to find all informations about this application.
 * Or we can search class name for specific as we did usually.
 */
public class FotaLog {

	private static FotaLog mInstance = new FotaLog();
	
	public static final String EXTRA_LOG = "extra_log";
	
	public static final String EXTRA_LOG_FILE = "extra_log_file";
	/**
	 * Flag to turn on or off debug logs.
	 */
	private static final boolean DEBUG = true;

	/**
	 * Flag to turn on or off verbose logs.
	 */
	private static final boolean VERBOSE = true;
	
	/**
	 * Flag to turn on or off verbose logs.
	 */
	private static final boolean INFO = true;
	
	/**
	 * Flag to turn on or off warn logs.
	 */
	private static final boolean WARN = true;

	/**
	 * Simple method to write debug log.This method will do nothing if DEBUG is
	 * false.
	 */
	public static void d(String tag, String log) {
		if (DEBUG) {
			Log.d(FotaApp.TAG + "@@@" + tag, log);
			//no need to record
//			mInstance.recordLog(formatAndEncryptLog(tag,log));
		}
	}

	/**
	 * Simple method to write debug log.This method will do nothing if DEBUG is
	 * false. This method use FotaApp@@@ as tag.
	 */
	public static void d(String log) {
		d("", log);
	}

	/**
	 * Simple method to write debug log.This method will do nothing if DEBUG is
	 * false.
	 */
	public static void v(String tag, String log) {
		if (VERBOSE) {
			Log.v(FotaApp.TAG + "@@@" + tag, log);
			//no need to record
//			mInstance.recordLog(formatAndEncryptLog(tag,log));
		}
	}

	
	
	/**
	 * Simple method to write verbose log.This method will do nothing if VERBOSE is
	 * false. This method use FotaApp@@@ as tag.
	 */
	public static void v(String log) {
		v("", log);
	}

	/**
	 * Simple method to write debug log.This method will do nothing if DEBUG is
	 * false.
	 */
	public static void i(String tag, String log) {
		if (INFO) {
			Log.i(FotaApp.TAG + "@@@" + tag, log);
			mInstance.recordLog(formatAndEncryptLog(tag,log,false),FotaUtil.updateLog());
		}
	}

	
	
	/**
	 * Simple method to write verbose log.This method will do nothing if VERBOSE is
	 * false. This method use FotaApp@@@ as tag.
	 */
	public static void i(String log) {
		i("", log);
	}
	
	
	/**
	 * Simple method to write debug log.This method will do nothing if DEBUG is
	 * false.
	 */
	public static void w(String tag, String log) {
		if (WARN) {
			Log.w(FotaApp.TAG + "@@@" + tag, log);
			mInstance.recordLog(formatAndEncryptLog(tag,log, false),FotaUtil.updateLog());
		}
	}
	
	/**
	 * For special case
	 */
	public static void s(String tag ,String log){
		Log.w(FotaApp.TAG + "@@@" + tag, log);
		File sdcard = Environment.getExternalStorageDirectory();
		File logFile = new File(sdcard, "fotaapps.log");
		mInstance.recordLog(formatAndEncryptLog(tag,log ,false),logFile);
	}
	
	private boolean mEnableRecord = true;

	public void setEnableRecordLog(boolean enable){
		mEnableRecord = enable;
	}
	
	private void recordLog(String log , File logFile){
		Context context = FotaApp.getApp();
		if(context != null && mEnableRecord){
			Intent i = new Intent(context , LogService.class);
			i.putExtra(EXTRA_LOG, log);
			i.putExtra(EXTRA_LOG_FILE, logFile);
			context.startService(i);
		}
	}
	
	private static String formatAndEncryptLog(String tag,String log , boolean encryptLog){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String timestamp = sdf.format(cal.getTime());
		String format = timestamp +" :  " + FotaApp.TAG + "@@@" + tag +" :  " + log;
		if(encryptLog){
			String encrypt = AESUtil.encrypt(FotaUtil.appendTail(), format);
			return encrypt;
		}
		return format;
	}
	
	/**
	 * For testing
	 */
	public static void decryptLogToFile(){
		File logFile = FotaUtil.updateLog();
		FileReader reader;
		List<String> logs;
		try {
			reader = new FileReader(logFile);
			logs = FileUtil.readLines(reader);
			for(String s : logs){
				String decrypt = AESUtil.decrypt(FotaUtil.appendTail(), s);
				writeToFile(decrypt);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * For testing
	 */
	private static String writeToFile(String text) {
		if(text == null ){
			return "";
		}
		File logFile =  new File(FotaConstants.UPDATE_FILE_DIR , FotaConstants.UPDATE_LOG_NAME+".txt");
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logFile.getName();
	}
}
