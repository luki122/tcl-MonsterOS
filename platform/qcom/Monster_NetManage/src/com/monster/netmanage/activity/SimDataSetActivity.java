package com.monster.netmanage.activity;

import com.monster.netmanage.R;
import com.monster.netmanage.receiver.SimStateReceiver;
import com.monster.netmanage.receiver.SimStateReceiver.ISimStateChangeListener;
import com.monster.netmanage.service.NetManagerService;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.view.WarnDataPreference;
import com.monster.netmanage.view.WarnDataPreference.ISeekBarChangeListener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import mst.app.dialog.AlertDialog.Builder;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceGroup;
import mst.preference.PreferenceScreen;
import mst.preference.SwitchPreference;
import mst.widget.toolbar.Toolbar;

/**
 * SIM卡设置
 * @author zhaolaichao
 */
public class SimDataSetActivity extends BasePreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	/**
	 * 默认显示总量的比例
	 */
	private static int DEFAULT_RATE = 85;
	/**
	 * 默认显示最少总量的比例
	 */
	private static int DEFAULT_MIN_RATE = 50;
	private Preference mLayDataPlan; 
	private SwitchPreference mLayPassWarning;
	private PreferenceGroup mLayWarnGroup;
	private WarnDataPreference mLayWarnValue;
	private PreferenceScreen mLayOrientApp;
	private SwitchPreference mLayAutoCorrect;
	private PreferenceScreen mLayManCorrect;
	private PreferenceScreen mLayDataClean;
	String mSimTitle;
	/**
	 * 当前选择的sim的IMS号
	 */
	String mSelectedSimIMSI;
	private String mDefaultWarnValue;
	private int mTotalData;
	private AlarmManager mAlarm;
	private Intent mAlarmIntent;
	private int mSelectedIndex;
	private int mWarnMaxProgress;
	private int mCurrentRate;
	
     @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.activity_sim_set);
    	Intent intent = getIntent();
	    mSimTitle = intent.getStringExtra("SIM_TITLE");
	    initSimInfo();
    	initView();
    	// sim卡状态发生改变时更新UI
    	updateUIBySimChange ();
    }
    
     /**
      * 初始化数据
      */
     private void initSimInfo() {
    	 mSelectedIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);
  	    if (mSelectedIndex == 0) {
  	    	//卡1
  	    	mSelectedSimIMSI = PreferenceUtil.getString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, "");
  	    } else if (mSelectedIndex == 1) {
  	    	//卡2
  	    	mSelectedSimIMSI = PreferenceUtil.getString(this, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, "");
  	    }
     }
     
    @SuppressWarnings("deprecation")
	private void initView() {
		 Toolbar toolbar = getToolbar();
		 toolbar.setTitle(mSimTitle);
		 toolbar.setElevation(1);
		 toolbar.setNavigationOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SimDataSetActivity.this.finish();
			}
		});
    	//套餐设置
		 mLayDataPlan = findPreference("data_plan_set");
		 mLayDataPlan.setOnPreferenceClickListener(this);
    	
    	//超额预警
    	boolean warnState = PreferenceUtil.getBoolean(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_STATE_KEY, true);
    	mLayPassWarning = (SwitchPreference) findPreference("data_plan_warn");
    	mLayPassWarning.setChecked(warnState);
    	mLayPassWarning.setOnPreferenceChangeListener(this);
		
    	//超额预警值
    	mLayWarnGroup = ((PreferenceGroup) findPreference("lay_warn"));  
    	mLayWarnValue = (WarnDataPreference) findPreference("data_plan_warn_value");
    	mDefaultWarnValue = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, DEFAULT_RATE + "%");
    	mTotalData = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
    	//转为MB
    	mTotalData = mTotalData /1024;
    	mCurrentRate = Integer.parseInt(mDefaultWarnValue.substring(0, mDefaultWarnValue.indexOf("%")));
    	if (mTotalData > 0) {
    		mWarnMaxProgress = (mTotalData * mCurrentRate) /100;
    		mDefaultWarnValue = mCurrentRate + "% (" + mWarnMaxProgress + getString(R.string.megabyte_short)  + ")";
    	} else {
    		mLayWarnValue.setEnabled(false);
    	}
    	PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, mDefaultWarnValue);
    	//最左端为50%,最右端为100%
