/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data.calendar;

import android.content.Context;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by user on 16-9-13.
 */
public class CalendarEventModel implements Serializable {

    private static final String TAG = "CalendarEventModel";

    /**
     * A single attendee entry.
     */
    public static class Attendee implements Serializable {

        public String mName;
        public String mEmail;
        public int mStatus;
        public int mRelationship;
        public int mType;

        public Attendee(String name, String email) {
            this(name, email, Attendees.ATTENDEE_STATUS_NONE, 0, 0);
        }

        public Attendee(String name, String email, int status, int relationship,
                        int type) {
            mName = name;
            mEmail = email;
            mStatus = status;
            mRelationship = relationship;
            mType = type;
        }

    }

    /**
     * A single reminder entry.
     */
    public static class Reminder implements Serializable {
        public int mMinutes;
        public int mMethod;

        public Reminder(int minutes, int method) {
            mMinutes = minutes;
            mMethod = method;
        }

        public int getMinutes() {
            return mMinutes;
        }

        public int getMethod() {
            return mMethod;
        }
    }


    public long mId = -1;
    public String mTitle = null;
    public String mDescription = null;
    public String mLocation = null;
    public boolean mAllDay = false;
    public boolean mHasAlarm = false;
    public long mCalendarId = -1;
    public long mStart = -1;
    public long mEnd = -1;
    public String mDuration = null;
    public String mTimezone = null;
    public String mRrule = null;
    public String mSyncId = null;
    public int mAvailability = Events.AVAILABILITY_BUSY;
    public int mAccessLevel = 0;
    public String mOwnerAccount = null;
    public boolean mHasAttendeeData = true;
    public String mOriginalSyncId = null;
    public String mOrganizer = null;
    public boolean mGuestsCanModify = false;
    public String mOriginalId = null;
    public int mEventStatus = Events.STATUS_CONFIRMED;

    public ArrayList<Reminder> mRemindersList;
    //public ArrayList<Attendee> mAttendeesList;

    public CalendarEventModel() {
        mRemindersList = new ArrayList<Reminder>();
        //mAttendeesList = new ArrayList<Attendee>();
    }

    public boolean isValid() {
        if (mCalendarId == -1) {
            return false;
        }
        if (TextUtils.isEmpty(mOwnerAccount)) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        if (mTitle != null && mTitle.trim().length() > 0) {
            return false;
        }

        if (mLocation != null && mLocation.trim().length() > 0) {
            return false;
        }

        if (mDescription != null && mDescription.trim().length() > 0) {
            return false;
        }

        return true;
    }

    public void clear() {
        mId = -1;
        mCalendarId = -1;

        mSyncId = null;
        mOwnerAccount = null;

        mTitle = null;
        mLocation = null;
        mDescription = null;
        mRrule = null;
        mOrganizer = null;

        mStart = -1;
        mEnd = -1;
        mDuration = null;
        mTimezone = null;
        mAllDay = false;
        mHasAlarm = false;

        mHasAttendeeData = true;
        mOriginalId = null;
        mOriginalSyncId = null;

        mGuestsCanModify = false;
        mAccessLevel = 0;
        mEventStatus = Events.STATUS_CONFIRMED;

        mRemindersList = new ArrayList<Reminder>();
        //mAttendeesList.clear();
    }

    public void addAttendee(Attendee attendee) {
        //mAttendeesList.add(attendee);
    }

}
