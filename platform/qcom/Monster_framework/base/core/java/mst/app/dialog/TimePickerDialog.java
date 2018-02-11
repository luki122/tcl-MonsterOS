package mst.app.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import mst.widget.TimePicker;
import mst.widget.TimePicker.OnTimeChangedListener;
import mst.widget.TimePicker.ValidationCallback;

public class TimePickerDialog extends PopupDialog implements OnClickListener,
OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private final TimePicker mTimePicker;
    private final OnTimeSetListener mTimeSetListener;

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourView;

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (e.g. they clicked on the 'OK' button).
     */
    public interface OnTimeSetListener {
        /**
         * Called when the user is done setting a new time and the dialog has
         * closed.
         *
         * @param view the view associated with this listener
         * @param hourOfDay the hour that was set
         * @param minute the minute that was set
         */
        public void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    public TimePickerDialog(Context context,boolean is24HourView){
        this(context, com.mst.R.style.Dialog_Spinner, null,is24HourView);
    }

    public TimePickerDialog(Context context,
                            OnTimeSetListener callBack,boolean is24HourView) {
        this(context, com.mst.R.style.Dialog_Spinner, callBack,is24HourView);
    }

    /**
     * Creates a new time picker dialog.
     *
     * @param context the parent context
     * @param listener the listener to call when the time is set
     * @param hourOfDay the initial hour
     * @param minute the initial minute
     * @param is24HourView whether this is a 24 hour view or AM/PM
     */
    public TimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute,
            boolean is24HourView) {
        this(context, 0, listener, hourOfDay, minute, is24HourView);
    }

    static int resolveDialogTheme(Context context, int resId) {
        if (resId == 0) {
            final TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(com.mst.R.attr.timePickerDialogTheme, outValue, true);
            return outValue.resourceId;
        } else {
            return resId;
        }
    }

    /**
     * Creates a new time picker dialog with the specified theme.
     *
     * @param context the parent context
     * @param themeResId the resource ID of the theme to apply to this dialog
     * @param listener the listener to call when the time is set
     * @param hourOfDay the initial hour
     * @param minute the initial minute
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public TimePickerDialog(Context context, int themeResId, OnTimeSetListener listener,
                            int hourOfDay, int minute, boolean is24HourView) {
        this(context,themeResId,listener,is24HourView);
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mTimePicker.setCurrentHour(mInitialHourOfDay);
        mTimePicker.setCurrentMinute(mInitialMinute);
        mTimePicker.setOnTimeChangedListener(this);
    }
    public TimePickerDialog(Context context, int themeResId, OnTimeSetListener listener,boolean is24HourView) {
        super(context, resolveDialogTheme(context, themeResId));

        mTimeSetListener = listener;


        final Context themeContext = getContext();


        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(com.mst.R.attr.timePickerDialogTheme, outValue, true);
        final int layoutResId = outValue.resourceId;

        setCustomView(com.mst.R.layout.time_picker_dialog);
        setPositiveButton(true);
        setNegativeButton(true);
        setOnClickListener(this);

        mTimePicker = (TimePicker) findViewById(com.android.internal.R.id.timePicker);
        mTimePicker.setValidationCallback(mValidationCallback);
        mIs24HourView = is24HourView;
        mTimePicker.setIs24HourView(mIs24HourView);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        /* do nothing */
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mTimeSetListener != null) {
                    mTimeSetListener.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
                            mTimePicker.getCurrentMinute());
                }
                dismiss();
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    /**
     * Sets the current time.
     *
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current minute within the hour.
     */
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour);
    }

    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int hour = savedInstanceState.getInt(HOUR);
        final int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }

    private final ValidationCallback mValidationCallback = new ValidationCallback() {
        @Override
        public void onValidationChanged(boolean valid) {
            final Button positive = getButton(BUTTON_POSITIVE);
            if (positive != null) {
                positive.setEnabled(valid);
            }
        }
    };
}
