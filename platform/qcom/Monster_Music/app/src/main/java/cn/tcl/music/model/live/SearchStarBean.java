package cn.tcl.music.model.live;

import java.io.Serializable;

/**
 * Created by xiangxiangliu on 2015/11/12.
 */
public class SearchStarBean implements Serializable {
    public String word;
    public String url;

    @Override
    public String toString() {
        return "SearchStarBean{" +
                "word='" + word + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
