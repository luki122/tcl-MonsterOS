package cn.com.xy.sms.sdk.ui.publicinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.BaseAdapter;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class PublicInfoManager {
    private static final String TAG = "PublicInfoManager";
    public static final int IMAGE_WIDTH = 100;
    public static final int IMAGE_HIGHT = 100;
    private static HashMap<String, String> extend = null;

    // public static XyMemoryCache memoryCache = new XyMemoryCache();
    private static LruCache<String, BitmapDrawable> logoCache = new LruCache<String, BitmapDrawable>(100);
    // 是否需要预加载logo，根据具体要求设置
    public static boolean isBeforeLoadLogo = true;

    public static HashMap<String, JSONObject> publicInfoData = new HashMap<String, JSONObject>();

    /* SDK-449 zhaojiangwei 20160524 start */
    public static HashMap<String, String[]> phonePublicIdData = new HashMap<String, String[]>();
    /* SDK-449 zhaojiangwei 20160524 end */

    public static ExecutorService beforPublicInfoPool = Executors.newFixedThreadPool(2);

    public static ExecutorService beforLogoPublicInfoPool = Executors.newFixedThreadPool(2);

    public static ExecutorService publicInfoPool = Executors.newFixedThreadPool(2);

    private static BaseAdapter mBaseAdapter = null;
    private static boolean isLoaded = false;
    private static Handler mHandler = null;

    public static void registerBaseAdapter(Handler handler, BaseAdapter baseAdapter) {
        if (isLoaded || mBaseAdapter != null) {
            return;
        }
        synchronized (publicInfoPool) {
            mBaseAdapter = baseAdapter;
            mHandler = handler;
        }
    }

    private static void notifyDataChange() {
        if (mBaseAdapter != null) {

            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mBaseAdapter.notifyDataSetChanged();
                        synchronized (publicInfoPool) {
                            mBaseAdapter = null;
                            mHandler = null;
                        }
                    }
                });
            } else {
                mBaseAdapter = null;
            }
        }
    }

    private static void putLogoDrawable(String logo, BitmapDrawable bd) {

        if (logo == null || bd == null) {
            return;
        }
        try {
            synchronized (logoCache) {
                logoCache.put(logo, bd);
            }

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    public static BitmapDrawable getLogoDrawable(String logo) {

        if (logo == null) {
            return null;
        }
        try {
            synchronized (logoCache) {
                return logoCache.get(logo);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }

    private static Set<String> loadTop50Num(Context context) {
        String selection = " _id in (SELECT " + Threads.RECIPIENT_IDS + " FROM threads ORDER BY date DESC LIMIT 50)";
        return loadPublicNumbers(context, selection);
    }

    /**
     * 预加载企业资料
     * 
     * @param context
     */
    public static void beforeLoadPublicInfo(final Context context) {
        beforPublicInfoPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Set<String> publicNumbers = loadTop50Num(context);
                    boolean isInit = ParseManager.isInitData();
                    long stTime = System.currentTimeMillis();
                    while (!isInit) {
                        isInit = ParseManager.isInitData();
                        if (isInit) {
                            break;
                        } else {
                            long edTime = System.currentTimeMillis();
                            if (edTime - stTime > 30000) {
                                break;
                            }
                            Thread.sleep(3);
                        }
                    }

                    befroeLoadPublicInfo(context, publicNumbers);
                    notifyDataChange();
                    isLoaded = true;
                    Set<String> allPublicNumbers = loadPublicNumbers(context, null);

                    allPublicNumbers.removeAll(publicNumbers);
                    befroeLoadPublicInfo(context, allPublicNumbers);

                    publicNumbers.clear();
                    allPublicNumbers.clear();

                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                }
            }
        });
    }

    private static void befroeLoadPublicInfo(final Context context, Set<String> publicNumbers) {
        try {
            if (publicNumbers == null || publicNumbers.isEmpty()) {
                return;
            }
            final HashMap<String, String[]> numResult = ParseManager.loadAllPubNum(publicNumbers);

            if (numResult != null && !numResult.isEmpty()) {
                synchronized (phonePublicIdData) {
                    phonePublicIdData.putAll(numResult);
                }
                HashSet<String> tempSet = new HashSet<String>();
                Set set = numResult.keySet();
                Iterator it = set.iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    /* SDK-449 zhaojiangwei 20160524 start */
                    String[] numVelus = (String[]) numResult.get(key);
                    if (numVelus.length > 0) {
                        tempSet.add(numVelus[0]);
                    }
                    /* SDK-449 zhaojiangwei 20160524 end */
                }

                HashMap<String, JSONObject> result = ParseManager.loadAllPubInfo(tempSet);
                if (result != null && !result.isEmpty()) {
                    synchronized (publicInfoData) {
                        publicInfoData.putAll(result);
                    }
                }
            }
            beforeLoadPublicInfoAndLogo(context, publicNumbers, numResult);
            numResult.clear();

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

    }

    private static void beforeLoadPublicInfoAndLogo(final Context context, Set<String> publicNumbers,
            HashMap<String, String[]> numResult) {
        try {
            if (publicNumbers != null && !publicNumbers.isEmpty()) {
                Iterator<String> it = publicNumbers.iterator();
                while (it.hasNext()) {
                    final String phoneNum = it.next();
                    /* SDK-449 zhaojiangwei 20160524 start */
                    String[] phoneValues = numResult.get(phoneNum);
                    String tmpPubValId = null;
                    if (phoneValues!=null && phoneValues.length > 0) {
                        tmpPubValId = phoneValues[0];
                    }
                    final String pubId = tmpPubValId;
                    /* SDK-449 zhaojiangwei 20160524 end */
                    if (pubId != null) {
                        beforLogoPublicInfoPool.execute(new Runnable() {
                            public void run() {
                                try {
                                    JSONObject json = publicInfoData.get(pubId);
                                    if (json != null) {
                                        String logoName = json.optString("logoc");
                                        findLogoByLogoName(logoName, null);
                                    }

                                } catch (Throwable e) {
                                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                                }
                            }
                        });
                        continue;
                    }
                    publicInfoPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                loadPublicInfo(context, phoneNum);
                            } catch (Throwable e) {
                                SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                            }
                        }
                    });
                }
            }

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    //
    //
    private static Set<String> loadPublicNumbers(Context context, String selection) {
        HashSet<String> hashSet = new HashSet<String>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"),
                    new String[] { "address" }, selection, null, null);
            if (cursor != null) {
                int addressColumn = cursor.getColumnIndex("address");
                while (cursor.moveToNext()) {

                    String address = cursor.getString(addressColumn);
                    if (!StringUtils.isNull(address)) {
                        String phoneNumber = address;
                        phoneNumber = phoneNumber.replace(" ", "");
                        if (!StringUtils.isPhoneNumber(phoneNumber)) {
                            hashSet.add(phoneNumber);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        } finally {
            try {
                cursor.close();
                cursor = null;
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                // TODO: handle Throwable
            }
        }
        return hashSet;

    }

    private static void loadPublicInfo(final Context context, final String phoneNum) {
        try {
            if (StringUtils.isNull(phoneNum) || !ParseManager.isInitData()) {
                return;
            }
            SdkCallBack callBack = new SdkCallBack() {
                @Override
                public void execute(Object... obj) {
                    try {
                        if (obj != null && obj.length > 0) {
                            if (!StringUtils.isNull((String) obj[0])) {
                                final JSONObject json = new JSONObject((String) obj[0]);
                                saveJsonToCache(json, phoneNum);
                                final String logoName = json.optString("logoc");
                                if (getLogoDrawable(logoName) != null) {
                                    return;
                                }
                                if (isBeforeLoadLogo) {
                                    beforLogoPublicInfoPool.execute(new Runnable() {
                                        public void run() {
                                            try {
                                                findLogoByLogoName(logoName, null);
                                            } catch (Throwable e) {
                                                SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                    }
                }
            };
            ParseManager.queryPublicInfo(context, phoneNum, 1, "", null, callBack);

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    /**
     * 有回调
     * 
     * @param context
     * @param phoneNum
     * @param sdkCallBack
     */
    public static void loadPublicInfo(final Context context, final String phoneNum, final SdkCallBack sdkCallBack) {
        try {
            if (StringUtils.isNull(phoneNum)) {
                return;
            }
            publicInfoPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!ParseManager.isInitData()){
                            return;
                         }
                        SdkCallBack callBack = new SdkCallBack() {

                            @Override
                            public void execute(Object... obj) {
                                try {
                                    if (obj == null || obj.length <= 2) {
                                        return;
                                    }
                                    String Oldid = (String) obj[2];
                                    String result = (String) obj[1];
                                    Integer status = (Integer) obj[0];
                                    if (status == 0 && !StringUtils.isNull(result) && phoneNum.equals(Oldid)) {
                                        JSONObject json = new JSONObject(result);
                                        saveJsonToCache(json, phoneNum);
                                        String logoName = json.optString("logoc");
                                        String name = json.optString("name");
                                        BitmapDrawable bd = findLogoByLogoName(logoName, null);
                                        sdkCallBack.execute(phoneNum, name, logoName, bd);
                                    }
                                } catch (Throwable e) {
                                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                                }
                            }
                        };
                        Map<String, String> extend = new HashMap<String, String>();
                        extend.put("id", phoneNum);
                        ParseManager.queryPublicInfoWithId(context, phoneNum, 1, "", extend, callBack);

                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                    }

                }
            });

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

   private static HashMap<String, PublicLoadRequest> sQueryList;
    private static Object sQueryListLock = new Object();
    synchronized private static HashMap<String, PublicLoadRequest> getQueryList(){
        if(sQueryList == null){
            sQueryList = new HashMap<String, PublicInfoManager.PublicLoadRequest>();
        }
        return sQueryList;
    }
    
    private static class PublicLoadRequest{
        private String number;
        private ArrayList<SdkCallBack> callBacks;
        public PublicLoadRequest(String number, SdkCallBack cb){
            this.number = number;
            callBacks = new ArrayList<SdkCallBack>();
            if(cb != null){
                callBacks.add(cb);
            }
        }
        
        public void addCallBack(SdkCallBack callBack){
            if(callBack == null){
                return;
            }
            if(callBacks == null){
                callBacks = new ArrayList<SdkCallBack>();
            }
            
            synchronized (sQueryListLock) {
                callBacks.add(callBack);
            }
        }
        
        @SuppressWarnings("unused")
        public void removeCallBack(SdkCallBack callBack){
            if(callBacks != null && callBack != null){
                callBacks.remove(callBack);
            }
        }
        
        public void noyifyCallBack(Object... obj){
            if(callBacks == null){
                return;
            }
            
            synchronized (sQueryListLock) {
                for(SdkCallBack callBack:callBacks){
                    if(callBack != null){
                        callBack.execute(obj);
                    }
                }
                callBacks.clear();
            }
        }
    };
    
    public static void loadPublicInfofrombubble(final Context context, final String phoneNum, final SdkCallBack sdkCallBack) {
        if(TextUtils.isEmpty(phoneNum)){
            return;
        }
        final HashMap<String, PublicLoadRequest> queryList = getQueryList();
        
        if(queryList != null && queryList.containsKey(phoneNum)){
            queryList.get(phoneNum).addCallBack(sdkCallBack);
            return;
        }else{
            PublicLoadRequest loadRe = new PublicLoadRequest(phoneNum, sdkCallBack);
            synchronized (sQueryListLock) {
                if(queryList != null){
                    queryList.put(phoneNum, loadRe);
                }
            }
            publicInfoPool.execute(new RunableQuery(context, phoneNum));
        }
    }
    
    private static class RunableQuery implements Runnable{
        Context context;
        String phoneNum;
        public RunableQuery(Context context,String phoneNum){
            this.context = context;
            this.phoneNum = phoneNum;
        }
        @Override
        public void run() {
                try {
                    SdkCallBack callBack = new SdkCallBack() {
                        @Override
                        public void execute(Object... obj) {
                            try {
                                if (obj == null || obj.length <= 2) {
                                    return;
                                }
                                PublicLoadRequest qr = getQueryList().get(phoneNum);
                                String Oldid = (String) obj[2];
                                String result = (String) obj[1];
                                Integer status = (Integer) obj[0];
                                if (status == 0 && !StringUtils.isNull(result) && phoneNum.equals(Oldid)) {
                                    JSONObject json = new JSONObject(result);
                                    saveJsonToCache(json, phoneNum);
                                    String logoName = json.optString("logoc");
                                    String name = json.optString("name");
                                    BitmapDrawable bd = findLogoByLogoName(logoName, null);
                                    qr.noyifyCallBack(phoneNum, name, logoName, bd);
                                }
                                synchronized (sQueryListLock) {
                                    getQueryList().remove(phoneNum);
                                }
                            } catch (Throwable e) {
                                SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                            }
                        }
                    };
                    Map<String, String> extend = new HashMap<String, String>();
                    extend.put("id", phoneNum);
                    ParseManager.queryPublicInfoWithId(context, phoneNum, 1, "", extend, callBack);

                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                }
            }
    }

    public static JSONObject getPublicInfoByPhoneIncache(String phone) {
        return getPublicInfoByPhoneIncache(phone, false);
    }

    public static JSONObject getPublicInfoByPhoneIncache(String phone, boolean isloadAsynchronous) {
        if (StringUtils.isPhoneNumber(phone))
            return null;
        JSONObject json = null;
        phone = StringUtils.getPhoneNumberNo86(phone);
        /* SDK-449 zhaojiangwei 20160524 start */
        String[] phoneValues = phonePublicIdData.get(phone);
        String purpose = "";
        if (phoneValues != null && phoneValues.length > 0) {
            String publicId = phoneValues[0];
            if (phoneValues.length > 1) {
                purpose = phoneValues[1];
            }
            json = publicInfoData.get(publicId);
        }
        /* SDK-449 zhaojiangwei 20160524 end */
        if (json == null && isloadAsynchronous) {
            final String queryPhone = phone;
            publicInfoPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadPublicInfo(Constant.getContext(), queryPhone);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
                    }
                }
            });
        }
        /* SDK-449 zhaojiangwei 20160524 start */
        if (json != null && !StringUtils.isNull(purpose)) {
            try {
                json.put("purpose", purpose);
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
            }
        }
        /* SDK-449 zhaojiangwei 20160524 end */
        return json;
    }

    public static void saveJsonToCache(JSONObject json, String phoneNum) {
        try {
            if (json == null)
                return;
            String publicId = json.optString("id");
            json.remove("classifyName");
            json.remove("classifyCode");
            json.remove("email");
            json.remove("weiboName");
            json.remove("weiboUrl");
            json.remove("weixin");
            json.remove("website");
            json.remove("moveWebSite");
            json.remove("pubnum");
            if (!StringUtils.isNull(publicId)) {
                publicInfoData.put(publicId, json);
                /* SDK-449 zhaojiangwei 20160524 start */
                String[] numValues = { publicId };
                phonePublicIdData.put(phoneNum, numValues);
                /* SDK-449 zhaojiangwei 20160524 end */

            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

    }

    public static synchronized HashMap<String, String> getExtend(boolean isAsync) {
        if (isAsync) {
            return null;
        }
        if (extend == null) {
            extend = new HashMap<String, String>();
            extend.put("syn", "true");
        }
        return extend;
    }

    public static BitmapDrawable findLogoByLogoName(String logoName, final SdkCallBack callBack) {
        BitmapDrawable bd = null;
        try {
            if (StringUtils.isNull(logoName) || !ParseManager.isInitData()) {
                return null;
            }
            bd = getLogoDrawable(logoName);
            if (bd != null) {
                return bd;
            }
            final String localLogoName = logoName;
            SdkCallBack logoCallback = new SdkCallBack() {
                public void execute(Object... obj) {
                    if (obj != null && obj.length > 0) {
                        if (obj[0] != null) {
                            putLogoDrawable(localLogoName, (BitmapDrawable) obj[0]);
                            if (callBack != null) {
                                callBack.execute(localLogoName, obj[0]);
                            }
                        }
                    }
                }
            };
            bd = ParseManager.findLogoByLogoName(Constant.getContext(), logoName, IMAGE_WIDTH, IMAGE_HIGHT, 1,
                    getExtend(true), logoCallback);
            putLogoDrawable(localLogoName, bd);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

        return bd;
    }

    public static synchronized HashMap<String, String> getExtend () {
        if(extend == null){
            extend = new HashMap<String, String>();
            extend.put("syn", "true");
        }
        return (HashMap<String, String>) extend;
    }

    public static BitmapDrawable findLogoByLogoName(String logoName) {
        
        return findLogoByLogoName(logoName, null);
    }
    
    
    public static String getValueByKey(String phone, String key) {
        return getValueByKey(phone, key, true);
    }

    public static String getValueByKey(String phone, String key, boolean isloadAsynchronous) {
        try {
            if (StringUtils.isNull(phone) || StringUtils.isPhoneNumber(phone) || StringUtils.isNull(key))
                return null;
            phone = StringUtils.getPhoneNumberNo86(phone);
            JSONObject json = getPublicInfoByPhoneIncache(phone, isloadAsynchronous);
            if (json != null) {
                return json.optString(key);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

        return null;
    }

    /* COOLPAD-260 huangzhiqiang 20160816 start */
    public static void removePublicInfoDataCache(String pubId) {
        publicInfoData.remove(pubId);
    }

    public static void removePhonePublicIdDataCache(String num) {
        phonePublicIdData.remove(num);
    }

    public static String getPubIdByNum(String num){
        String[] pubInfo = phonePublicIdData.get(num);
        if (pubInfo == null || pubInfo.length < 1) {
            return null;
        }
        return pubInfo[0];
    }
    /* COOLPAD-260 huangzhiqiang 20160816 end */
}
