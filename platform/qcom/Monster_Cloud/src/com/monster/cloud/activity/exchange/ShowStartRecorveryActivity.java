package com.monster.cloud.activity.exchange;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.activity.OneKeyRecoveryActiviy;
import com.monster.cloud.utils.SyncTimeUtil;
import com.monster.cloud.utils.SystemUtil;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-11-9.
 */
public class ShowStartRecorveryActivity extends MstActivity implements View.OnClickListener{

    private RelativeLayout auto_sync_head_layout;
    private ImageView img_arrow;
    private ScrollView sync_list;

    private RelativeLayout contact_layout;
    private TextView contact_sync_time;
    private Switch contact_switch;

    private RelativeLayout sms_layout;
    private TextView sms_sync_time;
    private Switch sms_switch;

    private RelativeLayout calllog_layout;
    private TextView calllog_sync_time;
    private Switch calllog_switch;

    private RelativeLayout app_layout;
    private TextView app_sync_time;
    private Switch app_switch;

    private RelativeLayout start_recovery_layout;
    private TextView text_start_revovery;

    private boolean isShowScrollView = false;
    private boolean is_should_return = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.setStatusBarColor(this,R.color.background_fafafa);

        setMstContentView(R.layout.start_recovery_activity);
        getToolbar().setTitle(R.string.str_net_trans);
        getIntentData();
        initView();
    }

    private void getIntentData(){
        Intent i = getIntent();
        if(i !=null){
            is_should_return = i.getBooleanExtra("is_should_return",true);
        }
    }

    private void initView(){
        auto_sync_head_layout = (RelativeLayout)findViewById(R.id.auto_sync_head_layout);
        img_arrow = (ImageView)findViewById(R.id.img_arrow);
        sync_list = (ScrollView)findViewById(R.id.sync_list);

        contact_layout = (RelativeLayout)findViewById(R.id.contact_layout);
        contact_sync_time = (TextView)findViewById(R.id.contact_sync_time);
        contact_switch = (Switch)findViewById(R.id.contact_switch);

        sms_layout = (RelativeLayout)findViewById(R.id.sms_layout);
        sms_sync_time = (TextView)findViewById(R.id.sms_sync_time);
        sms_switch = (Switch)findViewById(R.id.sms_switch);

        calllog_layout = (RelativeLayout)findViewById(R.id.calllog_layout);
        calllog_sync_time = (TextView)findViewById(R.id.calllog_sync_time);
        calllog_switch = (Switch)findViewById(R.id.calllog_switch);

        app_layout = (RelativeLayout)findViewById(R.id.app_layout);
        app_sync_time = (TextView)findViewById(R.id.app_sync_time);
        app_switch = (Switch)findViewById(R.id.app_switch);

        start_recovery_layout = (RelativeLayout)findViewById(R.id.start_recovery_layout);
        text_start_revovery = (TextView)findViewById(R.id.text_start_revovery);

        if(is_should_return){
            text_start_revovery.setText(R.string.str_finish);
            isShowScrollView = true;
            sync_list.setVisibility(View.VISIBLE);
            img_arrow.setImageResource(R.drawable.arrow_up);
        } else {
            text_start_revovery.setText(R.string.next_step);
        }

        auto_sync_head_layout.setOnClickListener(this);
        contact_layout.setOnClickListener(this);
        sms_layout.setOnClickListener(this);
        calllog_layout.setOnClickListener(this);
        app_layout.setOnClickListener(this);
        start_recovery_layout.setOnClickListener(this);

        contact_switch.setChecked(true);
        sms_switch.setChecked(true);
        calllog_switch.setChecked(true);
        app_switch.setChecked(true);
        SyncTimeUtil.setContactSyncLabel(ShowStartRecorveryActivity.this,true);
        SyncTimeUtil.setSmsSyncLabel(ShowStartRecorveryActivity.this,true);
        SyncTimeUtil.setRecordSyncLabel(ShowStartRecorveryActivity.this,true);
        SyncTimeUtil.setAppListSyncLabel(ShowStartRecorveryActivity.this,true);
        contact_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SyncTimeUtil.setContactSyncLabel(ShowStartRecorveryActivity.this,b);

            }
        });
        sms_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SyncTimeUtil.setSmsSyncLabel(ShowStartRecorveryActivity.this,b);

            }
        });
        calllog_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SyncTimeUtil.setRecordSyncLabel(ShowStartRecorveryActivity.this,b);

            }
        });
        app_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SyncTimeUtil.setAppListSyncLabel(ShowStartRecorveryActivity.this,b);

            }
        });

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.auto_sync_head_layout:
                isShowScrollView = !isShowScrollView;
                if(isShowScrollView){
                    sync_list.setVisibility(View.VISIBLE);
                    img_arrow.setImageResource(R.drawable.arrow_up);
                } else {
                    sync_list.setVisibility(View.INVISIBLE);
                    img_arrow.setImageResource(R.drawable.arrow_down);
                }
                break;
            case R.id.contact_layout:
                contact_switch.setChecked(!contact_switch.isChecked());
                break;
            case R.id.sms_layout:
                sms_switch.setChecked(!sms_switch.isChecked());
                break;
            case R.id.calllog_layout:
                calllog_switch.setChecked(!calllog_switch.isChecked());
                break;
            case R.id.app_layout:
                app_switch.setChecked(!app_switch.isChecked());
                break;
            case R.id.start_recovery_layout:
                if(is_should_return){
                    setResult(RESULT_OK);
                } else {
                    Intent i = new Intent(ShowStartRecorveryActivity.this,OneKeyRecoveryActiviy.class);
                    startActivity(i);
                }
                finish();
                break;
        }

    }

    @Override
    public void onNavigationClicked(View view) {

        finish();
    }

}
