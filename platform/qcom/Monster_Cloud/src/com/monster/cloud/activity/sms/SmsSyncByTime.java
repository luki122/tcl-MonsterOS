package com.monster.cloud.activity.sms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.utils.SystemUtil;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.interfaces.statistics.IStatisticsUtil;
import com.tencent.tclsdk.utils.GetCountUtils;

import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;

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

    private int sms_num_1;
    private int sms_num_3;
    private int sms_num_6;
    private int sms_num_12;
    private int sms_num_all;

    private TextView text_last_month;
    private TextView text_last_three_month;
    private TextView text_last_half_a_year;
    private TextView text_last_year;
    private TextView text_all;
    private ProgressDialog getDataDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.setStatusBarColor(this,R.color.white);

        setMstContentView(R.layout.sms_sync_by_time);
        getIntentData();
        getToolbar().setTitle(R.string.choose_sms);
        initView();
        setClickEnable(false);
    }

    private void getIntentData() {
        Intent i = getIntent();
        if (i != null) {
            type = i.getIntExtra("type", 0);
            time_type = i.getIntExtra("time_type", 1);
        }
    }

    public void initView() {

        text_last_month = (TextView) findViewById(R.id.text_last_month);
        text_last_three_month = (TextView) findViewById(R.id.text_last_three_month);
        text_last_half_a_year = (TextView) findViewById(R.id.text_last_half_a_year);
        text_last_year = (TextView) findViewById(R.id.text_last_year);
        text_all = (TextView) findViewById(R.id.text_all);

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

        if (type == 0) {
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
        getSmsNum();
    }

    boolean is_local = false;

    private void getSmsNum() {


        if (type == 0) {//获取本地短信的数量
            is_local = true;
        } else {//获取云端短信的数量
            is_local= false;
        }

        getDataDialog = new ProgressDialog(this);
        getDataDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        getDataDialog.setMessage(getString(R.string.sync_ing));
        getDataDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long one = 1000L * 60 * 60 * 24 * 30 * 1;
                long three = 1000L * 60 * 60 * 24 * 30 * 3;
                long six = 1000L * 60 * 60 * 24 * 30 * 6;
                long year = 1000L * 60 * 60 * 24 * 30 * 12;
                //long all = 1000L * 60 * 60 * 24 * 30 * 12;

                sms_num_1 = GetCountUtils.getRecordNumOfSMS(SmsSyncByTime.this,System.currentTimeMillis() - one,is_local);
                sms_num_3 = GetCountUtils.getRecordNumOfSMS(SmsSyncByTime.this,System.currentTimeMillis() - three,is_local);
                sms_num_6 = GetCountUtils.getRecordNumOfSMS(SmsSyncByTime.this,System.currentTimeMillis() - six,is_local);
                sms_num_12 = GetCountUtils.getRecordNumOfSMS(SmsSyncByTime.this,System.currentTimeMillis() - year,is_local);
                sms_num_all = GetCountUtils.getRecordNumOfSMS(SmsSyncByTime.this,0l,is_local);
                m_handler.sendEmptyMessage(0);
            }
        }).start();

    }

    private Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (getDataDialog != null) {
                getDataDialog.dismiss();
            }

            text_last_month.setText(getString(R.string.last_month) + "(" + sms_num_1 + ")");
            text_last_three_month.setText(getString(R.string.last_three_month) + "(" + sms_num_3 + ")");
            text_last_half_a_year.setText(getString(R.string.last_half_a_year) + "(" + sms_num_6 + ")");
            text_last_year.setText(getString(R.string.last_year) + "(" + sms_num_12 + ")");
            text_all.setText(getString(R.string.all) + "(" + sms_num_all + ")");

            if (sms_num_1 <= 0 && sms_num_3 <= 0 && sms_num_6 <= 0 && sms_num_12 <= 0 && sms_num_all <= 0) {
                setClickEnable(false);
            } else {
                setClickEnable(true);
            }

            if (sms_num_1 <= 0) {
                last_month_layout.setVisibility(View.GONE);
                radio_bt_last_month.setChecked(false);
                if(time_type ==1){
                    radio_bt_last_three_month.setChecked(true);
                    time_type =2;
                }
            }

            if (sms_num_3 <= 0) {
                last_three_month_layout.setVisibility(View.GONE);
                radio_bt_last_three_month.setChecked(false);
                if(time_type ==2){
                    radio_bt_half_a_year.setChecked(true);
                    time_type =3;
                }
            }

            if (sms_num_6 <= 0) {
                last_half_a_year_layout.setVisibility(View.GONE);
                radio_bt_half_a_year.setChecked(false);
                if(time_type ==3){
                    radio_bt_last_year.setChecked(true);
                    time_type =4;
                }
            }

            if (sms_num_12 <= 0) {
                last_year_layout.setVisibility(View.GONE);
                radio_bt_last_year.setChecked(false);
                if(time_type ==4){
                    radio_bt_all.setChecked(true);
                    time_type =5;
                }
            }
            if (sms_num_all <= 0) {
                all_layout.setVisibility(View.GONE);
                radio_bt_all.setChecked(false);
            }

            if (!radio_bt_last_month.isChecked() && !radio_bt_last_three_month.isChecked() && !radio_bt_half_a_year.isChecked() && !radio_bt_last_year.isChecked() &&!radio_bt_all.isChecked() ) {
                if(sms_num_1>0){
                    radio_bt_last_month.setChecked(true);
                } else if(sms_num_3>0){
                    radio_bt_last_three_month.setChecked(true);
                } else if(sms_num_6>0){
                    radio_bt_half_a_year.setChecked(true);
                } else if(sms_num_12>0){
                    radio_bt_last_year.setChecked(true);
                } else if(sms_num_all>0){
                    radio_bt_all.setChecked(true);
                }
            }

            checkClickAble();
        }
    };

    private void checkClickAble(){
        if (radio_bt_last_month.isChecked() || radio_bt_last_three_month.isChecked() || radio_bt_half_a_year.isChecked() || radio_bt_last_year.isChecked() ||radio_bt_all.isChecked() ) {
            setClickEnable(true);
        } else {
            setClickEnable(false);
        }
    }

    private void setClickEnable(boolean is) {
        sync_now.setClickable(is);
        if (is) {
            sync_now.setAlpha(1.0f);
        } else {
            sync_now.setAlpha(0.3f);
        }
    }

    private void ClickSyncNow() {

        int time_type = 1;
        int count = 0;

        if (radio_bt_last_month.isChecked()) {
            time_type = 1;
            count = sms_num_1;
        } else if (radio_bt_last_three_month.isChecked()) {
            time_type = 2;
            count = sms_num_3;

        } else if (radio_bt_half_a_year.isChecked()) {
            time_type = 3;
            count = sms_num_6;

        } else if (radio_bt_last_year.isChecked()) {
            time_type = 4;
            count = sms_num_12;

        } else if (radio_bt_all.isChecked()) {
            time_type = 5;
            count = sms_num_all;

        }

        Intent i = new Intent();
        i.putExtra("is_sync_by_time", true);
        i.putExtra("type", type);
        i.putExtra("time_type", time_type);
        i.putExtra("count", count);
        setResult(RESULT_OK, i);
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
                break;
            case 2://3个月
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(true);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(false);

                break;
            case 3://半年
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(true);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(false);

                break;
            case 4://一年
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(true);
                radio_bt_all.setChecked(false);

                break;
            case 5://全部
                radio_bt_last_month.setChecked(false);
                radio_bt_last_three_month.setChecked(false);
                radio_bt_half_a_year.setChecked(false);
                radio_bt_last_year.setChecked(false);
                radio_bt_all.setChecked(true);


                break;
        }
        checkClickAble();
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }


}
