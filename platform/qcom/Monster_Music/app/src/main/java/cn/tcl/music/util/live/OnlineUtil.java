package cn.tcl.music.util.live;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.model.live.SongDetailBean;

public class OnlineUtil {

    public static List<SongDetailBean> mDataList = new ArrayList<SongDetailBean>();
    public static void setSongDetailData(List<SongDetailBean> data) {
        mDataList = data;
    }
    public static List<SongDetailBean> getSongDetailData() {
        return mDataList;
    }
    
}
