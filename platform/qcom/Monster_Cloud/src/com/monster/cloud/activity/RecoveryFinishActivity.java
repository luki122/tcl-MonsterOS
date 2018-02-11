package com.monster.cloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.cloud.R;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-10-31.
 */
public class RecoveryFinishActivity extends MstActivity implements View.OnClickListener {

    public static final String OPEN_TYPE = "openType";
    public static final int TYPE_NO_APP = 0;
    public static final int TYPE_CONTAINER_APP = 1;
    public static final int TYPE_ONLY_APP = 2;

    private TextView tv_text;
    private RelativeLayout ok_layout;
    private LinearLayout app_download_layout;
    private RelativeLayout app_ok_layout;
    private RelativeLayout app_manager_layout;

    private int openType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.recovery_finish);
        getToolbar().setTitle(R.string.onekeyrecovery);

        getIntentData();

        tv_text = (TextView) findViewById(R.id.tv_text);
        ok_layout = (RelativeLayout)findViewById(R.id.ok_layout);
        app_download_layout = (LinearLayout) findViewById(R.id.app_download_layout);
        app_ok_layout = (RelativeLayout) findViewById(R.id.app_ok_layout);
        app_manager_layout = (RelativeLayout) findViewById(R.id.app_manager_layout);

        ok_layout.setOnClickListener(this);
        app_ok_layout.setOnClickListener(this);
        app_manager_layout.setOnClickListener(this);

        if (openType == TYPE_NO_APP) {
            tv_text.setText(R.string.revocery_finish);
            ok_layout.setVisibility(View.VISIBLE);
            app_download_layout.setVisibility(View.GONE);
        } else if (openType == TYPE_CONTAINER_APP) {
            tv_text.setText(R.string.recovery_finish_without_app);
            ok_layout.setVisibility(View.GONE);
            app_download_layout.setVisibility(View.VISIBLE);
        } else if (openType == TYPE_ONLY_APP) {
            tv_text.setText(R.string.recovery_finish_only_app);
            ok_layout.setVisibility(View.GONE);
            app_download_layout.setVisibility(View.VISIBLE);
        }
    }

    private void getIntentData(){
        Intent i = getIntent();
        if (i != null) {
            openType = i.getIntExtra(OPEN_TYPE, TYPE_NO_APP);
        }
    }

    private void gotoHome(){
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_layout:
                gotoHome();
                break;
            case R.id.app_ok_layout:
                gotoHome();
                break;
            case R.id.app_manager_layout:
                openDownloadManager();
                break;
        }
    }

    private void openDownloadManager() {
        Intent managerIntent = new Intent("com.monster.market.downloadmanager");
        startActivity(managerIntent);
    }

}
