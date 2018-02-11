package com.mst.interception;
  
import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;   
import java.net.HttpURLConnection;  
import java.net.URL; 
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
// Aurora xuyong 2015-08-29 modified for bug #15926 start
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
// Aurora xuyong 2015-08-29 modified for bug #15926 end
import android.util.Log;
import android.os.SystemProperties;
  
public class InterceptionUtils {  
    private static final String TAG = "InterceptionUtils";
    
    private static Map<String, Boolean> mCache =  new HashMap<String, Boolean>();;

    private static Uri black_uri = Uri.parse("content://com.android.contacts/black");
    
    private static final String[] BLACK_PROJECTION = new String[] {
    	"_id",   //唯一标示，递增
    	"isblack",   // 标记黑白名单（0: 白名单/1:黑名单）
    	"lable",    //通话记录表中获取的标记String, 或添加黑名单时直接搜搜狗获取的标记
    	"black_name",  // 黑名单中的名字
    	"number", //号码
    	"reject" //标示是否拦截通话，短信（0：不拦截/ 1：拦截通话/2:拦截短信/3同时拦截通话、短信）
    };
        
    public static boolean isBlackNumber(Context context, String number) {
		Log.v("isBlackNumber", " number = " + number);
		
		if(!isSupportBlack() || TextUtils.isEmpty(number)) {
			return false;
		}
		
		Cursor cursor = context.getContentResolver().query(black_uri, BLACK_PROJECTION,
		"(reject = '1' OR reject = '3') AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
		Log.v("isBlackNumber", " cursor = " + cursor);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				mBlackName = cursor.getString(3);
				return true;
			}
	    	return false;
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
    }
    
    private static String mBlackName;
    public static String getLastBlackName() {
    	return mBlackName;
    }
    
    public static boolean isToAddBlack(Context context, String number) {
 		 Log.v("isToAddBlack", " number = " + number);
    	
    	
		if(!isSupportBlack() || TextUtils.isEmpty(number)) {
			return false;
		}
		
//		if(AuroraPrivacyUtils.isSupportPrivate()) {
//			int[] privateData = new int[3];
//			privateData = AuroraPrivacyUtils.getPrivateData(number);
//	        long privateId = privateData != null ? privateData[1] : 0; 
//			if(privateId > 0) {
//	            return false ;
//			}				
//		}
		
    	 String[] projection = {CallLog.Calls.NUMBER};
    	 Uri uri = Uri.withAppendedPath(CallLog.Calls.CONTENT_FILTER_URI, Uri.encode(number));
    	 Cursor cursor = context.getContentResolver().query(uri, projection,
                 CallLog.Calls.TYPE + "='" + CallLog.Calls.INCOMING_TYPE + "' AND " + CallLog.Calls.DURATION + "='0' AND " + CallLog.Calls.DATE + " > '" + (System.currentTimeMillis() - 24 * 3600 * 1000 ) + "'"  , null, null);
    	 if(cursor != null) {
    		 Log.v("isToAddBlack", " cursor = " + cursor.getCount());
    	 }
		try {
	    	if (cursor != null && cursor.getCount() >= 3) {
	    		if(!isBlackNumber(context, number)) {
	    			Log.v("isToAddBlack", " number = " + number);
	    		    return true;
	    		}
	    	}
	    	return false;
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
    }       
        
    
    public static boolean isSupportBlack() {
        return true;
    }   
    
    
//    public static boolean isAuroraNeedHangup(String number, boolean notify) {
//    	boolean result = false;
////    	if(mCache.get(number) != null) {
////			result = mCache.get(number);
////    		Log.v("isAuroraNeedHangup", "cache result = " + result );
////    		return result;
////    	}
//		if(AuroraPrivacyUtils.isSupportPrivate()) {
//			int[] privateData = new int[3];
//			privateData = AuroraPrivacyUtils.getPrivateData(number);
//			long currentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
//	        long privateId = privateData != null ? privateData[1] : 0; 
//	        int private_noti_type = privateData != null ? privateData[2] : 0; 
//			if(privateId != currentPrivateId && privateId > 0 && private_noti_type == 1) {
//		    	Log.v(TAG, "isPrivateHangup true "); 
//		    	if(notify) {
//		    		PhoneGlobals.getInstance().mManagePrivate.notificationMgr.notifyHangupPrivateRingingCallFake(privateId);
//		    	}
//		    	result = true;
//			} else if(privateId == 0 && isBlackNumber(number)) {
//		    	if(notify) {
//		    		PhoneGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackCall(number);
//		    	}
//		    	result = true;
//	        }
//		} else if(isBlackNumber(number)) {
//	    	if(notify) {
//	    		PhoneGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackCall(number);
//	    	}
//	    	result = true;
//		}	
////		mCache.put(number, result);
//		Log.v("isAuroraNeedHangup", "result = " + result );
//		return result;
//    }
    
    public static void reset() { 
    	Log.v(TAG, "reset "); 
    	mCache.clear();
	}

}  