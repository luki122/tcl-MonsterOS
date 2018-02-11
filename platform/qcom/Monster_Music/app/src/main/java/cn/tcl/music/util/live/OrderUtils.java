package cn.tcl.music.util.live;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.tcl.music.model.live.CollectionBean;
import cn.tcl.music.model.live.RadioBean;

public class OrderUtils {

    private static Comparator radioByPalycount = new Comparator<RadioBean>() {
        @Override
        public int compare(RadioBean o1, RadioBean o2) {
            return Integer.parseInt(o2.play_count) - Integer.parseInt(o1.play_count);
        }
    };

    private static Comparator collectByPlaycount = new Comparator<CollectionBean>() {
        @Override
        public int compare(CollectionBean o1, CollectionBean o2) {
            return Integer.parseInt(o2.play_count) - Integer.parseInt(o1.play_count);
        }
    };

    public static void orderRadioByPalycount (List<RadioBean> list) {
        Collections.sort(list,radioByPalycount);
    }

    public static void orderCollectByPalycount (List<CollectionBean> list) {
        Collections.sort(list, collectByPlaycount);
    }
}
