package cn.tcl.music.model.live;

import java.io.Serializable;

public class LiveMusicAutoTipsBean extends BaseSong implements Serializable {
    public int object_type;    //对象状态
    public String type = "";   //类型：艺人, 歌曲, 专辑等
    public String tip = "";    //提示
    public String url = "";    //scheme url

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[" + object_type + "]  [" + type + "]  [" + tip + "]  [" + url + "]";
    }

}
