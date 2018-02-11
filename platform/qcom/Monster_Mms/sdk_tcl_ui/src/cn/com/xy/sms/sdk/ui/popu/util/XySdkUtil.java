package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.LruCache;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.queue.BubbleTaskQueue;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.publicinfo.PublicInfoManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseNotificationManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import cn.com.xy.sms.util.ParseSmsToBubbleUtil;

@SuppressLint("NewApi")
public class XySdkUtil {
    public static final String TAG = "XIAOYUAN";
    // 密钥
    public static final String DUOQU_SDK_CHANNEL = "al30zFgQTEST_T";
    private static final String DUOQU_SDK_CHANNEL_SECRETKEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKzp/+9niMrPrxOMD++V4ZrM1tsl1htDfi9P2nqJTeFD6SofoyzyPe+42ViYL6ZVkyGjnhYH4i9cRD3L6gen++xfPCgLtiiNgFyoWK9bJgs8N4/QkBhAXxgmoxBjxcWv6vVR/OKlWcEwWLEbi1/bYj50KxAgwitf3LBzNqXfTMWBAgMBAAECgYBIuFikrJxA/zsYM214S0aIPeGWZME40he9eg3ePuR7+n2S859ChdY6fTkbI1XW1GJ+YTtY9JPRazJf8uRHuAQgYa0SIhkYH2JihEFat4XS01mlRdZZQiWrxciwzArYrJ5BIXQ9jKmbeIfZQlwBXTl2V5LQqoddb1wTPQ91GBclyQJBANJwrrciem/yoWVBijVc4xyLH2sBWYoVuORS0yMb5SeqAtUs4omeM/9RKTyXQYdkkrYevUn0gn9DS19ObdfJmqMCQQDSWX3VZGIA/0VQsICEKrwcNsogAu33Lv6X0Nsudk13+ORfTPQxSfmLuaktISZluT1juCu0dJrwOfZ7hO1WluWLAkEAhvIvd8yqSyOCD9aJdFLOaL1mNk41RvFLEU9zia4Xsum4y07vLmS+31kCYpJ0OQhrdFId/FDJZiaXLNS0Z44mlQJAYKOGwSv+LpEYqkp8sLvlclzlnbFa3I41n0/v8redPboWSYZURfTDdiMVC0vIlUF2Z8LsKVrM+ALZL8RROc/XowJBALMAfzaP2R0Rhj/piQTDJTmxu693EIS7f+gbyLph7uri7lmggO7+bjkrjR4D1tlxZB3RiYEoY2lKXJlsAucxatk=";
    public static final int DUOQU_BUBBLE_DATA_CACHE_SIZE = 200;
    private static final int PARSE_MSG_TYPE_SIMPLE_AND_RICH = 3;
    /** setting-params start */
    public static final String SMARTSMS_SWITCH = "smartsms_switch";
    public static final String SMARTSMS_ENHANCE = "smartsms_enhance";
    public static final String SMARTSMS_BUBBLE = "smartsms_bubble";
    public static final String SMARTSMS_UPDATE_TYPE = "smartsms_update_type";
    public static final String SMARTSMS_NO_SHOW_AGAIN = "smartsms_no_show_again";
    public static final String SMARTSMS_HAS_SHOW_FIRST = "smartsms_has_show_first";
    /** setting-params end */
    /* UIX-148 lianghailun 20160505 start */
    private static String mBubbleActivityRemusePhoneNum = null;
    private static int mBubbleActivityRemuseHashCode = -1;
    /* UIX-148 lianghailun 20160505 end */
    /**
     * cache for Bubble-Data
     */
    private static final LruCache<Long, JSONObject> mBubbleDataCache = new LruCache<Long, JSONObject>(
            DUOQU_BUBBLE_DATA_CACHE_SIZE);

    // private static final HashMap<Integer, Integer> mWidthMap= new
    // HashMap<Integer, Integer>();
    /**
     * init sdk custom
     * 
     * @param context
     * @param channel
     * @param key
     * @param myDoAction
     * @param iccid
     * @param extend
     */
    public static void init(final Context context, String channel, String key, AbsSdkDoAction myDoAction, String iccid, HashMap<String, String> extend) {
        try {
            extend.put("SECRETKEY", key);

            ParseManager.initSdk(context, channel, iccid, true, true, extend);

            ParseManager.setSdkDoAction(myDoAction);

            Handler hd = new Handler() {
                public void handleMessage(Message msg) {
                    ParseSmsToBubbleUtil.beforeHandParseReceiveSms(500, PARSE_MSG_TYPE_SIMPLE_AND_RICH);
                }
            };
            Message msg = hd.obtainMessage();
            hd.sendMessageDelayed(msg, 6000);
            // LogManager.debug = true;
            PublicInfoManager.beforeLoadPublicInfo(context);

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("cn.com.xy.sms.sdk.SmartSmsSdkUtil.init error", e);
        }
    }

