package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

@SuppressLint("ResourceAsColor")
public class BubbleGeneralOneBody extends UIPart {
    private TextView mTitleTextView = null;
    private TextView mContentTextView = null;
    private RelativeLayout mRelativeLayout = null ;

    public BubbleGeneralOneBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    @Override
    public void initUi() {
        mTitleTextView = (TextView) mView.findViewById(R.id.duoqu_bubble_generalone_title);
        mContentTextView = (TextView) mView.findViewById(R.id.duoqu_bubble_generalone_content);
        mRelativeLayout = (RelativeLayout) mView.findViewById(R.id.duoqu_bubble_generalone_rl);
    }

    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message == null) {
            return;
        }
        ChannelContentUtil.setText(mTitleTextView, (String) mMessage.getValue("m_by_text_u_1"), "");
        ChannelContentUtil.setText(mContentTextView, (String) mMessage.getValue("m_by_text_d_1"), "");
        if (StringUtils.isNull((String) mMessage.getValue("m_by_text_u_1"))) {
            mTitleTextView.setVisibility(View.GONE);
        } else {
            mTitleTextView.setVisibility(View.VISIBLE);
        }
        if (StringUtils.isNull((String) mMessage.getValue("m_by_text_d_1"))) {
            mContentTextView.setVisibility(View.GONE);
        } else {
            mContentTextView.setVisibility(View.VISIBLE);
        }
        if (StringUtils.isNull((String) mMessage.getValue("m_by_text_d_1"))&&StringUtils.isNull((String) mMessage.getValue("m_by_text_u_1"))) {
            mRelativeLayout.setVisibility(View.GONE);
        }else {
            mRelativeLayout.setVisibility(View.VISIBLE);
        }
        ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
        ThemeUtil.setTextColor(mContext, mTitleTextView, (String) mMessage.getValue("v_by_text_u_1"),
                R.color.duoqu_theme_color_4011);
        ThemeUtil.setTextColor(mContext, mContentTextView, (String) mMessage.getValue("v_by_text_d_1"),
                R.color.duoqu_theme_color_4010);

    }

    public void setVisibility(int visibility) {
        mTitleTextView.setVisibility(visibility);
        mContentTextView.setVisibility(visibility);
    }

}
