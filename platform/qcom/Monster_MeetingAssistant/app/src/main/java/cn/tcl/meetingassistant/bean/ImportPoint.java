/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.bean;


import android.text.TextUtils;
import cn.tcl.meetingassistant.utils.FileUtils;
import java.io.File;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-13.
 * Meeting main info
 * This object has contain a highlight main info
 */

public class ImportPoint {

    public static final long DATA_IS_NOT_IN_DB = -1;

    private long mId = -1;

    private String mInfoContent;

    private long mMeetingId;

    private long mCreateTime;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getInfoContent() {
        return mInfoContent;
    }

    public void setInfoContent(String infoContent) {
        this.mInfoContent = infoContent;
    }

    public long getMeetingId() {
        return mMeetingId;
    }

    public void setMeetingId(long meetingId) {
        this.mMeetingId = meetingId;
    }

    public boolean isEmpty(){
        boolean emptyContent = TextUtils.isEmpty(mInfoContent);
        return emptyContent && !hasImage();
    }

    private boolean hasImage(){
        String filePath = FileUtils.IMAGE_FILE_PATH + mCreateTime;
        File[] imageFilePaths = FileUtils.getImageFilesByTime(filePath);

        if(imageFilePaths!= null && imageFilePaths.length > 0) {
            return true;
        }else {
            return false;
        }
    }



    public long getCreatTime() {
        return mCreateTime;
    }

    public void setCreatTime(long creatTime) {
        this.mCreateTime = creatTime;
    }

}
