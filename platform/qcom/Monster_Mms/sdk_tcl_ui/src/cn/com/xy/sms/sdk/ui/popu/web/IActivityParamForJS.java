package cn.com.xy.sms.sdk.ui.popu.web;

import android.app.Activity;
import android.webkit.WebView;

public interface IActivityParamForJS {

    public WebView getWebView();

    /**
     * get value by key
     * 
     * @param key
     * @return
     */
    public String getParamData(String key);

    public Activity getActivity();

    /**
     * Somehow the screen gets the current screen
     * 
     * @return
     */
    public int checkOrientation();
    
    /* RM-356 zhengxiaobo 20160506 begin */
    public void setParamData(String key, String value);
    /* RM-356 zhengxiaobo 20160506 end */
    
    public String getType();
      
    /*QIK-592 wangxingjian 20160727 begin*/
    public void setData(int type, Object data);
    /*QIK-592 wangxingjian 20160727 end*/
}
