/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;


import java.io.Serializable;

import cn.tcl.note.util.XmlPrase;

public class NoteAudioData extends NoteAttachData implements Serializable {
    private long mDuration;

    public NoteAudioData(String mFileName, long mDuration) {
        setDuration(mDuration);
        this.mFileName = mFileName;
    }

    public NoteAudioData() {
        this("", -1);
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long mDuration) {
        if (mDuration > 0 && mDuration < 1000) {
            mDuration = 1000;
        }
        this.mDuration = mDuration;
    }

    @Override
    public String toString() {
        return "audio name=" + mFileName + " duration=" + mDuration;
    }

    @Override
    public String toXmlString() {
        String mSpecailText = XmlPrase.replaceSpecailCharBefore(mFileName);
        String name = XmlPrase.buildXml(XmlPrase.TAG_AUDIO_TYPE, mSpecailText);
        String duration = XmlPrase.buildXml(XmlPrase.TAG_AUDIO_DURA, "" + mDuration);
        String result = XmlPrase.buildXml(XmlPrase.TAG_NOTE_TYPE, name + duration);
        return result;
    }
}
