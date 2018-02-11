package com.monster.cloud.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.activity.exchange.ShowChangeQQActivity;
import com.monster.cloud.activity.exchange.ShowImportQQDataActivity;
import com.monster.cloud.utils.HttpRequestUtil;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.monster.cloud.utils.SystemUtil;
import com.tcl.account.sdkapi.QQLoginListener;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;
import com.tencent.tauth.Tencent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;

/**
 * Created by yubai on 16-11-16.
 */
public class CloudSettingActivity extends MstActivity {
    private final String TAG = "CloudSettingActivity";

    private mst.widget.toolbar.Toolbar toolbar;
    private Switch wifiSwitch;

    private Tencent tencent;
    private String APPID = "101181845";
    private String token = "";
    private String openId = "";

    private TextView nickTextView;
    private TextView tclAccountName;
    private RelativeLayout changeQQLayout;
    private RelativeLayout tclAccountState;

    private UiAccountHelper uiAccountHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_setting);

        toolbar = getToolbar();
        toolbar.setTitle(R.string.setting);

        wifiSwitch = (Switch) findViewById(R.id.sync_when_wifi);
        if (SyncTimeUtil.getSyncWhenWifiLabel(this)) {
            wifiSwitch.setChecked(true);
        } else {
            wifiSwitch.setChecked(false);
        }
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SyncTimeUtil.setSyncWhenWifiLabel(CloudSettingActivity.this, isChecked);
                if (!isChecked) {
                    new AlertDialog.Builder(CloudSettingActivity.this)
                            .setTitle(getString(R.string.cozy_hint_title))
                            .setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //DO NOTHING
                                    }
                                })
                            .setMessage(getString(R.string.cozy_hint_content))
                            .setNegativeButton(com.mst.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    wifiSwitch.setChecked(true);
                                }
                            }).show();

                }
            }
        });

        uiAccountHelper = new UiAccountHelper(this, callback);
        uiAccountHelper.onCreate(savedInstanceState);

        nickTextView = (TextView) findViewById(R.id.already_authorize);
        tclAccountName = (TextView) findViewById(R.id.tcl_account_name);

        if (LoginUtil.getLoginLabel(this)) {
            token = LoginUtil.getToken(this);
            openId = LoginUtil.getOpenId(this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getUserInfo();
                }
            }).start();
        } else {
            //TODO login first

        }

        if(TextUtils.isEmpty(token) || TextUtils.isEmpty(openId)){
            nickTextView.setText(R.string.not_connect);
        }

        User user = UiAccountHelper.getUserInfo(this);
        if (user.nickName != null && !user.nickName.isEmpty()) {
            tclAccountName.setText(user.nickName);
        } else {
            tclAccountName.setText(R.string.login_state);
        }

        changeQQLayout = (RelativeLayout) findViewById(R.id.qq_helper_state);
        changeQQLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloudSettingActivity.this, ShowChangeQQActivity.class);
                intent.putExtra("title", getString(R.string.change_qq_id));
                intent.putExtra("is_should_return", true);
                startActivityForResult(intent,100);
            }
        });

        tclAccountState = (RelativeLayout) findViewById(R.id.tcl_account_state);
        tclAccountState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.tcl.sso.accountservice.USER_INFO");
                intent.putExtra("APP_ID", "101181845");
                intent.putExtra("PACKAGE_NAME", getPackageName());
                intent.putExtra("FOR_SETTING", true);
                startActivity(intent);
            }
        });
    }

    private SessionStatusCallback callback = new SessionStatusCallback() {
        @Override
        public void onSuccess(Token token) {
        }

        @Override
        public void onError(int i) {
        }

        @Override
        public void onOAuth(String s) {

        }
    };


    @Override
    public void onNavigationClicked(View view) {
        reloadQQSDK();
    }

    String nickname = "";
    public void getUserInfo() {
        Map<String, String> params = new HashMap<>();
        params.put("oauth_consumer_key", APPID);
        params.put("access_token", token);
        params.put("openid", openId);
        HttpsURLConnection conn = HttpRequestUtil
                .sendGetRequest("https://graph.qq.com/user/get_user_info", params, null);
        if (null != conn) {
            int result = 0;
            try {
                result = conn.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result == 200) {
                InputStream inputStream;
                try {
                    inputStream = conn.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    //读取长度
                    int len = 0;
                    //定义缓冲区
                    byte buffer[] = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    String jsonString = outputStream.toString();
                    inputStream.close();
                    outputStream.close();

                    Log.d(TAG, "json :" + jsonString);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    nickname = jsonObject.getString("nickname");

                    if (nickname != null && !nickname.equals("")) {
                        handler.sendEmptyMessage(0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    nickTextView.setText(getString(R.string.already_connect) + " " +nickname);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if(is_finishing){
            return;
        }
        uiAccountHelper.onResume();
        User user = UiAccountHelper.getUserInfo(this);
        if(user == null || TextUtils.isEmpty(user.accountName)){//有可能点击账号后退出账号
            UiAccountHelper.cleanCache();
            Intent i = new Intent();
            i.putExtra("shold_finish",true);
            setResult(RESULT_OK,i);
            finish();
            return;
        } else if(!user.accountName.equals(LoginUtil.getTCLUsrID(CloudSettingActivity.this))){//切换了账号
            if(!reload_qqsdk){
                reload_qqsdk = true;
                checkIsTCLQQLogin();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiAccountHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiAccountHelper.onDestroy();
    }

    private void gotoShowImportQQData() {
        Intent i = new Intent(this, ShowImportQQDataActivity.class);
        i.putExtra("is_should_return", true);
        i.putExtra("title", getString(R.string.app_name));
        startActivityForResult(i, SystemUtil.REQUEST_LOGIN_QQ_SETTING_CHANGE_ACCOUNT);
    }

    private  boolean is_finishing = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiAccountHelper.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SystemUtil.REQUEST_MAINACT_FIRST_LOGIN){
            getUserInfo();
            return;
        }

        if(resultCode !=RESULT_OK && reload_qqsdk){//切换QQ的时候按了返回
            changeAccount();
            return;
        }
        token = LoginUtil.getToken(this);
        openId = LoginUtil.getOpenId(this);
        if(TextUtils.isEmpty(token) || TextUtils.isEmpty(openId)){
            nickTextView.setText(R.string.not_connect);
        } else {
            nickTextView.setText(getString(R.string.already_connect) + " " +nickname);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                getUserInfo();
            }
        }).start();

    }

    public void changeAccount(){
        Intent i = new Intent();
        i.putExtra("change_account_no_qq",true);//切换了账户但是没有QQ授权
        setResult(RESULT_OK,i);
        is_finishing = true;
        LoginUtil.updateOpenId(this, "");//清空openid taken
        LoginUtil.updateToken(this, "");

        finish();
    }

    private boolean reload_qqsdk = false;


    public void reloadQQSDK(){
        Intent i = new Intent();
        i.putExtra("reload_qqsdk",reload_qqsdk);
        setResult(RESULT_OK,i);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            reloadQQSDK();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkIsTCLQQLogin() {

        boolean enable = uiAccountHelper.qqLoginWithCache(this, new QQLoginListener() {
            @Override
            public void onSuccess(String str_json) {

                JSONObject json;

                String openid_form_tcl_account = "";
                String accessToken_form_tcl_account = "";

                try {
                    json = new JSONObject(str_json);
                    openid_form_tcl_account = json.getString("openid");
                    accessToken_form_tcl_account = json.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(!TextUtils.isEmpty(openid_form_tcl_account) && TextUtils.isEmpty(accessToken_form_tcl_account)){//发生了账号改变而且是用QQ登陆的

                    openId = openid_form_tcl_account;
                    token = accessToken_form_tcl_account;
                    LoginUtil.updateOpenId(CloudSettingActivity.this, openid_form_tcl_account);
                    LoginUtil.updateToken(CloudSettingActivity.this, accessToken_form_tcl_account);

                    Intent i = new Intent();
                    i.setClass(CloudSettingActivity.this, ShowChangeQQActivity.class);
                    i.putExtra("is_should_return",true);
                    i.putExtra("start_sync",true);
                    startActivityForResult(i,SystemUtil.REQUEST_MAINACT_FIRST_LOGIN);
                    LoginUtil.saveTCLUserId(CloudSettingActivity.this);

                } else {
                    gotoShowImportQQData();
                }

            }

            @Override
            public void onError(int errorCode, String errorMessage, String errorDetail) {
                Toast.makeText(CloudSettingActivity.this, "onError errorCode:" + errorCode + ",errorMessage:" + errorMessage + ",errorDetail" + errorDetail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(CloudSettingActivity.this, "onCancel", Toast.LENGTH_LONG).show();
            }
        });

        if (!enable) {
            Toast.makeText(CloudSettingActivity.this, "QQ Login unEnable", Toast.LENGTH_LONG).show();
        }

    }

}
