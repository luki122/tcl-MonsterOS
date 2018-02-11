package com.android.deskclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.PopupDialog;
import mst.widget.TimePicker;

import com.android.deskclock.Util.AppConst;
import com.android.deskclock.provider.Alarm;
import com.android.deskclock.provider.DaysOfWeek;
import com.android.deskclock.view.NumberPickerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SpinnerPopupDialog;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker.OnTimeChangedListener;


public class SetAlarmActivity extends MstActivity implements OnClickListener {

    // private TimePicker time_picker;
    private RelativeLayout set_repeat;
    private TextView repeat_date;
    private RelativeLayout set_ring;
    private TextView ring_data;
    private RelativeLayout set_remark;
    private TextView remark_data;
    private RelativeLayout set_remind_later;
    private Switch remind_later_onoff;

    // 照搬AlarmClockFragment那边的逻辑
    private int[] mDayOrder;

    private final int[] DAY_ORDER = new int[] { Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, };

    private Integer int1;
    private Integer int2;
    private Integer int3;
    private Integer int4;
    private Integer int5;
    private Integer int6;
    private Integer int7;
    private List<Integer> dayList = new ArrayList<Integer>();

    private boolean is_add_alarm = false;
    private Alarm mAlarm;

    private boolean isTime24;// 是否是２４小时制

