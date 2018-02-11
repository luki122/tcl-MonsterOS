package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleCodeHead extends UIPart {
    private ImageView mLeftlogo;
    private TextView mTitleText;

    public BubbleCodeHead(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initUi() throws Exception {
        // TODO Auto-generated method stub
        super.initUi();
        mLeftlogo = (ImageView) mView.findViewById(R.id.duoqu_left_logo);
        mTitleText = (TextView) mView.findViewById(R.id.duoqu_title_text);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        // TODO Auto-generated method stub
        super.setContent(message, isRebind);

        this.mMessage = message;
        mView.setBackgroundResource(R.drawable.duoqu_top_rectangle);
        ThemeUtil.setTextColor(mContext, mTitleText, (String) message.getValue("v_hd_text_1"),
                R.color.duoqu_theme_color_4010);
        
        String headBackgroundColor = (String)message.getValue("v_hd_bg_1") ;
        String bodyBackGroundColor = (String) message.getValue("v_by_bg_1") ;
        if (StringUtils.isNull(headBackgroundColor)|| StringUtils.isNull(bodyBackGroundColor)) {
            ThemeUtil.setViewBg(mContext, mView, "", R.color.duoqu_theme_color_1090);
        }else {
            ThemeUtil.setViewBg(mContext, mView, headBackgroundColor , R.color.duoqu_theme_color_1090);
        }
        ThemeUtil.setViewBg(mContext, mLeftlogo, (String) message.getValue("v_hd_logo"),
                R.drawable.duoqu_default_head_logo);
        String phoneNumber = (String) message.getValue("phoneNum");
        ChannelContentUtil.bindTextImageView(phoneNumber, mLeftlogo, ChannelContentUtil.TYPE_LOGOC_ROUND);

        ChannelContentUtil.setText(mTitleText, (String) message.getValue("view_title_name"), "");


    }

}
