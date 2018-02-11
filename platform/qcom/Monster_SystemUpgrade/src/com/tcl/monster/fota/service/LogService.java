package com.tcl.monster.fota.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;

import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.FileUtil;
import com.tcl.monster.fota.utils.FotaLog;

/**
 * Record logs , CAUTION : do not use FotaLog to write logs here or
 * death loop will occur .
 * @author haijun.chen
 *
 */
public class LogService extends IntentService {

	private static final String TAG = "LogService";

	public LogService() {
		super(TAG);
	}

	private String writeToFile(String text ,File logFile) {
		if(text == null ){
			return "";
		}
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
			}else{
				
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
		
		long len = logFile.length();
		while(len > FotaConstants.LOG_FILE_SIZE){
			shrinkFileSizeIfNeed(logFile);
			len = logFile.length();
		}
		return logFile.getName();
	}
	
	private void shrinkFileSizeIfNeed(File logFile){
		long fileLength = logFile.length();
		FotaLog.v(TAG, "shrinkFileSizeIfNeed:" + fileLength);
		if(fileLength > 10 * FotaConstants.LOG_FILE_SIZE){
			//avoid OOM error .
			logFile.delete();
			return ;
		}
		if( fileLength >= FotaConstants.LOG_FILE_SIZE){
			File tmp = new File(logFile.getAbsolutePath()+".tmp");
			FileReader reader;
			List<String> logs;
			try {
				reader = new FileReader(logFile);
				logs = FileUtil.readLines(reader);
				int lines = logs.size();

				for (int i = lines / 2; i < lines; i++) {
					try {
						BufferedWriter buf = new BufferedWriter(new FileWriter(
								tmp, true));
						buf.append(logs.get(i));
						buf.newLine();
						buf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				logFile.delete();
				tmp.renameTo(logFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// use Log instead of FotaLog here
		//Log.d(FotaApp.TAG+ "@@@"+TAG, "start collect logs");
		if(intent == null){
			return ;
		}
		String log = intent.getStringExtra(FotaLog.EXTRA_LOG);
		File logFile = (File)intent.getSerializableExtra(FotaLog.EXTRA_LOG_FILE);
		writeToFile(log , logFile);
		

	}
}
