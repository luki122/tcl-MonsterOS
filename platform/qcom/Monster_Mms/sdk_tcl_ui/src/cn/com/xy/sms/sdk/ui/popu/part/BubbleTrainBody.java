package cn.com.xy.sms.sdk.ui.popu.part;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.TrainDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.TravelDataUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.AirTrainSelectDialog;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

@SuppressLint("ResourceAsColor")
public class BubbleTrainBody extends UIPart {
    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private ViewGroup mBodyView;
    private TextView mTrainNumber;
    private TextView mDepartDate;
    private TextView mArriveDate;
    private TextView mDepartTime;
    private TextView mArriveTime;
    private TextView mDepartCity;
    private TextView mArriveCity;
    private ImageView mTrainCenterIcon;
    private int mCurrentListIndex = 0;
    private JSONObject mTrainData;
    private JSONObject mToWebActivityJsonObject = new JSONObject();
    private ImageView mSelectTrainIcon;
    private RelativeLayout mSelectArea;
    private ImageView mSelectArriveAddressIcon;
    private TextView mTrainSeatInfoTitle;
    private TextView mTrainSeatInfo;
    private JSONArray mTrainArray;
    private long mLastClickTime;
    private TrianStationSelectedReceiver mRecerver;
    private String mResverFilag = "cn.com.xy.sms.TrianStationSelectedReceiver";
    private String mChectString;
    private String mDepartDateString = null;
    private TrainDataUtil mTrainDataUtil = TrainDataUtil.getInstance();
    private String mSupplementType1 = "1";
    private String mSupplementType2 = "2";
    private SimpleDateFormat mFormat = new SimpleDateFormat(ChannelContentUtil.TRAIN_DATE_FORMAT);
    private String mTrainSelectString = ChannelContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_select);

    private static final String SELECT_INDEX_KEY = "currentListIndex";
    private String trainNumber;
    private String departCityString;
    private String departTimeString;
    private String arriveCityString;
    private String arriveDateString;
    private String arriveTimeString;
    private JSONArray seatInfoArray;
    private String seatInfo;
    private String seatInfoTitle;

    public BubbleTrainBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initUi() throws Exception {
        // TODO Auto-generated method stub
        super.initUi();
        mBodyView = (ViewGroup) mView.findViewById(R.id.content_area);

        mTrainNumber = (TextView) mView.findViewById(R.id.duoqu_air_number);
        mDepartDate = (TextView) mView.findViewById(R.id.duoqu_depart_date);
        mArriveDate = (TextView) mView.findViewById(R.id.duoqu_arrive_date);
        mDepartTime = (TextView) mView.findViewById(R.id.duoqu_depart_time);
        mArriveTime = (TextView) mView.findViewById(R.id.duoqu_arrive_time);
        mDepartCity = (TextView) mView.findViewById(R.id.duoqu_depart_address);
        mArriveCity = (TextView) mView.findViewById(R.id.duoqu_arrive_address);
        mTrainCenterIcon = (ImageView) mView.findViewById(R.id.flight_center_icon);
        mSelectTrainIcon = (ImageView) mView.findViewById(R.id.duoqu_select_air_icon);
        mSelectArriveAddressIcon = (ImageView) mView.findViewById(R.id.duoqu_right_four_text_icon);
        mTrainSeatInfoTitle = (TextView) mView.findViewById(R.id.duoqu_train_seat_info_title);
        mTrainSeatInfo = (TextView) mView.findViewById(R.id.duoqu_train_seat_info);
        mSelectArea = (RelativeLayout) mView.findViewById(R.id.duoqu_left_first_text_icon_layout);

        
    }

    @SuppressLint("ResourceAsColor")
    private void setImageAndTextColor(BusinessSmsMessage message) {
        ThemeUtil.setTextColor(mContext, mTrainNumber, (String) message.getValue("v_by_text_7"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mDepartDate, (String) message.getValue("v_by_text_5"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mArriveDate, (String) message.getValue("v_by_text_6"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mDepartTime, (String) message.getValue("v_by_text_3"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mArriveTime, (String) message.getValue("v_by_text_4"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mDepartCity, (String) message.getValue("v_by_text_1"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mArriveCity, (String) message.getValue("v_by_text_2"),
                R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext, mTrainSeatInfoTitle, (String) message.getValue("v_by_text_l_1"),
                R.color.duoqu_theme_color_4011);
        ThemeUtil.setTextColor(mContext, mTrainSeatInfo, (String) message.getValue("v_by_text_r_1"),
                R.color.duoqu_theme_color_4010);

        ThemeUtil.setViewBg(mContext, mSelectTrainIcon, (String) message.getValue("v_by_icon_2"),
                R.drawable.duoqu_down_icon);
        ThemeUtil.setViewBg(mContext, mSelectArriveAddressIcon, (String) message.getValue("v_by_icon_3"),
                R.drawable.duoqu_down_icon);
        ThemeUtil.setViewBg(mContext, mTrainCenterIcon, (String) message.getValue("v_by_icon_8"),
                R.drawable.duoqu_train_direction);
        ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
    }
    
    @SuppressLint("ResourceAsColor")
    private void setTimeColorByStringIsNull(String time,TextView mTimeView , String nullTimeColor,String timeColor){
        if (StringUtils.isNull(time)) {
            ThemeUtil.setTextColor(mContext, mTimeView, (String) mMessage.getValue(nullTimeColor),
                    R.color.duoqu_theme_color_4010);
        }else {
            ThemeUtil.setTextColor(mContext, mTimeView, (String) mMessage.getValue(timeColor),
                    R.color.duoqu_theme_color_4010);
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        // TODO Auto-generated method stub
        super.setContent(message, isRebind);
        if (message == null ) {
            return ;
        }
        this.mMessage = message;
        setImageAndTextColor(mMessage);
        setTrainbodyBottom(message);
        initData(isRebind);
        bindTrainTickeInfo(mCurrentListIndex);
    }
    private void setTrainbodyBottom(BusinessSmsMessage message){
        int paddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)16, Constant.getContext().getResources().getDisplayMetrics());
        int paddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)16, Constant.getContext().getResources().getDisplayMetrics());
        int size = message.getTableDataSize("duoqu_table_data_horiz");
        if (size > 0) {
            int paddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)8, Constant.getContext().getResources().getDisplayMetrics());
            mBodyView.setPadding(paddingLeft, 0, paddingRight, paddingBottom);
        }else {
            int paddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)25, Constant.getContext().getResources().getDisplayMetrics());
            mBodyView.setPadding(paddingLeft, 0, paddingRight, paddingBottom);
        }
    }
    private void initData(boolean isRebind) {
        if (ChannelContentUtil.bubbleDataIsNull(mMessage)) {
            return;
        }
        Object index = mMessage.getValue(SELECT_INDEX_KEY);
        if (index != null) {
            mCurrentListIndex = (Integer) index;
        } else {
            mCurrentListIndex = 0;
        }


        try {
            Object obTrain = mMessage.getValue("card_arr");
            if (obTrain == null) {
                return;
            }

            mTrainArray = (JSONArray) obTrain;
            int trainSize = mTrainArray.length();
            if (trainSize > 1) {
                mSelectArea.setVisibility(View.VISIBLE);
                mTrainNumber.setOnClickListener(new OnClickDialog());
                mSelectArea.setOnClickListener(new OnClickDialog());
            } else {
                mTrainNumber.setOnClickListener(null);
                mSelectArea.setOnClickListener(null);
                mSelectArea.setVisibility(View.GONE);
            }

            if (mCurrentListIndex >= trainSize) {
                mCurrentListIndex = 0;
                mMessage.putValue(SELECT_INDEX_KEY, mCurrentListIndex);
            }

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTrainBody initData: " + e.getMessage(), e);
        }
    }
    private void setViewVisibilityByTrainSeatInfo(View view , String info){
        if (StringUtils.isNull(info)) {
            ChannelContentUtil.setViewVisibility(view, View.GONE);
            ChannelContentUtil.setViewVisibility(mTrainSeatInfoTitle, View.GONE);
        }else {
            ChannelContentUtil.setViewVisibility(view, View.VISIBLE);
            ChannelContentUtil.setViewVisibility(mTrainSeatInfoTitle, View.VISIBLE);
        }
    }
    @SuppressLint("ResourceAsColor")
    private void bindTrainTickeInfo(int currentTrainIndex) {
        mMessage.putValue("db_train_data_index", currentTrainIndex);
        mCurrentListIndex = currentTrainIndex;

        mTrainData = (JSONObject) mTrainDataUtil.getViewContentData(mMessage);
        if (mTrainData == null) {
            return;
        }

        trainNumber = mTrainData.optString("view_m_trainnumber");
        departCityString = mTrainData.optString("view_depart_city");
        mDepartDateString = mTrainData.optString("view_depart_date");
        departTimeString = mTrainData.optString("view_depart_time");
        arriveCityString = null;
        arriveCityString = mTrainData.optString("view_arrive_city");
        arriveDateString = mTrainData.optString("view_arrive_date");
        arriveTimeString = mTrainData.optString("view_arrive_time");
        seatInfoArray = mTrainData.optJSONArray("view_seat_info_list");
        seatInfo = getSeatInfoString(seatInfoArray);
        seatInfoTitle = (String) mMessage.getValue("m_by_text_l_1");
        mMessage.putValue("GROUP_KEY", trainNumber.replace(" ", "").replace(ChannelContentUtil.TRAIN_SUPPLEMENT_NUMBER, ""));

        Object hasSelect = mMessage.getValue("hasSelect" + mTrainDataUtil.getDataIndex(mMessage));
        Boolean hasQuery = mMessage.bubbleJsonObj.optBoolean("hasQuery" + mTrainDataUtil.getDataIndex(mMessage));
        Object supType = mMessage.bubbleJsonObj.optString("supplementType" + mTrainDataUtil.getDataIndex(mMessage));
        boolean isQuery = (hasQuery != null && hasQuery && supType != null);
        boolean isSelct = (hasSelect != null && (Boolean) hasSelect);
        
        if (StringUtils.isNull(arriveCityString)) {
            arriveCityString = ChannelContentUtil.TRAIN_SELECT_SITES;
            mSelectArriveAddressIcon.setVisibility(View.VISIBLE);
            mArriveCity.setOnClickListener(popupStationInfoListClickListener);
            mSelectArriveAddressIcon.setOnClickListener(popupStationInfoListClickListener);
        } else {
            mArriveCity.setOnClickListener(null);
            mSelectArriveAddressIcon.setOnClickListener(null);
            mSelectArriveAddressIcon.setVisibility(View.GONE);
        }
        if (isSelct) {
            arriveCityString = mTrainDataUtil.getInterfaceTrainArriveCity(mMessage);
            if (StringUtils.isNull(arriveCityString)) {
                arriveCityString = ChannelContentUtil.TRAIN_SELECT_SITES;
            }
        }

        if (isSelct || mSupplementType2.equals(supType)) {
            arriveDateString = mTrainDataUtil.getInterfaceTrainArriveDate(mMessage);
            if (StringUtils.isNull(arriveCityString)) {
                arriveDateString = "";
            }

            arriveTimeString = mTrainDataUtil.getInterfaceTrainArriveTime(mMessage);
            if (StringUtils.isNull(arriveTimeString)) {
                arriveTimeString = ChannelContentUtil.NO_DATA_ARR_TIME;
            }
        }

        if (StringUtils.isNull(departTimeString) && isQuery) {
            String departTimeStringQuery = mTrainDataUtil.getInterfaceTrainDepartTime(mMessage);
            if (!StringUtils.isNull(departTimeStringQuery)) {
                departTimeString = departTimeStringQuery;
            }
        }


        
        ChannelContentUtil.setText(mDepartCity, departCityString, "");
        ChannelContentUtil.setText(mDepartDate, mDepartDateString, "");
        ChannelContentUtil.setText(mDepartTime, departTimeString, ChannelContentUtil.NO_DATA_EN);
        setTimeColorByStringIsNull(departTimeString, mDepartTime, "v_by_n_1", "v_by_text_3");
        ChannelContentUtil.setText(mTrainSeatInfo, seatInfo, ChannelContentUtil.NO_DATA_EN);
        setViewVisibilityByTrainSeatInfo(mTrainSeatInfo, seatInfo);
        ChannelContentUtil.setText(mTrainNumber, trainNumber, "");
        ChannelContentUtil.setText(mTrainSeatInfoTitle, seatInfoTitle, ChannelContentUtil.NO_SEAT_INFO);

        
        setArriveInfo(arriveCityString, arriveDateString, arriveTimeString);

        if (StringUtils.isNull(trainNumber) || StringUtils.isNull(mDepartDateString)
                || StringUtils.isNull(departCityString)) {
            return;
        }

        if (!isQuery) {
            if (StringUtils.isNull(arriveCityString)) {
                if (StringUtils.isNull(departTimeString)) {
                    queryTrainStation(mSupplementType1);
                }
            } else {
                if (StringUtils.isNull(departTimeString) || StringUtils.isNull(arriveDateString)
                        || StringUtils.isNull(arriveTimeString)) {
                    queryTrainStation(mSupplementType2);
                }
            }
        }
    }

    private String getSeatInfoString(JSONArray seatInfoArray) {
        if(seatInfoArray == null || seatInfoArray.length() < 1){
            return null;
        }
        String seat = "";
        String carriage = "";
        String seatType = "";
        String sheets = "";
        StringBuilder sb = new StringBuilder();
        
        for(int i = 0; i < seatInfoArray.length(); i++){
            JSONObject seatInfo = seatInfoArray.optJSONObject(i);
            if(seatInfo == null){
                continue;
            }
            
            seat = seatInfo.optString("seat");
            carriage = seatInfo.optString("carriage");
            seatType = seatInfo.optString("seatType");
            if(!StringUtils.isNull(seat)){
                if(i > 0){
                    sb.append(ChannelContentUtil.TRAIN_SEAT_SPLIT);
                }
                if(!StringUtils.isNull(carriage)){
                    sb.append(carriage); 
                }
                sb.append(seat);
                if(!StringUtils.isNull(seatType)){
                    sb.append(seatType); 
                }
            }else if(!StringUtils.isNull(carriage) || !StringUtils.isNull(seatType)){
                sheets = seatInfo.optString("sheets");
                if(i > 0){
                    sb.append(ChannelContentUtil.TRAIN_SEAT_SPLIT);
                }
                if(!StringUtils.isNull(carriage)){
                    sb.append(carriage); 
                }
                if(!StringUtils.isNull(seatType)){
                    sb.append(seatType); 
                }
                if(!StringUtils.isNull(sheets)){
                    sb.append(sheets); 
                }
            }
        }
        
        return sb.toString();
    }

    protected void queryTrainStation(final String type) {
        if (ChannelContentUtil.bubbleDataIsNull(mMessage)) {
            return;
        }
        mTrainDataUtil.queryTrainStation(mMessage, new SdkCallBack() {

            @Override
            public void execute(Object... obj) {
                if (queryFail(obj) || !mTrainDataUtil.dataBelongCurrentMsg(mMessage, obj) || mContext == null
                        || mContext.isFinishing()) {
                    return;
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        trainDataCompletion(mMessage, type);
                    }
                });
            }

            private boolean queryFail(final Object... results) {
                return results == null || results.length != 2 || results[0] == null
                        || !(results[1] instanceof JSONArray);
            }
        }, type);
    }

    private void setArriveInfo(String arriveCity, String arriveDate, String arriveTime) {
        BubbleTrainBody.this.arriveCityString =arriveCity ;
        BubbleTrainBody.this.arriveDateString = arriveDate ;
        BubbleTrainBody.this.arriveTimeString = arriveTime ;
        ChannelContentUtil.setText(mArriveCity, arriveCity, ChannelContentUtil.TRAIN_SELECT_SITES);
        ChannelContentUtil.setText(mArriveDate, arriveDate, "");
        ChannelContentUtil.setText(mArriveTime, arriveTime, ChannelContentUtil.NO_DATA_ARR_TIME);
        setTimeColorByStringIsNull(arriveTime, mArriveTime, "v_by_n_2", "v_by_text_4");
    }

    private void trainDataCompletion(BusinessSmsMessage message, String supType) {
        if (!TravelDataUtil.hasValue(mDepartTime, ChannelContentUtil.NO_DATA_EN)) {
            ChannelContentUtil.setText(mDepartTime, mTrainDataUtil.getInterfaceTrainDepartTime(mMessage),
                    ChannelContentUtil.NO_DATA_EN);
            setTimeColorByStringIsNull(mTrainDataUtil.getInterfaceTrainDepartTime(mMessage), mDepartTime, "v_by_n_1", "v_by_text_3");
        }

        if (mSupplementType2.equals(supType)) {
            if (!TravelDataUtil.hasValue(mArriveDate, "")) {
                ChannelContentUtil.setText(mArriveDate, mTrainDataUtil.getInterfaceTrainArriveDate(mMessage), "");
            }
            if (!TravelDataUtil.hasValue(mArriveTime, ChannelContentUtil.NO_DATA_ARR_TIME)) {
                String interfaceTrainArriveTime = mTrainDataUtil.getInterfaceTrainArriveTime(mMessage) ;
                    ChannelContentUtil.setText(mArriveTime, interfaceTrainArriveTime,
                            ChannelContentUtil.NO_DATA_ARR_TIME);
                    setTimeColorByStringIsNull(interfaceTrainArriveTime, mArriveTime, "v_by_n_2", "v_by_text_4");
            }
        }
    }

    private void setWebJson(int currentTrainIndex, JSONObject train_data, String trainNumber, String departCityString,
            String departDateString, String departTimeString, String arriveCityString, String arriveDateString,
            String arriveTimeString) {
        mChectString = getCheckString(mMessage, currentTrainIndex);
        try {
            mToWebActivityJsonObject.put("checkString", mChectString);
            mToWebActivityJsonObject.put("type", "WEB_TRAIN_STATION_NEW");

            mToWebActivityJsonObject.put("view_train_number", trainNumber);
            mToWebActivityJsonObject.put("view_depart_date_time", train_data.optLong("view_depart_date_time"));

            mToWebActivityJsonObject.put("view_arrive_city", arriveCityString);
            mToWebActivityJsonObject.put("view_arrive_date", arriveDateString);
            mToWebActivityJsonObject.put("view_arrive_time", arriveTimeString);

            mToWebActivityJsonObject.put("view_depart_city", departCityString);
            mToWebActivityJsonObject.put("view_depart_date", departDateString);
            mToWebActivityJsonObject.put("view_depart_time", departTimeString);
            mToWebActivityJsonObject.put("select_station_title", ChannelContentUtil.TRAIN_SELECT_SITES);

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTrainBody: setWebJson : " + e.getMessage(), e);
        }
    }

    // 实现底部弹出列表的选中监听
    private class Onclick implements AirTrainSelectDialog.OnBottomClick {
        @Override
        public void Onclick(int type, int select) {
            // TODO Auto-generated method stub
            switch (type) {
            case AirTrainSelectDialog.CONFIRM:
                mCurrentListIndex = select;
                mMessage.putValue(SELECT_INDEX_KEY, mCurrentListIndex);
                bindTrainTickeInfo(mCurrentListIndex);

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

    // 点击选择车次监听
    private class OnClickDialog implements OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            AirTrainSelectDialog dialog = new AirTrainSelectDialog(mTrainArray, mContext, mCurrentListIndex);
            dialog.mParams.mSelectItemKey = "view_m_trainnumber";
            dialog.mParams.mDefaultTitleName = mTrainSelectString;
            dialog.ShowDialog(new Onclick());
        }
    }

    /* 调用H5进行站点查询监听 start */
    OnClickListener popupStationInfoListClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isOverClick()) {
                return;
            }
            registerReceiver();
            setWebJson(mCurrentListIndex, mTrainData, trainNumber, departCityString, mDepartDateString, departTimeString,
                    arriveCityString, arriveDateString, arriveTimeString);
            PopupUtil.startWebActivity(mContext, mToWebActivityJsonObject, "", "");
        }
    };

    private boolean isOverClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastClickTime > MIN_CLICK_DELAY_TIME) {
            mLastClickTime = currentTime;
            return false;
        }
        return true;
    }

    private void registerReceiver() {
        try {
            if (mRecerver == null) {
                mRecerver = new TrianStationSelectedReceiver(this);
            } else {
                mRecerver.setItem(this);
            }
            mContext.registerReceiver(mRecerver, new IntentFilter((String) mResverFilag));
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTrainBodyTtem: registerReceiver : " + e.getMessage(), e);
        }
    }

    static class TrianStationSelectedReceiver extends BroadcastReceiver {
        public void setItem(BubbleTrainBody item) {
            this.item = item;
        }

        private BubbleTrainBody item;

        public TrianStationSelectedReceiver(BubbleTrainBody item) {
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

    public void onReciver(Intent intent) {
        try {
            if (mMessage == null || mMessage.bubbleJsonObj == null)
                return;
            
            if (mSelectArriveAddressIcon.getVisibility() == View.GONE) {
                return ;
            }
            String action = intent.getAction();
            if (!mResverFilag.equals(action))
                return;
            String key = intent.getStringExtra("JSONDATA");
            if (StringUtils.isNull(key))
                return;
            String chect = null;
            String arriveCity = null;
            String arriveDate = null;
            String arriveTime = null;

            JSONObject jsonDataObject = new JSONObject(key);

            chect = jsonDataObject.optString("checkString");
            arriveCity = jsonDataObject.optString("view_arrive_city");
            SimpleDateFormat format_1 = new SimpleDateFormat("yyyy-MM-dd");
            String str = jsonDataObject.optString("view_arrive_date");
            if (!StringUtils.isNull(str)) {
                Date date = format_1.parse(str);
                arriveDate = mFormat.format(date);
            }
            arriveTime = jsonDataObject.optString("view_arrive_time");

            if (chect == null || !chect.equals(mChectString)) {
                return;
            }

            if (StringUtils.isNull(arriveCity) || StringUtils.isNull(arriveDate) || StringUtils.isNull(arriveTime)) {
                return;
            }

            if (!StringUtils.isNull(arriveDate)) {
                arriveDate = ChannelContentUtil.TRAIN_SUPPLEMENT_DATE + arriveDate;
            }

            mMessage.bubbleJsonObj.put(mTrainDataUtil.getInterfaceTrainArriveCityKey(mMessage), arriveCity);
            mMessage.bubbleJsonObj.put(mTrainDataUtil.getInterfaceTrainArriveDateKey(mMessage), arriveDate);
            mMessage.bubbleJsonObj.put(mTrainDataUtil.getInterfaceTrainArriveTimeKey(mMessage), arriveTime);

            mMessage.putValue("hasSelect" + mTrainDataUtil.getDataIndex(mMessage), true);
            ParseManager.updateMatchCacheManager(mMessage);
            setArriveInfo(arriveCity, arriveDate, arriveTime);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTrainBody onReciver error:", e);
        }
    }

    private String getCheckString(BusinessSmsMessage message, int dataIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(((JSONObject) mTrainDataUtil.getViewContentData(message)).optString("view_train_number"));
        stringBuilder.append(System.currentTimeMillis() + "");
        stringBuilder.append(String.valueOf(message.getSmsId()));
        stringBuilder.append(dataIndex + "");
        return stringBuilder.toString();
    }

        @Override
    public void changeData(Map<String, Object> param) {
        try {
            Object ob =  param.get("adjust_data");
            if(ob != null){
                setUpScheduleData(ob);
                return;
            }
        } catch (Throwable e) {
        }
    }
 
    private void setUpScheduleData(Object ob) {
        if(ob == null || !(ob instanceof  HashMap<?, ?>) || mToWebActivityJsonObject == null){
            return;
        }

        try {
            HashMap<String, Object> msgData = (HashMap<String, Object>) ob;
            msgData.put("checkString", mToWebActivityJsonObject.optString("checkString"));
        } catch (Throwable e) {
        }
        registerReceiver();
    }
            
    @Override
    public void destroy() {
      try{
          mContext.unregisterReceiver(mRecerver);
      }catch(Throwable e){
          
      }
      
      super.destroy();
    }

}
