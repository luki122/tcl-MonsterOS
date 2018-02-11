package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.widget.AirUISelectDialog;
import cn.com.xy.sms.sdk.ui.popu.widget.AirUISelectDialog.OnBottomClick;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class SimpleButtonUtil {
    private static final String TAG = "SimpleButtonUtil";
    private static final int DRAWABLE_BOUNDS_TOP = (int) ViewUtil.getDimension(R.dimen.duoqu_drawable_bounds_top);
    private static final int DRAWABLE_BOUNDS_RIGHT = (int) ViewUtil.getDimension(R.dimen.duoqu_drawable_bounds_right);
    private static UiPartInterface mUiInterface;

    static {
        mUiInterface = ViewManger.getUiPartInterface();
    }

    public static void setButtonTextAndImg(Context mContext, TextView buttonText, String action, boolean disLogo,
            boolean isClickAble) {
        try {
            if (null == buttonText) {
                return;
            }
            String buttonName = buttonText.getText().toString();
            boolean setText = StringUtils.isNull(buttonName);
            int resLogoId = bindButtonData(buttonText, action, setText, isClickAble);
            if (disLogo && resLogoId != -1) {
                Drawable dw = Constant.getContext().getResources().getDrawable(resLogoId);
                dw.setBounds(0, getTop(), getRight(), getBottom());
                buttonText.setCompoundDrawables(dw, null, null, null);
            } else {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    public static int bindButtonData(TextView buttonText, String action, boolean setText, boolean isClickAble) {
        int resLogoId = -1;
        if (!StringUtils.isNull(action)) {
            if (action.equalsIgnoreCase("url")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_network;
                } else {
                    resLogoId = R.drawable.duoqu_network_disable;
                }
                if (setText)
                    buttonText.setText(R.string.duoqu_open_net);
            } else if (action.equalsIgnoreCase("reply_sms") || action.equalsIgnoreCase("send_sms")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_reply;
                } else {
                    resLogoId = R.drawable.duoqu_reply_disable;
                }

                if (setText)
                    buttonText.setText(R.string.duoqu_reply_sms);
            } else if (action.equalsIgnoreCase("reply_sms_fwd")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_reply;
                } else {
                    resLogoId = R.drawable.duoqu_reply_disable;
                }

                if (setText)
                    buttonText.setText(R.string.duoqu_forword_sms);
            } else if (action.equalsIgnoreCase("call_phone") || action.equalsIgnoreCase("call")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_call;
                } else {
                    resLogoId = R.drawable.duoqu_call_disable;
                }

                if (setText)
                    buttonText.setText(R.string.duoqu_call_phone);
            } else if (action.equalsIgnoreCase("reply_sms_open")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_reply;
                } else {
                    resLogoId = R.drawable.duoqu_reply_disable;
                }
                if (setText)
                    buttonText.setText(R.string.duoqu_open_text);
            } else if (action.equalsIgnoreCase("access_url")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_network;
                } else {
                    resLogoId = R.drawable.duoqu_network_disable;
                }
                if (setText)
                    buttonText.setText(R.string.duoqu_open_net);
            } else if (action.equalsIgnoreCase("down_url")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_download;
                } else {
                    resLogoId = R.drawable.duoqu_download_disable;
                }

                if (setText)
                    buttonText.setText(R.string.duoqu_open_net);
            } else if (action.equalsIgnoreCase("send_email")) {
                resLogoId = R.drawable.duoqu_email;

                if (setText)
                    buttonText.setText(R.string.duoqu_send_email);
            } else if (action.equalsIgnoreCase("map_site") || action.equalsIgnoreCase("open_map_list")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_map;
                } else {
                    resLogoId = R.drawable.duoqu_map_disable;
                }

                if (setText)
                    buttonText.setText(R.string.duoqu_open_map);
            } else if (action.equalsIgnoreCase("chong_zhi") || action.equalsIgnoreCase("recharge")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chongzhi;
                } else {
                    resLogoId = R.drawable.duoqu_chongzhi_disable;
                }

                if (setText)
                    buttonText.setText(R.string.duoqu_chonzhi);
            } else if (action.equalsIgnoreCase("WEB_QUERY_EXPRESS_FLOW")
                    || action.equalsIgnoreCase("WEB_QUERY_FLIGHT_TREND")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chakan;
                } else {
                    resLogoId = R.drawable.duoqu_chakan_disable;
                }

            } else if (action.equalsIgnoreCase("WEB_TRAFFIC_ORDER")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chongzhi;
                } else {
                    resLogoId = R.drawable.duoqu_chongzhi_disable;
                }

            } else if (action.equalsIgnoreCase("WEB_INSTALMENT_PLAN")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chongzhi;
                } else {
                    resLogoId = R.drawable.duoqu_chongzhi_disable;
                }

            } else if (action.equalsIgnoreCase("copy_code")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_copy_code;
                } else {
                    resLogoId = R.drawable.duoqu_copy_code_disable;
                }

            } else if (action.equalsIgnoreCase("zfb_repayment") || action.equalsIgnoreCase("repayment")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chongzhi;
                } else {
                    resLogoId = R.drawable.duoqu_chongzhi_disable;
                }

            } else if (action.equalsIgnoreCase("sdk_time_remind")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_time_remind;

                } else {
                    resLogoId = R.drawable.duoqu_time_remind_disable;
                }

            } else if (action.equalsIgnoreCase("web_train_station")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chakan;

                } else {
                    resLogoId = R.drawable.duoqu_chakan_disable;
                }

            } else if (action.equalsIgnoreCase("open_map")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_map;
                } else {
                    resLogoId = R.drawable.duoqu_map_disable;
                }

            } else if (action.equalsIgnoreCase("pay_water_gas")) {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chongzhi;
                } else {
                    resLogoId = R.drawable.duoqu_chongzhi_disable;
                }

            } else {
                if (isClickAble) {
                    resLogoId = R.drawable.duoqu_chakan;
                } else {
                    resLogoId = R.drawable.duoqu_chakan_disable;
                }
            }
        }

        return resLogoId;
    }

    private static int mTop = 0;
    static int mRight = 0;
    static int mBottom = 0;

    public static int getTop() {
        if (mTop == 0) {
            mTop = DRAWABLE_BOUNDS_TOP;
        }
        return mTop;
    }

    public static int getRight() {
        if (mRight == 0) {
            mRight = DRAWABLE_BOUNDS_RIGHT;
        }
        return mRight;
    }

    public static int getBottom() {
        if (mBottom == 0) {
            mBottom = getRight();
        }
        return mBottom;
    }

    public static void setBotton(final Activity context, View button, final TextView buttonText,
            final JSONArray jsonArray, boolean disLogo, final HashMap<String, Object> extend) throws Exception {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return;
        }
        final JSONObject actionMap = jsonArray.getJSONObject(0);
        final String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
        if (StringUtils.isNull(action)) {
            return;
        }
        setBottonValue(context, buttonText, actionMap, disLogo, true);
        button.setTag(actionMap);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUiInterface == null) {
                    doAction(context, actionMap, extend);
                    return;
                }
                Object obj = mUiInterface.doUiAction(ViewManger.UIPART_ACTION_SHOW_DIALOG, null);
                if (obj ==null || Boolean.FALSE.equals(obj)) {
                    doAction(context, actionMap, extend);
                    return;
                }

                int length = jsonArray.length();
                if (length <= 1) {
                    doAction(context, actionMap, extend);
                    return;
                }
                final AirUISelectDialog selectDialog = new AirUISelectDialog(jsonArray, context, 0);
                selectDialog.ShowDialog(new OnBottomClick() {
                    @Override
                    public void Onclick(int type, int select) {
                        try {
                            if (type == AirUISelectDialog.CONFIRM) {
                                SimpleButtonUtil.doAction(context, jsonArray.getJSONObject(select), extend);
                            }
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                        }
                    }
                });
            }
        });
    }

    public static void setBottonValue(final Activity mContext, final TextView buttonText, final JSONObject actionMap,
            boolean disLogo, boolean isClickAble) {
        if (actionMap == null) {
            return;
        }
        final String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
        if (mUiInterface != null) {
            Object obj = mUiInterface.doUiActionMulti(ViewManger.UIPART_ACTION_SET_BUTTON_TEXT_COLOR, mContext, buttonText,
                    actionMap);
            // mUiInterface.setBubbleText(mContext, buttonText, actionMap);
            if (obj == null || Boolean.FALSE.equals(obj) ) {
                setButtonValue(mContext, buttonText, actionMap, disLogo, action, isClickAble);
            }
        } else {
            setButtonValue(mContext, buttonText, actionMap, disLogo, action, isClickAble);
        }
    }

    private static void setButtonValue(final Activity mContext, final TextView buttonText, final JSONObject actionMap,
            boolean disLogo, final String action, final boolean isClickAble) {
        String btnName = ContentUtil.getBtnName(actionMap);
        if (!StringUtils.isNull(btnName)) {
            if (isClickAble) {
                buttonText.setTextColor(buttonText.getResources().getColor(R.color.duoqu_huawei_text_blue));
            } else {
                buttonText.setTextColor(buttonText.getResources().getColor(R.color.duoqu_huawei_text_disable));
            }
            buttonText.setText(btnName);
        }
    }

    public static void doAction(final Activity mContext, final JSONObject actionMap,
            final HashMap<String, Object> extend) {
        try {
            HashMap<String, String> valueMap = new HashMap<String, String>();
            if (extend != null && !extend.isEmpty()) {
                for (Entry<String, Object> entry : extend.entrySet()) {

                    if (entry.getValue() instanceof String) {
                        valueMap.put(entry.getKey(), (String) entry.getValue());
                    }
                }
            }

            String action_data = (String) JsonUtil.getValueFromJsonObject(actionMap, "action_data");
            DuoquUtils.doAction(mContext, action_data, valueMap);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }
}
