package cn.com.xy.sms.sdk.ui.popu.util;

import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.widget.AdapterDataSource;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquDialogSelected;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquSourceAdapterDataSource;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

public class TravelDataUtil {
    private static final String TAG = "TravelDataUtil";
    private String mDataIndexKey = null;
    private String mViewContentKeyStart = null;
    private String mInterfaceDataKeyStart = null;
    private String mQueryTimeKeyStart = null;

    public String getDataIndexKey() {
        return mDataIndexKey;
    }

    public String getViewContentKeyStart() {
        return mViewContentKeyStart;
    }

    public String getInterfaceDataKeyStart() {
        return mInterfaceDataKeyStart;
    }

    public String getQueryTimeKeyStart() {
        return mQueryTimeKeyStart;
    }

    public TravelDataUtil(String dataIndexKey, String viewContentKeyStart, String interfaceDataKeyStart,
            String queryTimeKeyStart) {
        mDataIndexKey = dataIndexKey;
        mViewContentKeyStart = viewContentKeyStart;
        mInterfaceDataKeyStart = interfaceDataKeyStart;
        mQueryTimeKeyStart = queryTimeKeyStart;
    }
    /* QIK-634/yangzhi/2016.07.18---start--- */
    public JSONObject getViewContentData(BusinessSmsMessage smsMessage) {
        return getViewContentData(smsMessage, getDefaultSelectedIndex(smsMessage));
    }

