package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiangxiangliu on 2015/11/13.
 */
public class LiveMusicSearchHotBean extends BaseSong implements Serializable {
    public List<SearchWordBean> search_words;
    public List<SearchStarBean> star_words;

    @Override
    public String toString() {
        return "LiveMusicSearchHotBean{" +
                "search_words=" + search_words +
                ", star_words=" + star_words +
                "} " + super.toString();
    }
}
