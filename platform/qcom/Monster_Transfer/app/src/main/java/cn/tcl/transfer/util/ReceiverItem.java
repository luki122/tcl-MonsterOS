/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

public class ReceiverItem {
    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_APP = 1;
    public static final int TYPE_PICTURE = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_AUDIO = 4;
    public static final int TYPE_DOCUMENT = 5;
    private int type;
    private long size;
    private int progress;
    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public long getSize() {
        return size;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }
    public int getProgress() {
        return progress;
    }
}