    public JSONObject getViewContentData(BusinessSmsMessage smsMessage, int index) {
        if (ContentUtil.bubbleDataIsNull(smsMessage) || getViewContentDataArray(smsMessage) == null) {
            return null;
        }
        try {
            return (JSONObject) getViewContentDataArray(smsMessage).get(index);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }

    public JSONArray getViewContentDataArray(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optJSONArray(getViewContentKeyStart());
    }
    /* QIK-634/yangzhi/2016.07.18---end--- */
    public String getViewContentKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, mViewContentKeyStart);
    }

    public String getInterfaceDataKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, mInterfaceDataKeyStart);
    }

    public long getQueryTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return 0L;
        }
        return smsMessage.bubbleJsonObj.optLong(getQueryTimeKey(smsMessage));
    }

    public String getQueryTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, mQueryTimeKeyStart);
    }

    public String getDataIndex(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return "0";
        }
        String dataIndex = smsMessage.bubbleJsonObj.optString(mDataIndexKey);
        return StringUtils.isNull(dataIndex) ? "0" : dataIndex;
    }

    public int getDefaultSelectedIndex(BusinessSmsMessage smsMessage) {
        int defaultSelectedIndex = 0;
        try {
            defaultSelectedIndex = Integer.parseInt(getDataIndex(smsMessage));
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return defaultSelectedIndex;
    }

    public boolean hasInterfaceData(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return false;
        }
        return smsMessage.bubbleJsonObj.has(getInterfaceDataKey(smsMessage));
    }

    public static boolean hasValue(TextView textView, String noDataValue) {
        if (textView == null) {
            return false;
        }
        String value = textView.getText().toString();
        return !StringUtils.isNull(value) && !value.equals(noDataValue);
    }
    /* QIK-634/yangzhi/2016.07.18---start--- */
    public static void setViewValue(String value, TextView textView, String lostValueShowText,
            ImageView lostValueShowImage) {
        boolean timeIsNull = StringUtils.isNull(value);
        ContentUtil.setViewVisibility(lostValueShowImage, (timeIsNull ? View.VISIBLE : View.GONE));
        ContentUtil.setViewVisibility(textView, (timeIsNull ? View.GONE : View.VISIBLE));
        ContentUtil.setText(textView, value, lostValueShowText);
    }
    /* QIK-634/yangzhi/2016.07.18---end--- */
    public static void setViewValue(String value, TextView textView, String lostValueShowText) {
        boolean timeIsNull = StringUtils.isNull(value);
        ContentUtil.setText(textView, value, lostValueShowText);
    }

    public static void setPopupDialogClickListener(Context ctx, AdapterDataSource adapterDataSource,
            final DuoquDialogSelected dialogSelected, String dialogTitle, final SdkCallBack callBack,
            View... bindListenerViews) {
        if (ctx == null || dialogSelected == null || bindListenerViews == null || bindListenerViews.length == 0
                || adapterDataSource == null || adapterDataSource.getDataSrouce() == null) {
            return;
        }
        Resources res = ctx.getResources();
        OnClickListener clickListener = SelectListDialogUtil.showSelectListDialogClickListener(ctx, dialogTitle,
                res.getString(R.string.duoqu_confirm), res.getString(R.string.duoqu_cancel), adapterDataSource,
                dialogSelected, new SdkCallBack() {

                    @Override
                    public void execute(Object... obj) {
                        ContentUtil.callBackExecute(callBack, obj);
                    }
                });

        ContentUtil.setOnClickListener(clickListener, bindListenerViews);
    }

    public static void setPopupDialogClickListener(Context ctx, final BusinessSmsMessage smsMessage,
            String dialogTitle, AdapterDataSource adapterDataSource, final TravelDataUtil dataUtil,
            final DuoquDialogSelected dialogSelected, final SdkCallBack callBack, View... bindListenerViews) {
        if (ctx == null || smsMessage == null || adapterDataSource == null || adapterDataSource.getDataSrouce() == null
                || adapterDataSource.getDataSrouce().length() < 2 || dataUtil == null || dialogSelected == null
                || bindListenerViews == null || bindListenerViews.length == 0) {
            return;
        }
        dialogSelected.setSelectIndex(dataUtil.getDefaultSelectedIndex(smsMessage));
        setPopupDialogClickListener(ctx, adapterDataSource, dialogSelected, dialogTitle, new SdkCallBack() {
            @Override
            public void execute(Object... obj) {
                if (queryFail(obj)) {
                    return;
                }
                int selectedIndex = (Integer) obj[1];
                if (dialogSelected.getSelectIndex() == selectedIndex) {
                    return;
                }
                dialogSelected.setSelectIndex(selectedIndex);

                JSONObject selectItemData = (JSONObject) obj[0];
                String index = selectItemData.optString(DuoquSourceAdapterDataSource.INDEX_KEY);
                ContentUtil.saveSelectedIndex(smsMessage, dataUtil.getDataIndexKey(), index);

                ContentUtil.callBackExecute(callBack, obj);
            }

            private boolean queryFail(Object... obj) {
                return obj == null || obj.length < 3 || !(obj[0] instanceof JSONObject) || !(obj[1] instanceof Integer)
                        || obj[2] == null;
            }
        }, bindListenerViews);
    }
    /* QIK-634/yangzhi/2016.07.18---start--- */
    public void resetBackgroundResource(ViewGroup root, BusinessSmsMessage smsMessage, int bgResourceId) {
        if (root == null || ContentUtil.bubbleDataIsNull(smsMessage)) {
            return;
        }
        // int bgResourceId = R.drawable.duoqu_pop_bg_gray;

        try {
            Object currentBgResourceId = root.getTag(R.id.tag_bg_resource);
            if (currentBgResourceId != null && (Integer) currentBgResourceId == bgResourceId) {
                return;
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

        root.setTag(R.id.tag_bg_resource, bgResourceId);
        root.setBackgroundResource(bgResourceId);
    }
    /* QIK-634/yangzhi/2016.07.18---end--- */
    public static String getDataByKey(JSONObject viewContentData, String key, String... replaceToEmpty) {
        if (viewContentData == null || StringUtils.isNull(key)) {
            return null;
        }
        String value = viewContentData.optString(key);
        if (StringUtils.isNull(value)) {
            return null;
        }
        if (replaceToEmpty != null) {
            for (String replace : replaceToEmpty) {
                value = value.replace(replace, "");
            }
        }
        return value.trim();
    }

    public boolean dataBelongCurrentMsg(BusinessSmsMessage smsMessage, Object... obj) {
        if (smsMessage == null || obj == null || obj.length < 1) {
            return false;
        }
        String callbackMsgId = obj[0].toString();
        String currentMsgId = String.valueOf(smsMessage.getSmsId());
        return !StringUtils.isNull(currentMsgId) && currentMsgId.equals(callbackMsgId);
    }

    public String getKey(BusinessSmsMessage smsMessage, String keyStart) {
        return keyStart + getDataIndex(smsMessage);
    }

    public static long timeStrTolong(String timeStr) {
        // this.DateFormatConvert(timeStr);
        long mills = 0l;
        if (!StringUtils.isNull(timeStr) && timeStr.contains(":")) {
            String[] time = timeStr.split(":");
            mills = (Integer.valueOf(time[0]) * 60 + Integer.valueOf(time[1])) * 60 * 1000;
        }
        return mills;
    }

    public static long convert2long(String date) {

        if (!StringUtils.isNull(date)) {
            SimpleDateFormat sf = new SimpleDateFormat("MM-dd HH:mm");

            try {
                return sf.parse(date).getTime();
            } catch (Throwable e) {
                // TODO Auto-generated catch block
            }
        }

        return 0l;
    }

}
