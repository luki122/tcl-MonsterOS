/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.entity;

import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.*;

public abstract class DocumentContent {

    public static final Uri CONTENT_URI = Files.getContentUri("external");



    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_PATH = "_data";

    public static final String COLUMN_DISP_NAME = "_display_name";

    public static final String MIME_TYPE = "mime_type";

    public static final String COLUMN_SIZE = "_size";

    public static final String WHERE = MIME_TYPE + " IN (\"text/plain\", \"application/vnd.ms-excel\", \"application/mspowerpoint\", \"application/msword\")";

}
