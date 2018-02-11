package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zheng.ding on 2016/04/27.
 */
public class LiveMusicSearchMatchSongBean extends BaseSong implements Serializable {
    public List<SongAllDetailBean> songs;

    @Override
    public String toString() {
        return "LiveMusicSearchSongBean{" +
                "songs=" + songs +
                '}';
    }
}
