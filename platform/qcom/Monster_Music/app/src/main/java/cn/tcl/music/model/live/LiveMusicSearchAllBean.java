package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiangxiangliu on 2015/11/13.
 */
public class LiveMusicSearchAllBean extends BaseSong implements Serializable {
    public String song_count;
    public String album_count;
    public String artist_count;
    public String collect_count;

    public List<SongDetailBean> songs;   //歌曲
    public List<AlbumBean> albums;  //专辑
    public List<ArtistBean> artists; //歌手

    @Override
    public String toString() {
        return "LiveMusicSearchAllBean{" +
                "song_count='" + song_count + '\'' +
                ", album_count='" + album_count + '\'' +
                ", artist_count='" + artist_count + '\'' +
                ", collect_count='" + collect_count + '\'' +
                ", songs=" + songs +
                ", albums=" + albums +
                ", artists=" + artists +
                "} " + super.toString();
    }
}
