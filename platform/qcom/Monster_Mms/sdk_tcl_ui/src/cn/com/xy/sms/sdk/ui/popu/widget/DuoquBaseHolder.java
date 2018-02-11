package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

@SuppressLint("ResourceAsColor")
public class DuoquBaseHolder {
    public TextView mTitleView;
    public TextView mContentView;
    
    public DuoquBaseHolder() {
    
    }
    
    public void setContent(int pos, BusinessSmsMessage message, String dataKey, boolean isReBind) {
        JSONObject jsobj = (JSONObject) message.getTableData(pos, dataKey);
        String titleText = (String) JsonUtil.getValFromJsonObject(jsobj, "t1");
        String contentText = (String) JsonUtil.getValFromJsonObject(jsobj, "t2");
        mTitleView.setText(titleText);
        mContentView.setText(contentText);
        
        if (isReBind == false || pos == 0) {
            setViewStyle(jsobj, message);
        }
    }
    
    @SuppressLint("ResourceAsColor")
    private void setViewStyle(JSONObject jsobj, BusinessSmsMessage message) {
        // set text color from parseUtilBubble
        
        String titleColor = (String) JsonUtil.getValFromJsonObject(jsobj, "c1");
        String contentColor = (String) JsonUtil.getValFromJsonObject(jsobj, "c2");
        if (!StringUtils.isNull(titleColor)) {
            ThemeUtil.setTextColor(mTitleView.getContext(), mTitleView, titleColor, R.color.duoqu_theme_color_4011);
        }
        if (!StringUtils.isNull(contentColor)) {
            ThemeUtil.setTextColor(mContentView.getContext(), mContentView, contentColor, R.color.duoqu_theme_color_4010);
        }
        
        // set text size from parseUtilBubble
        String titleSize = (String) JsonUtil.getValFromJsonObject(jsobj, "s1");
        String contentSize = (String) JsonUtil.getValFromJsonObject(jsobj, "s2");
        if (!StringUtils.isNull(titleSize)) {
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(titleSize));
        }
        if (!StringUtils.isNull(contentSize)) {
            mContentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(contentSize));
        } else {
            Object defaultTextSizeObj = mContentView.getTag(R.id.tag_default_content_text_size);
            if (defaultTextSizeObj != null) {
                mContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Float.parseFloat(defaultTextSizeObj.toString()));
            }
        }
    }
    
    public void setVisibility(int visibility) {
        mTitleView.setVisibility(visibility);
        mContentView.setVisibility(visibility);
        if (mContentView.getTag(R.id.tag_parent_layout) != null) {
            ((RelativeLayout) mContentView.getTag(R.id.tag_parent_layout)).setVisibility(visibility);
        }
    }
}