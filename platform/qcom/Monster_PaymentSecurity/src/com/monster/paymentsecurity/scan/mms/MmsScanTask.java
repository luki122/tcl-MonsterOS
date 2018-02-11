package com.monster.paymentsecurity.scan.mms;

import android.content.Context;
import android.provider.Telephony;

import com.monster.paymentsecurity.scan.BaseScanTask;

import java.lang.ref.WeakReference;

/**
 * Created by logic on 16-11-21.
 */
public class MmsScanTask extends BaseScanTask {

    private final WeakReference<Context> weakContext;


    public MmsScanTask(Context context) {
        super();
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected boolean onPrepare() {
        Context context = weakContext.get();
        return context != null;
    }

    @Override
    protected Integer onStart() {
        Context context = weakContext.get();
        if (context == null)
            return 0;

        String pkgName = Telephony.Sms.getDefaultSmsPackage(context);
        if (!"com.android.mms".equals(pkgName))
            return 1;

        return 0;
    }

    @Override
    protected void onFinished() {

    }

    @Override
    protected void onCancel() {

    }

    @Override
    public @Priority int getPriority() {
        return PRIORITY_TWO;
    }

    @Override
    protected @ScanType int getScanType() {
        return SCAN_TYPE_SMS_APP;
    }
}
