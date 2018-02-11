package cn.tcl.music.fragments;

import android.Manifest;
import android.app.LoaderManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import cn.tcl.music.R;
import cn.tcl.music.activities.DownloadManagerActivity;
import cn.tcl.music.activities.LocalMusicActivity;
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
import cn.tcl.music.util.PermissionsUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.ContextMenuReyclerView;
import cn.tcl.music.widget.ActionModeHandler;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

public class LocalMediaFragment extends BaseRecyclerViewFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = LocalMediaFragment.class.getSimpleName();
    private boolean mIsCheckPermission = true;
    private TextView mEmptyView;
    private TextView mLocalMediaTotalNumTextView;
    private TextView mTextViewPlayAll;
    private ImageView mImageViewPlayAll;
    private RelativeLayout mPlayAllLayout;
    private LinearLayout mBatchOperateLinearLayout;
    private RelativeLayout mBatchOperateDelete;
    private RelativeLayout mBatchOperateAddPlaylist;
    private RelativeLayout mDownloadManager;
    private ContextMenuReyclerView mLocalMediaReyclerView;
    private LocalMediaAdapter mLocalMediaAdapter;
    private boolean mIsMultiMode = false;
    public static boolean DISPLAY_SORT_OPTIONS = true;
    private OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener;
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
                        leaveMultiChoose();
                    }
                    mProgressDialog.dismiss();
                    if (getActivity().getString(R.string.my_favourite_music).equals(playlistName)) {
                        //如果是添加到喜欢，那么需要刷新当前界面数据
                        getLoaderManager().restartLoader(0, null, LocalMediaFragment.this);
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

    public boolean isSelectAll() {
        return mLocalMediaAdapter.isSelectAll();
    }

    public interface OnMediaFragmentSelectedListener {
        void onAudioSelectdNum(ArrayList<Integer> songIds);
    }

    public LocalMediaFragment() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null) {
            return;
        } else if (getActivity() instanceof LocalMusicActivity) {
            LocalMusicActivity localMusicActivity = (LocalMusicActivity) getActivity();
            setmOnMediaFragmentSelectedListener(localMusicActivity.OnMediaFragmentSelectedListener);
            getActivity().bindService(new Intent(getActivity(), MusicPlayBackService.class), mConnection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mIsCheckPermission = PermissionsUtil.checkSelfPermission(LocalMediaFragment.this.getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        View rootView = inflater.inflate(R.layout.fragment_local_media, container, false);
        mPlayAllLayout = (RelativeLayout) rootView.findViewById(R.id.rl_play_all);
        RelativeLayout rlDownloadManager = (RelativeLayout) rootView.findViewById(R.id.rl_download_manager);
        mEmptyView = (TextView) rootView.findViewById(android.R.id.empty);
        mLocalMediaTotalNumTextView = (TextView) rootView.findViewById(R.id.tv_local_media_total_num);
        mTextViewPlayAll = (TextView) rootView.findViewById(R.id.tv_play_all);
        mImageViewPlayAll = (ImageView) rootView.findViewById(R.id.play_all_image);
        mPlayAllLayout.setOnClickListener(this);
        rlDownloadManager.setOnClickListener(this);
        if (!mIsCheckPermission) {
            mEmptyView.setText(R.string.permission_tips3);
            mEmptyView.setTextColor(ContextCompat.getColor(getContext(), R.color.base));
        } else {
            mEmptyView.setText(R.string.no_song_found);
        }
        mBatchOperateLinearLayout = (LinearLayout) rootView.findViewById(R.id.batch_operate_linearlayout);
        mBatchOperateDelete = (RelativeLayout) rootView.findViewById(R.id.batch_operate_delete);
        mBatchOperateAddPlaylist = (RelativeLayout) rootView.findViewById(R.id.batch_operate_addplaylist);
        mDownloadManager = (RelativeLayout) rootView.findViewById(R.id.rl_download_manager);
        mBatchOperateDelete.setOnClickListener(this);
        mBatchOperateAddPlaylist.setOnClickListener(this);
        mLocalMediaReyclerView = (ContextMenuReyclerView) rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mLocalMediaAdapter == null) {
            mLocalMediaAdapter = new LocalMediaAdapter(getActivity(), null, mImageFetcher, false);
        }
        setRecyclerAdapter(mLocalMediaAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onRecyclerItemClick(RecyclerView.ViewHolder viewHolder, int position, View v) {

    }

    /**
     * 条目点击事件
     *
     * @param position
     */
    public void clickItem(int position) {
        LogUtil.d(TAG, "position is " + position);
        //Step1:判断当前的播放模式：如果不是全部本地歌曲，进入Step2，否则进入Step3
        //Step2:清空当前的queue表，然后把全部本地歌曲加入到播放列表中，从点击的position开发播放
        //Step3:判断Queue表的总歌曲数是否等于全部本地歌曲数，如果不等于则需要清空Queue表重新生成，如果等于，则判断一下当前点击歌曲的mediaId是否在Queue表中
        //Step4:如果不在Queue表中，需要清空Queue表重新生成，否则直接切换index播放
        if (getActivity() == null) {
            return;
        }
        int type = AddType.getAddType(getActivity());
        if (type == AddType.ADD_TYPE_LOCAL_MUSIC && mLocalMediaAdapter.getItemCount() == QueueUtil.getQueuePlayableCount(getActivity()) && QueueUtil.getQueuePlayableCount(getActivity()) > 0 && MusicPlayBackService.getCurrentMediaInfo() != null) {
            MediaInfo info = MusicUtil.getMediaInfoFromCursor(mLocalMediaAdapter.getCursorAtAdapterPosition(position));
            if (null != info) {
                ArrayList<MediaInfo> infoArrayList = new ArrayList<MediaInfo>();
                Cursor cursor = mLocalMediaAdapter.getCursor();
                if (null != cursor) {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
                        infoArrayList.add(mediaInfo);
                    }
                    QueueUtil.addMediaToQueue(getActivity(), true, infoArrayList);
                    if (mService != null) {
                        try {
                            mService.playByMediaInfo(info);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LogUtil.e(TAG, "though we make queue table again,but cannot find the clicked position media info");
                    }
                }
            } else {
                //TODO 歌曲已经不存在media表中
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
                        Log.i(TAG, "action_add_to_playlist");
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
                        Log.i(TAG, "action_remove");
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
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
                }
                break;
            case R.id.rl_play_all:
                int count = mLocalMediaAdapter.getItemCount();
                if(count > 0){
                    PlayMode.setMode(getActivity(),PlayMode.PLAY_MODE_RANDOM);
                    clickItem(new Random().nextInt(count));
                }
                break;
            case R.id.rl_download_manager: {
                Intent intent = new Intent(getActivity(), DownloadManagerActivity.class);
                startActivity(intent);
            }
            break;
            default:
                break;
        }
    }

    /**
     * 批量删除歌曲
     */
    private void batchDeleteSongs() {
        Long[] ids = new Long[mLocalMediaAdapter.getmSelectedSongIds().size()];
        for (int i = 0; i < mLocalMediaAdapter.getmSelectedSongIds().size(); i++) {
            LogUtil.d(TAG, "batchDeleteSongs and id is " + mLocalMediaAdapter.getmSelectedSongIds().get(i));
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ? ";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX)};
        return new CursorLoader(getActivity(), MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                DBUtil.MEDIA_FOLDER_COLUMNS, selection, selectionArgs, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d(TAG, "data count is " + data.getCount());
        if (data.getCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
        if (mLocalMediaAdapter == null) {
            mLocalMediaAdapter = new LocalMediaAdapter(getActivity(), data, mImageFetcher, false);
            setRecyclerAdapter(mLocalMediaAdapter);
        } else {
            mLocalMediaAdapter.changeCursor(data);
        }
        LocalMusicActivity localMusicActivity = (LocalMusicActivity) getActivity();
        mLocalMediaAdapter.setmOnMediaFragmentSelectedListener(localMusicActivity.OnMediaFragmentSelectedListener);
        refreshTotalCount(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * 隐藏下载管理
     */
    public void hideDownloadManager() {
        mDownloadManager.setAlpha(0.40f);
        mDownloadManager.setClickable(false);
    }

    /**
     * 显示下载管理
     */
    public void showDownloadManager() {
        mDownloadManager.setAlpha(1f);
        mDownloadManager.setClickable(true);
    }

    /**
     * 无法点击随机播放全部
     */
    public void noclickableplayall() {
        mPlayAllLayout.setClickable(false);
        mTextViewPlayAll.setAlpha(0.40f);
        mImageViewPlayAll.setAlpha(0.40f);
    }

    /**
     * 恢复点击随机播放全部
     */
    public void clickableplayall() {
        mPlayAllLayout.setClickable(true);
        mTextViewPlayAll.setAlpha(1f);
        mImageViewPlayAll.setAlpha(1f);
    }

    public void showBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
    }

    public void hideBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
    }

    public void setMultiMode(boolean isMulitMode, int firstSongId) {
        if (null != mLocalMediaAdapter) {
            mLocalMediaAdapter.setMultiMode(isMulitMode, firstSongId);
            mIsMultiMode = isMulitMode;
            DISPLAY_SORT_OPTIONS = !isMulitMode;
        }
    }

    public void refreshData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public void selectAll(boolean isSelect) {
        mLocalMediaAdapter.setSelectAll(isSelect);
        mLocalMediaAdapter.notifyDataSetChanged();
    }

    private void setmOnMediaFragmentSelectedListener(
            OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnMediaFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public void deleteItem(final int position) {
        LogUtil.d(TAG, "deleteItem and position is " + position);
        if (null == mLocalMediaAdapter) {
            LogUtil.d(TAG, "deleteItem and adapter is null");
        } else {
            LogUtil.d(TAG, "deleteItem and items count is " + mLocalMediaAdapter.getItemCount());
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            dialog.setMessage(R.string.confirm_to_delete);
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Cursor cursour = mLocalMediaAdapter.getCursorAtAdapterPosition(position);
                    if (null != cursour) {
                        long mediaId = cursour.getLong(cursour.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unbindService(mConnection);
        } catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void onCurrentMetaChanged() {
        mLocalMediaAdapter.notifyDataSetChanged();
    }

    /**
     * 文件处理任务
     */
    private class FileTask extends AsyncTask<Long, Object, Boolean> {
        private Long[] mIds;
        private boolean isPlaying;
        private boolean containCurrentPlayMusic;
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
            long currentMediaID = MusicPlayBackService.getMediaID();
            for (int i = 0; i < mIds.length - 1; i++) {
                where.append(mIds[i] + ",");
                if(Long.compare(mIds[i],currentMediaID) == 0){
                    containCurrentPlayMusic =true;
                }
            }
            where.append(mIds[mIds.length - 1] + ") ");
            if(Long.compare(mIds[mIds.length - 1],currentMediaID) == 0){
                containCurrentPlayMusic = true;
            }
            LogUtil.i(TAG,"containCurrentPlayMusic " + (containCurrentPlayMusic?"yes ":"no "));
            if(containCurrentPlayMusic){
                if(MusicPlayBackService.isPlaying()){
                    isPlaying = true;
                    try {
                        mService.pause();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    isPlaying = false;
                }

            } else {
                try {
                    mService.setToCompletion(false);
                } catch (Exception e){

                }
            }
            QueueUtil.clearAllIneffectiveSongInQueue(getActivity());
            String updateQueue = where.toString().replaceFirst(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID, MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE,CommonConstants.VALUE_QUEUE_IS_NOT_EFFECTIVE);
            boolean result = false;
            int count = getActivity().getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI,
                    null,
                    MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE + " = ?",
                    new String[]{String.valueOf(CommonConstants.VALUE_QUEUE_IS_EFFECTIVE)},
                    null).getCount();
            if (count > 0) {
                int updateCount = getActivity().getContentResolver().update(MusicMediaDatabaseHelper.Queue.CONTENT_URI, contentValues, updateQueue, null);
                if (updateCount <= 0) {
                    return result;
                }
            }
            Cursor c = getActivity().getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, DBUtil.MEDIA_FOLDER_COLUMNS, where.toString(), null, null);
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
                if(containCurrentPlayMusic){
                    if(isPlaying){
                        try {
                            MediaInfo info = QueueUtil.getNextPlayableMediaInQueueIfCurrentMediaIsIneffective(getActivity(),MusicPlayBackService.getMediaID());
                            try {
                                mService.playByMediaInfo(info);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            LogUtil.i(TAG,"contain next song ");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        MediaInfo info = QueueUtil.getNextPlayableMediaInQueueIfCurrentMediaIsIneffective(getActivity(),MusicPlayBackService.getMediaID());
                        try {
                            mService.playByMediaInfoIfNowPlay(info,false);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        mService.setToCompletion(true);
                        if(!MusicPlayBackService.isPlaying()){
                            mService.pause();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            } else {
                ToastUtil.showToast(getActivity(), R.string.operation_failed);
                if(containCurrentPlayMusic){
                    if(isPlaying){
                        try {
                            mService.play();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        mService.setToCompletion(true);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            mProgressDialog.setProgressStyle(mProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(getResources().getString(R.string.operating));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
        } else {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
        mProgressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG,"onResume");
        getLoaderManager().restartLoader(0, null,this);
    }

    /**
     * 刷新歌曲总数
     *
     * @param data 歌曲数据
     */
    private void refreshTotalCount(Cursor data) {
        if (data == null || data.getCount() <= 0) {
            mLocalMediaTotalNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_songs_3, 0, 0));
            mPlayAllLayout.setVisibility(View.GONE);
        } else {
            mLocalMediaTotalNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, data.getCount(), data.getCount()));
            mPlayAllLayout.setVisibility(View.VISIBLE);
        }
    }

    public void goToMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        ((LocalMusicActivity)getActivity()).showActionMode(true);
        ((LocalMusicActivity)getActivity()).setMultiMode(true);
        ((LocalMusicActivity)getActivity()).getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void leaveMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
        setMultiMode(false, -1);
        ((LocalMusicActivity)getActivity()).setMultiMode(false);
        ((LocalMusicActivity) getActivity()).showActionMode(false);
    }
}
