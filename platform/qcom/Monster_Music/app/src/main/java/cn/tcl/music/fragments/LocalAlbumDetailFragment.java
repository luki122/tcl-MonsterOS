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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.framework.log.NLog;

import java.util.ArrayList;
import java.util.Random;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumDetailActivity;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.adapter.RecyclerViewCursorAdapter;
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

public class LocalAlbumDetailFragment extends BaseRecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static String TAG = LocalAlbumDetailFragment.class.getSimpleName();

    private RelativeLayout mPlayAllRelat;
    private TextView mPlayAllTextView;
    private ImageView mPlayAllImageView;
    private TextView mDetailNumTextView;
    private boolean mIsMultiMode = false;
    private LocalMediaAdapter mLocalMediaAdapter;
    private String mDetailId = "";
    private LocalAlbumDetailActivity detailActivity;
    public LinearLayout mBatchOperateLinearLayout;
    private RelativeLayout mBatchOperateDeleteLayout;
    private RelativeLayout mBatchOperateAddPlaylistLayout;
    private ProgressDialog mProgressDialog;
    private FileTask mFileTask;
    private static final int MSG_ADD_SUCCESS = 0x01;
    private static final int MSG_ADD_FAILURE = 0x02;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_SUCCESS: {
                    String playlistName = (String) msg.obj;
                    if (ActionModeHandler.mActionMode != null) {
                        leaveMultiChoose();
                    }
                    mProgressDialog.dismiss();
                    if (getActivity().getString(R.string.my_favourite_music).equals(playlistName)) {
                        //如果是添加到喜欢，那么需要刷新当前界面数据
                        getLoaderManager().restartLoader(0, null, LocalAlbumDetailFragment.this);
                    }
                    ToastUtil.showToast(getActivity(), getActivity().getString(R.string.song_had_been_added_to_playlist, playlistName));
                }
                break;
                case MSG_ADD_FAILURE:
                    if (ActionModeHandler.mActionMode != null) {
                        leaveMultiChoose();
                    }
                    mProgressDialog.dismiss();
                    ToastUtil.showToast(getActivity(), getActivity().getString(R.string.operation_failed));
                    break;
                default:
                    break;
            }
        }
    };

    public interface OnMediaFragmentSelectedListener {
        void onAudioSelectdNum(ArrayList<Integer> songIds);
    }

    OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener;
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
        mDetailId = bundle.getString(CommonConstants.BUNDLE_KEY_ALBUM_ID);
        LogUtil.i(TAG, "detail id = " + mDetailId);
        if (getActivity() == null) {
            return;
        } else if (getActivity() instanceof LocalAlbumDetailActivity) {
            LocalAlbumDetailActivity detailActivity = (LocalAlbumDetailActivity) getActivity();
            setmOnMediaFragmentSelectedListener(detailActivity.OnMediaFragmentSelectedListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_detail, container, false);
        mPlayAllRelat = (RelativeLayout) rootView.findViewById(R.id.detail_play_all_rl);
        mDetailNumTextView = (TextView) rootView.findViewById(R.id.detail_total_num_tv);
        mBatchOperateLinearLayout = (LinearLayout) rootView.findViewById(R.id.batch_operate_linearlayout);
        mBatchOperateDeleteLayout = (RelativeLayout) rootView.findViewById(R.id.batch_operate_delete);
        mBatchOperateAddPlaylistLayout = (RelativeLayout) rootView.findViewById(R.id.batch_operate_addplaylist);
        mPlayAllTextView = (TextView) rootView.findViewById(R.id.detail_play_all_tv);
        mPlayAllImageView = (ImageView) rootView.findViewById(R.id.detail_play_all_image);
        mPlayAllRelat.setOnClickListener(this);
        mBatchOperateDeleteLayout.setOnClickListener(this);
        mBatchOperateAddPlaylistLayout.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.d(TAG, "mDetailId is " + mDetailId + " and artist id is " + getArguments().getString(CommonConstants.BUNDLE_KEY_ARTIST_ID));
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ? and " + MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID + " = ?  and "
                + MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX), mDetailId, getArguments().getString(CommonConstants.BUNDLE_KEY_ARTIST_ID)};

        String orderBy = MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY;
        return new CursorLoader(getActivity(),
                MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                DBUtil.MEDIA_FOLDER_COLUMNS,
                selection,
                selectionArgs,
                orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        if (mLocalMediaAdapter == null) {
            NLog.i(TAG, "FolderDetailFragment onLoadFinished  mAdapterType = NORMAL ");
            mLocalMediaAdapter = new LocalMediaAdapter(getActivity(), data, mImageFetcher, mIsMultiMode);
            if (getActivity() instanceof LocalAlbumDetailActivity) {
                LocalAlbumDetailActivity detailActivity = (LocalAlbumDetailActivity) getActivity();
                mLocalMediaAdapter.setmOnMediaFragmentSelectedListener(detailActivity.OnMediaFragmentSelectedListener);
            }
            setRecyclerAdapter(mLocalMediaAdapter);
        } else {
            mLocalMediaAdapter.changeCursor(data);
        }
        if (data == null || data.getCount() <= 0) {
            manageEmptyView(true);
            detailActivity = (LocalAlbumDetailActivity) getActivity();
            detailActivity.showEmpty();
        } else {
            manageEmptyView(false);
            detailActivity = (LocalAlbumDetailActivity) getActivity();
            detailActivity.hideEmpty();
        }
        refreshTotalCount(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_play_all_rl:
                if (mLocalMediaAdapter == null) {
                    return;
                }
                int count = mLocalMediaAdapter.getItemCount();
                if (count > 0) {
                    PlayMode.setMode(getActivity(), PlayMode.PLAY_MODE_RANDOM);
                    clickItem(new Random().nextInt(count));
                }
                break;
            case R.id.batch_operate_delete:
                if (mLocalMediaAdapter.getmSelectedSongIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_media_items);
                } else {
                    LogUtil.d(TAG, "batch_operate_delete and select items count is " + mLocalMediaAdapter.getmSelectedSongIds().size());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                    dialog.setMessage(R.string.confirm_to_delete);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            batchDeleteSongs();
                            leaveMultiChoose();
                        }
                    });
                    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();

                }
                break;
            case R.id.batch_operate_addplaylist:
                if (mLocalMediaAdapter.getmSelectedSongIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_media_items);
                } else {
                    batchAddToPlayList();
//                    leaveMultiChoose();
                }
                break;
        }
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


    private void refreshTotalCount(Cursor data) {
        if (data == null || data.getCount() <= 0) {
            mDetailNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, 0, 0));
        } else {
            mDetailNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, data.getCount(), data.getCount()));
        }
    }

    /**
     * 条目点击事件
     *
     * @param position
     */
    public void clickItem(int position) {
//        Log.d(TAG, "clickItem and position is " + position);
//        PlaylistManager.AddPlaylistParameter parameters = new PlaylistManager.AddPlaylistParameter();
//        parameters.addType = PlaylistManager.AddTypes.ALBUM;
//        parameters.id = Long.valueOf(mDetailId);
//        parameters.clearQueue = true;
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
        AddType.setAddType(getActivity(), AddType.ADD_TYPE_ALBUM);
    }

    public void deleteItem(final int position) {
        Log.d(TAG, "deleteItem and position is " + position);
        if (null == mLocalMediaAdapter) {
            Log.d(TAG, "deleteItem and adapter is null");
        } else {
            Log.d(TAG, "deleteItem and items count is " + mLocalMediaAdapter.getItemCount());
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            dialog.setMessage(R.string.confirm_to_delete);
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Cursor cursour = ((RecyclerViewCursorAdapter<?>) mLocalMediaAdapter).getCursorAtAdapterPosition(position);
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

    @Override
    protected void onPopulatePopupMenu(MenuInflater menuInflater, Menu menu, RecyclerView.ViewHolder itemViewHolder,
                                       final int position) {
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
        return false;
    }

    @Override
    public void onDestroy() {
//        if (mLocalMediaAdapter != null) {
//            mLocalMediaAdapter.unRegisterListener();
//        }
        super.onDestroy();
        mOnMediaFragmentSelectedListener = null;
    }

    private void setmOnMediaFragmentSelectedListener(
            OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnMediaFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public void setMultiMode(boolean isMultiMode) {
        mLocalMediaAdapter.setMultiMode(isMultiMode, -1);
        mIsMultiMode = isMultiMode;
    }

    public void showBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
    }

    public void hideBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
    }

    public void noclickableplayall() {
        mPlayAllRelat.setClickable(false);
        mPlayAllTextView.setAlpha(0.40f);
        mPlayAllImageView.setAlpha(0.40f);
    }

    public void clickableplayall() {
        mPlayAllRelat.setClickable(true);
        mPlayAllTextView.setAlpha(1f);
        mPlayAllImageView.setAlpha(1f);
    }

    public void selectAll(boolean isSelect) {
        if (null != mLocalMediaAdapter) {
            mLocalMediaAdapter.setSelectAll(isSelect);
            mLocalMediaAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 批量删除歌曲
     */
    private void batchDeleteSongs() {
        Long[] ids = new Long[mLocalMediaAdapter.getmSelectedSongIds().size()];
        for (int i = 0; i < mLocalMediaAdapter.getmSelectedSongIds().size(); i++) {
            Log.d(TAG, "batchDeleteSongs and id is " + mLocalMediaAdapter.getmSelectedSongIds().get(i));
            ids[i] = Long.valueOf(mLocalMediaAdapter.getmSelectedSongIds().get(i));
        }
        mFileTask = new FileTask();
        mFileTask.execute(ids);
    }

    /**
     * 批量添加到歌单
     */
    private void batchAddToPlayList() {
        final ArrayList<Object> ids = new ArrayList<Object>();
        for (int i = 0; i < mLocalMediaAdapter.getmSelectedSongIds().size(); i++) {

            ids.add(mLocalMediaAdapter.getmSelectedSongIds().get(i));
        }
        SimplePlaylistChooserAdapter.OnPlaylistChoiceListener onPlaylistChoiceListener = new SimplePlaylistChooserAdapter.OnPlaylistChoiceListener() {

            @Override
            public void onPlaylistChosen(final Uri playlistUri, final String playlistName) {
                LogUtil.d(TAG, "playlistUri is " + playlistUri);
                showProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = 0;
                        if (MusicMediaDatabaseHelper.Playlists.FAVORITE_URI.equals(playlistUri)) {
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
                }).start();


            }
        };
        DialogMenuUtils.displayAddToPlaylistDialog(getActivity(), onPlaylistChoiceListener, R.string.add_to_playlist);
    }


    /**
     * 文件处理任务
     */
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
//            MediaQueue.getInstance(getActivity()).doAfterRemoveSongFromAPlaylist(getActivity(), CommonConstants.ADD_TYPE_LOCAL_MUSIC, true, mIds);
            Uri mediaStoreUri = MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED;
            StringBuffer where = new StringBuffer(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID + " IN (");
            for (int i = 0; i < mIds.length - 1; i++) {
                where.append(mIds[i] + ",");
            }
            where.append(mIds[mIds.length - 1] + ") ");
            Cursor c = getActivity().getContentResolver().query(mediaStoreUri, DBUtil.MEDIA_FOLDER_COLUMNS, where.toString(), null, null);
            boolean result = false;
            if (!isCancelled()) {
                while (c.moveToNext()) {
                    result = DialogMenuUtils.removeFromTableAndDeleteLocalFileIfNecessary(getActivity(), c);
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

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        mProgressDialog.setProgressStyle(mProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.operating));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mFileTask != null && mFileTask.getStatus() != AsyncTask.Status.FINISHED) {
                    mFileTask.cancel(true);
                }
            }
        });
        mProgressDialog.show();
    }

    private void refreshData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public void goToMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        ((LocalAlbumDetailActivity) getActivity()).showActionMode(true);
        ((LocalAlbumDetailActivity) getActivity()).getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void leaveMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
        clickableplayall();
        setMultiMode(false);
        ((LocalAlbumDetailActivity) getActivity()).showActionMode(false);
    }

    public boolean isSelectAll() {
        return mLocalMediaAdapter.isSelectAll();
    }
}