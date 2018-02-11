/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.telecom.Log;
import android.text.TextUtils;

import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;

public class FileInfoManager {

    public static final int PASTE_MODE_CUT = 1;
    public static final int PASTE_MODE_COPY = 2;
    public static final int PASTE_MODE_UNKOWN = 0;
    public static final int SHIFT_OUT_SOURCE_MODE = 0;
    public static final int SHIFT_OUT_TARGET_MODE = 1;
    public static final int PASTE_MODE_GOING = 0;
    public static final int PASTE_MODE_CANCEL = 1;
    public static final int DELETE_MODE_GOING = 0;
    public static final int DELETE_MODE_CANCEL = 1;

    private final List<FileInfo> mAddFilesInfoList = new CopyOnWriteArrayList<FileInfo>();
    private final List<FileInfo> mRemoveFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mPasteFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mShowFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mSearchFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mCategoryFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mAddHideFilesInfoList = new ArrayList<FileInfo>();
    private final List<File> mFailFiles = new ArrayList<>(); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329


    private FileInfo mSafeFileInfo;


    public List<FileInfo> mBeforeSearchList = new ArrayList<FileInfo>();
    private int mPasteOperation = PASTE_MODE_UNKOWN;
    private int mPasteStatus = PASTE_MODE_GOING;
    private int mDeleteStatus = DELETE_MODE_GOING;
    private String mLastAccessPath;
    protected long mModifiedTime = -1;
    public int mCurMode = 0;//add for PR972394 by yane.wang@jrdcom.com 20150410


    public FileInfoManager() { // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
    }

