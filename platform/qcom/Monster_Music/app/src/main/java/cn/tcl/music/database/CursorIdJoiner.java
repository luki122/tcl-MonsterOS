package cn.tcl.music.database;

import java.util.Iterator;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.LogUtil;

/**
 * Does a join on two cursors using the specified column Id. The cursors must already
 * be sorted by Id in ascending order.
 * This CursorJoiner is a specific version of the Android CursorJoiner, to work around the issue
 * when dealing with numbers instead of string values.
 */

public class CursorIdJoiner implements Iterator<CursorIdJoiner.Result>, Iterable<CursorIdJoiner.Result> {
    private static final String TAG = CursorIdJoiner.class.getSimpleName();
    private Result mCompareResult;
    private boolean mCompareResultIsValid;
    private Cursor mAndroidCursor;
    private Cursor mOwnCursor;

    /**
     * The result of a call to next().
     */
    public enum Result {
        /**
         * The row currently pointed to by the left cursor is unique
         */
        RIGHT,
        /**
         * The row currently pointed to by the right cursor is unique
         */
        LEFT,
        /**
         * The rows pointed to by both cursors are the same
         */
        BOTH
    }

    public CursorIdJoiner(Cursor cursorLeft, Cursor cursorRight) {
        mAndroidCursor = cursorLeft;
        mOwnCursor = cursorRight;
        mAndroidCursor.moveToFirst();
        mOwnCursor.moveToFirst();
        mCompareResultIsValid = false;
        LogUtil.d(TAG,"mAndroidCursor count is " + mAndroidCursor.getCount() + " and mOwnCursor count is " + mOwnCursor.getCount());
    }


    @Override
    public Iterator<Result> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (mCompareResultIsValid) {
            switch (mCompareResult) {
                case BOTH:
                    return !mAndroidCursor.isLast() || !mOwnCursor.isLast();

                case LEFT:
                    return !mAndroidCursor.isLast() || !mOwnCursor.isAfterLast();

                case RIGHT:
                    return !mAndroidCursor.isAfterLast() || !mOwnCursor.isLast();

                default:
                    throw new IllegalStateException("bad value for mCompareResult, " + mCompareResult);
            }
        } else {
            return !mAndroidCursor.isAfterLast() || !mOwnCursor.isAfterLast();
        }
    }

    @Override
    public Result next() {
        if (!hasNext()) {
            throw new IllegalStateException("you must only call next() when hasNext() is true");
        }
        incrementCursors();
        assert hasNext();
        boolean hasLeft = !mAndroidCursor.isAfterLast();
        boolean hasRight = !mOwnCursor.isAfterLast();
        LogUtil.d(TAG,"hasLeft = " + hasLeft + " hasRight = " + hasRight);
        if (hasLeft && hasRight) {
            long idLeft = mAndroidCursor.getLong(mAndroidCursor.getColumnIndex(MediaStore.Audio.Media._ID));
            long idRight = mOwnCursor.getLong(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
            switch (compareIds(idLeft, idRight)) {
                case -1:
                    mCompareResult = Result.LEFT;
                    //if in the meida table of MusicMedia.db,the column android_media_id value is 0,and both path value is same,the song is new downloaded,wo should take
                    //care of this when data sync
                    LogUtil.d(TAG,"android audio id is " + mOwnCursor.getLong(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID)));
                    LogUtil.d(TAG,"source type is " + mOwnCursor.getInt(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE)));
                    LogUtil.d(TAG,"android cursor data is " + mAndroidCursor.getString(mAndroidCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    LogUtil.d(TAG,"own cursor path is " + mOwnCursor.getString(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.PATH)));

                    if (mOwnCursor.getLong(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID)) == 0
                            && mOwnCursor.getInt(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE)) == CommonConstants.SRC_TYPE_DEEZER
                            && mAndroidCursor.getString(mAndroidCursor.getColumnIndex(MediaStore.Audio.Media.DATA)).equals(mOwnCursor.getString(mOwnCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.PATH)))) {
                        mCompareResult = Result.BOTH;
                    }
                    break;
                case 0:
                    mCompareResult = Result.BOTH;
                    break;
                case 1:
                    mCompareResult = Result.RIGHT;
                    break;
            }
        } else if (hasLeft) {
            mCompareResult = Result.LEFT;
        } else {
            assert hasRight;
            mCompareResult = Result.RIGHT;
        }
        mCompareResultIsValid = true;
        return mCompareResult;
    }

    /**
     * Increment the cursors past the rows indicated in the most recent call to next().
     * This will only have an affect once per call to next().
     */
    private void incrementCursors() {
        if (mCompareResultIsValid) {
            switch (mCompareResult) {
                case LEFT:
                    mAndroidCursor.moveToNext();
                    break;
                case RIGHT:
                    mOwnCursor.moveToNext();
                    break;
                case BOTH:
                    mAndroidCursor.moveToNext();
                    mOwnCursor.moveToNext();
                    break;
            }
            mCompareResultIsValid = false;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }

    protected int compareIds(long idLeft, long idRight) {
        if (idLeft == idRight)
            return 0;
        else {
            return idLeft < idRight ? -1 : 1;
        }
    }

}
