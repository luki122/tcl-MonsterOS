package cn.tcl.music.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.ViewGroup;
import com.tcl.framework.log.NLog;
import java.util.ArrayList;

import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.fragments.LocalAlbumDetailFragment;
import cn.tcl.music.fragments.LocalAlbumListFragment; // MODIFIED by Kang.Ren, 2016-11-07,BUG-3373604
import cn.tcl.music.fragments.LocalMediaFragment;
import cn.tcl.music.fragments.MyFavouriteMusicFragment;
import cn.tcl.music.fragments.PlaylistDetailFragment;
import cn.tcl.music.fragments.RecentlyPlayFragment;
import cn.tcl.music.util.LogUtil;


public abstract class RecyclerViewCursorAdapter<VH extends ClickableViewHolder> extends RecyclerViewAdapter<VH> {

    private static final String TAG = RecyclerViewCursorAdapter.class.getSimpleName();
    protected boolean mDataValid;
    protected Cursor mCursor;
    protected int mRowIDColumn;
    private ArrayList<Integer> mSelectedSongIds = new ArrayList<Integer>();//[BUGFIX]-Add by TCTNJ,huiyuan.wang, 2015-07-14,PR996622
    private ArrayList<Integer> mSelectedArtistIds = new ArrayList<Integer>();

    private ArrayList<Integer> mSelectedAlbumIds = new ArrayList<Integer>();
    protected DataSetObserver mDataSetObserver;

    public RecyclerViewCursorAdapter(Context context, Cursor c) {
        this(context, c, new int[]{com.mixvibes.mvlib.R.id.media_content});
    }

    public RecyclerViewCursorAdapter(Context context, Cursor c, int[] itemViewTypes) {
        super(context, itemViewTypes);
        init(context, c);
    }

    void init(Context context, Cursor c) {
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        mDataSetObserver = new MyDataSetObserver();

        if (cursorPresent) {
            if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
        }
    }

    /**
     * Returns the cursor.
     *
     * @return the cursor.
     */
    public Cursor getCursor() {
        return mCursor;
    }

