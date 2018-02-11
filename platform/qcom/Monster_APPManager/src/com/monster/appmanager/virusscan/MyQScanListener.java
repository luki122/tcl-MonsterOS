package com.monster.appmanager.virusscan;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.monster.appmanager.db.MulwareProvider;
import com.monster.appmanager.db.MulwareProvider.MulwareTable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScanListenerV2;

public class MyQScanListener extends QScanListenerV2 {
	private ScannerActivity scannerActivity;
	private Handler mHandle, mHandle2;
	private LinkedList<String> mSb = new LinkedList<String>();
	private OnAdScanListener mOnAdScanListener;
	private int mAdCount;

	public MyQScanListener(ScannerActivity scannerActivity, Handler mHandle, Handler mHandle2) {
		this.scannerActivity = scannerActivity;
		this.mHandle = mHandle;
		this.mHandle2 = mHandle2;
	}
	
	@Override
	public void onScanStarted(int scanType) {
		mAdCount = 0;
		android.util.Log.v(ScannerActivity.TAG, "onScanStarted:[" + scanType + "]");
		updateTip("扫描开始：", -1);
		updateTip("扫描类型：" + getScanTypeString(scanType), -1);
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
		updateTip(result, progress);
	}

	/**
	 * 搜索到不扫描的文件的回调
	 */
	@Override
	public void onFoundElseFile(int scanType, File file) {

		android.util.Log.v(ScannerActivity.TAG, "onFoundElseFile:[" + scanType
				+ "]");
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
		android.util.Log.v(ScannerActivity.TAG, "onScanError--scanType["
				+ scanType + "]errCode[" + errCode + "]");

		updateTip("查杀出错，出错码：" + errCode + " " + "查杀类型-"
				+ getScanTypeString(scanType), 0);
		mHandle2.sendEmptyMessage(ScannerActivity.MSG_RESET_PAUSE);
	}

	/**
	 * 扫描被暂停时回调
	 */
	@Override
	public void onScanPaused(int scanType) {
		android.util.Log.v(ScannerActivity.TAG, "onScanPaused--scanType["
				+ scanType + "]");
		updateTip("暂停扫描：查杀类型-" + getScanTypeString(scanType), -1);
	}

	/**
	 * 扫描继续时回调
	 */
	@Override
	public void onScanContinue(int scanType) {
		android.util.Log.v(ScannerActivity.TAG, "onScanContinue--scanType["
				+ scanType + "]");

		updateTip("继续扫描：查杀类型-" + getScanTypeString(scanType), -1);
	}

	/**
	 * 扫描被取消时回调
	 */
	@Override
	public void onScanCanceled(int scanType) {
		android.util.Log.v(ScannerActivity.TAG, "onScanCanceled--scanType["
				+ scanType + "]");

		updateTip("扫描已取消：查杀类型-" + getScanTypeString(scanType), -1);
		scannerActivity.mScanThread = null;
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
		android.util.Log.v(ScannerActivity.TAG, "onScanFinished--scanType["
				+ scanType + "]results.size()[" + results.size() + "]");
		System.out.println("onScanFinished results.size()=" + results.size());
		scannerActivity.mMulwareCount = results.size();
		for (QScanResultEntity entity : results) {
			Log.v(ScannerActivity.ENG_TAG, "[onScanFinished]" + "softName["
					+ entity.softName + "]packageName[" + entity.packageName
					+ "]path[" + entity.path + "]name[" + entity.name + "]");

			Log.v(ScannerActivity.ENG_TAG, "[onScanFinished]" + "discription["
					+ entity.discription + "]url[" + entity.url);
		}

		// updateTip("扫描结束:查杀类型-" + getScanTypeString(scanType), -1);
		if (results != null) {
			new DisplayResult(mHandle, mHandle2).displayResult(results);
		}
	}

	// @Override
	// public void onSdcardScanProgress(int progress, QScanResultEntity
	// result) {
	// updateTip(result, progress);
	// }
	//
	// @Override
	// public void onCloudScan() {
	// updateTip("正在进行云查杀：", -1);
	// isScanning = true;
	// }

	// @Override
	// public void onCloudScanError(int errCode) {
	// updateTip("云查杀出错，出错码：" + errCode, -1);
	// isScanning = false;
	// }

	// 更新提示
	private void updateTip(String text, int progress) {
		if (mSb.size() == 20) {
			mSb.remove(0);
		}

		mSb.add(text + "\n");
		StringBuffer tmp = new StringBuffer();
		for (String line : mSb) {
			tmp.append(line);
		}

		Message msg = mHandle.obtainMessage();
		msg.obj = tmp.toString();
		msg.arg1 = progress;
		Log.i("andysinguan", "progress -> " + progress);
		msg.sendToTarget();
	}

