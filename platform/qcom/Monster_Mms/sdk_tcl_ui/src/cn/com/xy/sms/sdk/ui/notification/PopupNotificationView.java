package cn.com.xy.sms.sdk.ui.notification;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.util.StringUtils;

public class PopupNotificationView extends BasePopupNotificationView {

    @Override
    public void bindViewData(Context ctx, Bitmap logoBitmap, String contentTitle, String contentText,
            JSONArray adAction, MessageItem message) {
        super.bindViewData(ctx, logoBitmap, contentTitle, contentText, adAction, message);
        /* QIK-605/yangzhi/2016.07.13---start--- */
        mRemoteViews.setViewVisibility(R.id.duoqu_popup_horizontal_line, View.GONE);
        mRemoteViews.setViewVisibility(R.id.duoqu_popup_btn_layout, View.GONE);
        /* QIK-605/yangzhi/2016.07.13---end--- */
        /* QIK-585/yangzhi/2016.06.30---start--- */
        if (adAction != null && adAction.length() == 2) {
            try {
                mRemoteViews.setViewVisibility(R.id.duoqu_popup_horizontal_line, View.VISIBLE);
                mRemoteViews.setViewVisibility(R.id.duoqu_popup_btn_layout, View.VISIBLE);

                mRemoteViews.setTextViewText(R.id.duoqu_popup_btn_two, getButtonName(adAction.optJSONObject(0)));
                mRemoteViews.setImageViewBitmap(R.id.duoqu_popup_left_logo,
                        DuoquNotificationViewManager.getBitmap(ctx, (String) adAction.optJSONObject(0).get("action")));

                mRemoteViews.setTextViewText(R.id.duoqu_popup_btn_three, getButtonName(adAction.optJSONObject(1)));
                mRemoteViews.setImageViewBitmap(R.id.duoqu_popup_right_logo,
                        DuoquNotificationViewManager.getBitmap(ctx, (String) adAction.optJSONObject(1).get("action")));
                /* QIK-585/yangzhi/2016.06.30---end--- */
            } catch (Throwable e) {
                cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog(
                        "PopupNotificationView bindViewData error:", e);
            }
        }

    }

    protected static String getButtonName(JSONObject btnDataJson) {
        if (btnDataJson == null) {
            return "";
        }

        String buttonName = btnDataJson.optString("btn_short_name");
        Log.e("yangzhi", "PopupNotificationView buttonName: " + buttonName);
        if (StringUtils.isNull(buttonName)) {
            buttonName = btnDataJson.optString("btn_name");
        }
        return buttonName;
    }

}
