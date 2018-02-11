package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

public class LiveMusicCollectListBean extends BaseSong implements Serializable {
    public int total;
    public String more = "";
    public List<CollectionBean> list;
}
