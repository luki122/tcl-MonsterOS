package cn.tcl.music.model.live;

import java.util.List;

public class BaseSong {
    public int returnCode;      //返回状态码
    public String returnMessage;//返回状态描述
    public List returnArray;//返回为jsonArray时，保存list列表

    @Override
    public String toString() {
        return "BaseSong{" +
                "returnCode=" + returnCode +
                ", returnMessage='" + returnMessage + '\'' +
                ", returnArray=" + returnArray +
                '}';
    }
}
