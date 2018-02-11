package cn.tcl.music.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.CopyOnWriteArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalMusicActivity;
import cn.tcl.music.activities.MyFavouriteMusicActivity;
import cn.tcl.music.activities.PlaylistDetailActivity;
import cn.tcl.music.activities.RecentlyPlayActivity;
import cn.tcl.music.activities.live.HotMusicRecommendActivity;
import cn.tcl.music.adapter.PlaylistAdapter;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.fragments.live.LiveMusicCollectSongListFragment;
import cn.tcl.music.model.PlaylistInfo;
import cn.tcl.music.util.Connectivity;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.PlaylistView;

public class LocalMusicFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LocalMusicFragment.class.getSimpleName();
    private View mRootView;
    private TextView mLocalMusicTextView;
    private TextView mRecentsMusicTextView;
    private TextView mILikeMusicTextView;
    private TextView mPlayListMusicTextview;
    private TextView mTodayMusicTextView;
    private PlaylistView mPlaylistView;
    private CopyOnWriteArrayList<PlaylistInfo> mPlaylistInfo;
    private PlaylistAdapter mPlaylistAdapter;
    private LoadPlaylistDetailAsync mPlaylistDetailAsync;
    private LocalInfoAsync mLocalInfoTask;
    private static final int TODAY_SONGS_COUNT = 30;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_local_music, null);
        initData();
        initView();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void initData() {
        mPlaylistInfo = new CopyOnWriteArrayList<PlaylistInfo>();
    }

    public void refresh() {
        if (getActivity() != null && (mLocalInfoTask == null || mLocalInfoTask.getStatus() != AsyncTask.Status.RUNNING)) {
            mLocalInfoTask = new LocalInfoAsync();
            mLocalInfoTask.execute();
        }
        if (getActivity() != null && (mPlaylistDetailAsync == null || mPlaylistDetailAsync.getStatus() != AsyncTask.Status.RUNNING)) {
            mPlaylistDetailAsync = new LoadPlaylistDetailAsync();
            mPlaylistDetailAsync.execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalInfoTask.cancel(true);
        getLoaderManager().destroyLoader(0);
    }

    private void initView() {
        mLocalMusicTextView = (TextView) mRootView.findViewById(R.id.local_total_textview);
        mILikeMusicTextView = (TextView) mRootView.findViewById(R.id.i_like_textview);
        mRecentsMusicTextView = (TextView) mRootView.findViewById(R.id.recentsplay_music_textview);
        mPlayListMusicTextview = (TextView) mRootView.findViewById(R.id.playlist_number);
        mTodayMusicTextView = (TextView) mRootView.findViewById(R.id.today_music_textview);
        mPlaylistView = (PlaylistView) mRootView.findViewById(R.id.playlist_list_view);
        LinearLayout recentLinearLayout = (LinearLayout) mRootView.findViewById(R.id.recent_linearlayout);
        LinearLayout favoriteliearlayout = (LinearLayout) mRootView.findViewById(R.id.favorite_linearlayout);
        LinearLayout playlistLinearLayout = (LinearLayout) mRootView.findViewById(R.id.playlist_linearlayout);
        RelativeLayout locallinearlayout = (RelativeLayout) mRootView.findViewById(R.id.local_linearlayout);
        LinearLayout ilikelinearlayout = (LinearLayout) mRootView.findViewById(R.id.i_like_inearlayout);
        LinearLayout todaylinearlayout = (LinearLayout) mRootView.findViewById(R.id.today_linearlayout);
        mPlaylistAdapter = new PlaylistAdapter(getContext(), mPlaylistInfo);
        mPlaylistView.setAdapter(mPlaylistAdapter);
        mPlaylistView.setFocusable(false);
        mPlaylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaylistInfo info = mPlaylistInfo.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(CommonConstants.BUNDLE_KEY_PLAYLIST_TITLE, info.getName());
                bundle.putString(CommonConstants.BUNDLE_KEY_PLAYLIST_ID, String.valueOf(info.getId()));
                Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        ilikelinearlayout.setOnClickListener(this);
        locallinearlayout.setOnClickListener(this);
        recentLinearLayout.setOnClickListener(this);
        favoriteliearlayout.setOnClickListener(this);
        playlistLinearLayout.setOnClickListener(this);
        todaylinearlayout.setOnClickListener(this);
        String todaySongs = getResources().getQuantityString(R.plurals.number_of_songs, TODAY_SONGS_COUNT, TODAY_SONGS_COUNT);
        mTodayMusicTextView.setText(todaySongs);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.local_linearlayout: {
                Intent intent = new Intent(getActivity(), LocalMusicActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.recent_linearlayout: {
                Intent intent = new Intent(getActivity(), RecentlyPlayActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.favorite_linearlayout:
                //favorite
                LiveMusicCollectSongListFragment.launch(getActivity());
                break;
            case R.id.playlist_linearlayout:
                showCreatePlayListDialog();
                break;
            case R.id.i_like_inearlayout: {
                Intent intent = new Intent(getActivity(), MyFavouriteMusicActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.today_linearlayout: {
                if (MusicApplication.isNetWorkCanUsed()) {
                    Intent intent = new Intent(getContext(), HotMusicRecommendActivity.class);
                    intent.putExtra("position", 0);
                    intent.setAction("daily_recommend");
                    startActivity(intent);
                } else {
                    ToastUtil.showToast(getContext(), R.string.network_error_prompt);
                }
            }
                break;
            default:
                break;
        }
    }

    private class LocalInfo {
        public int numTrack;
        public int numRecent;
        public int numLike;
    }

    private class LocalInfoAsync extends AsyncTask<Long, Void, LocalInfo> {

        @Override
        protected LocalInfo doInBackground(final Long... params) {
            LocalInfo localInfo = new LocalInfo();
            localInfo.numTrack = MusicUtil.getSongCount(getActivity());
            localInfo.numRecent = MusicUtil.getRecentCount(getActivity());
            localInfo.numLike = MusicUtil.getLikeCount(getActivity());
            return localInfo;
        }

        @Override
        protected void onPostExecute(final LocalInfo result) {
            int numTrack = result.numTrack;
            int numRecent = result.numRecent;
            int numLike = result.numLike;
            String myMusicNum = getResources().getQuantityString(R.plurals.number_of_songs, numTrack, numTrack);
            String likeNum = getResources().getQuantityString(R.plurals.number_of_songs, numLike, numLike);
            String recentNum = getResources().getQuantityString(R.plurals.number_of_songs, numRecent, numRecent);
            mLocalMusicTextView.setText(myMusicNum);
            mILikeMusicTextView.setText(likeNum);
            mRecentsMusicTextView.setText(recentNum);
        }
    }

    /**
     * show create playlist dialog
     */
    private void showCreatePlayListDialog() {
        SimplePlaylistChooserAdapter.OnPlaylistChoiceListener mOnPlaylistChoiceListener = new SimplePlaylistChooserAdapter.OnPlaylistChoiceListener() {
            @Override
            public void onPlaylistChosen(Uri playlistUri, String playlistName) {
                //step1:get playlist from database by the name
                //step2:jump to playlist detail activity with flag
                //step3:then jump to SelectSongsActivity,and import music
                LogUtil.d(TAG,"playlistUri is " + playlistUri + " and playlistName is " + playlistName);
                String selection = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME + " = ? ";
                String[] selectionArgs = new String[]{playlistName};
                Cursor cursor = getActivity().getContentResolver().query(
                        MusicMediaDatabaseHelper.Playlists.CONTENT_URI, DBUtil.defaultPlaylistColumns,
                        selection,
                        selectionArgs,
                        null);
                if (null != cursor && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    PlaylistInfo info = new PlaylistInfo();
                    info.setId(cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME)));
                    info.setArtwork(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.ARTWORK)));
                    info.setDescription(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.DESCRIPTION)));
                    info.setType(cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.TYPE)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.PATH)));
                    cursor.close();

                    Bundle bundle = new Bundle();
                    bundle.putString(CommonConstants.BUNDLE_KEY_PLAYLIST_TITLE, info.getName());
                    bundle.putString(CommonConstants.BUNDLE_KEY_PLAYLIST_ID, String.valueOf(info.getId()));
                    bundle.putInt(CommonConstants.BUNDLE_KEY_PLAYLIST_JUMP_FLAG, CommonConstants.VALUE_PLAYLIST_FLAG_IMPORT_MEDIA);
                    Intent intent = new Intent(getActivity(), PlaylistDetailActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        };
        DialogMenuUtils.displayCreateNewPlaylistDialog(getActivity(), R.string.new_song_list, mOnPlaylistChoiceListener);
    }

    private class LoadPlaylistDetailAsync extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            String selection = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.TYPE + " = ? ";
            String[] selectionArgs = new String[]{String.valueOf(CommonConstants.TYPE_PLAYLIST_CREATED)};
            Cursor cursor = getActivity().getContentResolver().query(MusicMediaDatabaseHelper.Playlists.CONTENT_URI, DBUtil.defaultPlaylistColumns,
                    selection,
                    selectionArgs,
                    MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID);
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            mPlaylistInfo.clear();
            if (cursor != null && !cursor.isClosed()) {
                while (cursor.moveToNext()) {
                    PlaylistInfo info = new PlaylistInfo();
                    info.setId(cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME)));
                    info.setArtwork(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.ARTWORK)));
                    info.setDescription(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.DESCRIPTION)));
                    info.setType(cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.TYPE)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.PATH)));
                    mPlaylistInfo.add(info);
                }
            }

            // Close the cursor
            if (cursor != null) {
                cursor.close();
            }
            mPlaylistAdapter.notifyDataSetChanged();
        }
    }

}
