package com.monster.netmanage.fragement;

import java.util.ArrayList;

import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.R;
import com.monster.netmanage.activity.DataRangeActivity;
import com.monster.netmanage.activity.MainActivity;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.mst.tms.NetInfoEntity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;

/**
 * sim02卡设置
 * @author zhaolaichao
 *
 */
public class Sim02Fragement extends Fragment implements View.OnClickListener{
	private DataManagerApplication mInstance;
	private static ArrayList<View> mViewContainer = new ArrayList<View>();
	private View mView;
	/**
	 * 当前上网卡
	 */
	private TextView mTvCurrent;
	/**
	 * 剩余通用流量
	 */
	private TextView mTvUsedData;
	/**
	 * 剩余闲时流量
	 */
	private TextView mTvSetData;
	/**
	 * 切换上网卡
	 */
	private TextView mTvChangeSim;
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
	 * sim卡2 当前上网卡所在卡槽
	 */
	private final int mCurrentNetSimIndex = 1;
	/**
	 * sim卡２ 选中卡位置为１
	 */
	private final int mSelectedSimIndex = 1;
	/**
	 *月结日
	 */
	private int mMonthEndDay = 0;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
		MainActivity mActivity = (MainActivity) getActivity();
		mActivity.statsAppsData(mSelectedSimIndex, true);
		mActivity.statsAppsCommon(mSelectedSimIndex, true);
		mActivity.statsAppsFree(mSelectedSimIndex, true);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.net_current_sim, container, false);
		mViewContainer.clear();
		mViewContainer.add(mView);
		initView();
		return mView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		int commonData = PreferenceUtil.getInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
		int remainData = commonData / 1024;
		mTvUsedData.setText("" + remainData);
		boolean freeStaus = PreferenceUtil.getBoolean(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.FREE_DATA_STATE_KEY, false);
		if (freeStaus) {
			mTvSetData.setVisibility(View.VISIBLE);
		} else {
			mTvSetData.setVisibility(View.GONE);
		}
		int freeData = PreferenceUtil.getInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
		mTvSetData.setText(String.format(getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(getActivity(), freeData)));
	
	}
	
	private void initView() {
		mImvSim = (ImageView) mView.findViewById(R.id.imv_sim);
		TextView tvSimType = (TextView) mView.findViewById(R.id.tv_sim_type);
		String operator = null;
		if (DataManagerApplication.mImsiArray.length < mCurrentNetSimIndex) {
			return;
		}
		if (TextUtils.isEmpty(DataManagerApplication.mImsiArray[mSelectedSimIndex])) {
			operator = getActivity().getString(R.string.un_operator);
		} else {
			operator = ToolsUtil.getSimOperator(this.getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex]);
		}
		tvSimType.setText(operator);
		mTvCurrent = (TextView) mView.findViewById(R.id.tv_current);
		mTvChangeSim = (TextView) mView.findViewById(R.id.tv_change_sim);
		//当前上网卡的卡槽位置
		int currentNetSimIndex = getArguments().getInt("CurrentNetSimIndex");
		if( mCurrentNetSimIndex == currentNetSimIndex) {
			//显示当前上网卡
			mTvCurrent.setText(getString(R.string.current_data_sim));
			mTvCurrent.setVisibility(View.VISIBLE);
			mImvSim.setImageResource(R.drawable.ic_sim2);
		} else if (currentNetSimIndex == -1){
			//当前卡槽无卡
			mImvSim.setImageResource(R.drawable.ic_un_sim2);
		} else {
			//切换上网卡
			mTvChangeSim.setVisibility(View.VISIBLE);
			mTvChangeSim.setOnClickListener(this);
			mImvSim.setImageResource(R.drawable.ic_un_sim2);
		}
		RelativeLayout layData = (RelativeLayout) mView.findViewById(R.id.lay_data_use);
		layData.setOnClickListener(this);
		//剩余通用流量
		mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		int commonData = PreferenceUtil.getInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
		int remainData = commonData / 1024;
		String data = "" + remainData;
		mTvUsedData.setText(data);
		//闲时流量可用
		mTvSetData = (TextView) mView.findViewById(R.id.tv_set_data);
		boolean freeStaus = PreferenceUtil.getBoolean(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.FREE_DATA_STATE_KEY, false);
		if (freeStaus) {
			mTvSetData.setVisibility(View.VISIBLE);
		} else {
			mTvSetData.setVisibility(View.GONE);
		}
		int freeData = PreferenceUtil.getInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
		mTvSetData.setText(String.format(getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(getActivity(), freeData)));
	
		mTvTodayUsed = (TextView) mView.findViewById(R.id.tv_today_used_count);
		mTvTodayUsedUnit = (TextView) mView.findViewById(R.id.tv_today_used_count_unit);
		mTvMonthEndDay = (TextView) mView.findViewById(R.id.tv_month_end_day);
		mTvAverageUse = (TextView) mView.findViewById(R.id.tv_average_use);
		mTvAverageUnit = (TextView) mView.findViewById(R.id.tv_average_unit);
		mTvTodayUsed.setText("0");
		mTvTodayUsedUnit.setText(getString(R.string.kilobyte_short));
		mTvAverageUse.setText("0");
		mTvAverageUnit.setText(getString(R.string.kilobyte_short));
		mMonthEndDay = PreferenceUtil.getInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.CLOSEDAY_KEY, 1);
		mTvMonthEndDay.setText("" + StringUtil.getDaysToMonthEndDay(mMonthEndDay));
	}
	
	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lay_data_use:
			String simTitle = null;
			int length = DataManagerApplication.mImsiArray.length;
			if (length == 1) {
				//只有一张卡
				simTitle = getString(R.string.sim) + getString(R.string.data_range);
			} else {
				if (TextUtils.isEmpty(DataManagerApplication.mImsiArray[0]) || TextUtils.isEmpty(DataManagerApplication.mImsiArray[1])) {
					//没有卡
					simTitle = getString(R.string.net_control);
				} else {
					simTitle = getString(R.string.sim2) + getString(R.string.data_range);
				}
			}
			Intent rangeIntent  = new Intent(getActivity(), DataRangeActivity.class);
			rangeIntent.putExtra("SIM_TITLE", simTitle);
			rangeIntent.putExtra("CURRENT_INDEX", mSelectedSimIndex);
			startActivity(rangeIntent);
			break;
		case R.id.tv_change_sim:
			dialogInfo(getActivity(), getString(R.string.info_change_sim));
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * 校正流量后更新UI界面
	 */
	public void updateUISim2() {
		mInstance = DataManagerApplication.getInstance();
		if (null == mView) {
			mView = mViewContainer.get(0);
		}
		int currentNetSimIndex = ToolsUtil.getCurrentNetSimSubInfo(DataManagerApplication.getInstance());
		if (currentNetSimIndex == -1){
			//当前卡槽无卡
			if ( null != mImvSim) {
				mImvSim.setImageResource(R.drawable.ic_un_sim2);
			}
			return;
		}
		if( mCurrentNetSimIndex == currentNetSimIndex) {
			//显示当前上网卡
			if (null != mImvSim) {
				mImvSim.setImageResource(R.drawable.ic_sim2);
			}
			if (null != mTvCurrent) {
				mTvCurrent.setText(mInstance.getString(R.string.current_data_sim));
				mTvCurrent.setVisibility(View.VISIBLE);
			}
			if (null != mTvChangeSim) {
				mTvChangeSim.setVisibility(View.GONE);
			}
		}  else {
			//切换上网卡
			if ( null != mImvSim) {
				mImvSim.setImageResource(R.drawable.ic_un_sim2);
			}
			if (null != mTvCurrent) {
				mTvCurrent.setVisibility(View.GONE);
			}
			if (null != mTvChangeSim) {
				mTvChangeSim.setVisibility(View.VISIBLE);
				mTvChangeSim.setOnClickListener(this);
			}
		}
		//常规-剩余
		int commonData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
		if (null != mTvUsedData) {
			int remainData = commonData / 1024;
			mTvUsedData.setText("" + remainData);
		}
		int freeData = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
		if (null != mTvSetData) {
			mTvSetData.setText(String.format(mInstance.getString(R.string.data_free_available), StringUtil.formatIntDataFlowSize(mInstance, freeData)));
		}
		//平均每天可用
		setAvgDataByDay();
	}
	
	/**
	 * 流量统计界面更新
	 * @param usedTotalForDay 今天已用
	 */
	public void updateUIByDataStats(NetInfoEntity netInfoEntity) {
		mInstance = DataManagerApplication.getInstance();
		if (null == mView) {
			mView = mViewContainer.get(0);
		}
		float usedTotalForDay = netInfoEntity.mUsedForDay / 1024;
		String totalData = StringUtil.formatDataFlowSize(mInstance, usedTotalForDay);
		if (null == mTvUsedData) {
			mTvUsedData = (TextView) mView.findViewById(R.id.tv_data_count);
		}
		if (null == mTvSetData) {
			mTvSetData = (TextView) mView.findViewById(R.id.tv_set_data);
		}
		if (null == mTvTodayUsed) {
			mTvTodayUsed = (TextView) mView.findViewById(R.id.tv_today_used_count);
		}
		if (null == mTvTodayUsedUnit) {
			mTvTodayUsedUnit = (TextView) mView.findViewById(R.id.tv_today_used_count_unit);
		}
		int firstCorrect = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.DATAPLAN_CORRECT_FIRST_KEY, 0);
		//手动输入校正
		boolean manInputState = PreferenceUtil.getBoolean(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.MAN_INPUT_CORRECT_KEY, false);
		if (firstCorrect == 0 || manInputState) {
			//没有使用流量校正
			float usedForMonth = netInfoEntity.mUsedForMonth / 1024;
			float usedFreeForMonth = netInfoEntity.mFreeUsedForMonth / 1024;
			int commonTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
			int remainData = (int)(commonTotal - (usedForMonth - usedFreeForMonth)) / 1024;
			if (remainData < 0) {
				remainData = 0;
			}
			PreferenceUtil.putInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, (int)remainData);
			mTvUsedData.setText("" + remainData);
			int freeTotal = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
			int remainFreeData = (int)(freeTotal - usedFreeForMonth);
			if (remainFreeData < 0) {
				remainFreeData = 0;
			}
			PreferenceUtil.putInt(getActivity(), DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, (int)remainFreeData);
			String remainFree = StringUtil.formatIntDataFlowSize(mInstance, remainFreeData);
			mTvSetData.setText(String.format(getString(R.string.data_free_available), remainFree));
		}
	    if (totalData.contains(StringUtil.DATA_DIVIDER_TAG)) {
	    	String[] dataArray = totalData.split(StringUtil.DATA_DIVIDER_TAG);
	    	mTvTodayUsed.setText(dataArray[0]);
	    	mTvTodayUsedUnit.setText(dataArray[1]);
	    } else {
	    	mTvTodayUsed.setText("0");
	    }
		setAvgDataByDay();
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
		float commonData = Float.parseFloat(TextUtils.isEmpty(mTvUsedData.getText().toString()) ? "" +0 : mTvUsedData.getText().toString());
		int closeDay = PreferenceUtil.getInt(mInstance, DataManagerApplication.mImsiArray[mSelectedSimIndex], PreferenceUtil.CLOSEDAY_KEY, 1);
		//距离月结日剩余的天数
		int days = StringUtil.getDaysToMonthEndDay(closeDay);
		int avg = 0;
		if (days > 0) {
			avg = (int) (commonData * 1024 /days);
		} else {
			avg = (int)(commonData * 1024);
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
	        	  boolean state = ToolsUtil.changeNetSim(getActivity(), mCurrentNetSimIndex);
	  		      if (state) {
	  		    	   mTvChangeSim.setVisibility(View.GONE);
	  		    	   mTvCurrent.setText(getString(R.string.current_data_sim));
	  				   mTvCurrent.setVisibility(View.VISIBLE);
	  				   mImvSim.setImageResource(R.drawable.ic_sim2);
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
