package cn.tcl.music.model.live;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/10 19:20
 * @copyright TCL-MIE
 */
public class ArtistHotMusicDataBean extends BaseSong implements Serializable {

    public int total;//总数
    public boolean more;//是否有下一页
    public List<SongDetailBean> songs;


    public String toString(){
        return new Gson().toJson(this);
    }
}
