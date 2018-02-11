package cn.tcl.music.model.live;

import java.io.Serializable;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 电台bean类
 */
public class RadioBean extends BaseSong implements Serializable {
    public String play_count = "";//	int	是		电台播放数
    public String radio_id = "";//	int	是	99	电台id
    public String radio_logo = "";//	string	是	http://img.xiami.net/res/img/default/fm/old_50.jpg	电台logo
    public String radio_name = "";//	string	是	50S	电台名
    public String desc = "";//	string	是		描述信息
    public String category_type = "";//	string	是	none	电台类型，原创为original，其他为none

    @Override
    public String toString() {
        return "RadioBean{" +
                "play_count='" + play_count + '\'' +
                ", radio_id='" + radio_id + '\'' +
                ", radio_logo='" + radio_logo + '\'' +
                ", radio_name='" + radio_name + '\'' +
                ", desc='" + desc + '\'' +
                ", category_type='" + category_type + '\'' +
                "} " + super.toString();
    }
}
