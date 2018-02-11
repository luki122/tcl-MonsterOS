package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.framework.log.NLog;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumDetailActivity;
import cn.tcl.music.activities.LocalAlbumListActivity;
import cn.tcl.music.adapter.AlbumListAdapter;
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

public class LocalAlbumListFragment extends BaseRecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static final String TAG = LocalAlbumListFragment.class.getSimpleName();
    private String mArtistId;
    private TextView mAlnumsCountTextView;
    private TextView mAlbumEmptyTextView;
    private LinearLayout mAlbumLinearLayout;
    private AlbumListAdapter mAlbumListAdapter;

    public LinearLayout mBatchOperateLinearLayout;
    RelativeLayout mBatchOperateDelete;
    RelativeLayout mBatchOperateAddPlaylist;
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
                        getLoaderManager().restartLoader(0, null, LocalAlbumListFragment.this);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate and artist id is " + getArguments().getString(CommonConstants.BUNDLE_KEY_ARTIST_ID));
        mArtistId = getArguments().getString(CommonConstants.BUNDLE_KEY_ARTIST_ID);
        if (getActivity() == null) {
            return;
        } else if (getActivity() instanceof LocalAlbumListActivity) {
            LocalAlbumListActivity localAlbumListActivity = (LocalAlbumListActivity) getActivity();
            setmOnMediaFragmentSelectedListener(localAlbumListActivity.OnAlbumFragmentSelectedListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnMediaFragmentSelectedListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local_album, container, false);
        mAlnumsCountTextView = (TextView) rootView.findViewById(R.id.alnums_count_tv);
        mAlbumEmptyTextView = (TextView) rootView.findViewById(R.id.album_empty_tv);
        mAlbumLinearLayout = (LinearLayout) rootView.findViewById(R.id.ll_album);
        mBatchOperateLinearLayout = (LinearLayout) rootView.findViewById(R.id.batch_operate_linearlayout);
        mBatchOperateDelete = (RelativeLayout) rootView.findViewById(R.id.batch_operate_delete);
        mBatchOperateAddPlaylist = (RelativeLayout) rootView.findViewById(R.id.batch_operate_addplaylist);
        mBatchOperateDelete.setOnClickListener(this);
        mBatchOperateAddPlaylist.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_ID + " = ?";
        String[] selectionArgs = new String[]{mArtistId};
        return new CommonCursorLoader(getActivity(),
                MusicMediaDatabaseHelper.Albums.CONTENT_URI,
                DBUtil.defaultAlbumColumns,
                selection,
                selectionArgs,
                MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM_KEY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        if (mAlbumListAdapter == null) {
            mAlbumListAdapter = new AlbumListAdapter(getActivity(), data, mImageFetcher);
            setRecyclerAdapter(mAlbumListAdapter);
        } else {
            mAlbumListAdapter.changeCursor(data);
        }
        if (data == null || data.getCount() <= 0) {
            manageEmptyView(true);
            mAlbumLinearLayout.setVisibility(View.GONE);
            mAlbumEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            manageEmptyView(false);
            mAlbumLinearLayout.setVisibility(View.VISIBLE);
            mAlbumEmptyTextView.setVisibility(View.GONE);
        }
        LocalAlbumListActivity localAlbumListActivity = (LocalAlbumListActivity)getActivity();
        mAlbumListAdapter.setmOnAlbumFragmentSelectedListener(localAlbumListActivity.OnAlbumFragmentSelectedListener);
        refreshTotalNumber(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAlbumListAdapter == null)
            return;
        mAlbumListAdapter.changeCursor(null);
    }

    @Override
    protected void onRecyclerItemClick(RecyclerView.ViewHolder viewHolder, int position,
                                       View v) {
        Log.d(TAG, "LocalAlbumListFragment onRecyclerItemClick");
//        Cursor c = ((RecyclerViewCursorAdapter<?>) mAdapter).getCursorAtAdapterPosition(position);
//        LibraryNavigationUtil.goTo(ItemMediaType.ALBUM, getActivity(), c);
    }

    @Override
    protected void onPopulatePopupMenu(MenuInflater menuInflater, Menu menu, RecyclerView.ViewHolder itemViewHolder, int position) {
        Log.d(TAG, "LocalAlbumListFragment onRecyclerItemClick");
//        Cursor c = ((RecyclerViewCursorAdapter<?>) mAdapter).getCursorAtAdapterPosition(position);
//        LibraryNavigationUtil.goTo(ItemMediaType.ALBUM, getActivity(), c);
    }

    @Override
    protected boolean onPopupMenuItemSelected(MenuItem item, RecyclerView.ViewHolder itemViewHolder, int position) {
        return false;
//        if (mAdapter == null) {
//            return false;
//        }
//        Cursor c = mAlbumListAdapter.getCursorAtAdapterPosition(position);
//        return  LibraryNavigationUtil.manageMenuItem(ItemMediaType.ALBUM, item, getActivity(), c);
    }

    @Override
    public void onResume() {
        super.onResume();
        NLog.d(TAG, "LocalAlbumListFragment onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        NLog.d(TAG, "LocalAlbumListFragment onPause");
    }

    /**
     * 刷新总数
     */
    private void refreshTotalNumber(Cursor data) {
        if (null == data || data.getCount() == 0) {
            mAlnumsCountTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_albums, 0, 0));
        } else {
            mAlnumsCountTextView.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_albums, data.getCount(), data.getCount()));
        }
    }

    public void clickItem(int position) {
        Cursor c = mAlbumListAdapter.getCursorAtAdapterPosition(position);

        Bundle bundle = new Bundle();
        bundle.putString(CommonConstants.BUNDLE_KEY_ALBUM_NAME, c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM)));
        bundle.putString(CommonConstants.BUNDLE_KEY_ALBUM_ID, c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID)));
        bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST, c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST)));
        bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST_ID, c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_ID)));
        bundle.putString(CommonConstants.BUNDLE_KEY_ARTWORK, c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTWORK)));

        Intent intent = new Intent(getActivity(), LocalAlbumDetailActivity.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    /**
     * 删除专辑
     *
     * @param position
     */
    public void deleteItem(int position) {
        final Cursor cursor = mAlbumListAdapter.getCursorAtAdapterPosition(position);
        if (cursor == null || cursor.isClosed()) {
            return;
        }
        Log.d(TAG, "delete album name is " + cursor.getString(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST)));
        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        if (cursor != null && !cursor.isClosed()) {
                            mAlbumListAdapter.getmSelectedAlbumIds().add(Integer.valueOf(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID)))));
                            batchDeleteAlbums();
                        }
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        break;
                }
                dialog.dismiss();
            }
        };
        DialogMenuUtils.displayIgnoreConfirmDialog(getActivity(), getActivity().getResources().getString(R.string.alert_title_delete_album), getActivity().getResources().getString(R.string.alert_message_delete_album, cursor.getString(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM)))
                , onClick);
    }

    protected boolean mIsMultiMode = false;

    public void setMultiMode(boolean isMultiMode) {
        if (null != mAlbumListAdapter) {
            mAlbumListAdapter.setMultiMode(isMultiMode, -1);
            mIsMultiMode = isMultiMode;
//            DISPLAY_SORT_OPTIONS = !isMulitMode;
        }
    }

    public void showBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
    }

    public void hideBottomActionLayout() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.batch_operate_delete:
                Log.d(TAG, "onClick delete and select count is " + mAlbumListAdapter.getmSelectedAlbumIds().size());
                if (mAlbumListAdapter.getmSelectedAlbumIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_album_items);
                } else {
                    Log.d(TAG, "batch_operate_delete and select items count is " + mAlbumListAdapter.getmSelectedAlbumIds().size());
                    mst.app.dialog.AlertDialog.Builder dialog = new mst.app.dialog.AlertDialog.Builder(getActivity());
                    dialog.setMessage(R.string.confirm_to_delete);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            batchDeleteAlbums();
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
                Log.d(TAG, "onClick add and select count is " + mAlbumListAdapter.getmSelectedAlbumIds().size());
                if (mAlbumListAdapter.getmSelectedAlbumIds().size() == 0) {
                    ToastUtil.showToast(getActivity(), R.string.please_select_album_items);
                } else {
                    batchAddToPlayList();
                }
                break;
        }
    }

    public void noclickableplayall() {
//        mPlayAllLayout.setClickable(false);
//        mTextViewPlayAll.setAlpha(0.40f);
//        mImageViewPlayAll.setAlpha(0.40f);
    }

    public void clickableplayall() {
//        mPlayAllLayout.setClickable(true);
//        mTextViewPlayAll.setAlpha(1f);
//        mImageViewPlayAll.setAlpha(1f);
    }

    private void setmOnMediaFragmentSelectedListener(
            OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener) {
        this.mOnMediaFragmentSelectedListener = mOnMediaFragmentSelectedListener;
    }

    public void selectAll(boolean isSelect) {
        if (null != mAlbumListAdapter) {
            mAlbumListAdapter.setSelectAll(isSelect);
            mAlbumListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 批量删除歌手
     */
    private void batchDeleteAlbums() {
        Long[] ids = new Long[mAlbumListAdapter.getmSelectedAlbumIds().size()];
        for (int i = 0; i < mAlbumListAdapter.getmSelectedAlbumIds().size(); i++) {
            LogUtil.d(TAG, "batchDeleteSongs and id is " + mAlbumListAdapter.getmSelectedAlbumIds().get(i));
            ids[i] = Long.valueOf(mAlbumListAdapter.getmSelectedAlbumIds().get(i));
        }
        mFileTask = new FileTask();
        mFileTask.execute(ids);
    }

    /**
     * 批量添加到歌单
     */
    private void batchAddToPlayList() {
        Log.d(TAG, "batchAddToPlayList");
        final ArrayList<Object> ids = new ArrayList<Object>();
        for (int i = 0; i < mAlbumListAdapter.getmSelectedAlbumIds().size(); i++) {
            ids.add(mAlbumListAdapter.getmSelectedAlbumIds().get(i));
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
                            result = DBUtil.addAlbumsToFavorite(getActivity(),ids);
                        } else
                        {
                            long playlistId = Long.valueOf(playlistUri.getLastPathSegment());
                            result = DBUtil.addAlbumsToPlaylist(getActivity(), playlistId, ids);
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
            StringBuffer where = new StringBuffer(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID + " IN (");
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
            mAlbumListAdapter.getmSelectedArtistIds().clear();
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
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.operating));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
//        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                if (mFileTask != null && mFileTask.getStatus() != AsyncTask.Status.FINISHED) {
//                    mFileTask.cancel(true);
//                }
//            }
//        });
        mProgressDialog.show();
    }

    private void refreshData() {
        getLoaderManager().restartLoader(1, null, this);
    }

    public void goToMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        ((LocalAlbumListActivity)getActivity()).showActionMode(true);
        ((LocalAlbumListActivity)getActivity()).setMultiMode(true);
        ((LocalAlbumListActivity)getActivity()).getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void leaveMultiChoose() {
        mBatchOperateLinearLayout.setVisibility(View.GONE);
        setMultiMode(false);
        ((LocalAlbumListActivity) getActivity()).setMultiMode(false);
        ((LocalAlbumListActivity) getActivity()).showActionMode(false);
    }

    public boolean isSelectAll() {
        return mAlbumListAdapter.isSelectAll();
    }

}
