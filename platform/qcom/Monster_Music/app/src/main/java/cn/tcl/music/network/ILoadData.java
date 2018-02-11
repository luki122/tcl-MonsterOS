package cn.tcl.music.network;

import java.util.List;

/**
 * 2015-11-03
 */
public interface ILoadData {
    void onLoadSuccess(int dataType, List datas);
    void onLoadFail(int dataType, String message);

}
