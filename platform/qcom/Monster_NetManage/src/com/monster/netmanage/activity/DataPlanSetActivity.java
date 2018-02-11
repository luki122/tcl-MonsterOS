package com.monster.netmanage.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.monster.netmanage.R;
import com.monster.netmanage.TrafficCorrectionWrapper;
import com.monster.netmanage.receiver.SimStateReceiver;
import com.monster.netmanage.receiver.SimStateReceiver.ISimStateChangeListener;
import com.monster.netmanage.utils.NotificationUtil;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.DataplanPreference;
import com.monster.netmanage.view.EditDataPreference;
import com.monster.netmanage.view.EditDataPreference.IEtChangeListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceGroup;
import mst.preference.SwitchPreference;
import mst.widget.TimePicker;
import mst.widget.TimePicker.OnTimeChangedListener;
import mst.widget.toolbar.Toolbar;
import tmsdk.bg.module.network.CodeName;

/**
 * 套餐流量设置
 * @author zhaolaichao
 */
public class DataPlanSetActivity extends BasePreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener{
	private static final String TAG = "DataPlanSetActivity";
	 
	private mst.widget.TimePicker mTpFreeTime;
	
	private DataplanPreference mPreProvince;
	private DataplanPreference mPreCity;
	private DataplanPreference mPreOperator;
	private DataplanPreference mPreDataplanType;
	/**
	 * 套餐流量
	 */
	private EditDataPreference mPreDataplanTotal;
	private DataplanPreference mPreCloseDay;
	private SwitchPreference mPreFreeState;
	/**
	 * 闲时套餐流量
	 */
	private EditDataPreference mPreFreeTotal;
	private DataplanPreference mPreStartTime;
	private DataplanPreference mPreEndTime;
	private PreferenceGroup mFreeGroup;
	/**
	 * 点击view的item
	 */
	private Preference mClickPre;
	/**
	 * 当前选择的sim的IMS号
	 */
	String mSelectedSimIMSI;
	private String mSelectedTime = null;
	private int mCloseDay = 1;
	/**
	 * 当前卡索引
	 */
	private int mSelectedIndex;
	/**
	 * 初始化加载
	 */
	private final static int LOAD_FIRST = 1;
	private final static int MONTH_LENGTH = 31;
	private static String[] mDays = new String[MONTH_LENGTH];
	/**
	 * 套餐设置状态
	 */
	private List<CodeName> mAllProvinces;
	private List<CodeName> mCities;
	private List<CodeName> mBrands;
	private String[] mTempArray = null;
	private String[] mAllProvincesArray = null;
	private String[] mCitysArray = null;
	private String[] mCarriesArray = null;
	private String[] mBrandsArray = null;
	private HashMap< String, String> mProvincesNameCodeMap = new HashMap<String, String>();
	private HashMap< String, String> mNameCodeMap = new HashMap<String, String>();
	