    public void clearAll() {
        mAddFilesInfoList.clear();
        mRemoveFilesInfoList.clear();
        mPasteFilesInfoList.clear();
        mShowFilesInfoList.clear();
        mSearchFilesInfoList.clear();
        mCategoryFilesInfoList.clear();
        mAddHideFilesInfoList.clear();
        mBeforeSearchList.clear();
        mFailFiles.clear();
        mSafeFileInfo = null;
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    public void clearShowFiles() {
        //LogUtils.d("solonen", "----- clearShowFiles()", new Exception()); //MODIFIED by wenjing.ni, 2016-04-13,BUG-1941073
        mShowFilesInfoList.clear();
        mCategoryFilesInfoList.clear();
    }
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/



    public  List<File> getFailFiles(){
        return  mFailFiles;
    }

    public  void addFailFiles(File file){
        mFailFiles.add(file);
    }
    public  void clearFailFiles(){
        mFailFiles.clear();
    }



    /**
     * This method updates mPasteFilesInfoList.
     *
     * @param pasteType previous operation before paste, copy or cut
     * @param fileInfos list of copied (or cut) files
     */
    public void savePasteList(int pasteType, List<FileInfo> fileInfos) {
        mPasteOperation = pasteType;
        mPasteFilesInfoList.clear();
        mPasteFilesInfoList.addAll(fileInfos);
    }

    /**
     * This method checks weather current path is modified.
     *
     * @param path certain path to be checked
     * @return true for modified, and false for not modified
     */
    public boolean isPathModified(String path) {
        if (!TextUtils.isEmpty(path) && !path.equals(mLastAccessPath)) {
            return true;
        }
        if (mLastAccessPath != null
                && mModifiedTime != (new File(mLastAccessPath)).lastModified()) {
            return true;
        }
        return false;
    }

    /**
     * This method gets a ArrayList of FileInfo with content of
     * mPasteFilesInfoList.
     *
     * @return list of files, which paste operation involve
     */
    public List<FileInfo> getPasteList() {
        return new ArrayList<FileInfo>(mPasteFilesInfoList);
    }

    /**
     * This method gets previous operation before paste, copy or cut
     *
     * @return copy or cut
     */
    public int getPasteType() {
        return mPasteOperation;
    }

    /**
     * This method gets previous paste status in pasting, copy or cut
     */
    public int getPasteStatus() {
        return mPasteStatus;
    }

    /**
     * This method gets previous delete status in deleting
     */
    public int getDeleteStatus() {
        return mDeleteStatus;
    }

    /**
     * This method sets previous paste status in pasting, copy or cut
     */
    public void setPasteStatus(int status) {
        mPasteStatus = status;
    }

    /**
     * This method sets previous delete status in deleting
     */
    public void setDeleteStatus(int status) {
        mDeleteStatus = status;
    }

    /**
     * This method add file to mAddFilesInfoList
     *
     * @param fileInfo information of certain file
     */
    public void addItem(FileInfo dest) {
        mAddFilesInfoList.add(dest);
    }


    /**
     * This method add file list to mAddFilesInfoList
     *
     * @param destList information of file list
     */
    public void addAllItem(List<FileInfo> destList) {
        mAddFilesInfoList.addAll(destList);
    }

    public void addItem(FileInfo dest, boolean firstItem) {
        if (firstItem && mAddFilesInfoList.size() > 0) {
            mAddFilesInfoList.clear();
            mAddFilesInfoList.add(dest);
        } else {
            mAddFilesInfoList.add(dest);
        }
    }

    public List<FileInfo> getAddFilesInfoList() {
        return mAddFilesInfoList;
    }

    public void addHideItem(FileInfo fileInfo) {
        mAddHideFilesInfoList.add(fileInfo);
    }

    public void addAllHideItem(List<FileInfo> fileList) {
        mAddHideFilesInfoList.addAll(fileList);
    }

    public void clearHideItem() {
        mAddHideFilesInfoList.clear();
    }

    public void removeHideItem(FileInfo fileInfo) {
        mAddHideFilesInfoList.remove(fileInfo);
    }

    public void removeHideItemList(List<FileInfo> fileInfoList) {
        mAddFilesInfoList.removeAll(fileInfoList);
    }

    public List<FileInfo> getHideItemList() {
        return mAddHideFilesInfoList;
    }

    /**
     * This method adds file to mRemoveFilesInfoList
     *
     * @param fileInfo information of certain file
     */
    public void removeItem(FileInfo fileInfo) {
        mRemoveFilesInfoList.add(fileInfo);
        mSearchFilesInfoList.remove(fileInfo);
        mCategoryFilesInfoList.remove(fileInfo);
        mBeforeSearchList.remove(fileInfo);
        mPasteFilesInfoList.remove(fileInfo);
    }

    /**
     * This method updates all file lists according to parameter path and
     * sortType, and called in onTaskResult() of HeavyOperationListener, which
     * corresponds to operations like delete, copyPaste, cutPaste and so on.
     *
     * @param currentPath current path
     * @param sortType    sort type, which determine files' list sequence
     */
    public void updateFileInfoList(String currentPath, int sortType) {
        LogUtils.d("niky", "updateFileInfoList(), currentPath=" + currentPath);
        try {
            mLastAccessPath = currentPath;
            if (mLastAccessPath != null) {
                mModifiedTime = (new File(mLastAccessPath)).lastModified();
            }
            int len = mAddFilesInfoList.size();
            for (int i = 0; i < len; i++) {
                FileInfo fileInfo = mAddFilesInfoList.get(i);
                if (fileInfo.getFileParentPath().equals(mLastAccessPath)) {
                    mShowFilesInfoList.add(fileInfo);
                }
            }

            // ADD START FOR PR568131 BY HONGBIN.CHEN 20150907
            for (int i = 0; i < mRemoveFilesInfoList.size(); i++) {
                mShowFilesInfoList.remove(mRemoveFilesInfoList.get(i));
            }
            // ADD END FOR PR568131 BY HONGBIN.CHEN 20150907

            mPasteFilesInfoList.removeAll(mRemoveFilesInfoList);
            mAddFilesInfoList.clear();
            mRemoveFilesInfoList.clear();
            sort(sortType);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method adds one file to mShowFilesInfoList, and called in
     * onTaskResult() of LightOperationListener, which corresponds to operations
     * like rename, createFolder and so on.
     *
     * @param path     current path
     * @param sortType sort type, which determine files' list sequence
     * @return information of file, which will be set selected after UI updated.
     * null if size of mAddFilesInfoList is zero
     */
    public FileInfo updateOneFileInfoList(String path, int sortType) {
        FileInfo fileInfo = null;
        mLastAccessPath = path;
        mModifiedTime = (new File(mLastAccessPath)).lastModified();

        if (mAddFilesInfoList.size() > 0) {
            fileInfo = mAddFilesInfoList.get(0);
            LogUtils.e("updateOneFileInfoList","LastAccessPath :" + mLastAccessPath + " fileInfoPath " + fileInfo.getFileParentPath());
            if (fileInfo.getFileParentPath().equals(mLastAccessPath)) {
                mShowFilesInfoList.add(fileInfo);
                LogUtils.e("updateOneFileInfoList","updateOneFileInfoList add showfilelist " + fileInfo.getFileName());
            }
        }
        LogUtils.e("updateOneFileInfoList","RemoveFileInfoList size " + mRemoveFilesInfoList.size());
        if (mRemoveFilesInfoList.size() > 0) LogUtils.e("updateOneFileInfoList","RemoveFileInfoList Name " + mRemoveFilesInfoList.get(0).getFileName());
        mShowFilesInfoList.removeAll(mRemoveFilesInfoList);
        mPasteFilesInfoList.removeAll(mRemoveFilesInfoList);
        mAddFilesInfoList.clear();
        mRemoveFilesInfoList.clear();
        sort(sortType);

        return fileInfo;
    }

    /**
     * This method adds one file to mShowFilesInfoList in the category list, and called in
     * onTaskResult() of LightOperationListener, which corresponds to operations
     * like rename.
     *
     * @param sortType sort type, which determine files' list sequence
     * @return information of file, which will be set selected after UI updated.
     * null if size of mAddFilesInfoList is zero
     */
    public FileInfo updateOneCategoryFileInfoList(int sortType) {
        FileInfo fileInfo = null;
        if (mAddFilesInfoList.size() > 0) {
            fileInfo = mAddFilesInfoList.get(0);
            mShowFilesInfoList.add(fileInfo);
        }
        mShowFilesInfoList.removeAll(mRemoveFilesInfoList);
        mPasteFilesInfoList.removeAll(mRemoveFilesInfoList);
        mAddFilesInfoList.clear();
        mRemoveFilesInfoList.clear();
        sort(sortType);

        return fileInfo;
    }

    /**
     * This method adds mAddFilesInfoList to loadFileInfoList
     *
     * @param path     the current path to list files
     * @param sortType sort type, which determine files' sequence
     * @param isSort sort or not
     */
    public void loadFileInfoList(String path, int sortType) {
        if (!TextUtils.isEmpty(path)) {
            mShowFilesInfoList.clear();
            mLastAccessPath = path;
            mModifiedTime = (new File(mLastAccessPath)).lastModified();
            for (FileInfo fileInfo : mAddFilesInfoList) {
                if (mLastAccessPath.equals(fileInfo.getFileParentPath())
                        || MountManager.getInstance().isMountPoint(
                        fileInfo.getFileAbsolutePath())) {
                    mShowFilesInfoList.add(fileInfo);
                }
            }
            mAddFilesInfoList.clear();
            if (!MountManager.getInstance().isRootPath(path)) {
                sort(sortType);
            }
        }
    }

    /**
     * This method adds mAddFilesInfoList to loadFileInfoList
     *
     * @param path     the current path to list files
     */
    public void loadFileInfoList(String path) {
        if (!TextUtils.isEmpty(path)) {
            mShowFilesInfoList.clear();
            mLastAccessPath = path;
            mModifiedTime = (new File(mLastAccessPath)).lastModified();
            for (FileInfo fileInfo : mAddFilesInfoList) {
                if (mLastAccessPath.equals(fileInfo.getFileParentPath())
                        || MountManager.getInstance().isMountPoint(
                        fileInfo.getFileAbsolutePath())) {
                    mShowFilesInfoList.add(fileInfo);
                }
            }
            mAddFilesInfoList.clear();
        }
    }

    /**
     * This method adds mAddFilesInfoList to loadFileInfoList
     *
     */
    public void loadAllFileInfoList() {
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mAddFilesInfoList);
        mAddFilesInfoList.clear();
    }

    /**
     * This method adds list to mAddFilesInfoList
     *
     * @param fileInfoList list of files
     */
    public void addItemList(List<FileInfo> fileInfoList) {
        mAddFilesInfoList.addAll(fileInfoList);
    }
    //[BUGFIX]-Mod-BEGIN by TSNJ,qinglian.zhang,09/02/2014,PR-777038

    /**
     * This method removes all item in mAddFilesInfoList
     */
    public void removeAllItem() {
        mAddFilesInfoList.clear();
    }
    //[BUGFIX]-Mod-END by TSNJ,qinglian.zhang

    /**
     * This method checks weather certain item is included in paste list
     *
     * @param currentItem certain item, which needs to be checked
     * @return status of weather the item is included in paste list
     */
    public boolean isPasteItem(FileInfo currentItem) {
        return mPasteFilesInfoList.contains(currentItem);
    }

    /**
     * This method gets count of files in PasteFileInfoList, which need to paste
     *
     * @return number of files, which need to be pasted
     */
    public int getPasteCount() {
        return mPasteFilesInfoList.size();
    }

    /**
     * This method clears pasteList, which stores files need to paste(after copy
     * , or cut)
     */
    public void clearPasteList() {
        if (null != mPasteFilesInfoList) {
            mPasteFilesInfoList.clear();
            mPasteOperation = PASTE_MODE_UNKOWN;
        }
    }

    /**
     * This method clears removeList, which stores files need to remove(after delete)
     */
    public void clearRemoveList() {
        mRemoveFilesInfoList.clear();
    }

    /**
     * This method gets file list for show
     *
     * @return file list for show
     */
    public List<FileInfo> getShowFileList() {
        return mShowFilesInfoList;
    }

    /**
     * This method saves file list before search operation
     */
    public void saveListBeforeSearch() {
        mBeforeSearchList.clear();
        mBeforeSearchList.addAll(mShowFilesInfoList);
    }

    /**
     * This method gets file list that saved before search
     *
     * @return file list for show
     */
    public List<FileInfo> getBeforeSearchList() {
        return mBeforeSearchList;
    }

    /**
     * This method sorts files with given sort type
     *
     * @param sortType sort type
     */
    public void sort(int sortType) {
        try {
            LogUtils.d(this.getClass().getName(), "CategoryManager.mCurrentCagegory:" + CategoryManager.mCurrentCagegory + ",sortType:" + sortType);
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                Collections.sort(mShowFilesInfoList, FileInfoComparator.getInstance(FileInfoComparator.SORT_BY_TIME));
            } else if (CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_RECENT &&
                    CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_DOWNLOAD &&
                    CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_PICTURES) {
                Collections.sort(mShowFilesInfoList, FileInfoComparator.getInstance(sortType));
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method updates search list, which stores search result
     *
     * @param sortType sort type
     */
    public void updateSearchList(int sortType) {
        mSearchFilesInfoList.clear();
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mAddFilesInfoList);
        mSearchFilesInfoList.addAll(mAddFilesInfoList);
        mAddFilesInfoList.clear();
        sort(sortType);
    }

    /**
     * This method gets the the list of search list
     *
     * @return
     */
    public List<FileInfo> getSearchFileList() {
        return mSearchFilesInfoList;
    }


    /**
     * This method gets the number of the search result
     *
     * @return the number of the search result
     */
    public int getSearchItemsCount() {
        return mSearchFilesInfoList.size();
    }

    /**
     * This method updates category list
     *
     * @param sortType
     */
    public void updateCategoryList(int sortType) {
        LogUtils.d("niky", ">>>>>>>>.updateCategoryList()");
        mLastAccessPath = null;
        mCategoryFilesInfoList.clear();
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mAddFilesInfoList);
        mCategoryFilesInfoList.addAll(mAddFilesInfoList);
        mAddFilesInfoList.clear();

        sort(sortType);
    }

    public void updatingCategoryList(int sortType) {
        mShowFilesInfoList.addAll(mAddFilesInfoList);
        mCategoryFilesInfoList.addAll(mAddFilesInfoList);
        mAddFilesInfoList.clear();
        sort(sortType);
    }

    /**
     * This method gets the list of category list
     *
     * @return
     */
    public List<FileInfo> getCategoryFileList() {
        return mCategoryFilesInfoList;
    }

    /**
     * This method show the category result list
     *
     * @param sortType sort type
     */
    public void showCategoryResultView(int sortType) {
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mCategoryFilesInfoList);
        sort(sortType);
    }

    /**
     * This method gets the number of the category list
     *
     * @return
     */
    public int getCategoryItemsCount() {
        return mCategoryFilesInfoList.size();
    }

    public FileInfo getSafeFileInfo() {
        return mSafeFileInfo;
    }

    public void setSafeFileInfo(FileInfo safeFileInfo) {
        this.mSafeFileInfo = safeFileInfo;
    }

}
