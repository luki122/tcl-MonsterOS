package com.monster.netmanage.fragement;


import java.util.ArrayList;

import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.R;
import com.monster.netmanage.activity.DataRangeActivity;
import com.monster.netmanage.activity.MainActivity;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.CircleView;
import com.mst.tms.NetInfoEntity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;

/**
 * sim01卡设置
 * @author zhaolaichao
 *
 */
public class Sim01Fragement extends Fragment implements View.OnClickListener{
	private static DataManagerApplication mInstance;
	private static ArrayList<View> mViewContainer = new ArrayList<View>();
	private View mView;
	/**
	 * 剩余通用流量
	 */
	private TextView mTvUsedData;
	private TextView mTvUsedDataUnit;
	/**
	 * 剩余闲时流量
	 */
	private TextView mTvSetData;
	/**
	 * 当前上网卡
	 */
	private TextView mTvCurrent;
	/**
	 * 今天已用
	 */
	private TextView mTvTodayUsed;
	/**
	 * 今天已用 单位
	 */
	private TextView mTvTodayUsedUnit;
	/**
	 * 距月结日
	 */
	private TextView mTvMonthEndDay;
	/**
	 * 平均可用
	 */
	private TextView mTvAverageUse;
	/**
	 * 平均可用 单位
	 */
	private TextView mTvAverageUnit;
	/**
	 * 当前使用卡图标
	 */
	private ImageView mImvSim;
	/**
	 * 切换上网卡
	 */
	private RelativeLayout mLayChange;
	/**
	 * sim卡 卡槽位置为0
	 */
	private int mCurrentNetSimIndex = 0;
	private int mCheckedSimIndex = 0;
	/**
	 * sim卡1
	 */
	private final int SIM_1 = 0;
	/**
	 * sim卡2 
	 */
	private final int SIM_2 = 1;
	/**
	 *月结日
	 */
	private int mMonthEndDay = 0;
	private int mFreeData;
	private int mCommonData;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.net_current_sim, container, false);
		//sim卡状态发生改变
		mViewContainer.clear();
		mViewContainer.add(mView);
		mInstance = DataManagerApplication.getInstance();
		initView();
		return mView;
	}
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		boolean freeStaus = PreferenceUtil.getBoolean(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_STATE_KEY, false);
		if (freeStaus) {
			mTvSetData.setVisibility(View.VISIBLE);
		} else {
			mTvSetData.setVisibility(View.GONE);
		}
		setWarnState();
	}
	
	/**
	 * 设置警告状态
	 */
    private void setWarnState() {
    	if (TextUtils.isEmpty(DataManagerApplication.mImsiArray [0]) && TextUtils.isEmpty(DataManagerApplication.mImsiArray [1])) {
    		 return;
    	}
    	String activeImsi = DataManagerApplication.mImsiArray[MainActivity.PAGE_SELECTED_INDEX];
		int commonUsed = PreferenceUtil.getInt(mInstance, activeImsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
		int commonTotal = PreferenceUtil.getInt(mInstance, activeImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		String warnRate = PreferenceUtil.getString(mInstance, activeImsi, PreferenceUtil.PASS_WARNING_VALUE_KEY, "85%");
		int rate = Integer.parseInt(warnRate.substring(0, warnRate.indexOf("%")));
		if (commonTotal > 0) {
			boolean state = commonUsed * 100 / commonTotal > rate;
			Log.v("Sim01Fragement", "state>>" + state + ">>commonRemainData>>" + commonUsed + ">commonTotal>>>" + commonTotal);
			if (state) {
				MainActivity.mCircleView.setColor(CircleView.CIRVIEW_WARN_COLOR);
			} else {
				MainActivity.mCircleView.setColor(CircleView.CIRVIEW_COLOR);
			}
			PreferenceUtil.putBoolean(mInstance, DataManagerApplication.mImsiArray [MainActivity.PAGE_SELECTED_INDEX], PreferenceUtil.NOTIFY_WARN_MONTH_KEY, state);
		}
    }
    
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mView = null;
	}
	
	private void initView() {
		mCurrentNetSimIndex = ToolsUtil.getCurrentNetSimSubInfo(mInstance);
		mCheckedSimIndex = getArguments().getInt("CurrentSimIndex", mCheckedSimIndex);
		Log.v("Sim01Fragement", ">>mCheckedSimIndex>>>" + mCheckedSimIndex + ">>>mCurrentNetSimIndex>>>>>" + mCurrentNetSimIndex);
		mLayChange = (RelativeLayout) mView.findViewById(R.id.lay_change_sim);
		mImvSim = (ImageView) mView.findViewById(R.id.imv_sim);
		TextView tvSimType = (TextView) mView.findViewById(R.id.tv_sim_type);
		String operator = null;
		if (mCurrentNetSimIndex == -1 && TextUtils.isEmpty(DataManagerApplication.mImsiArray[mCheckedSimIndex])) {
			operator = mInstance.getResources().getString(R.string.un_operator);
		} else {
			operator = ToolsUtil.getSimOperator(this.getActivity(), DataManagerApplication.mImsiArray[mCheckedSimIndex]);
		}
		tvSimType.setText(operator);
		mTvCurrent = (TextView) mView.findViewById(R.id.tv_current);
		mTvUsedDataUnit = (TextView) mView.findViewById(R.id.tv_data_used);
		RelativeLayout layData = (RelativeLayout) mView.findViewById(R.id.lay_data_use);
		layData.setOnClickListener(this);
		if ( mCurrentNetSimIndex == mCheckedSimIndex) {
			//显示当前上网卡
			mTvCurrent.setText(mInstance.getResources().getString(R.string.current_data_sim));
			mTvCurrent.setVisibility(View.VISIBLE);
			if (SIM_1 == mCurrentNetSimIndex) {
				mImvSim.setImageResource(R.drawable.ic_sim1);
			} else if (SIM_2 == mCurrentNetSimIndex) {
				mImvSim.setImageResource(R.drawable.ic_sim2);
			}
		} else if (mCurrentNetSimIndex == -1){
			//当前卡槽无卡
			if (SIM_1 == mCheckedSimIndex) {
				mImvSim.setImageResource(R.drawable.ic_un_sim1);
			} else if (SIM_2 == mCheckedSimIndex) {
				mImvSim.setImageResource(R.drawable.ic_un_sim2);
			}
		} else {
			//切换上网卡
			mLayChange.setVisibility(View.VISIBLE);
			if (SIM_1 == mCheckedSimIndex) {
				mImvSim.setImageResource(R.drawable.ic_un_sim1);
			} else if (SIM_2 == mCheckedSimIndex) {
				mImvSim.setImageResource(R.drawable.ic_un_sim2);
			}
			mLayChange.setOnClickListener(this);
		}
		//剩余通用流量
		mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		int commonTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
        if (commonTotal > 0) {
        	mCommonData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
        	int remainData = mCommonData / 1024;
        	if (remainData < 0) {
        		mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.out_data_normal));
        	} else {
        		mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
        	}
        	mTvUsedData.setText("" + Math.abs(remainData));
        } else {
        	mTvUsedData.setText("" + Math.abs(0));
        	mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
        }
		//闲时流量可用
		mTvSetData = (TextView) mView.findViewById(R.id.tv_set_data);
		boolean freeStaus = PreferenceUtil.getBoolean(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_STATE_KEY, false);
		if (freeStaus) {
			mTvSetData.setVisibility(View.VISIBLE);
		} else {
			mTvSetData.setVisibility(View.GONE);
		}
		int freeTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
        if (freeTotal > 0) {
        	mFreeData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
        	if (mFreeData < 0) {
        		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_out), StringUtil.formatIntDataFlowSize(mInstance,  Math.abs(mFreeData))));
        	} else {
        		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance,  mFreeData)));
        	}
        } else {
    		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance,  0)));
        }
	
		mTvTodayUsed = (TextView) mView.findViewById(R.id.tv_today_used_count);
		mTvTodayUsedUnit = (TextView) mView.findViewById(R.id.tv_today_used_count_unit);
		mTvMonthEndDay = (TextView) mView.findViewById(R.id.tv_month_end_day);
		mTvAverageUse = (TextView) mView.findViewById(R.id.tv_average_use);
		mTvAverageUnit = (TextView) mView.findViewById(R.id.tv_average_unit);
		int usedDay = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DAY_USED_STATS_KEY, 0);
		String totalData = StringUtil.formatDataFlowSize(mInstance, usedDay);
		if (totalData.contains(StringUtil.DATA_DIVIDER_TAG)) {
	    	String[] dataArray = totalData.split(StringUtil.DATA_DIVIDER_TAG);
	    	mTvTodayUsed.setText(dataArray[0]);
	    	mTvTodayUsedUnit.setText(dataArray[1]);
	    } else {
	    	mTvTodayUsed.setText("0");
	    	mTvTodayUsedUnit.setText(mInstance.getResources().getString(R.string.kilobyte_short));
	    }
		mTvAverageUse.setText("0");
		mTvAverageUnit.setText(mInstance.getResources().getString(R.string.kilobyte_short));
		mMonthEndDay = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.CLOSEDAY_KEY, 1);
		mTvMonthEndDay.setText("" + StringUtil.getDaysToMonthEndDay(mMonthEndDay));
		//初始化
		updateUISim1(mCheckedSimIndex);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lay_data_use:
			String simTitle = null;
			Intent rangeIntent  = new Intent(mInstance, DataRangeActivity.class);
			if (mCurrentNetSimIndex == -1) {
				//没有卡
				simTitle = getString(R.string.net_control);
			} else {
				if (SIM_1 == mCheckedSimIndex) {
					simTitle = getString(R.string.sim1) + getString(R.string.data_range);
				} else if (SIM_2 == mCheckedSimIndex) {
					simTitle = getString(R.string.sim2) + getString(R.string.data_range);
				}
				rangeIntent.putExtra("CURRENT_INDEX", mCheckedSimIndex);
			}
			rangeIntent.putExtra("SIM_TITLE", simTitle);
			startActivity(rangeIntent);
			break;
		case R.id.lay_change_sim:
			dialogInfo(getActivity(), getString(R.string.info_change_sim));
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * 实时更新界面 每分钟
	 * @param simImsi
	 * @param netInfoEntity
	 */
	public void updateTiming(int updateIndex, NetInfoEntity netInfoEntity) {
		if (null == mView && mViewContainer.size() > 0) {
			mView = mViewContainer.get(0);
		} else {
			return;
		}
		if (null == mTvUsedData) {
			mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		}
		if (null == mTvUsedDataUnit) {
			mTvUsedDataUnit = (TextView) mView.findViewById(R.id.tv_data_used);
		}
		if (null == mTvSetData) {
			mTvSetData = (TextView) mView.findViewById(R.id.tv_set_data);
		}
		mInstance = DataManagerApplication.getInstance();
		mCheckedSimIndex = updateIndex;
		String simImsi = DataManagerApplication.mImsiArray[mCheckedSimIndex];
		int commonTotalData  = PreferenceUtil.getInt(mInstance, simImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		if (commonTotalData > 0) {
			int remainData  = PreferenceUtil.getInt(mInstance, simImsi, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
			remainData = remainData - (int)(netInfoEntity.mUsedForMinute) / 1024;
			if (remainData < 0) {
				mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.out_data_normal));
			} else {
				mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
			}
			PreferenceUtil.putInt(mInstance, simImsi, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, remainData);
			PreferenceUtil.putInt(mInstance, simImsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, commonTotalData - remainData);
			mTvUsedData.setText("" + Math.abs(remainData /1024));
		} else {
			mTvUsedData.setText("" + Math.abs(0));
		}
		boolean freeStaus = PreferenceUtil.getBoolean(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_STATE_KEY, false);
        if (freeStaus) {
        	int totalFreeData  = PreferenceUtil.getInt(mInstance, simImsi, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
        	if (totalFreeData > 0) {
        		int remainFreeData  = PreferenceUtil.getInt(mInstance, simImsi, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
        		//开始时间
        		String freeStartTime = PreferenceUtil.getString(mInstance, simImsi, PreferenceUtil.FREE_DATA_START_TIME_KEY, "00:00");
        		//结束时间
        		String freeEndTime = PreferenceUtil.getString(mInstance, simImsi, PreferenceUtil.FREE_DATA_END_TIME_KEY, "00:00");
        		int startHour = Integer.parseInt(freeStartTime.split(":")[0]);
        		int startMinute = Integer.parseInt(freeStartTime.split(":")[1]);
        		int endHour = Integer.parseInt(freeEndTime.split(":")[0]);
        		int endMinute = Integer.parseInt(freeEndTime.split(":")[1]);
        		long start = StringUtil.getStartTime(endHour, endMinute, 0);
        		long end = StringUtil.getEndTime(startHour, startMinute, 0);
        		//更新剩余闲时流量
        		if (System.currentTimeMillis() <= start || System.currentTimeMillis() >= end) {
        			remainFreeData = remainFreeData - (int)netInfoEntity.mUsedForMinute / 1024;
        		}
        		if (remainFreeData < 0) {
        			mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_out), StringUtil.formatIntDataFlowSize(mInstance,  Math.abs(remainFreeData))));
        		} else {
        			mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance, remainFreeData)));
        		}
        		PreferenceUtil.putInt(mInstance, simImsi, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, remainFreeData);
        		PreferenceUtil.putInt(mInstance, simImsi, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, totalFreeData - remainFreeData);
        	}
        } else {
    		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance, 0)));
        }
        usedForToday();
        setWarnState();
        setAvgDataByDay();
	}
	
	/**
	 * 校正流量后更新UI界面
	 */
	public void updateUISim1(int selectedSimIndex) {
		if (null == mView && mViewContainer.size() > 0) {
			mView = mViewContainer.get(0);
		} else {
			return;
		}
		mCheckedSimIndex = selectedSimIndex;
		mInstance = DataManagerApplication.getInstance();
		if (null == mImvSim) {
			mImvSim = (ImageView) mView.findViewById(R.id.imv_sim);
		}
		if (null == mTvCurrent) {
			mTvCurrent = (TextView) mView.findViewById(R.id.tv_current);
		}
		if (null == mTvUsedData) {
			mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		}
		if (null == mTvUsedDataUnit) {
			mTvUsedDataUnit = (TextView) mView.findViewById(R.id.tv_data_used);
		}
		if (null == mTvSetData) {
			mTvSetData = (TextView) mView.findViewById(R.id.tv_set_data);
		}
		mCurrentNetSimIndex = ToolsUtil.getCurrentNetSimSubInfo(DataManagerApplication.getInstance());
		if (mCurrentNetSimIndex == -1){
			//当前卡槽无卡
			mImvSim.setImageResource(R.drawable.ic_un_sim1);
			return;
		}
		
		if( mCurrentNetSimIndex == mCheckedSimIndex) {
			//显示当前上网卡
			if (mCurrentNetSimIndex == SIM_1) {
				mImvSim.setImageResource(R.drawable.ic_sim1);
			} else if (mCurrentNetSimIndex == SIM_2) {
				mImvSim.setImageResource(R.drawable.ic_sim2);
			}
			mTvCurrent.setText(mInstance.getString(R.string.current_data_sim));
			mTvCurrent.setVisibility(View.VISIBLE);
			if (null != mLayChange) {
				mLayChange.setVisibility(View.GONE);
			}
		}  else {
			//切换上网卡
			if (mCheckedSimIndex == SIM_1) {
				mImvSim.setImageResource(R.drawable.ic_un_sim1);
			} else if (mCheckedSimIndex == SIM_2) {
				mImvSim.setImageResource(R.drawable.ic_un_sim2);
			}
			mTvCurrent.setVisibility(View.GONE);
			if (null != mLayChange) {
				mLayChange.setVisibility(View.VISIBLE);
				mLayChange.setOnClickListener(this);
			}
		}
		int commonTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		if (commonTotal > 0) {
			//常规-剩余
			int commonData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
			if (null != mTvUsedData) {
				int remainData = commonData / 1024;
				if (remainData < 0) {
					mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.out_data_normal));
				} else {
					mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
				}
				mTvUsedData.setText("" + Math.abs(remainData));
			}
		} else {
			mTvUsedData.setText("" + Math.abs(0));
			mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
		}
		int freeTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
        if (freeTotal > 0) {
        	int freeData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
        	if (null != mTvSetData) {
        		if (freeData < 0) {
        			mTvSetData.setText(String.format(mInstance.getString(R.string.data_free_out), StringUtil.formatIntDataFlowSize(mInstance,  Math.abs(freeData))));
        		} else {
        			mTvSetData.setText(String.format(mInstance.getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance,  freeData)));
        		}
        	}
        } else {
			mTvSetData.setText(String.format(mInstance.getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance,  0)));
        }
        setWarnState();
		//平均每天可用
		setAvgDataByDay();
	}

	/**
	 * 流量统计界面更新
	 * @param usedTotalForDay 今天已用
	 */
	public void updateUIByDataStats(int selectSimIndex, NetInfoEntity netInfoEntity) {
		if (null == mView && mViewContainer.size() > 0) {
			mView = mViewContainer.get(0);
		} else {
			return;
		}
		mInstance = DataManagerApplication.getInstance();
		mCheckedSimIndex = selectSimIndex;
		
		if (null == mTvUsedData) {
			mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		}
		if (null == mTvUsedDataUnit) {
			mTvUsedDataUnit = (TextView) mView.findViewById(R.id.tv_data_used);
		}
		if (null == mTvSetData) {
			mTvSetData = (TextView) mView.findViewById(R.id.tv_set_data);
		}
		
		int firstCorrect = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DATAPLAN_CORRECT_FIRST_KEY, 0);
		//手动输入校正
		boolean manInputState = PreferenceUtil.getBoolean(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.MAN_INPUT_CORRECT_KEY, false);
		if (firstCorrect == 0 || manInputState) {
			//没有使用流量校正
			int commonTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
            if (commonTotal > 0) {
            	int remainData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
            	if (remainData < 0) {
            		mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.out_data_normal));
            	} else {
            		mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
            	}
            	mTvUsedData.setText("" + Math.abs(remainData / 1024));
            } else {
            	mTvUsedData.setText("" + Math.abs(0));
            	mTvUsedDataUnit.setText(mInstance.getResources().getString(R.string.remain_data_normal));
            }
			boolean freeStaus = PreferenceUtil.getBoolean(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_STATE_KEY, false);
			if (freeStaus) {
				int freeTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
                if (freeTotal > 0) {
                	int remainFreeData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
                	String remainFree = StringUtil.formatIntDataFlowSize(mInstance, Math.abs(remainFreeData));
                	if (remainFreeData < 0 ) {
                		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_out), remainFree));
                	} else {
                		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_available), remainFree));
                	}
                } else {
            		mTvSetData.setText(String.format(mInstance.getResources().getString(R.string.data_free_available), 0));
                }
			}
		}
		usedForToday();
		setAvgDataByDay();
	}
	
	/**
	 * 今天已用
	 * @param netInfoEntity
	 */
	private void usedForToday() {
		if (null == mTvTodayUsed) {
			mTvTodayUsed = (TextView) mView.findViewById(R.id.tv_today_used_count);
		}
		if (null == mTvTodayUsedUnit) {
			mTvTodayUsedUnit = (TextView) mView.findViewById(R.id.tv_today_used_count_unit);
		}
		float usedTotalForDay = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DAY_USED_STATS_KEY, 0);
		String totalData = StringUtil.formatDataFlowSize(mInstance, usedTotalForDay);
		if (totalData.contains(StringUtil.DATA_DIVIDER_TAG)) {
	    	String[] dataArray = totalData.split(StringUtil.DATA_DIVIDER_TAG);
	    	mTvTodayUsed.setText(dataArray[0]);
	    	mTvTodayUsedUnit.setText(dataArray[1]);
	    } else {
	    	mTvTodayUsed.setText("0");
	    }
	}
	
	/**
	 * 平均每天可用
	 */
	private void setAvgDataByDay() {
		if (null == mTvUsedData) {
			mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		}
		if (null == mTvAverageUse) {
			mTvAverageUse = (TextView) mView.findViewById(R.id.tv_average_use);
		}
		if (null == mTvAverageUnit) {
			mTvAverageUnit = (TextView) mView.findViewById(R.id.tv_average_unit);
		}
		int commonTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		int avg = 0;
        if (commonTotal > 0) {
        	int commonData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
        	int closeDay = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mCheckedSimIndex], PreferenceUtil.CLOSEDAY_KEY, 1);
        	//距离月结日剩余的天数
        	int days = StringUtil.getDaysToMonthEndDay(closeDay);
        	if (commonData <= 0) {
        		avg = 0;
        	} else {
        		if (days > 0) {
        			avg = (int) (commonData / days);
        		} else {
        			avg = (int)(commonData);
        		}
        	}
        } 
		String avgDay = StringUtil.formatDataFlowSize(mInstance, avg);
		if (avgDay.contains(StringUtil.DATA_DIVIDER_TAG)) {
	    	String[] dataArray = avgDay.split(StringUtil.DATA_DIVIDER_TAG);
	    	mTvAverageUse.setText(dataArray[0]);
	    	mTvAverageUnit.setText(dataArray[1]);
	    } else {
	    	mTvAverageUse.setText("0");
	    }
	}
	
	/**
	 * 切换默认上网卡
	 * @param context
	 * @param message
	 */
	protected void dialogInfo(Context context, String message) {
	      AlertDialog.Builder builder = new Builder(context);
	      builder.setMessage(message);
	      builder.setTitle(context.getString(R.string.show_info));
	      builder.setPositiveButton(context.getString(com.mst.R.string.ok), new OnClickListener() {
	          @Override
	          public void onClick(DialogInterface dialog, int which) {
	        	  boolean state = ToolsUtil.changeNetSim(getActivity(), mCheckedSimIndex);
	  		      if (state) {
	  		    	  MainActivity.PAGE_SELECTED_INDEX = mCheckedSimIndex;
	  		    	  mLayChange.setVisibility(View.GONE);
	  		    	   mTvCurrent.setText(getString(R.string.current_data_sim));
	  				   mTvCurrent.setVisibility(View.VISIBLE);
	  				   if (SIM_1 == mCheckedSimIndex) {
	  					   mImvSim.setImageResource(R.drawable.ic_sim1);
	  				   } else if (SIM_2 == mCheckedSimIndex) {
	  					   mImvSim.setImageResource(R.drawable.ic_sim2);
	  				   }
	  		      }
	              dialog.dismiss();
	          }
	      });
	      builder.setNegativeButton(context.getString(com.mst.R.string.cancel), new OnClickListener() {
	           @Override
	           public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	            }
	      });
	      builder.create().show();
	 }
}