	static {
		//设置月结日
		for (int i = 0; i < MONTH_LENGTH; i++) {
			mDays[i] = "" + (i + 1) + "日";
		}
	}
     @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.activity_dataplan_set);
    	try {
    		mAllProvinces = TrafficCorrectionWrapper.getInstance().getAllProvinces();
    		Log.v(TAG, "allProvinces>>>>" + mAllProvinces.size());
    		if (mAllProvinces != null) {
    			mAllProvincesArray = new String[mAllProvinces.size()];
    			for (int i = 0; i < mAllProvinces.size(); i++) {
    				CodeName codeNameInfo = mAllProvinces.get(i);
    				mAllProvincesArray[i] = codeNameInfo.mName;
    				mProvincesNameCodeMap.put(codeNameInfo.mName, codeNameInfo.mCode);
				}
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	//初始化数据
    	initSimInfo();
    	//sim卡状态发生改变时更新UI
    	updateUIBySimChange ();
    }
   
     /**
      * 初始化数据
      */
     private void initSimInfo() {
    	 mSelectedIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);
     	if (mSelectedIndex == 0) {
 	    	//卡1
 	    	mSelectedSimIMSI = PreferenceUtil.getString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, null);
 	    } else if (mSelectedIndex == 1) {
 	    	//卡2
 	    	mSelectedSimIMSI = PreferenceUtil.getString(this, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, null);
 	    }
     	initView();
     }
     
    @SuppressWarnings("deprecation")
	private void initView() {
    	Toolbar toolbar = getToolbar();
		toolbar.setTitle(getString(R.string.dataplan_set));
		toolbar.setElevation(1);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                DataPlanSetActivity.this.finish();				
			}
		});
		int loadFirst = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.LOAD_FIRST_KEY, 0);
		if (loadFirst == 0) {
			PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.LOAD_FIRST_KEY, LOAD_FIRST);
		}
    	//省份
    	String province = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_KEY, getString(R.string.un_operator));
		mPreProvince = (DataplanPreference) findPreference("sim_province");
		mPreProvince.setItemTitle(getString(R.string.province));
		mPreProvince.setSubContent(province);
		mPreProvince.setOnPreferenceClickListener(this);
		
    	//所在城市
    	String city = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.CITY_KEY, getString(R.string.un_operator));
		mPreCity = (DataplanPreference) findPreference("sim_city");
		mPreCity.setItemTitle(getString(R.string.city));
		mPreCity.setSubContent(city);
		mPreCity.setOnPreferenceClickListener(this);
		
    	//运营商
		mPreOperator = (DataplanPreference) findPreference("sim_operator");
		mPreOperator.setItemTitle(getString(R.string.operators));
		String simOperator = ToolsUtil.getSimOperator(this, mSelectedSimIMSI);
		mPreOperator.setSubContent(simOperator);
		try {
			//初始化时用户运营商默认获得,用户可以不用选择,这时要做个匹配来获得套餐类别
			ArrayList<CodeName> carries = TrafficCorrectionWrapper.getInstance().getCarries();
			for (int i = 0; i < carries.size(); i++) {
				CodeName codeNameInfo = carries.get(i);
				if (codeNameInfo.mName.equals(simOperator)) {
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_KEY, codeNameInfo.mName);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_CODE_KEY, codeNameInfo.mCode);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	//套餐类型
    	String dataPlayType = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_KEY, getString(R.string.un_operator));
    	mPreDataplanType = (DataplanPreference) findPreference("sim_dataplan_type");
    	mPreDataplanType.setItemTitle(getString(R.string.dataplan_type));
    	mPreDataplanType.setSubContent(dataPlayType);
    	mPreDataplanType.setOnPreferenceClickListener(this);
    	
    	//套餐流量
     	int dataTotal = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
     	//取整
     	dataTotal = dataTotal - dataTotal % 1024;
    	mPreDataplanTotal = (EditDataPreference) findPreference("data_plan_total");
    	mPreDataplanTotal.setItemTitle(getString(R.string.data_total));
    	//显示单位为MB
    	String formatData = null;
    	if (dataTotal > 0) {
    		formatData = StringUtil.formatDataFlowSize(this, dataTotal);
    		if (formatData.contains(getString(R.string.gigabyte_short))) {
    			//目前运营商给的流量最大单位为GB
    			formatData = "" + (dataTotal / 1024);
    		}
    		if (formatData.contains(getString(R.string.megabyte_short))) {
    			formatData = "" + (dataTotal / 1024);
    		}
    	} else {
    		formatData = "" + 0; 
    	}
    	mPreDataplanTotal.setEtValue(formatData);
    	mPreDataplanTotal.setEtChangeListener(new IEtChangeListener() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				//套餐流量大小 ,以KB为单位来存储
				boolean match = StringUtil.matchNumber(TextUtils.isEmpty(s) ? "" : s.toString());
				if (match) {
					PreferenceUtil.putInt(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, TextUtils.isEmpty(s) ? 0 : (Integer.parseInt(s.toString())) * 1024);
				} else {
					Toast.makeText(DataPlanSetActivity.this, R.string.input_number, Toast.LENGTH_SHORT).show();
				}
				PreferenceUtil.putBoolean(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.MAN_INPUT_CORRECT_KEY, true);
			}
		});
    	
    	//月结日
    	mCloseDay = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.CLOSEDAY_KEY, 1);
    	mPreCloseDay = (DataplanPreference) findPreference("sim_close_day");
    	mPreCloseDay.setItemTitle(getString(R.string.month_end_day));
    	mPreCloseDay.setSubContent(mCloseDay + getString(R.string.date));
    	mPreCloseDay.setOnPreferenceClickListener(this);
    	
    	//闲时流量
    	mFreeGroup = (PreferenceGroup) findPreference("lay_data_free");
    	boolean freeStaus = PreferenceUtil.getBoolean(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_STATE_KEY, false);
    	mPreFreeState = (SwitchPreference) findPreference("free_data_state");
    	mPreFreeState.setChecked(freeStaus);
    	mPreFreeState.setOnPreferenceChangeListener(this);
    	
    	//闲时套餐流量
    	int freeTotal = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
    	freeTotal = freeTotal - freeTotal % 1024;
    	mPreFreeTotal = (EditDataPreference) findPreference("data_free");
    	mPreFreeTotal.setItemTitle(getString(R.string.dataplan_free));
    	//显示单位为MB
    	String freeData = null;
    	if (freeTotal > 0) {
    		freeData = StringUtil.formatDataFlowSize(this, freeTotal);
    		if (freeData.contains(getString(R.string.gigabyte_short))) {
    			freeData = "" + (freeTotal / 1024);
    		}
    		if (freeData.contains(getString(R.string.megabyte_short))) {
    			freeData = "" + (freeTotal / 1024);
    		}
    	} else {
    		freeData = "" + 0; 
    	}
    	mPreFreeTotal.setEtValue(freeData);
    	mPreFreeTotal.setEtChangeListener(new IEtChangeListener() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				//闲时套餐流量大小 以KB为单位来存储
				boolean match = StringUtil.matchNumber(TextUtils.isEmpty(s) ? "" : s.toString());
				if (match) {
					PreferenceUtil.putInt(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, TextUtils.isEmpty(s) ? 0 : (Integer.parseInt(s.toString()) * 1024));
				} else {
					Toast.makeText(DataPlanSetActivity.this, R.string.input_number, Toast.LENGTH_SHORT).show();
				}
				PreferenceUtil.putBoolean(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.MAN_INPUT_CORRECT_KEY, true);
			}
		});
    	//开始时间
    	String startTime = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_START_TIME_KEY, "00:00");
    	mPreStartTime = (DataplanPreference) findPreference("sim_start_time");
    	mPreStartTime.setItemTitle(getString(R.string.start_time));
    	mPreStartTime.setSubContent(startTime);
    	mPreStartTime.setOnPreferenceClickListener(this);
    	
    	//结束时间
    	String endTime = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_END_TIME_KEY, "00:00");
    	mPreEndTime = (DataplanPreference) findPreference("sim_end_time");
    	mPreEndTime.setItemTitle(getString(R.string.end_time));
    	mPreEndTime.setSubContent(endTime);
    	mPreEndTime.setOnPreferenceClickListener(this);
    	if (!freeStaus) {
    		mFreeGroup.removePreference(mPreFreeTotal);
    		mFreeGroup.removePreference(mPreStartTime);
    		mFreeGroup.removePreference(mPreEndTime);
    	}
     }
    
    /**
     * sim卡状态发生改变时更新UI
     */
    private void updateUIBySimChange () {
    	SimStateReceiver.setSimStateChangeListener(new ISimStateChangeListener() {
			
			@Override
			public void onSimStateChange() {
				
			}
		});
    }
    
    @Override
	public boolean onPreferenceClick(Preference preference) {
    	mClickPre = preference;
		if (preference == mPreProvince) {
			showDataType(getString(R.string.select) + getString(R.string.province), mAllProvincesArray);
		} else if (preference == mPreCity) {
			 showSelectDialog(mPreCity);
		} else if (preference == mPreDataplanType) {
			 showSelectDialog(mPreDataplanType);
		} else if (preference == mPreCloseDay) {
			showDataType(getString(R.string.select) + getString(R.string.month_end_day), mDays);
		} else if (preference == mPreStartTime) {
			changeFreeTime(mPreStartTime, getString(R.string.set) + getString(R.string.start_time), PreferenceUtil.FREE_DATA_START_TIME_KEY);
		} else if (preference == mPreEndTime) {
			changeFreeTime(mPreEndTime, getString(R.string.set) + getString(R.string.end_time), PreferenceUtil.FREE_DATA_END_TIME_KEY);
		}
		return true;
	}
    
    @Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
    	Log.v("changed", "changed>>" + newValue);
    	if (preference == mPreProvince) {
    		mPreProvince.setSubContent((String)newValue);
    	} else if (preference == mPreFreeState) {
			boolean state = (Boolean) newValue;
			PreferenceUtil.putBoolean(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_STATE_KEY, state);
			if (state) {
				mFreeGroup.addPreference(mPreFreeTotal);
				mFreeGroup.addPreference(mPreStartTime);
				mFreeGroup.addPreference(mPreEndTime);
			} else {
		    	mFreeGroup.removePreference(mPreFreeTotal);
		    	mFreeGroup.removePreference(mPreStartTime);
		    	mFreeGroup.removePreference(mPreEndTime);
			}
		}
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveDataPlanSet();
	}
	
	private void showSelectDialog(Preference preference) {
		if (preference == mPreCity) {
			try {
		  		String code = PreferenceUtil.getString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_CODE_KEY, null);
	    		if (TextUtils.isEmpty(code)) {
	    			return ;
	    		}
	    		mNameCodeMap.clear();
	    		if (null == mCities || mCities.size() == 0) {
	    			mCities = TrafficCorrectionWrapper.getInstance().getCities(code);
	    		}
	    		mCitysArray = new String[mCities.size()];
	    		for (int i = 0; i < mCities.size(); i++) {
	    			CodeName codeNameInfo = mCities.get(i);
	    			mCitysArray[i] = codeNameInfo.mName;
	    			mNameCodeMap.put(codeNameInfo.mName, codeNameInfo.mCode);
	    		}
	    		showDataType(getString(R.string.select) + getString(R.string.city), mCitysArray);
			} catch (Exception e) {
				e.printStackTrace();
			}
	  } /*else if (preference == mPreOperator) {
		  mNameCodeMap.clear();
	    	try {
	    		//获得运营商类别
	    		List<CodeName> carries = TrafficCorrectionWrapper.getInstance().getCarries();
	    		mCarriesArray = new String[carries.size()];
	    		for (int i = 0; i < carries.size(); i++) {
	    			CodeName codeNameInfo = carries.get(i);
	    			mCarriesArray[i] = codeNameInfo.mName;
	    			mNameCodeMap.put(codeNameInfo.mName, codeNameInfo.mCode);
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
			showDataType(getString(R.string.select) + getString(R.string.operators), mCarriesArray);
	  }*/ else if (preference == mPreDataplanType) {
		  mNameCodeMap.clear();
	    	try {
				//获得套餐类别
	    		if (null == mBrands) {
					String code = PreferenceUtil.getString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_CODE_KEY, null);
					mBrands = TrafficCorrectionWrapper.getInstance().getBrands(code);
	    		}
	    		mBrandsArray = new String[mBrands.size()];
	    		for (int i = 0; i < mBrands.size(); i++) {
	    			CodeName codeNameInfo = mBrands.get(i);
	    			mBrandsArray[i] = codeNameInfo.mName;
	    			mNameCodeMap.put(codeNameInfo.mName, codeNameInfo.mCode);
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
			showDataType(getString(R.string.select) + getString(R.string.dataplan_type), mBrandsArray);
	  } 
  }
	
	/**
	 * 显示数据类型
	 */
	private void showDataType(String title, String[] valuesArray) {
        mTempArray = valuesArray;
		mst.app.dialog.AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setCancelable(true);
		builder.setItems(valuesArray, new mst.app.dialog.AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = mTempArray[which];
				String code = null;
				if (mClickPre == mPreProvince) {
					code = mProvincesNameCodeMap.get(value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_KEY, value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_CODE_KEY, code);
					mPreProvince.setSubContent(value);
					mCities = TrafficCorrectionWrapper.getInstance().getCities(code);
					mPreCity.setSubContent(mCities.get(0).mName);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.CITY_KEY, mCities.get(0).mName);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.CITY_CODE_KEY, mCities.get(0).mCode);
					mClickPre = mPreCity;
					showSelectDialog(mPreCity);
				} else if (mClickPre == mPreCity) {
					code = mNameCodeMap.get(value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.CITY_KEY, value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.CITY_CODE_KEY, code);
					mPreCity.setSubContent(value);
					mClickPre = mPreDataplanType;
					showSelectDialog(mPreDataplanType);
				} /*else if (mClickPre == mPreOperator) {
					code = mNameCodeMap.get(value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_KEY, value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_CODE_KEY, code);
					mPreOperator.setSubContent(value);
					mBrands = TrafficCorrectionWrapper.getInstance().getBrands(code);
					mPreDataplanType.setSubContent(mBrands.get(0).mName);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_KEY, mBrands.get(0).mName);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_CODE_KEY, mBrands.get(0).mCode);
					mClickPre = mPreDataplanType;
					showSelectDialog(mPreDataplanType);
				} */else if (mClickPre == mPreDataplanType) {
					code = mNameCodeMap.get(value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_KEY, value);
					PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_CODE_KEY, code);
					mPreDataplanType.setSubContent(value);
				} else if (mClickPre == mPreCloseDay) {
					PreferenceUtil.putInt(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.CLOSEDAY_KEY, Integer.parseInt(mDays[which].replace(getString(R.string.date ), "")));
					mPreCloseDay.setSubContent(mDays[which]);
				}
				dialog.dismiss();
			}
		});
		AlertDialog create = builder.create();
		create.setCanceledOnTouchOutside(true);
		create.show();
	 }
	
	/**
	 * 闲时时间点
	 * @param tv
	 * @param timeKey
	 */
	private void changeFreeTime(final DataplanPreference preference, String title, final String timeKey) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		View view = layoutInflater.inflate(R.layout.data_type, null);  
		mTpFreeTime = (mst.widget.TimePicker) view.findViewById(R.id.tp_type);  
		mTpFreeTime.setVisibility(View.VISIBLE);
		mTpFreeTime.setIs24HourView(true);
		String time = PreferenceUtil.getString(DataPlanSetActivity.this, mSelectedSimIMSI, timeKey, "00:00");
		if (TextUtils.isEmpty(time)) {
			time = "00:00";
		}
		String[] split = time.split(":");
		mSelectedTime = split[0] + ":" + split[1];
		mTpFreeTime.setHour(Integer.parseInt(split[0]));
		mTpFreeTime.setMinute(Integer.parseInt(split[1]));
		mTpFreeTime.setOnTimeChangedListener(new OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker timePick, int hourOfDay, int minute) {
				String time = (hourOfDay < 10 ?  "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ?  "0" + minute : minute);
				mSelectedTime = time;
			}
		});
		mst.app.dialog.AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setView(view);
		builder.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				preference.setSubContent(mSelectedTime);
				PreferenceUtil.putString(DataPlanSetActivity.this, mSelectedSimIMSI, timeKey, mSelectedTime);
			}
		});
		AlertDialog create = builder.create();
		create.setCanceledOnTouchOutside(true);
		create.show();
	}
	
	private boolean isSetDataPlan() {
    	String province = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_KEY, getString(R.string.un_operator));
    	String city = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.CITY_KEY, getString(R.string.un_operator));
    	String dataPlanType = PreferenceUtil.getString(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_KEY, getString(R.string.un_operator));

		int dataTotal = PreferenceUtil.getInt(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
		//sim卡基本信息设置
		if (province.equals(getString(R.string.un_operator)) || city.equals(getString(R.string.un_operator))
				||dataPlanType. equals(getString(R.string.un_operator))) {
			return false;
		}
		//套餐流量设置
		if (dataTotal < 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 保存sim卡套餐设置
	 */
	private void saveDataPlanSet() {
		int commonTotal = PreferenceUtil.getInt(DataPlanSetActivity.this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		String activeSimImsi = ToolsUtil.getActiveSimImsi(this);
        if (commonTotal > 0 && mSelectedSimIMSI.equals(activeSimImsi)) {
        	NotificationUtil.clearNotify(this, NotificationUtil.TYPE_BIGTEXT);
        }
		//省份
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.PROVINCE_KEY, mPreProvince.getSubContent().toString());
		//市
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.CITY_KEY, mPreCity.getSubContent().toString());
		//运营商
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.OPERATOR_KEY, mPreOperator.getSubContent().toString());
		//套餐类型
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_TYPE_KEY, mPreDataplanType.getSubContent().toString());
		//月结日
		String closeDay = mPreCloseDay.getSubContent().toString();
		Log.v(TAG, "closeDay>>" + closeDay);
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.CLOSEDAY_KEY, Integer.parseInt(closeDay.replace(getString(R.string.date ), "")));
		//开始时间
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_START_TIME_KEY, mPreStartTime.getSubContent().toString());
		//结束时间
		PreferenceUtil.putString(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_END_TIME_KEY, mPreEndTime.getSubContent().toString());
		//sim基本信息设置完整检查 
		PreferenceUtil.putBoolean(this, mSelectedSimIMSI, PreferenceUtil.SIM_BASEINFO_KEY, isSetDataPlan());
		//未设置流量套餐时,提示最多弹出3次
		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.NOTIFY_UNDATA_COUNT_KEY, 0);
	}

}
