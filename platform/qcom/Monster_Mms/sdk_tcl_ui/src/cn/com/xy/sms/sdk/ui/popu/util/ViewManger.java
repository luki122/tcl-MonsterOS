package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.BusinessReceiveSmsActivity;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import cn.com.xy.sms.sdk.ui.popu.widget.IViewAttr;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;

public class ViewManger {
    private static final String TAG = "ViewManger";
    public static final int ONE_SIDE_POPUPVIEW = 1;
    private static final int TYPE_PADDING_11 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_padding_11);
    private static final int TYPE_VIEW_HEIGHT_11 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_view_height_11);
    private static final int TYPE_SPLIT_LR_MARGIN_111 = getIntDimen(Constant.getContext(),
            R.dimen.duoqu_type_split_lr_margin_111);
    private static final int TYPE_SPLIT_LR_MARGIN_112 = getIntDimen(Constant.getContext(),
            R.dimen.duoqu_type_split_lr_margin_112);
    private static final int TYPE_MARGIN_11 = getIntDimen(Constant.getContext(), R.dimen.duoqu_type_margin_11);

    /*
     * 101-499 for head;501~899 for body;901~999 for button Don't set in
     * multiples of 100 number
     */
    /**
     * Head part
     * 
     * @param context
     * @param message
     * @param xyCallBack
     * @param root
     * @param partId
     * @return
     * @throws Exception
     */
    private UIPart getHeadUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        return part;
    }

    /**
     * Body part
     * 
     * @param context
     * @param message
     * @param xyCallBack
     * @param root
     * @param partId
     * @return
     * @throws Exception
     */
    private UIPart getBodyUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        return part;
    }

    /**
     * Foot part
     * 
     * @param context
     * @param message
     * @param xyCallBack
     * @param root
     * @param partId
     * @return
     * @throws Exception
     */
    private UIPart getFootUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        return part;
    }

    static boolean checkHasViewPartId(int partId) throws Exception {
        Integer VIEW_PART_ID[] = getViewManger().getViewPartIdArr();
        for (Integer i : VIEW_PART_ID) {
            if (i == partId) {
                return true;
            }
        }
        throw new Exception("checkHasViewPartId partId: " + partId + " not Find.");
    }

    public Integer[] getViewPartIdArr() {
        return null;
    };

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width)
            throws Exception {
        setViewBg(context, view, relativePath, resId, width, false);
    }

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width, boolean cache)
            throws Exception {
        setViewBg(context, view, relativePath, resId, width, cache, false);
    }

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width, boolean cache,
            boolean needColorDw) throws Exception {
        // LogManager.i("setViewBg", "relativePath=" + relativePath + "resId=" +
        // resId);
        // if (context == null)
        // return;
        try {
            Drawable dw = ViewUtil.getDrawable(context, relativePath, needColorDw, cache);
            // Drawable dw =null;
            if (dw != null) {
                ViewUtil.setBackground(view, dw.mutate());
            } else {
                if (resId != -1) {
                    view.setBackgroundResource(resId);
                    // view.setBackgroundDrawable(dw);
                    // view.setTag(true);
                    GradientDrawable myGrad = (GradientDrawable) view.getBackground();
                    if (!StringUtils.isNull(relativePath)) {
                        int color = ResourceCacheUtil.parseColor(relativePath);
                        myGrad.setColor(color);
                        width = width > 0 ? width : 0;
                        // if (width > 0) {
                        myGrad.setStroke(width, color);
                        // } else {
                        // myGrad.setStroke(0,ResourceCacheUtil
                        // .parseColor(relativePath));
                        // }
                    }
                    // view.setBackground(myGrad);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

    }

    public static void setViewBg(Context context, View view, String bgColor, String strokeColor, int resId, int width)
            throws Exception {
        // LogManager.i("setViewBg", "bgColor=" + bgColor + "strokeColor=" +
        // strokeColor + "resId=" + resId);
        if (context == null)
            return;
        try {
            if (view != null && !StringUtils.isNull(bgColor) && !StringUtils.isNull(strokeColor)) {
                bgColor = bgColor.trim();
                strokeColor = strokeColor.trim();
                try {
                    view.setBackgroundResource(resId);

                    GradientDrawable myGrad = (GradientDrawable) view.getBackground();
                    if (!StringUtils.isNull(bgColor)) {
                        myGrad.setColor(ResourceCacheUtil.parseColor(bgColor));
                    }
                    if (!StringUtils.isNull(strokeColor)) {
                        myGrad.setStroke(width, ResourceCacheUtil.parseColor(strokeColor));

                    }
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                }

            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }

    }

    public static View createContextByLayoutId(Context packAgeCtx, int layoutId, ViewGroup root) {
        try {
            LayoutInflater currentInflater = (LayoutInflater) packAgeCtx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return currentInflater.inflate(layoutId, root);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }

    public static boolean isPopupAble(Map<String, Object> handervalueMap, String titleNo) {

        try {
            // Log.i("handervalueMap", handervalueMap.toString());
            if (handervalueMap == null || StringUtils.isNull(titleNo))
                return false;
            int viewid = -1;
            if (handervalueMap.containsKey("View_viewid")) {
                String id = (String) handervalueMap.get("View_viewid");
                // if (LogManager.debug) {
                // Log.i("PopupMsgManager", "View_viewid=" + id);
                // }
                if (!StringUtils.isNull(id)) {
                    viewid = Integer.parseInt(id);

                    try {
                        String viewPartParam = (String) handervalueMap.get("View_fdes");
                        // viewPartParam="H254;B652,100000;F954950";
                        Map<String, PartViewParam> viewPartParamMap = parseViewPartParam(viewPartParam);
                        if (viewPartParamMap != null && !viewPartParamMap.isEmpty()) {
                            handervalueMap.put("viewPartParam", viewPartParamMap);
                            if (viewid == ViewManger.ONE_SIDE_POPUPVIEW) {
                                //
                            } else if (viewid == ViewManger.ONE_SIDE_POPUPVIEW) {

                            }
                            return true;
                        }

                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                    }

                    return false;

                }
            } else {
                // if (LogManager.debug) {
                // Log.i("PopupMsgManager", "View_viewid is null");
                // }
            }

            return false;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return false;

    }

    public static int getIdentifier(String name, String defType) {
        return Constant.getContext().getResources()
                .getIdentifier(name, defType, Constant.getContext().getPackageName());
    }

    /**
     * Dimen intValue
     * 
     * @param ctx
     * @param dimenId
     * @return
     */
    public static int getIntDimen(Context ctx, int dimenId) {
        float f = ctx.getResources().getDimension(dimenId);
        return Math.round(f);
    }

    public static int getDouquAttrDimen(IViewAttr iAttr, int duoquAttrId) {
        Object f = iAttr.obtainStyledAttributes(IViewAttr.ATTR_TYPE_DIMEN, duoquAttrId);
        if (f != null) {
            return Math.round((Float) f);
        }
        return 0;
    }

    public static Object obtainStyledAttributes(TypedArray duoquAttr, byte styleType, int styleId) {
        Object obj = null;
        if (duoquAttr != null) {
            switch (styleType) {
            case IViewAttr.ATTR_TYPE_DIMEN:
                obj = duoquAttr.getDimension(styleId, -1);
                break;
            }
            duoquAttr.recycle();
        }
        return obj;
    }

    public static ArrayList<Integer> getViewPartList(String orgNo) throws Exception {

        ArrayList<Integer> res = new ArrayList<Integer>();
        int len = orgNo.length();
        int viewPartId;
        for (int i = 0; i < len; i += 3) {
            if (i + 3 > len)
                break;
            viewPartId = Integer.parseInt(orgNo.substring(i, i + 3));
            checkHasViewPartId(viewPartId);
            res.add(viewPartId);
        }
        return res;
    }

    private static void setPartViewParamRule(PartViewParam param, String paramStr) throws Exception {

        if (paramStr != null) {
            int len = paramStr.length();
            if (len > 0) {
                param.mNeedScroll = Integer.parseInt(paramStr.substring(0, 1)) == 1 ? true : false;
            }
            if (len > 1) {
                param.mAddImageMark = Integer.parseInt(paramStr.substring(1, 2)) == 1 ? true : false;
            }
            if (len > 3) {
                param.mBodyHeightType = Integer.parseInt(paramStr.substring(2, 4));
            }
            if (len > 5) {
                param.mBodyMaxHeightType = Integer.parseInt(paramStr.substring(4, 6));
            }
            if (len > 7) {
                param.mPaddingLeftType = Integer.parseInt(paramStr.substring(6, 8));
            }
            if (len > 9) {
                param.mPaddingTopType = Integer.parseInt(paramStr.substring(8, 10));
            }
            if (len > 11) {
                param.mPaddingRightType = Integer.parseInt(paramStr.substring(10, 12));
            }
            if (len > 13) {
                param.mPaddingBottomType = Integer.parseInt(paramStr.substring(12, 14));
            }
            if (len > 15) {
                param.mUiPartMarginTopType = Integer.parseInt(paramStr.substring(14, 16));
            }
        }

    }

    public static Map<String, PartViewParam> parseViewPartParam(String uiPartParam) throws Exception {
        if (uiPartParam == null)
            return null;
        Map<String, PartViewParam> res = null;
        String[] attr = uiPartParam.split(";");
        if (attr != null) {
            res = new HashMap<String, PartViewParam>();
            PartViewParam temp = null;
            String tempStr = null;
            String typeKey = null;
            for (String str : attr) {
                temp = new PartViewParam();
                int index = str.indexOf(",");
                if (index > 0) {// View rules
                    tempStr = str.substring(0, index);// View part
                    str = str.substring(index + 1);// View the rules section
                } else {
                    tempStr = str;
                    str = null;// Don't view the rules
                }
                typeKey = tempStr.substring(0, 1);
                if (PartViewParam.HEAD.equals(typeKey) || PartViewParam.FOOT.equals(typeKey)
                        || PartViewParam.BODY.equals(typeKey)) {
                    temp.mLayOutList = getViewPartList(tempStr.substring(1));
                    res.put(typeKey, temp);
                    // Processing view rules
                    setPartViewParamRule(temp, str);
                }
            }
        }
        return res;
    }

    /**
     * Create watermark mark
     * 
     * @param packAgeCtx
     * @return
     */
    public static View getDuoquImgMark(Context packAgeCtx) {
        return ViewManger.createContextByLayoutId(packAgeCtx, R.layout.duoqu_img_mark, null);
    }

    /**
     * Create time mark
     * 
     * @param packAgeCtx
     * @return
     */
    public static View getDuoquTimeMark(Context packAgeCtx) {
        return ViewManger.createContextByLayoutId(packAgeCtx, R.layout.duoqu_bottom_info, null);
    }

    public UIPart getUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        return part;
    }

    public static ScrollView createScrollView(final Context packAgeCtx, View root) {
        final ScrollView sView = (ScrollView) ViewManger.createContextByLayoutId(packAgeCtx,
                R.layout.duoqu_scroll_view, null);
        return sView;
    }

    public static ViewGroup createFrameViewGroup(Context packAgeCtx) {
        return (ViewGroup) ViewManger.createContextByLayoutId(packAgeCtx, R.layout.duoqu_frame_view, null);
    }

    public static RelativeLayout createRootView(Context packAgeCtx) {
        RelativeLayout rootView = new RelativeLayout(packAgeCtx);
        ViewGroup.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return rootView;
    }

    public static int setBodyViewPadding(Context context, View view, View childView, PartViewParam viewParam,
            int addPadding) {
        if (view == null || viewParam == null)
            return -1;
        int leftPadding = getBodyViewPadding(context, viewParam.mPaddingLeftType);
        int topPadding = getBodyViewPadding(context, viewParam.mPaddingTopType);
        int rightPadding = getBodyViewPadding(context, viewParam.mPaddingRightType);
        int bottomPadding = getBodyViewPadding(context, viewParam.mPaddingBottomType);

        if (leftPadding != 0 || topPadding != 0 || rightPadding != 0 || bottomPadding != 0) {
            view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        }

        return 1;
    }

    public static int getBodyViewPadding(Context context, int type) {
        int padding = 0;
        switch (type) {
        case 11:
            padding = TYPE_PADDING_11;
            break;
        default:
            break;
        }
        return padding;
    }

    public static int setBodyLayoutHeight(Context context, ViewGroup.LayoutParams lparam, int layoutHeightType,
            int sBodyPadding) {
        // if (layoutHeightType < 10)
        // return -1;
        int h = -1;
        switch (layoutHeightType) {
        case 11:
            h = TYPE_VIEW_HEIGHT_11;
            break;
        default:
            break;
        }
        if (h != -1) {
            lparam.height = h;
        }
        return h;
    }

    /**
     * Margin data access procedures for internal use
     * 
     * @param context
     * @param marginType
     * @return
     */
    public static int getInnerLayoutMargin(Context context, int marginType) {
        int margin = 0;
        switch (marginType) {
        case 111:
            margin = TYPE_SPLIT_LR_MARGIN_111;
            break;
        case 112:
            margin = TYPE_SPLIT_LR_MARGIN_112;
            break;
        default:
            break;
        }
        return margin;
    }

    public static int setLayoutMarginTop(Context context, ViewGroup.LayoutParams lparam, int marginTopType) {

        int marginTop = -1;
        if (lparam != null && lparam instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) lparam;

            switch (marginTopType) {
            case 11:
                marginTop = TYPE_MARGIN_11;
                break;
            default:
                break;
            }
            if (marginTop != -1 && lp != null) {
                lp.setMargins(lp.leftMargin, marginTop, lp.rightMargin, lp.bottomMargin);
            }
        }
        return marginTop;

    }

    public static void setViewTreeObserver(final View view, final XyCallBack callBack) {
        try {
            final ViewTreeObserver vto = view.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressLint("NewApi")
                public void onGlobalLayout() {
                    try {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    } catch (java.lang.NoSuchMethodError ex) {
                        try {
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } catch (java.lang.NoSuchMethodError e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("ViewManager setViewTreeObserver error:", e);
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("ViewManager setViewTreeObserver error:", e);
                        }
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("ViewManager setViewTreeObserver error:", e);
                    }
                    callBack.execute();
                }
            });
        } catch (Throwable e) {
            // e.printStackTrace();
        }
    }

    /**
     * Set the view of water ripple
     * 
     * @param view
     */
    public static void setRippleDrawable(View view) {
        try {

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    /**
     * Whether can display the watermark mark
     * 
     * @param msg
     * @return
     */
    public static boolean displayMarkImage(BusinessSmsMessage msg) {

        return true;
    }

    /**
     * Whether can display the time and the layout of the double card
     * 
     * @param msg
     * @return
     */
    public static boolean displayTime(BusinessSmsMessage msg) {

        return false;
    }

    public static int indexOfChild(View view, ViewGroup apView) {
        if (view == null || apView == null) {
            // android.util.Log.e("duoqu_xiaoyuan",
            // "indexOfChild view == null || apView == null");
            return -1;
        }
        int childCount = apView.getChildCount();
        View child = null;
        View tempChild = null;
        // android.util.Log.w("duoqu_xiaoyuan", "childCount : "+childCount);
        for (int i = 0; i < childCount; i++) {
            child = apView.getChildAt(i);
            if (child == view) {
                // android.util.Log.d("duoqu_xiaoyuan",
                // "indexOfChild child == views");
                return i;
            } else {
                tempChild = child
                        .findViewById(cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager.DUOQU_BUBBLE_VIEW_ID);
                if (tempChild == null) {
                    // android.util.Log.w("duoqu_xiaoyuan",
                    // "indexOfChild not find tempChild.");
                    continue;
                }
                if (tempChild == view) {
                    // android.util.Log.w("duoqu_xiaoyuan",
                    // " indexOfChild find tempChild tempChild == view");
                    return i;
                }
            }
        }
        // android.util.Log.e("duoqu_xiaoyuan",
        // "indexOfChild not find tempChild: -1");
        return -1;
    }

    /**
     * 是否含打开短信原文
     * 
     * @param message
     * @return
     */
    public static boolean isOpensmsEnable(BusinessSmsMessage message) {
        if (message != null) {
            String isOpensms_enable = (String) message.getValue("opensms_enable");
            if (!StringUtils.isNull(isOpensms_enable) && isOpensms_enable.equals("true"))
                return true;
        }
        return false;
    }

    public static boolean isDeadline(BusinessSmsMessage message) {
        try {
            if (message.getValue("deadline") != null) {
                long lastDepartLong = Long.parseLong(String.valueOf(message.getValue("deadline")));
                if (lastDepartLong == 0l)
                    return false;
                if (lastDepartLong < System.currentTimeMillis())
                    return true;
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("xiaoyuan ViewManager isDeadline error: " + e.getMessage(), e);
        }
        return false;
    }

    public static void setSelectFlg(BusinessSmsMessage smsMessage, boolean selectFlag) {
        try {
            smsMessage.bubbleJsonObj.put("isFirstSelect", true);
            ParseManager.updateMatchCacheManager(smsMessage);

            // String msg_num_md5 = MatchCacheManager.getMD5((String)
            // smsMessage.getValue("phoneNum"),
            // smsMessage.getMessageBody());
            // if (StringUtils.isNull(msg_num_md5)) {
            // return;
            // }
            //
            // synchronized (smsMessage.bubbleJsonObj) {
            // String temp = smsMessage.bubbleJsonObj.toString();
            // JSONObject newJSONObject = new JSONObject(temp);
            // MatchCacheManager.removeUselessKey(newJSONObject);
            // ContentValues matchCache = BaseManager.getContentValues(null,
            // "msg_num_md5", msg_num_md5, "phonenum",
            // StringUtils.getPhoneNumberNo86((String)
            // smsMessage.getValue("phoneNum")), "scene_id",
            // smsMessage.getTitleNo(), "msg_id",
            // String.valueOf(smsMessage.getSmsId()), "bubble_result",
            // newJSONObject.toString(), "save_time", System.currentTimeMillis()
            // + "", "bubble_lasttime",
            // System.currentTimeMillis() + "");
            // MatchCacheManager.insertOrUpdate(matchCache, 2);
            // }

        } catch (Throwable e) {
            // TODO Auto-generated catch block
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    public static boolean getSelectFlg(BusinessSmsMessage smsMessage) {
        try {
            return smsMessage.bubbleJsonObj.optBoolean("isFirstSelect", false);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return false;
    }

    static ViewManger mViewManager = null;

    public synchronized static ViewManger getViewManger() {
        try {
            if (mViewManager != null) {
                return mViewManager;
            }
            String clsNameImpl = "cn.com.xy.sms.sdk.ui.popu.util.ViewMangerImpl";
            Class cls = Class.forName(clsNameImpl);
            mViewManager = (ViewManger) cls.newInstance();
            return mViewManager;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }
    
    private static UiPartInterface mUiPartInterface = null;
    public final static  int UIPART_ACTION_SET_BG = 1;
    public final static  int UIPART_ACTION_SET_POP_MENU = 2;
    public final static  int UIPART_ACTION_SHOW_POP_MENU = 3;
    public final static  int UIPART_ACTION_SHOW_DIALOG = 4;
    public final static  int UIPART_ACTION_SET_BUTTON_TEXT_COLOR = 5;
    
    
    public synchronized static UiPartInterface getUiPartInterface(){
        try {
            if (mUiPartInterface != null) {
                return mUiPartInterface;
            }
            String clsNameImpl = "cn.com.xy.sms.sdk.ui.popu.util.UiPartAction";
            Class cls = Class.forName(clsNameImpl);
            mUiPartInterface = (UiPartInterface) cls.newInstance();
            return mUiPartInterface;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return null;
    }
    
    

    /* QIK-800 zhaoxiachao 20161013 start */
    public static final int OneSide_PopupView = 1;
    public static BasePopupView getView(BusinessReceiveSmsActivity context,
            XyCallBack callback, BusinessSmsMessage message, String titleNo) {
        BasePopupView basePopuView = null;
        try {
            int viewid = -1;
            String id = (String) message.getValue("View_viewid");
            if (!StringUtils.isNull(id)) {
                viewid = Integer.parseInt(id);
            }
            if (viewid == ViewManger.OneSide_PopupView) {
                basePopuView = new BasePopupView(context);
                basePopuView.init(context, message, callback);
            }
            return basePopuView;

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /* QIK-800 zhaoxiachao 20161013 end */
}
