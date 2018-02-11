package cn.com.xy.sms.sdk.ui.simplebubbleview;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.ui.popu.simplepart.SimpleBubbleBottom;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.SdkCallBack;

/**
 * 
 * @author Administrator
 * 
 */
@SuppressLint("NewApi")
public class DuoquSimpleBubbleViewManager {
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_ID = 1;
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_VALUE = 2;
    public final static String TAG = "DuoquSimpleBubbleViewManager";

    public static void getSimpleBubbleData(String msgIds, String phoneNum, String smsCenterNum, String smsContent,
            long smsReceiveTime, byte returnCacheType, HashMap<String, Object> extend, SdkCallBack callBack,
            boolean scrollFing) throws Exception {
        HashMap <String , String> exExtend = new HashMap<String, String>();
        try{
            if(extend.containsValue("isUseNewAction")){
                exExtend.put("isUseNewAction", (String) extend.get("isUseNewAction"));
            }else{
                exExtend.put("isUseNewAction", "true");
            }
        }catch(Throwable e){
            exExtend.put("isUseNewAction", "true");
        }
        
        ParseBubbleManager.queryDataByMsgItem(msgIds, phoneNum, smsContent, smsCenterNum, 1, smsReceiveTime, callBack,
                scrollFing,exExtend);
    }

    public static View getBubbleView(Activity ctx, String msgIds, String phoneNum, String smsCenterNum,
            String smsContent, long smsReceiveTime, byte returnCacheType, ViewGroup parentView,
            HashMap<String, Object> extend) {
        try {
            JSONObject jsonObject = ParseBubbleManager.queryDataByMsgItem(msgIds, phoneNum, smsContent, smsCenterNum,
                    1, smsReceiveTime);
            if (jsonObject == null) {
                return null;
            }
            JSONArray cacheValue = null;
            try {
                cacheValue = jsonObject.getJSONArray("session_reuslt");
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("DuoquSimpleBubbleViewManager getBubbleView error:", e);
            }
            if (cacheValue == null) {
                return null;
            }
            return getSimpleBubbleView(ctx, cacheValue, parentView, extend);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquSimpleBubbleViewManager getBubbleView error:", e);
        }
        return null;
    }

    /**
     * @param ctx
     * @param msg
     * @param viewId
     * @return
     */
    public static View getSimpleBubbleView(Activity ctx, JSONArray jsonArray, ViewGroup buttonGroup,
            HashMap<String, Object> extend) throws Exception {
        if (jsonArray == null || buttonGroup == null)
            return null;
        SimpleBubbleBottom view = null;
        if (buttonGroup.getChildCount() > 0) {
            view = (SimpleBubbleBottom) buttonGroup.getChildAt(0);
        }

        if (view != null) {
            try {
                view.setContent(jsonArray, extend);
            } catch (Throwable e) {
                view = null;
            }
        }
        if (view == null) {
            view = new SimpleBubbleBottom(ctx, jsonArray, extend);
            view.setId(Integer.MAX_VALUE);
            buttonGroup.addView(view);
        }
        return view;
    }
}
