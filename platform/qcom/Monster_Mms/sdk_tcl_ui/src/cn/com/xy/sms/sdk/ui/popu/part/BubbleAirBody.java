package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.FlightDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.AirTrainSelectDialog;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class BubbleAirBody extends UIPart {
    private ViewGroup mBodyView;
    private TextView  mAirNumberTextView;
    private TextView  mDepartDateTextView;
    private TextView  mArriveDateTextView;
    private TextView  mDepartTimeTextView;
    private TextView  mArriveTimeTextView;
    private TextView  mDepartAdressTextView;
    private TextView  mArriveAdressTextView;
    private TextView mStateTextView;
    String mFlightNumber = null;
    String mDepartDate = null;
    String mDepartTime = null;
    String mDepartCity = null;
    String mDepartTerminal = null;
    String mDepartAirPort = null;
    String mArriveDate = null;
    String mArriveTime = null;
    String mArriveCity = null;
    String mArriveAirPort = null;
    String mArriveTerminal = null;
    String mDepartAdress = null;
    String mArriveAdress = null;
    private ImageView mCenterIcon;
    private JSONArray mAirArray;
    private JSONObject mAirData;
    private ImageView mSelectAirIcon;
    private FlightDataUtil mFlightDataUtil;
    private static final String SELECT_INDEX_KEY = "currentListIndex";
    private static final String AIR_LIST_KEY = "view_flight_number";
    private int mCurrentListIndex = 0;
    
    public BubbleAirBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
        // TODO Auto-generated constructor stub
        mFlightDataUtil = FlightDataUtil.getInstance();
    }
    
    @Override
    public void initUi() throws Exception {
        // TODO Auto-generated method stub
        super.initUi();
        mBodyView = (ViewGroup) mView.findViewById(R.id.content_area);
        mAirNumberTextView = (TextView) mView.findViewById(R.id.duoqu_air_number);
        mDepartDateTextView = (TextView) mView.findViewById(R.id.duoqu_depart_date);
        mArriveDateTextView = (TextView) mView.findViewById(R.id.duoqu_arrive_date);
        mDepartTimeTextView = (TextView) mView.findViewById(R.id.duoqu_depart_time);
        mArriveTimeTextView = (TextView) mView.findViewById(R.id.duoqu_arrive_time);
        mDepartAdressTextView = (TextView) mView.findViewById(R.id.duoqu_depart_address);
        mArriveAdressTextView = (TextView) mView.findViewById(R.id.duoqu_arrive_address);
        mCenterIcon = (ImageView) mView.findViewById(R.id.flight_center_icon);
        mSelectAirIcon = (ImageView) mView.findViewById(R.id.duoqu_select_air_icon);
        mStateTextView = (TextView) mView.findViewById(R.id.duoqu_air_body_sec_state);
    }

       
   @SuppressLint("ResourceAsColor")
    private void setImageAndTextColor() {
       ThemeUtil.setTextColor(mContext, mAirNumberTextView, (String)mMessage.getValue("v_by_text_7"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mDepartDateTextView, (String)mMessage.getValue("v_by_text_5"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mArriveDateTextView, (String)mMessage.getValue("v_by_text_6"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mDepartTimeTextView, (String)mMessage.getValue("v_by_text_3"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mArriveTimeTextView, (String)mMessage.getValue("v_by_text_4"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mDepartAdressTextView, (String)mMessage.getValue("v_by_text_1"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mArriveAdressTextView, (String)mMessage.getValue("v_by_text_2"), R.color.duoqu_theme_color_4010);
       ThemeUtil.setTextColor(mContext, mStateTextView, (String) mMessage.getValue("v_by_text_8"),
               R.color.duoqu_theme_color_4010);
       ThemeUtil.setViewBg(mContext, mSelectAirIcon, (String)mMessage.getValue("v_by_icon_4"), R.drawable.v_by_icon_2);
       ThemeUtil.setViewBg(mContext, mCenterIcon, (String)mMessage.getValue("v_by_icon_7"), R.drawable.duoqu_air);
       ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        // TODO Auto-generated method stub
        super.setContent(message, isRebind);
        this.mMessage = message;
        if (ChannelContentUtil.bubbleDataIsNull(mMessage)){
            return;
        }

        setImageAndTextColor();
        
        initData();
        bindAirData();
        updateStateView();
    }

    private void initData() {
        if (ChannelContentUtil.bubbleDataIsNull(mMessage)) {
            return;
        }
        Object index = mMessage.getValue(SELECT_INDEX_KEY);
        if (index != null) {
            mCurrentListIndex = (Integer) index;
        }else{
            mCurrentListIndex = 0 ;
        }
        
        try {
            Object obAir = mMessage.getValue("flight_data_arr");
            if (obAir != null) {
                mAirArray = (JSONArray) obAir;
            } 
            
            if (mAirArray != null) {
                int airSize = mAirArray.length();
                if (airSize > 1) {
                    mAirNumberTextView.setOnClickListener(new OnClickDialog());
                    mSelectAirIcon.setVisibility(View.VISIBLE);
                    mSelectAirIcon.setOnClickListener(new OnClickDialog());
                } else {
                    mAirNumberTextView.setOnClickListener(null);
                    mSelectAirIcon.setOnClickListener(null);
                    mSelectAirIcon.setVisibility(View.GONE);
                }

                if(mCurrentListIndex >= airSize){
                	 mCurrentListIndex = 0;
                	 mMessage.putValue(SELECT_INDEX_KEY, mCurrentListIndex);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleAirBody initData error:", e);
        }
        
    }
    
    private void bindAirData() {
        mMessage.putValue("db_air_data_index", mCurrentListIndex);
        try{
            mAirData = (JSONObject) mFlightDataUtil.getViewContentData(mMessage);
            if (mAirData == null){
                return;
            }
        }catch(Throwable e){
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleAirBody bindAirData error:", e);
        }
        
        mFlightNumber = mAirData.optString(AIR_LIST_KEY);
        mDepartDate = mAirData.optString("view_depart_date");
        mDepartTime = mAirData.optString("view_depart_time");
        mDepartCity = mAirData.optString("view_depart_city");
        mDepartTerminal = mAirData.optString("view_depart_terminal");
        mDepartAirPort = mAirData.optString("view_depart_airport");
        mArriveDate = mAirData.optString("view_arrive_date");
        mArriveTime = mAirData.optString("view_arrive_time");
        mArriveCity = mAirData.optString("view_arrive_city");
        mArriveAirPort = mAirData.optString("view_arrive_airport");
        mArriveTerminal = mAirData.optString("view_arrive_terminal");
        mMessage.putValue("GROUP_KEY", mFlightNumber.replace(" ", ""));

        mDepartAdress = FlightDataUtil.getAirPortDetail(mDepartCity,mDepartAirPort, mDepartTerminal);
        mArriveAdress = FlightDataUtil.getAirPortDetail(mArriveCity,mArriveAirPort, mArriveTerminal);
        Boolean hasQuery = mMessage.bubbleJsonObj.optBoolean("hasQuery" + mFlightDataUtil.getDataIndex(mMessage));
        boolean isQuery = (hasQuery != null && hasQuery);

        if(isQuery){
            String arriveCity = mFlightDataUtil.getInterfaceFlightArriveCity(mMessage);
            if (!StringUtils.isNull(arriveCity)
                && (StringUtils.isNull(mArriveAdress) ? true : arriveCity.length() > mArriveAdress.length())) {
                mArriveAdress = arriveCity;
            }

            if (StringUtils.isNull(mArriveDate)) {
                mArriveDate = mFlightDataUtil.getInterfaceFlightArriveDate(mMessage);
            }


            if (StringUtils.isNull(mArriveTime)) {
                mArriveTime = mFlightDataUtil.getInterfaceFlightArriveTime(mMessage);
            }

            if (StringUtils.isNull(mDepartTime)) {
                mDepartTime = mFlightDataUtil.getInterfaceFlightDepartTime(mMessage);
            }

            String departCity = mFlightDataUtil.getInterfaceFlightDepartCity(mMessage);
            if (!StringUtils.isNull(departCity)
                && (StringUtils.isNull(mDepartAdress) ? true : departCity.length() > mDepartAdress.length())) {
                mDepartAdress = departCity;
            }
            
        }
        
        ChannelContentUtil.setText(mDepartAdressTextView, mDepartAdress, "");
        ChannelContentUtil.setText(mDepartDateTextView, mDepartDate, "");
        ChannelContentUtil.setText(mDepartTimeTextView, mDepartTime, ChannelContentUtil.NO_DATA_ARR_TIME);
        ChannelContentUtil.setText(mArriveAdressTextView, mArriveAdress, "");
        ChannelContentUtil.setText(mArriveDateTextView, mArriveDate, "");
        ChannelContentUtil.setText(mArriveTimeTextView, mArriveTime, ChannelContentUtil.NO_DATA_ARR_TIME);
        if(!StringUtils.isNull(mFlightNumber)){
        	if(mFlightNumber.contains(ChannelContentUtil.FLIGHT_DEF_END)){
        		mAirNumberTextView.setText(mFlightNumber);
        	}
        	else{
        		mAirNumberTextView.setText(mFlightNumber+ChannelContentUtil.FLIGHT_DEF_END);
        	}
        }
        else{
        	mAirNumberTextView.setText("");
        }
        
        
        if (!isQuery){
            if (StringUtils.isNull(mDepartTime) || StringUtils.isNull(mArriveAdress)
                    || StringUtils.isNull(mArriveDate) || StringUtils.isNull(mArriveTime)|| StringUtils.isNull(mFlightDataUtil.getInterfaceFlightState(mMessage))) {
                if (!StringUtils.isNull(mFlightNumber) && !StringUtils.isNull(mDepartDate)
                        && !StringUtils.isNull(mDepartAdress)) {
                    queryFlyDataAsy(mMessage);
                }
            }
        }
   
    }
    
    private class Onclick implements AirTrainSelectDialog.OnBottomClick {
        
        @Override
        public void Onclick(int type, int select) {
            // TODO Auto-generated method stub
            switch (type) {
                case AirTrainSelectDialog.CONFIRM:
                    mCurrentListIndex = select;
                    mMessage.putValue(SELECT_INDEX_KEY, mCurrentListIndex);
                    bindAirData();
                    ParseManager.updateMatchCacheManager(mMessage);
                    HashMap<String, Object> param = new HashMap<String, Object>();
                    String groupValue = "do";
                    param.put("type", BasePopupView.CHANGE_DATA_TYPE_FOOT);
                    param.put("groupValue", groupValue);
                    if (mBasePopupView != null) {
                        mBasePopupView.changeData(param);
                    }
 
                    break;
                    
                default:
                    break;
            }
        }
        
    }
    
    private class OnClickDialog implements OnClickListener{
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            AirTrainSelectDialog dialog = new AirTrainSelectDialog(mAirArray, mContext, mCurrentListIndex);
            dialog.mParams.mSelectItemKey = AIR_LIST_KEY;
            dialog.mParams.mDefaultTitleName = ChannelContentUtil.FIGHT_NUMBER_SELECT;
            dialog.ShowDialog(new Onclick());
        }
    }
    @SuppressLint("ResourceAsColor")
    protected void flyDataCompletion() {
       String mArriveCity = mFlightDataUtil.getInterfaceFlightArriveCity(mMessage);
       String state = FlightDataUtil.getInstance().getInterfaceFlightState(mMessage);
       if(!StringUtils.isNull(state)){
           ChannelContentUtil.setText(mStateTextView, state, "");
           ThemeUtil.setTextColor(mContext, mStateTextView, (String) mMessage.getValue("v_by_text_8"),
                   R.color.duoqu_theme_color_4010);
           mStateTextView.setVisibility(View.VISIBLE);
       }
       else{
    	   mStateTextView.setVisibility(View.GONE);
       }
       if (!StringUtils.isNull(mArriveCity)
               && (StringUtils.isNull(mArriveAdress) ? true : mArriveCity.length() > mArriveAdress.length())) {
           ChannelContentUtil.setText(mArriveAdressTextView, mArriveCity, "");
       }
       
       if (StringUtils.isNull(mArriveDate)) {
           ChannelContentUtil.setText(mArriveDateTextView, mFlightDataUtil.getInterfaceFlightArriveDate(mMessage), "");
       }
       
       /*HUAWEI-1334 zhengxiaobo 20160711 begin*/
       if (StringUtils.isNull(mArriveTime)) {
           ChannelContentUtil.setText(mArriveTimeTextView, mFlightDataUtil.getInterfaceFlightArriveTime(mMessage),
                   ChannelContentUtil.NO_DATA_ARR_TIME);
       }
       
       if (StringUtils.isNull(mDepartTime)) {
           ChannelContentUtil.setText(mDepartTimeTextView, mFlightDataUtil.getInterfaceFlightDepartTime(mMessage),
                   ChannelContentUtil.NO_DATA_DEP_TIME);
       }
       /*HUAWEI-1334 zhengxiaobo 20160711 end*/
       String mDepartCity = mFlightDataUtil.getInterfaceFlightDepartCity(mMessage);
       if (!StringUtils.isNull(mDepartCity)
               && (StringUtils.isNull(mDepartAdress) ? true : mDepartCity.length() > mDepartAdress.length())) {
           ChannelContentUtil.setText(mDepartAdressTextView, mDepartCity, "");
       }
    }
    
    protected void queryFlyDataAsy(final BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return;
        }

        mFlightDataUtil.queryFlyData(smsMessage, new SdkCallBack() {
            @Override
            public void execute(Object... obj) {
                if (queryFail(obj) 
                        || !mFlightDataUtil.dataBelongCurrentMsg(mMessage, obj) 
                        || mContext == null 
                        || mContext.isFinishing()
                        || mCurrentListIndex != mFlightDataUtil.getDefaultSelectedIndex(mMessage)) {
                    return;
                }

                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flyDataCompletion();
                    }
                });     
            }

            private boolean queryFail(Object... obj) {
                return obj == null || obj.length < 1 || obj[0] == null;
            }
        });
    }
    
    @Override
    public void destroy() {
        super.destroy();
        if (mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Throwable e) {

            }
        }
    }
    
    @Override
    public void changeData(Map<String, Object> param) {
        super.changeData(param);
        if(param == null){
            return;
        }
        Object isFlightState = param.get("isFlightState");
        if (isFlightState != null && (Boolean) isFlightState) {
            registerReceiver();
        }
        
        String isNumberChange = (String) param.get("number_change");
        if(isNumberChange!= null && "true".equals(isNumberChange)){
            Object index = mMessage.getValue(SELECT_INDEX_KEY);
            if (index != null) {
                mCurrentListIndex = (Integer) index;
            }else{
                mCurrentListIndex = 0 ;
            }
            bindAirData();
        }
        
    }
    
    @SuppressLint("ResourceAsColor")
    private void updateStateView() {
        Object hasQuery = mMessage.getValue("hasQuery" + mCurrentListIndex);
        if (hasQuery == null || !(Boolean) hasQuery) {
            mStateTextView.setVisibility(View.GONE);
            return;
        }
        mMessage.putValue("db_air_data_index", mCurrentListIndex);
        String state = FlightDataUtil.getInstance().getInterfaceFlightState(mMessage);
        if (StringUtils.isNull(state)) {
            mStateTextView.setVisibility(View.GONE);
            return;
        }
        ChannelContentUtil.setText(mStateTextView, state, "");
        ThemeUtil.setTextColor(mContext, mStateTextView, (String) mMessage.getValue("v_by_text_8"),
                R.color.duoqu_theme_color_4010);
        mStateTextView.setVisibility(View.VISIBLE);
    }
    
    
    static class FlightStateQueryReceiver extends BroadcastReceiver {

        private BubbleAirBody item;

        public void setItem(BubbleAirBody item) {
            this.item = item;
        }

        public FlightStateQueryReceiver(BubbleAirBody item) {
            this.item = item;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (item == null)
                return;
            item.onReciver(intent);
            item = null;
        }
    }
    
    public FlightStateQueryReceiver mReceiver = null;
    private final static String ACTION_FLIGHT_STATE_QUERY = "cn.com.xy.sms.FlightStateQueryReceiver";

    private void registerReceiver() {
        try {
            if (mReceiver == null) {
                mReceiver = new FlightStateQueryReceiver(this);
            } else {
                mReceiver.setItem(this);
            }
            mContext.registerReceiver(mReceiver, new IntentFilter(ACTION_FLIGHT_STATE_QUERY));
        } catch (Exception e) {
            LogManager.e(Constant.TAG, "BubbleTitleHead: registerReceiver : " + e.getMessage(), e);
        }
    }
    public void onReciver(Intent intent) {
        try {
            mContext.unregisterReceiver(mReceiver);
            if (mMessage == null || mMessage.bubbleJsonObj == null) {
                return;
            }

            String action = intent.getAction();
            if (ACTION_FLIGHT_STATE_QUERY.equals(action)) {
                String key = intent.getStringExtra("JSONDATA");
                if (StringUtils.isNull(key))
                    return;
                JSONObject jsonObject = new JSONObject(key);
                String statusString = jsonObject.optString("view_flight_latest_status");
                if (!StringUtils.isNull(statusString)) {
                    mFlightDataUtil.savaArriveInfo(mMessage, new JSONObject(statusString));
                    updateStateView();
                }
            }
        } catch (Throwable e) {
            LogManager.e(Constant.TAG, "BubbleTitleHead: onReciver : " + e.getMessage(), e);
        }
    }
 
}
