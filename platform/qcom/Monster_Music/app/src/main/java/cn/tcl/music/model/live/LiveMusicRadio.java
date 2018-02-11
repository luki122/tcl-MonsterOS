package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 在线音乐-电台
 */
public class LiveMusicRadio extends BaseSong implements Serializable {
    public int total;
    public boolean more ;
    public List<RadioBean> radios;

    @Override
    public String toString() {
        return "LiveMusicRadio{" +
                "total=" + total +
                ", more='" + more + '\'' +
                ", radios=" + radios +
                "} " + super.toString();
    }
}
