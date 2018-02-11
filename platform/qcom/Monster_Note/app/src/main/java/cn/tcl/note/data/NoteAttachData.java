/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;

import java.io.Serializable;

public class NoteAttachData extends CommonData implements Serializable {
    protected String mFileName;

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    @Override
    public String toXmlString() {
        return null;
    }
}
