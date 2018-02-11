package cn.tcl.music.util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;

import cn.tcl.music.R;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import mst.app.dialog.AlertDialog;

public final class DialogMenuUtils {
    private static final String TAG = DialogMenuUtils.class.getSimpleName();
    private final static int MAX_LENGTH = 40;

    /**
     * 创建歌单对话框
     *
     * @param context
     * @param titleResId
     * @param onPlaylistChoice
     * @return
     */
    public static boolean displayCreateNewPlaylistDialog(final Context context, int titleResId, final SimplePlaylistChooserAdapter.OnPlaylistChoiceListener onPlaylistChoice) {
        final EditText title = new EditText(context);
        title.setTextColor(context.getResources().getColor(R.color.base_title_unselect_color));
        final EditText inputName = new EditText(context, null);
        EditTextLimitTextWatcher editTextLimitTextWatcher = new EditTextLimitTextWatcher(context, inputName, MAX_LENGTH, context.getString(R.string.character_exceed_limit));

        title.setText(titleResId);
        title.setTextSize(20);
        title.setBackground(null);
        title.setFocusable(false);
        title.setPadding(title.getPaddingLeft(), 0, 0, 0);
        inputName.setHint(R.string.name);
        inputName.setTextColor(context.getResources().getColor(R.color.base_title_unselect_color));
        inputName.setHintTextColor(context.getResources().getColor(R.color.black_30));
        inputName.setSingleLine(true);
        inputName.addTextChangedListener(editTextLimitTextWatcher);

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        String name = inputName.getText().toString();
                        Uri playlistUri = DBUtil.createPlayList(context, name);
                        if (null != playlistUri) {
                            onPlaylistChoice.onPlaylistChosen(playlistUri, name);
                        }
                        InputMethodManager positiveIm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        positiveIm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        InputMethodManager negativeIm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        negativeIm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        LinearLayout ll = new LinearLayout(context);
        ll.setPadding(context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_default_margin), context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_top_margin)
                , context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_default_margin), context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_bottom_margin));
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        ll.addView(title, lpTitle);
        LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_item_height));
        lpName.topMargin = context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_item_top_margin);
        ll.addView(inputName, lpName);
        LinearLayout.LayoutParams lpDesc = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.dialog_new_playlist_item_height));

        builder.setView(ll);
        builder.setPositiveButton(R.string.sure, onClick);
        builder.setNegativeButton(R.string.cancel, onClick);
        Dialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        final Button positiveBtn = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
        final Button negativeBtn = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
        positiveBtn.setEnabled(false);
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(inputName.getText().toString().trim())) {
                    positiveBtn.setEnabled(false);
                } else {
                    positiveBtn.setEnabled(true);
                }
            }
        });
        return true;
    }

    public static void displaySimpleConfirmDialog(final Context context, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.delete, onClick);
        builder.setNegativeButton(R.string.cancel, onClick);
        builder.show();
    }

    /**
     * 弹出忽略文件夹提示
     *
     * @param context
     * @param title
     * @param message
     * @param onClick
     */
    public static void displayIgnoreConfirmDialog(final Context context, String title, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, onClick);
        builder.setNegativeButton(R.string.cancel, onClick);
        builder.show();
    }

    /**
     * 添加到 彈出的 dialog
     */
    public static boolean displayAddToPlaylistDialog(Context context, SimplePlaylistChooserAdapter.OnPlaylistChoiceListener onPlaylistChoiceListener, int titleResId) {
        if (!(context instanceof Activity)) {
            return false;
        }
        final Cursor c = context.getContentResolver().query(MusicMediaDatabaseHelper.Playlists.CONTENT_URI,
                new String[]{MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID,
                        MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME},
                MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.TYPE + " != ?",
                new String[]{String.valueOf(MusicMediaDatabaseHelper.Playlists.AUTOMATIC_TYPE)},
                MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.dialog_recycler_view, null);
        View listItem = mInflater.inflate(R.layout.simple_list_item, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        builder.setView(view);
        Dialog dialog = builder.create();
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new SimplePlaylistChooserAdapter(context, c, onPlaylistChoiceListener, dialog));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                c.close();
            }
        });
        dialog.show();
        return true;
    }

    public static boolean fileIsExists(String s) {
        try {
            File f = new File(s);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /***
     * when we remove songs and delete the local files , we need to delete them in media table ,then we query system media.audio table,get its absolute path,
     * and delete it in system media.audio table; we won't delete items in queue table because if the song is playing ,doing deletion will stop playing and clear
     * the whole queue table.
     *
     * @param context
     * @param cursor
     * @return
     */
    public static boolean removeFromTableAndDeleteLocalFileIfNecessary(Context context, Cursor cursor) {
        Uri mediaStoreUri = null;
        String selection = null;
        String[] selectionArgs = null;
        String colName;
        boolean shouldRemoveFromAndroid = false;
        colName = MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID;
        long itemId = cursor.getLong(cursor.getColumnIndex(colName));
        LogUtil.d(TAG, "removeFromTableAndDeleteLocalFileIfNecessary and item is is " + itemId);
        mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        selection = BaseColumns._ID + " = ?";
        selectionArgs = new String[]{String.valueOf(itemId)};
        shouldRemoveFromAndroid = true;
        DBUtil.deleteMediaInMediaTableWithMediaId(context, itemId);
        removeFromPlaylistSongsTable(context, itemId);
        boolean success = true;
        if (shouldRemoveFromAndroid) {
            Cursor c = context.getContentResolver().query(mediaStoreUri, new String[]{MediaStore.Audio.Media.DATA}, selection, selectionArgs, null);
            if (c == null) {
                return false;
            }
            success = false;
            while (c.moveToNext()) {
                String path = c.getString(0);
                if (fileIsExists(path)) {
                    File fileToRemove = new File(path);
                    LogUtil.d(TAG, "removeFromTableAndDeleteLocalFileIfNecessary and delete local file path=....." + path);
                    success = fileToRemove.delete();
                    if (!success)
                        break;
                }
            }
            c.close();
        }
        if (success) {
            context.getContentResolver().delete(mediaStoreUri, selection, selectionArgs);
        }
        return success;
    }

    /**
     * remove a song in playlist_songs table by its media_ID
     *
     * @param context
     * @param itemID  the song's mediaID
     * @return
     */
    public static int removeFromPlaylistSongsTable(Context context, long itemID) {
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " = ?",
                new String[]{String.valueOf(itemID)});
    }

}
