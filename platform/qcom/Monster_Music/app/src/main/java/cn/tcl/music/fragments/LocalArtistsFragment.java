package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumListActivity;
import cn.tcl.music.activities.LocalMusicActivity;
import cn.tcl.music.adapter.LocalArtistAdapter;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.loaders.CommonCursorLoader;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.widget.ActionModeHandler;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.widget.MstIndexBar;

public class LocalArtistsFragment extends BaseRecyclerViewFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = LocalArtistsFragment.class.getSimpleName();
    private TextView mArtistEmptyTextView;
    private RelativeLayout mArtistRelativeLayout;
    private LinearLayout mBatchOperateLinearLayout;
    private RelativeLayout mBatchOperateDelete;
    private RelativeLayout mBatchOperateAddPlaylist;
    private TextView mStickHeadView;
    private RecyclerView mRecyclerView;
    private LocalArtistAdapter mLocalArtistAdapter;
    private LocalScenesFragment.OnFragmentInteractionListener mListener;
    private ProgressDialog mProgressDialog;
    private FileTask mFileTask;

    private MstIndexBar mIndexBar;
    private boolean isMoveToTop = false;
    private int mCurrentPosition = 0;
    private int mCurrentSection = -1;

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
                        getLoaderManager().restartLoader(0, null, LocalArtistsFragment.this);
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

    public LocalArtistsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local_artists, container, false);
        mStickHeadView = (TextView) rootView.findViewById(R.id.head_letter_sticky);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mArtistEmptyTextView = (TextView) rootView.findViewById(R.id.empty);
        mArtistRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.recycler_container);
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
                // Get the sticky information from the topmost view of the screen.
                View stickyInfoView = recyclerView.findChildViewUnder(
                        mStickHeadView.getMeasuredWidth() / 2, mStickHeadView.getMeasuredHeight() / 2);
                if (stickyInfoView != null && stickyInfoView.getContentDescription() != null) {
                    mStickHeadView.setText(String.valueOf(stickyInfoView.getContentDescription()));
                }
                if (mLocalArtistAdapter != null && mLocalArtistAdapter.getItemCount() == 0) {
                    mStickHeadView.setText("");
                }
                Log.i(TAG, "onScrolled: stickHeadView = " + mStickHeadView.getText());
                // Get the sticky view's translationY by the first view below the sticky's height.
                View transInfoView = recyclerView.findChildViewUnder(
                        mStickHeadView.getMeasuredWidth() / 2, mStickHeadView.getMeasuredHeight() + 1);
                if (transInfoView != null && transInfoView.getTag() != null) {
                    int transViewStatus = (int) transInfoView.getTag();
                    int dealtY = transInfoView.getTop() - mStickHeadView.getMeasuredHeight();

                    if (transViewStatus == CommonConstants.TAG_HEAD_VIEW) {
                        // If the first view below the sticky's height scroll off the screen,
                        // then recovery the sticky view's translationY.
                        if (transInfoView.getTop() > 0) {
                            mStickHeadView.setTranslationY(dealtY);
                        } else {
                            mStickHeadView.setTranslationY(0);
                        }
                    } else if (transViewStatus == CommonConstants.TAG_CONTENT_VIEW) {
                        mStickHeadView.setTranslationY(0);
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
        initIndexBar(rootView);
        mBatchOperateLinearLayout = (LinearLayout) rootView.findViewById(R.id.batch_operate_linearlayout);
        mBatchOperateDelete = (RelativeLayout) rootView.findViewById(R.id.batch_operate_delete);
        mBatchOperateAddPlaylist = (RelativeLayout) rootView.findViewById(R.id.batch_operate_addplaylist);
        mBatchOperateDelete.setOnClickListener(this);
        mBatchOperateAddPlaylist.setOnClickListener(this);
        return rootView;
    }

    private void initIndexBar(View root) {
        mIndexBar = (MstIndexBar) root.findViewById(R.id.index_bar);
        mIndexBar.deleteLetter(0);
        mIndexBar.setLetterTextSize((int) getResources().getDimension(R.dimen.sp_8));
        mIndexBar.setColor(getResources().getColorStateList(R.color.index_bar_color));
        mIndexBar.setBalloonDiameter(0);
        mIndexBar.setEnables(true, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
        mIndexBar.setOnSelectListener((index, layer, letter) -> {
            Log.i(TAG, "onSelect: index = " + index + " layer = " + layer + " letter = " + letter);
            mCurrentSection = index;
            moveToPosition(mLocalArtistAdapter.getPositionForSection(index));
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
        LogUtil.d(TAG,"asdasda");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.batch_operate_delete:
                if (mLocalArtistAdapter.getmSelectedArtistIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_media_items);
                } else {
                    LogUtil.d(TAG, "batch_operate_delete and select items count is " + mLocalArtistAdapter.getmSelectedSongIds().size());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                    dialog.setMessage(R.string.confirm_to_delete);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            batchDeleteArtists();
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
                if (mLocalArtistAdapter.getmSelectedArtistIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_media_items);
                } else {
                    batchAddToPlayList();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 批量删除歌曲
     */
    private void batchDeleteArtists() {
        Long[] ids = new Long[mLocalArtistAdapter.getmSelectedArtistIds().size()];
        for (int i = 0; i < mLocalArtistAdapter.getmSelectedArtistIds().size(); i++) {
            LogUtil.d(TAG, "batchDeleteSongs and id is " + mLocalArtistAdapter.getmSelectedArtistIds().get(i));
            ids[i] = Long.valueOf(mLocalArtistAdapter.getmSelectedArtistIds().get(i));
        }
        mFileTask = new FileTask();
        mFileTask.execute(ids);
    }

    /**
     * 批量添加到歌单
     */
    private void batchAddToPlayList() {
        final ArrayList<Object> ids = new ArrayList<Object>();
        for (int i = 0; i < mLocalArtistAdapter.getmSelectedArtistIds().size(); i++) {
            ids.add(mLocalArtistAdapter.getmSelectedArtistIds().get(i));
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
                            result = DBUtil.addArtistsToFavorite(getActivity(), ids);
                        } else {
                            long playlistId = Long.valueOf(playlistUri.getLastPathSegment());
                            result = DBUtil.addArtistsToPlaylist(getActivity(), playlistId, ids);
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
        return new CommonCursorLoader(getActivity(),
                MusicMediaDatabaseHelper.Artists.CONTENT_URI,
                null,
                null,
                null,
                MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d(TAG, "data count is " + data.getCount());
        if (getActivity() == null) {
            return;
        }
        if (mLocalArtistAdapter == null) {
            mLocalArtistAdapter = new LocalArtistAdapter(getActivity(), data, null);
            setRecyclerAdapter(mLocalArtistAdapter);
        } else {
            mLocalArtistAdapter.changeCursor(data);
            View newStickyView = mRecyclerView.findChildViewUnder(mStickHeadView.getMeasuredWidth() / 2,
                    mStickHeadView.getMeasuredHeight() / 2);
            if (newStickyView != null && newStickyView.getContentDescription() != mStickHeadView.getText()) {
                mStickHeadView.setText(newStickyView.getContentDescription());
            }
        }
        LocalMusicActivity localMusicActivity = (LocalMusicActivity) getActivity();
        mLocalArtistAdapter.setmOnMediaFragmentSelectedListener(localMusicActivity.OnMediaFragmentSelectedListener);
        if (data == null || data.getCount() <= 0) {
            mArtistRelativeLayout.setVisibility(View.GONE);
            mArtistEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mArtistRelativeLayout.setVisibility(View.VISIBLE);
            mArtistEmptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void deleteItem(final int position) {
        //TODO delete item
        final Cursor cursor = mLocalArtistAdapter.getCursorAtAdapterPosition(position);
        if(cursor == null || cursor.isClosed()){
            return;
        }
        Log.d(TAG,"delete artist name is " + cursor.getString(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST)));
        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case mst.app.dialog.AlertDialog.BUTTON_POSITIVE: {
                        mLocalArtistAdapter.getmSelectedArtistIds().add(Integer.valueOf(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID)))));
                        batchDeleteArtists();
                    }
                    break;
                    case mst.app.dialog.AlertDialog.BUTTON_NEGATIVE:
                        break;
                }
                dialog.dismiss();
            }
        };
        DialogMenuUtils.displayIgnoreConfirmDialog(getActivity(),getActivity().getResources().getString(R.string.alert_title_delete_artist),getActivity().getResources().getString(R.string.alert_message_delete_artist,cursor.getString(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST)))
                ,onClick);
    }

    /**
     * 条目点击事件
     *
     * @param position
     */
    public void clickItem(int position) {
        LogUtil.d(TAG, "position is " + position);
        Log.d(TAG, "onClick and position is " + position);
        if (mLocalArtistAdapter.getItemCount() > position) {
            Cursor c = mLocalArtistAdapter.getCursorAtAdapterPosition(position);
            Bundle bundle = new Bundle();
            bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST_ID, c.getString(c.getColumnIndex
                    (MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID)));
            bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST, c.getString(c.getColumnIndexOrThrow
                    (MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST)));
            Intent intent = new Intent(getActivity(), LocalAlbumListActivity.class);
            intent.putExtras(bundle);
            getActivity().startActivity(intent);
        }

    }

    public void showBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
    }

    public void hideBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
    }

    public void setMultiMode(boolean isMulitMode, int firstSongId) {
        if (null != mLocalArtistAdapter && mLocalArtistAdapter instanceof LocalArtistAdapter) {
            LocalArtistAdapter localArtistAdapter = (LocalArtistAdapter) mLocalArtistAdapter;
            localArtistAdapter.setMultiMode(isMulitMode, firstSongId);
        }
    }

    public void refreshData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public void selectAll(boolean isSelect) {
        mLocalArtistAdapter.setSelectAll(isSelect);
        mLocalArtistAdapter.notifyDataSetChanged();
    }

    public boolean isSelectAll() {
        return mLocalArtistAdapter.isSelectAll();
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
            StringBuffer where = new StringBuffer(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID + " IN (");
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
            mLocalArtistAdapter.getmSelectedArtistIds().clear();
            refreshData();
            if (result) {
                ToastUtil.showToast(getActivity(), R.string.songs_removed);
            } else {
                ToastUtil.showToast(getActivity(), R.string.operation_failed);
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
