/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;

import java.util.ArrayList;
import java.util.LinkedList;

import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.XmlPrase;

public class NoteHomeData {
    private final String TAG = NoteHomeData.class.getSimpleName();
    private long mId;
    private String mFirstLine;
    private String mSecondLine;
    private int mWillNum;
    private int mImgNum;
    private int mAudioNum;
    private String mTime;
    private ArrayList<String> mAllText = new ArrayList<>();
    private Boolean isCheck;

    public NoteHomeData(long id, String mFirstLine, String mSecondLine, int mWillNum, int mImgNum,
                        int mAudioNum, String mTime, String defaultImgString, String defaultAudioString, String xmlStr) {
        mId = id;
        this.mAudioNum = mAudioNum;
        this.mFirstLine = mFirstLine;
        this.mImgNum = mImgNum;
        this.mSecondLine = mSecondLine;
        this.mTime = mTime;
        this.mWillNum = mWillNum;
        isCheck = false;
        handleFirstSecondLine(defaultImgString, defaultAudioString);
        handleAllText(xmlStr);
    }

    public NoteHomeData(long id, String mFirstLine, String mSecondLine, int mWillNum, int mImgNum,
                        int mAudioNum, String mTime, String defaultImgString, String defaultAudioString) {
        this(id, mFirstLine, mSecondLine, mWillNum, mImgNum, mAudioNum, mTime, defaultImgString, defaultAudioString, null);
    }

    public NoteHomeData() {
    }

    private void handleAllText(String xmlStr) {
        if (xmlStr != null) {
            LinkedList<CommonData> data = XmlPrase.prase(xmlStr);
            for (CommonData commonData : data) {
                if (commonData instanceof NoteTextData) {
                    String str = ((NoteTextData) commonData).getText();
                    if (XmlPrase.isBlankStr(str)) {
                        mAllText.add(str);
                    }
                }
            }
        }
    }

    private void handleFirstSecondLine(String defaultImgString, String defaultAudioString) {
        if (mFirstLine.length() == 0) {
            StringBuilder sb = new StringBuilder();
            if (mImgNum > 0) {
                sb.append(String.format(defaultImgString, mImgNum));
            }
            if (mAudioNum > 0) {
                sb.append(String.format(defaultAudioString, mAudioNum));
            }
            mFirstLine = sb.toString();
            mSecondLine = mFirstLine;
        } else if (mSecondLine.length() == 0) {
            mSecondLine = mFirstLine;
        }
    }

    public String getFirstLine() {
        return mFirstLine;
    }


    public String getSecondLine() {
        return mSecondLine;
    }

    public String getmTime() {
        return (String) mTime.subSequence(0, mTime.length() - 3);
    }

    public boolean getImgNum() {
        if (mImgNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getAudioNum() {
        if (mAudioNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getWillNum() {
        if (mWillNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    public long getId() {
        return mId;
    }

    public boolean searchResult(String text) {
        int i = 0;
        boolean isHave = false;
        if (mAllText.size() > 1) {
            mSecondLine = mAllText.get(1);
        } else {
            mSecondLine = mFirstLine;
        }
        for (String str : mAllText) {
            if (str.contains(text)) {
                isHave = true;
                if (i != 0) {
                    mSecondLine = str;
                    break;
                }
            }
            i++;
        }
        return isHave;
    }

    public Boolean getCheck() {
        NoteLog.d(TAG, "get check=" + isCheck);
        return isCheck;
    }

    public void setCheck(Boolean check) {
        isCheck = check;
        NoteLog.d(TAG, "set check=" + check + " class:" + this);
    }

    public void setCheck() {
        isCheck = !isCheck;
        NoteLog.d(TAG, "set check=" + isCheck + " class:" + this);
    }
}
