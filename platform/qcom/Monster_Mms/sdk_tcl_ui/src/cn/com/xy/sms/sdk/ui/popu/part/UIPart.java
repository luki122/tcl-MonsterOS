package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;

public abstract class UIPart {

    public BasePopupView mBasePopupView;
    public View mView;
    public Activity mContext;
    public BusinessSmsMessage mMessage;
    public XyCallBack mCallback;
    public int mPartId = 0;
    public boolean mNeedFirstToPadding = true;// body first child padding
    public int mAddRootView = 0;// add RootView 0:not need 1: need only
                                // bodyViewGroup one child and set content bg
                                // and padtop
    public HashMap<String, Object> mExtendParam = null;// PART of extension
                                                       // parameters
    // private ImageView mTelCard;
    // private TextView mTime;
    // private TextView mTelName;

    public UIPart(Activity context, BusinessSmsMessage message,
            XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        this.mPartId = partId;
        init(context, message, callback, layoutId, root);

    }

    // public void reSetActivity(Activity context) {
    // this.mContext = context;
    // }

    void init(Activity context, BusinessSmsMessage message,
            XyCallBack callback, int layoutId, ViewGroup root) {
        this.mContext = context;
        this.mMessage = message;
        this.mCallback = callback;
        mView = ViewManger.createContextByLayoutId(mContext, layoutId, null);
    }

    public void build() throws Exception {
        initUi();
        initListener();
        setContent(mMessage, false);

    }

    public void executePopuCmd(byte popu_cmd) {
        if (mCallback != null) {

            mCallback.execute(popu_cmd);
        }
    }

    public String getTitleNo() {
        return (String) mMessage.getValue("title_num");
    }

    public void destroy() {
        mView = null;
        mContext = null;
        mMessage = null;
        mCallback = null;

    }

    /**
     * put param vlaue
     * 
     * @param key
     * @param val
     */
    public void putParam(String key, Object val) {
        if (key != null && val != null) {
            if (mExtendParam == null) {
                mExtendParam = new HashMap<String, Object>();
            }
            mExtendParam.put(key, val);
        }
    }

    /**
     * get param value
     * 
     * @param key
     * @return
     */
    public Object getParam(String key) {
        if (mExtendParam != null) {
            return mExtendParam.get(key);
        }
        return null;
    }

    public void initUi() throws Exception {
    }

    public void initListener() throws Exception {
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind)
            throws Exception {
        this.mMessage = message ;
    }

    //
    // public void setBottomInfo(View view) {// Show time/ Operator/ SIM Card
    // if (view != null)
    // try {
    // mTelCard = (ImageView) view.findViewById(R.id.tel_card);
    // mTime = (TextView) view.findViewById(R.id.time);
    // mTelName = (TextView) view.findViewById(R.id.tel_name);
    //
    // String simName = mMessage.simName;
    // LogManager.i("simName", simName + "");
    // int simIndex = mMessage.simIndex;
    // switch (simIndex) {
    // case 0:
    // // tel_card.setImageResource(R.drawable.duoqu_black_sim1);
    // break;
    // case 1:
    // // tel_card.setImageResource(R.drawable.duoqu_black_sim2);
    // break;
    // default:
    // mTelCard.setVisibility(View.GONE);
    // break;
    // }
    //
    // mTime.setTextColor(view.getResources().getColor(
    // R.color.duoqu_mark_text_color_blcak));
    // mTelName.setTextColor(view.getResources().getColor(
    // R.color.duoqu_mark_text_color_blcak));
    // String timeText = DateUtils.getTimeString("HH:mm",
    // mMessage.msgTime);
    // if (!StringUtils.isNull(timeText)) {
    // mTime.setText(timeText);
    // }
    // if (!StringUtils.isNull(simName)) {
    // mTelName.setText(simName);
    // }
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // }

    // get sms count
    public int getMsgCount() {
        try {

        } catch (Throwable e) {
        }
        return 0;
    }

    // get the current index is showing
    public int getCurrentIndex() {
        try {

        } catch (Throwable e) {
        }

        return 0;
    }

    /**
     * Change data
     * 
     * @param param
     */
    public void changeData(Map<String, Object> param) {
    }
}
