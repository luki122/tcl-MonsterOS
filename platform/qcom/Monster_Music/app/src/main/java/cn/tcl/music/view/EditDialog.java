package cn.tcl.music.view;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;

import cn.tcl.music.model.MediaInfo;

public class EditDialog extends AlertDialog {
    private final String TAG = "EditDialog";


    public enum ItemMediaType {
        SINGLE_MEDIA,
        ALL_MEDIA,
        FAVORITE,
        RECORDS,
        LASTADD,
        HIDDEN,
        ARTIST,
        ALBUM,
        GENRE,
        PLAYLIST,
        REMOTE_FOLDER,
        REMOTE_MEDIA,
        FOLDER
    }

    private final Uri MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final Uri ALBUM_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    private final Uri GENRES_URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
    public final static String UPDATE_ALBUMS_OR_ARTISTS_ID = "new_albums_or_artists_id";
    public final static String UPDATE_ALBUMS_OR_ARTISTS = "new_albums_or_artists";
    private final Context mContext;
    private final MediaInfo mMediaInfo;
    private ItemMediaType mItemMediaType;
    private EditText mTrack;
    private EditText mAlbum;
    private EditText mArtist;
    private EditText mGenre;
    private OnEditDialogClickListener mOnEditDialogClickListener;
    public interface OnEditDialogClickListener{
        void OnEditDialogOk(String title, String album, String artist, String genre);
    }

    private View.OnClickListener Oklistener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }

    };

    public EditDialog(Context context,ItemMediaType itemMediaType,final MediaInfo mediaInfo) {
        super(context);
        mContext = context;
        mMediaInfo = mediaInfo;
        mItemMediaType = itemMediaType;
    }

    public EditDialog(Context context,ItemMediaType itemMediaType,final MediaInfo mediaInfo,final OnEditDialogClickListener listener) {
            super(context,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            mContext = context;
            mMediaInfo = mediaInfo;
            mItemMediaType = itemMediaType;
            mOnEditDialogClickListener= listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    private String getText(EditText editText) {
        return null != editText ? editText.getText().toString().trim() : "";
    }
}
