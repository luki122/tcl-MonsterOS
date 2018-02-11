package cn.com.xy.sms.sdk.ui.popu.popupview;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.UiPartInterface;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

/**
 * 
 * @author Administrator
 * 
 */
public class BubblePopupView extends BasePopupView implements IBubbleView {

    private Integer mDuoquBubbleViewWidth = null;
    private ImageView mOverMark           = null;
    private ImageView mCollectionView     = null;
    private UiPartInterface mUiInterface;
    
    public BubblePopupView(Context context) {
        super(context);
    }

    public BubblePopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initExtendParamData(boolean isRebind) {
        mDuoquBubbleViewWidth = (Integer) mBusinessSmsMessage
                .getExtendParamValue("duoqu_bubble_view_width");
        if (!isRebind) {
            Integer bgResId = (Integer) mBusinessSmsMessage
                    .getExtendParamValue("duoqu_bg_resid");
            if (bgResId != null) {
                mView.setBackgroundResource(bgResId);
            } else {
                Drawable bg = (Drawable) mBusinessSmsMessage
                        .getExtendParamValue("duoqu_bg_drawable");
                if (bg != null) {
                    ViewUtil.setBackground(mView, bg);
                }
            }
        }
    }

    @Override
    public void initUIAfter() {
        initExtendParamData(false);
        // Set the left and top padding
        Integer leftPadding = (Integer) mBusinessSmsMessage
                .getExtendParamValue("duoqu_leftPadding");
        Integer topPadding = (Integer) mBusinessSmsMessage
                .getExtendParamValue("duoqu_topPadding");
        this.setPadding(leftPadding == null ? this.getPaddingLeft()
                : leftPadding, topPadding == null ? this.getPaddingTop()
                : topPadding, this.getPaddingTop(), this.getPaddingBottom());
        setLayoutParam();
        updateMarkView();
        updateCollectionView();
    }
    
    /* NUBIA-77 zhaoxiachao 20161109 start */
    public void updateCollectionView() {
        // TODO Auto-generated method stub
        Boolean collectionFlag = (Boolean) mBusinessSmsMessage.extendParamMap.get("isFavourited");
        if(collectionFlag != null){
            showCollectionView(collectionFlag);
        }else{
            if(mCollectionView!=null){
                mCollectionView.setVisibility(View.GONE);
            }
        }
        
    }

    private void showCollectionView(boolean showFlag) {
        // TODO Auto-generated method stub
        if (mBusinessSmsMessage == null) {
            setCollectionViewVisible(false);
            return;
        }

        if(showFlag){
            if (mCollectionView == null) {
                mUiInterface = ViewManger.getUiPartInterface();
                ImageView tempView = (ImageView) mUiInterface.doUiActionMulti(ViewManger.UIPART_ACTION_ADD_COLLECTION, mBusinessSmsMessage);
                if(tempView == null){
                    addCollectionView();
                }else{
                    if (mView != null) {
                        mView.addView(tempView);
                        mCollectionView = tempView;
                    }
                }
            }
            setCollectionViewVisible(true);
        }else{
            setCollectionViewVisible(false);
        }
    }

    private void setCollectionViewVisible(boolean visible) {
        int visibleState = GONE;
        if (visible) {
            visibleState = VISIBLE;
        }

        if (mCollectionView != null && (mCollectionView.getVisibility() != visibleState)) {
            mCollectionView.setVisibility(visibleState);
        }
    }
    
    private void addCollectionView() {
        
        if (mBusinessSmsMessage == null) {
            return;
        }

        ImageView collection = null;
        Drawable dr = getResources().getDrawable(R.drawable.dia_xing);
        if (dr == null) {
            return;
        }

        collection = new ImageView(getContext());
        collection.setImageDrawable(dr);

        LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        collection.setPadding(0, 0, 0, 0);
        if (mView != null) {
            mView.addView(collection, param);
            mCollectionView = collection;
        }
    }
    /* NUBIA-77 zhaoxiachao 20161109 end */
    private void updateMarkView() {
        showExpireMarkView();
    }

    private void showExpireMarkView() {
        showExpireMarkView(0L);
    }

    private void showExpireMarkView(long deadLine) {
        if (mBusinessSmsMessage == null || !isExpire(deadLine)) {
            setMarkViewVisible(false, EXPIRE_TYPE);
            return;
        }

        if (mOverMark == null) {
            addMarkView();
        }
        setMarkViewVisible(true, EXPIRE_TYPE);
    }

