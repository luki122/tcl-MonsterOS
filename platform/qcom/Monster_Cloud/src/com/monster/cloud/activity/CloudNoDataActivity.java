package com.monster.cloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.monster.cloud.R;
import com.monster.cloud.activity.exchange.LoginTCLAccountActivity;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-11-17.
 */
public class CloudNoDataActivity extends MstActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.cloud_no_data_activity);
        getToolbar().setTitle(R.string.onekeyrecovery);
        RelativeLayout sync_now =(RelativeLayout)findViewById(R.id.sync_now);
        sync_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(CloudNoDataActivity.this,LoginTCLAccountActivity.class);
                i.putExtra("sync_now",true);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
    }

}
