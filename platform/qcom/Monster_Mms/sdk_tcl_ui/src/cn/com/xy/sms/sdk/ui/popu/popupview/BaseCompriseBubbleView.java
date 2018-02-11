package cn.com.xy.sms.sdk.ui.popu.popupview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.widget.IViewAttr;

public class BaseCompriseBubbleView {
    private static final String TAG = "BaseCompriseBubbleView";
    public ImageView mMarkExpiresView = null;
    public List<UIPart> mHeadUIPartList = null;// View the head set
    public List<UIPart> mBodyUIPartList = null;// central view collection
    public List<UIPart> mFootUIPartList = null;// View at the bottom of the
                                               // collection
    public Map<String, PartViewParam> mPartViewMap = null;// The view parameter
                                                          // rules
    public BusinessSmsMessage mBusinessSmsMessage;
    public Activity mContext;
    public XyCallBack mCallback = null;
    public ViewGroup mRoot = null;
    public ViewGroup mHeadBodyContainer = null;
    public int mPopupContentPadding = 0;// Both sides spacing
    private final int FIRST_PREV_ID = 100000;
    int mPrevId = FIRST_PREV_ID;

    public BaseCompriseBubbleView(Activity context, XyCallBack callback, BusinessSmsMessage message, ViewGroup root) {
        initData(context, callback, message, root);
        mPopupContentPadding = Math.round(Constant.getContext().getResources()
                .getDimension(R.dimen.popup_content_padding));
    }

    public void addViews(ViewGroup root, BasePopupView bubbleView) throws Exception {
        if (root != null) {
            this.mRoot = root;
        }

        addUIPartToRoot(PartViewParam.HEAD, mHeadUIPartList, bubbleView);
        addUIPartToRoot(PartViewParam.BODY, mBodyUIPartList, bubbleView);
        addUIPartToRoot(PartViewParam.FOOT, mFootUIPartList, bubbleView);
    }

    public void initData(Activity context, XyCallBack callback, BusinessSmsMessage message, ViewGroup root) {
        this.mContext = context;
        this.mCallback = callback;
        this.mBusinessSmsMessage = message;
        this.mRoot = root;
        try {
            mPartViewMap = (Map<String, PartViewParam>) message.getValue("viewPartParam");
            if (mPartViewMap == null) {
                SmartSmsSdkUtil.smartSdkExceptionLog("duoqu_xiaoyuan BaseCompriseBubbleView.initData mPartViewMap is null.", null);
                return;
            }
            mHeadUIPartList = initUIPart(PartViewParam.HEAD, mPartViewMap);
            mBodyUIPartList = initUIPart(PartViewParam.BODY, mPartViewMap);
            mFootUIPartList = initUIPart(PartViewParam.FOOT, mPartViewMap);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    public List<UIPart> initUIPart(String type, Map<String, PartViewParam> partViewMap) throws Exception {

        PartViewParam param = partViewMap.get(type);
        List<UIPart> partList = null;
        if (param == null || param.mLayOutList == null) {
            return partList;
        }
        int size = param.mLayOutList.size();
        partList = new ArrayList<UIPart>();
        UIPart tempPart = null;
        for (int i = 0; i < size; i++) {
            tempPart = ViewManger.getViewManger().getUIPartByPartId(mContext, mBusinessSmsMessage, mCallback, mRoot,
                    param.mLayOutList.get(i));
            if (tempPart != null) {
                partList.add(tempPart);
            }
        }
        return partList;

    }

    /**
     * Set the view of high wide
     * 
     * @param view
     * @param viewParamRule
     */
    public void setViewLayoutParam(View view, PartViewParam viewParamRule, UIPart part) throws Exception {
        if (view == null) {
            return;
        }
        int margin = 0;
        if (part == null) {
            return;
        }
        Integer mgType = (Integer) part.getParam("MLR");// Obtaining UI_PART
                                                        // about spacing
        if (mgType != null) {
            margin = ViewManger.getInnerLayoutMargin(Constant.getContext(), mgType);
        }
        Integer h = (Integer) part.getParam("H");// Obtain the UI_PART height
        if (h != null) {
            view.getLayoutParams().height = h;
        }
        Integer mtpo = (Integer) part.getParam("MTPO");// Obtaining UI_PART
                                                       // about spacing
        // LogManager.e("duoqu_test",
        // "MTPOMTPO: "+mtpo+" partid: "+part.mPartId);
        if (mtpo != null) {
            if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin = mtpo;
            }
        }

        if (margin > 0) {
            if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams vMp = ((ViewGroup.MarginLayoutParams) view.getLayoutParams());
                vMp.leftMargin = margin;
                vMp.rightMargin = margin;
            }
        }
        //TODO
//        if (part instanceof BubbleBottomTwo) {
//            ((BubbleBottomTwo) part).setLayoutParam();
//        }
        if (view instanceof IViewAttr) {
            IViewAttr iAttr = (IViewAttr) view;
            int height = ViewManger.getDouquAttrDimen(iAttr, R.styleable.duoqu_attr_duoqu_height);
            if (height > 0) {
                view.getLayoutParams().height = height;
            }
        }
    }

