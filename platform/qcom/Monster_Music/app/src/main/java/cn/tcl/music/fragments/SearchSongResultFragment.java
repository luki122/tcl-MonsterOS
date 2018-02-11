package cn.tcl.music.fragments;


import android.app.Fragment;

import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.util.ToastUtil;
import mst.app.dialog.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.SearchActivity;
import cn.tcl.music.adapter.SearchSongResultAdapter;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.database.QueueUtil;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.AddType;
import cn.tcl.music.model.live.LiveMusicSearchSongBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicSearchSongByKeywordTask;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.Connectivity;
import cn.tcl.music.util.DialogMenuUtils;
import mst.app.dialog.AlertDialog;

public class SearchSongResultFragment extends Fragment implements ILoadData {

    private static final int ONLINE_SONG_SIZE = 10;
    private static final int MSG_ADD_SUCCESS = 0x01;
    private static final int MSG_ADD_FAILURE = 0x02;
    private static final String TAG = SearchSongResultFragment.class.getSimpleName();

    private View mRootView;
    private FileTask mFileTask;
    private TextView mEmptyTextView;
    private SearchActivity mContext;
    private ProgressDialog mProgressDialog;
    private RecyclerView mSearchMediaRecyclerView;
    private SearchSongResultAdapter mSearchSongResultAdapter;
    private LiveMusicSearchSongByKeywordTask mLiveMusicSearchSongByKeywordTask;

    protected boolean mHaveMoreData = true;
    private ArrayList<Object> mOnlineSongsList = new ArrayList<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_SUCCESS: {
                    String playlistName = (String) msg.obj;
                    mProgressDialog.dismiss();
                    if (getActivity().getString(R.string.my_favourite_music).equals(playlistName)) {
                        mSearchSongResultAdapter.mLocalSongsList.set(mSearchSongResultAdapter.getmCurrentOperatePosition(),
                                mSearchSongResultAdapter.getInfoAtAdapterPosition(mSearchSongResultAdapter.getmCurrentOperatePosition()));
                        mSearchSongResultAdapter.notifyDataSetChanged();
                    }
                    ToastUtil.showToast(getActivity(), getActivity().getString(R.string.song_had_been_added_to_playlist, playlistName));
                }
                break;
                case MSG_ADD_FAILURE:
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = (SearchActivity) getActivity();
        mRootView = inflater.inflate(R.layout.fragment_search_media, null);
        mEmptyTextView = (TextView) mRootView.findViewById(R.id.empty_content);
        mEmptyTextView.setText(R.string.loading);
        mSearchMediaRecyclerView = (RecyclerView) mRootView.findViewById(R.id.search_media_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mSearchMediaRecyclerView.setLayoutManager(linearLayoutManager);
        if (mSearchSongResultAdapter == null) {
            mSearchSongResultAdapter = new SearchSongResultAdapter(this);
        }
        mSearchMediaRecyclerView.setAdapter(mSearchSongResultAdapter);
        return mRootView;
    }

