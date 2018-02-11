package cn.tcl.music.fragments;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tcl.music.R;
import cn.tcl.music.activities.FolderDetailActivity;
import cn.tcl.music.adapter.LocalFolderAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.loaders.CommonCursorLoader;
import cn.tcl.music.util.LogUtil;
import mst.app.dialog.AlertDialog;

public class IgnoredFragment extends BaseRecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static final String TAG = IgnoredFragment.class.getSimpleName();

    private TextView mAllRecoverScanTextView;
    private TextView mFolderAndSongsCountTextView;
    private LinearLayout mIgnoreLayout;
    private LocalFolderAdapter mLocalFolderAdapter;
    private RelativeLayout mIgnoredAndScanLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPopulatePopupMenu(MenuInflater menuInflater, Menu menu, RecyclerView.ViewHolder itemViewHolder, int position) {
        LogUtil.d(TAG, "LocalFolderFragment onRecyclerItemClick position = " + position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ignored, container, false);
        mAllRecoverScanTextView = (TextView) rootView.findViewById(R.id.folder_recover_scan_all);
        mFolderAndSongsCountTextView = (TextView) rootView.findViewById(R.id.folder_and_songs_count);
        mIgnoreLayout = (LinearLayout) rootView.findViewById(R.id.ll_ignore);
        mIgnoredAndScanLayout = (RelativeLayout) rootView.findViewById(R.id.ignored_and_scan_layout);
        mAllRecoverScanTextView.setTextColor(this.getResources().getColor(R.color.recover_scan));
        mAllRecoverScanTextView.setOnClickListener(this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.VALUE_FOLDER_IS_NOT_SCAN)};
        return new CommonCursorLoader(getActivity(),
                MusicMediaDatabaseHelper.Folders.CONTENT_URI, DBUtil.defaultFolderColumns, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        if (mLocalFolderAdapter == null) {
            mLocalFolderAdapter = new LocalFolderAdapter(getActivity(), data, mImageFetcher, true);
            setRecyclerAdapter(mLocalFolderAdapter);
        } else {
            mLocalFolderAdapter.changeCursor(data);
        }
        if (data == null || data.getCount() <= 0) {
            manageEmptyView(true);
            mIgnoreLayout.setVisibility(View.GONE);
            mIgnoredAndScanLayout.setVisibility(View.GONE);
        } else {
            manageEmptyView(false);
            mIgnoreLayout.setVisibility(View.VISIBLE);
            mIgnoredAndScanLayout.setVisibility(View.VISIBLE);
        }
        refreshTotalCount(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mLocalFolderAdapter != null) {
            mLocalFolderAdapter.changeCursor(null);
        }
    }

    @Override
    protected void onRecyclerItemClick(RecyclerView.ViewHolder viewHolder, int position, View v) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.folder_recover_scan_all:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(),
                        android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                dialog.setMessage(R.string.dialogmessage_all_recover_scan);
                dialog.setCancelable(true);
                dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recoverScanAll();
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
                break;
        }
    }

    public void clickItem(int position) {
        if (mLocalFolderAdapter.getItemCount() > position) {
            Cursor c = mLocalFolderAdapter.getCursorAtAdapterPosition(position);
            Integer foldeId = c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID));
            String folderName = c.getString(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_NAME));
            int songNum = c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_SONGS_NUM));
            int isScan = c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN));

            Bundle bundle = new Bundle();
            bundle.putInt(CommonConstants.BUNDLE_KEY_FOLDER_TYPE, CommonConstants.VALUE_FOLDER_IS_NOT_SCAN);
            bundle.putString(CommonConstants.BUNDLE_KEY_FOLDER_NAME, folderName);
            bundle.putString(CommonConstants.BUNDLE_KEY_FOLDER_ID, String.valueOf(foldeId));
            bundle.putInt(CommonConstants.BUNDLE_KEY_FOLDER_SONG_NUM, songNum);
            bundle.putInt(CommonConstants.BUNDLE_KEY_FOLDER_IS_SCAM, isScan);

            //jump to scan folder detail
            Intent intent = new Intent(getActivity(), FolderDetailActivity.class);
            intent.putExtras(bundle);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void recoverScan(int position) {
        Log.d(TAG, "recoverScan and position is " + position);
        if (null == mLocalFolderAdapter || mLocalFolderAdapter.getItemCount() <= position) {
            Log.d(TAG, "deleteItem and adapter is null or out of bounds");
        } else {

            final Cursor c = mLocalFolderAdapter.getCursorAtAdapterPosition(position);
            Log.d(TAG, c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID)) + "");

            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN, 1);
            String where = MusicMediaDatabaseHelper.Folders.FoldersColumns._ID + " = ? ";
            String[] selectionArgs = new String[]{String.valueOf(c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID)))};

            getActivity().getContentResolver().update(MusicMediaDatabaseHelper.Folders.CONTENT_URI,
                    contentValues, where, selectionArgs);

            c.close();
            getLoaderManager().getLoader(0).onContentChanged();


            //send broadcast to inform LocalMediaFragment refresh data
            Intent intent = new Intent();
            intent.setAction(CommonConstants.BROADCAST_IGNORE_OR_RECOVER_FOLDER);
            getActivity().sendBroadcast(intent);
        }
    }

    @Override
    public void leaveMultiChoose() {

    }

    public void recoverScanAll() {
        if (null == mLocalFolderAdapter || mLocalFolderAdapter.getItemCount() == 0) {
            Log.d(TAG, "deleteItem and adapter is null or empty");
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN, 1);
            getActivity().getContentResolver().update(MusicMediaDatabaseHelper.Folders.CONTENT_URI,
                    contentValues, null, null);
            getLoaderManager().getLoader(0).onContentChanged();
            //发送广播通知歌曲界面进行刷新
            Intent intent = new Intent();
            intent.setAction(CommonConstants.BROADCAST_IGNORE_OR_RECOVER_FOLDER);
            getActivity().sendBroadcast(intent);
        }
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
