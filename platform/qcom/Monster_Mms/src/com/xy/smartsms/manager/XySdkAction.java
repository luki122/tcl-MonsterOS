package com.xy.smartsms.manager;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Handler;
import mst.provider.Telephony.Mms;
import mst.provider.Telephony.MmsSms;
import mst.provider.Telephony.Sms;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.mms.R;
import com.android.mms.transaction.SmsMessageSender;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.popu.util.UIConstant;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.log.LogManager;

public class XySdkAction extends AbsSdkDoAction { 
    public static final String TAG = "XIAOYUAN";

    //lichao add in 2016-11-29
    public static final String MAP_APP_PREFIX = "geo:";
	
    @Override
    public JSONObject getContactObj(Context context, String phoneNum) {
        //通讯录匹配名称
        String friendsName = null;
        JSONObject obj = new JSONObject();
        try {
            //使用phoneNum匹配通讯录，如果存在则参考以下实现
            if (friendsName != null) {
                obj.put(UIConstant.CONTACT_TYPE, UIConstant.CONTCAT_TYPE_FRIEND);
                obj.put(UIConstant.CONTACT_NAME, friendsName);
            } else {
                //使用phoneNum匹配通讯录，如果不存在则参考以下实现
                obj.put(UIConstant.CONTACT_TYPE, UIConstant.CONTCAT_TYPE_UNKNOW);
                obj.put(UIConstant.CONTACT_NAME, "陌生人");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    @Override
    public List<JSONObject> getReceiveMsgByReceiveTime(
	        String phone, 
		    long startReceiveTime, 
		    long endReceiveTime, 
		    int limit) {
        List<JSONObject> jsonList = null;
        String[] projection = new String[]{"_id", "address", "body", "service_center", "date"};
        StringBuffer sbSelection = new StringBuffer(" date > ");
        sbSelection.append(startReceiveTime);
        sbSelection.append("  and date < ");
        sbSelection.append(endReceiveTime);
        //String selection = " date > "+startReceiveTime +"  and date < "+endReceiveTime;
        String[] selectionArgs = null;
        if (!StringUtils.isNull(phone)) {
            sbSelection.append(" and address = ? ");
            selectionArgs = new String[]{phone};
        }
        Cursor cusor = null;
        try {
            cusor = Constant.getContext().getContentResolver()
			    .query(Uri.parse("content://sms/inbox"),projection, sbSelection.toString(), selectionArgs, "date desc LIMIT " + limit + " OFFSET 0");
            if (cusor != null && cusor.getCount() > 0) {
                jsonList = new ArrayList<JSONObject>();
                JSONObject smsJson = null;
                while (cusor.moveToNext()) {
                    smsJson = new JSONObject();
                    smsJson.put("msgId", cusor.getString(0));
                    smsJson.put("phone", cusor.getString(1));
                    smsJson.put("msg", cusor.getString(2));
                    smsJson.put("centerNum", cusor.getString(3));
                    smsJson.put("smsReceiveTime", cusor.getString(4));
                    jsonList.add(smsJson);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (cusor != null) {
                cusor.close();
	            cusor = null;
            }
        }
        return jsonList;
    }

    /**
     * 发短信,
     * 双卡需要指定卡位发短信.
     * @param phoneNum  接收者号码
     * @param sms  短信内容
     * @param simIndex 卡位 此值需要在  调用接口函数方法的时候 在其extend函数中.加入simIndex的key将当前sim卡位传入. -1表示没有传入卡位
     * @param params 扩展参数
     */
    @Override
    public void sendSms(Context context, String phoneNum, String sms, 
	        int simIndex, Map<String, String> params) {

        Log.d(TAG, "sendSms, simIndex=" + simIndex);

        int subId = -1;

        /*slotId*/
        if (simIndex == -1) {
            subId = SubscriptionManager.getDefaultDataSubscriptionId();
        }else{
            subId = SubscriptionManager.getSubId(simIndex)[0];
        }

        if (subId == -1) {
            Toast.makeText(context, R.string.no_sim_card, Toast.LENGTH_SHORT).show();
            return;
        }

        //Toast.makeText(context, "发送短信: phoneNum:"+phoneNum+" sms:"+sms, Toast.LENGTH_SHORT).show();

        // begin tangyisen
        // long threadId = Conversation.getOrCreateThreadId(context, phoneNum);
        long threadId = Conversation.getOrCreateThreadId(context, phoneNum, subId);
        // end tangyisen

		//String[] dests = new String[]{"" + phoneNum};
        String[] dests = TextUtils.split(phoneNum, ";");

		//if (TelephonyManager.getDefault().isMultiSimEnabled()) {
        SmsMessageSender smsMessageSender = new SmsMessageSender(context, dests, sms, threadId, subId);

		try {
			// This call simply puts the message on a queue and sends a broadcast to start
			// a service to send the message. In queing up the message, however, it does
			// insert the message into the DB.
			smsMessageSender.sendMessage(threadId);
		} catch (Exception e) {
			Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
		}
    }

    @Override
    public void openSms(Context context, String phoneNum, Map<String, String> params) {
        //打开短信原文，需要开发者实现

    }

    @Override
    public String getContactName(Context context, String phoneNum) {
        //暂不需要实现
        return null;
    }

    public static int mCurrentConvSubId = 0;
    public static void setSelectedSimId(int simId) {
        mCurrentConvSubId = simId;
    }
    public void setWorkingMessageSub(int subId) {
        mCurrentConvSubId = subId;
    }

    /**
     * @param simIndex 0:卡1 1:卡2
     */
    @Override
    public String getIccidBySimIndex(int simIndex) {
        Log.d(TAG, "getIccidBySimIndex, simIndex=" + simIndex);
        //需要开发者实现 index 卡1：0 卡2:1
        List<SubscriptionInfo> subInfoList = SubscriptionManager.from(Constant.getContext()).getActiveSubscriptionInfoList();
        //final int phoneId = i;
        if (subInfoList != null) {
            for (SubscriptionInfo info : subInfoList) {
                if (info.getSimSlotIndex() == simIndex) {
                    return info.getIccId();
                }
            }
        }
        return String.valueOf(simIndex);
    }

    @Override
    public void markAsReadForDatabase(Context context, String msgId) {
        //需要开发者实现，标记已读
        //Uri messageUri = Mms.CONTENT_URI.buildUpon().appendPath(msgId).build();
        Uri messageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, Long.valueOf(msgId));
        Log.d(TAG, "deleteMsgForDatabase,  messageUri = "+messageUri);
        MessageUtils.markAsRead(context, messageUri);
    }

    @Override
    public void deleteMsgForDatabase(Context context, String msgId) {
        //需要开发者实现，根据msgId 删除短信
        //long msgId = cursor.getLong(COLUMN_ID)
        //Uri messageUri = Sms.CONTENT_URI.buildUpon().appendPath(msgId).build();
        Uri messageUri_Mms = ContentUris.withAppendedId(Mms.CONTENT_URI, Long.valueOf(msgId));
        Uri messageUri_Sms = ContentUris.withAppendedId(Sms.CONTENT_URI, Long.valueOf(msgId));
        Log.d(TAG, "deleteMsgForDatabase,  messageUri_Mms = "+messageUri_Mms);
        Log.d(TAG, "deleteMsgForDatabase,  messageUri_Sms = "+messageUri_Sms);
        //int count_Mms = SqliteWrapper.delete(context, context.getContentResolver(), messageUri_Mms, null, null);
        int count_Sms = SqliteWrapper.delete(context, context.getContentResolver(), messageUri_Sms, null, null);
    }

    /**
     * 打开单个短信原文
     *
     * @param context
     * @param msgId
     * @param extend 扩展参数
     */
    @Override
    public void openSmsDetail(Context context, String msgId, Map extend) {
        Log.d(TAG, "openSmsDetail,  msgId = "+msgId);
        //需要开发者实现 打开短信详情
        //Cursor c = (Cursor) getListView().getAdapter().getItem(mSelectedPos.iterator().next());
        //String type = c.getString(COLUMN_MSG_TYPE);
        //if (type.equals("sms")) {
            // this only for sms
            MessageUtils.showSmsMessageContent(context, Long.valueOf(msgId));
        //} else if (type.equals("mms")) {
        //    MessageUtils.viewMmsMessageAttachment(
        //            ComposeMessageActivity.this, mSelectedMsg.get(0), null,
        //            new AsyncDialog(ComposeMessageActivity.this));
        //}

//        Uri.Builder builder = Mms.CONTENT_URI.buildUpon();
//        builder.appendPath(msgId).appendPath("addr");
//        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
//                builder.build(), new String[] {Addr.ADDRESS, Addr.CHARSET},
//                Addr.TYPE + "=" + PduHeaders.FROM, null, null);
    }
    /* linweijie test start */
    /*
	@Override
    public void openMap(Context context, String title, String address, double lon, double lat) {
        // TODO Auto-generated method stub
        Log.d(TAG, "openMap, title = "+title+", address = "+address+", lon = "+lon+", lat = "+lat);
        try {
            if (!StringUtils.isNull(title)) {
                title = URLEncoder.encode(title, "utf-8");
            }
            else {
                title = "";
            }
            // LogManager.i("openMap", "new address=" + address);

            if (!StringUtils.isNull(address)) {
                address = address.replace(" ", ",").replace("（", ",").replace("(", ",").replace("，", ",")
                        .replace("）", "").replace(")", "").replace("?", "").replace("&", "").replace("#", "").trim();
                address = URLEncoder.encode(address, "utf-8");

                // 尝试调用本机地图软件客户端
                Uri dataUri = Uri.parse(MAP_APP_PREFIX + lat + "," + lon + "?q=" + address);
                //dataUri = geo:0.0,0.0?q=%E9%93%B6%E8%A1%8C
                Log.d(TAG, "openMap, try to open local Map APP, dataUri = "+dataUri);
                //src/com/android/mms/ui/ComposeMessageActivity.java
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(dataUri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                // context.finish();
            }
        } catch (Throwable e) {
            // e.printStackTrace();
            //LogManager.e(Constant.TAG, "openMap: ", e);
            Log.d(TAG, "openMap, Throwable: "+e);
            try {
                if (!StringUtils.isNull(address)) {
                    String uriStr = "";
                    if (lon != 0 && lat != 0) {
                        // 地图标点功能，调用该接口可调起PC或web地图，且在指定坐标点上显示点的名称和内容信息。
                        uriStr = "http://api.map.baidu.com/marker?location=" + lat + "," + lon + "&title=" + title
                                + "&content=" + address;
                    }
                    else {
                        // 地址解析，调用该接口可以在调起百度地图时，当前页面显示地址对应的坐标点。
                        uriStr = "http://api.map.baidu.com/geocoder?address=" + address;
                    }
                    Uri uri = Uri.parse(uriStr + "&output=html&src=xiaoyuan|多趣");
                    Log.d(TAG, "openMap, try to open browser 111, uri = "+uri.toString());

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);//调用浏览器跳转
                    // context.finish();
                }
                else if (lon != 0 && lat != 0) {
                    // 反向地址解析，调用该接口可调起PC或Web百度地图，经过逆地理编码后，以标注形式显示位置和地址信息。
                    Uri uri = Uri.parse("http://api.map.baidu.com/geocoder?location=" + lat + "," + lon
                            + "&coord_type=gcj02&output=html&src=xiaoyuan|多趣");
                    Log.d(TAG, "openMap, try to open browser 222, uri = "+uri);

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
            catch (Throwable e2) {
                LogManager.w(Constant.TAG, "openMap: ", e2);
            }

        }
    }
    */
	/* linweijie test end */
    /**
     * 重写定位
     */
    @Override
    public void getLocation(Context context, Handler handler) {
        //MapLocation.getLocation(context, handler);
    }

}
