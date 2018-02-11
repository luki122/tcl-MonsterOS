/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.photopicker;

import android.net.Uri;

import java.io.Serializable;

/**
 * set the imageinfo attribute
 */
public class ItemImageInfo implements Serializable {

    public long imageId;
    public String filePath;
    public long size;
    public String orientation;
    public Uri uri;

    public boolean isChecked = false;

}
