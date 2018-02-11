package com.android.deskclock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mst.app.MstActivity;

import com.android.deskclock.provider.DaysOfWeek;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SetCustomRepeatActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
    private int[] mDayOrder;
    private final int[] DAY_ORDER = new int[] { Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };

    private RelativeLayout layout1;
    private TextView text_1;
    private CheckBox check_1;

    private RelativeLayout layout2;
    private TextView text_2;
    private CheckBox check_2;

    private RelativeLayout layout3;
    private TextView text_3;
    private CheckBox check_3;

    private RelativeLayout layout4;
    private TextView text_4;
    private CheckBox check_4;

    private RelativeLayout layout5;
    private TextView text_5;
    private CheckBox check_5;

    private RelativeLayout layout6;
    private TextView text_6;
    private CheckBox check_6;

    private RelativeLayout layout7;
    private TextView text_7;
    private CheckBox check_7;

    private Integer int1;
    private Integer int2;
    private Integer int3;
    private Integer int4;
    private Integer int5;
    private Integer int6;
    private Integer int7;

    private List<Integer> dayList = new ArrayList<Integer>();

    private RelativeLayout text_cancle_layout;
    private RelativeLayout text_ok_layout;
    private RelativeLayout main_layout;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
//        setContentView(R.layout.set_custom_repeat);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        setContentView(R.layout.set_custom_repeat);
        initData();
        initView();
        updateCheckBoxByDayList();
        
    }

    private void initView() {
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        text_cancle_layout = (RelativeLayout) findViewById(R.id.text_cancle_layout);
        text_ok_layout = (RelativeLayout) findViewById(R.id.text_ok_layout);

        layout1 = (RelativeLayout) findViewById(R.id.layout1);
        text_1 = (TextView) findViewById(R.id.text_1);
        check_1 = (CheckBox) findViewById(R.id.check_1);

        layout2 = (RelativeLayout) findViewById(R.id.layout2);
        text_2 = (TextView) findViewById(R.id.text_2);
        check_2 = (CheckBox) findViewById(R.id.check_2);

        layout3 = (RelativeLayout) findViewById(R.id.layout3);
        text_3 = (TextView) findViewById(R.id.text_3);
        check_3 = (CheckBox) findViewById(R.id.check_3);

        layout4 = (RelativeLayout) findViewById(R.id.layout4);
        text_4 = (TextView) findViewById(R.id.text_4);
        check_4 = (CheckBox) findViewById(R.id.check_4);

        layout5 = (RelativeLayout) findViewById(R.id.layout5);
        text_5 = (TextView) findViewById(R.id.text_5);
        check_5 = (CheckBox) findViewById(R.id.check_5);

        layout6 = (RelativeLayout) findViewById(R.id.layout6);
        text_6 = (TextView) findViewById(R.id.text_6);
        check_6 = (CheckBox) findViewById(R.id.check_6);

        layout7 = (RelativeLayout) findViewById(R.id.layout7);
        text_7 = (TextView) findViewById(R.id.text_7);
        check_7 = (CheckBox) findViewById(R.id.check_7);

        setTextById(text_1, mDayOrder[0]);
        setTextById(text_2, mDayOrder[1]);
        setTextById(text_3, mDayOrder[2]);
        setTextById(text_4, mDayOrder[3]);
        setTextById(text_5, mDayOrder[4]);
        setTextById(text_6, mDayOrder[5]);
        setTextById(text_7, mDayOrder[6]);

        layout1.setOnClickListener(this);
        layout2.setOnClickListener(this);
        layout3.setOnClickListener(this);
        layout4.setOnClickListener(this);
        layout5.setOnClickListener(this);
        layout6.setOnClickListener(this);
        layout7.setOnClickListener(this);
        text_cancle_layout.setOnClickListener(this);
        text_ok_layout.setOnClickListener(this);
        main_layout.setOnClickListener(this);

        check_1.setOnCheckedChangeListener(this);
        check_2.setOnCheckedChangeListener(this);
        check_3.setOnCheckedChangeListener(this);
        check_4.setOnCheckedChangeListener(this);
        check_5.setOnCheckedChangeListener(this);
        check_6.setOnCheckedChangeListener(this);
        check_7.setOnCheckedChangeListener(this);
    }

    private void initData() {

        Intent i = getIntent();
        if (i != null) {
            dayList = (List<Integer>) i.getSerializableExtra("dayList");
        }

        setDayOrder();
        int1 = mDayOrder[0];
        int2 = mDayOrder[1];
        int3 = mDayOrder[2];
        int4 = mDayOrder[3];
        int5 = mDayOrder[4];
        int6 = mDayOrder[5];
        int7 = mDayOrder[6];
    }

    private void updateCheckBoxByDayList() {
        for (int i = 0; i < dayList.size(); i++) {
            Integer getInt = dayList.get(i);
            if (getInt.equals(int1)) {
                check_1.setChecked(true);
            } else if (getInt.equals(int2)) {
                check_2.setChecked(true);
            } else if (getInt.equals(int3)) {
                check_3.setChecked(true);
            } else if (getInt.equals(int4)) {
                check_4.setChecked(true);
            } else if (getInt.equals(int5)) {
                check_5.setChecked(true);
            } else if (getInt.equals(int6)) {
                check_6.setChecked(true);
            } else if (getInt.equals(int7)) {
                check_7.setChecked(true);
            }
        }
    }

    private void setDayOrder() {
        final int startDay = Utils.getZeroIndexedFirstDayOfWeek(this);
        mDayOrder = new int[DaysOfWeek.DAYS_IN_A_WEEK];
        for (int i = 0; i < DaysOfWeek.DAYS_IN_A_WEEK; ++i) {
            mDayOrder[i] = DAY_ORDER[(startDay + i) % 7];
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.layout1:
            check_1.setChecked(!check_1.isChecked());
            break;
        case R.id.layout2:
            check_2.setChecked(!check_2.isChecked());
            break;
        case R.id.layout3:
            check_3.setChecked(!check_3.isChecked());
            break;
        case R.id.layout4:
            check_4.setChecked(!check_4.isChecked());
            break;
        case R.id.layout5:
            check_5.setChecked(!check_5.isChecked());
            break;
        case R.id.layout6:
            check_6.setChecked(!check_6.isChecked());
            break;
        case R.id.layout7:
            check_7.setChecked(!check_7.isChecked());
            break;
        case R.id.main_layout:
        case R.id.text_cancle_layout:
            finish();
            break;
        case R.id.text_ok_layout:
            clickOK();
            break;
            
        }
    }

    private void setTextById(TextView textview, int id) {
        textview.setText(getChineseDayById(id));
    }

    private String getChineseDayById(int id) {
        String str = "";
        switch (id) {
        case Calendar.SUNDAY:
            str = "周日";
            break;
        case Calendar.MONDAY:
            str = "周一";
            break;

        case Calendar.TUESDAY:
            str = "周二";
            break;

        case Calendar.WEDNESDAY:
            str = "周三";
            break;

        case Calendar.THURSDAY:
            str = "周四";
            break;

        case Calendar.FRIDAY:
            str = "周五";
            break;

        case Calendar.SATURDAY:
            str = "周六";
            break;
        }
        return str;
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean arg1) {
        int id = v.getId();
        switch (id) {
        case R.id.check_1:
            if (arg1) {
                if (!dayList.contains(int1)) {
                    dayList.add(int1);
                }
            } else {
                dayList.remove(int1);
            }
            break;
        case R.id.check_2:
            if (arg1) {
                if (!dayList.contains(int2)) {
                    dayList.add(int2);
                }
            } else {
                dayList.remove(int2);
            }
            break;
        case R.id.check_3:
            if (arg1) {
                if (!dayList.contains(int3)) {
                    dayList.add(int3);
                }
            } else {
                dayList.remove(int3);
            }
            break;
        case R.id.check_4:
            if (arg1) {
                if (!dayList.contains(int4)) {
                    dayList.add(int4);
                }
            } else {
                dayList.remove(int4);
            }
            break;
        case R.id.check_5:
            if (arg1) {
                if (!dayList.contains(int5)) {
                    dayList.add(int5);
                }
            } else {
                dayList.remove(int5);
            }
            break;
        case R.id.check_6:
            if (arg1) {
                if (!dayList.contains(int6)) {
                    dayList.add(int6);
                }
            } else {
                dayList.remove(int6);
            }
            break;
        case R.id.check_7:
            if (arg1) {
                if (!dayList.contains(int7)) {
                    dayList.add(int7);
                }
            } else {
                dayList.remove(int7);
            }
            break;
        }
    }

    private void clickOK() {
        Intent i = new Intent();
        i.putExtra("dayList", (Serializable) dayList);
        setResult(RESULT_OK, i);
        finish();
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.dialog_anim_exit);
    }


}
