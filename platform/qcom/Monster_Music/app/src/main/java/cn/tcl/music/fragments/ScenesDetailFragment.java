package cn.tcl.music.fragments;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.QueueUtil;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.model.live.AddType;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;

public class ScenesDetailFragment extends BaseRecyclerViewFragment implements
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = ScenesDetailFragment.class.getSimpleName();
    private TextView mNumTextView;
    private LocalMediaAdapter mLocalMediaAdapter;
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scenes_detail, container, false);
        RelativeLayout mShuffleRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.scenes_detail_shuffle_rl);
        mNumTextView = (TextView) rootView.findViewById(R.id.scenes_detail_shuffle_num_tv);
        mShuffleRelativeLayout.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
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
        if (mLocalMediaAdapter == null) {
            mLocalMediaAdapter = new LocalMediaAdapter(getActivity(), data, mImageFetcher, false);
            setRecyclerAdapter(mLocalMediaAdapter);
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
            case R.id.scenes_detail_shuffle_rl:
                if(mService == null){
                    return;
                }
                if(mLocalMediaAdapter!=null){
                    int count = mLocalMediaAdapter.getItemCount();
                    PlayMode.setMode(getActivity(),PlayMode.PLAY_MODE_RANDOM);
                    clickItem(new Random().nextInt(count));
                }
                break;
        }
    }

    private void refreshTotalCount(Cursor data) {
        if (data == null || data.getCount() <= 0) {
            mNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, 0, 0));
        } else {
            mNumTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, data.getCount(), data.getCount()));
        }
    }

    public void clickItem(int position){
        if (getActivity() == null) {
            return;
        }
        int type = AddType.getAddType(getActivity());
        if (type == AddType.ADD_TYPE_SCENE_DETAIL_OTHERS && mLocalMediaAdapter.getItemCount() == QueueUtil.getQueuePlayableCount(getActivity()) && QueueUtil.getQueuePlayableCount(getActivity()) > 0 && MusicPlayBackService.getCurrentMediaInfo() != null) {
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
            AddType.setAddType(getActivity(), AddType.ADD_TYPE_SCENE_DETAIL_OTHERS);
        }
    }

    public void onCurrentMusicChanged() {
        if(mLocalMediaAdapter != null){
            mLocalMediaAdapter.notifyDataSetChanged();
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

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null,this);
    }

    @Override
    public void leaveMultiChoose() {

    }
}
