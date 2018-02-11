/**
 * Copyright (C) 2016 Tcl Corporation Limited
 */
package cn.tcl.transfer;

public class File_Exchange {
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_AUDIO = 3;
    public static final int TYPE_DOCUMENT = 6;
    public static final int TYPE_APP = 4;
    public static final int TYPE_WALLPAPER = 5;

    private long id;
    private String filePath;
    private long size;
    private int type;

    //gets
    public long getId() {
        return this.id;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public long getSize() {
        return this.size;
    }

    public int getType() {
        return this.type;
    }

    //sets
    public void setId(long newValue) {
        this.id = newValue;
    }

    public void setFilePath(String newValue) {
        this.filePath = newValue;
    }

    public void setSize(long newValue) {
        this.size = newValue;
    }

    public void setType(int newValue) {
        this.type = newValue;
    }

}
