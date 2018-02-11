package cn.com.xy.sms.sdk.ui.notification;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;

public class DropNotificationView extends BaseDropNotificationView {

    @Override
    public void bindViewData(Context ctx, Bitmap logoBitmap,
        String contentTitle, String contentText, JSONArray adAction,
            MessageItem message) {
            super.bindViewData(ctx, logoBitmap, contentTitle, contentText,adAction, message);
            /* QIK-605/yangzhi/2016.07.13---start--- */
            mRemoteViews.setViewVisibility(R.id.duoqu_drop_horizontal_line,
                    View.GONE);
            mRemoteViews.setViewVisibility(R.id.duoqu_drop_btn_layout, View.GONE);
            /* QIK-605/yangzhi/2016.07.13---end--- */
            /* QIK-585/yangzhi/2016.06.30---start--- */
            if (adAction != null && adAction.length() == 1) {
                try {
                    // if (adAction.length() == 1) {
                    mRemoteViews.setViewVisibility(R.id.duoqu_drop_time,
                    View.GONE);
                    mRemoteViews.setViewVisibility(R.id.duoqu_drop_single_btn_ll,
                    View.VISIBLE);
                    mRemoteViews.setTextViewText(R.id.duoqu_drop_single_btn,
                    getButtonName(adAction.optJSONObject(0)));
                    /* QIK-585/yangzhi/2016.06.30---end--- */
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("BubbleAirBody setAdditionalInfo error:", e);
                }
            }
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
}
