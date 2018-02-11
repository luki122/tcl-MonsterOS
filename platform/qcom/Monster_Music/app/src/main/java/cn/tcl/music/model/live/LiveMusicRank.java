package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 */
public class LiveMusicRank extends BaseSong implements Serializable {
    public String category = "";
    public List<LiveMusicRankItem> items;
}
