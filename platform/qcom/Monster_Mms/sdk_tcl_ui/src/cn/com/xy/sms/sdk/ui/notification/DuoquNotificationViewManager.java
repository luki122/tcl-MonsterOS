package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class DuoquNotificationViewManager {

    private static NotificationManager mNotifyManager = null;
    public static final String DUOQU_NOTIFICATION_DOACTION_COMPLETE_0 = "com.xy.sms.ui.notification.fristServiceButtonClickAction";
    public static final String DUOQU_NOTIFICATION_DOACTION_COMPLETE_1 = "com.xy.sms.ui.notification.secondServiceButtonClickAction";
    public static final String DUOQU_NOTIFICATION_DOACTION_ROOT_LAYOUT = "com.xy.sms.ui.notification.layoutClickAction";
    public static final String DUOQU_NOTIFICATION_DOACTION_HAS_READ = "com.xy.sms.ui.notification.hasReadButtonClickAction";

    private static boolean checkVaildData(Context context, long msgId,
            String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend) {
        if (context == null) {
            return false;
        }
        if (msgId == 0 || StringUtils.isNull(phoneNum)
                || StringUtils.isNull(msg) || resultMap == null
                || resultMap.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean createNotification(Context context, long msgId,
            String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend) {
        try {
            if (!checkVaildData(context, msgId, phoneNum, msg, smsReceiveTime,
                    resultMap, extend)) {
                return false;
            }
            // bigContentView + headsUpContentView
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(
                            R.drawable.duoqu_notification_default_small_icon)
                    .setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX).build();

            notification.headsUpContentView = getPopupNotificationView(context,
                    msgId, phoneNum, msg, smsReceiveTime, resultMap, extend,
                    false);
            /*QIK-499/yangzhi/2016.04.07---start---*/
            notification.bigContentView = getPopupNotificationView(context,
                    msgId, phoneNum, msg, smsReceiveTime, resultMap, extend,
                    false);
            /*QIK-499/yangzhi/2016.04.07---end---*/
            notification.contentView = getDropNotificationSmallView(context,
                    msgId, phoneNum, msg, smsReceiveTime, resultMap, extend,
                    false);
            if (notification.contentView == null
                    || notification.headsUpContentView == null) {
                return false;
            }
            getNotificationManager(context).notify(
                    Integer.parseInt(String.valueOf(msgId)), notification);
            return true;
        } catch (Throwable e) {
            cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager createNotification error:", e);
            return false;
        }
    }

    public static RemoteViews getPopupNotificationView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend) {
        return getPopupNotificationView(context, msgId, phoneNum, msg,
                smsReceiveTime, resultMap, extend, true);
    }

    private static RemoteViews getPopupNotificationView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend,
            boolean needCheckData) {

        if (needCheckData
                && !checkVaildData(context, msgId, phoneNum, msg,
                        smsReceiveTime, resultMap, extend)) {
            return null;
        }
        String mTitle = (String) resultMap.get("view_content_title");
        String mText = (String) resultMap.get("view_content_text");

        if (StringUtils.isNull(mTitle)) {
            return null;
        }
        if (StringUtils.isNull(mText)) {
            mText = msg.trim();
        }
        LogManager.i("getPopupNotificationView", "mTitle---" + mTitle
                + ", mText---" + mText);

        MessageItem msgItem = getMessage(getMsgIdToInt(msgId), phoneNum, msg,
                smsReceiveTime, resultMap, extend);
        PopupNotificationView popupView = new PopupNotificationView();
        RemoteViews remoteView = popupView.getRemoteViews(context);
        // String defaultBtnName =
        // context.getResources().getString(R.string.duoqu_has_read);
        // String rightBtnName =
        // context.getResources().getString(R.string.duoqu_delete);
        popupView
                .bindViewData(
                        context,
                        ((BitmapDrawable) getLogoDrawable(context, phoneNum,
                                resultMap)).getBitmap(), mTitle, mText,
                        getAdAction(resultMap), msgItem);
        return remoteView;
    }

    public static RemoteViews getDropNotificationSmallView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend) {
        return getDropNotificationSmallView(context, msgId, phoneNum, msg,
                smsReceiveTime, resultMap, extend, true);
    }

    private static RemoteViews getDropNotificationSmallView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend,
            boolean needCheckData) {
        if (needCheckData
                && !checkVaildData(context, msgId, phoneNum, msg,
                        smsReceiveTime, resultMap, extend)) {
            return null;
        }
        String mTitle = (String) resultMap.get("view_content_title");
        String mText = (String) resultMap.get("view_content_text");

        if (StringUtils.isNull(mTitle)) {
            return null;
        }
        if (StringUtils.isNull(mText)) {
            mText = msg.trim();
        }
        LogManager.i("getDropNotificationView", "mTitle---" + mTitle
                + ", mText---" + mText);

        MessageItem msgItem = getMessage(getMsgIdToInt(msgId), phoneNum, msg,
                smsReceiveTime, resultMap, extend);
        DropNotificationView dropView = new DropNotificationView();

        RemoteViews remoteView = dropView.getRemoteViews(context);
        /* QIK-585/yangzhi/2016.06.30---start--- */
        dropView.bindViewData(context, ((BitmapDrawable) getLogoDrawable(context, phoneNum, resultMap)).getBitmap(),
                mTitle, mText, getAdAction(resultMap), msgItem);
        /* QIK-585/yangzhi/2016.06.30---end--- */

        return remoteView;
    }

    private static RemoteViews getDropNotificationView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend) {
        String mTitle = (String) resultMap.get("view_content_title");
        String mText = (String) resultMap.get("view_content_text");

        if (StringUtils.isNull(mTitle)) {
            return null;
        }
        if (StringUtils.isNull(mText)) {
            mText = msg.trim();
        }
        LogManager.i("getDropNotificationView", "mTitle---" + mTitle
                + ", mText---" + mText);

        MessageItem msgItem = getMessage(getMsgIdToInt(msgId), phoneNum, msg,
                smsReceiveTime, resultMap, extend);
        DropNotificationView dropView = new DropNotificationView();

        RemoteViews remoteView = dropView.getRemoteViews(context);
        // String defaultBtnName =
        // context.getResources().getString(R.string.duoqu_has_read);
        // String rightBtnName =
        // context.getResources().getString(R.string.duoqu_delete);
        dropView.bindViewData(
                context,
                ((BitmapDrawable) getLogoDrawable(context, phoneNum, resultMap))
                        .getBitmap(), mTitle, mText, getAdAction(resultMap),
                msgItem);

        return remoteView;
    }

    public static RemoteViews getDropNotificationBigView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend) {
        return getDropNotificationBigView(context, msgId, phoneNum, msg,
                smsReceiveTime, resultMap, extend, true);
    }

    private static RemoteViews getDropNotificationBigView(Context context,
            long msgId, String phoneNum, String msg, long smsReceiveTime,
            Map<String, Object> resultMap, Map<String, String> extend,
            boolean needCheckData) {
        if (needCheckData
                && !checkVaildData(context, msgId, phoneNum, msg,
                        smsReceiveTime, resultMap, extend)) {
            return null;
        }
        return getDropNotificationView(context, msgId, phoneNum, msg,
                smsReceiveTime, resultMap, extend);

    }

    private static MessageItem getMessage(int msgId, String phoneNum,
            String msg, long smsReceiveTime, Map<String, Object> resultMap,
            Map<String, String> extend) {

        MessageItem msgItem = new MessageItem();
        msgItem.mMsgId = msgId;
        msgItem.mPhoneNum = phoneNum;
        msgItem.mMsg = msg;
        msgItem.mExtend = extend;
        msgItem.mMap = resultMap;
        return msgItem;

    }

    public static int getMsgIdToInt(Long id) {
        if (id == null) {
            return 0;
        }
        return id.intValue();
    }

    public static Drawable getLogoDrawable(Context context, String phoneNum,
            Map<String, Object> resultMap) {
        Drawable logoDrawable = context.getResources().getDrawable(
                R.drawable.duoqu_notification_default_small_icon);
        return logoDrawable;
    }

    private static JSONArray getAdAction(Map<String, Object> map) {
        JSONArray result = null;
        try {
            String newAdAction = (String) map.get("NEW_ADACTION");
            if (!StringUtils.isNull(newAdAction)) {
                result = getNewAdAction(newAdAction);
            }else {
                String adAction = (String) map.get("ADACTION");
                if(!StringUtils.isNull(newAdAction)){
                    result = new JSONArray(adAction);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager getAdAction error:", e);
        }
        
        return result;
    }

    private static JSONArray getNewAdAction(String newAdAction) {
        try {
            JSONArray newAction = new JSONArray(newAdAction);
            if (newAction == null || newAction.length() <= 0){
                return null;
            }
            JSONArray tempJsonArr = new JSONArray();
            String groupvalue = getFirstGroupValue(newAction);
            for (int i = 0; i < newAction.length(); i++) {
                JSONObject tempObject = getButtonItem(newAction.optJSONObject(i),groupvalue);
                if (tempObject != null) {
                    tempJsonArr.put(tempObject);
                }
                if (tempJsonArr.length() >= 2) {
                    break;
                }
            }
            return tempJsonArr;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager getNewAdAction error:", e);
        }
        return null;
    }

    private static JSONObject getButtonItem(JSONObject optJSONObject, String groupKey) {
        if (optJSONObject == null){
            return null;
        }
        String groupValue = optJSONObject.optString("groupValue");
        if (!StringUtils.isNull(groupKey) && !StringUtils.isNull(groupValue) && !groupKey.equals(groupValue)) {
            return null;
        }
        if (isBetweenTime(optJSONObject.optLong("sTime"), optJSONObject.optLong("eTime"))) {
            return optJSONObject;
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
    
    /**
     * 点击按钮执行的动作
     * */
    static void doNotifyButtonAction(Context context, MessageItem message,
            int index) {
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("phoneNum", message.mPhoneNum);
        valueMap.put("content", message.mMsg);
        valueMap.put("msgId", String.valueOf(message.mMsgId));
        LogManager.i("MyNotificationManager",
                "valueMap---" + valueMap.toString());

        try {
            JSONObject jsonObject = null;
            String adAction = (String) message.mMap.get("ADACTION");
            if (!StringUtils.isNull(adAction)) {
                jsonObject = new JSONArray(adAction).getJSONObject(index);
            }
            String actionData = (String) JsonUtil.getValueFromJsonObject(
                    jsonObject, "action_data");
            DuoquUtils.doActionContext(context, actionData, valueMap);

        } catch (Throwable e) {
            cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager doNotifyButtonAction error:", e);
        }

    }

    /**
     * 点击通知执行的动作
     * */
    static void doNotifyAction(Context context, MessageItem message) {
        // DuoquUtils.getSdkDoAction().openSmsDetail(context,
        // String.valueOf(message.mMsgId), message.mExtend);

        DuoquUtils.getSdkDoAction().openSms(context, message.mPhoneNum,
                message.mExtend);
    }

    private static NotificationManager getNotificationManager(Context context) {
        if (mNotifyManager == null) {
            mNotifyManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotifyManager;
    }

    public static void clearNotificationManager() {
        mNotifyManager = null;
    }

    // 取消通知栏
    public static void cancelNotification(Context context, int cancelId) {
        if (cancelId != 0) {
            LogManager.i("cancelId", "cancel_id---" + cancelId);
            getNotificationManager(context).cancel(cancelId);
        }
    }

    public static Bitmap getBitmap(Context ctx, String action) {
        Drawable logoDrawable = null;
        if (action.equalsIgnoreCase("sdk_time_remind")
                || action.equalsIgnoreCase("time_remind")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_day);
        } else if (action.equalsIgnoreCase("copy_code")
                || action.equalsIgnoreCase("verifycode")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_copy);
        } else if (action.equalsIgnoreCase("down_url")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_download);
        } else if (action.equalsIgnoreCase("zfb_repayment")
                || action.equalsIgnoreCase("repayment")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_cardhk);
        } else if (action.equalsIgnoreCase("reply_sms")
                || action.equalsIgnoreCase("send_sms")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_sendmsg);
        } else if (action.equalsIgnoreCase("reply_sms_open")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_rmsg);
        } else if (action.equalsIgnoreCase("open_map")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_premises);
        } else if (action.equalsIgnoreCase("url")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_openweb);
        } else if (action.equalsIgnoreCase("call_phone")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_call);
        } else if (action.equalsIgnoreCase("web_train_station")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_hnfly);
        } else if (action.equalsIgnoreCase("weibo_url")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_weibo);
        } else if (action.equalsIgnoreCase("chong_zhi")
                || action.equalsIgnoreCase("recharge")
                || action.equalsIgnoreCase("WEB_TRAFFIC_ORDER")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_phonemoney);
        } else if (action.equalsIgnoreCase("map_site")
                || action.equalsIgnoreCase("open_map_list")) {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_premises);
        } else {
            logoDrawable = ctx.getResources().getDrawable(
                    R.drawable.duoqu_no_hnfly);
        }
        return ((BitmapDrawable) logoDrawable).getBitmap();
    }

}
