package com.monster.paymentsecurity.scan.system;

import android.content.Context;

import com.monster.paymentsecurity.scan.BaseScanTask;

import java.lang.ref.WeakReference;

/**
 * Created by logic on 16-11-21.
 */
public abstract class SystemScanTask<Result> extends BaseScanTask {

    final WeakReference<Context> weakContext;

    SystemScanTask(Context context){
        super();
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected boolean onPrepare() {
        Context context = weakContext.get();
        return context != null;
    }

    @Override
    protected void onCancel() {
        //nothing
    }

    @Override
    protected void onFinished() {
        //nothing
    }

    @Override
    public int getPriority() {
        return PRIORITY_THREE;
    }

}
