package com.monster.cloud.activity.exchange;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.activity.OneKeyRecoveryActiviy;
import com.monster.cloud.utils.HttpRequestUtil;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SystemUtil;
import com.tcl.account.sdkapi.QQLoginListener;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-11-10.
 */
public class ShowChangeQQActivity extends MstActivity implements View.OnClickListener{

    private UiAccountHelper uiAccountHelper = null;

    private RelativeLayout change_qq_layout;
    private RelativeLayout start_recovery_layout;

    private boolean is_should_return = false;
    private String title;

    private String APPID = "101181845";
    private String token = "";
    private String openId = "";

    private TextView tcl_account_info;
    private TextView text_start_revovery;

    private boolean start_sync = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUtil.setStatusBarColor(this,R.color.white);
        setMstContentView(R.layout.change_qq_activity);
        change_qq_layout= (RelativeLayout)findViewById(R.id.change_qq_layout);
        start_recovery_layout= (RelativeLayout)findViewById(R.id.start_recovery_layout);
        tcl_account_info= (TextView) findViewById(R.id.tcl_account_info);
        text_start_revovery= (TextView) findViewById(R.id.text_start_revovery);
        change_qq_layout.setOnClickListener(this);
        start_recovery_layout.setOnClickListener(this);
        uiAccountHelper = new UiAccountHelper(this, callback);
        uiAccountHelper.onCreate(savedInstanceState);
        getIntentData();
        if(!TextUtils.isEmpty(title)){
            getToolbar().setTitle(title);
        } else {
            getToolbar().setTitle(R.string.str_one_key_exchange);
        }
        if(is_should_return && start_sync == false){
            start_recovery_layout.setVisibility(View.GONE);
        } else if(start_sync){
            text_start_revovery.setText(R.string.start_sync);
        } else {
            text_start_revovery.setText(R.string.next_step);
        }

        if (LoginUtil.getLoginLabel(this)) {
            token = LoginUtil.getToken(this);
            openId = LoginUtil.getOpenId(this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getUserInfo();
                }
            }).start();
        }
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
                    User user = UiAccountHelper.getUserInfo(ShowChangeQQActivity.this);

                    String info = getString(R.string.str_tcl_account_info);
                    tcl_account_info.setText(String.format(info,user.accountName,nickname));
                    break;
                default:
                    break;
            }
        }
    };



    private void getIntentData(){
        Intent i = getIntent();
        if(i!=null){
            is_should_return = i.getBooleanExtra("is_should_return",false);
            start_sync = i.getBooleanExtra("start_sync",false);
            title = i.getStringExtra("title");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiAccountHelper.onResume();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiAccountHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if(100 == requestCode){
            setResult(RESULT_OK);
            finish();
        }
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
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_regist:
                break;
        }
        return true;
    }

    private void QQLogIn() {
        boolean enable = uiAccountHelper.qqLogin(this, new QQLoginListener() {
            @Override
            public void onSuccess(String s) {
                JSONObject json;
                String openId="";
                String accessToken="";

                try {
                    json = new JSONObject(s);
                    openId = json.getString("openid");
                    accessToken = json.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LoginUtil.updateOpenId(ShowChangeQQActivity.this, openId);
                LoginUtil.updateToken(ShowChangeQQActivity.this, accessToken);

                Intent i = new Intent(ShowChangeQQActivity.this,ShowStartRecorveryActivity.class);
                i.putExtra("is_should_return",is_should_return);
                startActivityForResult(i, 100);
                finish();
            }

            @Override
            public void onError(int errorCode, String errorMessage, String errorDetail) {
//                Toast.makeText(ShowChangeQQActivity.this,
//                        "onError errorCode:" + errorCode + ",errorMessage:" + errorMessage + ",errorDetail" + errorDetail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
//                Toast.makeText(ShowChangeQQActivity.this, "onCancel", Toast.LENGTH_LONG).show();
            }
        });
        if (!enable) {
//            Toast.makeText(ShowChangeQQActivity.this, "QQ Login disabled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.change_qq_layout:
                QQLogIn();
                break;
            case R.id.start_recovery_layout:
                startRecovery();
                break;
        }
    }

    public void startRecovery(){
//        Intent i = new Intent(this, OneKeyRecoveryActiviy.class);
//        startActivity(i);
//        finish();
        Intent i = new Intent(ShowChangeQQActivity.this,ShowStartRecorveryActivity.class);
        i.putExtra("is_should_return",is_should_return);
        startActivityForResult(i, 100);
        finish();
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }
}