    public static RelativeLayout.LayoutParams getRelativeLayoutParam(RelativeLayout.LayoutParams lp, int verb,
            int viewId, int... verbs) {
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
        if (viewId > 0) {
            lp.addRule(verb, viewId);
        }
        if (verbs != null) {
            for (int v : verbs) {
                lp.addRule(v);
            }
        }
        return lp;
    }

    public static RelativeLayout.LayoutParams getRelativeLayoutParam2(RelativeLayout.LayoutParams lp, int viewId,
            int... verbs) {
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
        if (viewId > 0 && verbs != null) {
            for (int v : verbs) {
                lp.addRule(v, viewId);
            }
        }
        return lp;
    }

    public RelativeLayout.LayoutParams getGloabRelativeLayoutParams(View view, int prevId) {
        RelativeLayout.LayoutParams lp = null;
        LayoutParams vlp = view.getLayoutParams();
        if (vlp != null && vlp instanceof RelativeLayout.LayoutParams) {
            lp = (RelativeLayout.LayoutParams) vlp;
        }
        // Log.e("duoqu_test", "getGloabRelativeLayoutParams lp : "+lp);
        if (prevId == FIRST_PREV_ID) {
            lp = getRelativeLayoutParam(lp, -1, -1, RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            lp = getRelativeLayoutParam(lp, RelativeLayout.BELOW, prevId);
        }
        if (mBusinessSmsMessage.viewType == 0) {// Popup window
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else {
            lp.addRule(RelativeLayout.ALIGN_LEFT);
        }
        return lp;
    }

    private void addUIPartToRoot(String uiType, List<UIPart> uiPartList, BasePopupView bubbleView) throws Exception {

        if (uiPartList != null) {
            int size = uiPartList.size();
            if (size <= 0)
                return;
            // Log.i("MainActivity", "$$$$$$$$$$$$$$$$$$$ uiPartList size: " +
            // size);
            UIPart uiPart = null;
            PartViewParam viewParamRule = mPartViewMap.get(uiType);
            boolean isBodyUi = PartViewParam.BODY.equals(uiType);
            int newViewId = mPrevId + 1;
            for (int i = 0; i < size; i++) {
                uiPart = uiPartList.get(i);
                uiPart.mBasePopupView = bubbleView;
                uiPart.build();
                RelativeLayout.LayoutParams lp = getGloabRelativeLayoutParams(uiPart.mView, mPrevId);
                uiPart.mView.setId(newViewId);
                mRoot.addView(uiPart.mView, lp);
                setViewLayoutParam(uiPart.mView, viewParamRule, uiPart);
                if (uiPart.mView != null && viewParamRule != null && i > 0) {// MarginTop
                                                                             // between
                                                                             // set
                                                                             // view
                                                                             // and
                                                                             // view
                    ViewManger.setLayoutMarginTop(Constant.getContext(), uiPart.mView.getLayoutParams(),
                            viewParamRule.mUiPartMarginTopType);
                }
                if (isBodyUi) {
                    ViewManger.setBodyViewPadding(Constant.getContext(), uiPart.mView, uiPart.mView, viewParamRule,
                            mPopupContentPadding);
                }
                mPrevId = newViewId;
                newViewId++;

            }
        }
    }

    private void destory(List<UIPart> UIPartList) {
        if (UIPartList != null) {
            for (UIPart part : UIPartList) {
                part.destroy();
            }
            UIPartList.clear();
        }
    }

    public void destory() {
        destory(mHeadUIPartList);// View the head set
        destory(mBodyUIPartList);// The central view collection
        destory(mFootUIPartList);// View at the bottom of the collection
        mPartViewMap = null;
        mBusinessSmsMessage = null;
        mContext = null;
        mCallback = null;
        mRoot = null;
        mHeadUIPartList = null;
        mBodyUIPartList = null;
        mFootUIPartList = null;
    }

    /**
     * Rebind the data
     * 
     * @param businessSmsMessage
     */
    public void reBindData(Activity context, BusinessSmsMessage businessSmsMessage, boolean reBindData)
            throws Exception {
        this.mBusinessSmsMessage = businessSmsMessage;
        if (mHeadUIPartList != null) {
            for (UIPart part : mHeadUIPartList) {
                part.mContext = context;
                part.setContent(businessSmsMessage, reBindData);
            }
        }
        if (mBodyUIPartList != null) {
            for (UIPart part : mBodyUIPartList) {
                part.mContext = context;
                part.setContent(businessSmsMessage, reBindData);
            }
        }
        if (mFootUIPartList != null) {
            for (UIPart part : mFootUIPartList) {
                part.mContext = context;
                part.setContent(businessSmsMessage, reBindData);

            }
        }
    }
}
