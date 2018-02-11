package com.android.calculator2.exchange.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.android.calculator2.R;
import com.android.calculator2.utils.NetworkUtil;
import com.android.calculator2.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class ConnectManager {

    private static final String APPLICATION_JSON = "application/json";
    private static final String CHARSET = HTTP.UTF_8;

    private static HttpClient mHttpClient;

    /**
     * HttpClient使用单例模式
     * 
     * @param httpClient
     */
    public static synchronized HttpClient getHttpClient() {
        if (mHttpClient == null) {
            HttpParams params = new BasicHttpParams();
            // 设置HTTP基本参数
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams
                    .setUserAgent(
                            params,
                            "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
                                    + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
            // 超时设置
            /* 从连接池中取连接的超时时间 */
            ConnManagerParams.setTimeout(params, 5000);
            /* 连接超时 */
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            /* 请求超时 */
            HttpConnectionParams.setSoTimeout(params, 10000);

            // 支持HTTP和HTTPS两种模式
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory
                    .getSocketFactory(), 443));

            // 使用线程安全的连接管理来创建HttpClient
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                    params, schReg);
            mHttpClient = new DefaultHttpClient(conMgr, params);

        }
        return mHttpClient;
    }

    /**
     * 获取标准 Cookie ，并存储
     */
    private static synchronized void getCookie(HttpClient Client) {
        CookieStore cookiestore = (CookieStore) ((AbstractHttpClient) Client)
                .getCookieStore();
        List<Cookie> cookies = cookiestore.getCookies();
        String sendcookie = cookiestore.getCookies().toString();
        if (!cookies.isEmpty()) {
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = cookies.get(i);
                String cookieName = cookie.getName();
                String cookieValue = cookie.getValue();
            }
        }
    }

    /**
     * post获取信息 例：微信请求
     */
    public static synchronized String postInfo(Context context, String url,
            List<NameValuePair> formparams) {

        if (!netIsConnect(context)) {
            return null;
        }

        String responseBody = null;
        try {
            // 编码参数
            HttpClient httpClient = getHttpClient();
            getCookie(httpClient);

            UrlEncodedFormEntity entity = null;
            if (formparams != null) {
                entity = new UrlEncodedFormEntity(formparams, CHARSET);
            }
            // 创建POST请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept-Encoding", "identity");
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
            httpPost.setEntity(entity);
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity resEntity = httpResponse.getEntity();
            HttpEntity httpEntity = null;
            if (resEntity != null) {
                httpEntity = new BufferedHttpEntity(resEntity);
            }
            responseBody = EntityUtils.toString(httpEntity, CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    /**
     * post获取图片信息并加载到本地
     * 
     * @param imageView
     */
    public static synchronized Bitmap postImageLoader(String url) {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            // 编码参数
            HttpClient httpClient = getHttpClient();
            getCookie(httpClient);
            // 创建POST请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept-Encoding", "identity");
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return bitmap;
            }
            HttpEntity resEntity = httpResponse.getEntity();
            HttpEntity httpEntity = null;
            if (resEntity != null) {
                httpEntity = new BufferedHttpEntity(resEntity);
            }
            is = httpEntity.getContent();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * post文件图片上传
     */
    public static synchronized String postUpLoad(Context context, String url,
            HttpEntity httpEntity, String filePath) {

        String responseInfo = null;
        if (!netIsConnect(context)) {
            return responseInfo;
        }
        try {
            HttpClient httpClient = getHttpClient();
            getCookie(httpClient);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(httpEntity);
            httpClient.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, 120000);
            httpClient.getParams().setParameter(
                    CoreConnectionPNames.SO_TIMEOUT, 120000);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity resEntity = httpResponse.getEntity();
                String response = (resEntity == null) ? null : EntityUtils
                        .toString(resEntity, CHARSET);
                if (response != null) {
                    JSONArray jSONArray = new JSONArray(response);
                    JSONObject jSONObject = jSONArray.getJSONObject(0);
                    responseInfo = jSONObject.get("info").toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseInfo;
    }

    /**
     * 
     * @Title: post
     * @Description: 服务器信息请求接口
     * @param @param context
     * @param @param url 地址
     * @param @param formparams 参数
     * @param @param isShowError 是否显示错误信息
     * @return String data数据 错误时为空
     * @throws
     */

    public static synchronized String post(Context context, String url,
            JSONObject jsonParam, boolean isShowError) {
        return post(context, url, jsonParam, isShowError, true);
    }

    public static synchronized String post(Context context, String url,
            JSONObject jsonParam, boolean isShowError, boolean isneedsession) {
        if (!netIsConnect(context)) {
            return null;
        }

        String responseEntity = null;
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("params", jsonParam);
            JSONArray param = new JSONArray();
            param.put(jsonObj);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("param", param.toString()));
            if (formparams != null) {
                try {
                    Log.e("zouxu",formparams.get(0).toString());
                } catch (Exception e) {
                }
            }
            HttpClient httpClient = getHttpClient();
            getCookie(httpClient);
            HttpGet httpPost = new HttpGet(url);
            httpPost.setHeader("Accept-Encoding", "identity");
            // 传入sessionid给服务器授权判断
//            httpPost.setEntity(new UrlEncodedFormEntity(formparams, CHARSET));
//            httpPost.s
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int code = httpResponse.getStatusLine().getStatusCode();
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                if (isShowError) {
                    ToastNetError(context);
                }
                return responseEntity;
            }
            HttpEntity resEntity = httpResponse.getEntity();
            HttpEntity httpEntity = null;
            if (resEntity != null) {
                httpEntity = new BufferedHttpEntity(resEntity);
            }
            String response = EntityUtils.toString(httpEntity, CHARSET);
            if (TextUtils.isEmpty(response) || response.equals("(null)")) {
                if (isShowError) {
                    ToastNetError(context);
                }
                return null;
            }
            responseEntity = response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseEntity;
    }

    /** 请求网络时判断设备是否联网 */
    public static boolean netIsConnect(final Context context) {
        Handler handler = null;
        if (Looper.getMainLooper() != null) {
            handler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                    
                    Toast.makeText(context, getNetErrorString(context), Toast.LENGTH_SHORT).show();
                };
            };
        }
        if (null == context) {
            return false;
        }
        boolean isConn = NetworkUtil.isNetworkAvailable(context);
        if (!isConn) {
            if (null != handler) {
                handler.sendEmptyMessage(0);
            }
        }
        return isConn;
    }
    
    
    public  static String getNetErrorString(Context context){
        String str;
        String update_time = Utils.getRateUpDataInfo(context);
        if(TextUtils.isEmpty(update_time)){
            str = context.getString(R.string.str_first_no_net);
        } else {
            str = context.getString(R.string.str_no_net)+","+update_time;
        }
        
        return str;
    }

    public static void ToastNetError(final Context context) {
        Handler handler = null;
        if (Looper.getMainLooper() != null) {
            handler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                    Toast.makeText(context, getNetErrorString(context), Toast.LENGTH_SHORT).show();
                };
            };
        }
        if (null == context) {
            return;
        }
        if (null != handler) {
            handler.sendEmptyMessage(0);
        }
    }
}
