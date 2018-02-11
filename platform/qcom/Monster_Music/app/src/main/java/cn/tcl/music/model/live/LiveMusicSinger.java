package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 在线音乐-歌手bean类
 */
public class LiveMusicSinger extends BaseSong implements Serializable {
    public List<ArtistBean> artists;
    public int total;
    public boolean more;

    @Override
    public String toString() {
        return "LiveMusicSinger{" +
                "artists=" + artists +
                ", total=" + total +
                ", more='" + more + '\'' +
                "} " + super.toString();
    }
}
