package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public abstract class DuoquBaseTable extends RelativeLayout {
    protected int                   mChildId             = 0;
    protected List<DuoquHorizTableViewHolder> mDuoquBaseHolderList = null;
    // private static String mTitleViewColor = "";
    // private static String mContentViewColor = "";
    
    public DuoquBaseTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }
    
    public void setContentList(BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        if (dataSize == 0) {
            this.setVisibility(View.GONE);
            return;
        }
        if (dataSize > 5) {
            dataSize = 5;
        }
        this.setVisibility(View.VISIBLE);
        final List<DuoquHorizTableViewHolder> holderList = mDuoquBaseHolderList;
        if (isRebind && holderList != null) {
            int holderSize = holderList.size();
            int diffSize = holderSize - dataSize;
            DuoquBaseHolder tempHolder = null;
            if (diffSize > 0) {
                for (int i = 0; i < holderSize; i++) {
                    tempHolder = holderList.get(i);
                    if (i < dataSize) {
                        tempHolder.setVisibility(View.VISIBLE);
                        tempHolder.setContent(i, message, dataKey, isRebind);
                    } else {
                        tempHolder.setVisibility(View.GONE);
                    }
                }
            } else {
                for (int i = 0; i < dataSize; i++) {
                    if (i < holderSize) {
                        tempHolder = holderList.get(i);
                        tempHolder.setVisibility(View.VISIBLE);
                        tempHolder.setContent(i, message, dataKey, isRebind);
                    } else {
                        getHolder(i, message, dataSize, dataKey, false);
                    }
                }
            }
            return;
        }
        mDuoquBaseHolderList = new ArrayList<DuoquHorizTableViewHolder>();
        for (int i = 0; i < dataSize; i++) {
            getHolder(i, message, dataSize, dataKey, false);
        }
    }
    
    /**
     * Set the location of view
     */
    protected abstract RelativeLayout.LayoutParams getLayoutParams(int childId);
    
    /**
     * get custom params
     */
    protected abstract void initParams(Context context, AttributeSet attrs);
    
    /**
     * create and set TextView
     * 
     * @param pos
     * @param message
     * @param dataSize
     * @param dataKey
     */
    protected abstract void getHolder(int pos, BusinessSmsMessage message, int dataSize, String dataKey,
            boolean isRebind);
}
