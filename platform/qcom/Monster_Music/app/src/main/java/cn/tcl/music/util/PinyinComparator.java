package cn.tcl.music.util;

import java.util.Comparator;

import cn.tcl.music.model.live.ArtistBean;

public class PinyinComparator implements Comparator<ArtistBean> {

    public int compare(ArtistBean o1, ArtistBean o2) {
        if (o1.sort_title.equals("@")
                || o2.sort_title.equals("#")) {
            return -1;
        } else if (o1.sort_title.equals("#")
                || o2.sort_title.equals("@")) {
            return 1;
        } else {
            return o1.sort_title.compareTo(o2.sort_title);
        }
    }

}