//    	mLayWarnValue.setMaxProgress(mTotalData == 0 ? 50 : mTotalData);
//    	mLayWarnValue.setProgress(mWarnMaxProgress == 0 ? (DEFAULT_RATE - 50): mWarnMaxProgress);
    	mLayWarnValue.setMaxProgress(50);
    	mLayWarnValue.setProgress(mCurrentRate == 0 ? (DEFAULT_RATE - 50) : mCurrentRate - 50);
    	mLayWarnValue.setWarnValue(mDefaultWarnValue);
    	if(!warnState) {
    		mLayWarnGroup.removePreference(mLayWarnValue);
    	} 
    	mLayWarnValue.setSeekBarChangeListener(new ISeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//超额预警值
				PreferenceUtil.putString(SimDataSetActivity.this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, mLayWarnValue.getWarnValue());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.v("mSb.getProgress()", "mSb.getProgress()>>" + progress);
				mCurrentRate = progress + DEFAULT_MIN_RATE;
				int value  = mCurrentRate * mTotalData / 100;
				mDefaultWarnValue = mCurrentRate + "% (" + value + getString(R.string.megabyte_short)  + ")";
				mLayWarnValue.setWarnValue(mDefaultWarnValue);
			}
		});
    	//定向流量应用
    	mLayOrientApp = (PreferenceScreen) findPreference("orient_app");
    	mLayOrientApp.setOnPreferenceClickListener(this);
    	
    	//自动校正流量
    	mLayAutoCorrect = (SwitchPreference) findPreference("data_auto");
    	boolean correctState = PreferenceUtil.getBoolean(this, mSelectedSimIMSI, PreferenceUtil.AUTO_CORRECT_STATE_KEY, true);
    	mLayAutoCorrect.setChecked(correctState);
    	mLayAutoCorrect.setOnPreferenceChangeListener(this);
    	boolean correct_repeat = PreferenceUtil.getBoolean(this, mSelectedSimIMSI, PreferenceUtil.AUTO_CORRECT_REPEAT_KEY, false);
    	//sim基本信息设置完整检查 
        boolean simInfoState = PreferenceUtil.getBoolean(this, mSelectedSimIMSI, PreferenceUtil.SIM_BASEINFO_KEY, false);
    	if (!correct_repeat && simInfoState) {
    		setAlarm(correctState);
    	}
		
    	//手动校正流量
    	mLayManCorrect = (PreferenceScreen) findPreference("data_plan_man_correct");
    	mLayManCorrect.setOnPreferenceClickListener(this);
    	
    	//清空流量数据
    	mLayDataClean = (PreferenceScreen) findPreference("data_clean");
    	mLayDataClean.setOnPreferenceClickListener(this);
     }
    
    /**
     * sim卡状态发生改变时更新UI
     */
    private void updateUIBySimChange () {
    	SimStateReceiver.setSimStateChangeListener(new ISimStateChangeListener() {

			@Override
			public void onSimStateChange(int simState) {
				if (simState == SimStateReceiver.SIM_INVALID) {
					mLayDataPlan.setEnabled(false);
					mLayAutoCorrect.setChecked(false);
					mLayManCorrect.setEnabled(false);
					mLayWarnValue.setEnabled(false);
				}
			}
		});
    }
    
    @Override
	public boolean onPreferenceClick(Preference preference) {
    	Intent intent = null;
		if (preference == mLayDataPlan) {
			intent = new Intent(SimDataSetActivity.this, DataPlanSetActivity.class);
			intent.putExtra("CURRENT_INDEX", mSelectedIndex);
			startActivityForResult(intent, 1000);
		} else if (preference == mLayOrientApp) {
			intent = new Intent(SimDataSetActivity.this, OrientAppActivity.class);
			intent.putExtra("CURRENT_INDEX", mSelectedIndex);
			startActivity(intent);
		} else if (preference == mLayManCorrect) {
			intent = new Intent(SimDataSetActivity.this, DataManCorrectActivity.class);
			intent.putExtra("CURRENT_INDEX", mSelectedIndex);
			startActivity(intent);
		} else if (preference == mLayDataClean) {
			showCleanDialog();
		}
		return false;
	}
    
    @Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
    	Log.v("onPreferenceChange", "newValue>>" + newValue);
		if (preference == mLayPassWarning) {
			boolean state = (Boolean) newValue;
			if (state) {
				mLayWarnGroup.addPreference(mLayWarnValue);
				mLayWarnValue.setMaxProgress(DEFAULT_MIN_RATE);
		    	mLayWarnValue.setProgress(mCurrentRate - DEFAULT_MIN_RATE);
		    	mLayWarnValue.setWarnValue(mDefaultWarnValue);
			} else {
				mLayWarnGroup.removePreference(mLayWarnValue);
			}
			//超额预警开关
			PreferenceUtil.putBoolean(SimDataSetActivity.this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_STATE_KEY, state);
		} else if (preference == mLayWarnValue) {
			mLayWarnValue.setWarnValue(mDefaultWarnValue);
        } else if (preference == mLayAutoCorrect) {
			boolean state = (Boolean) newValue;
			//自动校正流量开关
			PreferenceUtil.putBoolean(SimDataSetActivity.this, mSelectedSimIMSI, PreferenceUtil.AUTO_CORRECT_STATE_KEY, state);
			//设置闹钟进行定时任务
			setAlarm(state);
		}
		return true;
	}

    @Override
	 protected void onRestart() {
		super.onRestart();
		mTotalData = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		//转为MB
		mTotalData = mTotalData /1024;
		if (mTotalData > 0) {
			mLayWarnValue.setEnabled(true);
			mDefaultWarnValue = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, DEFAULT_RATE + "%");
		    if (mDefaultWarnValue.contains("%")) {
		    	int rate = Integer.parseInt(mDefaultWarnValue.substring(0, mDefaultWarnValue.indexOf("%")));
		    	//显示MB
		    	mWarnMaxProgress = (mTotalData * rate) /100;
		    	mLayWarnValue.setMaxProgress(DEFAULT_MIN_RATE);
		    	mLayWarnValue.setProgress(rate - DEFAULT_MIN_RATE);
		    	mDefaultWarnValue = rate + "%(" + mWarnMaxProgress +  getString(R.string.megabyte_short) + ")";
		    	mLayWarnValue.setWarnValue(mDefaultWarnValue);
		    	PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, mDefaultWarnValue);
		    }
		} else {
			mLayWarnValue.setEnabled(false);
			mDefaultWarnValue = DEFAULT_RATE + "%";
			mLayWarnValue.setWarnValue(mDefaultWarnValue);
	    	PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, mDefaultWarnValue);
		}
	}
    
	/**
	 * 是否每天定时发送验证短信
	 * @param isCorrect
	 */
	private void setAlarm(boolean isCorrect) {
		mAlarmIntent  = new Intent(SimDataSetActivity.this, NetManagerService.class);
		mAlarmIntent.putExtra("CURRENT_IMSI", mSelectedSimIMSI );     
		PendingIntent pi = PendingIntent.getService(SimDataSetActivity.this, 0, mAlarmIntent, 0);
		 mAlarm = (AlarmManager)SimDataSetActivity.this.getSystemService(Context.ALARM_SERVICE);
		if (isCorrect) {
			//从现在起第三天相同时间开始
			long repeatTime = 3 * 24 * 3600 * 1000;
			mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), repeatTime,  pi);   
			PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.AUTO_CORRECT_REPEAT_KEY, true);
		} else {
			mAlarm.cancel(pi);
			PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.AUTO_CORRECT_REPEAT_KEY, false);
		}
	}
	
	
	/**
	 * 清除缓存数据
	 */
	private void showCleanDialog() {
		mst.app.dialog.AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(getString(R.string.data_clean));
		builder.setMessage(getString(R.string.data_clean_info));
		builder.setPositiveButton(com.mst.R.string.ok, new mst.app.dialog.AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//提示用户
				cleanDataAll();
			}
		});
		builder.setNegativeButton(com.mst.R.string.cancel, new mst.app.dialog.AlertDialog.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		builder.create().show();
	}
	
	/**
	 * 清空所有相关套餐设置数据
	 */
	private void cleanDataAll() {
		mLayWarnValue.setMaxProgress(DEFAULT_MIN_RATE);
    	mLayWarnValue.setProgress(DEFAULT_RATE - DEFAULT_MIN_RATE);
    	mLayWarnValue.setWarnValue(DEFAULT_RATE + "%");
    	mLayWarnValue.setEnabled(false);
		//超额预警开关
		PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_STATE_KEY, true);
		//超额预警值
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PASS_WARNING_VALUE_KEY, null);
		//自动校正流量开关
		PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.AUTO_CORRECT_STATE_KEY, true);
		//省份
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_KEY, null);
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_CODE_KEY, null);
		//市
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.CITY_KEY, null);
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.CITY_CODE_KEY, null);
		//运营商
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_KEY, null);
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_CODE_KEY, null);
		//套餐类型
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_KEY, null);
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_CODE_KEY, null);
		//套餐流量大小
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		//月结日
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.CLOSEDAY_KEY, 1);
		//闲时流量开关状态
		PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_STATE_KEY, true);
		//闲时套餐流量
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
		//开始时间
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_START_TIME_KEY, "23:00");
		//结束时间
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_END_TIME_KEY, "8:00");
		//已用套餐流量
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
		//已用闲时流量
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, 0);
		//常规-剩余
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
		//闲时-剩余
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
		//记录日用流量设置
		PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.NOTIFY_WARN_DAY_KEY, false);
		//清除当前上网卡的imsi
		PreferenceUtil.putString(this, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, null);
		//sim基本信息设置完整检查 
		PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.SIM_BASEINFO_KEY, false);
		//未设置流量套餐时,提示最多弹出3次
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.NOTIFY_UNDATA_COUNT_KEY, 0);
		//第一次加载
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.LOAD_FIRST_KEY, 0);
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_CORRECT_FIRST_KEY, 0);
		PreferenceUtil.putString(this, "", PreferenceUtil.TOP_APP_NAME_KEY, null);
		PreferenceUtil.putBoolean(this, "", PreferenceUtil.DATE_CHANGE_KEY, false);
		PreferenceUtil.putLong(this, mSelectedSimIMSI, PreferenceUtil.SIM_SUBID_SMS_KEY, System.currentTimeMillis());
		//每分钟已用数据
		PreferenceUtil.putLong(this, mSelectedSimIMSI, PreferenceUtil.MINUTE_DATA_USED_KEY, 0);
   	    PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.SMS_BODY_KEY, null);
   	    //每分钟提示框标志
   	    PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.MINUTE_DATA_USED_DIALOG_KEY, false);
   	    //清除日使用流量
   	    PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.DAY_USED_STATS_KEY, 0);
		setAlarm(false);
		stopService(mAlarmIntent);
	}
	
}
