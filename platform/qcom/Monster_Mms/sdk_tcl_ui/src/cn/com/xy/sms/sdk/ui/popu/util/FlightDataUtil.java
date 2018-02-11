package cn.com.xy.sms.sdk.ui.popu.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class FlightDataUtil extends TravelDataUtil {

    private static final String FIGHT = ChannelContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_fight_end);
    private static final String INTERFACE_FLIGHT_ARRIVE_CITY_KEY_START = "db_flight_arrive_city_";
    private static final String INTERFACE_FLIGHT_ARRIVE_DATE_KEY_START = "db_flight_arrive_date_";
    private static final String INTERFACE_FLIGHT_ARRIVE_TIME_KEY_START = "db_flight_arrive_time_";
    private static final String INTERFACE_FLIGHT_DEPART_CITY_KEY_START = "db_flight_depart_city_";
    private static final String INTERFACE_FLIGHT_DEPART_TIME_KEY_START = "db_flight_depart_time_";
    private static final String INTERFACE_FLIGHT_STATE_KEY_START = "db_flight_state_";
    private static final String INTERFACE_FLIGHT_ARRIVE_DATE_TIME_KEY_START = "db_flight_arrive_date_time_";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final SimpleDateFormat YYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final SimpleDateFormat MMDD = new SimpleDateFormat(ChannelContentUtil.TRAIN_DATE_FORMAT);

    private FlightDataUtil() {
        super("db_air_data_index", "flight_data_arr", "flight_data_", "query_time_");
    }

    private static class FlightDataUtilHolder {
        private static FlightDataUtil instance = new FlightDataUtil();
    }

    public static FlightDataUtil getInstance() {
        return FlightDataUtilHolder.instance;
    }

    public void queryFlyData(final BusinessSmsMessage smsMessage, final SdkCallBack callBack) {
        try {
            if (ChannelContentUtil.bubbleDataIsNull(smsMessage) || hasInterfaceData(smsMessage) || isOffNetwork()
                    || isRepeatQuery(smsMessage)) {
                return;
            }
            smsMessage.bubbleJsonObj.put(getQueryTimeKey(smsMessage), System.currentTimeMillis());
            JSONObject viewContentData = getViewContentData(smsMessage);
            if (viewContentData == null) {
                return;
            }
            String flightNum = getFlightNum(viewContentData);
            if (StringUtils.isNull(flightNum)) {
                return;
            }

            SdkCallBack xyCallBack = new SdkCallBack() {
                @Override
                public void execute(final Object... results) {
                    try {
                        if (queryFail(results)) {
                            return;
                        }
                        saveArriveInfo(smsMessage, results);
                        String smsId = results[0].toString();
                        ChannelContentUtil.callBackExecute(callBack, smsId);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("FlightDataUtil queryFlyData error:", e);
                    }
                }

                private void saveArriveInfo(BusinessSmsMessage smsMessage, Object... obj) {
                    try {
                        JSONObject flightData = (JSONObject) obj[1];
                        savaArriveInfo(smsMessage, flightData);
                    } catch (Throwable e) {

                    }
                }

                private boolean queryFail(final Object... results) {
                    return results == null || results.length != 2 || results[0] == null || results[1] == null
                            || !(results[1] instanceof JSONObject);
                }
            };

            String smsId = String.valueOf(smsMessage.getSmsId());
            String departDate = viewContentData.optString("view_depart_date");
            ParseManager.queryFlightData(smsId, flightNum, departDate, getExtend(smsMessage, viewContentData),
                    xyCallBack);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("FlightDataUtil queryFlyData error:", e);
        }
    }

    /* HUAWEI-1324/zhegnxiaobo 2016.07.07 start */
    public void savaArriveInfo(BusinessSmsMessage smsMessage, JSONObject flightData) throws JSONException {
        smsMessage.bubbleJsonObj.put(getInterfaceDataKey(smsMessage), flightData);
        smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveCityKey(smsMessage),
                getCityAndAirportAndHTerminal(smsMessage, flightData, TYPE_ARRIVE_ADRESS));
        smsMessage.bubbleJsonObj.put(getInterfaceFlightDepartCityKey(smsMessage),
                getCityAndAirportAndHTerminal(smsMessage, flightData, TYPE_DEPART_ADRESS));
        smsMessage.bubbleJsonObj.put(getInterfaceFlightStateKey(smsMessage), flightData.opt("FlightState"));

        String arriveDateTimeStr = flightData.optString("FlightArrtimePlanDate");
        Date arriveDateTime = ChannelContentUtil.stringToDate(arriveDateTimeStr, DATE_FORMAT);
        if (arriveDateTime != null) {
            String dateTime = ChannelContentUtil.getFormatDate(arriveDateTime, YYYYMMDDHHMM);
            smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveDateTimeKey(smsMessage), dateTime);
            String[] dateTimeArr = dateTime.split(" ");
            smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveDateKey(smsMessage),
                    ChannelContentUtil.TRAIN_SUPPLEMENT_DATE + ChannelContentUtil.getFormatDate(arriveDateTime, MMDD));
            smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveTimeKey(smsMessage), dateTimeArr[1]);
        }
        String departDateTimeStr = flightData.optString("FlightDeptimePlanDate");
        Date departDateTime = ChannelContentUtil.stringToDate(departDateTimeStr, DATE_FORMAT);
        if (departDateTime != null) {
            String dateTime = ChannelContentUtil.getFormatDate(departDateTime, YYYYMMDDHHMM);
            String[] dateTimeArr = dateTime.split(" ");
            smsMessage.bubbleJsonObj.put(getInterfaceFlightDepartTimeKey(smsMessage), dateTimeArr[1]);
        }
        smsMessage.bubbleJsonObj.put("hasQuery" + getDataIndex(smsMessage), true);
        ParseManager.updateMatchCacheManager(smsMessage);
    }

    /* HUAWEI-1324/zhegnxiaobo 2016.07.07 end */

    private static final int TYPE_DEPART_ADRESS = 1;
    private static final int TYPE_ARRIVE_ADRESS = 2;

    private Object getCityAndAirportAndHTerminal(BusinessSmsMessage smsMessage, JSONObject flightData, int type) {
        JSONObject airData = (JSONObject) getViewContentData(smsMessage);
        String city = "";
        String airport = "";
        String terminal = "";
        String qCity = "";
        String qAirport = "";
        String qTerminal = "";

        switch (type) {
            case TYPE_DEPART_ADRESS:
                if (airData != null) {
                    city = airData.optString("view_depart_city");
                    airport = airData.optString("view_depart_airport");
                    terminal = airData.optString("view_depart_terminal");
                }
                if (flightData != null) {
                    qCity = flightData.optString("FlightDep");
                    qAirport = flightData.optString("FlightDepAirport");
                    qTerminal = flightData.optString("FlightHTerminal");
                }
                break;
            case TYPE_ARRIVE_ADRESS:
                if (airData != null) {
                    city = airData.optString("view_arrive_city");
                    airport = airData.optString("view_arrive_airport");
                    terminal = airData.optString("view_arrive_terminal");
                }
                if (flightData != null) {
                    qCity = flightData.optString("FlightArr");
                    qAirport = flightData.optString("FlightArrAirport");
                    qTerminal = flightData.optString("FlightTerminal");
                }
                break;
            default:
        }

        if (StringUtils.isNull(city)) {
            city = qCity;
        }

        if (StringUtils.isNull(airport)) {
            airport = qAirport;
        }

        if (StringUtils.isNull(terminal)) {
            terminal = qTerminal;
        }

        return getAirPortDetail(city, airport, terminal);
    }

    private Map<String, Object> getExtend(BusinessSmsMessage smsMessage, JSONObject viewContentData) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        String departCity = viewContentData.optString("view_depart_city");
        String arriveCity = viewContentData.optString("view_arrive_city");
        Map<String, Object> extend = new HashMap<String, Object>();
        extend.put("flight_form", departCity);
        extend.put("flight_to", arriveCity);
        extend.put("flight_from_airport", departCity);
        extend.put("flight_to_airport", arriveCity);
        extend.put("phoneNumber", smsMessage.bubbleJsonObj.optString("phoneNum"));
        extend.put("titleNo", smsMessage.getTitleNo());
        extend.put("msgId", String.valueOf(smsMessage.getSmsId()));
        extend.put("bubbleJsonObj", smsMessage.bubbleJsonObj.toString());
        extend.put("messageBody", smsMessage.getMessageBody());
        extend.put("notSaveToDb", Boolean.TRUE.toString());
        return extend;
    }

    public JSONObject getInterfaceData(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optJSONObject(getInterfaceDataKey(smsMessage));
    }

    public String getInterfaceFlightArriveCity(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveCityKey(smsMessage));
    }

    public String getInterfaceFlightArriveCityKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_CITY_KEY_START);
    }

    public String getInterfaceFlightArriveDate(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveDateKey(smsMessage));
    }

    public String getInterfaceFlightArriveDateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_DATE_KEY_START);
    }

    public String getInterfaceFlightArriveTime(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveTimeKey(smsMessage));
    }

    public String getInterfaceFlightArriveTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_TIME_KEY_START);
    }

    public String getInterfaceFlightArriveDateTime(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveDateTimeKey(smsMessage));
    }

    public String getInterfaceFlightArriveDateTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_DATE_TIME_KEY_START);
    }

    public void setViewValue(String value, TextView textView, ImageView lostValueShowImage) {
        TravelDataUtil.setViewValue(value, textView, ChannelContentUtil.NO_DATA_EN, lostValueShowImage);
    }

    public static String getAirPortDetail(String city, String port, String terminal) {
        String returnStr = "";
        returnStr += StringUtils.isNull(city) ? "" : (city + " ");
        if (port != null && port.startsWith(city)) {
            port = port.replace(city, "");
            returnStr += port;
        } else {
            returnStr += StringUtils.isNull(port) ? "" : port;
        }
        returnStr += StringUtils.isNull(terminal) ? "" : terminal;
        return returnStr;
    }

    private static String getFlightNum(JSONObject viewContentData) {
        if (viewContentData == null) {
            return null;
        }
        return TravelDataUtil.getDataByKey(viewContentData, "view_flight_number", FIGHT, " ");
    }

    private static boolean isOffNetwork() {
        return !NetUtil.checkAccessNetWork(2);
    }

    private boolean isRepeatQuery(BusinessSmsMessage smsMessage) {
        return DateUtils.isToday(getQueryTime(smsMessage));
    }

    public String getInterfaceFlightDepartTime(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightDepartTimeKey(smsMessage));
    }

    public String getInterfaceFlightDepartTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_DEPART_TIME_KEY_START);
    }

    public String getInterfaceFlightDepartCity(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightDepartCityKey(smsMessage));
    }

    public String getInterfaceFlightDepartCityKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_DEPART_CITY_KEY_START);
    }

    public String getInterfaceFlightState(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightStateKey(smsMessage));
    }

    public String getInterfaceFlightStateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_STATE_KEY_START);
    }
}
