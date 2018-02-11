package cn.tcl.music.model.live;

import java.io.Serializable;

/**
 * Created by xiangxiangliu on 2015/11/11.
 */
public class RadioCategoryBean extends BaseSong implements Serializable {
    public String category_id;  //分类id
    public String radio_logo;   //电台logo
    public String category_name;  //电台分类
    public String category_type;  //电台类型，原创为original，其他为none

    @Override
    public String toString() {
        return "RadioCategoryBean{" +
                "category_id='" + category_id + '\'' +
                ", radio_logo='" + radio_logo + '\'' +
                ", category_name='" + category_name + '\'' +
                ", category_type='" + category_type + '\'' +
                '}';
    }
}
