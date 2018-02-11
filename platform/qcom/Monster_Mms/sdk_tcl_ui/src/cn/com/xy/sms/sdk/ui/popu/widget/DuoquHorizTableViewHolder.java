package cn.com.xy.sms.sdk.ui.popu.widget;

import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;

public class DuoquHorizTableViewHolder extends DuoquBaseHolder {
    public RelativeLayout mUsedLayout;

    public DuoquHorizTableViewHolder() {
    }

    @Override
    public void setContent(int pos, BusinessSmsMessage message, String dataKey, boolean isReBind) {
        try {
            super.setContent(pos, message, dataKey, isReBind);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquHorizTableViewHolder setContent error:", e);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        try {
            if (mUsedLayout != null) {
                mUsedLayout.setVisibility(visibility);
            }
            mTitleView.setVisibility(visibility);
            mContentView.setVisibility(visibility);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquHorizTableViewHolder setVisibility error:", e);
        }
    }
}
