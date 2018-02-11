package com.mst.wallpaper.widget;

import java.util.Calendar;
import java.util.Date;

import com.mst.wallpaper.utils.LunarUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mst.wallpaper.R;
public class TimeWidget extends LinearLayout {

    private static final String TAG = "TimeWidget";
    
    private static final String[] sTimeArray = {
    	"06:00","08:00","10:00","12:00"
    	,"14:00","16:00","18:00","20:00"
    	,"22:00","00:00","02:00","04:00"
    };

    private static final String DATE_FORMAT = "MM-dd";
    private static final String DATE_MONTH_FORMAT = "MM";
    private static final String DATE_DAY_FORMAT = "dd";
    private static final boolean USE_UPPER_CASE = true;

    public static final int LOCK_ICON = 0; 
    public static final int CHARGING_ICON = 0;
    public static final int BATTERY_LOW_ICON = 0; 

    private CharSequence mDateFormatString;

    private TextView mDateMonthView;
    private TextView mDateDivView;
    private TextView mDateDayView;
    private TextView mAlarmStatusView;

    private TextView mLunarDateView;
    private TextView mWeekView;
    private TextView mTimeView;
    private Typeface mDateFace;
    private Typeface mWeekFace;

    private Context mContext;


    public TimeWidget(Context context) {
        this(context, null);
    }

    public TimeWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources res = getContext().getResources();
        mDateFormatString = DATE_FORMAT;
        mDateMonthView = ( TextView ) findViewById(R.id.date_month);
        mDateDivView = ( TextView ) findViewById(R.id.date_div);
        mDateDayView = ( TextView ) findViewById(R.id.date_day);
        mAlarmStatusView = ( TextView ) findViewById(R.id.alarm_status);
        mWeekView = ( TextView ) findViewById(R.id.week);
        mTimeView = (TextView)findViewById(R.id.time_text);
        mDateDivView.setText("-");

        // Required to get Marquee to work.
        final View marqueeViews[] = {mDateMonthView, mDateDayView, mWeekView, mAlarmStatusView};
        for (int i = 0; i < marqueeViews.length; i++) {
            View v = marqueeViews[i];
            if (v == null) {
                throw new RuntimeException("Can't find widget at index " + i);
            }
            v.setSelected(true);
        }
        refresh(0);
    }

    public void refresh(int index) {
        refreshDate(index);
        refreshWeek(index);
        refreshTime(index);
    }
    
    private void refreshTime(int index) {
		// TODO Auto-generated method stub
		mTimeView.setText(sTimeArray[index]);
	}

	public void setBlackStyle(boolean bool, int color) {
    	setTextColor(color);
	}
    
    private void setTextColor(int color) {
		mDateMonthView.setTextColor(color);
		mDateDivView.setTextColor(color);
		mDateDayView.setTextColor(color);
		mWeekView.setTextColor(color);
		mTimeView.setTextColor(color);
	}
    
    void refreshAlarmStatus() {
        // Update Alarm status
        String nextAlarm = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (!TextUtils.isEmpty(nextAlarm)) {
            maybeSetUpperCaseText(mAlarmStatusView, nextAlarm);
            mAlarmStatusView.setVisibility(View.GONE);
        } else {
            mAlarmStatusView.setVisibility(View.GONE);
        }
    }

    void refreshDate(int index) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (isChangeDate(index)) {
            calendar.add(Calendar.DATE, 1);
        }
        Date date = calendar.getTime();
        maybeSetUpperCaseText(mDateMonthView, mDateDayView, DateFormat.format(mDateFormatString, date));
    }

    void refreshLunarDate(int index) {
        setLunarDate(mLunarDateView);
    }

    void refreshWeek(int index) {
        setWeek(index);
    }

    private void maybeSetUpperCaseText(TextView textView, CharSequence text) {
        if (USE_UPPER_CASE) {
            textView.setText(text != null ? text.toString().toUpperCase() : null);
        } else {
            textView.setText(text);
        }
    }

    private void maybeSetUpperCaseText(TextView monthView, TextView dayView, CharSequence text) {
        try {
            String date = "";
            String time[] = null;
            if (USE_UPPER_CASE) {
                date = text.toString().toUpperCase();
            } else {
                date = text.toString();
            }
            if (date.length() > 0) {
                Log.d(TAG, "date=" + date);
                time = date.split("-");
                monthView.setText(time[0]);
                dayView.setText(time[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void maybeSetUpperCaseText(TextView monthView, TextView dayView, CharSequence monthText,
            CharSequence dayText) {
        if (USE_UPPER_CASE) {
            monthView.setText(monthText != null ? monthText.toString().toUpperCase() : null);
            dayView.setText(dayText != null ? dayText.toString().toUpperCase() : null);
        } else {
            monthView.setText(monthText);
            dayView.setText(dayText);
        }
    }

    private void setLunarDate(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Time tCalendar = null;
        if (tCalendar == null) {
            tCalendar = new Time(calendar.getTimeZone().getID());
        }
        long now = System.currentTimeMillis();
        tCalendar.set(now);
        LunarUtils lunar = new LunarUtils(mContext);
        lunar.SetSolarDate(tCalendar);

        String chinaDate = lunar.GetLunarNYRString();
        String shortDate = lunar.GetLunarDateString();
        shortDate = shortDate.trim();
        String nlString = "";

        if (chinaDate != null) {
            if (chinaDate.substring(10, 11).equalsIgnoreCase(" ")) {
                nlString = chinaDate.substring(3, 10);
            } else {
                nlString = chinaDate.substring(3);
                if (shortDate != null) {
                    if (chinaDate.substring(7).contains(shortDate)) {
                        nlString = shortDate;
                    }
                }
            }
        }
        textView.setText(nlString);
    }

    private void setWeek(int index) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (isChangeDate(index)) {
            calendar.add(Calendar.DATE, 1);
        }
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String[] dayOfWeek = mContext.getResources().getStringArray(R.array.day_of_week);
        if (week > 0 && dayOfWeek != null) {
            mWeekView.setText(dayOfWeek[week - 1]);
        }
    }

    private boolean isChangeDate(int index) {
        boolean bool = false;
        if (index > 8) {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	
    }

    public void onPageSelected(int position) {
        onPageSelected(position, false);
    }

    public void onPageSelected(int position, boolean isSingle) {
        refresh(position);
    }

    public void setSingle(boolean bool) {
    }
    
    public void updateClock() {
	}
}
