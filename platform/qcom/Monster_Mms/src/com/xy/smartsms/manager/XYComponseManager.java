package com.xy.smartsms.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.menu.PopMenus;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.util.XySdkUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

import com.android.mms.R;
import com.xy.smartsms.iface.IXYSmartSmsHolder;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
//xiaoyuan add in 2016-10-27 begin
import android.widget.ImageView;
//xiaoyuan add in 2016-10-27 end


public class XYComponseManager {
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_EDIT_LAYOUT = 1;
    public static final short SMART_SMS_DUOQU_EVENT_HIDE_EDIT_LAYOUT = 4;
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_VIEW_MENU = 5;

    private static final ExecutorService mDataInitPool = Executors.newFixedThreadPool(10);

    private final String TAG = "XIAOYUAN";
    private static final boolean DEBUG = true;

    private static final String SECONDMENU = "secondmenu";
    private static final String ACTION_DATA = "action_data";
    private ViewGroup mXYMenuContent;
    private View mButtonToEditMenu;
    private View mEditRootLayout=null;
    private PopMenus mPopupWindowCustommenu;
    private View mButtonToXYMenu;
    //begin tangyisen
    private View mAddAttachmentButton;
    //end tangyisen
    private IXYSmartSmsHolder mXYSmsHolder;
    private LayoutInflater mLayoutInflater = null;
    private View mXYMenuRootLayout = null;
    private String mNumber = null;
    private Activity mCtx;
    private boolean mIsNotifyComposeMessage = true;

    /**
     * get SmartSmsMenu root-view
     */
    public View getMenuRootView() {
        return mXYMenuRootLayout;
    }

    public XYComponseManager(IXYSmartSmsHolder xiaoYuanSmsHolder) {
        this.mXYSmsHolder = xiaoYuanSmsHolder;
        mCtx = xiaoYuanSmsHolder.getActivityContext();
    }

    /**
     * query Menu data by recipientNumber
     */
    public void loadMenu(IXYSmartSmsHolder xiaoYuanSmsHolder, String recipientNumber) {
        if(DEBUG) Log.d(TAG, "loadMenu(), recipientNumber = "+recipientNumber);
        if (recipientNumber == null) {
            Log.w(TAG, XYComponseManager.class.getName()
                    + "  queryMenu recipientNumber is null");
            return;
        }
        if (mCtx == null) {
            Log.w(TAG, "loadMenu(), mCtx is null, return");
            return;
        }

        /* UIX-148 lianghailun 20160506 start */
        XySdkUtil.setBubbleActivityResumePhoneNum(mCtx.hashCode(), recipientNumber);
        /* UIX-148 lianghailun 20160506 end */
        if (mNumber == null || !mNumber.equals(recipientNumber)) {
            mNumber = recipientNumber;
            dataInit(recipientNumber, mCtx, xiaoYuanSmsHolder);
        }
    }

    //lichao modify showMenu to showXYMenu in 2016-10-28
    public void showXYMenu() {
        Log.d(TAG, "showXYMenu()");
        isShowingXYMenu = true;//lichao add
        if(null != mXYMenuRootLayout){
            mXYMenuRootLayout.setVisibility(View.VISIBLE);
        }
        if(null != mButtonToEditMenu){
            mButtonToEditMenu.setVisibility(View.VISIBLE);
        }
        //same as mBottomPanel in ComposeMessageActivity
        if(null != mEditRootLayout){
            mEditRootLayout.setVisibility(View.GONE);
        }
        if(null != mAddAttachmentButton){
            //can't send mms to a public number
            mAddAttachmentButton.setVisibility(View.GONE);
        }
    }

    //lichao modify hideMenu to hideXYMenu in 2016-10-28
    public void hideXYMenu() {
        Log.d(TAG, "hideXYMenu()");
        isShowingXYMenu = false;//lichao add
        if(null != mXYMenuRootLayout){
            mXYMenuRootLayout.setVisibility(View.GONE);
        }
        if(null != mButtonToEditMenu){
            mButtonToEditMenu.setVisibility(View.GONE);
        }
    }

    public void hideXYMenuAndShowEditRoot() {
        hideXYMenu();
        //same as mBottomPanel in ComposeMessageActivity
        if(null != mEditRootLayout){
            mEditRootLayout.setVisibility(View.VISIBLE);
        }
    }

    public void hideXYMenuAndShowAttachmentButton() {
        hideXYMenuAndShowEditRoot();
        setAttachmentButtonVisible(true);
        setButtonToXYMenuVisible(false);
    }

    public void hideXYMenuAndShowButtonToXYMenu() {
        hideXYMenuAndShowEditRoot();
        setAttachmentButtonVisible(false);
        setButtonToXYMenuVisible(true);
    }

