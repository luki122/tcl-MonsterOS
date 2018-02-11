package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;

/**
 * Two fonts level relationship in a Item ---------------- XXXXX XXXXXX
 * ---------------- XXXXX XXXXXX ---------------- .
 * 
 * @author Administrator
 * 
 */
public class DuoquVerticalItemTable extends DuoquBaseTable {
    private int mTitleSize = 0;
    private int mContentSize = 0;
    private int mTitleColor = 0;
    private int mContentColor = 0;
    private int mTitlePaddingTop = 0;
    private int mTitlePaddingLeft = 5 ;
    private int mContentPaddingTop = 0;
    private int mContentPaddingLeft = 0;
    private int mLineSpacing = 0;
    private int mMarginTop = 0;
    private String mSingleLine = null;
    public DuoquTableViewShowMoreInfo mDuoquTableViewShowMoreInfo;
    private boolean mExpanded = false;
    private int mLineSpacingLeft;
    private int mFirstTitlePaddingTop = 0;
    private boolean mUseFirstTitle = true;

    public boolean ismUseFirstTitle() {
        return mUseFirstTitle;
    }

    public void setmUseFirstTitle(boolean mUseFirstTitle) {
        this.mUseFirstTitle = mUseFirstTitle;
    }

    public DuoquVerticalItemTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
        mDuoquTableViewShowMoreInfo = new DuoquTableViewShowMoreInfo(this, null);
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
            /* MEIZU-568 huangzhiqiang 20160427 end */
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquHorizItemTable setContentList error:", e);
        }
    }

    @Override
    protected void initParams(Context context, AttributeSet attrs) {
        TypedArray duoquTbAttr = context.obtainStyledAttributes(attrs, R.styleable.duoqu_table_attr);
        mTitleSize = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_title_textsize, 0));
        mContentSize = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_content_textsize, 0));
        mTitleColor = duoquTbAttr.getResourceId(R.styleable.duoqu_table_attr_title_textcolor, 0);
        mContentColor = duoquTbAttr.getResourceId(R.styleable.duoqu_table_attr_content_textcolor, 0);
        mTitlePaddingTop = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_title_paddingtop, 0));
        mTitlePaddingLeft = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_title_paddingleft, 0)) ;
        mContentPaddingLeft = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_content_paddingleft, 0));
        mContentPaddingTop = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_content_paddingtop, 0));
        mLineSpacing = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_line_spacing, 0));
        mSingleLine = duoquTbAttr.getString(R.styleable.duoqu_table_attr_single_line);
        mMarginTop = Math.round(duoquTbAttr.getDimension(R.styleable.duoqu_table_attr_margin_top, 0));
        mFirstTitlePaddingTop = Math.round(duoquTbAttr.getDimension(
                R.styleable.duoqu_table_attr_first_title_paddingtop, 0));

        duoquTbAttr.recycle();
        if (mMarginTop != 0) {
            RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, mMarginTop, 0, 0);
            this.setLayoutParams(rp);
        }
    }
    @SuppressLint("ResourceAsColor")
    @Override
    protected void getHolder(int pos, BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        DuoquHorizTableViewHolder holder = new DuoquHorizTableViewHolder();
        holder.mTitleView = new TextView(this.getContext());
        holder.mContentView = new TextView(this.getContext());
        // holder.titleView.setTypeface(ContentUtil.SIMPLIFIED);
        // holder.contentView.setTypeface(ContentUtil.SIMPLIFIED);
        holder.mTitleView.setId(++mChildId);
        RelativeLayout.LayoutParams leftParam = getLayoutParams(mChildId);
        this.addView(holder.mTitleView, leftParam);

        holder.mContentView.setId(++mChildId);
        RelativeLayout.LayoutParams rightParam = getLayoutParams(mChildId);
        this.addView(holder.mContentView, rightParam);

        holder.mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
        holder.mContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContentSize);
        /* TCL-162 linwejie 2016-09-21 start  */
        Object valueTitleColor = message.getValue("v_by_text_u_2");
        if (valueTitleColor!=null || "".equals((String)valueTitleColor)) {
            String strTitleColor = (String) valueTitleColor ;
            ThemeUtil.setTextColor(Constant.getContext(), holder.mTitleView, strTitleColor, R.color.duoqu_title_color);
        }else {
            holder.mTitleView.setTextColor(holder.mTitleView.getContext().getResources().getColor(mTitleColor));
        }
        Object valueContentColor= message.getValue("v_by_text_d_2");
        if (valueContentColor!=null || "".equals((String)valueContentColor)) {
            String strContentColor = (String) valueContentColor ;
        ThemeUtil.setTextColor(Constant.getContext(), holder.mContentView, strContentColor, R.color.duoqu_theme_color_4010);
        }else {
            holder.mContentView.setTextColor(holder.mContentView.getContext().getResources().getColor(mContentColor));
        }
        /* TCL2.0 linwejie 2016-09-21 end */
        if (mTitlePaddingTop > 0 || mFirstTitlePaddingTop > 0) {
            int paddingTop = 0;
            if (holder.mTitleView.getId() == 1 && mUseFirstTitle) {
                paddingTop = mFirstTitlePaddingTop;
            } else {
                paddingTop = mTitlePaddingTop;
            }
            RelativeLayout.LayoutParams tmpParas = (LayoutParams) holder.mTitleView.getLayoutParams();
            tmpParas.topMargin = paddingTop;
            if (mTitlePaddingLeft > 0 ) {
                tmpParas.leftMargin = mTitlePaddingLeft ;
            }else {
                tmpParas.leftMargin = 0;
            }
            tmpParas.bottomMargin = 0;
            tmpParas.rightMargin = 0;
            holder.mTitleView.setLayoutParams(tmpParas);
        }

        if (mContentPaddingLeft > 0 || mTitlePaddingTop > 0) {
            RelativeLayout.LayoutParams tmpParas = (LayoutParams) holder.mContentView.getLayoutParams();
            tmpParas.topMargin = mContentPaddingTop;
            tmpParas.bottomMargin = 0;
            tmpParas.leftMargin = mContentPaddingLeft;
            tmpParas.rightMargin = 0;
            holder.mContentView.setLayoutParams(tmpParas);
        }

        if ("true".equals(mSingleLine)) {
            holder.mContentView.setSingleLine();
            holder.mContentView.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        }
        holder.setContent(pos, message, dataKey, isRebind);
        mDuoquBaseHolderList.add(holder);
    }

    @Override
    public RelativeLayout.LayoutParams getLayoutParams(int childId) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (childId == 1) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            params.addRule(RelativeLayout.BELOW, childId - 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        return params;
    }

    /* MEIZU-568 huangzhiqiang 20160427 start */
    /**
     * 根据展开状态显示或隐藏更多信息
     * 
     * @param pos
     */
    private void showOrHiddenMoreInfo(int pos) {
        if (pos < mDuoquTableViewShowMoreInfo.getDefaultShowRow() || mDuoquBaseHolderList == null
                || mDuoquBaseHolderList.size() < pos) {
            return;
        }

        DuoquHorizTableViewHolder viewHolder = (DuoquHorizTableViewHolder) mDuoquBaseHolderList.get(pos);
        if (pos < mDuoquTableViewShowMoreInfo.getmDefaultLimitDataSize()) {
            viewHolder.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
            /* QIK-574 /2016.07.12/ wangxingjian start */
            mDuoquTableViewShowMoreInfo.addMoreInfoViewHolder(viewHolder);
            /* QIK-574 /2016.07.12/ wangxingjian end */
        } else {
            viewHolder.setVisibility(View.GONE);
        }
    }

    /**
     * 根据当前数据数量显示或隐藏展开收起控件
     * 
     * @param message
     * @param dataSize
     *            当前数据数量
     */
    private void setShowHiddenMoreInfoButtonVisibility(final BusinessSmsMessage message, int dataSize) {
        if (dataSize <= mDuoquTableViewShowMoreInfo.getDefaultShowRow()) {
            mDuoquTableViewShowMoreInfo.hiddenExpandCollapseMoreInfoView();
        } else {
            int lastItemId = mDuoquBaseHolderList.get(dataSize - 1).mContentView.getId();
            mDuoquTableViewShowMoreInfo.showExpandCollapseMoreInfoView(message, mExpanded, lastItemId, dataSize);
        }
    }

    /**
     * 创建展开收起控件于表格数据分割线
     * 
     * @return
     */
    private ImageView newSplitImageView() {
        ImageView splitImageView = new ImageView(this.getContext());
        splitImageView.setBackgroundResource(R.drawable.duoqu_dotted_split);
        RelativeLayout.LayoutParams lineParam = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3);
        lineParam.setMargins(mLineSpacingLeft, 0, mLineSpacingLeft, 0);
        splitImageView.setLayoutParams(lineParam);
        return splitImageView;
    }

    /**
     * 设置默认显示数据行数
     * 
     * @param defaultShowRow
     */
    public void setDefaultShowRow(int defaultShowRow) {
        mDuoquTableViewShowMoreInfo.setDefaultShowRow(defaultShowRow);
    }

    public void setmDefaultLimitDataSize(int defaultLimitRow) {
        mDuoquTableViewShowMoreInfo.setmDefaultLimitDataSize(defaultLimitRow);
    }
    /* MEIZU-568 huangzhiqiang 20160427 end */
}
