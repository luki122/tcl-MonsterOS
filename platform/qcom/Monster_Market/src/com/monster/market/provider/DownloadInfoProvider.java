package com.monster.market.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.monster.market.MarketApplication;
import com.monster.market.download.AppDownloadService;

/**
 * Created by xiaobin on 16-11-14.
 */
public class DownloadInfoProvider extends ContentProvider implements DownloadInfoConstants {

    private static final String TAG = "DownloadInfoProvider";

    private static final int CODE_DOWNLOAD_LIST_COUNT = 1;
    private static final UriMatcher sUrlMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sUrlMatcher.addURI(AUTHORITY, DOWNLOAD_LIST_INFO, CODE_DOWNLOAD_LIST_COUNT);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        int match = sUrlMatcher.match(uri);
        switch (match) {
            case CODE_DOWNLOAD_LIST_COUNT: {
                return getDownloadListCount();
            }
            default: {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    private Cursor getDownloadListCount() {
        MatrixCursor mc = new MatrixCursor(DOWNLOAD_LIST_COUNT_COLUMNS, 1);
        mc.newRow().add(AppDownloadService.getDownloadingCount())
                    .add(AppDownloadService.getDownloadingCountMore());

        return mc;
    }

    /**
     * 如果query接口返回的数据有改变，调用此接口来通知观察者
     */
    public static void notifyQueryDataChanged() {
        MarketApplication.getInstance().getContentResolver()
                .notifyChange(QUERY_URI, null, false);
    }

}
