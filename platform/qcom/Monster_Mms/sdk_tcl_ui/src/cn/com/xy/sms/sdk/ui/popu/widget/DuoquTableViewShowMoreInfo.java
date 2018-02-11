package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

/* MEIZU-568 huangzhiqiang 20160427 start */
public class DuoquTableViewShowMoreInfo {

    public static final String KEY_EXPANDED = "expanded";
    private static final int TYPE_EXPAND = 0;
    private static final int TYPE_COLLAPSE = 1;
    private ViewGroup mContainer = null;
    private List<DuoquBaseHolder> mMoreInfoViewHolder = new LinkedList<DuoquBaseHolder>();
    private View mExpandCollapseMoreInfoView = null;
    private View mSplitView = null;
    private int mDefaultShowRow = 5;
    private int mDefaultLimitDataSize = 8;
    private int mDataSize = 0;
    public OnOpenSmsDetailListener onOpenSmsDetailListener = null;
    TextView mShowSms;
    TextView mButton;
    ImageView mImgBtn ;
    public interface OnOpenSmsDetailListener {
        public void onClick(View v);
    }

    public int getmDefaultLimitDataSize() {
        return mDefaultLimitDataSize;
    }

    public void setmDefaultLimitDataSize(int mDefaultLimitDataSize) {
        this.mDefaultLimitDataSize = mDefaultLimitDataSize;
    }

    public DuoquTableViewShowMoreInfo(ViewGroup container) {
        mContainer = container;
    }

    public DuoquTableViewShowMoreInfo(ViewGroup container, View splitView) {
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
            mShowSms = (TextView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_show_more_info);
            mButton = (TextView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_expand_button);
            mImgBtn = (ImageView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_expand_button_pic) ;
            BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) mButton.getTag(R.id.tag_business_sms_message);
            if (businessSmsMessage == null) {
                return;
            }
            if (getExpanded(businessSmsMessage)) {
                if (dataSize > mDefaultLimitDataSize) {
                    mShowSms.setVisibility(View.VISIBLE);
                    ThemeUtil.setTextColor(Constant.getContext(), mShowSms , (String) businessSmsMessage.getValue("v_by_packuptip"),R.color.duoqu_theme_color_4010);
                    mShowSms.setOnClickListener(new OnClickListener() {

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
                    mShowSms.setVisibility(View.GONE);
                }
            } else {
                mShowSms.setVisibility(View.GONE);
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
        ChannelContentUtil.setViewVisibility(mShowSms, View.VISIBLE);
        ChannelContentUtil.setViewVisibility(mButton, View.VISIBLE);
        if (mExpandCollapseMoreInfoView == null) {
            mExpandCollapseMoreInfoView = newExpandCollapseMoreInfoView();
            LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 20.5, Constant.getContext().getResources().getDisplayMetrics());
            layoutParams.leftMargin=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) -5.5, Constant.getContext().getResources().getDisplayMetrics());
            layoutParams.rightMargin=0;
            layoutParams.bottomMargin=0;
            mContainer.addView(mExpandCollapseMoreInfoView,layoutParams);
            if (mSplitView != null) {
                mExpandCollapseMoreInfoView.setMinimumHeight(30);
                mContainer.addView(mSplitView);
            }

        }
        
        setStyle(lastItemId);
        
        TextView mExpandCollapseMoreTextView = (TextView) mExpandCollapseMoreInfoView
                .findViewById(R.id.duoqu_expand_button);
        ImageView mExpandCollapseMoreImageView = (ImageView) mExpandCollapseMoreInfoView.findViewById(R.id.duoqu_expand_button_pic) ;
        mExpandCollapseMoreTextView.setTag(R.id.tag_business_sms_message, message);
        mExpandCollapseMoreTextView.setText(expanded ? ChannelContentUtil.MORE_INFO_HIDE : ChannelContentUtil.MORE_INFO_SHOW);
        ThemeUtil.setTextColor(mContainer.getContext(), mExpandCollapseMoreTextView, getMoreTextColor(message, expanded ? TYPE_COLLAPSE : TYPE_EXPAND),R.color.duoqu_theme_color_4010);
        mExpandCollapseMoreImageView.setImageDrawable(getExpandCollapseMoreInfoDrawable(message,expanded ? TYPE_COLLAPSE : TYPE_EXPAND));
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
        final ImageView expandCollapseMoreImageView = (ImageView) expandCollapseMoreInfoView.findViewById(R.id.duoqu_expand_button_pic) ;
        expandCollapseMoreTextView.getPaint().setAntiAlias(true);
        expandCollapseMoreTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { 
                OnClickMoreButton(v, expandCollapseMoreTextView , expandCollapseMoreImageView); 
            }
        });
        expandCollapseMoreImageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                OnClickMoreButton(v, expandCollapseMoreTextView , expandCollapseMoreImageView);
            }
        });
        return expandCollapseMoreInfoView;
    }

    private void OnClickMoreButton(View v,TextView expandCollapseMoreTextView,ImageView expandCollapseMoreImageView){
        BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) v.getTag(R.id.tag_business_sms_message);
        if (businessSmsMessage == null) {
            return;
        }
        int visibility = getExpanded(businessSmsMessage) ? View.GONE : View.VISIBLE;
        try {
            businessSmsMessage.bubbleJsonObj.put(KEY_EXPANDED, (visibility == View.VISIBLE));
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(
                    "DuoquTableViewShowMoreInfo newExpandCollapseMoreInfoView error:", e);
        }
        for (DuoquBaseHolder viewHolder : mMoreInfoViewHolder) {
            viewHolder.setVisibility(visibility);
        }
        expandCollapseMoreTextView.setText(visibility == View.VISIBLE ? ChannelContentUtil.MORE_INFO_HIDE : ChannelContentUtil.MORE_INFO_SHOW);
        ThemeUtil.setTextColor(mContainer.getContext(), expandCollapseMoreTextView, getMoreTextColor(businessSmsMessage, visibility == View.VISIBLE ? TYPE_COLLAPSE : TYPE_EXPAND),R.color.duoqu_theme_color_4010);
        expandCollapseMoreImageView.setImageDrawable(getExpandCollapseMoreInfoDrawable(businessSmsMessage,visibility == View.VISIBLE ? TYPE_COLLAPSE : TYPE_EXPAND));
        showChangeToOriginSmsText(mDataSize);
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
                .getImgNameByKey(type == TYPE_EXPAND ? "v_by_icon_5" : "v_by_icon_6");
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
    private String getMoreTextColor(BusinessSmsMessage message , int type){
        Object valueColor ;
        if (type== TYPE_EXPAND) {
            valueColor = message.getValue("v_by_more") ;
        }else {
            valueColor = message.getValue("v_by_packup") ;
        }
        String strColor = null  ;
        if (valueColor!=null) {
            strColor = (String) valueColor ;
        }
        return strColor ; 
        
    }
}
