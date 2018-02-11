package cn.tcl.music.network;

import java.util.List;

/**
 * 2015-11-03
 */
public interface IDownloadData {
    void onLoadSuccess(int dataType, List datas, boolean isBatchDownload, boolean isDownloadMusic);
    void onLoadFail(int dataType, String message);

}
