package com.monster.market.provider;

import android.net.Uri;

/**
 * Created by xiaobin on 16-11-14.
 */
public abstract interface DownloadInfoConstants {

    public static final String AUTHORITY = "com.monster.market.provider";

    public static final String DOWNLOAD_LIST_INFO = "download_list_info";

    public static final Uri QUERY_URI = Uri.parse("content://" + AUTHORITY + "/" + DOWNLOAD_LIST_INFO);

    public static final String DOWNLOAD_LIST_COUNT =  "downloadListCount";
    public static final String DOWNLOAD_LIST_COUNT_MORE = "downloadListCountMore";

    public static final String[] DOWNLOAD_LIST_COUNT_COLUMNS = {
            DOWNLOAD_LIST_COUNT,
            DOWNLOAD_LIST_COUNT_MORE
    };

}
