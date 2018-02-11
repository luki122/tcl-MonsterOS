// IMusicService.aidl
package cn.tcl.music.service;

// Declare any non-default types here with import statements
import cn.tcl.music.model.MediaInfo;
interface IMusicService {
        boolean isPlaying();
        void stop();
        void pause();
        void play();
        void prev();
        void next();
        int duration();
        int position();
        void seek(int pos);
        String getAlbumName();
        long getAlbumId();
        String getArtistName();
        long getArtistId();
        void playByMediaInfo(in MediaInfo info);
        void playByMediaID(long media_id);
        void playByMediaInfoIfNowPlay(in MediaInfo info, boolean playNow);
        void playByMediaIDIfNowPlay(long media_id,boolean playNow);
        long getCurrentMediaID();
        boolean getIsPreparing();
        int currentLoadingPerCent();
        String getTitle();
        String getArtWorkPath();
        String getSongRemoteID();
        long getMediaID();
        void setToCompletion(boolean toCompletion);
}
