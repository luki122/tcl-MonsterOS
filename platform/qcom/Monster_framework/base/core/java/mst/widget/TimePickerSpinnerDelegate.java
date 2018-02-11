package mst.widget;

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES;

import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import libcore.icu.LocaleData;
import mst.widget.NumberPicker;
import android.widget.TextView;
import mst.widget.TimePicker;

public class TimePickerSpinnerDelegate extends TimePicker.AbstractTimePickerDelegate{
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int HOURS_IN_HALF_DAY = 12;

    // state
    private boolean mIs24HourView;
    private boolean mIsAm;

    // ui components
    private final NumberPicker mHourSpinner;
    private final NumberPicker mMinuteSpinner;
    private final NumberPicker mAmPmSpinner;
    private final EditText mHourSpinnerInput;
    private final EditText mMinuteSpinnerInput;
    private final EditText mAmPmSpinnerInput;
    private final TextView mDivider;

    // Note that the legacy implementation of the TimePicker is
    // using a button for toggling between AM/PM while the new
    // version uses a NumberPicker spinner. Therefore the code
    // accommodates these two cases to be backwards compatible.
    private final Button mAmPmButton;

    private final String[] mAmPmStrings;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;
    private Calendar mTempCalendar;
    private boolean mHourWithTwoDigit;
    private char mHourFormat;

    private int mNormalTextSize;
    private int mSelectorTextSize;
    private int mSelectorTextColor;
    private int mSelectorLabelTextSize;
    private int mSelectorLabelTextColor;
    private int mSecondTextSize;
    private int mSecondTextColor;
    private String mLabelFontFamily;
    private int mLabelSpace1;
    private int mLabelSpace2;
    private int mLabelSpace3;
    private int mLabelTextStyle;
    private String mFontFamily;
    private int mTextStyle;

