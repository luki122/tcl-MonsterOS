package com.monster.cloud.activity.sms;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.cloud.R;

import mst.app.MstActivity;

/**
 * Created by zouxu on 16-10-27.
 */
public class SmsSyncByTime extends MstActivity {

    private int type = 0;//0 备份 1 恢复

    private RelativeLayout last_month_layout;
    private RadioButton radio_bt_last_month;
    private RelativeLayout last_three_month_layout;
    private RadioButton radio_bt_last_three_month;
    private RelativeLayout last_half_a_year_layout;
    private RadioButton radio_bt_half_a_year;
    private RelativeLayout last_year_layout;
    private RadioButton radio_bt_last_year;
    private RelativeLayout all_layout;
    private RadioButton radio_bt_all;

    private RelativeLayout sync_now;
    private TextView text_sync_now;
    private int time_type = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.sms_sync_by_time);
        getIntentData();
        getToolbar().setTitle(R.string.choose_sms);
        initView();
    }

    private void getIntentData() {
        Intent i = getIntent();
        if (i != null) {
            type = i.getIntExtra("type", 0);
            time_type = i.getIntExtra("time_type", 1);
        }
    }

    public void initView() {
        last_month_layout = (RelativeLayout) findViewById(R.id.last_month_layout);
        radio_bt_last_month = (RadioButton) findViewById(R.id.radio_bt_last_month);
        last_three_month_layout = (RelativeLayout) findViewById(R.id.last_three_month_layout);
        radio_bt_last_three_month = (RadioButton) findViewById(R.id.radio_bt_last_three_month);
        last_half_a_year_layout = (RelativeLayout) findViewById(R.id.last_half_a_year_layout);
        radio_bt_half_a_year = (RadioButton) findViewById(R.id.radio_bt_half_a_year);
        last_year_layout = (RelativeLayout) findViewById(R.id.last_year_layout);
        radio_bt_last_year = (RadioButton) findViewById(R.id.radio_bt_last_year);
        all_layout = (RelativeLayout) findViewById(R.id.all_layout);
        radio_bt_all = (RadioButton) findViewById(R.id.radio_bt_all);
        sync_now = (RelativeLayout) findViewById(R.id.sync_now);
        text_sync_now = (TextView) findViewById(R.id.text_sync_now);

        if(type == 0){
            text_sync_now.setText(R.string.sync_to_cloud);
        } else {
            text_sync_now.setText(R.string.download_to_local);
        }

        selectType(time_type);

        last_month_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectType(1);
            }
        });
        last_three_month_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectType(2);
            }
        });
        last_half_a_year_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectType(3);
            }
        });
        last_year_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectType(4);
            }
        });
        all_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectType(5);
            }
        });

        sync_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickSyncNow();
            }
        });

    }

    private void ClickSyncNow(){

        int time_type  = 1;

        if(radio_bt_last_month.isChecked()){
            time_type = 1;
        } else if(radio_bt_last_three_month.isChecked()){
            time_type = 2;
        }else if(radio_bt_half_a_year.isChecked()){
            time_type = 3;
        }else if(radio_bt_last_year.isChecked()){
            time_type = 4;
        }else if(radio_bt_all.isChecked()){
            time_type = 5;
        }

        Intent i = new Intent();
        i.putExtra("is_sync_by_time",true);
        i.putExtra("type",type);
        i.putExtra("time_type",time_type);
        setResult(RESULT_OK,i);
        finish();

    }

    private void selectType(int type) {
        switch (type) {
            case 1://1个月
                radio_bt_last_month.setChecked(true);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(false);

//                radio_bt_last_month.setVisibility(View.VISIBLE);
//                radio_bt_last_three_month.setVisibility(View.INVISIBLE);
//                radio_bt_half_a_year.setVisibility(View.INVISIBLE);
//                radio_bt_last_year.setVisibility(View.INVISIBLE);
//                radio_bt_all.setVisibility(View.INVISIBLE);
                break;
            case 2://3个月
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(true);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(false);

//                radio_bt_last_month.setVisibility(View.INVISIBLE);
//                radio_bt_last_three_month.setVisibility(View.VISIBLE);
//                radio_bt_half_a_year.setVisibility(View.INVISIBLE);
//                radio_bt_last_year.setVisibility(View.INVISIBLE);
//                radio_bt_all.setVisibility(View.INVISIBLE);

                break;
            case 3://半年
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(true);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(false);

//                radio_bt_last_month.setVisibility(View.INVISIBLE);
//                radio_bt_last_three_month.setVisibility(View.INVISIBLE);
//                radio_bt_half_a_year.setVisibility(View.VISIBLE);
//                radio_bt_last_year.setVisibility(View.INVISIBLE);
//                radio_bt_all.setVisibility(View.INVISIBLE);

                break;
            case 4://一年
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(true);
                radio_bt_all.setChecked(false);

//                radio_bt_last_month.setVisibility(View.INVISIBLE);
//                radio_bt_last_three_month.setVisibility(View.INVISIBLE);
//                radio_bt_half_a_year.setVisibility(View.INVISIBLE);
//                radio_bt_last_year.setVisibility(View.VISIBLE);
//                radio_bt_all.setVisibility(View.INVISIBLE);

                break;
            case 5://全部
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(true);

//                radio_bt_last_month.setVisibility(View.INVISIBLE);
//                radio_bt_last_three_month.setVisibility(View.INVISIBLE);
//                radio_bt_half_a_year.setVisibility(View.INVISIBLE);
//                radio_bt_last_year.setVisibility(View.INVISIBLE);
//                radio_bt_all.setVisibility(View.VISIBLE);

                break;
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }


}
