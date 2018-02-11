package com.monster.cloud.activity.sms;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.monster.cloud.R;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-10-27.
 */
public class SmsChooseSyncTypeActivity  extends MstActivity {

    private int type;//0 备份 1 恢复
    private RelativeLayout sms_type_by_time;
    private RelativeLayout sms_type_by_contacts;
    private int time_type = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.sms_choose_type);
        getIntentData();
        if(type ==0){
            getToolbar().setTitle(R.string.sms_choose_type_sync);
        } else {
            getToolbar().setTitle(R.string.sms_choose_type_restore);
        }
        initView();
    }

    private void getIntentData(){
        Intent i = getIntent();
        if(i!=null){
            type = i.getIntExtra("type",0);
            time_type = i.getIntExtra("time_type",1);
        }
    }

    public void initView(){
        sms_type_by_time = (RelativeLayout)findViewById(R.id.sms_type_by_time);
        sms_type_by_contacts = (RelativeLayout)findViewById(R.id.sms_type_by_contacts);

        sms_type_by_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SmsChooseSyncTypeActivity.this,SmsSyncByTime.class);
                i.putExtra("type",type);
                i.putExtra("time_type",time_type);
                startActivityForResult(i,1);
            }
        });
        sms_type_by_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            setResult(resultCode,data);
            finish();
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

}
