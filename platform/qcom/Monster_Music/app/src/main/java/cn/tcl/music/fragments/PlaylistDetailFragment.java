package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import cn.tcl.music.R;
import cn.tcl.music.activities.PlaylistDetailActivity;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.database.QueueUtil;
import cn.tcl.music.loaders.CommonCursorLoader;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.model.live.AddType;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.RemoveSongsDialog;
import cn.tcl.music.view.image.AsyncTask;
import cn.tcl.music.widget.ActionModeHandler;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

public class PlaylistDetailFragment extends BaseRecyclerViewFragment implements
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = PlaylistDetailFragment.class.getSimpleName();

    private String mPlayListId;
    private TextView mSongsNumTextView;
    public LinearLayout mBatchOperateLinearLayout;
    private RelativeLayout mBatchOperateDelete;
    private RelativeLayout mBatchOperateAddPlaylist;
    private RelativeLayout mPlayAllRelative;
    private TextView mTextViewPlayAll;
    private ImageView mImageViewPlayAll;
    private LocalMediaAdapter mLocalMediaAdapter;
    private OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener;
    private boolean mIsMultiMode = false;
    private ProgressDialog mProgressDialog;
    private ChangePlaylistTask mChangePlaylistTask;
    private DeleteFileTask mDeleteFileTask;
    private String mPlaylistArtwork;
    private static final String ARTWORK = "artwork";

    public String getPlaylistArtwork() {
        return mPlaylistArtwork;
    }

    public void setPlaylistArtwork(String playlistArtwork) {
        this.mPlaylistArtwork = playlistArtwork;
    }

    private static final int MSG_ADD_SUCCESS = 0x01;
    private static final int MSG_ADD_FAILURE = 0x02;
    private Handler mHandler = new Handler(){
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
                        getLoaderManager().restartLoader(0, null, PlaylistDetailFragment.this);
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
        mPlayListId = getArguments().getString(CommonConstants.BUNDLE_KEY_PLAYLIST_ID);
        if (getActivity() == null) {
            return;
        } else if (getActivity() instanceof PlaylistDetailActivity) {
            PlaylistDetailActivity playlistDetailActivity = (PlaylistDetailActivity) getActivity();
            setOnPlaylistDetailFragmentSelectedListener(playlistDetailActivity.mOnMediaFragmentSelectedListener);
        }
    }

    private void setOnPlaylistDetailFragmentSelectedListener(
            OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnMediaFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        mSongsNumTextView = (TextView) rootView.findViewById(R.id.detail_total_num_tv);
        mPlayAllRelative = (RelativeLayout) rootView.findViewById(R.id.detail_play_all_rl);
        mBatchOperateLinearLayout = (LinearLayout) rootView.findViewById(R.id.batch_operate_linearlayout);
        mBatchOperateDelete = (RelativeLayout) rootView.findViewById(R.id.batch_operate_delete);
        mBatchOperateAddPlaylist = (RelativeLayout) rootView.findViewById(R.id.batch_operate_addplaylist);
        mTextViewPlayAll = (TextView) rootView.findViewById(R.id.detail_play_all_tv);
        mImageViewPlayAll = (ImageView) rootView.findViewById(R.id.detail_play_all_image);
        mPlayAllRelative.setOnClickListener(this);
        mBatchOperateDelete.setOnClickListener(this);
        mBatchOperateAddPlaylist.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.detail_play_all_rl: {
                if(mLocalMediaAdapter == null){
                    return;
                }
                int count = mLocalMediaAdapter.getItemCount();
                if(count > 0){
                    PlayMode.setMode(getActivity(), PlayMode.PLAY_MODE_RANDOM);
                    clickItem(new Random().nextInt(count));
                }
            }
            break;
            case R.id.batch_operate_delete: {
                if (mLocalMediaAdapter.getmSelectedSongIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_media_items);
                } else {
                    batchDeleteSongs();
                }
            }
            break;
            case R.id.batch_operate_addplaylist:
                if (mLocalMediaAdapter.getmSelectedSongIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_media_items);
                } else {
                    batchAddToPlayList();
                }
                break;
        }
    }

    /**
     * 批量删除歌曲
     */
    private void batchDeleteSongs() {
        RemoveSongsDialog removeSongsDialog = new RemoveSongsDialog(getActivity(), true);
        removeSongsDialog.setOnRemoveSongsDialogOkClickListener(new RemoveSongsDialog.OnRemoveSongsDialogOkClickListener() {
            @Override
            public void onOkClick(boolean shouldKeepIt) {
                Long[] ids = new Long[mLocalMediaAdapter.getmSelectedSongIds().size()];
                for (int i = 0; i < mLocalMediaAdapter.getmSelectedSongIds().size(); i++) {
                    LogUtil.d(TAG, "batchDeleteSongs and id is " + mLocalMediaAdapter.getmSelectedSongIds().get(i));
                    ids[i] = Long.valueOf(mLocalMediaAdapter.getmSelectedSongIds().get(i));
                }
                if (shouldKeepIt) {
                    mChangePlaylistTask = new ChangePlaylistTask();
                    mChangePlaylistTask.execute(ids);
                } else {
                    mDeleteFileTask = new DeleteFileTask();
                    mDeleteFileTask.execute(ids);
                }
                leaveMultiChoose();
            }
        });
        removeSongsDialog.createRemoveDialog().show();
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
                    case R.id.action_download:

                        break;
                }
                mLocalMediaDialog.dismiss();
            }
        };
        TextView actionAddToPlaylist = (TextView) localMediaDialogView.findViewById(R.id.action_add_to_playlist);
        TextView actionShare = (TextView) localMediaDialogView.findViewById(R.id.action_share);
        TextView actionRemove = (TextView) localMediaDialogView.findViewById(R.id.action_remove);
        TextView actionDownload = (TextView) localMediaDialogView.findViewById(R.id.action_download);
        actionAddToPlaylist.setOnClickListener(dialogListener);
        actionShare.setOnClickListener(dialogListener);
        actionRemove.setOnClickListener(dialogListener);
        actionDownload.setOnClickListener(dialogListener);
        actionDownload.setVisibility(View.VISIBLE);
        if (info.sourceType < CommonConstants.SRC_TYPE_DEEZER) {
            actionDownload.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        } else {
            actionDownload.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        }
    }

    public void clickItem(int position) {
        int type = AddType.getAddType(getActivity());
        long id = AddType.getPlayListID(getActivity());
        if (type == AddType.ADD_TYPE_LOCAL_MUSIC && id == Long.parseLong(mPlayListId) && mLocalMediaAdapter.getItemCount() == QueueUtil.getQueuePlayableCount(getActivity()) && QueueUtil.getQueuePlayableCount(getActivity()) > 0 && MusicPlayBackService.getCurrentMediaInfo() != null) {
            MediaInfo info = MusicUtil.getMediaInfoFromCursor(mLocalMediaAdapter.getCursorAtAdapterPosition(position));
            if (info == null) {
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
                info = MusicUtil.getMediaInfoFromCursor(mLocalMediaAdapter.getCursorAtAdapterPosition(position));
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
            } else {
                if (mService != null) {
                    try {
                        mService.playByMediaInfo(info);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
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
            MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(mLocalMediaAdapter.getCursorAtAdapterPosition(position));
            if (null != mediaInfo) {
                if (mService != null) {
                    try {
                        mService.playByMediaInfo(mediaInfo);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            AddType.setAddType(getActivity(), AddType.ADD_TYPE_LOCAL_MUSIC);
            AddType.setPlayListID(getActivity(), Long.parseLong(mPlayListId));
        }
    }

    public void deleteItem(final int position) {
        RemoveSongsDialog removeSongsDialog = new RemoveSongsDialog(getActivity(), true);
        removeSongsDialog.setOnRemoveSongsDialogOkClickListener(new RemoveSongsDialog.OnRemoveSongsDialogOkClickListener() {
            @Override
            public void onOkClick(boolean shouldKeepIt) {
                if (null == mLocalMediaAdapter) {
                    LogUtil.d(TAG, "deleteItem and adapter is null");
                } else {
                    LogUtil.d(TAG, "deleteItem and items count is " + mLocalMediaAdapter.getItemCount());
                    Cursor cursour = mLocalMediaAdapter.getCursorAtAdapterPosition(position);
                    if (null != cursour) {
                        long mediaId = cursour.getLong(cursour.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
                        cursour.close();
                        Long[] ids = new Long[]{mediaId};
                        if (shouldKeepIt) {
                            mChangePlaylistTask = new ChangePlaylistTask();
                            mChangePlaylistTask.execute(ids);
                        } else {
                            mDeleteFileTask = new DeleteFileTask();
                            mDeleteFileTask.execute(ids);
                        }
                    }
                }
            }
        });
        removeSongsDialog.createRemoveDialog().show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String section = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ? ";
        String[] selectionArgs = new String[]{mPlayListId};
        return new CommonCursorLoader(getActivity(),
                MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI_PLAYLISTSONGS_MEDIA,
                DBUtil.MEDIA_PLAYLIST_COLUMNS,
                section,
                selectionArgs,
                MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d(TAG, "data count is " + data.getCount());
        if (mLocalMediaAdapter == null) {
            mLocalMediaAdapter = new LocalMediaAdapter(getActivity(), data, mImageFetcher, false);
            setRecyclerAdapter(mLocalMediaAdapter);
        } else {
            mLocalMediaAdapter.changeCursor(data);
        }
        PlaylistDetailActivity playlistDetailActivity = (PlaylistDetailActivity) getActivity();
        mLocalMediaAdapter.setOnPlayListFragmentSelectedListener(playlistDetailActivity.mOnMediaFragmentSelectedListener);
        if(data.moveToFirst()) {
            setPlaylistArtwork(data.getString(data.getColumnIndex(ARTWORK)));
            playlistDetailActivity.getArtwork();
        }
        refreshTotalCount(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mLocalMediaAdapter != null) {
            mLocalMediaAdapter.changeCursor(null);
        }
    }

    private void refreshTotalCount(Cursor data) {
        if (data == null || data.getCount() <= 0) {
            mSongsNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, 0, 0));
        } else {
            mSongsNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, data.getCount(), data.getCount()));
        }
    }

    @Override
    public void onDestroy() {
//        if (mLocalMediaAdapter != null) {
//            mLocalMediaAdapter.unRegisterListener();
//        }
        super.onDestroy();
    }

    public void setMultiMode(boolean isMultiMode) {
        if (null != mLocalMediaAdapter) {
            mLocalMediaAdapter.setMultiMode(isMultiMode, -1);
            mIsMultiMode = isMultiMode;
        }
    }

    public void showBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
    }

    public void hideBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
    }

    public void noclickableplayall() {
        mPlayAllRelative.setClickable(false);
        mTextViewPlayAll.setAlpha(0.40f);
        mImageViewPlayAll.setAlpha(0.40f);
    }

    public void clickableplayall() {
        mPlayAllRelative.setClickable(true);
        mTextViewPlayAll.setAlpha(1f);
        mImageViewPlayAll.setAlpha(1f);
    }

    public void selectAll(boolean isSelect) {
        if (null != mLocalMediaAdapter) {
            mLocalMediaAdapter.setSelectAll(isSelect);
            mLocalMediaAdapter.notifyDataSetChanged();
        }
    }

    public ArrayList<Long> getSongsIds() {
        Cursor cursor = mLocalMediaAdapter.getCursor();
        ArrayList<Long> ids = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
                ids.add(mediaInfo.audioId);
            }
            cursor.close();
        }
        return ids;
    }

    public void refreshData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    private void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(getActivity().getString(R.string.operating));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