	// 判断应用安全
	private String getEntityDes(QScanResultEntity result) {
		System.out.println("getEntityDes2 result.type=" + result.type);
		StringBuilder content = new StringBuilder();
		String message = result.softName;
		if (message == null || message.length() == 0) {
			message = result.path;
		}
		message = message + "[" + result.discription + "]";

		switch (result.type) {
		case QScanConstants.TYPE_OK:
			content.append(message + " 正常");
			scannerActivity.mCount++;
			break;

		case QScanConstants.TYPE_RISK:
			android.util.Log.v("demo", result.softName + " is TYPE_RISK ");
			content.append(message + "  风险");
			scannerActivity.mMulwareCount++;
			scannerActivity.mCount++;
			break;

		case QScanConstants.TYPE_VIRUS:
			android.util.Log.v("demo", result.packageName + " is TYPE_VIRUS ");
			content.append(message + " " + result.name + " 病毒");
			scannerActivity.mMulwareCount++;
			scannerActivity.mCount++;
			break;

		case QScanConstants.TYPE_SYSTEM_FLAW:
			android.util.Log.v("demo", result.packageName
					+ " is TYPE_SYSTEM_FLAW ");
			content.append(message + " " + result.name + " 系统漏洞");
			scannerActivity.mMulwareCount++;
			scannerActivity.mCount++;
			break;

		case QScanConstants.TYPE_TROJAN:
			android.util.Log.v("demo", result.packageName + " is TYPE_TROJAN ");
			content.append(message + " " + result.name + " 专杀木马");
			scannerActivity.mMulwareCount++;
			scannerActivity.mCount++;
			break;

		case QScanConstants.TYPE_UNKNOWN:
			content.append((scannerActivity.mCount + 1) + "未知_______________" + message);
			scannerActivity.mCount++;
			break;

		default:
			android.util.Log.v("demo", result.softName + " is others! ");
			content.append(message + "  未知");
			break;
		}
		//log_adinfo(result);
		addOrUpdateAdInfo(result);
		return content.append(getEntityAdvice(result)).toString();
	}
	
	public static final int TYPE_AD_BLOCK = 0x0001;
	public static final int TYPE_AD_BANNER = 0x0002;
	public static final int TYPE_AD_CHABO = 0x0004;
	//扫描广告添加到数据库
	private void addOrUpdateAdInfo(QScanResultEntity result) {
		//广告信息
		if (result.plugins != null) {
			ArrayList<QScanAdPluginEntity> plugins = result.plugins;
			if (plugins.size() > 0) {
				List<String> ipList = new LinkedList<String>();
				List<String> urlList = new LinkedList<String>();
				int typeList = 0;
				int type = 0;
				for (QScanAdPluginEntity n : plugins) {
					switch(n.type){
					case QScanConstants.TYPE_AD_BLOCK:
						type = TYPE_AD_BLOCK;
						break;
					case QScanConstants.TYPE_AD_BANNER:
						type = TYPE_AD_BANNER;
						break;
					case QScanConstants.TYPE_AD_CHABO:
						type = TYPE_AD_CHABO;
						break;
					}
					typeList = typeList|type;
					if(n.banIps!=null && n.banIps.size()>0){
						for(String ip:n.banIps){
							if(!ipList.contains(ip)){
								ipList.add(ip);
							}
						}
					}
					if(n.banUrls!=null && n.banUrls.size()>0){
						for(String url:n.banUrls){
							if(!urlList.contains(url)){
								urlList.add(url);
							}
						}
					}
				}
				ContentValues values = new ContentValues();
				setContentValues(values, ipList, urlList, result.packageName, plugins.size(), typeList);
				ContentResolver contentResolver = scannerActivity.getContentResolver();
				Cursor cursor = contentResolver.query(MulwareTable.CONTENT_URI, new String[]{MulwareTable.AD_PACKAGENAME}, MulwareTable.AD_PACKAGENAME+"=?", new String[]{result.packageName}, null);
				if(cursor.getCount()>0 && cursor.moveToFirst()){
					contentResolver.update(MulwareTable.CONTENT_URI, values, MulwareTable.AD_PACKAGENAME+"=?", new String[]{result.packageName});
				}else{
					contentResolver.insert(MulwareTable.CONTENT_URI, values);
				}				
				
				if(getOnAdScanListener() != null){
					getOnAdScanListener().onAdScan(++mAdCount);
				}
			}
		}
	}	
	
