package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

public class LiveMusicSceneListBean extends BaseSong implements Serializable {
    public int radio_id;
    public int radio_type;
    public String title = "";
    public String logo = "";
    public String tag = "";

    @Override
    public String toString() {
        return "["+radio_id+","+radio_type+","+title+","+logo+","+tag+"]";
    }
}