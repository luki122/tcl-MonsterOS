package cn.download.mie.base.update;


public class DownloadEvent {
	public static final int DE_START = 0;
	public static final int DE_PROGRESS = 1;
	public static final int DE_FAIL = 2;
	public static final int DE_COMPLETE = 3;
	
	public static final int ERROR_FILE_IO = 100001;
	
	public int event;
	public URLParams params;
	public int extra;
}
