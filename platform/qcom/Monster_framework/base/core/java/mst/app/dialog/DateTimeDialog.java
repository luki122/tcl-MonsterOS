package mst.app.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.widget.Button;

import com.mst.internal.R;

import java.util.Calendar;

import mst.widget.DateTimePicker;
import mst.widget.DateTimePicker.OnDateChangedListener;
import mst.widget.DateTimePicker.ValidationCallback;

public class DateTimeDialog extends PopupDialog implements OnClickListener,
        OnDateChangedListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";

    private final DateTimePicker mDatePicker;
    private final OnDateTimeSetListener mDateSetListener;
    private final Calendar mCalendar;

    private boolean mTitleNeedsUpdate = true;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateTimeSetListener {

        /**
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *  with {@link Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateTimeSet(DateTimePicker view, int year, int monthOfYear, int dayOfMonth, int hour, int minute);
    }

    public DateTimeDialog(Context context){
        this(context, com.mst.R.style.Dialog_Spinner, null);
    }

    public DateTimeDialog(Context context,
                          OnDateTimeSetListener callBack) {
        this(context, com.mst.R.style.Dialog_Spinner, callBack);
    }

    /**
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    public DateTimeDialog(Context context,
                          OnDateTimeSetListener callBack,
                          int year,
                          int monthOfYear,
                          int dayOfMonth,
                          int hour,
                          int minute) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, hour, minute);
    }

    static int resolveDialogTheme(Context context, int resid) {
        if (resid == 0) {
            final TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(com.mst.R.attr.datePickerDialogTheme, outValue, true);
            return outValue.resourceId;
        } else {
            return resid;
        }
    }

    /**
     * @param context The context the dialog is to run in.
     * @param theme the theme to apply to this dialog
     * @param listener How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    public DateTimeDialog(Context context, int theme, OnDateTimeSetListener listener, int year,
                          int monthOfYear, int dayOfMonth, int hour, int minute) {
        this(context, resolveDialogTheme(context, theme),listener);
        mDatePicker.init(year, monthOfYear, dayOfMonth, hour, minute, this);
        updateTitle(year, monthOfYear, dayOfMonth);
    }
    public DateTimeDialog(Context context, int theme, OnDateTimeSetListener listener) {
        super(context, resolveDialogTheme(context, theme));

        mDateSetListener = listener;
        mCalendar = Calendar.getInstance();

        final Context themeContext = getContext();
        setCustomView(com.mst.R.layout.date_time_dialog);
        setPositiveButton(true);
        setNegativeButton(true);
        setOnClickListener(this);
        //setButtonPanelLayoutHint(LAYOUT_HINT_SIDE);

        mDatePicker = (DateTimePicker) findViewById(com.android.internal.R.id.datePicker);
        mDatePicker.setValidationCallback(mValidationCallback);
        mDatePicker.setOnDateChangedListener(this);
        updateTitle(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onDateChanged(DateTimePicker view, int year, int month, int day, int hour, int minute) {
        mDatePicker.init(year, month, day, hour, minute, this);
        updateTitle(year, month, day);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mDateSetListener != null) {
                    // Clearing focus forces the dialog to commit any pending
                    // changes, e.g. typed text in a NumberPicker.
                    mDatePicker.clearFocus();
                    mDateSetListener.onDateTimeSet(mDatePicker, mDatePicker.getYear(),
                            mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), mDatePicker.getHour(), mDatePicker.getMinute());
                }
                dismiss();
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    /**
     * Gets the {@link DatetTimePicker} contained in this dialog.
     *
     * @return The calendar view.
     */
    public DateTimePicker getDatePicker() {
        return mDatePicker;
    }

    /**
     * Sets the current date.
     *
     * @param year The date year.
     * @param monthOfYear The date month.
     * @param dayOfMonth The date day of month.
     */
    public void updateDate(int year, int monthOfYear, int dayOfMonth, int hour, int minute) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth, hour, minute);
    }

    private void updateTitle(int year, int month, int day) {
        if (!mDatePicker.getCalendarViewShown()) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            String title = DateUtils.formatDateTime(getContext(),
                    mCalendar.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_YEAR
                            | DateUtils.FORMAT_ABBREV_MONTH
                            | DateUtils.FORMAT_ABBREV_WEEKDAY);
            setTitle(title);
            mTitleNeedsUpdate = true;
        } else {
            if (mTitleNeedsUpdate) {
                mTitleNeedsUpdate = false;
                setTitle(com.mst.internal.R.string.date_picker_dialog_title);
            }
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        state.putInt(HOUR, mDatePicker.getHour());
        state.putInt(MINUTE, mDatePicker.getMinute());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int year = savedInstanceState.getInt(YEAR);
        final int month = savedInstanceState.getInt(MONTH);
        final int day = savedInstanceState.getInt(DAY);
        final int hour = savedInstanceState.getInt(HOUR);
        final int minute = savedInstanceState.getInt(MINUTE);
        mDatePicker.init(year, month, day, hour, minute, this);
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
