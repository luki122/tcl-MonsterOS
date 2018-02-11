package cn.tcl.music.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.content.CursorLoader;

import cn.tcl.music.database.MusicMediaDatabaseHelper.Playlists;

public class CommonCursorLoader extends CursorLoader {
    final ForceLoadContentObserver mFavoriteObserver;

    public CommonCursorLoader(Context context, Uri uri, String[] projection,
           String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);

        mFavoriteObserver = new ForceLoadContentObserver();
    }

    @Override
    public Cursor loadInBackground() {
        Cursor c = super.loadInBackground();

        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);

        getContext().getContentResolver().registerContentObserver(Playlists.FAVORITE_URI, true, mFavoriteObserver);
        return c;
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);
    }

    @Override
    public void onCanceled(Cursor cursor) {
        super.onCanceled(cursor);
        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);
    }
}
