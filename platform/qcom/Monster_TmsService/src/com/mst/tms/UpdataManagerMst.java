package com.mst.tms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateManager;

public class UpdataManagerMst {

	public static final String TAG = "UpdataManagerMst";

	private static UpdateManager mUpdateManager;

	private static CheckResult mCheckResults;

	public static synchronized UpdateManager getInstance() {
		if (mUpdateManager == null) {
			mUpdateManager = ManagerCreatorC.getManager(UpdateManager.class);
		}
		return mUpdateManager;
	}
	
	public static void updateDatabaseIfNeed() {
		Log.d(TAG, "updateDatabaseIfNeed");
		check();
	}

	private static void check() {

		long flags = UpdateConfig.UPDATA_FLAG_NUM_MARK// 号码标记模块
				// | UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG//病毒扫描模块
				// | UpdateConfig.UPDATE_FLAG_ADB_DES_LIST//病毒扫描模块
				// | UpdateConfig.UPDATE_FLAG_VIRUS_BASE//病毒扫描模块
				// | UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST//病毒扫描模块
				// | UpdateConfig.UPDATE_FLAG_PAY_LIST//病毒扫描模块
				// | UpdateConfig.UPDATE_FLAG_TRAFFIC_MONITOR_CONFIG//流量监控
				| UpdateConfig.UPDATE_FLAG_LOCATION// 归属地模块
		// | UpdateConfig.UPDATE_FLAG_PROCESSMANAGER_WHITE_LIST// 瘦身大文件模块
		// | UpdateConfig.UPDATE_FLAG_WeixinTrashCleanNew//瘦身微信
		 | UpdateConfig.UPDATE_FLAG_POSEIDONV2//智能拦截
		;
		getInstance().check(flags, new ICheckListener() {
			@Override
			// 检查网络，如果网络失败则回调
			public void onCheckEvent(int arg0) {
				// 检查网络状态，如果网络失败则不能更新
				Log.d(TAG, "onCheckEvent arg0 = " + arg0);
				setResult(false);
			}

			@Override
			public void onCheckStarted() {

			}

			@Override
			public void onCheckCanceled() {

			}

			@Override
			public void onCheckFinished(CheckResult result) {
				mCheckResults = result;
				setResult(true);
				update();
			}
		});

	}

	private static void update() {
		Log.d(TAG, "update");
		if (mCheckResults != null && mCheckResults.mUpdateInfoList != null
				&& mCheckResults.mUpdateInfoList.size() > 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (null == mCheckResults)
						return;
					Log.d(TAG, "update finally");
					getInstance().update(mCheckResults.mUpdateInfoList, null);
				}
			}).start();
		}
	}
	
	private static void setResult(boolean value) {
        SharedPreferences prefs = TmsApp.getInstance().getSharedPreferences("updatedatabase", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("update_result", value);
        editor.commit();    
	}

}
