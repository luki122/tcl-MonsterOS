/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.content.pm.ProviderInfo;
import android.os.Environment;

import java.io.DataOutputStream;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.util.FilePathUtils;

public class MeetingAssistantSysApp extends SysBaseApp {

    public static final String NAME = "cn.tcl.meetingassistant";

    private static final String DATA_CACHE_PATH = Environment.getExternalStorageDirectory() + "/" + "MeetingAssistant";

    public MeetingAssistantSysApp() {
        super(NAME);
    }

    public MeetingAssistantSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public ArrayList<String> getCreateSdcardData() {
        ArrayList<String> pathList = new ArrayList<String>();
        return FilePathUtils.getFilelistFromPath(DATA_CACHE_PATH);
    }

    @Override
    public long calculateCreatedDirSize() {
        long size = FilePathUtils.getDirSizeFromPath(DATA_CACHE_PATH);
        return size;
    }
}
