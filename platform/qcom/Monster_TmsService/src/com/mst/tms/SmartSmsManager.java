package com.mst.tms;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import tmsdk.common.SmsEntity;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;
import tmsdk.common.module.intelli_sms.IntelliSmsManager;

public class SmartSmsManager {
    
    public static final String TAG = "SmartSmsManager";
    
    private static IntelliSmsManager mIntelliSmsManager;
    
    private static synchronized  IntelliSmsManager  getInstance() {        
        if(mIntelliSmsManager == null) {
            mIntelliSmsManager = ManagerCreatorC.getManager(IntelliSmsManager.class);
            //必须初始化,与destroy（）一一对应
            mIntelliSmsManager.init();
        }
        return mIntelliSmsManager;
    }
    
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mIntelliSmsManager.destroy();//必须销毁
//    }
    
    public static boolean canRejectSms(String number, String smscontent){
        Log.d(TAG, "canRejectSms number " +number + ", smscontent = " + smscontent);
        if(TextUtils.isEmpty(number) || TextUtils.isEmpty(smscontent)) {
            return false;
        }
        SmsEntity sms = new SmsEntity();
        sms.phonenum = number;
        sms.body = smscontent;
        // 智能拦截调用接口
        IntelliSmsCheckResult checkresult = null;
        checkresult = getInstance().checkSms(sms, false);//本地查
//        ConnectivityManager manager = (ConnectivityManager) TmsApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);  
//        NetworkInfo activeInfo = manager.getActiveNetworkInfo();  
//        if(activeInfo != null && activeInfo.isConnected()) {
//              checkresult = getInstance().checkSms(sms, true);//支持云查,最好是在wifi情况下云查。
//        }
        if(checkresult != null) {  
            Log.d(TAG, "checkresult.suggestion =  " + checkresult.suggestion);
            return checkresult.suggestion == IntelliSmsCheckResult.SUGGESTION_INTERCEPT 
                    || checkresult.suggestion == IntelliSmsCheckResult.SUGGESTION_DOUBT;
        }
        return false;
    }
}
