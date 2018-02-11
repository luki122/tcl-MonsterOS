package cn.tcl.music.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.PlayingActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.QueueUtil;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicSongDetailTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.RemoteViewsUtils;
import cn.tcl.music.util.Util;
import cn.tcl.music.util.Utils;

public class MusicPlayBackService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MusicApplication.AppBackgroundListener {
    private static MediaPlayer mPlayer;
    private static final String TAG = MusicPlayBackService.class.getClass().getName();

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PLAY_PAUSE = "action_play_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_QUEUE_MODE = "action_queue_mode";

    public static final String ACTION_QUIT = "action_quit";
    public static final String ACTION_CLOSE_APPLICATION = "action_close_application";

    public static final int REQUESTCODE_PLAYPAUSE_BUTTON = 1;
    public static final int REQUESTCODE_NEXT_BUTTON = 2;
    public static final int REQUESTCODE_PREV_BUTTON = 3;
    public static final int REQUESTCODE_WIDGET_OFFSET = REQUESTCODE_PREV_BUTTON;
    public static final int REQUESTCODE_QUIT = 4;
    // mark the state of media_player
    private static int mPlayState;
    //Sometimes we load a song but not play now;
    private boolean mPlayNow;
    // mark the music info of now playing song;
    private static MediaInfo mCurrentMusic;
    // is preparing
    private boolean mIsPreparing;
    // time to update statusBar
    public static final int UPDATE_NOTIFICATION = 1;
    private MusicBinder mBinder;
    // loading percentage of online song
    private int mLoadingPercentage;
    public Notification mNotification = null;
    public static final int ONGOING_NOTIFICATION_ID = 1;
    protected boolean appIsInBackground = false;
    // TCT: Have no idea about when app go to background, and the music is paused, the notification bar can't be removed
    //      If there has another solution, replace it.
    private boolean foregroundService = false;
    protected Class<?> activityClass;
    private boolean isFirstStartService;
    private boolean mToCompletion = true;
    private String mOnlineMediaUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new MusicBinder(this);
        initPlayer();
        readData();
        ((MusicApplication) getApplication()).registerAppBackgroundListener(this);
        setServiceBackground(MusicApplication.isInBackground(), null);
        isFirstStartService = true;
    }

    /*
     when we do this action,that means maybe the next action will affect next song(such as delete song,ignore folders,remove song from list and so on) and
     we don't know how long it will take, so we set false and it wont play next automatically,if all actions done, if current music still playing,
     we set true and it does not make any effect, if current music has come to end,in onCompletion() method, we pause it ,
     and seek it to its duration - 100 position, so when we set true, it will still play, but soon it will play next;
      */
    public void setToCompletion(boolean toCompletion) {
        mToCompletion = toCompletion;
        if (toCompletion) {
            play();
        }
    }

    public static MediaPlayer getMediaplayer() {
        return mPlayer;
    }

    public static MediaInfo getCurrentMediaInfo() {
        return mCurrentMusic;
    }

    private void notifyChange(String what) {
        Intent intent = new Intent(what);
        sendBroadcast(intent);
        LogUtil.i(TAG, "notifyChange-what = " + what);
        // TODO send broadcast to notify
        if (what.equals(CommonConstants.PLAY_STATE_CHANGED)) {
            //TODO refresh remote client
        } else if (what.equals(CommonConstants.META_CHANGED)) {
            //TODO refresh remote client
            saveData();
        }
        //TODO widget refresh
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            isFirstStartService = false;
            return START_NOT_STICKY;
        }
        String command = intent.getStringExtra(CommonConstants.COMMAND_TO_SERVICE);
        if (TextUtils.isEmpty(command)) {

        } else if (CommonConstants.COMMAND_PLAY.equals(command)) {
            play();
        } else if (CommonConstants.COMMAND_PAUSE.equals(command)) {
            pause();
        } else if (CommonConstants.COMMAND_PREV.equals(command)) {
            prev();
        } else if (CommonConstants.COMMAND_NEXT.equals(command)) {
            next();
        } else if (CommonConstants.COMMAND_STOP.equals(command)) {
            stop();
        } else if (CommonConstants.COMMAND_TOGGLE_PLAY_OR_PAUSE.equals(command)) {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
        } else if (CommonConstants.COMMAND_PLAY_A_SONG.equals(command)) {
            long mediaID = intent.getLongExtra(CommonConstants.COMMAND_PLAY_A_SONG_MEDIA_ID, -1);
            playByMediaID(mediaID);
        } else if (CommonConstants.COMMAND_CLOSE_NOTIFICAITON.equals(command)) {
            stop();
            MusicApplication.getApp().forceStopAPK();
        }
        if (MusicApplication.isInBackground() && !isFirstStartService) {
            updateNotification();
        }
        isFirstStartService = false;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        saveData();
        exitPlay();
        super.onDestroy();
        ((MusicApplication) getApplication()).unRegisterAppBackgroundListener(this);
    }

    @Override
    public void appGoToBackground(Activity activity) {
        setServiceBackground(true, activity);
    }

    @Override
    public void appComeToForeground(Activity activity) {
        setServiceBackground(false, activity);
    }

    // set notification view
    private Notification buildNotification() {
        LogUtil.i(TAG, "buildNotification()");
        if (getApplicationContext() == null) {
            return null;
        }
//        if (MediaQueue.getInstance() != null && MediaQueue.getInstance().getCount() <= 0) {
//            return null;
//        }
        MediaInfo mediaInfo = (MusicPlayBackService.getCurrentMediaInfo() != null) ? MusicPlayBackService.getCurrentMediaInfo() : null;
        if (mNotification == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.icon_notification)
                    .setOngoing(mPlayState != CommonConstants.PLAY_STATE_STOP);

            if (Utils.hasLollipop()) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC)    // Show controls on lock screen even when user hides sensitive content
                        .setCategory(Notification.CATEGORY_TRANSPORT);   // media transport control for playback.

                if (mediaInfo != null && mediaInfo.artworkPath != null) {
                    builder.setLargeIcon(BitmapFactory.decodeFile(mediaInfo.artworkPath));
                    Bundle extras = new Bundle();
                    extras.putString(Notification.EXTRA_BACKGROUND_IMAGE_URI, mediaInfo.artworkPath);
                    builder.setExtras(extras);
                }
            }
