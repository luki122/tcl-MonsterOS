package com.tcl.monster.fota.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.os.RecoverySystem;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tcl.monster.fota.FotaMainActivity;
import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;
import com.tcl.monster.fota.utils.FotaUtil;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class FotaUpdateService extends IntentService {
	/**
	 * TAG for Log
	 */
	private static final String TAG = FotaUpdateService.class.getSimpleName();
	
	/**
	 * The task that is currently ongoing. this task contains a lot of information about this update.
	 */
	DownloadTask mCurrentTask ;
	
	/**
	 * Constructor method.
	 */
	public FotaUpdateService() {
		super(TAG);
	}

	/**
	 * Start FotaUpdateService action
	 */
	public static final String ACTION_DO_UPDATE = "com.tcl.fota.action.DO_UPDATE";

	/**
	 * Battery threshold
	 */
	public static final int BATTERY_WARNING_THRESHOLD = 35;

	/**
	 * Install update need minimum size
	 */
	public static final long INSTALL_NEED_MIN_SIZE = 500;

    /**
     * Flag to indicate if we should notify update after
     * battery regained to more than 35%.
     */
    private boolean mShouldNotifyUpdateWhenBatteryOK ;
    
    /**
     * current battery level value .
     */
    private int mBatteryLevel = 0;
    
    /**
     * Filter to register Intent.ACTION_BATTERY_CHANGED
     */
    private IntentFilter mIntentFilter;

	@Override
	public void onCreate() {
		super.onCreate();
		//register battery changed here
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mIntentReceiver, mIntentFilter);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		//unregister receivers 
		unregisterReceiver(mIntentReceiver);
    }
    
    /**
     * BroadcastReceiver to monitor battery change.
     */
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            	mBatteryLevel = intent.getIntExtra("level", 0);
                 if(mBatteryLevel >= BATTERY_WARNING_THRESHOLD && mShouldNotifyUpdateWhenBatteryOK){
					Intent i = new Intent(getApplicationContext(),
							FotaMainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplicationContext().startActivity(i);
					mShouldNotifyUpdateWhenBatteryOK = false;
            	}
            }
        }
    };
	
    /**
     * This method verifies downloaded package .
     * @param update the update file
     * @return true if pass, false otherwise.
     */
	private boolean verifyDownloadedPackage(File update){
		//1) check file exists
		boolean fileExists = update.exists();
		if(!fileExists){
			FotaLog.v(TAG, "verifyDownloadedPackage file not exists");
			onVerifyFail();
			return false;
		}
		
		//2) check md5 check sum
		String sha1 = FotaUtil.SHA1(update);
		mCurrentTask = FotaUIPresenter.getInstance(this).getCurrentDownloadTask();
		boolean f = mCurrentTask.getUpdateInfo().mFiles.get(0).mCheckSum.equalsIgnoreCase(sha1);
		if(!f){
			FotaLog.w(TAG, "verifyDownloadedPackage checksum not same!!!");
			FotaLog.w(TAG, "verifyDownloadedPackage checksum sha1:" + sha1 + ","
					+ mCurrentTask.getUpdateInfo().mFiles.get(0).mCheckSum);
			onVerifyFail();
			return false;
		}
		
		long fileSize = mCurrentTask.getUpdateInfo().mFiles.get(0).mFileSize ;
		if(update.length() != fileSize){
			FotaLog.w(TAG, "verifyDownloadedPackage file size not same!!!");
			FotaLog.v(TAG, "mCurrentTask.getUpdateInfo().mFiles.get(0).mFileSize : " + fileSize
					+ ",file length():" + update.length());
			onVerifyFail();
			return false;
		}

		//3) RecoverySystem to verify package
		boolean v = false;
		try {
			RecoverySystem.verifyPackage(update, null, null);
			v = true;
		} catch (IOException e) {
			FotaLog.w(TAG, "RecoverySystem.verifyPackage(update,null , null): "
					+ Log.getStackTraceString(e));
		} catch (GeneralSecurityException e) {
			FotaLog.w(TAG, "RecoverySystem.verifyPackage(update,null , null): "
					+ Log.getStackTraceString(e));
		}
		
		// do not care result of  verify package in test mode.
		if(FotaUtil.isTestMode()){
			v = true;
		}
		
		if(!v){
			onVerifyFail();
			return false;
		}
		return true;
	}
	
	/**
	 * Default implementation, not used here.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
    	if(intent == null){
    		return;
    	}
    	if(intent.getAction() == null){
    		return ;
    	}
    	if(!intent.getAction().equals(ACTION_DO_UPDATE)){
    		return ;
    	}

		if(FotaUtil.isCalling(this)){
			FotaLog.d(TAG, "onHandleIntent -> calling..." );
			notifyWaitForCallEnd();
			return;
		}

		// if we don't get battery level, then wait.
		while (mBatteryLevel == 0) {
			try {
				FotaLog.w(TAG, "onHandleIntent -> sleep to wait get mBatteryLevel:" + mBatteryLevel);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (mBatteryLevel < BATTERY_WARNING_THRESHOLD) {
			FotaLog.w(TAG, "onHandleIntent -> mBatteryLevel :" + mBatteryLevel);
			notifyBatteryNotEnough();
//			mShouldNotifyUpdateWhenBatteryOK = true;
			return;
		}

		long needSize = FotaUtil.getFotaInstallNeedMinSize(getApplicationContext());
		File dataFile = Environment.getDataDirectory();
		long availableSize = FotaUtil.getAvailableStorageSize(dataFile.getPath());
		long percentSize = FotaUtil.getStorageSize(dataFile.getPath());
		FotaLog.w(TAG, "availableSize = " + availableSize + ", percentSize = "
				+ percentSize + ", needSize = " + needSize);
		if (needSize == 0) {
			needSize = percentSize <= INSTALL_NEED_MIN_SIZE ? percentSize : INSTALL_NEED_MIN_SIZE;
		}
		FotaLog.w(TAG, "needSize = " + needSize + ", availableSize = " + availableSize);
		if (availableSize < needSize) {
			onStorageNotEnough();
			return;
		}

		//check if package is vaild.
		File update = FotaUtil.updateZip();
		FotaLog.d(TAG, "update.zip path : " + update.getAbsolutePath());
		boolean f = verifyDownloadedPackage(update);
		FotaLog.d(TAG, "verifyDownloadedPackage result = " + f);
		if (f) {
			try {
				RecoverySystem.installPackage(this, update);
			} catch (IOException e) {
				FotaLog.w(TAG, "installPackage failed :" + Log.getStackTraceString(e));
				onUpdateFail();
			}
		}
	}

	private void notifyBatteryNotEnough() {
		FotaUIPresenter.getInstance(this).showUpdateResult(
				FotaUIPresenter.FOTA_INSTALL_RESULT_LOW_BATTERY);
	}

	private void notifyWaitForCallEnd() {
		FotaUIPresenter.getInstance(this).showUpdateResult(
				FotaUIPresenter.FOTA_INSTALL_RESULT_UPDATE_WAIT);
	}

	private void onStorageNotEnough() {
		FotaUIPresenter.getInstance(this).showUpdateResult(
				FotaUIPresenter.FOTA_INSTALL_RESULT_STORAGE_NOT_ENOUGH);
	}

	private void onVerifyFail() {
		FotaUIPresenter.getInstance(this).deleteUpdatePackage();
		FotaUIPresenter.getInstance(this).
				showUpdateResult(FotaUIPresenter.FOTA_INSTALL_RESULT_VERIFY_FAIL);
	}

	private void onUpdateFail() {
		FotaUIPresenter.getInstance(this).deleteUpdatePackage();
		FotaUIPresenter.getInstance(this).
				showUpdateResult(FotaUIPresenter.FOTA_INSTALL_RESULT_UPDATE_FAIL);
	}
}