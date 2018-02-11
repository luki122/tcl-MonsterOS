/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.fasttransfer.sms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SmsService extends Service {
    public SmsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
