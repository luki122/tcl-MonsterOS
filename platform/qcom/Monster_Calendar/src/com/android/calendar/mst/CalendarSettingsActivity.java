package com.android.calendar.mst;

import com.android.calendar.GeneralPreferences;
import com.android.calendar.R;
import com.android.calendar.Utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import mst.preference.ListPreference;
import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.RingtonePreference;
import mst.preference.SwitchPreference;
import mst.preference.Preference.OnPreferenceChangeListener;

public class CalendarSettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	private static final String RINGTONE_NOTIFICATION_DEFAULT = "content://settings/system/notification_sound";

	private static final String KEY_CALENDAR_FILTER = "calendar_filter";
	public static final String KEY_BIRTHDAY_REMINDER = "birthday_reminder";

	Preference mCalendarFilter;
	ListPreference mWeekStart;
	SwitchPreference mBirthdayReminder;
	ListPreference mEventsReminder;
	RingtonePreference mRingtone;

	private boolean checkBirthdayReminder = true;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.mst_settings_preferences);

		mCalendarFilter = findPreference(KEY_CALENDAR_FILTER);
		mWeekStart = (ListPreference) findPreference(GeneralPreferences.KEY_WEEK_START_DAY);
		mBirthdayReminder = (SwitchPreference) findPreference(KEY_BIRTHDAY_REMINDER);
		mEventsReminder = (ListPreference) findPreference(GeneralPreferences.KEY_DEFAULT_REMINDER);
		mRingtone = (RingtonePreference) findPreference(GeneralPreferences.KEY_ALERTS_RINGTONE);
		mRingtone.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mWeekStart.setSummary(mWeekStart.getEntry());
		mEventsReminder.setSummary(mEventsReminder.getEntry());

		String ringToneUri = Utils.getRingTonePreference(this);
		Utils.setSharedPreference(this, GeneralPreferences.KEY_ALERTS_RINGTONE, ringToneUri);

		String ringtoneDisplayString = getRingtoneTitleFromUri(this, ringToneUri);
		mRingtone.setSummary(ringtoneDisplayString == null ? getString(R.string.ringtone_none) : ringtoneDisplayString);

		setPreferenceListeners(this);
	}

	private void setPreferenceListeners(OnPreferenceChangeListener listener) {
        mWeekStart.setOnPreferenceChangeListener(listener);
        mBirthdayReminder.setOnPreferenceChangeListener(listener);
        mEventsReminder.setOnPreferenceChangeListener(listener);
        mRingtone.setOnPreferenceChangeListener(listener);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWeekStart) {
            mWeekStart.setValue((String) newValue);
            mWeekStart.setSummary(mWeekStart.getEntry());
        } else if (preference == mBirthdayReminder) {
            Boolean checked = (Boolean) newValue;
            mBirthdayReminder.setChecked(checked);
            if (checkBirthdayReminder && checked) {
                checkBirthdayReminder = false;
                BirthdayReceicer.checkBirthdayReminder(this);
            }
        } else if (preference == mEventsReminder) {
        	mEventsReminder.setValue((String) newValue);
        	mEventsReminder.setSummary(mEventsReminder.getEntry());
        } else if (preference == mRingtone) {
            if (newValue instanceof String) {
                Utils.setRingTonePreference(this, (String) newValue);
                String ringtone = getRingtoneTitleFromUri(this, (String) newValue);
                mRingtone.setSummary(ringtone == null ? getString(R.string.ringtone_none) : ringtone);
            }
            return true;
        } else {
            return true;
        }
        return false;
    }

    public String getRingtoneTitleFromUri(Context context, String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }

        Ringtone ring = RingtoneManager.getRingtone(context, Uri.parse(uri));
        if (ring != null) {
            return ring.getTitle(context);
        }
        if (RINGTONE_NOTIFICATION_DEFAULT.equals(uri)) {
            return getString(R.string.ringtone_default_none);
        }
        return null;
    }

}
