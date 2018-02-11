/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.entity;

import android.net.Uri;
import android.provider.MediaStore.Audio.Media;

public abstract class AudioContent {

    public static final Uri CONTENT_URI = Media.EXTERNAL_CONTENT_URI;

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_PATH = "_data";


    public static final String COLUMN_DISP_NAME = "_display_name";

//	public static final String COLUMN_DISP_NAME = Media.TITLE;

//	public static final String COLUMN_ARTIST = "artist";

    public static final String COLUMN_ARTIST = Media.ARTIST;

    public static final String COLUMN_AlBUM = Media.ALBUM;

    public static final String COLUMN_DURATION = "duration";

    public static final String COLUMN_SIZE = "_size";

    public static final String COLUMN_IS_MUSIC = "is_music";


}
