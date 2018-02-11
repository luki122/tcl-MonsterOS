/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data;

public class SysApp implements DataItem {
    private String mApkPath;
    private long mTotalSize;


    private long getTotalSize() {
       return  mTotalSize;
    }

    @Override
    public void sendBackup() {

    }

    @Override
    public void backup() {

    }

    @Override
    public void restore() {

    }

    @Override
    public void receive() {

    }
}
