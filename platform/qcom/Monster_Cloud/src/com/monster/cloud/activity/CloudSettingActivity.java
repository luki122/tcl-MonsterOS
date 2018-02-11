package com.monster.cloud.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.activity.exchange.ShowChangeQQActivity;
import com.monster.cloud.utils.HttpRequestUtil;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;
import com.tencent.tauth.Tencent;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import mst.app.MstActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_setting);

        toolbar = getToolbar();
        toolbar.setBackgroundColor(Color.parseColor("#05000000"));
        toolbar.setTitle(R.string.setting);

        //default: sync only when wifi on
        SyncTimeUtil.setSyncWhenWifiLabel(this, true);
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
            }
        });

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

        User user = UiAccountHelper.getUserInfo(this);
        tclAccountName.setText(user.accountName);

        changeQQLayout = (RelativeLayout) findViewById(R.id.qq_helper_state);
        changeQQLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloudSettingActivity.this, ShowChangeQQActivity.class);
                intent.putExtra("title", getString(R.string.change_qq_id));
                intent.putExtra("is_should_return", true);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
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
}
