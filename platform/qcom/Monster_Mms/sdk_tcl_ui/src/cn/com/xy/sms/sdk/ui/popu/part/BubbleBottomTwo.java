package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.BottomButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SimpleButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquRelativeLayout;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleBottomTwo extends UIPart {
    
    private boolean             mDisLogo                    = false;
    private View                mDuoqu_bottom_split_line;
    private View                mDuoqu_btn_split_line;
    private View                mBtn1                       = null, mBtn2 = null;
    private TextView            mTextView1                  = null, mTextView2 = null;
    public int[]                mHarr                       = new int[2];
    private DuoquRelativeLayout mdlayout;
    public int                  mSize                       = 0;
    private static final int    DRAWABLE_BOUNDS_TOP         = (int) ViewUtil
            .getDimension(R.dimen.duoqu_drawable_bounds_top);
    private static final int    DRAWABLE_BOUNDS_RIGHT       = (int) ViewUtil
            .getDimension(R.dimen.duoqu_drawable_bounds_right);
    private static final int    DRAWABLE_BOUNDS_BOTTOM      = (int) ViewUtil
            .getDimension(R.dimen.duoqu_drawable_bounds_bottom);
    private static final int    FIRST_TEXTVIEW_PADDING_TOP  = (int) ViewUtil
            .getDimension(R.dimen.duoqu_ui_part_first_textview_padding_bottom);
    private static final int    SECOND_TEXTVIEW_PADDING_TOP = (int) ViewUtil
            .getDimension(R.dimen.duoqu_ui_part_first_textview_padding_bottom);
            
    public BubbleBottomTwo(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
        
    }
    
    @Override
    public void initUi() throws Exception {
        mHarr[0] = Math.round(mContext.getResources().getDimension(R.dimen.bubble_bottom_two_height));
        mHarr[1] = Math.round(mContext.getResources().getDimension(R.dimen.bubble_bottom_two_minheight));
        mDuoqu_bottom_split_line = mView.findViewById(R.id.duoqu_bottom_split_line);
        mDuoqu_btn_split_line = mView.findViewById(R.id.duoqu_btn_split_line);
        mdlayout = (DuoquRelativeLayout) mView.findViewById(R.id.duoqu_bubble_bottom_two);
        mBtn1 = mView.findViewById(R.id.duoqu_btn_1);
        mBtn2 = mView.findViewById(R.id.duoqu_btn_2);
        mTextView1 = (TextView) mView.findViewById(R.id.duoqu_btn_text_1);
        mTextView2 = (TextView) mView.findViewById(R.id.duoqu_btn_text_2);
    }
    
    public void setButtonTextAndImg(TextView buttonText, String action, boolean disLogo) {
        try {
            String buttonName = buttonText.getText().toString();
            boolean setText = StringUtils.isNull(buttonName);
            
            int resLogoId = SimpleButtonUtil.bindButtonData(buttonText, action, setText, true);
            
            if (disLogo && resLogoId != -1) {
                Drawable dw = Constant.getContext().getResources().getDrawable(resLogoId);
                dw.setBounds(0, DRAWABLE_BOUNDS_TOP, DRAWABLE_BOUNDS_RIGHT, DRAWABLE_BOUNDS_BOTTOM);
                buttonText.setCompoundDrawables(dw, null, null, null);
            } else {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
            
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleBottomTwo setButtonTextAndImg error:", e);
        }
        
    }
    
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            try {
                // Processing action
                JSONObject jsonObject = (JSONObject) v.getTag();
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("simIndex", mMessage.simIndex + "");
                valueMap.put("phoneNum", mMessage.originatingAddress + "");
                valueMap.put("content", mMessage.getMessageBody() + "");
                byte viewType = mMessage.viewType;
                valueMap.put("viewType", viewType + "");
                
                String type = jsonObject.optString("action", "");
                if (StringUtils.isNull(type)) {
                    return;
                }
                
                HashMap<String, Object> param = new HashMap<String, Object>();
                param.put("type", BasePopupView.CHANGE_DATA_TYPE_BODY);
                param.put("adjust_data", valueMap);
                
                if ("WEB_QUERY_EXPRESS_FLOW".equals(type)) {  
                    param.put("catagory", "express");
                } else if ("WEB_QUERY_FLIGHT_TREND".equals(type)) {
                    param.put("isFlightState", true);
                }
                    
                JSONObject simpleObj = null;
                try {
                 simpleObj = new JSONObject(mMessage.bubbleJsonObj.toString());
                 simpleObj.remove("NEW_ADACTION");
                 simpleObj.remove("ADACTION");
                 simpleObj.remove("viewPartParam");
                }catch (Throwable e) {
                }
                
                mBasePopupView.changeData(param);
                if (simpleObj!=null) {
                    valueMap.put("bubbleJson", simpleObj.toString());
                }

                JsonUtil.putJsonToMap(jsonObject, valueMap);
                String action_data = (String) JsonUtil.getValueFromJsonObject(jsonObject, "action_data");
                DuoquUtils.doAction(mContext, action_data, valueMap);
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("BubbleBottomTwo OnClickListener error:", e);
            }
            
        }
        
    };
    
    public void setBotton(View button, final TextView buttonText, final JSONObject actionMap, boolean disLogo) {
        if (actionMap == null) {
            return;
        }
        
        final String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
        String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap, "btn_name");
        if (!StringUtils.isNull(btnName)) {
            buttonText.setText(btnName);
            setButtonTextAndImg(buttonText, action, disLogo);
        }
        if (!StringUtils.isNull(action)) {
            button.setTag(actionMap);
            button.setOnClickListener(mOnClickListener);
        }
    }
    
    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (!isRebind) {
            mTextView1.setPadding(0, 0, 0, FIRST_TEXTVIEW_PADDING_TOP);
            mTextView2.setPadding(0, 0, 0, SECOND_TEXTVIEW_PADDING_TOP);
            
            String color = (String) message.getValue("v_bt_bg_1");
            ThemeUtil.setViewBg(mContext, mdlayout, StringUtils.isNull(color) ? String.valueOf(4010) : color,
                    R.drawable.duoqu_bottom_rectangle, ThemeUtil.SET_NULL, ThemeUtil.getColorId(4010));
        }
            
        String textColor = (String) message.getValue("v_bt_text_1");
        ThemeUtil.setTextColor(mContext, mTextView1, textColor, ThemeUtil.getColorId(3011));
        ThemeUtil.setTextColor(mContext, mTextView2, textColor, ThemeUtil.getColorId(3011));
        
        setBottom(message, isRebind);
    }
    
    private void setButtonViewVisibility(int button1Visible, int button2Visible, int splitLineVisible) {
        ChannelContentUtil.setViewVisibility(mBtn1, button1Visible);
        ChannelContentUtil.setViewVisibility(mBtn2, button2Visible);
        ChannelContentUtil.setViewVisibility(mTextView1, button1Visible);
        ChannelContentUtil.setViewVisibility(mTextView2, button2Visible);
        ChannelContentUtil.setViewVisibility(mDuoqu_btn_split_line, splitLineVisible);
    }
    
    
    @Override
    public void destroy() {
        ViewUtil.recycleViewBg(mDuoqu_bottom_split_line);
        ViewUtil.recycleViewBg(mView);
        super.destroy();
    }
    
    private void setBottom(BusinessSmsMessage message, boolean isRebind) {
        String groupValue = (String) mMessage.getValue("GROUP_KEY");
        if(StringUtils.isNull(groupValue)){
            groupValue = "";
        }

        try {
            /* UIX标准方案UIX-167/2016.05.31/ kedeyuan starts */
            HashMap<String, Object> formatBubbleParamMap = message.extendParamMap;
            // Log.v("kedeyuan", message.bubbleJsonObj.toString());
            formatBubbleParamMap.put("isUseNewAction", "true");
            
            /* UIX-216 zhaoxiachao 20160927 start */
            JSONArray actionArr = BottomButtonUtil.getActionArrayData(mContext, mMessage, groupValue, 2, mMessage.extendParamMap);
            if (StringUtils.isNull(groupValue)) {
                groupValue = BottomButtonUtil.getFirstGroupValue(actionArr) ;
                actionArr = BottomButtonUtil.getActionArrayData(mContext, mMessage, groupValue, 2, mMessage.extendParamMap);
            }
            /* UIX-216 zhaoxiachao 20160927 end */
            
            /* UIX标准方案UIX-167/2016.05.31/ kedeyuan starts */
            if (actionArr == null) {
                mSize = 0;
            } else {
                mSize = actionArr.length();
            }
            if (mSize==0&&!StringUtils.isNull((String) message.getValue("v_by_bg_1"))&&!StringUtils.isNull((String) message.getValue("v_hd_bg_1"))) {
                ThemeUtil.setViewBg(mContext, mView, (String) message.getValue("v_by_bg_1"), R.color.duoqu_theme_color_1090);
            }else {
                ThemeUtil.setViewBg(mContext, mView, (String) message.getValue("v_bt_bg_1"), R.color.duoqu_theme_color_4010);
            }
            switch (mSize) {
                case 0:
                    mDuoqu_bottom_split_line.setVisibility(View.GONE);
                    setButtonViewVisibility(View.GONE, View.GONE, View.GONE);
                    break;
                case 1:
                    mDuoqu_bottom_split_line.setVisibility(View.VISIBLE);
                    setButtonViewVisibility(View.VISIBLE, View.GONE, View.GONE);
                    setBotton(mBtn1, mTextView1, actionArr.getJSONObject(0), mDisLogo);
                    break;
                default:
                    mDuoqu_bottom_split_line.setVisibility(View.VISIBLE);
                    setButtonViewVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE);
                    setBotton(mBtn1, mTextView1, actionArr.getJSONObject(1), mDisLogo);
                    setBotton(mBtn2, mTextView2, actionArr.getJSONObject(0), mDisLogo);
                    break;
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("setBottom exception: " + e.getMessage(), e);
        }
    }
    
    public void changeData(Map<String, Object> param) {
        try {
            String groupValue = "";
            if (param != null) {
                groupValue = (String) param.get("groupValue");
            }
            if(!StringUtils.isNull(groupValue)){
                setBottom(this.mMessage, true);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleBottomTwo changeData error:", e);
        }
        
    }
}
