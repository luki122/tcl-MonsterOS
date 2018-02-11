/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.app;

import android.os.Environment;

import java.io.DataOutputStream;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.util.FilePathUtils;

public class AmapApp extends BaseApp {

    private static final String NAME = "com.autonavi.minimap";

    public AmapApp() {
        super(NAME);
    }

    public AmapApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    public ArrayList<String> getCreateSdcardData() {
        ArrayList<String> pathList = new ArrayList<String>();
        String path = "/sdcard/autonavi";
        return FilePathUtils.getFilelistFromPath(path);
    }

    @Override
    public long calculateCreatedDirSize() {
        long size = FilePathUtils.getDirSizeFromPath(Environment.getExternalStorageDirectory() + "/" + "autonavi");
        return size;
    }
}
