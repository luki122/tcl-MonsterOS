package cn.com.xy.sms.sdk.ui.popu.web;

import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class CommonWebChromeClientEx extends WebChromeClient {

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces(view);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("CommonWebChromeClientEx error:", e);
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message,
            String defaultValue, JsPromptResult result) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                if (webview.handleJsInterface(view, url, message, defaultValue,
                        result)) {
                    return true;
                }
            }
        } catch (Throwable e) {
           SmartSmsSdkUtil.smartSdkExceptionLog("CommonWebChromeClientEx error:", e);
        }
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        try {
            if (view instanceof CommonWebView) {
                CommonWebView webview = (CommonWebView) view;
                webview.injectJavascriptInterfaces(view);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("CommonWebChromeClientEx error:", e);
        }
    }
}
