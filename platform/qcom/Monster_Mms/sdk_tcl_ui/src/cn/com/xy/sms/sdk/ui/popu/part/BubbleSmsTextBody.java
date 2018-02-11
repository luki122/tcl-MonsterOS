package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleSmsTextBody extends UIPart {
    private TextView mSmsTextView ;
    private TextView mHintTextView ;
    
    public BubbleSmsTextBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void initUi() throws Exception {
        // TODO Auto-generated method stub
        super.initUi();
        mSmsTextView = (TextView) mView.findViewById(R.id.duoqu_sms_origin_text);
        mHintTextView = (TextView) mView.findViewById(R.id.duoqu_sms_origin_text_hint);
    }
    
    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        // TODO Auto-generated method stub
        super.setContent(message, isRebind);
        if (message==null) {
            return ;
        }
        this.mMessage = message;
        ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
        ThemeUtil.setTextColor(mContext , mSmsTextView, (String)message.getValue("v_by_original"), R.color.duoqu_theme_color_4010);
        ThemeUtil.setTextColor(mContext , mHintTextView, (String)message.getValue("v_by_text_9"), R.color.duoqu_theme_color_4010);
        ChannelContentUtil.setText(mSmsTextView, (String)message.getValue("m_by_original"), message.getMessageBody());
        String hintText = (String)message.getValue("m_by_text_9");
        if(StringUtils.isNull(hintText)){
            mHintTextView.setVisibility(View.GONE);
        }else{
            mHintTextView.setVisibility(View.VISIBLE);
            ChannelContentUtil.setText(mHintTextView, hintText, "");
        }
        
    }
    
}
