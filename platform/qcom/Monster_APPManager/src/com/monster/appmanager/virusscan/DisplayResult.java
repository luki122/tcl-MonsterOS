package com.monster.appmanager.virusscan;

import java.util.LinkedList;
import java.util.List;

import android.os.Handler;
import android.os.Message;

import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;

public class DisplayResult {
	private int mCount;
	private int mMulwareCount;
	private Handler mHandle,mHandle2;
	public DisplayResult(Handler mHandle, Handler mHandle2) {
		this.mHandle = mHandle;
		this.mHandle2 = mHandle2;
	}
	public void displayResult(List<QScanResultEntity> results) {
		LinkedList<String> mSb = new LinkedList<String>();
		mCount = 0;
		mMulwareCount = 0;
		String des = null;
		mSb.add("Results:\n");
		for(QScanResultEntity re : results) {
			des = getEntityDes(re);
			if(des != null) {
				mSb.add(des+"\n");
			}
		}
		
		Message msg = mHandle.obtainMessage();
		msg.obj = mSb.toString();
		msg.arg1 = 100;
		//msg.sendToTarget();
		
		Message msg1 = mHandle2.obtainMessage();
		String msgValue = "扫描软件:" + mCount + "个 病毒：" + mMulwareCount + "个";
		msg1.obj = msgValue.toString();
		msg1.sendToTarget();
		
	}
	private String getEntityDes(QScanResultEntity result) {
		System.out.println("getEntityDes1 result.type="+result.type);
		StringBuilder content = new StringBuilder();
		content.append("应用程序扫描：");
		String message = result.softName + "##" + result.discription;
		if (message == null || message.length() == 0) {
			message = result.path;
		}
		boolean isNormal = false;
		switch (result.type) {
		case QScanConstants.TYPE_OK:
			content.append(message + " 正常");
			mCount++;
			isNormal = true;
			break;

		case QScanConstants.TYPE_RISK:
			android.util.Log.v("demo", result.softName + " is TYPE_RISK ");
			content.append(message + "  风险");
			mMulwareCount++;
			mCount++;
			break;

		case QScanConstants.TYPE_VIRUS:
			android.util.Log.v("demo", result.packageName + " is TYPE_VIRUS ");
			content.append(message + " " + result.name + " 病毒");
			mMulwareCount++;
			mCount++;
			break;

		case QScanConstants.TYPE_SYSTEM_FLAW:
			android.util.Log.v("demo", result.packageName + " is TYPE_SYSTEM_FLAW ");
			content.append(message + " " + result.name + " 系统漏洞");
			mMulwareCount++;
			mCount++;
			break;

		case QScanConstants.TYPE_TROJAN:
			android.util.Log.v("demo", result.packageName + " is TYPE_TROJAN ");
			content.append(message + " " + result.name + " 专杀木马");
			mMulwareCount++;
			mCount++;
			break;

		case QScanConstants.TYPE_NOT_OFFICIAL:
			android.util.Log.v("demo", result.packageName + " is TYPE_NOT_OFFICIAL ");
			content.append(message + " " + result.name + " 非官方证书");
			mMulwareCount++;
			mCount++;
			break;
			
		case QScanConstants.TYPE_RISK_PAY:
			android.util.Log.v("demo", result.packageName + " is TYPE_RISK_PAY ");
			content.append(message + " " + result.name + " 支付风险");
			mMulwareCount++;
			mCount++;
			break;
			
		case QScanConstants.TYPE_RISK_STEALACCOUNT:
			android.util.Log.v("demo", result.packageName + " is TYPE_RISK_STEALACCOUNT ");
			content.append(message + " " + result.name + " 账号风险");
			mMulwareCount++;
			mCount++;
			break;
			
		case QScanConstants.TYPE_UNKNOWN:
			content.append(message + "  未知");
			mCount++;
			isNormal = true;
			break;

		default:
			android.util.Log.v("demo", result.softName + " is others! ");
			content.append(message + "  未知");
			mCount++;
			break;
		}
		if(isNormal) {
			return null;
		}
		return content.append(getEntityAdvice(result)).toString();
	}
	
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
}
