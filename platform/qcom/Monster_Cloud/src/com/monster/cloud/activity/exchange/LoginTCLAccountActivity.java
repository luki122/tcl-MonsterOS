package com.monster.cloud.activity.exchange;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.utils.LoginUtil;
import com.tcl.account.sdkapi.AuthConfig;
import com.tcl.account.sdkapi.QQLoginListener;
import com.tcl.account.sdkapi.SessionAuthorizationType;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;

import org.json.JSONException;
import org.json.JSONObject;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-11-10.
 */
public class LoginTCLAccountActivity extends MstActivity implements View.OnClickListener {

    private RelativeLayout regist_layout;
    private RelativeLayout login_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.login_tcl_account);
        getToolbar().setTitle(R.string.str_one_key_exchange);
        regist_layout = (RelativeLayout)findViewById(R.id.regist_layout);
        login_layout = (RelativeLayout)findViewById(R.id.login_layout);
        regist_layout.setOnClickListener(this);
        login_layout.setOnClickListener(this);
        mUiAccountHelper = new UiAccountHelper(LoginTCLAccountActivity.this, mCallback);
        mUiAccountHelper.onCreate(savedInstanceState);


        if(LoginUtil.isLogInTCLAccount(this)){
            CheckQQLogin();
        }

    }

    private void CheckQQLogin(){
        if(LoginUtil.isQQLogIn(this)){
            Intent i = new Intent(this,ShowChangeQQActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this,ShowImportQQDataActivity.class);
            i.putExtra("is_should_return",false);
            startActivity(i);
        }
        finish();
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.regist_layout:
                Login();
                break;
            case R.id.login_layout:
                Login();
                break;
        }
    }

    private void Login(){
        AuthConfig authConfig = new AuthConfig(SessionAuthorizationType.AUTH);
        authConfig.setSessionAuthorizationType(SessionAuthorizationType.AUTH);
        Token token = UiAccountHelper.getCurrentToken(getApplicationContext());

        User user = UiAccountHelper.getUserInfo(this);
        User use2 = token.getUser();

        if (null != token && !token.isInvalid()) {
            Toast.makeText(LoginTCLAccountActivity.this, "logged!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mUiAccountHelper.requestSSOAuth(authConfig, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void Regist(){
//        AuthConfig authConfig = new AuthConfig(SessionAuthorizationType.AUTH);
//        authConfig.setSessionAuthorizationType(SessionAuthorizationType.AUTH);
//        Token token = UiAccountHelper.getCurrentToken(getApplicationContext());
////        if (null != token && !token.isInvalid()) {
////            Toast.makeText(LoginTCLAccountActivity.this, "logged!", Toast.LENGTH_LONG).show();
////            return;
////        }
//        User user = UiAccountHelper.getUserInfo(this);
//        if(token!=null){
//            User use2 = token.getUser();
//        }
//
//        try {
//            mUiAccountHelper.requestSSOAuth(authConfig, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private UiAccountHelper mUiAccountHelper = null;

    private SessionStatusCallback mCallback = new SessionStatusCallback() {

        @Override
        public void onSuccess(Token token) {
            Toast.makeText(LoginTCLAccountActivity.this, "success", Toast.LENGTH_LONG).show();
            qqLogin();
        }

        @Override
        public void onError(int err) {
            Toast.makeText(LoginTCLAccountActivity.this, "error", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onOAuth(String code) {
            Toast.makeText(LoginTCLAccountActivity.this, "code:" + code, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onNavigationClicked(View view) {

        finish();
    }


    private void qqLogin() {

        boolean enable = mUiAccountHelper.qqLoginWithCache(this, new QQLoginListener() {
            @Override
            public void onSuccess(String str_json) {

                JSONObject json;
                String openId=null;
                String accessToken=null;

                try {
                    json = new JSONObject(str_json);
                    openId = json.getString("openid");
                    accessToken = json.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (openId != null && accessToken != null) {
                    LoginUtil.updateOpenId(LoginTCLAccountActivity.this, openId);
                    LoginUtil.updateToken(LoginTCLAccountActivity.this, accessToken);
                }

                CheckQQLogin();

            }

            @Override
            public void onError(int errorCode, String errorMessage, String errorDetail) {
                Toast.makeText(LoginTCLAccountActivity.this, "onError errorCode:" + errorCode + ",errorMessage:" + errorMessage + ",errorDetail" + errorDetail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginTCLAccountActivity.this, "onCancel", Toast.LENGTH_LONG).show();
            }
        });

        if (!enable) {
            Toast.makeText(LoginTCLAccountActivity.this, "QQ Login unEnable", Toast.LENGTH_LONG).show();
        }


    }


}
