package com.monster.market.download;

public class DownloadManagerBean {

	public static final int TYPE_DOWNLOADING = 0;
	public static final int TYPE_DOWNLOADED = 1;
	public static final int TYPE_DOWNLOADED_SHOW = 2;

	private int type;

	private AppDownloadData downloadData;
	private String filePath;
	private long fileSize;
	private long downloadSize;
	private int progress;
	private int downloadStatus;
	private String progressInfo = "";
	private long createTime;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public AppDownloadData getDownloadData() {
		return downloadData;
	}

	public void setDownloadData(AppDownloadData downloadData) {
		this.downloadData = downloadData;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public void setDownloadSize(long downloadSize) {
		this.downloadSize = downloadSize;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(int downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public String getProgressInfo() {
		return progressInfo;
	}

	public void setProgressInfo(String progressInfo) {
		this.progressInfo = progressInfo;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}
