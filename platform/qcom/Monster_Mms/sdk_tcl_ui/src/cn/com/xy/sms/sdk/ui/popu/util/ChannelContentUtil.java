package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.Map;

import org.json.JSONObject;

import android.view.View;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.StringUtils;

public class ChannelContentUtil extends ContentUtil {
    private static final String TAG = "ChannelContentUtil";
    public static final String TRAIN_DATE_FORMAT = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_date_format_qiku);
    public static final String FIGHT_STATE_DELAYS = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_state_delays);
    public static final String FIGHT_STATE_PLAN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_state_plan);
    public static final String FIGHT_STATE_DEPARTURE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_state_departure);
    public static final String FIGHT_STATE_RETURN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_state_return);
    public static final String FIGHT_STATE_ARRIVAL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_date_arrival);
    public static final String FIGHT_NUMBER_SELECT = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_number_select);
    public static final String TRAIN_SUPPLEMENT_DATE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_dupplement_date);
    public static final String TRAIN_SELECT_SITES = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_ui_part_select_destination);
    public static final String TRAIN_SEAT_SPLIT = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_train_seat_split);
    public static final String NO_DATA_ARR_TIME = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_double_line_arr_time);
    public static final String NO_DATA_DEP_TIME = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_double_line_dep_time);
    public static final String NO_SEAT_INFO = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_seat_info);
    public static final CharSequence TRAIN_SUPPLEMENT_NUMBER = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_time);
    public static final CharSequence MORE_INFO_SHOW = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_more_info_show);
    public static final CharSequence MORE_INFO_HIDE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_more_info_hide);
    public static final int FLIGHT_UNKNOW_TIME_TEXT_SIZE = ContentUtil
            .getDimension(R.dimen.duoqu_air_body_sec_unknow_time_size);
    public static final String FLIGHT_DEF_END = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_end);
    public static final int FLIGHT_TIME_TEXT_SIZE = ContentUtil.getDimension(R.dimen.duoqu_air_body_sec_normal_time_size);
    public static final String FLIGHT_UNKNOW_TIME = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_unknow_time);
    public static final String TRAIN_DEF_END = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_end);
    public static final String FLIGHT_NUMBER_SELECT = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_number_select);
    public static final String TRAIN_NUMBER_SELECT = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_select);
    public static final String TRAIN_DEPART = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_depart);
    public static final String TRAIN_ARRIVE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_arrive);
    public static final String FLIGHT_ARRIVE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_flight_arrive);
    
    public static final String FLIGHT_STATE_PLAN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_plan);
    public static final String FLIGHT_STATE_DEPARTURE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_departure);
    public static final String FLIGHT_STATE_RETURN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_return);
    public static final String FLIGHT_STATE_RETURN_DEPARTURE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_return_departure);
    public static final String FLIGHT_STATE_ALTERNATE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_alternate);
    public static final String FLIGHT_STATE_PLAN_ALTERNATE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_alternate_departure);
    public static final String FLIGHT_STATE_DELAYS = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_delays);
    public static final String FLIGHT_STATE_CANCEL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_cancel);
    public static final String FLIGHT_STATE_ALTERNATE_CANCEL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_alternate_cancel);
    public static final String FLIGHT_STATE_RETURN_CANCEL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_return_cancel);
    public static final String FLIGHT_STATE_AHEAD_CANCEL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_state_ahead_cancel);
    public static final String FLIGHT_STATE_ARRIVAL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_date_arrival);
    public static final String FLIGHT_STATE_RETURN_ARRIVAL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_date_return_arrival);
    public static final String FLIGHT_STATE_ALTERNATE_ARRIVAL = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_flight_date_alternate_arrival);
    public static final String NO_TIME_INFO = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_train_unknow_time);
    public static final String POST_TITLE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_post_title);
    public static final String POST_SENDING = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_post_sending);
    public static final String SPLIT_KEY = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_air_train_split);
    /*TCL-162 2016-09-21 linweijie start */
    public static final String SHOW_EXTEND = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_bubble_more_info_show_extend); 
    /*TCL-162 2016-09-21 linweijie end*/
    public static final String NOTIFICATION_FLAG_READ = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_ui_part_notification_flag_read);
    public static void setBodyDefaultBackGroundByTowKey(BusinessSmsMessage message,View bodyRootView){
        if (message==null || bodyRootView ==null) {
            return ;
        }
        String headBackgroundColor = (String)message.getValue("v_hd_bg_1") ;
        String bodyBackGroundColor = (String) message.getValue("v_by_bg_1") ;
        if (StringUtils.isNull(headBackgroundColor)||StringUtils.isNull(bodyBackGroundColor)) {
            ThemeUtil.setViewBg(Constant.getContext(), bodyRootView, "", R.color.duoqu_theme_color_1090);
        }else {
            ThemeUtil.setViewBg(Constant.getContext(), bodyRootView, bodyBackGroundColor , R.color.duoqu_theme_color_1090);
        }
    }
    /**
     * 获取数据依据key
     * 
     * @param data
     * @param jsobj
     * @param key
     * @param type
     * @return
     */
    public static Object getDataByKey(Map<String, String> data, JSONObject jsobj, String key, int type) {
        if (type == 0) {
            // map
            if (data != null) {
                return data.get(key);
            }
        } else {
            // json
            if (jsobj != null && jsobj.has(key)) {
                try {
                    return jsobj.get(key);
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                }
            }
        }
        return "";
    }

    public static String getContentInfo(boolean isWithBlank, String... obj) {
        StringBuffer sb = new StringBuffer();
        if (obj == null || obj.length <= 0)
            return null;
        for (int i = 0; i < obj.length; i++) {
            String item = obj[i];
            if (item != null) {
                item = item.replaceAll(Constant.Delimiter, "").replaceAll("NULL", "");
            }
            if (!StringUtils.isNull(item)) {
                sb.append(item);
                if (isWithBlank) {
                    sb.append(" ");
                }

            }

        }
        return sb.toString();

    }

    public static String getContentInfo(String orgString, String oldStr, String newStr) {
        try {
            orgString = orgString.replaceAll(oldStr, newStr);
            return orgString;

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return "";
    }
    
    public static String getFightStateColor(String mState,BusinessSmsMessage smsMessage) {
        if (StringUtils.isNull(mState)) {
            return null;
        }
        String clr1 = null;
        String clr2 = null;
        String clr3 = null;
        if(smsMessage != null){
            clr1 = (String) smsMessage.getValue("v_hd_text_2");
            clr2 = (String) smsMessage.getValue("v_hd_text_3");
            clr3 = (String) smsMessage.getValue("v_hd_text_4");
        }
        
        if(FLIGHT_STATE_CANCEL.equals(mState)
            ||FLIGHT_STATE_DEPARTURE.equals(mState)
            ||FLIGHT_STATE_RETURN.equals(mState)
            ||FLIGHT_STATE_RETURN_DEPARTURE.equals(mState)
            ||FLIGHT_STATE_ALTERNATE.equals(mState)
            ||FLIGHT_STATE_PLAN_ALTERNATE.equals(mState)){
            if(!StringUtils.isNull(clr1)){
                return clr1;
            }else{
                return "3010";
            }
        }else if(FLIGHT_STATE_DELAYS.equals(mState)
                || FLIGHT_STATE_CANCEL.equals(mState)
                || FLIGHT_STATE_ALTERNATE_CANCEL.equals(mState)
                || FLIGHT_STATE_RETURN_CANCEL.equals(mState)
                || FLIGHT_STATE_AHEAD_CANCEL.equals(mState)){
            if(!StringUtils.isNull(clr1)){
                return clr2;
            }else{
                return "1010";
            }
        }else if(FLIGHT_STATE_ARRIVAL.equals(mState)
                || FLIGHT_STATE_RETURN_ARRIVAL.equals(mState)
                || FLIGHT_STATE_ALTERNATE_ARRIVAL.equals(mState)){
            if(!StringUtils.isNull(clr3)){
                return clr3;
            }else{
                return "5010";
            }
        }
        
        return "3010";
    }
}
