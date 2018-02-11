package com.android.packageinstaller.adplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.qscanner.QScanListenerV2;
import tmsdk.fg.module.qscanner.QScannerManagerV2;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 广告扫描 2016/8/8 16:52
 * @author caoyong
 * 
 */
public class ScannerAdPlugin {
	/**
	 * 扫描开始
	 */
	public static final int MSG_SCANNER_START = 1001;
	/**
	 * 扫描结束
	 */
	public static final int MSG_SCANNER_END = 1002;
	/**
	 * 扫描出错
	 */
	public static final int MSG_SCANNER_ERROR = 1003 ;
	/**
	 * 扫描暂停
	 */
	public static final int MSG_SCANNER_PAUSE = 1004 ;
	/**
	 * 扫描取消
	 */
	public static final int MSG_SCANNER_CANNEL = 1005 ;
	/**
	 * 获取不到扫描文件
	 */
	public static final int MSG_SCANNER_NO_FOUND = 1006 ;
	
	private QScannerManagerV2 mQScannerMananger;//病毒扫描功能接口
	private Thread mScanThread;//扫描线程对象
	private Handler mHandler ;
	
	public ScannerAdPlugin(Handler handler){
		mHandler = handler ;
		mQScannerMananger = ManagerCreatorF.getManager(QScannerManagerV2.class);
		
        if(mQScannerMananger.initScanner()==0) {
			Log.v("demo", "initScanner return true");
		} else {
			Log.v("demo", "initScanner return false");
		}
        
	}
	
	/**
	 * 扫描应用
	 * @param pkgName 扫描的包名
	 */
	public void startScanner(final String pkgName){
		mScanThread = new Thread() {
			@Override
			public void run() {
				ArrayList<String> pkgNames = new ArrayList<String>() ;
				pkgNames.add(pkgName) ;
				
				mQScannerMananger.scanSelectedPackages(pkgNames ,new MyQScanListener(), true);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	     };
	     mScanThread.start() ;
	}
	
	public void startScannerApk(final String path){
		mScanThread = new Thread() {
			@Override
			public void run() {
				ArrayList<String> pathNames = new ArrayList<String>() ;
				pathNames.add(path) ;
				
				mQScannerMananger.scanSelectedApks(pathNames ,new MyQScanListener(), true);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	     };
	     mScanThread.start() ;
	}
	
	private class MyQScanListener extends QScanListenerV2 {
		
		/**
		 * 扫描开始
		 * @param scanType
		 */
		@Override
		public void onScanStarted(int scanType) {
			Message msg = new Message() ;
			msg.what = ScannerAdPlugin.MSG_SCANNER_START ;
			mHandler.sendMessage(msg) ;
		}
		
		/**
		 * 安装包扫描进度回调
		 * 
		 * @param scanType
		 *            扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
		 *            {@link QScanConstants#SCAN_SPECIALS}
		 * @param progress
		 *            扫描进度 像未安装apk扫描，progress无法计算，这里会返回-1的值，标识未知
		 * @param result
		 *            扫描项信息
		 */
		@Override
		public void onScanProgress(int scanType, int progress,
				QScanResultEntity result) {
			
		}
		
		/**
		 * 搜索到不扫描的文件的回调
		 */
		@Override
		public void onFoundElseFile(int scanType, File file) {
			Message msg = new Message() ;
			msg.what = ScannerAdPlugin.MSG_SCANNER_NO_FOUND;
			mHandler.sendMessage(msg) ;
		}
		
		/**
		 * 云扫描出现网络错误
		 * 
		 * @param scanType
		 *            扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
		 *            {@link QScanConstants#SCAN_SPECIALS}
		 * @param errCode
		 *            错误码
		 */
		@Override
		public void onScanError(int scanType, int errCode) {
				Message msg = new Message() ;
				msg.what = ScannerAdPlugin.MSG_SCANNER_ERROR;
				mHandler.sendMessage(msg) ;
		}
		
		/**
		 * 扫描被暂停时回调
		 */
		@Override
		public void onScanPaused(int scanType) {
			Message msg = new Message() ;
			msg.what = ScannerAdPlugin.MSG_SCANNER_PAUSE;
			mHandler.sendMessage(msg) ;
		}
		
		/**
		 * 扫描继续时回调
		 */
		@Override
		public void onScanContinue(int scanType) {
			
		}
		
		/**
		 * 扫描被取消时回调
		 */
		@Override
		public void onScanCanceled(int scanType) {
			Message msg = new Message() ;
			msg.what = ScannerAdPlugin.MSG_SCANNER_CANNEL;
			mHandler.sendMessage(msg) ;
		}
		
		/**
		 * 扫描结束
		 * 
		 * @param scanType
		 *            扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
		 *            {@link QScanConstants#SCAN_SPECIALS}
		 * @param results
		 *            扫描的所有结果
		 */
		@Override
		public void onScanFinished(int scanType, List<QScanResultEntity> results) {
			Message msg = new Message() ;
			msg.what = ScannerAdPlugin.MSG_SCANNER_END;
			msg.obj = results ;
			mHandler.sendMessage(msg) ;
		}
		
	}
	
}
