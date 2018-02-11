package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiangyuanxi on 3/4/16.
 */
public class LiveMusicDailyRecommend extends BaseSong implements Serializable {
    public String logo = "";
    public String title = "";
    public int day;
    public int total;
    public List<SongDetailBean> songs;
}
