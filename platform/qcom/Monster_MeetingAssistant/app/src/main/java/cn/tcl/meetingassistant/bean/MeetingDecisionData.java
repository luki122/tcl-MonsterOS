/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.bean;

import android.text.TextUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Meeting decision info
 * This object has contain a decision's info
 */
public class MeetingDecisionData{
    //PRIMARY KEY
    private long mId = -1;

    private long mMeetingId = -1;
    private String mDecisionInfo;
    private String mPersons;
    private Long mDeadline;

    private final String TAG = MeetingDecisionData.class.getSimpleName();

    public MeetingDecisionData() {
        mDeadline = 0L;
    }

    public MeetingDecisionData(String decisionInfo, String persons, Long deadline, int meetingID) {
        this();
        this.mDecisionInfo = decisionInfo;
        this.mPersons = persons;
        this.mDeadline = deadline;
        this.mMeetingId = meetingID;
    }

    public MeetingDecisionData(String decisionInfo, String persons, Long deadline, int id, int meetingID) {
        this(decisionInfo, persons, deadline, meetingID);
        this.mId = id;
    }

    public void setDecisionInfo(String mDecisionInfo) {
        this.mDecisionInfo = mDecisionInfo;
    }

    public void setPersons(String persons) {
        this.mPersons = persons;
    }

    public void setDeadline(Long deadline) {
        this.mDeadline = deadline;
    }

    public String getDecisionInfo() {

        return mDecisionInfo;
    }

    public String getPersons() {
        return mPersons;
    }

    public Long getDeadline() {
        return mDeadline;
    }

    public long getMeetingId() {
        return mMeetingId;
    }

    public void setMeetingId(long meetingId) {
        this.mMeetingId = meetingId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public boolean isEmpty(){
        boolean isInfoEmpty = TextUtils.isEmpty(mDecisionInfo);
        boolean isPersonsEmpty = TextUtils.isEmpty(mPersons);
        boolean isTimeEmpty = mDeadline == 0;
        return isInfoEmpty && isPersonsEmpty && isTimeEmpty;
    }

}
