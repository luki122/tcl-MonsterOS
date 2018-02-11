package cn.tcl.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Method;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/9 19:30
 * @copyright TCL-MIE
 */
public class MusicWebView extends WebView {

    public MusicWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(true);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);

        try {
            Method method = WebSettings.class.getMethod("setNavDump", boolean.class);
            method.setAccessible(true);
            method.invoke(settings, false);
        }catch (Throwable t) {
        }
        //*** 打开本地缓存提供JS调用 **//
        settings.setDomStorageEnabled(true);
        // Set cache size to 8 mb by default. should be more than enough
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        // This next one is crazy. It's the DEFAULT location for your app's
        // cache
        // But it didn't work for me without this line.
        // UPDATE: no hardcoded path. Thanks to Kevin Hawkins
        String appCachePath = context.getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    loadUrl("javascript:$(\"input[type='submit']\").trigger('click')");
                    return true;
                }
                return false;
            }
        });
        setWebViewClient(new MusicWebViewClient());
    }

    private static class MusicWebViewClient extends WebViewClient {
        public static final String TAG = "MusicWebViewClient";
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }
}