	/**
	 * 检测结果转数据库数据
	 * @param values
	 * @param ipList
	 * @param urlList
	 * @param packageName
	 * @param count
	 * @param type 
	 */
	private void setContentValues(ContentValues values, List<String> ipList, List<String> urlList, String packageName, int count, int type){
		values.put(MulwareTable.AD_PACKAGENAME, packageName);
		values.put(MulwareTable.AD_COUNT, count);
		values.put(MulwareTable.AD_BANIPS, listToString(ipList));
		values.put(MulwareTable.AD_BANURLS, listToString(urlList));
		values.put(MulwareTable.AD_TYPE, type);
	}
	
	/**
	 * 列表转字符串
	 * @param array
	 * @return
	 */
	private String listToString(List<String> array){
		StringBuilder returnString = new StringBuilder();
		if(array!=null && array.size()>0){
			for(String string:array){
				returnString.append(string).append(MulwareProvider.SPLIT);
			}
			if(returnString.length()>0){
				returnString.replace(returnString.length()-1, returnString.length(), "");
			}
		}
		return returnString.toString();
	}
	
	// ad block info
	private void log_adinfo(QScanResultEntity result) {
		//广告信息
		if (result.plugins != null) {
			ArrayList<QScanAdPluginEntity> plugins = result.plugins;
			if (plugins.size() > 0) {
				android.util.Log.v("demo", result.softName + " has ad : "
						+ plugins.size());
				int i = 1;
				for (QScanAdPluginEntity n : plugins) {
					android.util.Log.v("demo", "" + i + ". ");
					android.util.Log.v("demo", "  " + n.id + "_:" + n.type + "__:"
							+ "nonName=" + (n.name == null ? "nonName" : n.name) + ":"
							+ "nonbanIps=" + (n.banIps == null ? "nonbanIps" : n.banIps) + ":"
							+ "nonbanUrls=" + (n.banUrls == null ? "nonbanUrls" : n.banUrls));
					i++;
				}
			}
		}
	}

	// 应用安全建议
	private String getEntityAdvice(QScanResultEntity result) {
		StringBuilder content = new StringBuilder();
		content.append("[");
		switch (result.advice) {
		case QScanConstants.ADVICE_NONE:
			content.append("无建议");
			break;

		case QScanConstants.ADVICE_CLEAR:
			content.append("建议清除");
			break;

		case QScanConstants.ADVICE_UPDATE:
			content.append("建议升级");
			break;

		case QScanConstants.ADVICE_CLEAR_UPDATE:
			content.append("建议清除或升级");
			break;

		case QScanConstants.ADVICE_CHECK_PAGE:
			content.append("建议查看清除方法");
			break;

		case QScanConstants.ADVICE_CHECK_PAGE_UPDATE:
			content.append("建议查看清除方法或者升级");
			break;

		case QScanConstants.ADVICE_DOWN_TOOL:
			content.append("建议下载专杀清除");
			break;

		case QScanConstants.ADVICE_DOWN_TOOL_UPDATE:
			content.append("建议下载专杀清除或者升级");
			break;

		default:
			content.append("无建议");
			break;
		}
		content.append("]");
		return content.toString();
	}

	private void updateTip(QScanResultEntity result, int progress) {
		// //正常的不显示
		// if(result.type == QScanConstants.TYPE_OK || result.type ==
		// QScanConstants.TYPE_UNKNOWN){
		// return;
		// }
		//

		/*
		 * if (mSb.size() == 20) { mSb.remove(0); }
		 */

		mSb.add(getEntityDes(result) + "\n");
		StringBuffer tmp = new StringBuffer();
		for (String line : mSb) {
			tmp.append(line);
		}

		Message msg = mHandle.obtainMessage();
		msg.obj = tmp.toString();
		msg.arg1 = progress;
		Log.i("andysinguan", "progress -> " + progress);
		msg.sendToTarget();
	}

	private String getScanTypeString(int type) {
		switch (type) {
		case QScanConstants.SCAN_INSTALLEDPKGS:
			return "已安装软件扫描";
		case QScanConstants.SCAN_UNINSTALLEDAPKS:
			return "未安装的APK扫描";
		case QScanConstants.SCAN_CLOUD:
			return "云查杀";
		default:
			return String.valueOf(type);
		}
	}
	
	public OnAdScanListener getOnAdScanListener() {
		return mOnAdScanListener;
	}

	public void setOnAdScanListener(OnAdScanListener mOnAdScanListener) {
		this.mOnAdScanListener = mOnAdScanListener;
	}

	public static interface OnAdScanListener{
		public void onAdScan(int count);
	}
}