//            mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialogInterface) {
//                    if (mChangePlaylistTask != null) {
//                        mChangePlaylistTask.cancel(true);
//                    }
//                }
//            });
        }
        mProgressDialog.show();
    }

    private class ChangePlaylistTask extends AsyncTask<Long, Object, Boolean> {
        private Long[] mIDs;
        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            LogUtil.d(TAG, "ChangePlaylistTask and params count is " + params.length);
            mIDs = params;
            String where = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " in ";
            ArrayList<Object> arrayList = new ArrayList<Object>();
            for (Long id : params) {
                arrayList.add(id);
            }
            where += "(";
            where += Util.translateArrayToString(arrayList, ",");
            where += ")";
            where += " and " + MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = " + mPlayListId;
            LogUtil.d(TAG, "ChangePlaylistTask and where is " + where);
            return (getActivity().getContentResolver().delete(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, where, null) != 0);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            LogUtil.d(TAG, "ChangePlaylistTask and onPostExecute result is " + result);
            mProgressDialog.dismiss();
            refreshData();
            if (!result) {
                ToastUtil.showToast(getActivity(), R.string.operation_failed);
            } else {
                ToastUtil.showToast(getActivity(), R.string.songs_removed);
            }
        }
    }

    private class DeleteFileTask extends android.os.AsyncTask<Long, Object, Boolean> {
        private Long[] mIds;

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
            where.append(mIds[mIds.length - 1]+") ");
            Cursor c = getActivity().getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, DBUtil.MEDIA_FOLDER_COLUMNS, where.toString(), null, null);
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
    public void leaveMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
        clickableplayall();
        setMultiMode(false);
        ((PlaylistDetailActivity) getActivity()).showActionMode(false);
    }

    public boolean isSelectAll() {
        return mLocalMediaAdapter.isSelectAll();
    }
}

