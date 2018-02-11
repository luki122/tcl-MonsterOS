package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

/* MEIZU-568 huangzhiqiang 20160427 start */
public class DuoquTableViewShowMoreInfoSec {

    public static final String KEY_EXPANDED = "expanded";
    private static final int TYPE_EXPAND = 0;
    private ViewGroup mContainer = null;
    private List<DuoquBaseHolder> mMoreInfoViewHolder = new LinkedList<DuoquBaseHolder>();
    private View mExpandCollapseMoreInfoView = null;
    private View mSplitView = null;
    private int mDefaultShowRow = 5;
    private int mDefaultLimitDataSize = 8;
    private int mDataSize = 0;
    public OnOpenSmsDetailListener onOpenSmsDetailListener = null;
    private TextView mCallName;
    private ImageView mCallImg;
    

    public interface OnOpenSmsDetailListener {
        public void onClick(View v);
    }

    public int getmDefaultLimitDataSize() {
        return mDefaultLimitDataSize;
    }

    public void setmDefaultLimitDataSize(int mDefaultLimitDataSize) {
        this.mDefaultLimitDataSize = mDefaultLimitDataSize;
    }

    public DuoquTableViewShowMoreInfoSec(ViewGroup container) {
        mContainer = container;
    }

    public DuoquTableViewShowMoreInfoSec(ViewGroup container, View splitView) {
        mContainer = container;
        mSplitView = splitView;
    }

    /**
     * 添加需要隐藏/显示的更多信息ViewHolder
     * 
     * @param viewHolder
     */
    public void addMoreInfoViewHolder(DuoquBaseHolder viewHolder) {
        mMoreInfoViewHolder.add(viewHolder);
    }

    /**
     * 清空需要隐藏/显示的更多信息ViewHolder
     */
    public void clearMoreInfoViewHolderList() {
        mMoreInfoViewHolder.clear();
    }

    /**
     * 隐藏展开/收起控件
     * 
     * @param message
     * @param expanded
     * @param lastItemId
     */
    public void hiddenExpandCollapseMoreInfoView() {
        ChannelContentUtil.setViewVisibility(mExpandCollapseMoreInfoView, View.GONE);
        ChannelContentUtil.setViewVisibility(mSplitView, View.GONE);
    }

