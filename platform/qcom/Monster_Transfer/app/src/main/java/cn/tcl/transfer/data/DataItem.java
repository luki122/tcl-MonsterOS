/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data;

public interface DataItem {

    public void backup();
    public void restore();
    public void sendBackup();
    public void receive();

}
