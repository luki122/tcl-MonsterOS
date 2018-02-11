/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.app;

import java.io.DataOutputStream;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;

public class MeituApp extends BaseApp {

    private static final String NAME = "com.mt.mtxx.mtxx";

    public MeituApp() {
        super(NAME);
    }

    public MeituApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    public ArrayList<String> getCreateSdcardData() {
        return null;
    }
}
