package cn.tcl.music.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.fragments.PlaylistDetailFragment;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.EditTextLimitTextWatcher;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.image.AsyncTask;
import cn.tcl.music.widget.ActionModeHandler;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;

public class PlaylistDetailActivity extends BaseMusicActivity implements
        View.OnClickListener, LocalMediaAdapter.IonSlidingViewClickListener {

    private final static String TAG = PlaylistDetailActivity.class.getSimpleName();

    private final static int MAX_LENGTH = 40;
    private final static int RENAME_COMPLETE = 1;
    private PlaylistDetailFragment mPlaylistDetailFragment;
    private ImageView mArtworkImageView;
    private EditText mTitleEditText;
    private TextView mAddTextView;
    private TextView mEditTextView;
    private TextView mDeleteTextView;
    private TextView mDownloadTextView;
    private String mTitle;
    private String mPlaylistId;
    private ProgressDialog mProgressDialog;
    private PlaylistTask mPlaylistTask;
    private ActionModeHandler mActionModeHandler;

    public static final int REQUEST_CODE_SELECT_SONGS = 1000;
    public static final int RESULT_CODE_SELECT_SONGS = REQUEST_CODE_SELECT_SONGS + 1;
    public static final int RESULT_CODE_SELECT_SONGS_CANCEL = REQUEST_CODE_SELECT_SONGS + 2;

    public PlaylistDetailFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener =
            new PlaylistDetailFragment.OnMediaFragmentSelectedListener() {
                @Override
                public void onAudioSelectdNum(ArrayList<Integer> songIds) {
                    if (mActionModeHandler == null) {
                        mActionModeHandler = new ActionModeHandler(PlaylistDetailActivity.this);
                    }
                    mActionModeHandler.setItemNum(songIds);
                }
            };


    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_play_list);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getIntent()) {
            mPlaylistDetailFragment = new PlaylistDetailFragment();
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            mPlaylistDetailFragment.setArguments(bundle);
            mTitle = bundle.getString(CommonConstants.BUNDLE_KEY_PLAYLIST_TITLE);
            mPlaylistId = bundle.getString(CommonConstants.BUNDLE_KEY_PLAYLIST_ID);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, mPlaylistDetailFragment, PlaylistDetailActivity.class.getSimpleName()).commit();
            initToolBar();
            initView(mTitle);
            mActionModeHandler = new ActionModeHandler(this);

            if (bundle.getInt(CommonConstants.BUNDLE_KEY_PLAYLIST_JUMP_FLAG) == CommonConstants.VALUE_PLAYLIST_FLAG_IMPORT_MEDIA) {
                //import media
                Intent selectIntent = new Intent(this, SelectSongsActivity.class);
                selectIntent.putExtra(CommonConstants.SELECT_SONGS_ADD, CommonConstants.SELECT_SONGS_TO_PLAYLIST);
                selectIntent.putExtra(CommonConstants.BUNDLE_KEY_SELECT_SONGS_FROM,CommonConstants.VALUE_SELECT_SONGS_FROM_IMPORT);
                selectIntent.putExtra(CommonConstants.BUNDLE_KEY_PLAYLIST_ID, mPlaylistId);
                startActivityForResult(selectIntent, REQUEST_CODE_SELECT_SONGS);
            }
        }

    }

    @Override
    public void onCurrentMusicMetaChanged() {
        mPlaylistDetailFragment.onCurrentMetaChanged();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_detail_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(getResources().getString(R.string.playlist));
        toolbar.setTitleTextAppearance(PlaylistDetailActivity.this,R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(mOnMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        mPlaylistDetailFragment.leaveMultiChoose();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        if (mPlaylistDetailFragment.isSelectAll()) {
                            getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                            mPlaylistDetailFragment.selectAll(false);
                        } else {
                            getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                            mPlaylistDetailFragment.selectAll(true);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {
            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {
            }
        });
    }

    private void initView(String title) {
        mTitleEditText = (EditText) findViewById(R.id.playlist_detail_title_et);
        mTitleEditText.addTextChangedListener(new PlayListTitleEditTextWatcher(this, mTitleEditText, MAX_LENGTH,
                getString(R.string.character_exceed_limit)));

        mArtworkImageView = (ImageView) findViewById(R.id.artwork_image_view);
        mAddTextView = (TextView) findViewById(R.id.detail_add_tv);
        mEditTextView = (TextView) findViewById(R.id.detail_edit_tv);
        mDeleteTextView = (TextView) findViewById(R.id.detail_delete_tv);
        mDownloadTextView = (TextView) findViewById(R.id.detail_download_tv);

        mTitleEditText.setText(title);
        mAddTextView.setOnClickListener(this);
        mEditTextView.setOnClickListener(this);
        mDeleteTextView.setOnClickListener(this);
        mDownloadTextView.setOnClickListener(this);
    }

    public void getArtwork(){
        String artWork = mPlaylistDetailFragment.getPlaylistArtwork();
        Glide.with(this)
                .load(artWork)
                .placeholder(R.drawable.empty_album)
                .into(mArtworkImageView);
    }

    private Toolbar.OnMenuItemClickListener mOnMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(PlaylistDetailActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.detail_add_tv:
                ArrayList<Long> arrayList = mPlaylistDetailFragment.getSongsIds();
                long[] ids = new long[arrayList.size()];
                for (int i = 0; i < arrayList.size(); i++){
                    ids[i] = arrayList.get(i);
                }
                Intent selectIntent = new Intent(this, SelectSongsActivity.class);
                selectIntent.putExtra(CommonConstants.SELECT_SONGS_ADD, CommonConstants.SELECT_SONGS_TO_PLAYLIST);
                selectIntent.putExtra(CommonConstants.BUNDLE_KEY_PLAYLIST_ADD_FLAG, ids);
                selectIntent.putExtra(CommonConstants.BUNDLE_KEY_PLAYLIST_ID, mPlaylistId);
                startActivityForResult(selectIntent, REQUEST_CODE_SELECT_SONGS);
                break;
            case R.id.detail_edit_tv:
                setDetailActionEnable(false);
                mTitleEditText.requestFocus();
                mTitleEditText.setSelection(mTitle.length());
                break;
            case R.id.detail_delete_tv:
                showDeletePlayListDialog();
                break;
            case R.id.detail_download_tv:
                //TODO click download
                break;
        }
    }

    /**
     * show delete playlist dialog
     */
    private void showDeletePlayListDialog() {
        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE: {
                        mPlaylistTask = new PlaylistTask();
                        mPlaylistTask.execute();
                    }
                    break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        break;
                }
                dialog.dismiss();
            }
        };
        DialogMenuUtils.displaySimpleConfirmDialog(PlaylistDetailActivity.this, PlaylistDetailActivity.this.getString(R.string.confirmation_remove_playlist, mTitle), onClick);
    }


    public void onItemClick(View view, int position) {
        if (null != mPlaylistDetailFragment) {
            mPlaylistDetailFragment.clickItem(position);
        }
    }

    public void onDeleteBtnClick(View view, int position) {
        if (null != mPlaylistDetailFragment) {
            mPlaylistDetailFragment.deleteItem(position);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d(TAG, "onActivityResult and result code is " + resultCode);
        if (requestCode == REQUEST_CODE_SELECT_SONGS) {
            if (resultCode == RESULT_CODE_SELECT_SONGS && data.getExtras().getBoolean(CommonConstants.ADD_SUCCESS_KEY)) {
                ToastUtil.showToast(PlaylistDetailActivity.this, R.string.save_success);
            }
            mPlaylistDetailFragment.refreshData();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP && mTitleEditText.isFocused()) {
            View v = getCurrentFocus();
            if (isShouldUpdate(v, ev)) {
                updatePlaylistView();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldUpdate(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void updatePlaylistView() {
        renamePlaylist();
        setDetailActionEnable(true);
        mTitleEditText.setText(mTitle);
    }

    private void setDetailActionEnable(boolean isAvailable) {
        mTitleEditText.setEnabled(!isAvailable);
        if (isAvailable) {
            mTitleEditText.setBackgroundColor(getColor(R.color.transparent));
        } else {
            mTitleEditText.setBackgroundColor(getColor(R.color.white_50));
        }
        setPlayListTitleEnable(isAvailable);
    }

    public void setPlayListTitleEnable(boolean isAvailable) {
        mAddTextView.setClickable(isAvailable);
        mEditTextView.setClickable(isAvailable);
        mDeleteTextView.setClickable(isAvailable);
        mDownloadTextView.setClickable(isAvailable);
        if (isAvailable) {
            mAddTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
            mEditTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
            mDeleteTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
            mDownloadTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
        } else {
            mAddTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
            mEditTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
            mDeleteTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
            mDownloadTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        }
    }

    private void renamePlaylist() {
        String newTitle = mTitleEditText.getText().toString();
        if (newTitle.trim().length() == 0) {
            ToastUtil.showToast(this, getString(R.string.playlist_name_can_not_be_empty));
        } else if (!mTitle.equals(newTitle)) {
            if (DBUtil.doesPlaylistExist(this, newTitle, MusicMediaDatabaseHelper.Playlists.CONTENT_URI)) {
                ToastUtil.showToast(this, com.mixvibes.mvlib.R.string.playlist_name_already_exists_);
                return;
            }
            String where = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID + " = ? ";
            String[] selectionArgs = new String[]{mPlaylistId};
            ContentValues updateName = new ContentValues();
            updateName.put(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME, newTitle.toString());
            int complete = getContentResolver().update(MusicMediaDatabaseHelper.Playlists.CONTENT_URI, updateName,
                    where, selectionArgs);
            if (complete == RENAME_COMPLETE) {
                mTitle = newTitle.toString();
            } else {
                ToastUtil.showToast(this, getString(R.string.rename_playlist_failed));
            }
        }
    }

    private boolean deletePlaylist() {
        //delete the record of playlist
        String where_playlist = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID + " =? ";
        String[] selectArgs_playlist = new String[]{String.valueOf(mPlaylistId)};
        int result = getContentResolver().delete(MusicMediaDatabaseHelper.Playlists.CONTENT_URI, where_playlist, selectArgs_playlist);
        if (result == 1) {
            //delete playlist success,need to delete related rows of playlistsongs table
            String where_playlistsongs = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ?";
            String[] selectArgs_playlistsongs = new String[]{String.valueOf(mPlaylistId)};
            getContentResolver().delete(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, where_playlistsongs, selectArgs_playlistsongs);
            //TODO the playlist deleted maybe playing now,need to update play logic
        } else {
            //delete playlist failure
            return false;
        }
        return true;
    }

    /**
     * show delete playlist progress dialog
     */
    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(PlaylistDetailActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.delete));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
//        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                if (mPlaylistTask != null) {
//                    mPlaylistTask.cancel(true);
//                }
//            }
//        });
        mProgressDialog.show();
    }

    private class PlaylistTask extends AsyncTask<Void, Object, Boolean> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            if (!isCancelled()) {
                result = deletePlaylist();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            if (result) {
                ToastUtil.showToast(PlaylistDetailActivity.this, PlaylistDetailActivity.this.getString(R.string.success_delete_playlist, mTitle));
                finish();
            } else {
                ToastUtil.showToast(PlaylistDetailActivity.this, PlaylistDetailActivity.this.getString(R.string.delete_playlist_failed));
            }
        }
    }

    private class PlayListTitleEditTextWatcher extends EditTextLimitTextWatcher {

        private int mStart;
        private int mCount;

        public PlayListTitleEditTextWatcher(Context context, EditText editText, int maxLength, String toastText) {
            super(context, editText, maxLength, toastText);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
            mStart = start;
            mCount = count;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.subSequence(mStart, mStart + mCount).toString().equals("\n")) {
                s.replace(mStart, mStart + mCount, "");
                updatePlaylistView();
            } else {
                super.afterTextChanged(s);
            }
        }
    }
    public void goToMultiChoose() {
        mPlaylistDetailFragment.mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        mPlaylistDetailFragment.noclickableplayall();
        showActionMode(true);
        getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void onBackPressed() {
        if (getActionMode().isShowing()) {
            mPlaylistDetailFragment.leaveMultiChoose();
            return;
        }
        super.onBackPressed();
    }

    public void setSelectedNumber(int selectedNum) {
        String format = getResources().getString(R.string.batch_songs_num);
        getActionMode().setTitle(String.format(format, selectedNum));
    }
}
