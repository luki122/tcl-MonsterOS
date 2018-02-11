package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/6.
 * 精选音乐合集类
 */
public class LiveMusicEssence extends BaseSong implements Serializable {
    public int total;
    public String more = "";
    public List<CollectionBean> collects;
}
