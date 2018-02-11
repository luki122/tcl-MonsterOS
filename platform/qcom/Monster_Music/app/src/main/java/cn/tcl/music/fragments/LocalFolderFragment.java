package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.tcl.music.R;
import cn.tcl.music.activities.FolderDetailActivity;
import cn.tcl.music.adapter.LocalFolderAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.QueueUtil;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.model.live.AddType;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;

public class LocalFolderFragment extends BaseRecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = LocalFolderFragment.class.getSimpleName();
    private TextView mEmptyTextView;
    private TextView mIgnoredTextView;
    private TextView mFolderAndSongsCountTextView;
    private LocalFolderAdapter mLocalFolderAdapter;

    public LocalFolderFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local_folder, container, false);
        mEmptyTextView = (TextView) rootView.findViewById(android.R.id.empty);
        mEmptyTextView.setText(R.string.no_song_found);
        mFolderAndSongsCountTextView = (TextView) rootView.findViewById(R.id.folder_and_songs_count);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void leaveMultiChoose() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN + " = ? and " + MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_NAME + " != ?";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.VALUE_FOLDER_IS_SCAN), CommonConstants.VIRTUAL_ONLINE_FOLDER_NAME};
        return new CursorLoader(getActivity(), MusicMediaDatabaseHelper.Folders.CONTENT_URI,
                null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d(TAG, "data count is " + data.getCount());
        if (getActivity() == null) {
            return;
        }
        if (mLocalFolderAdapter == null) {
            mLocalFolderAdapter = new LocalFolderAdapter(getActivity(), data, null, false);
            setRecyclerAdapter(mLocalFolderAdapter);
        } else {
            mLocalFolderAdapter.changeCursor(data);
        }
        if (data == null || data.getCount() <= 0) {
            manageEmptyView(true);
        } else {
            manageEmptyView(false);
        }
        refreshTotalCount(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void refreshData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /*
    文件夹忽略，首先， 判断当前是否有歌曲播放（无论暂停或播放），如果没有，更新folder表，清除queue(防止某种情况下没有歌曲播放但是queue表不为空，怕出错）
    如果有，判断当前播放歌曲的文件夹是否为即将被忽略， 如果是，暂停播放,更新folder表，清除queue表中所有ineffective的字段，将下一个可以播放的folder所有
    歌曲放入queue表，加载第一首歌曲（是否播放看需要），
     */
    public void deleteItem(final int position) {
        LogUtil.d(TAG, "position is " + position);
        Log.d(TAG, "deleteItem and position is " + position);
        if (null == mLocalFolderAdapter) {
            Log.d(TAG, "deleteItem and adapter is null");
        } else {
            final Cursor c = mLocalFolderAdapter.getCursorAtAdapterPosition(position);
            if (c != null) {
                long folderId = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID));
                LogUtil.d(TAG, "folderId is " + folderId);
                int result = -1;
                if (MusicPlayBackService.getCurrentMediaInfo() == null) {
                    result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                    QueueUtil.clearQueueTable(getActivity());
                } else {
                    long folderID = MusicPlayBackService.getCurrentMediaInfo().folderId;
                    if (folderID == folderId) {
                        // current playing song is in the to be ignored folder
                        if (AddType.getAddType(getActivity()) == AddType.ADD_TYPE_FOLDER) {
                            // add type is folder and current play song is in the folder
                            if (MusicPlayBackService.isPlaying()) {
                                // pause
                                // update folder and queue(inflater next playable folder songs), clear current queue table and insert new folder songs to it
                                // play first song in next folder, if null ,play(null);
                                try {
                                    mService.pause();
                                    result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                                    if (result != -1) {
                                        long id = QueueUtil.getFirstPlayableFolder(getActivity());
                                        if (id != -1) {
                                            result = QueueUtil.insertFolderToQueueByID(getActivity(), id);
                                            if (result == -3 || result == -2) {
                                                // insert folder to queue fail or next folder has no playable song
                                                mService.playByMediaInfo(null);
                                            } else if (result == -1) {
                                                // fail at first clear queue table
                                                mService.play();
                                            } else {
                                                MediaInfo info = QueueUtil.getFirstPlayableSongInQueue(getActivity());
                                                LogUtil.i(TAG, "find next info = " + (info == null ? "null" : info.title));
                                                mService.playByMediaInfo(info);
                                            }
                                        } else {
                                            mService.playByMediaInfo(null);
                                        }
                                    } else {
                                        mService.play();
                                    }
                                } catch (Exception e) {
                                }

                            } else {
                                // pause
                                // update folder and queue
                                // load next song not play
                                try {
                                    mService.pause();
                                    result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                                    if (result != -1) {
                                        long id = QueueUtil.getFirstPlayableFolder(getActivity());
                                        if (id != -1) {
                                            result = QueueUtil.insertFolderToQueueByID(getActivity(), id);
                                            if (result == -3 || result == -2) {
                                                // insert folder to queue fail or next folder has no playable song
                                                mService.playByMediaInfo(null);
                                            } else if (result == -1) {
                                                // fail at first clear queue table
                                                mService.play();
                                            } else {
                                                MediaInfo info = QueueUtil.getFirstPlayableSongInQueue(getActivity());
                                                LogUtil.i(TAG, "find next info = " + (info == null ? "null" : info.title));
                                                mService.playByMediaInfoIfNowPlay(info, false);
                                            }
                                        }
                                    } else {
                                        //nothing to do, modify folder fail, music has been paused;
                                    }
                                } catch (Exception e) {
                                }
                            }
                        } else {
                            // add type not folder but current play song is in the to be ignored folders
                            if (MusicPlayBackService.isPlaying()) {
                                //pause
                                //update folder and queue
                                //next song in queue
                                try {
                                    mService.pause();
                                    QueueUtil.clearAllIneffectiveSongInQueue(getActivity());
                                    result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                                    if (result != -1) {
                                        MediaInfo info = QueueUtil.getNextPlayableMediaInfo(getActivity(), PlayMode.getMode(getActivity()), MusicPlayBackService.getCurrentMediaInfo().audioId);
                                        LogUtil.i(TAG, "current song id = " + MusicPlayBackService.getCurrentMediaInfo().audioId);
                                        LogUtil.i(TAG, "info = " + (info == null ? " null " : info.toString()));
                                        mService.playByMediaInfo(info);
                                    } else {
                                        mService.play();
                                    }

                                } catch (Exception e) {

                                }

                            } else {
                                //update folder and queue
                                //load next song not play
                                try {
                                    mService.pause();
                                    QueueUtil.clearAllIneffectiveSongInQueue(getActivity());
                                    result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                                    if (result != -1) {
                                        MediaInfo info = QueueUtil.getNextPlayableMediaInfo(getActivity(), PlayMode.getMode(getActivity()), MusicPlayBackService.getCurrentMediaInfo().audioId);
                                        mService.playByMediaInfoIfNowPlay(info, false);
                                    } else {
                                        mService.pause();
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    } else {
                        // current playing song is not in the to be ignored folder
                        if (AddType.getAddType(getActivity()) == AddType.ADD_TYPE_FOLDER) {
                            result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                        } else {
                            //set false
                            //update folder and queue
                            //set true
                            try {
                                mService.setToCompletion(false);
                                result = DBUtil.changeIsScanInFolderTableWithFolderId(getActivity(), folderId, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
                                mService.setToCompletion(true);
                            } catch (Exception e) {
                                mService.setToCompletion(true);
                            }
                        }
                    }
                }
                getLoaderManager().getLoader(0).onContentChanged();
                //发送广播通知歌曲界面进行刷新
                Intent intent = new Intent();
                intent.setAction(CommonConstants.BROADCAST_IGNORE_OR_RECOVER_FOLDER);
                getActivity().sendBroadcast(intent);
                if (result == -1) {
                    ToastUtil.showToast(getActivity(), R.string.operation_failed);
                }
            }
        }
    }

    /**
     * 条目点击事件
     *
     * @param position
     */
    public void clickItem(int position) {
        LogUtil.d(TAG, "position is " + position);
        if (mLocalFolderAdapter.getItemCount() > position) {
            Cursor c = mLocalFolderAdapter.getCursorAtAdapterPosition(position);
            Integer folderId = c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID));
            String folderName = c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_NAME));
            int songNum = c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_SONGS_NUM));
            int isScan = c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN));

            Bundle bundle = new Bundle();
            bundle.putInt(CommonConstants.BUNDLE_KEY_FOLDER_TYPE, CommonConstants.VALUE_FOLDER_IS_SCAN);
            bundle.putString(CommonConstants.BUNDLE_KEY_FOLDER_NAME, folderName);
            bundle.putString(CommonConstants.BUNDLE_KEY_FOLDER_ID, String.valueOf(folderId));
            bundle.putInt(CommonConstants.BUNDLE_KEY_FOLDER_SONG_NUM, songNum);
            bundle.putInt(CommonConstants.BUNDLE_KEY_FOLDER_IS_SCAM, isScan);

            //jump to scan folder detail
            Intent intent = new Intent(getActivity(), FolderDetailActivity.class);
            intent.putExtras(bundle);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().bindService(new Intent(getActivity(), MusicPlayBackService.class), mConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(mConnection);
        super.onDestroy();
    }

    /**
     * show the folders and medias total count
     *
     * @param data
     */
    private void refreshTotalCount(Cursor data) {
        int foldersCount = 0;
        int mediasCount = 0;
        if (null != data && data.getCount() != 0) {
            foldersCount = data.getCount();
            while (data.moveToNext()) {
                mediasCount += data.getInt(data.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_SONGS_NUM));
            }
        }
        String str_folders = getResources().getQuantityString(R.plurals.folder_number_of_folders, foldersCount, foldersCount);
        String str_songs = getResources().getQuantityString(R.plurals.folder_number_of_songs, mediasCount, mediasCount);
        mFolderAndSongsCountTextView.setText(str_folders + str_songs);
    }
}
