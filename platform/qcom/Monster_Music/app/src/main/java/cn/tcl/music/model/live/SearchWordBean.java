package cn.tcl.music.model.live;

import java.io.Serializable;

/**
 * Created by xiangxiangliu on 2015/11/12.
 */
public class SearchWordBean  implements Serializable {
    public String word;
    public String change;

    @Override
    public String toString() {
        return "SearchWordBean{" +
                "word='" + word + '\'' +
                ", change='" + change + '\'' +
                '}';
    }
}
