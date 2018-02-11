/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;

public class NoData extends NoteHomeData {
    private String mText;

    public NoData(String mText) {
        super();
        this.mText = mText;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }
}