    public TimePickerSpinnerDelegate(TimePicker delegator, Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(delegator, context);

        // process style attributes
        final TypedArray a = mContext.obtainStyledAttributes(
                attrs, com.mst.internal.R.styleable.TimePicker, defStyleAttr, defStyleRes);
        final int layoutResourceId = a.getResourceId(
        		com.mst.internal.R.styleable.TimePicker_legacyLayout, com.mst.internal.R.layout.time_picker_legacy);

        mNormalTextSize         = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_normalTextSize,0          );
        mSelectorTextSize       = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_selectionTextSize,0       );
        mSelectorTextColor      = a.getColor(com.mst.internal.R.styleable.TimePicker_selectionTextColor,0      );
        mSelectorLabelTextSize  = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_selectionLabelTextSize,0  );
        mSelectorLabelTextColor = a.getColor(com.mst.internal.R.styleable.TimePicker_selectionLabelTextColor,0 );
        mSecondTextSize         = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_secondTextSize,0          );
        mSecondTextColor        = a.getColor(com.mst.internal.R.styleable.TimePicker_secondTextColor,0         );
        mLabelFontFamily        = a.getString(com.mst.internal.R.styleable.TimePicker_labelFontFamily         );
        mLabelSpace1             = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_labelspace1,0             );
        mLabelSpace2             = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_labelspace2,0             );
        mLabelSpace3             = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_labelspace3,0             );
        mLabelTextStyle         = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_labelTextStyle,0          );
        mFontFamily             = a.getString(com.mst.internal.R.styleable.TimePicker_android_fontFamily      );
        mTextStyle              = a.getDimensionPixelSize(com.mst.internal.R.styleable.TimePicker_android_textStyle,0       );

        a.recycle();

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(layoutResourceId, mDelegator, true);

        // hour
        mHourSpinner = (NumberPicker) delegator.findViewById(com.android.internal.R.id.hour);
        mHourSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                if (!is24HourView()) {
                    if ((oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY) ||
                            (oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1)) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                }
                onTimeChanged();
            }
        });

        if(mNormalTextSize != 0)                 mHourSpinner.setNormalTextSize(mNormalTextSize);
        if(mSelectorTextSize != 0)               mHourSpinner.setSelectorTextSize(mSelectorTextSize);
        if(mSelectorTextColor != 0)              mHourSpinner.setSelectorTextColor(mSelectorTextColor);
        if(mSelectorLabelTextSize != 0)          mHourSpinner.setSelectorLabelTextSize(mSelectorLabelTextSize);
        if(mSelectorLabelTextColor != 0)         mHourSpinner.setSelectorLabelTextColor(mSelectorLabelTextColor);
        if(mSecondTextSize != 0)                 mHourSpinner.setSecondTextSize(mSecondTextSize);
        if(mSecondTextColor != 0)                mHourSpinner.setSecondTextColor(mSecondTextColor);
        if(!TextUtils.isEmpty(mLabelFontFamily)) mHourSpinner.setSelectorLabelFontFamily(mLabelFontFamily,mLabelTextStyle);
        if(mLabelSpace1 != 0)                    mHourSpinner.setLabelSpace(mLabelSpace1);
        if(!TextUtils.isEmpty(mFontFamily))      mHourSpinner.setFontFamily(mFontFamily,mTextStyle);

        mHourSpinnerInput = (EditText) mHourSpinner.findViewById(com.android.internal.R.id.numberpicker_input);
        mHourSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // divider (only for the new widget style)
        mDivider = (TextView) mDelegator.findViewById(com.android.internal.R.id.divider);
        if (mDivider != null) {
            setDividerText();
        }

        // minute
        mMinuteSpinner = (NumberPicker) mDelegator.findViewById(com.android.internal.R.id.minute);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                int minValue = mMinuteSpinner.getMinValue();
                int maxValue = mMinuteSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourSpinner.getValue() + 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourSpinner.getValue() - 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY - 1) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setValue(newHour);
                }
                onTimeChanged();
            }
        });

        if(mNormalTextSize != 0)                 mMinuteSpinner.setNormalTextSize(mNormalTextSize);
        if(mSelectorTextSize != 0)               mMinuteSpinner.setSelectorTextSize(mSelectorTextSize);
        if(mSelectorTextColor != 0)              mMinuteSpinner.setSelectorTextColor(mSelectorTextColor);
        if(mSelectorLabelTextSize != 0)          mMinuteSpinner.setSelectorLabelTextSize(mSelectorLabelTextSize);
        if(mSelectorLabelTextColor != 0)         mMinuteSpinner.setSelectorLabelTextColor(mSelectorLabelTextColor);
        if(mSecondTextSize != 0)                 mMinuteSpinner.setSecondTextSize(mSecondTextSize);
        if(mSecondTextColor != 0)                mMinuteSpinner.setSecondTextColor(mSecondTextColor);
        if(!TextUtils.isEmpty(mLabelFontFamily)) mMinuteSpinner.setSelectorLabelFontFamily(mLabelFontFamily,mLabelTextStyle);
        if(mLabelSpace2 != 0)                    mMinuteSpinner.setLabelSpace(mLabelSpace2);
        if(!TextUtils.isEmpty(mFontFamily))      mMinuteSpinner.setFontFamily(mFontFamily,mTextStyle);

        mMinuteSpinnerInput = (EditText) mMinuteSpinner.findViewById(com.android.internal.R.id.numberpicker_input);
        mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // Get the localized am/pm strings and use them in the spinner.
        mAmPmStrings = getAmPmStrings(context);

        // am/pm
        final View amPmView = mDelegator.findViewById(com.android.internal.R.id.amPm);
        if (amPmView instanceof Button) {
            mAmPmSpinner = null;
            mAmPmSpinnerInput = null;
            mAmPmButton = (Button) amPmView;
            mAmPmButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged();
                }
            });
        } else {
            mAmPmButton = null;
            mAmPmSpinner = (NumberPicker) amPmView;
            mAmPmSpinner.setMinValue(0);
            mAmPmSpinner.setMaxValue(1);
            mAmPmSpinner.setDisplayedValues(mAmPmStrings);
            mAmPmSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    updateInputState();
                    picker.requestFocus();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged();
                }
            });

            if(mNormalTextSize != 0)                 mAmPmSpinner.setNormalTextSize(mNormalTextSize);
            if(mSelectorTextSize != 0)               mAmPmSpinner.setSelectorTextSize(mSelectorTextSize);
            if(mSelectorTextColor != 0)              mAmPmSpinner.setSelectorTextColor(mSelectorTextColor);
            if(mSelectorLabelTextSize != 0)          mAmPmSpinner.setSelectorLabelTextSize(mSelectorLabelTextSize);
            if(mSelectorLabelTextColor != 0)         mAmPmSpinner.setSelectorLabelTextColor(mSelectorLabelTextColor);
            if(mSecondTextSize != 0)                 mAmPmSpinner.setSecondTextSize(mSecondTextSize);
            if(mSecondTextColor != 0)                mAmPmSpinner.setSecondTextColor(mSecondTextColor);
            if(!TextUtils.isEmpty(mLabelFontFamily)) mAmPmSpinner.setSelectorLabelFontFamily(mLabelFontFamily,mLabelTextStyle);
            if(mLabelSpace3 != 0)                    mAmPmSpinner.setLabelSpace(mLabelSpace3);
            if(!TextUtils.isEmpty(mFontFamily))      mAmPmSpinner.setFontFamily(mFontFamily,mTextStyle);

            mAmPmSpinnerInput = (EditText) mAmPmSpinner.findViewById(com.android.internal.R.id.numberpicker_input);
            mAmPmSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        if (isAmPmAtStart()) {
            // Move the am/pm view to the beginning
            ViewGroup amPmParent = (ViewGroup) delegator.findViewById(com.android.internal.R.id.timePickerLayout);
            amPmParent.removeView(amPmView);
            amPmParent.addView(amPmView, 0);
            // Swap layout margins if needed. They may be not symmetrical (Old Standard Theme
            // for example and not for Holo Theme)
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) amPmView.getLayoutParams();
            final int startMargin = lp.getMarginStart();
            final int endMargin = lp.getMarginEnd();
            if (startMargin != endMargin) {
                lp.setMarginStart(endMargin);
                lp.setMarginEnd(startMargin);
            }
        }

        getHourFormatData();

        // update controls to initial state
        updateHourControl();
        updateMinuteControl();
        updateAmPmControl();

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));

        if (!isEnabled()) {
            setEnabled(false);
        }

        // set the content descriptions
        setContentDescriptions();

        // If not explicitly specified this view is important for accessibility.
        if (mDelegator.getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            mDelegator.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    private void getHourFormatData() {
        final String bestDateTimePattern = DateFormat.getBestDateTimePattern(mCurrentLocale,
                (mIs24HourView) ? "Hm" : "hm");
        final int lengthPattern = bestDateTimePattern.length();
        mHourWithTwoDigit = false;
        char hourFormat = '\0';
        // Check if the returned pattern is single or double 'H', 'h', 'K', 'k'. We also save
        // the hour format that we found.
        for (int i = 0; i < lengthPattern; i++) {
            final char c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == 'h' || c == 'K' || c == 'k') {
                mHourFormat = c;
                if (i + 1 < lengthPattern && c == bestDateTimePattern.charAt(i + 1)) {
                    mHourWithTwoDigit = true;
                }
                break;
            }
        }
    }

    private boolean isAmPmAtStart() {
        final String bestDateTimePattern = DateFormat.getBestDateTimePattern(mCurrentLocale,
                "hm" /* skeleton */);

        return bestDateTimePattern.startsWith("a");
    }

    /**
     * The time separator is defined in the Unicode CLDR and cannot be supposed to be ":".
     *
     * See http://unicode.org/cldr/trac/browser/trunk/common/main
     *
     * We pass the correct "skeleton" depending on 12 or 24 hours view and then extract the
     * separator as the character which is just after the hour marker in the returned pattern.
     */
    private void setDividerText() {
        final String skeleton = (mIs24HourView) ? "Hm" : "hm";
        final String bestDateTimePattern = DateFormat.getBestDateTimePattern(mCurrentLocale,
                skeleton);
        final String separatorText;
        int hourIndex = bestDateTimePattern.lastIndexOf('H');
        if (hourIndex == -1) {
            hourIndex = bestDateTimePattern.lastIndexOf('h');
        }
        if (hourIndex == -1) {
            // Default case
            separatorText = ":";
        } else {
            int minuteIndex = bestDateTimePattern.indexOf('m', hourIndex + 1);
            if  (minuteIndex == -1) {
                separatorText = Character.toString(bestDateTimePattern.charAt(hourIndex + 1));
            } else {
                separatorText = bestDateTimePattern.substring(hourIndex + 1, minuteIndex);
            }
        }
        mDivider.setText(separatorText);
    }

    @Override
    public void setCurrentHour(int currentHour) {
        setCurrentHour(currentHour, true);
    }

    private void setCurrentHour(int currentHour, boolean notifyTimeChanged) {
        // why was Integer used in the first place?
        if (currentHour == getCurrentHour()) {
            return;
        }
        if (!is24HourView()) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour >= HOURS_IN_HALF_DAY) {
                mIsAm = false;
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour = currentHour - HOURS_IN_HALF_DAY;
                }
            } else {
                mIsAm = true;
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY;
                }
            }
            updateAmPmControl();
        }
        mHourSpinner.setValue(currentHour);
        if (notifyTimeChanged) {
            onTimeChanged();
        }
    }

    @Override
    public int getCurrentHour() {
        int currentHour = mHourSpinner.getValue();
        if (is24HourView()) {
            return currentHour;
        } else if (mIsAm) {
            return currentHour % HOURS_IN_HALF_DAY;
        } else {
            return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
        }
    }

    @Override
    public void setCurrentMinute(int currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        mMinuteSpinner.setValue(currentMinute);
        onTimeChanged();
    }

    @Override
    public int getCurrentMinute() {
        return mMinuteSpinner.getValue();
    }

    @Override
    public void setIs24HourView(boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        // cache the current hour since spinner range changes and BEFORE changing mIs24HourView!!
        int currentHour = getCurrentHour();
        // Order is important here.
        mIs24HourView = is24HourView;
        getHourFormatData();
        updateHourControl();
        // set value after spinner range is updated
        setCurrentHour(currentHour, false);
        updateMinuteControl();
        updateAmPmControl();
    }

    @Override
    public boolean is24HourView() {
        return mIs24HourView;
    }

    @Override
    public void setOnTimeChangedListener(TimePicker.OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mMinuteSpinner.setEnabled(enabled);
        if (mDivider != null) {
            mDivider.setEnabled(enabled);
        }
        mHourSpinner.setEnabled(enabled);
        if (mAmPmSpinner != null) {
            mAmPmSpinner.setEnabled(enabled);
        } else {
            mAmPmButton.setEnabled(enabled);
        }
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    public int getBaseline() {
        return mHourSpinner.getBaseline();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setCurrentLocale(newConfig.locale);
    }

    @Override
    public Parcelable onSaveInstanceState(Parcelable superState) {
        return new SavedState(superState, getCurrentHour(), getCurrentMinute());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags = DateUtils.FORMAT_SHOW_TIME;
        if (mIs24HourView) {
            flags |= DateUtils.FORMAT_24HOUR;
        } else {
            flags |= DateUtils.FORMAT_12HOUR;
        }
        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
                mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourSpinnerInput)) {
                mHourSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(mDelegator.getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteSpinnerInput)) {
                mMinuteSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(mDelegator.getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mAmPmSpinnerInput)) {
                mAmPmSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(mDelegator.getWindowToken(), 0);
            }
        }
    }

    private void updateAmPmControl() {
        if (is24HourView()) {
            if (mAmPmSpinner != null) {
                mAmPmSpinner.setVisibility(View.GONE);
            } else {
                mAmPmButton.setVisibility(View.GONE);
            }
        } else {
            int index = mIsAm ? Calendar.AM : Calendar.PM;
            if (mAmPmSpinner != null) {
                mAmPmSpinner.setValue(index);
                mAmPmSpinner.setVisibility(View.VISIBLE);
            } else {
                mAmPmButton.setText(mAmPmStrings[index]);
                mAmPmButton.setVisibility(View.VISIBLE);
            }
        }
        mDelegator.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    @Override
    public void setCurrentLocale(Locale locale) {
        super.setCurrentLocale(locale);
        mTempCalendar = Calendar.getInstance(locale);
    }

    private void onTimeChanged() {
        mDelegator.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(mDelegator, getCurrentHour(),
                    getCurrentMinute());
        }
    }

    private void updateHourControl() {
        if (is24HourView()) {
            // 'k' means 1-24 hour
            if (mHourFormat == 'k') {
                mHourSpinner.setMinValue(1);
                mHourSpinner.setMaxValue(24);
            } else {
                mHourSpinner.setMinValue(0);
                mHourSpinner.setMaxValue(23);
            }
        } else {
            // 'K' means 0-11 hour
            if (mHourFormat == 'K') {
                mHourSpinner.setMinValue(0);
                mHourSpinner.setMaxValue(11);
            } else {
                mHourSpinner.setMinValue(1);
                mHourSpinner.setMaxValue(12);
            }
        }
        mHourSpinner.setFormatter(mHourWithTwoDigit ? NumberPicker.getTwoDigitFormatter() : null);
    }

    private void updateMinuteControl() {
        if (is24HourView()) {
            mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        } else {
            mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
    }

    private void setContentDescriptions() {
        // Minute
        trySetContentDescription(mMinuteSpinner, com.android.internal.R.id.increment,
        		com.android.internal.R.string.time_picker_increment_minute_button);
        trySetContentDescription(mMinuteSpinner, com.android.internal.R.id.decrement,
        		com.android.internal.R.string.time_picker_decrement_minute_button);
        // Hour
        trySetContentDescription(mHourSpinner, com.android.internal.R.id.increment,
        		com.android.internal.R.string.time_picker_increment_hour_button);
        trySetContentDescription(mHourSpinner, com.android.internal.R.id.decrement,
        		com.android.internal.R.string.time_picker_decrement_hour_button);
        // AM/PM
        if (mAmPmSpinner != null) {
            trySetContentDescription(mAmPmSpinner, com.android.internal.R.id.increment,
            		com.android.internal.R.string.time_picker_increment_set_pm_button);
            trySetContentDescription(mAmPmSpinner, com.android.internal.R.id.decrement,
            		com.android.internal.R.string.time_picker_decrement_set_am_button);
        }
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(mContext.getString(contDescResId));
        }
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends View.BaseSavedState {
        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        @SuppressWarnings({"unused", "hiding"})
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public static String[] getAmPmStrings(Context context) {
        String[] result = new String[2];
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        result[0] = d.amPm[0].length() > 4 ? d.narrowAm : d.amPm[0];
        result[1] = d.amPm[1].length() > 4 ? d.narrowPm : d.amPm[1];
        return result;
    }
}
