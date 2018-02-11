/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.bean;

import android.content.Context;
import android.content.res.Resources;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Meeting all info
 * This object has contain all information of a meeting,such as meetingInfo(main info),decisions,
 * key points,pictures,sound records,etc.
 */
public class Meeting {

    private long mId  = -1;

    private final String TAG = Meeting.class.getSimpleName();

    private final int SEARCH_TITLE_COLOR  = R.color.bullet_color;

    private final int SEARCH_CONTENT_COLOR  = R.color.bullet_color;

    //main information of a meeting
    private MeetingInfo mMeetingInfo;

    //decisions of a meeting
    private List<MeetingDecisionData> mMeetingDecisions;

    private List<ImportPoint> mImportPoints;

    private List<MeetingVoice> mMeetingVoices;



    public Meeting(){
        mId = -1;
        mMeetingDecisions = new ArrayList<>();
        mImportPoints = new ArrayList<>();
        mMeetingVoices = new ArrayList<>();
    }

    private SearchResult mSearchResult;

    public MeetingInfo getMeetingInfo() {
        return mMeetingInfo;
    }

    public void setMeetingInfo(MeetingInfo mMeetingInfo) {
        this.mMeetingInfo = mMeetingInfo;
        mId = mMeetingInfo.getId();
    }

    public void addDecision(MeetingDecisionData decisionData){
        mMeetingDecisions.add(decisionData);
    }

    public void addImportPoint(ImportPoint importPoint){
        mImportPoints.add(importPoint);
    }

    public void addMeetingVoice(MeetingVoice meetingVoice){
        mMeetingVoices.add(meetingVoice);
    }

    public List<MeetingVoice> getMeetingVoices() {
        return mMeetingVoices;
    }

    public List<ImportPoint> getImportPoints() {
        return mImportPoints;
    }

    public List<MeetingDecisionData> getMeetingDecisions() {
        return mMeetingDecisions;
    }


    public SearchResult search(String targetString, Context context){
        mSearchResult = new SearchResult();
        if(TextUtils.isEmpty(targetString)){
            return mSearchResult;
        }
        Resources resources =  context.getResources();
        Editable.Factory editableFactort = Editable.Factory.getInstance();


        // search meetingInfo
        if(!TextUtils.isEmpty(mMeetingInfo.getTopics())
                && mMeetingInfo.getTopics().contains(targetString)){
            mSearchResult.isSucceed = true;
            mSearchResult.mResultTitle = resources.getString(R.string.conference_key_note);
            mSearchResult.mResultString = mMeetingInfo.getTopics();
            return mSearchResult;
        }
        if(!TextUtils.isEmpty(mMeetingInfo.getPersons())&&mMeetingInfo.getPersons().contains(targetString)){
            mSearchResult.isSucceed = true;
            mSearchResult.mResultTitle = resources.getString(R.string.person);
            mSearchResult.mResultString = mMeetingInfo.getPersons();
            return mSearchResult;
        }
        if(!TextUtils.isEmpty(mMeetingInfo.getAddress()) && mMeetingInfo.getAddress().contains(targetString)){
            mSearchResult.isSucceed = true;
            mSearchResult.mResultTitle = resources.getString(R.string.meet_location);
            mSearchResult.mResultString = mMeetingInfo.getAddress();
            return mSearchResult;
        }

        // search import point
        for(ImportPoint importPoint : mImportPoints){
            if(!TextUtils.isEmpty(importPoint.getInfoContent())&&importPoint.getInfoContent().contains(targetString)){
                mSearchResult.isSucceed = true;
                mSearchResult.mResultTitle = resources.getString(R.string.highlight);
                mSearchResult.mResultString = importPoint.getInfoContent();
                return mSearchResult;
            }
        }

        // search decisions
        for(MeetingDecisionData decisionData : mMeetingDecisions){
            if(!TextUtils.isEmpty(decisionData.getPersons()) && decisionData.getPersons().contains(targetString)){
                mSearchResult.isSucceed = true;
                mSearchResult.mResultTitle = resources.getString(R.string.owner);
                mSearchResult.mResultString = decisionData.getPersons();
                return mSearchResult;
            }

            if(!TextUtils.isEmpty(decisionData.getDecisionInfo()) && decisionData.getDecisionInfo().contains(targetString)){
                mSearchResult.isSucceed = true;
                mSearchResult.mResultTitle = resources.getString(R.string.decision);
                mSearchResult.mResultString = decisionData.getDecisionInfo();
                return mSearchResult;
            }
        }

        // search voice
        for(MeetingVoice voice : mMeetingVoices){
            if(!TextUtils.isEmpty(voice.getVoiceText()) && voice.getVoiceText().contains(targetString)){
                mSearchResult.isSucceed = true;
                mSearchResult.mResultTitle = resources.getString(R.string.voice_to_text);
                mSearchResult.mResultString = voice.getVoiceText();
                return mSearchResult;
            }
        }

        // search meeting title
        if(!TextUtils.isEmpty(mMeetingInfo.getTitle()) && mMeetingInfo.getTitle().contains(targetString)){
            mSearchResult.isSucceed = true;
            return mSearchResult;
        }

        return mSearchResult;
    }

    public long getId() {
        return mId;
    }

    public class SearchResult{
        public boolean isSucceed = false;
        public String mResultTitle;
        public String mResultString;
    }

    public SearchResult getSearchResult(){
        if(null == mSearchResult){
            MeetingLog.e(TAG,"getSearchResult",new RuntimeException("This has not been searched"));
        }
        return mSearchResult;
    }

}
