/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;


import java.io.Serializable;

import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.XmlPrase;

public class NoteTextData extends CommonData implements Serializable {
    private final String TAG = NoteTextData.class.getSimpleName();
    private String mText;

    //mText mFlag,have four status:no,willdo_uncheck,willdo_check,dot
    private int mFlag;
    public static final int FLAG_NO = 0;
    public static final int FLAG_WILLDO_UN = 1;
    public static final int FLAG_WILLDO_CK = 2;
    public static final int FLAG_DOT = 3;

    public NoteTextData() {
        this("", FLAG_NO);
    }

    public NoteTextData(String mText, int flag) {
        this.mFlag = flag;
        this.mText = mText;
    }

    public int getFlag() {
        return mFlag;
    }

    public void setFlag(int flag) {
        NoteLog.d(TAG, "change mFlag from " + flagToString(this.mFlag) + " to " + flagToString(flag));
        this.mFlag = flag;
    }

    private String flagToString(int flag) {
        switch (flag) {
            case FLAG_NO:
                return "flag_no";
            case FLAG_WILLDO_UN:
                return "flag_willdo_un";
            case FLAG_WILLDO_CK:
                return "flag_willdo_ck";
            case FLAG_DOT:
                return "flag_dot";
            default:
                return "error";
        }
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    @Override
    public String toString() {
        return "text=" + mText + "  flag=" + flagToString(mFlag);
    }

    @Override
    public String toXmlString() {
        String mSpecailText = XmlPrase.replaceSpecailCharBefore(mText);
        String text = XmlPrase.buildXml(XmlPrase.TAG_TEXT_TYPE, mSpecailText);
        String flag = XmlPrase.buildXml(XmlPrase.TAG_TEXT_FLAG, "" + mFlag);
        return XmlPrase.buildXml(XmlPrase.TAG_NOTE_TYPE, text + flag);
    }
}
