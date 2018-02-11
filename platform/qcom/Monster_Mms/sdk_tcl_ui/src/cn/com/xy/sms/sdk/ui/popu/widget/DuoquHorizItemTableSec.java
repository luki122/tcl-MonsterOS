package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;

public class DuoquHorizItemTableSec extends DuoquBaseTable {
    public DuoquTableViewShowMoreInfoSec mDuoquTableViewShowMoreInfo;
    private boolean mExpanded = false;
    private boolean mUseFirstTitle = true;

    public boolean ismUseFirstTitle() {
        return mUseFirstTitle;
    }

    
    public void setmUseFirstTitle(boolean mUseFirstTitle) {
        this.mUseFirstTitle = mUseFirstTitle;
    }

    public DuoquHorizItemTableSec(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
        mDuoquTableViewShowMoreInfo = new DuoquTableViewShowMoreInfoSec(this, null);
        
    }

    @Override
    public void setContentList(BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        try {
            if (dataSize == 0) {
                this.setVisibility(View.GONE);
                return;
            }
            mExpanded = mDuoquTableViewShowMoreInfo.getExpanded(message);
            mDuoquTableViewShowMoreInfo.clearMoreInfoViewHolderList();
            this.setVisibility(View.VISIBLE);
            final List<DuoquHorizTableViewHolder> holderList = mDuoquBaseHolderList;
            if (isRebind && holderList != null) {
                int holderSize = holderList.size();
                DuoquHorizTableViewHolder tempHolder = null;

                if (holderSize > dataSize) {
                    for (int i = 0; i < holderSize; i++) {
                        tempHolder = (DuoquHorizTableViewHolder) holderList.get(i);
                        if (i < dataSize) {
                            tempHolder.setVisibility(View.VISIBLE);
                            tempHolder.setContent(i, message, dataKey, isRebind);
                            showOrHiddenMoreInfo(i);
                        } else {
                            tempHolder.setVisibility(View.GONE);
                        }
                    }
                } else {
                    for (int i = 0; i < dataSize; i++) {
                        if (i < holderSize) {
                            tempHolder = (DuoquHorizTableViewHolder) holderList.get(i);
                            tempHolder.setVisibility(View.VISIBLE);
                            tempHolder.setContent(i, message, dataKey, isRebind);
                        } else {
                            getHolder(i, message, dataSize, dataKey, false);
                        }
                        showOrHiddenMoreInfo(i);
                    }
                }
                setShowHiddenMoreInfoButtonVisibility(message, dataSize);
                return;
            }
            mDuoquBaseHolderList = new ArrayList<DuoquHorizTableViewHolder>();
            mChildId = 0;
            this.removeAllViews();
            for (int i = 0; i < dataSize; i++) {
                getHolder(i, message, dataSize, dataKey, false);
                showOrHiddenMoreInfo(i);
            }
            setShowHiddenMoreInfoButtonVisibility(message, dataSize);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquHorizItemTable setContentList error:", e);
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void getHolder(int pos, BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        DuoquHorizTableViewHolder holder = new DuoquHorizTableViewHolder();
        holder.mUsedLayout = (RelativeLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.duoqu_horiz_table_body_sec, null);
        holder.mTitleView=(TextView) holder.mUsedLayout.findViewById(R.id.duoqu_horiz_table_title_sec);
        holder.mContentView=(TextView) holder.mUsedLayout.findViewById(R.id.duoqu_horiz_table_content_sec);
        holder.mUsedLayout.setId(++mChildId);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        if (mChildId == 1) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        if (mChildId > 1) {
            params.addRule(RelativeLayout.BELOW, mChildId - 1);
            params.setMargins(0, ViewUtil.dp2px(getContext(), -20), 0, 0);
            
        }
        if (message.getValue("v_by_text_l_2")!= null) {
            ThemeUtil.setTextColor(Constant.getContext(), holder.mTitleView,(String)message.getValue("v_by_text_l_2"), R.color.duoqu_hori_title_color);
        }
        if (message.getValue("v_by_text_r_2")!=null) {
            ThemeUtil.setTextColor(Constant.getContext(), holder.mContentView, (String)message.getValue("v_by_text_r_2"), R.color.duoqu_hori_content_color);
        }
        
        this.addView(holder.mUsedLayout, params);
        
        holder.setContent(pos, message, dataKey, isRebind);
        mDuoquBaseHolderList.add(holder);
    }

    private void showOrHiddenMoreInfo(int pos) {
        if (pos < mDuoquTableViewShowMoreInfo.getDefaultShowRow()|| mDuoquBaseHolderList == null
                || mDuoquBaseHolderList.size() < pos) {
            return;
        }

        DuoquHorizTableViewHolder viewHolder = (DuoquHorizTableViewHolder) mDuoquBaseHolderList.get(pos);
        if (pos < mDuoquTableViewShowMoreInfo.getmDefaultLimitDataSize()) {
            viewHolder.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
            mDuoquTableViewShowMoreInfo.addMoreInfoViewHolder(viewHolder);
        } else {
            viewHolder.setVisibility(View.GONE);
        }
    }

    private void setShowHiddenMoreInfoButtonVisibility(final BusinessSmsMessage message, int dataSize) {
        if (dataSize <= mDuoquTableViewShowMoreInfo.getDefaultShowRow()) {
            mDuoquTableViewShowMoreInfo.hiddenExpandCollapseMoreInfoView();
        } else {
            int lastItemId = mDuoquBaseHolderList.get(dataSize - 1).mUsedLayout.getId();
            if (specialUiPartFlag) {
                mDuoquTableViewShowMoreInfo.hiddenExpandCollapseMoreInfoView();
            }else {
                mDuoquTableViewShowMoreInfo.showExpandCollapseMoreInfoView(message, mExpanded, lastItemId, dataSize);
            }
        }
    }

    public void setDefaultShowRow(int defaultShowRow) {
        mDuoquTableViewShowMoreInfo.setDefaultShowRow(defaultShowRow);
    }

    public void setmDefaultLimitDataSize(int defaultLimitRow) {
        mDuoquTableViewShowMoreInfo.setmDefaultLimitDataSize(defaultLimitRow);
    }

    @Override
    protected LayoutParams getLayoutParams(int childId) {
        return null;
    }

    @Override
    protected void initParams(Context context, AttributeSet attrs) {
        
    }

    private boolean specialUiPartFlag = false ;
    public void setSpecialUiPartFlag(boolean flag ) {
        this.specialUiPartFlag = flag ;
    }
    
   
}
