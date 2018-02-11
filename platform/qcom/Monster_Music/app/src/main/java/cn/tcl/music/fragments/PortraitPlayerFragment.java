package cn.tcl.music.fragments;

import android.app.Activity;

import cn.tcl.music.util.ToastUtil;
import mst.app.dialog.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tcl.framework.log.NLog;
import com.tcl.framework.notification.NotificationCenter;
import com.tcl.framework.notification.TopicSubscriber;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumDetailActivity;
import cn.tcl.music.activities.LocalAlbumListActivity;
import cn.tcl.music.activities.PlayingActivity;
import cn.tcl.music.adapter.SimplePlaylistChooserAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.MixUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.view.PlayMusicButton;
import cn.tcl.music.view.VisualizerView;
import cn.tcl.music.view.image.ImageFetcher;

public class PortraitPlayerFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = PortraitPlayerFragment.class.getSimpleName();

    private ImageView mArtworkImageView;
    private TextView mSongNameTextView;
    private TextView mSingerNameTextView;
    private LinearLayout mSongLinearLayout;
    private LinearLayout mSettingLinearLayout;
    private LinearLayout mAddLinearLayout;
    private LinearLayout mDownloadLinearLayout;
    private LinearLayout mSingerLinearLayout;
    private LinearLayout mAlbumLinearLayout;
    private LinearLayout mSoundLinearLayout;
    private VisualizerView mVisualizerView;
    protected ViewGroup mRootView;
    private ImageView mDownloadImageView;
    private TextView mDownloadTextView;
    private ArrayList<LinearLayout> mSettingLayoutList = new ArrayList<>();
    private PlayMusicSubscriber mPlayMusicSubscriber;
    private ImageFetcher mImageFetcher;
    private ProgressDialog mProgressDialog;

    private static final int MSG_ADD_SUCCESS = 0x01;
    private static final int MSG_ADD_FAILURE = 0x02;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_SUCCESS: {
                    if (MusicPlayBackService.getCurrentMediaInfo() != null) {
                        HandleObject object = (HandleObject) msg.obj;
                        if (MusicMediaDatabaseHelper.Playlists.FAVORITE_URI.toString().equals(object.mUri.toString())) {
                            MusicPlayBackService.getCurrentMediaInfo().Favorite = true;
                            ((PlayingActivity) getActivity()).updateMyFavouriteButton();
                            ((PlayingActivity) getActivity()).notityQueueFragment();
                        }
                        ToastUtil.showToast(getActivity(), getActivity().getString(R.string.song_had_been_added_to_playlist, object.mPlayListName));
                    }
                }
                break;
                case MSG_ADD_FAILURE:
                    ToastUtil.showToast(getActivity(), getActivity().getString(R.string.operation_failed));
                    break;
                default:
                    break;
            }
            mProgressDialog.dismiss();
        }
    };