    private RelativeLayout delete_alarm;
    private TimePicker time_picker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setStatusBarColor(this,R.color.white);
        setMstContentView(R.layout.set_alarm_activity);
        getToolbar().setTitle(R.string.str_set_alarm);
        initData();
        initView();
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null) {
            is_add_alarm = i.getBooleanExtra("is_add_alarm", false);
            mAlarm = i.getParcelableExtra("alarm");
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

    private void initView() {

        set_repeat = (RelativeLayout) findViewById(R.id.set_repeat);
        delete_alarm = (RelativeLayout) findViewById(R.id.delete_alarm);
        time_picker = (TimePicker) findViewById(R.id.time_picker);

        delete_alarm.setOnClickListener(this);

        repeat_date = (TextView) findViewById(R.id.repeat_date);
        set_ring = (RelativeLayout) findViewById(R.id.set_ring);
        ring_data = (TextView) findViewById(R.id.ring_data);
        set_remark = (RelativeLayout) findViewById(R.id.set_remark);
        remark_data = (TextView) findViewById(R.id.remark_data);
        set_remind_later = (RelativeLayout) findViewById(R.id.set_remind_later);
        remind_later_onoff = (Switch) findViewById(R.id.remind_later_onoff);

        set_repeat.setOnClickListener(this);
        set_ring.setOnClickListener(this);
        set_remark.setOnClickListener(this);
        set_remind_later.setOnClickListener(this);

        remind_later_onoff.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mAlarm.is_remaind_later = arg1;
            }
        });

        // time_picker.setIs24HourView(true);//24小时制

        if (is_add_alarm && mAlarm == null) {
            Date dt = new Date();
            int hour = dt.getHours();
            int min = dt.getMinutes();
            mAlarm = getDefaultAlarm(hour, min);
        }

        if (!is_add_alarm) {
            time_picker.setHour(mAlarm.hour);
            time_picker.setMinute(mAlarm.minutes);
            delete_alarm.setVisibility(View.VISIBLE);
        } else {
            delete_alarm.setVisibility(View.GONE);
        }

        // time_picker.setOnTimeChangedListener(new OnTimeChangedListener() {
        //
        // @Override
        // public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
        // mAlarm.hour = time_picker.getHour();
        // mAlarm.minutes = time_picker.getMinute();
        // }
        // });
        //
        // picker_hour.setOnValueChangedListener(new OnValueChangeListener() {
        //
        // @Override
        // public void onValueChange(NumberPickerView picker, int oldVal, int
        // newVal) {
        // mAlarm.hour =
        // }
        // });

        updateUIByAlarm();

        isTime24 = Utils.isTime24(this);
        if (isTime24) {
            time_picker.setIs24HourView(true);
        } else {
            time_picker.setIs24HourView(false);
        }
    }

    private void updateUIByAlarm() {
        updateRepeat();
        updateRingTone();
        updateRemark();
        updateRemindLater();
    }

    public void updateRemindLater() {
        remind_later_onoff.setChecked(mAlarm.is_remaind_later);
    }

    public void updateRemark() {
        if (mAlarm.label != null && mAlarm.label.length() > 0) {
            remark_data.setText(mAlarm.label);
        } else {
            remark_data.setText("");
        }
    }

    private void updateRepeat() {
        dayList.clear();
        if (mAlarm.isWorkDayAlarm) {
            repeat_date.setText(R.string.work_day);
        } else if (mAlarm.isNoWorkDayAlarm) {
            repeat_date.setText(R.string.no_work_day);
        } else if (mAlarm.daysOfWeek.isRepeating()) {// 更新重复的天数
            HashSet<Integer> setDays = mAlarm.daysOfWeek.getSetDays();
            String str = getString(R.string.str_week);
            for (int i = 0; i < 7; i++) {
                if (setDays.contains(mDayOrder[i])) {
                    str = str + getChineseDayById(mDayOrder[i]) + " ";
                    switch (i) {
                    case 0:
                        dayList.add(int1);
                        break;
                    case 1:
                        dayList.add(int2);
                        break;
                    case 2:
                        dayList.add(int3);
                        break;
                    case 3:
                        dayList.add(int4);
                        break;
                    case 4:
                        dayList.add(int5);
                        break;
                    case 5:
                        dayList.add(int6);
                        break;
                    case 6:
                        dayList.add(int7);
                        break;
                    }
                }
            }
            repeat_date.setText(str.substring(0, str.length() - 1));
        } else {
            repeat_date.setText(R.string.one_time);
        }
    }

    private void updateRingTone() {
        String ring_titel = getRingToneTitle(mAlarm.alert);
        ring_data.setText(ring_titel);
    }

    private String getChineseDayById(int id) {
        String str = "";
        switch (id) {
        case Calendar.SUNDAY:
            // str = getString(R.string.str_sunday);
            str = getString(R.string.str_sunday_short);
            break;
        case Calendar.MONDAY:
            // str = getString(R.string.str_monday);
            str = getString(R.string.str_monday_short);
            break;

        case Calendar.TUESDAY:
            // str = getString(R.string.str_tuesday);
            str = getString(R.string.str_tuesday_short);
            break;

        case Calendar.WEDNESDAY:
            // str = getString(R.string.str_wednesday);
            str = getString(R.string.str_wednesday_short);
            break;

        case Calendar.THURSDAY:
            // str = getString(R.string.str_thursday);
            str = getString(R.string.str_thursday_short);
            break;

        case Calendar.FRIDAY:
            // str = getString(R.string.str_friday);
            str = getString(R.string.str_friday_short);
            break;

        case Calendar.SATURDAY:
            // str = getString(R.string.str_saturday);
            str = getString(R.string.str_saturday_short);
            break;
        }
        return str;
    }

    private String getCustomeChineseDayById(int id) {
        String str = "";
        switch (id) {
        case Calendar.SUNDAY:
            str = getString(R.string.str_sunday);
            // str = getString(R.string.str_sunday_short);
            break;
        case Calendar.MONDAY:
            str = getString(R.string.str_monday);
            // str = getString(R.string.str_monday_short);
            break;

        case Calendar.TUESDAY:
            str = getString(R.string.str_tuesday);
            // str = getString(R.string.str_tuesday_short);
            break;

        case Calendar.WEDNESDAY:
            str = getString(R.string.str_wednesday);
            // str = getString(R.string.str_wednesday_short);
            break;

        case Calendar.THURSDAY:
            str = getString(R.string.str_thursday);
            // str = getString(R.string.str_thursday_short);
            break;

        case Calendar.FRIDAY:
            str = getString(R.string.str_friday);
            // str = getString(R.string.str_friday_short);
            break;

        case Calendar.SATURDAY:
            str = getString(R.string.str_saturday);
            // str = getString(R.string.str_saturday_short);
            break;
        }
        return str;
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        switch (id) {
        case R.id.set_repeat:
            showRepeatDialog();
            // testPopDialog();
            break;
        case R.id.set_ring:
            showRingDialog();
            break;
        case R.id.set_remark:
            showRemarkDialog();
            break;
        case R.id.set_remind_later:
            remind_later_onoff.setChecked(!remind_later_onoff.isChecked());
            break;

        // 自定义重复
//        case R.id.layout1:
//            check_1.setChecked(!check_1.isChecked());
//            break;
//        case R.id.layout2:
//            check_2.setChecked(!check_2.isChecked());
//            break;
//        case R.id.layout3:
//            check_3.setChecked(!check_3.isChecked());
//            break;
//        case R.id.layout4:
//            check_4.setChecked(!check_4.isChecked());
//            break;
//        case R.id.layout5:
//            check_5.setChecked(!check_5.isChecked());
//            break;
//        case R.id.layout6:
//            check_6.setChecked(!check_6.isChecked());
//            break;
//        case R.id.layout7:
//            check_7.setChecked(!check_7.isChecked());
//            break;
        case R.id.text_ok_layout:
            dayList.clear();
            dayList.addAll(dayListCustom);
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateDaysOfWeekByDayList();
            m_pop_dialog_repeat.dismiss();
            break;

        // 选择重复的对话框

        case R.id.one_time_layout:
            repeat_date.setText(R.string.one_time);
            dayList.clear();
            mAlarm.daysOfWeek.clearAllDays();
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateRepeat();

            if (repeat_dialog != null) {
                repeat_dialog.dismiss();
            }

            break;
        case R.id.every_day_layout:
            repeat_date.setText(R.string.every_day);
            selectEveryDay();
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateRepeat();
            if (repeat_dialog != null) {
                repeat_dialog.dismiss();
            }

            break;
        case R.id.mon_to_fir_layout:
            repeat_date.setText(R.string.mon_to_fir);
            selectMonToFri();
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateRepeat();
            if (repeat_dialog != null) {
                repeat_dialog.dismiss();
            }

            break;
        case R.id.work_day_layout:
            mAlarm.isWorkDayAlarm = true;
            mAlarm.isNoWorkDayAlarm = false;
            repeat_date.setText(R.string.work_day);
            selectMonToFri();
            updateRepeat();
            if (repeat_dialog != null) {
                repeat_dialog.dismiss();
            }

            break;
        case R.id.no_work_day_layout:
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = true;
            repeat_date.setText(R.string.no_work_day);
            selectSaSunay();
            updateRepeat();
            if (repeat_dialog != null) {
                repeat_dialog.dismiss();
            }

            break;
        case R.id.custom_layout:
            gotoCustomRepeat();
            if (repeat_dialog != null) {
                repeat_dialog.dismiss();
            }
            break;

        case R.id.delete_alarm:
            showDeleteDiaolog();
            break;
        }

    }

    private void showDeleteDiaolog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.delete_alarm)
                .setPositiveButton(R.string.time_picker_set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("alarm", mAlarm);
                        i.putExtras(bundle);
                        setResult(AppConst.RESULT_DELETE_ALARM, i);
                        finish();

                    }
                }).setNegativeButton(R.string.time_picker_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setMessage(R.string.delete_alarm_hint).create();
        alertDialog.show();
    }

    private void showRingDialog() {
        Uri oldRingtone = Alarm.NO_RINGTONE_URI.equals(mAlarm.alert) ? null : mAlarm.alert;
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, oldRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        startActivityForResult(intent, AppConst.REQUEST_CODE_RINGTONE);

    }

    private void showRemarkDialog() {
        final EditText mEdit = new EditText(this);
        String label = mAlarm.label;
        mEdit.setText(label);
        if (!TextUtils.isEmpty(label)) {
            mEdit.setSelection(label.length());
        }
        mEdit.setHint(getString(R.string.str_remark_limit_hint));
        mEdit.setBackgroundResource(com.mst.R.drawable.edit_text_material);
        mEdit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10) });
        mEdit.requestFocus();
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setView(mEdit)
                .setPositiveButton(R.string.time_picker_set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlarm.label = mEdit.getText().toString();
                        updateRemark();
                        hideKeyBoard(mEdit);
                    }
                }).setNegativeButton(R.string.time_picker_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyBoard(mEdit);
                    }
                }).setMessage(R.string.remark).create();
        alertDialog.show();
        mEdit.post(new Runnable() {
            @Override
            public void run() {
                showKeyBoard(mEdit);
            }
        });

    }

    public void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void showKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == AppConst.REQUEST_CODE_RINGTONE) {// 选择铃声返回
            saveRingtoneUri(data);
            return;
        }

        if (requestCode == AppConst.REQUEST_SET_CUSTOME_REPEAT) {// 自定义选择重复天数返回
            dayList = (List<Integer>) data.getSerializableExtra("dayList");
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateDaysOfWeekByDayList();
            return;
        }

        int result = data.getIntExtra("result", AppConst.RESULT_ONE_TIME);
        switch (result) {
        case AppConst.RESULT_ONE_TIME:
            repeat_date.setText(R.string.one_time);
            dayList.clear();
            mAlarm.daysOfWeek.clearAllDays();
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateRepeat();
            break;
        case AppConst.RESULT_EVERYDAY:
            repeat_date.setText(R.string.every_day);
            selectEveryDay();
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateRepeat();
            break;
        case AppConst.RESULT_MON_TO_FIR:
            repeat_date.setText(R.string.mon_to_fir);
            selectMonToFri();
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = false;
            updateRepeat();
            break;
        case AppConst.RESULT_WORK_DAY:
            mAlarm.isWorkDayAlarm = true;
            mAlarm.isNoWorkDayAlarm = false;
            repeat_date.setText(R.string.work_day);
            selectMonToFri();
            updateRepeat();
            break;
        case AppConst.RESULT_NO_WORK_DAY:
            mAlarm.isWorkDayAlarm = false;
            mAlarm.isNoWorkDayAlarm = true;
            repeat_date.setText(R.string.no_work_day);
            selectSaSunay();
            updateRepeat();
            break;
        case AppConst.RESULT_CUSTOM:
            gotoCustomRepeat();
            break;
        }

    }

    private void saveRingtoneUri(Intent intent) {
        if (intent == null) {
            return;
        }
        Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) {
            uri = Alarm.NO_RINGTONE_URI;
        }
        mAlarm.alert = uri;

        setDefaultRingtoneUri(uri);

        if (!AlarmUtils.hasPermissionToDisplayRingtoneTitle(this, uri)) {
            final String[] perms = { Manifest.permission.READ_EXTERNAL_STORAGE };
            requestPermissions(perms, AlarmClockFragment.REQUEST_CODE_PERMISSIONS);
        }
        updateRingTone();
    }

    private void setDefaultRingtoneUri(Uri uri) { // 保存最后选择的铃声作为默认铃声
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (uri == null) {
            sp.edit().remove(AlarmClockFragment.PREF_KEY_DEFAULT_ALARM_RINGTONE_URI).apply();
        } else {
            sp.edit().putString(AlarmClockFragment.PREF_KEY_DEFAULT_ALARM_RINGTONE_URI, uri.toString()).apply();
        }
    }

    public Alarm getDefaultAlarm(int hourOfDay, int minute) {
        Alarm a = new Alarm();
        a.alert = getDefaultRingtoneUri();
        if (a.alert == null) {
            a.alert = Uri.parse("content://settings/system/alarm_alert");
        }
        a.hour = hourOfDay;
        a.minutes = minute;
        a.enabled = true;
        a.label = getString(R.string.remark_hint);
        return a;
    }

    private Uri getDefaultRingtoneUri() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String ringtoneUriString = sp.getString(AlarmClockFragment.PREF_KEY_DEFAULT_ALARM_RINGTONE_URI, null);

        final Uri ringtoneUri;
        if (ringtoneUriString != null) {
            ringtoneUri = Uri.parse(ringtoneUriString);
        } else {
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        }
        return ringtoneUri;
    }

    private void ClickOK() {
        Intent i = new Intent();
        Bundle bundle = new Bundle();

        // if (isTime24) {
        // mAlarm.hour = Integer.parseInt(picker_hour.getContentByCurrValue());
        // } else {
        // int am_pm_value = picker_am_pm.getValue();
        // mAlarm.hour = Integer.parseInt(picker_hour.getContentByCurrValue());
        // if (am_pm_value == 1) {// pm
        // if (mAlarm.hour != 12) {
        // mAlarm.hour = mAlarm.hour + 12;
        // }
        // } else {// am
        // if (mAlarm.hour == 12) {
        // mAlarm.hour = mAlarm.hour + 12;
        // }
        // }
        // }
        //
        // if (mAlarm.hour == 24) {
        // mAlarm.hour = 0;
        // }
        // mAlarm.minutes =
        // Integer.parseInt(picker_minute.getContentByCurrValue());

        mAlarm.hour = time_picker.getHour();
        mAlarm.minutes = time_picker.getMinute();

        bundle.putParcelable("alarm", mAlarm);
        i.putExtras(bundle);
        i.putExtra("is_add_alarm", is_add_alarm);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    private void setDayOrder() {
        final int startDay = Utils.getZeroIndexedFirstDayOfWeek(this);
        mDayOrder = new int[DaysOfWeek.DAYS_IN_A_WEEK];
        for (int i = 0; i < DaysOfWeek.DAYS_IN_A_WEEK; ++i) {
            mDayOrder[i] = DAY_ORDER[(startDay + i) % 7];
        }
    }

    private void selectEveryDay() {
        mAlarm.daysOfWeek.setDaysOfWeek(true, mDayOrder);
    }

    private void selectMonToFri() {

        for (int i = 0; i < 7; i++) {
            int day = mDayOrder[i];
            if (day == Calendar.SUNDAY || day == Calendar.SATURDAY) {
                mAlarm.daysOfWeek.setDaysOfWeek(false, day);
            } else {
                mAlarm.daysOfWeek.setDaysOfWeek(true, day);
            }
        }
    }

    private void selectSaSunay() {
        for (int i = 0; i < 7; i++) {
            int day = mDayOrder[i];
            if (day == Calendar.SUNDAY || day == Calendar.SATURDAY) {
                mAlarm.daysOfWeek.setDaysOfWeek(true, day);
            } else {
                mAlarm.daysOfWeek.setDaysOfWeek(false, day);
            }
        }
    }

    private void updateDaysOfWeekByDayList() {
        if (dayList.size() == 0) {
            mAlarm.daysOfWeek.clearAllDays();
            repeat_date.setText(R.string.one_time);
            return;
        }
        mAlarm.daysOfWeek.clearAllDays();
        for (int i = 0; i < dayList.size(); i++) {
            int day = dayList.get(i);
            mAlarm.daysOfWeek.setDaysOfWeek(true, day);
        }
        updateRepeat();
    }

    private String getRingToneTitle(Uri uri) {
        if (Alarm.NO_RINGTONE_URI.equals(uri)) {
            return getResources().getString(R.string.silent_alarm_summary);
        }
        String title;
        if (!AlarmUtils.hasPermissionToDisplayRingtoneTitle(this, uri)) {
            title = getString(R.string.custom_ringtone);
        } else {
            final Ringtone ringTone = RingtoneManager.getRingtone(this, uri);
            if (ringTone == null) {
                LogUtils.i("No ringtone for uri %s", uri.toString());
                return null;
            }
            title = ringTone.getTitle(this);
        }
        return title;
    }

    /* 自定义重复dialog */
//    private CheckBox check_1;
//    private CheckBox check_2;
//    private CheckBox check_3;
//    private CheckBox check_4;
//    private CheckBox check_5;
//    private CheckBox check_6;
//    private CheckBox check_7;
//    private RelativeLayout text_cancle_layout;
//    private RelativeLayout text_ok_layout;
//    private RelativeLayout layout1;
//    private RelativeLayout layout2;
//    private RelativeLayout layout3;
//    private RelativeLayout layout4;
//    private RelativeLayout layout5;
//    private RelativeLayout layout6;
//    private RelativeLayout layout7;

    private SpinnerPopupDialog m_pop_dialog_repeat;

    private List<Integer> dayListCustom = new ArrayList<Integer>();

    private void gotoCustomRepeat() {
        // Intent i = new Intent();
        // i.setClass(this, SetCustomRepeatActivity.class);
        // i.putExtra("dayList", (Serializable) dayList);
        // startActivityForResult(i, AppConst.REQUEST_SET_CUSTOME_REPEAT);
        // overridePendingTransition(R.anim.dialog_anim_enter, 0);
        dayListCustom.clear();

        // if (m_pop_dialog_repeat != null) {
        // m_pop_dialog_repeat.show();
        // return;
        // }

        m_pop_dialog_repeat = new SpinnerPopupDialog(this,R.style.MyDialogSpinner);
        m_pop_dialog_repeat.setTitle(R.string.custom);

        CharSequence[] custom_str = { getCustomeChineseDayById(mDayOrder[0]), getCustomeChineseDayById(mDayOrder[1]),
                getCustomeChineseDayById(mDayOrder[2]), getCustomeChineseDayById(mDayOrder[3]),
                getCustomeChineseDayById(mDayOrder[4]), getCustomeChineseDayById(mDayOrder[5]) ,getCustomeChineseDayById(mDayOrder[6])};
        boolean[] selectedItem = { false, false, false, false, false, false, false };
        dayListCustom.addAll(dayList);

        for (int i = 0; i < dayList.size(); i++) {
            Integer getInt = dayList.get(i);
            if (getInt.equals(int1)) {
                selectedItem[0] = true;
            } else if (getInt.equals(int2)) {
                // check_2.setChecked(true);
                selectedItem[1] = true;
            } else if (getInt.equals(int3)) {
                // check_3.setChecked(true);
                selectedItem[2] = true;
            } else if (getInt.equals(int4)) {
                // check_4.setChecked(true);
                selectedItem[3] = true;
            } else if (getInt.equals(int5)) {
                // check_5.setChecked(true);
                selectedItem[4] = true;
            } else if (getInt.equals(int6)) {
                // check_6.setChecked(true);
                selectedItem[5] = true;
            } else if (getInt.equals(int7)) {
                // check_7.setChecked(true);
                selectedItem[6] = true;
            }
        }

        m_pop_dialog_repeat.setMultipleChoiceItems(custom_str, selectedItem,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int pos, boolean arg1) {
                        switch (pos) {

                        case 0:
                            if (arg1) {
                                if (!dayListCustom.contains(int1)) {
                                    dayListCustom.add(int1);
                                }
                            } else {
                                dayListCustom.remove(int1);
                            }
                            break;
                        case 1:
                            if (arg1) {
                                if (!dayListCustom.contains(int2)) {
                                    dayListCustom.add(int2);
                                }
                            } else {
                                dayListCustom.remove(int2);
                            }
                            break;
                        case 2:
                            if (arg1) {
                                if (!dayListCustom.contains(int3)) {
                                    dayListCustom.add(int3);
                                }
                            } else {
                                dayListCustom.remove(int3);
                            }
                            break;
                        case 3:
                            if (arg1) {
                                if (!dayListCustom.contains(int4)) {
                                    dayListCustom.add(int4);
                                }
                            } else {
                                dayListCustom.remove(int4);
                            }
                            break;
                        case 4:
                            if (arg1) {
                                if (!dayListCustom.contains(int5)) {
                                    dayListCustom.add(int5);
                                }
                            } else {
                                dayListCustom.remove(int5);
                            }
                            break;
                        case 5:
                            if (arg1) {
                                if (!dayListCustom.contains(int6)) {
                                    dayListCustom.add(int6);
                                }
                            } else {
                                dayListCustom.remove(int6);
                            }
                            break;
                        case 6:
                            if (arg1) {
                                if (!dayListCustom.contains(int7)) {
                                    dayListCustom.add(int7);
                                }
                            } else {
                                dayListCustom.remove(int7);
                            }
                            break;
                        }
                    }

                });

        m_pop_dialog_repeat.setPositiveButton(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dayList.clear();
                dayList.addAll(dayListCustom);
                mAlarm.isWorkDayAlarm = false;
                mAlarm.isNoWorkDayAlarm = false;
                updateDaysOfWeekByDayList();
                m_pop_dialog_repeat.dismiss();
            }
        });
        
        m_pop_dialog_repeat.setNegativeButton(new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                m_pop_dialog_repeat.dismiss();
            }
            
        });

        // m_pop_dialog_repeat.setOnClickListener(new
        // DialogInterface.OnClickListener() {
        //
        // @Override
        // public void onClick(DialogInterface arg0, int arg1) {
        // if (arg1 == PopupDialog.BUTTON_NEGATIVE) {
        // m_pop_dialog_repeat.dismiss();
        // } else if (arg1 == PopupDialog.BUTTON_POSITIVE) {
        // dayList.clear();
        // dayList.addAll(dayListCustom);
        // mAlarm.isWorkDayAlarm = false;
        // mAlarm.isNoWorkDayAlarm = false;
        // updateDaysOfWeekByDayList();
        // m_pop_dialog_repeat.dismiss();
        // }
        // }
        // });
        m_pop_dialog_repeat.show();

