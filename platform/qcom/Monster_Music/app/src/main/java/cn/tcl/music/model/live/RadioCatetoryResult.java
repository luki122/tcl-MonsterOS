package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiangxiangliu on 2015/11/11.
 */
public class RadioCatetoryResult extends BaseSong implements Serializable {
    public List<RadioCategoryBean> categories;

    @Override
    public String toString() {
        return "RadioCatetoryResult{" +
                "categories=" + categories +
                "} " + super.toString();
    }
}
