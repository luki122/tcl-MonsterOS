package cn.com.xy.sms.sdk.ui.bubbleview;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BubblePopupView;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import cn.com.xy.sms.util.SdkCallBack;

/**
 * xiaoyuan richBubbleView Manager
 * 
 * @author Administrator
 * 
 */
@SuppressLint("NewApi")
public class DuoquBubbleViewManager {

    final static String TAG = "XIAOYUAN";
    final static int DUOQU_CACHE_ITEM_VIEW_MAX_SIZE = 200;
    public static final int DUOQU_BUBBLE_VIEW_ID = 999999999;
    // Cache type of the return data 1:Returns the SDK cache id
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_ID = 1;
    // Cache type of the return data 2: Returns the recognition results, Save it
    // by the developer or identification results.
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_VALUE = 2;
    private static final String DUOQU_VIEW_ID = "View_fdes";

    // cache msg data
    static LruCache<String, BusinessSmsMessage> mFormatSmsDataCache = new LruCache<String, BusinessSmsMessage>(
            DUOQU_CACHE_ITEM_VIEW_MAX_SIZE);
    // cache bubble view
    static LruCache<String, LinkedList<BubblePopupView>> mFormatItemViewCacheMapList = new LruCache<String, LinkedList<BubblePopupView>>(
            DUOQU_CACHE_ITEM_VIEW_MAX_SIZE);
    // cache PartViewParam
    static HashMap<String, Map<String, PartViewParam>> viewParamCache = new HashMap<String, Map<String, PartViewParam>>();

    public static void putMsgToCache(String cacheKey, BusinessSmsMessage msg) {
        if (cacheKey == null || msg == null) {
            SmartSmsSdkUtil
                    .smartSdkExceptionLog("DuoquBubbleViewManager.pubMsgToCache cacheKey or msg is null. ", null);
            return;
        }
        synchronized (mFormatSmsDataCache) {
            mFormatSmsDataCache.put(cacheKey, msg);
        }
    }

    public static BusinessSmsMessage getFomratSmsData(String cacheKey) {
        if (cacheKey == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquBubbleViewManager.getMsgFromCache cacheKey is null. ", null);
            return null;
        }
        return mFormatSmsDataCache.get(cacheKey);
    }

    public static void putBubbleItemTypeViewToCache(String cacheKey, LinkedList<BubblePopupView> bubbleViews) {
        if (cacheKey == null || bubbleViews == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog(
                    "DuoquBubbleViewManager.putBubbleItemTypeViewToCache cacheKey or msg is null. ", null);
            return;
        }
        synchronized (mFormatItemViewCacheMapList) {
            mFormatItemViewCacheMapList.put(cacheKey, bubbleViews);
        }
    }

    public static LinkedList<BubblePopupView> getFomratItemViewList(String cacheKey) {
        if (cacheKey == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog(
                    "DuoquBubbleViewManager.getBubbleItemTypeViewFromCache cacheKey is null. ", null);
            return null;
        }
        return mFormatItemViewCacheMapList.get(cacheKey);
    }

    public static void clearCacheData() {
        if (mFormatSmsDataCache != null) {
            synchronized (mFormatSmsDataCache) {
                mFormatSmsDataCache.evictAll();
            }
        }
        if (mFormatItemViewCacheMapList != null) {
            synchronized (mFormatItemViewCacheMapList) {
                mFormatItemViewCacheMapList.evictAll();
            }
        }
    }
    
    
    public static void clearCacheDataByMsgId(String key){    	
    	if(StringUtils.isNull(key))return;
    	if (mFormatSmsDataCache != null) {    		
            synchronized (mFormatSmsDataCache) {
                mFormatSmsDataCache.remove(key);
            }
        }
    	
    }
    
    public static void clearCacheData(String phoneNum,String key){
            clearCacheDataByMsgId(key);
    }
    

