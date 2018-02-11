package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;

public class BubbleBodyPostMessage extends UIPart {
    private TextView mPostTitleView;
    private TextView mPostState;
    private static final String  ACTION_EXPRESS_STATUS_CHANGE  = "cn.com.xy.sms.ExpressStatusReceiver";
    public ExpressStatusReceiver mReceiver;

    public BubbleBodyPostMessage(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);

    }

    @Override
    public void initUi() throws Exception {
        mPostTitleView = (TextView) mView.findViewById(R.id.duoqu_bubble_post_title);
        mPostState = (TextView) mView.findViewById(R.id.duoqu_bubble_post_state);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(final BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message==null) {
            return ;
        }
        ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
        ThemeUtil.setTextColor(mContext, mPostTitleView, (String) message.getValue("v_by_text_u_3"), R.color.duoqu_theme_color_4011);
        ThemeUtil.setTextColor(mContext, mPostState, (String) message.getValue("v_by_text_d_3"), R.color.duoqu_theme_color_4010);
        
        ChannelContentUtil.setText(mPostTitleView, (String) message.getValue("m_by_text_u_3"), ChannelContentUtil.POST_TITLE);
        ChannelContentUtil.setText(mPostState, (String) message.getValue("m_by_text_d_3"), ChannelContentUtil.POST_SENDING);
        
        }
    
    @Override
    public void changeData(Map<String, Object> param) {
        super.changeData(param);
        if (param.containsKey("catagory") && ((String) param.get("catagory")).equals("express")) {
            registerReceiver();
        }
    }

    private void registerReceiver() {
        try {
            if (mReceiver == null) {
                mReceiver = new ExpressStatusReceiver(this);
            } else {
                mReceiver.setItem(this);
            }
            mContext.registerReceiver(mReceiver, new IntentFilter(ACTION_EXPRESS_STATUS_CHANGE));
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    public void onReciver(Intent intent) {
        try {
            mContext.unregisterReceiver(mReceiver);
            if (mMessage == null || mMessage.bubbleJsonObj == null)
                return;
            String action = intent.getAction();
            if (!ACTION_EXPRESS_STATUS_CHANGE.equals(action))
                return;
            String key = intent.getStringExtra("JSONDATA");
            if (StringUtils.isNull(key))
                return;
            // check
            JSONObject jsonObject = new JSONObject(key);
            String statusString = jsonObject.optString("view_express_latest_status");
            if(!StringUtils.isNull(statusString)){
                ContentUtil.setText(mPostState, statusString,ChannelContentUtil.POST_SENDING);
                // save status data into message
                mMessage.bubbleJsonObj.put("view_express_latest_status", statusString);
                ParseManager.updateMatchCacheManager(mMessage);
            }
            
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    static class ExpressStatusReceiver extends BroadcastReceiver {
        private BubbleBodyPostMessage item;

        public void setItem(BubbleBodyPostMessage item) {
            this.item = item;
        }

        public ExpressStatusReceiver(BubbleBodyPostMessage item) {
            this.item = item;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (item == null)
                return;
            item.onReciver(intent);
            item = null;
        }
    }
    @Override
    public void destroy() {
       super.destroy();
       try{
           mContext.unregisterReceiver(mReceiver);
       }
       catch(Throwable e) {
           SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
       }
       
    }
}