    private boolean isExpire(long deadLine) {
        long deadLineF = 0L;
        if (deadLine > 0L) {
            deadLineF = deadLine;
        } else {
            try {
                deadLineF = mBusinessSmsMessage.bubbleJsonObj.optLong("deadline");
            } catch (Exception e) {
                deadLineF = -1;
            }
        }

        if (deadLineF <= 0) {
            return false;
        }

        long current = System.currentTimeMillis();
        return current > deadLineF;
    }

    private final static int EXPIRE_TYPE = 1;

    private void setMarkViewVisible(boolean visible, int type) {
        int visibleState = GONE;
        if (visible) {
            visibleState = VISIBLE;
        }

        switch (type) {
        case EXPIRE_TYPE:
            if (mOverMark != null && (mOverMark.getVisibility() != visibleState)) {
                mOverMark.setVisibility(visibleState);
            }
            break;
        default:
        }
    }

    private void addMarkView() {
        if (mBusinessSmsMessage == null) {
            return;
        }

        ImageView mark = null;
        Drawable dr = ViewUtil.getDrawable(getContext(), (String) mBusinessSmsMessage.getValue("v_by_mark_1"), false,
                false);
        if (dr == null) {
            return;
        }

        mark = new ImageView(getContext());
        mark.setImageDrawable(dr);

        LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        try {
            int paddingTpx = -1;
            int paddingRpx = -1;

            String paddingTString = (String) mBusinessSmsMessage.getValue("v_by_mark_1_padding_u");
            String paddingRString = (String) mBusinessSmsMessage.getValue("v_by_mark_1_padding_r");
            if (!StringUtils.isNull(paddingTString)) {
                paddingTpx = ContentUtil.getPxDimensionFromString(getContext(), paddingTString);
            }

            if (!StringUtils.isNull(paddingRString)) {
                paddingRpx = ContentUtil.getPxDimensionFromString(getContext(), paddingRString);
            }

            if (paddingTpx < 0) {
                paddingTpx = getResources().getDimensionPixelOffset(R.dimen.duoqu_ui_overmark_padding_top);
            }

            if (paddingRpx < 0) {
                paddingRpx = getResources().getDimensionPixelOffset(R.dimen.duoqu_ui_overmark_padding_right);
            }

            mark.setPadding(0, paddingTpx, paddingRpx, 0);
        } catch (Throwable e1){
            SmartSmsSdkUtil.smartSdkExceptionLog("BubblePopupView addMarkView", e1);
        }

        if (mView != null) {
            mView.addView(mark, param);
            mOverMark = mark;
        }
    }
    public void initUIPartBefore(Activity mContext,
            BusinessSmsMessage businessSmsMessage) {
        this.mView = this;
    }

    void setLayoutParam() {
        int width = LayoutParams.WRAP_CONTENT;
        if (mDuoquBubbleViewWidth != null) {
            width = mDuoquBubbleViewWidth;
        }
        ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(width,
                    LayoutParams.WRAP_CONTENT);
        } else {
            lp.width = width;
        }
        mView.setId(cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager.DUOQU_BUBBLE_VIEW_ID);
        mView.setLayoutParams(lp);
        // this.addView(mView, lp);
    }

    @Override
    public void reBindData(Activity context,
            BusinessSmsMessage businessSmsMessage) throws Exception {
        if (mBusinessSmsMessage.messageBody == null) {
            SmartSmsSdkUtil.smartSdkExceptionLog(
                    "duoqu_xiaoyuan mBusinessSmsMessage.messageBody is null reBindData false.", null);
            // befroe init Bubble BusinessSmsMessage data is empty. need reinit
            // all data
            initData(businessSmsMessage);
            bindData(context, false);
            updateMarkView();
            updateCollectionView();
            return;
        }
        super.reBindData(context, businessSmsMessage);
        initExtendParamData(true);
        // LogManager.e("duoqu_xiaoyuan", "w1: "+
        // this.mView.getLayoutParams().width + " w2: "+
        // mDuoquBubbleViewWidth+"  hascode: "+businessSmsMessage.hashCode());
        if (mDuoquBubbleViewWidth != null
                && this.mView.getLayoutParams().width != mDuoquBubbleViewWidth) {
            ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
            lp.width = mDuoquBubbleViewWidth;
            this.mView.setLayoutParams(lp);
        }
        updateMarkView();
        updateCollectionView();
    }

    @Override
    public void addExtendView(View view, int place) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAllExtendView() throws Exception {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void changeData(Map<String, Object> param) {
        super.changeData(param);
        if (param == null || !param.containsKey("deadline")) {
            return;
        }

        long dl = 0L;
        try {
            dl = (Long) param.get("deadline");
        } catch (Exception e) {
            dl = 0L;
        }

        if (dl > 0L) {
            showExpireMarkView(dl);
        }
    }

}
