package cn.download.mie.downloader;

/**
 * Created by Rex on 2015/6/3.
 */
public class DownloadException extends Exception{

    public static final int ECODE_PATH_NOT_EXIST = -100;
    public static final int ECODE_URL_CHECK_FAILED = -101;
    public static final int ECODE_NETWORK = -102;
    public static final int ECODE_UNKNOWN = -103;
    public static final int ECODE_SERVER = -104;
    public static final int ECODE_CONTENT_TYPE_NOT_ACCEPTABLE = -105;
    public static final int ECODE_LARGER_THAN_TARGET = -106;
    public static final int ECODE_RENAME_FAILED = -107;
    public static final int ECODE_PAUSE = -108;
    /**
     * 大于0是HTTP，小于0是自定义错误
     */
    public int mErrorCode;

    public DownloadException(int mErrorCode) {
        this.mErrorCode = mErrorCode;
    }

    public DownloadException(Throwable throwable, int mErrorCode) {
        super(throwable);
        this.mErrorCode = mErrorCode;
    }

    @Override
    public String toString() {
        return super.toString() + "ErrorCode " + mErrorCode;
    }
}
