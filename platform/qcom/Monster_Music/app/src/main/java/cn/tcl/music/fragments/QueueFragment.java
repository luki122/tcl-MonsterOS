package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.framework.log.NLog;

import cn.tcl.music.R;
import cn.tcl.music.activities.PlayingActivity;
import cn.tcl.music.adapter.QueueAdapter;
import cn.tcl.music.adapter.RecyclerViewAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MixUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.view.QueueItemAnimator;

public class QueueFragment extends BaseRecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewAdapter
        .OnItemClickListener {
    public static final String TAG = QueueFragment.class.getSimpleName();
    private View mRootView;
    private TextView mSongTextView;
    private TextView mSingerTextView;
    private RelativeLayout mRelativeLayout;
    private int mPendingPosition = RecyclerView.NO_POSITION;
    private QueueAdapter mQueueAdapter;
    private long mCurrentPlaySongMediaID = -1;
    private long mClickItemTime = 0;
    private int durationPopup = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    protected boolean mSimpleAdapter = true;

    private void makeDismissInfoToast() {
        String text = getActivity().getString(R.string.songs_removed);
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mQueueAdapter != null) {
                    mQueueAdapter.clearDismissedInfos();
                    mQueueAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    public void refresh() {
        if (mQueueAdapter != null) {
            mQueueAdapter.notifyDataSetChanged();
        }
    }

    public void setSimpleAdapter(boolean isSimple) {
        mSimpleAdapter = isSimple;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageFetcher.setEmptyImageRes(R.drawable.default_cover_list);
        mImageFetcher.setImageSize(getResources().getDimensionPixelSize(R.dimen.image_artwork_row_size));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_queue, container, false);
        mRelativeLayout = (RelativeLayout) mRootView.findViewById(R.id.queue_ll);
        int statusBarHeight = SystemUtility.getStatusBarHeight();
        Log.d(TAG, "onViewCreated statusBarHeight = " + statusBarHeight);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, statusBarHeight, 0, 0);
        mSongTextView = (TextView) mRootView.findViewById(R.id.playing_song_name);
        mSingerTextView = (TextView) mRootView.findViewById(R.id.playing_singer_name);
        mRelativeLayout.setLayoutParams(layoutParams);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        onCurrentMediaMetaChanged(MusicPlayBackService.getArtistName(),MusicPlayBackService.getTitle());
        // refresh view when user change add types and so on in portrait fragment(back to playing activity)
        getLoaderManager().restartLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.VALUE_QUEUE_IS_EFFECTIVE)};
        return new CursorLoader(getActivity(), MusicMediaDatabaseHelper.Queue.CONTENT_URI_QUEUE_MEDIA,
                null, selection, selectionArgs, MusicMediaDatabaseHelper.Queue.QueueColumns.PLAY_ORDER);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mQueueAdapter == null) {
            Log.d(TAG, "onLoadFinished mAdapter == null ");
            QueueAdapter queueAdapter = new QueueAdapter(getActivity(), data, mImageFetcher);
            queueAdapter.setSimpleAdapter(mSimpleAdapter);
            setRecyclerAdapter(queueAdapter);
            mQueueAdapter = queueAdapter;
            mQueueAdapter.setOnItemClickListener(this);
        } else {
            mQueueAdapter.changeCursor(data);
            mQueueAdapter.setOnItemClickListener(this);
        }
        Cursor c = mQueueAdapter.getCursor();
        long currentID = MusicPlayBackService.getMediaID();
        for(int i=0; i<c.getCount(); i++){
            c.moveToPosition(i);
            if(c.getLong(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID)) == currentID){
                mPendingPosition = i;
                break;
            }
        }
        Log.i(TAG,"mPendingPosition ");
        if (mPendingPosition >= 0) {
            getRecyclerView().getLayoutManager().scrollToPosition(mPendingPosition);
            mPendingPosition = RecyclerView.NO_POSITION;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        NLog.i(TAG, "onLoaderReset");
        if (mQueueAdapter == null)
            return;
        mQueueAdapter.changeCursor(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRecyclerItemClick(RecyclerView.ViewHolder viewHolder, int position,
                                       View v) {
        super.onRecyclerItemClick(viewHolder, position, v);
        //find the true index of select media
        //step1:find the item _id of this adapter cursor
        //step2:get the index in queue table by the previous _id
        final Cursor cursor = mQueueAdapter.getCursorAtAdapterPosition(position);

    }

    @Override
    protected void onRecyclerViewCreated(RecyclerView rv) {
        super.onRecyclerViewCreated(rv);
        rv.setHasFixedSize(true);
        rv.setItemAnimator(new QueueItemAnimator());
    }

    @Override
    public void leaveMultiChoose() {

    }

    private void refreshView() {
//            mSongTextView.setText(MediaQueue.mCurrentMedia.getCurrentMedia().title);
//            mSingerTextView.setText(MediaQueue.mCurrentMedia.getCurrentMedia().artist);
    }

    private void removeItem(int position) {
        if (mQueueAdapter == null) {
            NLog.w(TAG, "removeItem() mAdapter==null");
            return;
        }
        long id = mQueueAdapter.getItemId(position);
        NLog.i(TAG, "removeItem() id == " + id);
        StringBuilder idsToDelete = new StringBuilder(BaseColumns._ID).append(" IN (");
        idsToDelete.append(id).append(" )");
        String selection = idsToDelete.toString();
    }


    /**
     * @param id 对应数据库PlaylistSongs表中的_id字段
     */
    public void removeSongFromPlayList(int id) {
        StringBuilder idsToDelete = new StringBuilder(BaseColumns._ID).append(" IN (");
        idsToDelete.append(id).append(" )");
        String selection = idsToDelete.toString();
    }


    @Override
    protected void onPopulatePopupMenu(MenuInflater menuInflater, Menu menu,
                                       RecyclerView.ViewHolder itemViewHolder, int position) {
        menuInflater.inflate(R.menu.queue_songs_menu, menu);
    }

    @Override
    protected boolean onPopupMenuItemSelected(MenuItem item, RecyclerView.ViewHolder itemViewHolder, int position) {
        NLog.d(TAG, "onPopupMenuItemSelected ");
        switch (item.getItemId()) {
            case R.id.action_remove_from_playlists: {
                LogUtil.d(TAG, "onPopupMenuItemSelected action_remove_from_playlists");
//                removeItem(position);
                return true;
            }
            case R.id.action_play_next: {
                LogUtil.d(TAG, "onPopupMenuItemSelected action_play_next");
                return true;
            }
            default: {
                NLog.d(TAG, "onPopupMenuItemSelected default");
//                Cursor c = mQueueAdapter.getCursorAtAdapterPosition(position);
                return false;
            }
        }
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position, View v) {
//        if (v.getId() == R.id.item_menu_image_button) {
//            viewHolder.itemView.setVisibility(View.GONE);
//            removeItem(position);
//        } else {
//            NLog.d(TAG, "mOnItemClickListener alreadyHandled mItemClickFromFragmentListener = " + mItemClickFromFragmentListener);
//            boolean alreadyHandled = false;
//            if (mItemClickFromFragmentListener != null) {
//                alreadyHandled = mItemClickFromFragmentListener.onRecyclerItemClick(QueueFragment.this, viewHolder, position, v);
//            }
//            if (!alreadyHandled)
//                onRecyclerItemClick(viewHolder, position, v);
//        }
        Cursor c = mQueueAdapter.getCursorAtAdapterPosition(position);
        MediaInfo info = MusicUtil.getMediaInfoFromCursor(c);
        if (info != null) {
            ((PlayingActivity) getActivity()).clickItem(info);
        }
    }

    public void onCurrentMediaMetaChanged(String singerName, String songName) {
        if (mQueueAdapter != null) {
            mQueueAdapter.notifyDataSetChanged();
        }
        if (null != singerName) {
            mSingerTextView.setText(singerName);
        }
        if (null != songName) {
            mSongTextView.setText(MixUtil.getSongNameWithNoSuffix(songName));
        }
    }
    public  void notifyFavouriteHasChanged(){
        getLoaderManager().restartLoader(0, null, this);
    }
}