    /**
     * init sdk
     * 
     * @param context
     * @param channel
     * @param key
     * @param myDoAction
     *            override
     */
    public static void init(final Context context, String channel, String key, AbsSdkDoAction myDoAction) {
        try {
            HashMap<String, String> extend = new HashMap<String, String>();
            extend.put("ONLINE_UPDATE_SDK", "1");
            extend.put("SUPPORT_NETWORK_TYPE", "1");
            extend.put(SMARTSMS_ENHANCE, "true");

            extend.put("SECRETKEY", DUOQU_SDK_CHANNEL_SECRETKEY);

            String iccid = getICCID(context);

            init(context, channel, key, myDoAction, iccid, extend);

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("cn.com.xy.sms.sdk.SmartSmsSdkUtil.init error", e);
        }
    }

    // /**
    // * initialize SmartSms-Data
    // */
    // public static void init(final Context context){
    // try {
    // HashMap<String, String> extend = new HashMap<String, String>();
    // extend.put("ONLINE_UPDATE_SDK", "1");
    //
    // extend.put("SUPPORT_NETWORK_TYPE", "1");
    // extend.put(SMARTSMS_ENHANCE, "true");
    //
    // extend.put("SECRETKEY",DUOQU_SDK_CHANNEL_SECRETKEY);
    //
    // String iccid =getICCID(context);
    //
    // ParseManager.initSdk(context, DUOQU_SDK_CHANNEL,iccid , true, true,
    // extend);
    //
    // ParseManager.setSdkDoAction(new XySdkAction());
    //
    // Handler hd = new Handler(){
    // public void handleMessage(Message msg) {
    // ParseSmsToBubbleUtil.beforeHandParseReceiveSms(500,
    // PARSE_MSG_TYPE_SIMPLE_AND_RICH);
    // }
    // };
    // Message msg = hd.obtainMessage();
    // hd.sendMessageDelayed(msg, 6000);
    // LogManager.debug = true;
    // PublicInfoManager.BeforeLoadPublicInfo(context);
    //
    // } catch (Exception e) {
    // // TODO: handle exception
    // Log.e(TAG, "cn.com.xy.sms.sdk.SmartSmsSdkUtil.init error",e);
    // }
    // }

    public static String getICCID(Context context) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (!StringUtils.isNull(manager.getSimSerialNumber())) {
                return manager.getSimSerialNumber();
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return "";
    }

    /**
     * cache for Bubble-cache
     */
    public static void clearCache(int acHasCode, String phoneNum) {
        /* UIX-148 lianghailun 20160505 start */
        if (acHasCode != mBubbleActivityRemuseHashCode && phoneNum != null
                && phoneNum.equals(mBubbleActivityRemusePhoneNum)) {
            return;
        }
        /* UIX-148 lianghailun 20160505 end */
//        if (mBubbleDataCache != null) {
//            mBubbleDataCache.evictAll();
//        }
        ParseBubbleManager.clearAllCache(phoneNum);
        ParseRichBubbleManager.clearCacheBubbleData(phoneNum);
        DuoquBubbleViewManager.clearCacheData();
    }

    /* UIX-148 lianghailun 20160505 start */
    public static void setBubbleActivityResumePhoneNum(int acHashCode, String bubbleActivityResume) {
        mBubbleActivityRemusePhoneNum = bubbleActivityResume;
        mBubbleActivityRemuseHashCode = acHashCode;
    }

    /* UIX-148 lianghailun 20160505 end */

    public static void putBubbleDataToCache(Long key, JSONObject value) {
        if (key == null || value == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog(
                    "cn.com.xy.sms.sdk.SmartSmsSdkUtil putBubbleDataToCache key is null or value is null.", null);
            return;
        }
        synchronized (mBubbleDataCache) {
            mBubbleDataCache.put(key, value);
        }
    }

    public static JSONObject getBubbleDataFromCache(Long key) {
        if (key == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog(
                    "cn.com.xy.sms.sdk.SmartSmsSdkUtil getBubbleDataFromCache key is null", null);
            return null;
        }
        return mBubbleDataCache.get(key);
    }