//    @Override
//    protected int inflateContentView() {
//        return R.layout.simple_player_layout_v3;
//    }
//    @Override
//    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
//        super.layoutInit(inflater, savedInstanceSate);
//        initView();
//    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_portrait_player, null);

        int mArtworkHeightPixels = getActivity().getResources().getDimensionPixelSize(
                R.dimen.play_bitmap_size);
        mImageFetcher = new ImageFetcher(getActivity(), mArtworkHeightPixels, R.drawable.default_cover_menu);
        initView();
        return mRootView;
    }

    private void initView() {
        mVisualizerView = (VisualizerView) mRootView.findViewById(R.id.iv_visualizer_view);
        mSongNameTextView = (TextView) mRootView.findViewById(R.id.iv_play_song_name);
        mArtworkImageView = (ImageView) mRootView.findViewById(R.id.iv_player_artwork_bitmap);
        mSingerNameTextView = (TextView) mRootView.findViewById(R.id.iv_play_singer_name);
        mSongLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_song);
        mSettingLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_setting);
        mAddLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_add_ll);
        mDownloadLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_download_ll);
        mSingerLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_singer_ll);
        mAlbumLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_album_ll);
        mSoundLinearLayout = (LinearLayout) mRootView.findViewById(R.id.portrait_sound_ll);
        mDownloadImageView = (ImageView) mRootView.findViewById(R.id.portrait_download_iv);
        mDownloadTextView = (TextView) mRootView.findViewById(R.id.portrait_download_tv);
        mSettingLayoutList.add(mAddLinearLayout);
        mSettingLayoutList.add(mDownloadLinearLayout);
        mSettingLayoutList.add(mSingerLinearLayout);
        mSettingLayoutList.add(mAlbumLinearLayout);
        mSettingLayoutList.add(mSoundLinearLayout);
        mArtworkImageView.setOnClickListener(this);
        mAddLinearLayout.setOnClickListener(this);
        mSettingLinearLayout.setOnClickListener(this);
        mAddLinearLayout.setOnClickListener(this);
        mDownloadLinearLayout.setOnClickListener(this);
        mSingerLinearLayout.setOnClickListener(this);
        mAlbumLinearLayout.setOnClickListener(this);
        mSoundLinearLayout.setOnClickListener(this);
        mPlayMusicSubscriber = new PlayMusicSubscriber();
        changeDownloadColor();
    }

    private void setApppearAnimation(int position) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation
                .RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        translateAnimation.setDuration(300);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setStartOffset(position * 100);
        mSettingLayoutList.get(position).setAnimation(animationSet);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle;
        switch (v.getId()) {
            case R.id.iv_player_artwork_bitmap:
                if (mSongLinearLayout.getVisibility() == View.VISIBLE) {
                    mSongLinearLayout.setVisibility(View.GONE);
                    mSettingLinearLayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i < 5; i++) {
                        setApppearAnimation(i);
                    }
                } else {
                    mSongLinearLayout.setVisibility(View.VISIBLE);
                    mSettingLinearLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.portrait_add_ll:
                final ArrayList<Object> ids = new ArrayList<Object>();
                ids.add(MusicPlayBackService.getMediaID());
                DialogMenuUtils.displayAddToPlaylistDialog(getActivity(), new SimplePlaylistChooserAdapter.OnPlaylistChoiceListener() {

                    @Override
                    public void onPlaylistChosen(final Uri playlistUri, final String playlistName) {
                        showProgressDialog();
                        int result = 0;
                        if (playlistUri.toString().equals(MusicMediaDatabaseHelper.Playlists.FAVORITE_URI.toString())) {
                            result = DBUtil.changeFavoriteWithIds(getActivity(), ids, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
                        } else {
                            long playlistId = Long.valueOf(playlistUri.getLastPathSegment());
                            result = DBUtil.addSongsToPlaylist(getActivity(), playlistId, ids);
                        }
                        if (result == 0) {
                            mHandler.sendEmptyMessage(MSG_ADD_FAILURE);
                        } else {
                            Message message = new Message();
                            HandleObject object = new HandleObject();
                            object.mUri = playlistUri;
                            object.mPlayListName = playlistName;
                            message.obj = object;
                            message.what = MSG_ADD_SUCCESS;
                            mHandler.sendMessage(message);
                        }
                    }
                }, R.string.add_to_playlist);
                break;
            case R.id.portrait_download_ll:
                onClickDownload();
                break;
            case R.id.portrait_singer_ll:
                bundle = new Bundle();
                bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST, (MusicPlayBackService.getArtistName()));
                bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST_ID, String.valueOf((MusicPlayBackService.getArtistId())));
                intent = new Intent(getActivity(), LocalAlbumListActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.portrait_album_ll:
                bundle = new Bundle();
                bundle.putString(CommonConstants.BUNDLE_KEY_ALBUM_NAME, MusicPlayBackService.getAlbumName());
                bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST, MusicPlayBackService.getArtistName());
                bundle.putString(CommonConstants.BUNDLE_KEY_ARTWORK, MusicPlayBackService.getArtWorkPath());
                bundle.putString(CommonConstants.BUNDLE_KEY_ALBUM_ID, String.valueOf(MusicPlayBackService.getAlbumId()));
                bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST_ID, String.valueOf(MusicPlayBackService.getArtistId()));
                intent = new Intent(getActivity(), LocalAlbumDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.portrait_sound_ll:
                break;
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        NotificationCenter.defaultCenter().subscriber(LiveMusicPlayTask.MSG_PLAY_MUSIC, mPlayMusicSubscriber);
//        if (null != MediaQueue.mCurrentMedia.getCurrentMedia()) {
//            mVisualizerView.link(APMixSession.getAPInstance().player().getCurrentMediaPlayer());
//        }
    }

    @Override
    public void onStop() {
        if (mPlayMusicSubscriber != null) {
            NotificationCenter.defaultCenter().unsubscribe(LiveMusicPlayTask.MSG_PLAY_MUSIC, mPlayMusicSubscriber);
        }
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshView();
    }

    @Override
    public void onDestroy() {
//        if (getActivity() != null)
//        {
//            ((RecentlyPlayedManager) SessionManager.getInstance(getActivity())).setOnRecordingSessionListener(null);
//        }
        mVisualizerView.releaseView();
        super.onDestroy();
    }


