package cn.com.xy.sms.sdk.ui.popu.popupview;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.util.UiPartInterface;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BasePopupView extends RelativeLayout {

    public static final byte POPU_CMD_DEL = 0;

    public static final byte POPU_CMD_READ = 1;

    public static final byte POPU_CMD_CALL = 2;

    public static final byte POPU_CMD_OPEN = 3;

    public static final byte POPU_CMD_SEND = 4;

    public static final byte POPU_CMD_OPEN_EDIT = 5;

    public static final byte POPU_CMD_DOACTION = 6;
    
    public static final int CHANGE_DATA_TYPE_ALL = 1;
    public static final int CHANGE_DATA_TYPE_BODY = 0;
    public static final int CHANGE_DATA_TYPE_FOOT = 2;
    public static final int CHANGE_DATA_TYPE_HEAD = 3;
    
    public BusinessSmsMessage mBusinessSmsMessage;
    public ViewGroup mView = null;
    private BaseCompriseBubbleView mBaseCompriseView = null;
    public String groupValue = "";

    public BasePopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePopupView(Context context) {
        super(context);
    }

    public void init(Activity context, BusinessSmsMessage message,
            XyCallBack callback) throws Exception {
        this.mBusinessSmsMessage = message;
        initUIPartBefore(context, message);
        mBaseCompriseView = new BaseCompriseBubbleView(context, callback,
                message, mView);
        initUI();
        initData(message);
    }

    public void initData(BusinessSmsMessage message) {
        this.mBusinessSmsMessage = message;
        initUIAfter();
        UiPartInterface part = ViewManger.getUiPartInterface();
        if(part != null){
            part.doUiAction(ViewManger.UIPART_ACTION_SET_BG, this);
        }
    }

    public void initUIAfter() {

    }

    // public void setBackground(BusinessSmsMessage message) {
    // String popupColor = (String) message.getValue("popup_color");
    // String channelColor = (String) message.getValue("v_sms_form_bg");
    // String color = null;
    // if ("1".equals((String) message.getValue("custom_colors"))) {
    // color = popupColor;
    // } else {
    // color = channelColor;
    // }
    //
    // if (BasePopupView.this.getTag(R.id.tag_bg_resource) != null
    // && ((String) this.getTag(R.id.tag_bg_resource)).equals(color)) {
    // return;
    // }
    //
    // if (!StringUtils.isNull(color)) {
    // setBackground(color);
    // } else {
    // setBackground(popupColor);
    // }
    // }

    // public void setBackground(String popupColor) {
    // if (popupColor == null) {
    // this.setTag(R.id.tag_bg_resource, "blue");
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_blue);
    // return;
    // }
    //
    // // BasePopupView.this.setTag(R.id.tag_bg_resource, popupColor);
    //
    // /* if (popupColor.equalsIgnoreCase("blue")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_blue);
    // } else if (popupColor.equalsIgnoreCase("green")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_green);
    // } else if (popupColor.equalsIgnoreCase("olive")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_olive);
    // } else if (popupColor.equalsIgnoreCase("pink")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_pink);
    // } else if (popupColor.equalsIgnoreCase("purple")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_purple);
    // } else if (popupColor.equalsIgnoreCase("red")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_red);
    // } else if (popupColor.equalsIgnoreCase("sky")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_sky);
    // } else if (popupColor.equalsIgnoreCase("yellow")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_yellow);
    // } else if (popupColor.equalsIgnoreCase("rose")) {
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_rose);
    // } else {
    // this.setTag(R.id.tag_bg_resource, "blue");
    // this.setBackgroundResource(R.drawable.duoqu_pop_bg_blue);
    // }*/
    //
    // this.setBackgroundResource(R.drawable.duoqu_bottom_rectangle_op);
    // // LayoutParams params=(LayoutParams) this.getLayoutParams();
    // // params = new LayoutParams(LayoutParams.WRAP_CONTENT,
    // LayoutParams.WRAP_CONTENT);
    // // this.setLayoutParams(params);
    // }

    public void initUI() throws Exception {
        mBaseCompriseView.addViews(mView, this);
    }



    public void initUIPartBefore(Activity mContext, BusinessSmsMessage businessSmsMessage) {
        int bgType = SysParamEntityManager.getIntParam(mContext, Constant.POPUP_BG_TYPE);

        mView = this;
        this.setBackgroundColor(Color.BLUE);
        if (bgType == 0) {
            this.setBackgroundResource(R.color.duoqu_all_transparent);
        } else if (bgType == 2) {

            try {
                String popubgDrawableName = businessSmsMessage.getImgNameByKey("popubgDrawableName");
                if (!StringUtils.isNull(popubgDrawableName)) {
                    mView = (ViewGroup) ViewManger.createContextByLayoutId(mContext, R.layout.duoqu_popup_oneside, null);
                    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                    mView.setLayoutParams(params);
                    this.addView(mView, params);
                }
                ViewManger.setViewBg(mContext, mView, popubgDrawableName, R.color.dark_transparent, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            this.setBackgroundResource(R.color.dark_transparent);
        }
        if (ViewUtil.getChannelType() == 1 || ViewUtil.getChannelType() == 2 || ViewUtil.getChannelType() == 8 || ViewUtil.getChannelType() == 25) {// 酷派或中兴渠道类型
            this.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else {
            mView.setPadding(mView.getPaddingLeft(), ViewManger.getIntDimen(mContext, R.dimen.base_margin_top),
                    mView.getPaddingRight(), mView.getPaddingBottom());
        }
    }

    public void destroy() {
        ViewUtil.recycleViewBg(mView);
        mView = null;
        if (mBaseCompriseView != null)
            mBaseCompriseView.destory();
        mBaseCompriseView = null;
    }

    /**
     * Rebind the data
     * 
     * @param businessSmsMessage
     */
    public void reBindData(Activity context,
            BusinessSmsMessage businessSmsMessage) throws Exception {
        this.mBusinessSmsMessage = businessSmsMessage;
        bindData(context, true);
    }

    public void bindData(Activity context, boolean isRebind) throws Exception {
        mBaseCompriseView.reBindData(context, mBusinessSmsMessage, isRebind);
    }

    // public void reSetActivity(Activity context){
    // mBaseCompriseView.reSetActivity(context);
    // }

    /**
     * Change data
     * 
     * @param param
     *            key type: 0: Just need to change the body parts 1:All parts
     *            change other key custom
     */
    public void changeData(Map<String, Object> param) {
        if (param == null) {
            return;
        }

        Integer type = (Integer) param.get("type");
        if (type == null) {
            return;
        }

        switch (type) {
        case CHANGE_DATA_TYPE_BODY:
            if (mBaseCompriseView.mBodyUIPartList != null) {
                for (UIPart part : mBaseCompriseView.mBodyUIPartList) {
                    part.changeData(param);
                }
            }
            break;
        case CHANGE_DATA_TYPE_ALL:
            if (mBaseCompriseView.mHeadUIPartList != null) {// change head
                for (UIPart part : mBaseCompriseView.mHeadUIPartList) {
                    part.changeData(param);
                }
            }
            if (mBaseCompriseView.mBodyUIPartList != null) {// change body
                for (UIPart part : mBaseCompriseView.mBodyUIPartList) {
                    part.changeData(param);
                }
            }
            if (mBaseCompriseView.mFootUIPartList != null) {// change bottom
                for (UIPart part : mBaseCompriseView.mFootUIPartList) {
                    part.changeData(param);
                }
            }
            break;
        case CHANGE_DATA_TYPE_FOOT:
            if (mBaseCompriseView.mFootUIPartList != null) {// change bottom
                for (UIPart part : mBaseCompriseView.mFootUIPartList) {
                    part.changeData(param);
                }
            }
            break;
        case CHANGE_DATA_TYPE_HEAD:
            if (mBaseCompriseView.mHeadUIPartList != null) {// change head
                for (UIPart part : mBaseCompriseView.mHeadUIPartList) {
                    part.changeData(param);
                }
            }
            break;
        default:
            break;
        }
    }

}
