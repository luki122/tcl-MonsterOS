/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.bean;


import android.text.TextUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Meeting main info
 * This object has contain a meeting main info,such as title,topic person,etc.
 */
public class MeetingInfo{

    //default -1 means the meeting info may be new created
    private long mId = -1;
    private String mTitle = "";
    private String mTopics = "";
    private String mPersons = "";
    private Long mStartTime;
    private Long mEndTime;
    private String mAddress = "";
    private Long mUpdateTime;

    private final String TAG = MeetingInfo.class.getSimpleName();

    public MeetingInfo() {
        mStartTime = 0l;
        mEndTime = 0l;
    }

    public MeetingInfo(String title, String topics, String persons, Long startTime, Long endTime, String address) {
        this();
        this.mTitle = title;
        this.mTopics = topics;
        this.mPersons = persons;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
        this.mAddress = address;
    }

    public MeetingInfo(long id, String title, String topics, String persons, Long startTime, Long endTime, String address) {
        this(title, topics, persons, startTime, endTime, address);
        this.mId = id;
    }


    public String getPersons() {
        return mPersons;
    }

    public void setPersons(String persons) {
        this.mPersons = persons;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getTopics() {
        return mTopics;
    }

    public void setTopics(String topics) {
        this.mTopics = topics;
    }

    public Long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Long startTime) {
        this.mStartTime = startTime;
    }

    public Long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Long endTime) {
        this.mEndTime = endTime;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    /**
     * if the meeting info is empty return true
     * @return true:if empty
     */
    public boolean isEmpty() {

        boolean emptyTopics = TextUtils.isEmpty(mTopics);
        boolean emptyPerson = TextUtils.isEmpty(mPersons);
        boolean emptyStartTime = mStartTime == 0;
        boolean emptyEndTime = mEndTime == 0;

        return emptyTopics&&emptyPerson&&emptyStartTime&&emptyEndTime;
    }

    public void setUpdateTime(long updateTime){
        mUpdateTime = updateTime;
    }

    public long getUpdateTime(){
        return mUpdateTime;
    }
}
