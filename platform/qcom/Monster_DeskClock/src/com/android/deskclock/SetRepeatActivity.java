package com.android.deskclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mst.app.MstActivity;

import com.android.deskclock.Util.AppConst;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SetRepeatActivity extends MstActivity implements OnClickListener {

    private RelativeLayout main_layout;
    private RelativeLayout one_time_layout;
    private RelativeLayout every_day_layout;
    private RelativeLayout mon_to_fir_layout;
    private RelativeLayout work_day_layout;
    private RelativeLayout no_work_day_layout;
    private RelativeLayout custom_layout;
    private List<Integer> dayList = new ArrayList<Integer>();
//    private TextView text_one_time;
//    private TextView text_every_day;
//    private TextView text_mon_to_fri;
//    private TextView text_work_day;
//    private TextView text_no_work_day;
//    private TextView text_custom;
    
    private ImageView img_one_time_select;
    private ImageView img_ervery_day_select;
    private ImageView img_mon_to_fri_select;
    private ImageView img_work_day_select;
    private ImageView img_no_work_day_select;
    private ImageView img_custom_select;

    private boolean isWorkDayAlarm = false;
    private boolean isNoWorkDayAlarm = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        // setContentView(R.layout.set_repeat_dialog);
        // getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);

        getToolbar().setVisibility(View.GONE);
        setMstContentView(R.layout.set_repeat_dialog);
        initView();
        initData();
        updateSelect();
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null) {
            dayList = (List<Integer>) i.getSerializableExtra("dayList");
            isWorkDayAlarm = i.getBooleanExtra("isWorkDayAlarm", false);
            isNoWorkDayAlarm = i.getBooleanExtra("isNoWorkDayAlarm", false);
        }
    }

    private void updateSelect() {
        if (isWorkDayAlarm) {
//            text_work_day.setTextColor(getColor(R.color.text_ok_color));
            img_work_day_select.setVisibility(View.VISIBLE);
        } else if (isNoWorkDayAlarm) {
//            text_no_work_day.setTextColor(getColor(R.color.text_ok_color));
            img_no_work_day_select.setVisibility(View.VISIBLE);
        } else if (dayList.size() == 0) {
//            text_one_time.setTextColor(getColor(R.color.text_ok_color));
            img_one_time_select.setVisibility(View.VISIBLE);
        } else if (isSelectEveryDay()) {
//            text_every_day.setTextColor(getColor(R.color.text_ok_color));
            img_ervery_day_select.setVisibility(View.VISIBLE);
        } else if (isSelectMonToFirDay()) {
//            text_mon_to_fri.setTextColor(getColor(R.color.text_ok_color));
            img_mon_to_fri_select.setVisibility(View.VISIBLE);
        } else {
//            text_custom.setTextColor(getColor(R.color.text_ok_color));
            img_custom_select.setVisibility(View.VISIBLE);
        }
    }

    private boolean isSelectMonToFirDay() {
        if (!dayList.contains(Calendar.SUNDAY) && dayList.contains(Calendar.MONDAY)
                && dayList.contains(Calendar.TUESDAY) && dayList.contains(Calendar.WEDNESDAY)
                && dayList.contains(Calendar.THURSDAY) && dayList.contains(Calendar.FRIDAY)
                && !dayList.contains(Calendar.SATURDAY)) {
            return true;
        }
        return false;
    }

    private boolean isSelectEveryDay() {
        if (dayList.contains(Calendar.SUNDAY) && dayList.contains(Calendar.MONDAY)
                && dayList.contains(Calendar.TUESDAY) && dayList.contains(Calendar.WEDNESDAY)
                && dayList.contains(Calendar.THURSDAY) && dayList.contains(Calendar.FRIDAY)
                && dayList.contains(Calendar.SATURDAY)) {
            return true;
        }
        return false;
    }

    private void initView() {
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        one_time_layout = (RelativeLayout) findViewById(R.id.one_time_layout);
        every_day_layout = (RelativeLayout) findViewById(R.id.every_day_layout);
        mon_to_fir_layout = (RelativeLayout) findViewById(R.id.mon_to_fir_layout);
        work_day_layout = (RelativeLayout) findViewById(R.id.work_day_layout);
        no_work_day_layout = (RelativeLayout) findViewById(R.id.no_work_day_layout);
        custom_layout = (RelativeLayout) findViewById(R.id.custom_layout);

//        text_one_time = (TextView) findViewById(R.id.text_one_time);
//        text_every_day = (TextView) findViewById(R.id.text_every_day);
//        text_mon_to_fri = (TextView) findViewById(R.id.text_mon_to_fri);
//        text_work_day = (TextView) findViewById(R.id.text_work_day);
//        text_no_work_day = (TextView) findViewById(R.id.text_no_work_day);
//        text_custom = (TextView) findViewById(R.id.text_custom);

        main_layout.setOnClickListener(this);
        one_time_layout.setOnClickListener(this);
        every_day_layout.setOnClickListener(this);
        mon_to_fir_layout.setOnClickListener(this);
        work_day_layout.setOnClickListener(this);
        no_work_day_layout.setOnClickListener(this);
        custom_layout.setOnClickListener(this);
        
        img_one_time_select = (ImageView)findViewById(R.id.img_one_time_select);
        img_ervery_day_select = (ImageView)findViewById(R.id.img_ervery_day_select);
        img_mon_to_fri_select = (ImageView)findViewById(R.id.img_mon_to_fri_select);
        img_work_day_select = (ImageView)findViewById(R.id.img_work_day_select);
        img_no_work_day_select = (ImageView)findViewById(R.id.img_no_work_day_select);
        img_custom_select = (ImageView)findViewById(R.id.img_custom_select);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        switch (id) {
        case R.id.main_layout:
            finish();
            break;
        case R.id.one_time_layout:
            setIntentResult(AppConst.RESULT_ONE_TIME);
            break;
        case R.id.every_day_layout:
            setIntentResult(AppConst.RESULT_EVERYDAY);

            break;
        case R.id.mon_to_fir_layout:
            setIntentResult(AppConst.RESULT_MON_TO_FIR);

            break;
        case R.id.work_day_layout:
            setIntentResult(AppConst.RESULT_WORK_DAY);

            break;
        case R.id.no_work_day_layout:
            setIntentResult(AppConst.RESULT_NO_WORK_DAY);

            break;
        case R.id.custom_layout:
            setIntentResult(AppConst.RESULT_CUSTOM);

            break;
        }
    }

    private void setIntentResult(int result) {
        Intent i = new Intent();
        i.putExtra("result", result);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.dialog_anim_exit);
    }

}