//        View custom_repeat = LayoutInflater.from(this).inflate(R.layout.set_custom_repeat, null);
//
//        // m_pop_dialog_repeat.setContentView(R.layout.set_repeat_dialog);
//        // m_pop_dialog_repeat.show();
//        m_pop_dialog_repeat.setCustomView(custom_repeat);
//        // m_pop_dialog_repeat.setContentView(custom_repeat);
//        m_pop_dialog_repeat.setPositiveButton(true);
//
//        layout1 = (RelativeLayout) custom_repeat.findViewById(R.id.layout1);
//        TextView text_1 = (TextView) custom_repeat.findViewById(R.id.text_1);
//        check_1 = (CheckBox) custom_repeat.findViewById(R.id.check_1);
//
//        layout2 = (RelativeLayout) custom_repeat.findViewById(R.id.layout2);
//        TextView text_2 = (TextView) custom_repeat.findViewById(R.id.text_2);
//        check_2 = (CheckBox) custom_repeat.findViewById(R.id.check_2);
//
//        layout3 = (RelativeLayout) custom_repeat.findViewById(R.id.layout3);
//        TextView text_3 = (TextView) custom_repeat.findViewById(R.id.text_3);
//        check_3 = (CheckBox) custom_repeat.findViewById(R.id.check_3);
//
//        layout4 = (RelativeLayout) custom_repeat.findViewById(R.id.layout4);
//        TextView text_4 = (TextView) custom_repeat.findViewById(R.id.text_4);
//        check_4 = (CheckBox) custom_repeat.findViewById(R.id.check_4);
//
//        layout5 = (RelativeLayout) custom_repeat.findViewById(R.id.layout5);
//        TextView text_5 = (TextView) custom_repeat.findViewById(R.id.text_5);
//        check_5 = (CheckBox) custom_repeat.findViewById(R.id.check_5);
//
//        layout6 = (RelativeLayout) custom_repeat.findViewById(R.id.layout6);
//        TextView text_6 = (TextView) custom_repeat.findViewById(R.id.text_6);
//        check_6 = (CheckBox) custom_repeat.findViewById(R.id.check_6);
//
//        layout7 = (RelativeLayout) custom_repeat.findViewById(R.id.layout7);
//        TextView text_7 = (TextView) custom_repeat.findViewById(R.id.text_7);
//        check_7 = (CheckBox) custom_repeat.findViewById(R.id.check_7);
//
//        // RelativeLayout main_layout = (RelativeLayout)
//        // findViewById(R.id.main_layout);
//        text_cancle_layout = (RelativeLayout) custom_repeat.findViewById(R.id.text_cancle_layout);
//        text_ok_layout = (RelativeLayout) custom_repeat.findViewById(R.id.text_ok_layout);
//
//        setTextById(text_1, mDayOrder[0]);
//        setTextById(text_2, mDayOrder[1]);
//        setTextById(text_3, mDayOrder[2]);
//        setTextById(text_4, mDayOrder[3]);
//        setTextById(text_5, mDayOrder[4]);
//        setTextById(text_6, mDayOrder[5]);
//        setTextById(text_7, mDayOrder[6]);
//
//        layout1.setOnClickListener(this);
//        layout2.setOnClickListener(this);
//        layout3.setOnClickListener(this);
//        layout4.setOnClickListener(this);
//        layout5.setOnClickListener(this);
//        layout6.setOnClickListener(this);
//        layout7.setOnClickListener(this);
//        text_cancle_layout.setOnClickListener(this);
//        text_ok_layout.setOnClickListener(this);
//
//        check_1.setOnCheckedChangeListener(this);
//        check_2.setOnCheckedChangeListener(this);
//        check_3.setOnCheckedChangeListener(this);
//        check_4.setOnCheckedChangeListener(this);
//        check_5.setOnCheckedChangeListener(this);
//        check_6.setOnCheckedChangeListener(this);
//        check_7.setOnCheckedChangeListener(this);
//
//        dayListCustom.addAll(dayList);
//
//        for (int i = 0; i < dayList.size(); i++) {
//            Integer getInt = dayList.get(i);
//            if (getInt.equals(int1)) {
//                check_1.setChecked(true);
//            } else if (getInt.equals(int2)) {
//                check_2.setChecked(true);
//            } else if (getInt.equals(int3)) {
//                check_3.setChecked(true);
//            } else if (getInt.equals(int4)) {
//                check_4.setChecked(true);
//            } else if (getInt.equals(int5)) {
//                check_5.setChecked(true);
//            } else if (getInt.equals(int6)) {
//                check_6.setChecked(true);
//            } else if (getInt.equals(int7)) {
//                check_7.setChecked(true);
//            }
//        }
    }

