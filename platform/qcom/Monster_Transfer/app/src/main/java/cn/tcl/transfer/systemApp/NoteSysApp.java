/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.os.Environment;

import java.io.DataOutputStream;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.util.FilePathUtils;

public class NoteSysApp extends SysBaseApp {

    public static final String NAME = "cn.tcl.note";
    private static final String DATA_CACHE_PATH = Environment.getExternalStorageDirectory() + "/" + "Note";

    public NoteSysApp() {
        super(NAME);
    }

    public NoteSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public ArrayList<String> getCreateSdcardData() {
        return FilePathUtils.getFilelistFromPath(DATA_CACHE_PATH);
    }

    @Override
    public long calculateCreatedDirSize() {
        long size = FilePathUtils.getDirSizeFromPath(DATA_CACHE_PATH);
        return size;
    }
}
