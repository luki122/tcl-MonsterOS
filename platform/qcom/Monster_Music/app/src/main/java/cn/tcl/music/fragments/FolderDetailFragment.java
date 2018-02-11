package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import cn.tcl.music.R;
import cn.tcl.music.adapter.IgnoredAdapter;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.database.QueueUtil;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.model.live.AddType;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.widget.ActionModeHandler;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

public class FolderDetailFragment extends BaseRecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static String TAG = FolderDetailFragment.class.getSimpleName();

    private RelativeLayout mShuffleAllLinear;
    private TextView mFolderDetailNumText;
    private TextView mEmptyTextView;
    private String mFolderId;
    private int mType;
    private LocalMediaAdapter mLocalMediaAdapter;
    private IgnoredAdapter mIgnoredAdapter;
    private ProgressDialog mProgressDialog;
    private FileTask mFileTask;

    private static final int MSG_ADD_SUCCESS = 0x01;
    private static final int MSG_ADD_FAILURE = 0x02;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_SUCCESS: {
                    String playlistName = (String) msg.obj;
                    if (ActionModeHandler.mActionMode != null) {
                        ActionModeHandler.mActionMode.finish();
                    }
                    mProgressDialog.dismiss();
                    if (getActivity().getString(R.string.my_favourite_music).equals(playlistName)) {
                        //if is add to favorite ,need to refresh current fragment
                        getLoaderManager().restartLoader(0, null, FolderDetailFragment.this);
                    }
                    ToastUtil.showToast(getActivity(), getActivity().getString(R.string.song_had_been_added_to_playlist, playlistName));
                }
                break;
                case MSG_ADD_FAILURE:
                    if (ActionModeHandler.mActionMode != null) {
                        ActionModeHandler.mActionMode.finish();
                    }
                    mProgressDialog.dismiss();
                    ToastUtil.showToast(getActivity(), getActivity().getString(R.string.operation_failed));
                    break;
                default:
                    break;
            }
        }
    };

    private MusicPlayBackService.MusicBinder mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = (MusicPlayBackService.MusicBinder) MusicPlayBackService.MusicBinder.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mType = bundle.getInt(CommonConstants.BUNDLE_KEY_FOLDER_TYPE);
        mFolderId = bundle.getString(CommonConstants.BUNDLE_KEY_FOLDER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_folder_detail, container, false);
        mShuffleAllLinear = (RelativeLayout) rootView.findViewById(R.id.rl_shuffle_all);
        mFolderDetailNumText = (TextView) rootView.findViewById(R.id.tv_shuffle_total_num);
        mEmptyTextView = (TextView) rootView.findViewById(android.R.id.empty);
        mEmptyTextView.setText(R.string.no_song_found);
        if (mType == CommonConstants.VALUE_FOLDER_IS_SCAN) {
            mShuffleAllLinear.setVisibility(View.VISIBLE);
            mShuffleAllLinear.setOnClickListener(this);
        } else if (mType == CommonConstants.VALUE_FOLDER_IS_NOT_SCAN) {
            mShuffleAllLinear.setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ? and " + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX), mFolderId};
        if (mType == CommonConstants.VALUE_FOLDER_IS_SCAN) {
            return new CursorLoader(getActivity(),
                    MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                    DBUtil.MEDIA_FOLDER_COLUMNS,
                    selection,
                    selectionArgs,
                    MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
        } else {
            return new CursorLoader(getActivity(),
                    MusicMediaDatabaseHelper.Media.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        if (mLocalMediaAdapter == null) {
            LogUtil.i(TAG, "FolderDetailFragment onLoadFinished  mAdapterType = NORMAL ");
            LogUtil.d(TAG, "mType : " + mType);
            if (mType == CommonConstants.VALUE_FOLDER_IS_SCAN) {
                mLocalMediaAdapter = new LocalMediaAdapter(getActivity(), data, mImageFetcher, false);
                setRecyclerAdapter(mLocalMediaAdapter);
            } else if (mType == CommonConstants.VALUE_FOLDER_IS_NOT_SCAN) {
                IgnoredAdapter ignoredAdapter = new IgnoredAdapter(getActivity(), data, mImageFetcher);
                setRecyclerAdapter(ignoredAdapter);
                mIgnoredAdapter = ignoredAdapter;
            }
        } else {
            mLocalMediaAdapter.changeCursor(data);
        }
        refreshTotalCount(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_shuffle_all:
                if(mLocalMediaAdapter == null){
                    return;
                }
                int count = mLocalMediaAdapter.getItemCount();
                if(count > 0){
                    PlayMode.setMode(getActivity(), PlayMode.PLAY_MODE_RANDOM);
                    clickItem(new Random().nextInt(count));
                }
                break;
        }
    }

    @Override
    protected void onPopulatePopupMenu(MenuInflater menuInflater, Menu menu, RecyclerView.ViewHolder itemViewHolder, final int position) {
        super.onPopulatePopupMenu(menuInflater, menu, itemViewHolder, position);
        final Cursor c = mLocalMediaAdapter.getCursorAtAdapterPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        View localMediaDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_local_media, null);
        builder.setView(localMediaDialogView);
        final AlertDialog mLocalMediaDialog = builder.create();

        mLocalMediaDialog.show();
        final MediaInfo info = MusicUtil.getMediaInfoFromCursor(c);
        menuInflater.inflate(R.menu.all_songs_menu, menu);

        View.OnClickListener dialogListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.action_add_to_playlist:
                        final ArrayList<Object> ids = new ArrayList<Object>();
                        ids.add(info.audioId);
                        DialogMenuUtils.displayAddToPlaylistDialog(getActivity(), new SimplePlaylistChooserAdapter.OnPlaylistChoiceListener() {

                            @Override
                            public void onPlaylistChosen(final Uri playlistUri, final String playlistName) {
                                showProgressDialog();
                                int result = 0;
                                if (playlistUri.toString().equals(MusicMediaDatabaseHelper.Playlists.FAVORITE_URI.toString())) {
                                    result = DBUtil.changeFavoriteWithIds(getActivity(), ids, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
                                } else {
                                    long playlistId = Long.valueOf(playlistUri.getLastPathSegment());
                                    result = DBUtil.addSongsToPlaylist(getActivity(), playlistId, ids);
                                }
                                if (result == 0) {
                                    mHandler.sendEmptyMessage(MSG_ADD_FAILURE);
                                } else {
                                    Message message = new Message();
                                    message.obj = playlistName;
                                    message.what = MSG_ADD_SUCCESS;
                                    mHandler.sendMessage(message);
                                }
                            }
                        }, R.string.add_to_playlist);
                        break;
                    case R.id.action_share:
                        PathUtils.shareLocalMedia(info, getContext());
                        break;
                    case R.id.action_remove:
                        deleteItem(position);
                        break;
                }
                mLocalMediaDialog.dismiss();
            }
        };
        TextView actionAddToPlaylist = (TextView) localMediaDialogView.findViewById(R.id.action_add_to_playlist);
        TextView actionShare = (TextView) localMediaDialogView.findViewById(R.id.action_share);
        TextView actionRemove = (TextView) localMediaDialogView.findViewById(R.id.action_remove);
        actionAddToPlaylist.setOnClickListener(dialogListener);
        actionShare.setOnClickListener(dialogListener);
        actionRemove.setOnClickListener(dialogListener);
    }

    @Override
    protected boolean onPopupMenuItemSelected(MenuItem item, RecyclerView.ViewHolder itemViewHolder, int position) {
//        Cursor curosr = ((RecyclerViewCursorAdapter<?>) mAdapter).getCursorAtAdapterPosition(position);
//
//        NLog.d(TAG, "onPopupMenuItemSelected position = " + position);
//        return LibraryNavigationUtil.manageMenuItem(LibraryNavigationUtil.ItemMediaType.SINGLE_MEDIA, item, getActivity(), curosr);
        return false;
    }

    @Override
    public void leaveMultiChoose() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        getActivity().bindService(new Intent(getActivity(), MusicPlayBackService.class), mConnection, Service.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        getActivity().unbindService(mConnection);
        super.onStop();
    }

    public void onCurrentMetaChanged() {
        mLocalMediaAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
//        if (mLocalMediaAdapter != null) {
//            mLocalMediaAdapter.unRegisterListener();
//        }
        super.onDestroy();
    }

    private void refreshTotalCount(Cursor data) {
        if (data == null || data.getCount() <= 0) {
            mFolderDetailNumText.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, 0, 0));
        } else {
            mFolderDetailNumText.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, data.getCount(), data.getCount()));
        }
    }

    /**
     * 条目点击事件
     *
     * @param position
     */
    public void clickItem(int position) {
        Log.d(TAG, "clickItem and position is " + position);
//        PlaylistManager.AddPlaylistParameter parameters = new PlaylistManager.AddPlaylistParameter();
//        parameters.addType = PlaylistManager.AddTypes.FOLDER;
//        parameters.id = Long.valueOf(mFolderId);
//        parameters.folder_name = mFolderName;
//        parameters.clearQueue = true;
//        //TODO check before play song and refresh the queue table
//        LibraryNavigationUtil.addAndPlayNow(parameters, position, getActivity(), MediaQueue.QueueMode.NORMAL);
//        MediaQueue.getInstance(getActivity()).setAddType(CommonConstants.ADD_TYPE_FOLDER);
        ArrayList<MediaInfo> infoArrayList = new ArrayList<MediaInfo>();
        Cursor cursor = mLocalMediaAdapter.getCursor();
        if (null != cursor) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
                infoArrayList.add(mediaInfo);
            }
            QueueUtil.addMediaToQueue(getActivity(), true, infoArrayList);
        }
        MediaInfo info = MusicUtil.getMediaInfoFromCursor(mLocalMediaAdapter.getCursorAtAdapterPosition(position));
        if (null != info) {
            if (mService != null) {
                try {
                    mService.playByMediaInfo(info);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            LogUtil.e(TAG, "though we make queue table again,but cannot find the clicked position media info");
        }
        AddType.setAddType(getActivity(),AddType.ADD_TYPE_FOLDER);
    }

    /**
     * 删除歌曲条目
     *
     * @param position
     */
    public void deleteItem(final int position) {
        LogUtil.d(TAG, "deleteItem and position is " + position);
        if (null == mLocalMediaAdapter) {
            LogUtil.d(TAG, "deleteItem and adapter is null");
        } else {
            LogUtil.d(TAG, "deleteItem and items count is " + mLocalMediaAdapter.getItemCount());
            AlertDialog.Builder dialog = new mst.app.dialog.AlertDialog.Builder(getActivity());
            dialog.setMessage(R.string.confirm_to_delete);
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Cursor cursour = mLocalMediaAdapter.getCursorAtAdapterPosition(position);
                    if (null != cursour) {
                        long mediaId = cursour.getLong(cursour.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
                        cursour.close();
                        Long[] ids = new Long[]{mediaId};
                        mFileTask = new FileTask();
                        mFileTask.execute(ids);
                    }
                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            dialog.show();
        }
    }

    private class FileTask extends AsyncTask<Long, Object, Boolean> {
        private Long[] mIds;

        public FileTask() {
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            mIds = params;
            StringBuffer where = new StringBuffer(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID + " IN (");
            for (int i = 0; i < mIds.length - 1; i++) {
                where.append(mIds[i] + ",");
            }
            where.append(mIds[mIds.length - 1] + ") ");
            Cursor c = getActivity().getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, DBUtil.MEDIA_FOLDER_COLUMNS, where.toString(), null, null);
            boolean result = false;
            if (!isCancelled()) {
                while (c.moveToNext()) {
                    result =  DialogMenuUtils.removeFromTableAndDeleteLocalFileIfNecessary(getActivity(), c);
                }
            }
            c.close();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            mLocalMediaAdapter.getmSelectedSongIds().clear();
            refreshData();
            if (result) {
                ToastUtil.showToast(getActivity(), R.string.songs_removed);
            } else {
                ToastUtil.showToast(getActivity(), R.string.operation_failed);
            }
        }
    }

    private void refreshData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.operating));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
}
