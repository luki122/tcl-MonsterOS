/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.bean;

import android.text.TextUtils;

import java.text.NumberFormat;
import java.util.ArrayList;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The voice info includes the transformed text from voice and the time marks.
 */
public class MeetingVoice {

    private final String TAG = MeetingVoice.class.getSimpleName();

    private long mId = - 1;
    private long mMeetingId;
    private String mVoicePath;
    private String mDurationMarks = "";
    private String mVoiceText;
    private long mCreateTime;

    public MeetingVoice(){
        mVoicePath = "";
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getMeetingId() {
        return mMeetingId;
    }

    public void setMeetingId(long meetingId) {
        this.mMeetingId = meetingId;
    }

    public String getVoicePath() {
        return mVoicePath;
    }

    public void setVoicePath(String voicePath) {
        this.mVoicePath = voicePath;
    }

    public String getDurationMarks() {
        return mDurationMarks;
    }

    public void setDurationMarks(String duration) {
        mDurationMarks = duration;
    }

    public void addDurationMark(Long duration){
        if(TextUtils.isEmpty(mDurationMarks)){
            mDurationMarks = duration.toString();
        }else {
            mDurationMarks = mDurationMarks + "," + duration;
        }
    }

    public String getVoiceText() {
        return mVoiceText;
    }

    public void setVoiceText(String voiceText) {
        this.mVoiceText = voiceText;
    }

    public ArrayList<Long> getDurationLong(){
        String[] string = mDurationMarks.split(",");
        ArrayList<Long> durations = new ArrayList<>();

        for(int i = 0;i< string.length;i++){
            Long l;
            try{
                l = Long.parseLong(string[i]);
                durations.add(l);
            }catch (Exception e){
                l = 0l;
            }
            MeetingLog.d(TAG,string[i] + "-> String to Long-> " + l);
        }
        return durations;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        this.mCreateTime = createTime;
    }
}