    public static final int SMARTSMS_PARSE_TYPE_ONLY_BUBBLE = 0;
    public static final int SMARTSMS_PARSE_TYPE_NOTIFY = 1;
    public static final int SMARTSMS_PARSE_TYPE_POUPP = 2;

    /**
     * 
     * @param msgId
     *            消息ID
     * @param msgs
     *            短信BODY
     * @param type
     *            type 0:只需要丰富汽泡，1：通知栏浮窗，2：弹窗
     * @param simIndex
     *            卡位
     */
    public static void parseMsg(String msgId, SmsMessage[] msgs, int parseType, int simIndex) {
        /* UIX-148 lianghailun 20160505 start */
        try {
            mBubbleDataCache.remove(Long.valueOf(msgId));
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        /* UIX-148 lianghailun 20160505 end */
        try {
            final int pduCount = msgs.length;
            String bodyText = null;
            if (pduCount == 1) {
                bodyText = replaceFormFeeds(msgs[0].getDisplayMessageBody());
            } else {
                StringBuilder body = new StringBuilder();
                SmsMessage sms = null;
                for (int i = 0; i < pduCount; i++) {
                    sms = msgs[i];
                    // if (sms.mWrappedSmsMessage != null) {
                    if (sms != null) {
                        body.append(sms.getDisplayMessageBody());
                    }
                }
                bodyText = replaceFormFeeds(body.toString());
            }
            String phoneNum = msgs[0].getOriginatingAddress();
            String centerNum = msgs[0].getServiceCenterAddress();
            // add to background queue parse
            if (parseType == 0) {
                // 加入后台队列，丰富汽泡流程。
                BubbleTaskQueue.addDataToQueue(0, msgId, phoneNum, bodyText, centerNum, msgs[0].getTimestampMillis(),
                        3, null);
            } else if (parseType == 1) {
                // 通知栏，浮窗结果，内含丰富汽泡流程。同步方法调用。后续通知栏依据消息ID，调用
                // getNotifyDataCacheByMsgId 是否能获取到数据，依据有无数据，是否显示智能短信通知栏。
                // Map<String,Object> map =
                // ParseSmsToBubbleUtil.parseSmsToBubbleResult(msgId, phoneNum,
                // bodyText, centerNum, msgs[0].getTimestampMillis(), 4,
                // false, true, null);
                HashMap<String, String> extendMap = getExtendMap(simIndex, msgId, msgs[0].getTimestampMillis());

                Map<String, Object> map = ParseNotificationManager.parseNotificationMsg(Constant.getContext(), msgId,
                        phoneNum, centerNum, bodyText, msgs[0].getTimestampMillis(), extendMap);
                if (map != null && map.size() > 1) {
                    notifyDataCacheMap.put(msgId, map);
                }
            } else if (parseType == 2) {
                // 调用弹窗
                HashMap<String, String> extendMap = getExtendMap(simIndex, msgId, msgs[0].getTimestampMillis());

                ParseManager.parseMsgToPopupWindow(Constant.getContext(), phoneNum, centerNum, bodyText, true,
                        extendMap);
                // 加入后台队列，丰富汽泡流程。
                // BubbleTaskQueue.addDataToQueue(0, msgId, phoneNum, bodyText,
                // centerNum, msgs[0].getTimestampMillis(), 3, null);
            } else if (parseType == 3) {// 有通知栏和弹窗。
                // 调用弹窗
                HashMap<String, String> extendMap = getExtendMap(simIndex, msgId, msgs[0].getTimestampMillis());

                Map<String, Object> map = ParseNotificationManager.parseNotificationMsgAndPopupWindow(
                        Constant.getContext(), msgId, phoneNum, centerNum, bodyText, msgs[0].getTimestampMillis(),
                        extendMap);
                if (map != null && map.size() > 1) {
                    notifyDataCacheMap.put(msgId, map);
                }
                // ParseManager.parseMsgToPopupWindow(Constant.getContext(),
                // phoneNum, centerNum, bodyText, extendMap);
                // 加入后台队列，丰富汽泡流程。
                // BubbleTaskQueue.addDataToQueue(0, msgId, phoneNum, bodyText,
                // centerNum, msgs[0].getTimestampMillis(), 3, null);
            }

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    public static void parseMsg(String msgId, String bodyText, String phoneNum, String centerNum, long time,
            int parseType, int simIndex) {

        try {
            // add to background queue parse
            if (parseType == 0) {
                // 加入后台队列，丰富汽泡流程。
                BubbleTaskQueue.addDataToQueue(0, msgId, phoneNum, bodyText, centerNum, time, 3, null);
            } else if (parseType == 1) {
                // 通知栏，浮窗结果，内含丰富汽泡流程。同步方法调用。后续通知栏依据消息ID，调用
                // getNotifyDataCacheByMsgId 是否能获取到数据，依据有无数据，是否显示智能短信通知栏。
                // Map<String,Object> map =
                // ParseSmsToBubbleUtil.parseSmsToBubbleResult(msgId, phoneNum,
                // bodyText, centerNum, time, 4,
                // false, true, null);
                Map<String, Object> map = ParseNotificationManager.parseNotificationMsg(Constant.getContext(), msgId,
                        phoneNum, centerNum, bodyText, time, null);
                if (map != null && map.size() > 1) {
                    notifyDataCacheMap.put(msgId, map);
                }
            } else if (parseType == 2) {
                // 调用弹窗
                HashMap<String, String> extendMap = new HashMap<String, String>();
                extendMap.put("simIndex", String.valueOf(simIndex));
                extendMap.put("simName", getSimNameBySimIndex(simIndex));
                extendMap.put("msgId", String.valueOf(msgId));
                extendMap.put("opensms_enable", "true");
                ParseManager.parseMsgToPopupWindow(Constant.getContext(), phoneNum, centerNum, bodyText, extendMap);
                // 加入后台队列，丰富汽泡流程。
                BubbleTaskQueue.addDataToQueue(0, msgId, phoneNum, bodyText, centerNum, time, 3, null);
            }

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

    }

    public static HashMap<String, String> getExtendMap(int simIndex, String msgId, long msgTime) {
        HashMap<String, String> extendMap = new HashMap<String, String>();
        extendMap.put("simIndex", String.valueOf(simIndex));
        extendMap.put("simName", getSimNameBySimIndex(simIndex));
        extendMap.put("msgId", msgId);
        extendMap.put("opensms_enable", "true");// 是否开启短信原文按钮
        extendMap.put("msgTime", String.valueOf(msgTime));
        extendMap.put("handle_type", "1");// 验证码 回调。
        return extendMap;
    }

    // 根据simIndex获取卡的名字
    public static String getSimNameBySimIndex(int simIndex) {
        return "";
    }

    private static LruCache<String, Map<String, Object>> notifyDataCacheMap = new LruCache<String, Map<String, Object>>(
            10);

    public static Map<String, Object> getNotifyDataCacheByMsgId(long msgId, boolean removeCache) {
        String key = String.valueOf(msgId);
        Map<String, Object> res = notifyDataCacheMap.get(key);
        if (res != null && removeCache) {
            notifyDataCacheMap.remove(key);
        }
        return res;
    }

    /**
     * format PhoneNumber before parse message
     */
    public static String formatPhoneNum(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        phoneNumber = phoneNumber.replaceAll(" ", "").replaceAll("-", "");
        if (phoneNumber.startsWith("86")) {
            phoneNumber = phoneNumber.substring(2, phoneNumber.length());
        } else if (phoneNumber.startsWith("+86")) {
            phoneNumber = phoneNumber.substring(3, phoneNumber.length());
        } else if (phoneNumber.startsWith("0086")) {
            phoneNumber = phoneNumber.substring(4, phoneNumber.length());
        }
        return phoneNumber;
    }

    private static String replaceFormFeeds(String s) {
        String str = "";
        if (null != s) {
            str = s.replace('\f', '\n');
        }
        return str;
    }
    
    public static void removeCache(String phoneNum, final Set<Integer> ids,Set<String> keys ) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		Iterator<Integer> it = ids.iterator();
		while (it.hasNext()) {
			Integer id = it.next();
			mBubbleDataCache.remove(Long.valueOf(id));
				
		}
		
		Iterator<String> iterator = keys.iterator();
		while(iterator.hasNext()){
			String key=iterator.next();
			DuoquBubbleViewManager
			.clearCacheData(phoneNum, key);
		}
		ParseBubbleManager.deleteBubbleData(ids);
	}
    
    public static void removeCache(String phoneNum, final Set<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		Iterator<Integer> it = ids.iterator();
		while (it.hasNext()) {
			Integer msgId = it.next();
			mBubbleDataCache.remove(Long.valueOf(msgId));
			DuoquBubbleViewManager
					.clearCacheData(phoneNum, String.valueOf(msgId));

		}
		ParseBubbleManager.deleteBubbleData(ids);
    }
}
