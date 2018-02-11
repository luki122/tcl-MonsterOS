package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/25.
 * 电台详情bean
 */
public class RadioDetailBean extends BaseSong implements Serializable{
    public String radio_id = "";
    public String radio_name = "";
    public String logo = "";
    public List<SongDetailBean> songs;

    @Override
    public String toString() {
        return "["+radio_id+","+radio_name+","+logo+","+songs+"]";
    }
}
