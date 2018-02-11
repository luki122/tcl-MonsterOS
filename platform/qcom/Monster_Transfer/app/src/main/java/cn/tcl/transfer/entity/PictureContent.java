/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.entity;


import android.net.Uri;
import android.provider.MediaStore.Images.Media;

public abstract class PictureContent {

    public static final Uri CONTENT_URI = Media.EXTERNAL_CONTENT_URI;

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_PATH = "_data";

    public static final String COLUMN_SIZE = "_size";

    public static final String COLUMN_ALBUM_NAME = "bucket_display_name";

    public static final String COLUMN_ALBUM_ID = "bucket_id";

}