    public void refreshSearchResult() {
        ArrayList<Object> localSongsList = new ArrayList<>();
        localSongsList.addAll(mContext.getmLocalSearchResultList());
        if (localSongsList.size() == 0 && mOnlineSongsList.size() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
        mSearchSongResultAdapter.setLocalSongsData(localSongsList);
        mSearchSongResultAdapter.notifyDataSetChanged();
    }

    public void searchOnlineSong(String searchKey, int currentPage) {
        if (MusicApplication.isNetWorkCanUsed()) {
            if (mLiveMusicSearchSongByKeywordTask != null && mLiveMusicSearchSongByKeywordTask.getStatus() == AsyncTask.Status.RUNNING) {
                mLiveMusicSearchSongByKeywordTask.cancel(true);
            }
            mLiveMusicSearchSongByKeywordTask = new LiveMusicSearchSongByKeywordTask(getActivity(), this, searchKey, String.valueOf(currentPage));
            mLiveMusicSearchSongByKeywordTask.executeMultiTask();
        } else {
            mOnlineSongsList.clear();
            mEmptyTextView.setText(R.string.search_no_results_found);
            mSearchSongResultAdapter.setOnlineSongsData(mOnlineSongsList);
            mSearchSongResultAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        if (datas != null && datas.size() > 0) {
            LiveMusicSearchSongBean data = (LiveMusicSearchSongBean) datas.get(0);
            if (data != null) {
                mHaveMoreData = data.more;
                int searchSongSize = data.songs.size() > ONLINE_SONG_SIZE ? ONLINE_SONG_SIZE : data.songs.size();

                mOnlineSongsList.clear();
                for (int i = 0; i < searchSongSize; i++) {
                    mOnlineSongsList.add(data.songs.get(i));
                    Log.d(TAG, "mOnlineSongsList : " + mOnlineSongsList.get(i).toString() + "\n");
                }
                if (mOnlineSongsList.size() == 0) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                }
                mSearchSongResultAdapter.setOnlineSongsData(mOnlineSongsList);
                mSearchSongResultAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoadFail(int dataType, String message) {
    }

    public void onPopulatePopupMenu(View v, final int position, final boolean isLocalSong, final Object info) {
        PopupMenu popup = new PopupMenu(getActivity(), v, Gravity.CENTER);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        View localMediaDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_local_media, null);
        builder.setView(localMediaDialogView);
        final AlertDialog localMediaDialog = builder.create();
        localMediaDialog.show();
        final Menu menu = popup.getMenu();
        popup.getMenuInflater().inflate(R.menu.all_songs_menu, menu);

        View.OnClickListener dialogListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<Object> ids = new ArrayList<Object>();
                if (isLocalSong) {
                    ids.add(((MediaInfo)info).audioId);
                }
                switch (v.getId()) {
                    case R.id.action_add_to_playlist:
                        if (!isLocalSong) {
                            return;
                        }
                        DialogMenuUtils.displayAddToPlaylistDialog(getActivity(), new SimplePlaylistChooserAdapter.OnPlaylistChoiceListener() {

                            @Override
                            public void onPlaylistChosen(final Uri playlistUri, final String playlistName) {
                                showProgressDialog();
                                int result = 0;
                                if (playlistUri.toString().equals(MusicMediaDatabaseHelper.Playlists.FAVORITE_URI.toString())) {
                                    result = DBUtil.changeFavoriteWithIds(getActivity(), ids, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
                                    ((MediaInfo)info).Favorite = true;
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
                        if (isLocalSong) {
                            PathUtils.shareLocalMedia((MediaInfo)info, getContext());
                        } else {
                            PathUtils.shareOnline(getContext(),CommonConstants.URL_SONG_SHARE+((SongDetailBean)info).song_id);
                        }
                        break;
                    case R.id.action_remove:
                        if (isLocalSong) {
                            deleteItem(((MediaInfo)info).audioId);
                        }
                        break;
                }
                localMediaDialog.dismiss();
            }
        };
        TextView actionAddToPlaylist = (TextView) localMediaDialogView.findViewById(R.id.action_add_to_playlist);
        TextView actionDownload = (TextView) localMediaDialogView.findViewById(R.id.action_download);
        TextView actionShare = (TextView) localMediaDialogView.findViewById(R.id.action_share);
        TextView actionRemove = (TextView) localMediaDialogView.findViewById(R.id.action_remove);
        actionAddToPlaylist.setOnClickListener(dialogListener);
        actionShare.setOnClickListener(dialogListener);
        actionRemove.setOnClickListener(dialogListener);
        actionDownload.setOnClickListener(dialogListener);
        if (!isLocalSong) {
            actionDownload.setVisibility(View.VISIBLE);
            actionRemove.setVisibility(View.GONE);
        }
    }

    // play local or downloaded songs
    public void onItemClick(int position) {
        if (mService == null) {
            return;
        }
        MediaInfo info = mSearchSongResultAdapter.getInfoAtAdapterPosition(position);
        if (info != null) {
            if (QueueUtil.isMediaEffectiveInQueue(getActivity(), info.audioId) <= 0) {
                ArrayList<MediaInfo> infos = new ArrayList<>();
                infos.add(info);
                if (QueueUtil.addMediaToQueue(getActivity(), false, infos) != 0) {
                    try {
                        mService.playByMediaInfo(info);
                        AddType.setAddType(getActivity(), AddType.ADD_TYPE_CONTAIN_SEARCH_SONG);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    mService.playByMediaInfo(info);
                } catch (RemoteException e) {
                    e.printStackTrace();
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

    public void deleteItem(final Long id) {
        if (null == mSearchSongResultAdapter) {
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            dialog.setMessage(R.string.confirm_to_delete);
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Long[] ids = new Long[]{id};
                    mFileTask = new FileTask();
                    mFileTask.execute(ids);
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
                    result = DialogMenuUtils.removeFromTableAndDeleteLocalFileIfNecessary(getActivity(), c);
                }
            }
            c.close();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            if (result) {
                mSearchSongResultAdapter.mLocalSongsList.remove(mSearchSongResultAdapter.getmCurrentOperatePosition());
                mSearchSongResultAdapter.notifyDataSetChanged();
                if (mSearchSongResultAdapter.mLocalSongsList.size() == 0 && mSearchSongResultAdapter.mOnlineSongsList.size() == 0) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }
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

    public void notifyCurrentSongChanged() {
        if (mSearchSongResultAdapter != null) {
            mSearchSongResultAdapter.notifyDataSetChanged();
        }
    }
}