    public static Map<String, Object> parseMsgToBubbleCardResult(Context ctx, String msgId, String phoneNum,
            String smsCenterNum, String smsContent, long smsReceiveTime, byte returnCacheType,
            HashMap<String, String> extend) {
        try {
            return ParseManager.parseMsgToBubbleCardResult(ctx, msgId, phoneNum, smsCenterNum, smsContent,
                    smsReceiveTime, returnCacheType, extend);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }

    public static JSONObject getRichBubbleData(final Activity ctx, final String msgIds, final String phoneNum,
            final String smsCenterNum, final String smsContent, long smsReceiveTime, byte returnCacheType,
            final View itemView, ViewGroup parentView, final ViewGroup richItemGroup, final AdapterView adViews,
            final HashMap<String, Object> extend, final SdkCallBack xyCallBack, boolean scrollFing) {
        if (StringUtils.isNull(msgIds)) {
            XyUtil.doXycallBackResult(xyCallBack, -1, null, msgIds);
            return null;
        }
        try {
            ParseRichBubbleManager.queryDataByMsgItem(msgIds, phoneNum, smsContent, smsReceiveTime, smsCenterNum, 2,
                    xyCallBack, scrollFing, extend);
        } catch (Throwable e) {
            XyUtil.doXycallBackResult(xyCallBack, -1, null, msgIds);
            SmartSmsSdkUtil.smartSdkExceptionLog("getRichBubbleData error: ", e);
        }

        return null;

    }

    private static View getBubblePopupView(Activity ctx, BusinessSmsMessage msg, String viewId, View itemView,
            ViewGroup apView) throws Exception {
        // long time =System.currentTimeMillis();
        BubblePopupView view = null;
        LinkedList<BubblePopupView> linkedList = null;
        StringBuilder key = new StringBuilder();
        if(ctx!=null){
            key.append(ctx.hashCode());
        }
        if(apView!=null){
            key.append(apView.hashCode());
        }
        key.append(viewId);
        int size = 0;
        if (!StringUtils.isNull(viewId)) {
            linkedList = getFomratItemViewList(key.toString());
            if (linkedList != null) {
                size = linkedList.size();
                int index = -1;
                int cnt = 0;
                do {
                    view = getCacheItemView(linkedList);
                    index = ViewManger.indexOfChild(view, apView);
                    cnt++;
                } while (index != -1 && cnt < size);
                if (index != -1) {// view used
                    view = null;
                }
                // long edtime =System.currentTimeMillis();
                // LogManager.d("duoqu_xiaoyuan","aaaa getBubblePopupView take time: "+(edtime-time));
                if (view != null) {
                    try {
                        // view.reSetActivity(ctx);
                        view.reBindData(ctx, msg);
                        // edtime =System.currentTimeMillis();
                        // LogManager.d("duoqu_xiaoyuan","bbbb getBubblePopupView take time: "+(edtime-time));
                        // LogManager.d("duoqu_xiaoyuan",
                        // "msgid: "+msg.smsId+" view reBindData viewId: "+viewId);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                        view = null;
                        // LogManager.w("duoqu_xiaoyuan",
                        // "msgid: "+msg.smsId+" view reBindData error:  "+e.getMessage());
                    }
                }
            }
        }
        if (view == null) {
            view = new BubblePopupView(ctx);
            Map<String, PartViewParam> map = viewParamCache.get(viewId);
            if (map == null) {
                map = ViewManger.parseViewPartParam(viewId);
                viewParamCache.put(viewId, map);
            }
            msg.putValue("viewPartParam", map);
            view.init(ctx, msg, null);
            view.setId(DUOQU_BUBBLE_VIEW_ID);
            // long edtime =System.currentTimeMillis();
            // LogManager.d("duoqu_xiaoyuan","cccc getBubblePopupView take time: "+(edtime-time));
            if (linkedList == null) {
                linkedList = new LinkedList<BubblePopupView>();
                putBubbleItemTypeViewToCache(key.toString(), linkedList);
            }
            addCacheItemView(view, linkedList);
            // edtime =System.currentTimeMillis();
            // LogManager.d("duoqu_xiaoyuan","dddd getBubblePopupView take time: "+(edtime-time));
        }

        return view;
    }

    @SuppressLint("NewApi")
    private static void addCacheItemView(BubblePopupView itewView, LinkedList<BubblePopupView> listView) {
        listView.offerLast(itewView);// add elements
    }
    

    public static void clearCacheData(Activity ctx) {
        if (mFormatSmsDataCache != null) {
            synchronized (mFormatSmsDataCache) {
                mFormatSmsDataCache.evictAll();
            }
        }
        if (mFormatItemViewCacheMapList != null) {
            synchronized (mFormatItemViewCacheMapList) {
                if (ctx == null) {
                    mFormatItemViewCacheMapList.evictAll();
                    return;
                }

                Map<String, LinkedList<BubblePopupView>> cache = mFormatItemViewCacheMapList.snapshot();
                for (String key : cache.keySet()) {
                    if (key.startsWith(ctx.hashCode() + "")) {
                        mFormatItemViewCacheMapList.remove(key);
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private static BubblePopupView getCacheItemView(LinkedList<BubblePopupView> listView) {
        BubblePopupView itemView = null;
        if (listView != null) {
            itemView = listView.pollFirst();
            if (itemView != null) {
                addCacheItemView(itemView, listView);
            }
        } else {
            itemView = null;
        }
        return itemView;
    }

    public static View getRichBubbleView(Activity ctx, JSONObject jsobj, String smsId, String smsContent,
            String phoneNum, long smsReceiveTime, final View itemView, final AdapterView adViews,
            final HashMap<String, Object> extend) {
        View richview = null;
        try {
            String key = smsId + smsReceiveTime;
//            BusinessSmsMessage msg = getFomratSmsData(key);
            
            //query BusinessSmsMessage from cache
            BusinessSmsMessage msg = getFomratSmsData(key);
            if (msg != null) {
                // long stTime = System.currentTimeMillis();
                String viewId = (String) msg.getValue(DUOQU_VIEW_ID);
                try {
                    msg.extendParamMap = extend;
                    msg.messageBody = smsContent;
                    richview = getBubblePopupView(ctx, msg, viewId, itemView, adViews);
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("View_fdes : " + viewId + " error: " + e.getMessage(), e);
                }
            } else if (jsobj != null && jsobj.has(DUOQU_VIEW_ID)) {
                // long stTime = System.currentTimeMillis();
                final String viewId = jsobj.getString(DUOQU_VIEW_ID);
                msg = BusinessSmsMessage.createMsgObj();
                msg.smsId = Long.parseLong(smsId);
                msg.viewType = 1;// bubble view
                msg.bubbleJsonObj = jsobj;
                msg.messageBody = smsContent;
                msg.originatingAddress = phoneNum;
                msg.titleNo = jsobj.optString("title_num");
                msg.extendParamMap = extend;
                if (extend != null && !extend.isEmpty()) {
                    msg.simIndex = XyUtil.getSimIndex(extend);
                    msg.simName = (String) extend.get("simName");
                    if (extend.containsKey("msgTime")) {
                        try {
                            msg.msgTime = Long.parseLong(extend.get("msgTime").toString());
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                        }
                    }
                }
                // long etTime = System.currentTimeMillis();
                // LogManager.e("duoqu_xiaoyuan",
                // " 1111 getRichBubbleView  take time:"+(etTime-stTime)+" viewId:"+viewId);
                richview = getBubblePopupView(ctx, msg, viewId, itemView, adViews);
                // etTime = System.currentTimeMillis();
                // LogManager.e("duoqu_xiaoyuan",
                // " 2222 getRichBubbleView  take time:"+(etTime-stTime));
                putMsgToCache(key, msg);
                // etTime = System.currentTimeMillis();
                // LogManager.e("duoqu_xiaoyuan",
                // " 3333 getRichBubbleView  take time:"+(etTime-stTime));
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return richview;
    }

    /**
     * before init bubbleview for scroll speed each bubbleView init maxsize 4
     * 
     * @param ctx
     * @param phone
     */
    public static void beforeInitBubbleView(Activity ctx, String phone) {
        // if(true)return;
        JSONObject obj = PhoneSmsParseManager.findObjectByPhone(phone);
        if (obj != null && obj.has("useBubbleViews")) {
            JSONArray arr = null;
            try {
                arr = new JSONArray(obj.getString("useBubbleViews"));
                if (arr != null) {
                    int len = arr.length();
                    BubblePopupView view = null;
                    String viewId = null;
                    int viewCacheSize = 0;
                    LinkedList<BubblePopupView> linkedList = null;
                    int maxCacheSize = 4;
                    BusinessSmsMessage msg = BusinessSmsMessage.createMsgObj();
                    msg.viewType = 1;
                    for (int i = 0; i < len; i++) {
                        viewId = arr.getString(i);
                        if (StringUtils.isNull(viewId)) {
                            continue;
                        }
                        linkedList = getFomratItemViewList(viewId);
                        if (linkedList == null) {
                            linkedList = new LinkedList<BubblePopupView>();
                            putBubbleItemTypeViewToCache(viewId, linkedList);
                        }
                        viewCacheSize = linkedList.size();
                        if (viewCacheSize >= maxCacheSize) {
                            continue;
                        }

                        Map<String, PartViewParam> map = viewParamCache.get(viewId);
                        if (map == null) {
                            map = ViewManger.parseViewPartParam(viewId);
                            viewParamCache.put(viewId, map);
                        }
                        msg.putValue("viewPartParam", map);
                        do {
                            view = new BubblePopupView(ctx);
                            view.init(ctx, msg, null);
                            view.setId(DUOQU_BUBBLE_VIEW_ID);
                            addCacheItemView(view, linkedList);
                            viewCacheSize++;
                        } while (viewCacheSize < maxCacheSize);
                    }
                }
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
            }
        }
    }
}
