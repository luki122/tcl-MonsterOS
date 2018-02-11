package cn.com.xy.sms.sdk.ui.popu.web;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.config.UIConfig;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;

public class SdkWebActivity extends Activity implements IActivityParamForJS {

    public static String endUrl = "";
    private CommonWebView mWebView = null;
    private SdkWebJavaScript sdkJs;
    private RelativeLayout mWebViewLy = null;
    private TextView mTitleNameView = null;
    private LinearLayout mHeadBackView = null;
    private LinearLayout mMenuView = null;
    // private RelativeLayout mDuoquBar;
    private ProgressBar mDuoquProgressBar;
    private JSONObject mJsObj = null;
    private Context mContext = null;
    private RelativeLayout mErrorPage = null;
    private RelativeLayout mNetworkSetting = null;
    private String mDuoquText = "";

    private String mChannelId = "";
    private String mSdkVersion = "";
    private String mActionType = "";
    private JSONArray mMenuDataArr = null;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.duoqu_sdk_web_main);
        // ActionBar actionBar = getActionBar();
        // actionBar.setDisplayShowCustomEnabled(true);
        // actionBar.setDisplayShowHomeEnabled(false);
        // actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        // LayoutInflater inflater = getLayoutInflater();
        // RelativeLayout actionBarLayout =
        // (RelativeLayout)inflater.inflate(R.layout.duoqu_web_action_bar,
        // null);
        // actionBar.setCustomView(actionBarLayout);

        mContext = this;

        mWebViewLy = (RelativeLayout) findViewById(R.id.duoqu_webview);
        mWebView = new CommonWebView(this);

        mWebViewLy.addView(mWebView);
        mWebView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mTitleNameView = (TextView) findViewById(R.id.duoqu_title_name);

        mHeadBackView = (LinearLayout) findViewById(R.id.duoqu_header_back);

        mMenuView = (LinearLayout) findViewById(R.id.duoqu_header_menu);

        mErrorPage = (RelativeLayout) findViewById(R.id.duoqu_error_page);
        mNetworkSetting = (RelativeLayout) findViewById(R.id.duoqu_network_setting);
        // mDuoquBar = (RelativeLayout) findViewById(R.id.duoqu_progressbar);
        mDuoquProgressBar = (ProgressBar) findViewById(R.id.duoqu_progressbar);

        mDuoquText = getResources().getString(R.string.duoqu_tip_duoqu_name);

        // When version greater than 4.4 enable immersive status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setImmersion();
        }

        initParams();
        initWebView();
        loadWebViewUrl();
        initListener();
        /* SDK-425 lilong 20160510 start */
        initMenuView();
        /* SDK-425 lilong 20160510 end */
    }

    @Override
    protected void onDestroy() {

        // TODO Auto-generated method stub
        super.onDestroy();
        /* RM-356 zhengxiaobo 20160506 begin */
        sendBroadcast();
        /* RM-356 zhengxiaobo 20160506 end */
        mWebViewLy.removeAllViews();
        mWebView.destroy();
        //
        // //如果该Activity实例是当前进程内最后一个实例，则结束进程
        // if(mCreateCount < 1){
        // System.exit(0);
        // }
    }

    /**
     * Immersive setting
     */
    private void setImmersion() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    /**
     * set title from menu name
     * 
     * @param title
     * @param menuName
     */
    public void setTitle(String title, String menuName) {
        if (StringUtils.isNull(menuName)) {
            SdkWebActivity.this.setTitle(title);
            mTitleNameView.setText(title);
        } else {
            SdkWebActivity.this.setTitle(menuName);
            mTitleNameView.setText(menuName);
        }
    }

    /**
     * init some params from sdk
     */
    void initParams() {
        try {
            KeyManager.initAppKey();
            Constant.initContext(this);
            mChannelId = KeyManager.getAppKey();
            mSdkVersion = ParseManager.getSdkVersion();
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
        }
    }

    /**
     * OnclickListener method
     */
    void initListener() {

        mMenuView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MenuWindow mLifeHallWindow = new MenuWindow(SdkWebActivity.this, getWebView(), mMenuDataArr,
                        new OnClickListener() {
                    public void onClick(View v) {
                    }
                });
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int yOffset = frame.top + SdkWebActivity.this.findViewById(R.id.duoqu_header).getHeight();
                int xOffset = ViewUtil.dp2px(SdkWebActivity.this, 11);
                // mLifeHallWindow.showAsDropDown(SdkWebActivity.this.findViewById(R.id.duoqu_header));
                mLifeHallWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.v_stroke_1));
                mLifeHallWindow.showAtLocation(SdkWebActivity.this.findViewById(R.id.duoqu_webview),
                        Gravity.TOP | Gravity.RIGHT, xOffset, yOffset);
            }
        });
        mHeadBackView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        mNetworkSetting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = null;
                try {

                    String sdkVersion = android.os.Build.VERSION.SDK;
                    if (Integer.valueOf(sdkVersion) > 10) {
                        intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    } else {
                        intent = new Intent();
                        ComponentName comp = new ComponentName("com.android.settings",
                                "com.android.settings.WirelessSettings");
                        intent.setComponent(comp);
                        intent.setAction("android.intent.action.VIEW");
                    }
                    SdkWebActivity.this.startActivity(intent);
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
                }
            }
        });

    }

    /* RM-356 zhengxiaobo 20160506 begin */
    public void sendBroadcast() {
        if (mJsObj == null) {
            String jsonData = getIntent().getStringExtra("JSONDATA");
            try {
                mJsObj = new JSONObject(jsonData);
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
            }
        }
        Intent intent = new Intent();
        if (mJsObj != null) {
            intent.putExtra("JSONDATA", mJsObj.toString());
        }

        String type = mJsObj.optString("type", "");
        String resverFilag = "";
        if (type.equals("WEB_QUERY_EXPRESS_FLOW")) {
            resverFilag = "cn.com.xy.sms.ExpressStatusReceiver";
            /* HUAWEI-1324/zhegnxiaobo 2016.07.07 start */
        } else if ("WEB_QUERY_FLIGHT_TREND".equals(type)) {
            resverFilag = "cn.com.xy.sms.FlightStateQueryReceiver";
            /* HUAWEI-1324/zhegnxiaobo 2016.07.07 end */
        } else {
            resverFilag = "cn.com.xy.sms.TrianStationSelectedReceiver";
        }
        intent.setAction(resverFilag);
        /* HUAWEI-1293/kedeyuan 2016.07.06 end */
        SdkWebActivity.this.sendBroadcast(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void initData() {
        try {
            if (mJsObj == null) {
                String jsonData = getIntent().getStringExtra("JSONDATA");
                mJsObj = new JSONObject(jsonData);
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }
    }

    /**
     * Load the URL
     */
    void loadWebViewUrl() {
        initData();
        String aType = super.getIntent().getStringExtra("actionType");
        mActionType = getParamData("type");
        String pageViewUrl = null;
        sdkJs.queryJson(mActionType, JsonUtil.jsonObjectToString(mJsObj), true);
        if (aType != null && "WEB_URL".equals(aType)) {
            pageViewUrl = getParamData("url");
        } else {
            String host = getParamData("HOST");

            if (StringUtils.isNull(host)) {
                host = NetWebUtil.WEB_SERVER_URL;
            }
            pageViewUrl = getParamData("PAGEVIEW");
            if (StringUtils.isNull(pageViewUrl)) {

                if ("WEB_MAP_SITE".equals(mActionType)) {
                    String address = getParamData("address");
                    pageViewUrl = "http://api.map.baidu.com/geocoder?address=" + address + "&output=html&src=xiaoyuan|"
                            + mDuoquText;

                    if (!StringUtils.isNull(address)) {
                        mDuoquProgressBar.setVisibility(View.GONE);
                        mWebView.loadUrl(pageViewUrl);
                    } else {
                        errorPage();
                    }
                    return;
                } else {
                    pageViewUrl = "h5service?action_type=" + mActionType + "&xy_channel=" + mChannelId + "&xy_sdkver="
                            + mSdkVersion;
                }
            }
            if (!StringUtils.isNull(pageViewUrl)) {
                pageViewUrl = host + "/" + pageViewUrl;
            }
        }
        if (!StringUtils.isNull(pageViewUrl)) {
            int isNetWork = XyUtil.checkNetWork(mContext);
            if (isNetWork == 0 || isNetWork == 1) {
                Map<String, String> header = getHttpRequsetHeader(pageViewUrl);
                //header.put("xy-url", pageViewUrl);
                /* SERVER-62 xusongzhou 20160413 start */
                if(header == null){
                    header = new HashMap<String, String>();
                }
                // header.put("xy-url", pageViewUrl);
                /* SERVER-62 xusongzhou 20160413 start */
                header.put("xy-channel", mChannelId);
                header.put("xy-sdk-ver", mSdkVersion);
                header.put("xy-req-time", String.valueOf(new Date().getTime()));
                header.put("xy-x", DuoquUtils.getXid());
                header.put("xy-p", DuoquUtils.getPid());
                if (getParamData("menuName") != null || "".equals(getParamData("menuName"))) {
                    try {
                        String encodeMenuName = URLEncoder.encode(getParamData("menuName"), "utf-8");
                        header.put("xy-menu-name", encodeMenuName);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
                    }
                }
                if (getParamData("publicId") != null || "".equals(getParamData("publicId"))) {
                    try {
                        String encodePublicId = URLEncoder.encode(getParamData("publicId"), "utf-8");
                        header.put("xy-public-id", encodePublicId);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
                    }
                }
                mWebView.loadUrl(pageViewUrl, header);
                /* SERVER-62 xusongzhou 20160413 end */
            } else {
                errorPage();
                endUrl = pageViewUrl;
            }

        } else {
            errorPage();
        }
    }

    /**
     * Error page
     */
    void errorPage() {
        mDuoquProgressBar.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mTitleNameView.setText(R.string.duoqu_web_not_find_page);
        mErrorPage.setVisibility(View.VISIBLE);
    }
    
    
    


    private static final String [] INNER_HTTP_URL = {"bizport.cn","duoqu.in","microfountain.com","mfexcel.com"};
    
    public static Map<String, String> getHttpRequsetHeader(String url){
        if(url == null || INNER_HTTP_URL == null || INNER_HTTP_URL.length <= 0){
            return null;
        }
        
        boolean isInnerUrl = false;
        for(String s:INNER_HTTP_URL){
            if(url.contains(s)){
                isInnerUrl = true;
                break;
            }
        }
        
        if(!isInnerUrl){
            return null;
        }
        Map<String, String> header = new HashMap<String, String>();
        header.put("XY_CK", KeyManager.getAppKey());
        header.put("XY_XID", DuoquUtils.getXid());
        header.put("XY_MID", DuoquUtils.getPid());
        header.put("XY_SV", ParseManager.getSdkVersion());
        header.put("XY_UV", ParseManager.getUIVersion());
        header.put("XY_AI", DuoquUtils.getAI());
        header.put("XY_NI", DuoquUtils.getNI());
        return header;
    }


    /**
     * Initialize webview
     */
    @SuppressLint("SetJavaScriptEnabled")
    void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDomStorageEnabled(true); // open DOM storage

        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);// support h5 view port
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setTextSize(TextSize.NORMAL);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 设置
                                                                        // 缓存模式
        mWebView.getSettings().setRenderPriority(RenderPriority.HIGH); // 提高渲染优先级
        mWebView.getSettings().setBlockNetworkImage(true);// 图片放在最后加载

        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();

        mWebView.getSettings().setGeolocationDatabasePath(dir);

        mWebView.getSettings().setGeolocationEnabled(true);

        // API
        // function
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                    long contentLength) {
                // TODO Auto-generated method stub
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }

        });
        mWebView.setWebViewClient(new CommonWebViewClientEx() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            // Response to a hyperlink
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Block identification number for telephone connection
                if (url != null && !url.toLowerCase(Locale.getDefault()).startsWith("http")) {
                    try {
                        if (url.indexOf("tel:") >= 0) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                        }
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
                    }
                    return true;
                }
                // view.loadUrl(url);
                return false;
            }

            // end loading
            @Override
            public void onPageFinished(WebView view, String url) {

                String menuName = getParamData("menuName");
                String title = view.getTitle();
                setTitle(title, menuName);
                
                mWebView.getSettings().setBlockNetworkImage(false);
                if(view != null && !StringUtils.isNull(view.getUrl())){
                    /* notify webView finish loaded */
                    mIsWebViewLoaded = true;
                    sendConversationDataChange();
                    filterMenuWebLoaded();
                }
                super.onPageFinished(view, url);
            }

            // start loading
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                mErrorPage.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            // Handle the abnormal case
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                if (errorCode != -10) {
                    view.stopLoading();
                    view.clearView();
                    errorPage();
                    endUrl = failingUrl;
                }
            }
        });

        mWebView.setWebChromeClient(new CommonWebChromeClientEx() {
            @Override
            @Deprecated
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                // TODO Auto-generated method stub
                super.onConsoleMessage(message, lineNumber, sourceID);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
                callback.invoke(origin, true, false);
                // TODO Auto-generated method stub
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                return true;
            }

            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                Builder builder = new Builder(SdkWebActivity.this);
                builder.setTitle("Alert");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                builder.setCancelable(false);
                builder.create();
                builder.show();
                return true;
            };

            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                Builder builder = new Builder(SdkWebActivity.this);
                builder.setTitle("confirm");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                builder.setCancelable(false);
                builder.create();
                builder.show();
                return true;
            };

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                // if(newProgress < 90){
                // if(mDuoquBar.getVisibility() != View.VISIBLE
                // ||mWebViewLy.getVisibility() != View.GONE){
                // mDuoquBar.setVisibility(View.VISIBLE);
                // mWebViewLy.setVisibility(View.GONE);
                // }
                // }else{
                // if(mDuoquBar.getVisibility() != View.GONE
                // ||mWebViewLy.getVisibility() != View.VISIBLE){
                // mDuoquBar.setVisibility(View.GONE);
                // mWebViewLy.setVisibility(View.VISIBLE);
                // }
                // }
                if (newProgress == 100) {
                    mDuoquProgressBar.setVisibility(View.GONE);
                    sdkJs.postData(true);
                } else {
                    if (View.GONE == mDuoquProgressBar.getVisibility()) {
                        mDuoquProgressBar.setVisibility(View.VISIBLE);
                    }
                    mDuoquProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }

            /**
             * set web view title
             */
            public void onReceivedTitle(WebView view, String title) {
                String menuName = getParamData("menuName");
                setTitle(title, menuName);
                super.onReceivedTitle(view, title);
            }

        });

        sdkJs = new SdkWebJavaScript(SdkWebActivity.this);
        mWebView.addJavascriptInterface(sdkJs, "injs");
    }

    @Override
    public WebView getWebView() {
        return mWebView;
    }

    @Override
    public String getParamData(String key) {
        String res = null;
        if (key != null) {
            try {
                if (mJsObj == null) {
                    String jsonData = getIntent().getStringExtra("JSONDATA");
                    mJsObj = new JSONObject(jsonData);
                }
                if (mJsObj != null && mJsObj.has(key)) {
                    res = mJsObj.getString(key);
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
            }
        }
        if (res == null)
            res = "";
        return res;
    }

    /* RM-356 zhengxiaobo 20160506 begin */
    public void setParamData(String key, String value) {
        if (StringUtils.isNull(key))
            return;
        try {
            if (mJsObj == null) {
                String jsonData = getIntent().getStringExtra("JSONDATA");
                mJsObj = new JSONObject(jsonData);
            }
            if (mJsObj != null) {
                mJsObj.put(key, value);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity error ", e);
        }
    }

    /* RM-356 zhengxiaobo 20160506 end */
    @Override
    public Activity getActivity() {
        return SdkWebActivity.this;
    }

    @Override
    public int checkOrientation() {
        int currentOrientation = mContext.getResources().getConfiguration().orientation;
        return currentOrientation;
    }

    /* SDK-425 lilong 20160510 start */
    public boolean getOnOffByType(int type) {
        return ParseManager.geOnOffByType(type);
    }

    public void initMenuView() {
        if (!getOnOffByType(MenuWindow.CONFIG_WEB_TOP_MENU)) {
            mMenuView.setVisibility(View.INVISIBLE);
            return;
        }

        mMenuDataArr = ParseManager.getConfigByType(MenuWindow.CONFIG_WEB_TOP_MENU, UIConfig.UIVERSION,
                UIConfig.SUPORT_STATE);
        if (mMenuDataArr == null || mMenuDataArr.length() == 0) {
            mMenuDataArr = UIConfig.getDefaultSuportMenuData();
        }

        mMenuDataArr = filterMenu(mMenuDataArr);
        if (mMenuDataArr != null && mMenuDataArr.length() > 0) {
            mMenuView.setVisibility(View.VISIBLE);
        } else {
            mMenuView.setVisibility(View.INVISIBLE);
        }
    }

    private JSONArray filterMenu(JSONArray menuDataArr) {
        if (TextUtils.isEmpty(mActionType) || menuDataArr == null) {
            return menuDataArr;
        }

        JSONArray newMenu = new JSONArray();
        for (int i = 0; i < menuDataArr.length(); i++) {
            JSONObject menuData = menuDataArr.optJSONObject(i);
            if (menuData != null && !mActionType.equals(menuData.opt("type"))) {
                newMenu.put(menuData);
            }
        }

        return newMenu;
    }
    /* SDK-425 lilong 20160510 start */

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return mActionType;
    }

    /* QIK-592 wangxingjian 20160727 begin */
    public static final int MSG_TYPE_CONVERSATION = 1;

    private JSONObject mConversation = null;
    private boolean mIsWebViewLoaded = false;
    private boolean mIsCallBackData = false;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TYPE_CONVERSATION:
                doConversationStateChange();
                break;
            default:
            }
        }
    };

    private void doConversationStateChange() {
        if (!mIsCallBackData && mIsWebViewLoaded && mConversation != null && mWebView != null) {
            mIsCallBackData = true;
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(
                            "javascript:" + "callBackData" + "('" + JsonUtil.jsonObjectToString(mConversation) + "')");
                }
            });
        }

    }

    @Override
    public void setData(int type, Object data) {
        switch (type) {
        case SdkWebJavaScript.DATA_TYPE_CONVERSATION:
            mConversation = (JSONObject) data;
            sendConversationDataChange();
            break;
        default:
        }
    }

    private void sendConversationDataChange() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_TYPE_CONVERSATION);
            mHandler.obtainMessage(MSG_TYPE_CONVERSATION).sendToTarget();
        }
    }
    /* QIK-592 wangxingjian 20160727 end */

    private void filterMenuWebLoaded() {
        String webType = getParamData("web_url_type");
        if (mMenuDataArr == null || TextUtils.isEmpty(webType)) {
            return ;
        } 

        JSONArray newMenu = new JSONArray();
        for (int i = 0; i < mMenuDataArr.length(); i++) {
            JSONObject menuData = mMenuDataArr.optJSONObject(i);
            if (menuData != null && !webType.equals(menuData.opt("type"))) {
                newMenu.put(menuData);
            }
        }

        mMenuDataArr = newMenu;
    }
}
