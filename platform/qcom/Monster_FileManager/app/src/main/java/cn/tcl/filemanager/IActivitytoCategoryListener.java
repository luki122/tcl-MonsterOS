/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager;

public interface IActivitytoCategoryListener {
    public void refreshCategory();
    public void onScannerStarted();
    public void onScannerFinished();
    public void disableCategoryEvent(boolean disable);
}
