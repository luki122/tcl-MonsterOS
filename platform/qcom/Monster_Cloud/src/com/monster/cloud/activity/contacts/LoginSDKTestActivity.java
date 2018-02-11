package com.monster.cloud.activity.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.tencent.connect.common.Constants;
import com.tencent.qqpim.sdk.accesslayer.LoginMgrFactory;
import com.tencent.qqpim.sdk.accesslayer.interfaces.ILoginMgr;
import com.tencent.qqpim.sdk.utils.AccountUtils;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by opdeng on 2016/8/23.
 */
public class LoginSDKTestActivity extends Activity {

    private TextView mOpenidTv;
    private TextView mAccessTokenTv;

    private Tencent mTencent;
    private String APPID = "1105638128";

    private ILoginMgr mLoginMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_login);

        mLoginMgr = LoginMgrFactory.getLoginMgr(this, AccountUtils.ACCOUNT_TYPE_QQ_SDK);

        mOpenidTv = (TextView) findViewById(R.id.openid_tv);
        mAccessTokenTv = (TextView) findViewById(R.id.token_tv);

        mTencent = Tencent.createInstance(APPID, getApplicationContext());

        findViewById(R.id.test_login_btn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "登录测试", Toast.LENGTH_SHORT).show();
                        if (!mTencent.isSessionValid()) {
                            mTencent.login(LoginSDKTestActivity.this, "all", mLoginListener);
                        }
                    }
                }
        );
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            mTencent.handleLoginData(data, mLoginListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private IUiListener mLoginListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            try {
                JSONObject obj = new JSONObject(o.toString());
                final String openid = obj.optString("openid");
                final String token = obj.optString("access_token");

                mOpenidTv.setText("openid：\n" + openid);
                mAccessTokenTv.setText("access_token：\n" + token);

                findViewById(R.id.login_op).setVisibility(View.VISIBLE);
                findViewById(R.id.login_op).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //0为登录成功，非0为失败
                        int result = mLoginMgr.qqSDKlogin(openid, token, APPID);
                        findViewById(R.id.result_msg).setVisibility(View.VISIBLE);
                        if (result == 0) {
                            ((TextView) findViewById(R.id.result_msg)).setText("登录成功");
                        } else {
                            ((TextView) findViewById(R.id.result_msg)).setText("登录失败");
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(getApplicationContext(), uiError.errorDetail, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "onCancel", Toast.LENGTH_SHORT).show();
        }
    };

}