    /**
     * 用于显示是否查看原文的
     * 
     * @param dataSize
     */
    private void showChangeToOriginSmsText(int dataSize) {
        if (mExpandCollapseMoreInfoView != null) {
            TextView showSms = (TextView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_show_more_info);
            TextView Button = (TextView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_expand_button);
            BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) Button.getTag(R.id.tag_business_sms_message);
            if (businessSmsMessage == null) {
                return;
            }
            if (getExpanded(businessSmsMessage)) {
                if (dataSize > mDefaultLimitDataSize) {
                    showSms.setVisibility(View.VISIBLE);
                    showSms.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (onOpenSmsDetailListener != null) {
                                try {
                                    onOpenSmsDetailListener.onClick(v);
                                } catch (Throwable e) {
                                    cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil.smartSdkExceptionLog("DuoquTableViewShowMoreInfo showChangeToOriginSmsText error:", e);
                                }
                            }
                        }
                    });
                } else {
                    showSms.setVisibility(View.GONE);
                }
            } else {
                showSms.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 显示展开/收起控件
     * 
     * @param message
     * @param expanded
     * @param lastItemId
     */
    @SuppressLint("ResourceAsColor")
    public void showExpandCollapseMoreInfoView(final BusinessSmsMessage message, boolean expanded, int lastItemId,
            int dataSize) {
        this.mDataSize = dataSize;
        if (mExpandCollapseMoreInfoView == null) {
            mExpandCollapseMoreInfoView = newExpandCollapseMoreInfoView();
            mContainer.addView(mExpandCollapseMoreInfoView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            if (mSplitView != null) {
                mContainer.addView(mSplitView);
            }

        }
        
        
        setStyle(lastItemId);
        
        mCallName=(TextView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_call_name_sec);
        mCallImg=(ImageView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_tel_card_sec);
        
        ThemeUtil.setTextColor(mContainer.getContext(), mCallName, (String) message.getValue("v_by_text_1"),R.color.duoqu_theme_color_5010);
        ChannelContentUtil.setText(mCallName, message.simName, "");

        int simIndex = message.simIndex;
        String drawable = getCardNumLogo(message, simIndex);
        if (!StringUtils.isNull(drawable)) {
            try {
                ViewManger.setViewBg(Constant.getContext(), mCallImg, drawable, R.drawable.duoqu_bottom_rectangle, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mCallImg.setVisibility(View.GONE);
        }

        TextView mExpandCollapseMoreTextView = (TextView) mExpandCollapseMoreInfoView
                .findViewById(R.id.duoqu_expand_button);
        mExpandCollapseMoreTextView.setTag(R.id.tag_business_sms_message, message);
        mExpandCollapseMoreTextView.setText(expanded ? ChannelContentUtil.MORE_INFO_HIDE : ChannelContentUtil.MORE_INFO_SHOW);
        showChangeToOriginSmsText(mDataSize);
        ChannelContentUtil.setViewVisibility(mSplitView, View.VISIBLE);
        ChannelContentUtil.setViewVisibility(mExpandCollapseMoreInfoView, View.VISIBLE);
    }

    /**
     * 设置展开收起控件样式
     * 
     * @param lastItemId
     *            最后一个item的viewId，展开收起控件将below该viewId
     */
    private void setStyle(int lastItemId) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mExpandCollapseMoreInfoView
                .getLayoutParams();
        layoutParams.addRule(RelativeLayout.BELOW, lastItemId);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        if (mSplitView != null) {
            layoutParams = (RelativeLayout.LayoutParams) mSplitView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.BELOW, lastItemId);
        }
    }
    
    

    /**
     * 创建展开收起控件
     * 
     * @return
     */
    @SuppressLint("NewApi")
    private View newExpandCollapseMoreInfoView() {
        final View expandCollapseMoreInfoView = View.inflate(mContainer.getContext(), R.layout.duoqu_expand_layout,
                null);
        final TextView expandCollapseMoreTextView = (TextView) expandCollapseMoreInfoView
                .findViewById(R.id.duoqu_expand_button);
        expandCollapseMoreInfoView
                .findViewById(R.id.tag_business_sms_message);

        expandCollapseMoreTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
        expandCollapseMoreTextView.getPaint().setAntiAlias(true);// 抗锯齿
        expandCollapseMoreTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) v.getTag(R.id.tag_business_sms_message);
                if (businessSmsMessage == null) {
                    return;
                }
                int visibility = getExpanded(businessSmsMessage) ? View.GONE : View.VISIBLE;
                try {
                    businessSmsMessage.bubbleJsonObj.put(KEY_EXPANDED, (visibility == View.VISIBLE));
                    // ParseManager.updateMatchCacheManager(businessSmsMessage);
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(
                            "DuoquTableViewShowMoreInfo newExpandCollapseMoreInfoView error:", e);
                }
                for (DuoquBaseHolder viewHolder : mMoreInfoViewHolder) {
                    viewHolder.setVisibility(visibility);
                }
                expandCollapseMoreTextView.setText(visibility == View.VISIBLE ? ChannelContentUtil.MORE_INFO_HIDE : ChannelContentUtil.MORE_INFO_SHOW);
                showChangeToOriginSmsText(mDataSize);
            }
        });
        return expandCollapseMoreInfoView;
    }

    /**
     * 获取展开状态
     * 
     * @param message
     * @return true:展开 false:收起
     */
    public boolean getExpanded(BusinessSmsMessage message) {
        return message.bubbleJsonObj != null && message.bubbleJsonObj.optBoolean(KEY_EXPANDED);
    }

    /**
     * 设置默认显示数据行数
     * 
     * @param defaultShowRow
     */
    public void setDefaultShowRow(int defaultShowRow) {
        mDefaultShowRow = defaultShowRow;
    }

    /**
     * 获取默认显示数据行数
     * 
     * @return
     */
    public int getDefaultShowRow() {
        return mDefaultShowRow;
    }

    private Drawable getExpandCollapseMoreInfoDrawable(BusinessSmsMessage message, int type) {
        String imageName = (String) message
                .getImgNameByKey(type == TYPE_EXPAND ? "v_by_iconname_3" : "v_by_iconname_4");
        if (!StringUtils.isNull(imageName)) {
            return ViewUtil.getDrawable(mContainer.getContext(), imageName, true, true);
        } else {
            return mContainer
                    .getContext()
                    .getResources()
                    .getDrawable(
                            type == TYPE_EXPAND ? R.drawable.duoqu_show_more_info_expand
                                    : R.drawable.duoqu_show_more_info_collapse);
        }
    }
    /* MEIZU-568 huangzhiqiang 20160427 end */
    
    private String getCardNumLogo(BusinessSmsMessage message, int simIndex) {
        String cardnumLogo=null;
        if (simIndex == 0) {
            cardnumLogo = (String) message.getImgNameByKey("v_tc_card_1");
                
        } else if(simIndex == 1){
            cardnumLogo = (String) message.getImgNameByKey("v_tc_card_2");
        }
        return cardnumLogo;
    }
}
