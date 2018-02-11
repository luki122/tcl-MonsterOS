/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tcl.account.sdkapi.AuthConfig;
import com.tcl.account.sdkapi.SessionAuthorizationType;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Setting;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.MetaUtil;
import cn.tcl.setupwizard.utils.UrlConfig;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView headIconTV;
    private TextView usernameTV;
    private TextView nickNameTV;
    private TextView phoneTV;
    private TextView mailTV;
    private TextView idTV;
    private TextView tokenTV;
    private TextView statusTV;
    private TextView activatedTV;
    private TextView lastRefreshTV;
    private TextView expiresTV;

    private TextView validateAuthTV;

    UiAccountHelper mUiAccountHelper = null;

    private SessionStatusCallback mCallback = new SessionStatusCallback() {

        @Override
        public void onSuccess(Token token) {
            success(token);
            Toast.makeText(AccountActivity.this, "success", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(int err) {
            error(err);
            Toast.makeText(AccountActivity.this, "error: " + err, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setDebugEnabled(true);
        setContentView(R.layout.activity_account);

        mUiAccountHelper = new UiAccountHelper(AccountActivity.this, mCallback);
        mUiAccountHelper.onCreate(savedInstanceState);
        initView();
        NukeSSLCerts.nuke();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUiAccountHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUiAccountHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUiAccountHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiAccountHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUiAccountHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        initUI();

        Token token = UiAccountHelper.getCurrentToken(AccountActivity.this.getApplicationContext());
        if (null != token && !token.isInvalid()) {
            success(token);
            Toast.makeText(AccountActivity.this, "Already login", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View v) {
        AuthConfig authConfig = new AuthConfig(SessionAuthorizationType.AUTH);//appId can also be configured in the Manifest
        int id = v.getId();
        switch (id) {
            case R.id.getUserInfo:
                User user = UiAccountHelper.getUserInfo(AccountActivity.this);
                success(user);
                break;
            case R.id.requestssoauth:
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.AUTH);
                login(authConfig);
                break;
            case R.id.requestssoauth_email_only:
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.EMAIL_ONLY_AUTH);
                login(authConfig);
                break;
            case R.id.requestssoauth_phone_only:
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.PHONE_ONLY_AUTH);
                login(authConfig);
                break;
            case R.id.request_no_ui_auth:
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.NO_UI_AUTH);
                login(authConfig);
                break;
            case R.id.request_no_ui_email_only_auth:
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.NO_UI_EMAIL_ONLY_AUTH);
                login(authConfig);
                break;
            case R.id.request_no_ui_phone_only_auth:
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.NO_UI_PHONE_ONLY_AUTH);
                login(authConfig);
                break;
            case R.id.cleanCache:
                UiAccountHelper.cleanCache();
                break;
            case R.id.validateAuth:
                // just for test
                UrlConfig.init(AccountActivity.this);
                String domainUrl = UrlConfig.getDomainUrl();
                RequestQueue mQueue = Volley.newRequestQueue(AccountActivity.this);
                String uri = String.format(domainUrl + "account/ssoValidateAuth?username=%1$s&token=%2$s&appId=%3$s&appKey=%4$s",
                        usernameTV.getText().toString(), tokenTV.getText().toString(), MetaUtil.getMetaData(getApplicationContext(), "TCL_APPID"), MetaUtil.getMetaData(getApplicationContext(), "TCL_APPKEY"));
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        uri, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {
                                Log.e("TAG", response.toString());

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        validateAuthTV.setText(response.toString());
                                        System.out.println(response.toString());
                                    }
                                });
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                validateAuthTV.setText(error.getMessage());
                            }
                        });
                    }
                }) {

                };
                mQueue.add(jsonObjectRequest);
                break;
            default:
                break;
        }
    }

    public void login(AuthConfig authConfig) {
        Token token = UiAccountHelper.getCurrentToken(getApplicationContext());
        if (null != token && !token.isInvalid()) {
            Toast.makeText(AccountActivity.this, "Already login", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mUiAccountHelper.requestSSOAuth(authConfig, null);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void success(Token token) {
        if (null != token) {
            User user = token.getUser();
            if (null != user) {
                headIconTV.setText("" + user.headIconUrl);
                usernameTV.setText(user.accountName);
                nickNameTV.setText("Not filled");
                phoneTV.setText("" + user.phone);
                mailTV.setText("" + user.email);
                tokenTV.setText(token.getToken());
                statusTV.setText("Success");
                activatedTV.setText("" + user.isActivated);
                lastRefreshTV.setText(token.getLastRefresh().toString());
                expiresTV.setText(token.getExpires().toString());
            }
        }
    }

    public void error(int err) {
        usernameTV.setText("Nothing");
        nickNameTV.setText("Nothing");
        phoneTV.setText("Nothing");
        mailTV.setText("Nothing");
        idTV.setText("Nothing");
        tokenTV.setText("Nothing");
        statusTV.setText("fail:  " + err);
        activatedTV.setText("Nothing");
        lastRefreshTV.setText("Nothing");
        expiresTV.setText("Nothing");
    }

    public void success(User user) {
        if (null != user) {
            headIconTV.setText("" + user.headIconUrl);
            usernameTV.setText(user.accountName);
            nickNameTV.setText("Not filled");
            phoneTV.setText("" + user.phone);
            mailTV.setText("" + user.email);
            tokenTV.setText("Nothing");
            statusTV.setText("Success");
            activatedTV.setText("" + user.isActivated);
            lastRefreshTV.setText("Nothing");
            expiresTV.setText("Nothing");
        } else {
            headIconTV.setText("Nothing");
            usernameTV.setText("Nothing");
            nickNameTV.setText("Nothing");
            phoneTV.setText("Nothing");
            mailTV.setText("Nothing");
            tokenTV.setText("Nothing");
            statusTV.setText("fail");
            activatedTV.setText("Nothing");
            lastRefreshTV.setText("Nothing");
            expiresTV.setText("Nothing");
        }
    }

    private void initUI() {
        headIconTV = (TextView) findViewById(R.id.head_icon);
        usernameTV = (TextView) findViewById(R.id.username);
        nickNameTV = (TextView) findViewById(R.id.nick_name);
        phoneTV = (TextView) findViewById(R.id.phone);
        mailTV = (TextView) findViewById(R.id.mail);
        idTV = (TextView) findViewById(R.id.id);
        tokenTV = (TextView) findViewById(R.id.token);
        statusTV = (TextView) findViewById(R.id.status);
        activatedTV = (TextView) findViewById(R.id.activated);
        lastRefreshTV = (TextView) findViewById(R.id.lastRefresh);
        expiresTV = (TextView) findViewById(R.id.expires);
        validateAuthTV = (TextView) findViewById(R.id.validateAuthstatus);
        findViewById(R.id.requestssoauth).setOnClickListener(this);
        findViewById(R.id.requestssoauth_email_only).setOnClickListener(this);
        findViewById(R.id.requestssoauth_phone_only).setOnClickListener(this);
        findViewById(R.id.requestssoauth_email_phone_only).setOnClickListener(this);
        findViewById(R.id.requestssoauth_active).setOnClickListener(this);
        findViewById(R.id.getUserInfo).setOnClickListener(this);
        findViewById(R.id.request_no_ui_auth).setOnClickListener(this);
        findViewById(R.id.request_no_ui_email_only_auth).setOnClickListener(this);
        findViewById(R.id.request_no_ui_phone_only_auth).setOnClickListener(this);
        findViewById(R.id.request_no_ui_activate_auth).setOnClickListener(this);
        findViewById(R.id.cleanCache).setOnClickListener(this);
        findViewById(R.id.validateAuth).setOnClickListener(this);

        headIconTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        nickNameTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        phoneTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        mailTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        idTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        tokenTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        activatedTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        lastRefreshTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        expiresTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        validateAuthTV.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * Ignore this
     */
    public static class NukeSSLCerts {
        protected static final String TAG = "NukeSSLCerts";

        public static void nuke() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
            }
        }
    }
}
