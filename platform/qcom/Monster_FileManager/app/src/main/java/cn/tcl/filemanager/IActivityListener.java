/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager;

import android.view.View;

import java.util.List;

import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.activity.FileBrowserActivity.HideInputMethodListener;

public interface IActivityListener {
    public void clickEditBtn();
    public void clickNewFolderBtn();
    public void clickSearchBtn();
    public void clickGlobalSearchBtn();
    public void clickPasteBtn();
    public void clickDelteBtn(int mode);
    public void clickCopyBtn();
    public void clickCutBtn();
    //public void clickFavoriteBtn();
    //public void clickFavoriteCancelBtn();
    public void clickShareBtn();
    public void clickRenameBtn(String searchMessage);
    public void clickDetailsBtn();
    public void clickSelectAllBtn();
    public void deleteFileInfo(FileInfo fileInfo);
    public void clickEncryptBtn();
    public void clickDecryptBtn();
    public void DecryptFileInfo();

    /**
     * @param isFromFilesCategory true is
     * {@link CategoryManager#CATEGORY_MUSIC}
     * {@link CategoryManager#CATEGORY_VEDIOS}
     * {@link CategoryManager#CATEGORY_PICTURES}
     * false is {@link CategoryManager#CATEGORY_DOCS}
     */
    public void clickPrivateBtn(boolean isFromFilesCategory);
    public void clickSelectDoneBtn();
    public void refreshAdapter(String path);
    public void onBackPressed();
    public void onGlobalSearchBackPressed();
    public void updateAdapter();
    public void clearAdapter();
    public void showNoSearchResults(boolean isShow, String args);
    public void showNoFolderResults(boolean isShow);
    //public boolean checkHasFavorite();
    public boolean checkIsSelectAll();
    public void unMountUpdate();
    public int getAdapterSize();
    public void showBeforeSearchList();
    //public void clickBarFavoriteBtn(FileInfo fileInfo);
    //public void clickBarFavoriteCancelBtn(FileInfo fileInfo);
    public void onConfiguarationChanged();
    public void onScannerStarted();
    public void onScannerFinished();
    public void closeItemMorePop();
    public boolean isItemMorePop();
    public void clearChecked();
    public void clearDecryptAndEncryptList();
    public void showNoFolderResultView(boolean b);
    public void switchToCopyView();
    public void clickMigrateBtn();
    public void clickShortcutBtn();
    public void clickShortcutToNormal();
//    public void showHideToolbar();
    public void updateActionMode(int mode);
    public int getActionMode();

    public void clickShiftOutBth(int mode,String mDesFolder);

    public void deleteSafeTempFile();

    public void clickDestorySafe(int mode,String mDesFolder);

    public List<FileInfo> getSafeBoxInfo(); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636


    public void setHideInputMethod(HideInputMethodListener hideInputMethodListener);
}
