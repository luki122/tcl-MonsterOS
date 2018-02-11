package cn.tcl.music.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.framework.log.NLog;
import com.tcl.framework.notification.NotificationCenter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.PlayingActivity;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.FileManager;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MixUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.view.PlayMusicButton;
import cn.tcl.music.view.lyric.DefaultLrcBuilder;
import cn.tcl.music.view.lyric.ILrcBuilder;
import cn.tcl.music.view.lyric.LrcRow;
import cn.tcl.music.view.lyric.LrcView;

public class PlayLyricFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = PlayLyricFragment.class.getSimpleName();
    private LrcView lyricView;
    private TextView mSongTextView;
    private TextView mSingerTextView;
    private MediaInfo mOldMedia;        //记录上一首歌曲
    private RelativeLayout mRelativeLayout;
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_play_lyric, container, false);
        initView();
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshView();
    }

    private void initView() {
        mRelativeLayout = (RelativeLayout) mRootView.findViewById(R.id.lyric_ll);
        int statusBarHeight = SystemUtility.getStatusBarHeight();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, statusBarHeight, 0, 0);
        mRelativeLayout.setLayoutParams(layoutParams);
        lyricView = (LrcView) mRootView.findViewById(R.id.mylrc);
        mSongTextView = (TextView) mRootView.findViewById(R.id.playing_song_name_lyrics);
        mSingerTextView = (TextView) mRootView.findViewById(R.id.playing_singer_name_lyrics);
    }


    private HashMap<String, List<LrcRow>> map = new HashMap<>();

    public void setCurrentMediaInfo(MediaInfo media) {
        boolean isnew = true;
        if(null != mOldMedia && MusicPlayBackService.getCurrentMediaInfo() == mOldMedia){
            isnew = false;
        }
        this.mOldMedia = media;
        LogUtil.d(TAG, "setCurrentMediaInfo mCurrentMediaInfo = " + media);
        String lrc = FileManager.readLrcFileBySongTitleAndArtist(MusicPlayBackService.getTitle(), MusicPlayBackService.getArtistName());
        NLog.d(TAG, "setCurrentMediaInfo lrc file name = " + lrc);
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);
        if (!TextUtils.isEmpty(lrc) && rows != null && rows.size() > 0
                && MusicPlayBackService.getCurrentMediaInfo() !=null && !TextUtils.isEmpty(MusicPlayBackService.getSongRemoteID())){
            map.put(MusicPlayBackService.getSongRemoteID(),rows);
        }

        if (lyricView != null){
            if(isnew){
                lyricView.setLrc(rows,0);
            }else{
                lyricView.setLrc(rows);
            }
        }
        mHandler.removeCallbacks(updateRunable);
        mHandler.post(updateRunable);
    }


    int count = 0;

//    public void onCurrentMediaIsPreparing(MediaInfo media, int deckIndex) {
//        Activity activity = getActivity();
//        if (isDetached() || !isAdded() || activity == null) {
//            return;
//        }
//
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (!PlayLyricFragment.this.isAdded()) {
//                    return;
//                }
//
//            }
//        });
//    }

    public void onCurrentMediaChanged() {
        if (!isAdded()) {
            return;
        }
        final Activity activity = getActivity();
        if (activity == null || isDetached()) {
            return;
        }
        onCurrentMediaPositionChanged(0);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshView();
            }
        });
    }

    private void refreshView() {
        if (null != MusicPlayBackService.getCurrentMediaInfo() && !TextUtils.isEmpty(MusicPlayBackService.getTitle())) {
            NotificationCenter.defaultCenter().publish(PlayMusicButton.TAG, PlayMusicButton.CURRENT_MEDIA_CHANGED);
            mSongTextView.setText(MixUtil.getSongNameWithNoSuffix(MusicPlayBackService.getTitle()));
            mSingerTextView.setText(MusicPlayBackService.getArtistName());
            setCurrentMediaInfo(MusicPlayBackService.getCurrentMediaInfo());
        }
    }

//    public void onCurrentMediaPlayStateChanged(boolean isPlaying) {
//
//    }

    public void onCurrentMediaPositionChanged(int position) {
        count++;
        currentPlayTime = position;
        if (false && count >= 100) {
            count = 0;
            LogUtil.d(TAG, "onCurrentMediaPlayStateChanged before position = " + position + ", currentPlayTime = " + currentPlayTime);
        }

    }


    private int currentPlayTime = 0;
    public static final int DELAY_TIME = 1000;
    Handler mHandler = new Handler();
    Runnable updateRunable = new Runnable() {
        public void run() {
            lyricView.seekLrcToTime(currentPlayTime);

            mHandler.removeCallbacks(updateRunable);
            mHandler.postDelayed(updateRunable, DELAY_TIME);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        NLog.d(TAG, "onDestroy unRegisterListener");
    }

    @Override
    public void onResume() {
        super.onResume();
        mSongTextView.setText(MixUtil.getSongNameWithNoSuffix(MusicPlayBackService.getTitle()));
        mSingerTextView.setText(MusicPlayBackService.getArtistName());
        LogUtil.e(TAG, "onResume postDelayed updateRunable");
        mHandler.removeCallbacks(updateRunable);
        mHandler.postDelayed(updateRunable, 1000);
        ((PlayingActivity) getActivity()).setStatusBarState(2);
    }

    @Override
    public void onStop() {
        super.onStop();
        NLog.e(TAG, "onStop removeCallbacks ");
        mHandler.removeCallbacks(updateRunable);
    }


//    public void automixStateChanged(boolean automixStarted) {
//
//    }

    @Override
    public void onClick(View view) {

    }
}
