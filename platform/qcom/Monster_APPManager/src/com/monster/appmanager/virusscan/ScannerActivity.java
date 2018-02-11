package com.monster.appmanager.virusscan;

import java.util.ArrayList;

import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.monster.appmanager.FullActivityBase;
import com.monster.appmanager.R;
import com.monster.appmanager.virusscan.MyQScanListener.OnAdScanListener;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.qscanner.QScannerManagerV2;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class ScannerActivity extends FullActivityBase implements Callbacks {
	public boolean isMainActivity = false;
	public static final String TAG = "ScannerActivity";
	public static final String HAS_SCAN = "has_scan";
	static final String ENG_TAG = "englishTag";

	private ProgressBar mProgressbar;// 扫描进度条
	private TextView mProgressbarText;
	private ScrollView scrollLayout;
	private TextView mContentShower;// 显示扫描动态与结果建议
	private TextView mScanResultStateView = null;// 扫描结果
	private TextView appCount;

	private ApplicationsState mState;
	private ApplicationsState.Session mSession;

	private QScannerManagerV2 mQScannerMananger;// 病毒扫描功能接口
	public Thread mScanThread;// 扫描线程对象

	public ArrayList<ApplicationsState.AppEntry> entries;
	public ArrayList<String> packagesName = new ArrayList<>();
	int mMulwareCount = 0;// 病毒数
	int mAppCount = 0;// 软件数
	public int mCount = 0;// 扫描软件数
	long mTimeValue = 0;// 扫描开始时间
	
	private OnAdScanListener mOnAdScanListener;

	// 计时器
	void countTime() {
		if (mTimeValue == 0) {
			mMulwareCount = 0;
			mTimeValue = System.currentTimeMillis();
		} else {
			long end = System.currentTimeMillis();

			Message msg = mHandle2.obtainMessage();
			String msgValue = "用时：" + String.valueOf(end - mTimeValue)
					+ "毫秒 扫描软件:" + mCount + "个 病毒：" + mMulwareCount + "个";
			msg.obj = msgValue.toString();
			msg.sendToTarget();

			mTimeValue = 0;
		}
	}

	public static final int MSG_ENABLE_ALLBTN = 101;
	public static final int MSG_RESET_PAUSE = 102;
	private Handler mHandle2 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				onLoadFinish(msg);
				break;
			}
		}
	};

	public void onLoadFinish(Message msg) {
		if (!isMainActivity) {
			mScanResultStateView.setText((String) msg.obj);
		}
		SharedPreferences mySharedPreferences = getSharedPreferences(
				ScannerActivity.TAG, Activity.MODE_PRIVATE);
		// 实例化SharedPreferences.Editor对象（第二步）
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putBoolean(ScannerActivity.HAS_SCAN, true);
		// 提交当前数据
		editor.commit();
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}

	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			onScaning(msg);
		}
	};

	public void onScaning(Message msg) {
		if (!isMainActivity) {
			mContentShower.setText("");
			mContentShower.setText((String) msg.obj);
			// 滚动到底部
			int offset = mContentShower.getMeasuredHeight() - scrollLayout.getHeight();
			if (offset < 0) {
				offset = 0;
			}
			scrollLayout.scrollTo(0, offset);

			if (msg.arg1 > 0) {
				mProgressbar.setProgress(msg.arg1);
				mProgressbarText.setText(mCount + "/" + mAppCount);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isMainActivity) {
			setContentView(R.layout.qscanner_activity);
			mProgressbar = (ProgressBar) findViewById(R.id.progress_bar);
			mProgressbarText = (TextView) findViewById(R.id.progress_bar_text);
			mContentShower = (TextView) findViewById(R.id.content_shower);
			mScanResultStateView = (TextView) findViewById(R.id.ResultStateText);
			appCount = (TextView) findViewById(R.id.app_count);
			scrollLayout = (ScrollView) findViewById(R.id.scroll_layout);
		}

		mQScannerMananger = ManagerCreatorF.getManager(QScannerManagerV2.class);

		if (mQScannerMananger.initScanner() == 0) {
			Log.v("demo", "initScanner return true");
		} else {
			Log.v("demo", "initScanner return false");
		}

		mState = ApplicationsState.getInstance(getApplication());
		mSession = mState.newSession(this);
		mSession.resume();
	}

	// 进程销毁
	@Override
	protected void onDestroy() {
		// 这里不要忘记了
		mQScannerMananger.freeScanner();
		mSession.release();
		super.onDestroy();
	}

	public void startScan() {
		if (mScanThread == null || !mScanThread.isAlive()) {
			mScanThread = new Thread() {
				@Override
				public void run() {
					countTime();
					MyQScanListener listener = new MyQScanListener(ScannerActivity.this, mHandle, mHandle2);
					listener.setOnAdScanListener(getOnAdScanListener());
					mQScannerMananger.scanSelectedPackages(packagesName, listener, false);
					countTime();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mHandle2.sendEmptyMessage(MSG_ENABLE_ALLBTN);
				}
			};
			mScanThread.start();
		}
	}

	@Override
	public void onAllSizesComputed() {
	}

	@Override
	public void onLauncherInfoChanged() {
	}

	@Override
	public void onLoadEntriesCompleted() {
		entries = mSession.rebuild(
				ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER,
				ApplicationsState.SIZE_COMPARATOR);
		mAppCount = entries.size();
		packagesName.clear();
		for (ApplicationsState.AppEntry appEntry : entries) {
			packagesName.add(appEntry.info.packageName);
		}
		if (packagesName != null && packagesName.size() > 0 && !isMainActivity) {
			startScan();
		}
		if (!isMainActivity) {
			appCount.setText(getResources().getString(R.string.app_count,
					mAppCount));
			mProgressbar.setMax(mAppCount);
		}
		mSession.pause();
	}

	@Override
	public void onPackageIconChanged() {
	}

	@Override
	public void onPackageListChanged() {
	}

	@Override
	public void onPackageSizeChanged(String arg0) {
	}

	@Override
	public void onRebuildComplete(ArrayList<AppEntry> arg0) {
	}

	@Override
	public void onRunningStateChanged(boolean arg0) {
	}

	@Override
	protected void onPause() {
		super.onPause();
//		mSession.pause();
	}

	protected OnAdScanListener getOnAdScanListener() {
		return mOnAdScanListener;
	}

	protected void setOnAdScanListener(OnAdScanListener mOnAdScanListener) {
		this.mOnAdScanListener = mOnAdScanListener;
	}
	
	protected void cancelScan() {
		try {
			if(mQScannerMananger != null) {
				mQScannerMananger.cancelScan();
			}
		} catch (Exception e) {
		}
	}
}