    public void setAttachmentButtonVisible(boolean visible) {
        if (mAddAttachmentButton != null) {
            mAddAttachmentButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setButtonToXYMenuVisible(boolean visible) {
        if (mButtonToXYMenu != null) {
            mButtonToXYMenu.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    //lichao add begin
    private boolean isShowingXYMenu = false;
    public boolean isShowingXYMenu() {
        return isShowingXYMenu;
    }
    //lichao add end

    public boolean getIsNotifyComposeMessage() {
        return mIsNotifyComposeMessage;
    }

    private void initMenu() {
        if (mXYSmsHolder == null) {
            Log.w(TAG, XYComponseManager.class.getName()+ "  initSmartSmsMenuManager iSmartSmsUIHolder is null");
            return;
        }
        if (mCtx == null) {
            Log.w(TAG,
                    XYComponseManager.class.getName()+ "  initSmartSmsMenuManager iSmartSmsUIHolder.getActivityContext() is null");
            return;
        }
        mLayoutInflater = (LayoutInflater) mCtx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initBottomMenuView(mCtx, mXYSmsHolder);

    }

    /**
     * initialization SmartSmsMenu all views
     */
    private void initBottomMenuView(Activity ctx,
            final IXYSmartSmsHolder xiaoYuanSmsHolder) {

        if (mXYMenuRootLayout == null) {
            ViewStub mMenuRootStub = (ViewStub) xiaoYuanSmsHolder.findViewById(R.id.duoqu_menu_layout_stub);
            if (mMenuRootStub == null) {
                Log.w(TAG, XYComponseManager.class.getName()+ " initBottomMenu menuRootStub is null.");
                return;
            }
            mXYMenuRootLayout = mMenuRootStub.inflate();
        }
        if (mXYMenuRootLayout == null) {
            return;
        }
        mButtonToXYMenu =  xiaoYuanSmsHolder.findViewById(R.id.duoqu_button_menu);
        mButtonToXYMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showXYMenu();
            }
        });
        //begin tangyisen
        mAddAttachmentButton = xiaoYuanSmsHolder.findViewById(R.id.add_attachment_first);
        //end tangyisen
        mEditRootLayout = xiaoYuanSmsHolder.findViewById(R.id.bottom_panel);
        mXYMenuContent = (LinearLayout) mXYMenuRootLayout.findViewById(R.id.layout_menu);

        mButtonToEditMenu = (LinearLayout) mXYMenuRootLayout.findViewById(R.id.layout_exchange);
        mButtonToEditMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideXYMenuAndShowButtonToXYMenu();
            }
        });
    }

    private void dataInit(final String recipientNumber, final Activity ctx,
            final IXYSmartSmsHolder xiaoYuanSmsHolder) {
        if(DEBUG) Log.d(TAG, "dataInit()");
        if (StringUtils.isNull(recipientNumber)) {
            return;
        }
        if(DEBUG) Log.d(TAG, "dataInit() >>>loadBubbleData()");
        loadBubbleData(recipientNumber);

        mDataInitPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String menuJsonData = getMenuJsonData(recipientNumber);
                    if(DEBUG) Log.d(TAG, "dataInit(), menuJsonData = "+menuJsonData);
                    mCtx.runOnUiThread(new Runnable() {
                        public void run() {
                            if (StringUtils.isNull(menuJsonData)) {
                                hideXYMenuAndShowAttachmentButton();
                                if(DEBUG) Log.d(TAG, "dataInit(), menuJsonData is null, return");
                                return;
                            }
                            try {
                                /**
                                 * json is not empty, show the SmartSmsMenu
                                 */
                                initMenu();
                                if (null == mXYMenuRootLayout
                                        || null == mButtonToXYMenu
                                        || null == mXYMenuContent
                                        || null == mLayoutInflater) {
                                    Log.d(TAG, "mXYMenuRootLayout is null, return");
                                    return;
                                }
                                bindMenuView(new JSONArray(menuJsonData), ctx);
                                showXYMenu();
                            } catch (Exception e) {
                                Log.w(TAG,
                                        "SmartSmsMenuManager queryMenu execute error: "
                                                + e.getMessage());
                            }
                        }
                    });
                } catch (Throwable ex) {
                    Log.w(TAG, "queryMenu error: " + ex.getMessage());
                }
            }
        });
    }

    private String getMenuJsonData(String recipientNumber) {
        try {
            String formatNumber = StringUtils.getPhoneNumberNo86(recipientNumber);
            if(DEBUG) Log.d(TAG, "getMenuJsonData(), formatNumber = "+formatNumber);
            String menuJsonData = ParseManager.queryMenuByPhoneNum(mCtx, formatNumber, 1, null, null);
            if (mCtx == null || mCtx.isFinishing()) {
                return null;
            }
            return menuJsonData;
        } catch (Throwable ex) {
            Log.w(TAG, "getMenuJsonData error: " + ex.getMessage());
        }
        return null;
    }

    private void beforeInitBubbleView() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCtx == null || mCtx.isFinishing()) {
                        return;
                    }
                    DuoquBubbleViewManager.beforeInitBubbleView(mCtx, mNumber);
                } catch (Throwable e) {
                    Log.w(TAG, "beforeInitBubbleView error: " + e.getMessage());
                }
            }
        }, 2000);
    }

    /**
     * load bubble data by recipientNumber
     * 
     * @param recipientNumber
     */
    private void loadBubbleData(final String recipientNumber) {
        Log.d(TAG, "loadBubbleData()");
        if (mCtx == null) {
            return;
        }
        mDataInitPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    ParseBubbleManager.loadBubbleDataByPhoneNum(recipientNumber, true);
                    mIsNotifyComposeMessage = ParseManager.isEnterpriseSms(mCtx, recipientNumber, null, null);
                    //TCL lichao delete in 2016-10-14
                    //beforeInitBubbleView();
                } catch (Throwable e) {
                    Log.w(TAG, "loadBubbleData error: " + e.getMessage());
                }
            }
        });
    }

    private static int mIsNotShow = -1;
    public static int mSMenuFlag = -1;
    private void bindMenuView(JSONArray jsonCustomMenu, final Activity ctx)
            throws JSONException {
        if (jsonCustomMenu != null && jsonCustomMenu.length() > 0) {
            mXYMenuContent.removeAllViews();
            JSONArray btnJson = jsonCustomMenu;
            Drawable drawable = mXYMenuContent.getResources().getDrawable(R.drawable.duoqu_menu_item_logo);
            int drawableParam = (int)ViewUtil.getDimension(R.dimen.duoqu_menu_logo_param);
            drawable.setBounds(0,0,drawableParam,drawableParam);
            for (int i = 0; i < btnJson.length(); i++) {
                final int menuIndex = i;
                final JSONObject ob = btnJson.getJSONObject(i);
                final LinearLayout layout = (LinearLayout) mLayoutInflater
                        .inflate(R.layout.duoqu_item_custommenu, null);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                        1.0f);
                layout.setLayoutParams(lp);
                TextView tvCustommenuName = (TextView) layout
                        .findViewById(R.id.duoqu_custommenu_name);
                tvCustommenuName.setText(ob.getString("name"));
                //xiaoyuan modify in 2016-10-27 begin
                ImageView tvCustommenuImage = (ImageView) layout.findViewById(R.id.douqu_iv_menu_bounce);
                tvCustommenuImage.setVisibility(View.INVISIBLE);
                if (ob.has(SECONDMENU)
                        && ob.getJSONArray(SECONDMENU).length() > 0) {
                    //tvCustommenuName.setCompoundDrawables(drawable, null, null,null);
                    tvCustommenuImage.setVisibility(View.VISIBLE);
                }
                //xiaoyuan modify in 2016-10-27 end
                layout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //lichao & weijie modify in 2016-11-02 begin
                        Object obj = layout.getTag();
                        if(obj != null && mPopupWindowCustommenu != null){
                            if(mPopupWindowCustommenu == (PopMenus)obj){
                                mPopupWindowCustommenu = null;
                                return;
                            }
                        }
                        try {
                            if (ob.has(SECONDMENU)) {
                                if (ob.getJSONArray(SECONDMENU).length() == 0) {
                                    Map<String, String> extend = new HashMap<String, String>();
                                    ParseManager.doAction(ctx, ob.get(ACTION_DATA).toString(), extend);
                                } else {
                                    final SdkCallBack onClickCallBack = new SdkCallBack() {
                                        @Override
                                        public void execute(Object... backData) {
                                            try {
                                                if (ob.has(SECONDMENU) && ob.getJSONArray(SECONDMENU).length() > 0) {
                                                    // dismiss before show the
                                                    // new one
                                                    if (mSMenuFlag != menuIndex) {
                                                        mPopupWindowCustommenu = new PopMenus(ctx, ob.getJSONArray(SECONDMENU), 0, 0);
                                                        mPopupWindowCustommenu.showAtLocation(layout);
                                                        layout.setTag(mPopupWindowCustommenu);
                                                    }else{
                                                        mPopupWindowCustommenu.dismiss();
                                                        mSMenuFlag = mIsNotShow;
                                                    }
                                                } else {
                                                    Map<String, String> extend = new HashMap<String, String>();
                                                    ParseManager.doAction(ctx, ob.get(ACTION_DATA).toString(), extend);
                                                }
                                            } catch (Exception e) {

                                            }
                                        }
                                    };
                                    onClickCallBack.execute();
                                }
                            } else {
                                Map<String, String> extend = new HashMap<String, String>();
                                ParseManager.doAction(ctx, ob.get(ACTION_DATA).toString(), extend);
                            }
                            //lichao & weijie modify in 2016-11-02 end
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }

                });
                mXYMenuContent.addView(layout);
            }
        } else {
            mXYMenuRootLayout.setVisibility(View.GONE);
        }
    }
}
