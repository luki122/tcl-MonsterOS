package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import cn.com.xy.sms.util.ParseNotificationManager;

public class SmartSmsNotificationManager {

    /**
     * 调用生成通知栏接口
     * */
    public static boolean callApiToNotification(Context context, long msgId, String phoneNum, String smsCenterNum,
            String msg, long smsReceiveTime, Map<String, String> extendMap) {
        try {
            // 调用解析api
            Map<String, Object> valueMap = ParseNotificationManager.parseNotificationMsg(context, msgId, phoneNum,
                    smsCenterNum, msg, extendMap);

            boolean isCreate = false;
            if (valueMap != null && valueMap.containsKey("Result")) {
                isCreate = (Boolean) valueMap.get("Result");
            }
            if (isCreate) {
//                DuoquNotificationViewManager.createNotification(context, msgId, phoneNum, msg, smsReceiveTime,
//                        valueMap, extendMap);
//                return true;
                  return SmartNewNotificationManager.bindSmartNotifyView(context,valueMap,msgId,phoneNum,msg,mapToHashMap(extendMap));
            } else {
                return false;
            }
        } catch (Throwable e) {
            cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog(
                    "SmartSmsNotificationManager callApiToNotification error:", e);
        }
        return false;
    }
    
    private  static HashMap<String, String> mapToHashMap(Map<String, String> map ){
        if (map instanceof HashMap ) {
            return ( HashMap<String, String>) map ;
        }else {
            HashMap<String, String> hashMap = new HashMap<String, String>() ;
            Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, String> entry =  it.next();
                hashMap.put((String)entry.getKey(), (String)entry.getValue()) ;
            }
            return hashMap ;
        }
            
    }

    @SuppressLint("NewApi")
    public static boolean bindSmartNotifyView(Context context, Notification notification, Bitmap avatar,
            Map<String, Object> smartResultMap, String msgId, String phoneNum, String msg,
            HashMap<String, String> extend) {
        if (smartResultMap == null) {
            return false;
        }

        try {
            notification.contentView = SmartSmsNotificationManager.getFloatContentView(context, phoneNum, msg, msgId,
                    smartResultMap, avatar, extend);
            notification.bigContentView = SmartSmsNotificationManager.getBigContentView(context, phoneNum, msg, msgId,
                    smartResultMap, avatar, extend);
            if (notification.contentView != null && notification.bigContentView != null) {
                return true;
            }
        } catch (Throwable e) {
            cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog(
                    "SmartSmsNotificationManager bindSmartNotifyView error:", e);
        }
        return false;
    }

    @SuppressLint("NewApi")
    public static boolean bindSmartNotityDropContentView(Context context, Notification notification, Bitmap avatar,
            Map<String, Object> smartResultMap, String msgId, String phoneNum, String msg,
            HashMap<String, String> extend) {
        try {
            if (smartResultMap != null) {
                notification.contentView = SmartSmsNotificationManager.getContentView(context, phoneNum, msg, msgId,
                        smartResultMap, avatar, extend);
                notification.bigContentView = SmartSmsNotificationManager.getBigContentView(context, phoneNum, msg,
                        msgId, smartResultMap, avatar, extend);
                if (notification.contentView != null && notification.bigContentView != null) {
                    return true;
                }
            }
        } catch (Throwable e) {
            cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog(
                    "SmartSmsNotificationManager bindSmartNotityDropContentView error:", e);
        }
        return false;
    }

    /**
     * get duoqu contentView
     * 
     * @param context
     * @param number
     * @param msg
     * @return
     */
    public static RemoteViews getFloatContentView(Context context, String number, String msg, String msgId,
            Map<String, Object> valueMap, Bitmap avatar, HashMap<String, String> extend) {
        return DuoquNotificationViewManager.getPopupNotificationView(context, Long.valueOf(msgId), number, msgId, 0,
                valueMap, extend);
    }

    /**
     * get duoqu contentView
     * 
     * @param context
     * @param number
     * @param msg
     * @return
     */
    public static RemoteViews getContentView(Context context, String number, String msg, String msgId,
            Map<String, Object> valueMap, Bitmap avatar, HashMap<String, String> extend) {
        return DuoquNotificationViewManager.getDropNotificationSmallView(context, Long.valueOf(msgId), number, msgId,
                0, valueMap, extend);
    }

    /**
     * get duoqu bigContentView
     * 
     * @param context
     * @param number
     * @param msg
     * @return
     */
    public static RemoteViews getBigContentView(Context context, String number, String msg, String msgId,
            Map<String, Object> valueMap, Bitmap avatar, HashMap<String, String> extend) {
        return DuoquNotificationViewManager.getDropNotificationBigView(context, Long.valueOf(msgId), number, msg, 0,
                valueMap, extend);
    }
}
