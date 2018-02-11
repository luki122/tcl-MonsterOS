/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;
import mst.app.dialog.AlertDialog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-9.
 * Date and time dialog
 */
public class DateAndTimePickerDialogFragment extends DialogFragment {

    private DatePicker mDatePicker;

    private TimePicker mTimePicker;

    private String mDate;

    private String mTime;

    private OnPositiveButtonClickListener mOnPositiveButtonClickListener;

    private final String TAG = DateAndTimePickerDialogFragment.class.getSimpleName();

    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MeetingLog.i(TAG,"onCreateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View fragmentView = inflater.inflate(R.layout.layout_date_time_picker, null);
        builder.setView(fragmentView)
                // Add action buttons
                .setPositiveButton(R.string.Confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (mOnPositiveButtonClickListener != null) {
                                    mOnPositiveButtonClickListener.onClick(getTime(),getTag());
                                }
                            }
                        }).setNegativeButton(R.string.cancel, null);
        mDatePicker = (DatePicker) fragmentView.findViewById(R.id.date_picker);
        mTimePicker = (TimePicker) fragmentView.findViewById(R.id.time_picker);
        mDatePicker.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                mYear = year;
                mMonth = month;
                mDay = day;
            }
        });

        mTimePicker.setHour(mHour);
        mTimePicker.setMinute(mMinute);
        mTimePicker.setIs24HourView(true);

        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                mHour = hour;
                mMinute = minute;
            }
        });
        return builder.create();
    }

    public void setCurrentTime(long currentTime){
        MeetingLog.i(TAG,"setCurrentTime");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
    }

    public Long getTime() {
        mDate = new StringBuilder().append(mYear).append("-").append(mMonth+1).append("-")
                .append(mDay).toString();
        mTime = new StringBuilder().append(mHour).append(":").append(mMinute).toString();
        String time = new StringBuilder().append(mDate).append("\b\b").append(mTime).toString();
        return TimeFormatUtil.getDateFromString(time);
    }

    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener onPositiveButtonClickListener) {
        this.mOnPositiveButtonClickListener = onPositiveButtonClickListener;
    }

    public interface OnPositiveButtonClickListener {
        void onClick(Long time,String tag);
    }


}
