package cn.com.xy.sms.sdk.ui.popu.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfigInterface;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class TrainDataUtil extends TravelDataUtil {

    private static final String STATION_NAME_KEY = "name";
    private static final String OFF_NETWORK = "offNetwork";
    private static final String INTERFACE_TRAIN_ARRIVE_CITY_KEY_START = "db_train_arrive_city_";
    private static final String INTERFACE_TRAIN_ARRIVE_DATE_KEY_START = "db_train_arrive_date_";
    private static final String INTERFACE_TRAIN_ARRIVE_TIME_KEY_START = "db_train_arrive_time_";
    private static final String INTERFACE_TRAIN_DEPART_TIME_KEY_START = "db_train_depart_time_";
    private static final String NETWORK_STATE_KEY_START = "net_work_state_";
    private static final String INTERFACE_STATION_LIST_KEY = "station_list";

    private TrainDataUtil() {
        super("db_train_data_index", "card_arr", "station_list_", "query_time_");
    }

    private static class TrainDataUtilHolder {
        private static TrainDataUtil instance = new TrainDataUtil();
    }

    public static TrainDataUtil getInstance() {
        return TrainDataUtilHolder.instance;
    }

    /**
     * Query train ticket info
     * 
     */
    @SuppressLint("SimpleDateFormat")
    public void queryTrainStation(final BusinessSmsMessage smsMessage, final SdkCallBack callBack ,final String type ) {
        try {
            if (ChannelContentUtil.bubbleDataIsNull(smsMessage) 
                    ||hasInterfaceData(smsMessage) 
                    || isRepeatQuery(smsMessage)
                    || isOffNetwork(smsMessage)
                    ){
                return;
            }
            smsMessage.bubbleJsonObj.put(getQueryTimeKey(smsMessage), System.currentTimeMillis());
            smsMessage.bubbleJsonObj.put(getNetworkStateKey(smsMessage), null);

            final JSONObject viewContentData = getViewContentData(smsMessage);
            if (viewContentData == null) {
                return;
            }
            final String trainNum = getTrainNum(viewContentData);
            if (StringUtils.isNull(trainNum)) {
                return;
            }
            final Long departDateMills = viewContentData.optLong("view_depart_date_time");
            SdkCallBack xyCallBack = new SdkCallBack() {

                @Override
                public void execute(final Object... results) {
                    if (queryFail(results)) {
                        try {
                            if (hasOffNetworkState(results)) {
                                smsMessage.bubbleJsonObj.put(getNetworkStateKey(smsMessage), OFF_NETWORK);
                                smsMessage.bubbleJsonObj.put(getQueryTimeKey(smsMessage), null);
                            }else if (isTimeout(results)) {
                                smsMessage.bubbleJsonObj.put(getQueryTimeKey(smsMessage), null);
                            }
                        } catch (Throwable ex) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil queryTrainStation error:", ex);
                        }
                        return;
                    }
                    try {
                        JSONArray stationInfoJson = buildTrainInfo(smsMessage, (JSONObject) results[1], departDateMills, type);
                        cacheTrainInfo(trainNum,(JSONObject) results[1]);
                        String smsId = (String) results[0];
                        ChannelContentUtil.callBackExecute(callBack, smsId, stationInfoJson);
                    } catch (Throwable ex) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil queryTrainStation error:", ex);
                    }
                }

                private boolean hasOffNetworkState(final Object... results) {
                    return results != null && results.length > 0 && results[0] != null
                            && OFF_NETWORK.equalsIgnoreCase(results[0].toString());
                }

                private boolean queryFail(final Object... results) {
                    boolean result = false ;
                    if (results == null || results.length != 6 || results[0] == null || results[1] == null
                            || !(results[1] instanceof JSONObject)) {
                        result = true ;
                    }else {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.PATTERN);
                        long departTime = viewContentData.optLong("view_depart_date_time");
                        String departDate = ContentUtil.getFormatDate(new Date(departTime), simpleDateFormat);
                        String compleDepartDateString;
                        try {
                            compleDepartDateString = ((JSONObject) results[1]).getString("day");
                            Date compleDepartDate = simpleDateFormat.parse(compleDepartDateString);
                            if (compleDepartDate.compareTo(simpleDateFormat.parse(departDate)) != 0) {
                                result = true;
                            }
                        } catch (Exception e) {
                        }
                    }
                    return result;
                }
                
                private boolean isTimeout(final Object... results) {
                    return results != null && results.length > 0 && results[0] == null;
                }
            };

            String departCity = viewContentData.optString("view_depart_city");
            String arriveCity = viewContentData.optString("view_arrive_city");
            long departTime = viewContentData.optLong("view_depart_date_time") ;
            String formatDate = ContentUtil.getFormatDate(new Date(departTime), new SimpleDateFormat("yyyy-MM-dd"));
            
            String smsId = String.valueOf(smsMessage.getSmsId());
            JSONObject cacheObject = getTrainInfoByNumner(trainNum,formatDate);
            if(cacheObject != null){
            	 JSONArray stationInfoJson = buildTrainInfo(smsMessage, cacheObject, departDateMills, type);
                 ChannelContentUtil.callBackExecute(callBack, smsId, stationInfoJson);
            }else{
                ParseManager.queryTrainInfo(smsId, trainNum, departCity, arriveCity, getExtend(smsMessage, smsId,formatDate),
                        xyCallBack);
            }
        } catch (Throwable ex) {
             SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil queryTrainStation error:", ex);
        }
    }
    /**
     * filter station info
     * 
     */
    public static JSONObject stationFilter(final JSONArray stationInfoJsonArray, final String departName) {
        if (stationInfoJsonArray == null || stationInfoJsonArray.length() == 0) {
            return null;
        }
        JSONObject result = null ;
        int len = stationInfoJsonArray.length();
        try {
            for (int i = 0; i < len; i++) {
                String stationName = stationInfoJsonArray.getJSONObject(i).optString(STATION_NAME_KEY);
                if (stationName.equalsIgnoreCase(departName)) {
                    result = stationInfoJsonArray.getJSONObject(i);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("ThemeUtil stationFilter error:", e);
        }
        return result;
    }
    public static JSONArray stationFilter(final JSONArray stationInfoJsonArray, final String departName,
            final String arriveName) {
        if (stationInfoJsonArray == null || stationInfoJsonArray.length() == 0) {
            return null;
        }
        JSONArray result = new JSONArray();
        boolean addStationInfo = false;
        int len = stationInfoJsonArray.length();
        try {
            for (int i = 0; i < len; i++) {
                String stationName = stationInfoJsonArray.getJSONObject(i).optString(STATION_NAME_KEY);
                if (addStationInfo) {
                    result.put(stationInfoJsonArray.getJSONObject(i));
                }
                if (stationName.equalsIgnoreCase(departName)) {
                    addStationInfo = true;
                }
                if (stationName.equalsIgnoreCase(arriveName)) {
                    break;
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil stationFilter error:", e);
        }
        return result;
    }
    public JSONObject getInterfaceData(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optJSONObject(getInterfaceDataKey(smsMessage));
    }

    public int getDefaultStationSelectedIndex(BusinessSmsMessage smsMessage) {
        int defaultSelectedIndex = 0;
        try {
            defaultSelectedIndex = Integer.parseInt(getDataIndex(smsMessage));
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil getDefaultStationSelectedIndex error:", e);
        }
        return defaultSelectedIndex;
    }

    public void saveSelectedStationData(BusinessSmsMessage smsMessage, String stationName, String arriveTime) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return;
        }
        try {
            smsMessage.bubbleJsonObj.put(getInterfaceTrainArriveCityKey(smsMessage), stationName);
            smsMessage.bubbleJsonObj.put(getInterfaceTrainArriveTimeKey(smsMessage), arriveTime);
            ParseManager.updateMatchCacheManager(smsMessage);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil saveSelectedStationData error:", e);
        }
    }

    public String getTrainNum(JSONObject viewContentData) {
        if (viewContentData == null) {
            return null;
        }
        return viewContentData.optString(viewContentData.has("view_m_trainnumber") ? "view_m_trainnumber" : "view_train_number");
    }

    public String getInterfaceTrainArriveCity(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainArriveCityKey(smsMessage));
    }

    public String getInterfaceTrainArriveCityKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_CITY_KEY_START);
    }

    private JSONObject getFilterStationData(BusinessSmsMessage smsMessage, JSONArray interfaceData,String key ) {
        if (interfaceData == null || ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        JSONObject viewContentData = getViewContentData(smsMessage);
        if (viewContentData == null) {
            return null;
        }
        String stc = viewContentData.optString(key);
        return TrainDataUtil.stationFilter(interfaceData, stc);
    }
    private JSONArray getFilterStationData(BusinessSmsMessage smsMessage, JSONArray interfaceData) {
        if (interfaceData == null || ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        JSONObject viewContentData = getViewContentData(smsMessage);
        if (viewContentData == null) {
            return null;
        }
        String departCity = viewContentData.optString("view_depart_city");
        String arriveCity = viewContentData.optString("view_arrive_city");
        return TrainDataUtil.stationFilter(interfaceData, departCity, arriveCity);
    }
    private String getNetworkState(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getNetworkStateKey(smsMessage));
    }

    private String getNetworkStateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, NETWORK_STATE_KEY_START);
    }

    private Map<String, Object> getExtend(BusinessSmsMessage smsMessage, String smsId, String formatDate) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        Map<String, Object> extend = new HashMap<String, Object>();
        extend.put("phoneNumber", smsMessage.bubbleJsonObj.optString("phoneNum"));
        extend.put("titleNo", smsMessage.getTitleNo());
        extend.put("msgId", smsId);
        extend.put("bubbleJsonObj", smsMessage.bubbleJsonObj.toString());
        extend.put("messageBody", smsMessage.getMessageBody());
        extend.put("notSaveToDb", Boolean.TRUE.toString());
        extend.put("day", formatDate); 
        return extend;
    }

    private boolean isOffNetwork(BusinessSmsMessage smsMessage) {
        return !NetUtil.checkAccessNetWork(2)
                && OFF_NETWORK.equalsIgnoreCase(getNetworkState(smsMessage));
    }

    public boolean isRepeatQuery(BusinessSmsMessage smsMessage) {
        return DateUtils.isToday(getQueryTime(smsMessage))
                && !OFF_NETWORK.equalsIgnoreCase(getNetworkState(smsMessage));
    }

    public String getInterfaceTrainDepartTime(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainDepartTimeKey(smsMessage));
    }
    public String getInterfaceTrainDepartTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_DEPART_TIME_KEY_START);
    }
    public String getInterfaceTrainArriveDate(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainArriveDateKey(smsMessage));
    }
    public String getInterfaceTrainArriveDateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_DATE_KEY_START);
    }
    public String getInterfaceTrainArriveTime(BusinessSmsMessage smsMessage) {
        if (ChannelContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainArriveTimeKey(smsMessage));
    
    }
    public String getInterfaceTrainArriveTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_TIME_KEY_START);
    }

    private  JSONArray buildTrainInfo(BusinessSmsMessage smsMessage,JSONObject data, long departDateMills, String type) throws JSONException{
    	if(data == null){
    		return null;
    	}
    	
        String stationInfoStr = data.optString(INTERFACE_STATION_LIST_KEY);
        if (StringUtils.isNull(stationInfoStr)) {
            return null;
        }
      
        JSONObject trainDepart = getFilterStationData(smsMessage, new JSONArray(stationInfoStr),"view_depart_city");
        String trainDepart_time = null;
        if(trainDepart!=null){
            trainDepart_time = (String) trainDepart.opt("travel_time");
            smsMessage.bubbleJsonObj.put(getInterfaceTrainDepartTimeKey(smsMessage), trainDepart.opt("stt"));
        }
        JSONObject trainArrive = getFilterStationData(smsMessage, new JSONArray(stationInfoStr),"view_arrive_city");
        String trainArrive_time = null;
        if(trainArrive!=null){
            trainArrive_time = (String) trainArrive.opt("travel_time");
            smsMessage.bubbleJsonObj.put(getInterfaceTrainArriveTimeKey(smsMessage), trainArrive.opt("spt"));
        }
        if(!StringUtils.isNull(trainArrive_time)){
            Date date = new Date(departDateMills
                    + (timeStrTolong(trainArrive_time) - timeStrTolong(trainDepart_time)));
            SimpleDateFormat sdf = new SimpleDateFormat(ChannelContentUtil.TRAIN_DATE_FORMAT);
            smsMessage.bubbleJsonObj.put(getInterfaceTrainArriveDateKey(smsMessage), ChannelContentUtil.TRAIN_SUPPLEMENT_DATE+sdf.format(date));
        }
        smsMessage.bubbleJsonObj.put("hasQuery"+getDataIndex(smsMessage),true);
        smsMessage.bubbleJsonObj.put("supplementType"+getDataIndex(smsMessage), type);
        JSONArray stationInfoJson = getFilterStationData(smsMessage, new JSONArray(stationInfoStr));
        if (stationInfoJson != null) {
            smsMessage.bubbleJsonObj.put(getInterfaceDataKey(smsMessage), stationInfoJson);
        }
        ParseManager.updateMatchCacheManager(smsMessage);
    	return stationInfoJson;	
    }
    
    private static final long TRAIN_DATA_EXPIRE = 7 * 24 * 3600 * 1000;
    private static HashMap<String,JSONObject> sTrainStationCache = new HashMap<String, JSONObject>(5);
    
    private static JSONObject getTrainInfoByNumner(String trainNumber,String formatDate){
        if(sTrainStationCache != null && sTrainStationCache.containsKey(trainNumber)){
            JSONObject data = sTrainStationCache.get(trainNumber);
            Date cacheDepartDate = null;
            Date cacheCompleDepartDate = null ;
            try {
                String cacheDepartDateStirng = data.getString("day");
                cacheCompleDepartDate = new SimpleDateFormat(Constant.PATTERN).parse(cacheDepartDateStirng) ;
                cacheDepartDate = new SimpleDateFormat(Constant.PATTERN).parse(formatDate);
            } catch (Exception e) {
                return null ;
            }
            if (data != null && !needQueryNetTrainInfo(data.optLong("data_time")) && cacheCompleDepartDate != null
                    && cacheDepartDate != null && (cacheCompleDepartDate.compareTo(cacheDepartDate) == 0)) {
                return data;
            }
        }
        return null;
    }
    
    private static void cacheTrainInfo(String trainNumber,JSONObject data){
    	if(TextUtils.isEmpty(trainNumber) || data == null){
    		return;
    	}else{
    		if(sTrainStationCache == null){
    			sTrainStationCache = new HashMap<String, JSONObject>(5);
    		}else if(sTrainStationCache.size() > 100){
    			sTrainStationCache.clear();
    		}
    		
    		sTrainStationCache.put(trainNumber,data);
    	}
    }
    
    
    private static boolean needQueryNetTrainInfo(long dateTime) {
        return System.currentTimeMillis() - dateTime > DexUtil.getUpdateCycleByType(OnlineUpdateCycleConfigInterface.TYPE_TRAIN_DATA_VALID_CYCLE, TRAIN_DATA_EXPIRE);
    }
    
}