//            builder.addAction(R.drawable.notification_previous_white, "Previous", createPendingAction(CommonConstants.COMMAND_PREV, this, REQUESTCODE_PREV_BUTTON, MusicPlayBackService.class));
//            if (isPlaying()) {
//                builder.addAction(R.drawable.notification_pause_white, "Pause", createPendingAction(CommonConstants.COMMAND_PAUSE, this, REQUESTCODE_PLAYPAUSE_BUTTON, MusicPlayBackService.class));
//            } else {
//                builder.addAction(R.drawable.notification_play_white, "Play", createPendingAction(CommonConstants.COMMAND_PLAY, this, REQUESTCODE_PLAYPAUSE_BUTTON, MusicPlayBackService.class));
//            }
//            builder.addAction(R.drawable.notification_next_white, "Next", createPendingAction(CommonConstants.COMMAND_NEXT, this, REQUESTCODE_NEXT_BUTTON, MusicPlayBackService.class));

            mNotification = builder.build();

            RemoteViews bigRemoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification_big);
            RemoteViewsUtils.updateMusic5RemoteViews(mNotification, bigRemoteViews, this, mediaInfo, 0, false);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
            RemoteViewsUtils.updateMusic5RemoteViews(mNotification, remoteViews, this, mediaInfo, 0, false);

            Intent intent = new Intent(getApplicationContext(), PlayingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent bringAppToFrontIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
            PendingIntent stopServiceIntent = MusicPlayBackService.createPendingAction(MusicPlayBackService.ACTION_QUIT,
                    this, MusicPlayBackService.REQUESTCODE_QUIT, MusicPlayBackService.class);

            mNotification.bigContentView = bigRemoteViews;
            mNotification.contentView = remoteViews;
            mNotification.contentIntent = bringAppToFrontIntent;
            mNotification.deleteIntent = stopServiceIntent;
        } else {
            mNotification.contentView = new RemoteViews(getPackageName(), R.layout.layout_notification);
            mNotification.bigContentView = new RemoteViews(getPackageName(), R.layout.layout_notification_big);
            RemoteViewsUtils.updateMusic5RemoteViews(mNotification, mNotification.bigContentView, this, mediaInfo, 0, false);
            RemoteViewsUtils.updateMusic5RemoteViews(mNotification, mNotification.contentView, this, mediaInfo, 0, false);

            if (Utils.hasLollipop() && mediaInfo != null && mediaInfo.artworkPath != null) {
                Bundle extras = new Bundle();
                extras.putString(Notification.EXTRA_BACKGROUND_IMAGE_URI, mediaInfo.artworkPath);
                mNotification.extras = extras;
            }
            mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
            mNotification.when = System.currentTimeMillis();  // update notification chronology
        }

        return mNotification;
    }


    static public PendingIntent createPendingAction(String action, Context context, int requestCode, Class<? extends MusicPlayBackService> serviceClass) {
        Intent intent = new Intent(context.getApplicationContext(), serviceClass);
        /*if (widgetComponentName != null)
            intent.putExtra("widgetComponentName", widgetComponentName);*/
        intent.putExtra(CommonConstants.COMMAND_TO_SERVICE, action);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    private void readData() {
        LogUtil.i(TAG, "readData --- start");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long media_id = sp.getLong(CommonConstants.PREFERENCE_SAVED_MEDIA_ID, -1);
        int current_position = sp.getInt(CommonConstants.PREFERENCE_SAVED_CURRENT_POSITION, -1);
        if (media_id != -1) {
            mCurrentMusic = DBUtil.getMediaInfoWithMediaId(this, media_id);
            if (mCurrentMusic != null) {
                playByMediaInfoIfNowPlay(mCurrentMusic, false);
                if (current_position != -1) {
                    try {
                        mPlayer.seekTo(current_position);
                    } catch (Exception e) {
                        LogUtil.e(TAG, "read data but seek to " + current_position + " failed");
                    }
                }
            } else {
                mCurrentMusic = null;
            }
        }
    }

    private void exitPlay() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            notifyChange(CommonConstants.PLAY_STATE_CHANGED);
            mPlayer = null;
            mCurrentMusic = null;
        }
    }

    private void saveData() {
        LogUtil.i(TAG, "save data --- start");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (mCurrentMusic != null) {
            editor.putLong(CommonConstants.PREFERENCE_SAVED_MEDIA_ID, mCurrentMusic.audioId);
        } else {
            editor.putLong(CommonConstants.PREFERENCE_SAVED_MEDIA_ID, -1);
        }
        if (mPlayer != null && mPlayState == CommonConstants.PLAY_STATE_PLAYING || mPlayState == CommonConstants.PLAY_STATE_PAUSE) {
            editor.putInt(CommonConstants.PREFERENCE_SAVED_CURRENT_POSITION, mPlayer.getCurrentPosition());
        } else {
            editor.putInt(CommonConstants.PREFERENCE_SAVED_CURRENT_POSITION, -1);
        }
        editor.commit();
    }

    private void initPlayer() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } catch (Exception e) {
                LogUtil.e(TAG, "initPlayer error ,maybe cannot be stop");
            }
            mPlayer = null;
        }
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayNow = true;
        mCurrentMusic = null;
        setPlayState(CommonConstants.PLAY_STATE_IDLE);
        mLoadingPercentage = 100;
        notifyChange(CommonConstants.PLAY_STATE_CHANGED);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(TAG, "onBind ----- start");
        if (mBinder == null) {
            mBinder = new MusicBinder(this);
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i(TAG, "onUnbind ---- end");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        LogUtil.i(TAG, "onRebind ---- begin");
        super.onRebind(intent);
    }

    public static boolean isPlaying() {
        return mPlayState == CommonConstants.PLAY_STATE_PLAYING || mPlayState == CommonConstants.PLAY_STATE_PREPARING;
    }

    public void stop() {
        LogUtil.i(TAG, "now will stop music");
        if (mPlayer != null) {
            if ((mPlayState == CommonConstants.PLAY_STATE_PLAYING || mPlayState == CommonConstants.PLAY_STATE_PAUSE)) {
                mPlayer.stop();
                LogUtil.i(TAG, "do stop music");
                setPlayState(CommonConstants.PLAY_STATE_STOP);
                notifyChange(CommonConstants.PLAY_STATE_CHANGED);
            }
        } else {
            initPlayer();
        }
    }

    private void setPlayState(int state) {
        mPlayState = state;
        LogUtil.i(TAG, "setPlayState " + state);
    }

    public void pause() {
        LogUtil.i(TAG, "now will pause music");
        if (mPlayer != null) {
            if (mPlayState == CommonConstants.PLAY_STATE_PLAYING) {
                mPlayer.pause();
                LogUtil.i(TAG, "do pause music");
                setPlayState(CommonConstants.PLAY_STATE_PAUSE);
                notifyChange(CommonConstants.PLAY_STATE_CHANGED);
                saveData();
            }
        } else {
            initPlayer();
        }
    }

    public void play() {
        LogUtil.i(TAG, "now will play music");
        if (mPlayer != null) {
            if (mPlayState == CommonConstants.PLAY_STATE_PAUSE) {
                LogUtil.i(TAG, "do play music from pause");
                mPlayer.start();
                setPlayState(CommonConstants.PLAY_STATE_PLAYING);
            } else if (mPlayState == CommonConstants.PLAY_STATE_STOP) {
                mPlayer.prepareAsync();
                mPlayer.seekTo(0);
                LogUtil.i(TAG, "do play music from stop and start from zero");
                mIsPreparing = true;
                setPlayState(CommonConstants.PLAY_STATE_PREPARING);
                notifyChange(CommonConstants.META_CHANGED);
            }
            notifyChange(CommonConstants.PLAY_STATE_CHANGED);
        } else {
            initPlayer();
        }
    }

    public void prev() {
        LogUtil.i(TAG, "prev start ---- ");
        if (mCurrentMusic == null) {
            LogUtil.i(TAG, "next but current music is null, need to play first song in queue table");
            MediaInfo next = QueueUtil.getPrePlayableMediaInfo(this, PlayMode.getMode(this), -1);
            playByMediaInfo(next);
        } else {
            MediaInfo next = QueueUtil.getPrePlayableMediaInfo(this, PlayMode.getMode(this), mCurrentMusic.audioId);
            playByMediaInfo(next);
        }
    }

    public boolean getIsPreparing() {
        return mIsPreparing;
    }

    public void next() {
        LogUtil.i(TAG, "next start ---");
        if (mCurrentMusic == null) {
            LogUtil.i(TAG, "next but current music is null, need to play first song in queue table");
            MediaInfo next = QueueUtil.getNextPlayableMediaInfo(this, PlayMode.getMode(this), -1);
            playByMediaInfo(next);
        } else {
            LogUtil.i(TAG, "next  start search for next playable song");
            MediaInfo next = QueueUtil.getNextPlayableMediaInfo(this, PlayMode.getMode(this), mCurrentMusic.audioId);
            LogUtil.i(TAG, "next find next song info and info is " + (next == null ? "null" : next.title));
            playByMediaInfo(next);
        }
        if (MusicApplication.isInBackground()) {
            updateNotification();
        }
    }

    public int duration() {
        if (mPlayer != null) {
            if (mPlayState == CommonConstants.PLAY_STATE_PLAYING || mPlayState == CommonConstants.PLAY_STATE_PAUSE) {
                return mPlayer.getDuration();
            }
        }
        return 0;
    }

    public int position() {
        if (mPlayer != null) {
            if (mPlayState == CommonConstants.PLAY_STATE_PLAYING || mPlayState == CommonConstants.PLAY_STATE_PAUSE) {
                return mPlayer.getCurrentPosition();
            }
        }
        return 0;
    }

    public void seek(int pos) {
        if (mPlayer != null && (mPlayState != CommonConstants.PLAY_STATE_IDLE && mPlayState != CommonConstants.PLAY_STATE_RESET && mPlayState != CommonConstants.PLAY_STATE_STOP)) {
            mPlayer.seekTo(pos);
            LogUtil.i(TAG, "seek to position : " + pos);
        } else {
            initPlayer();
        }
    }

    public static String getTitle() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.title;
        }
        return null;
    }

    public static String getAlbumName() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.album;
        }
        return null;
    }

    public static long getAlbumId() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.albumId;
        }
        return 0;
    }

    public static String getArtistName() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.artist;
        }
        return null;
    }

    public static String getArtWorkPath() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.artworkPath;
        }
        return null;
    }

    public static String getSongRemoteID() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.songRemoteId;
        }
        return null;
    }

    public static long getArtistId() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.artistId;
        }
        return 0;
    }

    public static long getMediaID() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.audioId;
        }
        return 0;
    }

    public void playByMediaInfo(MediaInfo info) {
        LogUtil.i(TAG, "play by media info and info is " + ((info == null) ? "null" : ("not null , id = " + info.audioId + " , title = " + info.title)));
        playByMediaInfoIfNowPlay(info, true);
    }

    private boolean checkInfoIsPlayable(MediaInfo info) {
        if (info == null) {
            LogUtil.e(TAG, "checkInfoIsPlayable but info is null");
            return false;
        }
        if (info.sourceType == CommonConstants.SRC_TYPE_LOCAL || info.sourceType == CommonConstants.SRC_TYPE_DOWNLOADED) {
            if (TextUtils.isEmpty(info.filePath)) {
                LogUtil.e(TAG, "checkInfoIsPlayable but path is null");
                return false;
            }
            File file = new File(info.filePath);
            return file.exists();
        }
        return true;
    }

    public void playByMediaID(long media_id) {
        LogUtil.i(TAG, "play by media id and id = " + media_id);
        playByMediaIDIfNowPlay(media_id, true);
    }

    public void playByMediaInfoIfNowPlay(MediaInfo info, boolean playNow) {
        LogUtil.i(TAG, "play by media info if now play start --- and play now is " + ((playNow) ? "true" : "false"));
        if (info == null) {
            LogUtil.e(TAG, "play by media info if now but info is null and we won't play anything");
            if (mPlayer == null) {
                initPlayer();
            } else {
                mPlayer.reset();
                setPlayState(CommonConstants.PLAY_STATE_RESET);
            }
            mCurrentMusic = null;
            return;
        }
        if (checkInfoIsPlayable(info)) {
            if (mPlayer == null) {
                initPlayer();
            }
            try {
                mPlayer.reset();
                setPlayState(CommonConstants.PLAY_STATE_RESET);
                // the path is absolute path or http
                LogUtil.i(TAG, "play by media info if now play set path " + info.filePath);
//                mPlayer.setDataSource(info.filePath);
//                mPlayer.prepareAsync();
//                mIsPreparing = true;
//                setPlayState(CommonConstants.PLAY_STATE_PREPARING);
//                mCurrentMusic = info;
//                mPlayNow = playNow;
//                DBUtil.addToRecentlyPlay(this, mCurrentMusic.audioId);
//                notifyChange(CommonConstants.META_CHANGED);
//                notifyChange(CommonConstants.PLAY_STATE_CHANGED);
                if (info.filePath == null) { //maybe it is a online media
                    getOnlineUrl(info);
                    info.filePath = mOnlineMediaUrl;
                } else {
                    mPlayer.setDataSource(info.filePath);
                    mPlayer.prepareAsync();
                    mIsPreparing = true;
                    setPlayState(CommonConstants.PLAY_STATE_PREPARING);
                    mCurrentMusic = info;
                    mPlayNow = playNow;
                    DBUtil.addToRecentlyPlay(this,mCurrentMusic.audioId);
                    notifyChange(CommonConstants.META_CHANGED);
                    notifyChange(CommonConstants.PLAY_STATE_CHANGED);
                }
            } catch (IOException e) {
                initPlayer();
                LogUtil.e(TAG, "play song io exception, cannot play");
            }
        } else {
            LogUtil.i(TAG, "check info but file not exist and now play next");
            mCurrentMusic = info;
            next();
        }
    }

    private void getOnlineUrl(final MediaInfo info) {
        new LiveMusicSongDetailTask(getApplication(), new ILoadData() {
            @Override
            public void onLoadSuccess(int dataType, List datas) {
                if (datas == null || datas.size() == 0) {
                    return;
                }

                List<SongDetailBean> list = datas;
                SongDetailBean song = list.get(0);

                if (song == null) {
                    return;
                }

                mOnlineMediaUrl = Util.getMediaUrl(song.listen_file);
                playOnlineMedia(info);
            }

            @Override
            public void onLoadFail(int dataType, String message) {
                mOnlineMediaUrl = "";
            }
        }, info.songRemoteId).executeMultiTask();
    }

    private void playOnlineMedia(MediaInfo info) {
        try {
            mPlayer.setDataSource(mOnlineMediaUrl);
            mPlayer.prepareAsync();
            mIsPreparing = true;
            setPlayState(CommonConstants.PLAY_STATE_PREPARING);
            mCurrentMusic = info;
            mPlayNow = true;
            DBUtil.addToRecentlyPlay(this, mCurrentMusic.audioId);
            notifyChange(CommonConstants.META_CHANGED);
            notifyChange(CommonConstants.PLAY_STATE_CHANGED);
        } catch (IOException e) {

        }
    }

    public void playByMediaIDIfNowPlay(long media_id, boolean playNow) {
        MediaInfo info = DBUtil.getMediaInfoWithMediaId(this, media_id);
        playByMediaInfoIfNowPlay(info, playNow);
    }

    public long getCurrentMediaID() {
        if (mCurrentMusic != null) {
            return mCurrentMusic.audioId;
        }
        return -1;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            LogUtil.i(TAG, "onCompletion and we will play next");
            if (PlayMode.getMode(this) == PlayMode.PLAY_MODE_REPEAT) {
                if (checkInfoIsPlayable(mCurrentMusic)) {
                    playByMediaInfo(mCurrentMusic);
                } else {
                    if (mToCompletion) {
                        next();
                    } else {
                        pause();
                        seek(duration() - 100);
                    }
                }
            } else {
                next();
            }
        } else {
            initPlayer();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            LogUtil.i(TAG, "onPrepared and the song is prepared");
            mIsPreparing = false;
            if (mPlayNow) {
                mediaPlayer.start();
                setPlayState(CommonConstants.PLAY_STATE_PLAYING);
                LogUtil.i(TAG, "onPrepared and play song right now");
            } else {
                mediaPlayer.start();
                mediaPlayer.pause();
                setPlayState(CommonConstants.PLAY_STATE_PAUSE);
                LogUtil.i(TAG, "onPrepared but not start right now");
            }
        } else {
            initPlayer();
        }
        notifyChange(CommonConstants.PLAY_STATE_CHANGED);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        LogUtil.e(TAG, "play error , error code is " + what + " , extra is " + extra);
        if (mediaPlayer == mPlayer) {
            initPlayer();
            return true;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        if (mediaPlayer == mPlayer) {
            mLoadingPercentage = i;
        }
    }

    public int currentLoadingPerCent() {
        if (mCurrentMusic != null) {
            if (mCurrentMusic.sourceType != CommonConstants.SRC_TYPE_LOCAL && mCurrentMusic.sourceType != CommonConstants.SRC_TYPE_DOWNLOADED  ) {
                return mLoadingPercentage;
            } else {
                return 100;
            }
        } else {
            return 0;
        }
    }

    public static class MusicBinder extends IMusicService.Stub {
        private MusicPlayBackService mService;

        public MusicBinder(MusicPlayBackService service) {
            mService = service;
        }

        @Override
        public boolean isPlaying() {
            return mService.isPlaying();
        }

        @Override
        public void stop() {
            mService.stop();
        }

        @Override
        public void pause() {
            mService.pause();
        }

        @Override
        public void play() {
            mService.play();
        }

        @Override
        public void prev() {
            mService.prev();
        }

        @Override
        public void next() {
            mService.next();
        }

        @Override
        public int duration() {
            return mService.duration();
        }

        @Override
        public int position() {
            return mService.position();
        }

        @Override
        public void seek(int pos) {
            mService.seek(pos);
        }

        @Override
        public String getAlbumName() {
            return mService.getAlbumName();
        }

        @Override
        public long getAlbumId() {
            return mService.getAlbumId();
        }

        @Override
        public String getArtistName() {
            return mService.getArtistName();
        }

        @Override
        public long getArtistId() {
            return mService.getArtistId();
        }

        @Override
        public void playByMediaInfo(MediaInfo info) throws RemoteException {
            mService.playByMediaInfo(info);
        }

        @Override
        public void playByMediaID(long media_id) throws RemoteException {
            mService.playByMediaID(media_id);
        }

        @Override
        public void playByMediaInfoIfNowPlay(MediaInfo info, boolean playNow) throws RemoteException {
            mService.playByMediaInfoIfNowPlay(info, playNow);
        }

        @Override
        public void playByMediaIDIfNowPlay(long media_id, boolean playNow) throws RemoteException {
            mService.playByMediaIDIfNowPlay(media_id, playNow);
        }

        @Override
        public long getCurrentMediaID() {
            return mService.getCurrentMediaID();
        }

        @Override
        public boolean getIsPreparing() {
            return mService.getIsPreparing();
        }

        @Override
        public int currentLoadingPerCent() throws RemoteException {
            return mService.currentLoadingPerCent();
        }

        @Override
        public String getTitle() throws RemoteException {
            return mService.getTitle();
        }

        @Override
        public String getArtWorkPath() throws RemoteException {
            return mService.getArtWorkPath();
        }

        @Override
        public String getSongRemoteID() throws RemoteException {
            return mService.getSongRemoteID();
        }

        @Override
        public long getMediaID() throws RemoteException {
            return mService.getMediaID();
        }

        @Override
        public void setToCompletion(boolean toCompletion) {
            mService.setToCompletion(toCompletion);
        }
    }

    protected void setServiceBackground(boolean isNowInBAckground, Activity activity) {
        if (mPlayer == null)
            return;

        appIsInBackground = isNowInBAckground;

        if (isNowInBAckground) {
            //[BUGFIX]-MOD-BEGIN by yuanxi.jiang for PR2013242 on 2016/5/24
            if (isPlaying()) {
                updateNotification();
            }
            //[BUGFIX]-MOD-END by yuanxi.jiang
        } else {
            if (activity != null)
                activityClass = activity.getClass();
            else
                activityClass = null;
            cancelNotification();
        }
    }

    private void cancelNotification() {
        if (foregroundService) {
            stopForeground(true);
            foregroundService = false;
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(ONGOING_NOTIFICATION_ID);
        }
    }

    private void updateNotification() {
        if (mPlayer == null)
            return;
        if (!appIsInBackground)
            return;

        Notification n = buildNotification();

        if (n != null) {
            if ((n.flags & Notification.FLAG_ONGOING_EVENT) == 0) {
                if (foregroundService) {
                    //[BUGFIX]-Add by TCTNJ,xing.yuan, 2015-09-10,PR1079697 begin
                    startForeground(ONGOING_NOTIFICATION_ID, n);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(ONGOING_NOTIFICATION_ID, n);
                    //[BUGFIX]-Add by TCTNJ,xing.yuan, 2015-09-10,PR1079697 end
                    stopForeground(false);
                    foregroundService = false;
                } else {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(ONGOING_NOTIFICATION_ID, n);
                }
            } else {
                startForeground(ONGOING_NOTIFICATION_ID, n);
                foregroundService = true;
            }
        }
    }
}