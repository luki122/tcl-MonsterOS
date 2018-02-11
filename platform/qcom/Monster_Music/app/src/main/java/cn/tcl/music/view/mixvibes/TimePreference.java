package cn.tcl.music.view.mixvibes;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
    private int lastHour=0;
    private int lastMinute=0;
    private TimePicker picker=null;

    public static int getHour(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[1]));
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText(com.mixvibes.mvlib.R.string.set);
        setNegativeButtonText(com.mixvibes.mvlib.R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {    	
    	picker = new TimePicker(getContext(), null, TimePickerDialog.THEME_HOLO_LIGHT);
        picker.setIs24HourView(true);
        picker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        return(picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour=picker.getCurrentHour();
            lastMinute=picker.getCurrentMinute();

            int timeInSeconds = getTimeInSeconds();
            if (callChangeListener(timeInSeconds)) 
            {
                setSummary(getSummary());
                persistInt(getTimeInSeconds());
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getInt(index, 3600));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        
        if (restoreValue) {
            if (defaultValue==null)
            	setTimeInSeconds(getPersistedInt(60*10));
            else
            	setTimeInSeconds(getPersistedInt((Integer)defaultValue));
        }
        else 
        {
        	setTimeInSeconds(getPersistedInt((Integer)defaultValue));
        }
        
        setSummary(getSummary());
    }
    
    public int getTimeInSeconds()
    {
    	return lastMinute * 60 + lastHour * 3600;
    }

    public int getTimeInMinutes()
    {
        return lastMinute + lastHour * 60;
    }

    public void setTimeInSeconds(int timeInSeconds)
    {
        int hours = timeInSeconds / 3600;
        int remainder = timeInSeconds - hours * 3600;
        int minutes = remainder / 60;

        lastHour = hours;
        lastMinute = minutes;
        
        setSummary(getSummary());
    }
    
    public String getSummary() 
    {

        String timeInMinute;
        if (getTimeInMinutes() <=1) {
            timeInMinute = getContext().getString(com.mixvibes.mvlib.R.string.record_minute,getTimeInMinutes());
        }
        else {
            timeInMinute = getContext().getString(com.mixvibes.mvlib.R.string.record_minutes,getTimeInMinutes());
        }
        return timeInMinute;

    }
}