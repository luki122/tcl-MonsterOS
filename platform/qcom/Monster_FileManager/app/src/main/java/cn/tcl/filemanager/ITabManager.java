/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager;

public interface ITabManager {
    public void refreshCategoryTab();
    public void refreshTab(String initFileInfo);
    public void showPrevNavigationView(String newPath);
    public void addTab(String text);
    public void updateNavigationBar(int id);
}
