package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiangxiangliu on 2015/11/13.
 */
public class LiveMusicSearchSongBean extends BaseSong implements Serializable {
    public List<SongDetailBean> songs;
    public int total;
    public boolean more;

    @Override
    public String toString() {
        return "LiveMusicSearchSongBean{" +
                "songs=" + songs +
                ", total=" + total +
                ", more=" + more +
                '}';
    }
}
