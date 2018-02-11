package cn.com.xy.sms.sdk.ui.popu.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.config.UIConfig;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;

public class MenuWindow extends PopupWindow {

    private View mMenuView;
    public static final int CONFIG_WEB_TOP_MENU = 2;
    private JSONArray mJsonArray;
    private Activity mContext;
    private LinearLayout mMenuItemView;

    public MenuWindow(final Activity context, final WebView webView, JSONArray menuData, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.duoqu_popup_menu, null);
        mMenuItemView = (LinearLayout) mMenuView.findViewById(R.id.pop_layout);
        mContext = context;
        try {
            if(menuData != null){
                mJsonArray = menuData;
            }else{
            /*UIX-161/yangzhi/2016.05.19---start---*/
            mJsonArray = ParseManager.getConfigByType(CONFIG_WEB_TOP_MENU,
                    UIConfig.UIVERSION, UIConfig.SUPORT_STATE);
            if (mJsonArray == null || mJsonArray.length() == 0) {
                mJsonArray = UIConfig.getDefaultSuportMenuData();
            }
            /*UIX-161/yangzhi/2016.05.19---end---*/
            }
            setSubMenu();
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
            SmartSmsSdkUtil.smartSdkExceptionLog("MenuWindow  error:", e1);
        }

        this.setContentView(mMenuView);
        this.setWidth(LayoutParams.WRAP_CONTENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(android.R.style.Animation_Dialog);
        ColorDrawable dw = new ColorDrawable(0000000000);
        this.setBackgroundDrawable(dw);
        mMenuView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        cleanData();
                        dismiss();
                    }
                }
                return true;
            }
        });

    }
    
    /*SDK-425 lilong 20160510 start*/
    void setSubMenu() throws JSONException {
        if (mJsonArray != null && mJsonArray.length() > 0) {
            for (int i = 0; i < mJsonArray.length(); i++) {
                final JSONObject ob = mJsonArray.getJSONObject(i);
                LinearLayout layoutItem = (LinearLayout) ((LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.duoqu_menu_item, null);
                layoutItem.setFocusable(true);
                TextView tv_funbtntitle = (TextView) layoutItem.findViewById(R.id.duoqu_menu_item_textView);
                tv_funbtntitle.setText(ob.getString("name"));
                layoutItem.setOnClickListener(new OnClickListener() {   
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            doActionContext(ob);
                            MenuWindow.this.dismiss();
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("MenuWindow  error:", e);
                        }
                    }
                });
                mMenuItemView.addView(layoutItem);
            }
        }
    }
    
    /*UIX-161/yangzhi/2016.05.19---start---*/
    private void doActionContext(JSONObject jsobj) throws JSONException{
        if(!TextUtils.isEmpty(jsobj.optString("web_menu_type"))){
            menuDoActionContext(jsobj);
        }else{
            String actionData = jsobj.get("action_data").toString();
            DuoquUtils.doActionContext(mContext, actionData, null);
        }
    }
    /*UIX-161/yangzhi/2016.05.19---end---*/
    
    /*UIX-161/yangzhi/2016.05.19---start---*/
    private void menuDoActionContext(JSONObject jsobj) throws JSONException{
        String webMenuType = (String) jsobj.optString("web_menu_type");
        if(webMenuType.toUpperCase().equals("WM_RELOAD")){
            reloadWebView();
        }
    }
    /*UIX-161/yangzhi/2016.05.19---end---*/
    
    private void reloadWebView(){
        try {
        RelativeLayout webViewLy = (RelativeLayout) mContext
                .findViewById(R.id.duoqu_webview);
        RelativeLayout errViewLy = (RelativeLayout) mContext
                .findViewById(R.id.duoqu_error_page);
        TextView titleName = (TextView) mContext
                .findViewById(R.id.duoqu_title_name);
        if (webViewLy != null && errViewLy != null) {
            WebView webView = (WebView) webViewLy.getChildAt(0);
            int isNetWork = XyUtil.checkNetWork(mContext,2);
            if (isNetWork == -1 || isNetWork == 1) {
                errViewLy.setVisibility(View.VISIBLE);
                titleName.setText(R.string.duoqu_web_not_find_page);
                webView.setVisibility(View.GONE);
            } else {
                if(!StringUtils.isNull(SdkWebActivity.endUrl)){
                    webView.loadUrl(SdkWebActivity.endUrl);
                    SdkWebActivity.endUrl = "";
                }else{
                    webView.reload();
                }
                errViewLy.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        }
        }catch(Throwable e){
            
        }
    }
    
    private void cleanData(){
        mContext =null;
        mJsonArray =null;
    }
    /*SDK-425 lilong 20160510 end*/
}