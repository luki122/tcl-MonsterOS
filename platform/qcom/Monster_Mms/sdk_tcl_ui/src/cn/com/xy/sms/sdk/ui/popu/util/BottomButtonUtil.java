package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;

public class BottomButtonUtil {
    private static final String TAG = "BottomButtonUtil";
    public static final String PACKAGE_NAME_KEY = "packageName";
    public static final String EMPTY_GROUP = "EMPTY_GROUP";
    public static final String TIME_EX = "TIME";
    public static final long BUTTON_TIME_CYC = 5 * 60 * 1000L;// 按钮数据超时时间
    
    public static JSONArray getActionArrayData(Context context, BusinessSmsMessage message, String groupKey, int count,
            Map<String, Object> extend) {
        return getActionArrayData(context,
                getAdAction(message, groupKey, count, extend));
    }
    
    public static JSONArray getActionArrayData(Context context, JSONArray adAction) {
        try {
            JSONArray jsonArray = new JSONArray();
            if (adAction == null) {
                return jsonArray;
            }
            int lengh = adAction.length();
            if (lengh == 0) {
                return jsonArray;
            }
            if (lengh == 1 && !adAction.getJSONObject(0).has(PACKAGE_NAME_KEY)) {
                return adAction;
            }
            if (!adAction.toString().contains(PACKAGE_NAME_KEY)) {
                return adAction;
            }
            for (int i = 0; i < lengh; i++) {
                JSONObject object = (JSONObject) adAction.get(i);
                if (!object.has(PACKAGE_NAME_KEY)) {
                    jsonArray.put(object);
                    continue;
                }
                String appName = object.optString(PACKAGE_NAME_KEY);
                if (StringUtils.isNull(appName)) {
                    jsonArray.put(object);
                    continue;
                }
                if (DuoquUtils.getSdkDoAction().checkHasAppName(context, appName)) {
                    jsonArray.put(object);
                }
            }

            return jsonArray;
        } catch (Throwable e) {
            e.printStackTrace();
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }
    
    public static JSONArray getActionArrayData(Context context, String adAction, BusinessSmsMessage message) {
        try {
            JSONArray jsonArray = null;
            if (!StringUtils.isNull(adAction)) {
                jsonArray = new JSONArray(adAction);
            }
            return jsonArray;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }

    public static void setButtonTextAndImg(TextView buttonText, String action, boolean disLogo) {
        try {
            String buttonName = buttonText.getText().toString();
            boolean setText = StringUtils.isNull(buttonName);

            int resLogoId = SimpleButtonUtil.bindButtonData(buttonText, action, setText, true);

            if (disLogo && resLogoId != -1) {
                Drawable dw = Constant.getContext().getResources().getDrawable(resLogoId);
                buttonText.setCompoundDrawablesWithIntrinsicBounds(dw, null, null, null);
            } else {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

    }

    public static void setBotton(View button, final TextView buttonText, final JSONObject actionMap, boolean disLogo,
            final Activity mContext, final BusinessSmsMessage message) {
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // do action
                    JSONObject jsonObject = (JSONObject) v.getTag();
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("simIndex", message.simIndex + "");
                    valueMap.put("phoneNum", message.originatingAddress + "");
                    valueMap.put("content", message.getMessageBody() + "");
                    byte viewType = message.viewType;
                    valueMap.put("viewType", viewType + "");
                    String msgId = message.getExtendParamValue("msgId") + "";
                    valueMap.put("msgId", msgId);
                    JsonUtil.putJsonToMap(jsonObject, valueMap);
                    String action_data = (String) JsonUtil.getValueFromJsonObject(jsonObject, "action_data");
                    DuoquUtils.doAction(mContext, action_data, valueMap);
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                }

            }

        };

        if (actionMap != null) {
            if (!disLogo) {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
            final String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
            String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap, "btn_name");
            if (!StringUtils.isNull(btnName)) {
                buttonText.setText(btnName);
                BottomButtonUtil.setButtonTextAndImg(buttonText, action, disLogo);
            }
            if (!StringUtils.isNull(action)) {
                button.setTag(actionMap);
                button.setOnClickListener(onClickListener);
            }
        }
        ViewManger.setRippleDrawable(button);
    }

    /* TOSII-145 mobaohua 2016-04-25 start */
    /**
     * 返回adacion数组
     * 
     * @param context
     * @param adAction
     *            json数组字符串
     * @param message
     * @param count
     *            需要的按钮个数,-1会返回所有符合条件的按钮数据。大于0的话就会获取指定的个数的按钮
     * @param extend
     * @return
     */
    public static JSONArray getAdAction(BusinessSmsMessage message, String groupKey, int count,
            Map<String, Object> extend) {

        try {
            if (message == null)
                return null;

            boolean isEmptyGroupKey = false;
            if (StringUtils.isNull(groupKey)) {
                isEmptyGroupKey = true;
            }
            JSONArray cacheArray = null;
            if (isEmptyGroupKey) {
                cacheArray = getAdActionFromCache(message, EMPTY_GROUP, extend);
            } else {
                cacheArray = getAdActionFromCache(message, groupKey, extend);
            }
            if (cacheArray != null) {
                SmartSmsSdkUtil.smartSdkExceptionLog("getAdAction  groupKey=" + groupKey + "objAction=" + cacheArray, null);
                return cacheArray;
            }

            String objAction = getAdAction(message, extend);
            if (StringUtils.isNull(objAction)) {
                return null;
            }
            SmartSmsSdkUtil.smartSdkExceptionLog("getAdAction groupKey=" + groupKey + "objAction=" + objAction, null);

            JSONArray jsonArray = new JSONArray(objAction);
            if (jsonArray == null || jsonArray.length() <= 0)
                return null;
            JSONArray tempJsonArr = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = getButtonItem(jsonArray.optJSONObject(i), groupKey, extend);
                if (tempObject != null) {
                    tempJsonArr.put(tempObject);
                }

                // 当按钮个数达到需要个数时，结束循环
                if (count > 0 && tempJsonArr.length() >= count) {
                    break;
                }
            }
            if (isEmptyGroupKey) {
                message.putValue(EMPTY_GROUP, tempJsonArr);
                message.putValue(EMPTY_GROUP + TIME_EX, System.currentTimeMillis());
            } else {
                message.putValue(groupKey, tempJsonArr);
                message.putValue(groupKey + TIME_EX, System.currentTimeMillis());
            }
            ParseManager.updateMatchCacheManager(message);
            return tempJsonArr;

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("getAdAction:" + e.getMessage(), e);
        }
        return null;
    }

