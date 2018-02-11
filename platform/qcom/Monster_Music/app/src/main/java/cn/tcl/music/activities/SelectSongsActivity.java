package cn.tcl.music.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import cn.tcl.music.R;
import cn.tcl.music.adapter.SelectSongsAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.widget.ActionModeHandler;
import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;
import mst.widget.MstIndexBar;
import mst.widget.toolbar.Toolbar;

public class SelectSongsActivity extends MstActivity implements View.OnClickListener {
    private static final String TAG = SelectSongsActivity.class.getSimpleName();

    private final static int DEFAULT_SELECTED_COUNT = 0;
    private RecyclerView mRecyclerView;
    private TextView mToolbarSelectNumTv;
    private TextView mToolbarCancelTv;
    private TextView mToolbarSelectAllTv;
    private TextView mLoadingTv;
    private RelativeLayout mAddtoRelativeLayout;
    private SelectSongsAdapter mSelectSongsAdapter;
    private ArrayList<MediaInfo> mSongsList = new ArrayList<>();
    private ArrayList<Long> mSelectedSongdId = new ArrayList<>();
    private boolean mIsSelectAll = true;
    private LoadSelectSongsAsync mLoadSelectSongsAsync;
    private Uri mPlaylistUri;
    private AddToPlayListAsync mAddToPlayListTask;
    private long[] mPlaylistSelectedSongsIds;
    private long[] mFavoriteSelectedSongsIds;
    private int mAddType;
    private ProgressDialog mProgressDialog;
    private static final int MSG_ADD_SUCCESS = 0x01;
    private static final int MSG_ADD_FAILURE = 0x02;
    private MstIndexBar mIndexBar;
    private boolean isMoveToTop = false;
    private int mCurrentPosition = 0;
    private int mCurrentSection = -1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_SUCCESS: {
                    String playlistName = (String) msg.obj;
                    if (ActionModeHandler.mActionMode != null) {
                        ActionModeHandler.mActionMode.finish();
                    }
                    mProgressDialog.dismiss();
                    ToastUtil.showToast(SelectSongsActivity.this, getString(R.string.song_had_been_added_to_playlist, playlistName));
                }
                break;
                case MSG_ADD_FAILURE:
                    if (ActionModeHandler.mActionMode != null) {
                        ActionModeHandler.mActionMode.finish();
                    }
                    mProgressDialog.dismiss();
                    ToastUtil.showToast(SelectSongsActivity.this, getString(R.string.operation_failed));
                    break;
                default:
                    break;
            }
            onBackPressed();
        }
    };

    public SelectSongsAdapter.OnSongsSelectedListner mOnSongsSelectedListner = new SelectSongsAdapter.OnSongsSelectedListner() {
        @Override
        public void onSongsSelected(ArrayList<Long> songIds) {
            mSelectedSongdId = songIds;
        }

        @Override
        public void onSongsSelectedCount(int count) {
            String format = getResources().getString(R.string.batch_songs_num);
            mToolbarSelectNumTv.setText(String.format(format, count));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_song);
        if (getIntent() != null && getIntent().getExtras() != null) {
            String playlistId = getIntent().getExtras().getString(CommonConstants.BUNDLE_KEY_PLAYLIST_ID);
            mPlaylistSelectedSongsIds = getIntent().getExtras().getLongArray(CommonConstants.BUNDLE_KEY_PLAYLIST_ADD_FLAG);
            mFavoriteSelectedSongsIds = getIntent().getExtras().getLongArray(CommonConstants.BUNDLE_KEY_FAVORITE_ADD_FLAG);
            mAddType = getIntent().getExtras().getInt(CommonConstants.SELECT_SONGS_ADD);
            if (CommonConstants.SELECT_SONGS_TO_PLAYLIST == mAddType) {
                mPlaylistUri = MusicMediaDatabaseHelper.Playlists.CONTENT_URI.buildUpon().appendPath(String.valueOf(playlistId)).build();
            } else if (CommonConstants.SELECT_SONGS_TO_FAVORITE == mAddType) {
                mPlaylistUri = MusicMediaDatabaseHelper.Playlists.FAVORITE_URI;
            }
            mRecyclerView = (RecyclerView) findViewById(R.id.select_songs_recyclerview);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (isMoveToTop) {
                        isMoveToTop = false;
                        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int n = mCurrentPosition - llm.findFirstVisibleItemPosition();
                        if (0 <= n && n < recyclerView.getChildCount()) {
                            int top = recyclerView.getChildAt(n).getTop();
                            recyclerView.scrollBy(0, top);
                        }
                    }
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == recyclerView.SCROLL_STATE_DRAGGING) {
                        mIndexBar.clearLetterFocus();
                        mIndexBar.clearLetterColor(mCurrentSection);
                    }
                }
            });
            mToolbarSelectNumTv = (TextView) findViewById(R.id.toolbar_select_num_tv);
            String format = getResources().getString(R.string.batch_songs_num);
            mToolbarSelectNumTv.setText(String.format(format, DEFAULT_SELECTED_COUNT));
            mToolbarCancelTv = (TextView) findViewById(R.id.toolbar_cancel_tv);
            mToolbarSelectAllTv = (TextView) findViewById(R.id.toolbar_select_all_tv);
            mLoadingTv = (TextView) findViewById(R.id.select_songs_loading_tv);
            mAddtoRelativeLayout = (RelativeLayout) findViewById(R.id.select_songs_addto);
            initIndexBar();

            mToolbarCancelTv.setOnClickListener(this);
            mToolbarSelectAllTv.setOnClickListener(this);
            mAddtoRelativeLayout.setOnClickListener(this);
            initToolBar();
            if (mLoadSelectSongsAsync == null || mLoadSelectSongsAsync.getStatus() != AsyncTask.Status.RUNNING) {
                mLoadSelectSongsAsync = new LoadSelectSongsAsync(this);
                mLoadSelectSongsAsync.execute();
            }
        }
    }

    private void initIndexBar() {
        mIndexBar = (MstIndexBar) findViewById(R.id.index_bar);
        mIndexBar.deleteLetter(0);
        mIndexBar.setLetterTextSize((int) getResources().getDimension(R.dimen.sp_8));
        mIndexBar.setColor(getResources().getColorStateList(R.color.index_bar_color));
        mIndexBar.setBalloonDiameter(0);
        mIndexBar.setEnables(true, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
        mIndexBar.setOnSelectListener((index, layer, letter) -> {
            Log.i(TAG, "onSelect: index = " + index + " layer = " + layer + " letter = " + letter);
            mCurrentSection = index;
            moveToPosition(mSelectSongsAdapter.getPositionForSection(index));
        });
    }

    private void moveToPosition(int position) {
        mCurrentPosition = position;
        if (position > -1) {
            LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int firstItem = llm.findFirstVisibleItemPosition();
            int lastItem = llm.findLastVisibleItemPosition();
            if (position <= firstItem) {
                mRecyclerView.scrollToPosition(position);
            } else if (position <= lastItem) {
                int top = mRecyclerView.getChildAt(position - firstItem).getTop();
                mRecyclerView.scrollBy(0, top);
            } else {
                mRecyclerView.scrollToPosition(position);
                isMoveToTop = true;
            }
        } else {
            mCurrentSection = -1;
        }
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.select_songs_toolbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_cancel_tv:
                setResult(PlaylistDetailActivity.RESULT_CODE_SELECT_SONGS_CANCEL, null);
                setResult(MyFavouriteMusicActivity.RESULT_CODE_SELECT_SONGS_FAVORITE_CANCEL, null);
                finish();
                break;
            case R.id.toolbar_select_all_tv:
                if (mIsSelectAll) {
                    mToolbarSelectAllTv.setText(R.string.batch_cancel_select_all);
                } else {
                    mToolbarSelectAllTv.setText(R.string.select_all);
                }
                selectAll(mIsSelectAll);
                break;
            case R.id.select_songs_addto:
                if (mSelectedSongdId.isEmpty()) {
                    ToastUtil.showToast(this, R.string.please_select_media_items);
                } else {
                    batchAddToPlayList();
                }
                break;
        }
    }

    private void batchAddToPlayList() {
        mAddToPlayListTask = new AddToPlayListAsync();
        mAddToPlayListTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIndexBar.release();
        if (mSelectSongsAdapter != null) {
            mSelectSongsAdapter.setOnSelectSongsListner(null);
        }
    }

    private void selectAll(boolean isSelectAll) {
        mIsSelectAll = !isSelectAll;
        if (mSelectSongsAdapter != null) {
            mSelectSongsAdapter.selectAll(isSelectAll);
            mSelectSongsAdapter.notifyDataSetChanged();
        }
    }

    private class LoadSelectSongsAsync extends AsyncTask<Long, Void, ArrayList<MediaInfo>> {
        private Context mContext;

        public LoadSelectSongsAsync(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<MediaInfo> doInBackground(Long... params) {
            final Cursor c = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                    DBUtil.MEDIA_FOLDER_COLUMNS, null, null, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
            if (c != null) {
                while (c.moveToNext()) {
                    mSongsList.add(MusicUtil.getMediaInfoFromCursor(c));
                }
                c.close();
            }
            return mSongsList;
        }

        @Override
        protected void onPostExecute(ArrayList<MediaInfo> mediaInfos) {
            super.onPostExecute(mediaInfos);
            initView();
        }
    }

    private void initView() {
        mLoadingTv.setVisibility(View.INVISIBLE);
        mSelectSongsAdapter = new SelectSongsAdapter(this);
        mSelectSongsAdapter.setData(mSongsList);
        mSelectSongsAdapter.setSelectedSongs(mPlaylistSelectedSongsIds);
        mSelectSongsAdapter.setSelectedSongs(mFavoriteSelectedSongsIds);
        mRecyclerView.setAdapter(mSelectSongsAdapter);
        mSelectSongsAdapter.setOnSelectSongsListner(mOnSongsSelectedListner);
    }

    private class AddToPlayListAsync extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            final ArrayList<Object> ids = new ArrayList<Object>();
            for (int i = 0; i < mSelectedSongdId.size(); i++) {
                ids.add(mSelectedSongdId.get(i));
            }

            int result = 0;
            if (MusicMediaDatabaseHelper.Playlists.FAVORITE_URI.equals(mPlaylistUri)) {
                result = DBUtil.changeFavoriteWithIds(SelectSongsActivity.this, ids, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
            } else {
                long playlistId = Long.valueOf(mPlaylistUri.getLastPathSegment());
                result = DBUtil.addSongsToPlaylist(SelectSongsActivity.this, playlistId, ids);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result == 0) {
                ToastUtil.showToast(SelectSongsActivity.this, R.string.operation_failed);
            } else {
                if (CommonConstants.SELECT_SONGS_TO_PLAYLIST == mAddType) {
                    setResult(PlaylistDetailActivity.RESULT_CODE_SELECT_SONGS, new Intent().putExtra(CommonConstants.ADD_SUCCESS_KEY, mSelectedSongdId.size() > 0));
                } else if (CommonConstants.SELECT_SONGS_TO_FAVORITE == mAddType) {
                    setResult(MyFavouriteMusicActivity.RESULT_CODE_SELECT_FAVORITE_SONGS, new Intent().putExtra(CommonConstants.ADD_SUCCESS_KEY, mSelectedSongdId.size() > 0));
                }
                finish();
            }
        }
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.batch_operate_loading));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
//        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                if (mAddToPlayListTask != null) {
//                    mAddToPlayListTask.cancel(true);
//                }
//            }
//        });
        mProgressDialog.show();
    }

}
