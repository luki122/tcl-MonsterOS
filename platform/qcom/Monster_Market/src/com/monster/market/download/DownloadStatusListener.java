package com.monster.market.download;

/**
 * 下载状态监听 
 */
public interface DownloadStatusListener {
	
	public void onDownload(String taskId, int status, long downloadSize, long fileSize);
	
}
