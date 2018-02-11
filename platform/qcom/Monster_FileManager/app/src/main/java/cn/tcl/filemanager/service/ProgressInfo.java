/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import cn.tcl.filemanager.utils.FileInfo;

public class ProgressInfo {

    private String mUpdateInfo;
    private final int mProgress;
    private int mErrorCode;
    private final long mTotal;
    private final long mTotalSize;
    private final long mProgressSize;
    private final boolean mIsFailInfo;
    private FileInfo mFileInfo;

    public static final int M_MODE = 1;

    private int mUnitStyle = 0;

    public int getUnitStyle() {
        return mUnitStyle;
    }

    public void setUnitStyle(int style) {
        this.mUnitStyle = style;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param update    the string which will be shown on ProgressDialogFragment
     * @param progeress current progress number
     * @param total     total number
     */
    public ProgressInfo(String update, int progeress, long total) {
        mUpdateInfo = update;
        mProgress = progeress;
        mTotal = total;
        mProgressSize = 0;
        mTotalSize = 0;
        mIsFailInfo = false;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param update       the string which will be shown on ProgressDialogFragment
     * @param progeress    current progress number
     * @param total        total number
     * @param progressSize current progress size
     * @param totalSize    total size
     */
    public ProgressInfo(String update, int progeress, long total, long progressSize, long totalSize) {
        mUpdateInfo = update;
        mProgress = progeress;
        mTotal = total;
        mProgressSize = progressSize;
        mTotalSize = totalSize;
        mIsFailInfo = false;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param fileInfo  the fileInfo which will be associated with Dialog
     * @param progeress current progress number
     * @param total     total number
     */
    public ProgressInfo(FileInfo fileInfo, int progeress, long total) {
        mFileInfo = fileInfo;
        mProgress = progeress;
        mTotal = total;
        mProgressSize = 0;
        mTotalSize = 0;
        mIsFailInfo = false;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param errorCode  An int represents ERROR_CODE
     * @param isFailInfo status of task associated with certain progressDialog
     */
    public ProgressInfo(int errorCode, boolean isFailInfo) {
        mErrorCode = errorCode;
        mProgress = 0;
        mTotal = 0;
        mProgressSize = 0;
        mTotalSize = 0;
        mIsFailInfo = isFailInfo;
    }

    /**
     * This method gets status of task doing in background
     *
     * @return true for failed, false for no fail occurs in task
     */
    public boolean isFailInfo() {
        return mIsFailInfo;
    }

    /**
     * This method gets fileInfo, which will be updated on DetaiDialog
     *
     * @return fileInfo, which contains file's information(name, size, and so
     * on)
     */
    public FileInfo getFileInfo() {
        return mFileInfo;
    }

    /**
     * This method gets ERROR_CODE for certain task, which is doing in
     * background.
     *
     * @return ERROR_CODE for certain task
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * This method gets the content, which will be updated on ProgressDialog
     *
     * @return content, which need update
     */
    public String getUpdateInfo() {
        return mUpdateInfo;
    }

    /**
     * This method gets current progress number
     *
     * @return current progress number of progressDialog
     */
    public int getProgeress() {
        return mProgress;
    }

    /**
     * This method gets total number of progressDialog
     *
     * @return total number
     */
    public long getTotal() {
        return mTotal;
    }

    /**
     * This method gets current number of progressDialog
     *
     * @return current progress size
     */
    public long getProgressSize() {
        return mProgressSize;
    }

    /**
     * This method gets current number of progressDialog
     *
     * @return total size
     */
    public long getTotalSize() {
        return mTotalSize;
    }

}