//    @Override
//    public void onCheckedChanged(CompoundButton v, boolean arg1) {
//        int id = v.getId();
//        switch (id) {
//        case R.id.check_1:
//            if (arg1) {
//                if (!dayListCustom.contains(int1)) {
//                    dayListCustom.add(int1);
//                }
//            } else {
//                dayListCustom.remove(int1);
//            }
//            break;
//        case R.id.check_2:
//            if (arg1) {
//                if (!dayListCustom.contains(int2)) {
//                    dayListCustom.add(int2);
//                }
//            } else {
//                dayListCustom.remove(int2);
//            }
//            break;
//        case R.id.check_3:
//            if (arg1) {
//                if (!dayListCustom.contains(int3)) {
//                    dayListCustom.add(int3);
//                }
//            } else {
//                dayListCustom.remove(int3);
//            }
//            break;
//        case R.id.check_4:
//            if (arg1) {
//                if (!dayListCustom.contains(int4)) {
//                    dayListCustom.add(int4);
//                }
//            } else {
//                dayListCustom.remove(int4);
//            }
//            break;
//        case R.id.check_5:
//            if (arg1) {
//                if (!dayListCustom.contains(int5)) {
//                    dayListCustom.add(int5);
//                }
//            } else {
//                dayListCustom.remove(int5);
//            }
//            break;
//        case R.id.check_6:
//            if (arg1) {
//                if (!dayListCustom.contains(int6)) {
//                    dayListCustom.add(int6);
//                }
//            } else {
//                dayListCustom.remove(int6);
//            }
//            break;
//        case R.id.check_7:
//            if (arg1) {
//                if (!dayListCustom.contains(int7)) {
//                    dayListCustom.add(int7);
//                }
//            } else {
//                dayListCustom.remove(int7);
//            }
//            break;
//        }
//    }

    private void setTextById(TextView textview, int id) {
        textview.setText(getCustomeChineseDayById(id));
    }

    /*
     * 选择重复的对话框
     */

    // private RelativeLayout one_time_layout;
    // private RelativeLayout every_day_layout;
    // private RelativeLayout mon_to_fir_layout;
    // private RelativeLayout work_day_layout;
    // private RelativeLayout no_work_day_layout;
    // private RelativeLayout custom_layout;
    //
    // private RadioButton img_one_time_select;
    // private RadioButton img_ervery_day_select;
    // private RadioButton img_mon_to_fri_select;
    // private RadioButton img_work_day_select;
    // private RadioButton img_no_work_day_select;
    // private RadioButton img_custom_select;

    // private PopupDialog repeat_dialog;

    private SpinnerPopupDialog repeat_dialog;

    private void showRepeatDialog() {

        // View m_view =
        // LayoutInflater.from(this).inflate(R.layout.set_repeat_dialog, null);

        repeat_dialog = new SpinnerPopupDialog(this,R.style.MyDialogSpinner);
        // one_time_layout = (RelativeLayout)
        // m_view.findViewById(R.id.one_time_layout);
        // every_day_layout = (RelativeLayout)
        // m_view.findViewById(R.id.every_day_layout);
        // mon_to_fir_layout = (RelativeLayout)
        // m_view.findViewById(R.id.mon_to_fir_layout);
        // work_day_layout = (RelativeLayout)
        // m_view.findViewById(R.id.work_day_layout);
        // no_work_day_layout = (RelativeLayout)
        // m_view.findViewById(R.id.no_work_day_layout);
        // custom_layout = (RelativeLayout)
        // m_view.findViewById(R.id.custom_layout);

        // one_time_layout.setOnClickListener(this);
        // every_day_layout.setOnClickListener(this);
        // mon_to_fir_layout.setOnClickListener(this);
        // work_day_layout.setOnClickListener(this);
        // no_work_day_layout.setOnClickListener(this);
        // custom_layout.setOnClickListener(this);
        //
        // img_one_time_select = (RadioButton)
        // m_view.findViewById(R.id.img_one_time_select);
        // img_ervery_day_select = (RadioButton)
        // m_view.findViewById(R.id.img_ervery_day_select);
        // img_mon_to_fri_select = (RadioButton)
        // m_view.findViewById(R.id.img_mon_to_fri_select);
        // img_work_day_select = (RadioButton)
        // m_view.findViewById(R.id.img_work_day_select);
        // img_no_work_day_select = (RadioButton)
        // m_view.findViewById(R.id.img_no_work_day_select);
        // img_custom_select = (RadioButton)
        // m_view.findViewById(R.id.img_custom_select);

        repeat_dialog.setTitle(R.string.alarm_repeat);
        CharSequence[] repeat_strs = { getString(R.string.one_time), getString(R.string.every_day),
                getString(R.string.mon_to_fir), getString(R.string.work_day_dec), getString(R.string.no_work_day_dec),
                getString(R.string.custom) };

        int select_pos = 0;
        if (mAlarm.isWorkDayAlarm) {
            // img_work_day_select.setVisibility(View.VISIBLE);
            // img_work_day_select.setChecked(true);
            select_pos = 3;
        } else if (mAlarm.isNoWorkDayAlarm) {
            // img_no_work_day_select.setVisibility(View.VISIBLE);
            // img_no_work_day_select.setChecked(true);
            select_pos = 4;

        } else if (dayList.size() == 0) {
            // img_one_time_select.setVisibility(View.VISIBLE);
            // img_one_time_select.setChecked(true);
            select_pos = 0;

        } else if (isSelectEveryDay()) {
            // img_ervery_day_select.setVisibility(View.VISIBLE);
            // img_ervery_day_select.setChecked(true);
            select_pos = 1;

        } else if (isSelectMonToFirDay()) {
            // img_mon_to_fri_select.setVisibility(View.VISIBLE);
            // img_mon_to_fri_select.setChecked(true);
            select_pos = 2;

        } else {
            // img_custom_select.setVisibility(View.VISIBLE);
            // img_custom_select.setChecked(true);
            select_pos = 5;
        }

        repeat_dialog.setSingleChoiceItems(repeat_strs, select_pos, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                switch (arg1) {
                case 0:
                    repeat_date.setText(R.string.one_time);
                    dayList.clear();
                    mAlarm.daysOfWeek.clearAllDays();
                    mAlarm.isWorkDayAlarm = false;
                    mAlarm.isNoWorkDayAlarm = false;
                    updateRepeat();

                    if (repeat_dialog != null) {
                        repeat_dialog.dismiss();
                    }
                    break;
                case 1:
                    repeat_date.setText(R.string.every_day);
                    selectEveryDay();
                    mAlarm.isWorkDayAlarm = false;
                    mAlarm.isNoWorkDayAlarm = false;
                    updateRepeat();
                    if (repeat_dialog != null) {
                        repeat_dialog.dismiss();
                    }
                    break;
                case 2:
                    repeat_date.setText(R.string.mon_to_fir);
                    selectMonToFri();
                    mAlarm.isWorkDayAlarm = false;
                    mAlarm.isNoWorkDayAlarm = false;
                    updateRepeat();
                    if (repeat_dialog != null) {
                        repeat_dialog.dismiss();
                    }
                    break;
                case 3:
                    mAlarm.isWorkDayAlarm = true;
                    mAlarm.isNoWorkDayAlarm = false;
                    repeat_date.setText(R.string.work_day);
                    selectMonToFri();
                    updateRepeat();
                    if (repeat_dialog != null) {
                        repeat_dialog.dismiss();
                    }
                    break;
                case 4:
                    mAlarm.isWorkDayAlarm = false;
                    mAlarm.isNoWorkDayAlarm = true;
                    repeat_date.setText(R.string.no_work_day);
                    selectSaSunay();
                    updateRepeat();
                    if (repeat_dialog != null) {
                        repeat_dialog.dismiss();
                    }
                    break;
                case 5:
                    gotoCustomRepeat();
                    if (repeat_dialog != null) {
                        repeat_dialog.dismiss();
                    }
                    break;
                }
            }
        });
        
        repeat_dialog.setNegativeButton(new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                repeat_dialog.dismiss();
            }
            
        });

        repeat_dialog.show();
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

    @Override
    protected void initialUI(Bundle savedInstanceState) {
        super.initialUI(savedInstanceState);
        inflateToolbarMenu(R.menu.set_alarm_menu);
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_ok:
            ClickOK();
            break;
        default:
            break;
        }
        return super.onMenuItemClick(item);
    }

}
