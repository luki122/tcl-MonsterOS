package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 */
public class LiveMusicRecommend extends BaseSong implements Serializable {
    public String logo = "";
    public int total;
    public List<SongDetailBean> songs;
}