     public static JSONArray getAdAction(JSONArray jsonArray,String groupKey,int count,Map<String, Object> extend){
         if(jsonArray==null||jsonArray.length() <= 0) return null;           
         JSONArray tempJsonArr = new JSONArray();
         for(int i=0;i<jsonArray.length();i++){
             JSONObject tempObject = getButtonItem(jsonArray.optJSONObject(i),groupKey,extend);
             if(tempObject!=null){
                 tempJsonArr.put(tempObject);
             }
           
             if(count>0 && tempJsonArr.length() >=count){
                 break;
             }
         }      
         return tempJsonArr;
    }

    public static JSONObject getButtonItem(JSONObject tempObject, String groupKey, Map<String, Object> extend) {
        if (tempObject == null)
            return null;
        String groupValue = tempObject.optString("groupValue");
        // 假如包含groupKey字段，则需要判断groupKey是对应的，否则不用处理
        /*
         * 1.当是流量充值界面：groupKey会为null，按钮数据{actionName:流量充值,....}
         * 那么这个按钮符合要求，会进入是否在时间轴里的判断。
         * 
         * 2.当在飞机票界面：groupKey
         * G201，按钮数据{actionName:酒店预订,....(这种和航班号无关的按钮，不会有groupValue)}
         * 所以这个按钮符合要求，会进入是否在时间轴里的判断。
         * 
         * groupKey和groupValue只在火车票和飞机票下拉选择才会有意义。
         */
        if (!StringUtils.isNull(groupKey) && !StringUtils.isNull(groupValue) && !groupKey.equals(groupValue)) {
            return null;
        }
        // groupKey是对应的，并且在时间轴内，则把对应数据放到子集中
        if (isBetweenTime(tempObject.optLong("sTime"), tempObject.optLong("eTime"))) {
            return tempObject;
        }
        return null;
    }

    private static boolean isBetweenTime(long startTime, long endTime) {
        if (startTime == 0 && endTime == 0)
            return true;
        long now = System.currentTimeMillis();
        if (startTime == 0) {
            return (now < endTime);
        } else if (endTime == 0) {
            return (now >= startTime);
        } else {
            return (now >= startTime && now < endTime);
        }
    }

    public static String getAdAction(BusinessSmsMessage message, Map<String, Object> extend) {
        try {
            if (message == null)
                return "";
            Object isUseNewAction = null;
            Object newAction = "";
            if (extend != null) {
                isUseNewAction = extend.get("isUseNewAction");
            }
            newAction = message.getValue("NEW_ADACTION");
            if (
            // isUseNewAction != null
            // && "true".equalsIgnoreCase((String) isUseNewAction)
            // &&
            null != newAction && !StringUtils.isNull(newAction.toString())) {
                return (String) newAction;
            } else {
                return (String) message.getValue("ADACTION");
            }

        } catch (Throwable e) {
            // TODO: handle exception
        }
        return "";
    }

    private static JSONArray getAdActionFromCache(BusinessSmsMessage message, String groupKey,
            Map<String, Object> extend) {
        Object time = message.getValue(groupKey + TIME_EX);
        if (time == null)
            return null;
        long lastUpdateTime = (Long) time;
        long currentTime = System.currentTimeMillis();
        long currenttimeEx = currentTime - BUTTON_TIME_CYC;
        
        if (lastUpdateTime == 0 
                || (currentTime < lastUpdateTime)
                || (currenttimeEx > lastUpdateTime)) {
            return null;
        } else {
            return (JSONArray) message.getValue(groupKey);
        }

    }
    /* TOSII-145 mobaohua 2016-04-25 end */

    public static String getFirstGroupValue(JSONArray jsonArray){
       if(jsonArray==null || jsonArray.length() <=0) return "";
       try {
           for(int i=0;i< jsonArray.length();i++){
               JSONObject tempObject = jsonArray.optJSONObject(i);
               if(tempObject == null){
                   continue;
               }
               String groupValue = tempObject.optString("groupValue");
               if(!StringUtils.isNull(groupValue)){
                   return groupValue;
               }
           }
       } catch (Exception e) {
           SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);             
       }       
       return "";
   }
}
