/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.bean;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.tcl.meetingassistant.db.MeetingInfoDBUtil;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Meeting decision info
 * The class to save something global
 */
public class MeetingStaticInfo {

    private static final String TAG = MeetingStaticInfo.class.getSimpleName();
    private static MeetingStaticInfo mMeetingStaticInfo = new MeetingStaticInfo();
    private String mLastMeetingLocation;
    private Meeting mCurrentMeeting;
    private List<Meeting> mMeetingList;

    private MeetingStaticInfo(){
        mMeetingList = new ArrayList<>();
        mLastMeetingLocation = "";
    }

    public static MeetingStaticInfo getInstance(){
        return mMeetingStaticInfo;
    }

    public static String getLastMeetingLocation() {
        MeetingStaticInfo meetingStaticInfo = getInstance();
        List<Meeting> meetings = meetingStaticInfo.getMeetingList();
        if(null != meetings && meetings.size() >= 2){
            Meeting lastMeeting = null;
            for(Meeting meeting : meetings){
                if(lastMeeting == null){
                    lastMeeting = meeting;
                }else {
                    if(lastMeeting.getMeetingInfo().getStartTime() < meeting.getMeetingInfo().getStartTime() &&
                            lastMeeting.getMeetingInfo().getStartTime() != MeetingStaticInfo.getCurrentMeeting().getMeetingInfo().getStartTime()){
                        lastMeeting = meeting;
                    }
                }
            }
            meetingStaticInfo.mLastMeetingLocation = lastMeeting.getMeetingInfo().getAddress();
        }else{
            meetingStaticInfo.mLastMeetingLocation = "";
        }
        return meetingStaticInfo.mLastMeetingLocation;
    }

    public static void setLastMeetingLocation(List<MeetingInfo> list) {
        MeetingStaticInfo meetingStaticInfo = getInstance();
        if (list != null && list.size() >= 1) {
            meetingStaticInfo.mLastMeetingLocation = list.get(list.size() - 1).getAddress();
        }
    }

    public static Meeting getCurrentMeeting() {
        MeetingStaticInfo meetingStaticInfo = getInstance();
        return meetingStaticInfo.mCurrentMeeting;
    }

    public static void setCurrentMeeting(Meeting mCurrentMeeting) {
        MeetingStaticInfo meetingStaticInfo = getInstance();
        meetingStaticInfo.mCurrentMeeting = mCurrentMeeting;
    }

    public static void removeMeeting(Meeting meeting){
        MeetingStaticInfo meetingStaticInfo = getInstance();
        meetingStaticInfo.mMeetingList.remove(meeting);
    }

    public List<Meeting> getMeetingList() {
        return mMeetingList;
    }

    public void setMeetingList(List<Meeting> meetingList) {
        this.mMeetingList = meetingList;
    }

    public static void addMeeting(Meeting meeting){
        getInstance().mMeetingList.add(meeting);
    }

    public static void updateCurrentTime(Context context){
        MeetingInfo meetinginfo = getCurrentMeeting().getMeetingInfo();
        meetinginfo.setUpdateTime(System.currentTimeMillis());
        MeetingInfoDBUtil.update(meetinginfo,context,null);
        MeetingLog.i(TAG,"update meeting update time");
    }
}
