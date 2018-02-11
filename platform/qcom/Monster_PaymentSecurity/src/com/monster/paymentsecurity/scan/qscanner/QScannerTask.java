package com.monster.paymentsecurity.scan.qscanner;

import android.content.Context;

import com.monster.paymentsecurity.scan.BaseScanTask;

import java.lang.ref.WeakReference;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

/**
 * QScanner封装
 * Created by logic on 16-11-21.
 */
public abstract class QScannerTask extends BaseScanTask {

    final WeakReference<Context> weakContext;
    QScannerManagerV2 mQScannerMananger;

    QScannerTask(Context context){
        super();
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected boolean onPrepare() {
        Context context = weakContext.get();
        if (context == null) return false;
        mQScannerMananger = ManagerCreatorF.getManager(QScannerManagerV2.class);
        boolean ret  = mQScannerMananger.initScanner() == 0;
        if (!ret){
            mQScannerMananger.freeScanner();
            result.setErrCode(SCAN_ERRCODE_INIT_QSCANNER_ERROR);
        }
        return ret;
    }


    @Override
    protected void onFinished() {
        mQScannerMananger.freeScanner();
        mQScannerMananger = null;
    }

    @Override
    protected void onCancel() {
        mQScannerMananger.freeScanner();
        mQScannerMananger = null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_FOUR;
    }
}
