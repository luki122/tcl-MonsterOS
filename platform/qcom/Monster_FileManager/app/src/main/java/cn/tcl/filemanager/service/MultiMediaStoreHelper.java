/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.util.ArrayList;
import java.util.List;

import android.media.MediaScannerConnection.OnScanCompletedListener;

public abstract class MultiMediaStoreHelper {
    protected final List<String> mPathList = new ArrayList<String>();
    //private static final int NEED_UPDATE = 500;
    protected final MediaStoreHelper mMediaStoreHelper;

    public MultiMediaStoreHelper(MediaStoreHelper mediaStoreHelper) {
        if (mediaStoreHelper == null) {
            throw new IllegalArgumentException("mediaStoreHelper has not been initialized.");
        }
        mMediaStoreHelper = mediaStoreHelper;
    }

    public void addRecord(String path) {
        mPathList.add(path);
//        if (mPathList.size() > NEED_UPDATE) {
//            updateRecords();
//        }
    }

    public void updateRecords() {
        mPathList.clear();
    }

    //add by yane.wang@jrdcom.com 20150303 begin
    /**
     * Set dstfolder to scan with folder.
     *
     * @param dstFolder
     */
    public void setDstFolder(String dstFolder) {
        mMediaStoreHelper.setDstFolder(dstFolder);
    }
  //add by yane.wang@jrdcom.com 20150303 end

    public void updateRecords(OnScanCompletedListener listener) {
        mPathList.clear();
    }

    public static class CopyMediaStoreHelper extends MultiMediaStoreHelper {
        public CopyMediaStoreHelper(MediaStoreHelper mediaStoreHelper) {
            super(mediaStoreHelper);
        }

        @Override
        public void updateRecords() {
            mMediaStoreHelper.scanPathforMediaStore(mPathList);
            super.updateRecords();
        }

        @Override
        public void updateRecords(OnScanCompletedListener listener) {
            mMediaStoreHelper.scanPathforMediaStore(mPathList, listener);
            super.updateRecords(listener);
        }
    }

    public static class DeleteMediaStoreHelper extends MultiMediaStoreHelper {
        public DeleteMediaStoreHelper(MediaStoreHelper mediaStoreHelper) {
            super(mediaStoreHelper);
        }

        @Override
        public void updateRecords() {
            mMediaStoreHelper.deleteFileInMediaStore(mPathList);
            super.updateRecords();
        }
    }

}
