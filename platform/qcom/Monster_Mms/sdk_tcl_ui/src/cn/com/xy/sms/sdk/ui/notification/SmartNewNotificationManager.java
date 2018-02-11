package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class SmartNewNotificationManager {
    
    public static final int REQUEST_TYPE_LAYOUT= 1;
    public static final int REQUEST_TYPE_BTN_CLICK= 2;
    private static int mRequestBtnClick = 100000;
    private static int mRequestLayoutClick = 300000;
  
    public static final int DATATYPE_FLAG_HAVE_READ = 1 ;
    public static final int DATETYPE_FLAG_BTN_CONTENT = 0 ;
    
    public static boolean bindSmartNotifyView(Context context,
            Map<String,Object> smartResultMap,
            long msgId,
            String phoneNum,
            String msg,
            HashMap<String,String> extend){
        if(smartResultMap == null){
            return false;
        }
        try{
            String _title = (String) smartResultMap.get("view_content_title");
            String _text = (String) smartResultMap.get("view_content_text");
            if (StringUtils.isNull(_title)||_title.equals("NO_TITLE")) {
                _title=phoneNum;
            }
            if (StringUtils.isNull(_text)) {
                _text = msg.trim();
            }
          Notification.Builder nBuilder = new Notification.Builder (context) ;
          nBuilder.setSmallIcon(R.drawable.duoqu_notifycation_default);
          nBuilder.setContentTitle(_title);
          nBuilder.setContentText(_text);
          int notificationId = 0; 
          try {
              if(extend!=null && !StringUtils.isNull(extend.get("notificationId"))){
                  notificationId = Integer.valueOf(extend.get("notificationId"));   
              }else{
                  notificationId = (int) msgId;
              }   
          } catch (Exception e) {
              SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager notificationId  ERROR: "+e.getMessage(), e);
          }
          bindAction(context,nBuilder,smartResultMap,msgId,phoneNum,msg,extend,notificationId);
          nBuilder.setAutoCancel(true);
          nBuilder.setDefaults(Notification.DEFAULT_ALL);
          nBuilder.setPriority(Notification.PRIORITY_MAX);
          getNotificationManager(context).notify(notificationId, nBuilder.build());
        }catch(Throwable e){
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager  ERROR: "+e.getMessage(), e);
        }
        return true;
    }
    

    
    public static Bitmap drawableToBitmap(Drawable drawable) {    
        int width = drawable.getIntrinsicWidth();    
        int height = drawable.getIntrinsicHeight();    
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);    
        Canvas canvas = new Canvas(bitmap);    
        drawable.setBounds(0, 0, width, height);    
        drawable.draw(canvas);    
        return bitmap;    
         
 }
    private static NotificationManager mNotifyManager = null ;
    private static NotificationManager getNotificationManager(Context context) {
        if (mNotifyManager == null) {
            mNotifyManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotifyManager;
    }
    
    
    @SuppressLint("NewApi")
    private static void bindAction(Context context,Notification.Builder nBuilder,Map<String, Object> smartResultMap,long msgId,
            String phoneNum,
            String msg,
            HashMap<String,String> extend
            ,int notificationId){
        if(nBuilder == null){
            return;
        }
        JSONArray actionArr = getActionData(smartResultMap);
        nBuilder.addAction(createAction(context,getRequestCode(REQUEST_TYPE_BTN_CLICK),null,msgId,(String)smartResultMap.get("threadId"),notificationId,extend));
        if(actionArr != null){
            int len = actionArr.length();
            if (len>0) {
                String threadId = (String)smartResultMap.get("threadId");
                nBuilder.addAction(createAction(context,getRequestCode(REQUEST_TYPE_BTN_CLICK),actionArr.optJSONObject(0),msgId,threadId,notificationId,extend));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @SuppressLint("NewApi")
    private static Notification.Action createAction(Context ctx,int requestCode, JSONObject actionData, long msgId,String threadId,int notificationId,HashMap<String,String> extend) {
        
        if (actionData == null ) {
            PendingIntent clickPendingIntent = getNotifyActionIntent(ctx,
                    requestCode, null, 
                    DATATYPE_FLAG_HAVE_READ,
                    null, msgId, threadId,notificationId,extend);
             return new Notification.Action.Builder(
                    0, ChannelContentUtil.NOTIFICATION_FLAG_READ, clickPendingIntent).build();
        }else {
            PendingIntent clickPendingIntent = getNotifyActionIntent(ctx,
                    requestCode, actionData.optString("action_data"), 
                    DATETYPE_FLAG_BTN_CONTENT,
                    null, msgId, threadId,notificationId,extend);
            return new Notification.Action.Builder(
                    0, getButtonName(actionData), clickPendingIntent).build();
        }

    }
    
    private static JSONArray getActionData(Map<String, Object> smartResultMap) {
        try {
            String adAction = (String) smartResultMap.get("ADACTION");
            if (!StringUtils.isNull(adAction)) {
                 return new JSONArray(adAction);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager.getButtonName ERROR: "+e.getMessage(), e);
        }
        return null;
    }
    private static PendingIntent getNotifyActionIntent(Context context, int id,
            String actionData, int actionType, String hwParseTime, long msgId, String threadId,int notificationId,HashMap<String,String> extend) {//String actionType
        Intent contentIntent = new Intent();
        contentIntent.setClassName(context,DoActionActivity.class.getName());
        contentIntent.putExtra("thread_id", threadId);
        contentIntent.putExtra("action_data", actionData);
        contentIntent.putExtra("notificationId", notificationId) ;
        contentIntent.putExtra("extend", extend);
        contentIntent.putExtra("action_type", actionType);
//        contentIntent.putExtra("msgId", msgId);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendIntent = PendingIntent.getActivity(context, id, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT, null);
        return pendIntent;
    }
    private static String getButtonName(JSONObject btnDataJson) {
        if (btnDataJson == null) {
            return "";
        }
        String buttonName = btnDataJson.optString("btn_short_name");
        if (StringUtils.isNull(buttonName)) {
            buttonName = btnDataJson.optString("btn_name");
        }
        return buttonName;
    }
    private static synchronized int getRequestCode(int requestType){
        int res = 0;
        if( REQUEST_TYPE_LAYOUT ==requestType){
            if (mRequestLayoutClick == 399999) {
                mRequestLayoutClick = 300000;
            }else{
                mRequestLayoutClick++;
            }
            res = mRequestLayoutClick;
        }
        else if( REQUEST_TYPE_BTN_CLICK  ==requestType){
            if (mRequestBtnClick == 299999) {
                mRequestBtnClick = 200000;
            }else{
                mRequestBtnClick++;
            }
            res = mRequestBtnClick;
        }
        return res;
    }
    
}
