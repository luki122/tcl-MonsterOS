package cn.com.xy.sms.sdk.ui.notification;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.util.StringUtils;

public abstract class BaseDropNotificationView {

    private static final String TAG = "BaseDropNotificationView";
    protected RemoteViews mRemoteViews = null;
    MessageItem mMessage = null;
    private final int DUOQU_DROP_BUTTON_ONE_CLICK_MAX_ID = 199999;
    private final int DUOQU_DROP_BUTTON_TWO_CLICK_MAX_ID = 299999;
    private final int DUOQU_DROP_LAYOUT_CLICK_MAX_ID = 399999;
    private final int DUOQU_DROP_READ_BUTTON_CLICK_MAX_ID = 499999;
    static int mRequestBtnOneClick = 100000;
    static int mRequestBtnTwoClick = 200000;
    static int mRequestLayoutClick = 300000;
    static int mRequestReadBtnClick = 400000;

    public RemoteViews getRemoteViews(Context ctx) {
        if (ctx == null) {
            return null;
        }
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            mRemoteViews = new RemoteViews(ctx.getPackageName(), layoutId);
        }

        return mRemoteViews;
    }

    public void bindViewData(Context ctx, Bitmap logoBitmap, String contentTitle, String contentText,
            JSONArray buttonName, MessageItem message) {
        if (mRemoteViews == null || message == null || ctx == null) {
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        String contentTime = formatter.format(curDate);

        this.mMessage = message;
        setLogoBitmap(logoBitmap);
        setContentTitle(contentTitle);
        setContentTime(contentTime);
        setContentText(contentText);
        initButtonListener(ctx, message, buttonName);

    }

    protected void setContentTime(String contentTime) {
        mRemoteViews.setTextViewText(R.id.duoqu_drop_time, contentTime);
    }

    protected void setLogoBitmap(Bitmap logoBitmap) {
        mRemoteViews.setImageViewBitmap(R.id.duoqu_drop_logo_img, logoBitmap);
    }

    protected void setContentTitle(String contentTitle) {
        mRemoteViews.setTextViewText(R.id.duoqu_drop_content_title, contentTitle);
    }

    protected void setContentText(String contentText) {
        mRemoteViews.setTextViewText(R.id.duoqu_drop_content_text, contentText);
    }

    protected void initButtonListener(Context ctx, MessageItem message, JSONArray btnName) {
        if (btnName == null || btnName.length() < 1) {
            return;
        }
        if (mRequestLayoutClick == DUOQU_DROP_LAYOUT_CLICK_MAX_ID) {
            mRequestLayoutClick = 300000;
        }
        if (mRequestReadBtnClick == DUOQU_DROP_READ_BUTTON_CLICK_MAX_ID) {
            mRequestReadBtnClick = 400000;
        }
        if (mRequestBtnOneClick == DUOQU_DROP_BUTTON_ONE_CLICK_MAX_ID) {
            mRequestBtnOneClick = 100000;
        }
        if (mRequestBtnTwoClick == DUOQU_DROP_BUTTON_TWO_CLICK_MAX_ID) {
            mRequestBtnTwoClick = 200000;
        }
        // 按钮点击事件
        /* QIK-585/yangzhi/2016.06.30---start--- */
        if (btnName.length() == 1) {
            mRemoteViews.setOnClickPendingIntent(
                    R.id.duoqu_drop_single_btn_ll,
                    getNotifyActionIntent(ctx, mRequestBtnOneClick++, message,
                            btnName.optJSONObject(0).optString("action_data")));

        } else if (btnName.length() == 2) {
            mRemoteViews.setOnClickPendingIntent(
                    R.id.duoqu_drop_btn_two_ll,
                    getNotifyActionIntent(ctx, mRequestBtnOneClick++, message,
                            btnName.optJSONObject(0).optString("action_data")));

            mRemoteViews.setOnClickPendingIntent(
                    R.id.duoqu_drop_btn_three_ll,
                    getNotifyActionIntent(ctx, mRequestBtnTwoClick++, message,
                            btnName.optJSONObject(1).optString("action_data")));
        }
        /* QIK-585/yangzhi/2016.06.30---end--- */

    }

    protected static String getButtonName(JSONObject btnDataJson) {
        if (btnDataJson == null) {
            return "";
        }

        String buttonName = btnDataJson.optString("btn_short_name");
        if (StringUtils.isNull(buttonName)) {
            buttonName = btnDataJson.optString("btn_name");
        }
        return buttonName;
    }

    protected PendingIntent getNotifyActionIntent(Context context, int id, MessageItem message, String actionType) {
        if (StringUtils.isNull(actionType)) {
            return null;
        }

        Intent contentIntent = new Intent();
        contentIntent.setClassName(context, DoActionActivity.class.getName());
        contentIntent.putExtra("action_data", actionType);
        contentIntent.putExtra("msgId", message.mMsgId);
        PendingIntent pendIntent = PendingIntent.getActivity(context, id, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendIntent;
    }

    protected PendingIntent getHasReadBtnActionIntent(Context context, int id, MessageItem message, String actionType) {
        Intent contentIntent = new Intent(actionType);
        contentIntent.putExtra("message", message);
        PendingIntent pendIntent = PendingIntent.getBroadcast(context, id, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendIntent;
    }

    /**
     * 布局文件ID
     * 
     * @return
     */
    protected int getLayoutId() {
        return R.layout.duoqu_drop_notification;
    }

    /**
     * Debug输出Log信息
     * 
     * @param tag
     * @param msg
     */
    protected void debugLog(String msg) {
        LogManager.i(TAG, msg);
    }

}