//    private void initCircleView(){
//
//    }


    private Bitmap toRoundBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    public void onCurrentMediaPlayStateChanged(boolean isPlaying) {
        NLog.d(TAG, "onCurrentMediaPlayStateChanged isPlaying = " + isPlaying);
        final Activity activity = getActivity();
        if (activity == null || isDetached()) {
            return;
        }
    }

    private void refreshView() {
        if (null != MusicPlayBackService.getCurrentMediaInfo()
                && !TextUtils.isEmpty(MusicPlayBackService.getTitle())) {
            NotificationCenter.defaultCenter().publish(PlayMusicButton.TAG, PlayMusicButton.CURRENT_MEDIA_CHANGED);
            mVisualizerView.link(MusicPlayBackService.getMediaplayer());
            mSongNameTextView.setText(MixUtil.getSongNameWithNoSuffix(MusicPlayBackService.getTitle()));
            mSingerNameTextView.setText(MusicPlayBackService.getArtistName());
            if (MusicPlayBackService.getArtWorkPath() != null
                    && !MusicPlayBackService.getArtWorkPath().startsWith("http")) {
                Bitmap bitmap = mImageFetcher.getArtWorkBitmap((MusicPlayBackService.getArtWorkPath()));
                mArtworkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mArtworkImageView.setImageBitmap(bitmap);
            } else {
                try {
                    if (MusicPlayBackService.getArtWorkPath() != null) {
                        mArtworkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        Glide.with(getActivity())
                                .load(ImageUtil.transferImgUrl(MusicPlayBackService.getArtWorkPath(), 300))
                                .placeholder(R.drawable.empty_playing_album)
                                .into(mArtworkImageView);
                        mArtworkImageView.invalidate();
                    } else {
                        mArtworkImageView.setScaleType(ImageView.ScaleType.CENTER);
                        mArtworkImageView.setImageResource(R.drawable.empty_playing_album);
                    }
                } catch (Exception e) {
                    NLog.e(TAG, "set bg Excetpion: " + e.getMessage());
                }
            }
        }
    }

    private void onClickDownload() {
        if (MusicPlayBackService.getCurrentMediaInfo() == null) {
            return;
        }
        //属于在线音乐
        if (MusicPlayBackService.getCurrentMediaInfo().sourceType == CommonConstants.SRC_TYPE_DEEZER || MusicPlayBackService.getCurrentMediaInfo().sourceType == CommonConstants.SRC_TYPE_DEEZERRADIO) {
            NLog.d(TAG, "online song");
        } else {
            //本地歌曲 不允许点击
            NLog.d(TAG, "local song, no need download again");
            //todo  提示已经下载
            ToastUtil.showToast(getActivity(),R.string.song_exist_in_local);
            return;
        }

        if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
            ToastUtil.showToast(getActivity(),R.string.invalid_network);
            return;
        }

        if (MusicPlayBackService.getCurrentMediaInfo() != null && !TextUtils.isEmpty(MusicPlayBackService.getSongRemoteID())) {
            IDownloader downloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
            downloader.startMusicDownload(MusicPlayBackService.getSongRemoteID());
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();
        ((PlayingActivity) getActivity()).setStatusBarState(1);
        refreshView();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            mProgressDialog.setProgressStyle(mProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(getResources().getString(R.string.batch_operate_loading));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
        } else {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
        mProgressDialog.show();
    }

    //接收点击播放事件
    class PlayMusicSubscriber implements TopicSubscriber {

        @Override
        public void onEvent(String s, Object o) {
            if (LiveMusicPlayTask.MSG_PLAY_MUSIC.equals(s)) {
                try {

                    NLog.i(TAG, "--msg--play--music--");
                    MediaInfo mediaInfo = (MediaInfo) o;

                    if (mediaInfo != null) {
                        //mJogWheel.showDeckCircleBg(mediaInfo.artworkPath);
                        mSongNameTextView.setText(MixUtil.getSongNameWithNoSuffix(MusicPlayBackService.getTitle()));
                    }
                } catch (Resources.NotFoundException e) {
                    NLog.e(TAG, e.toString());
                }
            }
        }
    }

    public void onCurrentMediaMetaChanged(String singerName, String songName) {
        if (null != singerName) {
            mSingerNameTextView.setText(singerName);
        }
        if (null != songName) {
            mSongNameTextView.setText(MixUtil.getSongNameWithNoSuffix(songName));
        }
        changeDownloadColor();
        //type为3或4 属于在线音乐 不需要显示虾米logo add by xiangxiang.liu 2015/11/27 10:41
        if (!isAdded()) {
            return;
        }
        final Activity activity = getActivity();
        if (activity == null || isDetached()) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshView();
            }
        });
    }

    private class HandleObject {
        public Uri mUri;
        public String mPlayListName;
    }

    private void changeDownloadColor() {
        if (null != MusicPlayBackService.getCurrentMediaInfo()) {
            if (MusicPlayBackService.getCurrentMediaInfo().sourceType == CommonConstants.SRC_TYPE_LOCAL ||
                    MusicPlayBackService.getCurrentMediaInfo().sourceType == CommonConstants.SRC_TYPE_DOWNLOADED ||
                    MusicPlayBackService.getCurrentMediaInfo().sourceType == CommonConstants.SRC_TYPE_MYMIX ||
                    MusicPlayBackService.getCurrentMediaInfo().sourceType == CommonConstants.SRC_TYPE_RDIO) {
                mDownloadImageView.setImageResource(R.drawable.detail_download_grap);
                mDownloadTextView.setAlpha(CommonConstants.VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA);
            } else {
                mDownloadImageView.setImageResource(R.drawable.portrait_download);
                mDownloadTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            }
        }
    }
}
