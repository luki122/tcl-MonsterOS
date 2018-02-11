/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;

import java.io.Serializable;

import cn.tcl.note.util.XmlPrase;

public class NotePicData extends NoteAttachData implements Serializable {

    public NotePicData(String mFileName) {
        this.mFileName = mFileName;
    }

    @Override
    public String toString() {
        return "img name=" + mFileName;
    }

    @Override
    public String toXmlString() {
        String mSpecailText = XmlPrase.replaceSpecailCharBefore(mFileName);
        String name = XmlPrase.buildXml(XmlPrase.TAG_IMG_TYPE, mSpecailText);
        return XmlPrase.buildXml(XmlPrase.TAG_NOTE_TYPE, name);
    }
}
