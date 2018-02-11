/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.util.Log;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;

import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;

public final class FileInfoComparator implements Comparator<FileInfo> {
    private static final String TAG = "FileInfoComparator";
    private static FileInfoComparator sInstance = new FileInfoComparator();

    public static final int DONT_SORT = -1;
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_TIME = 1;
    public static final int SORT_BY_TYPE = 2;
    public static final int SORT_BY_SIZE = 3;

    /**
     * Used for Hanzi
     */
    private RuleBasedCollator mCollator;

    private int mSortType = SORT_BY_TYPE;

    /**
     * Constructor for FileInfoComparator class.
     */
    private FileInfoComparator() {
    }

    /**
     * This method set the sort mode. 0 means by type, 1 means by name, 2 means
     * by size, 3 means by time.
     *
     * @param sort sort mode.
     */
    private void setSortType(int sort) {
//        mSortType = sort;
        if (mCollator == null) {
            mCollator = (RuleBasedCollator) Collator.getInstance(java.util.Locale.CHINA);
        }
        mSortType = sort;
    }

    /**
     * This method get instance of FileInfoComparator.
     *
     * @param sort sort mode.
     * @return a instance of FileInfoComparator.
     */
    public static FileInfoComparator getInstance(int sort) {
        sInstance.setSortType(sort);
        return sInstance;
    }

    /**
     * This method compares the files based on the order: category
     * folders->common folders->files
     *
     * @param op the first file
     * @param oq the second file
     * @return a negative integer, zero, or a positive integer as the first file
     * is smaller than, equal to, or greater than the second file,
     * ignoring case considerations.
     */
    @Override
    public int compare(FileInfo op, FileInfo oq) {
        // if only one is directory
        if (op == null || oq == null) {
            return -1;
        }
        boolean isOpDirectory = op.isDirectory();
        boolean isOqDirectory = oq.isDirectory();
        if (isOpDirectory ^ isOqDirectory) {
            // one is a folder and one is not a folder
            LogUtils.v(TAG, op.getFileName() + " vs " + oq.getFileName() + " result="
                    + (isOpDirectory ? -1 : 1));
            return isOpDirectory ? -1 : 1;
        }

        Log.i(TAG, "compare - type:" + mSortType);
        switch (mSortType) {
            case SORT_BY_TYPE:
                return sortByType(op, oq);
            case SORT_BY_NAME:
                return sortByName(op, oq);
            case SORT_BY_SIZE:
                return sortBySize(op, oq);
            case SORT_BY_TIME:
                return sortByTime(op, oq);
            default:
                return sortByName(op, oq);
        }
    }

    /**
     * This method compares the files based on their type
     *
     * @param op the first file
     * @param oq the second file
     * @return a negative integer, zero, or a positive integer as the first file
     * is smaller than, equal to, or greater than the second file,
     * ignoring case considerations.
     */
    private int sortByType(FileInfo op, FileInfo oq) {
        boolean isOpDirectory = op.isDirectory();
        boolean isOqDirectory = oq.isDirectory();
        if (isOpDirectory && isOqDirectory) {
            return sortByName(op, oq);//PR-1000427 Nicky Ni -001 201512012
        }
        if (!isOpDirectory && !isOqDirectory) {
            // both are not directory
            String opExtension = FileUtils.getFileExtension(op.getFileName());
            String oqExtension = FileUtils.getFileExtension(oq.getFileName());
            if (opExtension == null && oqExtension != null) {
                return 1;
            } else if (opExtension != null && oqExtension == null) {
                return -1;
            } else if (opExtension != null && oqExtension != null) {
                // get first and second file type,pictures, music, video, APK row in the second area
                String opMimeType = op.getShareMimeType();
                String oqMimeType = oq.getShareMimeType();
                if (oqMimeType.contains(FileInfo.MIME_HAED_IMAGE) && opMimeType.contains(FileInfo.MIME_HAED_IMAGE) ||
                        oqMimeType.contains(FileInfo.MIME_HEAD_AUDIO) && opMimeType.contains(FileInfo.MIME_HEAD_AUDIO) ||
                        oqMimeType.contains(FileInfo.MIME_HEAD_VIDEO) && opMimeType.contains(FileInfo.MIME_HEAD_VIDEO) ||
                        FileInfo.FILE_APK.equals(oqExtension) || FileInfo.FILE_APK.equals(opExtension)) {
                    int compares = opExtension.compareToIgnoreCase(oqExtension);
                    return compares;
                }
                return 0;
            }
        }
        return sortByName(op, oq);
    }

    /**
     * This method compares the files based on their names.
     *
     * @param op the first file
     * @param oq the second file
     * @return a negative integer, zero, or a positive integer as the first file
     * is smaller than, equal to, or greater than the second file,
     * ignoring case considerations.
     */
    private int sortByName(FileInfo op, FileInfo oq) {
        CollationKey c1 = mCollator.getCollationKey(op.getFileName());
        CollationKey c2 = mCollator.getCollationKey(oq.getFileName());
        return mCollator.compare(c1.getSourceString(), c2.getSourceString());
    }

    /**
     * This method compares the files based on their sizes
     *
     * @param op the first file
     * @param oq the second file
     * @return a negative integer, zero, or a positive integer as the first file
     * is smaller than, equal to, or greater than the second file,
     * ignoring case considerations.
     */
    private int sortBySize(FileInfo op, FileInfo oq) {
        if (!op.isDirectory() && !oq.isDirectory()) {
            long opSize = op.getFileSize();
            long oqSize = oq.getFileSize();
            if (opSize != oqSize) {
                return opSize > oqSize ? -1 : 1;
            }
        }
        return sortByName(op, oq);
    }

    /**
     * This method compares the files based on their modified time
     *
     * @param op the first file
     * @param oq the second file
     * @return a negative integer, zero, or a positive integer as the first file
     * is smaller than, equal to, or greater than the second file,
     * ignoring case considerations.
     */
    private int sortByTime(FileInfo op, FileInfo oq) {
        long opTime = op.getFileLastModifiedTime();
        long oqTime = oq.getFileLastModifiedTime();
        if (opTime != oqTime) {
            return opTime > oqTime ? -1 : 1;
        }
        return sortByName(op, oq);
    }
}
