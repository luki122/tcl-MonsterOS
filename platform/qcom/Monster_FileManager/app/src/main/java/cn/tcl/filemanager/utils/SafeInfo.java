/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

/**
 * Created by user on 16-2-29.
 */
public class SafeInfo {

    private String storage_name;
    private String storage_info;
    private String safe_name;
    private String safe_info;
    private String safe_path;
    private long safe_ct;

    public void setSafe_info(String safe_info) {
        this.safe_info = safe_info;
    }

    public void setSafe_name(String safe_name) {
        this.safe_name = safe_name;
    }

    public void setStorage_name(String storage_name) {
        this.storage_name = storage_name;
    }

    public void setStorage_info(String storage_info) {
        this.storage_info = storage_info;
    }

    public void setSafe_ct(long safe_ct) {
        this.safe_ct = safe_ct;
    }

    public String getSafe_info() {
        return safe_info;
    }

    public String getStorage_info() {
        return storage_info;
    }

    public String getSafe_name() {
        return safe_name;
    }

    public String getStorage_name() {
        return storage_name;
    }

    public long getSafe_ct() {
        return safe_ct;
    }

    public void setSafe_path(String safe_path) {
        this.safe_path = safe_path;
    }

    public String getSafe_path() {
        return safe_path;
    }
}