    public Cursor getCursorAtAdapterPosition(int position) {
        if (mDataValid && mCursor != null) {
            int contentPosition = getPositionForContent(position);
            mCursor.moveToPosition(contentPosition);
            return mCursor;
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        long itemId = 0;
        int itemViewType = getItemViewType(position);
        if (itemViewType == com.mixvibes.mvlib.R.id.media_content) {
            if (mDataValid && mCursor != null) {
                int cursorPosition = getPositionForContent(position - countHeaderSize(position));
                if (mCursor.moveToPosition(cursorPosition)) {
                    itemId = mCursor.getLong(mRowIDColumn);
                }
            }
        } else {
            itemId = getItemIdForViewType(position, itemViewType);
        }
        return itemId;
    }

    @Override
    public int getItemViewType(int position) {

        return com.mixvibes.mvlib.R.id.media_content;
    }

    @Override
    public final void onBindViewHolder(VH viewHolder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == com.mixvibes.mvlib.R.id.media_content) {
            int cursorPosition = getPositionForContent(position - countHeaderSize(position));
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(cursorPosition)) {
                throw new IllegalStateException("couldn't move cursor to position " + cursorPosition);
            }
            onBindCursorToViewHolder(viewHolder, cursorPosition);
        } else {
            onBindViewTypeToViewHolder(viewHolder, position, itemViewType);
        }

    }

    public int countHeaderSize(int position) {
        return 0;
    }
    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(Cursor cursor) {
//        Log.d(TAG, "RecyclerViewCursorAdapter changeCursor and cursor count is " + cursor.getCount());
        try {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    public Cursor swapCursor(Cursor newCursor) {
//        Log.d(TAG,"swapCursor and new cursor count is " + newCursor.getCount());
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }



    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            Log.d(TAG, "MyDataSetObserver onChanged");
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            Log.d(TAG,"MyDataSetObserver onInvalidated");
            mDataValid = false;
            notifyDataSetChanged();
        }
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }


    @Override
    public void onBindViewTypeToViewHolder(VH viewHolder, int position, int itemViewType) {

    }

    @Override
    public long getItemIdForViewType(int position, int itemViewType) {
        return 0;
    }

    /**
     * Return the count for a specific itemViewType.
     * Default implementation just return the count of the content cursor.
     *
     * @param itemViewType The view type we are counting
     * @return the total count of this type of items
     */
    @Override
    public int getItemCountFor(int itemViewType) {
        if (itemViewType != com.mixvibes.mvlib.R.id.media_content)
            return 0;

        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public abstract void onBindCursorToViewHolder(VH viewHolder, int position);


    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType,
                                          LayoutManager currentRecyclerLayoutManager);

    public ArrayList<Integer> getmSelectedSongIds() {
        return mSelectedSongIds;
    }

    public void setmSelectedSongIds(ArrayList<Integer> mSelectedSongIds) {
        this.mSelectedSongIds = mSelectedSongIds;
    }

    public boolean addSelectedSongId(int id) {
        if (!mSelectedSongIds.contains(id)) {
            mSelectedSongIds.add(id);
            return true;
        }
        return false;
    }

    public boolean removeSelectedSongId(Integer id) {
        if (mSelectedSongIds.contains(id)) {
            mSelectedSongIds.remove(id);
            return true;
        }
        return false;
    }

    public boolean clearSelectedSongs() {
        if (null != mSelectedSongIds) {
            mSelectedSongIds.clear();
            return true;
        }
        return false;
    }

    public LocalMediaFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener;
    public void setmOnMediaFragmentSelectedListener(
            LocalMediaFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnMediaFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public RecentlyPlayFragment.OnMediaFragmentSelectedListener mOnRecentlyFragmentSelectedListener;
    public void setOnRecentlyFragmentSelectedListener(
            RecentlyPlayFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnRecentlyFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public PlaylistDetailFragment.OnMediaFragmentSelectedListener mOnPlayListFragmentSelectedListener;
    public void setOnPlayListFragmentSelectedListener(
            PlaylistDetailFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnPlayListFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public MyFavouriteMusicFragment.OnMediaFragmentSelectedListener mOnMyFavouriteFragmentSelectedListener;
    public void setOnMyFavouriteFragmentSelectedListener(
            MyFavouriteMusicFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnMyFavouriteFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public LocalAlbumDetailFragment.OnMediaFragmentSelectedListener mOnDetailFragmentSelectedListener;
    public void setmOnMediaFragmentSelectedListener(
            LocalAlbumDetailFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnDetailFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    /* MODIFIED-BEGIN by Kang.Ren, 2016-11-07,BUG-3373604*/
    public LocalAlbumListFragment.OnMediaFragmentSelectedListener mOnAlbumFragmentSelectedListener;
    public void setmOnAlbumFragmentSelectedListener(
            LocalAlbumListFragment.OnMediaFragmentSelectedListener mOnAlbumFragmentSelectedListener) {
        this.mOnAlbumFragmentSelectedListener = mOnAlbumFragmentSelectedListener;
    }
    /* MODIFIED-END by Kang.Ren,BUG-3373604*/

    public ArrayList<Integer> getmSelectedArtistIds() {
        return mSelectedArtistIds;
    }

    public void setmSelectedArtistIds(ArrayList<Integer> mSelectedSongIds) {
        this.mSelectedArtistIds = mSelectedSongIds;
    }

    public boolean addSelectedArtistIds(int id) {
        if (!mSelectedArtistIds.contains(id)) {
            mSelectedArtistIds.add(id);
            return true;
        }
        return false;
    }

    public boolean removeSelectedArtistIds(Integer id) {
        if (mSelectedArtistIds.contains(id)) {
            mSelectedArtistIds.remove(id);
            return true;
        }
        return false;
    }

    public boolean clearSelectedArtist() {
        if (null != mSelectedArtistIds) {
            mSelectedArtistIds.clear();
            return true;
        }
        return false;
    }

    public boolean addSelectedAlbumIds(int id) {
        if (!mSelectedAlbumIds.contains(id)) {
            mSelectedAlbumIds.add(id);
            return true;
        }
        return false;
    }

    public boolean removeSelectedAlbumIds(Integer id) {
        if (mSelectedAlbumIds.contains(id)) {
            mSelectedAlbumIds.remove(id);
            return true;
        }
        return false;
    }

    public boolean clearSelectedAlbumIds() {
        if (null != mSelectedAlbumIds) {
            mSelectedAlbumIds.clear();
            return true;
        }
        return false;
    }

    public ArrayList<Integer> getmSelectedAlbumIds() {
        return mSelectedAlbumIds;
    }

    public void setmSelectedAlbumIds(ArrayList<Integer> mSelectedAlbumIds) {
        this.mSelectedAlbumIds = mSelectedAlbumIds;
    }
}
