package com.monster.netmanage.activity;

import com.monster.netmanage.DataCorrect;
import com.monster.netmanage.R;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.mst.tms.NetInfoEntity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import mst.widget.toolbar.Toolbar;

/**
 * 手动流量校正
 * @author zhaolaichao
 */
public class DataManCorrectActivity extends BaseActivity{
	
	private EditText mEdtDataPlan;
	
	/**
	 * 闲时流量
	 */
	private EditText mEdtFreeData;
    /** 
     * 手动校正sim卡ISSI
     */
	private String mSelectedSimIMSI;
	private int mSelectedIndex;
	
	private int mCommTotal;
	private int mFreeTotal;
	private int mUsedCommonData;
	private int mUsedFree;
	
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setMstContentView(R.layout.activity_data_man_correct);
    	Intent intent = getIntent();
    	mSelectedIndex = intent.getIntExtra("CURRENT_INDEX", 0);
    	if (mSelectedIndex == 0) {
	    	//卡1
	    	mSelectedSimIMSI = PreferenceUtil.getString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, "");
	    } else if (mSelectedIndex == 1) {
	    	//卡2
	    	mSelectedSimIMSI = PreferenceUtil.getString(this, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, "");
	    }
    	initView();
    }
    
    private void initView() {
    	Toolbar toolbar = getToolbar();
		 toolbar.setTitle(getString(R.string.data_correct_man));
		 toolbar.setElevation(1);
    	View dataView = findViewById(R.id.lay_dataplan);
		TextView tvData = (TextView) dataView.findViewById(R.id.tv_dataplan_free);
		tvData.setText(getString(R.string.dataplan_used));
    	mEdtDataPlan = (EditText) dataView.findViewById(R.id.edt_dataplan_total_free);
    	mEdtDataPlan.clearFocus(); //清除焦点
    	mEdtDataPlan.setSelectAllOnFocus(true);
    	View freeView = findViewById(R.id.lay_datafree);
		boolean freeStaus = PreferenceUtil.getBoolean(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_STATE_KEY, false);
		if (freeStaus) {
			freeView.setVisibility(View.VISIBLE);
		} else {
			freeView.setVisibility(View.GONE);
		}
    	TextView tvFreeData = (TextView)freeView. findViewById(R.id.tv_dataplan_free);
    	tvFreeData.setText(getString(R.string.data_free_used));
    	mEdtFreeData = (EditText) freeView.findViewById(R.id.edt_dataplan_total_free);
    	mEdtFreeData.clearFocus(); //清除焦点
    	mEdtFreeData.setSelectAllOnFocus(false);
    	mCommTotal = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
    	int commonRemain = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
    	mUsedCommonData = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
    	//闲时-已用
    	mFreeTotal = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
    	int remainFree = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
    	mUsedFree = PreferenceUtil.getInt(this, mSelectedSimIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, 0);
    	NetInfoEntity netInfoEntity = DataCorrect.getInstance().geNetInfoEntity();
    	//常规-已用
    	if (mUsedCommonData == 0 && commonRemain == 0) {
    		//初始化数据
    		if (freeStaus) {
    			mUsedCommonData = (int)((netInfoEntity.mUsedForMonth - netInfoEntity.mFreeUsedForMonth) / 1024);
    		} else {
    			mUsedCommonData = (int)(netInfoEntity.mUsedForMonth / 1024);
    		}
    		PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, mUsedCommonData);
    	} 
		if ( mUsedFree == 0 && remainFree == 0) {
			//初始化数据
			mUsedFree = (int)(netInfoEntity.mFreeUsedForMonth / 1024);
			PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, mUsedFree);
		} 
		
		mEdtDataPlan.setText("" + (mUsedCommonData >= 0 ? (int)(mUsedCommonData / 1024) : 0));
		mEdtFreeData.setText("" + (mUsedFree >= 0 ? (int)(mUsedFree / 1024) : 0));
		mEdtDataPlan.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				//常规套餐-已用流量大小 以KB为单位来存储
				boolean match = StringUtil.matchNumber(TextUtils.isEmpty(s) ? "" : s.toString());
				if (match) {
					PreferenceUtil.putInt(DataManCorrectActivity.this, mSelectedSimIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, TextUtils.isEmpty(s) ? 0 : (Integer.parseInt(s.toString()) * 1024));
				} else {
					Toast.makeText(DataManCorrectActivity.this, R.string.input_number, Toast.LENGTH_SHORT).show();
				}
				PreferenceUtil.putBoolean(DataManCorrectActivity.this, mSelectedSimIMSI, PreferenceUtil.MAN_INPUT_CORRECT_KEY, true);
			}
		});
		mEdtFreeData.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				//闲时套餐-已用流量大小 以KB为单位来存储
				boolean match = StringUtil.matchNumber(TextUtils.isEmpty(s) ? "" : s.toString());
				if (match) {
					PreferenceUtil.putInt(DataManCorrectActivity.this, mSelectedSimIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, TextUtils.isEmpty(s) ? 0 : (Integer.parseInt(s.toString()) * 1024));
				} else {
					Toast.makeText(DataManCorrectActivity.this, R.string.input_number, Toast.LENGTH_SHORT).show();
				}
				PreferenceUtil.putBoolean(DataManCorrectActivity.this, mSelectedSimIMSI, PreferenceUtil.MAN_INPUT_CORRECT_KEY, true);
			}
		});
		mEdtDataPlan.setOnFocusChangeListener(focusChangeListener);
		mEdtFreeData.setOnFocusChangeListener(focusChangeListener);
     }
    
    private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				((EditText) v).selectAll();
			}
		}
	};
	
	@Override
	protected void onPause() {
		super.onPause();
		int commUsed = PreferenceUtil.getInt(DataManCorrectActivity.this, mSelectedSimIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
		if (mCommTotal > 0) {
			PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY,  (mCommTotal - commUsed));
		}
		int freeUsed = PreferenceUtil.getInt(DataManCorrectActivity.this, mSelectedSimIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, 0);
		if (mFreeTotal > 0) {
			PreferenceUtil.putInt(this, mSelectedSimIMSI, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY,  (mFreeTotal - freeUsed));
		}
	}
	
	@Override
	public void onNavigationClicked(View view) {
		super.onNavigationClicked(view);
		this.finish();
	}

	@Override
	public void setSimStateChangeListener(int simState) {
	}
}
